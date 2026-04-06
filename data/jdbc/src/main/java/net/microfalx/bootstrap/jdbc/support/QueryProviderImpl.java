package net.microfalx.bootstrap.jdbc.support;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class QueryProviderImpl implements QueryProvider {

    private final Database database;
    private final Schema schema;

    public QueryProviderImpl(Database database) {
        requireNonNull(database);
        this.database = database;
        this.schema = database.getSchema();
    }

    @Override
    public Query withResource(String path) {
        return this.schema.getQuery(path);
    }

    @Override
    public Query withSql(String sql) {
        return Query.create(schema, sql);
    }

    @Override
    public String toString() {
        return "QueryProviderImpl{" +
                "schema=" + schema.getName() +
                ", database=" + database.getType() +
                '}';
    }
}
