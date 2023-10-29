package net.microfalx.bootstrap.web.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * A generic JSON response which contains the action outcome (successful/failed), a message, a status code, a response payload and custom attributes.
 */
public class JsonResponse<T extends JsonResponse<T>> implements Serializable {

    private static final long serialVersionUID = -2814974262080434683L;

    /**
     * Error code indicating that the cause us unknown.
     */
    public static final int UNKNOWN_ERROR = 1;

    /**
     * Error code indicating that the request contains invalid parameters, or it doesn't have all required parameters.
     */
    public static final int INVALID_REQUEST_ERROR = 2;

    /**
     * Error code indicating that then request resource cannot be found.
     */
    public static final int NOT_FOUND_ERROR = 3;

    /**
     * Error code indicating that an internal error occurred.
     */
    public static final int INTERNAL_ERROR = 4;

    /**
     * Error code indicating that the request resource failed to be validated.
     */
    public static final int VALIDATION_ERROR = 20;

    /**
     * Error code indicating that the request resource cannot be persisted.
     */
    public static final int PERSISTENCE_ERROR = 21;

    /**
     * Error code indicating that the request resource already exists.
     */
    public static final int DUPLICATE_ERROR = 22;

    /**
     * Error code indicating that the request cannot be fulfilled right now, but requesting the information again
     * at later time should be successful.
     */
    public static final int CANNOT_PROVIDE_ERROR = 23;

    /**
     * Holds a flag indicating if the request was processed successfully (true) or not (false)
     */
    private volatile boolean success;

    /**
     * A success or failure message passed with the response
     */
    private volatile String message;

    /**
     * A title associated with the response (used to display a message window, if applicable)
     */
    private volatile String title;

    /**
     * An error code returned to the client
     */
    private volatile int errorCode;

    /**
     * Attributes passed with the response
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * A payload object serialized back to the client.
     */
    private volatile Object payload;

    /**
     * In case the payload is an array of records this contains the total number of records
     */
    private volatile int total;

    /**
     * An exception attached to the response.
     */
    @JsonIgnore
    private volatile Throwable throwable;

    /**
     * Creates a JSON response object with a successful outcome.
     *
     * @return a non-null object
     */
    public static JsonResponse<?> success() {
        return new JsonResponse<>().setSuccess(true);
    }

    /**
     * Creates a JSON response object with a successful outcome and a message attached to it.
     *
     * @param message the message to be sent to the caller
     * @return a non-null object
     */
    public static JsonResponse<?> success(String message) {
        return success().setMessage(message);
    }

    /**
     * Creates a JSON response object with a successful outcome and a message and a title attached to it.
     *
     * @param message the message to be sent to the caller
     * @return a non-null object
     */
    public static JsonResponse<?> success(String title, String message) {
        return success().setMessage(message).setTitle(title);
    }

    /**
     * Creates a JSON response object with a failed outcome and a failure message attached to it.
     *
     * @param message the message to be sent to the caller
     * @return a non-null object
     */
    public static JsonResponse<?> fail(String message) {
        return fail(INTERNAL_ERROR, message);
    }

    /**
     * Creates a JSON response object with a failed outcome and an error code attached to it.
     *
     * @param errorCode the error code to be sent to the caller
     * @return a non-null object
     */
    public static JsonResponse<?> fail(int errorCode) {
        return fail(errorCode, (String) null);
    }

    /**
     * Creates a JSON response object with a failed outcome, an error code and and message attached to it.
     *
     * @param errorCode the error code to be sent to the caller
     * @param message   the message to be sent to the caller
     * @return a non-null object
     */
    public static JsonResponse<?> fail(int errorCode, String message) {
        return new JsonResponse<>().setSuccess(false).setErrorCode(errorCode).setMessage(message);
    }

    protected JsonResponse() {
    }

    protected JsonResponse(boolean success) {
        setSuccess(success);
    }

    /**
     * Changes the outcome of the JSON response
     *
     * @param success <code>true</code> if the request was processed successfully, <code>false</code> otherwise
     * @return self
     */
    public T setSuccess(boolean success) {
        this.success = success;
        return self();
    }

    /**
     * Returns whether the request was processed successfully.
     *
     * @return <code>true</code> if successful, <code>false</code> otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the message associated with this JSON response.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Changes the message associated with this JSON response
     *
     * @param message the message
     * @return self
     */
    public T setMessage(String message) {
        this.message = message;
        return self();
    }

    /**
     * Returns the exception associated with the response.
     *
     * @return the exception, null if there is no exception
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Sets an exception associated with the response.
     *
     * @param throwable the exception
     */
    public T setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return self();
    }

    /**
     * Returns the title associated with this JSON response.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Changes the title associated with this JSON response.
     *
     * @param title the title
     * @return self
     */
    public T setTitle(String title) {
        this.title = title;
        return self();
    }

    /**
     * Returns the error code associated with this JSON response.
     *
     * @return the error code
     */
    public int getErrorCode() {
        return isSuccess() ? 0 : errorCode > 0 ? errorCode : INTERNAL_ERROR;
    }

    /**
     * Changes the error code associated with this JSON response.
     *
     * @param errorCode the error code
     * @return self
     */
    public T setErrorCode(int errorCode) {
        this.errorCode = errorCode;
        return self();
    }

    /**
     * Returns a map containing attributes (key, value pairs) associated with this JSON response.
     *
     * @return a non-null map
     */
    public Map<String, Object> getAttributes() {
        return unmodifiableMap(attributes);
    }

    /**
     * Adds a new attribute.
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @return self
     */
    public T addAttribute(String name, Object value) {
        attributes.put(name, value);
        return self();
    }

    /**
     * Removes an attribute.
     *
     * @param name the attribute name
     * @return self
     */
    public T removeAttribute(String name) {
        attributes.remove(name);
        return self();
    }

    /**
     * Returns the attribute value.
     *
     * @param name the attribute name
     * @return the attribute value
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Returns the payload associated with this JSON response.
     *
     * @return the payload
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * Changes the payload associated with this JSON response.
     *
     * @param payload the payload
     * @return self
     */
    public T setPayload(Object payload) {
        this.payload = payload;
        return self();
    }

    /**
     * Returns the total number of records contained in the payload
     *
     * @return
     */
    public int getTotal() {
        return total;
    }

    /**
     * Sets the total number of records contained in the payload
     *
     * @param total
     */
    public T setTotal(int total) {
        this.total = total;
        return self();
    }

    @Override
    public String toString() {
        return "JsonResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", title='" + title + '\'' +
                ", errorCode=" + errorCode +
                ", attributes=" + attributes +
                ", payload=" + payload +
                ", total=" + total +
                ", throwable=" + throwable +
                '}';
    }

    /**
     * Returns a reference to self.
     *
     * @return a non-null instance
     */
    protected final T self() {
        return (T) this;
    }
}
