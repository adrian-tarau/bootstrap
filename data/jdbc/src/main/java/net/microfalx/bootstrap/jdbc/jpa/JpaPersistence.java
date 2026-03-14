package net.microfalx.bootstrap.jdbc.jpa;

import jakarta.persistence.Id;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Mapper;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.annotation.NaturalId;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.function.BiConsumer;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for a class which holds a collection of JPA repositories.
 * <p>
 * Subclasses would usually define themselves as a {@link org.springframework.stereotype.Component Spring component}, register all JPA repositories
 * needed for a given service.
 * <p>
 * In additional to standard JPA repositories, this class provides a convenient way to create
 * and use {@link NaturalIdEntityUpdater} for entities that have a natural ID.
 */
public abstract class JpaPersistence extends ApplicationContextSupport {

    /**
     * Returns an updater for the given entity class.
     *
     * @param repository the repository class
     * @param <M>        the entity type
     * @param <ID>       the entity ID type
     * @return a non-null instance
     */
    @SuppressWarnings({"unchecked"})
    public final <M, ID> NaturalIdEntityUpdater<M, ID> getUpdater(JpaRepository<M, ID> repository) {
        requireNonNull(repository);
        Class<?>[] jpaInterfaces = AopProxyUtils.proxiedUserInterfaces(repository);
        for (Class<?> jpaInterface : jpaInterfaces) {
            if (ClassUtils.isSubClassOf(jpaInterface, NaturalJpaRepository.class)) {
                return getUpdater((Class<? extends NaturalJpaRepository<M, ID>>) jpaInterface);
            }
        }
        throw new IllegalArgumentException("The repository must be an instance of NaturalJpaRepository");
    }

    /**
     * Returns an updater for the given entity class.
     *
     * @param repositoryClass the repository class
     * @param <M>             the entity type
     * @param <ID>            the entity ID type
     * @return a non-null instance
     */
    public final <M, ID> NaturalIdEntityUpdater<M, ID> getUpdater(Class<? extends NaturalJpaRepository<M, ID>> repositoryClass) {
        requireNonNull(repositoryClass);
        NaturalJpaRepository<M, ID> repository = getBean(repositoryClass);
        NaturalIdEntityUpdater<M, ID> updater = new NaturalIdEntityUpdater<>(getBean(MetadataService.class), repository);
        updater.setApplicationContext(getApplicationContext());
        return updater;
    }

    /**
     * Creates a mapper for the given source and destination classes.
     * <p>
     * The mapper will ignore all fields annotated with {@link Id} and {@link NaturalId}, complex types and will use a
     * {@link NaturalKeyFieldCallback} to copy the natural ID from the source to the destination instance.
     *
     * @param sourceClass      the source class
     * @param destinationClass the destination class
     * @param <S>              the source type
     * @param <D>              the destination type
     */
    public final <S, D> Mapper.Builder<S, D> createMapper(Class<S> sourceClass, Class<D> destinationClass) {
        requireNonNull(sourceClass);
        requireNonNull(destinationClass);
        return Mapper.builder(sourceClass, destinationClass)
                .ignoreAnnotations(net.microfalx.lang.annotation.Id.class, NaturalId.class,
                        jakarta.persistence.Id.class, org.hibernate.annotations.NaturalId.class)
                .ignoredFieldsCallback(new NaturalKeyFieldCallback<>()).ignoreComplexTypes(true);
    }

    /**
     * Registers a mapper for the given source and destination classes.
     * <p>
     * The mapper will ignore all fields annotated with {@link Id} and {@link NaturalId}, complex types and will use a
     * {@link NaturalKeyFieldCallback} to copy the natural ID from the source to the destination instance.
     *
     * @param sourceClass      the source class
     * @param destinationClass the destination class
     * @param <S>              the source type
     * @param <D>              the destination type
     */
    public final <S, D> void registerMapper(Class<S> sourceClass, Class<D> destinationClass) {
        createMapper(sourceClass, destinationClass).register();
    }

    /**
     * Registers a mapper for the given source and destination classes. The mapper will ignore all fields annotated
     * with {@link Id} and {@link NaturalId}.
     *
     * @param sourceClass      the source class
     * @param destinationClass the destination class
     * @param mapper           the field mapper to be used for mapping the source and destination instances, must not be null
     * @param <S>              the source type
     * @param <D>              the destination type
     */
    public final <S, D> void registerMapper(Class<S> sourceClass, Class<D> destinationClass, BiConsumer<S, D> mapper) {
        createMapper(sourceClass, destinationClass).mapper(mapper).register();
    }

    private static class NaturalKeyFieldCallback<S, D> implements Mapper.FieldCallback<S, D> {

        @Override
        public void doWithField(Mapper.FieldMapper<S, D> fieldMapper, S source, D destination) {
            if (fieldMapper.getSourceField().hasAnnotation(net.microfalx.lang.annotation.Id.class)) {
                Field<D> naturalIdField = fieldMapper.getMapper().getDestinationMetadata().findAnnotated(NaturalId.class);
                if (naturalIdField != null) {
                    Object naturalId = fieldMapper.getSourceField().get(source);
                    naturalIdField.set(destination, naturalId);
                } else {
                    throw new IllegalStateException("The destination class " + ClassUtils.getName(fieldMapper.getMapper().getDestinationMetadata().getModel()
                            + " must have a field annotated with @NaturalId"));
                }
            }
        }
    }
}
