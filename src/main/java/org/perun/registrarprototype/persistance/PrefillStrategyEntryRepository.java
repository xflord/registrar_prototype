package org.perun.registrarprototype.persistance;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.PrefillStrategyEntry;

public interface PrefillStrategyEntryRepository {
  PrefillStrategyEntry save(PrefillStrategyEntry entry);
  List<PrefillStrategyEntry> saveAll(List<PrefillStrategyEntry> entry);
  Optional<PrefillStrategyEntry> findById(int id);
  List<PrefillStrategyEntry> findAllById(List<Integer> ids);
  List<PrefillStrategyEntry> findAllGlobal();
  List<PrefillStrategyEntry> findByFormSpecification(FormSpecification formSpecification);

  /**
   * Checks whether prefill strategy with the same properties already exists
   * @param entry
   * @return
   */
  Optional<PrefillStrategyEntry> exists(PrefillStrategyEntry entry);
  void delete(PrefillStrategyEntry entry);
}
