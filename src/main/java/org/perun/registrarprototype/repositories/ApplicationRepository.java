package org.perun.registrarprototype.repositories;

import org.perun.registrarprototype.models.Application;

public interface ApplicationRepository {
  void save(Application application);

  int getNextId();
}
