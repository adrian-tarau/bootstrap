package net.microfalx.bootstrap.search;

import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.*;

import static net.microfalx.bootstrap.search.SearchUtils.NA_TIMESTAMP;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ObjectUtils.isEmpty;
import static net.microfalx.lang.TimeUtils.fromMillis;
import static net.microfalx.lang.TimeUtils.toMillis;

/**
 * Holds a document.
 * <p>
 * A document has a unique identifier, a name, possible a type and owner, creation and modification time. The description is
 * actually the body of the document.
 * <p>
 * In addition to the core attributes above, a document can have optional attributes using {@link Attribute}.
 * <p>
 * Documents are indexed and stored by the IndexService and can be retrieved by the SearchService.
 */
public final class Document implements Serializable {

    @Serial
    private static final long serialVersionUID = -3913827551374244394L;

    private String id;
    private String name;
    private String description;
    private Resource body;
    private URI bodyUri;
    private String type;

    private String owner;

    long timestamp = NA_TIMESTAMP;
    long createdAt = NA_TIMESTAMP;
    long modifiedAt = NA_TIMESTAMP;
    long receivedAt = NA_TIMESTAMP;
    long sentAt = NA_TIMESTAMP;

    /**
     * A set of tags which can be used to locate items (they are indexed)
     */
    private Set<String> tags;

    /**
     * A collection of (key,value) pairs which can store item attributes, and they can be searched (if searching is enabled)
     */
    Map<String, Attribute> attributes;

    /**
     * A collection of (key,value) pairs which are stored with the item and serve as display labels for an item (they can be displayed in a drop down)
     */
    Map<String, String> labels;

    private Object data;

    public static Document create(String id) {
        return new Document(id, null);
    }

    public static Document create(String id, String name) {
        return new Document(id, name);
    }

    // TODO the requirement to have a public constructor for an Externalizable is stupid; we need to serialize a placeholder instead of the actual class
    public Document() {
    }

    protected Document(String id, String name) {
        requireNonNull(id);

        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    protected Document setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Document setDescription(String description) {
        this.description = description;
        return this;
    }

    public Resource getBody() {
        if (body == null && bodyUri != null) {
            body = ResourceFactory.resolve(bodyUri);
        }
        return body;
    }

    public void setBody(Resource body) {
        this.body = body;
        this.bodyUri = body != null ? body.toURI() : null;
    }

    public URI getBodyUri() {
        return bodyUri;
    }

    public void setBodyUri(URI bodyUri) {
        this.bodyUri = bodyUri;
    }

    public String getType() {
        return type;
    }

    public Document setType(String type) {
        this.type = type;

        return this;
    }

    public String getOwner() {
        return owner;
    }

    public Document setOwner(String owner) {
        this.owner = owner;

        return this;
    }

    public ZonedDateTime getTimestamp() {
        return fromMillis(timestamp);
    }

    public Document setTimestamp(Temporal timestamp) {
        requireNonNull(timestamp);
        this.timestamp = toMillis(timestamp);
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return fromMillis(createdAt);
    }

    public Document setCreatedAt(Temporal createdAt) {
        this.createdAt = createdAt != null ? toMillis(createdAt) : NA_TIMESTAMP;
        this.timestamp = this.createdAt;
        return this;
    }

    public ZonedDateTime getModifiedAt() {
        return fromMillis(modifiedAt);
    }

    public Document setModifiedAt(Temporal modifiedAt) {
        this.modifiedAt = modifiedAt != null ? toMillis(modifiedAt) : NA_TIMESTAMP;
        this.timestamp = this.modifiedAt;
        return this;
    }

    public ZonedDateTime getReceivedAt() {
        return fromMillis(receivedAt);
    }

    public Document setReceivedAt(Temporal receivedAt) {
        this.receivedAt = receivedAt != null ? toMillis(receivedAt) : NA_TIMESTAMP;
        this.timestamp = this.receivedAt;
        return this;
    }

    public ZonedDateTime getSentAt() {
        return fromMillis(sentAt);
    }

    public Document setSentAt(Temporal sentAt) {
        this.sentAt = sentAt != null ? toMillis(sentAt) : NA_TIMESTAMP;
        this.timestamp = this.sentAt;
        return this;
    }

    public Object getData() {
        return data;
    }

    public Document setData(Object data) {
        this.data = data;
        return this;
    }

    public int getTagCount() {
        if (tags == null) return 0;
        return tags.size();
    }

    public Set<String> getTags() {
        if (tags == null) return Collections.emptySet();
        return tags;
    }

    public Document addTag(String tag) {
        requireNonNull(tag);
        if (tags == null) tags = new HashSet<>();
        tags.add(tag);
        return this;
    }

    public Document addTags(String... tags) {
        if (isEmpty(tags)) return this;
        if (this.tags == null) this.tags = new HashSet<>();
        this.tags.addAll(Arrays.asList(tags));
        return this;
    }

    public void removeTag(String tag) {
        requireNonNull(tag);
        if (tags == null) return;
        tags.remove(tag);
    }

    public Collection<Attribute> getAttributes() {
        if (attributes == null) return Collections.emptyList();
        return attributes.values();
    }

    public int getAttributeCount() {
        if (attributes == null) return 0;
        return attributes.size();
    }

    public Attribute addAttribute(Attribute attribute) {
        requireNonNull(attribute);
        if (attributes == null) attributes = new HashMap<>();
        attributes.put(attribute.getName(), attribute);
        return attribute;
    }

    public Attribute addAttribute(String name, Object value) {
        requireNonNull(name);
        return addAttribute(Attribute.create(name, value));
    }

    public Attribute getAttribute(String name) {
        requireNonNull(name);
        if (attributes == null) return null;
        return attributes.get(name);
    }

    public Attribute getAttribute(String name, Object defaultValue) {
        requireNonNull(name);
        Object value = getAttribute(name);
        if (value == null) return Attribute.create(name, defaultValue);
        return attributes.get(name);
    }

    public Attribute removeAttribute(String name) {
        requireNonNull(name);
        if (attributes == null) return null;
        return attributes.remove(name);
    }

    public Document addLabel(String name, String value) {
        requireNonNull(name);
        if (labels == null) labels = new HashMap<>();
        labels.put(name, value);
        return this;
    }

    public String getLabel(String name) {
        requireNonNull(name);

        if (labels == null) return null;
        return labels.get(name);
    }

    public Map<String, String> getLabels() {
        if (labels == null) return Collections.emptyMap();
        return Collections.unmodifiableMap(labels);
    }

    public int getLabelCount() {
        if (labels == null) return 0;
        return labels.size();
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description=" + description +
                ", type='" + type + '\'' +
                ", owner='" + owner + '\'' +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", receivedAt=" + receivedAt +
                ", tags=" + tags +
                ", attributes=" + attributes +
                ", labels=" + labels +
                ", data=" + data +
                '}';
    }
}
