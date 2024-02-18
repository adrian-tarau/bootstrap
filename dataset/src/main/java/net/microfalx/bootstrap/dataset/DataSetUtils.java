package net.microfalx.bootstrap.dataset;

import org.apache.commons.lang3.ClassUtils;
import org.joor.Reflect;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang3.ClassUtils.isAssignable;

/**
 * Various utilities around data sets.
 */
public class DataSetUtils {

    /**
     * The operator injected when the user clicks on a field value in the grid.
     */
    public final static String DEFAULT_FILTER_OPERATOR = " = ";

    /**
     * Returns the operator injected when the user clicks on a field value in the grid.
     */
    public final static char DEFAULT_FILTER_QUOTE_CHAR = '\"';

    /**
     * Finds a parameter by type.
     *
     * @param type       the type
     * @param parameters the parameters
     * @param <T>        the data type
     * @return the parameter, null if does not exist
     */
    public static <T> T find(Class<T> type, Object... parameters) {
        for (Object parameter : parameters) {
            if (isAssignable(parameter.getClass(), type)) return (T) parameter;
            Collection<Reflect> fields = Reflect.on(parameter).fields().values();
            for (Reflect field : fields) {
                if (ClassUtils.isAssignable(field.type(), type)) return field.get();
            }
        }
        return null;
    }

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
