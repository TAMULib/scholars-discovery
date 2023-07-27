package edu.tamu.scholars.middleware.discovery.service.component.solr;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;

import edu.tamu.scholars.middleware.discovery.annotation.FieldType;
import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.service.component.NamedTypedField;

public class SolrSchemaUtility {

    public final static String SOLR_CONSTANT_ADD_FIELD_NAME = "name";
    public final static String SOLR_CONSTANT_ADD_FIELD_TYPE = "type";
    public final static String SOLR_CONSTANT_ADD_FIELD_STORED = "stored";
    public final static String SOLR_CONSTANT_ADD_FIELD_INDEXED = "indexed";
    public final static String SOLR_CONSTANT_ADD_FIELD_REQUIRED = "required";
    public final static String SOLR_CONSTANT_ADD_FIELD_DEFAULT_VALUE = "defaultValue";
    public final static String SOLR_CONSTANT_ADD_FIELD_MULTI_VALUED = "multiValued";

    private SolrSchemaUtility() {

    }

    public static class SolrUtilityException extends RuntimeException {

    }

    /**
     * Utility method to collect NamedTypedFields from the defined class
     * AbstractIndexDocument.
     * 
     * @param type which discovery.model that is a AbstractIndexDocument
     * @return the stream of NamedTypedField found.
     */
    public static Stream<NamedTypedField> collect(Class<AbstractIndexDocument> type) {
        return FieldUtils.getFieldsListWithAnnotation(type, FieldType.class)
                .stream()
                .map(field -> {
                    FieldType fieldType = field.getAnnotation(FieldType.class);

                    String name = StringUtils.isNotEmpty(fieldType.value())
                            ? fieldType.value()
                            : field.getName();

                    NamedTypedField ntf = new NamedTypedField();
                    ntf.name = name;
                    ntf.fieldType = fieldType;
                    ntf.field = field;

                    return ntf;
                });
    }

    /**
     * Craft an add field request to Solr.
     * 
     * @param so shared object in stream which is typed in order of members of class NamedTypedFields, else types broke
     * @return SchemaRequest.AddField
     */
    public static SchemaRequest.AddField addFieldRequest(NamedTypedField ntf) {
        try {

            Map<String, Object> fieldAttributes = new HashMap<String, Object>();

            fieldAttributes.put(SOLR_CONSTANT_ADD_FIELD_NAME, ntf.name);
            fieldAttributes.put(SOLR_CONSTANT_ADD_FIELD_TYPE, ntf.fieldType.type());
            fieldAttributes.put(SOLR_CONSTANT_ADD_FIELD_STORED, ntf.fieldType.stored());
            fieldAttributes.put(SOLR_CONSTANT_ADD_FIELD_INDEXED, ntf.fieldType.searchable());
            fieldAttributes.put(SOLR_CONSTANT_ADD_FIELD_REQUIRED, ntf.fieldType.required());

            if (StringUtils.isNotEmpty(ntf.fieldType.defaultValue())) {
                fieldAttributes.put(SOLR_CONSTANT_ADD_FIELD_DEFAULT_VALUE, ntf.fieldType.defaultValue());
            }

            fieldAttributes.put(SOLR_CONSTANT_ADD_FIELD_MULTI_VALUED,
                    Collection.class.isAssignableFrom(ntf.field.getType()));

            return new SchemaRequest.AddField(fieldAttributes);
        } catch (Exception e) {
            throw new SolrSchemaUtility.SolrUtilityException();
        }
    }

    /**
     * Craft an add copy field request to Solr.
     * 
     * @param ntf NamedTypedField
     * @return SchemaRequest.AddCopyField
     */
    public static SchemaRequest.AddCopyField addCopyFieldRequest(NamedTypedField ntf) {
        try {

            return new SchemaRequest.AddCopyField(ntf.name, Arrays.asList(ntf.fieldType.copyTo()));
        } catch (Exception e) {
            throw new SolrSchemaUtility.SolrUtilityException();
        }
    }

}
