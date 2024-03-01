package net.microfalx.bootstrap.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which sorts a list of models.
 */
public class ModelSorter<M> {

    private final Metadata<M, ? extends Field<M>, ?> metadata;
    private final List<M> models;
    private final Sort sort;

    private Comparator<M>[] comparators;

    public ModelSorter(Metadata<M, ? extends Field<M>, ?> metadata, List<M> models, Sort sort) {
        requireNonNull(metadata);
        requireNonNull(models);
        this.metadata = metadata;
        this.models = new ArrayList<>(models);
        this.sort = sort != null ? sort : Sort.unsorted();
        initialize();
    }

    /**
     * Applies the sorting to the models.
     *
     * @return the sorted models
     */
    public List<M> apply() {
        if (!sort.isSorted()) return models;
        models.sort(new SortComparator());
        return models;
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
                "models=" + models.size() +
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
            } else if (value1 instanceof Comparable) {
                result = ((Comparable) value1).compareTo(value2);
            } else {
                result = value1.toString().compareTo(value2.toString());
            }
            return order.getDirection() == Sort.Direction.ASC ? result : -result;
        }
    }
}


