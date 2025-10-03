package org.perun.registrarprototype.core.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.core.models.AssignedFormModule;
import org.perun.registrarprototype.core.repositories.FormModuleRepository;
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
    modules.addAll(modulesToSave);
  }


  public void reset() {
    modules.clear();
  }
}
