package net.microfalx.bootstrap.web.controller.admin.broker;

import net.microfalx.bootstrap.broker.BrokerService;
import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Provider
public class BrokerConsumerDataSet extends PojoDataSet<BrokerConsumer, PojoField<BrokerConsumer>, String> {

    public BrokerConsumerDataSet(DataSetFactory<BrokerConsumer, PojoField<BrokerConsumer>, String> factory, Metadata<BrokerConsumer, PojoField<BrokerConsumer>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Optional<BrokerConsumer> doFindById(String id) {
        BrokerService brokerService = getService(BrokerService.class);
        try {
            return Optional.of(BrokerConsumer.from(brokerService.getConsumer(id)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    protected Page<BrokerConsumer> doFindAll(Pageable pageable, Filter filterable) {
        BrokerService brokerService = getService(BrokerService.class);
        List<BrokerConsumer> consumers = brokerService.getConsumers().stream().map(BrokerConsumer::from).toList();
        return getPage(consumers, pageable, filterable);
    }
}
