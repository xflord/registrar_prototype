package org.perun.registrarprototype.services.prefillStrategy.impl;

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
    if (item.getType().equals(FormItem.Type.LOGIN)) return find(LoginItemPrefillStrategy.class);

    if (item.getPrefillStrategyKeys().size() > 1) {
      return new CompositePrefillStrategy(strategies);
    }
    FormItem.PrefillStrategyKey key = item.getPrefillStrategyKeys().getFirst();
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
