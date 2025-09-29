package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.springframework.stereotype.Component;

// in-memory dummy implementation of persistent storage
@Component
public class ApplicationRepositoryDummy implements ApplicationRepository {
  private static List<Application> applications = new ArrayList<>();
  private static int currId = 0;

  @Override
  public Application save(Application application) {
    application.setId(currId++);
    applications.add(application);
    System.out.println("Created application " + application);
    return application;
  }

  @Override
  public Optional<Application> findById(int id) {
    return applications.stream().filter(application -> application.getId() == id).findFirst();
  }

  @Override
  public List<Application> findByFormId(int formId) {
    return applications.stream().filter(application -> application.getFormId() == formId).toList();
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
