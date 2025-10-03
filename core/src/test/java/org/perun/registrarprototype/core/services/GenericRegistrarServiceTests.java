package org.perun.registrarprototype.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.perun.registrarprototype.core.repositories.ApplicationRepository;
import org.perun.registrarprototype.core.repositories.FormModuleRepository;
import org.perun.registrarprototype.core.repositories.FormRepository;
import org.perun.registrarprototype.core.repositories.tempImpl.FormModuleRepositoryDummy;
import org.perun.registrarprototype.core.security.CurrentUserProvider;
import org.perun.registrarprototype.core.services.config.TestConfig;
import org.perun.registrarprototype.core.services.tempImpl.PerunIntegrationDummy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ImportAutoConfiguration(exclude = OAuth2ClientAutoConfiguration.class) // this is so that autowiring the oauth client does not break all the tests
@Import(TestConfig.class)
@ActiveProfiles( {"test", "basic-auth"} )
public class GenericRegistrarServiceTests {
   protected PerunIntegrationDummy perunIntegrationService;
   @Autowired
   protected ApplicationService applicationService;
   @Autowired
   protected FormService formService;
   @Autowired
   protected FormModuleService formModuleService;
   @Autowired
   protected CurrentUserProvider currentUserProvider;
   @Autowired
   protected ApplicationRepository applicationRepository;
   @Autowired
   protected FormRepository formRepository;
   protected FormModuleRepository formModuleRepository;

   private static int groupId = 0;

   @BeforeEach
    void setUp() {
        perunIntegrationService = new PerunIntegrationDummy();
        FormModuleRepositoryDummy formModuleRep = new FormModuleRepositoryDummy();
        formModuleRep.reset();
        formModuleRepository = formModuleRep;
    }

    protected int getGroupId() {
     return groupId++;
    }
}
