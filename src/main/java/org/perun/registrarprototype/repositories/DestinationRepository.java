package org.perun.registrarprototype.repositories;

import java.util.Map;
import java.util.Set;
import org.perun.registrarprototype.models.FormSpecification;

public interface DestinationRepository {
  Set<String> getDestinationsForForm(FormSpecification formSpecification);
  Set<String> getGlobalDestinations();
  String createDestination(FormSpecification formSpecification, String destination);
  void removeDestination(FormSpecification formSpecification, String destination);
  void saveAll(Map<FormSpecification, Set<String>> destinations);
  boolean exists(FormSpecification formSpecification, String destination);
}
