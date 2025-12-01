package org.perun.registrarprototype.persistance.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.persistance.ApplicationRepository;
import org.perun.registrarprototype.persistance.jdbc.entities.ApplicationEntity;
import org.perun.registrarprototype.persistance.jdbc.entities.FormItemDataEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class ApplicationRepositoryJdbc implements ApplicationRepository {

  private final ApplicationJdbcCrudRepository jdbcRepository;
  private final FormItemDataJdbcCrudRepository formItemDataJdbcRepository;
  private final FormItemJdbcCrudRepository formItemJdbcRepository;
  private final SimpleDomainMapper simpleDomainMapper;

  public ApplicationRepositoryJdbc(
      ApplicationJdbcCrudRepository jdbcRepository,
      FormItemDataJdbcCrudRepository formItemDataJdbcRepository,
      FormItemJdbcCrudRepository formItemJdbcRepository,
      SimpleDomainMapper simpleDomainMapper) {
    this.jdbcRepository = jdbcRepository;
    this.formItemDataJdbcRepository = formItemDataJdbcRepository;
    this.formItemJdbcRepository = formItemJdbcRepository;
    this.simpleDomainMapper = simpleDomainMapper;
  }

  @Override
  public Application save(Application application) {
    ApplicationEntity entity = toEntity(application);
    ApplicationEntity saved = jdbcRepository.save(entity);
    
    // Delete existing form item data for this application
    formItemDataJdbcRepository.deleteByApplicationId(saved.getId());
    
    // Save form item data
    if (application.getFormItemData() != null) {
      for (FormItemData formItemData : application.getFormItemData()) {
        FormItemDataEntity formItemDataEntity = toFormItemDataEntity(saved.getId(), formItemData);
        formItemDataJdbcRepository.save(formItemDataEntity);
      }
    }
    
    return simpleDomainMapper.toDomain(saved);
  }

  @Override
  public List<Application> updateAll(List<Application> applications) {
    List<ApplicationEntity> entities = applications.stream()
        .map(this::toEntity)
        .collect(Collectors.toList());
    Iterable<ApplicationEntity> saved = jdbcRepository.saveAll(entities);
    return StreamSupport.stream(saved.spliterator(), false)
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Application> findById(int id) {
    Optional<ApplicationEntity> entityOpt = jdbcRepository.findById(id);
    if (entityOpt.isPresent()) {
      ApplicationEntity entity = entityOpt.get();
      Application application = simpleDomainMapper.toDomain(entity);
      
      // Load form item data for this application
      List<FormItemDataEntity> formItemDataEntities = formItemDataJdbcRepository.findByApplicationId(id);
      // Load FormItem objects and map FormItemData properly
      List<FormItemData> formData = new ArrayList<>();
      for (FormItemDataEntity dataEntity : formItemDataEntities) {
        // Load the FormItem for this FormItemData
        Optional<FormItem> formItemOpt = formItemJdbcRepository.findById(dataEntity.getFormItemId())
            .map(simpleDomainMapper::toDomain);
        if (formItemOpt.isPresent()) {
          FormItemData formDataItem = simpleDomainMapper.toDomain(dataEntity, formItemOpt.get());
          formData.add(formDataItem);
        }
      }
      application.setFormItemData(formData);
      
      return Optional.of(application);
    }
    return Optional.empty();
  }

  @Override
  public List<Application> findByFormId(int formId) {
    return jdbcRepository.findByFormSpecificationId(formId).stream()
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Application> findAll() {
    Iterable<ApplicationEntity> entities = jdbcRepository.findAll();
    return StreamSupport.stream(entities.spliterator(), false)
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public int getNextId() {
    // In a real implementation, you might want to get this from the database
    // For now, we'll use a simple approach
    return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
  }

  @Override
  public List<Application> findOpenApplicationsByItemDefinitionId(Integer itemDefinitionId) {
    List<ApplicationEntity> entities = jdbcRepository.findOpenApplicationsByItemDefinitionId(itemDefinitionId);
    return entities.stream()
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  private ApplicationEntity toEntity(Application domain) {
    ApplicationEntity entity = new ApplicationEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setIdmUserId(domain.getIdmUserId());
    entity.setFormSpecificationId(domain.getFormSpecificationId());
    entity.setState(domain.getState().name());
    entity.setType(domain.getType().toString());
    entity.setRedirectUrl(domain.getRedirectUrl());
    entity.setSubmissionId(domain.getSubmissionId());
    return entity;
  }
  
  private FormItemDataEntity toFormItemDataEntity(Integer applicationId, FormItemData formItemData) {
    FormItemDataEntity entity = new FormItemDataEntity();
    entity.setApplicationId(applicationId);
    entity.setFormItemId(formItemData.getFormItem().getId());
    entity.setValue(formItemData.getValue());
    entity.setPrefilledValue(formItemData.getPrefilledValue());
    entity.setIdentityAttributeValue(formItemData.getIdentityAttributeValue());
    entity.setIdmAttributeValue(formItemData.getIdmAttributeValue());
    entity.setValueAssured(formItemData.isValueAssured());
    return entity;
  }
}