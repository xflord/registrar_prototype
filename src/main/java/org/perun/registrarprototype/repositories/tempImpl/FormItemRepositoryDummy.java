package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
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
}
