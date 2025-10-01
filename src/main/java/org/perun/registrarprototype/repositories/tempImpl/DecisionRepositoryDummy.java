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
    decision.setId(currId++);
    decisions.add(decision);
    return decision;
  }
}
