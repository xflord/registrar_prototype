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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import org.perun.registrarprototype.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.exceptions.FormModuleNotExistsException;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.models.Requirement;
import org.perun.registrarprototype.repositories.FormItemRepository;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.FormModuleRepository;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.FormTransitionRepository;
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

  public FormServiceImpl(FormRepository formRepository, AuthorizationService authorizationService,
                         FormModuleRepository formModuleRepository, ApplicationContext context,
                         FormItemRepository formItemRepository, FormTransitionRepository formTransitionRepository,
                         ApplicationRepository applicationRepository, SessionProvider sessionProvider) {
    this.formRepository = formRepository;
    this.authorizationService = authorizationService;
    this.formModuleRepository = formModuleRepository;
    this.context = context;
    this.formItemRepository = formItemRepository;
    this.formTransitionRepository = formTransitionRepository;
    this.applicationRepository = applicationRepository;
    this.sessionProvider = sessionProvider;
  }

  @Override
  public FormSpecification createForm(int groupId)
      throws FormItemRegexNotValid, InsufficientRightsException {

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
    FormSpecification formSpecification = formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form with ID " + formId + " not found"));

    formRepository.delete(formSpecification);
  }

  @Override
  public FormItem setFormItem(int formId, FormItem formItem) throws FormItemRegexNotValid {
    FormSpecification formSpecification = formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form with ID " + formId + " not found"));

    if (formItem.getConstraint() != null && !formItem.getConstraint().isEmpty()) {
        try {
          Pattern.compile(formItem.getConstraint());
        } catch (PatternSyntaxException e) {
          throw new FormItemRegexNotValid("Cannot compile regex: " + formItem.getConstraint(), formItem);
        }
      }

    formItem.setFormId(formId);
    formItemRepository.save(formItem);

    List<FormItem> items = formSpecification.getItems();
    items.add(formItem);
    formSpecification.setItems(items);

    formRepository.update(formSpecification);
    return formItem;
  }

  @Override
  public void setFormItems(int formId, List<FormItem> items) throws FormItemRegexNotValid {
    FormSpecification formSpecification = formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form with ID " + formId + " not found"));

    for (FormItem item : items) {
      if (item.getConstraint() != null && !item.getConstraint().isEmpty()) {
        try {
          Pattern.compile(item.getConstraint());
        } catch (PatternSyntaxException e) {
          throw new FormItemRegexNotValid("Cannot compile regex: " + item.getConstraint(), item);
        }
      }
    }

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
    FormSpecification formSpecification = formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form with ID " + formId + " not found"));

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
    return formRepository.findById(formId).orElseThrow(() -> new DataInconsistencyException("Form with ID " + formId + " not found. "));
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
               .anyMatch(transition -> transition.getTargetForm().equals(sourceForm));
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
               .map(FormTransition::getTargetForm)
               .toList();
  }

  // TODO do we allow multiple forms? Probably yes, build composite form with them in GUI
  @Override
  public List<FormSpecification> getRedirectForms(FormSpecification formSpecification, Requirement.TargetState targetState) {
    List<FormTransition> transitions = formTransitionRepository.getAllBySourceFormAndType(formSpecification, FormTransition.TransitionType.REDIRECT);
    return transitions.stream()
               .filter(transition -> transition.getSourceFormStates().contains(targetState))
               .map(FormTransition::getTargetForm)
               .toList();
  }

  @Override
  public List<FormItem> getFormItems(FormSpecification formSpecification, FormSpecification.FormType type) {
    return formItemRepository.getFormItemsByFormId(formSpecification.getId()).stream().filter(formItem -> formItem.getFormTypes().contains(type)).toList();
  }

  @Override
  public FormItem getFormItemById(int formItemId) {
    return formItemRepository.getFormItemById(formItemId).orElseThrow(() -> new DataInconsistencyException("Form item with ID " + formItemId + " not found"));
  }

  @Override
  public FormItem createFormItem(FormItem item) {
    return formItemRepository.save(item);
  }

  @Override
  // TODO make transactional?
  public void updateFormItems(int formId, List<FormItem> newItems) {
    formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form with ID " + formId + " not found"));

    if (!validateUniqueOrdNums(newItems)) {
      // is this the correct placement for the check?
      throw new IllegalArgumentException("Form items should have unique ordNums");
    }

    List<FormItem> existingItems = formItemRepository.getFormItemsByFormId(formId);

    Map<Integer, FormItem> existingById = existingItems.stream()
        .collect(Collectors.toMap(FormItem::getId, Function.identity()));

    // to resolve dependencies on new items, tempId -> real new ID
    Map<Integer, Integer> newIdMap = new HashMap<>();

    // new items
    newItems.stream()
        .filter(formItem -> formItem.getId() < 0)
        .forEach(formItem -> {
          FormItem createdItem = formItemRepository.save(formItem);
          newIdMap.put(formItem.getId(), createdItem.getId());
        });

    // updates
    for (FormItem newItem : newItems) {
      int actualId = newItem.getId();
      if (actualId < 0) {
        actualId = newIdMap.get(newItem.getId());
      } else {
        // check that updated items do not change destination, if so then warn/throw exception
        String oldDestination = existingById.get(actualId).getDestinationIdmAttribute();
        String newDestination = newItem.getDestinationIdmAttribute();
        boolean hasOpenApplications = applicationRepository.findByFormId(formId).stream()
            .anyMatch(app -> app.getState().isOpenState());
        if (!oldDestination.equals(newDestination) && hasOpenApplications) {
          // TODO throw an exception, or display some sort of warning in this case?
          throw new IllegalArgumentException("Cannot change destination of item with id " + actualId +
                                                 " because there are open applications");
        }
      }

      FormItem item = existingById.get(actualId);
      if (item == null) {
        item = formItemRepository.getFormItemById(actualId)
                   .orElseThrow(() -> new DataInconsistencyException("Form item with id " + newItem.getId() + " should exist"));
      }

      if (item.getType().equals(FormItem.Type.PASSWORD)) {
        // TODO a form of enforcing certain rules (label format, allowed destination attributes, etc.) via yaml config?
        if (item.getDestinationIdmAttribute() == null) {
          throw new IllegalArgumentException("Password item must have a destination IDM attribute");
        }
      }

      if (item.getType().isHtmlItem()) {
        // TODO validate/sanitize HTML content
      }

      item.setParentId(resolveReferenceItemId(item.getParentId(), newIdMap));
      item.setHiddenDependencyItemId(resolveReferenceItemId(item.getHiddenDependencyItemId(), newIdMap));
      item.setDisabledDependencyItemId(resolveReferenceItemId(item.getDisabledDependencyItemId(), newIdMap));
      // item.setOrdNum(newItem.getOrdNum());

      formItemRepository.save(item);
    }

    // removal
    Set<Integer> incomingRealIds = newItems.stream()
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
      if (item.isRequired() && willItemAlwaysBeEmpty(item, formItemMap, seenItems)) {
        invalidItems.add(item);
      }
    });
    if (!invalidItems.isEmpty()) {
      throw new IllegalArgumentException("The following items are required but cannot be filled out: " + invalidItems);
    }
  }

  private boolean willItemAlwaysBeEmpty(FormItem item, Map<Integer, FormItem> formItemMap, Set<Integer> seenItems) {
    if (!seenItems.add(item.getId())) {
      // Circular dependencies on their own aren't a problem, as long as they don't cause the items to be unfillable (as is the case here)
      throw new IllegalArgumentException("Circular dependency detected starting with item: " + item);
    }
    return isItemPrefillEmpty(item) &&
               (isEmptyItemHidden(item, formItemMap, seenItems) || isEmptyItemDisabled(item, formItemMap, seenItems));

  }

  private boolean isItemPrefillEmpty(FormItem item) {
    return StringUtils.isEmpty(item.getDefaultValue()) &&
               (item.getPrefillStrategyTypes() == null || item.getPrefillStrategyTypes().isEmpty());
  }

  private boolean isEmptyItemHidden(FormItem item, Map<Integer, FormItem> formItemMap, Set<Integer> seenItems) {
    return isEmptyItemConditionApplied(item.getHidden(), item, formItemMap, seenItems);
  }

  private boolean isEmptyItemDisabled(FormItem item, Map<Integer, FormItem> formItemMap, Set<Integer> seenItems) {
    return isEmptyItemConditionApplied(item.getDisabled(), item, formItemMap, seenItems);
  }

  private boolean isEmptyItemConditionApplied(FormItem.Condition condition, FormItem item,
                                              Map<Integer, FormItem> formItemMap, Set<Integer> seenItems) {
    FormItem dependencyItem = formItemMap.get(item.getHiddenDependencyItemId());
    if (dependencyItem != null) {
      return switch (condition) {
        case ALWAYS -> true;
        case NEVER -> false;
        case IF_EMPTY -> willItemAlwaysBeEmpty(dependencyItem, formItemMap, seenItems);
        // TODO how do we want to behave if the dependency item is prefilled, but changed by the user afterwards (e.g. the item itself
        //  is not disabled/hidden if_prefilled). This is mostly a GUI question, but still relates to this check.
        case IF_PREFILLED -> !isItemPrefillEmpty(dependencyItem); // not problematic if it doesn't actually get prefilled, still should not allow TODO verify this
      };
    }

    return switch (condition) {
      case ALWAYS, IF_EMPTY -> true; // since prefill is empty this will NOT be fine
      case NEVER, IF_PREFILLED -> false; // since prefill is empty this will be fine
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

  private void validateFormStructureAndDeps(List<FormItem> items) {
    Map<Integer, FormItem> existingById = items.stream()
        .collect(Collectors.toMap(FormItem::getId, Function.identity()));

    // parent integrity
    items.forEach(item -> {
      Integer parentId = item.getParentId();
      checkItemDependency(item.getId(), parentId, existingById, "Parent");
      checkItemDependency(item.getId(), item.getHiddenDependencyItemId(), existingById, "Hidden");
      checkItemDependency(item.getId(), item.getDisabledDependencyItemId(), existingById, "Disabled");
    });

    detectParentItemCycles(items);

    // TODO detect disabled/hidden dependency cycles as well (using DFS)? Or is it a real use case to have 2 items depend on each other? (we expect both to be prefilled -> hidden/disabled or something of that sort)
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
