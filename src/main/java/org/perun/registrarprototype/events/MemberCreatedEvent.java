package org.perun.registrarprototype.events;

/**
 * Uses this as a marker that a new member has been created (and consequently asynchronously validated, potentially probe the success of validation?)
 */
public class MemberCreatedEvent extends Event {

  private Integer userId;
  private Integer groupId;
  private Integer memberId;

  public MemberCreatedEvent(Integer userId, Integer groupId, Integer memberId) {
    this.userId = userId;
    this.groupId = groupId;
    this.memberId = memberId;
  }

  @Override
  public String getEventMessage() {
    return "";
  }
}
