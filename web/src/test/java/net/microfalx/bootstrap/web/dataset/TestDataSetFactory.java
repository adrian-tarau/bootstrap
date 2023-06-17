package net.microfalx.bootstrap.web.dataset;

import static org.apache.commons.lang3.ClassUtils.isAssignable;

public class TestDataSetFactory extends PojoDataSetFactory<TestDataSetFactory.TestModel, Integer> {

    @Override
    public boolean supports(Class<TestDataSetFactory.TestModel> modelClass) {
        return isAssignable(modelClass, TestModel.class);
    }

    @Override
    public DataSet<TestModel, Integer> create(Class<TestDataSetFactory.TestModel> modelClass) {
        return new TestDataSet(this, modelClass);
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

    public static final class TestDataSet extends PojoDataSet<TestModel, Integer> {

        public TestDataSet(DataSetFactory<TestModel, Integer> factory, Class<TestModel> modelClass) {
            super(factory, modelClass);
        }

    }
}
