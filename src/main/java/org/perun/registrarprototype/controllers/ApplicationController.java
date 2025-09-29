package org.perun.registrarprototype.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.security.CurrentUserProvider;
import org.perun.registrarprototype.services.ApplicationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

  private final ApplicationService applicationService;
  private final CurrentUserProvider currentUserProvider;

  public ApplicationController(ApplicationService applicationService, CurrentUserProvider currentUserProvider) {
      this.applicationService = applicationService;
      this.currentUserProvider = currentUserProvider;
  }

//  // --- User applies for membership ---
//  @PostMapping("/apply")
//  public ResponseEntity<Application> applyForMembership(
//          @RequestParam int groupId,
//          @RequestBody List<FormItemData> itemData, HttpServletRequest request
//  ) {
//    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION); // replace with spring security/filter after done with testing
//    CurrentUser user = currentUserProvider.getCurrentUser(authHeader);
//    Application app;
//    try {
//      app = applicationService.applyForMembership(user, groupId, Form.FormType.INITIAL, itemData);
//    } catch (InvalidApplicationDataException e) {
//      // probably modify to return ValidationErrors/Result
//      throw new RuntimeException(e);
//    }
//    return ResponseEntity.ok(app);
//  }

  // --- Manager approves an application ---
  @PostMapping("/{applicationId}/approve")
  public ResponseEntity<Void> approveApplication(@PathVariable int applicationId, HttpServletRequest request) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION); // replace with spring security/filter after done with testing
    CurrentUser sess = currentUserProvider.getCurrentUser(authHeader);
    try {
      applicationService.approveApplication(sess, applicationId);
    } catch (InsufficientRightsException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok().build();
  }

  // --- Manager rejects an application ---
  @PostMapping("/{applicationId}/reject")
  public ResponseEntity<Void> rejectApplication(@PathVariable int applicationId, HttpServletRequest request) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION); // replace with spring security/filter after done with testing
    CurrentUser sess = currentUserProvider.getCurrentUser(authHeader);
    try {
      applicationService.rejectApplication(sess, applicationId);
    } catch (InsufficientRightsException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok().build();
  }

  // --- Direct register (apply + auto-approve) ---
  @PostMapping("/register")
  public ResponseEntity<Application> registerUserToGroup(
          @RequestParam int userId,
          @RequestParam int groupId,
          @RequestBody List<FormItemData> itemData
  ) {
    Application app;
    try {
      app = applicationService.registerUserToGroup(new CurrentUser(userId, null), groupId, itemData);
    } catch (InvalidApplicationDataException e) {
      // probably modify to return ValidationErrors/Result
      throw new RuntimeException(e);
    }
    return ResponseEntity.ok(app);
  }

  /**
   * Loads form for given Group/Vo. This includes prefilling form items with data from IdMs/IdPs.
   * Also sets the redirect URL based on the passed argument.
   * TODO how to get form id? Since we want multiple forms for object, name/id is not enough
   *   maybe take operation type (e.g. EXTENSION/INITIAL) as an argument as well?
   *   Also potentially move this under forms, not important for now
   */
  @GetMapping("/loadForm")
  public ResponseEntity<Application> loadForm(@RequestParam int formId, @RequestParam(required = false) String redirectUrl, HttpServletRequest request) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    CurrentUser user = currentUserProvider.getCurrentUser(authHeader);

//    Application app = applicationService.loadForm(user, formId, redirectUrl);
//    return ResponseEntity.ok(app);
    return ResponseEntity.ok().build();
  }
}