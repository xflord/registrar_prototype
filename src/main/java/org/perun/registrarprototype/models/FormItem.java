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
  private int ordNum; // TODO rework domain to work without this (position in list should be enough)
  private Integer hiddenDependencyItemId;
  private Integer disabledDependencyItemId;
  private ItemDefinition itemDefinition;


  public FormItem() {
  }

  public FormItem(FormItem formItem) {
    this.id = formItem.id;
    this.formId = formItem.formId;
    this.shortName = formItem.shortName;
    this.parentId = formItem.parentId;
    this.ordNum = formItem.ordNum;
    this.hiddenDependencyItemId = formItem.hiddenDependencyItemId;
    this.disabledDependencyItemId = formItem.disabledDependencyItemId;
    this.itemDefinition = formItem.itemDefinition;
  }

  public FormItem(int id, int formId, String shortName, Integer parentId, int ordNum, Integer hiddenDependencyItemId,
                  Integer disabledDependencyItemId, ItemDefinition itemDefinition) {
    this.id = id;
    this.formId = formId;
    this.shortName = shortName;
    this.parentId = parentId;
    this.ordNum = ordNum;
    this.hiddenDependencyItemId = hiddenDependencyItemId;
    this.disabledDependencyItemId = disabledDependencyItemId;
    this.itemDefinition = itemDefinition;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getFormId() {
    return formId;
  }

  public void setFormId(int formId) {
    this.formId = formId;
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

  public ItemDefinition getItemDefinition() {
    return itemDefinition;
  }

  public void setItemDefinition(ItemDefinition itemDefinition) {
    this.itemDefinition = itemDefinition;
  }

  @Override
  public String toString() {
    return "FormItem{" +
               "id=" + id +
               ", formId=" + formId +
               ", shortName='" + shortName + '\'' +
               ", parentId=" + parentId +
               ", ordNum=" + ordNum +
               ", hiddenDependencyItemId=" + hiddenDependencyItemId +
               ", disabledDependencyItemId=" + disabledDependencyItemId +
               ", itemDefinition=" + itemDefinition +
               '}';
  }
}
