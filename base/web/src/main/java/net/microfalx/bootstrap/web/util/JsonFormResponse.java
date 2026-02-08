package net.microfalx.bootstrap.web.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.microfalx.lang.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * A specialized JSON response which deals with <code>form</code> requests.
 */
public class JsonFormResponse<T extends JsonFormResponse<T>> extends JsonResponse<T> {

    /**
     * Holds error message associated with form fields
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, String> errors = new HashMap<>();

    /**
     * Holds warnings message associated with form fields
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, String> warnings = new HashMap<>();

    /**
     * Creates a JSON response object with a successful outcome.
     *
     * @return a non-null object
     */
    public static JsonFormResponse<?> success() {
        return new JsonFormResponse<>(true);
    }

    /**
     * Creates a JSON response object with a successful outcome and a message attached to it.
     *
     * @param message the message to be sent to the caller
     * @return a non-null object
     */
    public static JsonFormResponse<?> success(String message) {
        return success().setMessage(message);
    }

    /**
     * Creates a JSON response object with a successful outcome and a message and a title attached to it.
     *
     * @param message the message to be sent to the caller
     * @return a non-null object
     */
    public static JsonFormResponse<?> success(String title, String message) {
        return success().setMessage(message).setTitle(title);
    }

    /**
     * Creates a JSON response object with a failed outcome and a failure message attached to it.
     *
     * @param message the message to be sent to the caller
     * @return a non-null object
     */
    public static JsonFormResponse<?> fail(String message) {
        return fail(INTERNAL_ERROR, message);
    }

    /**
     * Creates a JSON response object with a failed outcome and an error code attached to it.
     *
     * @param errorCode the error code to be sent to the caller
     * @return a non-null object
     */
    public static JsonFormResponse<?> fail(int errorCode) {
        return fail(errorCode, null);
    }

    /**
     * Creates a JSON response object with a failed outcome, an error code and message attached to it.
     *
     * @param errorCode the error code to be sent to the caller
     * @param message   the message to be sent to the caller
     * @return a non-null object
     */
    public static JsonFormResponse<?> fail(int errorCode, String message) {
        return new JsonFormResponse<>(false).setSuccess(false).setErrorCode(errorCode).setMessage(message);
    }

    protected JsonFormResponse() {
    }

    protected JsonFormResponse(boolean success) {
        super(success);
    }

    /**
     * Adds errors.
     *
     * @param errors the errors as a map (field name -> error message)
     * @return self
     */
    public T addErrors(Map<String, ?> errors) {
        if (errors != null) errors.forEach((k, v) -> addError(k, ObjectUtils.toString(v)));
        return self();
    }

    /**
     * Adds a new error message related with a form field.
     *
     * @param fieldName the field name
     * @param message   the error message
     * @return self
     */
    public T addError(String fieldName, String message) {
        return addError(fieldName, message, true);
    }

    /**
     * Adds a new error message related with a form field.
     *
     * @param fieldName the field name
     * @param message   the error message
     * @param override  {@code true} to override the error message, {@code false} to ignore if an error already exists
     * @return self
     */
    public T addError(String fieldName, String message, boolean override) {
        requireNotEmpty(fieldName);
        requireNotEmpty(fieldName);
        if (override) {
            this.errors.put(fieldName, message);
        } else {
            this.errors.putIfAbsent(fieldName, message);
        }
        setSuccess(false);
        return (T) this;
    }

    /**
     * Returns the validation error associated with a data set field
     *
     * @param fieldName the field name
     * @return the error message or null if is no error message associated with the field
     */
    public String getError(String fieldName) {
        requireNonNull(fieldName);
        return errors.get(fieldName);
    }

    /**
     * Returns a map with errors.
     *
     * @return a non-null map
     */
    public Map<String, String> getErrors() {
        return unmodifiableMap(errors);
    }

    /**
     * Remove all errors.
     */
    public void clearErrors() {
        errors.clear();
    }

    /**
     * Adds warnings.
     *
     * @param warnings the warnings as a map (field name -> error message)
     * @return self
     */
    public T addWarnings(Map<String, ?> warnings) {
        if (warnings != null) warnings.forEach((k, v) -> addError(k, ObjectUtils.toString(v)));
        return self();
    }

    /**
     * Adds a new error message related with a form field.
     *
     * @param fieldName the field name
     * @param message   the error message
     * @return self
     */
    public T addWarning(String fieldName, String message) {
        return addWarning(fieldName, message, true);
    }

    /**
     * Adds a new error message related with a form field.
     *
     * @param fieldName the field name
     * @param message   the error message
     * @param override  {@code true} to override the error message, {@code false} to ignore if an error already exists
     * @return self
     */
    public T addWarning(String fieldName, String message, boolean override) {
        requireNotEmpty(fieldName);
        requireNotEmpty(fieldName);
        if (override) {
            this.warnings.put(fieldName, message);
        } else {
            this.warnings.putIfAbsent(fieldName, message);
        }
        setSuccess(false);
        return self();
    }

    /**
     * Returns the validation error associated with a data set field
     *
     * @param fieldName the field name
     * @return the error message or null if is no error message associated with the field
     */
    public String getWarning(String fieldName) {
        requireNonNull(fieldName);
        return warnings.get(fieldName);
    }

    /**
     * Returns a map with errors.
     *
     * @return a non-null map
     */
    public Map<String, String> getWarnings() {
        return unmodifiableMap(warnings);
    }

    /**
     * Remove all warnings.
     */
    public void clearWarnings() {
        warnings.clear();
    }

    @Override
    public String toString() {
        return "JsonFormResponse{" +
                "errors=" + errors +
                ", warnings=" + warnings +
                "} " + super.toString();
    }
}
