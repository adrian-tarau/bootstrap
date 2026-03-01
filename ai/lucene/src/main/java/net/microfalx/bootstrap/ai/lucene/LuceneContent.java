package net.microfalx.bootstrap.ai.lucene;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.embedding.Embedding;

@Getter
@Setter
public class LuceneContent {

    protected static final String DUMMY_TEXT = "Dummy";

    @Setter(AccessLevel.PROTECTED)
    private Embedding embedding;

    LuceneContent() {

    }


}
