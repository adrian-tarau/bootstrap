package net.microfalx.bootstrap.web.dataset;

import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;

import static org.apache.commons.lang3.ClassUtils.isAssignable;

public class TestDataSetFactory extends PojoDataSetFactory<TestDataSetFactory.TestModel, PojoField<TestDataSetFactory.TestModel>, Integer> {

    @Override
    protected AbstractDataSet<TestModel, PojoField<TestModel>, Integer> doCreate(Metadata<TestModel, PojoField<TestModel>> metadata) {
        return new TestDataSet(this, metadata);
    }

    @Override
    public boolean supports(Metadata<TestModel, PojoField<TestModel>> metadata) {
        return isAssignable(metadata.getModel(), TestModel.class);
    }

    public static final class TestDataSet extends PojoDataSet<TestModel, PojoField<TestModel>, Integer> {

        public TestDataSet(DataSetFactory<TestModel, PojoField<TestModel>, Integer> factory, Metadata<TestModel, PojoField<TestModel>> metadata) {
            super(factory, metadata);
        }
    }

    public static final class TestModel {

        private int id;
        private String name;
        private String description;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }


}
