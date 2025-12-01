package org.perun.registrarprototype.persistance.jdbc;

import java.util.List;
import org.perun.registrarprototype.persistance.jdbc.entities.AssignedFormModuleEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignedFormModuleJdbcCrudRepository extends CrudRepository<AssignedFormModuleEntity, Integer> {
  List<AssignedFormModuleEntity> findByFormIdOrderByPosition(int formId);
}