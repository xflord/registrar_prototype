package org.perun.registrarprototype.core.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.core.models.Form;
import org.perun.registrarprototype.core.models.FormTransition;
import org.perun.registrarprototype.core.repositories.FormTransitionRepository;
import org.springframework.stereotype.Component;

@Component
public class FormTransitionRepositoryDummy implements FormTransitionRepository {
  private static List<FormTransition> formTransitions = new ArrayList<>();
  private static int currId = 0;

  @Override
  public List<FormTransition> getAllBySourceFormAndType(Form form, FormTransition.TransitionType type) {
    return formTransitions.stream()
               .filter((transition) -> transition.getSourceForm().getId() == form.getId() &&
                          transition.getType().equals(type)).toList();
  }
}
