package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.repositories.FormItemRepository;
import org.springframework.stereotype.Component;

@Component
public class FormItemRepositoryDummy implements FormItemRepository {
  private static List<FormItem> formItems = new ArrayList<>();
  private static int currId = 0;

  @Override
  public List<FormItem> getFormItemsByFormId(int formId) {
    return formItems.stream().filter(item -> item.getFormId() == formId).toList();
  }

  @Override
  public Optional<FormItem> getFormItemById(int formItemId) {
    return formItems.stream().filter(item -> item.getId() == formItemId).findFirst();
  }

  @Override
  public FormItem save(FormItem formItem) {
    formItem.setId(currId++);
    formItems.add(formItem);
    return formItem;
  }
}
