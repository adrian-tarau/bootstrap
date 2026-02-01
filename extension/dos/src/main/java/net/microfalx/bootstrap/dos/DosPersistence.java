package net.microfalx.bootstrap.dos;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.core.utils.IpWhoIs;
import net.microfalx.bootstrap.dos.jpa.*;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.threadpool.ThreadPool;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public class DosPersistence extends ApplicationContextSupport {

    private AuditRepository auditRepository;
    private RuleRepository ruleRepository;
    private RuleStatsRepository ruleStatsRepository;

    private ThreadPool threadPool = ThreadPool.get();

    void initialize(ApplicationContext applicationContext) {
        setApplicationContext(applicationContext);
        threadPool = getBean(ThreadPool.class);
        auditRepository = getBean(AuditRepository.class);
        ruleRepository = getBean(RuleRepository.class);
        ruleStatsRepository = getBean(RuleStatsRepository.class);
    }

    Collection<net.microfalx.bootstrap.dos.jpa.Rule> getRules() {
        return ruleRepository.findAll();
    }

    net.microfalx.bootstrap.dos.jpa.Rule resolve(Rule rule, boolean create) {
        Optional<net.microfalx.bootstrap.dos.jpa.Rule> jpaRule = ruleRepository.findByNaturalId(rule.getId());
        return jpaRule.orElseGet(() -> {
            if (create) {
                return ruleRepository.saveAndFlush(map(rule));
            } else {
                return null;
            }
        });
    }

    void updateStats(Rule rule, DosRegistry.AddressCounts counts) {
        RuleStats ruleStats = resolveRuleStats(resolve(rule, true));
        updateCounts(rule, ruleStats, counts);
        updateGeoLocation(rule, ruleStats);
        ruleStatsRepository.saveAndFlush(ruleStats);
    }

    void updateCounts(Rule rule, RuleStats ruleStats, DosRegistry.AddressCounts counts) {
        ruleStats.setRequestCount(ruleStats.getRequestCount() + counts.getAccessCount());
        ruleStats.setDenyCount(ruleStats.getDenyCount());
        ruleStats.setThrottleCount(ruleStats.getThrottleCount());
        ruleStats.setName(rule.getName());
        ruleStats.setDescription(rule.getDescription());
    }

    void updateGeoLocation(Rule rule, RuleStats ruleStats) {
        IpWhoIs lookup = IpWhoIs.lookup(rule.getAddress());
        ruleStats.setCountry(lookup.getCountry());
        ruleStats.setCountryCode(lookup.getCountryCode());
        ruleStats.setRegion(lookup.getRegion());
        ruleStats.setRegionCode(lookup.getRegionCode());
        ruleStats.setCity(lookup.getCity());
        ruleStats.setLatitude(lookup.getLatitude());
        ruleStats.setLongitude(lookup.getLongitude());
    }

    net.microfalx.bootstrap.dos.jpa.RuleStats resolveRuleStats(net.microfalx.bootstrap.dos.jpa.Rule rule) {
        Optional<RuleStats> ruleStats = ruleStatsRepository.findById(rule.getId());
        return ruleStats.orElseGet(() -> ruleStatsRepository.saveAndFlush(map(rule)));
    }

    void audit(Rule rule, Rule.Reason reason, Request request) {
        net.microfalx.bootstrap.dos.jpa.Rule model = resolve(rule, false);
        if (model == null) {
            new AuditTask(rule, request, reason, null).run();
        }
    }

    void audit(Rule rule, Request request, Rule.Reason reason, String description) {
        AuditTask task = new AuditTask(rule, request, reason, description);
        threadPool.execute(task);
    }

    private <M, ID> NaturalIdEntityUpdater<M, ID> getUpdater(Class<? extends NaturalJpaRepository<M, ID>> repositoryClass) {
        NaturalJpaRepository<M, ID> repository = getBean(repositoryClass);
        NaturalIdEntityUpdater<M, ID> updater = new NaturalIdEntityUpdater<>(getBean(MetadataService.class), repository);
        updater.setApplicationContext(getApplicationContext());
        return updater;
    }

    private net.microfalx.bootstrap.dos.jpa.Rule map(Rule rule) {
        net.microfalx.bootstrap.dos.jpa.Rule jpaRule = new net.microfalx.bootstrap.dos.jpa.Rule();
        jpaRule.setNaturalId(rule.getId());
        jpaRule.setName(rule.getName());
        jpaRule.setType(rule.getType());
        jpaRule.setActive(rule.isActive());
        jpaRule.setAction(rule.getAction());
        jpaRule.setAddress(rule.getAddress());
        jpaRule.setHostname(rule.getHostName());
        jpaRule.setRequestRate(rule.getRequestRate());
        jpaRule.setDescription(rule.getDescription());
        return jpaRule;
    }

    private net.microfalx.bootstrap.dos.jpa.RuleStats map(net.microfalx.bootstrap.dos.jpa.Rule rule) {
        net.microfalx.bootstrap.dos.jpa.RuleStats jpaRule = new net.microfalx.bootstrap.dos.jpa.RuleStats();
        jpaRule.setId(rule.getId());
        jpaRule.setName(rule.getName());
        return jpaRule;
    }

    private class AuditTask implements Runnable {

        private final Rule rule;
        private final Request request;
        private final Rule.Reason reason;
        private final String description;

        AuditTask(Rule rule, Request request, Rule.Reason reason, String description) {
            this.rule = rule;
            this.request = request;
            this.reason = reason;
            this.description = description;
        }

        @Override
        public void run() {
            Audit model = new Audit();
            model.setRule(resolve(rule, true));
            model.setUri(request.getUri().toASCIIString());
            model.setReason(reason);
            model.setCreatedAt(LocalDateTime.now());
            model.setDescription(description);
            auditRepository.save(model);
        }
    }
}
