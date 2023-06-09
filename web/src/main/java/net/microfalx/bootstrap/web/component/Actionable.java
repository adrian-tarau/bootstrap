package net.microfalx.bootstrap.web.component;

import java.util.Set;

/**
 * An interface which describes a component which can perform an action.
 * <p/>
 * The action translates into the following action:
 * <ul>
 * <li>action is translated into a path and it is executed directly</li>
 * <li>token is translated into the URL hash (can be combined with the action)</li>
 * <li>handler is executed as a JavaScript call to the current controller</li>
 * </ul>
 * </ul>
 */
public interface Actionable<A extends Actionable<A>> extends Itemable<A> {

    /**
     * Returns the action name associated with this actionable.
     *
     * @return the action or null if there is no action
     */
    String getAction();

    /**
     * Set the actionable action name.
     *
     * @param action the action name
     * @return self
     */
    A setAction(String action);

    /**
     * Returns the token (hash) pushed into the application history when the action is triggered.
     *
     * @return the action or null if there is no action
     */
    String getToken();

    /**
     * Set the token (hash) to be pushed into the application history when the action is triggered.
     *
     * @param token the token
     * @return self
     */
    A setToken(String token);

    /**
     * Returns the function name to be called  when this actionable triggers.
     *
     * @return the function name or null if there is no handler
     */
    String getHandler();

    /**
     * Sets the actionable handler function.
     *
     * @param handler the handler function
     * @return self
     */
    A setHandler(String handler);

    /**
     * Returns the roles required for the action to be enabled.
     * <p/>
     * The user must have at least one of the given roles to gain access to actionable.
     *
     * @return a non-null set of roles
     */
    Set<String> getRoles();

    /**
     * Replaces the roles required for the action to be enabled.
     * <p/>
     * The user must have at least one of the given roles to gain access to actionable.
     *
     * @param roles a list of role identifiers
     * @return self
     */
    A setRoles(String... roles);

    /**
     * Adds roles required for the action to be enabled.
     * <p/>
     * The user must have at least one of the given roles to gain access to actionable.
     *
     * @param roles a list of role identifiers
     * @return self
     */
    A addRoles(String... roles);
}
