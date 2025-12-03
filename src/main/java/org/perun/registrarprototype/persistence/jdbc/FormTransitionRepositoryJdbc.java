package org.perun.registrarprototype.persistence.jdbc;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.persistence.FormTransitionRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.FormTransitionJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.entities.FormTransitionEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.SourceStateRef;
import org.perun.registrarprototype.persistence.jdbc.mappers.DomainMapper;
import org.perun.registrarprototype.persistence.jdbc.mappers.EntityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class FormTransitionRepositoryJdbc implements FormTransitionRepository {

  private final FormTransitionJdbcCrudRepository jdbcRepository;

  public FormTransitionRepositoryJdbc(
      FormTransitionJdbcCrudRepository jdbcRepository) {
    this.jdbcRepository = jdbcRepository;
  }

  @Override
  public FormTransition save(FormTransition formTransition) {
    // TODO how to handle transitions here? bulk save only/custom query(?) required (can it be done using jdbc)
    FormTransitionEntity entity = EntityMapper.toEntity(formTransition);
    FormTransitionEntity saved = jdbcRepository.save(entity);
    return DomainMapper.toDomain(saved);
  }

  @Override
  public List<FormTransition> getAllBySourceFormAndType(FormSpecification formSpecification, FormTransition.TransitionType type) {
    List<FormTransitionEntity> entities = jdbcRepository.findBySourceFormSpecificationIdAndTransitionTypeOrderByPosition(formSpecification.getId(), type.name());
    List<FormTransition> transitions = new ArrayList<>();
    for (FormTransitionEntity entity : entities) {
      transitions.add(DomainMapper.toDomain(entity));
    }
    return transitions;
  }

  @Override
  public void remove(FormTransition formTransition) {
    if (formTransition.getId() > 0) {
      jdbcRepository.deleteById(formTransition.getId());
    }
  }
}