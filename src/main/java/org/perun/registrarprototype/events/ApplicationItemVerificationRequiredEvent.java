package org.perun.registrarprototype.events;

import java.util.List;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.FormItemData;

public class ApplicationItemVerificationRequiredEvent extends ApplicationEvent {

  private List<FormItemData> itemsToVerify;

  public ApplicationItemVerificationRequiredEvent(Application application, List<FormItemData> itemToVerify) {
    super(application);
    this.itemsToVerify = itemToVerify;
  }

  @Override
  public String getEventMessage() {
    return "";
  }

  public List<FormItemData> getItemsToVerify() {
    return itemsToVerify;
  }
}
