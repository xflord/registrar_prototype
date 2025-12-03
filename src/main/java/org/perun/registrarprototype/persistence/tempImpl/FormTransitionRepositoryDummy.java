package org.perun.registrarprototype.persistence.tempImpl;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.persistence.FormTransitionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dummy")
@Component
public class FormTransitionRepositoryDummy implements FormTransitionRepository {
  private static List<FormTransition> formTransitions = new ArrayList<>();
  private static int currId = 1;

  @Override
  public FormTransition save(FormTransition formTransition) {
    if (formTransition.getId() > 0) {
      // Remove existing form with the same ID
      boolean removed = formTransitions.removeIf(f -> f.getId() == formTransition.getId());
      formTransitions.add(formTransition);
      if (removed) {
        System.out.println("Updated form transition " + formTransition.getId());
        return formTransition;
      }
    }

    // Create new form
    formTransition.setId(currId++);
    formTransitions.add(formTransition);
    System.out.println("Created form transition " + formTransition.getId());
    return formTransition;
  }

  @Override
  public List<FormTransition> getAllBySourceFormAndType(FormSpecification formSpecification, FormTransition.TransitionType type) {
    return formTransitions.stream()
               .filter((transition) -> transition.getSourceFormSpecification().getId() == formSpecification.getId() &&
                          transition.getType().equals(type)).toList();
  }

  @Override
  public void remove(FormTransition formTransition) {
    formTransitions.removeIf(trans ->  trans.getId() == formTransition.getId());
  }
}
