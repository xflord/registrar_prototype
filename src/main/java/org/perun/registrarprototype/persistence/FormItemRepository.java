package org.perun.registrarprototype.persistence;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormSpecification;

public interface FormItemRepository {
  List<FormItem> getFormItemsByFormId(int formId);

  List<FormItem> getFormItemsByFormIdAndType(int formId, FormSpecification.FormType formType);

  List<FormItem> getFormItemsByDestinationAttribute(String urn);

  List<FormItem> getFormItemsByItemDefinitionId(Integer itemDefinitionId);

  Optional<FormItem> getFormItemById(int formItemId);

  FormItem save(FormItem formItem);

  List<FormItem> saveAll(List<FormItem> formItems);

  FormItem update(FormItem formItem);

  void delete(FormItem formItem);
}
