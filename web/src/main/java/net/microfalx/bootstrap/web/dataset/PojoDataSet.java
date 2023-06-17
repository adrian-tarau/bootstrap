package net.microfalx.bootstrap.web.dataset;

public abstract class PojoDataSet<M, ID> extends AbstractDataSet<M, ID> {

    public PojoDataSet(DataSetFactory<M, ID> factory, Class<M> modelClass) {
        super(factory, modelClass);
    }

}
