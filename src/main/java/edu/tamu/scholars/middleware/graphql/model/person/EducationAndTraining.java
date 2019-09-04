package edu.tamu.scholars.middleware.graphql.model.person;

import edu.tamu.scholars.middleware.graphql.model.person.EducationAndTrainingOrganization;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.tamu.scholars.middleware.graphql.model.AbstractNestedDocument;
import io.leangen.graphql.annotations.types.GraphQLType;
import java.lang.String;

/**
 * This file is automatically generated on compile.
 *
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */
@GraphQLType(
    name = "PersonEducationAndTraining"
)
@JsonInclude(NON_EMPTY)
public class EducationAndTraining extends AbstractNestedDocument {
  private static final long serialVersionUID = 1693135772L;

  private String label;

  private String name;

  private String info;

  private EducationAndTrainingOrganization organization;

  private String field;

  private String abbreviation;

  private String startDate;

  private String endDate;

  public EducationAndTraining() {
    super();
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public EducationAndTrainingOrganization getOrganization() {
    return organization;
  }

  public void setOrganization(EducationAndTrainingOrganization organization) {
    this.organization = organization;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getAbbreviation() {
    return abbreviation;
  }

  public void setAbbreviation(String abbreviation) {
    this.abbreviation = abbreviation;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }
}
