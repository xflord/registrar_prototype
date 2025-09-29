package org.perun.registrarprototype.events;

public class ApplicationRejectedEvent extends ApplicationEvent {

  public ApplicationRejectedEvent(int applicationId, int userId, int groupId) {
    super(applicationId, userId, groupId);
  }

  @Override
  public String getEventMessage() {
    return "";
  }
}
