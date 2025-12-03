package org.perun.registrarprototype.persistence.jdbc.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("form_item")
public class FormItemEntity {
  @Id
  @Column("id")
  private Integer id;

  @Column("form_id")
  private Integer formId;

  @Column("short_name")
  private String shortName;

  @Column("parent_id")
  private Integer parentId;

  @Column("ord_num")
  private Integer ordNum;

  @Column("hidden_dependency_item_id")
  private Integer hiddenDependencyItemId;

  @Column("disabled_dependency_item_id")
  private Integer disabledDependencyItemId;

  @Column("item_definition_id")
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

  @Override
  public String toString() {
    return "FormItemEntity{" +
               "id=" + id +
               ", formId=" + formId +
               ", shortName='" + shortName + '\'' +
               ", parentId=" + parentId +
               ", ordNum=" + ordNum +
               ", hiddenDependencyItemId=" + hiddenDependencyItemId +
               ", disabledDependencyItemId=" + disabledDependencyItemId +
               ", itemDefinitionId=" + itemDefinitionId +
               '}';
  }
}

