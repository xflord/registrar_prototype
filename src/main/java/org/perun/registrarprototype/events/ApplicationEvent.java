package org.perun.registrarprototype.events;

public abstract class ApplicationEvent extends Event {
  protected int applicationId;
  protected int userId;
  protected int groupId;

  protected ApplicationEvent(int applicationId, int userId, int groupId) {
    this.applicationId = applicationId;
    this.userId = userId;
    this.groupId = groupId;
  }

  public int getApplicationId() {
    return applicationId;
  }

  public int getUserId() {
    return userId;
  }

  public int getGroupId() {
    return groupId;
  }

}
