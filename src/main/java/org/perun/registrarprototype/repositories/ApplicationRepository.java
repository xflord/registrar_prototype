package org.perun.registrarprototype.repositories;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.Application;

public interface ApplicationRepository {
  void save(Application application);

  Optional<Application> findById(int id);

  List<Application> findByFormId(int formId);

  int getNextId();
}
