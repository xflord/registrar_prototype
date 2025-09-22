package org.perun.registrarprototype.repositories.tempImpl;

import java.util.List;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.repositories.FormModuleRepository;
import org.springframework.stereotype.Component;

@Component
public class FormModuleRepositoryDummy implements FormModuleRepository {
  @Override
  public List<AssignedFormModule> findAllByFormId(int formId) {
    return List.of();
  }
}
