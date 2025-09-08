package org.perun.registrarprototype.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.CurrentUser;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.services.ApplicationService;
import org.perun.registrarprototype.services.AuthorizationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final AuthorizationService authorizationService;

    public ApplicationController(ApplicationService applicationService, AuthorizationService authorizationService) {
        this.applicationService = applicationService;
        this.authorizationService = authorizationService;
    }

    // --- User applies for membership ---
    @PostMapping("/apply")
    public ResponseEntity<Application> applyForMembership(
            @RequestParam int groupId,
            @RequestBody List<FormItemData> itemData, HttpServletRequest request
    ) {
      String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION); // replace with spring security/filter after done with testing
      CurrentUser user = authorizationService.fetchPrincipal(authHeader);
      Application app = applicationService.applyForMembership(user.id(), groupId, itemData);
      return ResponseEntity.ok(app);
    }

    // --- Manager approves an application ---
    @PostMapping("/{applicationId}/approve")
    public ResponseEntity<Void> approveApplication(@PathVariable int applicationId, HttpServletRequest request) {
      String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION); // replace with spring security/filter after done with testing
      CurrentUser user = authorizationService.fetchPrincipal(authHeader);
      applicationService.approveApplication(applicationId);
      return ResponseEntity.ok().build();
    }

    // --- Direct register (apply + auto-approve) ---
    @PostMapping("/register")
    public ResponseEntity<Application> registerUserToGroup(
            @RequestParam int userId,
            @RequestParam int groupId,
            @RequestBody List<FormItemData> itemData
    ) {
        Application app = applicationService.registerUserToGroup(userId, groupId, itemData);
        return ResponseEntity.ok(app);
    }
}