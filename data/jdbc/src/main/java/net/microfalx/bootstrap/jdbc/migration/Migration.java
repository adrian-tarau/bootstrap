package net.microfalx.bootstrap.jdbc.migration;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

@Getter
@ToString
public final class Migration implements Identifiable<String>, Nameable {

    @ToString.Exclude
    private final Definition definition;
    private final String id;
    private final String path;
    Condition condition;

    Migration(Definition definition, String path) {
        requireNotEmpty(definition);
        requireNotEmpty(path);
        this.definition = definition;
        Hashing hashing = Hashing.create();
        hashing.update(definition.getId());
        hashing.update(definition.getPath());
        hashing.update(definition.getDatabaseType());
        this.id = hashing.asString();
        this.path = path;
    }

    @Override
    public String getName() {
        return path;
    }

}
