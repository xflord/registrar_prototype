package org.perun.registrarprototype.persistence.jdbc.entities;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("form_specification")
public class FormSpecificationEntity {
  @Id
  @Column("id")
  private Integer id;

  @Column("vo_id")
  private String voId;

  @Column("group_id")
  private String groupId;

  @Column("auto_approve")
  private Boolean autoApprove;

  @Column("auto_approve_extension")
  private Boolean autoApproveExtension;

  @MappedCollection(idColumn = "form_id", keyColumn = "ord_num")
  private List<FormItemEntity> formItemEntities;

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

  public List<FormItemEntity> getFormItemEntities() {
    return formItemEntities;
  }

  public void setFormItemEntities(
      List<FormItemEntity> formItemEntities) {
    this.formItemEntities = formItemEntities;
  }
}
