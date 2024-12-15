package net.microfalx.bootstrap.dataset;

import com.google.common.collect.Lists;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.Lookup;
import net.microfalx.bootstrap.dataset.annotation.*;
import net.microfalx.bootstrap.dataset.formatter.EnumFormatter;
import net.microfalx.bootstrap.dataset.formatter.Formatter;
import net.microfalx.bootstrap.dataset.formatter.FormatterUtils;
import net.microfalx.bootstrap.dataset.formatter.NumberFormatter;
import net.microfalx.bootstrap.metrics.Matrix;
import net.microfalx.bootstrap.model.*;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Label;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.ReadOnly;
import net.microfalx.lang.annotation.Visible;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.*;

import static net.microfalx.bootstrap.dataset.DataSetUtils.METRICS;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.*;

/**
 * Base class for all data sets.
 */
@net.microfalx.bootstrap.dataset.annotation.DataSet
public abstract class AbstractDataSet<M, F extends Field<M>, ID> implements DataSet<M, F, ID>, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetService.class);

    private final DataSetFactory<M, F, ID> factory;
    private final Metadata<M, F, ID> metadata;
    private String name;
    private boolean readOnly;
    private State state = State.BROWSE;

    DataSetService dataSetService;
    private List<F> browsableFields;
    private List<F> viewableFields;
    private List<F> editableFields;
    private List<F> appendableFields;

    public AbstractDataSet(DataSetFactory<M, F, ID> factory, Metadata<M, F, ID> metadata) {
        requireNonNull(factory);
        requireNonNull(metadata);

        this.factory = factory;
        this.metadata = metadata;
        initFromMetadata();
    }

    @Override
    public String getId() {
        return metadata.getId();
    }

    @Override
    public final String getName() {
        return defaultIfEmpty(name, metadata.getName());
    }

    @Override
    public final void setName(String name) {
        requireNotEmpty(name);
        this.name = name;
    }

    @Override
    public final DataSetFactory<M, F, ID> getFactory() {
        return factory;
    }

    @Override
    public final Metadata<M, F, ID> getMetadata() {
        return metadata;
    }

    @Override
    public final boolean isReadOnly() {
        return readOnly;
    }

    protected final void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public final State getState() {
        return state;
    }

    public final DataSet<M, F, ID> setState(State state) {
        requireNonNull(state);
        if (state != State.BROWSE) checkIfBrowse();
        this.state = state;
        return this;
    }

    @Override
    public final DataSet<M, F, ID> view() {
        return setState(State.VIEW);
    }

    @Override
    public final DataSet<M, F, ID> edit() {
        return setState(State.EDIT);
    }

    @Override
    public final DataSet<M, F, ID> add() {
        return setState(State.ADD);
    }

    @Override
    public final boolean isVisible(Field<M> field) {
        requireNonNull(field);
        Visible visibleAnnot = field.findAnnotation(Visible.class);
        if (visibleAnnot == null) return !field.isId();
        if (!visibleAnnot.value()) return false;
        return switch (state) {
            case BROWSE, DELETE -> ArrayUtils.contains(visibleAnnot.modes(), Visible.Mode.BROWSE);
            case VIEW -> ArrayUtils.contains(visibleAnnot.modes(), Visible.Mode.VIEW);
            case ADD -> ArrayUtils.contains(visibleAnnot.modes(), Visible.Mode.ADD);
            case EDIT -> ArrayUtils.contains(visibleAnnot.modes(), Visible.Mode.EDIT);
        };
    }

    @Override
    public boolean isReadOnly(Field<M> field) {
        requireNonNull(field);
        ReadOnly readOnlyAnnot = field.findAnnotation(ReadOnly.class);
        if (readOnlyAnnot == null || !readOnlyAnnot.value()) return false;
        return switch (state) {
            case ADD -> ArrayUtils.contains(readOnlyAnnot.modes(), ReadOnly.Mode.ADD);
            case EDIT -> ArrayUtils.contains(readOnlyAnnot.modes(), ReadOnly.Mode.EDIT);
            default -> false;
        };
    }

    @Override
    public boolean isSearchable(Field<M> field) {
        Searchable searchableAnnot = field.findAnnotation(Searchable.class);
        boolean canBeSearched = field.getDataType() == Field.DataType.STRING;
        return (canBeSearched && searchableAnnot == null) || (searchableAnnot != null && searchableAnnot.value());
    }

    @Override
    public boolean isSortable(Field<M> field) {
        Sortable sortableAnnot = field.findAnnotation(Sortable.class);
        return sortableAnnot == null || sortableAnnot.value();
    }

    @Override
    public boolean isFilterable(Field<M> field) {
        Filterable filterableAnnot = field.findAnnotation(Filterable.class);
        boolean canBeSearched = field.getDataType() == Field.DataType.STRING
                || field.getDataType() == Field.DataType.ENUM
                || field.getDataType() == Field.DataType.MODEL
                || field.getDataType() == Field.DataType.BOOLEAN;
        boolean canBeFiltered = (canBeSearched && filterableAnnot == null) || (filterableAnnot != null && filterableAnnot.value());
        return !field.isTransient() && canBeFiltered;
    }

    @Override
    public final List<F> getVisibleFields() {
        switch (state) {
            case BROWSE -> {
                if (browsableFields == null) browsableFields = getVisibleAndOrderedFields();
                return browsableFields;
            }
            case VIEW -> {
                if (viewableFields == null) viewableFields = getVisibleAndOrderedFields();
                return viewableFields;
            }
            case ADD -> {
                if (editableFields == null) editableFields = getVisibleAndOrderedFields();
                return editableFields;
            }
            case EDIT -> {
                if (appendableFields == null) appendableFields = getVisibleAndOrderedFields();
                return appendableFields;
            }
            default -> throw new DataSetException("Unhandled state: " + state);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final String getDisplayValue(M model, Field<M> field) {
        requireNonNull(field);
        if (model == null) return null;
        Object value = field.get(model);
        if (value == null) return null;
        return METRICS.time("Get Display Value", () -> {
            String displayValue = null;
            Formattable formattableAnnot = field.findAnnotation(Formattable.class);
            Renderable renderableAnnot = field.findAnnotation(Renderable.class);
            if (formattableAnnot != null && formattableAnnot.formatter() != Formatter.class) {
                displayValue = createFormatter(field, formattableAnnot).format(value, (F) field, model);
            } else if (renderableAnnot != null && renderableAnnot.discard()) {
                displayValue = EMPTY_STRING;
            } else {
                Lookup lookupAnnot = field.findAnnotation(Lookup.class);
                if (lookupAnnot != null) {
                    LookupProvider<net.microfalx.bootstrap.dataset.Lookup<Object>, Object> lookupProvider = getDataSetService().getLookupProvider(lookupAnnot.model());
                    Optional<?> lookupModel = lookupProvider.findById(value);
                    if (lookupModel.isPresent()) {
                        displayValue = ((net.microfalx.bootstrap.dataset.Lookup) lookupModel.get()).getName();
                    }
                } else if (value instanceof Enum) {
                    if (ENUM_FORMATTER.getI18n() == null) ENUM_FORMATTER.setI18n(getService(I18nService.class));
                    displayValue = ((Formatter<M, Field<M>, Object>) ENUM_FORMATTER).format(value, field, model);
                } else if (value instanceof Number) {
                    displayValue = ((Formatter<M, Field<M>, Object>) NUMBER_FORMATTER).format(value, field, model);
                } else if (field.getDataType().isStructure()) {
                    MetadataService metadataService = dataSetService.getBean(MetadataService.class);
                    Metadata modelMetadata = metadataService.getMetadata(field.getGenericDataClass());
                    StringBuilder builder = new StringBuilder();
                    Collection<Object> values = (Collection<Object>) value;
                    for (Object cvalue : values) {
                        displayValue = modelMetadata.getName(cvalue);
                        StringUtils.append(builder, displayValue, COMMA_WITH_SPACE);
                    }
                    displayValue = builder.toString();
                } else if (isJdkType(value)) {
                    displayValue = FormatterUtils.basicFormatting(value, formattableAnnot);
                } else {
                    MetadataService metadataService = dataSetService.getBean(MetadataService.class);
                    Metadata modelMetadata = metadataService.getMetadata(value.getClass());
                    displayValue = modelMetadata.getName(value);
                }
            }
            if (displayValue != null && !isJdkType(value)) {
                getDataSetService().registerByDisplayName(value, displayValue);
            }
            return displayValue;
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> T getValue(String displayValue, Field<M> field) {
        requireNonNull(field);
        if (StringUtils.isEmpty(displayValue)) return null;
        return METRICS.time("Get Value", () -> {
            Formattable formattableAnnot = field.findAnnotation(Formattable.class);
            if (formattableAnnot != null && formattableAnnot.formatter() != Formatter.class) {
                Formatter<M, F, T> formatter = (Formatter<M, F, T>) createFormatter(field, formattableAnnot);
                return formatter.parse(displayValue, (F) field);
            } else if (field.getDataType() == Field.DataType.ENUM) {
                EnumFormatter formatter = new EnumFormatter<>((Class<Enum>) field.getDataClass());
                return (T) formatter.parse(displayValue, field);
            } else {
                Lookup lookupAnnot = field.findAnnotation(Lookup.class);
                if (lookupAnnot != null) {
                    DataSet<?, ? extends Field<?>, Object> lookupDataSet = getDataSetService().getDataSet(lookupAnnot.model());
                    Object value = lookupDataSet.findByDisplayValue(displayValue).orElse(null);
                    if (value instanceof net.microfalx.bootstrap.dataset.Lookup<?> lookup) {
                        return (T) lookup.getId();
                    } else {
                        return (T) value;
                    }
                } else {
                    return (T) getDataSetService().resolve(field, displayValue);
                }
            }
        });
    }

    @Override
    public final CompositeIdentifier<M, F, ID> getCompositeId(M model) {
        return metadata.getId(model);
    }

    @Override
    public final void setCompositeId(M model, CompositeIdentifier<M, F, ID> id) {

    }

    @Override
    public final ID getId(M model) {
        return getCompositeId(model).toId();
    }

    @Override
    public String getName(M model) {
        return getMetadata().getName(model);
    }

    @Override
    public final void setId(M model, ID id) {
        new CompositeIdentifier<>(metadata, model);
    }

    @Override
    public Map<F, String> validate(M model) {
        return metadata.validate(model);
    }

    @Override
    public void detach(M model) {
        // expected empty
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void validate(Filter filter) {
        METRICS.time("Validate", (t) -> {
            LOGGER.debug("Validate filter " + filter);
            List<ComparisonExpression> comparisonExpressions = filter.getComparisonExpressions();
            for (ComparisonExpression comparisonExpression : comparisonExpressions) {
                Object value = comparisonExpression.getValue();
                LOGGER.debug(" - " + comparisonExpression.getField() + " = " + value);
                F field = getMetadata().find(comparisonExpression.getField());
                if (field == null) {
                    throw new DataSetException("A field with name '" + comparisonExpression.getField() + "' does not exist in data set " + getName());
                }
                if (ComparisonExpression.MATCH_ALL.equals(value)) continue;
                validate(field, value, comparisonExpression);
            }
        });
    }

    @Override
    public final List<M> findAll() {
        return METRICS.time("Find All", () -> doFindAll());
    }

    @Override
    public final List<M> findAllById(Iterable<ID> ids) {
        return METRICS.time("Find All By Id", () -> doFindAllById(ids));
    }

    @Override
    public final Optional<M> findById(ID id) {
        requireNonNull(id);
        return METRICS.time("Find By Id", () -> doFindById(id));
    }

    @Override
    public final boolean existsById(ID id) {
        return METRICS.time("Exists By Id", () -> doExistsById(id));
    }

    @Override
    public final long count() {
        return findAll().size();
    }

    @Override
    public final List<M> findAll(Sort sort) {
        return doFindAll(sort);
    }

    @Override
    public final Page<M> findAll(Pageable pageable) {
        return doFindAll(pageable);
    }

    @Override
    public final Page<M> findAll(Pageable pageable, Filter filterable) {
        return doFindAll(pageable, filterable);
    }

    @Override
    public Optional<M> findByDisplayValue(String displayValue) {
        return doFindByDisplayValue(displayValue);
    }

    @Override
    public final <S extends M> S save(S model) {
        checkReadOnly();
        return METRICS.time("Save", () -> doSave(model));
    }

    @Override
    public <S extends M> List<S> saveAll(Iterable<S> entities) {
        for (S entity : entities) {
            save(entity);
        }
        return Lists.newArrayList(entities);
    }

    @Override
    public final void deleteById(ID id) {
        checkReadOnly();
        METRICS.time("Delete By Id", (t) -> doDeleteById(id));
    }

    @Override
    public final void delete(M model) {
        checkReadOnly();
        METRICS.time("Delete", (t) -> doDelete(model));
    }

    @Override
    public final void deleteAllById(Iterable<? extends ID> ids) {
        checkReadOnly();
        METRICS.time("Delete All By Id", (t) -> doDeleteAllById(ids));
    }

    @Override
    public final void deleteAll(Iterable<? extends M> models) {
        checkReadOnly();
        METRICS.time("Delete All", (t) -> doDeleteAll(models));
    }

    @Override
    public final void deleteAll() {
        METRICS.time("Delete All", (t) -> doDeleteAll());
    }

    @Override
    public Matrix getTrend(Filter filterable, int points) {
        return throwUnsupported();
    }

    @Override
    public Collection<Matrix> getTrend(Filter filterable, Set<String> fields, int points) {
        return throwUnsupported();
    }

    @Override
    public Set<String> getTrendFields() {
        return Collections.emptySet();
    }

    @Override
    public int getTrendTermCount(String fieldName) {
        return 0;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // empty by default
    }

    /**
     * Returns the data set service.
     *
     * @return a non-null instance
     */
    protected final DataSetService getDataSetService() {
        return getService(DataSetService.class);
    }

    /**
     * Finds a service by class.
     *
     * @param serviceClass the service class
     * @param <S>          the service type
     * @return the service instance
     */
    protected final <S> S getService(Class<S> serviceClass) {
        return dataSetService.getBean(serviceClass);
    }

    protected List<M> doFindAll() {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnot = AnnotationUtils.getAnnotation(this, net.microfalx.bootstrap.dataset.annotation.DataSet.class);
        return doFindAll(Pageable.ofSize(dataSetAnnot.pageSize()).withPage(0)).getContent();
    }

    protected List<M> doFindAllById(Iterable<ID> ids) {
        return throwUnsupported();
    }

    protected Optional<M> doFindById(ID id) {
        for (M model : findAll()) {
            ID modelId = getId(model);
            if (id.equals(modelId)) return Optional.of(model);
        }
        return Optional.empty();
    }

    protected boolean doExistsById(ID id) {
        return doFindById(id) != null;
    }

    protected long doCount() {
        return throwUnsupported();
    }

    protected List<M> doFindAll(Sort sort) {
        return throwUnsupported();
    }

    protected Page<M> doFindAll(Pageable pageable) {
        return throwUnsupported();
    }

    protected Page<M> doFindAll(Pageable pageable, Filter filterable) {
        return throwUnsupported();
    }

    protected Optional<M> doFindByDisplayValue(String displayValue) {
        return throwUnsupported();
    }

    protected <S extends M> S doSave(S model) {
        return throwUnsupported();
    }

    protected void doDeleteById(ID id) {
        throwUnsupported();
    }

    protected void doDelete(M model) {
        throwUnsupported();
    }

    protected void doDeleteAllById(Iterable<? extends ID> ids) {
        throwUnsupported();
    }

    protected void doDeleteAll(Iterable<? extends M> models) {
        throwUnsupported();
    }

    protected void doDeleteAll() {
        throwUnsupported();
    }

    protected final void checkReadOnly() {
        if (isReadOnly()) throw new DataSetException("The data set '" + getMetadata().getName() + "' is read only");
    }

    protected final <T> T throwUnsupported() {
        throw new DataSetException("Unsupported operation for data set '" + getMetadata().getName() + "'");
    }

    /**
     * Filters and sorts a list of models.
     *
     * @param models     the models
     * @param filterable the filter
     * @param sort       the sort
     * @return a non-null instance
     */
    protected final List<M> filterAndSort(Iterable<M> models, Filter filterable, net.microfalx.bootstrap.model.Sort sort) {
        return DataSetUtils.filterAndSort(getMetadata(), models, filterable, sort);
    }

    /**
     * Sorts a list of models.
     *
     * @param models the models
     * @param sort   the sort
     * @return a non-null instance
     */
    protected final List<M> sort(Iterable<M> models, Sort sort) {
        return DataSetUtils.sort(getMetadata(), models, sort);
    }

    /**
     * Paginates a list of models.
     *
     * @param models   the models
     * @param pageable the page information
     * @return a page of models
     */
    protected final Page<M> getPage(List<M> models, Pageable pageable) {
        return DataSetUtils.getPage(getMetadata(), models, pageable);
    }

    /**
     * Filters and paginates a list of models.
     *
     * @param models     the models
     * @param pageable   the page information
     * @param filterable the filter
     * @return a page of models
     */
    protected final Page<M> getPage(List<M> models, Pageable pageable, Filter filterable) {
        return DataSetUtils.getPage(getMetadata(), models, pageable, filterable);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "factory=" + ClassUtils.getName(factory) + ", metadata=" + metadata + '}';
    }

    private void validate(F field, Object value, ComparisonExpression comparisonExpression) {
        try {
            switch (field.getDataType()) {
                case ENUM:
                    if (!(value instanceof Enum<?>)) {
                        Enum<?> resolvedEnum = EnumUtils.fromName((Class<Enum>) field.getDataClass(), Field.from(value, String.class));
                        LOGGER.debug("   - " + resolvedEnum);
                    }
                    break;
                case DATE:
                    if (!(value instanceof Temporal)) {
                        LocalDate date = Field.from(value, LocalDate.class);
                        LOGGER.debug("   - " + date);
                    }
                    break;
                case DATE_TIME:
                    if (!(value instanceof Temporal)) {
                        LocalDateTime dateTime = Field.from(value, LocalDateTime.class);
                        LOGGER.debug("   - " + dateTime);
                    }
                    break;
            }
        } catch (Exception e) {
            throw new DataSetException("A data conversion failure occurred for field with name '"
                    + comparisonExpression.getField() + "', reason: " + e.getMessage());
        }
    }

    private void initFromMetadata() {
        ReadOnly readOnlyAnnot = metadata.findAnnotation(ReadOnly.class);
        if (readOnlyAnnot != null) this.readOnly = readOnlyAnnot.value();
        Name nameAnnot = metadata.findAnnotation(Name.class);
        if (nameAnnot != null) setName(nameAnnot.value());
        Label labelAnnot = metadata.findAnnotation(Label.class);
        if (labelAnnot != null) setName(labelAnnot.value());
    }

    private List<F> getVisibleAndOrderedFields() {
        return getMetadata().getFields().stream().filter(this::isVisible)
                .sorted(Comparator.comparing(Field::getPosition))
                .toList();
    }

    private void checkIfBrowse() {
        if (state != State.BROWSE) throw new DataSetException("The data set is not in BROWSE state");
    }

    private boolean isJdkType(Object value) {
        if (value == null) return false;
        return value.getClass().getClassLoader() == StringUtils.NA_STRING.getClass().getClassLoader();
    }

    private Formatter<M, F, Object> createFormatter(Field<M> field, Formattable formattable) {
        try {
            return formattable.formatter().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new DataSetException("Failed to create formatter for field '" + field.getName() + "'", e);
        }
    }

    static final EnumFormatter ENUM_FORMATTER = new EnumFormatter<>();
    static final NumberFormatter NUMBER_FORMATTER = new NumberFormatter<>();
}
