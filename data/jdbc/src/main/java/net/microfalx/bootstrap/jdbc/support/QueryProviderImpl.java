package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.resource.Resource;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.addStartSlash;

class QueryProviderImpl implements QueryProvider {

    private final Database database;
    private final Schema schema;

    public QueryProviderImpl(Database database) {
        requireNonNull(database);
        this.database = database;
        this.schema = database.getSchema();
    }

    @Override
    public Query withResource(String resource) {
        return null;
    }

    @Override
    public Query withSql(String sql) {
        return Query.create(schema, sql);
    }

    private Resource getResource(String path) {
        return schema.getResource("queries" + addStartSlash(path));
    }
}
