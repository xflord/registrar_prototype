package org.perun.registrarprototype.events;

public class IdMUserCreatedEvent extends Event {

  private Integer userId;

  public IdMUserCreatedEvent(Integer userId) {
    this.userId = userId;
  }
  @Override
  public String getEventMessage() {
    return "";
  }
}
