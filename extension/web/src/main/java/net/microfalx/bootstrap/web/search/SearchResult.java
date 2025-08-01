package net.microfalx.bootstrap.web.search;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.dataset.model.IdentityAware;
import net.microfalx.bootstrap.search.Attribute;
import net.microfalx.lang.annotation.*;
import net.microfalx.resource.Resource;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@ToString
@Name("Search")
@ReadOnly
public class SearchResult extends IdentityAware<String> {

    @Visible(false)
    private String name;

    @Visible(false)
    private String description;

    @Name
    @Position(2)
    @Formattable(maximumLength = 300, maximumLines = 2)
    @Description("The name or description of the document")
    @Width("50%")
    private String title;

    @Position(3)
    @Description("The owner of the document (usually a service or a module)")
    @Width("90px")
    private String owner;

    @Position(4)
    @Description("The type of the document (the data type)")
    @Width("90px")
    private String type;

    @Position(10)
    @Formattable(negativeValue = Formattable.NA)
    @Description("The relevance of the document when a text search is performed")
    @Visible(false)
    private float relevance;

    @Position(20)
    @Description("The length (size) of the document")
    @Formattable(unit = Formattable.Unit.BYTES)
    @Width("90px")
    private int length;

    @Position(25)
    @Label("Fields")
    @Description("The number of fields present in the document")
    @Width("90px")
    private int attributeCount;

    @Position(100)
    @Description("The timestamp when the document was added")
    private LocalDateTime createdAt;

    @Position(101)
    @Timestamp
    @OrderBy(OrderBy.Direction.DESC)
    @Description("The timestamp when the document was modified last time")
    private LocalDateTime modifiedAt;

    @Visible(false)
    private Collection<Attribute> topAttributes;

    @Visible(false)
    private Collection<Attribute> coreAttributes;

    @Visible(false)
    private Collection<Attribute> attributes;

    @Visible(false)
    private String mimeType;

    @Visible(false)
    @Label("Body")
    @Description("The body of the document")
    private Resource body;

    @Visible(false)
    private URI bodyUri;

    @Visible(false)
    private String reference;
}
