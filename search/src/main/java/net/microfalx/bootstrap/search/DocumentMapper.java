package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.lang.TimeUtils;
import net.microfalx.metrics.Metrics;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.net.URI;

import static net.microfalx.bootstrap.search.Document.*;
import static net.microfalx.bootstrap.search.SearchUtils.NA_TIMESTAMP;
import static net.microfalx.bootstrap.search.SearchUtils.normalizeText;
import static net.microfalx.lang.ObjectUtils.isEmpty;

/**
 * A class which writes an {@link Document} into the Lucene index and reads
 * the {@link Document} from a Lucene document.
 */
class DocumentMapper {

    private static final Metrics DROPPED_FIELDS_METRICS = SearchUtils.INDEX_METRICS.withGroup("Dropped");

    private final ContentService contentService;

    public DocumentMapper(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * Writes a document into the index.
     *
     * @param document the item to index
     */
    public void write(IndexWriter indexWriter, Document document) throws IOException {
        final org.apache.lucene.document.Document ld = new org.apache.lucene.document.Document();

        ld.add(new StringField(ID_FIELD, document.getId(), Field.Store.YES));
        if (document.getName() != null)
            ld.add(new TextField(NAME_FIELD, normalizeText(document.getName(), true), Field.Store.YES));
        if (document.getType() != null) ld.add(new TextField(TYPE_FIELD, document.getType(), Field.Store.YES));
        if (document.getDescription() != null)
            ld.add(new TextField(DESCRIPTION_FIELD, normalizeText(document.getDescription(), true), Field.Store.YES));
        if (document.getBody() != null) {
            ld.add(new TextField(BODY_FIELD, normalizeBody(document), Field.Store.NO));
            if (document.getBodyUri() != null) {
                ld.add(new StringField(BODY_URI_FIELD, document.getBodyUri().toASCIIString(), Field.Store.YES));
            }
        }
        ld.add(new StringField(MIME_TYPE_FIELD, document.getMimeType(), Field.Store.YES));
        ld.add(new IntField(LENGTH_FIELD, document.getLength(), Field.Store.YES));

        if (document.getOwner() != null) ld.add(new TextField(OWNER_FIELD, document.getOwner(), Field.Store.YES));
        if (document.createdAt == NA_TIMESTAMP) document.createdAt = System.currentTimeMillis();
        ld.add(new LongPoint(CREATED_AT_FIELD, document.createdAt));
        ld.add(new SortedNumericDocValuesField(CREATED_AT_FIELD + SORTED_SUFFIX_FIELD, document.createdAt));
        ld.add(new StoredField(CREATED_AT_FIELD + STORED_SUFFIX_FIELD, document.createdAt));
        if (document.modifiedAt == NA_TIMESTAMP) document.modifiedAt = System.currentTimeMillis();
        ld.add(new LongPoint(MODIFIED_AT_FIELD, document.modifiedAt));
        ld.add(new SortedNumericDocValuesField(MODIFIED_AT_FIELD + SORTED_SUFFIX_FIELD, document.modifiedAt));
        ld.add(new StoredField(MODIFIED_AT_FIELD + STORED_SUFFIX_FIELD, document.modifiedAt));
        if (document.receivedAt > 0) {
            ld.add(new LongPoint(RECEIVED_AT_FIELD, document.receivedAt));
            ld.add(new SortedNumericDocValuesField(RECEIVED_AT_FIELD + SORTED_SUFFIX_FIELD, document.receivedAt));
            ld.add(new StoredField(RECEIVED_AT_FIELD + STORED_SUFFIX_FIELD, document.receivedAt));
        }
        if (document.sentAt > 0) {
            ld.add(new LongPoint(SENT_AT_FIELD, document.sentAt));
            ld.add(new SortedNumericDocValuesField(SENT_AT_FIELD + SORTED_SUFFIX_FIELD, document.sentAt));
            ld.add(new StoredField(SENT_AT_FIELD + STORED_SUFFIX_FIELD, document.sentAt));
        }
        if (document.getReference() != null) ld.add(new StoredField(REFERENCE_FIELD, document.getReference()));

        if (!document.getTags().isEmpty()) {
            StringBuilder tagBuilder = new StringBuilder();
            for (String tag : document.getTags()) {
                tagBuilder.append(tag).append(' ');
            }
            ld.add(new Field(TAG_FIELD, tagBuilder.toString(), TAG_TYPE));
        }

        if (document.getData() != null) {
            // TODO implement me
            byte[] serializedData = null;
            ld.add(new Field(USER_DATA_FIELD, serializedData, USER_DATA_TYPE));
        }

        for (Attribute attribute : document) {
            String name = attribute.getName();
            if (SearchUtils.isStandardFieldName(name)) {
                DROPPED_FIELDS_METRICS.count(name);
                continue;
            }
            Object value = attribute.getValue();
            if (isEmpty(value)) {
                value = StringUtils.EMPTY;
            } else if (value instanceof String && TimeUtils.seemsTemporal((String) value)) {
                DROPPED_FIELDS_METRICS.count(name);
                continue;
            }
            FieldType type = TYPES[attribute.getOptions()];
            if (type == null) {
                throw new IllegalStateException("Invalid type for attribute options: " + attribute.getOptions());
            }
            ld.add(new Field(name, normalizeText(value.toString(), true), type));
        }

        if (document.getLabelCount() > 0) {
            // TODO implement me
            byte[] labels = null;
            ld.add(new Field(LABEL_FIELD, new BytesRef(labels), LABEL_TYPE));
        }

        indexWriter.updateDocument(new Term(ID_FIELD, document.getId()), ld);
    }

    /**
     * Reads a document from a Lucene document.
     *
     * @param document the document
     * @return the item
     * @throws IOException if an I/O exception occurs
     */
    public Document read(org.apache.lucene.document.Document document) throws IOException {
        String id = document.get(ID_FIELD);
        String name = document.get(NAME_FIELD);
        Document item = Document.create(id, name);

        item.setDescription(document.get(DESCRIPTION_FIELD));
        String bodyUri = document.get(BODY_URI_FIELD);
        if (bodyUri != null) item.setBodyUri(URI.create(bodyUri));
        item.setOwner(document.get(OWNER_FIELD));
        item.setType(document.get(TYPE_FIELD));
        item.setMimeType(document.get(MIME_TYPE_FIELD));
        item.setLength(document.getField(LENGTH_FIELD).numericValue().intValue());
        item.setReference(document.get(REFERENCE_FIELD));

        IndexableField createdTime = document.getField(CREATED_AT_FIELD + STORED_SUFFIX_FIELD);
        if (createdTime != null) item.createdAt = createdTime.numericValue().longValue();
        IndexableField modifiedTime = document.getField(MODIFIED_AT_FIELD + STORED_SUFFIX_FIELD);
        if (modifiedTime != null) item.modifiedAt = modifiedTime.numericValue().longValue();
        IndexableField receivedTime = document.getField(RECEIVED_AT_FIELD + STORED_SUFFIX_FIELD);
        if (receivedTime != null) item.receivedAt = receivedTime.numericValue().longValue();
        IndexableField sentTime = document.getField(SENT_AT_FIELD + STORED_SUFFIX_FIELD);
        if (sentTime != null) item.sentAt = sentTime.numericValue().longValue();

        String tagsValue = document.get(TAG_FIELD);
        if (StringUtils.isNotEmpty(tagsValue)) {
            String[] tags = tagsValue.split(" ");
            item.addTags(tags);
        }

        IndexableField userDataField = document.getField(USER_DATA_FIELD);
        if (userDataField != null) {
            Object userData = null;
            byte[] userDataBytes = userDataField.binaryValue().bytes;
            if (userDataBytes != null) {
                userData = null;
            }
            item.setData(userData);
        }

        BytesRef labelBytes = document.getBinaryValue(LABEL_FIELD);
        if (labelBytes != null) {
            byte[] serializedLabels = labelBytes.bytes;
            item.labels = null;
        }

        for (IndexableField field : document.getFields()) {
            if (SearchUtils.isStandardFieldName(field.name())) continue;
            IndexableFieldType fieldType = field.fieldType();
            String attrName = field.name();
            String attrValue = field.stringValue();
            int options = 0;
            Attribute attribute = item.add(attrName, attrValue);
            if (fieldType.indexOptions() != IndexOptions.NONE) options |= Attribute.INDEXED_MASK;
            if (fieldType.indexOptions() == IndexOptions.DOCS_AND_FREQS_AND_POSITIONS) {
                options |= Attribute.POSITIONAL_MASK;
            }
            if (fieldType.tokenized()) options |= Attribute.TOKENIZED_MASK;
            if (fieldType.stored()) options |= Attribute.STORED_MASK;
            attribute.setOptions(options);
        }

        return item;
    }

    private String normalizeBody(Document document) throws IOException {
        return new TextExtractor(contentService, document).execute();
    }

    private static final FieldType TIME_TYPE;
    private static final FieldType TAG_TYPE;
    private static final FieldType USER_DATA_TYPE;
    private static final FieldType INDEX_TYPE;
    private static final FieldType LABEL_TYPE;
    private static final FieldType[] TYPES = new FieldType[16];

    static {
        TIME_TYPE = new FieldType();
        TIME_TYPE.setTokenized(false);
        TIME_TYPE.setStored(false);
        TIME_TYPE.setOmitNorms(true);
        TIME_TYPE.setIndexOptions(IndexOptions.NONE);
        TIME_TYPE.setDocValuesType(DocValuesType.NUMERIC);
        TIME_TYPE.freeze();

        TAG_TYPE = new FieldType();
        TAG_TYPE.setTokenized(true);
        TAG_TYPE.setStored(true);
        TAG_TYPE.setOmitNorms(true);
        TAG_TYPE.setIndexOptions(IndexOptions.DOCS);
        TAG_TYPE.freeze();

        USER_DATA_TYPE = new FieldType();
        USER_DATA_TYPE.setTokenized(false);
        USER_DATA_TYPE.setStored(true);
        USER_DATA_TYPE.setOmitNorms(true);
        USER_DATA_TYPE.setIndexOptions(IndexOptions.NONE);
        USER_DATA_TYPE.freeze();

        INDEX_TYPE = new FieldType();
        INDEX_TYPE.setTokenized(false);
        INDEX_TYPE.setStored(true);
        INDEX_TYPE.setOmitNorms(true);
        INDEX_TYPE.setIndexOptions(IndexOptions.NONE);
        INDEX_TYPE.freeze();

        LABEL_TYPE = new FieldType();
        LABEL_TYPE.setTokenized(false);
        LABEL_TYPE.setStored(true);
        LABEL_TYPE.setOmitNorms(true);
        LABEL_TYPE.setIndexOptions(IndexOptions.NONE);
        LABEL_TYPE.setDocValuesType(DocValuesType.BINARY);
        LABEL_TYPE.freeze();

        for (int index = 0; index < TYPES.length; index++) {
            FieldType type = new FieldType();
            boolean indexed = (Attribute.INDEXED_MASK & index) != 0;
            boolean tokenized = (Attribute.TOKENIZED_MASK & index) != 0;
            boolean stored = (Attribute.STORED_MASK & index) != 0;
            boolean positional = (Attribute.POSITIONAL_MASK & index) != 0;
            if (indexed) {
                type.setIndexOptions(positional ? IndexOptions.DOCS_AND_FREQS_AND_POSITIONS : IndexOptions.DOCS_AND_FREQS);
            } else {
                type.setIndexOptions(IndexOptions.NONE);
            }
            type.setTokenized(tokenized);
            type.setStored(stored);
            type.freeze();
            TYPES[index] = type;
        }
    }
}
