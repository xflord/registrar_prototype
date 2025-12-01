package org.perun.registrarprototype.events;

public class IdMUserCreatedEvent extends Event {

  private Integer userId;

  public IdMUserCreatedEvent(Integer userId) {
    this.userId = userId;
  }
  
  public Integer getUserId() {
    return userId;
  }
  
  @Override
  public String getEventMessage() {
    return "User created with ID: " + userId;
  }
}
