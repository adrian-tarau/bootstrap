package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.core.i18n.I18n;
import net.microfalx.bootstrap.model.*;
import net.microfalx.lang.EnumUtils;
import net.microfalx.metrics.Metrics;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.joor.Reflect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static org.apache.commons.lang3.ClassUtils.isAssignable;

/**
 * Various utilities around data sets.
 */
public class DataSetUtils {

    static Metrics METRICS = Metrics.of("DataSet");

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
    @SuppressWarnings("unchecked")
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
     * Returns the display value of an enum.
     *
     * @param i18n  the I18n resolver
     * @param value the enum value
     * @param <E>   the enum type
     * @return the display value
     */
    public static <E extends Enum<E>> String getDisplayValue(I18n i18n, E value) {
        if (value == null) return StringUtils.EMPTY;
        if (i18n != null) {
            return i18n.getText(value);
        } else {
            return EnumUtils.toLabel(value);
        }
    }

    /**
     * Filters and sorts a list of models.
     *
     * @param models     the models
     * @param filterable the filter
     * @param sort       the sort
     * @return a non-null instance
     */
    public static <M, F extends Field<M>, ID> List<M> filterAndSort(Metadata<M, F, ID> metadata, Iterable<M> models, Filter filterable, net.microfalx.bootstrap.model.Sort sort) {
        requireNonNull(models);
        requireNonNull(filterable);
        requireNonNull(sort);
        ModelFilter<M> filter = new ModelFilter<>(metadata, models, filterable);
        ModelSorter<M> sorter = new ModelSorter<>(metadata, filter.toList(), sort);
        return sorter.toList();
    }

    /**
     * Sorts a list of models.
     *
     * @param models the models
     * @param sort   the sort
     * @return a non-null instance
     */
    public static <M, F extends Field<M>, ID> List<M> sort(Metadata<M, F, ID> metadata, Iterable<M> models, Sort sort) {
        requireNonNull(models);
        requireNonNull(sort);
        ModelSorter<M> sorter = new ModelSorter<>(metadata, models, DataSetUtils.from(sort));
        return sorter.toList();
    }

    /**
     * Paginates a list of models.
     *
     * @param models   the models
     * @param pageable the page information
     * @return a page of models
     */
    public static <M, F extends Field<M>, ID> Page<M> getPage(Metadata<M, F, ID> metadata, Iterable<M> models, Pageable pageable) {
        requireNonNull(models);
        requireNonNull(pageable);
        ModelSorter<M> sorter = new ModelSorter<>(metadata, models, DataSetUtils.from(pageable.getSort()));
        return new DataSetPage<>(pageable, sorter.toList());
    }

    /**
     * Filters and paginates a list of models.
     *
     * @param models     the models
     * @param pageable   the page information
     * @param filterable the filter
     * @return a page of models
     */
    public static <M, F extends Field<M>, ID> Page<M> getPage(Metadata<M, F, ID> metadata, Iterable<M> models, Pageable pageable, Filter filterable) {
        return new DataSetPage<>(pageable, filterAndSort(metadata, models, filterable, DataSetUtils.from(pageable.getSort())));
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
