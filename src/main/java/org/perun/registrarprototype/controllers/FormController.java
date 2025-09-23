package org.perun.registrarprototype.controllers;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.CurrentUserProvider;
import org.perun.registrarprototype.services.FormService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forms")
public class FormController {

  private final FormService formService;
  private final CurrentUserProvider currentUserProvider;

  public FormController(FormService formService, CurrentUserProvider currentUserProvider) {
      this.formService = formService;
      this.currentUserProvider = currentUserProvider;
  }

  @PostMapping("/create")
  public ResponseEntity<Form> createForm(@RequestParam int groupId, @RequestBody List<FormItem> items, HttpServletRequest request) {
      String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION); // replace with spring security/filter after done with testing
      CurrentUser user = currentUserProvider.getCurrentUser(authHeader);
      try {
        formService.createForm(user, groupId, items);
      } catch (FormItemRegexNotValid e) {
        throw new RuntimeException(e);
      } catch (InsufficientRightsException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
      return ResponseEntity.ok().build();
  }

  // TODO probably do not mix request params and body
  @PostMapping("/setModule")
  public ResponseEntity<List<AssignedFormModule>> setModules(@RequestParam int formId, @RequestBody List<AssignedFormModule> modules, HttpServletRequest request) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    CurrentUser user = currentUserProvider.getCurrentUser(authHeader);
    List<AssignedFormModule> setModules;
    try {
      setModules = formService.setModules(user, formId, modules);
    } catch (InsufficientRightsException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(setModules);
  }

}
