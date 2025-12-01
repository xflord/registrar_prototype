package org.perun.registrarprototype.persistance.jdbc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.persistance.DestinationRepository;
import org.perun.registrarprototype.persistance.jdbc.entities.DestinationEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class DestinationRepositoryJdbc implements DestinationRepository {

  private final DestinationJdbcCrudRepository jdbcRepository;
  private final SimpleDomainMapper simpleDomainMapper;

  public DestinationRepositoryJdbc(
      DestinationJdbcCrudRepository jdbcRepository,
      SimpleDomainMapper simpleDomainMapper) {
    this.jdbcRepository = jdbcRepository;
    this.simpleDomainMapper = simpleDomainMapper;
  }

  @Override
  public Optional<Destination> findById(int id) {
    return jdbcRepository.findById(id)
        .map(simpleDomainMapper::toDomain);
  }

  @Override
  public List<Destination> getDestinationsForForm(FormSpecification formSpecification) {
    return jdbcRepository.findByFormSpecificationId(formSpecification.getId()).stream()
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Destination> getGlobalDestinations() {
    return jdbcRepository.findByGlobalTrue().stream()
        .map(simpleDomainMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Destination createDestination(Destination destination) {
    DestinationEntity entity = toEntity(destination);
    DestinationEntity saved = jdbcRepository.save(entity);
    return simpleDomainMapper.toDomain(saved);
  }

  @Override
  public void removeDestination(Destination destination) {
    jdbcRepository.deleteById(destination.getId());
  }

  @Override
  public void saveAll(List<Destination> destinations) {
    List<DestinationEntity> entities = destinations.stream()
        .map(this::toEntity)
        .collect(Collectors.toList());
    jdbcRepository.saveAll(entities);
  }

  @Override
  public boolean exists(Destination destination) {
    if (destination.isGlobal()) {
      return jdbcRepository.findByGlobalTrue().stream()
          .anyMatch(d -> d.getUrn().equals(destination.getUrn()));
    } else {
      return jdbcRepository.findByFormSpecificationId(
          destination.getFormSpecificationId()).stream()
          .anyMatch(d -> d.getUrn().equals(destination.getUrn()));
    }
  }

  private DestinationEntity toEntity(Destination domain) {
    DestinationEntity entity = new DestinationEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setUrn(domain.getUrn());
    entity.setFormSpecificationId(domain.getFormSpecificationId());
    entity.setGlobal(domain.isGlobal());
    return entity;
  }
}

