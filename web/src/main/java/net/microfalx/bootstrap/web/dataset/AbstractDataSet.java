package net.microfalx.bootstrap.web.dataset;

import com.google.common.collect.Lists;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all data sets.
 */
public abstract class AbstractDataSet<M, ID> implements DataSet<M, ID> {

    private final DataSetFactory<M, ID> factory;
    private final Metadata<M> metadata;
    private boolean readOnly;

    public AbstractDataSet(DataSetFactory<M, ID> factory, Class<M> modelClass) {
        requireNonNull(factory);
        requireNonNull(modelClass);

        this.factory = factory;
        this.metadata = factory.getMetadata(modelClass);
    }

    @Override
    public final DataSetFactory<M, ID> getFactory() {
        return factory;
    }

    @Override
    public final Metadata<M> getMetadata() {
        return metadata;
    }

    @Override
    public final boolean isReadOnly() {
        return readOnly;
    }

    protected void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
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
    public Page<M> findAll(Pageable pageable, Expression expression) {
        return doFindAll(pageable, expression);
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

    protected List<M> doFindAll() {
        return throwUnsupported();
    }

    protected List<M> doFindAllById(Iterable<ID> ids) {
        return throwUnsupported();
    }

    protected Optional<M> doFindById(ID id) {
        return throwUnsupported();
    }

    protected boolean doExistsById(ID id) {
        return throwUnsupported();
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

    protected Page<M> doFindAll(Pageable pageable, Expression expression) {
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
}
