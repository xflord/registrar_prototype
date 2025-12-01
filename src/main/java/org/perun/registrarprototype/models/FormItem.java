package org.perun.registrarprototype.models;

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
  private Integer formSpecificationId; // ID of the FormSpecification this FormItem belongs to
  private String shortName;
  private Integer parentId; // null for root, ID of parent element in the form, allows for hierarchical tree structure
  private int ordNum; // TODO rework domain to work without this (position in list should be enough)
  private Integer hiddenDependencyItemId;
  private Integer disabledDependencyItemId;
  private Integer itemDefinitionId; // TODO most likely rework this into direct reference to save number of repository calls


  public FormItem() {
  }

  public FormItem(FormItem formItem) {
    if (formItem.itemDefinitionId == null) {
      throw new IllegalArgumentException("FormItem itemDefinitionId cannot be null");
    }
    this.id = formItem.id;
    this.formSpecificationId = formItem.formSpecificationId;
    this.shortName = formItem.shortName;
    this.parentId = formItem.parentId;
    this.ordNum = formItem.ordNum;
    this.hiddenDependencyItemId = formItem.hiddenDependencyItemId;
    this.disabledDependencyItemId = formItem.disabledDependencyItemId;
    this.itemDefinitionId = formItem.itemDefinitionId;
  }

  public FormItem(int id, Integer formSpecificationId, String shortName, Integer parentId, int ordNum, Integer hiddenDependencyItemId,
                  Integer disabledDependencyItemId, Integer itemDefinitionId) {
    if (itemDefinitionId == null) {
      throw new IllegalArgumentException("FormItem itemDefinitionId cannot be null");
    }
    this.id = id;
    this.formSpecificationId = formSpecificationId;
    this.shortName = shortName;
    this.parentId = parentId;
    this.ordNum = ordNum;
    this.hiddenDependencyItemId = hiddenDependencyItemId;
    this.disabledDependencyItemId = disabledDependencyItemId;
    this.itemDefinitionId = itemDefinitionId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Integer getFormSpecificationId() {
    return formSpecificationId;
  }

  public void setFormSpecificationId(Integer formSpecificationId) {
    this.formSpecificationId = formSpecificationId;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
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

  public Integer getHiddenDependencyItemId() {
    return hiddenDependencyItemId;
  }

  public void setHiddenDependencyItemId(Integer hiddenDependencyItemId) {
    this.hiddenDependencyItemId = hiddenDependencyItemId;
  }

  public Integer getDisabledDependencyItemId() {
    return disabledDependencyItemId;
  }

  public void setDisabledDependencyItemId(Integer disabledDependencyItemId) {
    this.disabledDependencyItemId = disabledDependencyItemId;
  }

  public Integer getItemDefinitionId() {
    return itemDefinitionId;
  }

  public void setItemDefinitionId(Integer itemDefinitionId) {
    if (itemDefinitionId == null) {
      throw new IllegalArgumentException("FormItem itemDefinitionId cannot be null");
    }
    this.itemDefinitionId = itemDefinitionId;
  }

  @Override
  public String toString() {
    return "FormItem{" +
               "id=" + id +
               ", formSpecificationId=" + formSpecificationId +
               ", shortName='" + shortName + '\'' +
               ", parentId=" + parentId +
               ", ordNum=" + ordNum +
               ", hiddenDependencyItemId=" + hiddenDependencyItemId +
               ", disabledDependencyItemId=" + disabledDependencyItemId +
               ", itemDefinitionId=" + itemDefinitionId +
               '}';
  }
}
