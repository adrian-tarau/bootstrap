package net.microfalx.bootstrap.jdbc.migration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.jdbc.support.*;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.Identifiable;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;

import static java.lang.System.currentTimeMillis;
import static java.time.Duration.ofMillis;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;
import static net.microfalx.lang.FormatterUtils.formatDuration;
import static net.microfalx.lang.StringUtils.addStartSlash;
import static net.microfalx.lang.TextUtils.insertSpacesWithBlock;

/**
 * Represents a database migration session.
 */
@Slf4j
public final class Session implements Identifiable<String> {

    private final String id = java.util.UUID.randomUUID().toString();
    private final DataSource dataSource;
    private final Resource resource;
    private final StringBuilder logger = new StringBuilder();
    private final StringBuilder scriptLogger = new StringBuilder();
    private long startTime;
    private boolean failOnError = true;
    private int scriptCount;
    private int statementCount;
    private int failedStatementCount;
    private Database database;
    private Schema schema;
    private Collection<Module> modules;
    private Collection<Definition> definitions;
    private Definition currentDefinition;
    private Migration currentMigration;
    private Status status = Status.NA;
    @Setter
    @Getter
    private Executor executor = new ExecutorImpl();
    private Throwable throwable;

    public Session(DataSource dataSource) {
        requireNonNull(dataSource);
        this.dataSource = dataSource;
        this.resource = null;
    }

    public Session(DataSource dataSource, Resource resource) {
        requireNonNull(dataSource);
        requireNonNull(dataSource);
        this.dataSource = dataSource;
        this.resource = resource;
    }

    @Override
    public String getId() {
        return id;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    /**
     * Returns whether the migration should fail on any error.
     *
     * @param failOnError {@code true} to fail on any error, {@code false} to continue
     * @return self
     */
    public Session setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
        return this;
    }

    /**
     * Returns the number of executed scripts.
     *
     * @return a positive number
     */
    public int getScriptCount() {
        return scriptCount;
    }

    /**
     * Returns the number of executed statements.
     *
     * @return a positive number
     */
    public int getStatementCount() {
        return statementCount;
    }

    /**
     * Returns the number of failed statements.
     *
     * @return a positive number
     */
    public int getFailedStatementCount() {
        return failedStatementCount;
    }

    public Status getStatus() {
        return failedStatementCount > 0 ? Status.FAILED : Status.SUCCESSFUL;
    }

    /**
     * Executes the database migration
     */
    public void execute() {
        initialize();
        initMigrationTables();
        loadDefinitions();
        doExecute();
    }

    private void initialize() {
        startTime = currentTimeMillis();
        database = Database.create(dataSource);
        schema = database.getSchema();
    }

    private Duration getDuration() {
        return Duration.ofMillis(currentTimeMillis() - startTime);
    }

    private void initMigrationTables() {
        Query query = database.getSchema().getQuery("migration.schema.sql");
        query.update();
    }

    private void loadDefinitions() {
        DefinitionLoader loader = new DefinitionLoader();
        if (resource != null) {
            loader.load(resource);
        } else {
            loader.load();
        }
        modules = loader.getModules();
        definitions = loader.getDefinitions();
    }

    private void doExecute() {
        long migrations = definitions.stream().mapToLong(definition -> definition.getMigrations().size()).sum();
        LOGGER.info("Discovered {} schema descriptors (with {} migrations), in {} modules", definitions.size(), migrations, modules.size());
        for (Definition definition : definitions) {
            executeDefinition(definition);
        }
        if (statementCount > 0) {
            LOGGER.info("Database migration completed in {}, {} scripts executed, {} statements executed",
                    formatDuration(getDuration()), scriptCount, statementCount);
        } else {
            LOGGER.info("No migration required, database is up-to-date");
        }
    }

    private Status execute(Query query) {
        statementCount++;
        try {
            executor.execute(query);
            return Status.SUCCESSFUL;
        } catch (Exception e) {
            throwable = e;
            failedStatementCount++;
            status = Status.FAILED;
            logWarn("Migration failed while executing:\nStatement:\n" + insertSpacesWithBlock(query.getSql(), 5)
                    + "\nRoot Cause:\n" + insertSpacesWithBlock(getRootCauseMessage(e), 5));
            return Status.FAILED;
        }
    }

    private void logInfo(String message) {
        LOGGER.debug(message);
        scriptLogger.append(message).append("\n");
    }

    private void logWarn(String message) {
        if (failOnError) {
            LOGGER.debug(message);
        } else {
            LOGGER.warn(message);
        }
        scriptLogger.append(message).append("\n");
    }

    private void executeDefinition(Definition definition) {
        currentDefinition = definition;
        if (!wasApplied(definition)) {
            executeDefinitionScript(definition);
        } else {
            executeMigrations(definition);
        }
        if (!scriptLogger.isEmpty()) {
            logger.append(scriptLogger);
            scriptLogger.setLength(0);
        }
        if (status == Status.FAILED && failOnError) {
            throw new MigrationException("Migration failed during definition '" + currentDefinition.getName()
                    + "' from module '" + currentDefinition.getModule().getName() + "', log:\n" + insertSpacesWithBlock(logger.toString(), 5));
        }
        currentDefinition = null;
    }

    private void executeDefinitionScript(Definition definition) {
        logInfo("Executing definition '" + definition.getName() + "' from module '" + definition.getModule() + "'");
        Script script = getScript();
        executeScript(script);
        applyMigrations(definition);
    }

    private void executeMigrations(Definition definition) {
        logInfo("Apply migrations for '" + definition.getName() + "' from module '" + definition.getModule().getName() + "'");
        for (Migration migration : definition.getMigrations()) {
            Status status = getStatus(migration.getId());
            if (status.shouldSkip()) continue;
            executeMigration(migration);
        }
        currentMigration = null;
    }

    private void applyMigrations(Definition definition) {
        for (Migration migration : definition.getMigrations()) {
            currentMigration = migration;
            updateRegistry(Status.APPLIED, Duration.ZERO);
        }
        currentMigration = null;
    }

    private void executeMigration(Migration migration) {
        currentMigration = migration;
        Script script = getScript();
        boolean valid = migration.getCondition().evaluate(schema);
        if (!valid) executeScript(script);
    }

    private void updateRegistry(Status status, Duration duration) {
        String id;
        String name = currentDefinition.getName();
        Module module = currentDefinition.getModule();
        String path;
        String checksum = getChecksum();
        if (currentMigration != null) {
            id = currentMigration.getId();
            path = currentMigration.getPath();
        } else {
            id = currentDefinition.getId();
            path = currentDefinition.getPath();
        }
        Query query = schema.getQuery("migration.update.sql").parameters(id, name, module.getName(), path,
                LocalDateTime.now(), duration.toMillis(), status.name(), checksum, scriptLogger.toString());
        query.update();
    }

    private Script getScript() {
        if (currentMigration != null) {
            return schema.getScript("migration" + addStartSlash(currentMigration.getPath()));
        } else {
            return schema.getScript("schema" + addStartSlash(currentDefinition.getPath()));
        }
    }

    private String getChecksum() {
        Script script = getScript();
        try {
            return Hashing.create().update(script.getResource().loadAsString()).asString();
        } catch (IOException e) {
            throw new MigrationException("Failed to load checksum for script " + script.getResource().getPath(), e);
        }
    }

    private boolean wasApplied(Definition definition) {
        int countExists = 0;
        for (String tableName : definition.getTables()) {
            Table<?> table = schema.getTable(tableName);
            if (table.exists()) countExists++;
        }
        return countExists > 0;
    }

    private Status getStatus(String id) {
        Query query = schema.getQuery("migration.get_status.sql").parameter(1, id);
        return query.selectOne(Status.class, Status.NA);
    }

    private void executeScript(Script script) {
        logInfo("Executing script '" + script.getResource().getFileName() + "'");
        Status status = Status.SUCCESSFUL;
        long startTime = currentTimeMillis();
        for (Query query : script.getQueries()) {
            status = execute(query);
            if (status == Status.FAILED) break;
        }
        scriptCount++;
        if (!scriptLogger.isEmpty()) logger.append(scriptLogger);
        updateRegistry(status, ofMillis(currentTimeMillis() - startTime));
        scriptLogger.setLength(0);
        if (this.status == Status.NA) {
            this.status = status;
        } else if (status.ordinal() >= this.status.ordinal()) {
            this.status = status;
        }
    }

    public enum Status {
        NA, SUCCESSFUL, FAILED, APPLIED;

        public boolean shouldSkip() {
            return this == SUCCESSFUL || this == APPLIED;
        }
    }

    private static class ExecutorImpl implements Executor {

        @Override
        public void execute(Query query) {
            query.update();
        }

    }


}
