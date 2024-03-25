package net.microfalx.bootstrap.content;

import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.lang.*;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * A class which caries information about a content.
 * <p>
 * A content has an identifier and a type and a resource, possible a name, description and mime type.
 */
public final class Content implements Identifiable<String>, Nameable, Descriptable, Cloneable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Content.class);

    public static final String ID_ATTR = "id";
    public static final String NAME_ATTR = "name";
    public static final String TYPE_ATTR = "type";
    public static final String URI_ATTR = "uri";
    public static final String DESCRIPTION_ATTR = "description";
    public static final String MIME_TYPE_ATTR = "mime_type";

    private final ContentLocator locator;
    private final Resource resource;
    private final long createdAt = System.currentTimeMillis();

    private Attributes<?> attributes;

    /**
     * Creates content from a resource.
     *
     * @param resource the resource
     * @return a non-null instance
     */
    public static Content create(Resource resource) {
        return create(ContentLocator.create(resource), resource);
    }

    /**
     * Creates content from a locator and the resource located with the locator.
     *
     * @param locator  the content locator
     * @param resource the resource
     * @return a non-null instance
     */
    public static Content create(ContentLocator locator, Resource resource) {
        return new Content(locator, resource);
    }

    Content(ContentLocator locator, Resource resource) {
        requireNotEmpty(locator);
        requireNotEmpty(resource);
        this.attributes = Attributes.create(locator.getAttributes());
        this.locator = ContentLocator.create(attributes);
        this.resource = resource;
    }

    @Override
    public String getId() {
        return locator.getId();
    }

    @Override
    public String getName() {
        String name = attributes.get(NAME_ATTR).asString();
        return isNotEmpty(name) ? name : resource.getName();
    }

    @Override
    public String getDescription() {
        String description = attributes.get(DESCRIPTION_ATTR).asString();
        return isNotEmpty(description) ? description : resource.getDescription();
    }

    /**
     * Returns the attributes associated with the content.
     *
     * @return a non-null instance
     */
    public Attributes<?> getAttributes() {
        return attributes;
    }

    /**
     * Returns the content locator.
     *
     * @return a non-null instance
     */
    public ContentLocator getLocator() {
        return locator;
    }

    /**
     * Returns the resource which holds the actual content.
     *
     * @return a non-null instance
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Returns the content as a string.
     *
     * @return a non-empty string
     * @throws IOException an I/O exception if the content cannot be extracted
     */
    public String loadAsString() throws IOException {
        return resource.loadAsString();
    }

    /**
     * Returns a type associated with the content.
     *
     * @return a non-null instance
     */
    public String getType() {
        return locator.getType();
    }

    /**
     * Returns the URI associated with the content.
     *
     * @return a non-null instance
     */
    public URI getUri() {
        return locator.getUri();
    }

    /**
     * Returns the mime type associated with the content.
     *
     * @return a non-null instance
     */
    public String getMimeType() {
        String mimeType = attributes.get(MIME_TYPE_ATTR).asString();
        return defaultIfEmpty(mimeType, resource.getMimeType());
    }

    /**
     * Returns whether the content exists.
     *
     * @return {@code true} if content exists, {@code false} otherwise
     * @throws IOException if the existence cannot be calculated
     */
    public boolean exists() throws IOException {
        return resource.exists();
    }

    /**
     * Returns the time when the content object was created.
     *
     * @return a non-null instance
     */
    public LocalDateTime getCreatedAt() {
        return TimeUtils.toLocalDateTime(createdAt);
    }

    /**
     * Creates a copy of this object and changes the name.
     *
     * @param name the new name, null to use the name from resource (default)
     * @return a new instance
     */
    public Content withName(String name) {
        Content copy = copy();
        copy.attributes.add(NAME_ATTR, name);
        return copy;
    }

    /**
     * Creates a copy of this object and changes the description.
     *
     * @param description the new description, null to use the description from resource (default)
     * @return a new instance
     */
    public Content withDescription(String description) {
        Content copy = copy();
        copy.attributes.add(DESCRIPTION_ATTR, description);
        return copy;
    }

    /**
     * Creates a copy of this object and updates the attributes.
     *
     * @param attributes the new attributes
     * @return a new instance
     */
    public Content withAttributes(Attributes<?> attributes) {
        Content copy = copy();
        copy.attributes.copyFrom(attributes);
        return copy;
    }

    /**
     * Creates a copy of this object and changes the mime type.
     *
     * @param mimeType the new mime type, null to use the mime type from resource (default)
     * @return a new instance
     */
    public Content withMimeType(String mimeType) {
        Content copy = copy();
        copy.attributes.add(MIME_TYPE_ATTR, mimeType);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Content content = (Content) o;
        return Objects.equals(locator, content.locator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locator);
    }

    private Content copy() {
        try {
            Content content = (Content) clone();
            content.attributes = Attributes.create(this.attributes);
            return content;
        } catch (CloneNotSupportedException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Content.class.getSimpleName() + "[", "]")
                .add("id='" + getId() + "'")
                .add("name='" + getName() + "'")
                .add("description='" + getDescription() + "'")
                .add("mimeType='" + getMimeType() + "'")
                .add("type='" + getType() + "'")
                .add("resource=" + resource.toURI())
                .toString();
    }
}
