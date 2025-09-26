package org.perun.registrarprototype.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FormItem {
  private final int id;
  private int formId;
  private String type; // replace with enum/inheritance
  private Map<Locale, ItemTexts> texts = new HashMap<>();
  private boolean required;
  private String constraint; // regex or similar
  private String sourceIdentityAttribute;
  private String sourceIdmAttribute;
  private String destinationIdmAttribute;
  private boolean preferIdentityAttribute; // use IdM value if false, oauth claim value if true (and available)
  private String defaultValue;
  private List<Form.FormType> formTypes;

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

  public FormItem(int id, int formId, String type, Map<Locale, ItemTexts> texts, boolean required, String constraint,
                  String sourceIdentityAttribute, String sourceIdmAttribute, String destinationIdmAttribute,
                  boolean preferIdentityAttribute, List<Form.FormType> formTypes, String defaultValue) {
    this.id = id;
    this.formId = formId;
    this.type = type;
    this.texts = texts;
    this.required = required;
    this.constraint = constraint;
    this.sourceIdentityAttribute = sourceIdentityAttribute;
    this.sourceIdmAttribute = sourceIdmAttribute;
    this.destinationIdmAttribute = destinationIdmAttribute;
    this.preferIdentityAttribute = preferIdentityAttribute;
    this.formTypes = formTypes;
    this.defaultValue = defaultValue;
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

  public boolean isPreferIdentityAttribute() {
    return preferIdentityAttribute;
  }

  public void setPreferIdentityAttribute(boolean preferIdentityAttribute) {
    this.preferIdentityAttribute = preferIdentityAttribute;
  }

  public String getDestinationIdmAttribute() {
    return destinationIdmAttribute;
  }

  public void setDestinationIdmAttribute(String destinationIdmAttribute) {
    this.destinationIdmAttribute = destinationIdmAttribute;
  }

  public String getSourceIdmAttribute() {
    return sourceIdmAttribute;
  }

  public void setSourceIdmAttribute(String sourceIdmAttribute) {
    this.sourceIdmAttribute = sourceIdmAttribute;
  }

  public String getSourceIdentityAttribute() {
    return sourceIdentityAttribute;
  }

  public void setSourceIdentityAttribute(String sourceIdentityAttribute) {
    this.sourceIdentityAttribute = sourceIdentityAttribute;
  }

  public int getFormId() {
    return formId;
  }

  public List<Form.FormType> getFormTypes() {
    return formTypes;
  }

  public String getDefaultValue() {
    return defaultValue;
  }
}
