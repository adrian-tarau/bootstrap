package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;

public abstract class PojoDataSet<M, F extends PojoField<M>, ID> extends AbstractDataSet<M, F, ID> {

    public PojoDataSet(DataSetFactory<M, F, ID> factory, Metadata<M, F, ID> metadata) {
        super(factory, metadata);
    }

}
