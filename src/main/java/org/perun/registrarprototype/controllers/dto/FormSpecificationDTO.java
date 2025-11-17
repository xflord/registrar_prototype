package org.perun.registrarprototype.controllers.dto;

import java.util.List;

public class FormSpecificationDTO {
  private Integer id;
  private String voId;
  private String groupId;
  private List<FormItemDTO> items;
  private boolean autoApprove;
  private boolean autoApproveExtension;

  public FormSpecificationDTO() {
  }

  public FormSpecificationDTO(Integer id, String voId, String groupId, List<FormItemDTO> items, boolean autoApprove, boolean autoApproveExtension) {
    this.id = id;
    this.voId = voId;
    this.groupId = groupId;
    this.items = items;
    this.autoApprove = autoApprove;
    this.autoApproveExtension = autoApproveExtension;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getVoId() {
    return voId;
  }

  public void setVoId(String voId) {
    this.voId = voId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public List<FormItemDTO> getItems() {
    return items;
  }

  public void setItems(List<FormItemDTO> items) {
    this.items = items;
  }

  public boolean isAutoApprove() {
    return autoApprove;
  }

  public void setAutoApprove(boolean autoApprove) {
    this.autoApprove = autoApprove;
  }

  public boolean isAutoApproveExtension() {
    return autoApproveExtension;
  }

  public void setAutoApproveExtension(boolean autoApproveExtension) {
    this.autoApproveExtension = autoApproveExtension;
  }
}

