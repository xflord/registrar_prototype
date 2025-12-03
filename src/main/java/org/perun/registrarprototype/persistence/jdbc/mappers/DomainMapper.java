package org.perun.registrarprototype.persistence.jdbc.mappers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.AssignedFormModule;
import org.perun.registrarprototype.models.Decision;
import org.perun.registrarprototype.models.Destination;
import org.perun.registrarprototype.models.FormItem;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.models.ItemDefinition;
import org.perun.registrarprototype.models.ItemTexts;
import org.perun.registrarprototype.models.PrefillStrategyEntry;
import org.perun.registrarprototype.models.Requirement;
import org.perun.registrarprototype.models.ScriptModule;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.persistence.jdbc.entities.ApplicationEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.AssignedFormModuleEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.DecisionEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.DestinationEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.FormItemDataEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.FormItemEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.FormModuleOption;
import org.perun.registrarprototype.persistence.jdbc.entities.FormSpecificationEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.FormTransitionEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.ItemDefinitionEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.ItemTextsEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.PrefillStrategyEntryEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.PrefillStrategyOption;
import org.perun.registrarprototype.persistence.jdbc.entities.PrefillStrategyRef;
import org.perun.registrarprototype.persistence.jdbc.entities.ScriptModuleEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.SourceStateRef;
import org.perun.registrarprototype.persistence.jdbc.entities.SubmissionEntity;
import org.perun.registrarprototype.persistence.jdbc.entities.SubmissionIdentityAttribute;
import org.springframework.stereotype.Component;

// TODO probably could be moved into the entity classes
public class DomainMapper {
  
  public static PrefillStrategyEntry toDomain(PrefillStrategyEntryEntity entity) {
    Map<String, String> options = new HashMap<>();
    for (PrefillStrategyOption option : entity.getOptions()) {
      options.put(option.getOptionKey(), option.getOptionValue());
    }

    return new PrefillStrategyEntry(
        entity.getId(),
        entity.getType(),
        options,
        entity.getSourceAttribute(),
        entity.getFormSpecificationId(),
        entity.getGlobal() != null ? entity.getGlobal() : false
    );
  }

  public static ItemDefinition toDomain(ItemDefinitionEntity entity) {
    Map<Locale, ItemTexts> texts = new HashMap<>();
    for (ItemTextsEntity textEntity : entity.getTexts()) {
      Locale locale = Locale.forLanguageTag(textEntity.getLocale());
      texts.put(locale, new ItemTexts(
          textEntity.getLabel(),
          textEntity.getHelp(),
          textEntity.getError()
      ));
    }

    // Extract prefill strategy IDs from the refs
    List<Integer> prefillStrategyIds = entity.getPrefillStrategyRefs().stream()
                                           .sorted(Comparator.comparing(PrefillStrategyRef::getPosition))
        .map(PrefillStrategyRef::getPrefillStrategyEntryId)
        .collect(Collectors.toList());

    return new ItemDefinition(
        entity.getId(),
        entity.getFormSpecificationId(),
        entity.getDisplayName(),
        entity.getType(),
        entity.getUpdatable(),
        entity.getRequired(),
        entity.getValidator(),
        prefillStrategyIds,
        entity.getDestinationId(),
        entity.getFormTypes(),
        texts,
        entity.getHidden(),
        entity.getDisabled(),
        entity.getDefaultValue(),
        entity.getGlobal() != null ? entity.getGlobal() : false
    );
  }

  public static FormSpecification toDomain(FormSpecificationEntity entity) {

    return new FormSpecification(
        entity.getId(),
        entity.getVoId(),
        entity.getGroupId(),
        toDomainList(entity.getFormItemEntities()),
        entity.getAutoApprove() != null ? entity.getAutoApprove() : false,
        entity.getAutoApproveExtension() != null ? entity.getAutoApproveExtension() : false
    );
  }

  public static Destination toDomain(DestinationEntity entity) {
    return new Destination(
        entity.getId(),
        entity.getUrn(),
        entity.getFormSpecificationId(),
        entity.getGlobal() != null ? entity.getGlobal() : false
    );
  }

  public static List<FormItem> toDomainList(List<FormItemEntity> entities) {
    return entities.stream()
               .sorted(Comparator.comparing(FormItemEntity::getOrdNum)) // TODO doable quicker in db query
        .map(DomainMapper::toDomain)
        .collect(Collectors.toList());
  }
  
  public static FormItem toDomain(FormItemEntity entity) {
    return new FormItem(
        entity.getId(),
        entity.getFormId(), // formSpecificationId
        entity.getShortName(),
        entity.getParentId(),
        entity.getOrdNum() != null ? entity.getOrdNum() : 0,
        entity.getHiddenDependencyItemId(),
        entity.getDisabledDependencyItemId(),
        entity.getItemDefinitionId()
    );
  }
  
  public static Application toDomain(ApplicationEntity entity) {
    // This is a simplified mapping - in a real implementation you might need to fetch related data
    return new Application(
        entity.getId() != null ? entity.getId() : 0,
        entity.getIdmUserId(),
        entity.getFormSpecificationId(),
        new ArrayList<>(), // Will be populated by the repository
        entity.getRedirectUrl(),
        FormSpecification.FormType.valueOf(entity.getType()),
        ApplicationState.valueOf(entity.getState()),
        entity.getSubmissionId()
    );
  }
  
  public static Submission toDomain(SubmissionEntity entity) {
    // Parse identityAttributes from JSON string to Map
    Map<String, String> identityAttributes = new HashMap<>();
    if (entity.getIdentityAttributes() != null && !entity.getIdentityAttributes().isEmpty()) {
      for (SubmissionIdentityAttribute attribute : entity.getIdentityAttributes()) {
        identityAttributes.put(attribute.getAttributeKey(), attribute.getAttributeValue());
      }
    }
    
    return new Submission(
        entity.getId() != null ? entity.getId() : 0,
        new ArrayList<>(), // Will be populated by the repository
        entity.getTimestamp(),
        entity.getSubmitterId(),
        entity.getSubmitterName(),
        identityAttributes
    );
  }
  
  public static Decision toDomain(DecisionEntity entity) {
    return new Decision(
        entity.getId() != null ? entity.getId() : 0,
        entity.getApplicationId(),
        entity.getApproverId(),
        entity.getApproverName(),
        entity.getMessage(),
        entity.getTimestamp(),
        entity.getDecisionType() != null ? Decision.DecisionType.valueOf(entity.getDecisionType()) : null
    );
  }
  
  public static FormTransition toDomain(FormTransitionEntity entity) {
    List<Requirement.TargetState> sourceStates = entity.getSourceStates().stream()
        .map(SourceStateRef::getSourceState)
        .collect(Collectors.toList());
    
    return new FormTransition(
        null, // sourceFormSpecification will be set by the repository
        null, // targetFormSpecification will be set by the repository
        sourceStates,
        entity.getTargetFormState(),
        entity.getTransitionType()
    );
  }
  
  public static FormItemData toDomain(FormItemDataEntity entity, FormItem formItem) {
    return new FormItemData(
        formItem,
        entity.getValue(),
        entity.getPrefilledValue(),
        entity.getIdentityAttributeValue(),
        entity.getIdmAttributeValue(),
        entity.getValueAssured() != null ? entity.getValueAssured() : false
    );
  }

  public static AssignedFormModule toDomain(AssignedFormModuleEntity entity) {
    // Convert options from entity format to map
    Map<String, String> options = new HashMap<>();
    for (FormModuleOption option : entity.getOptions()) {
      options.put(option.getOptionKey(), option.getOptionValue());
    }

    // Determine module name based on entity type
    String moduleName = entity.getModuleName();

    return new AssignedFormModule(
        entity.getFormId(),
        moduleName,
        null, // formModule will be set by the service layer
        options
    );
  }

  public static ScriptModule toDomain(ScriptModuleEntity entity) {
    return new ScriptModule(
        entity.getId(),
        entity.getName(),
        entity.getScript()
    );
  }
}