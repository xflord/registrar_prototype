package org.perun.registrarprototype.persistence.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.persistence.FormSpecificationRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.FormSpecificationJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.entities.FormSpecificationEntity;
import org.perun.registrarprototype.persistence.jdbc.mappers.DomainMapper;
import org.perun.registrarprototype.persistence.jdbc.mappers.EntityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class FormSpecificationRepositoryJdbc implements FormSpecificationRepository {

  private final FormSpecificationJdbcCrudRepository jdbcRepository;

  public FormSpecificationRepositoryJdbc(
      FormSpecificationJdbcCrudRepository jdbcRepository) {
    this.jdbcRepository = jdbcRepository;
  }

  @Override
  public Optional<FormSpecification> findById(int formId) {
    Optional<FormSpecificationEntity> entityOpt = jdbcRepository.findById(formId);
    if (entityOpt.isPresent()) {
      FormSpecificationEntity entity = entityOpt.get();
      FormSpecification formSpecification = DomainMapper.toDomain(entity);
      return Optional.of(formSpecification);
    }
    return Optional.empty();
  }

  @Override
  public Optional<FormSpecification> findByGroupId(String groupId) {
    Optional<FormSpecificationEntity> entityOpt = jdbcRepository.findByGroupId(groupId);
    if (entityOpt.isPresent()) {
      FormSpecificationEntity entity = entityOpt.get();
      FormSpecification formSpecification = DomainMapper.toDomain(entity);
      return Optional.of(formSpecification);
    }
    return Optional.empty();
  }

  @Override
  public List<FormSpecification> findByVoId(String voId) {
    return List.of();
  }

  @Override
  public FormSpecification save(FormSpecification formSpecification) {
    FormSpecificationEntity entity = EntityMapper.toEntity(formSpecification);
    FormSpecificationEntity savedEntity = jdbcRepository.save(entity);
    
    // Reload the complete aggregate
    return findById(savedEntity.getId()).orElse(null);
  }


  @Override
  public void delete(FormSpecification formSpecification) {
    jdbcRepository.deleteById(formSpecification.getId());
  }

  @Override
  public List<FormSpecification> findAll() {
    List<FormSpecification> result = new ArrayList<>();
    for (FormSpecificationEntity entity : jdbcRepository.findAll()) {
      FormSpecification formSpecification = DomainMapper.toDomain(entity);
      result.add(formSpecification);
    }
    return result;
  }
}

