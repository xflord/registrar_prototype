package org.perun.registrarprototype.persistence.jdbc.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("application")
public class ApplicationEntity {
  @Id
  @Column("id")
  private Integer id;
  @Column("idm_user_id")
  private String idmUserId;
  @Column("form_specification_id")
  private Integer formSpecificationId;
  @Column("type")
  private String type;
  @Column("state")
  private String state;
  @Column("redirect_url")
  private String redirectUrl;
  @Column("submission_id")
  private Integer submissionId;

  public ApplicationEntity() {
  }

  public ApplicationEntity(Integer id, String idmUserId, Integer formSpecificationId, String state, String redirectUrl, Integer submissionId) {
    this.id = id;
    this.idmUserId = idmUserId;
    this.formSpecificationId = formSpecificationId;
    this.state = state;
    this.redirectUrl = redirectUrl;
    this.submissionId = submissionId;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getIdmUserId() {
    return idmUserId;
  }

  public void setIdmUserId(String idmUserId) {
    this.idmUserId = idmUserId;
  }

  public Integer getFormSpecificationId() {
    return formSpecificationId;
  }

  public void setFormSpecificationId(Integer formSpecificationId) {
    this.formSpecificationId = formSpecificationId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public Integer getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Integer submissionId) {
    this.submissionId = submissionId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}