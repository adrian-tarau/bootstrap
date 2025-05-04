package net.microfalx.bootstrap.web.component;

import net.microfalx.bootstrap.model.Parameters;

import java.util.Set;

/**
 * An interface which describes a component which can perform an action.
 * <p/>
 * The action translates into the following action:
 * <ul>
 * <li>action is translated to an event and it is fired to the correct listeners</li>
 * <li>token is translated into the URL hash (can be combined with the action)</li>
 * </ul>
 * </ul>
 */
public interface Actionable<A extends Actionable<A>> extends Itemable<A> {

    /**
     * Returns the action (event) name associated with this actionable.
     * <p>
     * The action is converted to an event on the client side (JavaScript).
     *
     * @return the action or null if there is no action
     */
    String getAction();

    /**
     * Set the action (event) name associated with this actionable.
     * <p>
     * The action will translate to an application event on the client side.
     *
     * @param action the action name
     * @return self
     */
    A setAction(String action);

    /**
     * Returns the target (controller path or URL) associated with this actionable.
     * <p>
     * The target can be a full URL instead of a controller path for external execution.
     *
     * @return the target or null if there is no target
     */
    String getTarget();

    /**
     * Set the target path or URL.
     *
     * @param target the target path or URL
     * @return self
     */
    A setTarget(String target);

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

    /**
     * Adds a new parameter to the action.
     *
     * @param name  the parameter name
     * @param value the parameter value
     */
    A addParameter(String name, Object value);

    /**
     * Returns all registered parameters.
     *
     * @return a non-null instance
     */
    Parameters getParameters();
}
