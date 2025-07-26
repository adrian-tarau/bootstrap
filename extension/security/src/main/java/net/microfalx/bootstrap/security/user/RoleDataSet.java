package net.microfalx.bootstrap.security.user;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;

@Provider
public class RoleDataSet extends MemoryDataSet<Role, PojoField<Role>, String> {

    public RoleDataSet(DataSetFactory<Role, PojoField<Role>, String> factory, Metadata<Role, PojoField<Role>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Role> extractModels(Filter filterable) {
        return getService(UserService.class).getRoles();
    }
}
