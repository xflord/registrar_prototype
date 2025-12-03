package org.perun.registrarprototype.persistence.jdbc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.persistence.DestinationRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.DestinationJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.entities.DestinationEntity;
import org.perun.registrarprototype.persistence.jdbc.mappers.DomainMapper;
import org.perun.registrarprototype.persistence.jdbc.mappers.EntityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class DestinationRepositoryJdbc implements DestinationRepository {

  private final DestinationJdbcCrudRepository jdbcRepository;

  public DestinationRepositoryJdbc(
      DestinationJdbcCrudRepository jdbcRepository) {
    this.jdbcRepository = jdbcRepository;
  }

  @Override
  public Optional<Destination> findById(int id) {
    return jdbcRepository.findById(id)
        .map(DomainMapper::toDomain);
  }

  @Override
  public List<Destination> getDestinationsForForm(FormSpecification formSpecification) {
    return jdbcRepository.findByFormSpecificationId(formSpecification.getId()).stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Destination> getGlobalDestinations() {
    return jdbcRepository.findByGlobalTrue().stream()
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Destination createDestination(Destination destination) {
    DestinationEntity entity = EntityMapper.toEntity(destination);
    DestinationEntity saved = jdbcRepository.save(entity);
    return DomainMapper.toDomain(saved);
  }

  @Override
  public void removeDestination(Destination destination) {
    jdbcRepository.deleteById(destination.getId());
  }

  @Override
  public void saveAll(List<Destination> destinations) {
    List<DestinationEntity> entities = destinations.stream()
        .map(EntityMapper::toEntity)
        .collect(Collectors.toList());
    jdbcRepository.saveAll(entities);
  }

  @Override
  public Optional<Destination> exists(Destination destination) {
    List<DestinationEntity> found = jdbcRepository.findByFormSpecificationIdAndUrnAndGlobal(
        destination.getFormSpecificationId(), destination.getUrn(), destination.isGlobal()
    );
    return found.isEmpty() ? Optional.empty() : Optional.of(DomainMapper.toDomain(found.getFirst()));
  }
}

