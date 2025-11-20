package org.perun.registrarprototype.persistance;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.Submission;

public interface SubmissionRepository {
  Submission save(Submission submission);
  Optional<Submission> findById(int id);
  List<Submission> updateAll(List<Submission> submissions);
  List<Submission> findAllByIdentifierAndIssuer(String identifier, String issuer);
}
