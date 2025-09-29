package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.repositories.SubmissionRepository;
import org.springframework.stereotype.Component;

@Component
public class SubmissionRepositorDummy implements SubmissionRepository {
  private static List<Submission> submissions = new ArrayList<>();
  private static int currId = 0;

  @Override
  public Submission save(Submission submission) {
    submissions.add(submission);
    return submission;
  }
}
