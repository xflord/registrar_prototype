package org.perun.registrarprototype.persistence.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.persistence.ApplicationRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.ApplicationJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.FormItemDataJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.FormItemJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.entities.ApplicationEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.FormItemDataEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.FormItemEntity;
import org.perun.registrarprototype.persistence.jdbc.mappers.DomainMapper;
import org.perun.registrarprototype.persistence.jdbc.mappers.EntityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class ApplicationRepositoryJdbc implements ApplicationRepository {

  private final ApplicationJdbcCrudRepository jdbcRepository;
  private final FormItemDataJdbcCrudRepository formItemDataJdbcRepository;
  private final FormItemJdbcCrudRepository formItemJdbcRepository;

  public ApplicationRepositoryJdbc(
      ApplicationJdbcCrudRepository jdbcRepository,
      FormItemDataJdbcCrudRepository formItemDataJdbcRepository,
      FormItemJdbcCrudRepository formItemJdbcRepository) {
    this.jdbcRepository = jdbcRepository;
    this.formItemDataJdbcRepository = formItemDataJdbcRepository;
    this.formItemJdbcRepository = formItemJdbcRepository;
  }

  @Override
  public Application save(Application application) {
    ApplicationEntity entity = EntityMapper.toEntity(application);
    ApplicationEntity saved = jdbcRepository.save(entity);
    
    // Delete existing form item data for this application
    formItemDataJdbcRepository.deleteByApplicationId(saved.getId());
    
    // Save form item data
    if (application.getFormItemData() != null) {
      for (FormItemData formItemData : application.getFormItemData()) {
        FormItemDataEntity formItemDataEntity = EntityMapper.toFormItemDataEntity(saved.getId(), formItemData);
        formItemDataJdbcRepository.save(formItemDataEntity);
      }
    }
    
    return DomainMapper.toDomain(saved);
  }

  @Override
  public List<Application> updateAll(List<Application> applications) {
    List<ApplicationEntity> entities = applications.stream()
        .map(EntityMapper::toEntity)
        .collect(Collectors.toList());
    Iterable<ApplicationEntity> saved = jdbcRepository.saveAll(entities);
    return StreamSupport.stream(saved.spliterator(), false)
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Application> findById(int id) {
    Optional<ApplicationEntity> entityOpt = jdbcRepository.findById(id);
    if (entityOpt.isPresent()) {
      ApplicationEntity entity = entityOpt.get();
      Application application = DomainMapper.toDomain(entity);

      List<FormItemDataEntity> formItemDataEntities = formItemDataJdbcRepository.findByApplicationId(id);
      List<Integer> itemIds = formItemDataEntities.stream().map(FormItemDataEntity::getFormItemId).toList();
      Map<Integer, FormItemEntity> itemEntityMap = new HashMap<>();
      formItemJdbcRepository.findAllById(itemIds).forEach(item -> itemEntityMap.put(item.getId(), item));
      List<FormItemData> formData = new ArrayList<>();
      for (FormItemDataEntity dataEntity : formItemDataEntities) {
        FormItemEntity formItemEntity = itemEntityMap.get(dataEntity.getFormItemId());
        if (formItemEntity == null) {
          throw new DataIntegrityViolationException("form item not found for form item data " + dataEntity.getId());
        }
        formData.add(DomainMapper.toDomain(dataEntity, DomainMapper.toDomain(formItemEntity)));
      }
      application.setFormItemData(formData);
      
      return Optional.of(application);
    }
    return Optional.empty();
  }

  @Override
  public List<Application> findByFormId(int formId) {
    return jdbcRepository.findByFormSpecificationId(formId).stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Application> findAll() {
    Iterable<ApplicationEntity> entities = jdbcRepository.findAll();
    return StreamSupport.stream(entities.spliterator(), false)
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Application> findOpenApplicationsByItemDefinitionId(Integer itemDefinitionId) {
    List<ApplicationEntity> entities = jdbcRepository.findOpenApplicationsByItemDefinitionId(itemDefinitionId);
    return entities.stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }


}