package org.perun.registrarprototype.exceptions;

public class EntityNotExistsException extends RuntimeException {
  private final String entityType;
  private final Object entityId;

  public EntityNotExistsException(String entityType, Object entityId) {
    super(entityType + " with ID " + entityId + " not found");
    this.entityType = entityType;
    this.entityId = entityId;
  }

  public EntityNotExistsException(String entityType, Object entityId, String message) {
    super(message);
    this.entityType = entityType;
    this.entityId = entityId;
  }

  public String getEntityType() {
    return entityType;
  }

  public Object getEntityId() {
    return entityId;
  }
}

