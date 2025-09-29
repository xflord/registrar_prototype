package org.perun.registrarprototype.models;

import java.util.ArrayList;
import java.util.List;

public class Form {
  private final int id;
  private int voId;
  private final int groupId;
  private final List<FormItem> items;

  public Form(int id, int groupId, List<FormItem> items) {
    this.id = id;
    this.groupId = groupId;
    this.items = items;
  }

  public Form(int id, int voId, int groupId, List<FormItem> items) {
    this.id = id;
    this.voId = voId;
    this.groupId = groupId;
    this.items = items;
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

  public int getVoId() {
    return voId;
  }

  public int getGroupId() {
    return groupId;
  }

  public List<FormItem> getItems() {
    return items;
  }

  public enum FormType {
    INITIAL,
    EXTENSION,
    CANCELLATION, // cancel membership
    UPDATE; // update attribute value
  }
}
