package org.perun.registrarprototype.models;

import java.util.ArrayList;
import java.util.List;

public class FormItem {
  private final int id;
  private String type; // replace with enum/inheritance
  private String label; // extend with localization
  private boolean required;
  private String constraint; // regex or similar

  public FormItem(int id, String type) {
    this.id = id;
    this.type = type;
  }

  public FormItem(int id, String type, String label, boolean required, String constraint) {
    this.id = id;
    this.type = type;
    this.label = label;
    this.required = required;
    this.constraint = constraint;
  }

  public int getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<ValidationError> validate(FormItemData response) {
    List<ValidationError> errors = new ArrayList<>();
    if (required && (response == null || response.isEmpty())) {
        errors.add(new ValidationError(id, "Field " + label + " is required"));
    }
    if (response != null && constraint != null) {
        if (!response.matches(constraint)) {
            errors.add(new ValidationError(id, "Item " + label + " must match constraint " + constraint));
        }
    }

    return errors;
  }

  public boolean isRequired() {
    return required;
  }

  public String getConstraint() {
    return constraint;
  }

  public String getLabel() {
    return label;
  }
}
