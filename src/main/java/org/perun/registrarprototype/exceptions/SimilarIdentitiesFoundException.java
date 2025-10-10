package org.perun.registrarprototype.exceptions;

import java.util.List;
import org.perun.registrarprototype.models.Identity;

public class SimilarIdentitiesFoundException extends RuntimeException {
  List<Identity> identities;

  public SimilarIdentitiesFoundException(String message) {
    super(message);
  }

  public SimilarIdentitiesFoundException(String message, List<Identity> identities) {
    super(message);
    this.identities = identities;
  }

  public List<Identity> getIdentities() {
    return identities;
  }
}
