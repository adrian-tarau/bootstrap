package net.microfalx.bootstrap.jdbc.support.mysql;

import net.microfalx.bootstrap.jdbc.support.*;
import net.microfalx.resource.Resource;

import java.util.Set;

public class MySqlSchema extends AbstractSchema {

    public MySqlSchema(Database database) {
        super(database);
    }

    @Override
    protected Set<String> doGetTableNames() {
        return getDatabase().getClient().sql(GET_TABLE_NAMES_SQL)
                .param(getName()).query(String.class).set();
    }

    @Override
    protected Set<String> doGetIndexNames() {
        return getDatabase().getClient().sql(GET_INDEX_NAMES_SQL)
                .param(getName()).query(String.class).set();
    }

    @Override
    protected Table<?> doGetTable(String name) {
        return new MySqlTable(MySqlSchema.this, name);
    }

    @Override
    protected Index<?> doGetIndex(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Script createScript(Resource resource) {
        return new MySqlScript(this, resource);
    }

    private static final String GET_TABLE_NAMES_SQL = "select table_name from information_schema.tables where table_schema = ?";
    private static final String GET_INDEX_NAMES_SQL = """
            select constraint_name from information_schema.TABLE_CONSTRAINTS
            \twhere CONSTRAINT_SCHEMA = ? and table_name = ?""";
}
