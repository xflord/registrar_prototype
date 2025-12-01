package org.perun.registrarprototype.persistance.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.persistance.SubmissionRepository;
import org.perun.registrarprototype.persistance.jdbc.entities.SubmissionEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class SubmissionRepositoryJdbc implements SubmissionRepository {

  private final SubmissionJdbcCrudRepository jdbcRepository;
  private final SimpleDomainMapper simpleDomainMapper;
  private final ApplicationJdbcCrudRepository applicationJdbcCrudRepository;

  public SubmissionRepositoryJdbc(
      SubmissionJdbcCrudRepository jdbcRepository,
      SimpleDomainMapper simpleDomainMapper,
      ApplicationJdbcCrudRepository applicationJdbcCrudRepository) {
    this.jdbcRepository = jdbcRepository;
    this.simpleDomainMapper = simpleDomainMapper;
    this.applicationJdbcCrudRepository = applicationJdbcCrudRepository;
  }

  @Override
  public Submission save(Submission submission) {
    SubmissionEntity entity = toEntity(submission);
    SubmissionEntity saved = jdbcRepository.save(entity);
    return simpleDomainMapper.toDomain(saved);
  }

  @Override
  public Optional<Submission> findById(int id) {
    Optional<SubmissionEntity> entity = jdbcRepository.findById(id);
    if (entity.isPresent()) {
      Submission submission = simpleDomainMapper.toDomain(entity.get());
      // Load applications for this submission
      List<Application> applications = applicationJdbcCrudRepository.findBySubmissionId(id).stream()
          .map(simpleDomainMapper::toDomain)
          .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
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
      Submission submission = simpleDomainMapper.toDomain(entity);
      // Load applications for this submission
      List<Application> applications = applicationJdbcCrudRepository.findBySubmissionId(entity.getId()).stream()
          .map(simpleDomainMapper::toDomain)
          .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
      submission.setApplications(applications);
      submissions.add(submission);
    }
    return submissions;
  }

  private SubmissionEntity toEntity(Submission domain) {
    SubmissionEntity entity = new SubmissionEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setTimestamp(domain.getTimestamp());
    entity.setSubmitterId(domain.getSubmitterId());
    entity.setSubmitterName(domain.getSubmitterName());
    entity.setIdentityIdentifier(domain.getIdentityIdentifier());
    entity.setIdentityIssuer(domain.getIdentityIssuer());
    
    // Convert identityAttributes Map to JSON string
    if (domain.getIdentityAttributes() != null) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        String identityAttributesJson = mapper.writeValueAsString(domain.getIdentityAttributes());
        entity.setIdentityAttributes(identityAttributesJson);
      } catch (JsonProcessingException e) {
        // Handle JSON serialization error
        entity.setIdentityAttributes("{}");
      }
    } else {
      entity.setIdentityAttributes("{}");
    }
    
    return entity;
  }
}