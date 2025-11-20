package org.perun.registrarprototype.persistance;

import java.util.List;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormSpecification;

public interface DestinationRepository {
  List<Destination> getDestinationsForForm(FormSpecification formSpecification);
  List<Destination> getGlobalDestinations();
  Destination createDestination(Destination destination);
  void removeDestination(Destination destination);
  void saveAll(List<Destination> destinations);
  boolean exists(Destination destination);
}
