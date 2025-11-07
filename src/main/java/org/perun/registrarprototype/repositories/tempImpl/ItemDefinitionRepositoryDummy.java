package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.repositories.ItemDefinitionRepository;
import org.springframework.stereotype.Component;

@Component
public class ItemDefinitionRepositoryDummy implements ItemDefinitionRepository {
  private static final List<ItemDefinition> storedItemDefinitions = new ArrayList<>();
  private static int currId = 1;

  @Override
  public Optional<ItemDefinition> findById(int id) {
    return storedItemDefinitions.stream()
               .filter(itemDefinition -> itemDefinition.getId() == id)
               .findFirst();
  }

  @Override
  public List<ItemDefinition> findAllGlobal() {
    return storedItemDefinitions.stream()
               .filter(ItemDefinition::isGlobal)
               .toList();
  }

  @Override
  public List<ItemDefinition> findAllByForm(FormSpecification formSpecification) {
    return List.of();
  }

  @Override
  public ItemDefinition save(ItemDefinition itemDefinition) {
    if (itemDefinition.getId() > 0) {
      boolean removed = storedItemDefinitions.removeIf(item -> item.getId() == itemDefinition.getId());
      storedItemDefinitions.add(itemDefinition);
      if (removed) {
        System.out.println("Updated item definition " + itemDefinition);
        return itemDefinition;
      }
    }

    ItemDefinition toCreate = new ItemDefinition(itemDefinition);
    toCreate.setId(currId++);
    storedItemDefinitions.add(toCreate);
    System.out.println("Created item definition " + toCreate);
    return toCreate;
  }

  @Override
  public List<ItemDefinition> saveAll(List<ItemDefinition> itemDefinitions) {
    storedItemDefinitions.addAll(itemDefinitions);
    return itemDefinitions;
  }
}
