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
public class NodeDataSet extends PojoDataSet<Node, PojoField<Node>, String> {

    private DatabaseService databaseService;

    public NodeDataSet(DataSetFactory<Node, PojoField<Node>, String> factory,
                       Metadata<Node, PojoField<Node>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        databaseService = getService(DatabaseService.class);
    }

    @Override
    protected Optional<Node> doFindById(String id) {
        try {
            return Optional.ofNullable(Node.from(databaseService.findNode(id).orElse(null)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    protected Page<Node> doFindAll(Pageable pageable, Filter filterable) {
        List<Node> models = databaseService.getNodes().stream().map(Node::from).toList();
        return getPage(models, pageable, filterable);
    }


}
