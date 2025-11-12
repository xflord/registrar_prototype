package org.perun.registrarprototype.controllers.dto;

import java.util.Map;

public class AssignedFormModuleDTO {
  private String moduleName;
  private Map<String, String> options;

  public AssignedFormModuleDTO() {}

  public AssignedFormModuleDTO(String moduleName, Map<String, String> options) {
    this.moduleName = moduleName;
    this.options = options;
  }

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }
}

