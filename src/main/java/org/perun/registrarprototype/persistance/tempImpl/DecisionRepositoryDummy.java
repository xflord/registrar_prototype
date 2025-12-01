package org.perun.registrarprototype.persistance.tempImpl;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.Decision;
import org.perun.registrarprototype.persistance.DecisionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!jdbc")
@Component
public class DecisionRepositoryDummy implements DecisionRepository {
  private static List<Decision> decisions = new ArrayList<>();
  private static int currId = 1;

  @Override
  public Decision save(Decision decision) {
    // Check if decision already exists (has an ID > 0 and is in the list)
    if (decision.getId() > 0) {
      // Remove existing decision with the same ID
      boolean removed = decisions.removeIf(d -> d.getId() == decision.getId());
      decisions.add(decision);
      if (removed) {
        System.out.println("Updated decision " + decision.getId());
        return decision;
      }
    }
    
    // Create new decision
    decision.setId(currId++);
    decisions.add(decision);
    System.out.println("Created decision " + decision.getId());
    return decision;
  }

  @Override
  public List<Decision> findByApplicationId(int applicationId) {
    return decisions.stream()
        .filter(d -> d.getApplicationId() != null && d.getApplicationId() == applicationId)
        .toList();
  }
}
