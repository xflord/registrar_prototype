package org.perun.registrarprototype.controllers.advice;

import org.perun.registrarprototype.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.exceptions.EntityNotExistsException;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.exceptions.InvalidApplicationStateTransitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EntityNotExistsException.class)
  public ResponseEntity<String> handleEntityNotExists(EntityNotExistsException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  @ExceptionHandler(InsufficientRightsException.class)
  public ResponseEntity<String> handleInsufficientRights(InsufficientRightsException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
  }

  @ExceptionHandler(FormItemRegexNotValid.class)
  public ResponseEntity<String> handleFormItemRegexNotValid(FormItemRegexNotValid ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }

  @ExceptionHandler(InvalidApplicationDataException.class)
  public ResponseEntity<InvalidApplicationDataException> handleInvalidApplicationData(InvalidApplicationDataException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex);
  }

  @ExceptionHandler(InvalidApplicationStateTransitionException.class)
  public ResponseEntity<String> handleInvalidApplicationStateTransition(InvalidApplicationStateTransitionException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }

  @ExceptionHandler(DataInconsistencyException.class)
  public ResponseEntity<String> handleDataInconsistency(DataInconsistencyException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }
}

