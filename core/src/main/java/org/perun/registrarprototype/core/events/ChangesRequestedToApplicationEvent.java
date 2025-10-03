package org.perun.registrarprototype.core.events;

public class ChangesRequestedToApplicationEvent extends ApplicationEvent {

  public ChangesRequestedToApplicationEvent(int applicationId, int userId, int groupId) {
    super(applicationId, userId, groupId);
  }

  @Override
  public String getEventMessage() {
    return "";
  }
}
