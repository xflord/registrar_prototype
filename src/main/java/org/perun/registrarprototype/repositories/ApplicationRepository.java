package org.perun.registrarprototype.repositories;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.Application;

public interface ApplicationRepository {
  Application save(Application application);

  List<Application> updateAll(List<Application> applications);

  Optional<Application> findById(int id);

  List<Application> findByFormId(int formId);
  List<Application> findAll();

  int getNextId();
}
