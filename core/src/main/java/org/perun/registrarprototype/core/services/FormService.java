package org.perun.registrarprototype.core.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.perun.registrarprototype.core.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.core.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.core.exceptions.FormModuleNotExistsException;
import org.perun.registrarprototype.core.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.core.models.AssignedFormModule;
import org.perun.registrarprototype.core.models.Form;
import org.perun.registrarprototype.core.models.FormItem;
import org.perun.registrarprototype.core.models.FormTransition;
import org.perun.registrarprototype.core.repositories.FormItemRepository;
import org.perun.registrarprototype.core.repositories.FormModuleRepository;
import org.perun.registrarprototype.core.repositories.FormRepository;
import org.perun.registrarprototype.core.repositories.FormTransitionRepository;
import org.perun.registrarprototype.core.security.CurrentUser;
import org.perun.registrarprototype.extension.dto.FormType;
import org.perun.registrarprototype.extension.services.modules.FormModule;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class FormService {
  private final FormRepository formRepository;
  private final AuthorizationService authorizationService;
  private final FormModuleService formModuleService;
  private final FormItemRepository formItemRepository;
  private final FormTransitionRepository formTransitionRepository;
  private final ApplicationContext context;

  public FormService(FormRepository formRepository, AuthorizationService authorizationService,
                     FormModuleService formModuleService, ApplicationContext context,
                     FormItemRepository formItemRepository, FormTransitionRepository formTransitionRepository) {
    this.formRepository = formRepository;
    this.formModuleService = formModuleService;
    this.authorizationService = authorizationService;
    this.context = context;
    this.formItemRepository = formItemRepository;
    this.formTransitionRepository = formTransitionRepository;
  }

  public Form createForm(CurrentUser sess, int groupId)
      throws FormItemRegexNotValid, InsufficientRightsException {
    if (!authorizationService.isAuthorized(sess, groupId)) {
      // 403
      throw new InsufficientRightsException("You are not authorized to create a form for this group");
    }

    return formRepository.save(new Form(-1, groupId, new ArrayList<>()));
  }

  public Form createForm(CurrentUser sess, int groupId, List<FormItem> items)
      throws FormItemRegexNotValid, InsufficientRightsException {
    Form form = createForm(sess, groupId);

    setFormItems(sess, form.getId(), items);

    return form;
  }

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

  public void setFormItems(CurrentUser sess, int formId, List<FormItem> items) throws FormItemRegexNotValid {
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




  public Form getFormById(CurrentUser sess, int formId) {
    return formRepository.findById(formId).orElseThrow(() -> new DataInconsistencyException("Form with ID " + formId + " not found. "));
  }

  /**
   * Retrieves all forms that are required to be filled before applying for membership via the supplied form.
   * TODO what about PRE forms that also have prerequisites? Recursively check all prerequisites or do not allow?
   * @return
   */
  public List<FormTransition> getPrerequisiteTransitions(Form form, FormType type) {
    List<FormTransition> transitions = formTransitionRepository.getAllBySourceFormAndType(form, FormTransition.TransitionType.PREREQUISITE);
    // TODO add some logic to determine the order of PRE forms? More so if we want recursive prerequisites.
    //  for now recursive form retrieval not necessary
    return transitions.stream()
               .filter(transition -> transition.getSourceFormTypes().contains(type))
               .toList();
  }

  /**
   * Retrieves all forms that are automatically submitted after the supplied form is submitted (AKA embedded).
   * TODO again what to do with autosubmit forms with autosubmit forms?
   * @param form
   * @return
   */
  public List<Form> getAutosubmitForms(Form form, FormType type) {
    List<FormTransition> transitions = formTransitionRepository.getAllBySourceFormAndType(form, FormTransition.TransitionType.AUTO_SUBMIT);
    return transitions.stream()
               .filter(transition -> transition.getSourceFormTypes().contains(type))
               .map(FormTransition::getTargetForm)
               .toList();
  }

  /**
   * Retrieves all forms that user is redirected to after submitting the supplied form.
   * TODO do we allow multiple forms? Probably yes, build composite form with them in GUI
   * @param form
   * @return
   */
  public List<Form> getRedirectForms(Form form, FormType type) {
    List<FormTransition> transitions = formTransitionRepository.getAllBySourceFormAndType(form, FormTransition.TransitionType.REDIRECT);
    return transitions.stream()
               .filter(transition -> transition.getSourceFormTypes().contains(type))
               .map(FormTransition::getTargetForm)
               .toList();
  }

  public List<FormItem> getFormItems(CurrentUser sess, Form form, FormType type) {
    return formItemRepository.getFormItemsByFormId(form.getId()).stream().filter(formItem -> formItem.getFormTypes().contains(type)).toList();
  }

  public FormItem getFormItemById(CurrentUser sess, int formItemId) {
    return formItemRepository.getFormItemById(formItemId).orElseThrow(() -> new DataInconsistencyException("Form item with ID " + formItemId + " not found"));
  }

  public FormItem createFormItem(FormItem item) {
    return formItemRepository.save(item);
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
