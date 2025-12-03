package org.perun.registrarprototype.persistence;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormSpecification;

public interface FormSpecificationRepository {
  Optional<FormSpecification> findById(int formId);
  FormSpecification save(FormSpecification formSpecification);
  Optional<FormSpecification> findByGroupId(String groupId);
  List<FormSpecification> findByVoId(String voId);
  void delete(FormSpecification formSpecification);
  List<FormSpecification> findAll();
}
