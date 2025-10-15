package org.perun.registrarprototype.events;

public class ChangesRequestedToApplicationEvent extends ApplicationEvent {

  public ChangesRequestedToApplicationEvent(int applicationId, Integer userId, int groupId) {
    super(applicationId, userId, groupId);
  }

  @Override
  public String getEventMessage() {
    return "";
  }
}
