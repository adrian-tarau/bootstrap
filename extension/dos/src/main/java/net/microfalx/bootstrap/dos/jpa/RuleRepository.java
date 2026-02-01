package net.microfalx.bootstrap.dos.jpa;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("DosRuleRepository")
@Transactional
public interface RuleRepository extends NaturalJpaRepository<Rule, Integer>, JpaSpecificationExecutor<Rule> {
}
