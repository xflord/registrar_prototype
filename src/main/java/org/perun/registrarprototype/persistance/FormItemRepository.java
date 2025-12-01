package org.perun.registrarprototype.persistance;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;

public interface FormItemRepository {
  List<FormItem> getFormItemsByFormId(int formId);

  List<FormItem> getFormItemsByDestinationAttribute(String urn);

  List<FormItem> getFormItemsByItemDefinitionId(Integer itemDefinitionId);

  Optional<FormItem> getFormItemById(int formItemId);

  FormItem save(FormItem formItem);

  List<FormItem> saveAll(List<FormItem> formItems);

  FormItem update(FormItem formItem);

  void delete(FormItem formItem);
}
