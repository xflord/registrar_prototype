package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.persistence.ItemDefinitionRepository;
import org.perun.registrarprototype.persistence.PrefillStrategyEntryRepository;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;
import org.springframework.stereotype.Component;

/**
 * Resolves prefill strategy for a given form item.
 * TODO consider making this pluggable, similar to FormModules?
 */
@Component
public class PrefillStrategyResolver {

  private final List<PrefillStrategy> strategies;
  private final ItemDefinitionRepository itemDefinitionRepository;
  private final PrefillStrategyEntryRepository prefillStrategyEntryRepository;

  public PrefillStrategyResolver(List<PrefillStrategy> strategies,
                                 ItemDefinitionRepository itemDefinitionRepository,
                                 PrefillStrategyEntryRepository prefillStrategyEntryRepository) {
      this.strategies = strategies;
      this.itemDefinitionRepository = itemDefinitionRepository;
      this.prefillStrategyEntryRepository = prefillStrategyEntryRepository;
  }

  public Optional<String> prefill(FormItem item) {
    // Get the ItemDefinition by ID
    if (item.getItemDefinitionId() == null) {
      return Optional.empty();
    }
    
    Optional<ItemDefinition> itemDefinitionOpt = itemDefinitionRepository.findById(item.getItemDefinitionId());
    if (itemDefinitionOpt.isEmpty()) {
      return Optional.empty();
    }
    
    ItemDefinition itemDefinition = itemDefinitionOpt.get();
    
    // Check if prefill strategies exist
    if (itemDefinition.getPrefillStrategyIds() == null || itemDefinition.getPrefillStrategyIds().isEmpty()) {
      return Optional.empty();
    }
    
    // Get all PrefillStrategyEntry objects by their IDs
    List<PrefillStrategyEntry> prefillStrategies = prefillStrategyEntryRepository.findAllById(itemDefinition.getPrefillStrategyIds());
    
    for (PrefillStrategyEntry entry : prefillStrategies) {
      PrefillStrategy prefillStrategy = getStrategyForKey(entry.getType());
      prefillStrategy.validateOptions(entry);
      Optional<String> result = prefillStrategy.prefill(item, entry);
      if (result.isPresent()) {
        return result;
      }
    }
    return Optional.empty();
  }

  private PrefillStrategy getStrategyForKey(PrefillStrategyEntry.PrefillStrategyType type) {
    switch (type) {
      case IDM_ATTRIBUTE -> {
        return find(IdmAttributePrefillStrategy.class);
      }
      case IDENTITY_ATTRIBUTE -> {
        return find(IdentityAttributePrefillStrategy.class);
      }
      case APPLICATION -> {
        return find(ExistingApplicationPrefillStrategy.class);
      }
      case LOGIN_ATTRIBUTE -> {
        // LOGIN_ATTRIBUTE is not yet implemented
        throw new IllegalArgumentException("Unsupported prefill strategy type: " + type);
      }
    }
    throw new IllegalArgumentException("Unsupported prefill strategy type: " + type);
  }

  private PrefillStrategy find(Class<? extends PrefillStrategy> type) {
      return strategies.stream()
          .filter(s -> type.isAssignableFrom(s.getClass()))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Missing strategy " + type.getSimpleName()));
  }
}
