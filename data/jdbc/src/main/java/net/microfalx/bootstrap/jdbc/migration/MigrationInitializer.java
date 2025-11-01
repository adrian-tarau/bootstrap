package net.microfalx.bootstrap.jdbc.migration;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class MigrationInitializer implements InitializingBean {

    private final DataSource dataSource;
    private final MigrationProperties properties;

    public MigrationInitializer(DataSource dataSource, MigrationProperties properties) {
        this.dataSource = dataSource;
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Session session = new Session(net.microfalx.bootstrap.jdbc.support.DataSource.create("main", "Main", dataSource));
        session.execute();
    }
}
