package org.perun.registrarprototype;

import org.junit.jupiter.api.BeforeEach;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.ApplicationRepositoryDummy;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.FormRepositoryDummy;
import org.perun.registrarprototype.services.ApplicationService;
import org.perun.registrarprototype.services.FormService;
import org.perun.registrarprototype.services.PerunIntegrationDummy;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GenericRegistrarTests {
  // lets ignore spring autowiring for now
   protected PerunIntegrationDummy perunIntegrationService;
   protected ApplicationService applicationService;
   protected FormService formService;
   protected ApplicationRepository applicationRepository;
   protected FormRepository formRepository;

   @BeforeEach
    void setUp() {
        perunIntegrationService = new PerunIntegrationDummy();
        ApplicationRepositoryDummy appRep = new ApplicationRepositoryDummy();
        appRep.reset();
        applicationRepository = appRep;
        FormRepositoryDummy formRep = new FormRepositoryDummy();
        formRep.reset();
        formRepository = formRep;
        applicationService = new ApplicationService(new AuthorizationServiceDummy());
        formService = new FormService(new AuthorizationServiceDummy());
    }
}
