*** Settings ***
Documentation     Suite description
...
...               IFS-2192 As a Portfolio manager I am able to create an EOI competition
...
...               IFS-2196 As an applicant I am able to apply for an EOI competition
...
...               IFS-2941 As an applicant I am only offered the Research category eligible for the competition
...
...               IFS-4046 Person to organisation acceptance test updates
...
...               IFS-4080 As an applicant I am able to confirm the Research category eligible for the competition
Suite Setup       custom suite setup
Suite Teardown    Close browser and delete emails
Force Tags        CompAdmin  Applicant  Assessor
Resource          ../../../resources/defaultResources.robot
Resource          ../Applicant_Commons.robot
Resource          ../../02__Competition_Setup/CompAdmin_Commons.robot
Resource          ../../07__Assessor/Assessor_Commons.robot

# This suite covers End to End flow of EOI type competition i.e comp creation, applicaiotn submission , assessmnet submission, release feedback
*** Variables ***
${comp_name}         EOI comp
${EOI_application}   EOI Application

*** Test Cases ***
Comp Admin Creates EOI type competition
    [Documentation]  IFS-2192
    [Tags]
    Given Logging in and Error Checking               &{Comp_admin1_credentials}
    Then the competition admin creates competition    ${business_type_id}  ${comp_name}  EOI  ${compType_EOI}  2  GRANT  release-feedback-completion-stage  no  1  true  collaborative

Applicant applies to newly created EOI competition
    [Documentation]  IFS-2192  IFS-2196  IFS-4046 IFS-4080
    [Tags]  MySQL
    When the competitions date changes so it is now Open                 ${comp_name}
    And Log in as a different user               &{assessor_bob_credentials}
    Then logged in user applies to competition   ${comp_name}  1

Applicant submits his application
    [Documentation]  IFS-2196  IFS-2941  IFS-4046
    [Tags]
    Given the user clicks the button/link               link = Application details
    When the user fills in the Application details      ${EOI_application}  ${tomorrowday}  ${month}  ${nextyear}
    And the lead applicant fills all the questions and marks as complete(EOI comp type)
    Then the user should not see the element            jQuery = h2:contains("Finances")
    And the applicant submits the application

Invite a registered assessor
    [Documentation]  IFS-2376
    [Tags]
    Given log in as a different user                          &{Comp_admin1_credentials}
    When the user clicks the button/link                      link = ${comp_name}
    And the user clicks the button/link                       link = Invite assessors to assess the competition
    And the user selects the option from the drop-down menu   Smart infrastructure  id = filterInnovationArea
    And the user clicks the button/link                       jQuery = .govuk-button:contains("Filter")
    Then the user clicks the button/link                      jQuery = tr:contains("Paul Plum") label[for^="assessor-row"]
    And the user clicks the button/link                       jQuery = .govuk-button:contains("Add selected to invite list")
    And the user clicks the button/link                       link = Invite
    And the user clicks the button/link                       link = Review and send invites
    And the user enters text to a text field                  id = message    This is custom text
    And the user clicks the button/link                       jQuery = .govuk-button:contains("Send invite")

Allocated assessor accepts invite to assess the competition
    [Documentation]  IFS-2376
    [Tags]
    [Setup]  Milestones are updated in database to move competition to assessment state
    Given Log in as a different user                        &{assessor_credentials}
    When The user clicks the button/link                    Link = ${comp_name}
    And the user selects the radio button                   acceptInvitation  true
    And The user clicks the button/link                     jQuery = button:contains("Confirm")
    Then the user should be redirected to the correct page  ${server}/assessment/assessor/dashboard

Comp Admin allocates assessor to application
    [Documentation]  IFS-2376
    [Tags]
    Given log in as a different user        &{Comp_admin1_credentials}
    When The user clicks the button/link    link = Dashboard
    And The user clicks the button/link     link = EOI comp
    And The user clicks the button/link     jQuery = a:contains("Manage assessments")
    And the user clicks the button/link     jQuery = a:contains("Allocate applications")
    Then the user clicks the button/link    jQuery = tr:contains("${EOI_application}") a:contains("Assign")
    And the user clicks the button/link     jQuery = tr:contains("Paul Plum") button:contains("Assign")
    When the user navigates to the page     ${server}/management/competition/${competitionId}
    Then the user clicks the button/link    jQuery = button:contains("Notify assessors")

Allocated assessor assess the application
    [Documentation]  IFS-2376
    [Tags]
    Given Log in as a different user                       &{assessor_credentials}
    When The user clicks the button/link                   link = EOI comp
    And the user clicks the button/link                    jQuery = li:contains("${EOI_application}") a:contains("Accept or reject")
    And the user selects the radio button                  assessmentAccept  true
    Then the user clicks the button/link                   jQuery = .govuk-button:contains("Confirm")
    And the user should be redirected to the correct page  ${server}/assessment/assessor/dashboard/competition/${competitionId}
    And the user clicks the button/link                    link = EOI Application
    And the assessor submits the assessment

the comp admin closes the assessment and releases feedback
    [Documentation]  IFS-2376
    [Tags]
    Given log in as a different user                  &{Comp_admin1_credentials}
    When making the application a successful project  ${competitionId}  ${EOI_application}
    And moving competition to Project Setup           ${competitionId}
    Then the user should not see an error in the page

the EOI comp moves to Previous tab
    [Documentation]  IFS-2376
    [Tags]
    Given the user clicks the button/link  link = Dashboard
    When the user clicks the button/link   jQuery = a:contains("Previous")
    Then the user clicks the button/link   link = ${comp_name}
    And the user should see the element    JQuery = h1:contains("${comp_name}")
#    TODO IFS-2471 Once implemented please update test to see the application appear in relevant section in Previous tab.

*** Keywords ***
Custom Suite Setup
    Set predefined date variables
    The guest user opens the browser

the lead applicant fills all the questions and marks as complete(EOI comp type)
    the lead applicant marks every question as complete   Project summary
    the lead applicant marks every question as complete   Scope
    the applicant completes application team
    the user selects Research category   Feasibility studies
    :FOR  ${ELEMENT}    IN    @{EOI_questions}
     \     the lead applicant marks every question as complete     ${ELEMENT}

Milestones are updated in database to move competition to assessment state
    Get competitions id and set it as suite variable    ${comp_name}
    the submission date changes in the db in the past   ${competitionId}

the assessor submits the assessment
    the assessor adds score and feedback for every question    5   # value 5: is the number of questions to loop through to submit feedback
    the user clicks the button/link               link = Review and complete your assessment
    the user selects the radio button             fundingConfirmation  true
    the user enters text to a text field          id = feedback    EOI application assessed
    the user clicks the button/link               jQuery = .govuk-button:contains("Save assessment")
    the user clicks the button/link               jQuery = li:contains("${EOI_application}") label[for^="assessmentIds"]
    the user clicks the button/link               jQuery = .govuk-button:contains("Submit assessments")
    the user clicks the button/link               jQuery = button:contains("Yes I want to submit the assessments")
    the user should see the element               jQuery = li:contains("EOI Application") strong:contains("Recommended")   #

logged in user applies to competition
    [Arguments]  ${competition}  ${applicationType}
    the user select the competition and starts application   ${competition}
    the user selects the radio button    organisationTypeId  ${applicationType}
    the user clicks the button/link      jQuery = button:contains("Save and continue")
    the user clicks the Not on companies house link
    the user clicks the button/link      jQuery = button:contains("Save and continue")
    the user selects the checkbox        agree
    the user clicks the button/link      css = .govuk-button[type="submit"]    #Continue