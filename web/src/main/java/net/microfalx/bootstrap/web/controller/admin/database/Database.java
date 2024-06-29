package net.microfalx.bootstrap.web.controller.admin.database;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.Alert;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.bootstrap.jdbc.support.Node;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.annotation.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Name("Databases")
@ReadOnly
public class Database extends NamedIdentityAware<String> {

    @Position(6)
    @Description("The type of the database (cluster)")
    private net.microfalx.bootstrap.jdbc.support.Database.Type type;

    @Position(10)
    @Description("The JDBC URL")
    private String uri;

    @Position(11)
    @Description("The JDBC user name")
    private String userName;

    @Position(20)
    @Label("Nodes")
    @Description("The number of nodes supporting the database")
    private int nodeCount;

    @Position(21)
    @Label("State")
    @Description("The state of the database")
    @Formattable(alert = AlertProvider.class)
    private Node.State state;

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

    @Position(200)
    @Description("The description of the validation failure")
    @Visible(false)
    private String validationError;

    /**
     * Creates a model from a {@link net.microfalx.bootstrap.jdbc.support.Database}.
     *
     * @param database the database
     * @return a non-null instance
     */
    public static Database from(net.microfalx.bootstrap.jdbc.support.Database database) {
        if (database == null) return null;
        Database model = new Database();
        model.setId(database.getId());
        model.setName(database.getName());
        model.setType(database.getType());
        model.setUri(database.getDataSource().getUri().toASCIIString());
        model.setNodeCount(database.getNodes().size());
        model.setUserName(database.getDataSource().getUserName());
        model.setStartedAt(database.getStartedAt());
        model.setCreatedAt(database.getCreatedAt());
        model.setModifiedAt(database.getModifiedAt());
        model.setValidationError(database.getValidationError());
        model.setState(database.getState());
        return model;
    }

    public static class AlertProvider implements Formattable.AlertProvider<Database, Field<Database>, Node.State> {

        @Override
        public Alert provide(Node.State value, Field<Database> field, Database model) {
            Alert.Type type = switch (value) {
                case UP -> Alert.Type.SUCCESS;
                case DOWN -> Alert.Type.DANGER;
                case STANDBY, RECOVERING -> Alert.Type.SECONDARY;
                case UNKNOWN -> Alert.Type.LIGHT;
            };
            return Alert.builder().type(type).message(model.getValidationError()).build();
        }
    }
}
