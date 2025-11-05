package org.perun.registrarprototype.controllers;

import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.controllers.dto.PrincipalInfoDTO;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.models.Requirement;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.AuthorizationService;
import org.perun.registrarprototype.services.FormService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forms")
public class FormController {

  private final FormService formService;
  private final SessionProvider sessionProvider;
  private final AuthorizationService  authorizationService;

  public FormController(FormService formService, SessionProvider sessionProvider,  AuthorizationService authorizationService) {
      this.formService = formService;
      this.sessionProvider = sessionProvider;
      this.authorizationService = authorizationService;
  }

  @PostMapping("/create")
  public ResponseEntity<FormSpecification> createForm(@RequestParam int groupId, @RequestBody List<FormItem> items) {
      try {
        formService.createForm(groupId, items);
      } catch (FormItemRegexNotValid e) {
        throw new RuntimeException(e);
      } catch (InsufficientRightsException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
      return ResponseEntity.ok().build();
  }

  // TODO probably do not mix request params and body
  @PostMapping("/setModule")
  public ResponseEntity<List<AssignedFormModule>> setModules(@RequestParam int formId, @RequestBody List<AssignedFormModule> modules) {
    RegistrarAuthenticationToken user = sessionProvider.getCurrentSession();
    List<AssignedFormModule> setModules;
    try {
      setModules = formService.setModules(user, formId, modules);
    } catch (InsufficientRightsException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(setModules);
  }

  /**
   * Updates form items. Existing items missing from the array will be removed, new items are expected to have negative id
   * , all ids still have to be unique though
   * @param formId
   * @param items
   * @return
   */
  @PostMapping("/updateFormItems")
  public ResponseEntity<Void> updateFormItems(@RequestParam int formId, @RequestBody List<FormItem> items) {
    //
    formService.updateFormItems(formId, items);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/addPrerequisiteForm")
  public ResponseEntity<FormTransition> addPrerequisiteForm(@RequestParam int sourceFormId,
                                                            @RequestParam int targetFormId,
                                                            @RequestBody List<Requirement.TargetState> sourceFormStates,
                                                            @RequestParam Requirement.TargetState targetState) {
    FormSpecification sourceForm = formService.getFormById(sourceFormId);
    authorizationService.canManage(sessionProvider.getCurrentSession(), sourceForm.getGroupId());
    FormSpecification targetForm = formService.getFormById(targetFormId);

    return ResponseEntity.ok(formService.addPrerequisiteToForm(sourceForm, targetForm, sourceFormStates, targetState));
  }

  @PostMapping("/removePrerequisiteForm")
  public ResponseEntity<Void> removePrerequisiteForm(@RequestBody FormTransition transition) {
    FormSpecification form = formService.getFormById(transition.getSourceFormSpecification().getId());
    // prolly do authorization in controllers, add ControllerAdvice that will translate the exceptions
    authorizationService.canManage(sessionProvider.getCurrentSession(), form.getGroupId());

    formService.removePrerequisiteFromForm(transition);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/prerequisiteForms")
  public ResponseEntity<List<FormTransition>> getPrerequisiteForms(@RequestParam int formId) {
    FormSpecification form = formService.getFormById(formId);
    authorizationService.canManage(sessionProvider.getCurrentSession(), form.getGroupId());

    return ResponseEntity.ok(formService.getPrerequisiteTransitionsForForm(form));
  }

  @GetMapping
  public ResponseEntity<List<FormSpecification>> getForms() {
    return ResponseEntity.ok(formService.getAllFormsWithItems());
  }

  @GetMapping("/modules")
  public ResponseEntity<Map<String, List<String>>> getModules() {
    // subject to change, currently returns Map of available modules' names as keys and required options as list of strings
    return ResponseEntity.ok(formService.getAvailableModulesWithRequiredOptions());
  }

  // Testing principal endpoint
  @GetMapping("/me")
  public ResponseEntity<PrincipalInfoDTO> me(@AuthenticationPrincipal CurrentUser principal) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();

    System.out.println(principal.getRoles());

    if (!session.isAuthenticated()) {
      return ResponseEntity.ok(new PrincipalInfoDTO(false));
    }

    return ResponseEntity.ok(new PrincipalInfoDTO(
      true,
      principal.id(),
      principal.name(),
      principal.getAttributes(),
      principal.getRoles()
    ));
  }
}
