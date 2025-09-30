package org.perun.registrarprototype.repositories;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;

public interface FormItemRepository {
  List<FormItem> getFormItemsByFormId(int formId);

  Optional<FormItem> getFormItemById(int formItemId);

  FormItem save(FormItem formItem);

  FormItem update(FormItem formItem);
}
