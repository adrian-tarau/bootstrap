package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.resource.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.containsWhiteSpacesOnly;

public abstract class AbstractScript implements Script {

    private final Schema schema;
    private final Resource resource;

    public AbstractScript(Schema schema, Resource resource) {
        requireNonNull(schema);
        requireNonNull(resource);
        this.schema = schema;
        this.resource = resource;
    }

    @Override
    public final Schema getSchema() {
        return schema;
    }

    @Override
    public final Resource getResource() {
        return resource;
    }

    @Override
    public final Collection<Query> getQueries() {
        try {
            if (!resource.exists()) {
                throw new ScriptException("Script " + resource + " does not exist");
            }
            return parseStatements(readLines(resource.getReader()));
        } catch (IOException e) {
            throw new ScriptException("Cannot read script " + resource, e);
        }
    }

    private List<String> readLines(Reader reader) {
        List<String> lines = new ArrayList<>();
        String line;
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new ScriptException("Cannot read script " + resource, e);
        }
        return lines;
    }

    private Collection<Query> parseStatements(List<String> lines) throws IOException {
        Collection<Query> statements = new ArrayList<>();
        boolean inMultilineComment = false;
        StringBuilder builder = new StringBuilder();
        int lineNumber = 0;
        for (String line : lines) {
            ++lineNumber;
            // Skip empty line between statements.
            if (!inMultilineComment && containsWhiteSpacesOnly(line)) continue;
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith("/*")) {
                inMultilineComment = true;
            } else if (inMultilineComment) {
                if (trimmedLine.endsWith("*/")) inMultilineComment = false;
            } else {
                if (!builder.isEmpty()) builder.append('\n');
                builder.append(line);
                if (isEndOfStatement(line)) createStatement(statements, builder);
            }
        }
        return statements;
    }

    private void createStatement(Collection<Query> statements, StringBuilder builder) {
        Query statement = Query.create(schema, builder.toString());
        statements.add(statement);
        builder.setLength(0);
    }

    private boolean isEndOfStatement(String line) {
        String trimmedLine = line.trim();
        return trimmedLine.endsWith(";");
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Script.class.getSimpleName() + "[", "]")
                .add("schema=" + schema)
                .add("resource=" + resource)
                .toString();
    }
}
