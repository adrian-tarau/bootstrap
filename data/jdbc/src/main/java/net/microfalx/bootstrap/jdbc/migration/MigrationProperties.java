package net.microfalx.bootstrap.jdbc.migration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("bootstrap.jdbc.migration")
@Configuration
@Getter
@Setter
@ToString
public class MigrationProperties {


    /**
     * Returns whether to enable the database migration.
     */
    private boolean enabled = true;

    /**
     * Name of the schema history table that will be used by Flyway.
     */
    private String table = "database_migration";
}
