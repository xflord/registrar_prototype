package org.perun.registrarprototype.controllers;

import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.exceptions.FormItemRegexNotValid;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Form;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.perun.registrarprototype.security.SessionProvider;
import org.perun.registrarprototype.services.FormServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forms")
public class FormController {

  private final FormServiceImpl formService;
  private final SessionProvider sessionProvider;

  public FormController(FormServiceImpl formService, SessionProvider sessionProvider) {
      this.formService = formService;
      this.sessionProvider = sessionProvider;
  }

  @PostMapping("/create")
  public ResponseEntity<Form> createForm(@RequestParam int groupId, @RequestBody List<FormItem> items) {
      try {
        formService.createForm(groupId, items);
      } catch (FormItemRegexNotValid e) {
        throw new RuntimeException(e);
      } catch (InsufficientRightsException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
      return ResponseEntity.ok().build();
  }

  // TODO probably do not mix request params and body
  @PostMapping("/setModule")
  public ResponseEntity<List<AssignedFormModule>> setModules(@RequestParam int formId, @RequestBody List<AssignedFormModule> modules) {
    RegistrarAuthenticationToken user = sessionProvider.getCurrentSession();
    List<AssignedFormModule> setModules;
    try {
      setModules = formService.setModules(user, formId, modules);
    } catch (InsufficientRightsException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(setModules);
  }

  @GetMapping
  public ResponseEntity<List<Form>> getForms() {
    return ResponseEntity.ok(formService.getAllFormsWithItems());
  }

  @GetMapping("/me")
  public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal CurrentUser principal) {
    RegistrarAuthenticationToken session = sessionProvider.getCurrentSession();

    if (!session.isAuthenticated()) {
      return  ResponseEntity.ok(Map.of("authenticated", session.isAuthenticated()));
    }
    System.out.println(principal.getRoles());
    return ResponseEntity.ok(principal.getAttributes());
  }
}
