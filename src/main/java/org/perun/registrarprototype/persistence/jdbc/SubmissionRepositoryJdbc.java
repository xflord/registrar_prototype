package org.perun.registrarprototype.persistence.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.persistence.SubmissionRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.ApplicationJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.crud.SubmissionJdbcCrudRepository;
import org.perun.registrarprototype.persistence.jdbc.entities.SubmissionEntity;
import org.perun.registrarprototype.persistence.jdbc.mappers.DomainMapper;
import org.perun.registrarprototype.persistence.jdbc.mappers.EntityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class SubmissionRepositoryJdbc implements SubmissionRepository {

  private final SubmissionJdbcCrudRepository jdbcRepository;
  private final ApplicationJdbcCrudRepository applicationJdbcCrudRepository;

  public SubmissionRepositoryJdbc(
      SubmissionJdbcCrudRepository jdbcRepository,
      ApplicationJdbcCrudRepository applicationJdbcCrudRepository) {
    this.jdbcRepository = jdbcRepository;
    this.applicationJdbcCrudRepository = applicationJdbcCrudRepository;
  }

  @Override
  public Submission save(Submission submission) {
    SubmissionEntity entity = EntityMapper.toEntity(submission);
    SubmissionEntity saved = jdbcRepository.save(entity);
    return DomainMapper.toDomain(saved);
  }

  @Override
  public Optional<Submission> findById(int id) {
    Optional<SubmissionEntity> entity = jdbcRepository.findById(id);
    if (entity.isPresent()) {
      Submission submission = DomainMapper.toDomain(entity.get());
      // Load applications for this submission
      List<Application> applications = applicationJdbcCrudRepository.findBySubmissionId(id).stream()
          .map(DomainMapper::toDomain)
          .collect(Collectors.toList());
      submission.setApplications(applications);
      return Optional.of(submission);
    }
    return Optional.empty();
  }

  @Override
  public List<Submission> updateAll(List<Submission> submissions) {
    List<Submission> result = new ArrayList<>();
    for (Submission submission : submissions) {
      result.add(save(submission));
    }
    return result;
  }

  @Override
  public List<Submission> findAllByIdentifierAndIssuer(String identifier, String issuer) {
    List<SubmissionEntity> entities = jdbcRepository.findByIdentityIdentifierAndIdentityIssuer(identifier, issuer);
    List<Submission> submissions = new ArrayList<>();
    for (SubmissionEntity entity : entities) {
      Submission submission = DomainMapper.toDomain(entity);
      // Load applications for this submission
      List<Application> applications = applicationJdbcCrudRepository.findBySubmissionId(entity.getId()).stream()
          .map(DomainMapper::toDomain)
          .collect(Collectors.toList());
      submission.setApplications(applications);
      submissions.add(submission);
    }
    return submissions;
  }
}