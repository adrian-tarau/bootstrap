package net.microfalx.bootstrap.web.search.support;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.bootstrap.search.Indexer;
import net.microfalx.bootstrap.search.IndexerOptions;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.annotation.*;

import static net.microfalx.lang.CollectionUtils.setToString;

@Getter
@Setter
@ToString
@Name("Indexes")
@ReadOnly
public class Index extends NamedIdentityAware<String> {

    @Position(10)
    @Description("Indicates whether the index is the primary index")
    private boolean primary;

    @Position(15)
    @Name
    @Description("The directory where the index is stored")
    @Width("20%")
    private String directory;

    @Position(20)
    @Description("The analyzer used to tokenize the documents in the index")
    @Width("20%")
    private String analyzer;

    @Position(30)
    @Description("The index size in bytes on disk")
    @Label(value = "Disk", group = "Size")
    @Formattable(unit = Formattable.Unit.BYTES)
    private long diskSize;

    @Position(30)
    @Description("The index size in bytes in memory")
    @Label(value = "Memory", group = "Size")
    @Formattable(unit = Formattable.Unit.BYTES)
    private long memorySize;

    @Position(35)
    @Description("The total number of documents in the index")
    @Label(value = "Total", group = "Documents")
    private long documentCount = -1;

    @Position(36)
    @Description("The number of documents that have been added since the last commit")
    @Label(value = "Pending", group = "Documents")
    private long pendingDocumentCount = -1;

    @Position(400)
    @Component(Component.Type.TEXT_AREA)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;

    public static Index from(Indexer indexer) {
        IndexerOptions options = indexer.getOptions();
        Index model = new Index();
        model.setId(options.getId());
        model.setName(options.getName());
        model.setDescription(options.getDescription());
        model.setDirectory(ObjectUtils.toString(options.getDirectory()));
        model.setAnalyzer(ClassUtils.getCompactName(options.getAnalyzer()));
        model.setPrimary(options.isPrimary());
        model.setMemorySize(indexer.getMemorySize());
        model.setDiskSize(indexer.getDiskSize());
        model.setTags(setToString(options.getTags()));
        try {
            model.setDocumentCount(indexer.getDocumentCount());
            model.setPendingDocumentCount(indexer.getPendingDocumentCount());
        } catch (Exception e) {
            // ignore if we cannot retrieve the document count
        }
        return model;
    }
}
