package org.perun.registrarprototype.controllers;

import java.util.ArrayList;
import org.perun.registrarprototype.exceptions.IdmObjectNotExistsException;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.Identity;
import org.perun.registrarprototype.models.Requirement;
import org.perun.registrarprototype.models.SubmissionContext;
import org.perun.registrarprototype.models.SubmissionResult;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import org.perun.registrarprototype.controllers.dto.ApplicationDTO;
import org.perun.registrarprototype.controllers.dto.ApplicationDetailDTO;
import org.perun.registrarprototype.controllers.dto.DecisionDTO;
import org.perun.registrarprototype.controllers.dto.SubmissionDTO;
import org.perun.registrarprototype.controllers.dto.SubmissionResultDTO;
import org.perun.registrarprototype.models.Decision;
import org.perun.registrarprototype.models.Submission;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

  private final ApplicationService applicationService;
  private final SessionProvider sessionProvider;

  public ApplicationController(ApplicationService applicationService, SessionProvider sessionProvider) {
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

  @GetMapping("/similarIdentities")
  public ResponseEntity<List<Identity>> getSimilarIdentitiesForPossibleConsolidation(@RequestBody List<FormItemData> itemData) {
    return ResponseEntity.ok(applicationService.checkForSimilarIdentities(itemData));
  }

  @GetMapping()
  public ResponseEntity<List<ApplicationDTO>> getApplications() {
    List<ApplicationDTO> dtos = applicationService.getAllApplications().stream()
        .map(this::toApplicationDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApplicationDetailDTO> getApplication(@PathVariable int id) {
    Application app = applicationService.getApplicationById(id);
    if (app == null) {
      return ResponseEntity.notFound().build();
    }

    ApplicationDetailDTO dto = toApplicationDetailDTO(app);
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/loadForms")
  public ResponseEntity<SubmissionContext> getSubmissionContext(@RequestParam int groupId)
      throws IdmObjectNotExistsException {
    return ResponseEntity.ok(applicationService.loadForms(new ArrayList<>(List.of(new Requirement(groupId, Requirement.TargetState.MEMBER))), "", false));
  }

  @PostMapping("/applyForMembership")
  public ResponseEntity<SubmissionResultDTO> applyForMembership(@RequestBody SubmissionContext context) {
    SubmissionResult result = applicationService.applyForMemberships(context);
    return ResponseEntity.ok(toSubmissionResultDTO(result));
  }

 // TODO use mappers in the future
  private ApplicationDTO toApplicationDTO(Application app) {
    return new ApplicationDTO(
        app.getId(),
        app.getForm(),
        app.getState(),
        app.getSubmission() != null ? app.getSubmission().getSubmitterName() : null,
        app.getSubmission() != null ? app.getSubmission().getId() : null,
        app.getType()
    );
  }

  private ApplicationDetailDTO toApplicationDetailDTO(Application app) {
    SubmissionDTO submissionDTO = toSubmissionDTO(app.getSubmission());
    DecisionDTO latestDecisionDTO = toDecisionDTO(applicationService.getLatestDecisionByApplicationId(app.getId()));

    return new ApplicationDetailDTO(
        app.getId(),
        app.getForm(),
        app.getState(),
        app.getSubmission() != null ? app.getSubmission().getSubmitterName() : null,
        app.getSubmission() != null ? app.getSubmission().getId() : null,
        app.getType(),
        app.getFormItemData(),
        submissionDTO,
        latestDecisionDTO
    );
  }

  private SubmissionDTO toSubmissionDTO(Submission submission) {
    if (submission == null) {
      return null;
    }
    return new SubmissionDTO(
        submission.getId(),
        submission.getTimestamp(),
        submission.getSubmitterId(),
        submission.getSubmitterName(),
        submission.getIdentityIdentifier(),
        submission.getIdentityIssuer(),
        submission.getIdentityAttributes()
    );
  }

  private DecisionDTO toDecisionDTO(Decision decision) {
    if (decision == null) {
      return null;
    }
    return new DecisionDTO(
        decision.getId(),
        decision.getApplication() != null ? decision.getApplication().getId() : null,
        decision.getApproverId(),
        decision.getApproverName(),
        decision.getMessage(),
        decision.getTimestamp(),
        decision.getDecisionType()
    );
  }

  private SubmissionResultDTO toSubmissionResultDTO(SubmissionResult result) {
    if (result == null) {
      return null;
    }
    SubmissionDTO submissionDTO = toSubmissionDTO(result.getSubmission());
    List<Integer> applicationIds = result.getSubmission() != null && result.getSubmission().getApplications() != null
        ? result.getSubmission().getApplications().stream().map(Application::getId).collect(Collectors.toList())
        : List.of();
    return new SubmissionResultDTO(
        result.getCustomMessages(),
        result.getRedirectUrl(),
        result.getRedirectForms(),
        submissionDTO,
        applicationIds
    );
  }

}