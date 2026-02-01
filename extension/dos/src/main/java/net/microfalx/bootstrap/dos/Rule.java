package net.microfalx.bootstrap.dos;

import inet.ipaddr.HostName;
import inet.ipaddr.IPAddressString;
import lombok.ToString;
import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.StringUtils;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.*;

/**
 * A DoS Rule.
 * <p>
 * Rules can be by IP or CIDR and can indicate whatever the request is allowed to be processed or denied.
 * <p>
 * A rule is made out of a network (identifier), which can be an IP or a range of IPs and the action to take against a network client
 */
@ToString
public final class Rule implements Identifiable<String>, Nameable, Descriptable {

    private String id;
    private String name;
    private String description;

    private String address;
    private Type type;
    private Action action;
    private boolean active;
    private String hostName;
    private String uri;

    private String requestRate;
    private Float requestRateValue;
    private IPAddressString addressCache;

    public static Builder create(String address, Type type) {
        return new Builder(address, type);
    }

    public static String toIdentifier(String address, Type type) {
        requireNotEmpty(address);
        requireNonNull(type);
        return StringUtils.toIdentifier(type.name() + "_" + address);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Returns the network identifier/address.
     *
     * @return a non-null String
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the type of the network identifier
     *
     * @return a non-null enum
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns whether the rule is active.
     *
     * @return <code>true</code> if active, <code>false</code> otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns the action to be taken when a network client performs a request.
     *
     * @return a non-null enum
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns the hostname associated with the rule.
     * <p>
     * It only applies to {@link Type#IP}.
     *
     * @return the hostname, null if not known
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Returns the request rate allowed by the rule when action is {@link Action#THROTTLE}.
     *
     * @return the request rate, null if it does not apply
     */
    public String getRequestRate() {
        return requestRate;
    }

    /**
     * Returns the request rate value allowed by the rule when action is {@link Action#THROTTLE}.
     *
     * @return the value in request/second
     */
    public float getRequestRateValue() {
        if (requestRateValue == null) requestRateValue = DosUtils.parseRequestRate(requestRate);
        return requestRateValue;
    }

    /**
     * Returns the URI attached to the rule.
     *
     * @return the uri, null if not provided
     */
    public String getUri() {
        return uri;
    }

    /**
     * Returns whether the given address matches the rule.
     * <p>
     * If the rule identifies an IP, the IPs must match, otherwise the method checks if the IP is in the range.
     *
     * @param ip the IP to validate
     * @return <code>true</code> if it matches the rule, <code>false</code> otherwise
     */
    public boolean matches(String ip) {
        requireNotEmpty(address);
        if (type == Type.IP) {
            return address.equalsIgnoreCase(ip);
        } else {
            if (addressCache == null) addressCache = new IPAddressString(address);
            return addressCache.contains(new IPAddressString(ip));
        }
    }

    /**
     * Returns a short description of the rule.
     *
     * @return a non-null instance
     */
    public String toDescription() {
        return "name=" + name + ",address=" + address + ",type=" + type + ",action=" + action;
    }

    /**
     * Identifies the type of the rule.
     */
    public enum Type {

        /**
         * The rule identifies one IP
         */
        IP,

        /**
         * The rule identifies a class of IPs
         */
        CIDR
    }

    public enum Action {

        /**
         * The request is denied all the time.
         */
        DENY(true),

        /**
         * The request is allowed to be processed.
         */
        ALLOW(false),

        /**
         * The requests will be throttled
         */
        THROTTLE(true),

        /**
         * The request is allowed only if it is not crossing the DoS limits.
         */
        AUTO(false);

        boolean apply;

        Action(boolean apply) {
            this.apply = apply;
        }

        /**
         * Returns whether the DoS rule should be applied against the request.
         *
         * @return <code>true</code> to apply the rule, <code>false</code> otherwise
         */
        public boolean isApply() {
            return apply;
        }
    }

    /**
     * Identifies the reason why a rule rejected the request
     */
    public enum Reason {

        /**
         * The rule was discovered after a request was received
         */
        DISCOVERY,

        /**
         * The client seems to perform a classic DoS, mostly on valid URIs, to keep the services busy
         */
        DOS,

        /**
         * The client seems to attempt to guess user credentials
         */
        SECURITY,

        /**
         * The clients seems to try to scan end points and find exploits
         */
        SCAN,
    }

    public static class Builder {

        private String name;
        private String description;

        private String address;
        private Type type;
        private Action action = Action.AUTO;
        private boolean active = true;

        private String hostName;
        private String requestRate;

        public Builder(String address, Type type) {
            requireNotEmpty(address);
            requireNonNull(type);
            this.address = address;
            this.type = type;
        }

        public Builder name(String name) {
            requireNotEmpty(name);
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder action(Action action) {
            requireNonNull(action);
            if (action == Action.THROTTLE && isEmpty(requestRate)) {
                throw new IllegalArgumentException("A throttling action can only be registered with a request rate");
            }
            this.action = action;
            return this;
        }

        public Builder setRequestRate(String requestRate) {
            this.action = isNotEmpty(requestRate) ? Action.THROTTLE : this.action;
            this.requestRate = requestRate;
            return this;
        }

        public Builder hostName(String hostName) {
            if (type != Type.IP && isNotEmpty(hostName))
                throw new IllegalArgumentException("A hostname only applies to type=IP");
            this.hostName = hostName;
            return this;
        }

        public Rule build() {
            Rule rule = new Rule();
            rule.id = toIdentifier(address, type);
            rule.address = address;
            rule.type = type;
            rule.action = action;
            rule.active = active;
            if (hostName == null && type == Type.IP) {
                HostName host = new HostName(address);
                if (!host.isAddress()) hostName = address;
            }
            rule.hostName = hostName;
            rule.name = defaultIfEmpty(name, address);
            if (new IPAddressString(address).isLoopback()) rule.name = "Local";
            rule.requestRate = requestRate;
            rule.description = description;
            return rule;
        }
    }
}
