package net.microfalx.bootstrap.jdbc.migration;

import net.microfalx.bootstrap.jdbc.support.DataSource;
import net.microfalx.bootstrap.jdbc.support.Query;
import net.microfalx.resource.ClassPathResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionTest {

    private final Collection<String> sqls = new ArrayList<>();

    @Mock private DataSource dataSource;
    @Mock private javax.sql.DataSource jdbcDataSource;
    @Mock private Connection connection;
    @Mock private Statement statement;
    @Mock private DatabaseMetaData metaData;

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.unwrap()).thenReturn(jdbcDataSource);
        when(dataSource.getId()).thenReturn("mysql");
        when(dataSource.getName()).thenReturn("MySQL");
        when(dataSource.getUri()).thenReturn(URI.create("jdbc:mysql://localhost:3306/demo"));
        when(dataSource.getConnection()).thenReturn(connection);
        when(jdbcDataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            return Mockito.mock(PreparedStatement.class, new PreparedStatementAnswer(invocation.getArgument(0)));
        });
        when(metaData.getDatabaseProductName()).thenReturn("mysql");
    }

    @Test
    void schema1() {
        Session session = new Session(dataSource, ClassPathResource.file("schema1.xml"));
        session.setExecutor(new ExecutorImpl());
        session.execute();
        assertEquals(Session.Status.SUCCESSFUL, session.getStatus());
        assertEquals(0, session.getScriptCount());
        assertEquals(0, session.getStatementCount());
        assertEquals(0, session.getFailedStatementCount());
        assertEquals(0, sqls.size());
    }

    private class ExecutorImpl implements Executor {

        @Override
        public void execute(Query query) {
            sqls.add(query.getSql());
        }
    }

    private static class ResultSetAnswer implements Answer<Object> {

        private final List<Object[]> rows;
        private int index = -1;

        public ResultSetAnswer(List<Object[]> rows) {
            this.rows = rows;
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            String name = invocation.getMethod().getName();
            if (name.equals("next")) {
                return ++index < rows.size();
            } else if (name.equals("getMetaData")) {
                return Mockito.mock(ResultSetMetaData.class, this);
            } else if (name.equals("getColumnCount")) {
                return rows.isEmpty() ? 0 : rows.getFirst().length;
            } else if (name.equals("close")) {
                return null;
            } else {
                if (name.startsWith("get")) {
                    Object position = invocation.getArgument(0);
                    if (position instanceof Integer) {
                        return rows.get(index)[(Integer) position - 1];
                    } else {
                        throw new IllegalArgumentException("Unsupported get method argument: " + position);
                    }
                }
                throw new IllegalArgumentException("Unknown answer type: " + name);
            }
        }
    }

    private static class PreparedStatementAnswer implements Answer<Object> {

        private final String sql;

        public PreparedStatementAnswer(String sql) {
            this.sql = sql;
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            String name = invocation.getMethod().getName();
            if (name.equals("execute") || name.equals("executeUpdate")) {
                return null;
            } else if (name.equals("close")) {
                return null;
            } else if (name.equals("executeQuery")) {
                return getResultSet();
            } else {
                if (name.startsWith("set")) {
                    return null;
                } else {
                    throw new IllegalArgumentException("Unknown answer type: " + name);
                }
            }
        }

        private ResultSet getResultSet() throws SQLException {
            List<Object[]> rows = new ArrayList<>();
            if (sql.contains("DATABASE()")) {
                rows.add(new Object[]{"test"});
            } else {
                throw new IllegalArgumentException("Unknown answer type: " + sql);
            }
            return Mockito.mock(ResultSet.class, new ResultSetAnswer(rows));
        }
    }

}