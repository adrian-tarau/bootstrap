package net.microfalx.bootstrap.jdbc.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
@ContextConfiguration(classes = {TestEntityRepository.class})
@TestPropertySource(properties = "spring.flyway.enabled=false")
@AutoConfigureTestDatabase
public abstract class AbstractJpaTestCase {
}
