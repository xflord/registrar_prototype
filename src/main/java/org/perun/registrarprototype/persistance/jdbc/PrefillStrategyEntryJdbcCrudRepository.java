package org.perun.registrarprototype.persistance.jdbc;

import java.util.List;
import org.perun.registrarprototype.persistance.jdbc.entities.PrefillStrategyEntryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrefillStrategyEntryJdbcCrudRepository extends CrudRepository<PrefillStrategyEntryEntity, Integer> {
  List<PrefillStrategyEntryEntity> findByGlobalTrue();
  List<PrefillStrategyEntryEntity> findByFormSpecificationId(Integer formSpecificationId);
  // TODO if we want to include options in the check, a custom SQL query will be required
  List<PrefillStrategyEntryEntity> findByFormSpecificationIdAndTypeAndSourceAttributeAndGlobal(
      Integer formSpecificationId,
      String type,
      String sourceAttribute,
      Boolean global);
}


