package net.microfalx.bootstrap.jdbc.jpa;

import net.microfalx.lang.ClassUtils;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Utilities around JPA entities
 */
public class JpaUtils {

    /**
     * Returns the entity class from a {@link JpaRepository}.
     *
     * @param repository the repository
     * @param <T>        the entity type
     * @param <ID>       the entity identifier type
     * @return the entity type
     */
    public static <T, ID> Class<T> getEntityType(JpaRepository<T, ID> repository) {
        return ClassUtils.getClassParametrizedType(repository.getClass(), 0);
    }
}
