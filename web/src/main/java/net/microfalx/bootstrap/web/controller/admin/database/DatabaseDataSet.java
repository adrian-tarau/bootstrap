package net.microfalx.bootstrap.web.controller.admin.database;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.jdbc.support.DatabaseService;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Provider
public class DatabaseDataSet extends PojoDataSet<Database, PojoField<Database>, String> {

    private DatabaseService databaseService;

    public DatabaseDataSet(DataSetFactory<Database, PojoField<Database>, String> factory,
                           Metadata<Database, PojoField<Database>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        databaseService = getService(DatabaseService.class);
    }

    @Override
    protected Optional<Database> doFindById(String id) {
        try {
            return Optional.of(Database.from(databaseService.getDatabase(id)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    protected Page<Database> doFindAll(Pageable pageable, Filter filterable) {
        List<Database> models = databaseService.getDatabase().stream().map(Database::from).toList();
        return getPage(models, pageable, filterable);
    }


}
