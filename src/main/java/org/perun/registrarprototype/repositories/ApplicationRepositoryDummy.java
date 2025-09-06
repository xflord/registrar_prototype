package org.perun.registrarprototype.repositories;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.Application;

// in-memory dummy implementation of persistent storage
public class ApplicationRepositoryDummy implements ApplicationRepository {
  private static List<Application> applications = new ArrayList<>();
  private static int currId = 0;

  @Override
  public void save(Application application) {
    applications.add(application);
  }

  @Override
  public int getNextId() {
    return currId++;
  }
}
