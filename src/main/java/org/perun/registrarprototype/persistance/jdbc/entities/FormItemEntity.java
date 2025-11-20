package org.perun.registrarprototype.persistance.jdbc.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("form_item")
public class FormItemEntity {
  @Id
  private Integer id;

  private Integer formId;

  private String shortName;

  private Integer parentId;

  private Integer ordNum;

  private Integer hiddenDependencyItemId;

  private Integer disabledDependencyItemId;

  private Integer itemDefinitionId;

  public FormItemEntity() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getFormId() {
    return formId;
  }

  public void setFormId(Integer formId) {
    this.formId = formId;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public Integer getParentId() {
    return parentId;
  }

  public void setParentId(Integer parentId) {
    this.parentId = parentId;
  }

  public Integer getOrdNum() {
    return ordNum;
  }

  public void setOrdNum(Integer ordNum) {
    this.ordNum = ordNum;
  }

  public Integer getHiddenDependencyItemId() {
    return hiddenDependencyItemId;
  }

  public void setHiddenDependencyItemId(Integer hiddenDependencyItemId) {
    this.hiddenDependencyItemId = hiddenDependencyItemId;
  }

  public Integer getDisabledDependencyItemId() {
    return disabledDependencyItemId;
  }

  public void setDisabledDependencyItemId(Integer disabledDependencyItemId) {
    this.disabledDependencyItemId = disabledDependencyItemId;
  }

  public Integer getItemDefinitionId() {
    return itemDefinitionId;
  }

  public void setItemDefinitionId(Integer itemDefinitionId) {
    this.itemDefinitionId = itemDefinitionId;
  }
}

