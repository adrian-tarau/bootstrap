package net.microfalx.bootstrap.help;

import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.UriUtils;
import net.microfalx.resource.Resource;

import java.util.*;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Represents a Table of Contents (ToC) for help documentation.
 */
public class Toc extends NamedIdentityAware<String> {

     static final String ROOT_ID = "<root>";

    private Toc parent;
    private List<Toc> children;
    String fileName;
    int order = -1;
    private Resource content;

    public Toc() {
        this(Toc.ROOT_ID, "Root");
    }

    protected Toc(String id, String name) {
        requireNotEmpty(id);
        requireNotEmpty(name);
        setId(id);
        setName(name);
    }

    /**
     * Returns the parent TOC entry.
     * @return the parent TOC entry or null if this is the root TOC entry
     */
    public Toc getParent() {
        return parent;
    }

    /**
     * Returns true if this TOC entry is the root TOC entry.
     *
     * @return true if this is the root TOC entry
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Returns the children of this TOC entry.
     * @return a non-null list
     */
    public List<Toc> getChildren() {
        return children != null ? unmodifiableList(children): Collections.emptyList();
    }

    /**
     * Returns true if this TOC entry has no children.
     *
     * @return {@code true} if this TOC entry has no children
     */
    public boolean isLeaf() {
        return ObjectUtils.isEmpty(children);
    }

    /**
     * Returns true if this TOC entry has children.
     *
     * @return {@code true} if this TOC entry has children
     */
    public boolean hasChildren() {
        return !isLeaf();
    }

    /**
     * Returns the parents of this TOC entry, up to with the root TOC entry.
     *
     * @return a non-null list of TOC entries
     */
    public List<Toc> getParents() {
        List<Toc> parents = new ArrayList<>();
        Toc entry = this;
        while (entry != null) {
            if (ROOT_ID.equalsIgnoreCase(entry.getId())) break;
            parents.add(entry);
            entry = entry.parent;
        }
        Collections.reverse(parents);
        return unmodifiableList(parents);
    }

    /**
     * Returns the path of the TOC to the class path resource.
     *
     * @return a non-null instance
     */
    public String getPath() {
        StringBuilder builder = new StringBuilder();
        Toc entry = this;
        while (entry != null) {
            if (ROOT_ID.equalsIgnoreCase(entry.getId())) break;
            if (!builder.isEmpty()) builder.insert(0, '/');
            builder.insert(0, entry.getId());
            entry = entry.parent;
        }
        return builder.toString();
    }

    /**
     * Returns the path of the TOC to the class path resource.
     *
     * @return a non-null instance
     */
    public String getNumbering() {
        StringBuilder builder = new StringBuilder();
        Toc entry = this;
        while (entry != null) {
            if (ROOT_ID.equalsIgnoreCase(entry.getId())) break;
            if (!builder.isEmpty()) builder.insert(0, '.');
            builder.insert(0, entry.getPositionInParent());
            entry = entry.parent;
        }
        return builder.toString() + ".";
    }

    /**
     * Returns the depth of this TOC entry in the hierarchy.
     *
     * @return the depth, where 0 is the root TOC entry
     */
    public int getDepth() {
        int depth = 0;
        Toc entry = this;
        while (entry != null) {
            if (ROOT_ID.equalsIgnoreCase(entry.getId())) break;
            depth++;
            entry = entry.parent;
        }
        return depth;
    }

    /**
     * Returns the file name of the file providing the contents of this TOC.
     * @return a non-null instance
     */
    public String getFileName() {
        return isNotEmpty(fileName)?fileName:getId()+".md";
    }

    /**
     * Returns the order of this TOC entry inside its parent.
     * @return a positive integer
     */
    public int getOrder() {
        return order;
    }

    /**
     * Finds a child TOC entry by its path.
     * @param path the path of the TOC entry, e.g. "getting-started/installation"
     * @return the toc
     */
    public Toc findByPath(String path) {
        requireNonNull(path);
        String[] paths = StringUtils.split(path, UriUtils.SLASH);
        Toc currentParent = this;
        Toc currentToc = null;
        for (String childPath : paths) {
             currentToc = currentParent.findById(childPath);
            if (currentToc == null) return null;
            currentParent = currentToc;
        }
        return currentToc;
    }

    /**
     * Finds a child TOC entry by its identifier.
     * @param id the identifier of the TOC entry, e.g. "getting-started"
     * @return the TOC entry or null if not found
     */
    public Toc findById(String id) {
        requireNonNull(id);
        if (children == null) return null;
        for (Toc child : children) {
            if (child.getId().equalsIgnoreCase(id)) return child;
        }
        return null;
    }

    /**
     * Returns the content of this TOC entry.
     * @return a non-null instance
     */
    public Resource getContent() {
        if (content != null) {
            return content;
        } else if (parent == null) {
            return HelpUtilities.resolveContent("home");
        } else {
            return HelpUtilities.resolveContent(getPath());
        }
    }

    /**
     * Creates a copy of this TOC entry and
     *
     * @param content the new content
     * @return a new instance of Toc with the specified content
     */
    public Toc withContent(Resource content) {
        requireNonNull(content);
        Toc copy = copy();
        copy.content = content;
        return copy;
    }

    @Override
    protected Toc copy() {
        Toc toc = (Toc) super.copy();
        if (this.parent != null) this.parent.removeChild(this);
        return toc;
    }

    void addChild(Toc child) {
        requireNonNull(child);
        if (children == null) children = new ArrayList<>();
        if (child.order == -1) child.order = children.size();
        children.add(child);
        child.parent = this;
        children.sort(Comparator.comparing(Toc::getOrder));
    }

    void removeChild(Toc child) {
        requireNonNull(child);
        if (children != null) {
            children.remove(child);
            child.parent = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Toc toc)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(parent, toc.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parent);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Toc.class.getSimpleName() + "[", "]")
                .add("id=" + getId())
                .add("name=" + getName())
                .add("parent=" + (parent != null?parent.getId():"null"))
                .add("children=" + (children != null? children.size():0))
                .add("fileName='" + fileName + "'")
                .add("order=" + order)
                .toString();
    }

    private int getPositionInParent() {
        if (parent == null) return 0;
        if (parent.children == null) return 0;
        for (int i = 0; i < parent.children.size(); i++) {
            if (parent.children.get(i).equals(this)) return (i + 1);
        }
        return 0;
    }
}
