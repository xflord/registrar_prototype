package org.perun.registrarprototype.persistance.jdbc.entities;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("assigned_form_module")
public class AssignedFormModuleEntity {
  @Id
  private Integer id;

  private Integer formId;

  private String moduleName;

  private Integer position;

  @MappedCollection(idColumn = "assigned_form_module_id")
  private List<FormModuleOption> options = new ArrayList<>();

  public AssignedFormModuleEntity() {
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

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }

  public List<FormModuleOption> getOptions() {
    return options;
  }

  public void setOptions(List<FormModuleOption> options) {
    this.options = options != null ? options : new ArrayList<>();
  }
}

