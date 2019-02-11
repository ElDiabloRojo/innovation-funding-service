*** Settings ***
Documentation  IFS-50 Change an existing unsuccessful application into a successful project in setup
Suite Setup    The user logs-in in new browser  &{ifs_admin_user_credentials}
Suite Teardown  the user closes the browser
Force Tags      Administrator  HappyPath
Resource        ../../resources/defaultResources.robot
Resource        ../10__Project_setup/PS_Common.robot

*** Test Cases ***
Administrator is able to mark as successful an unsuccessful application
    [Documentation]  IFS-50
    [Tags]
    Given the user navigates to the page    ${server}/management/competition/${PROJECT_SETUP_COMPETITION}/applications/previous
    Then the user should be allowed to only reinstate Unsuccessful applications
    When the user clicks the button/link    jQuery = td:contains("Cleaning Product packaging") ~ td a:contains("Mark as successful")
    And the user clicks the button/link     css = .govuk-button[name="mark-as-successful"]  # I'm sure button
    Then the user should no longer see the application is capable of being marked as successful

*** Keywords ***
The user should be allowed to only reinstate Unsuccessful applications
    the user should see the element  jQuery = td:contains("Unsuccessful") ~ td a:contains("Mark as successful")

the user should no longer see the application is capable of being marked as successful
    the user should not see the element  jQuery = td:contains("Unsuccessful") ~ td a:contains("Mark as successful")
    the user navigates to the page       ${server}/project-setup-management/competition/${PROJECT_SETUP_COMPETITION}/status/all
    the user should see the element      jQuery = th:contains("Cleaning Product packaging")