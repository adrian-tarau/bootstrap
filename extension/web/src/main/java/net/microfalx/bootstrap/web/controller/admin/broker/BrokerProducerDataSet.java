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
public class BrokerProducerDataSet extends PojoDataSet<BrokerProducer, PojoField<BrokerProducer>, String> {

    public BrokerProducerDataSet(DataSetFactory<BrokerProducer, PojoField<BrokerProducer>, String> factory, Metadata<BrokerProducer, PojoField<BrokerProducer>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Optional<BrokerProducer> doFindById(String id) {
        BrokerService brokerService = getService(BrokerService.class);
        try {
            return Optional.of(BrokerProducer.from(brokerService.getProducer(id)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    protected Page<BrokerProducer> doFindAll(Pageable pageable, Filter filterable) {
        BrokerService brokerService = getService(BrokerService.class);
        List<BrokerProducer> consumers = brokerService.getProducers().stream().map(BrokerProducer::from).toList();
        return getPage(consumers, pageable, filterable);
    }
}
