package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.services.prefillStrategy.PrefillStrategy;
import org.springframework.stereotype.Component;

/**
 * Resolves prefill strategy for a given form item.
 * TODO consider making this pluggable, similar to FormModules?
 */
@Component
public class PrefillStrategyResolver {

  private final List<PrefillStrategy> strategies;

  public PrefillStrategyResolver(List<PrefillStrategy> strategies) {
      this.strategies = strategies;
  }

  public Optional<String> prefill(FormItem item) {
    for (PrefillStrategyEntry entry : item.getPrefillStrategyOptions()) {
      PrefillStrategy prefillStrategy = getStrategyForKey(entry.getPrefillStrategyType());
      prefillStrategy.validateOptions(entry);
      Optional<String> result = prefillStrategy.prefill(item, entry);
      if (result.isPresent()) {
        return result;
      }
    }
    return Optional.empty();
  }

  private PrefillStrategy getStrategyForKey(FormItem.PrefillStrategyType type) {
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
