package net.microfalx.bootstrap.security.audit;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Provides metadata to the audit.
 */
public class AuditContext {

    private static final ThreadLocal<AuditContext> CONTEXT = ThreadLocal.withInitial(AuditContext::new);

    private static final String NA = "-";

    private String action;
    private String module = "Core";
    private String category = "System";
    private String clientInfo;
    private String errorCode;
    private String reference;
    private String description;

    /**
     * Returns the context for the current thread.
     *
     * @return a non-null instance
     */
    public static AuditContext get() {
        return CONTEXT.get();
    }

    /**
     * Removes the audit context from the current thread.
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * Returns the action associated with the current request.
     *
     * @return the action, null if not changed
     */
    public String getAction() {
        return action;
    }

    /**
     * Changes the action associated with the current request.
     *
     * @param action the action
     */
    public AuditContext setAction(String action) {
        this.action = action;
        return this;
    }

    /**
     * Returns the module associated with the current request.
     *
     * @return the module, not if not known
     */
    public String getModule() {
        return module;
    }

    /**
     * Changes the module associated with the current request.
     *
     * @param module the module, null if not known
     */
    public AuditContext setModule(String module) {
        requireNotEmpty(module);
        this.module = module;
        return this;
    }

    /**
     * Returns the category of the action.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Changes the category.
     *
     * @param category the category
     */
    public AuditContext setCategory(String category) {
        requireNotEmpty(category);
        this.category = category;
        return this;
    }

    /**
     * Returns information about a client (its IP/host).
     *
     * @return the client info, null if not available
     */
    public String getClientInfo() {
        return isNotEmpty(clientInfo) ? clientInfo : NA;
    }

    /**
     * Changes the information about a client (its IP/host).
     *
     * @param clientInfo the client info
     */
    public AuditContext setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
        return this;
    }

    /**
     * Returns the error code associated with the action.
     *
     * @return the error code, null if successful or no code is available
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Changes  the error code associated with the action.
     *
     * @param errorCode the error code
     */
    public AuditContext setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    /**
     * Returns a reference associated with the audit, usually the request path
     *
     * @return the reference, null if not available
     */
    public String getReference() {
        return isNotEmpty(reference) ? reference : NA;
    }

    /**
     * Changes the reference a reference associated with the audit, usually the request path
     *
     * @param reference the reference
     */
    public AuditContext setReference(String reference) {
        this.reference = reference;
        return this;
    }

    /**
     * Returns a description of the action.
     *
     * @return the description, null if not available
     */
    public String getDescription() {
        return description;
    }

    /**
     * Changes the description associated with  the action.
     *
     * @param description the description
     */
    public AuditContext setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "AuditContext{" +
                "action='" + action + '\'' +
                ", module='" + module + '\'' +
                ", category='" + category + '\'' +
                ", clientInfo='" + clientInfo + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", reference='" + reference + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
