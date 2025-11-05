package org.perun.registrarprototype.exceptions;

public class IdmObjectNotExistsException extends Exception {
  private Integer objectId;
  public IdmObjectNotExistsException(String message, Integer objectId) {
    super(message);
    this.objectId = objectId;
  }

  public Integer getObjectId() {
    return objectId;
  }
}
