package net.microfalx.bootstrap.feature;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Represents an application feature.
 */
@Getter
@ToString
public class Feature implements Identifiable<String>, Nameable {

    private final String id;
    private final String name;

    public static Feature create(String id, String name) {
        return new Feature(id, name);
    }

    private Feature(String id, String name) {
        requireNotEmpty(id);
        requireNotEmpty(name);
        this.id = id;
        this.name = name;
    }

}
