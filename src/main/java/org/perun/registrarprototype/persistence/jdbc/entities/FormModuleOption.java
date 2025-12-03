package org.perun.registrarprototype.persistence.jdbc.entities;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("form_module_options")
public class FormModuleOption {
  @Column("assigned_form_module_id")
  private Integer assignedFormModuleId;

  @Column("option_key")
  private String optionKey;

  @Column("option_value")
  private String optionValue;

  public FormModuleOption() {
  }

  public FormModuleOption(Integer assignedFormModuleId, String optionKey, String optionValue) {
    this.assignedFormModuleId = assignedFormModuleId;
    this.optionKey = optionKey;
    this.optionValue = optionValue;
  }

  public Integer getAssignedFormModuleId() {
    return assignedFormModuleId;
  }

  public void setAssignedFormModuleId(Integer assignedFormModuleId) {
    this.assignedFormModuleId = assignedFormModuleId;
  }

  public String getOptionKey() {
    return optionKey;
  }

  public void setOptionKey(String optionKey) {
    this.optionKey = optionKey;
  }

  public String getOptionValue() {
    return optionValue;
  }

  public void setOptionValue(String optionValue) {
    this.optionValue = optionValue;
  }
}

