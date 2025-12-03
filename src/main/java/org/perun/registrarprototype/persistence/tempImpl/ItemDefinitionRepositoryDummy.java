package org.perun.registrarprototype.persistence.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.persistence.ItemDefinitionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dummy")
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
  public List<ItemDefinition> findAllById(List<Integer> ids) {
    return storedItemDefinitions.stream()
               .filter(item -> ids.contains(item.getId()))
               .toList();
  }

  @Override
  public Optional<ItemDefinition> findByShortName(String shortName) {
    return storedItemDefinitions.stream()
               .filter(item -> item.getDisplayName().equals(shortName))
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
    return storedItemDefinitions.stream()
               .filter(def -> def.getFormSpecificationId() != null && def.getFormSpecificationId() == formSpecification.getId())
               .toList();
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
    itemDefinitions.forEach(this::save);
    return itemDefinitions;
  }

  @Override
  public void delete(ItemDefinition itemDefinition) {
    storedItemDefinitions.remove(itemDefinition);
  }
}
