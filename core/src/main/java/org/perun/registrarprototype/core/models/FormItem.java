package org.perun.registrarprototype.core.models;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.perun.registrarprototype.extension.dto.FormType;
import org.perun.registrarprototype.extension.dto.ItemCondition;
import org.perun.registrarprototype.extension.dto.ItemType;

public class FormItem {
  private int id;
  private int formId;
  private ItemType type; // replace with enum/inheritance
  private Map<Locale, ItemTexts> texts = new HashMap<>();
  private boolean required;
  private String constraint; // regex or similar
  private String sourceIdentityAttribute;
  private String sourceIdmAttribute;
  private String destinationIdmAttribute;
  private boolean preferIdentityAttribute; // use IdM value if false, oauth claim value if true (and available)
  private String defaultValue;
  private List<FormType> formTypes = List.of(FormType.INITIAL, FormType.EXTENSION);
  private ItemCondition hidden;
  private ItemCondition disabled;
  private Integer hiddenDependencyItemId;
  private Integer disabledDependencyItemId;

  public FormItem() {}

  public FormItem(int id, ItemType type) {
    this.id = id;
    this.type = type;
  }

  public FormItem(int id, ItemType type, Map<Locale, ItemTexts> texts, boolean required, String constraint) {
    this.id = id;
    this.type = type;
    this.texts = texts;
    this.required = required;
    this.constraint = constraint;
  }

  public FormItem(int id, ItemType type, String label, boolean required, String constraint) {
    this.id = id;
    this.type = type;
    this.texts.put(Locale.ENGLISH, new ItemTexts(label, null, null));
    this.required = required;
    this.constraint = constraint;
  }

  public FormItem(int id, int formId, ItemType type, Map<Locale, ItemTexts> texts, boolean required, String constraint,
                  String sourceIdentityAttribute, String sourceIdmAttribute, String destinationIdmAttribute,
                  boolean preferIdentityAttribute, List<FormType> formTypes, String defaultValue,
                  ItemCondition hidden, ItemCondition disabled) {
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
    // TODO use positive naming for these two fields (and related methods)
    this.hidden = hidden;
    this.disabled = disabled;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public ItemType getType() {
    return type;
  }

  public void setType(ItemType type) {
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

  public void setFormId(int formId) {
    this.formId = formId;
  }

  public List<FormType> getFormTypes() {
    return formTypes;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public ItemCondition getDisabled() {
    return disabled;
  }

  public void setDisabled(ItemCondition disabled) {
    this.disabled = disabled;
  }

  public ItemCondition getHidden() {
    return hidden;
  }

  public void setHidden(ItemCondition hidden) {
    this.hidden = hidden;
  }

  public Integer getDisabledDependencyItemId() {
    return disabledDependencyItemId;
  }

  public Integer getHiddenDependencyItemId() {
    return hiddenDependencyItemId;
  }


}
