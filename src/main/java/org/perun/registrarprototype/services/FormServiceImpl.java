package org.perun.registrarprototype.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.perun.registrarprototype.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.exceptions.FormModuleNotExistsException;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.repositories.FormItemRepository;
import org.perun.registrarprototype.repositories.FormModuleRepository;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.FormTransitionRepository;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.services.modules.FormModule;
import org.perun.registrarprototype.services.modules.ModulesManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class FormServiceImpl implements FormService {
  private final FormRepository formRepository;
  private final AuthorizationService authorizationService;
  private final FormModuleRepository formModuleRepository;
  private final ModulesManager modulesManager;
  private final FormItemRepository formItemRepository;
  private final FormTransitionRepository formTransitionRepository;
  private final ApplicationContext context;

  public FormServiceImpl(FormRepository formRepository, AuthorizationService authorizationService,
                         FormModuleRepository formModuleRepository, ModulesManager modulesManager,
                         ApplicationContext context,
                         FormItemRepository formItemRepository, FormTransitionRepository formTransitionRepository) {
    this.formRepository = formRepository;
    this.authorizationService = authorizationService;
    this.formModuleRepository = formModuleRepository;
    this.modulesManager = modulesManager;
    this.context = context;
    this.formItemRepository = formItemRepository;
    this.formTransitionRepository = formTransitionRepository;
  }

  @Override
  public Form createForm(int groupId)
      throws FormItemRegexNotValid, InsufficientRightsException {

    return formRepository.save(new Form(-1, groupId, new ArrayList<>()));
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
      FormModule moduleComponent = modulesManager.getModule(module.getModuleName());
      if (moduleComponent != null) {
        module.setFormModule(moduleComponent);
      } else {
        throw new DataInconsistencyException("Already assigned module class " + module.getModuleName() + " not found" +
                                                 " when retrieving modules for form " + form.getId());
      }
    }

    return modules;
  }

  @Override
  public List<AssignedFormModule> setModules(RegistrarAuthenticationToken sess, int formId,
                                             List<AssignedFormModule> modulesToAssign)
      throws InsufficientRightsException {
    Form form = formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form with ID " + formId + " not found"));

    if (!authorizationService.canManage(sess, form.getGroupId())) {
      // 403
      throw new InsufficientRightsException("You are not authorized to create a form for this group");
    }

    List<AssignedFormModule> modules = new ArrayList<>();

    for (AssignedFormModule assignedFormModule : modulesToAssign) {
        FormModule moduleComponent = modulesManager.getModule(assignedFormModule.getModuleName());
        if (moduleComponent == null) {
          throw new IllegalArgumentException("Module " + assignedFormModule.getModuleName() + " not found");
        }
        if (!moduleComponent.getRequiredOptions().isEmpty()) {
          if (assignedFormModule.getOptions() == null || assignedFormModule.getOptions().isEmpty()) {
            throw new IllegalArgumentException("Module " + assignedFormModule.getModuleName() + " requires options.");
          }
          for (String requiredOption : moduleComponent.getRequiredOptions()) {
            if (!assignedFormModule.getOptions().containsKey(requiredOption)) {
              throw new IllegalArgumentException("Module " + assignedFormModule.getModuleName() + " requires option " + requiredOption);
            }
          }
        }
        assignedFormModule.setFormId(form.getId());
        modules.add(assignedFormModule);
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
}
