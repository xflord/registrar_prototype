package org.perun.registrarprototype.services;

import org.junit.jupiter.api.BeforeEach;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.FormModuleRepository;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.tempImpl.FormModuleRepositoryDummy;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.config.TestConfig;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
import org.perun.registrarprototype.services.idmIntegration.perun.PerunIdMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ImportAutoConfiguration(exclude = {OAuth2ClientAutoConfiguration.class}) // this is so that autowiring the oauth client does not break all the tests
@Import(TestConfig.class)
@ActiveProfiles( {"test", "basic-auth"} )
public class GenericRegistrarServiceTests {
   @Autowired
   protected ApplicationServiceImpl applicationService;
   @Autowired
   protected FormServiceImpl formService;
   @Autowired
   protected SessionProvider sessionProvider;
   @Autowired
   protected IdMService idmService;
   @Autowired
   protected ApplicationRepository applicationRepository;
   @Autowired
   protected FormRepository formRepository;
   protected FormModuleRepository formModuleRepository;

   private static int groupId = 0;
   protected Submission submission;

   @BeforeEach
    void setUp() {
        FormModuleRepositoryDummy formModuleRep = new FormModuleRepositoryDummy();
        formModuleRep.reset();
        formModuleRepository = formModuleRep;
        submission = new Submission();
        submission.setIdentityIssuer("testIssuer");
        submission.setIdentityIdentifier("testIdentifier");
    }

    protected int getGroupId() {
     return groupId++;
    }
}
