package net.microfalx.bootstrap.web.search;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.search.Attribute;
import net.microfalx.lang.annotation.*;
import net.microfalx.resource.Resource;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Name("Search")
@ReadOnly
public class SearchResult {

    @Id
    @Visible(value = false)
    private String id;

    @Visible(false)
    private String name;

    @Visible(false)
    private String description;

    @Name
    @Position(2)
    @Formattable(maximumLength = 300, maximumLines = 2)
    private String title;

    @Position(3)
    private String owner;

    @Position(4)
    private String type;

    @Position(10)
    @Formattable(negativeValue = Formattable.NA)
    private float relevance;

    @Position(20)
    private int length;

    @Position(25)
    @Label("Fields")
    private int attributeCount;

    @Position(100)
    private LocalDateTime createdAt;

    @Position(101)
    @Timestamp
    @OrderBy(OrderBy.Direction.DESC)
    private LocalDateTime modifiedAt;

    @Visible(false)
    private Collection<Attribute> topAttributes;

    @Visible(false)
    private Collection<Attribute> attributes;

    @Visible(false)
    private String mimeType;

    @Visible(false)
    private Resource body;
}
