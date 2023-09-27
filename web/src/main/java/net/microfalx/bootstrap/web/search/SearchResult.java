package net.microfalx.bootstrap.web.search;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.lang.annotation.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

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

    @Name
    @Position(2)
    @Formattable(maximumLength = 300, maximumLines = 2)
    private String description;

    @Position(3)
    private String type;

    @Position(10)
    @Formattable(negativeValue = Formattable.NA)
    private float relevance;

    @Visible(false)
    private String owner;

    @Position(100)
    private LocalDateTime createdAt;

    @Position(101)
    @Timestamp
    @OrderBy(OrderBy.Direction.DESC)
    private LocalDateTime modifiedAt;

    @Visible(false)
    private Collection<? extends Attribute> attributes = Collections.emptyList();
}
