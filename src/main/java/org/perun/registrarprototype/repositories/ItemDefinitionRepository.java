package org.perun.registrarprototype.repositories;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.ItemDefinition;

public interface ItemDefinitionRepository {
  Optional<ItemDefinition> findById(int id);
  List<ItemDefinition> findAllGlobal();
  List<ItemDefinition> findAllByForm(FormSpecification formSpecification);
  ItemDefinition save(ItemDefinition itemDefinition);
  List<ItemDefinition> saveAll(List<ItemDefinition> itemDefinitions);
}
