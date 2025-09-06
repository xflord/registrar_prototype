package org.perun.registrarprototype.events;

public class ApplicationApprovedEvent {
  private final int applicationId;
  private final int userId;
  private final int groupId;

  public ApplicationApprovedEvent(int applicationId, int userId, int groupId) {
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
