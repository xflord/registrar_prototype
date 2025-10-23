package org.perun.registrarprototype.services.prefillStrategy;

import java.util.Map;
import java.util.Optional;
import org.perun.registrarprototype.models.FormItem;

public interface PrefillStrategy {
  Optional<String> prefill(FormItem item, Map<String, Object> config);
}
