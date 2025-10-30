package org.perun.registrarprototype.exceptions;

public class IdmAttributeNotExistsException extends Exception {
  private String attributeUrn;

  public IdmAttributeNotExistsException(String message) {
    super(message);
  }
  public IdmAttributeNotExistsException(String message, String attributeUrn) {
    super(message);
    this.attributeUrn = attributeUrn;
  }

  public String getAttributeUrn() {
    return attributeUrn;
  }
}
