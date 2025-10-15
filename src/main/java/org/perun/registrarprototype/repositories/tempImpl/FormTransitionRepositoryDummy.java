package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.repositories.FormTransitionRepository;
import org.springframework.stereotype.Component;

@Component
public class FormTransitionRepositoryDummy implements FormTransitionRepository {
  private static List<FormTransition> formTransitions = new ArrayList<>();
  private static int currId = 1;

  @Override
  public List<FormTransition> getAllBySourceFormAndType(Form form, FormTransition.TransitionType type) {
    return formTransitions.stream()
               .filter((transition) -> transition.getSourceForm().getId() == form.getId() &&
                          transition.getType().equals(type)).toList();
  }
}
