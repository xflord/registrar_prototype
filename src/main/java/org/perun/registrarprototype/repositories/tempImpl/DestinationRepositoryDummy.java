package org.perun.registrarprototype.repositories.tempImpl;

import java.util.ArrayList;
import java.util.List;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.repositories.DestinationRepository;
import org.springframework.stereotype.Component;

@Component
public class DestinationRepositoryDummy implements DestinationRepository {
  private static final List<Destination> destinations = new ArrayList<>();

  @Override
  public List<Destination> getDestinationsForForm(FormSpecification formSpecification) {
    return destinations.stream()
               .filter(dest -> dest.getFormSpecification() != null && dest.getFormSpecification().equals(formSpecification))
               .toList();
  }

  @Override
  public List<Destination> getGlobalDestinations() {
    return destinations.stream()
               .filter(Destination::isGlobal)
               .toList();
  }

  @Override
  public Destination createDestination(Destination destination) {
    if (!destinations.contains(destination)) {
      destinations.add(destination);
    }
    return destination;
  }

  @Override
  public void removeDestination(Destination destination) {
    destinations.remove(destination);
  }

  @Override
  public void saveAll(List<Destination> destinations) {
    for (Destination destination : destinations) {
      this.createDestination(destination);
    }
  }

  @Override
  public boolean exists(Destination destination) {
    return destinations.contains(destination);
  }
}
