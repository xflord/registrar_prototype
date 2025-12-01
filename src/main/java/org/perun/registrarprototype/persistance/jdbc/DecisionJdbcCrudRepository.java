package org.perun.registrarprototype.persistance.jdbc;

import java.util.List;
import org.perun.registrarprototype.persistance.jdbc.entities.DecisionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DecisionJdbcCrudRepository extends CrudRepository<DecisionEntity, Integer> {
  List<DecisionEntity> findByApplicationId(Integer applicationId);
}