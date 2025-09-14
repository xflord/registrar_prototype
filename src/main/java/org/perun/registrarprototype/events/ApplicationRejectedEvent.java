package org.perun.registrarprototype.events;

public class ApplicationRejectedEvent extends Event {
  private final int applicationId;
  private final int userId;
  private final int groupId;

  public ApplicationRejectedEvent(int applicationId, int userId, int groupId) {
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

  @Override
  public String getEventMessage() {
    return "";
  }
}
