package org.perun.registrarprototype.repositories;

import java.util.Optional;
import org.perun.registrarprototype.models.Form;

public interface FormRepository {
  Optional<Form> findById(int formId);

  // dependent on whether we want more forms per group (not meant by EXTENSION/INITIAL)
  Optional<Form> findByGroupId(int groupId);

  int getNextId();
  Form save(Form form);

  Form update(Form form);
}
