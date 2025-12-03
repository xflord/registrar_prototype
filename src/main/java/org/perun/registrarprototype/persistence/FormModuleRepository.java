package org.perun.registrarprototype.persistence;

import java.util.List;
import org.perun.registrarprototype.models.AssignedFormModule;

public interface FormModuleRepository {
  /**
   * Retrieve all form modules assigned to a form.
   * @return
   */
  List<AssignedFormModule> findAllByFormId(int formId);

  void saveAll(List<AssignedFormModule> modules);
}
