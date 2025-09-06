package org.perun.registrarprototype;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.repositories.ApplicationRepository;
import org.perun.registrarprototype.repositories.ApplicationRepositoryDummy;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.FormRepositoryDummy;
import org.perun.registrarprototype.services.ApplicationService;
import org.perun.registrarprototype.services.PerunIntegrationDummy;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RegistrarPrototypeApplicationTests {

  @Test
  void contextLoads() {
  }

  @Test
  void registerUserIntoGroup() {
    // lets ignore spring autowiring for now
    PerunIntegrationDummy perunIntegrationService = new PerunIntegrationDummy();
    ApplicationService applicationService = new ApplicationService();
    ApplicationRepository applicationRepository = new ApplicationRepositoryDummy();
    FormRepository formRepository = new FormRepositoryDummy();

    int userId = 1;
    FormItem item1 = new FormItem(1, "test");
    FormItem item2 = new FormItem(2, "test");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);
    Form form = new Form(formRepository.getNextId(), groupId, List.of(item1, item2));
    formRepository.save(form);
    FormItemData formItemData1 = new FormItemData(item1.getId(), "test1");
    FormItemData formItemData2 = new FormItemData(item2.getId(), "test2");

    Application application = applicationService.registerUserToGroup(userId, groupId, form.getId(),
        List.of(formItemData1, formItemData2));

    assert application.getState() == ApplicationState.APPROVED;
    assert perunIntegrationService.isUserMemberOfGroup(groupId, userId);
  }

}
