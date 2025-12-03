package org.perun.registrarprototype.persistence.jdbc.entities;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("submission_identity_attributes")
public class SubmissionIdentityAttribute {


  @Column("submission_id")
  private Integer submissionId;

  @Column("attribute_key")
  private String attributeKey;

  @Column("attribute_value")
  private String attributeValue;

  public Integer getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Integer submissionId) {
    this.submissionId = submissionId;
  }

  public String getAttributeKey() {
    return attributeKey;
  }

  public void setAttributeKey(String attributeKey) {
    this.attributeKey = attributeKey;
  }

  public String getAttributeValue() {
    return attributeValue;
  }

  public void setAttributeValue(String attributeValue) {
    this.attributeValue = attributeValue;
  }
}
