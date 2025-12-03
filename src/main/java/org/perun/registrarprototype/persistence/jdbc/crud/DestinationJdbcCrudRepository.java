package org.perun.registrarprototype.persistence.jdbc.crud;

import java.util.List;
import org.perun.registrarprototype.persistence.jdbc.entities.DestinationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationJdbcCrudRepository extends CrudRepository<DestinationEntity, Integer> {
  List<DestinationEntity> findByFormSpecificationId(Integer formSpecificationId);
  List<DestinationEntity> findByGlobalTrue();
  List<DestinationEntity> findByFormSpecificationIdAndUrnAndGlobal(Integer formSpecificationId, String urn, boolean global);
}


