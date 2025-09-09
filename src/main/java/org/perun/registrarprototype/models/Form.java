package org.perun.registrarprototype.models;

import java.util.ArrayList;
import java.util.List;

public class Form {
  private final int id;
  private final int groupId;
  private final List<FormItem> items;

  public Form(int id, int groupId, List<FormItem> items) {
    this.id = id;
    this.groupId = groupId;
    this.items = items;
  }

  public ValidationResult validateItemData(List<FormItemData> itemData) {
    List<ValidationError> errors = new ArrayList<>();
    for (FormItem item : items) {
      FormItemData response = itemData.stream()
          .filter(r -> r.getItemId() == item.getId())
          .findFirst()
          .orElse(null);

      errors.addAll(item.validate(response));
    }
    return new ValidationResult(errors);
  }

  public int getId() {
    return id;
  }

  public int getGroupId() {
    return groupId;
  }

  public List<FormItem> getItems() {
    return items;
  }
}
