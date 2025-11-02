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
        LOGGER.info("Discovered {} schema descriptors, in {} modules", definitions.size(), modules.size());
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

    private void execute(Query query) {
        statementCount++;
        try {
            executor.execute(query);
        } catch (Exception e) {
            throwable = e;
            failedStatementCount++;
            status = Status.FAILED;
            logWarn("Migration failed while executing:\nStatement:\n" + insertSpacesWithBlock(query.getSql(), 5)
                    + "\nStack Trace:\n" + insertSpacesWithBlock(getRootCauseMessage(e), 5));
        }
        if (status == Status.FAILED && failOnError) {
            throw new MigrationException("Migration failed while executing definition " + currentDefinition.getName(), throwable);
        }
    }

    private void logInfo(String message) {
        LOGGER.debug(message);
        scriptLogger.append(message).append("\n");
    }

    private void logWarn(String message) {
        LOGGER.warn(message);
        scriptLogger.append(message).append("\n");
    }

    private void logError(String message) {
        LOGGER.error(message);
        scriptLogger.append(message).append("\n");
    }

    private void executeDefinition(Definition definition) {
        if (!wasApplied(definition)) {
            executeDefinitionScript(definition);
        } else if (getStatus(definition.getId()) == Status.SUCCESSFUL) {
            executeMigrations(definition);
        }
        if (!logger.isEmpty()) logger.append('\n');
        logger.append(scriptLogger);
    }

    private void executeDefinitionScript(Definition definition) {
        currentDefinition = definition;
        logger.append("Executing definition ").append(definition.getName()).append(" from module ").append(definition.getModule()).append('\n');
        Script script = getScript();
        scriptCount++;
        executeScript(script);
        applyMigrations(definition);
    }

    private void executeMigrations(Definition definition) {
        for (Migration migration : definition.getMigrations()) {
            executeMigration(definition, migration);
            scriptCount++;
        }
        currentMigration = null;
    }

    private void applyMigrations(Definition definition) {
        for (Migration migration : definition.getMigrations()) {
            currentMigration = migration;
            updateRegistry(Status.APPLIED, Duration.ZERO);
            scriptCount++;
        }
        currentMigration = null;
    }

    private void executeMigration(Definition definition, Migration migration) {
        currentMigration = migration;
        Script script = getScript();
        executeScript(script);
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
        logInfo("Executing script '" + script.getResource().getFileName() + "')");
        status = Status.SUCCESSFUL;
        long startTime = currentTimeMillis();
        for (Query query : script.getQueries()) {
            execute(query);
            if (status == Status.FAILED) break;
        }
        if (!logger.isEmpty()) logger.append('\n');
        logger.append(scriptLogger);
        updateRegistry(status, ofMillis(currentTimeMillis() - startTime));
        scriptLogger.setLength(0);
    }

    public enum Status {
        NA, SUCCESSFUL, FAILED, APPLIED
    }

    private static class ExecutorImpl implements Executor {

        @Override
        public void execute(Query query) {
            query.update();
        }

    }


}
