package org.perun.registrarprototype.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.Application;

// in-memory dummy implementation of persistent storage
public class ApplicationRepositoryDummy implements ApplicationRepository {
  private static List<Application> applications = new ArrayList<>();
  private static int currId = 0;

  @Override
  public void save(Application application) {
    applications.add(application);
    System.out.println("Created application " + application);
  }

  @Override
  public Optional<Application> findById(int id) {
    return applications.stream().filter(application -> application.getId() == id).findFirst();
  }

  @Override
  public int getNextId() {
    return currId++;
  }

  // for testing purposes
  public void reset()  {
    applications.clear();
    currId = 0;
  }
}
