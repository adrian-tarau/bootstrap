package net.microfalx.bootstrap.test.answer;

import net.microfalx.lang.ClassUtils;
import org.mockito.Mockito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all answer for {@link Repository}
 */
public abstract class RepositoryAnswer extends AbstractAnswer {

    /**
     * Mocks a Spring repository and attaches default answers to it.
     *
     * @param repositoryClass the class of the repository to mock
     * @param <T>             the type of the entity
     * @param <ID>            the type of the primary key
     * @param <R>             the type of the repository
     * @return the mocked repository
     */
    public static <T, ID, R extends Repository<T, ID>> R mock(Class<R> repositoryClass) {
        requireNonNull(repositoryClass);
        if (ClassUtils.isSubClassOf(repositoryClass, JpaRepository.class)) {
            return Mockito.mock(repositoryClass, new JpaRepositoryAnswer());
        } else {
            throw new IllegalStateException("Unknown repository type: " + ClassUtils.getName(repositoryClass));
        }
    }
}
