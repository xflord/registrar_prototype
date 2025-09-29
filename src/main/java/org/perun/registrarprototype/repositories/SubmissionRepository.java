package org.perun.registrarprototype.repositories;

import org.perun.registrarprototype.models.Submission;

public interface SubmissionRepository {
  Submission save(Submission submission);
}
