package org.perun.registrarprototype.services.prefillStrategy;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.PrefillStrategyEntry;

public interface PrefillStrategy {
  /**
   * Calculate the prefill value for the supplied item.
   * @param item
   * @param entry
   * @return
   */
  Optional<String> prefill(FormItem item, PrefillStrategyEntry entry);

  /**
   * Validate that the options passed to the strategy include all the required entries and that they are in the expected
   * format.
   * @param entry
   */
  void validateOptions(PrefillStrategyEntry entry);

  /**
   * Return the type of the prefill strategy.
   * @return
   */
  PrefillStrategyEntry.PrefillStrategyType getType();

  /**
   * From a map of all attribute's prefill strategy options, return the ones relevant for the current strategy.
   * More specifically, returns the entries, where the key has a prefix of that PrefillStrategyType, while also removing
   * the prefix for easier handling.
   * @param options
   * @return
   */
  default Map<String, String> getTypeSpecificOptions(Map<String, String> options) {
    return options.entrySet().stream()
               .filter(entry -> entry.getKey().startsWith(getType() + "."))
               .collect(Collectors.toMap(entry -> entry.getKey().substring((getType() + ".").length()),
                   Map.Entry::getValue));
  }
}
