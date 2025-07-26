package net.microfalx.bootstrap.web.preference;

import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Holds a user preference.
 *
 * @param <T> the data type
 */
@Getter
@ToString
public class Preference<T> {

    private final String name;
    private final Optional<T> value;

    public static <T> Preference<T> create(String name) {
        return new Preference<>(name, null);
    }

    public static <T> Preference<T> create(String name, T value) {
        return new Preference<>(name, value);
    }

    Preference(String name, T value) {
        requireNotEmpty(name);
        if (value instanceof List<?> || value instanceof Map) {
            throw new IllegalArgumentException("The value cannot be an instance of List or Map");
        }
        this.name = name;
        this.value = Optional.ofNullable(value);
    }

    /**
     * Returns the name of this preference.
     *
     * @return a non-null instance
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value associated with this preference.
     *
     * @return a non-null instance
     */
    public Optional<T> getValue() {
        return value;
    }

    /**
     * Returns the value associated with this preference or a default value if there is no associated value.
     *
     * @param defaultValue the default value
     * @return the value or the default value
     */
    public T getValue(T defaultValue) {
        return value.orElse(defaultValue);
    }

}
