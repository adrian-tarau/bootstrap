package net.microfalx.bootstrap.web.controller.admin.database;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.lang.annotation.*;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Name("Sessions")
@ReadOnly
public class Session {

    @Id
    @Visible(value = false)
    private String id;

    @Position(1)
    @Description("The database node which runs this session")
    private Node node;

    @Position(2)
    @Description("The name of the schema (database)")
    @Visible(modes = Visible.Mode.VIEW)
    private String schema;

    @Position(3)
    @Description("The user name used to create the session")
    private String userName;

    @Position(4)
    @Description("The hostname of the client for the session")
    private String hostname;

    @Position(10)
    @Description("The state of the database session")
    private net.microfalx.bootstrap.jdbc.support.Session.State state;

    @Position(11)
    @Description("The time since the session change its state")
    @OrderBy(OrderBy.Direction.DESC)
    @Filterable
    private Duration elapsed;

    @Position(20)
    @Description("The statement executed by the session")
    @Filterable
    @Name
    @Component(Component.Type.TEXT_AREA)
    private String statement;

    @Position(21)
    @Description("An additional information about session (state/operations/etc.)")
    @Filterable
    private String info;

    @Position(100)
    @Description("The timestamp when the database session was created")
    @Visible
    private LocalDateTime createdAt;

    public static Session from(net.microfalx.bootstrap.jdbc.support.Session session) {
        if (session == null) return null;
        Session model = new Session();
        model.setId(session.getId());
        model.setNode(Node.from(session.getNode()));
        model.setSchema(session.getSchema());
        model.setUserName(session.getUserName());
        model.setHostname(session.getClientHostname());
        model.setElapsed(session.getElapsed());
        model.setState(session.getState());
        if (session.getStatement() != null) {
            model.setStatement(StringUtils.abbreviate(session.getStatement().getContent(), 90));
        }
        model.setInfo(session.getInfo());
        model.setCreatedAt(session.getCreatedAt());
        return model;
    }
}
