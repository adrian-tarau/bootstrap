package net.microfalx.bootstrap.jdbc.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration("bootstrap-jpa")
@EnableJpaRepositories("net.microfalx.bootstrap")
@EntityScan("net.microfalx.bootstrap")
@EnableTransactionManagement
public class JpaConfig {
}
