package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;

import java.util.Arrays;

/**
 * A dataset supporting enums.
 *
 * @param <E> the enum type
 */
public class EnumDataSet<E extends Enum<E>> extends MemoryDataSet<E, Field<E>, String> {

    public EnumDataSet(DataSetFactory<E, Field<E>, String> factory, Metadata<E, Field<E>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<E> extractModels() {
        return Arrays.asList(getMetadata().getModel().getEnumConstants());
    }

    public static class Factory<E extends Enum<E>> extends AbstractDataSetFactory<E, Field<E>, String> {

        @Override
        protected AbstractDataSet<E, Field<E>, String> doCreate(Metadata<E, Field<E>, String> metadata) {
            return new EnumDataSet<>(this, metadata);
        }

        @Override
        public boolean supports(Metadata<E, Field<E>, String> metadata) {
            return metadata.getModel().isEnum();
        }
    }
}
