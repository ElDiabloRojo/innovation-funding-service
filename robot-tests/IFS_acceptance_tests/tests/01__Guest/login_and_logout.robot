*** Settings ***
Documentation     INFUND-399 As a client, I would like to demo the system with real-like logins per user role
...
...
...               INFUND-171 As a user, I am able to sign in providing a emailaddress and password, so I have access to my data
...
...
...               INFUND-2130 As a competition administrator I want to be able to log into IFS so that I can access the system with appropriate permissions for my role
...
...               INFUND-1479 As an assessor with an existing Applicant AND Assessor account I want to be able to choose the correct profile when I log in, so that I don't access the wrong profile information
...
...               IFS-188 Stakeholder views – Support team
Suite Setup       The guest user opens the browser
Suite Teardown    The user closes the browser
Force Tags        Guest
Resource          ../../resources/defaultResources.robot

*** Test Cases ***
Invalid Login
    [Tags]
    Given the user is not logged-in
    Then the user cannot login with their new details    ${lead_applicant}    ${passw0rd2}

Valid login with double role as Applicant
    [Documentation]    INFUND-1479
    [Tags]
    Given The guest user inserts user email and password      &{Multiple_user_credentials}
    And The guest user clicks the log-in button
    Then The user should see the text in the page             Please choose the role you are signing in as today
    And The user clicks the button/link                       id = continue
    Then the user should see a field and summary error        Please select a role.
    And the user selects the radio button                     selectedRole    APPLICANT
    And The user clicks the button/link                       id = continue
    Then the user should be redirected to the correct page    ${DASHBOARD_URL}
    [Teardown]    Logout as user

Valid login with Double role as Assessor
    [Documentation]    INFUND-1479
    Given The guest user inserts user email and password      &{Multiple_user_credentials}
    And The guest user clicks the log-in button
    And the user selects the radio button                     selectedRole    ASSESSOR
    And The user clicks the button/link                       jQuery = button:contains("Continue")
    Then the user should be redirected to the correct page    ${assessor_dashboard_url}
    [Teardown]    Logout as user

Valid login with triple role Assessor
    [Documentation]  IFS-4568
    Given the guest user inserts user email and password      &{triple_user_credentials}
    And The guest user clicks the log-in button
    And the user selects the radio button                     selectedRole    APPLICANT
    And The user clicks the button/link                       jQuery = button:contains("Continue")
    Then the user should be redirected to the correct page    ${DASHBOARD_URL}
    [Teardown]    Logout as user

Valid login with triple role Stakeholder
    [Documentation]  IFS-4568
    Given the guest user inserts user email and password      &{triple_user_credentials}
    And The guest user clicks the log-in button
    And the user selects the radio button                     selectedRole    STAKEHOLDER
    And The user clicks the button/link                       jQuery = button:contains("Continue")
    Then the user should be redirected to the correct page    ${COMP_ADMINISTRATOR_DASHBOARD}
    [Teardown]    Logout as user

Valid login with triple role Applicant
    [Documentation]  IFS-4568
    Given the guest user inserts user email and password      &{triple_user_credentials}
    And The guest user clicks the log-in button
    And the user selects the radio button                     selectedRole    ASSESSOR
    And The user clicks the button/link                       jQuery = button:contains("Continue")
    Then the user should be redirected to the correct page    ${assessor_dashboard_url}
    [Teardown]    Logout as user

Should not see the Sign in link when on the login page
    Given the user navigates to the page        ${LOGIN_URL}
    Then the user should not see the element    link = Sign in

Should see the Sign in link when not logged in
    Given the user is not logged-in
    And the user navigates to the page      ${frontDoor}
    Then the user should see the element    link = Sign in

Reset password
    [Documentation]    INFUND-1889
    [Tags]  HappyPath
    Given the user navigates to the page           ${LOGIN_URL}
    When the user clicks the forgot psw link
    And the user enters text to a text field       id = email  ${test_mailbox_one}+changepsw@gmail.com
    And the user clicks the button/link            id = forgotten-password-cta
    Then the user should see the text in the page  If your email address is recognised and valid, you’ll receive a notification with instructions about how to reset your password. If you do not receive a notification, please check your junk folder or try again.
    And the user reads his email from the default mailbox and clicks the link  ${test_mailbox_one}+changepsw@gmail.com  Reset your password  If you didn't request this
    And the user should see the element            jQuery = h1:contains("Password reset")

Reset password user enters new psw
    [Documentation]    INFUND-1889
    [Tags]
    [Setup]    Clear the login fields
    When the user enters text to a text field              id = password  ${newPassw0rd}
    And the user clicks the button/link                    css = button[type="submit"]
    Then the user should see the text in the page          Your password is updated, you can now sign in with your new password
    And the user clicks the button/link                    link = Sign in
    And the user cannot login with their new details       ${test_mailbox_one}+changepsw@gmail.com    ${short_password}
    When Logging in and Error Checking                     ${test_mailbox_one}+changepsw@gmail.com    ${newPassw0rd}
    Then the user should see the element                   link = Sign out

*** Keywords ***
the user is not logged-in
    the user should not see the element  link = Dashboard
    the user should not see the element  link = Sign out

Clear the login fields
    the user reloads the page
    the user enters text to a text field       id = password    ${EMPTY}
    Mouse Out                                  id = password
    wait for autosave
