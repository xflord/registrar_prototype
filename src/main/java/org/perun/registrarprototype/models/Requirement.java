package org.perun.registrarprototype.models;

public class Requirement {
  private int groupId;
  private TargetState targetState;

  public Requirement() {}

  public Requirement(int groupId, TargetState targetState) {
    this.groupId = groupId;
    this.targetState = targetState;
  }

  public int getGroupId() {
    return groupId;
  }

  public void setGroupId(int groupId) {
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
