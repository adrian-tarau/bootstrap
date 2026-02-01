package net.microfalx.bootstrap.dos.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("DosRuleStatsRepository")
@Transactional
public interface RuleStatsRepository extends JpaRepository<RuleStats, Integer> {
}
