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
import org.perun.registrarprototype.repositories.FormModuleRepository;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.services.modules.FormModule;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class FormService {
  private final FormRepository formRepository;
  private final AuthorizationService authorizationService;
  private final FormModuleRepository formModuleRepository;
  private final ApplicationContext context;

  public FormService(FormRepository formRepository, AuthorizationService authorizationService, FormModuleRepository formModuleRepository, ApplicationContext context) {
    this.formRepository = formRepository;
    this.authorizationService = authorizationService;
    this.formModuleRepository = formModuleRepository;
    this.context = context;
  }

  public Form createForm(CurrentUser sess, int groupId, List<FormItem> items)
      throws FormItemRegexNotValid, InsufficientRightsException {
    if (!authorizationService.isAuthorized(sess, groupId)) {
      // 403
      throw new InsufficientRightsException("You are not authorized to create a form for this group");
    }

    for (FormItem item : items) {
      if (item.getConstraint() != null && !item.getConstraint().isEmpty()) {
        try {
          Pattern.compile(item.getConstraint());
        } catch (PatternSyntaxException e) {
          throw new FormItemRegexNotValid("Cannot compile regex: " + item.getConstraint(), item);
        }
      }
    }

    Form form = new Form(formRepository.getNextId(), groupId, items);
    formRepository.save(form);
    return form;
  }

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

  /**
   * Sets modules for the form. Checks whether the module actually exists and whether all the required options are set.
   *
   * @param formId id of the form to assign modules to
   * @param modulesToAssign modules with module name and options set (the rest is ignored)
   * @return
   */
  public List<AssignedFormModule> setModules(CurrentUser sess, int formId, List<AssignedFormModule> modulesToAssign)
      throws InsufficientRightsException {
    Form form = formRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form with ID " + formId + " not found"));

    if (!authorizationService.isAuthorized(sess, form.getGroupId())) {
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
