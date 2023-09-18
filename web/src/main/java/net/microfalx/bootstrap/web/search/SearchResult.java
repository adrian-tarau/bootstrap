package net.microfalx.bootstrap.web.search;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.search.Attribute;
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
    private String name;

    @Position(3)
    private String type;

    @Visible(false)
    private String owner;

    @Position(50)
    @Formattable(maximumLength = 300, maximumLines = 2)
    private String description;

    @Position(100)
    @Timestamp
    private LocalDateTime timestamp;

    @Visible(false)
    private Collection<Attribute> attributes = Collections.emptyList();
}
