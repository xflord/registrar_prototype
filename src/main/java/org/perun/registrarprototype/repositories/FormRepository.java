package org.perun.registrarprototype.repositories;

import java.util.Optional;
import org.perun.registrarprototype.models.Form;

public interface FormRepository {
  Optional<Form> findById(int formId);

  int getNextId();
  void save(Form form);
}
