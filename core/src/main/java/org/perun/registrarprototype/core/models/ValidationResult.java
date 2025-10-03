package org.perun.registrarprototype.core.models;

import java.util.List;

public record ValidationResult(List<ValidationError> errors) {

  public boolean isValid() {
    return errors.isEmpty();
  }
}
