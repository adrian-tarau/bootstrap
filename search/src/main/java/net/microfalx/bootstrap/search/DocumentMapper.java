package net.microfalx.bootstrap.search;

import net.microfalx.resource.ResourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Map;

import static net.microfalx.bootstrap.search.SearchUtilities.*;

/**
 * A class which writes an {@link Document} into the Lucene index and reads
 * the {@link Document} from a Lucene document.
 */
class DocumentMapper {

    /**
     * Writes a document into the index.
     *
     * @param document the item to index
     */
    public void write(IndexWriter indexWriter, Document document) throws IOException {
        final org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();

        luceneDocument.add(new StringField(ID_FIELD, document.getId(), Field.Store.YES));
        if (document.getName() != null)
            luceneDocument.add(new TextField(NAME_FIELD, normalizeText(document.getName()), Field.Store.YES));

        if (document.getType() != null)
            luceneDocument.add(new TextField(TYPE_FIELD, document.getType(), Field.Store.YES));

        if (document.getDescription() != null)
            luceneDocument.add(new TextField(DESCRIPTION_FIELD, normalizeText(document.getDescription()), Field.Store.YES));

        if (document.getOwner() != null)
            luceneDocument.add(new TextField(OWNER_FIELD, document.getOwner(), Field.Store.YES));
        if (document.getCreatedTime() > 0)
            luceneDocument.add(new LongPoint(CREATED_TIME_FIELD, document.getCreatedTime()));
        if (document.getModifiedTime() > 0)
            luceneDocument.add(new LongPoint(MODIFIED_TIME_FIELD, document.getModifiedTime()));

        if (!document.getTags().isEmpty()) {
            StringBuilder tagBuilder = new StringBuilder();
            for (String tag : document.getTags()) {
                tagBuilder.append(tag).append(' ');
            }
            luceneDocument.add(new Field(TAG_FIELD, tagBuilder.toString(), TAG_TYPE));
        }

        if (document.getData() != null) {
            // TODO implement me
            byte[] serializedData = null;
            luceneDocument.add(new Field(USER_DATA_FIELD, serializedData, USER_DATA_TYPE));
        }

        for (Map.Entry<String, Attribute> entry : document.getAttributes().entrySet()) {
            String name = entry.getKey();
            if (SearchUtilities.isStandardFieldName(name)) {
                throw new IndexException("The item contains an attribute with a reserved name: " + name + ", item " + document);
            }
            Attribute attribute = entry.getValue();
            Object value = attribute.getValue();
            if (ResourceUtils.isEmpty(value)) value = StringUtils.EMPTY;
            FieldType type = TYPES[attribute.getOptions()];
            luceneDocument.add(new Field(name, normalizeText(value.toString()), type));
        }

        if (document.getLabelCount() > 0) {
            // TODO implement me
            byte[] labels = null;
            luceneDocument.add(new Field(LABEL_FIELD, new BytesRef(labels), LABEL_TYPE));
        }

        indexWriter.updateDocument(new Term(ID_FIELD, document.getId()), luceneDocument);
    }

    /**
     * Reads a document from a Lucene document.
     *
     * @param document the document
     * @return the item
     * @throws IOException
     */
    public Document read(org.apache.lucene.document.Document document) throws IOException {
        String id = document.get(ID_FIELD);
        String name = document.get(NAME_FIELD);
        Document item = Document.create(id, name);

        item.setDescription(document.get(DESCRIPTION_FIELD));
        item.setOwner(document.get(OWNER_FIELD));
        item.setType(document.get(TYPE_FIELD));

        IndexableField createdTime = document.getField(CREATED_TIME_FIELD);
        if (createdTime != null) {
            item.setCreatedTime(createdTime.numericValue().longValue());
        }
        IndexableField modifiedTime = document.getField(MODIFIED_TIME_FIELD);
        if (modifiedTime != null) {
            item.setModifiedTime(modifiedTime.numericValue().longValue());
        }

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
            if (SearchUtilities.isStandardFieldName(field.name())) {
                // skip default fields
                continue;
            }
            IndexableFieldType fieldType = field.fieldType();
            String attrName = field.name();
            String attrValue = field.stringValue();

            int options = 0;
            Attribute attribute = item.addAttribute(attrName, attrValue);
            if (fieldType.indexOptions() != IndexOptions.NONE) options |= Attribute.INDEXED_MASK;
            if (fieldType.tokenized()) options |= Attribute.TOKENIZED_MASK;
            if (fieldType.stored()) options |= Attribute.STORED_MASK;
            attribute.setOptions(options);
        }

        return item;
    }

    private static final FieldType TIME_TYPE;
    private static final FieldType TAG_TYPE;
    private static final FieldType USER_DATA_TYPE;
    private static final FieldType INDEX_TYPE;
    private static final FieldType LABEL_TYPE;
    private static final FieldType[] TYPES = new FieldType[9];

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

        for (int index = 0; index < 9; index++) {
            FieldType type = new FieldType();
            type.setIndexOptions((Attribute.INDEXED_MASK & index) != 0 ? IndexOptions.DOCS : IndexOptions.NONE);
            type.setTokenized((Attribute.TOKENIZED_MASK & index) != 0);
            type.setStored((Attribute.STORED_MASK & index) != 0);
            type.setOmitNorms(true);
            type.setIndexOptions(IndexOptions.DOCS);
            type.freeze();

            TYPES[index] = type;
        }
    }
}