package org.perun.registrarprototype.repositories;

import java.util.List;
import org.perun.registrarprototype.models.FormItem;

public interface FormItemRepository {
  List<FormItem> getFormItemsByFormId(int formId);
}
