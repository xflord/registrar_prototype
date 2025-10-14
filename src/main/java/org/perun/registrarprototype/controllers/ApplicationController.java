package org.perun.registrarprototype.controllers;

import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.SubmissionContext;
import org.perun.registrarprototype.models.SubmissionResult;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.ApplicationServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

  private final ApplicationServiceImpl applicationService;
  private final SessionProvider sessionProvider;

  public ApplicationController(ApplicationServiceImpl applicationService, SessionProvider sessionProvider) {
      this.applicationService = applicationService;
      this.sessionProvider = sessionProvider;
  }

  // --- Manager approves an application ---
  @PostMapping("/{applicationId}/approve")
  public ResponseEntity<Void> approveApplication(@PathVariable int applicationId, @RequestBody String message) {
    try {
      applicationService.approveApplication(sessionProvider.getCurrentSession(), applicationId, message);
    } catch (InsufficientRightsException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok().build();
  }

  // --- Manager rejects an application ---
  @PostMapping("/{applicationId}/reject")
  public ResponseEntity<Void> rejectApplication(@PathVariable int applicationId, @RequestBody String message) {
    try {
      applicationService.rejectApplication(sessionProvider.getCurrentSession(), applicationId, message);
    } catch (InsufficientRightsException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok().build();
  }

  @GetMapping()
  public ResponseEntity<List<Application>> getApplications() {
    return ResponseEntity.ok(applicationService.getAllApplications());
  }

  @GetMapping("/loadForms")
  public ResponseEntity<SubmissionContext> getSubmissionContext(@RequestParam int groupId) {
    return ResponseEntity.ok(applicationService.loadForms(List.of(groupId), "", false));
  }

  @PostMapping("/applyForMembership")
  public ResponseEntity<SubmissionResult> applyForMembership(@RequestBody SubmissionContext context) {
    return ResponseEntity.ok(applicationService.applyForMemberships(context));
  }

}