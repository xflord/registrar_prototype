package org.perun.registrarprototype.services;

import org.junit.jupiter.api.BeforeEach;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.tempImpl.ApplicationRepositoryDummy;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.tempImpl.FormRepositoryDummy;
import org.perun.registrarprototype.services.tempImpl.PerunIntegrationDummy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@ImportAutoConfiguration(exclude = OAuth2ClientAutoConfiguration.class) // this is so that autowiring the oauth client does not break all the tests
@Import(TestConfig.class)
public class GenericRegistrarServiceTests {
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
