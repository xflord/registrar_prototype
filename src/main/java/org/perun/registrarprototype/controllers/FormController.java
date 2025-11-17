package org.perun.registrarprototype.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.perun.registrarprototype.controllers.dto.AssignedFormModuleDTO;
import org.perun.registrarprototype.controllers.dto.FormItemDTO;
import org.perun.registrarprototype.controllers.dto.FormSpecificationDTO;
import org.perun.registrarprototype.controllers.dto.FormTransitionDTO;
import org.perun.registrarprototype.controllers.dto.ItemDefinitionDTO;
import org.perun.registrarprototype.controllers.dto.ItemTextsDTO;
import org.perun.registrarprototype.controllers.dto.PrefillStrategyEntryDTO;
import org.perun.registrarprototype.controllers.dto.PrincipalInfoDTO;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemTexts;
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
  public ResponseEntity<FormSpecificationDTO> createForm(@RequestParam String groupId) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    if (!authorizationService.canManage(session, groupId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    FormSpecification form = formService.createForm(groupId);
    return ResponseEntity.ok(toFormSpecificationDTO(form));
  }

  @PostMapping("/setModule")
  public ResponseEntity<List<AssignedFormModuleDTO>> setModules(@RequestParam int formId, @RequestBody List<AssignedFormModuleDTO> modulesDTO) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    FormSpecification formSpec = formService.getFormById(formId);

    // Authorization check
    if (!authorizationService.canManage(session, formSpec.getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    List<AssignedFormModule> modules = modulesDTO.stream()
        .map(this::toAssignedFormModule)
        .collect(Collectors.toList());
    try {
      List<AssignedFormModule> setModules = formService.setModules(session, formId, modules);
      return ResponseEntity.ok(setModules.stream()
          .map(this::toAssignedFormModuleDTO)
          .collect(Collectors.toList()));
    } catch (InsufficientRightsException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  /**
   * Updates form items. Existing items missing from the array will be removed, new items are expected to have negative id
   * , all ids still have to be unique though
   * @param formId
   * @param itemsDTO
   * @return
   */
  @PostMapping("/updateFormItems")
  public ResponseEntity<Void> updateFormItems(@RequestParam int formId, @RequestBody List<FormItemDTO> itemsDTO) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    FormSpecification formSpec = formService.getFormById(formId);

    // Authorization check
    if (!authorizationService.canManage(session, formSpec.getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    boolean isSystemAdmin = authorizationService.isAdmin(session);

    List<FormItem> items = itemsDTO.stream()
        .map(this::toFormItem)
        .collect(Collectors.toList());

    // Validate prefill strategy scoping
    items.forEach(item -> validatePrefillStrategyScoping(item.getItemDefinition(), isSystemAdmin));

    formService.updateFormItems(formId, items);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/createPrefillStrategyEntry")
  public ResponseEntity<PrefillStrategyEntryDTO> createPrefillStrategyEntry(@RequestBody PrefillStrategyEntryDTO prefillStrategyEntryDTO) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    PrefillStrategyEntry prefillStrategyEntry = toPrefillStrategyEntry(prefillStrategyEntryDTO);
    
    if (prefillStrategyEntry.isGlobal()) {
      if (!authorizationService.isAdmin(session)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
    } else {
      if (prefillStrategyEntry.getFormSpecification() == null) {
        throw new IllegalArgumentException("Form specification is null");
      }
      FormSpecification formSpec = formService.getFormById(prefillStrategyEntry.getFormSpecification().getId());
      // Authorization check
      if (!authorizationService.canManage(session, formSpec.getGroupId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
    }
    PrefillStrategyEntry created = formService.createPrefillStrategy(prefillStrategyEntry);
    return ResponseEntity.ok(toPrefillStrategyEntryDTO(created));
  }

  @GetMapping("/getPrefillStrategyEntries/forForm")
  public ResponseEntity<List<PrefillStrategyEntryDTO>> getPrefillStrategyEntries(@RequestParam int formId) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    FormSpecification formSpec = formService.getFormById(formId);
    if (!authorizationService.canManage(session, formSpec.getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    List<PrefillStrategyEntry> prefillStrategyEntries = formService.getPrefillStrategiesForForm(formSpec);
    if (authorizationService.isAdmin(session)) {
      prefillStrategyEntries.addAll(formService.getGlobalPrefillStrategies());
    }
    return ResponseEntity.ok(prefillStrategyEntries.stream()
        .map(this::toPrefillStrategyEntryDTO)
        .collect(Collectors.toList()));
  }

  @GetMapping("/getPrefillStrategyEntries/global")
  public ResponseEntity<List<PrefillStrategyEntryDTO>> getPrefillStrategyEntriesGlobal() {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    if (!authorizationService.isAdmin(session)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return ResponseEntity.ok(formService.getGlobalPrefillStrategies().stream()
        .map(this::toPrefillStrategyEntryDTO)
        .collect(Collectors.toList()));
  }

  @PostMapping("/createItemDefinition")
  public ResponseEntity<ItemDefinitionDTO> createItemDefinition(@RequestBody ItemDefinitionDTO itemDefinitionDTO) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    boolean isSystemAdmin = authorizationService.isAdmin(session);
    ItemDefinition itemDefinition = toItemDefinition(itemDefinitionDTO);
    
    if (itemDefinition.isGlobal()) {
      if (!isSystemAdmin) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
    } else {
      if (itemDefinition.getFormSpecification() == null) {
        throw new IllegalArgumentException("Form specification is null");
      }
      FormSpecification formSpec = formService.getFormById(itemDefinition.getFormSpecification().getId());
      // Authorization check
      if (!authorizationService.canManage(session, formSpec.getGroupId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
    }

    validatePrefillStrategyScoping(itemDefinition, isSystemAdmin);

    ItemDefinition created = formService.createItemDefinition(itemDefinition);
    return ResponseEntity.ok(toItemDefinitionDTO(created));
  }

  @GetMapping("/getItemDefinitions/forForm")
  public ResponseEntity<List<ItemDefinitionDTO>> getItemDefinitions(@RequestParam int formId) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    FormSpecification formSpec = formService.getFormById(formId);
    if (!authorizationService.canManage(session, formSpec.getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(formService.getItemDefinitionsForForm(formSpec).stream()
        .map(this::toItemDefinitionDTO)
        .collect(Collectors.toList()));
  }

  @GetMapping("/getItemDefinitions/global")
  public ResponseEntity<List<ItemDefinitionDTO>> getItemDefinitionsGlobal() {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    if (!authorizationService.isAdmin(session)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(formService.getGlobalItemDefinitions().stream()
        .map(this::toItemDefinitionDTO)
        .collect(Collectors.toList()));
  }

  @PostMapping("/addPrerequisiteForm")
  public ResponseEntity<FormTransitionDTO> addPrerequisiteForm(@RequestParam int sourceFormId,
                                                            @RequestParam int targetFormId,
                                                            @RequestBody List<Requirement.TargetState> sourceFormStates,
                                                            @RequestParam Requirement.TargetState targetState) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    FormSpecification sourceForm = formService.getFormById(sourceFormId);
    
    // Authorization check
    if (!authorizationService.canManage(session, sourceForm.getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    FormSpecification targetForm = formService.getFormById(targetFormId);

    FormTransition transition = formService.addPrerequisiteToForm(sourceForm, targetForm, sourceFormStates, targetState);
    return ResponseEntity.ok(toFormTransitionDTO(transition));
  }

  @PostMapping("/removePrerequisiteForm")
  public ResponseEntity<Void> removePrerequisiteForm(@RequestBody FormTransitionDTO transitionDTO) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    FormTransition transition = toFormTransition(transitionDTO);
    FormSpecification form = formService.getFormById(transition.getSourceFormSpecification().getId());
    
    // Authorization check
    if (!authorizationService.canManage(session, form.getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    formService.removePrerequisiteFromForm(transition);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/prerequisiteForms")
  public ResponseEntity<List<FormTransitionDTO>> getPrerequisiteForms(@RequestParam int formId) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    FormSpecification form = formService.getFormById(formId);
    
    // Authorization check
    if (!authorizationService.canManage(session, form.getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return ResponseEntity.ok(formService.getPrerequisiteTransitionsForForm(form).stream()
        .map(this::toFormTransitionDTO)
        .collect(Collectors.toList()));
  }

  @GetMapping
  public ResponseEntity<List<FormSpecificationDTO>> getForms() {
    return ResponseEntity.ok(formService.getAllFormsWithItems().stream()
        .map(this::toFormSpecificationDTO)
        .collect(Collectors.toList()));
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

  // Mapper methods to convert DTOs to domain objects using constructors with validation

  private AssignedFormModule toAssignedFormModule(AssignedFormModuleDTO dto) {
    return new AssignedFormModule(dto.getModuleName(), dto.getOptions());
  }

  private PrefillStrategyEntry toPrefillStrategyEntry(PrefillStrategyEntryDTO dto) {
    FormSpecification formSpec = null;
    if (dto.getFormSpecificationId() != null) {
      formSpec = formService.getFormById(dto.getFormSpecificationId());
    }
    
    // Validation: form specification must be defined if NOT global, must be null if global
    if (dto.isGlobal()) {
      if (formSpec != null) {
        throw new IllegalArgumentException("PrefillStrategyEntry formSpecification must be null when global is true");
      }
    } else {
      if (formSpec == null) {
        throw new IllegalArgumentException("PrefillStrategyEntry formSpecificationId cannot be null when global is false");
      }
    }
    
    int id = dto.getId() != null ? dto.getId() : 0;
    // Use constructor with validation
    return new PrefillStrategyEntry(id, dto.getType(), dto.getOptions(), dto.getSourceAttribute(), formSpec, dto.isGlobal());
  }

  private ItemDefinition toItemDefinition(ItemDefinitionDTO dto) {
    FormSpecification formSpec = null;
    if (dto.getFormSpecificationId() != null) {
      formSpec = formService.getFormById(dto.getFormSpecificationId());
    }
    
    // Validation: form specification must be defined if NOT global, must be null if global
    if (dto.isGlobal()) {
      if (formSpec != null) {
        throw new IllegalArgumentException("ItemDefinition formSpecification must be null when global is true");
      }
    } else {
      if (formSpec == null) {
        throw new IllegalArgumentException("ItemDefinition formSpecificationId cannot be null when global is false");
      }
    }

    // Convert PrefillStrategyEntryDTOs to PrefillStrategyEntry objects
    List<PrefillStrategyEntry> prefillStrategies = null;
    if (dto.getPrefillStrategies() != null) {
      prefillStrategies = dto.getPrefillStrategies().stream()
          .map(this::toPrefillStrategyEntry)
          .collect(Collectors.toList());
    }

    // Convert texts map from Map<String, ItemTextsDTO> to Map<Locale, ItemTexts>
    Map<Locale, ItemTexts> texts = null;
    if (dto.getTexts() != null) {
      texts = new HashMap<>();
      for (Map.Entry<String, ItemTextsDTO> entry : dto.getTexts().entrySet()) {
        Locale locale = Locale.forLanguageTag(entry.getKey());
        ItemTextsDTO textDTO = entry.getValue();
        if (textDTO == null) {
          throw new IllegalArgumentException("ItemDefinition texts entry for locale '" + entry.getKey() + "' cannot be null. Must contain label, help, and error keys.");
        }
        // ItemTextsDTO already has the required structure (label, help, error)
        texts.put(locale, new ItemTexts(textDTO.getLabel(), textDTO.getHelp(), textDTO.getError()));
      }
    }

    int id = dto.getId() != null ? dto.getId() : 0;
    // Use constructor with validation
    return new ItemDefinition(
        id,
        formSpec,
        dto.getDisplayName(),
        dto.getType(),
        dto.getUpdatable(),
        dto.getRequired(),
        dto.getValidator(),
        prefillStrategies,
        dto.getDestinationAttributeUrn(),
        dto.getFormTypes() != null ? dto.getFormTypes() : java.util.Set.of(FormSpecification.FormType.INITIAL, FormSpecification.FormType.EXTENSION),
        texts != null ? texts : new HashMap<>(),
        dto.getHidden() != null ? dto.getHidden() : ItemDefinition.Condition.NEVER,
        dto.getDisabled() != null ? dto.getDisabled() : ItemDefinition.Condition.NEVER,
        dto.getDefaultValue(),
        dto.isGlobal()
    );
  }

  private FormItem toFormItem(FormItemDTO dto) {
    // Validation: formId cannot be null
    if (dto.getFormId() == null) {
      throw new IllegalArgumentException("FormItem formId cannot be null");
    }
    
    // Validation: ordNum must be non-null and non-negative
    if (dto.getOrdNum() == null) {
      throw new IllegalArgumentException("FormItem ordNum cannot be null");
    }
    if (dto.getOrdNum() < 0) {
      throw new IllegalArgumentException("FormItem ordNum must be non-negative, got: " + dto.getOrdNum());
    }
    
    // Validation: id must be negative when updating form items (this is the only time it's passed currently)
    if (dto.getId() != null && dto.getId() >= 0) {
      throw new IllegalArgumentException("FormItem id must be negative when updating form items, got: " + dto.getId());
    }
    
    ItemDefinition itemDefinition = null;
    if (dto.getItemDefinition() != null) {
      itemDefinition = toItemDefinition(dto.getItemDefinition());
    }
    int id = dto.getId() != null ? dto.getId() : 0;
    int formId = dto.getFormId();
    int ordNum = dto.getOrdNum();
    // Use constructor
    return new FormItem(
        id,
        formId,
        dto.getShortName(),
        dto.getParentId(),
        ordNum,
        dto.getHiddenDependencyItemId(),
        dto.getDisabledDependencyItemId(),
        itemDefinition
    );
  }

  private FormTransition toFormTransition(FormTransitionDTO dto) {
    // Validation: type cannot be null
    if (dto.getType() == null) {
      throw new IllegalArgumentException("FormTransition type cannot be null");
    }
    
    FormSpecification sourceForm = null;
    if (dto.getSourceFormSpecificationId() != null) {
      sourceForm = formService.getFormById(dto.getSourceFormSpecificationId());
    }
    FormSpecification targetForm = null;
    if (dto.getTargetFormSpecificationId() != null) {
      targetForm = formService.getFormById(dto.getTargetFormSpecificationId());
    }
    // Use constructor
    return new FormTransition(
        sourceForm,
        targetForm,
        dto.getSourceFormStates(),
        dto.getTargetFormState(),
        dto.getType()
    );
  }

  // Mapper methods to convert domain objects to DTOs

  private FormSpecificationDTO toFormSpecificationDTO(FormSpecification form) {
    if (form == null) {
      return null;
    }
    List<FormItemDTO> itemsDTO = null;
    if (form.getItems() != null) {
      itemsDTO = form.getItems().stream()
          .map(this::toFormItemDTO)
          .collect(Collectors.toList());
    }
    return new FormSpecificationDTO(
        form.getId(),
        form.getVoId(),
        form.getGroupId(),
        itemsDTO,
        form.isAutoApprove(),
        form.isAutoApproveExtension()
    );
  }

  private FormItemDTO toFormItemDTO(FormItem item) {
    if (item == null) {
      return null;
    }
    FormItemDTO dto = new FormItemDTO();
    dto.setId(item.getId());
    dto.setFormId(item.getFormId());
    dto.setShortName(item.getShortName());
    dto.setParentId(item.getParentId());
    dto.setOrdNum(item.getOrdNum());
    dto.setHiddenDependencyItemId(item.getHiddenDependencyItemId());
    dto.setDisabledDependencyItemId(item.getDisabledDependencyItemId());
    if (item.getItemDefinition() != null) {
      dto.setItemDefinition(toItemDefinitionDTO(item.getItemDefinition()));
    }
    return dto;
  }

  private AssignedFormModuleDTO toAssignedFormModuleDTO(AssignedFormModule module) {
    if (module == null) {
      return null;
    }
    return new AssignedFormModuleDTO(
        module.getModuleName(),
        module.getOptions()
    );
  }

  private PrefillStrategyEntryDTO toPrefillStrategyEntryDTO(PrefillStrategyEntry entry) {
    if (entry == null) {
      return null;
    }
    return new PrefillStrategyEntryDTO(
        entry.getId(),
        entry.getType(),
        entry.getOptions(),
        entry.getSourceAttribute(),
        entry.getFormSpecification() != null ? entry.getFormSpecification().getId() : null,
        entry.isGlobal()
    );
  }

  private ItemDefinitionDTO toItemDefinitionDTO(ItemDefinition itemDef) {
    if (itemDef == null) {
      return null;
    }
    
    ItemDefinitionDTO dto = new ItemDefinitionDTO();
    dto.setId(itemDef.getId());
    dto.setFormSpecificationId(itemDef.getFormSpecification() != null ? itemDef.getFormSpecification().getId() : null);
    dto.setDisplayName(itemDef.getDisplayName());
    dto.setType(itemDef.getType());
    dto.setUpdatable(itemDef.getUpdatable());
    dto.setRequired(itemDef.getRequired());
    dto.setValidator(itemDef.getValidator());
    
    // Convert PrefillStrategyEntry objects to DTOs
    if (itemDef.getPrefillStrategies() != null) {
      dto.setPrefillStrategies(itemDef.getPrefillStrategies().stream()
          .map(this::toPrefillStrategyEntryDTO)
          .collect(Collectors.toList()));
    }

    // Convert texts map from Map<Locale, ItemTexts> to Map<String, ItemTextsDTO>
    if (itemDef.getTexts() != null && !itemDef.getTexts().isEmpty()) {
      Map<String, ItemTextsDTO> textsDTO = new HashMap<>();
      for (Map.Entry<Locale, ItemTexts> entry : itemDef.getTexts().entrySet()) {
        Locale locale = entry.getKey();
        ItemTexts texts = entry.getValue();
        if (texts != null) {
          textsDTO.put(locale.toLanguageTag(), new ItemTextsDTO(texts.getLabel(), texts.getHelp(), texts.getError()));
        }
      }
      dto.setTexts(textsDTO);
    }

    dto.setDestinationAttributeUrn(itemDef.getDestinationAttributeUrn());
    dto.setFormTypes(itemDef.getFormTypes());
    dto.setHidden(itemDef.getHidden());
    dto.setDisabled(itemDef.getDisabled());
    dto.setDefaultValue(itemDef.getDefaultValue());
    dto.setGlobal(itemDef.isGlobal());
    
    return dto;
  }

  private FormTransitionDTO toFormTransitionDTO(FormTransition transition) {
    if (transition == null) {
      return null;
    }
    FormTransitionDTO dto = new FormTransitionDTO();
    dto.setId(transition.getId());
    dto.setSourceFormSpecificationId(transition.getSourceFormSpecification() != null ? transition.getSourceFormSpecification().getId() : null);
    dto.setTargetFormSpecificationId(transition.getTargetFormSpecification() != null ? transition.getTargetFormSpecification().getId() : null);
    dto.setSourceFormStates(transition.getSourceFormStates());
    dto.setTargetFormState(transition.getTargetFormState());
    dto.setType(transition.getType());
    return dto;
  }
}
