package org.perun.registrarprototype.repositories;

import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.PrefillStrategyEntry;

public interface PrefillStrategyEntryRepository {
  PrefillStrategyEntry save(PrefillStrategyEntry entry);
  List<PrefillStrategyEntry> saveAll(List<PrefillStrategyEntry> entry);
  Optional<PrefillStrategyEntry> findById(int id);
  List<PrefillStrategyEntry> findAllGlobal();
  List<PrefillStrategyEntry> findByFormSpecification(FormSpecification formSpecification);
}
