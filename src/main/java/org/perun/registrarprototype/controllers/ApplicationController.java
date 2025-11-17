package org.perun.registrarprototype.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.perun.registrarprototype.controllers.dto.ApplicationDTO;
import org.perun.registrarprototype.controllers.dto.ApplicationDetailDTO;
import org.perun.registrarprototype.controllers.dto.ApplicationFormDTO;
import org.perun.registrarprototype.controllers.dto.DecisionDTO;
import org.perun.registrarprototype.controllers.dto.FormItemDataDTO;
import org.perun.registrarprototype.controllers.dto.IdentityDTO;
import org.perun.registrarprototype.controllers.dto.SubmissionContextDTO;
import org.perun.registrarprototype.controllers.dto.SubmissionDTO;
import org.perun.registrarprototype.controllers.dto.SubmissionResultDTO;
import org.perun.registrarprototype.exceptions.IdmObjectNotExistsException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.ApplicationForm;
import org.perun.registrarprototype.models.Decision;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.Identity;
import org.perun.registrarprototype.models.Requirement;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.models.SubmissionContext;
import org.perun.registrarprototype.models.SubmissionResult;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.ApplicationService;
import org.perun.registrarprototype.services.AuthorizationService;
import org.perun.registrarprototype.services.FormService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

  private final ApplicationService applicationService;
  private final SessionProvider sessionProvider;
  private final FormService formService;
  private final AuthorizationService authorizationService;

  public ApplicationController(ApplicationService applicationService, SessionProvider sessionProvider, FormService formService, AuthorizationService authorizationService) {
      this.applicationService = applicationService;
      this.sessionProvider = sessionProvider;
      this.formService = formService;
      this.authorizationService = authorizationService;
  }

  // --- Manager approves an application ---
  @PostMapping("/{applicationId}/approve")
  public ResponseEntity<Void> approveApplication(@PathVariable int applicationId, @RequestBody String message) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    Application app = applicationService.getApplicationById(applicationId);

    // Authorization check
    if (!authorizationService.canDecide(session, app.getFormSpecification().getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    applicationService.approveApplication(session, applicationId, message);
    return ResponseEntity.ok().build();
  }

  // --- Manager rejects an application ---
  @PostMapping("/{applicationId}/reject")
  public ResponseEntity<Void> rejectApplication(@PathVariable int applicationId, @RequestBody String message) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    Application app = applicationService.getApplicationById(applicationId);

    // Authorization check
    if (!authorizationService.canDecide(session, app.getFormSpecification().getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    applicationService.rejectApplication(session, applicationId, message);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/similarIdentities")
  public ResponseEntity<List<IdentityDTO>> getSimilarIdentitiesForPossibleConsolidation(@RequestBody List<FormItemDataDTO> itemDataDTO) {
    List<FormItemData> itemData = itemDataDTO.stream()
        .map(this::toFormItemData)
        .collect(Collectors.toList());
    return ResponseEntity.ok(applicationService.checkForSimilarIdentities(itemData).stream()
        .map(this::toIdentityDTO)
        .collect(Collectors.toList()));
  }

  @GetMapping()
  public ResponseEntity<List<ApplicationDTO>> getApplications() {
    // test/admin endpoint
    List<ApplicationDTO> dtos = applicationService.getAllApplications().stream()
        .map(this::toApplicationDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApplicationDetailDTO> getApplication(@PathVariable int id) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    Application app = applicationService.getApplicationById(id);

    if (!authorizationService.canDecide(session, app.getFormSpecification().getGroupId()) &&
        !authorizationService.canManage(session, app)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    ApplicationDetailDTO dto = toApplicationDetailDTO(app);
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/loadForms")
  public ResponseEntity<SubmissionContextDTO> getSubmissionContext(@RequestParam String groupId)
      throws IdmObjectNotExistsException {
    SubmissionContext context = applicationService.loadForms(new ArrayList<>(List.of(new Requirement(groupId, Requirement.TargetState.MEMBER))), "", false);
    return ResponseEntity.ok(toSubmissionContextDTO(context));
  }

  @PostMapping("/applyForMembership")
  public ResponseEntity<SubmissionResultDTO> applyForMembership(@RequestBody SubmissionContextDTO contextDTO) {
    SubmissionContext context = toSubmissionContext(contextDTO);
    SubmissionResult result = applicationService.applyForMemberships(context);
    return ResponseEntity.ok(toSubmissionResultDTO(result));
  }

 // TODO use mappers in the future
  private ApplicationDTO toApplicationDTO(Application app) {
    return new ApplicationDTO(
        app.getId(),
        app.getFormSpecification() != null ? app.getFormSpecification().getId() : null,
        app.getState(),
        app.getSubmission() != null ? app.getSubmission().getSubmitterName() : null,
        app.getSubmission() != null ? app.getSubmission().getId() : null,
        app.getType()
    );
  }

  private ApplicationDetailDTO toApplicationDetailDTO(Application app) {
    SubmissionDTO submissionDTO = toSubmissionDTO(app.getSubmission());
    DecisionDTO latestDecisionDTO = toDecisionDTO(applicationService.getLatestDecisionByApplicationId(app.getId()));
    
    List<FormItemDataDTO> formItemDataDTO = null;
    if (app.getFormItemData() != null) {
      formItemDataDTO = app.getFormItemData().stream()
          .map(this::toFormItemDataDTO)
          .collect(Collectors.toList());
    }

    return new ApplicationDetailDTO(
        app.getId(),
        app.getFormSpecification() != null ? app.getFormSpecification().getId() : null,
        app.getState(),
        app.getSubmission() != null ? app.getSubmission().getSubmitterName() : null,
        app.getSubmission() != null ? app.getSubmission().getId() : null,
        app.getType(),
        formItemDataDTO,
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
    
    SubmissionContextDTO redirectFormsDTO = null;
    if (result.getRedirectForms() != null) {
      redirectFormsDTO = toSubmissionContextDTO(result.getRedirectForms());
    }
    
    return new SubmissionResultDTO(
        result.getCustomMessages(),
        result.getRedirectUrl(),
        redirectFormsDTO,
        submissionDTO,
        applicationIds
    );
  }

  // Mapper methods to convert DTOs to domain objects

  private FormItemData toFormItemData(FormItemDataDTO dto) {
    FormItem formItem = null;
    if (dto.getFormItemId() != null) {
      formItem = formService.getFormItemById(dto.getFormItemId());
    }
    return new FormItemData(
        formItem,
        dto.getValue(),
        dto.getPrefilledValue(),
        dto.getIdentityAttributeValue(),
        dto.getIdmAttributeValue(),
        dto.getValueAssured() != null ? dto.getValueAssured() : false
    );
  }

  private ApplicationForm toApplicationForm(ApplicationFormDTO dto) {
    org.perun.registrarprototype.models.FormSpecification formSpec = null;
    if (dto.getFormSpecificationId() != null) {
      formSpec = formService.getFormById(dto.getFormSpecificationId());
    }
    List<FormItemData> formItemData = null;
    if (dto.getFormItemData() != null) {
      formItemData = dto.getFormItemData().stream()
          .map(this::toFormItemData)
          .collect(Collectors.toList());
    }
    return new ApplicationForm(formSpec, formItemData, dto.getType());
  }

  private SubmissionContext toSubmissionContext(SubmissionContextDTO dto) {
    List<ApplicationForm> prefilledData = null;
    if (dto.getPrefilledData() != null) {
      prefilledData = dto.getPrefilledData().stream()
          .map(this::toApplicationForm)
          .collect(Collectors.toList());
    }
    return new SubmissionContext(dto.getRedirectUrl(), prefilledData);
  }

  // Mapper methods to convert domain objects to DTOs

  private FormItemDataDTO toFormItemDataDTO(FormItemData formItemData) {
    if (formItemData == null) {
      return null;
    }
    FormItemDataDTO dto = new FormItemDataDTO();
    dto.setFormItemId(formItemData.getFormItem() != null ? formItemData.getFormItem().getId() : null);
    dto.setValue(formItemData.getValue());
    dto.setPrefilledValue(formItemData.getPrefilledValue());
    dto.setIdentityAttributeValue(formItemData.getIdentityAttributeValue());
    dto.setIdmAttributeValue(formItemData.getIdmAttributeValue());
    dto.setValueAssured(formItemData.isValueAssured());
    return dto;
  }

  private ApplicationFormDTO toApplicationFormDTO(ApplicationForm applicationForm) {
    if (applicationForm == null) {
      return null;
    }
    ApplicationFormDTO dto = new ApplicationFormDTO();
    dto.setFormSpecificationId(applicationForm.getFormSpecification() != null ? applicationForm.getFormSpecification().getId() : null);
    dto.setType(applicationForm.getType());
    if (applicationForm.getFormItemData() != null) {
      dto.setFormItemData(applicationForm.getFormItemData().stream()
          .map(this::toFormItemDataDTO)
          .collect(Collectors.toList()));
    }
    return dto;
  }

  private SubmissionContextDTO toSubmissionContextDTO(SubmissionContext context) {
    if (context == null) {
      return null;
    }
    SubmissionContextDTO dto = new SubmissionContextDTO();
    dto.setRedirectUrl(context.getRedirectUrl());
    if (context.getPrefilledData() != null) {
      dto.setPrefilledData(context.getPrefilledData().stream()
          .map(this::toApplicationFormDTO)
          .collect(Collectors.toList()));
    }
    return dto;
  }

  private IdentityDTO toIdentityDTO(Identity identity) {
    if (identity == null) {
      return null;
    }
    return new IdentityDTO(
        identity.getName(),
        identity.getOrganization(),
        identity.getEmail(),
        identity.getType(),
        identity.getAttributes()
    );
  }

}