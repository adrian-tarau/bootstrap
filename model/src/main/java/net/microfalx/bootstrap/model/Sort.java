package net.microfalx.bootstrap.model;

import net.microfalx.lang.ObjectUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

public class Sort implements Iterable<Sort.Order> {

    private static final Sort UNSORTED = Sort.create(new Order[0]);
    public static final Direction DEFAULT_DIRECTION = Direction.ASC;

    private final List<Order> orders;

    /**
     * Creates a new {@link Sort} for the given fields.
     *
     * @param fields the fields to sort on, in {@link Direction#ASC} order
     * @return a non-null instance
     */
    public static Sort create(String... fields) {
        requireNonNull(fields);
        return fields.length == 0 ? Sort.unsorted() : new Sort(DEFAULT_DIRECTION, Arrays.asList(fields));
    }

    /**
     * Creates a new {@link Sort} for the given {@link Order}s.
     *
     * @param orders must not be {@literal null}.
     * @return a non-null instance
     */
    public static Sort create(List<Order> orders) {
        requireNonNull(orders);
        return orders.isEmpty() ? Sort.unsorted() : new Sort(orders);
    }

    /**
     * Creates a new {@link Sort} for the given {@link Order}s.
     *
     * @param orders must not be {@literal null}.
     * @return a non-null instance
     */
    public static Sort create(Order... orders) {
        requireNonNull(orders);
        return new Sort(Arrays.asList(orders));
    }

    /**
     * Creates a new {@link Sort} for the given {@link Direction} and properties.
     *
     * @param direction must not be {@literal null}.
     * @param fields    must not be {@literal null}.
     * @return a non-null instance
     */
    public static Sort create(Direction direction, String... fields) {
        if (ObjectUtils.isEmpty(fields)) return UNSORTED;
        requireNonNull(direction);
        requireNonNull(fields);
        if (fields.length == 0) throw new IllegalArgumentException("At least one field must be given");
        return Sort.create(Arrays.stream(fields).map(it -> new Order(direction, it)).collect(Collectors.toList()));
    }

    /**
     * Returns a {@link Sort} instances representing no sorting setup at all.
     *
     * @return a non-null instance
     */
    public static Sort unsorted() {
        return UNSORTED;
    }

    private Sort(List<Order> orders) {
        requireNonNull(orders);
        this.orders = orders;
    }

    private Sort(Direction direction, List<String> fields) {
        requireNotEmpty(fields);
        this.orders = fields.stream().map(it -> new Order(direction, it)).collect(Collectors.toList());
    }

    /**
     * Returns a new {@link Sort} with the current setup but descending order direction.
     *
     * @return a non-null instance
     */
    public Sort descending() {
        return withDirection(Direction.DESC);
    }

    /**
     * Returns a new {@link Sort} with the current setup but ascending order direction.
     *
     * @return a non-null instance
     */
    public Sort ascending() {
        return withDirection(Direction.ASC);
    }

    /**
     * Returns whether there are any fields defined for this sort.
     *
     * @return {@code true} if sorted, {@code false} otherwise
     */
    public boolean isSorted() {
        return !isEmpty();
    }

    /**
     * Returns whether there no fields defined for this sort.
     *
     * @return {@code true} if sorted, {@code false} otherwise
     */
    public boolean isEmpty() {
        return orders.isEmpty();
    }

    /**
     * Returns a new {@link Sort} consisting of the {@link Order}s of the current {@link Sort} combined with the given
     * ones.
     *
     * @param sort must not be {@literal null}.
     * @return a non-null instance
     */
    public Sort and(Sort sort) {
        requireNonNull(sort);
        List<Order> these = new ArrayList<>(orders);
        for (Order order : sort) {
            these.add(order);
        }
        return Sort.create(these);
    }

    /**
     * Returns the order registered for the given field.
     *
     * @param field the field
     * @return the order, null if there is nothing registered for the field
     */
    public Order get(String field) {
        for (Order order : this) {
            if (order.getField().equalsIgnoreCase(field)) return order;
        }
        return null;
    }

    public Iterator<Order> iterator() {
        return this.orders.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sort orders1 = (Sort) o;
        return Objects.equals(orders, orders1.orders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orders);
    }

    @Override
    public String toString() {
        return isEmpty() ? "UNSORTED" : StringUtils.collectionToCommaDelimitedString(orders);
    }

    /**
     * Creates a new {@link Sort} with the current setup but the given order direction.
     *
     * @param direction the direction
     * @return a non-null instance
     */
    private Sort withDirection(Direction direction) {
        List<Order> result = new ArrayList<>(orders.size());
        for (Order order : this) {
            result.add(order.with(direction));
        }
        return Sort.create(result);
    }

    /**
     * Enumeration for sort directions.
     */
    public enum Direction {

        /**
         * A value for ascending order.
         */
        ASC,

        /**
         * A value for descending order.
         */
        DESC;

        /**
         * Returns whether the direction is ascending.
         *
         * @return {@code true} if the order is ascending, {@code false} otherwise
         */
        public boolean isAscending() {
            return this == ASC;
        }

        /**
         * Returns whether the direction is descending.
         *
         * @return {@code true} if the order is descending, {@code false} otherwise
         */
        public boolean isDescending() {
            return this == DESC;
        }

        /**
         * Returns the {@link Direction} enum for the given {@link String} value.
         *
         * @param value the direction as string
         * @return a non-null instance
         * @throws IllegalArgumentException in case the given value cannot be parsed into an enum value.
         */
        public static Direction fromString(String value) {
            try {
                return Direction.valueOf(value.toUpperCase(Locale.US));
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(
                        "Invalid value '%s' for orders given; Has to be either 'desc' or 'asc' (case insensitive)", value), e);
            }
        }
    }

    /**
     * Enumeration for null handling hints that can be used in {@link Order} expressions.
     */
    public enum NullHandling {

        /**
         * Lets the data store decide what to do with nulls.
         */
        NATIVE,

        /**
         * A hint to the used data store to order entries with null values before non null entries.
         */
        NULLS_FIRST,

        /**
         * A hint to the used data store to order entries with null values after non null entries.
         */
        NULLS_LAST;
    }

    /**
     * Holds a sorting tuple, made out of a field and sorting direction.
     */
    public static class Order {

        private static final boolean DEFAULT_IGNORE_CASE = false;
        private static final NullHandling DEFAULT_NULL_HANDLING = NullHandling.NATIVE;

        private final Direction direction;
        private final String field;
        private final boolean ignoreCase;
        private final NullHandling nullHandling;


        /**
         * Creates a new {@link Order} instance. Takes a single property. Direction defaults to
         * {@link Sort#DEFAULT_DIRECTION}.
         *
         * @param property must not be {@literal null} or empty.
         * @since 2.0
         */
        public static Order create(String property) {
            return new Order(DEFAULT_DIRECTION, property);
        }

        /**
         * Creates a new {@link Order} instance. Takes a single property. Direction is {@link Direction#ASC} and
         * NullHandling {@link NullHandling#NATIVE}.
         *
         * @param property must not be {@literal null} or empty.
         * @since 2.0
         */
        public static Order asc(String property) {
            return new Order(Direction.ASC, property, DEFAULT_NULL_HANDLING);
        }

        /**
         * Creates a new {@link Order} instance. Takes a single property. Direction is {@link Direction#DESC} and
         * NullHandling {@link NullHandling#NATIVE}.
         *
         * @param property must not be {@literal null} or empty.
         * @since 2.0
         */
        public static Order desc(String property) {
            return new Order(Direction.DESC, property, DEFAULT_NULL_HANDLING);
        }


        private Order(@Nullable Direction direction, String field) {
            this(direction, field, DEFAULT_IGNORE_CASE, DEFAULT_NULL_HANDLING);
        }

        private Order(@Nullable Direction direction, String field, NullHandling nullHandlingHint) {
            this(direction, field, DEFAULT_IGNORE_CASE, nullHandlingHint);
        }

        private Order(@Nullable Direction direction, String field, boolean ignoreCase, NullHandling nullHandling) {
            requireNotEmpty(field);
            this.direction = direction == null ? DEFAULT_DIRECTION : direction;
            this.field = field;
            this.ignoreCase = ignoreCase;
            this.nullHandling = nullHandling;
        }

        /**
         * Returns the sorting order.
         *
         * @return a non-null instance
         */
        public Direction getDirection() {
            return direction;
        }

        /**
         * Returns the field to order for.
         *
         * @return a non-null instance
         */
        public String getField() {
            return field;
        }

        /**
         * Returns whether sorting for this property shall be ascending.
         *
         * @return {@code true} if the sorting order is ascending, {@code  false} otherwise
         */
        public boolean isAscending() {
            return this.direction.isAscending();
        }

        /**
         * Returns whether sorting for this property shall be descending.
         *
         * @return {@code true} if the sorting order is descending, {@code  false} otherwise
         */
        public boolean isDescending() {
            return this.direction.isDescending();
        }

        /**
         * Returns whether the sort will be case-sensitive or case-insensitive.
         *
         * @return {@code true} if the sorting is case insensitive, {@code  false} otherwise
         */
        public boolean isIgnoreCase() {
            return ignoreCase;
        }

        /**
         * Returns a new {@link Order} with the given {@link Direction}.
         *
         * @param direction the new direction
         * @return a new instance
         */
        public Order with(Direction direction) {
            return new Order(direction, this.field, this.ignoreCase, this.nullHandling);
        }

        /**
         * Returns a new {@link Order} with the reversed {@link #getDirection()}.
         *
         * @return a new instance
         */
        public Order reverse() {
            return with(this.direction == Direction.ASC ? Direction.DESC : Direction.ASC);
        }

        /**
         * Returns a new {@link Order}.
         *
         * @param property must not be {@literal null} or empty.
         * @return a new instance
         */
        public Order withField(String property) {
            return new Order(this.direction, property, this.ignoreCase, this.nullHandling);
        }

        /**
         * Returns a new {@link Sort} instance for the given fields.
         *
         * @param fields the new fields to be used by sorting
         * @return a non-null instance
         */
        public Sort withFields(String... fields) {
            return Sort.create(this.direction, fields);
        }

        /**
         * Returns a new {@link Order} with case-insensitive sorting enabled.
         *
         * @return a new instance
         */
        public Order ignoreCase() {
            return new Order(direction, field, true, nullHandling);
        }

        /**
         * Returns a {@link Order} with the given {@link NullHandling}.
         *
         * @param nullHandling can be {@literal null}.
         * @return a new instance
         */
        public Order with(NullHandling nullHandling) {
            return new Order(direction, this.field, ignoreCase, nullHandling);
        }

        /**
         * Returns a {@link Order} with {@link NullHandling#NULLS_FIRST} as null handling hint.
         *
         * @return a new instance
         */
        public Order nullsFirst() {
            return with(NullHandling.NULLS_FIRST);
        }

        /**
         * Returns a {@link Order} with {@link NullHandling#NULLS_LAST} as null handling hint.
         *
         * @return a new instance
         */
        public Order nullsLast() {
            return with(NullHandling.NULLS_LAST);
        }

        /**
         * Returns a {@link Order} with {@link NullHandling#NATIVE} as null handling hint.
         *
         * @return a new instance
         */
        public Order nullsNative() {
            return with(NullHandling.NATIVE);
        }

        /**
         * Returns the used {@link NullHandling} hint, which can but may not be respected by the used datastore.
         *
         * @return a new instance
         */
        public NullHandling getNullHandling() {
            return nullHandling;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Order order = (Order) o;
            return ignoreCase == order.ignoreCase && direction == order.direction && Objects.equals(field, order.field) && nullHandling == order.nullHandling;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, field, ignoreCase, nullHandling);
        }

        @Override
        public String toString() {
            String result = String.format("%s: %s", field, direction);
            if (!NullHandling.NATIVE.equals(nullHandling)) result += ", " + nullHandling;
            if (ignoreCase) result += ", ignoring case";
            return result;
        }
    }
}
