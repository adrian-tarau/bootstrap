package net.microfalx.bootstrap.broker;

/**
 * Base class for all broker providers.
 */
public abstract class AbstractBrokerProvider implements BrokerProvider {

    BrokerService brokerService;

    @Override
    public final BrokerService getBrokerService() {
        return brokerService;
    }
}
