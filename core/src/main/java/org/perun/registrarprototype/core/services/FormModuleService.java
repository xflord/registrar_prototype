package org.perun.registrarprototype.core.services;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.core.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.core.exceptions.FormModuleNotExistsException;
import org.perun.registrarprototype.core.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.core.mappers.ApplicationMapper;
import org.perun.registrarprototype.core.mappers.CurrentUserMapper;
import org.perun.registrarprototype.core.mappers.FormItemDataMapper;
import org.perun.registrarprototype.core.mappers.FormItemMapper;
import org.perun.registrarprototype.core.mappers.FormMapper;
import org.perun.registrarprototype.core.models.Application;
import org.perun.registrarprototype.core.models.AssignedFormModule;
import org.perun.registrarprototype.core.models.Form;
import org.perun.registrarprototype.core.models.FormItemData;
import org.perun.registrarprototype.core.repositories.FormModuleRepository;
import org.perun.registrarprototype.core.security.CurrentUser;
import org.perun.registrarprototype.core.services.modules.FormModuleLoader;
import org.perun.registrarprototype.extension.dto.FormType;
import org.perun.registrarprototype.extension.services.modules.FormModule;
import org.springframework.stereotype.Service;

@Service
public class FormModuleService {

  private final FormModuleRepository formModuleRepository;
  private final FormModuleLoader formModuleLoader;
  private final ApplicationMapper applicationMapper;
  private final CurrentUserMapper currentUserMapper;
  private final FormItemMapper formItemMapper;
  private final FormItemDataMapper formItemDataMapper;
  private final FormMapper formMapper;

  public FormModuleService(FormModuleRepository formModuleRepository, FormModuleLoader formModuleLoader,
                           ApplicationMapper applicationMapper, CurrentUserMapper currentUserMapper,
                           FormItemMapper formItemMapper, FormItemDataMapper formItemDataMapper,
                           FormMapper formMapper) {
    this.formModuleRepository = formModuleRepository;
    this.formModuleLoader = formModuleLoader;
    this.applicationMapper = applicationMapper;
    this.currentUserMapper = currentUserMapper;
    this.formItemMapper = formItemMapper;
    this.formItemDataMapper = formItemDataMapper;
    this.formMapper = formMapper;
  }

  public void canBeSubmitted(Form form, CurrentUser sess, FormType type) {
    List<AssignedFormModule> assignedModules = getAssignedFormModules(form);

    assignedModules.forEach(module ->
                                module.getFormModule().canBeSubmitted(currentUserMapper.toDto(sess),
                                    type, module.getOptions()));
  }

 public  void afterFormItemsPrefilled(Form form, CurrentUser sess, FormType type, List<FormItemData> prefilledData) {

  }

  public void afterApplicationSubmitted(Form form, Application application) {

  }

  public void beforeApproval(Form form, Application application) {

  }

  public void onApproval(Form form, Application application) {

  }

  public void onRejection(Form form, Application application) {

  }

  public FormModule getModule(String moduleName) {
    return formModuleLoader.getModule(moduleName);
  }

  /**
   * Sets modules for the form. Checks whether the module actually exists and whether all the required options are set.
   *
   * @param modulesToAssign modules with module name and options set (the rest is ignored)
   * @return
   */
  public List<AssignedFormModule> setModules(CurrentUser sess, Form form, List<AssignedFormModule> modulesToAssign)
      throws InsufficientRightsException {

    List<AssignedFormModule> modules = new ArrayList<>();

    for (AssignedFormModule module : modulesToAssign) {
      module.setFormModule(formModuleLoader.getModule(module.getModuleName()));
      if (module.getFormModule() == null) {
        throw new IllegalArgumentException("Module " + module.getModuleName() + " not found");
      }
      if (!module.getFormModule().getRequiredOptions().isEmpty()) {
        if (module.getOptions() == null || module.getOptions().isEmpty()) {
          throw new IllegalArgumentException("Module " + module.getModuleName() + " requires options.");
        }
        for (String requiredOption : module.getFormModule().getRequiredOptions()) {
          if (!module.getOptions().containsKey(requiredOption)) {
            throw new IllegalArgumentException("Module " + module.getModuleName() + " requires option " + requiredOption);
          }
        }
      }
      module.setFormId(form.getId());
      modules.add(module);
    }

    formModuleRepository.saveAll(modules);
    return modules;
  }

  private List<AssignedFormModule> getAssignedFormModules(Form form) {
    List<AssignedFormModule> modules = formModuleRepository.findAllByFormId(form.getId());

    for (AssignedFormModule assignedFormModule : modules) {
      FormModule module = formModuleLoader.getModule(assignedFormModule.getModuleName());
      if (module == null) {
        throw new DataInconsistencyException(
            "Already assigned module class " + assignedFormModule.getModuleName() + " not found" +
                " when retrieving modules for form " + form.getId());
      }
      assignedFormModule.setFormModule(module);
    }
    return modules;
  }
}
