package net.microfalx.bootstrap.broker;

import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.bootstrap.broker.BrokerUtils.describe;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A service which manages a collection of brokers.
 */
@Service
public class BrokerService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerService.class);

    private final Collection<BrokerProvider> providers = new CopyOnWriteArrayList<>();
    private final Map<String, Broker> brokers = new ConcurrentHashMap<>();
    private final Map<String, Topic> topics = new ConcurrentHashMap<>();
    private final Collection<WeakReference<BrokerConsumer<?, ?>>> consumers = new ConcurrentLinkedQueue<>();
    private final Collection<WeakReference<BrokerProducer<?, ?>>> producers = new ConcurrentLinkedQueue<>();

    /**
     * Returns registered brokers.
     *
     * @return a non-null instance
     */
    public Collection<Broker> getBrokers() {
        return unmodifiableCollection(brokers.values());
    }

    /**
     * Registers a broker.
     *
     * @param broker the broker
     */
    public void registerBroker(Broker broker) {
        requireNonNull(broker);
        brokers.put(broker.getId(), broker);
        LOGGER.info("Register broker '{}'", broker.getName());
    }

    /**
     * Returns registered topics.
     *
     * @return a non-null instance
     */
    public Collection<Topic> getTopics() {
        return unmodifiableCollection(topics.values());
    }

    /**
     * Registers a topic to be tracked by the service
     *
     * @param topic the topic
     */
    public void registerTopic(Topic topic) {
        requireNonNull(topic);
        topics.put(topic.getId(), topic);
        LOGGER.info("Register topic '{}' from broker '{}'", topic.getName(), topic.getBroker().getName());
    }

    /**
     * Returns a collection of registered consumers.
     *
     * @return a non-null instance
     */
    public Collection<BrokerConsumer<?, ?>> getConsumers() {
        return consumers.stream().map(Reference::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Returns a consumer by its identifier.
     *
     * @param id  the consumer identifier
     * @param <K> the key type
     * @param <V> the value type
     * @return the consumer
     * @throws IllegalArgumentException if the consumer cannot be located
     */
    @SuppressWarnings("unchecked")
    public <K, V> BrokerConsumer<K, V> getConsumer(String id) {
        requireNonNull(id);
        for (WeakReference<BrokerConsumer<?, ?>> consumer : consumers) {
            BrokerConsumer<?, ?> realConsumer = consumer.get();
            if (realConsumer != null && realConsumer.getId().equals(id)) return (BrokerConsumer<K, V>) realConsumer;
        }
        throw new IllegalArgumentException("A consumer with identifier '" + id + "' is not registered");
    }

    /**
     * Returns a collection of registered producers.
     *
     * @return a non-null instance
     */
    public Collection<BrokerProducer<?, ?>> getProducers() {
        return producers.stream().map(Reference::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Returns a producer by its identifier.
     *
     * @param id  the procuer identifier
     * @param <K> the key type
     * @param <V> the value type
     * @return the consumer
     * @throws IllegalArgumentException if the consumer cannot be located
     */
    @SuppressWarnings("unchecked")
    public <K, V> BrokerProducer<K, V> getProducer(String id) {
        requireNonNull(id);
        for (WeakReference<BrokerProducer<?, ?>> producer : producers) {
            BrokerProducer<?, ?> realProducer = producer.get();
            if (realProducer != null && realProducer.getId().equals(id)) return (BrokerProducer<K, V>) realProducer;
        }
        throw new IllegalArgumentException("A consumer with identifier '" + id + "' is not registered");
    }

    /**
     * Returns the broker by its identifier.
     *
     * @param id the broker identifier
     * @return the broker
     * @throws BrokerNotFoundException if such a broker is not registered
     */
    public Broker getBroker(String id) {
        requireNotEmpty(id);
        Broker broker = brokers.get(toIdentifier(id));
        if (broker == null) {
            throw new BrokerNotFoundException("A broker with identifier '" + id + "' is not registered");
        }
        return broker;
    }

    /**
     * Returns the broker by a topic URI.
     *
     * @param uri the topic URI
     * @return the broker
     * @throws BrokerNotFoundException if such a broker is not registered
     */
    public Broker getBroker(URI uri) {
        requireNonNull(uri);
        String host = uri.getHost();
        if (StringUtils.isEmpty(host)) {
            throw new BrokerNotFoundException("A hostname is required to locate a broker, URI '" + uri + "'");
        }
        return getBroker(host);
    }

    /**
     * Creates a consumer.
     *
     * @param uri the URI
     * @return a non-null instance
     */
    public <K, V> BrokerConsumer<K, V> createConsumer(URI uri) {
        requireNonNull(uri);
        return createConsumer(createTopic(uri));
    }

    /**
     * Creates a consumer.
     *
     * @param topic the topic
     * @return a non-null instance
     */
    public <K, V> BrokerConsumer<K, V> createConsumer(Topic topic) {
        requireNonNull(topic);
        BrokerProvider brokerProvider = locateProvider(topic.getBroker());
        try {
            BrokerConsumer<K, V> consumer = brokerProvider.createConsumer(topic);
            consumer.initialize();
            LOGGER.info("Create consumer for '{}'", describe(topic));
            return consumer;
        } catch (Exception e) {
            throw new BrokerException("Failed to create consumer for topic '" + describe(topic) + "'", e);
        }
    }

    /**
     * Creates a producer.
     *
     * @param uri the URI
     * @return a non-null instance
     */
    public <K, V> BrokerProducer<K, V> createProducer(URI uri) {
        requireNonNull(uri);
        return createProducer(createTopic(uri));
    }

    /**
     * Creates a consumer.
     *
     * @param topic the topic
     * @return a non-null instance
     */
    public <K, V> BrokerProducer<K, V> createProducer(Topic topic) {
        requireNonNull(topic);
        BrokerProvider brokerProvider = locateProvider(topic.getBroker());
        try {
            BrokerProducer<K, V> producer = brokerProvider.createProducer(topic);
            producer.initialize();
            LOGGER.info("Create producer for '{}'", describe(topic));
            return producer;
        } catch (Exception e) {
            throw new BrokerException("Failed to create producer for topic '" + describe(topic) + "'", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadProviders();
    }

    <K, V> void registerProducer(BrokerProducer<K, V> producer) {
        requireNonNull(producer);
        this.producers.add(new WeakReference<>(producer));
    }

    <K, V> void releaseProducer(BrokerProducer<K, V> producer) {
        requireNonNull(producer);
        for (WeakReference<BrokerProducer<?, ?>> reference : producers) {
            if (reference.get() == producer) {
                producers.remove(reference);
                break;
            }
        }
    }

    <K, V> void registerConsumer(BrokerConsumer<K, V> consumer) {
        requireNonNull(consumer);
        this.consumers.add(new WeakReference<>(consumer));
    }

    <K, V> void releaseConsumer(BrokerConsumer<K, V> consumer) {
        requireNonNull(consumer);
        for (WeakReference<BrokerConsumer<?, ?>> reference : consumers) {
            if (reference.get() == consumer) {
                consumers.remove(reference);
                break;
            }
        }
    }

    private BrokerProvider locateProvider(Broker broker) {
        for (BrokerProvider provider : providers) {
            if (provider.supports(broker)) return provider;
        }
        throw new BrokerException("A provider to support broker '" + broker.getName() + "' could not be located");
    }

    private Topic createTopic(URI uri) {
        Broker broker = getBroker(uri);
        return Topic.create(broker, uri.getPath());
    }

    private void loadProviders() {
        LOGGER.info("Load providers:");
        Collection<BrokerProvider> loadedProviders = ClassUtils.resolveProviderInstances(BrokerProvider.class);
        for (BrokerProvider loadedProvider : loadedProviders) {
            LOGGER.info(" - " + ClassUtils.getName(loadedProvider));
            if (loadedProvider instanceof AbstractBrokerProvider abstractBrokerProvider) {
                abstractBrokerProvider.brokerService = this;
            }
            providers.add(loadedProvider);
        }
    }
}
