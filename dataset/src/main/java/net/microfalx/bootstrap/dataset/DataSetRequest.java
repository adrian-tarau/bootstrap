package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.lang.IdGenerator;
import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.lang.TimeUtils;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Represents a request for a data set.
 * <p>
 * The class encapsulates the model class, filter criteria, and pagination information.
 */
public class DataSetRequest<M, F extends Field<M>, ID> extends NamedIdentityAware<String> {

    private final DataSet<M, F, ID> dataSet;
    private final Filter filter;
    private final Pageable pageable;
    private final long createdAt = System.currentTimeMillis();

    private static final IdGenerator INSTANCE = IdGenerator.get("DataSetRequest");

    static ThreadLocal<DataSetRequest<?, ?, ?>> CURRENT = new ThreadLocal<>();

    public static <M, F extends Field<M>, ID> DataSetRequest<M, F, ID> create(DataSet<M, F, ID> dataSet, Filter filter) {
        return create(dataSet, filter, null);
    }

    public static <M, F extends Field<M>, ID> DataSetRequest<M, F, ID> create(DataSet<M, F, ID> dataSet, Filter filter, Pageable pageable) {
        return new DataSetRequest<>(dataSet, filter, pageable);
    }

    public static DataSetRequest<?, ?, ?> current() {
        return CURRENT.get();
    }

    private DataSetRequest(DataSet<M, F, ID> dataSet, Filter filter, Pageable pageable) {
        requireNonNull(dataSet);
        requireNonNull(filter);
        if (pageable == null) pageable = Pageable.ofSize(500);
        setId(INSTANCE.nextAsString());
        setName(dataSet.getName());
        this.dataSet = dataSet;
        this.filter = filter;
        this.pageable = pageable;
    }

    /**
     * Returns the data set for which the request is made.
     *
     * @return a non-null instance
     */
    public DataSet<M, F, ID> getDataSet() {
        return dataSet;
    }

    /**
     * Returns the model class for which the data set is requested.
     *
     * @return a non-null instance
     */
    public Class<?> getModelClass() {
        return dataSet.getMetadata().getModel();
    }

    /**
     * Returns the filter criteria applied to the data set.
     *
     * @return a non-null instance
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Returns the pagination information for the data set request.
     *
     * @return a non-null instance
     */
    public Pageable getPageable() {
        return pageable;
    }

    /**
     * Returns the creation timestamp of the data set request.
     *
     * @return a non-null instance
     */
    public LocalDateTime getCreatedAt() {
        return TimeUtils.toLocalDateTime(createdAt);
    }
}
