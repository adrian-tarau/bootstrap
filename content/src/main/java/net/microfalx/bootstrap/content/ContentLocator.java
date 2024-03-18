package net.microfalx.bootstrap.content;

import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;

import java.net.URI;
import java.util.Objects;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.*;

/**
 * A class which carries information to identify a content.
 * <p>
 * The locator can carry information about a page inside the content, which can be used during display.
 */
public final class ContentLocator implements Identifiable<String>, Cloneable {

    private final String id;
    private final String type;
    private final URI uri;
    private final Attributes<?> attributes;

    private int pageNumber = -1;
    private int pageSize = 20;

    /**
     * Creates the locator from a resource.
     *
     * @param resource the attributes
     * @return a non-null instance
     */
    public static ContentLocator create(Resource resource) {
        requireNonNull(resource);
        Attributes<?> attributes = Attributes.create();
        attributes.add(Content.ID_ATTR, resource.getId());
        attributes.add(Content.TYPE_ATTR, resource.toURI().getScheme());
        attributes.add(Content.URI_ATTR, resource.toURI().toASCIIString());
        return new ContentLocator(attributes);
    }

    /**
     * Creates the locator from the content attributes.
     *
     * @param attributes the attributes
     * @param <A>        the attribute type
     * @return a non-null instance
     */
    public static <A extends Attribute> ContentLocator create(Attributes<A> attributes) {
        return new ContentLocator(attributes);
    }

    /**
     * Creates the locator from the content attributes.
     *
     * @param id   the identifier of the content
     * @param type the type of the content
     * @param uri  the URI to the content
     * @return a non-null instance
     */
    public static ContentLocator create(String id, String type, String uri) {
        URI resolvedUri = URI.create(uri);
        return create(id, type, resolvedUri);
    }

    /**
     * Creates the locator from the content attributes.
     *
     * @param id   the identifier of the content
     * @param type the type of the content
     * @param uri  the URI to the content
     * @return a non-null instance
     */
    public static ContentLocator create(String id, String type, URI uri) {
        Attributes<?> attributes = Attributes.create();
        attributes.add(Content.ID_ATTR, id);
        attributes.add(Content.TYPE_ATTR, type);
        attributes.add(Content.URI_ATTR, uri.toASCIIString());
        return create(attributes);
    }

    ContentLocator(Attributes<?> attributes) {
        requireNotEmpty(attributes);
        this.attributes = Attributes.create(attributes, true);
        this.id = attributes.get(Content.ID_ATTR).asString();
        if (StringUtils.isEmpty(id)) {
            throw new ContentException("The content identifier is missing in attributes " + attributes);
        }
        this.type = attributes.get(Content.TYPE_ATTR).asString();
        if (StringUtils.isEmpty(type)) {
            throw new ContentException("The content type is missing in attributes " + attributes);
        }
        String uriAsString = attributes.get(Content.URI_ATTR).asString();
        if (StringUtils.isEmpty(uriAsString)) {
            throw new ContentException("The content URI is missing in attributes " + attributes);
        }
        this.uri = URI.create(uriAsString);
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Returns a type associated with the content.
     *
     * @return a non-null instance
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the URI associated with the content.
     *
     * @return a non-null instance
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the attributes used to create the locator.
     * <p>
     * The attributes instance is read-only.
     *
     * @return a non-null instance
     */
    public Attributes<?> getAttributes() {
        return attributes;
    }

    /**
     * Returns whether the content locator contains pagination information.
     *
     * @return {@code true} if there is pagination information, {@code false} otherwise
     */
    public boolean isPaged() {
        return pageNumber >= 0;
    }

    /**
     * Returns whether the locator does not contain pagination information.
     *
     * @return {@code true} if there is no pagination information, {@code false} otherwise
     */
    public boolean isUnpaged() {
        return !isPaged();
    }

    /**
     * Returns the page to be returned from the content when the content is displayed.
     *
     * @return the page to be returned
     * @throws UnsupportedOperationException if the object is {@link #isUnpaged()}.
     */
    public int getPageNumber() {
        checkPageInformation();
        return pageNumber;
    }

    /**
     * Creates a copy of this locator and changes the page number.
     *
     * @param pageNumber the new page number
     * @return a new instance
     */
    public ContentLocator withPageNumber(int pageNumber) {
        requireBounded(pageNumber, 0, Integer.MAX_VALUE);
        ContentLocator copy = copy();
        copy.pageNumber = pageNumber;
        return copy;
    }

    /**
     * Returns the number of items to be returned.
     *
     * @return the number of items
     * @throws UnsupportedOperationException if the object is {@link #isUnpaged()}.
     */
    public int getPageSize() {
        checkPageInformation();
        return pageSize;
    }


    /**
     * Creates a copy of this locator and changes the page size.
     *
     * @param pageSize the new page size
     * @return a new instance
     */
    public ContentLocator withPageSizeNumber(int pageSize) {
        requireBounded(pageSize, 0, Integer.MAX_VALUE);
        ContentLocator copy = copy();
        copy.pageSize = pageSize;
        return copy;
    }

    /**
     * Returns the offset to be taken according to the underlying page and page size.
     *
     * @return the offset
     * @throws UnsupportedOperationException if the object is {@link #isUnpaged()}.
     */
    public long getOffset() {
        checkPageInformation();
        return (long) pageNumber * pageSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentLocator that = (ContentLocator) o;
        return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, uri);
    }

    private void checkPageInformation() {
        if (!isPaged()) throw new UnsupportedOperationException("No page information is available");
    }

    private ContentLocator copy() {
        try {
            return (ContentLocator) clone();
        } catch (CloneNotSupportedException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ContentLocator.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("type='" + type + "'")
                .add("uri=" + uri)
                .add("attributes=" + attributes.size())
                .toString();
    }
}
