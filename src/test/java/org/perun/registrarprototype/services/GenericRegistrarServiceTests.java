package org.perun.registrarprototype;

import org.junit.jupiter.api.BeforeEach;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.tempImpl.ApplicationRepositoryDummy;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.tempImpl.FormRepositoryDummy;
import org.perun.registrarprototype.services.ApplicationService;
import org.perun.registrarprototype.services.FormService;
import org.perun.registrarprototype.services.tempImpl.PerunIntegrationDummy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
public class GenericRegistrarTests {
   protected PerunIntegrationDummy perunIntegrationService;
   @Autowired
   protected ApplicationService applicationService;
   @Autowired
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
    }
}
