package net.microfalx.bootstrap.jdbc.jpa;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.lang.ClassUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
