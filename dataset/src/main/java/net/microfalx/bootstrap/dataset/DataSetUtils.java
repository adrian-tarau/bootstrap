package net.microfalx.bootstrap.dataset;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Various utilities around data sets.
 */
public class DataSetUtils {

    /**
     * Creates a data set sort form a Spring Data sort.
     *
     * @param sort the initial sort
     * @return a non-null instanace
     */
    static net.microfalx.bootstrap.model.Sort from(Sort sort) {
        List<net.microfalx.bootstrap.model.Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : sort.toList()) {
            orders.add(net.microfalx.bootstrap.model.Sort.Order.create(order.getProperty())
                    .ignoreCase(order.isIgnoreCase())
                    .with(from(order.getDirection()))
                    .with(from(order.getNullHandling())));

        }
        return net.microfalx.bootstrap.model.Sort.create(orders);
    }

    private static net.microfalx.bootstrap.model.Sort.Direction from(Sort.Direction direction) {
        return switch (direction) {
            case ASC -> net.microfalx.bootstrap.model.Sort.Direction.ASC;
            case DESC -> net.microfalx.bootstrap.model.Sort.Direction.DESC;
        };
    }

    private static net.microfalx.bootstrap.model.Sort.NullHandling from(Sort.NullHandling nullHandling) {
        return switch (nullHandling) {
            case NATIVE -> net.microfalx.bootstrap.model.Sort.NullHandling.NATIVE;
            case NULLS_FIRST -> net.microfalx.bootstrap.model.Sort.NullHandling.NULLS_FIRST;
            case NULLS_LAST -> net.microfalx.bootstrap.model.Sort.NullHandling.NULLS_LAST;
        };
    }
}
