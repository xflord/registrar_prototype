package org.perun.registrarprototype.models;

import java.util.ArrayList;
import java.util.List;

public class Form {
  private int id;
  private int voId;
  private int groupId;
  private List<FormItem> items;
  private boolean autoApprove = false;
  private boolean autoApproveExtension = false;

  public Form() {
  }

  public Form(int id, int groupId, List<FormItem> items) {
    this.id = id;
    this.groupId = groupId;
    this.items = items;
  }

  public Form(int id, int voId, int groupId, List<FormItem> items, boolean autoApprove, boolean autoApproveExtension) {
    this.id = id;
    this.voId = voId;
    this.groupId = groupId;
    this.items = items;
    this.autoApprove = autoApprove;
    this.autoApproveExtension = autoApproveExtension;
  }

  public ValidationResult validateItemData(List<FormItemData> itemData) {
    List<ValidationError> errors = new ArrayList<>();
    for (FormItem item : items) {
      FormItemData response = itemData.stream()
          .filter(r -> r.getFormItem().getId() == item.getId())
          .findFirst()
          .orElse(null);

      ValidationError validationResult = item.validate(response);
      if (validationResult != null) {
        errors.add(validationResult);
      }
    }
    return new ValidationResult(errors);
  }

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }

  public int getVoId() {
    return voId;
  }

  public int getGroupId() {
    return groupId;
  }

  public List<FormItem> getItems() {
    return items;
  }

  public void setItems(List<FormItem> items) {
    this.items = items;
  }

  public enum FormType {
    INITIAL,
    EXTENSION,
    CANCELLATION, // cancel membership
    UPDATE; // update attribute value
  }

  public void setVoId(int voId) {
    this.voId = voId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  public boolean isAutoApproveExtension() {
    return autoApproveExtension;
  }

  public void setAutoApproveExtension(boolean autoApproveExtension) {
    this.autoApproveExtension = autoApproveExtension;
  }

  public boolean isAutoApprove() {
    return autoApprove;
  }

  public void setAutoApprove(boolean autoApprove) {
    this.autoApprove = autoApprove;
  }
}
