package org.perun.registrarprototype.persistance.jdbc;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.persistance.FormTransitionRepository;
import org.perun.registrarprototype.persistance.jdbc.entities.FormTransitionEntity;
import org.perun.registrarprototype.persistance.jdbc.entities.SourceStateRef;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class FormTransitionRepositoryJdbc implements FormTransitionRepository {

  private final FormTransitionJdbcCrudRepository jdbcRepository;
  private final SimpleDomainMapper simpleDomainMapper;

  public FormTransitionRepositoryJdbc(
      FormTransitionJdbcCrudRepository jdbcRepository,
      SimpleDomainMapper simpleDomainMapper) {
    this.jdbcRepository = jdbcRepository;
    this.simpleDomainMapper = simpleDomainMapper;
  }

  @Override
  public FormTransition save(FormTransition formTransition) {
    FormTransitionEntity entity = toEntity(formTransition);
    FormTransitionEntity saved = jdbcRepository.save(entity);
    return simpleDomainMapper.toDomain(saved);
  }

  @Override
  public List<FormTransition> getAllBySourceFormAndType(FormSpecification formSpecification, FormTransition.TransitionType type) {
    List<FormTransitionEntity> entities = jdbcRepository.findBySourceFormSpecificationIdAndTransitionTypeOrderByPosition(formSpecification.getId(), type.name());
    List<FormTransition> transitions = new ArrayList<>();
    for (FormTransitionEntity entity : entities) {
      transitions.add(simpleDomainMapper.toDomain(entity));
    }
    return transitions;
  }

  @Override
  public void remove(FormTransition formTransition) {
    if (formTransition.getId() > 0) {
      jdbcRepository.deleteById(formTransition.getId());
    }
  }

  private FormTransitionEntity toEntity(FormTransition domain) {
    FormTransitionEntity entity = new FormTransitionEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    if (domain.getSourceFormSpecification() != null) {
      entity.setSourceFormSpecificationId(domain.getSourceFormSpecification().getId());
    }
    if (domain.getTargetFormSpecification() != null) {
      entity.setTargetFormSpecificationId(domain.getTargetFormSpecification().getId());
    }
    // Set position to 0 as default, you might want to handle this differently
    entity.setPosition(0);
    entity.setTargetFormState(domain.getTargetFormState());
    entity.setTransitionType(domain.getType());
    
    // Convert source states to SourceStateRef entities
    List<SourceStateRef> sourceStateRefs = new ArrayList<>();
    if (domain.getSourceFormStates() != null) {
      for (int i = 0; i < domain.getSourceFormStates().size(); i++) {
        sourceStateRefs.add(new SourceStateRef(null, domain.getSourceFormStates().get(i).name()));
      }
    }
    entity.setSourceStates(sourceStateRefs);
    
    return entity;
  }
}