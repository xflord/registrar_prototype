package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.FormItem;
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

  public PrefillStrategy resolveFor(FormItem item) {
    List<PrefillStrategy> itemStrategies = new ArrayList<>();

    if (item.getType().equals(FormItem.Type.LOGIN)) itemStrategies.add(find(LoginItemPrefillStrategy.class));

    if (item.getPrefillStrategyTypes() != null) {
      itemStrategies.addAll(item.getPrefillStrategyTypes().stream()
                                .map(this::getStrategyForKey).toList());
    }

    return new CompositePrefillStrategy(itemStrategies);
  }

  private PrefillStrategy getStrategyForKey(FormItem.PrefillStrategyType key) {
    switch (key) {
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
    return null;
  }

  private PrefillStrategy find(Class<? extends PrefillStrategy> type) {
      return strategies.stream()
          .filter(s -> type.isAssignableFrom(s.getClass()))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Missing strategy " + type.getSimpleName()));
  }
}
