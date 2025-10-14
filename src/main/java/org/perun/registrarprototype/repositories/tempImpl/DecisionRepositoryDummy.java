package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.Decision;
import org.perun.registrarprototype.repositories.DecisionRepository;
import org.springframework.stereotype.Component;

@Component
public class DecisionRepositoryDummy implements DecisionRepository {
  private static List<Decision> decisions = new ArrayList<>();
  private static int currId = 0;

  @Override
  public Decision save(Decision decision) {
    // Check if decision already exists (has an ID > 0 and is in the list)
    if (decision.getId() > 0) {
      // Remove existing decision with the same ID
      boolean removed = decisions.removeIf(d -> d.getId() == decision.getId());
      decisions.add(decision);
      if (removed) {
        System.out.println("Updated decision " + decision.getId());
      } else {
        System.out.println("Created decision " + decision.getId() + " (with existing ID)");
      }
      return decision;
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
        .filter(d -> d.getApplication() != null && d.getApplication().getId() == applicationId)
        .toList();
  }
}
