*** Settings ***
Documentation     INFUND-7042 As a member of the competitions team I can see list of applications with assessor statistics on the 'Manage Applications' dashboard so...
...
...               INFUND-7046 As a member of the competitions team I can view the application progress dashboard for an application so that I can see the application details
...
...               INFUND-7050 As a member of the competitions team I can notify assessors of their assigned applications by selecting 'Notify assessors' on 'In assessment' dashboard so that assessors know which applications they have to assess
...
...               INFUND-7038 As a member of the competitions team I can add an assessor to the 'Assign Assessors' list so that I can ensure an assessment has the correct number of assessors
...
...               INFUND-7233 As a member of the competitions team I can view the assessors list so that I can see who is available to assess the application
...
...               INFUND-7237 Implement Assessor Total Applications and Assigned Counts for Application Progress within Assessor Management
...
...               INFUND-7232 As a member of the competitions team I can view previously assigned assessors so I can see who has previously been removed from assessing the application
...
...               INFUND-8061 Filter and pagination on Allocate Applications (Closed competition) and Manage applications (In assessment) dashboards
...
...               IFS-319 View list of accepted assessors - In assessment state
...
...               IFS-1079 Remove an application - Closed and In assessment states
...
...               IFS-400 Filter by application number on Assessor progress dashboard - Closed and in assessments state
Suite Setup       The user logs-in in new browser  &{Comp_admin1_credentials}
Suite Teardown    The user closes the browser
Force Tags        CompAdmin    Assessor
Resource          ../../resources/defaultResources.robot

*** Variables ***
${Molecular_id}        ${application_ids['Molecular tree breeding']}
${Cryptocurrencies_id}  ${application_ids['Living with Cryptocurrencies']}
${Paul_Plum_id}        ${user_ids['${assessor_credentials["email"]}']}
${Intelligent_water}   ${application_ids['Intelligent water system']}

*** Test Cases ***
View the list of the applications
    [Documentation]    INFUND-7042
    [Tags]
    Given The user clicks the button/link        link=${IN_ASSESSMENT_COMPETITION_NAME}
    When The user clicks the button/link         jQuery=a:contains("Manage assessments")
    And The user clicks the button/link          jQuery=a:contains("Manage applications")
    Then the application list is correct before changes
    [Teardown]  the user clicks the button/link  link=Manage assessments

View the list of assessors
    [Documentation]  IFS-319
    [Tags]
    When The user clicks the button/link  jQuery=a:contains("Manage assessors")
    Then the assessor list is correct before changes

Assessor link goes to the assessor profile
    [Documentation]  IFS-319
    [Tags]
    Given the user clicks the button/link  link=Paul Plum
    Then the user should see the element   jQuery=h1:contains("Assessor profile") ~ p:contains("Paul Plum")
    [Teardown]    the user clicks the button/link  link=Back

Filter assessors
    [Documentation]    IFS-399
    [Tags]
    Given the user selects the option from the drop-down menu  Materials and manufacturing  id=innovationSector
    And the user clicks the button/link                        jQuery=.govuk-button:contains("Filter")
    Then the user should not see the element                   jQuery=td:contains("Paul Plum")
    And the user should see the element                        jQuery=td:contains("Felix Wilson")
    And the user should see the element                        jQuery=td:contains("Jenna Diaz")
    Then the user selects the option from the drop-down menu   Academic  id=businessType
    And the user clicks the button/link                        jQuery=.govuk-button:contains("Filter")
    Then the user should see the element                       jQuery=td:contains("Felix Wilson")
    And the user should not see the element                    jQuery=td:contains("Jenna Diaz")
    [Teardown]    the user clicks the button/link  link=Clear all filters

View assessor progress page
    [Documentation]  IFS-321
    [Tags]
    [Setup]  the user clicks the button/link  jQuery=a:contains("21 to 40")
    Given the user clicks the button/link  jQuery=td:contains("Paul Plum") ~ td a:contains("View progress")
    Then The user should see the element   jQuery=h2:contains("Paul Plum")
    And the user should see the element    jQuery=h4:contains("Innovation area") ~ ul li:contains("Urban living") ~ li:contains("Smart infrastructure")
    And the user should see the element    jQuery=h4:contains("Type") ~ span:contains("Academic")
    And the user should see the element    jQuery=h2:contains("Assigned") + div td:contains("${Molecular_id}") + td:contains("Molecular tree breeding") + td:contains("Forest Universe") + td:contains("2")
    And the user should see the element    jQuery=h2:contains("Assigned") + div td:contains("${Molecular_id}") ~ td:contains("Yes") + td:contains("-") + td:contains("-")
    And the user should see the element    jQuery=h2:contains("Applications") ~ div td:contains("${Cryptocurrencies_id}") + td:contains("Living with Cryptocurrencies") + td:contains("Moveis")
    And the user should see the element    jQuery=h2:contains("Applications") ~ div td:contains("${Cryptocurrencies_id}") ~ td:contains("0") + td:contains("0") + td:contains("0")

Selecting Review assessor link shows the assessor page
    [Documentation]  IFS-1046
    [Tags]
    Given the user clicks the button/link  link=Review assessor
    Then the user should see the element   jQuery=h3:contains("Name") + p:contains("Paul Plum")

Accepting the application changes the Accepted column
    [Documentation]  IFS-321
    [Tags]
    [Setup]  Log in as a different user   &{assessor_credentials}
    Given the user accepts the application
    And Log in as a different user        &{Comp_admin1_credentials}
    When the user navigates to the page   ${server}/management/assessment/competition/${IN_ASSESSMENT_COMPETITION}/assessors/${Paul_Plum_id}
    Then the user should see the element  jQuery=td:contains("${Molecular_id}") ~ td:contains("Yes") + td:contains("Yes")

Remove an assigned application (Notified)
    [Documentation]    INFUND-1079
    [Tags]
    Given the user clicks the button/link     jQuery=td:contains("${Molecular_id}") ~ td:contains("Yes") ~ td:contains("Remove")
    When the user clicks the button/link      jQuery=button:contains("Remove assessor")
    Then the user should not see the element  jQuery=td:contains("${Molecular_id}") ~ td:contains("Yes") ~ td:contains("Remove")
    And the user should see the element       jQuery=h2:contains("Previously assigned") ~ div td:contains("${Molecular_id}") + td:contains("Molecular tree breeding") ~ td:contains("Reassign")
    And the user clicks the button/link       jQuery=.pagination-label:contains("Next")

Reassign a removed application
    [Documentation]    INFUND-398
    [Tags]
    Given the user clicks the button/link      jQuery=button:contains("Reassign")
    Then the user should not see the element   jQuery=h2:contains("Previously assigned") ~ div td:contains("${Molecular_id}") + td:contains("Molecular tree breeding") ~ td:contains("Reassign")
    And the user should see the element        jQuery=h2:contains("Assigned") ~ div td:contains("${Molecular_id}") + td:contains("Molecular tree breeding") ~ td:contains("Remove")

Assign an application to an assessor
    [Documentation]    IFS-811
    [Tags]
    Given the user clicks the button/link  link=Allocate assessors
    And the user clicks the button/link    jQuery=a:contains("41 to")
    When the user clicks the button/link   jQuery=td:contains("Shaun Bradley") ~ td a:contains("View progress")
    Then the user should see the element   jQuery=h2:contains("Assigned (0)") + p:contains("No applications have been assigned to this assessor")
    And the user clicks the button/link    jQuery=td:contains("36") ~ td button:contains("Assign")
    Then the user should see the element   jQuery=h2:contains("Assigned (1)") + .table-overflow tr:contains("36")

Filter by application number on the assessor page
    [Documentation]    IFS-400
    [Tags]
    Given the user enters text to a text field  css=#filterSearch    ${Intelligent_water}
    When the user clicks the button/link        jQuery=button:contains("Filter")
    Then the user should see the element        jQuery=tr:nth-child(1) td:nth-child(1):contains("${Intelligent_water}")
    And the user should not see the element     jQuery=.pagination-label:contains("Next")

Filtering of the applications
    [Documentation]    INFUND-8061
    [Setup]  the user navigates to the page    ${SERVER}/management/assessment/competition/${IN_ASSESSMENT_COMPETITION}
    Given the user clicks the button/link      jQuery=a:contains("Manage applications")
    When The user enters text to a text field  css=#filterSearch    ${Intelligent_water}
    and The user clicks the button/link        jQuery=button:contains("Filter")
    Then the user should see the element       jQuery=tr:nth-child(1) td:nth-child(1):contains("${Intelligent_water}")
    And The user clicks the button/link        link=Clear all filters
    then the user should not see the element   jQuery=tr:nth-child(1) td:nth-child(1):contains("${Intelligent_water}")

Application number navigates to Overview
    [Documentation]    INFUND-7042
    [Tags]
    When the user clicks the button/link           link=${Intelligent_water}
    Then The user should see the text in the page  Intelligent water system
    And the user should see the text in the page   University of Bath
    And the user should see the text in the page   Cardiff University
    [Teardown]    the user clicks the button/link  link=Back

View application progress page
    [Documentation]    INFUND-7042, INFUND-7046
    [Tags]
    Given the user clicks the button/link          jQuery=td:contains("Living with Cryptocurrencies") ~ td:contains("View progress")
    Then The user should see the element           jQuery=h2:contains("Living with Cryptocurrencies")
    And the user should see the element            jQuery=h3:contains("Partners") ~ ul:contains("Moveis (Lead)")
    And the user should see the element            jQuery=h3:contains("Innovation area") ~ span:contains("Digital manufacturing")
    And the user should see the element            jQuery=p:contains("No assessors have been assigned to this application")
    And the user should see the element            jQuery=p:contains("No assessors have rejected this application.")
    And the user should see the element            jQuery=p:contains("No assessors were previously assigned to this application.")

Review the application
    [Documentation]    INFUND-7046
    [Tags]
    When the user clicks the button/link  link=Review application
    Then the user should see the element  jQuery=h1:contains("Application overview")
    And the user should see the element   jQuery=dt:contains("Innovation area") + dd:contains("Digital manufacturing")
    [Teardown]    The user goes back to the previous page

View the available assessors
    [Documentation]    INFUND-7233
    [Tags]
    Then the user should see the element  jQuery=.govuk-table__header:contains("Assessor")
    And the user clicks the button/link   link=21 to 40
    And the available assessors information is correct

View the application assigned list
    [Documentation]    INFUND-7230 INFUND-7038
    [Tags]
    [Setup]  the user should see the element  jQuery=h2:contains("Living with Cryptocurrencies")
    Given The user should see the element  jQuery=p:contains("No assessors have been assigned to this application.")
    When the user clicks the button/link   jQuery=tr:contains("Paul Plum") button:contains("Assign")
    Then the user should see the element   jQuery=h2:contains("Assigned (1)")
    And the assigned list is correct before notification
    And the user clicks the button/link    link=Allocate applications
    Then the user should see the element   jQuery=td:contains("Living with Cryptocurrencies") ~ td:nth-child(4):contains("1")

Remove an assigned user (Not notified)
    [Documentation]    INFUND-7230
    [Tags]
    Given the user clicks the button/link  jQuery=td:contains("Living with Cryptocurrencies") ~ td:contains("View progress")
    Then the user clicks the button/link   jQuery=td:contains("Paul Plum") ~ td:contains("Remove")

Notify an assigned user
    [Documentation]    INFUND-7050
    [Tags]
    [Setup]  the user clicks the button/link   link=21 to 40
    Given the user clicks the button/link  jQuery=tr:contains("Paul Plum") button:contains("Assign")
    And the user clicks the button/link    link=Allocate applications
    And the user clicks the button/link    link=Manage assessments
    And the user clicks the button/link    link=Competition
    And the user clicks the button/link    jQuery=button:contains("Notify assessors")
    And the element should be disabled     jQuery=button:contains("Notify assessors")

Assessor should see the assigned application
    [Documentation]    INFUND-7050
    [Setup]    Log in as a different user  &{assessor_credentials}
    When The user clicks the button/link   link=${IN_ASSESSMENT_COMPETITION_NAME}
    Then The user should see the element   Link=Living with Cryptocurrencies

Remove and notify an assessor (Notified)
    [Documentation]    INFUND-7232
    [Tags]
    [Setup]    Log in as a different user         &{Comp_admin1_credentials}
    Given The user clicks the button/link         link=${IN_ASSESSMENT_COMPETITION_NAME}
    And the user clicks the button/link           jQuery=a:contains("Manage assessments")
    And the user clicks the button/link           jQuery=a:contains("Manage applications")
    And the user clicks the button/link           jQuery=td:contains("Living with Cryptocurrencies") ~ td:contains("View progress")
    When the user clicks the button/link          jQuery=td:contains("Paul Plum") ~ td:contains("Remove")
    And the user clicks the button/link           jQuery=.button-clear:contains("Cancel")
    And the user should not see the element       jQuery=button:contains("Remove assessor")
    And the user clicks the button/link           jQuery=td:contains("Paul Plum") ~ td:contains("Remove")
    And the user clicks the button/link           jQuery=button:contains("Remove assessor")
    And the user should see the text in the page  Previously assigned (1)
    And the previously assigned list is correct

Assessor should not see the removed application
    [Documentation]    INFUND-7232
    [Setup]    Log in as a different user     &{assessor_credentials}
    When The user clicks the button/link      link=${IN_ASSESSMENT_COMPETITION_NAME}
    Then The user should not see the element  Link=Living with Cryptocurrencies

Reassign and notify an assessor (Notified)
    [Documentation]    INFUND-7048
    [Tags]
    [Setup]    Log in as a different user          &{Comp_admin1_credentials}
    Given The user clicks the button/link          link=${IN_ASSESSMENT_COMPETITION_NAME}
    And the user clicks the button/link            jQuery=a:contains("Manage assessments")
    And the user clicks the button/link            jQuery=a:contains("Manage applications")
    And the user clicks the button/link            jQuery=td:contains("Living with Cryptocurrencies") ~ td:contains("View progress")
    And the user should see the text in the page   Previously assigned (1)
    And the user clicks the button/link            jQuery=tr:contains("Paul Plum") button:contains("Reassign")
    Then the user should see the text in the page  Assigned (1)
    And the assigned list is correct before notification
    And the user clicks the button/link            link=Allocate applications
    And the user clicks the button/link            link=Manage assessments
    And the user clicks the button/link            link=Competition
    And the user clicks the button/link            jQuery=button:contains("Notify assessors")
    And the element should be disabled             jQuery=button:contains("Notify assessors")

Assessor should see the reassigned application
    [Documentation]    INFUND-7050
    [Setup]    Log in as a different user  &{assessor_credentials}
    When The user clicks the button/link   link=${IN_ASSESSMENT_COMPETITION_NAME}
    Then The user should see the element   Link=Living with Cryptocurrencies

*** Keywords ***
the application list is correct before changes
    the user should see the element    jQuery=tr:nth-child(1) td:contains(The Best Juggling Company)
    the user should see the element    jQuery=tr:nth-child(1) td:contains(Park living)
    the user should see the element    jQuery=tr:nth-child(1) td:nth-child(1):contains("19")
    the user should see the element    jQuery=tr:nth-child(1) td:nth-child(2):contains("Park living")
    the user should see the element    jQuery=tr:nth-child(1) td:nth-child(3):contains("The Best Juggling Company")
    the user should see the element    jQuery=tr:nth-child(1) td:nth-child(4):contains("2")
    the user should see the element    jQuery=tr:nth-child(1) td:nth-child(5):contains("1")
    the user should see the element    jQuery=tr:nth-child(1) td:nth-child(6):contains("0")

the available assessors information is correct
    the user should see the element  jQuery=tr:contains("Mabel Robinson") td:contains("3") + td:contains("0") + td:contains("0") + td:contains("Assign")
    # TODO Add some skills too IFS-1298

the assigned list is correct before notification
    the user should see the element  jQuery=.assessors-assigned td:nth-child(1):contains("Paul Plum") ~ td:contains("Academic") ~ td:contains("Urban living") ~ td:contains("9") + td:contains("9")

the previously assigned list is correct
    the user should see the element    jQuery=.assessors-previous td:contains("Paul Plum") + td:contains("Academic") + td:contains("Urban living")
    the user should see the element    jQuery=.assessors-previous td:contains("Paul Plum") ~ td:contains("8") + td:contains("8")

the assessor list is correct before changes
    the user clicks the button/link  link=21 to 40
    the user should see the element  jQuery=td:contains("Paul Plum") ~ td:contains("Town Planning, Construction") ~ td:contains("8") ~ td:contains("8") ~ td:contains("4") ~ td:contains("0") ~ td:contains("View progress")

the user accepts the application
    the user clicks the button/link  link=${IN_ASSESSMENT_COMPETITION_NAME}
    the user clicks the button/link  link=Molecular tree breeding
    the user selects the radio button  assessmentAccept  true
    the user clicks the button/link  jQuery=button:contains("Confirm")