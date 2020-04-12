package edu.tamu.scholars.middleware.graphql.model;

import edu.tamu.scholars.middleware.graphql.model.concept.AssociatedDepartment;
import edu.tamu.scholars.middleware.graphql.model.concept.ResearchAreaOf;
import edu.tamu.scholars.middleware.graphql.model.concept.Receipt;
import edu.tamu.scholars.middleware.graphql.model.concept.BroaderConcept;
import edu.tamu.scholars.middleware.graphql.model.concept.NarrowerConcept;
import edu.tamu.scholars.middleware.graphql.model.concept.RelatedConcept;
import edu.tamu.scholars.middleware.graphql.model.common.Website;
import edu.tamu.scholars.middleware.graphql.model.common.SameAs;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.leangen.graphql.annotations.types.GraphQLType;
import java.lang.String;
import java.util.List;

/**
 * This file is automatically generated on compile.
 *
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */
@GraphQLType(
    name = "Concept"
)
@JsonInclude(NON_EMPTY)
public class Concept extends AbstractNestedDocument {
  private static final long serialVersionUID = 1522676418L;

  private List<AssociatedDepartment> associatedDepartments;

  private List<ResearchAreaOf> researchAreaOf;

  private List<Receipt> receipts;

  private List<BroaderConcept> broaderConcepts;

  private List<NarrowerConcept> narrowerConcepts;

  private List<RelatedConcept> relatedConcepts;

  private List<Website> websites;

  private List<SameAs> sameAs;

  private String name;

  private List<String> type;

  private String image;

  private String thumbnail;

  private String modTime;

  @JsonProperty("class")
  private String clazz;

  public Concept() {
    super();
  }

  public List<AssociatedDepartment> getAssociatedDepartments() {
    return associatedDepartments;
  }

  public void setAssociatedDepartments(List<AssociatedDepartment> associatedDepartments) {
    this.associatedDepartments = associatedDepartments;
  }

  public List<ResearchAreaOf> getResearchAreaOf() {
    return researchAreaOf;
  }

  public void setResearchAreaOf(List<ResearchAreaOf> researchAreaOf) {
    this.researchAreaOf = researchAreaOf;
  }

  public List<Receipt> getReceipts() {
    return receipts;
  }

  public void setReceipts(List<Receipt> receipts) {
    this.receipts = receipts;
  }

  public List<BroaderConcept> getBroaderConcepts() {
    return broaderConcepts;
  }

  public void setBroaderConcepts(List<BroaderConcept> broaderConcepts) {
    this.broaderConcepts = broaderConcepts;
  }

  public List<NarrowerConcept> getNarrowerConcepts() {
    return narrowerConcepts;
  }

  public void setNarrowerConcepts(List<NarrowerConcept> narrowerConcepts) {
    this.narrowerConcepts = narrowerConcepts;
  }

  public List<RelatedConcept> getRelatedConcepts() {
    return relatedConcepts;
  }

  public void setRelatedConcepts(List<RelatedConcept> relatedConcepts) {
    this.relatedConcepts = relatedConcepts;
  }

  public List<Website> getWebsites() {
    return websites;
  }

  public void setWebsites(List<Website> websites) {
    this.websites = websites;
  }

  public List<SameAs> getSameAs() {
    return sameAs;
  }

  public void setSameAs(List<SameAs> sameAs) {
    this.sameAs = sameAs;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getType() {
    return type;
  }

  public void setType(List<String> type) {
    this.type = type;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  public String getModTime() {
    return modTime;
  }

  public void setModTime(String modTime) {
    this.modTime = modTime;
  }

  public String getClazz() {
    return clazz;
  }

  public void setClazz(String clazz) {
    this.clazz = clazz;
  }
}
