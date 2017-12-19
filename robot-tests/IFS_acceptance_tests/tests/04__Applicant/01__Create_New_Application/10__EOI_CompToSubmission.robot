*** Settings ***
Documentation   Suite description
...
...             IFS-2192 As a Portfolio manager I am able to create an EOI competition
...
...             IFS-2196 As an applicant I am able to apply for an EOI competition
Suite Setup     custom suite setup
Suite Teardown  Close browser and delete emails
Force Tags      compAdmin  Applicant  Assessor
Resource        ../../../resources/defaultResources.robot
Resource        ../Applicant_Commons.robot
Resource        ../../02__Competition_Setup/CompAdmin_Commons.robot

# This suite covers End to End flow of EOI type competition i.e comp creation, applicaiotn submission , assessmnet submission, release feedback
*** Variables ***
${comp_name}         EOI comp
${EOI_application}   EOI Application

*** Test Cases ***
Comp Admin Creates EOI type competition
    [Documentation]  IFS-2192
    [Tags]  CompAdmin  HappyPath
    Given Logging in and Error Checking                     &{Comp_admin1_credentials}
    Then The competition admin creates a EOI Comp     ${business_type_id}  ${comp_name}  EOI

Applicant applies to newly created EOI competition
    [Documentation]  IFS-2192  IFS-2196
    [Tags]  HappyPath  MySQL
    When the competition is open                                 ${comp_name}
    Then Lead Applicant applies to the new created competition   ${comp_name}  &{lead_applicant_credentials}

Applicant submits his application
    [Documentation]  IFS-2196
    [Tags]  HappyPath
    Given the user clicks the button/link               link=Application details
    When the user fills in the Application details      ${EOI_application}  Feasibility studies  ${tomorrowday}  ${month}  ${nextyear}
    and the lead applicant fills all the questions and marks as complete(EOI comp type)
    and the user should not see the element             jQuery=h2:contains("Finances")
    Then the applicant submits the application

Invite a registered assessor
    [Documentation]  IFS-2376
    [Tags]  HappyPath
    log in as a different user                            &{Comp_admin1_credentials}
    the user clicks the button/link                       link=${comp_name}
    the user clicks the button/link                       link=Invite assessors to assess the competition
    the user selects the option from the drop-down menu   Smart infrastructure  id=filterInnovationArea
    the user clicks the button/link                       jQuery=.button:contains("Filter")
    the user clicks the button/link                       jQuery=tr:contains("Paul Plum") label[for^="assessor-row"]
    the user clicks the button/link                       jQuery=.button:contains("Add selected to invite list")
    the user clicks the button/link                       link=Invite
    the user clicks the button/link                       link=Review and send invites  # a:contains("Review and send invites")
    And the user enters text to a text field              id=message    This is custom text
    And the user clicks the button/link                   jQuery=.button:contains("Send invite")

Milestones are updated db to move comp to assessment state
    [Documentation]  IFS-2376
    [Tags]  HappyPath  MySQL
    ${competitionId} =  get comp id from comp title  ${comp_name}
    Set suite variable  ${competitionId}
    ${today}=    get time
    ${yesterday} =    Subtract Time From Date    ${today}    1 day
    When execute sql string    UPDATE `${database_name}`.`milestone` SET `DATE`='${yesterday}' WHERE `competition_id`='${competitionId}' and type IN ('OPEN_DATE', 'SUBMISSION_DATE', 'ALLOCATE_ASSESSORS','ASSESSOR_BRIEFING');
    And reload page

Allocated assessor accepts invite to assess the competition
    [Documentation]  IFS-2376
    [Tags]  HappyPath
    Log in as a different user                              &{assessor_credentials}
    When The user clicks the button/link                    Link=${comp_name}
    And the user selects the radio button                   acceptInvitation  true
    And The user clicks the button/link                     jQuery=button:contains("Confirm")
    Then the user should be redirected to the correct page  ${server}/assessment/assessor/dashboard

Comp Admin allocates assessor to application
    [Documentation]  IFS-2376
    [Tags]  HappyPath
    log in as a different user              &{Comp_admin1_credentials}
    The user clicks the button/link         link=Dashboard
    The user clicks the button/link         link=EOI comp
    And The user clicks the button/link     jQuery=a:contains("Manage assessments")
    the user clicks the button/link         jQuery=a:contains("Allocate applications")
    the user clicks the button/link         jQuery=tr:contains("${EOI_application}") a:contains("Assign")
    the user clicks the button/link         jQuery=tr:contains("Paul Plum") button:contains("Assign")
    the user navigates to the page          ${server}/management/competition/${competitionId}
    the user clicks the button/link         jQuery=button:contains("Notify assessors")

Allocated assessor assess the application
    [Documentation]  IFS-2376
    [Tags]  HappyPath
    Log in as a different user                         &{assessor_credentials}
    When The user clicks the button/link               link=EOI comp
    the user clicks the button/link                    jQuery=li:contains("${EOI_application}") a:contains("Accept or reject")
    the user selects the radio button                  assessmentAccept  true
    the user clicks the button/link                    jQuery=.button:contains("Confirm")
    the user should be redirected to the correct page  ${server}/assessment/assessor/dashboard/competition/${competitionId}
    the user clicks the button/link                    link=EOI Application

the assessor adds score and feedback for every question
    [Documentation]  IFS-2376
    [Tags]  HappyPath
    The user clicks the button/link                       link=Scope
    The user selects the index from the drop-down menu    1    css=.research-category
    The user clicks the button/link                       jQuery=label:contains("Yes")
    The user enters text to a text field                  css=.editor    Testing scope feedback text
    mouse out  css=.editor
    Wait Until Page Contains Without Screenshots          Saved!
    :FOR  ${INDEX}  IN RANGE  1  5
      \    the user clicks the button/link    css=.next
      \    The user selects the option from the drop-down menu    10    css=.assessor-question-score
      \    The user enters text to a text field    css=.editor    Testing feedback text
      \    mouse out  css=.editor
      \    Wait Until Page Contains Without Screenshots    Saved!
    The user clicks the button/link               jquery=button:contains("Save and return to assessment overview")
    the user clicks the button/link               link=Review and complete your assessment
    the user selects the radio button             fundingConfirmation  true
    the user enters text to a text field          id=feedback    EOI application assessed
    the user clicks the button/link               jQuery=.button:contains("Save assessment")
    the user clicks the button/link               jQuery=li:contains("${EOI_application}") label[for^="assessmentIds"]
    the user clicks the button/link               jQuery=.button:contains("Submit assessments")
    the user clicks the button/link               jQuery=button:contains("Yes I want to submit the assessments")
    the user should see the element               jQuery=li:contains("EOI Application") strong:contains("Recommended")   # add selector to see its in submmited list

the comp admin closes the assessment
    [Documentation]  IFS-2376
    [Tags]  HappyPath
    log in as a different user                   &{Comp_admin1_credentials}
    the user clicks the button/link             link=${comp_name}
    the user clicks the button/link             jQuery=.button:contains("Close assessment")

the comp admin informs the applicants
    [Documentation]  IFS-2376
    [Tags]  HappyPath
    the user clicks the button/link             jQuery=a:contains("Input and review funding decision")
    the user clicks the button/link             jQuery=tr:contains("${EOI_application}") label[for^="app-row"]
    the user clicks the button/link             jQuery=button:contains("Successful")
    the user clicks the button/link             jQuery=a:contains("Competition")
    the user clicks the button/link             jQuery=a:contains("Manage funding notifications")
    the user clicks the button/link             jQuery=tr:contains("${EOI_application}") label
    the user clicks the button/link             jQuery=button:contains("Write and send email")
    the user should see the element             jQuery=h1:contains("Funding decision notification")
    the user enters text to a text field        css=.editor  EOI sussessful applicant
    the user clicks the button/link             jQuery=button:contains("Send email to all applicants")
    the user clicks the button/link             jQuery=.send-to-all-applicants-modal button:contains("Send email")
    the user clicks the button/link             jQuery=a:contains("Competition")
    the user clicks the button/link             jQuery=button:contains("Release feedback")

the EOI comp moves to Previous tab
    [Documentation]  IFS-2376
    [Tags]  HappyPath
    the user clicks the button/link             jQuery=a:contains("Previous")
    the user clicks the button/link             link=${comp_name}
    the user should see the element             JQuery=h1:contains("${comp_name}")
#    TODO IFS-2471 Once implemented please update test to see the application appear in relevant section in Previous tab.

*** Keywords ***
Custom Suite Setup
    Set predefined date variables
    The guest user opens the browser

The competition admin creates a EOI Comp
    [Arguments]  ${orgType}  ${competition}  ${extraKeyword}
    the user navigates to the page   ${CA_UpcomingComp}
    the user clicks the button/link  jQuery=.button:contains("Create competition")
    the user fills in the CS Initial details  ${competition}  ${month}  ${nextyear}  ${compType_EOI}
    the user fills in the CS Funding Information
    the user fills in the CS Eligibility  ${orgType}
    the user fills in the CS Milestones   ${month}  ${nextyear}
    the user marks the Application as done  no  ${compType_EOI}
    the user fills in the CS Assessors
    the user clicks the button/link  link=Public content
    the user fills in the Public content and publishes  ${extraKeyword}
    the user clicks the button/link   link=Return to setup overview
    the user clicks the button/link  jQuery=a:contains("Complete")
    the user clicks the button/link  jQuery=a:contains("Done")
    the user navigates to the page   ${CA_UpcomingComp}
    the user should see the element  jQuery=h2:contains("Ready to open") ~ ul a:contains("${competition}")

the lead applicant fills all the questions and marks as complete(EOI comp type)
    the lead applicant marks every question as complete   Project summary
    the lead applicant marks every question as complete   Scope
    :FOR  ${ELEMENT}    IN    @{EOI_questions}
     \     the lead applicant marks every question as complete     ${ELEMENT}

