package org.perun.registrarprototype.events;

public class ApplicationSubmittedEvent extends ApplicationEvent {

  public ApplicationSubmittedEvent(int applicationId, int userId, int groupId) {
    super(applicationId, userId, groupId);
  }

  @Override
  public String getEventMessage() {
    return "";
  }
}
