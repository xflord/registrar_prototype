package org.perun.registrarprototype.services.prefillStrategy;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.FormItem;

public interface PrefillStrategy {
  Optional<String> prefill(FormItem item, Map<String, String> options);

  void validateOptions(Map<String, String> options);

  FormItem.PrefillStrategyType getType();

  default Map<String, String> getTypeSpecificOptions(Map<String, String> options) {
    return options.entrySet().stream()
               .filter(entry -> entry.getKey().startsWith(getType() + "."))
               .collect(Collectors.toMap(entry -> entry.getKey().substring((getType() + ".").length()),
                   Map.Entry::getValue));
  }
}
