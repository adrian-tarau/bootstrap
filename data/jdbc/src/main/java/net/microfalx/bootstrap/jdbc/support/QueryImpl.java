package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.NumberUtils;
import net.microfalx.lang.ObjectUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ClassUtils.isSubClassOf;

final class QueryImpl implements Query {

    private final Schema schema;
    private final String sql;
    private final JdbcClient client;
    private final JdbcClient.StatementSpec statementSpec;

    QueryImpl(Schema schema, String sql) {
        requireNonNull(schema);
        requireNotEmpty(sql);
        this.schema = schema;
        this.sql = sql;
        this.client = JdbcClient.create(schema.getDatabase().getDataSource().unwrap());
        this.statementSpec = client.sql(sql);
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public JdbcClient getClient() {
        return client;
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public int update() {
        return statementSpec.update();
    }

    @Override
    public Query parameters(Object... values) {
        for (int index = 0; index < values.length; index++) {
            parameter(index + 1, values[index]);
        }
        return this;
    }

    @Override
    public Query parameter(int index, Object value) {
        statementSpec.param(index, value);
        return this;
    }

    @Override
    public Query parameter(String name, Object value) {
        statementSpec.param(name, value);
        return this;
    }

    @Override
    public <T> T selectOne(ResultSetExtractor<T> extractor) {
        return statementSpec.query(extractor);
    }

    @Override
    public <T> T selectOne(RowMapper<T> mapper) {
        return statementSpec.query(mapper).single();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> T selectOne(Class<T> type) {
        requireNonNull(type);
        Class<?> finalType = isSubClassOf(type, Enum.class) ? String.class : type;
        Object value = statementSpec.query(finalType).single();
        if (isSubClassOf(type, Enum.class)) {
            return (T) EnumUtils.fromName((Class<Enum>) type, ObjectUtils.toString(value));
        } else {
            return (T) value;
        }
    }

    @Override
    public <T> T selectOne(Class<T> type, T defaultValue) {
        try {
            return (T) statementSpec.query(type).single();
        } catch (IncorrectResultSizeDataAccessException | IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public <T> List<T> selectMany(RowMapper<T> rowMapper) {
        return statementSpec.query(rowMapper).list();
    }

    @Override
    public int selectInt() {
        return NumberUtils.toNumber(selectOne(Integer.class), 0).intValue();
    }

    @Override
    public long selectLong() {
        return NumberUtils.toNumber(selectOne(Integer.class), 0).longValue();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Query.class.getSimpleName() + "[", "]")
                .add("schema=" + schema.getName())
                .add("sql='" + sql + "'")
                .toString();
    }
}
