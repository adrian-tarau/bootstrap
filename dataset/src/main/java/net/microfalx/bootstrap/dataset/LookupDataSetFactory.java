package net.microfalx.bootstrap.dataset;

import com.google.common.collect.Lists;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;

import java.util.Collection;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A factory used to create memory data sets for {@link Lookup}.
 */
@SuppressWarnings("rawtypes")
class LookupDataSetFactory<M, F extends Field<M>, ID> extends AbstractDataSetFactory<M, F, ID> {

    private LookupProvider<M> provider;

    LookupDataSetFactory(LookupProvider<M> provider) {
        requireNonNull(provider);
        this.provider = provider;
    }

    @Override
    protected AbstractDataSet<M, F, ID> doCreate(Metadata<M, F, ID> metadata) {
        return new LookupDataSet<>(this, metadata);
    }

    @Override
    public boolean supports(Metadata<M, F, ID> metadata) {
        return metadata.getModel() == provider.getModel();
    }

    private class LookupDataSet<M, F extends Field<M>, ID> extends MemoryDataSet<M, F, ID> {

        public LookupDataSet(DataSetFactory<M, F, ID> factory, Metadata<M, F, ID> metadata) {
            super(factory, metadata);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Collection<M> extractModels() {
            return (Collection<M>) Lists.newArrayList(provider.extractAll());
        }
    }
}
