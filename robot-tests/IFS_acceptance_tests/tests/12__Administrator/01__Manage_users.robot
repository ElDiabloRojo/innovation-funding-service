*** Settings ***
Documentation     IFS-604: IFS Admin user navigation to Manage users section
...               IFS-606: Manage internal users: Read only view of internal user profile
...               IFS-27:  Invite new internal user
...               IFS-642: Email to new internal user inviting them to register
...               IFS-643: Complete internal user registration
...               IFS-644: Disable or reenable user profile
...               IFS-983: Manage users: Pending registration tab
...               IFS-2412: Internal users resend invites
...               IFS-2842: Add modals to the resending of invites to internal users
...               IFS-1944: Internal - Invite internal user - error field is missing
Suite Setup       Custom suite setup
Suite Teardown    the user closes the browser
Force Tags        Administrator  CompAdmin
Resource          ../../resources/defaultResources.robot

# NOTE: Please do not use hard coded email in this suite. We always need to check local vs remote for the difference in the domain name !!!

*** Variables ***
${localEmailInvtedUser}   ifs.innovationLead@innovateuk.test
${remoteEmailInvtedUser}  ifs.innovationLead@innovateuk.gov.uk
${invalidEmail}           test@test.com

*** Test Cases ***
Administrator can navigate to manage users page
    [Documentation]    INFUND-604
    [Tags]  HappyPath
    [Setup]  The user logs-in in new browser  &{ifs_admin_user_credentials}
    When the user clicks the button/link      link=Manage users
    Then the user should see the element      jQuery=h1:contains("Manage users")
    And the user should see the element       jQuery=a[aria-selected]:contains("Active")

Administrator can see the read only view of internal user profile
    [Documentation]  INFUND-606
    [Tags]
    When the user clicks the button/link  link=John Doe
    Then the user should see the element  jQuery=h1:contains("View internal user details")
    And the user should see the element   jQuery=dt:contains("Email address") + dd:contains("${Comp_admin1_credentials["email"]}")
    And the user should see the element   jQuery=dt:contains("Role") + dd:contains("Competition Administrator")
    And the user should see the element   jQuery=.form-footer__info:contains("Created by IFS Web System User")

Project finance user cannot navigate to manage users page
    [Documentation]  INFUND-604
    [Tags]
    User cannot see manage users page   &{Comp_admin1_credentials}
    User cannot see manage users page   &{internal_finance_credentials}

Server side validation for invite new internal user
    [Documentation]  IFS-27
    [Tags]
    [Setup]  Log in as a different user                 &{ifs_admin_user_credentials}
    Given the user navigates to the page                ${server}/management/admin/users/active
    And the user clicks the button/link                 link = Invite a new internal user
    And the user clicks the button/link                 jQuery = button:contains("Send invite")
    Then The user should see a field and summary error  Please enter a first name.
    And The user should see a field and summary error   Please enter a last name.
    And The user should see a field and summary error   Please enter an email address.

The user must use an Innovate UK email
    [Documentation]  IFS-1944
    [Tags]
    Given the user enters text to a text field            id = firstName  Support
    And the user enters text to a text field              id = lastName  User
    When the user enters text to a text field             id = emailAddress  ${invalidEmail}
    And the user clicks the button/link                   jQuery = button:contains("Send invite")
    Then the user should see a field and summary error    Users cannot be registered without an Innovate UK email address.
    [Teardown]  the user clicks the button/link           link = Cancel

Client side validations for invite new internal user
    [Documentation]  IFS-27
    [Tags]
    Given the user navigates to the page       ${server}/management/admin/invite-user
    When the user enters text to a text field  id=firstName  A
    Then the user should not see the element   jQuery=.govuk-error-message:contains("Please enter a first name.")
    And the user should see the element        jQuery=.govuk-error-message:contains("Your first name should have at least 2 characters.")
    When the user enters text to a text field  id=lastName  D
    Then the user should not see the element   jQuery=.govuk-error-message:contains("Please enter a last name.")
    And the user should see the element        jQuery=.govuk-error-message:contains("Your last name should have at least 2 characters.")
    When the user enters text to a text field  id=emailAddress  astle
    Then the user should not see the element   jQuery=.govuk-error-message:contains("Please enter an email address.")
    And the user should see the element        jQuery=.govuk-error-message:contains("Please enter a valid email address.")

Administrator can successfully invite a new user
    [Documentation]  IFS-27 IFS-983
    [Tags]  HappyPath
    Given the user navigates to the page                     ${server}/management/admin/invite-user
    When the user enters text to a text field                id=firstName  Support
    And the user enters text to a text field                 id=lastName  User
    And the user fills in the email address for the invitee
    And the user selects the option from the drop-down menu  IFS Administrator  id=role
    And the user clicks the button/link                      jQuery=.govuk-button:contains("Send invite")
    Then the user cannot see a validation error in the page

Administrator can successfully finish the rest of the invitation
    [Documentation]  IFS-27  IFS-983  IFS-2412  IFS-2842
    [Tags]  HappyPath
    Given the user should see the element                     jQuery=h1:contains("Manage users")
    #The Admin is redirected to the Manage Users page on Success
    And the user should see the element                      jQuery=a[aria-selected]:contains("Pending")
    When the user resends the invite
    Then the user verifies pending tab content
    When the user clicks the button/link                     jQuery=a:contains("Active")
    Then the user should not see the element                 jQuery=td:contains("Support User") ~ td:contains("IFS Administrator")
    When the user clicks the button/link                     jQuery=a:contains("Inactive")
    Then the user should not see the element                 jQuery=td:contains("Support User") ~ td:contains("IFS Administrator")
    [Teardown]  close any open browsers

Invited user can receive the invitation
    [Documentation]  IFS-642
    [Tags]  Email  HappyPath
    [Setup]  the guest user opens the browser
    The invitee reads his email and clicks the link  Invitation to Innovation Funding Service  Your Innovation Funding Service account has been created.

Account creation validation checks - Blank
    [Documentation]  IFS-643
    [Tags]  HappyPath
    Given the user clicks the button/link   jQuery=.govuk-button:contains("Create account")
    And the user should see a field and summary error   Please enter a first name.
    And the user should see a field and summary error   Please enter a last name.
    And The user should see a field and summary error   Password must be at least 8 characters
    When the user enters text to a text field  css=#firstName  New
    And the user enters text to a text field   css=#lastName  Administrator
    And the user enters text to a text field   css=#password  ${correct_password}
    Then the user should see the element       jQuery=h3:contains("Email") + p:contains("ifs.innovationLead@innovateuk")
    Focus                                      css=#lastName
    And the user cannot see a validation error in the page

Account creation validation checks - Lowercase password
    [Documentation]  IFS-3554
    [Tags]
    Given the user enters text to a text field  id=password  PASSWORD123
    When The user clicks the button/link        jQuery=.govuk-button:contains("Create account")
    Then The user should see a field and summary error  Password must contain at least one lower case letter.
    [Teardown]  the user enters text to a text field   css=#password  ${correct_password}

New user account is created and verified
    [Documentation]  IFS-643 IFS-983
    [Tags]   HappyPath
    Given the user clicks the button/link      jQuery=.govuk-button:contains("Create account")
    Then the user should see the element       jQuery=h1:contains("Your account has been created")
    When the user clicks the button/link       jQuery=.govuk-button:contains("Sign into your account")
    Then the invited user logs in
    And the user clicks the button/link        jQuery=a:contains("Manage users")
    And the user clicks the button/link        jQuery=a:contains("New Administrator")
    Then the user should see the element       jQuery=dt:contains("Full name") + dd:contains("New Administrator")
    And the user should see the element        jQuery=dt:contains("Email") + dd:contains("ifs.innovationLead@innovateuk")
    And the user should see the element        jQuery=dt:contains("Role") + dd:contains("IFS Administrator")
    When the user clicks the button/link       jQuery=a:contains("Manage users")
    And the user clicks the button/link        jQuery=a:contains("Pending")
    Then the user should see the element       jQuery=span:contains("0") + span:contains("pending internal users")
    And the user should not see the element    css=.table-overflow ~ td
    And the user clicks the button/link        jQuery=a:contains("Active")

Inviting the same user for the same role again should give an error
    [Documentation]  IFS-27
    [Tags]
    [Setup]  log in as a different user            &{ifs_admin_user_credentials}
    Given the user navigates to the page           ${server}/management/admin/invite-user
    When the user enters text to a text field      id=firstName  New
    And the user enters text to a text field       id=lastName  Administrator
    And the user fills in the email address for the invitee
    And the user selects the option from the drop-down menu  IFS Administrator  id=role
    And the user clicks the button/link            jQuery=.govuk-button:contains("Send invite")
    Then the user should see the element           jQuery=.govuk-error-summary:contains("This email address is already in use.")

Inviting the same user for the different role again should also give an error
    [Documentation]  IFS-27
    [Tags]
    Given the user navigates to the page       ${server}/management/admin/invite-user
    When the user enters text to a text field  id=firstName  Project
    And the user enters text to a text field   id=lastName  Finance
    And the user fills in the email address for the invitee
    And the user selects the option from the drop-down menu  Project Finance  id=role
    And the user clicks the button/link        jQuery=.govuk-button:contains("Send invite")
    Then The user should see a summary error   This email address is already in use.

Administrator can navigate to edit page to edit the internal user details
    [Documentation]  IFS-18
    [Tags]
    [Setup]  the user navigates to the View internal user details  New Administrator  active
    Given the user clicks the button/link         link=Edit
    And the user should see the text in the page  Edit internal user details
    And the user should see the element           css=#firstName[value="New"]
    And the user should see the element           css=#lastName[value="Administrator"]
    And the user should see the element           jQuery=dt:contains("Email address") ~ dd:contains("ifs.innovationLead")
    And the user should see the dropdown option selected  IFS Administrator  id=role

Server side validation for edit internal user details
    [Documentation]  IFS-18
    [Tags]
    Given the user enters text to a text field  id=firstName  ${empty}
    And the user enters text to a text field    id=lastName  ${empty}
    When the user clicks the button/link        jQuery=button:contains("Save and return")
    Then the user should see a field error      Please enter a first name.
    And the user should see a field error       Your first name should have at least 2 characters.
    And the user should see a field error       Please enter a last name.
    And the user should see a field error       Your last name should have at least 2 characters.

Client side validations for edit internal user details
    [Documentation]  IFS-18
    [Tags]
    Given the user enters text to a text field  id=firstName  A
    Then the user should not see the element   jQuery=.govuk-error-message:contains("Please enter a first name.")
    And the user should see the element        jQuery=.govuk-error-message:contains("Your first name should have at least 2 characters.")
    When the user enters text to a text field  id=lastName  D
    Then the user should not see the element   jQuery=.govuk-error-message:contains("Please enter a last name.")
    And the user should see the element        jQuery=.govuk-error-message:contains("Your last name should have at least 2 characters.")

Administrator can successfully edit internal user details
    [Documentation]  IFS-18
    [Tags]  HappyPath  InnovationLead
    [Setup]  log in as a different user                      &{ifs_admin_user_credentials}
    Given the user navigates to the View internal user details  New Administrator  active
    And the user clicks the button/link                      link=Edit
    When the user enters text to a text field                id=firstName  Innovation
    Then the user enters text to a text field                id=lastName  Lead
    # Has to be an Innovation Lead for the next test
    And the user selects the option from the drop-down menu  Innovation Lead  id=role
    And the user clicks the button/link                      jQuery=.govuk-button:contains("Save and return")
    Then the user cannot see a validation error in the page
    When the user should see the element                     jQuery=h1:contains("Manage users")
    #The Admin is redirected to the Manage Users page on Success
    And the user should see the element                      jQuery=a[aria-selected]:contains("Active")
    And the user should see the element                      jQuery=td:contains("Innovation Lead") + td:contains("Innovation Lead")
    [Teardown]  the user logs out if they are logged in

The internal user can login with his new role and sees no competitions assigned
    [Documentation]  IFS-1305  IFS-1308
    [Tags]  InnovationLead
    Given the invited user logs in
    Then the user should see the text in the page  There are no competitions assigned to you.
    And the user clicks the button/link            css=#section-4 a  #Project setup tab

Administrator is able to disable internal users
    [Documentation]  IFS-644
    [Tags]
    [Setup]  log in as a different user   &{ifs_admin_user_credentials}
    Given the user navigates to the View internal user details  Innovation Lead  active
    And the user clicks the button/link   link=Edit
    Then the user should see the element  css=.govuk-form-group input
    When the user clicks the button/link  jQuery=button:contains("Deactivate user")
    Then the user clicks the button/link  jQuery=button:contains("Cancel")
    When the user clicks the button/link  jQuery=button:contains("Deactivate user")
    And the user clicks the button/link   jQuery=button:contains("Yes, deactivate")
    Then the user should see the element  jQuery=.form-footer *:contains("Reactivate user") + *:contains("Deactivated by Arden Pimenta on ${today}")
    When the user navigates to the page   ${server}/management/admin/users/inactive
    Then the user should see the element  jQuery=tr:contains("Innovation Lead")  #Checking the user swapped tab

Deactivated user cannot login until he is activated
    [Documentation]  IFS-644
    [Tags]
    [Setup]  the user logs out if they are logged in
    Given the deactivated user is not able to login
    When Logging in and Error Checking                  &{ifs_admin_user_credentials}
    Then the user navigates to the View internal user details  Innovation Lead  inactive
    When the user clicks the button/link                jQuery=button:contains("Reactivate user")
    Then the user clicks the button/link                jQuery=button:contains("Yes, reactivate")
    When the user navigates to the page                 ${server}/management/admin/users/active
    Then the user should see the element                jQuery=tr:contains("Innovation Lead")  #Checking the user swapped tab
    When the re-activated user tries to login
    Then the user should not see an error in the page

*** Keywords ***
Custom suite setup
    ${today} =  get today
    set suite variable  ${today}

User cannot see manage users page
    [Arguments]  ${email}  ${password}
    Log in as a different user  ${email}  ${password}
    the user should not see the element   link=Manage users
    the user navigates to the page and gets a custom error message  ${USER_MGMT_URL}  ${403_error_message}

the user fills in the email address for the invitee
    # Locally the accepted domain is innovateuk.test
    run keyword if  ${docker}==1  the user enters text to a text field  id=emailAddress  ${localEmailInvtedUser}
    # On production the accepted domain is innovateuk.gov.uk
    run keyword if  ${docker}!=1  the user enters text to a text field  id=emailAddress  ${remoteEmailInvtedUser}

The invitee reads his email and clicks the link
    [Arguments]  ${title}  ${pattern}
    # Locally the accepted domain is innovateuk.test
    run keyword if  ${docker}==1  The user reads his email and clicks the link  ${localEmailInvtedUser}  ${title}  ${pattern}
    # On production the accepted domain is innovateuk.gov.uk
    run keyword if  ${docker}!=1  The user reads his email and clicks the link  ${remoteEmailInvtedUser}  ${title}  ${pattern}

the invited user logs in
    # Locally the accepted domain is innovateuk.test
    run keyword if  ${docker}==1  Logging in and Error Checking  ${localEmailInvtedUser}  ${correct_password}
    # On production the accepted domain is innovateuk.gov.uk
    run keyword if  ${docker}!=1  Logging in and Error Checking  ${remoteEmailInvtedUser}  ${correct_password}

the user verifies pending tab content
    # Locally the accepted domain is innovateuk.test
    run keyword if  ${docker}==1  the user should see the element  jQuery=td:contains("Support User") ~ td:contains("IFS Administrator") ~ td:contains("${localEmailInvtedUser}")
    # On production the accepted domain is innovateuk.gov.uk
    run keyword if  ${docker}!=1  the user should see the element  jQuery=td:contains("Support User") ~ td:contains("IFS Administrator") ~ td:contains("${remoteEmailInvtedUser}")

the user navigates to the View internal user details
    [Arguments]  ${user}  ${status}
    the user navigates to the page   ${server}/management/admin/users/${status}
    the user clicks the button/link  link=${user}

the deactivated user is not able to login
    # Locally the accepted domain is innovateuk.test
    run keyword if  ${docker}==1  the user cannot login with their new details  ${localEmailInvtedUser}  ${correct_password}
    # On production the accepted domain is innovateuk.gov.uk
    run keyword if  ${docker}!=1  the user cannot login with their new details  ${remoteEmailInvtedUser}  ${correct_password}

the re-activated user tries to login
    # Locally the accepted domain is innovateuk.test
    run keyword if  ${docker}==1  log in as a different user  ${localEmailInvtedUser}  ${correct_password}
    # On production the accepted domain is innovateuk.gov.uk
    run keyword if  ${docker}!=1  log in as a different user  ${remoteEmailInvtedUser}  ${correct_password}

the user resends the invite
    the user clicks the button/link    css=.button-secondary[type="submit"]     #Resend invite
    the user clicks the button/link    jQuery=button:contains("Resend")
    the user reads his email           ${localEmailInvtedUser}  Invitation to Innovation Funding  Your Innovation Funding Service
