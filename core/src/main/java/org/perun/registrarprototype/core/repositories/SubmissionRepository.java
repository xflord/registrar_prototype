package org.perun.registrarprototype.core.repositories;

import org.perun.registrarprototype.core.models.Submission;

public interface SubmissionRepository {
  Submission save(Submission submission);
}
