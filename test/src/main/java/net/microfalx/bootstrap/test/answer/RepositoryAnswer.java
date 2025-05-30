package net.microfalx.bootstrap.test.answer;

import jakarta.persistence.Id;
import net.microfalx.lang.annotation.NaturalId;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Optional.ofNullable;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A Mockito answer that provides safe behaviors for JPA repositories.
 * <p>
 * This is useful for testing purposes, allowing you to avoid manually setting
 * primary keys in your entities.
 */
public class RepositoryAnswer implements org.mockito.stubbing.Answer<Object> {

    private final AtomicInteger primaryKeyGenerator = new AtomicInteger(10);

    private final Map<Object, Object> entitiesById = new ConcurrentHashMap<>();
    private final Map<Object, Object> entitiesByNaturalId = new ConcurrentHashMap<>();

    /**
     * Mocks a JPA repository and attaches this answer to it.
     *
     * @param repositoryClass the class of the repository to mock
     * @param <T>             the type of the entity
     * @param <ID>            the type of the primary key
     * @param <R>             the type of the repository
     * @return the mocked repository
     */
    public static <T, ID, R extends JpaRepository<T, ID>> R mock(Class<R> repositoryClass) {
        requireNonNull(repositoryClass);
        return Mockito.mock(repositoryClass, new RepositoryAnswer());
    }

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        return switch (methodName) {
            case "saveAndFlush", "save" -> {
                Object jpaBean = invocation.getArgument(0);
                yield injectPrimaryKey(jpaBean);
            }
            case "findByNaturalId" -> findByNaturalId(invocation.getArgument(0));
            case "findById" -> findById(invocation.getArgument(0));
            default -> Mockito.RETURNS_SMART_NULLS.answer(invocation);
        };
    }

    private Object injectPrimaryKey(Object object) {
        Object primaryKey = getPrimaryKey(object);
        if (primaryKey == null) {
            primaryKey = primaryKeyGenerator.getAndIncrement();
            setPrimaryKey(object, primaryKey);
        }
        entitiesById.put(primaryKey, object);
        Object naturalKey = getNaturalKey(object);
        if (naturalKey != null) {
            entitiesByNaturalId.put(naturalKey, object);
        }
        return object;
    }

    private Object findById(Object id) {
        requireNonNull(id);
        return ofNullable(entitiesById.get(id));
    }

    private Object findByNaturalId(Object id) {
        requireNonNull(id);
        return ofNullable(entitiesByNaturalId.get(id));
    }

    private Object getPrimaryKey(Object object) {
        AtomicReference<Object> primaryKey = new AtomicReference<>();
        ReflectionUtils.doWithFields(object.getClass(), field -> {
            if (isPrimaryKey(field)) {
                field.setAccessible(true);
                primaryKey.set(field.get(object));
            }
        });
        return primaryKey.get();
    }

    private void setPrimaryKey(Object object, Object value) {
        ReflectionUtils.doWithFields(object.getClass(), field -> {
            if (isPrimaryKey(field)) {
                field.setAccessible(true);
                field.set(object, value);
            }
        });
    }

    private Object getNaturalKey(Object object) {
        AtomicReference<Object> naturalKey = new AtomicReference<>();
        ReflectionUtils.doWithFields(object.getClass(), field -> {
            if (isNaturalKey(field)) {
                field.setAccessible(true);
                naturalKey.set(field.get(object));
            }
        });
        return naturalKey.get();
    }

    private void setNaturalKey(Object object, Object value) {
        ReflectionUtils.doWithFields(object.getClass(), field -> {
            if (isNaturalKey(field)) {
                field.setAccessible(true);
                field.set(object, value);
            }
        });
    }

    private boolean isPrimaryKey(Field field) {
        return field.isAnnotationPresent(Id.class);
    }

    private boolean isNaturalKey(Field field) {
        return field.isAnnotationPresent(NaturalId.class) || field.isAnnotationPresent(org.hibernate.annotations.NaturalId.class);
    }


}
