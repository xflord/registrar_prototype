package org.perun.registrarprototype.core.idmIntegration.x509;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.metacentrum.perun.openapi.PerunRPC;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.core.services.idmIntegration.perun.PerunRPCConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = PerunRPCConfig.class)
@ActiveProfiles({"test", "x509"})
public class PerunX509Test {
  @Autowired
  private PerunRPC rpc;

  @Test
  public void testPerunRpcBeanIsCorrectlyConfigured() {
      assertNotNull(rpc);
  }
}
