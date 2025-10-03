package org.perun.registrarprototype.core.models;

import java.util.List;
import org.perun.registrarprototype.extension.dto.FormType;

public class PrefilledFormData {
  private Form form;
  private int groupId;
  private List<FormItemData> prefilledItems;
  private FormType type;

  public PrefilledFormData(Form form, int groupId, List<FormItemData> prefilledItems, FormType type) {
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

  public FormType getType() {
    return type;
  }
}
