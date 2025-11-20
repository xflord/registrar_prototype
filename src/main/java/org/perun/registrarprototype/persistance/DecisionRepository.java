package org.perun.registrarprototype.persistance;

import java.util.List;
import org.perun.registrarprototype.models.Decision;

public interface DecisionRepository {
  Decision save(Decision decision);
  List<Decision> findByApplicationId(int applicationId);
}
