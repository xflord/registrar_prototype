package org.perun.registrarprototype.services;

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
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.repositories.FormItemRepository;
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
  private final ApplicationService applicationService;
  private final SessionProvider sessionProvider;

  public FormServiceImpl(FormRepository formRepository, AuthorizationService authorizationService,
                         FormModuleRepository formModuleRepository, ApplicationContext context,
                         FormItemRepository formItemRepository, FormTransitionRepository formTransitionRepository,
                         ApplicationService applicationService, SessionProvider sessionProvider) {
    this.formRepository = formRepository;
    this.authorizationService = authorizationService;
    this.formModuleRepository = formModuleRepository;
    this.context = context;
    this.formItemRepository = formItemRepository;
    this.formTransitionRepository = formTransitionRepository;
    this.applicationService = applicationService;
    this.sessionProvider = sessionProvider;
  }

  @Override
  public Form createForm(int groupId)
      throws FormItemRegexNotValid, InsufficientRightsException {

    return formRepository.save(new Form(-1, groupId, new ArrayList<>()));
  }

  @Override
  public Form createForm(Form form, List<AssignedFormModule> modules) throws InsufficientRightsException {
    form.setId(-1);
    form = formRepository.save(form);
    setModules(sessionProvider.getCurrentSession(), form.getId(), modules);
    return form;
  }

  @Override
  public Form createForm(int groupId, List<FormItem> items)
      throws FormItemRegexNotValid, InsufficientRightsException {
    Form form = createForm(groupId);

    setFormItems(form.getId(), items);

    return form;
  }

  @Override
  public FormItem setFormItem(int formId, FormItem formItem) throws FormItemRegexNotValid {
    Form form = formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form with ID " + formId + " not found"));

    if (formItem.getConstraint() != null && !formItem.getConstraint().isEmpty()) {
        try {
          Pattern.compile(formItem.getConstraint());
        } catch (PatternSyntaxException e) {
          throw new FormItemRegexNotValid("Cannot compile regex: " + formItem.getConstraint(), formItem);
        }
      }

    formItem.setFormId(formId);
    formItemRepository.save(formItem);

    List<FormItem> items = form.getItems();
    items.add(formItem);
    form.setItems(items);

    formRepository.update(form);
    return formItem;
  }

  @Override
  public void setFormItems(int formId, List<FormItem> items) throws FormItemRegexNotValid {
    Form form = formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form with ID " + formId + " not found"));

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
                      item.setFormId(form.getId());
                      formItemRepository.update(item);
        }
    );

    form.setItems(items);
    formRepository.update(form);

  }

  @Override
  public List<AssignedFormModule> getAssignedFormModules(Form form) {
    List<AssignedFormModule> modules = formModuleRepository.findAllByFormId(form.getId());

    for (AssignedFormModule module : modules) {
      try {
        setModule(module);
      } catch (FormModuleNotExistsException e) {
        throw new DataInconsistencyException("Already assigned module class " + module.getModuleName() + " not found" +
                                                 " when retrieving modules for form " + form.getId());
      }
    }

    return modules;
  }

  @Override
  public List<AssignedFormModule> setModules(RegistrarAuthenticationToken sess, int formId, List<AssignedFormModule> modulesToAssign)
      throws InsufficientRightsException {
    Form form = formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form with ID " + formId + " not found"));

    if (!authorizationService.canManage(sess, form.getGroupId())) {
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
        moduleWithComponent.setFormId(form.getId());
        modules.add(moduleWithComponent);
      } catch (FormModuleNotExistsException e) {
        throw new IllegalArgumentException("Module " + module.getModuleName() + " not found");
      }
    }

    formModuleRepository.saveAll(modules);
    return modules;
  }

  @Override
  public List<Form> getAllFormsWithItems() {
    List<Form> forms = formRepository.findAll();
    forms.forEach(form -> form.setItems(formItemRepository.getFormItemsByFormId(form.getId())));
    return forms;
  }

  @Override
  public Form getFormById(int formId) {
    return formRepository.findById(formId).orElseThrow(() -> new DataInconsistencyException("Form with ID " + formId + " not found. "));
  }


  //TODO what about PRE forms that also have prerequisites? Recursively check all prerequisites or do not allow?
  @Override
  public List<FormTransition> getPrerequisiteTransitions(Form form, Form.FormType type) {
    List<FormTransition> transitions = formTransitionRepository.getAllBySourceFormAndType(form, FormTransition.TransitionType.PREREQUISITE);
    // TODO add some logic to determine the order of PRE forms? More so if we want recursive prerequisites.
    //  for now recursive form retrieval not necessary
    return transitions.stream()
               .filter(transition -> transition.getSourceFormTypes().contains(type))
               .toList();
  }

  //TODO again what to do with autosubmit forms with autosubmit forms?
  @Override
  public List<Form> getAutosubmitForms(Form form, Form.FormType type) {
    List<FormTransition> transitions = formTransitionRepository.getAllBySourceFormAndType(form, FormTransition.TransitionType.AUTO_SUBMIT);
    return transitions.stream()
               .filter(transition -> transition.getSourceFormTypes().contains(type))
               .map(FormTransition::getTargetForm)
               .toList();
  }

  // TODO do we allow multiple forms? Probably yes, build composite form with them in GUI
  @Override
  public List<Form> getRedirectForms(Form form, Form.FormType type) {
    List<FormTransition> transitions = formTransitionRepository.getAllBySourceFormAndType(form, FormTransition.TransitionType.REDIRECT);
    return transitions.stream()
               .filter(transition -> transition.getSourceFormTypes().contains(type))
               .map(FormTransition::getTargetForm)
               .toList();
  }

  @Override
  public List<FormItem> getFormItems(Form form, Form.FormType type) {
    return formItemRepository.getFormItemsByFormId(form.getId()).stream().filter(formItem -> formItem.getFormTypes().contains(type)).toList();
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
        boolean hasOpenApplications = !applicationService.getApplicationsForForm(formId,
            List.of(ApplicationState.PENDING, ApplicationState.CHANGES_REQUESTED)).isEmpty();
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
