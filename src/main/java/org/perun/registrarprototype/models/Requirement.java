package org.perun.registrarprototype.models;

public class Requirement {
  private String groupId;
  private TargetState targetState;

  public Requirement() {}

  public Requirement(String groupId, TargetState targetState) {
    this.groupId = groupId;
    this.targetState = targetState;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public TargetState getTargetState() {
    return targetState;
  }

  public void setTargetState(TargetState targetState) {
    this.targetState = targetState;
  }

  public enum TargetState {
    MEMBER,
    NOT_MEMBER,
    FRESH_ATTR_VALUE
  }
}
