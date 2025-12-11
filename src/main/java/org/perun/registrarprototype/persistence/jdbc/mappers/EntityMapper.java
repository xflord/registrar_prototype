package org.perun.registrarprototype.persistence.jdbc.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Decision;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.models.ScriptModule;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.persistence.jdbc.entities.ApplicationEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.AssignedFormModuleEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.AuditEventEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.DecisionEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.DestinationEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.FormItemDataEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.FormItemEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.FormModuleOption;
import org.perun.registrarprototype.persistence.jdbc.entities.FormSpecificationEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.FormTransitionEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.FormTypeRef;
import org.perun.registrarprototype.persistence.jdbc.entities.ItemDefinitionEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.PrefillStrategyEntryEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.PrefillStrategyOption;
import org.perun.registrarprototype.persistence.jdbc.entities.ScriptModuleEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.SourceStateRef;
import org.perun.registrarprototype.persistence.jdbc.entities.SubmissionEntity;
import org.perun.registrarprototype.services.events.RegistrarEvent;

public class EntityMapper {
  public static ApplicationEntity toEntity(Application domain) {
    ApplicationEntity entity = new ApplicationEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setIdmUserId(domain.getIdmUserId());
    entity.setFormSpecificationId(domain.getFormSpecificationId());
    entity.setState(domain.getState().name());
    entity.setType(domain.getType().toString());
    entity.setRedirectUrl(domain.getRedirectUrl());
    entity.setSubmissionId(domain.getSubmissionId());
    return entity;
  }

  public static FormItemDataEntity toFormItemDataEntity(Integer applicationId, FormItemData formItemData) {
    FormItemDataEntity entity = new FormItemDataEntity();
    entity.setApplicationId(applicationId);
    entity.setFormItemId(formItemData.getFormItem().getId());
    entity.setValue(formItemData.getValue());
    entity.setPrefilledValue(formItemData.getPrefilledValue());
    entity.setIdentityAttributeValue(formItemData.getIdentityAttributeValue());
    entity.setIdmAttributeValue(formItemData.getIdmAttributeValue());
    entity.setValueAssured(formItemData.isValueAssured());
    return entity;
  }

  public static DecisionEntity toEntity(Decision domain) {
    DecisionEntity entity = new DecisionEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setApplicationId(domain.getApplicationId());
    entity.setApproverId(domain.getApproverId());
    entity.setApproverName(domain.getApproverName());
    entity.setMessage(domain.getMessage());
    entity.setTimestamp(domain.getTimestamp());
    entity.setDecisionType(domain.getDecisionType() != null ? domain.getDecisionType().name() : null);
    return entity;
  }

  public static DestinationEntity toEntity(Destination domain) {
    DestinationEntity entity = new DestinationEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setUrn(domain.getUrn());
    entity.setFormSpecificationId(domain.getFormSpecificationId());
    entity.setGlobal(domain.isGlobal());
    return entity;
  }

  public static FormItemEntity toEntity(FormItem domain) {
    FormItemEntity entity = new FormItemEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setFormId(domain.getFormSpecificationId());
    entity.setShortName(domain.getShortName());
    entity.setParentId(domain.getParentId());
    entity.setOrdNum(domain.getOrdNum());
    entity.setHiddenDependencyItemId(domain.getHiddenDependencyItemId());
    entity.setDisabledDependencyItemId(domain.getDisabledDependencyItemId());
    entity.setItemDefinitionId(domain.getItemDefinitionId());
    return entity;
  }

  public static AssignedFormModuleEntity toEntity(AssignedFormModule domain) {
    AssignedFormModuleEntity entity = new AssignedFormModuleEntity();
    entity.setFormId(domain.getFormId());
    entity.setModuleName(domain.getModuleName());

    // Convert options from map to entity format
    if (domain.getOptions() != null) {
      List<FormModuleOption> options = domain.getOptions().entrySet().stream()
          .map(entry -> new FormModuleOption(null, entry.getKey(), entry.getValue()))
          .collect(Collectors.toList());
      entity.setOptions(options);
    }

    return entity;
  }

  public static FormSpecificationEntity toEntity(FormSpecification form) {
    FormSpecificationEntity entity = new FormSpecificationEntity();
    if (form.getId() > 0) {
      entity.setId(form.getId());
    }
    entity.setVoId(form.getVoId());
    entity.setGroupId(form.getGroupId());
    entity.setAutoApprove(form.isAutoApprove());
    entity.setAutoApproveExtension(form.isAutoApproveExtension());
    return entity;
  }

  public static FormTransitionEntity toEntity(FormTransition domain) {
    FormTransitionEntity entity = new FormTransitionEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    if (domain.getSourceFormSpecification() != null) {
      entity.setSourceFormSpecificationId(domain.getSourceFormSpecification().getId());
    }
    if (domain.getTargetFormSpecification() != null) {
      entity.setTargetFormSpecificationId(domain.getTargetFormSpecification().getId());
    }
    // TODO needs to be handled later
    entity.setPosition(0);
    entity.setTargetFormState(domain.getTargetFormState());
    entity.setTransitionType(domain.getType());

    // Convert source states to SourceStateRef entities
    List<SourceStateRef> sourceStateRefs = new ArrayList<>();
    if (domain.getSourceFormStates() != null) {
      domain.getSourceFormStates().forEach(sourceStateRef -> {
        sourceStateRefs.add(new SourceStateRef(null, sourceStateRef.toString()));
      });
    }
    entity.setSourceStates(sourceStateRefs);

    return entity;
  }

  public static ItemDefinitionEntity toEntity(ItemDefinition domain) {
    ItemDefinitionEntity entity = new ItemDefinitionEntity();

    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }

    entity.setFormSpecificationId(domain.getFormSpecificationId());
    entity.setDisplayName(domain.getDisplayName());
    entity.setType(domain.getType());
    entity.setUpdatable(domain.getUpdatable());
    entity.setRequired(domain.getRequired());
    entity.setValidator(domain.getValidator());
    entity.setDestinationId(domain.getDestinationId());
    entity.setHidden(domain.getHidden());
    entity.setDisabled(domain.getDisabled());
    entity.setDefaultValue(domain.getDefaultValue());
    entity.setGlobal(domain.isGlobal());
    entity.setFormTypes(domain.getFormTypes().stream().map(type -> new FormTypeRef(-1, type.toString())).collect(
        Collectors.toSet()));
    return entity;
  }

  public static PrefillStrategyEntryEntity toEntity(PrefillStrategyEntry domain) {
    PrefillStrategyEntryEntity entity = new PrefillStrategyEntryEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setFormSpecificationId(domain.getFormSpecificationId());
    entity.setType(domain.getType());
    entity.setSourceAttribute(domain.getSourceAttribute());
    entity.setGlobal(domain.isGlobal());

    List<PrefillStrategyOption> options = new ArrayList<>();
    if (domain.getOptions() != null) {
      for (Map.Entry<String, String> entry : domain.getOptions().entrySet()) {
        PrefillStrategyOption option = new PrefillStrategyOption();
        option.setOptionKey(entry.getKey());
        option.setOptionValue(entry.getValue());
        options.add(option);
      }
    }
    entity.setOptions(options);

    return entity;
  }

  public static ScriptModuleEntity toEntity(ScriptModule domain) {
    ScriptModuleEntity entity = new ScriptModuleEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setName(domain.getName());
    entity.setScript(domain.getScript());
    return entity;
  }

  public static SubmissionEntity toEntity(Submission domain) {
    SubmissionEntity entity = new SubmissionEntity();
    if (domain.getId() > 0) {
      entity.setId(domain.getId());
    }
    entity.setTimestamp(domain.getTimestamp());
    entity.setSubmitterId(domain.getSubmitterId());
    entity.setSubmitterName(domain.getSubmitterName());
    entity.setIdentityIdentifier(domain.getIdentityIdentifier());
    entity.setIdentityIssuer(domain.getIdentityIssuer());
    entity.setIdentityAttributes(domain.getIdentityAttributes());

    return entity;
  }

  public static AuditEventEntity toEntity(RegistrarEvent event) {
    AuditEventEntity entity = new AuditEventEntity();
    entity.setEventName(event.getClass().getSimpleName());
    entity.setCorrelationId(event.getCorrelationId());
    entity.setActor(event.getActor());
    entity.setTimestamp(event.getOccurredAt());
    entity.setContent(event);

    return entity;
  }
}
