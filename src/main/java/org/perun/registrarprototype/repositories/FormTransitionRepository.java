package org.perun.registrarprototype.repositories;

import java.util.List;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormTransition;

public interface FormTransitionRepository {
  List<FormTransition> getAllBySourceFormAndType(Form form, FormTransition.TransitionType type);
}
