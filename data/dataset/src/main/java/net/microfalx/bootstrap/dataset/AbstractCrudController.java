package net.microfalx.bootstrap.dataset;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.dataset.annotation.Searchable;
import net.microfalx.bootstrap.model.*;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.CreatedAt;
import net.microfalx.lang.annotation.ModifiedAt;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.*;

/**
 * A controller helper for CRUD operations on datasets.
 */
@Slf4j
public abstract class AbstractCrudController<M, ID, C> {

    private static final String INVALID_FILTER_PREFIX = "Invalid filter: ";
    private static final String DATE_RANGE_SEPARATOR = "|";

    private final C owner;
    private final DataSetService dataSetService;

    private String message;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private DataSet<M, Field<M>, ID> cachedDataSet;

    private static final ThreadLocal<Boolean> CANCELED = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public AbstractCrudController(DataSetService dataSetService, C owner) {
        requireNotEmpty(dataSetService);
        requireNotEmpty(owner);
        this.dataSetService = dataSetService;
        this.owner = owner;
    }

    /**
     * Returns the owner of this controller (the actual controller).
     *
     * @return a non-null instance
     */
    public C getOwner() {
        return owner;
    }

    /**
     * Returns the data set used with this controller.
     *
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    public final DataSet<M, Field<M>, ID> getDataSet() {
        if (cachedDataSet == null) {
            net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnot = getDataSetAnnotation();
            cachedDataSet = dataSetService.getDataSet((Class<M>) dataSetAnnot.model(), this);
            if (cachedDataSet instanceof AbstractDataSet<M, Field<M>, ID> abstractDataSet) {
                Arrays.stream(dataSetAnnot.tags()).forEach(abstractDataSet::addTag);
            }
        }
        return cachedDataSet;
    }

    /**
     * Finds a model by its identifier.
     *
     * @param id    the data set model identifier
     * @param state the state which triggered the request
     * @return the model, null if it does not exist
     */
    public final M findModel(String id, State state) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        return dataSetService.doWithDataSet(dataSet, ds -> doFindModel(ds, id, state));
    }

    /**
     * Finds a model by its identifier.
     *
     * @param id    the data set model identifier
     * @param state the state which triggered the request
     * @return the model, null if it does not exist
     */
    public final M findModel(ID id, State state) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        return dataSetService.doWithDataSet(dataSet, ds -> doFindModel(ds, id, state));
    }

    /**
     * Returns a page of models based on the provided parameters.
     *
     * @param timeRange the range parameter
     * @param query     the query parameter
     * @param sort      the sort parameter
     * @param page      the page parameter
     * @return a non-null instance
     */
    public final Page<M> getPage(String timeRange, String query, String sort, int page) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        Sort sortImpl = getSort(sort);
        Pageable pageImpl = getPage(page, sortImpl);
        Filter filter = getFilter(timeRange, query);
        DataSetRequest<M, Field<M>, ID> request = DataSetRequest.create(dataSet, filter, pageImpl);
        dataSetService.registerRequest(request);
        Page<M> pagedModels = extractModels(filter, pageImpl);
        updatePage(dataSet, pagedModels, filter, sortImpl);
        return pagedModels;
    }

    /**
     * Returns the filter based on the provided parameters.
     *
     * @param timeRange the time range parameter
     * @param query     the query parameter
     * @return a non-null instance
     */
    public final Filter getFilter(String timeRange, String query) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        Filter filter;
        if (useRawQuery()) {
            filter = Filter.create(ComparisonExpression.eq(ComparisonExpression.QUERY, query));
        } else {
            String defaultQuery = getDefaultQuery();
            if (isNotEmpty(defaultQuery)) {
                QueryParser<M, Field<M>, ID> queryParser = createQueryParser(dataSet, defaultQuery);
                if (!queryParser.isValid()) {
                    defaultQuery = null;
                    LOGGER.warn("Default query is not valid: " + queryParser.validate());
                }
            }
            filter = doGetFilter(dataSet, defaultIfEmpty(query, defaultQuery));
            if (filter == null) filter = doGetFilter(dataSet, defaultQuery);
        }
        return addRangeFilter(dataSet, timeRange, filter);
    }

    /**
     * Returns the sort based on the provided request parameter.
     *
     * @param value the sort parameter value
     * @return a non-null instance
     */
    public Sort getSort(String value) {
        if (isEmpty(value)) return getDefaultSort();
        List<Sort.Order> orders = new ArrayList<>();
        String[] parts = StringUtils.split(value, ";");
        for (String part : parts) {
            String[] parts2 = StringUtils.split(part, "=");
            if (parts2.length > 2)
                throw new IllegalArgumentException("Invalid sorting value ( " + value + "), expected format: FIELD_NAME1[=DIRECTION];FIELD_NAME2[=DIRECTION]");
            Sort.Direction direction = Sort.Direction.ASC;
            if (parts2.length == 2) {
                direction = "asc".equalsIgnoreCase(parts2[1]) ? Sort.Direction.ASC : "desc".equalsIgnoreCase(parts2[1]) ? Sort.Direction.DESC : null;
            }
            if (direction != null) {
                orders.add(new Sort.Order(direction, parts2[0]));
            }
        }
        return Sort.by(orders);
    }

    /**
     * Updates all fields annotated with {@link ModifiedAt} to the current date/time.
     *
     * @param model the model
     */
    public final void updateModifiedAtFields(M model) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        List<Field<M>> fields = dataSet.getMetadata().getFields(ModifiedAt.class);
        for (Field<M> field : fields) {
            field.set(model, LocalDateTime.now());
        }
    }

    /**
     * Updates all fields annotated with {@link CreatedAt} to the current date/time.
     *
     * @param model the model
     */
    public final void updateCreatedAtFields(M model) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        List<Field<M>> fields = dataSet.getMetadata().getFields(CreatedAt.class);
        for (Field<M> field : fields) {
            field.set(model, LocalDateTime.now());
        }
    }

    /**
     * Cancels a before event with a message.
     * <p>
     * The method can be called in any <code>beforeXXXX</code> events/callbacks to cancel the operation.
     *
     * @param message the message
     */
    public final void cancel(String message) {
        requireNotEmpty(message);
        setMessage(message);
        CANCELED.set(true);
        throw new CanceledException();
    }

    /**
     * Returns whether the operation was canceled.
     *
     * @return {@code true} if the operation was canceled, {@code false} otherwise
     */
    public final boolean isCanceled() {
        return Boolean.TRUE.equals(CANCELED.get());
    }

    /**
     * Persists the data set model, under a transaction if supported by the data set.
     *
     * @param model the model
     * @param state the data set state
     * @return {@code true} if the model was persisted, {@code false} if the operation was canceled
     */
    public final boolean persist(M model, State state) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        return dataSetService.doWithDataSet(dataSet, (ds, status) -> {
            doBeforePersist(dataSet, model, state);
            if (isCanceled() && status != null) status.setRollbackOnly();
            return !isCanceled();
        });
    }

    /**
     * Deletes the data set model, under a transaction if supported by the data set.
     *
     * @param id the model identifier
     * @return the deleted model
     */
    public final M deleteById(Object id) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        M model;
        if (id instanceof String) {
            model = findModel((String) id, State.DELETE);
        } else {
            model = findModel((ID) id, State.DELETE);
        }
        return deleteByModel(model);
    }

    /**
     * Deletes the data set model, under a transaction if supported by the data set.
     *
     * @param model the model
     * @return the deleted model
     */
    public final M deleteByModel(M model) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        return dataSetService.doWithDataSet(dataSet, (ds, status) -> {
            dataSet.delete(model);
            if (isCanceled() && status != null) status.setRollbackOnly();
            return model;
        });
    }

    /**
     * Returns the start of the time range, if any.
     *
     * @return the start time, null if not applicable
     */
    public final ZonedDateTime getStartTime() {
        return startTime;
    }

    /**
     * Returns the end of the time range, if any.
     *
     * @return the end time, null if not applicable
     */
    public final ZonedDateTime getEndTime() {
        return endTime;
    }

    /**
     * Returns the message for the user/client, if any.
     *
     * @return the message, null if the last operation was valid
     */
    public final String getMessage() {
        return message;
    }

    /**
     * Returns whether the data set has a time range.
     *
     * @return {@code true} if the data set has a time range, {@code false} otherwise
     */
    public final boolean hasTimeRange() {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        return dataSet.getMetadata().findTimestampField() != null && getDataSetAnnotation().timeFilter();
    }

    /**
     * Logs the request parameters.
     *
     * @param action    the action performed
     * @param timeRange the time range parameter
     * @param query     the query parameter
     * @param sort      the sort parameter
     * @param page      the page parameter
     */
    public final void logRequest(String action, String timeRange, String query, String sort, int page) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        timeRange = defaultIfEmpty(timeRange, "<empty>");
        query = defaultIfEmpty(query, "<empty>");
        sort = defaultIfEmpty(sort, "<empty>");
        LOGGER.debug("{} data set {}, time range{}, query {}, sort {}, page {}", capitalizeWords(action), dataSet.getName(), page, timeRange, query, sort);
    }

    /**
     * Throws a model not found exception.
     *
     * @param id  the model identifier
     * @param <T> the return type
     * @return a never returning value
     */
    public final <T> T throwModelNotFound(String id) {
        throw new ModelNotFoundException("A model with identifier '" + id + " does not exist");
    }

    /**
     * Throws an exception if the identifier is empty.
     *
     * @param id the identifier
     */
    public final void throwIdentifierRequired(Object id) {
        if (ObjectUtils.isEmpty(id)) throw new ModelException("The model identifier is required");
    }

    /**
     * Subclasses can update the controller model with additional variables and/or change the data set model.
     *
     * @param dataSet the data set
     * @param model   the model return by the data set, will be present only for {@link State#EDIT} and {@link State#VIEW}.
     * @param state   the state of the data set
     */
    protected void updateModel(DataSet<M, Field<M>, ID> dataSet, M model, State state) {
        // empty by default
    }

    /**
     * Subclasses can update the controller model with additional variables related to a data set page.
     *
     * @param dataSet the data set
     * @param page    the page of models
     * @param filter  the filter extracted from request
     * @param sort    the sort extracted from request
     */
    protected void updatePage(DataSet<M, Field<M>, ID> dataSet, Page<M> page, Filter filter, Sort sort) {
        // empty by default
    }

    /**
     * Subclasses can use the method to update the invalid message.
     *
     * @param message the message
     */
    protected void updateInvalidMessage(String message) {
        // empty by default
    }

    /**
     * Invoked before the model is persisted.
     * <p>
     * If the model is supported by a database, the event is called under a transaction.
     *
     * @param dataSet the data set
     * @param model   the model
     * @param state   the data set state
     */
    protected void beforePersist(DataSet<M, Field<M>, ID> dataSet, M model, State state) {
        // empty by default
    }

    /**
     * Invoked after the model is persisted.
     * <p>
     * If the model is supported by a database, the event is called under a transaction after the model is saved.
     *
     * @param dataSet the data set
     * @param model   the model
     * @param state   the data set state
     */
    protected void afterPersist(DataSet<M, Field<M>, ID> dataSet, M model, State state) {
        // empty by default
    }

    /**
     * Updates the message for the user/client.
     *
     * @param message the message
     */
    protected final void setMessage(String message) {
        this.message = message;
        updateInvalidMessage(message);
    }

    private M doFindModel(DataSet<M, Field<M>, ID> dataSet, String id, State state) {
        CompositeIdentifier<M, Field<M>, ID> compositeId = dataSet.getMetadata().getId(id);
        Optional<M> result = dataSet.findById(compositeId.toId());
        return doFindModel(dataSet, result, id, state);
    }

    private M doFindModel(DataSet<M, Field<M>, ID> dataSet, ID id, State state) {
        Optional<M> result = dataSet.findById(id);
        return doFindModel(dataSet, result, id, state);
    }

    private M doFindModel(DataSet<M, Field<M>, ID> dataSet, Optional<M> result, Object id, State state) {
        if (result.isPresent()) {
            M model = result.get();
            dataSet.detach(model);
            updateModel(dataSet, model, state);
            return model;
        } else {
            return throwModelNotFound(ObjectUtils.toString(id));
        }
    }

    private net.microfalx.bootstrap.dataset.annotation.DataSet getDataSetAnnotation() {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnot = AnnotationUtils.getAnnotation(owner.getClass(), net.microfalx.bootstrap.dataset.annotation.DataSet.class);
        if (dataSetAnnot == null) {
            throw new DataSetException("A @DataSet annotation could not be located for controller " + ClassUtils.getName(owner.getClass()));
        }
        return dataSetAnnot;
    }


    private Pageable getPage(int page, Sort sort) {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnotation = getDataSetAnnotation();
        if (page < 0) {
            return PageRequest.of(0, DataSetExport.MAXIMUM_PAGE_SIZE, sort);
        } else {
            return PageRequest.of(page, dataSetAnnotation.pageSize(), sort);
        }
    }

    private Page<M> extractModels(Filter filter, Pageable pageable) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        return dataSetService.doWithDataSet(dataSet, ds -> dataSet.findAll(pageable, filter));
    }


    private String getDefaultQuery() {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnotation = getDataSetAnnotation();
        return dataSetAnnotation.defaultQuery();
    }

    private boolean useRawQuery() {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnotation = getDataSetAnnotation();
        return dataSetAnnotation.rawQuery();
    }


    private Filter doGetFilter(DataSet<M, Field<M>, ID> dataSet, String query) {
        Filter filter = Filter.create();
        QueryParser<M, Field<M>, ID> queryParser = createQueryParser(dataSet, query);
        if (queryParser.isValid()) {
            filter = Filter.create(queryParser.parse());
        } else {
            String reason = queryParser.validate();
            setMessage(INVALID_FILTER_PREFIX + reason);
            LOGGER.warn("Failed to parse query '{}', reason: {}", query, reason);
        }
        try {
            dataSet.validate(filter);
        } catch (Exception e) {
            filter = null;
            String reason = e.getMessage();
            setMessage(INVALID_FILTER_PREFIX + reason);
            String message = "Failed to validate query '" + query + "', reason: " + reason;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(message, e);
            } else {
                LOGGER.warn(message);
            }
        }
        return filter;
    }

    private Filter addRangeFilter(DataSet<M, Field<M>, ID> dataSet, String rangeParameter, Filter filter) {
        if (!hasTimeRange()) return filter;
        String[] rangeParts = split(rangeParameter, DATE_RANGE_SEPARATOR);
        if (rangeParts.length == 0) rangeParts = getDefaultRange(dataSet);
        if (rangeParts.length == 0) return filter;
        if (!(rangeParts.length == 1 || rangeParts.length == 2)) {
            setMessage(INVALID_FILTER_PREFIX + "date/time range requires two components");
        } else {
            Field<M> timestampField = dataSet.getMetadata().findTimestampField();
            ZonedDateTime startTime;
            ZonedDateTime endTime;
            if (rangeParts.length == 1) {
                startTime = Field.from(rangeParts[0], ZonedDateTime.class);
                endTime = atEndOfDay(startTime);
            } else {
                startTime = Field.from(rangeParts[0], ZonedDateTime.class);
                endTime = Field.from(rangeParts[1], ZonedDateTime.class);
            }
            Expression timeExpression = ComparisonExpression.between(timestampField.getName(), startTime, endTime);
            LogicalExpression finalExpression = LogicalExpression.and(timeExpression, filter.getExpression());
            filter = Filter.create(finalExpression, filter.getOffset(), filter.getLimit());
        }
        return filter;
    }

    private String[] getDefaultRange(DataSet<M, Field<M>, ID> dataSet) {
        String[] range = EMPTY_STRING_ARRAY;
        String[] defaultRange = getDataSetAnnotation().range();
        if (defaultRange.length > 0) {
            range = new String[2];
            if (defaultRange.length == 1) {
                range[0] = Field.from(defaultRange[0], ZonedDateTime.class).toString();
                range[1] = atEndOfDay(Field.from(defaultRange[0], ZonedDateTime.class)).toString();
            } else if (defaultRange.length == 2) {
                range[0] = Field.from(defaultRange[0], ZonedDateTime.class).toString();
                range[1] = atEndOfDay(Field.from(defaultRange[1], ZonedDateTime.class)).toString();
            }
        }
        return range;
    }

    private Sort getDefaultSort() {
        List<Sort.Order> orders = new ArrayList<>();
        Metadata<M, Field<M>, ID> metadata = getDataSet().getMetadata();
        for (Field<M> field : metadata.getFields()) {
            OrderBy orderByAnnot = field.findAnnotation(OrderBy.class);
            if (orderByAnnot != null) {
                orders.add(Sort.Order.by(field.getName()).with(orderByAnnot.value() == OrderBy.Direction.ASC ? Sort.Direction.ASC : Sort.Direction.DESC));
            }
        }
        if (orders.isEmpty()) {
            for (Field<M> field : metadata.getNameFields()) {
                orders.add(Sort.Order.by(field.getName()));
            }
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    private QueryParser<M, Field<M>, ID> createQueryParser(DataSet<M, Field<M>, ID> dataSet, String query) {
        return new QueryParser<>(dataSet.getMetadata(), query).addDefaultFields(getStringFields(dataSet));
    }

    private List<String> getStringFields(DataSet<M, Field<M>, ID> dataSet) {
        return dataSet.getMetadata().getFields(Field.DataType.STRING).stream().filter(this::isSearchable).map(Field::getName).toList();
    }

    private boolean isSearchable(Field<M> field) {
        Searchable searchableAnnot = field.findAnnotation(Searchable.class);
        if (searchableAnnot != null) {
            return searchableAnnot.value();
        } else {
            return true;
        }
    }

    private ZonedDateTime atEndOfDay(ZonedDateTime dateTime) {
        return dateTime.plusDays(1).minusSeconds(1);
    }

    private void doBeforePersist(DataSet<M, Field<M>, ID> dataSet, M dataSetModel, State state) {
        try {
            beforePersist(dataSet, dataSetModel, state);
        } catch (CanceledException e) {
            return;
        }
        dataSet.save(dataSetModel);
        try {
            afterPersist(dataSet, dataSetModel, state);
        } catch (Exception e) {
            LOGGER.atError().setCause(e).log("Failed to invoke after persist callback for data set {} model {}", dataSet.getName(), dataSetModel);
        }
    }

    public static class CanceledException extends RuntimeException {

    }

}
