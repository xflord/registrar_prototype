package org.perun.registrarprototype.core.repositories;

import org.perun.registrarprototype.core.models.Decision;

public interface DecisionRepository {
  Decision save(Decision decision);
}
