package net.microfalx.bootstrap.jdbc.support.vertica;

import net.microfalx.bootstrap.jdbc.support.*;
import net.microfalx.resource.Resource;

import java.util.Set;

public class VerticaSchema extends AbstractSchema {

    public VerticaSchema(Database database) {
        super(database);
    }

    @Override
    protected Set<String> doGetTableNames() {
        return Set.of();
    }

    @Override
    protected Set<String> doGetViewNames() {
        return Set.of();
    }

    @Override
    protected Table<?> doGetTable(String name) {
        return new VerticaTable(this, name);
    }

    @Override
    protected Set<String> doGetIndexNames() {
        return Set.of();
    }

    @Override
    protected View<?> doGetView(String name) {
        return new VerticaView(this, name);
    }

    @Override
    protected Index<?> doGetIndex(String name) {
        return null;
    }

    @Override
    protected Script createScript(Resource resource) {
        return new VerticaScript(this, resource);
    }
}
