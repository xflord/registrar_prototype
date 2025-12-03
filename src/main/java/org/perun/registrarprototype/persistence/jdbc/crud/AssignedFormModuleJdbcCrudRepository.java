package org.perun.registrarprototype.persistence.jdbc.crud;

import java.util.List;
import org.perun.registrarprototype.persistence.jdbc.entities.AssignedFormModuleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignedFormModuleJdbcCrudRepository extends CrudRepository<AssignedFormModuleEntity, Integer> {
  List<AssignedFormModuleEntity> findByFormIdOrderByPosition(int formId);
}