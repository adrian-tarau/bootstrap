package net.microfalx.bootstrap.web.controller.admin.database;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.annotation.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Name("Nodes")
@ReadOnly
public class Node extends NamedTimestampAware {

    @Id
    @Visible(value = false)
    private String id;

    @Position(10)
    @Label(value = "Name", group = "Database")
    @Description("The database which owns this node")
    private Database database;

    @Position(11)
    @Label(value = "Type", group = "Database")
    @Description("The type of database")
    private net.microfalx.bootstrap.jdbc.support.Database.Type databaseType;

    @Position(6)
    @Description("The hostname of the database node")
    private String hostname;

    @Position(7)
    @Formattable(prettyPrint = false)
    @Description("The port of the database service")
    private int port;

    @Position(12)
    @Description("The state of the database node")
    private net.microfalx.bootstrap.jdbc.support.Node.State state;

    @Position(502)
    @Description("The timestamp when the database was started")
    private LocalDateTime startedAt;

    /**
     * Creates a model from a {@link net.microfalx.bootstrap.jdbc.support.Node}.
     *
     * @param node the node
     * @return a non-null instance
     */
    public static Node from(net.microfalx.bootstrap.jdbc.support.Node node) {
        if (node == null) return null;
        Node model = new Node();
        model.setId(node.getId());
        model.setDatabase(Database.from(node.getDatabase()));
        model.setDatabaseType(node.getDatabase().getType());
        model.setName(node.getName());
        model.setHostname(node.getDataSource().getHostname());
        model.setState(node.getState());
        model.setPort(node.getDataSource().getPort());
        model.setStartedAt(node.getStartedAt());
        model.setCreatedAt(node.getCreatedAt());
        model.setModifiedAt(node.getModifiedAt());
        return model;
    }

}
