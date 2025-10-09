package org.perun.registrarprototype.repositories;

import java.util.List;
import org.perun.registrarprototype.models.Submission;

public interface SubmissionRepository {
  Submission save(Submission submission);
  List<Submission> updateAll(List<Submission> submissions);
  List<Submission> findAllByIdentifierAndIssuer(String identifier, String issuer);
}
