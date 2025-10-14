package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.repositories.FormModuleRepository;
import org.springframework.stereotype.Component;

@Component
public class FormModuleRepositoryDummy implements FormModuleRepository {
  private final static List<AssignedFormModule> modules = new ArrayList<>();

  @Override
  public List<AssignedFormModule> findAllByFormId(int formId) {
    return modules.stream().filter(module -> module.getFormId() == formId).toList();
  }

  @Override
  public void saveAll(List<AssignedFormModule> modulesToSave) {
    for (AssignedFormModule module : modulesToSave) {
      // Remove existing module with same formId and moduleName if present
      modules.removeIf(existing -> 
          existing.getFormId() == module.getFormId() && 
          existing.getModuleName().equals(module.getModuleName())
      );
      modules.add(module);
      System.out.println("Saved/Updated module " + module.getModuleName() + " for form " + module.getFormId());
    }
  }


  public void reset() {
    modules.clear();
  }
}
