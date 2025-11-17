package org.perun.registrarprototype.models;

import java.util.List;

public class FormSpecification {
  private int id;
  private String voId;
  private String groupId;
  private List<FormItem> items;
  private boolean autoApprove = false;
  private boolean autoApproveExtension = false;
//  private List<PrefillStrategyEntry> availablePrefillStrategies;
//  private List<String> availableDestinationUrns;

  public FormSpecification() {
  }

  public FormSpecification(int id, String groupId, List<FormItem> items) {
    this.id = id;
    this.groupId = groupId;
    this.items = items;
  }

  public FormSpecification(int id, String voId, String groupId, List<FormItem> items, boolean autoApprove, boolean autoApproveExtension) {
    this.id = id;
    this.voId = voId;
    this.groupId = groupId;
    this.items = items;
    this.autoApprove = autoApprove;
    this.autoApproveExtension = autoApproveExtension;
  }

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }

  public String getVoId() {
    return voId;
  }

  public String getGroupId() {
    return groupId;
  }

  public List<FormItem> getItems() {
    return items;
  }

  public void setItems(List<FormItem> items) {
    this.items = items;
  }

  public enum FormType {
    INITIAL,
    EXTENSION,
    CANCELLATION, // cancel membership
    UPDATE; // update attribute value
  }

  public void setVoId(String voId) {
    this.voId = voId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public boolean isAutoApproveExtension() {
    return autoApproveExtension;
  }

  public void setAutoApproveExtension(boolean autoApproveExtension) {
    this.autoApproveExtension = autoApproveExtension;
  }

  public boolean isAutoApprove() {
    return autoApprove;
  }

  public void setAutoApprove(boolean autoApprove) {
    this.autoApprove = autoApprove;
  }
}
