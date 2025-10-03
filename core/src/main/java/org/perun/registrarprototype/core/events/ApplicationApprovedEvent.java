package org.perun.registrarprototype.core.events;

public class ApplicationApprovedEvent extends ApplicationEvent {

  public ApplicationApprovedEvent(int applicationId, int userId, int groupId) {
    super(applicationId, userId, groupId);
  }

  @Override
  public String getEventMessage() {
    return "";
  }
}
