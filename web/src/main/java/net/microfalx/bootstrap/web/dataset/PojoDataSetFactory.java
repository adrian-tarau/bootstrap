package net.microfalx.bootstrap.web.dataset;

/**
 * Base class for all POJO base data set factories.
 */
public abstract class PojoDataSetFactory<M, ID> extends AbstractDataSetFactory<M, ID> {

    @Override
    public void update(DataSet<M, ID> dataSet, Object owner) {

    }

    @Override
    protected AbstractMetadata<M> createMetadata(Class<M> modelClass) {
        return new PojoMetadata<>(modelClass);
    }

    @Override
    protected AbstractField<M> createField(Metadata<M> metadata, String name, String property) {
        return new PojoField<>(metadata, name, property);
    }

    public static class PojoMetadata<M> extends AbstractMetadata<M> {

        public PojoMetadata(Class<M> modelClass) {
            super(modelClass);
        }
    }

    public static class PojoField<M> extends AbstractField<M> {

        public PojoField(Metadata<M> metadata, String name, String property) {
            super(metadata, name, property);
        }
    }
}
