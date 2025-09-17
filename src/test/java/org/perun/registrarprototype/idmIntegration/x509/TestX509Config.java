package org.perun.registrarprototype.idmIntegration.x509;

import org.perun.registrarprototype.services.idmIntegration.perun.PerunRPCConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"test", "x509"})
public class TestX509Config extends PerunRPCConfig {
  // This class simply inherits and makes the PerunConfig's beans available for the test profile.
}
