package net.microfalx.bootstrap.serenity.junit;

import net.microfalx.bootstrap.serenity.task.Dashboard;
import net.microfalx.bootstrap.serenity.task.User;
import org.junit.jupiter.api.*;

/**
 * Base class for all dashboards.
 */
@Tag("dashboard")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractDashboardSystemTestCase<D extends Dashboard<D>> extends AbstractSystemTestCase {

    private D dashboard;
    private User user;

    @BeforeEach
    void loginAndOpen() {
        actor.attemptsTo(getUser().login());
        actor.attemptsTo(getDashboard().open(), getDashboard().validate());
    }

    @Test
    @Order(1)
    void open() {
        actor.attemptsTo(getDashboard().open(), getDashboard().validate());
    }

    /**
     * Returns the dashboard being testes.
     *
     * @return a non-null instance
     */
    protected final D getDashboard() {
        if (dashboard == null) dashboard = createDashboard();
        if (dashboard == null) throw new IllegalStateException("Dashboard not created");
        return dashboard;
    }

    /**
     * Returns the user used to the dashboard.
     *
     * @return a non-null instance
     */
    protected final User getUser() {
        if (user == null) user = createUser();
        if (user == null) throw new IllegalStateException("User not created");
        return user;
    }

    /**
     * Creates the {@link User} used to perform the login interactions.
     *
     * @return a non-null instance
     */
    protected abstract User createUser();

    /**
     * Creates the {@link Dashboard} used to perform interactions with a dashboard.
     *
     * @return a non-null instance
     */
    protected abstract D createDashboard();

}
