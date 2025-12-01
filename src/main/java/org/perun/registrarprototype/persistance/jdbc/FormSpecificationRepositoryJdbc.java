package org.perun.registrarprototype.persistance.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.persistance.FormRepository;
import org.perun.registrarprototype.persistance.jdbc.entities.FormSpecificationEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class FormSpecificationRepositoryJdbc implements FormRepository {

  private final FormSpecificationJdbcCrudRepository jdbcRepository;
  private final SimpleDomainMapper simpleDomainMapper;

  public FormSpecificationRepositoryJdbc(
      FormSpecificationJdbcCrudRepository jdbcRepository,
      SimpleDomainMapper simpleDomainMapper) {
    this.jdbcRepository = jdbcRepository;
    this.simpleDomainMapper = simpleDomainMapper;
  }

  @Override
  public Optional<FormSpecification> findById(int formId) {
    Optional<FormSpecificationEntity> entityOpt = jdbcRepository.findById(formId);
    if (entityOpt.isPresent()) {
      FormSpecificationEntity entity = entityOpt.get();
      FormSpecification formSpecification = simpleDomainMapper.toDomain(entity);
      return Optional.of(formSpecification);
    }
    return Optional.empty();
  }

  @Override
  public Optional<FormSpecification> findByGroupId(String groupId) {
    Optional<FormSpecificationEntity> entityOpt = jdbcRepository.findByGroupId(groupId);
    if (entityOpt.isPresent()) {
      FormSpecificationEntity entity = entityOpt.get();
      FormSpecification formSpecification = simpleDomainMapper.toDomain(entity);
      return Optional.of(formSpecification);
    }
    return Optional.empty();
  }

  @Override
  public FormSpecification save(FormSpecification formSpecification) {
    FormSpecificationEntity entity = toEntity(formSpecification);
    FormSpecificationEntity savedEntity = jdbcRepository.save(entity);
    
    // Reload the complete aggregate
    return findById(savedEntity.getId()).orElse(null);
  }

  @Override
  public FormSpecification update(FormSpecification formSpecification) {
    return save(formSpecification);
  }

  @Override
  public void delete(FormSpecification formSpecification) {
    jdbcRepository.deleteById(formSpecification.getId());
  }

  @Override
  public List<FormSpecification> findAll() {
    List<FormSpecification> result = new ArrayList<>();
    for (FormSpecificationEntity entity : jdbcRepository.findAll()) {
      FormSpecification formSpecification = simpleDomainMapper.toDomain(entity);
      result.add(formSpecification);
    }
    return result;
  }

  private FormSpecificationEntity toEntity(FormSpecification form) {
    FormSpecificationEntity entity = new FormSpecificationEntity();
    if (form.getId() > 0) {
      entity.setId(form.getId());
    }
    entity.setVoId(form.getVoId());
    entity.setGroupId(form.getGroupId());
    entity.setAutoApprove(form.isAutoApprove());
    entity.setAutoApproveExtension(form.isAutoApproveExtension());
    return entity;
  }
}

