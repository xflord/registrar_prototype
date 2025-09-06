package org.perun.registrarprototype.models;

import java.util.List;

public record ValidationResult(List<ValidationError> errors) {

  public boolean isValid() {
    return errors.isEmpty();
  }
}
