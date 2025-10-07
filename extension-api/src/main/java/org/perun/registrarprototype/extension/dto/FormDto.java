package org.perun.registrarprototype.extension.dto;

import java.util.List;

public class FormDto {
  private int id;
  private int voId;
  private int groupId;
  private List<FormItemDto> items;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getVoId() {
    return voId;
  }

  public void setVoId(int voId) {
    this.voId = voId;
  }

  public int getGroupId() {
    return groupId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  public List<FormItemDto> getItems() {
    return items;
  }

  public void setItems(List<FormItemDto> items) {
    this.items = items;
  }
}
