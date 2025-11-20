package org.perun.registrarprototype.persistance;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormSpecification;

public interface FormRepository {
  Optional<FormSpecification> findById(int formId);

  // dependent on whether we want more forms per group (not meant by EXTENSION/INITIAL)
  Optional<FormSpecification> findByGroupId(String groupId);

  FormSpecification save(FormSpecification formSpecification);

  FormSpecification update(FormSpecification formSpecification);
  void delete(FormSpecification formSpecification);
  List<FormSpecification> findAll();
}
