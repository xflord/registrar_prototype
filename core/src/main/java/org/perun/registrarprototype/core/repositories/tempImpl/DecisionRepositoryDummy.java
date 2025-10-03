package org.perun.registrarprototype.core.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.core.models.Decision;
import org.perun.registrarprototype.core.repositories.DecisionRepository;
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
