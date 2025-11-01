package net.microfalx.bootstrap.jdbc.support.mysql;

import net.microfalx.bootstrap.jdbc.support.AbstractTable;
import net.microfalx.bootstrap.jdbc.support.Database;
import net.microfalx.bootstrap.jdbc.support.Schema;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlTable extends AbstractTable<MySqlTable> {

    public MySqlTable(Schema schema, String name) {
        super(schema, name);
    }

    @Override
    protected Columns loadColumns() {
        Database database = getSchema().getDatabase();
        return database.getClient().sql(GET_TABLE_STRUCTURE_SQL)
                .param(getSchema().getName())
                .param(getName())
                .query(new TableStructureResultSetExtractor(this));
    }

    @Override
    protected Indexes loadIndexes() {
        return null;
    }

    private static class TableStructureResultSetExtractor implements ResultSetExtractor<Columns> {

        private final MySqlTable table;

        TableStructureResultSetExtractor(MySqlTable table) {
            this.table = table;
        }

        @Override
        public Columns extractData(ResultSet rs) throws SQLException, DataAccessException {
            Schema schema = table.getSchema();
            Columns columns = new Columns();
            while (rs.next()) {
                MySqlColumn column = new MySqlColumn(table, rs.getString("column_name"))
                        .withJdbcType(schema.getJdbcType(rs.getString("data_type")))
                        .withNullable("YES".equalsIgnoreCase(rs.getString("is_nullable")))
                        .withLength(rs.getObject("character_maximum_length", Integer.class));
                columns.addColumn(column);
            }
            return columns;
        }
    }

    private static final String GET_INDEX_NAMES_SQL = """
            select constraint_name from information_schema.TABLE_CONSTRAINTS
            \twhere CONSTRAINT_SCHEMA = ? and table_name = ?""";
    private static final String GET_TABLE_STRUCTURE_SQL = """
            select column_name, data_type, is_nullable, character_maximum_length, numeric_precision, numeric_scale, column_key, ordinal_position
            \tfrom information_schema.COLUMNS
            \twhere table_schema = ? and table_name = ?
            \torder by ordinal_position""";
}
