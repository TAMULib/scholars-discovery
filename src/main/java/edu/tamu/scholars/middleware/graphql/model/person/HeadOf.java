package edu.tamu.scholars.middleware.graphql.model.person;

import edu.tamu.scholars.middleware.graphql.model.person.HeadOfOrganization;

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
    name = "PersonHeadOf"
)
@JsonInclude(NON_EMPTY)
public class HeadOf extends AbstractNestedDocument {
  private static final long serialVersionUID = 203340202L;

  private String label;

  private String type;

  private HeadOfOrganization organization;

  private String startDate;

  private String endDate;

  public HeadOf() {
    super();
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public HeadOfOrganization getOrganization() {
    return organization;
  }

  public void setOrganization(HeadOfOrganization organization) {
    this.organization = organization;
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