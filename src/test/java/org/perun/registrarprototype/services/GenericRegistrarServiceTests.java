package org.perun.registrarprototype.services;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemType;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.FormModuleRepository;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.tempImpl.FormModuleRepositoryDummy;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.config.TestConfig;
import org.perun.registrarprototype.services.idmIntegration.IdMService;
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
   protected ApplicationService applicationService;
   @Autowired
   protected FormService formService;
   @Autowired
   protected SessionProvider sessionProvider;
   @Autowired
   protected IdMService idmService;
   @Autowired
   protected ApplicationRepository applicationRepository;
   @Autowired
   protected FormRepository formRepository;
   protected FormModuleRepository formModuleRepository;

   private static int groupId = 1;
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

    /**
     * Helper method to create an ItemDefinition via formService and get its ID.
     * Creates a non-global ItemDefinition for form-specific use.
     */
    protected ItemDefinition createItemDefinition(ItemType type, String displayName, Boolean required, String validator) {
      ItemDefinition itemDef = new ItemDefinition();
      itemDef.setType(type);
      itemDef.setDisplayName(displayName);
      itemDef.setRequired(required != null ? required : false);
      itemDef.setValidator(validator);
      itemDef.setFormTypes(Set.of(FormSpecification.FormType.INITIAL, FormSpecification.FormType.EXTENSION));
      itemDef.setHidden(ItemDefinition.Condition.NEVER);
      itemDef.setDisabled(ItemDefinition.Condition.NEVER);
      itemDef.setGlobal(false);
      return formService.createItemDefinition(itemDef);
    }

    /**
     * Helper method to create a FormItem with an ItemDefinition.
     */
    protected FormItem createFormItem(int formId, ItemDefinition itemDef, int ordNum) {
      FormItem formItem = new FormItem();
      formItem.setFormId(formId);
      formItem.setItemDefinition(itemDef);
      formItem.setShortName(itemDef.getDisplayName());
      formItem.setOrdNum(ordNum);
      formItem.setParentId(null);
      formItem.setHiddenDependencyItemId(null);
      formItem.setDisabledDependencyItemId(null);
      return formItem;
    }
}
