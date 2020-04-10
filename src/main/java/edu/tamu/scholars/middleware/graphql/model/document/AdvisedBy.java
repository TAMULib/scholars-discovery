package edu.tamu.scholars.middleware.graphql.model.document;

import edu.tamu.scholars.middleware.graphql.model.document.AdvisedByOrganization;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.tamu.scholars.middleware.graphql.model.AbstractNestedDocument;
import io.leangen.graphql.annotations.types.GraphQLType;
import java.lang.String;
import java.util.List;

/**
 * This file is automatically generated on compile.
 *
 * Do not modify this file -- YOUR CHANGES WILL BE ERASED!
 */
@GraphQLType(
    name = "DocumentAdvisedBy"
)
@JsonInclude(NON_EMPTY)
public class AdvisedBy extends AbstractNestedDocument {
  private static final long serialVersionUID = -1996667386L;

  private String label;

  private String email;

  private List<AdvisedByOrganization> organization;

  public AdvisedBy() {
    super();
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public List<AdvisedByOrganization> getOrganization() {
    return organization;
  }

  public void setOrganization(List<AdvisedByOrganization> organization) {
    this.organization = organization;
  }
}
