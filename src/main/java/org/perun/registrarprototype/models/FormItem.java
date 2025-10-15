package org.perun.registrarprototype.models;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FormItem {
  private int id;
  private int formId; // TODO this probably does not belong to the domain
  private Type type;
  private Map<Locale, ItemTexts> texts = new HashMap<>();
  private boolean required;
  private String constraint; // regex or similar
  private String sourceIdentityAttribute;
  private String sourceIdmAttribute;
  private String destinationIdmAttribute;
  private boolean preferIdentityAttribute; // use IdM value if false, oauth claim value if true (and available)
  private String defaultValue;
  private List<Form.FormType> formTypes = List.of(Form.FormType.INITIAL, Form.FormType.EXTENSION);
  private Condition hidden;
  private Condition disabled;
  private Integer hiddenDependencyItemId;
  private Integer disabledDependencyItemId;

  public FormItem() {}

  public FormItem(int id, Type type) {
    this.id = id;
    this.type = type;
  }

  public FormItem(int id, Type type, Map<Locale, ItemTexts> texts, boolean required, String constraint) {
    this.id = id;
    this.type = type;
    this.texts = texts;
    this.required = required;
    this.constraint = constraint;
  }

  public FormItem(int id, Type type, String label, boolean required, String constraint) {
    this.id = id;
    this.type = type;
    this.texts.put(Locale.ENGLISH, new ItemTexts(label, null, null));
    this.required = required;
    this.constraint = constraint;
  }

  public FormItem(int id, int formId, Type type, Map<Locale, ItemTexts> texts, boolean required, String constraint,
                  String sourceIdentityAttribute, String sourceIdmAttribute, String destinationIdmAttribute,
                  boolean preferIdentityAttribute, String defaultValue, List<Form.FormType> formTypes, Condition hidden,
                  Condition disabled, Integer hiddenDependencyItemId, Integer disabledDependencyItemId) {
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
    this.defaultValue = defaultValue;
    this.formTypes = formTypes;
    this.hidden = hidden;
    this.disabled = disabled;
    this.hiddenDependencyItemId = hiddenDependencyItemId;
    this.disabledDependencyItemId = disabledDependencyItemId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public ValidationError validate(FormItemData response) {
    // TODO ideally replace hardcoded strings with enums/inheritance and let GUI translate them

    if (required && (response == null || response.isEmpty())) {
        return new ValidationError(id, "Field " + getLabel() + " is required");
    }
    if (response != null && constraint != null) {
        if (!response.matches(constraint)) {
            return new ValidationError(id, "Item " + getLabel() + " must match constraint " + constraint);
        }
    }

    return null;
  }

  public boolean isRequired() {
    return required;
  }

  public String getConstraint() {
    return constraint;
  }

  public String getLabel() {
    return "defalt";
//    return texts.get(Locale.ENGLISH).getLabel();
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

  public void setFormId(int formId) {
    this.formId = formId;
  }

  public List<Form.FormType> getFormTypes() {
    return formTypes;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public Condition getDisabled() {
    return disabled;
  }

  public void setDisabled(Condition disabled) {
    this.disabled = disabled;
  }

  public Condition getHidden() {
    return hidden;
  }

  public void setHidden(Condition hidden) {
    this.hidden = hidden;
  }

  public Integer getDisabledDependencyItemId() {
    return disabledDependencyItemId;
  }

  public Integer getHiddenDependencyItemId() {
    return hiddenDependencyItemId;
  }

  public void setDisabledDependencyItemId(Integer disabledDependencyItemId) {
    this.disabledDependencyItemId = disabledDependencyItemId;
  }

  public void setHiddenDependencyItemId(Integer hiddenDependencyItemId) {
    this.hiddenDependencyItemId = hiddenDependencyItemId;
  }

  public void setFormTypes(List<Form.FormType> formTypes) {
    this.formTypes = formTypes;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void setConstraint(String constraint) {
    this.constraint = constraint;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public Map<Locale, ItemTexts> getTexts() {
    return texts;
  }

  public void setTexts(Map<Locale, ItemTexts> texts) {
    this.texts = texts;
  }

  @Override
  public String toString() {
    return "FormItem{" +
               "id=" + id +
               ", formId=" + formId +
               ", type=" + type +
               ", texts=" + texts +
               ", required=" + required +
               ", constraint='" + constraint + '\'' +
               ", sourceIdentityAttribute='" + sourceIdentityAttribute + '\'' +
               ", sourceIdmAttribute='" + sourceIdmAttribute + '\'' +
               ", destinationIdmAttribute='" + destinationIdmAttribute + '\'' +
               ", preferIdentityAttribute=" + preferIdentityAttribute +
               ", defaultValue='" + defaultValue + '\'' +
               ", formTypes=" + formTypes +
               ", hidden=" + hidden +
               ", disabled=" + disabled +
               ", hiddenDependencyItemId=" + hiddenDependencyItemId +
               ", disabledDependencyItemId=" + disabledDependencyItemId +
               '}';
  }

  public enum Type {
    LOGIN,
    VALIDATED_EMAIL,
    TEXTFIELD
  }

  public enum Condition {
    NEVER, ALWAYS, IF_PREFILLED, IF_EMPTY
  }
}
