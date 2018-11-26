*** Settings ***
Documentation     IFS-747 As a comp exec I am able to select a Competition type of Generic in Competition setup
Suite Setup       Custom suite setup
Suite Teardown    the user closes the browser
Force Tags        Applicant
Resource          ../../../resources/defaultResources.robot
Resource          ../Applicant_Commons.robot
Resource          ../../02__Competition_Setup/CompAdmin_Commons.robot

*** Variables ***
${competitionName}  Generic competition for TsnCs

*** Test Cases ***
User can edit the assesed question
    [Documentation]    IFS-747
    [Tags]
    [Setup]  logged in user applies to competition    ${openGenericCompetition}  1
    Given the user should not see the element         a:contains("7.")  # This comp has only 1 question
    When the user clicks the button/link              link = 1. Generic question title
    Then the user should see the element              jQuery = button:contains("Mark as complete")

CompAdmin creates a new Generic competition
    [Documentation]  IFS-3261
    [Tags]  HappyPath
    [Setup]  log in as a different user                &{Comp_admin1_credentials}
    The competition admin creates a competition for    4  ${competitionName}  Generic

Requesting the id of this Competition and moving to Open
    [Documentation]  IFS-3261
    ...   retrieving the id of the competition so that we can use it in urls
    [Tags]  MySQL  HappyPath
    ${competitionId} =  get comp id from comp title  ${competitionName}
    Set suite variable  ${competitionId}
    The competition moves to Open state  ${competitionId}

Applicant Applies to Generic competition and is able to see the Ts&Cs
    [Documentation]  IFS-1012  IFS-2879
    [Tags]  HappyPath
    [Setup]  Log in as a different user             becky.mason@gmail.com  ${short_password}
    Given logged in user applies to competition     ${competitionName}   4
    When the user clicks the button/link            link = Application details
    Then the user fills in the Application details  Application Ts&Cs  ${tomorrowday}  ${month}  ${nextyear}
    When the user clicks the button/link            link = View the competition terms and conditions
    Then the user should see the element            jQuery = h1:contains("Terms and conditions of an Innovate UK grant award")

*** Keywords ***
Custom suite setup
    Set predefined date variables
    The user logs-in in new browser  &{lead_applicant_credentials}

The competition admin creates a competition for
    [Arguments]  ${orgType}  ${competition}  ${extraKeyword}
    the user navigates to the page              ${CA_UpcomingComp}
    the user clicks the button/link             jQuery = .govuk-button:contains("Create competition")
    the user fills in the CS Initial details    ${competition}  ${month}  ${nextyear}  ${compType_Generic}  2
    the user selects the Terms and Conditions
    the user fills in the CS Funding Information
    the user fills in the CS Eligibility        ${orgType}  1  true  collaborative     # 1 means 30%
    the user fills in the CS Milestones         ${month}  ${nextyear}
    the user fills in the CS Application section with custom questions  no  Generic
    the user fills in the CS Assessors
    the user fills in the CS Documents in other projects
    the user clicks the button/link             link = Public content
    the user fills in the Public content and publishes  ${extraKeyword}
    the user clicks the button/link             link = Return to setup overview
    the user clicks the button/link             jQuery = a:contains("Complete")
    the user clicks the button/link             css = button[type="submit"]
    the user navigates to the page              ${CA_UpcomingComp}
    the user should see the element             jQuery = h2:contains("Ready to open") ~ ul a:contains("${competition}")