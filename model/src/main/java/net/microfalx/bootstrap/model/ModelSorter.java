package net.microfalx.bootstrap.model;

import com.google.common.collect.Streams;
import net.microfalx.lang.Prioritizable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which sorts a list of models.
 */
public class ModelSorter<M> {

    private final Metadata<M, ? extends Field<M>, ?> metadata;
    private final Iterable<M> models;
    private final Sort sort;

    private Comparator<M>[] comparators;

    public ModelSorter(Metadata<M, ? extends Field<M>, ?> metadata, Iterable<M> models, Sort sort) {
        requireNonNull(metadata);
        requireNonNull(models);
        this.metadata = metadata;
        this.models = models;
        this.sort = sort != null ? sort : Sort.unsorted();
        initialize();
    }

    /**
     * Applies the sorting to the models and returns the sorted models as a list.
     *
     * @return the sorted models
     */
    public List<M> toList() {
        return toStream().toList();
    }

    /**
     * Applies the sorting to the models and returns the sorted models as a stream.
     *
     * @return the sorted models
     */
    public Stream<M> toStream() {
        Stream<M> stream = Streams.stream(models);
        if (!sort.isSorted()) {
            return stream;
        } else {
            return stream.sorted(new SortComparator());
        }
    }

    @SuppressWarnings("unchecked")
    private void initialize() {
        List<OrderComparator<M>> comp = new ArrayList<>();
        for (Sort.Order order : sort) {
            comp.add(new OrderComparator<>(metadata.get(order.getField()), order));
        }
        comparators = comp.toArray(new OrderComparator[0]);
    }

    @Override
    public String toString() {
        return "ModelSorter{" +
                "models=" + models +
                ", sort=" + sort +
                '}';
    }

    class SortComparator implements Comparator<M> {

        @Override
        public int compare(M o1, M o2) {
            for (Comparator<M> comparator : comparators) {
                int result = comparator.compare(o1, o2);
                if (result != 0) return result;
            }
            return 0;
        }
    }

    static class OrderComparator<M> implements Comparator<M> {

        private final Field<M> field;
        private final Sort.Order order;

        OrderComparator(Field<M> field, Sort.Order order) {
            this.field = field;
            this.order = order;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public int compare(M o1, M o2) {
            Object value1 = Field.from(field.get(o1), field.getDataClass());
            Object value2 = Field.from(field.get(o2), field.getDataClass());
            int result;
            if (value1 == null && value2 == null) {
                result = 0;
            } else if (value1 != null && value2 == null) {
                result = order.getNullHandling() == Sort.NullHandling.NULLS_LAST ? -1 : 1;
            } else if (value1 == null) {
                result = order.getNullHandling() == Sort.NullHandling.NULLS_LAST ? 1 : -1;
            } else if (value1 instanceof Prioritizable && value2 instanceof Prioritizable) {
                return Integer.compare(((Prioritizable) value1).getPriority(), ((Prioritizable) value2).getPriority());
            } else if (value1 instanceof Comparable) {
                result = ((Comparable) value1).compareTo(value2);
            } else {
                result = value1.toString().compareTo(value2.toString());
            }
            return order.getDirection() == Sort.Direction.ASC ? result : -result;
        }
    }
}


