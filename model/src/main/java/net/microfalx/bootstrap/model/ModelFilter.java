package net.microfalx.bootstrap.model;

import java.util.ArrayList;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which filters a list of models.
 *
 * @param <M> the model type
 */
public class ModelFilter<M> {

    private final Metadata<M, ? extends Field<M>, ?> metadata;
    private final List<M> models;
    private final Filter filter;

    public ModelFilter(Metadata<M, ? extends Field<M>, ?> metadata, List<M> models, Filter filter) {
        requireNonNull(metadata);
        requireNonNull(models);
        requireNonNull(filter);
        this.metadata = metadata;
        this.models = new ArrayList<>(models);
        this.filter = filter;
    }

    /**
     * Applies the filter to the models.
     *
     * @return the sorted models
     */
    List<M> apply() {
        return models;
    }

    @Override
    public String toString() {
        return "ModelFilter{" +
                "models=" + models.size() +
                ", filter=" + filter +
                '}';
    }

    public void filter(M model){

    }
}
