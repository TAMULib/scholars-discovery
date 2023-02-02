package edu.tamu.scholars.middleware.discovery.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static edu.tamu.scholars.middleware.discovery.DiscoveryConstants.CORE_NAME;

import java.util.List;

import org.apache.solr.client.solrj.beans.Field;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.tamu.scholars.middleware.discovery.annotation.CollectionSource;
import edu.tamu.scholars.middleware.discovery.annotation.CollectionTarget;
import edu.tamu.scholars.middleware.discovery.annotation.NestedObject;
import edu.tamu.scholars.middleware.discovery.annotation.NestedObject.Reference;
import edu.tamu.scholars.middleware.discovery.annotation.PropertySource;
import edu.tamu.scholars.middleware.discovery.annotation.PropertyTarget;

@JsonInclude(NON_EMPTY)
@CollectionTarget(collection = CORE_NAME)
@CollectionSource(name = "collections", predicate = "http://purl.org/ontology/bibo/Collection")
public class Collection extends Common {

    @Field
    @PropertyTarget(type = "tokenized_string", copyTo = { "_text_", "name_sort" })
    @PropertySource(template = "collection/name", predicate = "http://www.w3.org/2000/01/rdf-schema#label")
    private String name;

    @Field("abstract")
    @JsonProperty("abstract")
    @PropertyTarget(type = "tokenized_string", value = "abstract", copyTo = "_text_")
    @PropertySource(template = "collection/abstract", predicate = "http://purl.org/ontology/bibo/abstract")
    private String abstractText;

    @Field
    @PropertyTarget(type = "whole_string")
    @PropertySource(template = "collection/abbreviation", predicate = "http://vivoweb.org/ontology/core#abbreviation")
    private String abbreviation;

    @Field
    @NestedObject
    @PropertyTarget(type = "nested_whole_strings", searchable = false)
    @PropertySource(template = "collection/publicationVenueFor", predicate = "http://www.w3.org/2000/01/rdf-schema#label")
    private List<String> publicationVenueFor;

    @Field
    @NestedObject
    @PropertyTarget(type = "nested_whole_strings", searchable = false)
    @PropertySource(template = "collection/editor", predicate = "http://www.w3.org/2000/01/rdf-schema#label")
    private List<String> editors;

    @Field
    @NestedObject
    @PropertyTarget(type = "nested_whole_strings", searchable = false)
    @PropertySource(template = "collection/translator", predicate = "http://www.w3.org/2000/01/rdf-schema#label")
    private List<String> translators;

    @Field
    @PropertyTarget(type = "pdate", searchable = false)
    @PropertySource(template = "collection/publicationDate", predicate = "http://vivoweb.org/ontology/core#dateTime")
    private String publicationDate;

    @Field
    @PropertyTarget(type = "nested_whole_string")
    @NestedObject(properties = { @Reference(value = "publisherType", key = "type") })
    @PropertySource(template = "collection/publisher", predicate = "http://www.w3.org/2000/01/rdf-schema#label", unique = true)
    private String publisher;

    @Field
    @PropertyTarget(type = "nested_whole_string")
    @PropertySource(template = "collection/publisherType", predicate = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType", parse = true)
    private String publisherType;

    @Field
    @NestedObject
    @PropertyTarget(type = "nested_whole_strings", searchable = false)
    @PropertySource(template = "collection/hasSubjectArea", predicate = "http://www.w3.org/2000/01/rdf-schema#label")
    private List<String> subjectAreas;

    @Field
    @NestedObject
    @PropertyTarget(type = "nested_whole_strings", searchable = false)
    @PropertySource(template = "collection/feature", predicate = "http://www.w3.org/2000/01/rdf-schema#label")
    private List<String> features;

    @Field
    @NestedObject
    @PropertyTarget(type = "nested_whole_strings", searchable = false)
    @PropertySource(template = "collection/outputOfProcessOrEvent", predicate = "http://www.w3.org/2000/01/rdf-schema#label")
    private List<String> outputOfProcessOrEvent;

    @Field
    @PropertyTarget(type = "whole_strings", copyTo = "_text_")
    @PropertySource(template = "collection/keyword", predicate = "http://vivoweb.org/ontology/core#freetextKeyword")
    private List<String> keywords;

    @Field
    @PropertyTarget(type = "whole_string")
    @PropertySource(template = "collection/issn", predicate = "http://purl.org/ontology/bibo/issn")
    private String issn;

    @Field
    @PropertyTarget(type = "whole_string")
    @PropertySource(template = "collection/eissn", predicate = "http://purl.org/ontology/bibo/eissn")
    private String eissn;

    @Field
    @PropertyTarget(type = "whole_string")
    @PropertySource(template = "collection/doi", predicate = "http://purl.org/ontology/bibo/doi")
    private String doi;

    @Field
    @PropertyTarget(type = "whole_string")
    @PropertySource(template = "collection/oclcnum", predicate = "http://purl.org/ontology/bibo/oclcnum")
    private String oclcnum;

    @Field
    @NestedObject
    @PropertyTarget(type = "nested_whole_strings", searchable = false)
    @PropertySource(template = "collection/isAbout", predicate = "http://www.w3.org/2000/01/rdf-schema#label")
    private List<String> isAbout;

    @Field
    @NestedObject
    @PropertyTarget(type = "nested_whole_strings", searchable = false)
    @PropertySource(template = "collection/specifiedOutputOf", predicate = "http://www.w3.org/2000/01/rdf-schema#label")
    private List<String> specifiedOutputOf;

    @Field
    @NestedObject
    @PropertyTarget(type = "nested_whole_strings", searchable = false)
    @PropertySource(template = "collection/mention", predicate = "http://www.w3.org/2000/01/rdf-schema#label")
    private List<String> mentions;

    @Field
    @NestedObject
    @PropertyTarget(type = "nested_whole_strings", searchable = false)
    @PropertySource(template = "collection/participatesIn", predicate = "http://www.w3.org/2000/01/rdf-schema#label")
    private List<String> participatesIn;

    @Field
    @NestedObject
    @PropertyTarget(type = "nested_whole_strings", searchable = false)
    @PropertySource(template = "collection/supportedBy", predicate = "http://www.w3.org/2000/01/rdf-schema#label")
    private List<String> supportedBy;

    public Collection() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public List<String> getPublicationVenueFor() {
        return publicationVenueFor;
    }

    public void setPublicationVenueFor(List<String> publicationVenueFor) {
        this.publicationVenueFor = publicationVenueFor;
    }

    public List<String> getEditors() {
        return editors;
    }

    public void setEditors(List<String> editors) {
        this.editors = editors;
    }

    public List<String> getTranslators() {
        return translators;
    }

    public void setTranslators(List<String> translators) {
        this.translators = translators;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublisherType() {
        return publisherType;
    }

    public void setPublisherType(String publisherType) {
        this.publisherType = publisherType;
    }

    public List<String> getSubjectAreas() {
        return subjectAreas;
    }

    public void setSubjectAreas(List<String> subjectAreas) {
        this.subjectAreas = subjectAreas;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public List<String> getOutputOfProcessOrEvent() {
        return outputOfProcessOrEvent;
    }

    public void setOutputOfProcessOrEvent(List<String> outputOfProcessOrEvent) {
        this.outputOfProcessOrEvent = outputOfProcessOrEvent;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getEissn() {
        return eissn;
    }

    public void setEissn(String eissn) {
        this.eissn = eissn;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getOclcnum() {
        return oclcnum;
    }

    public void setOclcnum(String oclcnum) {
        this.oclcnum = oclcnum;
    }

    public List<String> getIsAbout() {
        return isAbout;
    }

    public void setIsAbout(List<String> isAbout) {
        this.isAbout = isAbout;
    }

    public List<String> getSpecifiedOutputOf() {
        return specifiedOutputOf;
    }

    public void setSpecifiedOutputOf(List<String> specifiedOutputOf) {
        this.specifiedOutputOf = specifiedOutputOf;
    }

    public List<String> getMentions() {
        return mentions;
    }

    public void setMentions(List<String> mentions) {
        this.mentions = mentions;
    }

    public List<String> getParticipatesIn() {
        return participatesIn;
    }

    public void setParticipatesIn(List<String> participatesIn) {
        this.participatesIn = participatesIn;
    }

    public List<String> getSupportedBy() {
        return supportedBy;
    }

    public void setSupportedBy(List<String> supportedBy) {
        this.supportedBy = supportedBy;
    }

}
