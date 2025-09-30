package org.perun.registrarprototype.exceptions;

import java.util.List;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.ValidationError;

public class InvalidApplicationDataException extends RuntimeException {
  private final Application application;
  private final List<ValidationError> errors;

  public InvalidApplicationDataException(String message, List<ValidationError> errors, Application application) {
    super(message);
    this.errors = errors;
    this.application = application;
  }

  public List<ValidationError> getErrors() {
    return errors;
  }

  public Application getApplication() {
    return application;
  }
}
