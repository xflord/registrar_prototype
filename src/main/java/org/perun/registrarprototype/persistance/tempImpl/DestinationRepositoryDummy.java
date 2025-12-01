package org.perun.registrarprototype.persistance.tempImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.persistance.DestinationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!jdbc")
@Component
public class DestinationRepositoryDummy implements DestinationRepository {
  private static final List<Destination> destinations = new ArrayList<>();
  private static int currId = 1;

  @Override
  public Optional<Destination> findById(int id) {
    return destinations.stream().filter(dest -> dest.getId() == id).findFirst();
  }

  @Override
  public List<Destination> getDestinationsForForm(FormSpecification formSpecification) {
    return destinations.stream()
               .filter(dest -> dest.getFormSpecificationId() != null && dest.getFormSpecificationId() == formSpecification.getId())
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
      destination.setId(currId++);
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
