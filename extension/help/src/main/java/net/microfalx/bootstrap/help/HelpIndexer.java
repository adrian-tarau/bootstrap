package net.microfalx.bootstrap.help;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.search.Attribute;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.lang.Hashing;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.Temporal;

import static net.microfalx.bootstrap.help.HelpUtilities.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;

/**
 * Indexes the help content for the application.
 */
@Slf4j
class HelpIndexer implements Runnable {

    private final HelpService helpService;
    private final IndexService indexService;
    private final Toc root;

    HelpIndexer(HelpService helpService, IndexService indexService, Toc root) {
        requireNonNull(helpService);
        requireNonNull(indexService);
        requireNonNull(root);
        this.helpService = helpService;
        this.indexService = indexService;
        this.root = root;
    }

    @Override
    public void run() {
        try {
            index(root);
        } finally {
            helpService.indexed.set(true);
        }

    }

    private void index(Toc toc) {
        indexContent(toc);
        for (Toc child : toc.getChildren()) {
            index(child);
        }
    }

    private void indexContent(Toc toc) {
        try {
            if (toc.getContent().exists()) doIndexContent(toc);
        } catch (Exception e) {
            LOGGER.warn("Failed to index TOC content for {}, root cause: {}",
                    toc.getPath(), getRootCauseMessage(e));
        }
    }

    private void doIndexContent(Toc toc) {
        Hashing hashing = Hashing.create();
        hashing.update(DOCUMENT_OWNER);
        if (toc.getParent() != null) hashing.update(toc.getParent().getId());
        hashing.update(toc.getPath());
        Document doc = Document.create(hashing.asString(), toc.getName());
        doc.setOwner(DOCUMENT_OWNER).setType(DOCUMENT_TYPE);
        doc.setBody(toc.getContent());
        Temporal lastModified;
        try {
            lastModified = Instant.ofEpochMilli(toc.getContent().lastModified());
        } catch (IOException e) {
            lastModified = Instant.now();
        }
        doc.setCreatedAt(lastModified);
        doc.setModifiedAt(lastModified);
        doc.setReference(toc.getPath());
        doc.addTag(DOCUMENT_OWNER);
        doc.addIfAbsent(Attribute.create(PATH_FIELD, toc.getPath()).setIndexed(false));
        indexService.index(doc);
    }
}
