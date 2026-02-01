package net.microfalx.bootstrap.core.utils;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.*;
import net.microfalx.metrics.Metrics;
import net.microfalx.metrics.Timer;
import net.microfalx.threadpool.ThreadPool;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A cached network address.
 */
@Slf4j
@ToString
public class CachedAddress implements Identifiable<String>, Nameable, Timestampable<LocalDateTime>, Serializable {

    @Serial private static final long serialVersionUID = -7930577447752154283L;
    private static final long EXPIRY_DURATION = TimeUtils.ONE_HOUR;

    private final String id;
    private final String hostOrIp;
    private final InetAddress address;
    private Collection<InetAddress> addresses;
    private final long created = currentTimeMillis();
    private String canonicalHostName;
    private Set<String> aliases;
    private Set<String> ips;
    private String hash;
    private final boolean resolved;
    @ToString.Exclude private final AtomicBoolean resolving = new AtomicBoolean();

    private static final Metrics METRICS = Metrics.of("Socket");
    private static final Metrics RESOLVE = METRICS.withGroup("Resolve");
    private static final Metrics FAILURE = METRICS.withGroup("Failure");
    private static final Map<String, CachedAddress> addressCache = new ConcurrentHashMap<>();
    private static final Map<String, String> canonicalHostsCache = new ConcurrentHashMap<>();
    private static ThreadPool threadPool = ThreadPool.get();
    private static InetAddress anyAddress;

    /**
     * Changes the thread pool used for resolving addresses.
     */
    public static void setThreadPool(ThreadPool threadPool) {
        requireNonNull(threadPool);
        CachedAddress.threadPool = threadPool;
    }

    /**
     * Returns an network address which means "any" (interface) - basically "0.0.0.0".
     *
     * @return a non-null instance
     */
    public static InetAddress getAnyAddress() {
        return anyAddress;
    }

    /**
     * Returns whether the value matches an IP and not a hostname.
     *
     * @param value the value
     * @return {@code true} if IP, {@code false} otherwise
     */
    public static boolean isIP(String value) {
        if (StringUtils.isEmpty(value)) return false;
        return IP_PATTERN.matcher(value).matches();
    }

    /**
     * Returns whether the host/IP is actually "local"
     *
     * @param hostOrIp host or IP
     * @return {@code true} if local, {@code false} otherwise
     */
    public static boolean isLocalHost(String hostOrIp) {
        return "localhost".equalsIgnoreCase(hostOrIp) || "127.0.0.1".equals(hostOrIp) || "::1".equals(hostOrIp) || "0:0:0:0:0:0:0:1".equals(hostOrIp);
    }

    /**
     * Returns whether the host/IP belongs to a local network.
     *
     * @param hostOrIp the host or IP
     * @return {@code true} if local network, {@code false} otherwise
     */
    public static boolean isLocalNetwork(String hostOrIp) {
        if (StringUtils.isEmpty(hostOrIp)) return true;
        return hostOrIp.startsWith("192.168.") || hostOrIp.startsWith("10.") || hostOrIp.startsWith("172.16.");

    }

    /**
     * Returns the domain from the hostname.
     * <p>
     * If the host name is an IP, it returns the IP.
     *
     * @param hostOrIp the hostname or IP
     * @return the domain name
     */
    public static String getDomainName(String hostOrIp) {
        if (isIP(hostOrIp)) {
            return hostOrIp;
        } else {
            int position = hostOrIp.indexOf('.');
            if (position == -1) {
                return hostOrIp;
            } else {
                return hostOrIp.substring(position + 1);
            }
        }
    }

    /**
     * Returns a cached address for the given host or IP.
     *
     * @param hostOrIp the host or IP
     * @return a non-null instance
     */
    public static CachedAddress get(String hostOrIp) {
        return getCachedAddress(hostOrIp);
    }

    CachedAddress(String hostOrIp) {
        this(hostOrIp, getAnyAddress());
    }

    CachedAddress(String hostOrIp, InetAddress address) {
        requireNonNull(hostOrIp);
        requireNonNull(address);
        this.id = toIdentifier(hostOrIp);
        this.hostOrIp = hostOrIp;
        this.address = address;
        this.resolved = !getAnyAddress().equals(address);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return getHostname();
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return TimeUtils.toLocalDateTime(created);
    }

    public GeoLocation getLocation() {
        return IpWhoIs.lookup(getIp());
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getIp() {
        if (resolved) {
            return address.getHostAddress();
        } else {
            return hostOrIp;
        }
    }

    public String getHostname() {
        if (resolved) {
            return address.getHostName();
        } else {
            return hostOrIp;
        }
    }

    public String getCanonicalHostName() {
        if (resolved) {
            if (canonicalHostName == null) {
                canonicalHostName = address.getCanonicalHostName();
                if (canonicalHostName == null || isIP(canonicalHostName)) {
                    canonicalHostName = address.getHostName();
                }
            }
            return canonicalHostName;
        } else {
            return hostOrIp;
        }
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean isExpired() {
        Duration age = Duration.between(getCreatedAt(), LocalDateTime.now());
        return age.toMillis() > EXPIRY_DURATION;
    }

    public boolean isLocalHost() {
        return isLocalHost(hostOrIp);
    }

    public boolean isIp() {
        return isIP(hostOrIp);
    }

    public boolean hasAliases() {
        return aliases != null;
    }

    public Set<String> getAliases() {
        if (resolved) {
            return aliases != null ? unmodifiableSet(aliases) : Set.of(getHostname(), getCanonicalHostName());
        } else {
            return Set.of(hostOrIp);
        }
    }

    public Set<String> getIPs() {
        if (resolved) {
            return ips != null ? unmodifiableSet(ips) : Set.of(getIp());
        } else {
            return Set.of(hostOrIp);
        }
    }

    public String getHash() {
        if (hash == null) {
            Hashing checksum = Hashing.create();
            checksum.update(hostOrIp);
            checksum.update(getIp());
            checksum.update(getHostname());
            hash = checksum.asString();
        }
        return hash;
    }

    public String toDescription() {
        if (getIp().equalsIgnoreCase(getHostname())) {
            return getIp();
        } else if (getHostname().equalsIgnoreCase(getCanonicalHostName())) {
            return getHostname() + " (" + getIp() + ")";
        } else {
            return getCanonicalHostName() + " / " + getHostname() + " (" + getIp() + ")";
        }
    }

    Collection<InetAddress> getAddresses() {
        return addresses;
    }

    void setAddresses(InetAddress[] addresses) {
        requireNonNull(addresses);
        this.addresses = new HashSet<>(asList(addresses));
        this.aliases = new HashSet<>(asList(getHostname(), getCanonicalHostName()));
        this.ips = new HashSet<>(Set.of(getIp()));
        for (InetAddress inetAddress : addresses) {
            aliases.add(inetAddress.getHostName());
            aliases.add(inetAddress.getCanonicalHostName());
            ips.add(inetAddress.getHostAddress());
        }
    }

    private static CachedAddress resolve(String hostOrIp) {
        CachedAddress cachedAddress;
        try (Timer ignored = RESOLVE.startTimer(getDomainName(hostOrIp))) {
            InetAddress address = InetAddress.getByName(hostOrIp);
            cachedAddress = new CachedAddress(hostOrIp, address);
        } catch (UnknownHostException e) {
            cachedAddress = new CachedAddress(hostOrIp);
            FAILURE.count(hostOrIp);
        }
        if (cachedAddress.isResolved()) {
            LOGGER.debug("Resolved address {} for {}", cachedAddress.getHostname(), hostOrIp);
            try {
                InetAddress[] addresses = InetAddress.getAllByName(hostOrIp);
                if (addresses != null && addresses.length > 1) {
                    cachedAddress.setAddresses(addresses);
                }
            } catch (UnknownHostException e) {
                FAILURE.increment(hostOrIp);
            }
        }
        return cachedAddress;
    }

    private static boolean isExpired(CachedAddress address) {
        Duration age = Duration.between(address.getCreatedAt(), LocalDateTime.now());
        return age.toMillis() > EXPIRY_DURATION;
    }

    private static CachedAddress getCachedAddress(String hostOrIp) {
        requireNonNull(hostOrIp);
        hostOrIp = hostOrIp.toLowerCase();
        CachedAddress address = addressCache.get(hostOrIp);
        if (address == null) {
            address = resolve(hostOrIp);
            addressCache.put(hostOrIp, address);
            threadPool.execute(new ResolveCanonicalNameTask(address));
        } else if (isExpired(address) && address.resolving.compareAndSet(false, true)) {
            threadPool.execute(new ResolveTask(hostOrIp));
        }
        return address;
    }

    static {
        try {
            anyAddress = InetAddress.getByName("0.0.0.0");
        } catch (UnknownHostException e) {
            System.out.println("Failed to initialize ANY address, root cause: " + getRootCauseMessage(e));
            anyAddress = InetAddress.getLoopbackAddress();
        }
    }

    private static class ResolveCanonicalNameTask implements Runnable {

        private final CachedAddress cachedAddress;

        ResolveCanonicalNameTask(CachedAddress cachedAddress) {
            this.cachedAddress = cachedAddress;
        }

        @Override
        public void run() {
            String canonicalHostName = cachedAddress.getCanonicalHostName();
            String location = cachedAddress.getLocation().getDescription();
            LOGGER.debug("Resolved canonical host name {} for {}, location {}", canonicalHostName,
                    cachedAddress.getAddress(), location);
        }
    }


    private static class ResolveTask implements Runnable {

        private final String hostOrIp;

        ResolveTask(String hostOrIp) {
            this.hostOrIp = hostOrIp;
        }

        @Override
        public void run() {
            CachedAddress cachedAddress = resolve(hostOrIp);
            new ResolveCanonicalNameTask(cachedAddress).run();
            addressCache.put(hostOrIp, cachedAddress);
        }
    }

    private static final Pattern IP_PATTERN = Pattern.compile("((^\\s*((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))\\s*$)|(^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$))", Pattern.CASE_INSENSITIVE);
}
