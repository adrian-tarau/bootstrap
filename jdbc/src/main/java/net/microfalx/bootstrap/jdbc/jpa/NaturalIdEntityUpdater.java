package net.microfalx.bootstrap.jdbc.jpa;

import net.microfalx.bootstrap.core.config.RetryConfig;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.model.ModelComparator;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.ClassUtils;
import org.hibernate.annotations.NaturalId;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.retry.support.RetryTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static net.microfalx.bootstrap.jdbc.jpa.JpaUtils.getCurrentUserName;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which inserts/updates entities using a natural identifier.
 *
 * @param <M>  the entity type
 * @param <ID> the identifier type
 */
public final class NaturalIdEntityUpdater<M, ID> extends ApplicationContextSupport {

    private final NaturalJpaRepository<M, ID> repository;
    private final MetadataService metadataService;

    private RetryTemplate retryTemplate;
    private Metadata<M, Field<M>, ID> metadata;
    private final Map<String, UpdateStrategy> updateStrategies = new HashMap<>();
    private Map<String, Boolean> updateable = new HashMap<>();

    public NaturalIdEntityUpdater(MetadataService metadataService, NaturalJpaRepository<M, ID> repository) {
        requireNonNull(metadataService);
        requireNonNull(repository);
        this.metadataService = metadataService;
        this.repository = repository;
        updateStrategies();
    }

    /**
     * Returns the repository associated with this instance.
     *
     * @return a non-null instance
     */
    public NaturalJpaRepository<M, ID> getRepository() {
        return repository;
    }

    /**
     * Returns the retry template to be used with this instance.
     *
     * @return the template
     */
    public RetryTemplate getRetryTemplate() {
        if (retryTemplate == null) {
            try {
                retryTemplate = getBean(RetryTemplate.class);
            } catch (NoSuchBeanDefinitionException | IllegalStateException e) {
                // let it pass, we will create one manually
            }
        }
        if (retryTemplate == null) retryTemplate = new RetryConfig().retryTemplate();
        return retryTemplate;
    }

    /**
     * Changes the retry template.
     *
     * @param retryTemplate the template
     * @return self
     */
    public NaturalIdEntityUpdater<M, ID> setRetryTemplate(RetryTemplate retryTemplate) {
        requireNonNull(retryTemplate);
        this.retryTemplate = retryTemplate;
        return this;
    }

    /**
     * Overrides the updatable flag for a field name.
     *
     * @param fieldName  the field name
     * @param updateable {@code true} to allow a field to be updated, {@code false} otheriwse
     * @return self
     * @see UpdateStrategy
     */
    public NaturalIdEntityUpdater<M, ID> setUpdatable(String fieldName, boolean updateable) {
        requireNonNull(fieldName);
        this.updateable.put(fieldName.toLowerCase(), updateable);
        return this;
    }

    /**
     * Finds an entity by its natural identifier.
     * <p>
     * If the entity does not exist, it is created automatically.
     *
     * @param entity the entity
     * @return the entity
     */
    public M findByNaturalIdOrCreate(M entity) {
        requireNonNull(entity);
        return getRetryTemplate().execute(context -> {
            M persistedEntity = find(entity);
            if (persistedEntity == null) {
                updateCreated(entity);
                persistedEntity = repository.saveAndFlush(entity);
            }
            return persistedEntity;
        });
    }

    /**
     * Finds an entity by its natural identifier.
     * <p>
     * If the entity exists but an attribute has changed, updates the entity.
     *
     * @param entity the entity
     * @return the entity
     */
    public M findByNaturalIdAndUpdate(M entity) {
        requireNonNull(entity);
        M persistedEntity = findByNaturalIdOrCreate(entity);
        ModelComparator<M, Field<M>, ID> comparator = new ModelComparator<>(metadataService);
        if (comparator.setTrackChanges(false).compare(entity, persistedEntity) != 0) {
            return getRetryTemplate().execute(context -> {
                updateModified(entity);
                copyFields(persistedEntity, entity);
                return repository.saveAndFlush(entity);
            });
        }
        return persistedEntity;
    }

    private M find(M entity) {
        return repository.findByNaturalId(getNaturalKey(entity)).orElse(null);
    }

    private Field<M> findNaturalKey() {
        Field<M> field = getMetadata().findAnnotated(NaturalId.class);
        if (field == null) field = getMetadata().findAnnotated(net.microfalx.lang.annotation.NaturalId.class);
        return field;
    }

    private String getNaturalKey(M entity) {
        return findNaturalKey().get(entity, String.class);
    }

    private void updateCreated(M entity) {
        Field<M> createdAtField = getMetadata().findCreatedAtField();
        if (createdAtField != null) createdAtField.set(entity, LocalDateTime.now());
        Field<M> createdByField = getMetadata().findCreatedByField();
        if (createdByField != null) createdByField.set(entity, getCurrentUserName());
    }

    private void updateModified(M entity) {
        Field<M> modifiedAtField = getMetadata().findModifiedAtField();
        if (modifiedAtField != null) modifiedAtField.set(entity, LocalDateTime.now());
        Field<M> modifiedByField = getMetadata().findModifiedByField();
        if (modifiedByField != null) modifiedByField.set(entity, getCurrentUserName());
    }

    private void copyFields(M previousEntity, M currentEntity) {
        for (Field<M> idField : getMetadata().getIdFields()) {
            idField.set(currentEntity, idField.get(previousEntity));
        }
        for (Field<M> field : getMetadata().getFields()) {
            if (!shouldUpdate(field)) field.set(currentEntity, field.get(previousEntity));
        }
        Field<M> createdAtField = getMetadata().findCreatedAtField();
        if (createdAtField != null) createdAtField.set(currentEntity, createdAtField.get(previousEntity));
    }

    private boolean shouldUpdate(Field<M> field) {
        Boolean updatable = this.updateable.get(field.getName().toLowerCase());
        if (updatable != null) return updatable;
        UpdateStrategy updateStrategyAnnot = updateStrategies.get(field.getName());
        return updateStrategyAnnot == null || updateStrategyAnnot.updatable();
    }

    private void updateStrategies() {
        UpdateStrategy updateStrategyAnnot = AnnotationUtils.getAnnotation(getMetadata().getModel(), UpdateStrategy.class);
        if (updateStrategyAnnot != null) {
            for (String fieldName : updateStrategyAnnot.fieldNames()) {
                updateStrategies.put(fieldName, updateStrategyAnnot);
            }
        }
        for (Field<M> field : getMetadata().getFields()) {
            updateStrategyAnnot = field.findAnnotation(UpdateStrategy.class);
            if (updateStrategyAnnot != null) updateStrategies.put(field.getName(), updateStrategyAnnot);
        }
    }

    private Metadata<M, Field<M>, ID> getMetadata() {
        if (metadata == null) {
            Class<M> type = JpaUtils.getEntityType(repository);
            if (type == null) {
                throw new IllegalArgumentException("Failed to extract entity type from repository " + ClassUtils.getName(repository));
            }
            metadata = metadataService.getMetadata(type);
        }
        return metadata;
    }


}
