package org.perun.registrarprototype.persistance.jdbc;

import java.util.List;
import org.perun.registrarprototype.persistance.jdbc.entities.DestinationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationJdbcCrudRepository extends CrudRepository<DestinationEntity, Integer> {
  List<DestinationEntity> findByFormSpecificationId(Integer formSpecificationId);
  List<DestinationEntity> findByGlobalTrue();
}


