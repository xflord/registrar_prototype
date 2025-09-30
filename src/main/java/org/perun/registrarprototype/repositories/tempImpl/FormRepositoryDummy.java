package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.repositories.FormRepository;
import org.springframework.stereotype.Component;

// in-memory dummy implementation of persistent storage
@Component
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
  public Form save(Form form) {
    form.setId(currId++);
    forms.add(form);
    return form;
  }

  @Override
  public Form update(Form form) {
    Form existingForm = forms.stream().filter(dbForm -> dbForm.getId() == form.getId()).findFirst().orElse(null);
    if (existingForm == null) {
      return form;
    }
    existingForm.setItems(form.getItems());
    return existingForm;
  }

  // for testing purposes
  public void reset()  {
    forms.clear();
    currId = 0;
  }
}
