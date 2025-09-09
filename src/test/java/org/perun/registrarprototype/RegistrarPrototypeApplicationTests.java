package org.perun.registrarprototype;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.exceptions.InvalidApplicationDataException;
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
import org.perun.registrarprototype.services.FormService;
import org.perun.registrarprototype.services.PerunIntegrationDummy;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RegistrarPrototypeApplicationTests {
  // lets ignore spring autowiring for now
   private PerunIntegrationDummy perunIntegrationService;
   private ApplicationService applicationService;
   private FormService formService;
   private ApplicationRepository applicationRepository;
   private FormRepository formRepository;

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

  @Test
  void contextLoads() {
  }

  @Test
  void registerUserIntoGroup() throws Exception {

    int userId = 1;
    FormItem item1 = new FormItem(1, "test");
    FormItem item2 = new FormItem(2, "test");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);
    Form form = new Form(formRepository.getNextId(), groupId, List.of(item1, item2));
    formRepository.save(form);
    FormItemData formItemData1 = new FormItemData(item1.getId(), "test1");
    FormItemData formItemData2 = new FormItemData(item2.getId(), "test2");

    Application application = applicationService.registerUserToGroup(userId, groupId,
        List.of(formItemData1, formItemData2));

    assert application.getState() == ApplicationState.APPROVED;
    assert perunIntegrationService.isUserMemberOfGroup(groupId, userId);
  }

  @Test
  void createFormWithoutConstraints() throws Exception {
    FormItem item1 = new FormItem(1, "test");
    FormItem item2 = new FormItem(2, "test");

    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId, List.of(item1, item2));

    Form createdForm = formRepository.findById(form.getId()).orElse(null);
    assert createdForm == form;
    assert createdForm.getGroupId() == groupId;
    assert createdForm.getItems().size() == 2;
    assert createdForm.getItems().contains(item1);
    assert createdForm.getItems().contains(item2);
  }

  @Test
  void createFormCorrectConstraints() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    Form form = formService.createForm(null, groupId, List.of(item1));

    Form createdForm = formRepository.findById(form.getId()).orElse(null);
    assert createdForm == form;
    assert createdForm.getGroupId() == groupId;
    assert createdForm.getItems().size() == 1;
    assert createdForm.getItems().contains(item1);
  }

  @Test
  void createFormIncorrectConstraints() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", false, "^[a-zA-Z0-9._%+-+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    assertThrows(FormItemRegexNotValid.class, () -> formService.createForm(null, groupId, List.of(item1)));
  }

  @Test
  void applyMissingRequiredItems() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", true, "");
    FormItem item2 = new FormItem(2, "test");

    int groupId = 1;
    perunIntegrationService.createGroup(groupId);
    formService.createForm(null, groupId, List.of(item1, item2));

    FormItemData formItemData1 = new FormItemData(item2.getId(), "test2");

    InvalidApplicationDataException ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(-1, groupId, List.of(formItemData1)));
    assert ex.getErrors().getFirst().itemId() == 1;
    assert ex.getErrors().getFirst().message().equals("Field " + item1.getLabel() + " is required");
  }

  @Test
  void applyMissingRequiredItemsIncorrectConstraints() throws Exception {

    FormItem item1 = new FormItem(1, "email", "email", true, "");
    FormItem item2 = new FormItem(2, "email", "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    formService.createForm(null, groupId, List.of(item1, item2));

    FormItemData formItemData = new FormItemData(item2.getId(), "incorrectTestgmail.com");

    InvalidApplicationDataException ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(-1, groupId, List.of(formItemData)));
    assert ex.getErrors().getFirst().itemId() == 1;
    assert ex.getErrors().getFirst().message().equals("Field " + item1.getLabel() + " is required");
    assert ex.getErrors().get(1).itemId() == 2;
    assert ex.getErrors().get(1).message().equals("Item " + item2.getLabel() + " must match constraint " + item2.getConstraint());
  }

  @Test
  void applyWithIncorrectItemConstraints() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    formService.createForm(null, groupId, List.of(item1));

    FormItemData formItemData1 = new FormItemData(item1.getId(), "incorrectTestgmail.com");

    InvalidApplicationDataException ex = assertThrows(InvalidApplicationDataException.class, () -> applicationService.applyForMembership(-1, groupId, List.of(formItemData1)));
    assert ex.getErrors().getFirst().itemId() == 1;
    assert ex.getErrors().getFirst().message().equals("Item " + item1.getLabel() + " must match constraint " + item1.getConstraint());
  }

  @Test
  void applyWithCorrectItemConstraints() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    formService.createForm(null, groupId, List.of(item1));

    FormItemData formItemData1 = new FormItemData(item1.getId(), "test@gmail.com");

    Application app = applicationService.applyForMembership(-1, groupId, List.of(formItemData1));

    Application createdApp = applicationRepository.findById(app.getId()).orElse(null);

    assert createdApp == app;
    assert createdApp.getState() == ApplicationState.PENDING;
    assert createdApp.getGroupId() == groupId;
  }

  @Test
  void approveApplication() throws Exception {
    FormItem item1 = new FormItem(1, "email", "email", false, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    int groupId = 1;
    perunIntegrationService.createGroup(groupId);

    formService.createForm(null, groupId, List.of(item1));

    FormItemData formItemData1 = new FormItemData(item1.getId(), "test@gmail.com");

    Application app = applicationService.applyForMembership(-1, groupId, List.of(formItemData1));

    Application createdApp = applicationRepository.findById(app.getId()).orElse(null);

    assert createdApp == app;
    assert createdApp.getState() == ApplicationState.PENDING;

    applicationService.approveApplication(null, createdApp.getId());

    createdApp = applicationRepository.findById(app.getId()).orElse(null);
    assert createdApp != null;
    assert createdApp.getState().equals(ApplicationState.APPROVED);
  }

}
