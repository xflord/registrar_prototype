package org.perun.registrarprototype.repositories.tempImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.repositories.DestinationRepository;
import org.springframework.stereotype.Component;

@Component
public class DestinationRepositoryDummy implements DestinationRepository {
  private static final Map<Integer, Set<String>> destinations = new HashMap<>();

  @Override
  public Set<String> getDestinationsForForm(FormSpecification formSpecification) {
    return destinations.get(formSpecification.getId());
  }

  @Override
  public Set<String> getGlobalDestinations() {
    return destinations.get(null);
  }

  @Override
  public String createDestination(FormSpecification formSpecification, String destination) {
    Integer key = formSpecification == null ? null : formSpecification.getId();
    destinations.putIfAbsent(key, new HashSet<>());
    destinations.get(key).add(destination);
    return destination;
  }

  @Override
  public void removeDestination(FormSpecification formSpecification, String destination) {
    Integer key = formSpecification == null ? null : formSpecification.getId();
    if (destinations.containsKey(key)) {
      destinations.get(key).remove(destination);
    }
  }

  @Override
  public void saveAll(Map<FormSpecification, Set<String>> destinationsToSave) {
    for (FormSpecification formSpecification : destinationsToSave.keySet()) {
      destinationsToSave.get(formSpecification).forEach(destination -> createDestination(formSpecification, destination));
    }
  }

  @Override
  public boolean exists(FormSpecification formSpecification, String destination) {
    Integer key = formSpecification == null ? null : formSpecification.getId();
    Set<String> formDestinations =  destinations.get(key);
    return formDestinations != null && formDestinations.contains(destination);
  }
}
