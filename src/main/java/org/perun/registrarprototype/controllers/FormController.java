package org.perun.registrarprototype.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.perun.registrarprototype.controllers.dto.AssignedFormModuleDTO;
import org.perun.registrarprototype.controllers.dto.DestinationDTO;
import org.perun.registrarprototype.controllers.dto.FormItemDTO;
import org.perun.registrarprototype.controllers.dto.FormSpecificationDTO;
import org.perun.registrarprototype.controllers.dto.FormTransitionDTO;
import org.perun.registrarprototype.controllers.dto.ItemDefinitionDTO;
import org.perun.registrarprototype.controllers.dto.ItemTextsDTO;
import org.perun.registrarprototype.controllers.dto.PrefillStrategyEntryDTO;
import org.perun.registrarprototype.controllers.dto.PrincipalInfoDTO;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Destination;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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

    List<AssignedFormModule> setModules = formService.setModules(session, formSpec, modules);
    return ResponseEntity.ok(setModules.stream()
        .map(this::toAssignedFormModuleDTO)
        .collect(Collectors.toList()));
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
    items.forEach(item -> {
      ItemDefinition itemDef = formService.getItemDefinitionById(item.getItemDefinitionId());
      validatePrefillStrategyScoping(itemDef, isSystemAdmin);
    });

    formService.updateFormItems(formId, items);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/createPrefillStrategyEntry")
  public ResponseEntity<PrefillStrategyEntryDTO> createPrefillStrategyEntry(@RequestBody PrefillStrategyEntryDTO prefillStrategyEntryDTO) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    prefillStrategyEntryDTO.setId(null);
    PrefillStrategyEntry prefillStrategyEntry = toPrefillStrategyEntry(prefillStrategyEntryDTO);

    if (!authorizationService.isAdmin(session)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    PrefillStrategyEntry created = formService.createPrefillStrategy(prefillStrategyEntry);
    return ResponseEntity.ok(toPrefillStrategyEntryDTO(created));
  }

  @DeleteMapping("/deletePrefillStrategyEntry")
  public ResponseEntity<Void> deletePrefillStrategyEntry(@RequestParam int prefillStrategyId) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    PrefillStrategyEntry prefillStrategyEntry = formService.getPrefillStrategyById(prefillStrategyId);

    if (!authorizationService.isAdmin(session)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    formService.removePrefillStrategy(prefillStrategyEntry);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/getPrefillStrategyEntries/forForm")
  public ResponseEntity<List<PrefillStrategyEntryDTO>> getPrefillStrategyEntries(@RequestParam int formId) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    FormSpecification formSpec = formService.getFormById(formId);
    if (!authorizationService.canManage(session, formSpec.getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    List<PrefillStrategyEntry> result = new ArrayList<>(formService.getPrefillStrategiesForForm(formSpec));
    if (authorizationService.isAdmin(session)) {
      result.addAll(formService.getGlobalPrefillStrategies());
    }
    return ResponseEntity.ok(result.stream()
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
    itemDefinitionDTO.setId(null);
    ItemDefinition itemDefinition = toItemDefinition(itemDefinitionDTO);
    
    if (!authorizationService.canManage(session, itemDefinition)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    validatePrefillStrategyScoping(itemDefinition, isSystemAdmin);

    ItemDefinition created = formService.createItemDefinition(itemDefinition);
    return ResponseEntity.ok(toItemDefinitionDTO(created));
  }

  @DeleteMapping("/deleteItemDefinition")
  public ResponseEntity<Void> deleteItemDefinition(@RequestParam int itemDefinitionId) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    ItemDefinition itemDefinition = formService.getItemDefinitionById(itemDefinitionId);

    if (!authorizationService.canManage(session, itemDefinition)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    formService.removeItemDefinition(itemDefinition);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/updateItemDefinition")
  public ResponseEntity<ItemDefinitionDTO> updateItemDefinition(@RequestBody ItemDefinitionDTO itemDefinitionDTO) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    ItemDefinition itemDefinition = formService.getItemDefinitionById(itemDefinitionDTO.getId());
    if (!authorizationService.canManage(session, itemDefinition)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    itemDefinition = toItemDefinition(itemDefinitionDTO);

    validatePrefillStrategyScoping(itemDefinition, authorizationService.isAdmin(session));

    return ResponseEntity.ok(toItemDefinitionDTO(formService.updateItemDefinition(itemDefinition)));
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

  @PostMapping("/createDestination")
  public ResponseEntity<DestinationDTO> createDestination(@RequestBody DestinationDTO destinationDTO) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    Destination destination = toDestination(destinationDTO);

    if (!authorizationService.isAdmin(session)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return ResponseEntity.ok(toDestinationDTO(formService.createDestination(destination)));
  }

  @DeleteMapping("/deleteDestination")
  public ResponseEntity<Void> deleteDestination(@RequestBody DestinationDTO destinationDTO) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    Destination destination = toDestination(destinationDTO);

    if (!authorizationService.isAdmin(session)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    formService.removeDestination(destination);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/getDestinations/forForm")
  public ResponseEntity<List<DestinationDTO>> getDestinations(@RequestParam int formId) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    FormSpecification formSpec = formService.getFormById(formId);

    if (!authorizationService.canManage(session, formSpec.getGroupId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return ResponseEntity.ok(formService.getDestinationsForForm(formSpec).stream()
                                 .map(this::toDestinationDTO)
                                 .toList());
  }

  @GetMapping("/getDestinations/global")
  public ResponseEntity<List<DestinationDTO>> getDestinationsGlobal() {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();
    if (!authorizationService.isAdmin(session)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return ResponseEntity.ok(formService.getGlobalDestinations().stream()
                                 .map(this::toDestinationDTO)
                                 .toList());
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
    if (itemDef.getPrefillStrategyIds() == null || itemDef.getPrefillStrategyIds().isEmpty()) {
      return;
    }
    if (itemDef.isGlobal()) {
      // Global ItemDefinitions can only use global PrefillStrategyEntries
      for (Integer strategyId : itemDef.getPrefillStrategyIds()) {
        PrefillStrategyEntry existing = formService.getPrefillStrategyById(strategyId);

        if (!existing.isGlobal()) {
          throw new IllegalArgumentException(
              "Global ItemDefinition cannot use form-specific PrefillStrategyEntry with id " + strategyId);
        }
      }
    } else {
      // Non-global ItemDefinitions: form managers can only use form-specific strategies
      // System admins can also use global strategies
      for (Integer strategyId : itemDef.getPrefillStrategyIds()) {
        PrefillStrategyEntry existing = formService.getPrefillStrategyById(strategyId);

        if (existing.isGlobal()) {
          // Only system admins can use global strategies for non-global definitions
          if (!isSystemAdmin) {
            throw new IllegalArgumentException(
                "Form managers cannot use global PrefillStrategyEntry with id " + strategyId +
                " for non-global ItemDefinition. Only system admins can do this.");
          }
        } else {
          // Form-specific strategy must belong to this form
          if (existing.getFormSpecificationId() == null ||
              !existing.getFormSpecificationId().equals(itemDef.getFormSpecificationId())) {
            throw new IllegalArgumentException(
                "PrefillStrategyEntry with id " + strategyId +
                " does not belong to FormSpecification " + itemDef.getFormSpecificationId());
          }
        }
      }
    }
  }

  private Destination toDestination(DestinationDTO dto) {
    if (dto == null) {
      return null;
    }

    int id = dto.getId() != null ? dto.getId() : 0;
    return new Destination(id, dto.getUrn(), dto.getFormSpecificationId(), dto.isGlobal());
  }

  private DestinationDTO toDestinationDTO(Destination destination) {
    if (destination == null) {
      return null;
    }

    return new DestinationDTO(destination.getId(), destination.getUrn(), destination.getFormSpecificationId(), destination.isGlobal());
  }

  // Mapper methods to convert DTOs to domain objects using constructors with validation

  private AssignedFormModule toAssignedFormModule(AssignedFormModuleDTO dto) {
    return new AssignedFormModule(dto.getModuleName(), dto.getOptions());
  }

  private PrefillStrategyEntry toPrefillStrategyEntry(PrefillStrategyEntryDTO dto) {
    // Validation: form specification must be defined if NOT global, must be null if global
    if (dto.isGlobal()) {
      if (dto.getFormSpecificationId() != null) {
        throw new IllegalArgumentException("PrefillStrategyEntry formSpecification must be null when global is true");
      }
    } else {
      if (dto.getFormSpecificationId() == null) {
        throw new IllegalArgumentException("PrefillStrategyEntry formSpecificationId cannot be null when global is false");
      }
    }
    
    int id = dto.getId() != null ? dto.getId() : 0;
    // Use constructor with validation
    return new PrefillStrategyEntry(id, dto.getType(), dto.getOptions(), dto.getSourceAttribute(), dto.getFormSpecificationId(), dto.isGlobal());
  }

  private ItemDefinition toItemDefinition(ItemDefinitionDTO dto) {
    // Validation: form specification must be defined if NOT global, must be null if global
    if (dto.isGlobal()) {
      if (dto.getFormSpecificationId() != null) {
        throw new IllegalArgumentException("ItemDefinition formSpecification must be null when global is true");
      }
    } else {
      if (dto.getFormSpecificationId() == null) {
        throw new IllegalArgumentException("ItemDefinition formSpecificationId cannot be null when global is false");
      }
    }

    // Convert PrefillStrategyEntryDTOs to PrefillStrategyEntry IDs
    List<Integer> prefillStrategyIds = null;
    if (dto.getPrefillStrategies() != null) {
      prefillStrategyIds = dto.getPrefillStrategies().stream()
          .map(PrefillStrategyEntryDTO::getId)
          .filter(Objects::nonNull)
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
        dto.getFormSpecificationId(),
        dto.getDisplayName(),
        dto.getType(),
        dto.getUpdatable(),
        dto.getRequired(),
        dto.getValidator(),
        prefillStrategyIds,
        dto.getDestination() != null ? dto.getDestination().getId() : null,
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
    
    if (dto.getId() == null) {
      throw new IllegalArgumentException("FormItem id must be negative for removed items when updating form items, cannot be null " + dto.getId());
    }

    if (dto.getItemDefinition() == null) {
      throw new IllegalArgumentException("FormItem itemDefinition cannot be null");
    }

    ItemDefinition existingDef = formService.getItemDefinitionById(dto.getItemDefinition().getId());
    if (!existingDef.isGlobal()) {
      if (existingDef.getFormSpecificationId() == null ||
              !(Objects.equals(existingDef.getFormSpecificationId(), dto.getFormId()))) {
        throw new IllegalArgumentException(
            "ItemDefinition with id " + existingDef.getId() +
                " does not belong to FormSpecification " + dto.getFormId());
      }
    }


    int id = dto.getId() != null ? dto.getId() : 0;
    int formId = dto.getFormId();
    int ordNum = dto.getOrdNum();
    
    // Fetch the FormSpecification via FormService
    FormSpecification formSpecification = formService.getFormById(formId);
    
    // Use constructor
    return new FormItem(
        id,
        formSpecification.getId(), // formSpecificationId
        dto.getShortName(),
        dto.getParentId(),
        ordNum,
        dto.getHiddenDependencyItemId(),
        dto.getDisabledDependencyItemId(),
        existingDef.getId()
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
    dto.setFormId(item.getFormSpecificationId());
    dto.setShortName(item.getShortName());
    dto.setParentId(item.getParentId());
    dto.setOrdNum(item.getOrdNum());
    dto.setHiddenDependencyItemId(item.getHiddenDependencyItemId());
    dto.setDisabledDependencyItemId(item.getDisabledDependencyItemId());
    ItemDefinition itemDef = formService.getItemDefinitionById(item.getItemDefinitionId());
    dto.setItemDefinition(toItemDefinitionDTO(itemDef));
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
        entry.getFormSpecificationId(),
        entry.isGlobal()
    );
  }

  private ItemDefinitionDTO toItemDefinitionDTO(ItemDefinition itemDef) {
    if (itemDef == null) {
      return null;
    }
    
    ItemDefinitionDTO dto = new ItemDefinitionDTO();
    dto.setId(itemDef.getId());
    dto.setFormSpecificationId(itemDef.getFormSpecificationId());
    dto.setDisplayName(itemDef.getDisplayName());
    dto.setType(itemDef.getType());
    dto.setUpdatable(itemDef.getUpdatable());
    dto.setRequired(itemDef.getRequired());
    dto.setValidator(itemDef.getValidator());
    
    // Convert PrefillStrategyEntry IDs to DTOs
    if (itemDef.getPrefillStrategyIds() != null) {
      dto.setPrefillStrategies(itemDef.getPrefillStrategyIds().stream()
          .map(formService::getPrefillStrategyById)
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

    if (itemDef.getDestinationId() != null) {
      Destination destination = formService.getDestinationById(itemDef.getDestinationId());
      dto.setDestination(toDestinationDTO(destination));
    }
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
