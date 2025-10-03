package org.perun.registrarprototype.core.repositories;

import java.util.List;
import org.perun.registrarprototype.core.models.AssignedFormModule;

public interface FormModuleRepository {
  /**
   * Retrieve all form modules assigned to a form.
   * @return
   */
  List<AssignedFormModule> findAllByFormId(int formId);

  void saveAll(List<AssignedFormModule> modules);
}
