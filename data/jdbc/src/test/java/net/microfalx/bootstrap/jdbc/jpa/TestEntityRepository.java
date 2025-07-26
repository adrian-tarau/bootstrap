package net.microfalx.bootstrap.jdbc.jpa;

import org.springframework.stereotype.Repository;

@Repository
public interface TestEntityRepository extends NaturalJpaRepository<TestEntity, Integer> {
}
