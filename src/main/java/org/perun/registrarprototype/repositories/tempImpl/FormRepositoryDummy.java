package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.repositories.FormRepository;
import org.springframework.stereotype.Component;

// in-memory dummy implementation of persistent storage
@Component
public class FormRepositoryDummy implements FormRepository {
  private static List<FormSpecification> formSpecifications = new ArrayList<>();
  private static int currId = 1;


  @Override
  public Optional<FormSpecification> findById(int formId) {
    return formSpecifications.stream().filter(form -> form.getId() == formId).findFirst();
  }

  @Override
  public Optional<FormSpecification> findByGroupId(int groupId) {
    return formSpecifications.stream().filter(form -> form.getGroupId() == groupId).findFirst();
  }

  @Override
  public int getNextId() {
    return currId++;
  }

  @Override
  public FormSpecification save(FormSpecification formSpecification) {
    // Check if form already exists (has an ID > 0 and is in the list)
    if (formSpecification.getId() > 0) {
      // Remove existing form with the same ID
      boolean removed = formSpecifications.removeIf(f -> f.getId() == formSpecification.getId());
      formSpecifications.add(formSpecification);
      if (removed) {
        System.out.println("Updated form " + formSpecification.getId());
      } else {
        System.out.println("Created form " + formSpecification.getId() + " (with existing ID)");
      }
      return formSpecification;
    }
    
    // Create new form
    formSpecification.setId(currId++);
    formSpecifications.add(formSpecification);
    System.out.println("Created form " + formSpecification.getId());
    return formSpecification;
  }

  @Override
  public FormSpecification update(FormSpecification formSpecification) {
    FormSpecification
        existingFormSpecification = formSpecifications.stream().filter(dbForm -> dbForm.getId() == formSpecification.getId()).findFirst().orElse(null);
    if (existingFormSpecification == null) {
      return formSpecification;
    }
    existingFormSpecification.setItems(formSpecification.getItems());
    return existingFormSpecification;
  }

  @Override
  public void delete(FormSpecification formSpecification) {
    formSpecifications.removeIf(f -> f.getId() == formSpecification.getId());
  }

  @Override
  public List<FormSpecification> findAll() {
    return List.copyOf(formSpecifications);
  }

  // for testing purposes
  public void reset()  {
    formSpecifications.clear();
    currId = 0;
  }
}
