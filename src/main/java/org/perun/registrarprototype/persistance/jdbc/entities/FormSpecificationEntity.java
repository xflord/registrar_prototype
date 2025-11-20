package org.perun.registrarprototype.persistance.jdbc.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("form_specification")
public class FormSpecificationEntity {
  @Id
  private Integer id;

  private String voId;

  private String groupId;

  private Boolean autoApprove;

  private Boolean autoApproveExtension;

  public FormSpecificationEntity() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getVoId() {
    return voId;
  }

  public void setVoId(String voId) {
    this.voId = voId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public Boolean getAutoApprove() {
    return autoApprove;
  }

  public void setAutoApprove(Boolean autoApprove) {
    this.autoApprove = autoApprove;
  }

  public Boolean getAutoApproveExtension() {
    return autoApproveExtension;
  }

  public void setAutoApproveExtension(Boolean autoApproveExtension) {
    this.autoApproveExtension = autoApproveExtension;
  }
}
