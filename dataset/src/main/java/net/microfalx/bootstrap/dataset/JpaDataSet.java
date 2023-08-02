package net.microfalx.bootstrap.dataset;

import jakarta.persistence.Entity;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.JpaField;
import net.microfalx.bootstrap.model.Metadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

import static net.microfalx.lang.AnnotationUtils.getAnnotation;

/**
 * A data set for JPA entities.
 */
public class JpaDataSet<M, ID> extends PojoDataSet<M, JpaField<M>, ID> {

    JpaRepository<M, ID> repository;

    public JpaDataSet(DataSetFactory<M, JpaField<M>, ID> factory, Metadata<M, JpaField<M>, ID> metadata) {
        super(factory, metadata);
    }

    @Override
    public <S extends M> List<S> saveAll(Iterable<S> entities) {
        return repository.saveAll(entities);
    }

    @Override
    protected List<M> doFindAll() {
        return repository.findAll();
    }

    @Override
    protected List<M> doFindAllById(Iterable<ID> ids) {
        return repository.findAllById(ids);
    }

    @Override
    protected Optional<M> doFindById(ID id) {
        return repository.findById(id);
    }

    @Override
    protected boolean doExistsById(ID id) {
        return repository.existsById(id);
    }

    @Override
    protected long doCount() {
        return repository.count();
    }

    @Override
    protected List<M> doFindAll(Sort sort) {
        return repository.findAll(sort);
    }

    @Override
    protected Page<M> doFindAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    protected Page<M> doFindAll(Pageable pageable, Filter filterable) {
        if (repository instanceof JpaSpecificationExecutor && filterable != null) {
            JpaSpecificationExecutor<M> executor = (JpaSpecificationExecutor<M>) repository;
            Specification<M> specification = null;
            return executor.findAll(specification, pageable);
        } else {
            return findAll(pageable);
        }
    }

    @Override
    protected <S extends M> S doSave(S model) {
        return repository.save(model);
    }

    @Override
    protected void doDeleteById(ID id) {
        repository.deleteById(id);
    }

    @Override
    protected void doDelete(M model) {
        repository.delete(model);
    }

    @Override
    protected void doDeleteAllById(Iterable<? extends ID> ids) {
        repository.deleteAllById(ids);
    }

    @Override
    protected void doDeleteAll(Iterable<? extends M> models) {
        repository.deleteAll(models);
    }

    @Override
    protected void doDeleteAll() {
        repository.deleteAll();
    }

    void setRepository(JpaRepository<M, ID> repository) {
        this.repository = repository;
    }

    public static class Factory<M, ID> extends PojoDataSetFactory<M, JpaField<M>, ID> {

        @Override
        protected AbstractDataSet<M, JpaField<M>, ID> doCreate(Metadata<M, JpaField<M>, ID> metadata) {
            return new JpaDataSet<>(this, metadata);
        }

        @SuppressWarnings("unchecked")
        @Override
        void update(AbstractDataSet<M, JpaField<M>, ID> dataSet, Object... parameters) {
            super.update(dataSet, parameters);
            JpaRepository<M, ID> repository = find(JpaRepository.class, parameters);
            if (repository == null) {
                throw new DataSetException("A JPA repository is required to create a data set for " + dataSet.getMetadata().getName());
            }
            ((JpaDataSet<M, JpaField<M>>) dataSet).setRepository((JpaRepository<M, JpaField<M>>) repository);
        }

        @Override
        public boolean supports(Metadata<M, JpaField<M>, ID> metadata) {
            return getAnnotation(metadata.getModel(), Entity.class) != null;
        }
    }
}
