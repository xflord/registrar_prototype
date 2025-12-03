package org.perun.registrarprototype.persistence.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.persistence.ApplicationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// in-memory dummy implementation of persistent storage
@Profile("dummy")
@Component
public class ApplicationRepositoryDummy implements ApplicationRepository {
  private static List<Application> applications = new ArrayList<>();
  private static int currId = 1;

  @Override
  public Application save(Application application) {
    // Check if application already exists (has an ID > 0 and is in the list)
    if (application.getId() > 0) {
      // Remove existing application with the same ID
      boolean removed = applications.removeIf(app -> app.getId() == application.getId());
      applications.add(application);
      if (removed) {
        System.out.println("Updated application " + application.getId());
        return application;
      }
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
    return applications.stream()
        .filter(application -> application.getId() == id)
        .findFirst();
  }

  @Override
  public List<Application> findByFormId(int formId) {
    return applications.stream().filter(application -> application.getFormSpecificationId() == formId).toList();
  }

  @Override
  public List<Application> findAll() {
    return List.copyOf(applications);
  }

  @Override
  public List<Application> findOpenApplicationsByItemDefinitionId(Integer itemDefinitionId) {
    // This is a dummy implementation - in a real scenario, you would check the form items
    // associated with each application to see if they use the specified item definition
    return new ArrayList<>();
  }

  // for testing purposes
  public void reset()  {
    applications.clear();
    currId = 0;
  }
}
