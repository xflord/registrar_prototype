package org.perun.registrarprototype.core.events;

public class ApplicationSubmittedEvent extends ApplicationEvent {

  public ApplicationSubmittedEvent(int applicationId, int userId, int groupId) {
    super(applicationId, userId, groupId);
  }

  @Override
  public String getEventMessage() {
    return "";
  }
}
