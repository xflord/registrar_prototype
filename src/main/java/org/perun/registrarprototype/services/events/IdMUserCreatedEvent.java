package org.perun.registrarprototype.services.events;

public class IdMUserCreatedEvent extends RegistrarEvent {

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
