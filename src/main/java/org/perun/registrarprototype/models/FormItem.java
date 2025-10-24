package org.perun.registrarprototype.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Represents a single form item.
 * To provide a more structured (and customizable) way of storing form items, compared to old registrar, this
 * implementation provides a way to define a "forest" structure of form items.
 * In the GUI, position same level items vertically by default, use e.g. `ROW` item type to create a row.
 * The design of the item type can then potentially be customized by the GUI for each form.
 * e.g.:
 * Form:
 *  SECTION "User Information"
 *  │     ├── ROW
 *  │     │    ├── TEXTFIELD "First Name"
 *  │     │    └── TEXTFIELD "Last Name"
 *  │     ├── ROW
 *  │     │    ├── VALIDATED_EMAIL "Email"
 *  │     │    └── DATE_PICKER "Birth Date"
 *  SECTION "Actions"
 *  │     └── SUBMIT_BUTTON "Register"
 */
public class FormItem {
  private int id;
  private int formId; // TODO this probably does not belong to the domain
  private String shortName;
  private Integer parentId; // null for root, ID of parent element in the form, allows for hierarchical tree structure
  private int ordNum;
  private Type type;
  private Map<Locale, ItemTexts> texts = new HashMap<>();
  private boolean updatable;
  private boolean required;
  private String constraint; // regex or similar
  private List<PrefillStrategyType> prefillStrategyTypes;
  private Map<String, String> prefillStrategyOptions; // options for prefill strategies (keys are prefixed with the strategy type)
  private String destinationIdmAttribute;
  private String defaultValue;
  private List<FormSpecification.FormType> formTypes = List.of(FormSpecification.FormType.INITIAL, FormSpecification.FormType.EXTENSION);
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
                  String destinationIdmAttribute, String defaultValue, List<FormSpecification.FormType> formTypes,
                  Condition hidden, Condition disabled, Integer hiddenDependencyItemId, Integer disabledDependencyItemId) {
    this.id = id;
    this.formId = formId;
    this.type = type;
    this.texts = texts;
    this.required = required;
    this.constraint = constraint;
    this.destinationIdmAttribute = destinationIdmAttribute;
    this.defaultValue = defaultValue;
    this.formTypes = formTypes;
    this.hidden = hidden;
    this.disabled = disabled;
    this.hiddenDependencyItemId = hiddenDependencyItemId;
    this.disabledDependencyItemId = disabledDependencyItemId;
  }

  public FormItem(int id, int formId, String shortName, Integer parentId, int ordNum, Type type,
                  Map<Locale, ItemTexts> texts, boolean updatable, boolean required, String constraint,
                  String destinationIdmAttribute, String defaultValue, List<FormSpecification.FormType> formTypes,
                  Condition hidden, Condition disabled, Integer hiddenDependencyItemId,
                  Integer disabledDependencyItemId) {
    this.id = id;
    this.formId = formId;
    this.shortName = shortName;
    this.parentId = parentId;
    this.ordNum = ordNum;
    this.type = type;
    this.texts = texts;
    this.updatable = updatable;
    this.required = required;
    this.constraint = constraint;
    this.destinationIdmAttribute = destinationIdmAttribute;
    this.defaultValue = defaultValue;
    this.formTypes = formTypes;
    this.hidden = hidden;
    this.disabled = disabled;
    this.hiddenDependencyItemId = hiddenDependencyItemId;
    this.disabledDependencyItemId = disabledDependencyItemId;
  }

  public boolean isUpdatable() {
    return updatable;
  }

  public void setUpdatable(boolean updatable) {
    this.updatable = updatable;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
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

  public List<PrefillStrategyType> getPrefillStrategyTypes() {
    return prefillStrategyTypes;
  }

  public Map<String, String> getPrefillStrategyOptions() {
    return prefillStrategyOptions;
  }

  public void setPrefillStrategyOptions(Map<String, String> prefillStrategyOptions) {
    this.prefillStrategyOptions = prefillStrategyOptions;
  }

  public void setPrefillStrategyTypes(
      List<PrefillStrategyType> prefillStrategyTypes) {
    this.prefillStrategyTypes = prefillStrategyTypes;
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
  public String getDestinationIdmAttribute() {
    return destinationIdmAttribute;
  }

  public void setDestinationIdmAttribute(String destinationIdmAttribute) {
    this.destinationIdmAttribute = destinationIdmAttribute;
  }

  public int getFormId() {
    return formId;
  }

  public void setFormId(int formId) {
    this.formId = formId;
  }

  public List<FormSpecification.FormType> getFormTypes() {
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

  public void setFormTypes(List<FormSpecification.FormType> formTypes) {
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

  public Integer getParentId() {
    return parentId;
  }

  public void setParentId(Integer parentId) {
    this.parentId = parentId;
  }

  public int getOrdNum() {
    return ordNum;
  }

  public void setOrdNum(int ordNum) {
    this.ordNum = ordNum;
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
               ", destinationIdmAttribute='" + destinationIdmAttribute + '\'' +
               ", defaultValue='" + defaultValue + '\'' +
               ", formTypes=" + formTypes +
               ", hidden=" + hidden +
               ", disabled=" + disabled +
               ", hiddenDependencyItemId=" + hiddenDependencyItemId +
               ", disabledDependencyItemId=" + disabledDependencyItemId +
               '}';
  }

  public enum Type {
    ROW,
    SECTION,
    SUBMIT_BUTTON,
    DATE_PICKER,
    LOGIN,
    PASSWORD,
    VERIFIED_EMAIL,
    HTML_COMMENT,
    TEXTFIELD;

    public static final Set<Type> HTML_ITEMS = Set.of(HTML_COMMENT);
    public static final Set<Type> UPDATABLE_ITEMS = Set.of(TEXTFIELD, DATE_PICKER);
    public static final Set<Type> VERIFIED_ITEMS = Set.of(VERIFIED_EMAIL);

    public boolean isUpdatable() {
      return UPDATABLE_ITEMS.contains(this);
    }

    public boolean isHtmlItem() {
      return HTML_ITEMS.contains(this);
    }

    public boolean isVerifiedItem() {
      return VERIFIED_ITEMS.contains(this);
    }
  }

  public enum Condition {
    NEVER, ALWAYS, IF_PREFILLED, IF_EMPTY
  }

  public enum PrefillStrategyType {
    IDENTITY_ATTRIBUTE, IDM_ATTRIBUTE, LOGIN_ATTRIBUTE, APPLICATION
  }
}
