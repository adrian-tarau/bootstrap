package net.microfalx.bootstrap.dataset;

import com.google.common.collect.Lists;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.Lookup;
import net.microfalx.bootstrap.dataset.annotation.Searchable;
import net.microfalx.bootstrap.dataset.formatter.EnumFormatter;
import net.microfalx.bootstrap.dataset.formatter.Formatter;
import net.microfalx.bootstrap.dataset.formatter.FormatterUtils;
import net.microfalx.bootstrap.dataset.formatter.NumberFormatter;
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
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;

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

    ApplicationContext applicationContext;
    private List<Field<M>> browsableFields;
    private List<Field<M>> viewableFields;
    private List<Field<M>> editableFields;
    private List<Field<M>> appendableFields;

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
            case BROWSE -> ArrayUtils.contains(visibleAnnot.modes(), Visible.Mode.BROWSE);
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
        return canBeSearched && (searchableAnnot == null || !searchableAnnot.value());
    }

    @Override
    public boolean isFilterable(Field<M> field) {
        Filterable filterableAnnot = field.findAnnotation(Filterable.class);
        boolean canBeSearched = field.getDataType() == Field.DataType.STRING
                || field.getDataType() == Field.DataType.ENUM
                || field.getDataType() == Field.DataType.MODEL
                || field.getDataType() == Field.DataType.BOOLEAN;
        return canBeSearched && (filterableAnnot == null || !filterableAnnot.value());
    }

    @Override
    public final List<Field<M>> getVisibleFields() {
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
        Formattable formattableAnnot = field.findAnnotation(Formattable.class);
        if (formattableAnnot != null && formattableAnnot.formatter() != Formatter.class) {
            return createFormatter(field, formattableAnnot).format(value, (F) field, model);
        } else {
            if (value == null) return null;
            Lookup lookupAnnot = field.findAnnotation(Lookup.class);
            if (lookupAnnot != null) {
                DataSet<?, ? extends Field<?>, Object> lookupDataSet = getDataSetService().lookup(lookupAnnot.model());
                Optional<?> lookupModel = lookupDataSet.findById(value);
                if (lookupModel.isPresent()) {
                    return ((net.microfalx.bootstrap.dataset.Lookup) lookupModel.get()).getName();
                }
            }
            if (value instanceof Enum) {
                return ((Formatter<M, Field<M>, Object>) ENUM_FORMATTER).format(value, field, model);
            } else if (value instanceof Number) {
                return ((Formatter<M, Field<M>, Object>) NUMBER_FORMATTER).format(value, field, model);
            } else if (isJdkType(value)) {
                return FormatterUtils.basicFormatting(value, formattableAnnot);
            } else {
                MetadataService metadataService = applicationContext.getBean(MetadataService.class);
                Metadata modelMetadata = metadataService.getMetadata(value.getClass());
                return modelMetadata.getName(value);
            }
        }
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
    public final void setId(M model, ID id) {
        new CompositeIdentifier<>(metadata, model);
    }

    @Override
    public Map<F, String> validate(M model) {
        return metadata.validate(model);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void validate(Filter filter) {
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
    }

    @Override
    public final List<M> findAll() {
        return doFindAll();
    }

    @Override
    public final List<M> findAllById(Iterable<ID> ids) {
        return doFindAllById(ids);
    }

    @Override
    public final Optional<M> findById(ID id) {
        requireNonNull(id);
        return doFindById(id);
    }

    @Override
    public final boolean existsById(ID id) {
        return doExistsById(id);
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
    public final <S extends M> S save(S model) {
        checkReadOnly();
        return doSave(model);
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
        doDeleteById(id);
    }

    @Override
    public final void delete(M model) {
        checkReadOnly();
        doDelete(model);
    }

    @Override
    public final void deleteAllById(Iterable<? extends ID> ids) {
        checkReadOnly();
        doDeleteAllById(ids);
    }

    @Override
    public final void deleteAll(Iterable<? extends M> models) {
        checkReadOnly();
        doDeleteAll(models);
    }

    @Override
    public final void deleteAll() {
        doDeleteAll();
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
        return applicationContext.getBean(serviceClass);
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "factory=" + ClassUtils.getName(factory) + ", metadata=" + metadata + '}';
    }

    private void initFromMetadata() {
        ReadOnly readOnlyAnnot = metadata.findAnnotation(ReadOnly.class);
        if (readOnlyAnnot != null) this.readOnly = readOnlyAnnot.value();
        Name nameAnnot = metadata.findAnnotation(Name.class);
        if (nameAnnot != null) setName(nameAnnot.value());
        Label labelAnnot = metadata.findAnnotation(Label.class);
        if (labelAnnot != null) setName(labelAnnot.value());
    }

    private List<Field<M>> getVisibleAndOrderedFields() {
        return getMetadata().getFields().stream().filter(this::isVisible)
                .sorted(Comparator.comparing(Field::getPosition))
                .collect(Collectors.toUnmodifiableList());
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

    private static final Formatter<?, ?, ?> ENUM_FORMATTER = new EnumFormatter<>();
    private static final Formatter<?, ?, ?> NUMBER_FORMATTER = new NumberFormatter<>();
}
