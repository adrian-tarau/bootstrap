package net.microfalx.bootstrap.jdbc.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfiguration {

    @Bean
    public Database database(DatabaseService databaseService) {
        return databaseService.getDefaultDatabase();
    }
    
    @Bean
    public QueryProvider query(Database database) {
        return new QueryProviderImpl(database);
    }
}
