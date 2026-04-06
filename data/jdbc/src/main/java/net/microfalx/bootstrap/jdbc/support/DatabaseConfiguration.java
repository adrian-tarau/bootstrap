package net.microfalx.bootstrap.jdbc.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseConfiguration {

    @Bean
    public Database database(DatabaseService databaseService) {
        return databaseService.getDefaultDatabase();
    }

    @Bean
    public QueryProvider queryProvider(Database database) {
        return new QueryProviderImpl(database);
    }
}
