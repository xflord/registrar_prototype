package org.perun.registrarprototype.repositories;

import java.util.List;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormTransition;

public interface FormTransitionRepository {
  List<FormTransition> getAllBySourceFormAndType(FormSpecification formSpecification, FormTransition.TransitionType type);
}
