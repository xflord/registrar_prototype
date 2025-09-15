package org.perun.registrarprototype.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FormItem {
  private final int id;
  private String type; // replace with enum/inheritance
  private Map<Locale, ItemTexts> texts = new HashMap<>();
  private boolean required;
  private String constraint; // regex or similar

  public FormItem(int id, String type) {
    this.id = id;
    this.type = type;
  }

  public FormItem(int id, String type, Map<Locale, ItemTexts> texts, boolean required, String constraint) {
    this.id = id;
    this.type = type;
    this.texts = texts;
    this.required = required;
    this.constraint = constraint;
  }

  public FormItem(int id, String type, String label, boolean required, String constraint) {
    this.id = id;
    this.type = type;
    this.texts.put(Locale.ENGLISH, new ItemTexts(label, null, null));
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
    // TODO ideally replace hardcoded strings with enums/inheritance and let GUI translate them
    List<ValidationError> errors = new ArrayList<>();
    if (required && (response == null || response.isEmpty())) {
        errors.add(new ValidationError(id, "Field " + getLabel() + " is required"));
    }
    if (response != null && constraint != null) {
        if (!response.matches(constraint)) {
            errors.add(new ValidationError(id, "Item " + getLabel() + " must match constraint " + constraint));
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
    return texts.get(Locale.ENGLISH).getLabel();
  }
}
