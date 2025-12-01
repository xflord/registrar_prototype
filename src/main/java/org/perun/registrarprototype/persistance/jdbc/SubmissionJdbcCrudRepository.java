package org.perun.registrarprototype.persistance.jdbc;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.persistance.jdbc.entities.SubmissionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionJdbcCrudRepository extends CrudRepository<SubmissionEntity, Integer> {

  List<SubmissionEntity> findByIdentityIdentifierAndIdentityIssuer(String identifier, String issuer);

  Optional<SubmissionEntity> findById(int id);
}