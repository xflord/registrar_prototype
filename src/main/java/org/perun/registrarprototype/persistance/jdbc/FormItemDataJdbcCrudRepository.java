package org.perun.registrarprototype.persistance.jdbc;

import java.util.List;
import org.perun.registrarprototype.persistance.jdbc.entities.FormItemDataEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormItemDataJdbcCrudRepository extends CrudRepository<FormItemDataEntity, Integer> {
  
  List<FormItemDataEntity> findByApplicationId(Integer applicationId);
  
  void deleteByApplicationId(Integer applicationId);
}