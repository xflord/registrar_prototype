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
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
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
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    FormSpecification formSpec = formService.getFormById(formId);

    // Authorization check
    if (!authorizationService.canManage(session, formSpec.getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    boolean isSystemAdmin = authorizationService.isAdmin(session);

    // Validate prefill strategy scoping
    items.forEach(item -> validatePrefillStrategyScoping(item.getItemDefinition(), isSystemAdmin));

    formService.updateFormItems(formId, items);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/createPrefillStrategyEntry")
  public ResponseEntity<PrefillStrategyEntry> createPrefillStrategyEntry(@RequestBody PrefillStrategyEntry prefillStrategyEntry) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    if (prefillStrategyEntry.isGlobal()) {
      if (!authorizationService.isAdmin(session)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
    } else {
      if (prefillStrategyEntry.getFormSpecification() == null) {
        throw new IllegalArgumentException("Form specification is null");
      }
      FormSpecification formSpec = formService.getFormById(prefillStrategyEntry.getFormSpecification().getId());
      authorizationService.canManage(session, formSpec.getGroupId());
    }
    return ResponseEntity.ok(formService.createPrefillStrategy(prefillStrategyEntry));
  }

  @GetMapping("/getPrefillStrategyEntries/forForm")
  public ResponseEntity<List<PrefillStrategyEntry>> getPrefillStrategyEntries(@RequestParam int formId) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    FormSpecification formSpec = formService.getFormById(formId);
    if (!authorizationService.canManage(session, formSpec.getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    List<PrefillStrategyEntry> prefillStrategyEntries = formService.getPrefillStrategiesForForm(formSpec);
    if (authorizationService.isAdmin(session)) {
      prefillStrategyEntries.addAll(formService.getGlobalPrefillStrategies());
    }
    return ResponseEntity.ok(prefillStrategyEntries);
  }

  @GetMapping("/getPrefillStrategyEntries/global")
  public ResponseEntity<List<PrefillStrategyEntry>> getPrefillStrategyEntriesGlobal() {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    if (!authorizationService.isAdmin(session)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return ResponseEntity.ok(formService.getGlobalPrefillStrategies());
  }

  @PostMapping("/createItemDefinition")
  public ResponseEntity<ItemDefinition> createItemDefinition(@RequestBody ItemDefinition itemDefinition) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    boolean isSystemAdmin = authorizationService.isAdmin(session);
    if (itemDefinition.isGlobal()) {
      if (!isSystemAdmin) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
    } else {
      if (itemDefinition.getFormSpecification() == null) {
        throw new IllegalArgumentException("Form specification is null");
      }
      FormSpecification formSpec = formService.getFormById(itemDefinition.getFormSpecification().getId());
      authorizationService.canManage(session, formSpec.getGroupId());
    }

    validatePrefillStrategyScoping(itemDefinition, isSystemAdmin);

    return ResponseEntity.ok(formService.createItemDefinition(itemDefinition));
  }

  @GetMapping("/getItemDefinitions/forForm")
  public ResponseEntity<List<ItemDefinition>> getItemDefinitions(@RequestParam int formId) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    FormSpecification formSpec = formService.getFormById(formId);
    if (!authorizationService.canManage(session, formSpec.getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(formService.getItemDefinitionsForForm(formSpec));
  }

  @GetMapping("/getItemDefinitions/global")
  public ResponseEntity<List<ItemDefinition>> getItemDefinitionsGlobal() {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    if (!authorizationService.isAdmin(session)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(formService.getGlobalItemDefinitions());
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

  private void validatePrefillStrategyScoping(ItemDefinition itemDef, boolean isSystemAdmin) {
    if (itemDef.getPrefillStrategies() == null || itemDef.getPrefillStrategies().isEmpty()) {
      return;
    }
    if (itemDef.isGlobal()) {
      // Global ItemDefinitions can only use global PrefillStrategyEntries
      for (PrefillStrategyEntry strategy : itemDef.getPrefillStrategies()) {
        PrefillStrategyEntry existing = formService.getPrefillStrategyById(strategy.getId());
        if (existing == null) {
          throw new IllegalArgumentException(
              "PrefillStrategyEntry with id " + strategy.getId() + " not found");
        }

        if (!existing.isGlobal()) {
          throw new IllegalArgumentException(
              "Global ItemDefinition cannot use form-specific PrefillStrategyEntry with id " + strategy.getId());
        }
      }
    } else {
      // Non-global ItemDefinitions: form managers can only use form-specific strategies
      // System admins can also use global strategies
      for (PrefillStrategyEntry strategy : itemDef.getPrefillStrategies()) {
        PrefillStrategyEntry existing = formService.getPrefillStrategyById(strategy.getId());
        if (existing == null) {
          throw new IllegalArgumentException(
              "PrefillStrategyEntry with id " + strategy.getId() + " not found");
        }

        if (existing.isGlobal()) {
          // Only system admins can use global strategies for non-global definitions
          if (!isSystemAdmin) {
            throw new IllegalArgumentException(
                "Form managers cannot use global PrefillStrategyEntry with id " + strategy.getId() +
                " for non-global ItemDefinition. Only system admins can do this.");
          }
        } else {
          // Form-specific strategy must belong to this form
          if (existing.getFormSpecification() == null ||
              !existing.getFormSpecification().equals(itemDef.getFormSpecification())) {
            throw new IllegalArgumentException(
                "PrefillStrategyEntry with id " + strategy.getId() +
                " does not belong to FormSpecification " + itemDef.getFormSpecification().getId());
          }
        }
      }
    }
  }
}
