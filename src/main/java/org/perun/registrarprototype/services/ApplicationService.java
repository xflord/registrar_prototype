package org.perun.registrarprototype.services;

import java.util.List;
import org.perun.registrarprototype.exceptions.IdmObjectNotExistsException;
import org.perun.registrarprototype.exceptions.InsufficientRightsException;
import org.perun.registrarprototype.models.Application;
import org.perun.registrarprototype.models.ApplicationForm;
import org.perun.registrarprototype.models.ApplicationState;
import org.perun.registrarprototype.models.Decision;
import org.perun.registrarprototype.models.FormSpecification;
import org.perun.registrarprototype.models.FormItemData;
import org.perun.registrarprototype.models.FormTransition;
import org.perun.registrarprototype.models.Identity;
import org.perun.registrarprototype.models.Requirement;
import org.perun.registrarprototype.models.Submission;
import org.perun.registrarprototype.models.SubmissionContext;
import org.perun.registrarprototype.models.SubmissionResult;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;

/**
 * Service for managing application lifecycle, including submission, approval, rejection, and form loading.
 */
public interface ApplicationService {

  /**
   * Approves an application, executes form module hooks, propagates the approval to the IdM system,
   * and consolidates submissions for the user.
   *
   * @param sess authenticated session token
   * @param applicationId ID of the application to approve
   * @param message approval message from the approver
   * @return the approved application
   * @throws InsufficientRightsException if the user is not authorized to approve the application
   */
  Application approveApplication(RegistrarAuthenticationToken sess, int applicationId, String message)
      throws InsufficientRightsException;

  /**
   * Rejects an application, executes form module hooks, and emits a rejection event.
   *
   * @param sess authenticated session token
   * @param applicationId ID of the application to reject
   * @param message rejection message from the approver
   * @return the rejected application
   * @throws InsufficientRightsException if the user is not authorized to reject the application
   */
  Application rejectApplication(RegistrarAuthenticationToken sess, int applicationId, String message)
      throws InsufficientRightsException;

  /**
   * Requests changes to an application, transitions the application state, and emits a changes requested event.
   *
   * @param sess authenticated session token
   * @param applicationId ID of the application for which changes are requested
   * @param message message describing the requested changes
   * @return the application with updated state
   * @throws InsufficientRightsException if the user is not authorized to request changes
   */
  Application requestChanges(RegistrarAuthenticationToken sess, int applicationId, String message)
      throws InsufficientRightsException;

  /**
   * Submits an application to each of the supplied forms, aggregates them under one submission object along with
   * information about the submitter, automatically submits applications (to forms that are marked as such), retrieves
   * forms to which to redirect, and displays result messages.
   *
   * @param submissionData submission context containing prefilled form data and redirect URL
   * @return submission result with custom messages, redirect information, and the created submission
   */
  SubmissionResult applyForMemberships(SubmissionContext submissionData);

  /**
   * Validates the input data for the form, marks assured prefilled items, and submits an application.
   *
   * @param applicationForm context containing the form, group ID, prefilled items, and application type
   * @param submission the submission object to associate with the application
   * @param redirectUrl URL to redirect to after submission
   * @return the created application
   */
  Application applyForMembership(ApplicationForm applicationForm, Submission submission, String redirectUrl);

  /**
   * Retrieves all applications from the repository.
   *
   * @return list of all applications
   */
  List<Application> getAllApplications();

  /**
   * Retrieves an application by its ID.
   *
   * @param id application ID
   * @return the application, or null if not found
   */
  Application getApplicationById(int id);

  /**
   * Retrieves all decisions associated with a specific application.
   *
   * @param applicationId ID of the application
   * @return list of decisions for the application
   */
  List<Decision> getDecisionsByApplicationId(int applicationId);

  /**
   * Retrieves the most recent decision for a specific application based on timestamp.
   *
   * @param applicationId ID of the application
   * @return the latest decision, or null if no decisions exist
   */
  Decision getLatestDecisionByApplicationId(int applicationId);

  /**
   * Performs auto-submission of a form marked for auto submit. Uses data from principal and the previous submission to
   * fill out form item data.
   *
   * @param autoSubmitTransition the form to auto-submit
   * @param submissionData submission context from the triggering submission
   */
  void autoSubmitForm(FormTransition autoSubmitTransition, SubmissionContext submissionData);

  /**
   * Updates application data.
   * @param applicationId
   * @param itemData
   */
  void updateApplicationData(int applicationId, List<FormItemData> itemData);

  /**
   * Entry point from GUI. For a given group, determines which forms to display to the user, prefills form items,
   * and returns the submission context to the GUI.
   *
   * @param groupIds set of groups the user wants to apply for membership in
   * @param redirectUrl URL to redirect to after submission
   * @param checkSimilarUsers if true, checks for similar identities and throws exception if found; set to false after user decides on consolidation
   * @return prefilled submission context with redirect URL and individual prefilled form data
   */
  SubmissionContext loadForms(List<Requirement> requirements, String redirectUrl, boolean checkSimilarUsers)
      throws IdmObjectNotExistsException;

  /**
   * Generates prefilled form item data for the form and its type, calls module hooks, and ensures the validity of form
   * item visibility.
   *
   * @param sess authenticated session token
   * @param formSpecification the form to load
   * @param type the form type (INITIAL, EXTENSION, etc.)
   * @return list of prefilled form item data
   */
  List<FormItemData> loadForm(RegistrarAuthenticationToken sess, FormSpecification formSpecification, FormSpecification.FormType type);

  /**
   * To be called from GUI in case the user modifies items that could provide information to connect identities
   * with (e.g. email address, display name).
   *
   * @param itemData form item data to check for similar identities
   * @return list of similar identities found in the IdM system
   */
  List<Identity> checkForSimilarIdentities(List<FormItemData> itemData);

  List<Application> getApplicationsForForm(int formId, List<ApplicationState> states);
}

