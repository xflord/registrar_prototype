package org.perun.registrarprototype.services.prefillStrategy.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

  public CompositePrefillStrategy resolveFor(FormItem item) {
    List<PrefillStrategy> itemStrategies = new ArrayList<>();
    Map<PrefillStrategy, Map<String, String>> optionsMap = new HashMap<>();

    if (item.getType().equals(FormItem.Type.LOGIN)) {
      itemStrategies.add(find(LoginItemPrefillStrategy.class));
      optionsMap.put(find(LoginItemPrefillStrategy.class), null);
    }

    if (item.getPrefillStrategyOptions() != null && !item.getPrefillStrategyOptions().isEmpty()) {
      itemStrategies.addAll(item.getPrefillStrategyOptions().stream()
                                .map(this::getStrategyForKey).toList());
      item.getPrefillStrategyOptions().forEach(option ->
                                                  optionsMap.put(getStrategyForKey(option), option.getOptions()));
    }

    return new CompositePrefillStrategy(itemStrategies, optionsMap);
  }

  private PrefillStrategy getStrategyForKey(PrefillStrategyEntry prefillStrategyEntry) {
    switch (prefillStrategyEntry.getPrefillStrategyType()) {
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
