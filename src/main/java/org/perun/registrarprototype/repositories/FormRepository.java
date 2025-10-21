package org.perun.registrarprototype.repositories;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormSpecification;

public interface FormRepository {
  Optional<FormSpecification> findById(int formId);

  // dependent on whether we want more forms per group (not meant by EXTENSION/INITIAL)
  Optional<FormSpecification> findByGroupId(int groupId);

  int getNextId();
  FormSpecification save(FormSpecification formSpecification);

  FormSpecification update(FormSpecification formSpecification);
  List<FormSpecification> findAll();
}
