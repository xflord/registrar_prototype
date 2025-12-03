package org.perun.registrarprototype.persistence;

import java.util.List;
import org.perun.registrarprototype.models.Decision;

public interface DecisionRepository {
  Decision save(Decision decision);
  List<Decision> findByApplicationId(int applicationId);
}
