package org.perun.registrarprototype.persistence.jdbc.crud;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.persistence.jdbc.entities.SubmissionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionJdbcCrudRepository extends CrudRepository<SubmissionEntity, Integer> {

  List<SubmissionEntity> findByIdentityIdentifierAndIdentityIssuer(String identifier, String issuer);

  Optional<SubmissionEntity> findById(int id);
}