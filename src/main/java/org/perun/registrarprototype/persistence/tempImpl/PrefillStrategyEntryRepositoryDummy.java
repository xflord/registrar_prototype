package org.perun.registrarprototype.persistence.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.persistence.PrefillStrategyEntryRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dummy")
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
  public List<PrefillStrategyEntry> findAllById(List<Integer> ids) {
    return prefillStrategyEntries.stream()
               .filter(entry -> ids.contains(entry.getId()))
               .toList();
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
               .filter(entry -> formSpecification.getId() == entry.getFormSpecificationId())
               .toList();
  }

  @Override
  public Optional<PrefillStrategyEntry> exists(PrefillStrategyEntry entry) {
    return prefillStrategyEntries.stream().filter(existing ->
                                                       existing.isGlobal() == entry.isGlobal() &&
                                                       Objects.equals(existing.getFormSpecificationId(), entry.getFormSpecificationId()) &&
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
