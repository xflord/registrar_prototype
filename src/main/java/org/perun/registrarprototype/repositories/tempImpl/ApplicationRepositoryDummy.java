package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.exceptions.DataInconsistencyException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.FormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// in-memory dummy implementation of persistent storage
@Component
public class ApplicationRepositoryDummy implements ApplicationRepository {
  private static List<Application> applications = new ArrayList<>();
  private static int currId = 1;
  @Autowired
  private FormRepository formRepository;

  @Override
  public Application save(Application application) {
    // Check if application already exists (has an ID > 0 and is in the list)
    if (application.getId() > 0) {
      // Remove existing application with the same ID
      boolean removed = applications.removeIf(app -> app.getId() == application.getId());
      applications.add(application);
      if (removed) {
        System.out.println("Updated application " + application.getId());
      } else {
        System.out.println("Created application " + application.getId() + " (with existing ID)");
      }
      return application;
    }
    
    // Create new application
    application.setId(currId++);
    applications.add(application);
    System.out.println("Created application " + application.getId());
    return application;
  }

  @Override
  public List<Application> updateAll(List<Application> applicationsToUpdate) {
    applications.removeIf(applicationsToUpdate::contains);
    applications.addAll(applicationsToUpdate);
    return applicationsToUpdate;
  }

  @Override
  public Optional<Application> findById(int id) {
    Optional<Application> optionalApplication = applications.stream()
                                                    .filter(application -> application.getId() == id)
                                                    .findFirst();
    optionalApplication.ifPresent(
        application -> application.setForm(formRepository.findById(application.getForm().getId())
                                               .orElseThrow(() -> new DataInconsistencyException("Application: " + id +
                                                                                                     " has no form."))));
    return optionalApplication;
  }

  @Override
  public List<Application> findByFormId(int formId) {
    return applications.stream().filter(application -> application.getForm().getId() == formId).toList();
  }

  @Override
  public List<Application> findAll() {
    return List.copyOf(applications);
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
