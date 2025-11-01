package net.microfalx.bootstrap.jdbc.migration;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.support.Database;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Getter
@ToString
public final class Definition implements Identifiable<String>, Nameable {

    private final String id;
    private final String name;
    Module module;
    String path;
    Database.Type databaseType = Database.Type.MYSQL;
    Set<String> tables = new HashSet<>();
    Set<String> dependsOn = new HashSet<>();
    int order;
    private final List<Migration> migrations = new ArrayList<>();

    Definition(Module module, String name, String path) {
        requireNonNull(module);
        requireNonNull(name);
        Hashing hashing = Hashing.create();
        hashing.update(module.getId());
        hashing.update(path);
        hashing.update(databaseType);
        this.id = hashing.asString();
        this.module = module;
        this.name = name;
        this.path = path;
    }

    public int getOrder() {
        return module.getOrder() * 100 + order;
    }

    void addMigration(Migration migration) {
        requireNonNull(migration);
        migrations.add(migration);
    }
}
