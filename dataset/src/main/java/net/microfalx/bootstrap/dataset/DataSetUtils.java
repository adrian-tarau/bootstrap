package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.core.i18n.I18n;
import net.microfalx.bootstrap.model.*;
import net.microfalx.lang.ArgumentUtils;
import net.microfalx.lang.EnumUtils;
import net.microfalx.metrics.Metrics;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.joor.Reflect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Duration;
import java.time.ZonedDateTime;
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
     * Default number of points per interval for trends.
     */
    public final static int DEFAULT_POINTS = 100;

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
     * Returns the step (aggregation interval) for a time interval.
     * <p>
     * It uses a default number of {@link #DEFAULT_POINTS points}.
     *
     * @param start the start time
     * @param end   the end time
     * @return a non-null instance, rounded
     */
    public static Duration getStep(ZonedDateTime start, ZonedDateTime end) {
        return getStep(start, end, DEFAULT_POINTS);
    }

    /**
     * Returns the step (aggregation interval) for a time interval and number of points.
     *
     * @param start  the start time
     * @param end    the end time
     * @param points the number of points
     * @return a non-null instance, rounded
     */
    public static Duration getStep(ZonedDateTime start, ZonedDateTime end, int points) {
        ArgumentUtils.requireNonNull(start);
        ArgumentUtils.requireNonNull(end);
        Duration duration = Duration.between(start, end).dividedBy(points);
        for (int index = 1; index < STEP_LIMIT.length; index++) {
            int limitLow = STEP_LIMIT[index - 1];
            int limitHigh = STEP_LIMIT[index];
            if (duration.toMinutes() >= limitLow && duration.toMinutes() < limitHigh) {
                int round = STEP_ROUND[index - 1];
                return Duration.ofSeconds((duration.toSeconds() / round) * round);
            }
        }
        return Duration.ofMinutes(60 * (duration.toMinutes() / 60));
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

    private static final int[] STEP_LIMIT = {0, 1, 5, 60, 60 * 24};
    private static final int[] STEP_ROUND = {10, 60, 5 * 60, 15 * 60};
}
