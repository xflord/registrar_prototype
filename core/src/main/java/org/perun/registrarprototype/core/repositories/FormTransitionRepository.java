package org.perun.registrarprototype.core.repositories;

import java.util.List;
import org.perun.registrarprototype.core.models.Form;
import org.perun.registrarprototype.core.models.FormTransition;

public interface FormTransitionRepository {
  List<FormTransition> getAllBySourceFormAndType(Form form, FormTransition.TransitionType type);
}
