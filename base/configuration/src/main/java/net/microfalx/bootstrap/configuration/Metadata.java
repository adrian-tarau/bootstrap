package net.microfalx.bootstrap.configuration;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.bootstrap.configuration.ConfigurationUtils.ROOT_METADATA_ID;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.emptyIfNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

@Getter
@ToString
public class Metadata implements Identifiable<String>, Nameable, Descriptable {

    @ToString.Exclude
    private final Metadata parent;
    private final String key;
    private String fullKey;
    private String id;
    private final String name;

    String section;
    String defaultValue;
    String description;
    int order;
    boolean multiline;
    boolean client;
    int lineCount;
    DataType dataType = DataType.STRING;
    Number minimum;
    Number maximum;
    List<Metadata> children = new ArrayList<>();

    Metadata(Metadata parent, String key, String name) {
        requireNotEmpty(name);
        this.parent = parent;
        this.key = emptyIfNull(key);
        this.name = name;
        update();
    }

    public String getId() {
        return this.id;
    }

    public List<Metadata> getChildren() {
        return unmodifiableList(children);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    protected void add(Metadata metadata) {
        children.add(metadata);
        children.sort(Comparator.comparing(Metadata::getOrder));
    }

    private void update() {
        StringBuilder builder = new StringBuilder();
        if (parent == null) {
            builder.append(key);
        } else {
            Metadata parent = this.parent;
            builder.append(key);
            while (parent != null && !parent.getKey().equals(ROOT_METADATA_ID)) {
                builder.insert(0, parent.getKey() + ".");
                parent = parent.getParent();
            }
        }
        this.fullKey = builder.toString();
        this.id = toIdentifier(builder.toString());
    }

    public enum DataType {
        STRING,
        INTEGER,
        NUMBER,
        BOOLEAN,
        DURATION;

        public boolean isString() {
            return this == STRING || this == DURATION;
        }

        public boolean isBoolean() {
            return this == BOOLEAN;
        }

        public boolean isNumeric() {
            return this == INTEGER || this == NUMBER;
        }
    }
}
