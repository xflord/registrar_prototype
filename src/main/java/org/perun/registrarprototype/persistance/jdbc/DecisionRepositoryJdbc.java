package org.perun.registrarprototype.persistance.jdbc;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.Decision;
import org.perun.registrarprototype.persistance.DecisionRepository;
import org.perun.registrarprototype.persistance.jdbc.entities.DecisionEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class DecisionRepositoryJdbc implements DecisionRepository {

  private final DecisionJdbcCrudRepository jdbcRepository;
  private final SimpleDomainMapper simpleDomainMapper;

  public DecisionRepositoryJdbc(
      DecisionJdbcCrudRepository jdbcRepository,
      SimpleDomainMapper simpleDomainMapper) {
    this.jdbcRepository = jdbcRepository;
    this.simpleDomainMapper = simpleDomainMapper;
  }

  @Override
  public Decision save(Decision decision) {
    DecisionEntity entity = toEntity(decision);
    DecisionEntity saved = jdbcRepository.save(entity);
    return simpleDomainMapper.toDomain(saved);
  }

  @Override
  public List<Decision> findByApplicationId(int applicationId) {
    List<DecisionEntity> entities = jdbcRepository.findByApplicationId(applicationId);
    List<Decision> decisions = new ArrayList<>();
    for (DecisionEntity entity : entities) {
      decisions.add(simpleDomainMapper.toDomain(entity));
    }
    return decisions;
  }

  private DecisionEntity toEntity(Decision domain) {
    DecisionEntity entity = new DecisionEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setApplicationId(domain.getApplicationId());
    entity.setApproverId(domain.getApproverId());
    entity.setApproverName(domain.getApproverName());
    entity.setMessage(domain.getMessage());
    entity.setTimestamp(domain.getTimestamp());
    entity.setDecisionType(domain.getDecisionType() != null ? domain.getDecisionType().name() : null);
    return entity;
  }
}