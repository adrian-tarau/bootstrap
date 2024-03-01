package net.microfalx.bootstrap.web.controller.admin.database;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.lang.annotation.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Name("Nodes")
@ReadOnly
public class Node {

    @Id
    @Visible(value = false)
    private String id;

    @Position(1)
    @Description("The database which owns this node")
    private Database database;

    @Position(2)
    @Name
    @Description("The name of the database node")
    private String name;

    @Position(10)
    @Description("The hostname of the database node")
    private String hostname;

    @Position(11)
    @Formattable(prettyPrint = false)
    @Description("The port of the database service")
    private int port;

    @Position(12)
    @Description("The state of the database node")
    private net.microfalx.bootstrap.jdbc.support.Node.State state;

    @Position(100)
    @Description("The timestamp when the database was started")
    private LocalDateTime startedAt;

    @Position(101)
    @Description("The timestamp when the database was created")
    private LocalDateTime createdAt;

    @Position(102)
    @Timestamp
    @OrderBy(OrderBy.Direction.DESC)
    @Description("The timestamp when the database was modified last time")
    private LocalDateTime modifiedAt;

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
