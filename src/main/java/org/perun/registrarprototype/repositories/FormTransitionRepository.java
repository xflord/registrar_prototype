package org.perun.registrarprototype.repositories;

import java.util.List;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormTransition;

public interface FormTransitionRepository {
  FormTransition save(FormTransition formTransition);
  List<FormTransition> getAllBySourceFormAndType(FormSpecification formSpecification, FormTransition.TransitionType type);
  void remove(FormTransition formTransition);
}
