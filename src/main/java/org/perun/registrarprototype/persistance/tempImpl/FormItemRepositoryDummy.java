package org.perun.registrarprototype.persistance.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.persistance.DestinationRepository;
import org.perun.registrarprototype.persistance.FormItemRepository;
import org.perun.registrarprototype.persistance.ItemDefinitionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!jdbc")
@Component
public class FormItemRepositoryDummy implements FormItemRepository {
  private static List<FormItem> formItems = new ArrayList<>();
  private static int currId = 1;
  
  private final ItemDefinitionRepository itemDefinitionRepository;
  private final DestinationRepository destinationRepository;
  
  public FormItemRepositoryDummy(ItemDefinitionRepository itemDefinitionRepository, 
                                DestinationRepository destinationRepository) {
    this.itemDefinitionRepository = itemDefinitionRepository;
    this.destinationRepository = destinationRepository;
  }

  @Override
  public List<FormItem> getFormItemsByFormId(int formId) {
    return formItems.stream().filter(item -> item.getFormSpecificationId() != null && item.getFormSpecificationId() == formId).toList();
  }

  @Override
  public List<FormItem> getFormItemsByDestinationAttribute(String urn) {
    return formItems.stream().filter(item -> {
      Integer itemDefinitionId = item.getItemDefinitionId();
      ItemDefinition itemDefinition = itemDefinitionRepository.findById(itemDefinitionId)
          .orElseThrow(() -> new IllegalStateException("ItemDefinition not found for ID: " + itemDefinitionId));
      Integer destinationId = itemDefinition.getDestinationId();
      Destination destination = destinationId != null ? destinationRepository.findById(destinationId)
          .orElseThrow(() -> new IllegalStateException("Destination not found for ID: " + destinationId)) : null;
      return destination != null && destination.getUrn().equals(urn);
    }).toList();
  }

  @Override
  public List<FormItem> getFormItemsByItemDefinitionId(Integer itemDefinitionId) {
    return formItems.stream().filter(item -> item.getItemDefinitionId().equals(itemDefinitionId)).toList();
  }

  @Override
  public Optional<FormItem> getFormItemById(int formItemId) {
    return formItems.stream().filter(item -> item.getId() == formItemId).findFirst();
  }

  @Override
  public FormItem save(FormItem formItem) {
    // Check if formItem already exists (has an ID > 0 and is in the list)
    if (formItem.getId() > 0) {
      // Remove existing formItem with the same ID
      boolean removed = formItems.removeIf(item -> item.getId() == formItem.getId());
      formItems.add(formItem);
      if (removed) {
        System.out.println("Updated formItem " + formItem);
      } else {
        System.out.println("Created formItem " + formItem + " (with existing ID)");
      }
      return formItem;
    }
    
    // Create new formItem
    FormItem toCreate = new FormItem(formItem);
    toCreate.setId(currId++);
    formItems.add(toCreate);
    System.out.println("Created formItem " + toCreate);
    return toCreate;
  }

  @Override
  public List<FormItem> saveAll(List<FormItem> formItems) {
    formItems.forEach(this::save);
    return formItems;
  }

  @Override
  public FormItem update(FormItem formItem) {
    FormItem item = formItems.stream().filter(dbItem -> dbItem.getId() == formItem.getId()).findFirst().orElse(null);
    if (item == null) {
      return formItem;
    }
      item.setFormSpecificationId(formItem.getFormSpecificationId());
    return item;
  }

  @Override
  public void delete(FormItem formItem) {
    formItems.removeIf(item -> item.getId() == formItem.getId());
  }
}
