*** Settings ***
Documentation     IFS-2284 Assign new Ts and Cs for APC competition type template
...
...               IFS-2286 APC Competition type template
...
...               IFS-1497  As an applicant I am able to confirm the project location for my organisation
...
...               IFS-4221  As an applicant I am only able to invite contributors to a single project type competition application
...
Suite Setup       Custom Suite Setup
Suite Teardown    Close browser and delete emails
Resource          ../../../resources/defaultResources.robot
Resource          ../Applicant_Commons.robot
Resource          ../../02__Competition_Setup/CompAdmin_Commons.robot

*** Variables ***
${apcCompetitionTitle}  Advanced Propulsion Centre Competition
${apcApplicationTitle}  Advanced Propulsion Centre Application

*** Test Cases ***
Comp Admin creates an APC competition
    [Documentation]  IFS-2284, IFS-2286
    [Tags]
    Given The user logs-in in new browser          &{Comp_admin1_credentials}
    And the user navigates to the page             ${CA_UpcomingComp}
    When the user clicks the button/link           link = Create competition
    Then the user fills in the CS Initial details  ${apcCompetitionTitle}  ${month}  ${nextyear}  Advanced Propulsion Centre  1  GRANT
    And the user selects the Terms and Conditions
    And the user fills in the CS Funding Information
    And the user fills in the CS Eligibility       ${business_type_id}  1  true  single   # 1 means 30%
    And the user fills in the CS Milestones        project-setup-completion-stage   ${month}   ${nextyear}
    And the user fills in the CS Application section with custom questions  yes  ${compType_APC}
    And the user fills in the CS Assessors
    And the user fills in the CS Documents in other projects
    When the user clicks the button/link           link = Public content
    Then the user fills in the Public content and publishes  APC
    When the user clicks the button/link           link = Return to setup overview
    Then the user should see the element           jQuery = div:contains("Public content") ~ .task-status-complete
    When the user clicks the button/link           jQuery = a:contains("Complete")
    Then the user clicks the button/link           css = button[type = "submit"]

Applicant applies to newly created APC competition
    [Documentation]  IFS-2286  IFS-4221  IFS-4222
    [Tags]  MySQL
    When the competition is open                  ${apcCompetitionTitle}
    And Log in as a different user                &{lead_applicant_credentials}
    Then logged in user applies to competition    ${apccompetitionTitle}  1
    And the applicant cannot add a collaborator to a single comp
    And the applicant sees single comp finance summary
    And the applicant sees state aid information

Applicant submits his application
    [Documentation]  IFS-2286
    [Tags]
    Given the user clicks the button/link               link = Application details
    When the user fills in the Application details      ${apcApplicationTitle}  ${tomorrowday}  ${month}  ${nextyear}
    Then the lead applicant fills all the questions and marks as complete(APC)
    When the user navigates to Your-finances page       ${apcApplicationTitle}
    And the user marks the finances as complete         ${apcApplicationTitle}   labour costs  54,000  yes
    Then the applicant submits the application

*** Keywords ***
Custom Suite Setup
    Set predefined date variables
    The guest user opens the browser

the lead applicant fills all the questions and marks as complete(APC)
    the user marks the project details as complete
    the applicant completes application team
    :FOR  ${ELEMENT}    IN    @{APC_questions}
     \     the lead applicant marks every question as complete     ${ELEMENT}

the applicant cannot add a collaborator to a single comp
    the user clicks the button/link      link = Application team
    the user should not see the element  link = Add a collaborator organisation
    the user clicks the button/link      link = Application overview

the applicant sees single comp finance summary
    the user clicks the button/link      link = Finances overview
    the user should see the element      jQuery = .warning-alert:contains("You have not marked your finances as complete")
    the user should not see the element  jQuery = .finance-summary th[scope = "row"]:contains("Total")
    the user clicks the button/link      link = Application overview

the applicant sees state aid information
    the user clicks the button/link      link = Your finances
    the user clicks the button/link      link = Your organisation
    the user should see the element      link = eligible for state aid
    the user clicks the button/link      link = Your finances
    the user clicks the button/link      link = Application overview