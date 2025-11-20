package org.perun.registrarprototype.persistance.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.persistance.PrefillStrategyEntryRepository;
import org.springframework.stereotype.Component;

@Component
public class PrefillStrategyEntryRepositoryDummy implements PrefillStrategyEntryRepository {
  private static final List<PrefillStrategyEntry> prefillStrategyEntries = new ArrayList<>();
  private static int currId = 1;

  @Override
  public PrefillStrategyEntry save(PrefillStrategyEntry entry) {
    entry.setId(currId++);
    prefillStrategyEntries.add(entry);
    return entry;
  }

  @Override
  public List<PrefillStrategyEntry> saveAll(List<PrefillStrategyEntry> entry) {
    entry.forEach(this::save);
    return entry;
  }

  @Override
  public Optional<PrefillStrategyEntry> findById(int id) {
    return prefillStrategyEntries.stream()
               .filter(entry -> entry.getId() == id )
               .findFirst();
  }

  @Override
  public List<PrefillStrategyEntry> findAllGlobal() {
    return prefillStrategyEntries.stream()
               .filter(PrefillStrategyEntry::isGlobal)
               .toList();
  }

  @Override
  public List<PrefillStrategyEntry> findByFormSpecification(FormSpecification formSpecification) {
    return prefillStrategyEntries.stream()
               .filter(entry -> formSpecification.equals(entry.getFormSpecification()))
               .toList();
  }

  @Override
  public Optional<PrefillStrategyEntry> exists(PrefillStrategyEntry entry) {
    return prefillStrategyEntries.stream().filter(existing ->
                                                       existing.isGlobal() == entry.isGlobal() &&
                                                       Objects.equals(existing.getFormSpecification(), entry.getFormSpecification()) &&
                                                       existing.getType().equals(entry.getType()) &&
                                                       Objects.equals(existing.getSourceAttribute(), entry.getSourceAttribute()) &&
                                                       existing.getOptions().equals(entry.getOptions()))
               .findFirst();
  }

  @Override
  public void delete(PrefillStrategyEntry entry) {
    prefillStrategyEntries.remove(entry);
  }
}
