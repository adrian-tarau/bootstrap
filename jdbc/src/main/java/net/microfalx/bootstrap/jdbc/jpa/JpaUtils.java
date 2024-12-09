package net.microfalx.bootstrap.jdbc.jpa;

import net.microfalx.lang.ClassUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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

    /**
     * Returns the username of the current principal.
     * <p>
     * If the security context is not available, the method returns null;
     *
     * @return a non-null instance
     */
    public static String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) return ((UserDetails) principal).getUsername();
        }
        return null;
    }
}
