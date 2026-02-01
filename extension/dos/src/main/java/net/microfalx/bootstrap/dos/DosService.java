package net.microfalx.bootstrap.dos;


import inet.ipaddr.HostName;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.bootstrap.core.utils.CachedAddress;
import net.microfalx.threadpool.ThreadPool;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FormatterUtils.formatDuration;

/**
 * A service which provides protection against DoS attacks.
 * <p>
 * The service can validate whether an IP should be blocked by comparing the IP against DoS rules with {@link #validate(Request)}
 * <p>
 * The service can also track & discover DoS rules using {@link #register(Request)}. Whenever an IP seems to trip defined thresholds (separate thresholds
 * for good and bad requests), it is registered in the DoS rules. The rule is discovered once and after that the rule can be managed from UI (the IP
 * can be allowed to execute requests).
 */
@Slf4j
@Service
public class DosService implements InitializingBean {

    @Autowired private ApplicationContext applicationContext;
    @Autowired(required = false) private DosProperties properties = new DosProperties();
    private final DosPersistence persistence = new DosPersistence();
    private final DosRegistry registry = new DosRegistry();
    private ThreadPool threadPool;

    /**
     * Returns whether the DoS service is enabled.
     * <p>
     * When not enabled, it will still track but not interfere with request interception.
     *
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean isEnabled() {
        return properties.isEnabled();
    }

    /**
     * Returns registered rules.
     *
     * @return a non-null instance
     */
    public Collection<Rule> getRules() {
        return registry.getRules();
    }

    /**
     * Returns the counts for all tracked requests.
     *
     * @return a non-null instance
     */
    public Collection<RequestCounts> getRequestCounts() {
        return registry.getRequestCounts();
    }

    /**
     * Returns the rule for a given IP or hostname.
     *
     * @param ipOrHost the IP or hostname
     * @return the rule, null if a rule does not exist
     */
    public Rule findRule(String ipOrHost) {
        requireNonNull(ipOrHost);
        CachedAddress address = registry.resolve(ipOrHost);
        if (!address.isResolved()) return null;
        Rule rule = registry.findRule(address.getIp());
        if (rule == null) rule = registry.findRuleWithCidr(address.getIp());
        return rule;
    }

    /**
     * Returns the action taken against an IP or hostname based on current rules.
     *
     * @param ipOrHost the IP or hostname
     * @return a non-null instance
     */
    public Rule.Action getAction(String ipOrHost) {
        requireNonNull(ipOrHost);
        CachedAddress address = registry.resolve(ipOrHost);
        if (!address.isResolved()) return Rule.Action.DENY;
        DosRegistry.ActionCache action = registry.getAction(address.getIp());
        if (action == null || action.isExpired()) {
            return Rule.Action.ALLOW;
        } else {
            return action.getAction();
        }
    }

    /**
     * Registers a new rule.
     *
     * @param rule the rule
     */
    public void register(Rule rule) {
        requireNonNull(rule);
        LOGGER.info("Register DoS rule {}", rule.toDescription());
        doRegister(rule);
    }

    /**
     * Registers a request to be tracked and identifies against DoS thresholds.
     *
     * @param request the request
     */
    public void register(Request request) {
        requireNonNull(request);
        registry.register(request);
    }

    /**
     * Returns the (canonical) hostname of a given IP.
     *
     * @param ip the IP (or hostname)
     * @return the hostname or the IP if the hostname cannot be resolved
     */
    public String resolveIp(String ip) {
        requireNonNull(ip);
        return registry.resolve(ip).getHostname();
    }

    /**
     * Triggers reloading of rules from persistence.
     */
    public void reload() {
        registry.loadRulesAsync();
    }

    /**
     * Returns the action to be performed for the IP or hostname.
     *
     * @param request an request
     * @return a non-null instance
     */
    public Rule.Action validate(Request request) {
        requireNonNull(request);
        registry.checkReload();
        if (!isEnabled()) {
            registry.incrementCount(request.getAddress(), Rule.Action.ALLOW);
            return Rule.Action.ALLOW;
        }
        CachedAddress address = registry.resolve(request.getAddress());
        if (!address.isResolved()) {
            registry.incrementCount(request.getAddress(), Rule.Action.DENY);
            return Rule.Action.DENY;
        }
        return registry.validate(request, address);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initThreadPool();
        persistence.initialize(applicationContext);
        registry.initialize(persistence, properties, threadPool);
        initWorkers();
        initRules();
        logConfiguration();
    }

    private void initThreadPool() {
        threadPool = ThreadPoolFactory.create("DOS").create();
        CachedAddress.setThreadPool(threadPool);
    }

    private void doRegister(Rule rule) {
        rule = normalize(rule);
        if (rule == null) return;
        registry.register(rule);
        try {
            persistence.resolve(rule, true);
        } catch (Exception e) {
            LOGGER.atWarn().setCause(e).log("Failed to register rule {}", rule.toDescription());
        }
    }

    private void initWorkers() {
    }

    private void initRules() {
        registry.loadRulesAsync();
    }

    private void logConfiguration() {
        LOGGER.info("DoS Service is enabled = {}, maintenance interval {}, maximum throttling duration {}",
                isEnabled(), formatDuration(properties.getMaintenanceInterval()), formatDuration(properties.getMaximumThrottlingDuration()));
    }

    private Rule normalize(Rule rule) {
        if (rule.getType() == Rule.Type.CIDR) return rule;
        HostName host = new HostName(rule.getAddress());
        if (host.isAddress()) return rule;
        CachedAddress address = registry.resolve(rule.getAddress());
        if (!address.isResolved()) return null;
        return Rule.create(address.getIp(), Rule.Type.IP).hostName(address.getHostname())
                .name(rule.getName()).description(rule.getDescription()).action(rule.getAction())
                .build();
    }


}
