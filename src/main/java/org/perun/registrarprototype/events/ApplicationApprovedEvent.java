package org.perun.registrarprototype.events;

public class ApplicationApprovedEvent extends ApplicationEvent {

  public ApplicationApprovedEvent(int applicationId, Integer userId, int groupId) {
    super(applicationId, userId, groupId);
  }

  @Override
  public String getEventMessage() {
    return "";
  }
}
