*** Settings ***
Documentation     INFUND-2612 As a partner I want to have a overview of where I am in the process and what outstanding tasks I have to complete so that I can understand our project setup steps
...
...               INFUND-2613 As a lead partner I need to see an overview of project details for my project so that I can edit the project details in order for Innovate UK to be able to assign an appropriate Monitoring Officer
...
...               INFUND-2614 As a lead partner I need to provide a target start date for the project so that Innovate UK has correct details for my project setup
...
...               INFUND-2620 As a partner I want to provide my organisation's finance contact details so that the correct person is assigned to the role
...
...               INFUND-3382 As a partner I want to be able to view our project details after they have been submitted so that I can use them for reference
...
...               INFUND-2621 As a contributor I want to be able to review the current Project Setup status of all partners in my project so I can get an indication of the overall status of the consortium
...
...               INFUND-4583 As a partner I want to be able to continue with Project Setup once I have supplied my Project Details so that I don't have to wait until all partner details are submitted before providing further information
...
...               INFUND-4428 As a Partner, I should have access to the various Project Setup sections when they become available, so that I can access them when it is valid to
...
...               INFUND-5610 As a user I want to check the selected Project Manager value persists
...
...               INFUND-5368 Once finance contact is submitted, do not allow it to be changed again
...
...               INFUND-3483 As a lead partner I want to invite a new contributor to our organisation so that they can be assigned as our project manager
...
...               INFUND-3550 As a potential Project Manager, I can receive an email with a Join link, so that I can start the registration process and collaborate with the project
...
...               INFUND-3530 As a potential Finance Contact, I can click on a link to register and to become a Finance Contact for a Partner Organisation, so that I can start collaborating on the Project
...
...               INFUND-3554 As a potential Project Manager, I can click on a link to register and to become a Project Manager for the Project, so that I can start collaborating on the Project
...
...               INFUND-5898 As a partner I want to be able to change my Finance Contact in Project Setup so that I can submit updates to our partner details as appropriate
...
...               INFUND-5856 As an internal user I want to see a view of each project's submitted Project Details and the Finance contacts so I can use these for reference throughout Project Setup
...
...               INFUND-5827 As a lead partner I want my Project Setup dashboard to inform me when all the Project Details and Finance Contacts are provided so that I know if any tasks are outstanding
...
...               INFUND-5979 Consortium table - Project details - should update when partners submit their Finance Contacts
...
...               INFUND-5805 As a successful applicant I want to be able to view the grant terms and conditions from my dashboard so that I can confirm what I agreed to in the application
...
...               INFUND-6781 Spend Profile is accessible before preliminary sections are completed
...
...               INFUND-7174 Not eligible partner should not have access to his Bank details page
...
...               INFUND-6882 Email validation done when valid is input selected for PM selection in project details
...
...               INFUND-7432 Terms and Conditions of grant offer takes you to the IFS ts and cs, not the grant ones
...
...               INFUND-9062 Validation missing when inviting self as finance contact or PM
...
...               IFS-2642 Resend invites in Project Setup
...
...               IFS-2920 Project details: Project location per partner
Suite Setup       Custom suite setup
Suite Teardown    Custom suite teardown
Force Tags        Project Setup  Applicant
Resource          PS_Common.robot

*** Variables ***
${invitedFinanceContact}  ${test_mailbox_one}+invitedfinancecontact@gmail.com
${pmEmailId}  ${user_ids['${PS_SP_APPLICATION_PM_EMAIL}']}

*** Test Cases ***
Internal finance can see Project details not yet completed
    [Tags]  HappyPath
    When the user logs-in in new browser           &{internal_finance_credentials}
    And the user navigates to the page             ${internal_competition_status}
    And the user clicks the button/link            css = #table-project-status tr:nth-child(1) td:nth-child(2) a
    Then the user should see the element           jQuery = #no-project-manager:contains("Not yet completed")
    And the user should see the element            jQuery = #project-details-finance tr:nth-child(3) td:nth-child(2):contains("Not yet completed")

Competition admin can see Project details not yet completed
    [Documentation]    INFUND-5856
    [Tags]  HappyPath
    [Setup]  Log in as a different user            &{Comp_admin1_credentials}
    Given the user navigates to the page           ${internal_competition_status}
    And the user should not see the element        css = #table-project-status tr:nth-child(1) td.status.ok a    #Check here that there is no Green-Check
    When the user clicks the button/link           css = #table-project-status tr:nth-child(1) td:nth-child(2) a
    Then the competition admin should see that their Project details aren't completed

Status updates correctly for internal user's table    # This uses the Elbow grease project
    [Documentation]    INFUND-4049, INFUND-5507, INFUND-5543
    [Tags]
    Given the user navigates to the page    ${internal_competition_status}
    And the competition admin should see the status of each project setup stage
    # Internal user can view project details via the clickable 'hour glass' for Project details
    When the user clicks the button/link    css = #table-project-status tr:nth-of-type(1) td:nth-of-type(1).status.waiting a
    Then the user should see the element    jQuery = h1:contains("Project details")
    And the user clicks the button/link     link = Projects in setup
    And the user should see the element     css = #table-project-status tr:nth-of-type(1) td:nth-of-type(2).status.waiting
    And the user should see the element     css = #table-project-status > tbody > tr:nth-child(1) > td:nth-child(2)  # Project details

Non-lead partner can see the project setup page
    [Documentation]    INFUND-2612, INFUND-2621, INFUND-4428, INFUND-5827, INFUND-5805, INFUND-7432
    [Tags]  HappyPath
    Given log in as a different user                &{collaborator1_credentials}
    When The user clicks the button/link            link = ${PROJECT_SETUP_APPLICATION_1_TITLE}
    And the user should see the element             link = view application feedback
    And the user clicks the button/link             link = view the competition terms and conditions
    And the user goes back to the previous page
    And the user should see the element             css = li.require-action:nth-of-type(1)    #Action required, seen by non-lead
    And the user should see the project setup stages
    When the user clicks the button/link            link = View the status of partners
    Then the user should be redirected to the correct page    ${project_in_setup_page}/team-status
    And the user should see the element             jQuery = h1:contains("Project team status")
    And the user should see the element             css = #table-project-status tr:nth-of-type(1) td.status.action:nth-of-type(1)

Links to other sections in Project setup dependent on project details (applicable for Lead/ partner)
    [Documentation]    INFUND-4428
    [Tags]
    When the user navigates to the page        ${project_in_setup_page}
    Then the user should not see the element   link = Monitoring Officer
    And the user should not see the element    link = Bank details
    And the user should not see the element    link = Finance checks
    And the user should not see the element    link = Spend profile
    And the user should not see the element    link = Grant offer letter

Non-lead partner can see the application overview
    [Documentation]    INFUND-2612
    [Tags]
    [Setup]    the user navigates to the page        ${project_in_setup_page}
    When the user clicks the button/link             link = view application feedback
    Then the user should see the element             jQuery = .success-alert:contains("Congratulations, your application has been successful") ~ h2:contains("Application details")

Lead partner can see the project setup page
    [Documentation]    INFUND-2612, INFUND-2621, INFUND-5827, INFUND-5805
    [Tags]
    [Setup]    log in as a different user           &{lead_applicant_credentials}
    When the user navigates to the page             ${project_in_setup_page}
    And the user should see the project setup stages
    And the user should see the element             css = li.require-action:nth-of-type(1)    #Action required, seen by lead
    When the user clicks the button/link            link = View the status of partners
    Then the user should be redirected to the correct page    ${project_in_setup_page}/team-status
    And the user should see the element             jQuery = h1:contains("Project team status")
    And the user should see the element             css = #table-project-status tr:nth-of-type(1) td.status.action:nth-of-type(1)

Lead partner can click the Dashboard link
    [Documentation]    INFUND-4426
    [Tags]
    When the user clicks the button/link    link = Dashboard
    Then the user should see the element    jQuery = h2:contains("Set up your project")

Lead partner can see the application feedback overview
    [Documentation]    INFUND-2612
    [Tags]
    Given the user navigates to the page    ${project_in_setup_page}
    When the user clicks the button/link    link = view application feedback
    Then the user should see the element    jQuery = .success-alert:contains("Congratulations, your application has been successful") ~ h2:contains("Application details")

Lead partner is able to see finances without an error
    [Documentation]  INFUND-7634
    [Tags]
    Given the user clicks the button/link  jQuery = button:contains("Finances summary")
    When the user clicks the button/link   link = View finances
    And the user should see the element    jQuery = h2:contains("Finance summary")
    Then the user clicks the button/link   link = Application summary

Lead partner can see the overview of the project details
    [Documentation]    INFUND-2613
    [Tags]
    Given the user navigates to the page   ${project_in_setup_page}
    When the user clicks the button/link   link = Project details
    Then the user should see the element   jQuery = p:contains("Please supply the following details for your project and the team")
    And the user should see the element    link = Target start date
    And the user should see the element    link = Correspondence address
    And the user should see the element    link = Project Manager
    And the user should see the element    jQuery = h2:contains("Partner details")

Lead partner can change the Start Date
    [Documentation]    INFUND-2614
    [Tags]  HappyPath
    [Setup]    Log in as a different user           &{lead_applicant_credentials}
    Given the user navigates to the page            ${project_in_setup_details_page}
    Given the user clicks the button/link           link = Target start date
    And the duration should be visible
    When the user enters text to a text field       id = projectStartDate_year    2013
    And the user clicks the button/link                 jQuery = .govuk-button:contains("Save")
    Then the user should see a field and summary error  Please enter a future date.
    And the user shouldn't be able to edit the day field as all projects start on the first of the month
    When the user enters text to a text field       id = projectStartDate_month    1
    And the user enters text to a text field        id = projectStartDate_year    ${nextyear}
    And Mouse Out                                   id = projectStartDate_year
    And wait for autosave
    When the user clicks the button/link            jQuery = .govuk-button:contains("Save")
    Then The user should see the element            jQuery = h1:contains("Project details")
    And the user should see the element             jQuery = td:contains("1 Jan ${nextyear}")
    Then the matching status checkbox is updated    project-details    1    yes
    [Teardown]    the user changes the start date   ${nextyear}

Option to invite a project manager
    [Documentation]    INFUND-3483
    [Tags]  HappyPath
    Given the user navigates to the page               ${project_in_setup_page}
    And the user clicks the button/link                link = Project details
    And the user clicks the button/link                link = Project Manager
    And the user should see the element                jQuery = .govuk-hint:contains("Who will be the Project Manager for your project?")
    When the user selects the radio button             projectManager    new
    Then the user should see the element               id = invite-project-manager
    When the user selects the radio button             projectManager    projectManager1
    Then the user should not see the element           id = project-manager    # testing that the element disappears when the option is deselected
    [Teardown]    the user selects the radio button    projectManager    new

Inviting project manager server side validations
    [Documentation]    INFUND-3483, INFUND-9062
    [Tags]
    When the user clicks the button/link             id = invite-project-manager
    Then the user should see a field error           Please enter a valid name.
    And the user should see a field error            Please enter an email address.
    When the user enters text to a text field        id = name-project-manager    Steve Smith
    And the user enters text to a text field         id = email-project-manager    ${lead_applicant}
    And the user clicks the button/link              id = invite-project-manager
    Then the user should see a field error           You cannot invite yourself to the project.

Inviting project manager client side validations
    [Documentation]    INFUND-3483, INFUND-6882
    [Tags]
    When the user enters text to a text field            id = name-project-manager    John Smith
    And Set Focus To Element                             jQuery = .govuk-button:contains("Save")
    Then the user should not see the element             jQuery = .govuk-error-message:contains("Please enter a valid name.")
    When the user enters text to a text field            id = email-project-manager    test
    And Set Focus To Element                             jQuery = .govuk-button:contains("Save")
    Then the user should not see the element             jQuery = .govuk-error-message:contains("Please enter an email address.")
    And the user should see a field error                ${enter_a_valid_email}
    When the user selects the radio button               projectManager    projectManager1
    Then the user should not see the element             jQuery = .govuk-error-message:contains("Please enter an email address.")
    And the user should not see the element              jQuery = .govuk-error-message:contains("Please enter a valid name.")
    When the user selects the radio button               projectManager    new
    And the user enters text to a text field             id = email-project-manager    test@example.com
    And Set Focus To Element                             jQuery = .govuk-button:contains("Save")
    Then the user should not see the element             jQuery = .govuk-error-message:contains("Please enter an email address.")
    And the user should not see the element              jQuery = .govuk-error-message:contains("Please enter a valid name.")
    And the user should not see an error in the page

Partner invites a project manager
    [Documentation]    INFUND-3483
    [Tags]  HappyPath
    When the user enters text to a text field    id = name-project-manager    John Smith
    And the user enters text to a text field    id = email-project-manager    ${test_mailbox_one}+invitedprojectmanager@gmail.com
    And the user clicks the button/link    id = invite-project-manager
    Then the user should be redirected to the correct page    ${project_in_setup_page}

Lead Applicant resends the invite to the Project manager
    [Documentation]  IFS-2642
    [Tags]  HappyPath
    When the user resends and clicks the button    Cancel
    Then the user resends and clicks the button    Resend
    [Teardown]  logout as user

Invited project manager registration validation
    [Documentation]  INFUND-3550 INFUND-3554
    [Tags]  HappyPath
    Given the user accepts invitation                   ${TEST_MAILBOX_ONE}+invitedprojectmanager@gmail.com  ${PROJECT_SETUP_COMPETITION_NAME}: Project Manager invitation for project  managing the project
    When the user clicks the button/link                css = button[type = "submit"][name = "create-account"]
    Then The user should see a field and summary error  ${enter_a_first_name}
    And the user should see a field and summary error   ${enter_a_last_name}
    And the user should see a field and summary error   To create a new account you must agree to the website terms and conditions.
    And the user should see a field and summary error   Please enter your password.

Invited project manager registration flow
    [Documentation]  INFUND-3550 INFUND-3554
    [Tags]  HappyPath
    Given the user selects the checkbox                 termsAndConditions
    And the invited user fills the create account form  Bob  Jones
    And the user cannot see a validation error in the page
    When the invited user signs in                      ${TEST_MAILBOX_ONE}+invitedprojectmanager@gmail.com  Bob  Jones
    Then the user should see the element                jQuery = .progress-list:contains("${PROJECT_SETUP_APPLICATION_1_TITLE}")
    And the user should not see the element             css = .my-applications .in-progress  #applications in progress section

Invited project manager shows on the project manager selection screen
    [Documentation]    INFUND-3554
    [Tags]
    When the user clicks the button/link    link = ${PROJECT_SETUP_APPLICATION_1_TITLE}
    And the user clicks the button/link     link = Project details
    And the user clicks the button/link     link = Project Manager
    Then the user should see the element    jQuery = label:contains("Bob Jones")

Lead partner selects a project manager
    [Documentation]    INFUND-2616 INFUND-2996 INFUND-5610
    [Tags]  HappyPath
    Given the user navigates to the page             ${project_in_setup_details_page}
    When the user clicks the button/link             link = Project Manager
    Then the user clicks the button/link             jQuery = .govuk-button:contains("Save")
    And the user should see a validation error       You need to select a Project Manager before you can continue.
    When the user selects the radio button           projectManager    projectManager1
    And the user should not see the element          jQuery = .govuk-error-message:contains("You need to select a Project Manager before you can continue.")
    And the user clicks the button/link              jQuery = .govuk-button:contains("Save")
    Then the user should see the element             jQuery = td:contains("Project Manager") ~ td:contains("Steve Smith")
    And the user clicks the button/link              link = Project Manager
    And the user should see the element              css = #projectManager1:checked ~ label
    And the user selects the radio button            projectManager    projectManager2
    And the user clicks the button/link              jQuery = .govuk-button:contains("Save")
    Then the user should be redirected to the correct page    ${project_in_setup_page}
    And the user should see the element              jQuery = td:contains("Project Manager") ~ td:contains("Elmo Chenault")
    And the matching status checkbox is updated      project-details    3    yes

Lead partner can change the project address
    [Documentation]    INFUND-3157 INFUND-2165
    [Tags]  HappyPath
    Given the user navigates to the page             ${project_in_setup_details_page}
    And the user clicks the button/link              link = Correspondence address
    When the user clicks the button/link             jQuery = .govuk-button:contains("Save")
    And the user should see a field and summary error   Search using a valid postcode or enter the address manually.
    And the user enters text to a text field         id = addressForm.postcodeInput  BS1 4NT
    And the user clicks the button/link              id = postcode-lookup
    And the user selects the index from the drop-down menu  1  id=addressForm.selectedPostcodeIndex
    And the user clicks the button/link              jQuery = .govuk-button:contains("Save address")
    And the user should see the address data
    When the user clicks the button/link             link = Correspondence address
    And the user clicks the button/link              jQuery = .govuk-button:contains("Save address")
    Then the user should see the element             jQuery = td:contains("Correspondence address") ~ td:contains("Montrose House 1, Neston, CH64 3RU")

Project details can be submitted with PM, project address and start date
    [Documentation]    INFUND-4583
    [Tags]
    Given the user should see the element    css = #start-date-status.yes
    And the user should see the element      css = #project-address-status.yes
    And the user should see the element      css = #project-manager-status.yes

Non lead partner invites finance contact
    [Documentation]    INFUND-2620, INFUND-5368, INFUND-5827, INFUND-5979, INFUND-4428 IFS-285
    [Tags]  HappyPath
    When Log in as a different user             &{collaborator1_credentials}
    Then the user navigates to the page         ${project_in_setup_page}
    When the user clicks the button/link        link = View the status of partners
    Then the user should not see the element    css = #table-project-status tr:nth-of-type(2) td.status.ok:nth-of-type(1)
    And the user clicks the button/link         link = Set up your project
    And the user clicks the button/link         link = Project details
    When the user clicks the button/link        link = Select finance contact
    And the user selects the radio button       financeContact  new
    Then the user enters text to a text field   css = #name-finance-contact  LudlowFinContact
    And the user enters text to a text field    css = #email-finance-contact  ${test_mailbox_one}+ludlowfincont@gmail.com
    When the user clicks the button/link        jQuery = button:contains("Invite to project")
    Then the user should see the element        jQuery = label[for = "financeContact3"]:contains("Pending")
    And the user clicks the button/link         jQuery = .govuk-button:contains("Save finance contact")

Invited Fin Contact for non lead partner
    [Documentation]    INFUND-2620, INFUND-5368, INFUND-5827, INFUND-5979, INFUND-4428 IFS-285
    [Tags]  HappyPath
    [Setup]  Logout as user
    Given the invitee is able to assign himself as Finance Contact  ${test_mailbox_one}+ludlowfincont@gmail.com  ${PROJECT_SETUP_COMPETITION_NAME}: Finance contact invitation for project ${PROJECT_SETUP_APPLICATION_1}  providing finance details  Ludlow's  FinContact
    When log in as a different user       &{collaborator1_credentials}
    Then the user navigates to the page   ${project_in_setup_page}/details
    And the user should see the element   link = Ludlow's FinContact
    And select the project location       Ludlow
    When the user clicks the button/link  link = View the status of partners
    Then the user should see the element  css = #table-project-status tr:nth-of-type(3) td.status.ok:nth-of-type(1)

    # Please note that the following Test Cases regarding story INFUND-7090, have to remain in Project Details suite
    # and not in Bank Details. Because for this scenario there are testing data for project 4.
Non lead partner not eligible for funding
    [Documentation]    INFUND-7090, INFUND-7174
    [Tags]
    Given log in as a different user            &{collaborator1_credentials}
    When the user navigates to the page         ${project_in_setup_page}
    And the user should see the element         css = ul li.complete:nth-child(1)
    Then the user should not see the element    css = ul li.require-action:nth-child(3)
    When The user navigates to the page and gets a custom error message     ${project_in_setup_page}/bank-details    ${403_error_message}
    When the user navigates to the page         ${project_in_setup_page}
    And the user clicks the button/link         link = View the status of partners
    Then the user should be redirected to the correct page    ${project_in_setup_team_status_page}
    And the user should see the element         css = #table-project-status tr:nth-child(3) td.status.na:nth-child(4)

Other partners can see who needs to provide Bank Details
    [Documentation]    INFUND-7090
    [Tags]  HappyPath
    [Setup]    log in as a different user   &{lead_applicant_credentials}
    Given the user navigates to the page    ${project_in_setup_team_status_page}
    Then the user should see the element    css = #table-project-status tr:nth-child(3) td.status.na:nth-child(4)
    And the user should see the element     jQuery = #table-project-status tr:nth-child(2) td:nth-child(4):contains("")

Option to invite a finance contact
    [Documentation]    INFUND-3579
    [Tags]  HappyPath
    Given the user navigates to the page             ${project_in_setup_page}
    And the user clicks the button/link              link = Project details
    And the user clicks the button/link              jQuery = td:contains("${FUNDERS_PANEL_APPLICATION_1_LEAD_ORGANISATION_NAME}") ~ td a:contains("Select finance contact")
    When the user selects the radio button           financeContact    new
    Then the user should see the element             id = invite-finance-contact
    When the user selects the radio button           financeContact    financeContact1
    Then the user should not see the element         id = invite-finance-contact    # testing that the element disappears when the option is deselected
    [Teardown]    the user selects the radio button  financeContact    new

Inviting finance contact server side validations
    [Documentation]    INFUND-3483, INFUND-9062
    [Tags]
    When the user clicks the button/link             id = invite-finance-contact
    Then the user should see a field error           Please enter a valid name.
    And the user should see a field error            Please enter an email address.
    When the user enters text to a text field        id = name-finance-contact    Steve Smith
    And the user enters text to a text field         id = email-finance-contact  ${lead_applicant_credentials["email"]}
    And the user clicks the button/link              id = invite-finance-contact
    Then the user should see a field error           You cannot invite yourself to the project.

Inviting finance contact client side validations
    [Documentation]    INFUND-3483
    [Tags]
    When the user enters text to a text field            id = name-finance-contact    John Smith
    And Set Focus To Element                             jQuery = .govuk-button:contains("Save finance contact")
    Then the user should not see the element             jQuery = .govuk-error-message:contains("Please enter a valid name.")
    When the user enters text to a text field            id = email-finance-contact    test
    And Set Focus To Element                             jQuery = .govuk-button:contains("Save finance contact")
    Then the user should see a field error               ${enter_a_valid_email}
    When the user enters text to a text field            id = email-finance-contact    test@example.com
    And Set Focus To Element                             jQuery = .govuk-button:contains("Save finance contact")
    Then the user should not see the element             jQuery = .govuk-error-message:contains("Please enter a valid email address.")
    And the user should not see the element              jQuery = .govuk-error-message:contains("Please enter a valid name.")

Partner invites a finance contact
    [Documentation]    INFUND-3579
    [Tags]  HappyPath
    When the user enters text to a text field    id = name-finance-contact    John Smith
    And the user enters text to a text field    id = email-finance-contact  ${invitedFinanceContact}
    And the user clicks the button/link    id = invite-finance-contact
    Then the user should be redirected to the correct page    ${project_in_setup_page}

Lead applicant resends the invite to the Finance contact
    [Documentation]  IFS-2642
    [Tags]  HappyPath
    When the user resends and clicks the button    Cancel
    Then the user resends and clicks the button    Resend
    [Teardown]  logout as user

Invited finance contact registration flow
    [Documentation]  INFUND-3524 INFUND-3530
    [Tags]  HappyPath
    Given the user accepts invitation                   ${invitedFinanceContact}  ${PROJECT_SETUP_COMPETITION_NAME}: Finance contact invitation for project ${PROJECT_SETUP_APPLICATION_1}   providing finance details
    And the invited user fills the create account form  John  Smith
    When the invited user signs in                      ${invitedFinanceContact}  John  Smith
    Then the user should see the element                jQuery = .progress-list:contains("${PROJECT_SETUP_APPLICATION_1_TITLE}")

Invited finance contact shows on the finance contact selection screen
    [Documentation]    INFUND-3530
    [Tags]
    Given the user navigates to the page  ${server}/project-setup/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/details
    And the user clicks the button/link   jQuery = td:contains("${EMPIRE_LTD_NAME}") ~ td a:contains("Select finance contact")
    Then the user should see the element  jQuery = #finance-contact-section:contains("John Smith")

Lead partner selects a finance contact
    [Documentation]    INFUND-2620, INFUND-5571, INFUND-5898
    [Tags]  HappyPath
    Then the user navigates to the page                 ${project_in_setup_page}
    And the user clicks the button/link                 link = Project details
    And the user clicks the button/link                 jQuery = td:contains("${FUNDERS_PANEL_APPLICATION_1_LEAD_ORGANISATION_NAME}") ~ td a:contains("Select finance contact")
    And the user should not see duplicated select options
    And the user should not see the text in the page    jQuery = label:contains("Pending")
    And the user selects the radio button               financeContact    financeContact2
    And the user clicks the button/link                 jQuery = .govuk-button:contains("Save finance contact")
    Then the user should be redirected to the correct page    ${project_in_setup_page}
    And the user should see the element                  jQuery = td:contains("Project Manager") ~ td:contains("Elmo Chenault")

Non-lead partner cannot change start date, project manager or project address
    [Documentation]    INFUND-3157
    [Tags]
    Given log in as a different user            &{collaborator1_credentials}
    When the user navigates to the page         ${project_in_setup_page}
    Then the user should not see the element    link = Target start date
    And the user should not see the element     link = Project Manager
    And the user should not see the element     link = Correspondence address

Internal user should see project details are incomplete
    [Documentation]    INFUND-6781
    [Tags]
    [Setup]    log in as a different user    &{Comp_admin1_credentials}
    Given the user navigates to the page     ${internal_competition_status}
    When the user clicks the button/link     jQuery = #table-project-status tr:nth-of-type(1) td:nth-of-type(1).status.waiting
    Then the user should see the element     jQuery = td:contains("Correspondence address") ~ td:contains("Not yet completed")
    And the user should see the element      jQuery = td:contains("Project Manager") ~ td:contains("Not yet completed")

Academic Partner nominates Finance contact
    [Documentation]    INFUND-2620, INFUND-5368, INFUND-5827, INFUND-5979, INFUND-6781
    [Tags]  HappyPath
    [Setup]    Log in as a different user       &{collaborator2_credentials}
    Then the user navigates to the page         ${project_in_setup_page}
    When the user clicks the button/link        link = View the status of partners
    Then the user should not see the element    jQuery = #table-project-status tr:nth-of-type(2) td.status.ok:nth-of-type(1)
    When the user clicks the button/link        link = Set up your project
    Then the user should not see the element    jQuery = li.require-action:nth-child(3)
    When the user clicks the button/link        link = Project details
    And the user clicks the button/link         jQuery = td:contains("${organisationEggsName}") ~ td a:contains("Select finance contact")
    And the user selects the radio button       financeContact    financeContact1
    And the user clicks the button/link         jQuery = .govuk-button:contains("Save finance contact")
    Then the user should be redirected to the correct page    ${project_in_setup_page}
    And the user should see the element         jQuery = td:contains("${organisationEggsName}")
    And select the project location             EGGS
    When the user navigates to the page         ${project_in_setup_page}
    Then the user should see the element        jQuery = li.complete:nth-of-type(1)
    And the user should see the element         jQuery = li.require-action:nth-child(4)
    When the user clicks the button/link        link = View the status of partners
    Then the user should see the element        jQuery = #table-project-status tr:nth-of-type(2) td.status.ok:nth-of-type(1)

Validation for project location
    [Documentation]   IFS-2920
    [Setup]  log in as a different user                 &{lead_applicant_credentials}
    Given the user navigates to the page                ${project_in_setup_details_page}
    Given the user clicks the button/link               jQuery = #project-details-finance td:contains("Empire") ~ td a:contains("Select project location")
    And Set Focus To Element                            id = postcode
    And Set Focus To Element                            link = Contact us
    And the user should see a field error               ${empty_field_warning_message}
    When the user clicks the button/link                css = button[type = "submit"]
    Then the user should see a field and summary error  ${empty_field_warning_message}

Project details submission flow
    [Documentation]    INFUND-3381, INFUND-2621, INFUND-5827
    [Tags]  HappyPath
    [Setup]    log in as a different user  &{lead_applicant_credentials}
    Given the user navigates to the page  ${project_in_setup_details_page}
    And select the project location       Empire
    And the user clicks the button/link   link = Project details
    When all the fields are completed
    And the user navigates to the page    ${project_in_setup_page}
    Then the user should see the element  css = li.complete:nth-of-type(1)

Lead partner can see the status update when all Project details are submitted
    [Documentation]    INFUND-5827
    [Tags]
    When the user navigates to the page    ${project_in_setup_page}
    Then the user should see the element   css = ul li.complete:nth-child(1)
    And the user should see the element    css = ul li.require-action:nth-child(4)
    When the user clicks the button/link   link = View the status of partners
    Then the user should see the element   id = table-project-status
    And the user should see the element    css = #table-project-status tr:nth-of-type(1) td.status.ok:nth-of-type(1)

Project details links are still enabled after submission
    [Documentation]    INFUND-3381
    [Tags]
    Given the user navigates to the page    ${project_in_setup_details_page}
    When all the fields are completed
    Then The user should see the element    link = Target start date
    And the user should see the element     link = Correspondence address
    And the user should see the element     link = Project Manager

All partners can view submitted project details
    [Documentation]    INFUND-3382, INFUND-2621
    [Tags]
    When log in as a different user                  &{collaborator1_credentials}
    And the user navigates to the page               ${project_in_setup_details_page}
    Then the user should see the element             jQuery = td:contains("${organisationLudlowName}")
    When all the fields are completed
    And the user navigates to the page               ${project_in_setup_page}
    And the user clicks the button/link              link = View the status of partners
    Then the user should see the element             css = #table-project-status tr:nth-of-type(1) td.status.ok:nth-of-type(1)
    When log in as a different user                  &{lead_applicant_credentials}
    And the user navigates to the page               ${project_in_setup_details_page}
    Then the user should see the element             jQuery = td:contains("${FUNDERS_PANEL_APPLICATION_1_LEAD_ORGANISATION_NAME}")
    When all the fields are completed
    And the user navigates to the page               ${project_in_setup_page}
    And the user clicks the button/link              link = View the status of partners
    Then the user should see the element             css = #table-project-status tr:nth-of-type(1) td.status.ok:nth-of-type(1)

Non-lead partner cannot change any project details
    [Documentation]    INFUND-2619
    [Tags]
    [Setup]    log in as a different user           &{collaborator1_credentials}
    Given the user navigates to the page            ${project_in_setup_page}
    When the user clicks the button/link            link = Project details
    Then the user should see the element            jQuery = td:contains("Target start date") ~ td:contains("1 Jan ${nextyear}")
    And the user should not see the element         link = Target start date
    And the user should see the element             jQuery = td:contains("Project Manager") ~ td:contains("Elmo Chenault")
    And the user should not see the element         link = Project Manager
    And the user should see the element             jQuery = td:contains("Correspondence address") ~ td:contains("Montrose House 1, Neston, CH64 3RU")
    And the user should not see the element         link = Correspondence address
    When the user navigates to the page and gets a custom error message    ${project_start_date_page}    ${403_error_message}
    When the user navigates to the page and gets a custom error message    ${project_address_page}    ${403_error_message}

Internal user can see the Project details as submitted
    [Documentation]    INFUND-5856
    [Tags]
    [Setup]    log in as a different user    &{Comp_admin1_credentials}
    Given the user navigates to the page     ${internal_competition_status}
    When the user clicks the button/link     css = #table-project-status tr:nth-child(2) td.status.ok a
    Then the user should see the element     css = #project-details
    And the user can see all project details completed
    When the user should see the element     css = #project-details-finance
    And the user can see all finance contacts completed

Invited Finance contact is able to see the Finances
    [Documentation]  IFS-1209
    [Tags]
    [Setup]  log in as a different user   ${invitedFinanceContact}  ${correct_password}
    Given the user navigates to the page  ${server}/project-setup/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/finance-checks
    When the user clicks the button/link  link = your finances
    Then the user should see the element  css = .table-overview
    And the user should not see an error in the page
    When the user clicks the button/link  link = Finance checks
    And the user clicks the button/link   link = project finance overview
    Then the user should see the element  jQuery = h3:contains("Project cost breakdown")
    And the user should not see an error in the page

User is able to accept new site terms and conditions
    [Documentation]  IFS-3093
    [Tags]  MySQL
    [Setup]  Delete user from terms and conditions database   ${pmEmailId}
    Log in as a different user             ${PS_SP_APPLICATION_PM_EMAIL}   ${short_password}
    When the user selects the checkbox     agree
    And the user clicks the button/link    css = button[type = "submit"]
    Then the user should see the element   jQuery = h1:contains(${APPLICANT_DASHBOARD_TITLE})

*** Keywords ***
the user should see a validation error
    [Arguments]    ${ERROR1}
    Set Focus To Element    jQuery = button:contains("Save")
    wait for autosave
    Then the user should see a field error    ${ERROR1}

the matching status checkbox is updated
    [Arguments]    ${table_id}    ${ROW}    ${STATUS}
    the user should see the element    ${table_id}
    the user should see the element    css = #${table_id} tr:nth-of-type(${ROW}) .${STATUS}

the duration should be visible
    the user should see the element    jQuery = h2:contains("Project duration") ~ p:contains("36 months")

the user shouldn't be able to edit the day field as all projects start on the first of the month
    the user should see the element    css = .day [readonly]

the user should see the address data
    Run Keyword If    '${POSTCODE_LOOKUP_IMPLEMENTED}' != 'NO'    the user should see the valid data
    Run Keyword If    '${POSTCODE_LOOKUP_IMPLEMENTED}' == 'NO'    the user should see the dummy data

the user should see the valid data
    the user should see the element           jQuery = td:contains("Correspondence address") ~ td:contains("Am Reprographics, Bristol, BS1 4NT")

the user should see the dummy data
    the user should see the element           jQuery = td:contains("Correspondence address") ~ td:contains("Montrose House 1, Neston, CH64 3RU")

all the fields are completed
    the matching status checkbox is updated  project-details  1  yes
    the matching status checkbox is updated  project-details  2  yes
    the matching status checkbox is updated  project-details  3  yes

the user should not see duplicated select options
    ${NO_OPTIONs} =     Get Element Count    //*[@class="govuk-radios__item"]
    Should Be Equal As Integers    ${NO_OPTIONs}    5    # note that an extra option shows here due to the invited project manager appearing in the list for lead partner organisation members

the user can see all project details completed
    the user should see the element  jQuery = #start-date:contains("1 Jan ${nextyear}")
    the user should see the element  jQuery = #project-address:contains("Montrose House 1, Neston, CH64 3RU")
    the user should see the element  jQuery = #project-manager:contains("Elmo Chenault")

the user can see all finance contacts completed
    the user should see the element  jQuery = #project-details-finance tr:nth-child(1) td:nth-child(2):contains("Elmo Chenault")
    the user should see the element  jQuery = #project-details-finance tr:nth-child(2) td:nth-child(2):contains("Pete Tom")
    the user should see the element  jQuery = #project-details-finance tr:nth-child(3) td:nth-child(2):contains("Ludlow")

Custom suite setup
    ${nextyear} =  get next year
    Set suite variable  ${nextyear}
    Connect to database  @{database}

the invitee is able to assign himself as Finance Contact
    [Arguments]  ${email}  ${title}  ${pattern}  ${name}  ${famName}
    the user accepts invitation                     ${email}  ${title}  ${pattern}
    the invited user fills the create account form  ${name}  ${famName}
    the invited user signs in                       ${email}  ${name}  ${famName}
    the user navigates to the page                  ${server}/project-setup/project/${PROJECT_SETUP_APPLICATION_1_PROJECT}/details/finance-contact?organisation=${organisationLudlowId}
    the user selects the radio button               financeContact  financeContact3
    the user clicks the button/link                 jQuery = button:contains("Save finance contact")

the user accepts invitation
    [Arguments]  ${email}  ${title}  ${pattern}
    the user reads his email and clicks the link  ${email}  ${title}  ${pattern}
    the user should see the element               jQuery = h1:contains("Join a project")
    the user clicks the button/link               link = Create account

the invited user signs in
    [Arguments]  ${email}  ${name}  ${famName}
    the user reads his email and clicks the link    ${email}  Please verify your email address  Dear ${name} ${famName}
    the user should see the element                 jQuery = h1:contains("Account verified")
    the user clicks the button/link                 jQuery = .govuk-button:contains("Sign in")
    Logging in and Error Checking                   ${email}  ${correct_password}

The user resends and clicks the button
    [Arguments]  ${Resend_OR_Cancel}
    The user clicks the button/link    jQuery = label:contains("John Smith") ~ a:contains("Resend invite")
    The user should see the element    jQuery = h2:contains("Resend invite to team member")
    The user clicks the button/link    jQuery = button:contains("${Resend_OR_Cancel}")

Select the project location
    [Arguments]  ${org}
    the user navigates to the page        ${project_in_setup_details_page}
    the user clicks the button/link       jQuery = #project-details-finance td:contains("${org}") ~ td a:contains("Select project location")
    the user enters text to a text field  css = #postcode  ${postcode}
    the user clicks the button/link       css = button[type = "submit"]
    the user clicks the button/link       link = Set up your project

the user should see the project setup stages
    the user should see the element    link = Project details
    the user should see the element    jQuery = h2:contains("Monitoring Officer")
    the user should see the element    jQuery = h2:contains("Bank details")
    the user should see the element    jQuery = h2:contains("Finance checks")
    the user should see the element    jQuery = h2:contains("Spend profile")
    the user should see the element    link = Documents
    the user should see the element    jQuery = h2:contains("Grant offer letter")

the competition admin should see the status of each project setup stage
    the user should see the element    css = #table-project-status > tbody > tr:nth-child(1) > td:nth-child(3)                       # Documents
    the user should see the element    css = #table-project-status > tbody > tr:nth-child(1) > td:nth-child(4)                       # Monitoring Officer
    the user should see the element    css = #table-project-status > tbody > tr:nth-child(1) > td:nth-child(5)                       # Bank details
    the user should see the element    css = #table-project-status > tbody > tr:nth-child(1) > td.govuk-table__cell.status.action    # Finance checks
    the user should see the element    css = #table-project-status > tbody > tr:nth-child(1) > td:nth-child(7)                       # Spend Profile
    the user should see the element    css = #table-project-status > tbody > tr:nth-child(1) > td:nth-child(8)                       # GOL

the competition admin should see that their Project details aren't completed
    the user should see the element    jQuery = p:contains("These project details were supplied by the lead partner on behalf of the project.")
    the user should see the element    jQuery = p:contains("Each partner must provide a finance contact and a project location.")
    the user should see the element    css = #project-details
    the user should see the element    jQuery = #project-address:contains("Not yet completed")
    the user should see the element    jQuery = #no-project-manager:contains("Not yet completed")
    the user should see the element    css = #project-details-finance
    the user should see the element    jQuery = #project-details-finance tr:nth-child(1) td:nth-child(2):contains("Not yet completed")
    the user should see the element    jQuery = #project-details-finance tr:nth-child(2) td:nth-child(2):contains("Not yet completed")
    the user should see the element    jQuery = #project-details-finance tr:nth-child(3) td:nth-child(2):contains("Not yet completed")

Custom suite teardown
    Close browser and delete emails
    Disconnect from database