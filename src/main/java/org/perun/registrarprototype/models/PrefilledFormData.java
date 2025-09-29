package org.perun.registrarprototype.models;

import java.util.List;

public class PrefilledFormData {
  private Form form;
  private int groupId;
  private List<FormItemData> prefilledItems;
  private Form.FormType type;

  public PrefilledFormData(Form form, int groupId, List<FormItemData> prefilledItems, Form.FormType type) {
    this.form = form;
    this.groupId = groupId;
    this.prefilledItems = prefilledItems;
    this.type = type;
  }

  public Form getForm() {
    return form;
  }

  public int getGroupId() {
    return groupId;
  }

  public List<FormItemData> getPrefilledItems() {
    return prefilledItems;
  }

  public Form.FormType getType() {
    return type;
  }
}
