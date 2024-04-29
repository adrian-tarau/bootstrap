package net.microfalx.bootstrap.metrics;

import lombok.ToString;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A result of a {@link Query}.
 */
@ToString
public class Result implements Cloneable {

    private final Query query;
    private final Type type;
    private final boolean successful;
    private String message;

    final Collection<Matrix> matrixes = new ArrayList<>();
    final Collection<Vector> vectors = new ArrayList<>();
    private Value value;
    private String text;

    static Result failed(Query query) {
        return new Result(query, Type.SCALAR, false);
    }

    static Result success(Query query, Type type) {
        return new Result(query, type, true);
    }

    public static Result matrix(Query query, Collection<Matrix> matrixes) {
        Result result = new Result(query, Type.MATRIX, true);
        result.matrixes.addAll(matrixes);
        return result;
    }

    public static Result vector(Query query, Collection<Vector> vectors) {
        Result result = new Result(query, Type.VECTOR, true);
        result.vectors.addAll(vectors);
        return result;
    }

    public static Result scalar(Query query, Value value) {
        Result result = new Result(query, Type.SCALAR, true);
        result.value = value;
        return result;
    }

    static Result text(Query query, String text) {
        Result result = new Result(query, Type.STRING, true);
        result.text = text;
        return result;
    }

    private Result(Query query, Type type, boolean successful) {
        requireNonNull(query);
        requireNonNull(type);
        this.query = query;
        this.type = type;
        this.successful = successful;
    }

    public Query getQuery() {
        return query;
    }

    public Type getType() {
        return type;
    }

    public Collection<Matrix> getMatrixes() {
        return unmodifiableCollection(matrixes);
    }

    public Collection<Vector> getVectors() {
        return unmodifiableCollection(vectors);
    }

    public Value getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public boolean isEmpty() {
        return switch (type) {
            case STRING -> StringUtils.isEmpty(text);
            case SCALAR -> value == null;
            case VECTOR -> vectors.isEmpty();
            case MATRIX -> matrixes.isEmpty();
        };
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }

    public Result withMaxtrixes(Collection<Matrix> matrixes) {
        Result copy = copy();
        copy.matrixes.clear();
        copy.matrixes.addAll(matrixes);
        return copy;
    }

    public Result withVectors(Collection<Vector> vectors) {
        Result copy = copy();
        copy.vectors.clear();
        copy.vectors.addAll(vectors);
        return copy;
    }

    public Result withValue(Value value) {
        Result copy = copy();
        copy.value = value;
        return copy;
    }

    public Result withText(String text) {
        Result copy = copy();
        copy.text = text;
        return copy;
    }

    public Result withMessage(String message) {
        Result copy = copy();
        copy.message = message;
        return copy;
    }

    private Result copy() {
        try {
            return (Result) clone();
        } catch (CloneNotSupportedException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    public enum Type {
        MATRIX,
        VECTOR,
        SCALAR,
        STRING
    }
}
