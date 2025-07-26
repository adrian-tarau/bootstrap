package net.microfalx.bootstrap.web.controller.support.alert;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.logger.LoggerService;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Provider
public class AlertDataSet extends PojoDataSet<Alert, PojoField<Alert>, String> {

    public AlertDataSet(DataSetFactory<Alert, PojoField<Alert>, String> factory, Metadata<Alert, PojoField<Alert>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Optional<Alert> doFindById(String id) {
        try {
            return Optional.ofNullable(Alert.from(getService(LoggerService.class).getAlert(id)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    protected Page<Alert> doFindAll(Pageable pageable, Filter filterable) {
        List<Alert> alerts = getService(LoggerService.class).getAlerts(LocalDateTime.now().minusDays(1), LocalDateTime.now())
                .stream().map(Alert::from).collect(Collectors.toList());
        return getPage(alerts, pageable, filterable);
    }
}
