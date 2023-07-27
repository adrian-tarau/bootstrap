package net.microfalx.bootstrap.web.dataset;

import com.google.common.collect.Lists;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.web.dataset.annotation.Formattable;
import net.microfalx.bootstrap.web.dataset.formatter.Formatter;
import net.microfalx.lang.ArgumentUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.ReadOnly;
import net.microfalx.lang.annotation.Visible;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.microfalx.bootstrap.web.dataset.formatter.FormatterUtils.basicFormatting;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;

/**
 * Base class for all data sets.
 */
public abstract class AbstractDataSet<M, F extends Field<M>, ID> implements DataSet<M, F, ID> {

    private final DataSetFactory<M, F, ID> factory;
    private final Metadata<M, F> metadata;
    private String name;
    private boolean readOnly;
    private State state = State.BROWSE;

    ApplicationContext applicationContext;
    private List<Field<M>> browsableFields;
    private List<Field<M>> editableFields;
    private List<Field<M>> appendableFields;

    public AbstractDataSet(DataSetFactory<M, F, ID> factory, Metadata<M, F> metadata) {
        requireNonNull(factory);
        requireNonNull(metadata);

        this.factory = factory;
        this.metadata = metadata;
        initFromMetadata();
    }

    @Override
    public String getName() {
        return defaultIfEmpty(name, metadata.getName());
    }

    @Override
    public void setName(String name) {
        requireNotEmpty(name);
        this.name = name;
    }

    @Override
    public final DataSetFactory<M, F, ID> getFactory() {
        return factory;
    }

    @Override
    public final Metadata<M, F> getMetadata() {
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

    protected final void setState(State state) {
        this.state = state;
    }

    @Override
    public void edit() {
        checkIfBrowse();
        this.state = State.EDIT;
    }

    @Override
    public void append() {
        checkIfBrowse();
        this.state = State.EDIT;
    }

    @Override
    public final boolean isVisible(Field<M> field) {
        Visible visibleAnnot = field.findAnnotation(Visible.class);
        if (visibleAnnot == null) return true;
        if (!visibleAnnot.value()) return false;
        return switch (state) {
            case BROWSE -> ArrayUtils.contains(visibleAnnot.modes(), Visible.Mode.BROWSE);
            case ADD -> ArrayUtils.contains(visibleAnnot.modes(), Visible.Mode.ADD);
            case EDIT -> ArrayUtils.contains(visibleAnnot.modes(), Visible.Mode.EDIT);
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Field<M>> getVisibleFields() {
        switch (state) {
            case BROWSE:
                if (browsableFields == null) browsableFields = getVisibleAndOrderedFields();
                return browsableFields;
            case ADD:
                if (editableFields == null) editableFields = getVisibleAndOrderedFields();
                return editableFields;
            case EDIT:
                if (appendableFields == null) appendableFields = getVisibleAndOrderedFields();
                return appendableFields;
            default:
                throw new DataSetException("Unhandled state: " + state);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getDisplayValue(M model, Field<M> field) {
        ArgumentUtils.requireNonNull(field);
        if (model == null) return null;
        Object value = field.get(model);
        Formattable formattableAnnot = field.findAnnotation(Formattable.class);
        if (formattableAnnot != null && formattableAnnot.formatter() != Formatter.class) {
            return createFormatter(field, formattableAnnot).format(value, (F) field, model);
        } else {
            if (value == null) return null;
            if (isJdkType(value)) {
                return basicFormatting(value, formattableAnnot);
            } else {
                MetadataService metadataService = applicationContext.getBean(MetadataService.class);
                Metadata modelMetadata = metadataService.getMetadata(value.getClass());
                return modelMetadata.getName(value);
            }
        }
    }

    @Override
    public ID getId(M model) {
        return null;
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
        for (M model : findAll()) {
            ID modelId = getId(model);
            if (id.equals(modelId)) return Optional.of(model);
        }
        return Optional.empty();
    }

    @Override
    public final boolean existsById(ID id) {
        return findById(id).isPresent();
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
        return doFindAll(Pageable.ofSize(100).withPage(0)).getContent();
    }

    protected List<M> doFindAllById(Iterable<ID> ids) {
        return throwUnsupported();
    }

    protected Optional<M> doFindById(ID id) {
        return throwUnsupported();
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
        return "AbstractDataSet{" + "factory=" + factory + ", metadata=" + metadata + '}';
    }

    private void initFromMetadata() {
        ReadOnly readOnlyAnnot = metadata.findAnnotation(ReadOnly.class);
        if (readOnlyAnnot != null) this.readOnly = readOnlyAnnot.value();
    }

    private List<Field<M>> getVisibleAndOrderedFields() {
        return getMetadata().getFields().stream()
                .filter(this::isVisible)
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
}
