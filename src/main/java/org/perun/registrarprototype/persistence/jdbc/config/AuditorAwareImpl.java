package org.perun.registrarprototype.persistence.jdbc.config;

import java.util.Optional;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.SessionProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

/**
 * Resolves data used for `createdBy` and `modifiedBy`
 * TODO determine whether audit columns are necessary in the db if we have audit_log
 */
@Service
public class AuditorAwareImpl implements AuditorAware<String> {

  private final SessionProvider sessionProvider;
  public AuditorAwareImpl(SessionProvider sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  @Override
  public Optional<String> getCurrentAuditor() {
    // TODO determine what really to return
    if (sessionProvider.getCurrentSession() != null &&
            sessionProvider.getCurrentSession().isAuthenticated()) {
      CurrentUser currentUser = sessionProvider.getCurrentSession().getPrincipal();
      return Optional.of(currentUser.name());
    }
    return Optional.empty();
  }
}
