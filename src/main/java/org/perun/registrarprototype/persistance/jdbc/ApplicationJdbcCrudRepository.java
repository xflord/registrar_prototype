package org.perun.registrarprototype.persistance.jdbc;

import java.util.List;
import org.perun.registrarprototype.persistance.jdbc.entities.ApplicationEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationJdbcCrudRepository extends CrudRepository<ApplicationEntity, Integer> {
  List<ApplicationEntity> findByFormSpecificationId(Integer formSpecificationId);

  List<ApplicationEntity> findBySubmissionId(Integer submissionId);

  
  @Query("SELECT DISTINCT a.* FROM application a " +
         "JOIN form_item fi ON a.form_id = fi.form_id " +
         "WHERE fi.item_definition_id = :itemDefinitionId " +
         "AND a.state IN ('SUBMITTED', 'CHANGES_REQUESTED', 'VERIFIED')")
  List<ApplicationEntity> findOpenApplicationsByItemDefinitionId(@Param("itemDefinitionId") Integer itemDefinitionId);
}