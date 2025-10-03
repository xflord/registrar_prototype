package org.perun.registrarprototype.core.models;

import java.util.Map;
import org.perun.registrarprototype.extension.services.modules.FormModule;

/**
 * Class defining the association between a form and a form module.
 *
 */
public class AssignedFormModule {
  private final String moduleName; // save this to retrieve the module component
  private int formId;
  private FormModule formModule;
  private Map<String, String> options;

  public AssignedFormModule(String moduleName, Map<String, String> options) {
    this.moduleName = moduleName;
    this.options = options;
  }

  public AssignedFormModule(int formId, String moduleName, FormModule formModule, Map<String, String> options) {
    this.formId = formId;
    this.moduleName = moduleName;
    this.formModule = formModule;
    this.options = options;
  }

  public int getFormId() {
    return formId;
  }

  public String getModuleName() {
    return moduleName;
  }

  public FormModule getFormModule() {
    return formModule;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setFormId(int formId) {
    this.formId = formId;
  }

  public void setFormModule(FormModule formModule) {
    this.formModule = formModule;
  }
}
