package org.perun.registrarprototype.persistance.jdbc.entities;

import org.perun.registrarprototype.models.FormSpecification;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("item_definition_form_types")
public class FormTypeRef {
  @Column("item_definition_id")
  private Integer itemDefinitionId;

  @Column("form_type")
  private String formType;

  public FormTypeRef() {
  }

  public FormTypeRef(Integer itemDefinitionId, String formType) {
    this.itemDefinitionId = itemDefinitionId;
    this.formType = formType;
  }

  public Integer getItemDefinitionId() {
    return itemDefinitionId;
  }

  public void setItemDefinitionId(Integer itemDefinitionId) {
    this.itemDefinitionId = itemDefinitionId;
  }

  public FormSpecification.FormType getFormType() {
    return formType != null ? FormSpecification.FormType.valueOf(formType) : null;
  }

  public void setFormType(FormSpecification.FormType formType) {
    this.formType = formType != null ? formType.name() : null;
  }
}

