package org.perun.registrarprototype.services;

import io.micrometer.common.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.perun.registrarprototype.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.exceptions.EntityNotExistsException;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.exceptions.FormModuleNotExistsException;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.models.Requirement;
import org.perun.registrarprototype.repositories.DestinationRepository;
import org.perun.registrarprototype.repositories.FormItemRepository;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.FormModuleRepository;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.FormTransitionRepository;
import org.perun.registrarprototype.repositories.ItemDefinitionRepository;
import org.perun.registrarprototype.repositories.PrefillStrategyEntryRepository;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.modules.FormModule;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class FormServiceImpl implements FormService {
  private final FormRepository formRepository;
  private final AuthorizationService authorizationService;
  private final FormModuleRepository formModuleRepository;
  private final FormItemRepository formItemRepository;
  private final FormTransitionRepository formTransitionRepository;
  private final ApplicationContext context;
  private final ApplicationRepository applicationRepository;
  private final SessionProvider sessionProvider;
  private final ItemDefinitionRepository itemDefinitionRepository;
  private final PrefillStrategyEntryRepository prefillStrategyEntryRepository;
  private final DestinationRepository destinationRepository;

  public FormServiceImpl(FormRepository formRepository, AuthorizationService authorizationService,
                         FormModuleRepository formModuleRepository, ApplicationContext context,
                         FormItemRepository formItemRepository, FormTransitionRepository formTransitionRepository,
                         ApplicationRepository applicationRepository, SessionProvider sessionProvider,
                         ItemDefinitionRepository itemDefinitionRepository,
                         PrefillStrategyEntryRepository prefillStrategyEntryRepository,
                         DestinationRepository destinationRepository) {
    this.formRepository = formRepository;
    this.authorizationService = authorizationService;
    this.formModuleRepository = formModuleRepository;
    this.context = context;
    this.formItemRepository = formItemRepository;
    this.formTransitionRepository = formTransitionRepository;
    this.applicationRepository = applicationRepository;
    this.sessionProvider = sessionProvider;
    this.itemDefinitionRepository = itemDefinitionRepository;
    this.prefillStrategyEntryRepository = prefillStrategyEntryRepository;
    this.destinationRepository = destinationRepository;
  }

  @Override
  public FormSpecification createForm(int groupId) {

    return formRepository.save(new FormSpecification(-1, groupId, new ArrayList<>()));
  }

  @Override
  public FormSpecification createForm(FormSpecification formSpecification, List<AssignedFormModule> modules) throws InsufficientRightsException {
    formSpecification.setId(-1);
    formSpecification = formRepository.save(formSpecification);
    setModules(sessionProvider.getCurrentSession(), formSpecification.getId(), modules);
    return formSpecification;
  }

  @Override
  public FormSpecification createForm(int groupId, List<FormItem> items)
      throws FormItemRegexNotValid, InsufficientRightsException {
    FormSpecification formSpecification = createForm(groupId);

    setFormItems(formSpecification.getId(), items);

    return formSpecification;
  }

  @Override
  public void deleteForm(int formId) {
    FormSpecification formSpecification = formRepository.findById(formId).orElseThrow(() -> new EntityNotExistsException("FormSpecification", formId));

    formRepository.delete(formSpecification);
  }

  @Override
  public FormItem setFormItem(int formId, FormItem formItem) throws FormItemRegexNotValid {
    FormSpecification formSpecification = formRepository.findById(formId).orElseThrow(() -> new EntityNotExistsException("FormSpecification", formId));

    // Validate regex if present
    if (formItem.getItemDefinition() != null && formItem.getItemDefinition().getValidator() != null && !formItem.getItemDefinition().getValidator().isEmpty()) {
      try {
        java.util.regex.Pattern.compile(formItem.getItemDefinition().getValidator());
      } catch (java.util.regex.PatternSyntaxException e) {
        throw new FormItemRegexNotValid("Cannot compile regex: " + formItem.getItemDefinition().getValidator(), formItem);
      }
    }

    formItem.setFormId(formId);
    FormItem savedItem = formItemRepository.save(formItem);

    List<FormItem> items = formSpecification.getItems();
    items.add(savedItem);
    formSpecification.setItems(items);

    formRepository.update(formSpecification);
    return savedItem;
  }

  @Override
  public void setFormItems(int formId, List<FormItem> items) throws FormItemRegexNotValid {
    FormSpecification formSpecification = formRepository.findById(formId).orElseThrow(() -> new EntityNotExistsException("FormSpecification", formId));

    items.forEach(item -> {
                      item.setFormId(formSpecification.getId());
                      formItemRepository.update(item);
        }
    );

    formSpecification.setItems(items);
    formRepository.update(formSpecification);

  }

  @Override
  public List<AssignedFormModule> getAssignedFormModules(FormSpecification formSpecification) {
    List<AssignedFormModule> modules = formModuleRepository.findAllByFormId(formSpecification.getId());

    for (AssignedFormModule module : modules) {
      try {
        setModule(module);
      } catch (FormModuleNotExistsException e) {
        throw new DataInconsistencyException("Already assigned module class " + module.getModuleName() + " not found" +
                                                 " when retrieving modules for form " + formSpecification.getId());
      }
    }

    return modules;
  }

  @Override
  public List<AssignedFormModule> setModules(RegistrarAuthenticationToken sess, int formId, List<AssignedFormModule> modulesToAssign)
      throws InsufficientRightsException {
    FormSpecification formSpecification = formRepository.findById(formId).orElseThrow(() -> new EntityNotExistsException("FormSpecification", formId));

    if (!authorizationService.canManage(sess, formSpecification.getGroupId())) {
      // 403
      throw new InsufficientRightsException("You are not authorized to create a form for this group");
    }

    List<AssignedFormModule> modules = new ArrayList<>();

    for (AssignedFormModule module : modulesToAssign) {
      try {
        AssignedFormModule moduleWithComponent = this.setModule(module);
        if (!moduleWithComponent.getFormModule().getRequiredOptions().isEmpty()) {
          if (moduleWithComponent.getOptions() == null || moduleWithComponent.getOptions().isEmpty()) {
            throw new IllegalArgumentException("Module " + module.getModuleName() + " requires options.");
          }
          for (String requiredOption : moduleWithComponent.getFormModule().getRequiredOptions()) {
            if (!moduleWithComponent.getOptions().containsKey(requiredOption)) {
              throw new IllegalArgumentException("Module " + module.getModuleName() + " requires option " + requiredOption);
            }
          }
        }
        moduleWithComponent.setFormId(formSpecification.getId());
        modules.add(moduleWithComponent);
      } catch (FormModuleNotExistsException e) {
        throw new IllegalArgumentException("Module " + module.getModuleName() + " not found");
      }
    }

    formModuleRepository.saveAll(modules);
    return modules;
  }

  @Override
  public List<FormSpecification> getAllFormsWithItems() {
    List<FormSpecification> formSpecifications = formRepository.findAll();
    formSpecifications.forEach(form -> form.setItems(formItemRepository.getFormItemsByFormId(form.getId())));
    return formSpecifications;
  }

  @Override
  public FormSpecification getFormById(int formId) {
    return formRepository.findById(formId)
        .orElseThrow(() -> new EntityNotExistsException("FormSpecification", formId));
  }

  @Override
  public FormTransition addPrerequisiteToForm(FormSpecification sourceForm, FormSpecification prerequisiteForm,
                                              List<Requirement.TargetState> sourceFormStates,
                                              Requirement.TargetState targetState) {
    if (detectTransitionCycle(sourceForm, prerequisiteForm, targetState, FormTransition.TransitionType.PREREQUISITE)) {
      throw new IllegalArgumentException("Transition cycle detected");
    }
    return formTransitionRepository.save(new FormTransition(sourceForm, prerequisiteForm, sourceFormStates, targetState,
        FormTransition.TransitionType.PREREQUISITE));
  }

  @Override
  public void removePrerequisiteFromForm(FormTransition transition) {
    formTransitionRepository.remove(transition);
  }

  @Override
  public List<FormTransition> getPrerequisiteTransitionsForForm(FormSpecification formSpecification) {
    return formTransitionRepository.getAllBySourceFormAndType(formSpecification, FormTransition.TransitionType.PREREQUISITE);
  }

  /**
   * Detects whether the transition between the source and target forms has a cycle. (e.g. target prerequisite form already has a transition to the source form)
   * @param sourceForm
   * @param targetForm
   * @param targetFormState
   * @param transitionType
   * @return
   */
  private boolean detectTransitionCycle(FormSpecification sourceForm, FormSpecification targetForm,
                                        Requirement.TargetState targetFormState, FormTransition.TransitionType transitionType) {
    List<FormTransition> transitions = formTransitionRepository.getAllBySourceFormAndType(targetForm, transitionType);
    return transitions.stream()
               .filter(transition -> transition.getSourceFormStates().contains(targetFormState))
               .anyMatch(transition -> transition.getTargetFormSpecification().equals(sourceForm));
  }


  //TODO what about PRE forms that also have prerequisites? Recursively check all prerequisites or do not allow?
  @Override
  public List<FormTransition> getPrerequisiteTransitions(FormSpecification formSpecification, Requirement.TargetState targetState) {
    List<FormTransition> transitions = formTransitionRepository.getAllBySourceFormAndType(formSpecification, FormTransition.TransitionType.PREREQUISITE);
    // TODO add some logic to determine the order of PRE forms? More so if we want recursive prerequisites.
    //  for now recursive form retrieval not necessary
    return transitions.stream()
               .filter(transition -> transition.getSourceFormStates().contains(targetState))
               .toList();
  }

  //TODO again what to do with autosubmit forms with autosubmit forms?
  @Override
  public List<FormSpecification> getAutosubmitForms(FormSpecification formSpecification, Requirement.TargetState targetState) {
    List<FormTransition> transitions = formTransitionRepository.getAllBySourceFormAndType(formSpecification, FormTransition.TransitionType.AUTO_SUBMIT);
    return transitions.stream()
               .filter(transition -> transition.getSourceFormStates().contains(targetState))
               .map(FormTransition::getTargetFormSpecification)
               .toList();
  }

  // TODO do we allow multiple forms? Probably yes, build composite form with them in GUI
  @Override
  public List<FormSpecification> getRedirectForms(FormSpecification formSpecification, Requirement.TargetState targetState) {
    List<FormTransition> transitions = formTransitionRepository.getAllBySourceFormAndType(formSpecification, FormTransition.TransitionType.REDIRECT);
    return transitions.stream()
               .filter(transition -> transition.getSourceFormStates().contains(targetState))
               .map(FormTransition::getTargetFormSpecification)
               .toList();
  }

  @Override
  public List<FormItem> getFormItems(FormSpecification formSpecification, FormSpecification.FormType type) {
    return formItemRepository.getFormItemsByFormId(formSpecification.getId()).stream()
               .filter(formItem -> formItem.getItemDefinition().getFormTypes().contains(type)).toList();
  }

  @Override
  public FormItem getFormItemById(int formItemId) {
    return formItemRepository.getFormItemById(formItemId).orElseThrow(() -> new DataInconsistencyException("Form item with ID " + formItemId + " not found"));
  }

  @Override
  public List<PrefillStrategyEntry> getPrefillStrategiesForForm(FormSpecification formSpecification) {
    return prefillStrategyEntryRepository.findByFormSpecification(formSpecification);
  }

  @Override
  public  List<PrefillStrategyEntry> getGlobalPrefillStrategies() {
    return prefillStrategyEntryRepository.findAllGlobal();
  }

  @Override
  public PrefillStrategyEntry getPrefillStrategyById(int prefillStrategyId) {
    return prefillStrategyEntryRepository.findById(prefillStrategyId).orElse(null);
  }

  @Override
  public PrefillStrategyEntry createPrefillStrategy(PrefillStrategyEntry prefillStrategyEntry) {
    if (prefillStrategyEntryRepository.findById(prefillStrategyEntry.getId()).isPresent()) {
      throw new IllegalArgumentException("Prefill strategy with ID " + prefillStrategyEntry.getId() + " already exists");
    }

    return prefillStrategyEntryRepository.save(prefillStrategyEntry);
  }

  @Override
  public List<ItemDefinition> getItemDefinitionsForForm(FormSpecification formSpecification) {

    return itemDefinitionRepository.findAllByForm(formSpecification);
  }

  @Override
  public List<ItemDefinition> getGlobalItemDefinitions() {

    return itemDefinitionRepository.findAllGlobal();
  }

  @Override
  public Set<String> getGlobalDestinations() {
    return destinationRepository.getGlobalDestinations();
  }

  @Override
  public Set<String> getDestinationsForForm(FormSpecification formSpecification) {
    return destinationRepository.getDestinationsForForm(formSpecification);
  }

  @Override
  public String createDestination(FormSpecification formSpecification, String destination) {
    return destinationRepository.createDestination(formSpecification, destination);
  }

  @Override
  public void removeDestination(FormSpecification formSpecification, String destination) {
    destinationRepository.removeDestination(formSpecification, destination);
  }

  @Override
  public ItemDefinition createItemDefinition(ItemDefinition itemDefinition) {
    if (itemDefinitionRepository.findById(itemDefinition.getId()).isPresent()) {
      throw new IllegalArgumentException("Item definition with ID " + itemDefinition.getId() + " already exists");
    }

    return itemDefinitionRepository.save(itemDefinition);
  }

  @Override
  public FormItem createFormItem(FormItem item) {
    // TODO add some validation?

    return formItemRepository.save(item);
  }

  @Override
  // TODO make transactional?
  public void updateFormItems(int formId, List<FormItem> updatedItems) {
    formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form with ID " + formId + " not found"));
    updatedItems.forEach(item -> {
      if (item.getFormId() != formId) {
        throw new IllegalArgumentException("Form id of item  " + item + " does not match the specified form id!");
      }
    });

    Set<Integer> presentGlobalDefinitions = new HashSet<>();
    updatedItems.forEach(item -> {
      ItemDefinition itemDefinition = itemDefinitionRepository.findById(item.getItemDefinition().getId())
                                          .orElseThrow(() -> new EntityNotExistsException("Item definition", item.getItemDefinition().getId()));
      item.setItemDefinition(itemDefinition);
      if (itemDefinition.isGlobal()) {
        presentGlobalDefinitions.add(itemDefinition.getId());
        return;
      }
      if (itemDefinition.getFormSpecification().getId() != formId) {
        throw new IllegalArgumentException("Item definition " + item.getItemDefinition().getId() + " does not match the specified form id!");
      }
    });
    Set<Integer> globalDefinitions = getGlobalItemDefinitions().stream()
                                         .map(ItemDefinition::getId).collect(Collectors.toSet());
    Set<Integer> missingGlobalDefinitions = globalDefinitions.stream().filter(presentGlobalDefinitions::contains).collect(Collectors.toSet());
    if (!missingGlobalDefinitions.isEmpty()) {
      // ensure all enforced items are present
      throw new IllegalArgumentException("Global item definitions " + missingGlobalDefinitions + " not found");
    }

    if (!validateUniqueOrdNums(updatedItems)) {
      // is this the correct placement for the check?
      throw new IllegalArgumentException("Form items should have unique ordNums");
    }

    List<FormItem> existingItems = formItemRepository.getFormItemsByFormId(formId);

    Map<Integer, FormItem> existingById = existingItems.stream()
        .collect(Collectors.toMap(FormItem::getId, Function.identity()));

    // to resolve dependencies on new items, tempId -> real new ID
    Map<Integer, Integer> newIdMap = new HashMap<>();

    // new items
    updatedItems.stream()
        .filter(formItem -> formItem.getId() < 0)
        .forEach(formItem -> {
          FormItem createdItem = formItemRepository.save(formItem);
          newIdMap.put(formItem.getId(), createdItem.getId());
        });

    // updates
    for (FormItem updatedItem : updatedItems) {
      int actualId = updatedItem.getId();
      FormItem item = null;
      if (updatedItem.getId() < 0) {
        actualId = newIdMap.get(updatedItem.getId());
      } else {
        if (!existingById.containsKey(actualId)) {
          throw new IllegalArgumentException("Trying to update a form item from another form!");
        }
        item = updatedItem;
        // existing item so definition has to exist as well
        ItemDefinition existingItemDef = existingById.get(actualId).getItemDefinition();
        // prolly retrieve the items from db based on id in the entry layer (along with validation)
        if (existingItemDef.getId() != updatedItem.getItemDefinition().getId()) {
          throw new IllegalArgumentException("Trying to edit definition of another form item!");
        }

        // no need for checks if item definition is defined globally
        if (!existingItemDef.isGlobal()) {
          // check that updated items do not change destination, if so then warn/throw exception
          String oldDestination = existingItemDef.getDestinationAttributeUrn();
          if (oldDestination != null) {
            String newDestination = item.getItemDefinition().getDestinationAttributeUrn();
            boolean hasOpenApplications = applicationRepository.findByFormId(formId).stream()
                                              .anyMatch(app -> app.getState().isOpenState());
            if (!oldDestination.equals(newDestination) && hasOpenApplications) {
              // TODO throw an exception, or display some sort of warning in this case?
              throw new IllegalArgumentException("Cannot change destination of item with id " + actualId +
                                                     " because there are open applications");
            }
          }
        } else {
          // rewrite with global definition to prevent unauthorized changes to it
          updatedItem.setItemDefinition(existingItemDef);
        }
      }
      if (item == null) {
        item = formItemRepository.getFormItemById(actualId)
                   .orElseThrow(() -> new DataInconsistencyException("Form item with id " + updatedItem.getId() + " should exist"));
      }

      item.setParentId(resolveReferenceItemId(item.getParentId(), newIdMap));
      item.setHiddenDependencyItemId(resolveReferenceItemId(item.getHiddenDependencyItemId(), newIdMap));
      item.setDisabledDependencyItemId(resolveReferenceItemId(item.getDisabledDependencyItemId(), newIdMap));
      // item.setOrdNum(updatedItem.getOrdNum());

      formItemRepository.save(item);
    }

    // removal
    Set<Integer> incomingRealIds = updatedItems.stream()
        .map(formItem -> resolveReferenceItemId(formItem.getId(), newIdMap))
        .collect(Collectors.toSet());

    for (FormItem existingItem : existingItems) {
      if (!incomingRealIds.contains(existingItem.getId())) {
        formItemRepository.delete(existingItem);
      }
    }

    List<FormItem> finalItems = formItemRepository.getFormItemsByFormId(formId);

    validateFormStructureAndDeps(finalItems);

    checkFormItemVisibility(finalItems);
  }

  @Override
  public Map<String, List<String>> getAvailableModulesWithRequiredOptions() {
    return Map.of();
  }

  /**
   * Make sure it is possible to fill out all the items required for submission (e.g. no hidden/disabled required items)
   * @param items
   * @return
   */
  private void checkFormItemVisibility(List<FormItem> items) {
    Map<Integer, FormItem> formItemMap = items.stream()
                                             .collect(Collectors.toMap(FormItem::getId, item -> item));
    List<FormItem> invalidItems = new ArrayList<>();

    items.forEach(item -> {
      Set<Integer> seenItems = new HashSet<>();
      if (item.getItemDefinition().isRequired() && willItemAlwaysBeEmpty(item, formItemMap, seenItems)) {
        invalidItems.add(item);
      }
    });
    if (!invalidItems.isEmpty()) {
      throw new IllegalArgumentException("The following items are required but cannot be filled out: " + invalidItems);
    }
  }

  /**
   * Returns true if the item value cannot ever be filled, e.g. there is no prefill strategy, no default value and the
   * item is hidden/disabled. To determine whether the item will be hidden or disabled, this also recursively checks
   * IF_EMPTY dependency items.
   * TODO this could cause poor performance as it handles items listed as IF_EMPTY dependencies multiple times
   * @param item
   * @param formItemMap
   * @param seenItems
   * @return
   */
  private boolean willItemAlwaysBeEmpty(FormItem item, Map<Integer, FormItem> formItemMap, Set<Integer> seenItems) {
    if (!seenItems.add(item.getId())) {
      // Circular dependencies on their own aren't a problem(?), as long as they don't cause the items to be unfillable (as is the case here)
      throw new IllegalArgumentException("Circular dependency detected starting with item: " + item);
    }
    return isItemPrefillEmpty(item.getItemDefinition()) &&
               (isEmptyItemHidden(item, formItemMap, seenItems) || isEmptyItemDisabled(item, formItemMap, seenItems));

  }

  private boolean isItemPrefillEmpty(ItemDefinition itemDef) {
    return StringUtils.isEmpty(itemDef.getDefaultValue()) &&
               (itemDef.getPrefillStrategies() == null || itemDef.getPrefillStrategies().isEmpty());
  }

  private boolean isEmptyItemHidden(FormItem item, Map<Integer, FormItem> formItemMap, Set<Integer> seenItems) {
    return isEmptyItemConditionApplied(item.getItemDefinition().getHidden(), item, formItemMap, seenItems);
  }

  private boolean isEmptyItemDisabled(FormItem item, Map<Integer, FormItem> formItemMap, Set<Integer> seenItems) {
    return isEmptyItemConditionApplied(item.getItemDefinition().getDisabled(), item, formItemMap, seenItems);
  }

  private boolean isEmptyItemConditionApplied(ItemDefinition.Condition condition, FormItem item,
                                              Map<Integer, FormItem> formItemMap, Set<Integer> seenItems) {
    FormItem dependencyItem = formItemMap.get(item.getHiddenDependencyItemId());
    if (dependencyItem != null) {
      return switch (condition) {
        case ALWAYS -> true;
        case NEVER -> false;
        case IF_EMPTY -> willItemAlwaysBeEmpty(dependencyItem, formItemMap, seenItems);
        // TODO how do we want to behave if the dependency item is prefilled, but changed by the user afterwards (e.g. the item itself
        //  is not disabled/hidden if_prefilled). This is mostly a GUI question, but still relates to this check.
        case IF_PREFILLED -> !isItemPrefillEmpty(dependencyItem.getItemDefinition()); // not problematic if it doesn't actually get prefilled, still should not allow TODO verify this
      };
    }

    return switch (condition) {
      case ALWAYS, IF_EMPTY -> true; // since prefill is empty the condition will be applied
      case NEVER, IF_PREFILLED -> false; // since prefill is empty the condition will NOT be applied
    };
  }

  /**
   * Checks whether the items have unique ordNums if they share a parent/have none.
   * @param items
   * @return
   */
  private boolean validateUniqueOrdNums(List<FormItem> items) {
    Map<Integer, Set<Integer>> parentToOrdNum = new HashMap<>();
    for (FormItem item : items) {
      // initialize the set if first child item
      Set<Integer> ordNums = parentToOrdNum.computeIfAbsent(item.getParentId(), k -> new HashSet<>());

      if (!ordNums.add(item.getOrdNum())) {
        // duplicate ordNum
        return false;
      }
    }
    return true;
  }

  /**
   * Checks whether the item depends on a newly added item, if so return that item's newly assigned ID
   * @param referenceItemId
   * @param newIdMap
   * @return
   */
  private Integer resolveReferenceItemId(Integer referenceItemId, Map<Integer, Integer> newIdMap) {
    if (referenceItemId == null) {
      return null;
    }
    if (referenceItemId < 0) {
      return newIdMap.get(referenceItemId);
    }
    return referenceItemId;
  }

  /**
   * Validates item dependencies (e.g. the dependency items exist, item does not depend on itself, hidden/disabled
   * dependencies are not on layout items) and checks that there are no structure cycles.
   * @param items
   */
  private void validateFormStructureAndDeps(List<FormItem> items) {
    Map<Integer, FormItem> existingById = items.stream()
        .collect(Collectors.toMap(FormItem::getId, Function.identity()));

    // parent integrity
    items.forEach(item -> {
      checkItemDependency(item.getId(), item.getParentId(), existingById, "Parent");
      checkItemDependency(item.getId(), item.getHiddenDependencyItemId(), existingById, "Hidden");
      checkItemDependency(item.getId(), item.getDisabledDependencyItemId(), existingById, "Disabled");
    });

    // is submittable
    checkSubmissibility(items);

    detectParentItemCycles(items);

    // TODO detect disabled/hidden dependency cycles as well (using DFS)? Or is it a real use case to have 2 items depend on each other? (we expect both to be prefilled -> hidden/disabled or something of that sort)
  }

  /**
   * Checks whether the form contains fillable items. If so, it requires a submit item, check that it contains at least
   * one.
   * TODO technically visibility check using `willItemAlwaysBeEmpty` could be done
   * @param items
   */
  private void checkSubmissibility(List<FormItem> items) {
    boolean containsValueItems = items.stream()
                                     .map(FormItem::getItemDefinition)
            .anyMatch(itemDef -> !itemDef.getType().isLayoutItem());
    if (containsValueItems &&
            items.stream()
                .map(FormItem::getItemDefinition)
                .noneMatch(itemDef -> itemDef.getType().isSubmitItem() &&
                                       !(itemDef.getHidden().equals(ItemDefinition.Condition.ALWAYS) ||
                                           itemDef.getHidden().equals(ItemDefinition.Condition.IF_EMPTY)))) {
      throw new IllegalArgumentException("Submit item required but not present or visible.");
    }
  }

  private void checkItemDependency(Integer itemId, Integer dependencyId, Map<Integer, FormItem> existingById,
                                   String dependencyType) {
    if (dependencyId != null) {
      if (!existingById.containsKey(dependencyId)) {
        throw new IllegalArgumentException(dependencyType + " dependency item with id " + dependencyId + "for item " +
                                               itemId + " does not exist");
      }
      if (Objects.equals(dependencyId, itemId)) {
        throw new IllegalArgumentException(dependencyType + " dependency item" + dependencyId + " cannot be the same as the item itself");
      }

      if (!dependencyType.equals("Parent") && existingById.get(dependencyId).getItemDefinition().getType().isLayoutItem()) {
        throw new IllegalArgumentException("Cannot depend on layout item as it does not have a value");
      }
    }
  }

  private void detectParentItemCycles(List<FormItem> items) {
    Map<Integer, Integer> childToParent = items.stream()
                                              .filter(formItem -> formItem.getParentId() != null)
                                              .collect(Collectors.toMap(FormItem::getId, FormItem::getParentId));

    for (Integer childId : childToParent.keySet()) {
      Set<Integer> visited = new HashSet<>();
      Integer current = childId;
      while (current != null) {
        if (visited.contains(current)) {
          throw new IllegalArgumentException("Parent item cycle detected for item with id " + childId + " and parent with id " + current);
        }
        visited.add(current);
        current = childToParent.get(current);
      }
    }
  }

  /**
   * Sets module components by the name of the module.
   * @param module
   * @return
   */
  private AssignedFormModule setModule(AssignedFormModule module) throws FormModuleNotExistsException {
    try {
      FormModule formModule = context.getBean(module.getModuleName(), FormModule.class);
      module.setFormModule(formModule);
      return module;
    } catch (BeansException e) {
      throw new FormModuleNotExistsException("Could not find definition for module " + module.getModuleName() +
                                                 " when retrieving modules for form " + module.getFormId());
    }
  }
}
