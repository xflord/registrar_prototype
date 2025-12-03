package org.perun.registrarprototype.services.events;

/**
 * Uses this as a marker that a new member has been created (and consequently asynchronously validated, potentially probe the success of validation?)
 */
public class MemberCreatedEvent extends RegistrarEvent{

  private Integer userId;
  private Integer groupId;
  private Integer memberId;

  public MemberCreatedEvent(Integer userId, Integer groupId, Integer memberId) {
    this.userId = userId;
    this.groupId = groupId;
    this.memberId = memberId;
  }
  
  public Integer getUserId() {
    return userId;
  }
  
  public Integer getGroupId() {
    return groupId;
  }
  
  public Integer getMemberId() {
    return memberId;
  }

  @Override
  public String getEventMessage() {
    return "Member created with ID: " + memberId + " for user ID: " + userId + " in group ID: " + groupId;
  }
}
