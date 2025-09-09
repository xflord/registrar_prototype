package org.perun.registrarprototype.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.Form;

// in-memory dummy implementation of persistent storage
public class FormRepositoryDummy implements FormRepository {
  private static List<Form> forms = new ArrayList<>();
  private static int currId = 0;


  @Override
  public Optional<Form> findById(int formId) {
    return forms.stream().filter(form -> form.getId() == formId).findFirst();
  }

  @Override
  public Optional<Form> findByGroupId(int groupId) {
    return forms.stream().filter(form -> form.getGroupId() == groupId).findFirst();
  }

  @Override
  public int getNextId() {
    return currId++;
  }

  @Override
  public void save(Form form) {
    forms.add(form);
  }

  // for testing purposes
  public void reset()  {
    forms.clear();
    currId = 0;
  }
}
