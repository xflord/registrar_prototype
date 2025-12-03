package org.perun.registrarprototype.persistence.jdbc.crud;

import java.util.List;
import org.perun.registrarprototype.persistence.jdbc.entities.FormTransitionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormTransitionJdbcCrudRepository extends CrudRepository<FormTransitionEntity, Integer> {
  List<FormTransitionEntity> findBySourceFormSpecificationIdAndTransitionTypeOrderByPosition(int formSpecificationId, String type);
}