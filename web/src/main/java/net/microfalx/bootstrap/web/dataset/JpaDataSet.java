package net.microfalx.bootstrap.web.dataset;

import jakarta.persistence.Entity;

/**
 * A data set for JPA entities.
 */
public class JpaDataSet<T, ID> extends PojoDataSet<T, ID> {

    public JpaDataSet(DataSetFactory<T, ID> factory, Class<T> modelClass) {
        super(factory, modelClass);
    }



    public static class JpaMetadata<M> extends PojoDataSetFactory.PojoMetadata<M> {

        public JpaMetadata(Class<M> modelClass) {
            super(modelClass);
        }
    }

    public static class JpaField<M> extends PojoDataSetFactory.PojoField<M> {

        public JpaField(Metadata<M> metadata, String name, String property) {
            super(metadata, name, property);
        }
    }

    public static class Factory<T, ID> extends PojoDataSetFactory<T, ID> {

        @Override
        public boolean supports(Class<T> modelClass) {
            return modelClass.getAnnotation(Entity.class) != null;
        }

        @Override
        protected AbstractMetadata<T> createMetadata(Class<T> modelClass) {
            return new JpaMetadata<>(modelClass);
        }

        @Override
        protected AbstractField<T> createField(Metadata<T> metadata, String name, String property) {
            return new JpaField<>(metadata, name, property);
        }

        @Override
        public Expression parse(String value) {
            return null;
        }

        @Override
        public DataSet<T, ID> create(Class<T> modelClass) {
            return new JpaDataSet<>(this, modelClass);
        }
    }
}
