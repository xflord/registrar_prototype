package org.perun.registrarprototype.plugins;

import java.util.List;
import java.util.Map;
import org.perun.registrarprototype.extension.dto.ApplicationDto;
import org.perun.registrarprototype.extension.dto.CurrentUserDto;
import org.perun.registrarprototype.extension.dto.FormItemDataDto;
import org.perun.registrarprototype.extension.dto.FormType;
import org.perun.registrarprototype.extension.services.IdMService;
import org.perun.registrarprototype.extension.services.modules.FormModule;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

@Extension
    public class TestModule implements FormModule {

      @Autowired
      private IdMService idMService;


      @Override
      public String getName() {
        return "TestModule";
      }

      @Override
      public void canBeSubmitted(CurrentUserDto sess, FormType type, Map<String, String> options) {

      }

      @Override
      public void afterFormItemsPrefilled(CurrentUserDto sess, FormType type,
                                          List<FormItemDataDto> prefilledFormItems) {

      }

      @Override
      public void afterApplicationSubmitted(ApplicationDto application) {

      }

      @Override
      public void beforeApproval(ApplicationDto application) {

      }

      @Override
      public void onApproval(ApplicationDto application) {

      }

      @Override
      public void onAutoApproval(ApplicationDto application) {

      }

      @Override
      public void onRejection(ApplicationDto application) {

      }

      @Override
      public List<String> getRequiredOptions() {
        this.idMService.reserveLogin("TestModule", "login");
        return List.of();
      }
    }