package net.microfalx.bootstrap.store;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.function.Predicate;

/**
 * A query used to select objects from a store.
 *
 * @param <T> the object type
 */
@Data
@Builder
@ToString
public class Query<T> {

    private static final Predicate<?> INCLUDE_ALL = o -> true;

    private LocalDateTime start;
    private LocalDateTime end;
    @SuppressWarnings("unchecked")
    private Predicate<T> filter = (Predicate<T>) INCLUDE_ALL;
}
