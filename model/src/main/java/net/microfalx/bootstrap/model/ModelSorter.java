package net.microfalx.bootstrap.model;

import java.util.ArrayList;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which sorts a list of models.
 */
public class ModelSorter<M> {

    private final List<M> models;
    private final Sort sort;

    public ModelSorter(List<M> models, Sort sort) {
        requireNonNull(models);
        requireNonNull(sort);
        this.models = new ArrayList<>(models);
        this.sort = sort;
    }

    /**
     * Applies the sorting to the models.
     *
     * @return the sorted models
     */
    List<M> apply() {
        return models;
    }

    @Override
    public String toString() {
        return "ModelSorter{" +
                "models=" + models.size() +
                ", sort=" + sort +
                '}';
    }
}
