package net.microfalx.bootstrap.dataset;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;

import static org.apache.commons.lang3.ClassUtils.isAssignable;

public class TestDataSetFactory extends PojoDataSetFactory<TestDataSetFactory.TestModel, PojoField<TestDataSetFactory.TestModel>, Integer> {

    @Override
    protected AbstractDataSet<TestModel, PojoField<TestModel>, Integer> doCreate(Metadata<TestModel, PojoField<TestModel>, Integer> metadata) {
        return new TestDataSet(this, metadata);
    }

    @Override
    public boolean supports(Metadata<TestModel, PojoField<TestModel>, Integer> metadata) {
        return isAssignable(metadata.getModel(), TestModel.class);
    }

    public static final class TestDataSet extends PojoDataSet<TestModel, PojoField<TestModel>, Integer> {

        public TestDataSet(DataSetFactory<TestModel, PojoField<TestModel>, Integer> factory, Metadata<TestModel, PojoField<TestModel>, Integer> metadata) {
            super(factory, metadata);
        }
    }

    @Getter
    @Setter
    public static final class TestModel {

        private int id;
        private String name;
        private String description;
    }


}
