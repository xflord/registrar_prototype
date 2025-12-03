package org.perun.registrarprototype.persistence.jdbc;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.html.parser.Entity;
import org.perun.registrarprototype.models.Decision;
import org.perun.registrarprototype.persistence.DecisionRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.DecisionJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.entities.DecisionEntity;
import org.perun.registrarprototype.persistence.jdbc.mappers.DomainMapper;
import org.perun.registrarprototype.persistence.jdbc.mappers.EntityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class DecisionRepositoryJdbc implements DecisionRepository {

  private final DecisionJdbcCrudRepository jdbcRepository;

  public DecisionRepositoryJdbc(
      DecisionJdbcCrudRepository jdbcRepository) {
    this.jdbcRepository = jdbcRepository;
  }

  @Override
  public Decision save(Decision decision) {
    DecisionEntity entity = EntityMapper.toEntity(decision);
    DecisionEntity saved = jdbcRepository.save(entity);
    return DomainMapper.toDomain(saved);
  }

  @Override
  public List<Decision> findByApplicationId(int applicationId) {
    List<DecisionEntity> entities = jdbcRepository.findByApplicationId(applicationId);
    List<Decision> decisions = new ArrayList<>();
    for (DecisionEntity entity : entities) {
      decisions.add(DomainMapper.toDomain(entity));
    }
    return decisions;
  }
}