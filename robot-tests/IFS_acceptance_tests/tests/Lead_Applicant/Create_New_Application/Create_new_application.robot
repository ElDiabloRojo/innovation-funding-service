*** Settings ***
Documentation     INGINFUND-669 As an applicant I want to create a new application so that I can submit an entry into a relevant competition
Suite Setup       Login as User    &{lead_applicant_credentials}
Suite Teardown    TestTeardown User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot

*** Variables ***
${CREATE_CONFIRM_PAGE}    ${SERVER}/application/create-confirm-competition
${CREATE_APPLICATION_PAGE}    ${SERVER}/application/create/1?accept=accepted

*** Test Cases ***
Verify the confirmation page page
    [Documentation]    INFUND-669
    [Tags]    Applicant    New application
    Given the applicant goes to the confirm competition url
    And the applicant should get the confirm competition page
    When the applicant confirms this page
    Then the applicant should go to the create application page

Verify the validation errors for the new application input
    [Documentation]    INFUND-669
    [Tags]    Applicant    New application
    Given the user is in the "Create application" page
    When the applicant leaves the new application title empty
    Then the applicant should get a validation error(new application)

Verify the creation of a new application
    [Documentation]    INFUND-669
    [Tags]    Applicant    New application
    Given the user is in the "Create application" page
    When the applicant inserts a valid competition title
    And the applicant creates the new application
    Then the applicant should redirect in the application overview page
    And the title of the new application should be visible in the overview page
    And the new application should be visible in the dashboard page

*** Keywords ***
the applicant goes to the confirm competition url
    Go to    ${CREATE_CONFIRM_PAGE}

the applicant should get the confirm competition page
    Element Should Be Visible    css=#content > div > form > button

the applicant confirms this page
    click element    css=#content > div > form > button

the applicant should go to the create application page
    Location Should Be    ${CREATE_APPLICATION_PAGE}
    Element Should Be Visible    css=#content > form > input

the user is in the "Create application" page
    go to    ${CREATE_APPLICATION_PAGE}

the applicant leaves the new application title empty
    Click Element    css=#content > form > input

the applicant should get a validation error(new application)
    Wait Until Element Is Visible    css=#content > div

the applicant inserts a valid competition title
    Input Text    id=application_name    New application title

the applicant creates the new application
    Click Element    css=#content > form > input

the applicant should redirect in the application overview page
    Page Should Contain    Application overview

the title of the new application should be visible in the overview page
    Element Should Contain    css=.page-title    New application title

the new application should be visible in the dashboard page
    Click Link    link= My dashboard
    Page Should Contain Link    link=New application title
