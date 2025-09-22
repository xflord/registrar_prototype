package org.perun.registrarprototype.models;

import java.util.Map;
import org.perun.registrarprototype.services.modules.FormModule;

/**
 * Class defining the association between a form and a form module.
 *
 */
public class AssignedFormModule {
  private final int formId;
  private final String moduleName; // save this to retrieve the module component
  private FormModule formModule;
  private Map<String, String> options;

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

  public void setFormModule(FormModule formModule) {
    this.formModule = formModule;
  }
}
