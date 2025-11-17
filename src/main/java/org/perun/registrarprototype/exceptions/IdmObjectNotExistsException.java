package org.perun.registrarprototype.exceptions;

public class IdmObjectNotExistsException extends Exception {
  private String objectId;
  public IdmObjectNotExistsException(String message, String objectId) {
    super(message);
    this.objectId = objectId;
  }

  public String getObjectId() {
    return objectId;
  }
}
