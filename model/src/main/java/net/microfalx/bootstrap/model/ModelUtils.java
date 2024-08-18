package net.microfalx.bootstrap.model;

import net.microfalx.lang.annotation.Name;

import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A collection of utilities around models.
 */
public class ModelUtils {

    private ModelUtils() {
    }


    /**
     * Returns the sorting based on the model names.
     *
     * @param metadata the model metadata
     * @param <M>      the model type
     * @param <F>      the field type
     * @param <ID>     the identifier type
     * @return the sort criteri
     */
    public static <M, F extends Field<M>, ID> net.microfalx.bootstrap.model.Sort getSortByName(Metadata<M, F, ID> metadata) {
        requireNonNull(metadata);
        List<F> fields = metadata.getFields(Name.class);
        return net.microfalx.bootstrap.model.Sort.create(fields.stream().map(mField -> net.microfalx.bootstrap.model.Sort.Order.asc(mField.getName())).toList());
    }


}
