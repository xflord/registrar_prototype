package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.repositories.SubmissionRepository;
import org.springframework.stereotype.Component;

@Component
public class SubmissionRepositorDummy implements SubmissionRepository {
  private static List<Submission> submissions = new ArrayList<>();
  private static int currId = 1;

  @Override
  public Submission save(Submission submission) {
    submission.setId(currId++);
    submissions.add(submission);
    return submission;
  }

  @Override
  public Optional<Submission> findById(int id) {
    return submissions.stream().filter(submission -> submission.getId() == id).findFirst();
  }

  @Override
  public List<Submission> updateAll(List<Submission> submissionsToUpdate) {
    submissions.removeIf(submissionsToUpdate::contains);
    submissions.addAll(submissionsToUpdate);
    return submissionsToUpdate;
  }

  @Override
  public List<Submission> findAllByIdentifierAndIssuer(String identifier, String issuer) {
    return submissions.stream()
               .filter(submission -> submission.getIdentityIssuer().equals(issuer) &&
                          submission.getIdentityIdentifier().equals(identifier)).toList();
  }
}
