package org.perun.registrarprototype.services;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.repositories.FormRepository;
import org.perun.registrarprototype.repositories.FormRepositoryDummy;
import org.perun.registrarprototype.security.CurrentUser;

public class FormService {
  private final FormRepository formRepository = new FormRepositoryDummy();
  private AuthorizationService authorizationService = new AuthorizationServiceImpl();

  public FormService() {}

  public FormService(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  public Form createForm(CurrentUser sess, int groupId, List<FormItem> items)
      throws FormItemRegexNotValid, InsufficientRightsException {
    if (!authorizationService.isAuthorized(sess, groupId)) {
      // 403
      throw new InsufficientRightsException("You are not authorized to create a form for this group");
    }

    for (FormItem item : items) {
      if (item.getConstraint() != null && !item.getConstraint().isEmpty()) {
        try {
          Pattern.compile(item.getConstraint());
        } catch (PatternSyntaxException e) {
          throw new FormItemRegexNotValid("Cannot compile regex: " + item.getConstraint(), item);
        }
      }
    }

    Form form = new Form(formRepository.getNextId(), groupId, items);
    formRepository.save(form);
    return form;
  }

}
