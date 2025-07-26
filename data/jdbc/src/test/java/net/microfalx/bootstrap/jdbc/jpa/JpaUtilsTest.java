package net.microfalx.bootstrap.jdbc.jpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertSame;

class JpaUtilsTest extends AbstractJpaTestCase {

    @Autowired
    private TestEntityRepository repository;

    @Test
    void getEntityType() {
        assertSame(TestEntity.class, JpaUtils.getEntityType(repository));
    }
}