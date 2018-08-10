*** Settings ***
Documentation     INFUND-6923 Create new public Competition listings page for Applicants to view open and upcoming competitions
...
...               INFUND-7946 Sign in page facelift
...
...               IFS-247 As an applicant I am able to see the competitions in 'Competition listings' in reverse chronological order
...
...               IFS-1117 As a comp exec I am able to set Application milestones in Non-IFS competition details (Initial view)
Suite Setup       The guest user opens the browser
Force Tags        Applicant
Resource          ../../../resources/defaultResources.robot
Resource          ../../02__Competition_Setup/CompAdmin_Commons.robot

*** Test Cases ***
Guest user navigates to Front Door
    [Documentation]    INFUND-6923 INFUND-7946 IFS-247
    [Tags]
    [Setup]  the user navigates to the page  ${FRONTDOOR}
    When the user should see the element     jQuery=a:contains("Innovate UK")
    Then the user should see the element     jQuery=h1:contains("Innovation competitions")
    And the user should see the element     css=#keywords
    Then the user should see the element     css=#innovation-area
    # Guest user can see competitions sorted in reverse chronological order by opening date
    When verify first date is greater than or equal to second  css=li:nth-child(1) .date-definition-list dd:nth-of-type(1)  css=li:nth-child(2) .date-definition-list dd:nth-of-type(1)
    Then verify first date is greater than or equal to second  css=li:nth-child(2) .date-definition-list dd:nth-of-type(1)  css=li:nth-child(3) .date-definition-list dd:nth-of-type(1)
    When the user clicks the button/link     link=Contact us
    Then the user should see the element     jQuery=h1:contains("Contact us")
    And the user should not see an error in the page
    And the user should see the element      jQuery=a:contains("feedback")

Guest user can see Competitions and their information
    [Documentation]    INFUND-6923
    [Tags]
    [Setup]    the user navigates to the page    ${frontDoor}
    Given the user should see the element in the paginated list     link=${createApplicationOpenCompetition}
    Then the user should see the element    jQuery=h3:contains("Eligibility")
    And the user should see the element    jQuery=div:contains("UK based business of any size. Must involve at least one SME")
    Then the user should see the element    jQuery=dt:contains("Opened") + dd:contains("${createApplicationOpenCompetitionOpenDate}")
    And the user should see the element    jQuery=dt:contains("Closes") + dd:contains("${createApplicationOpenCompetitionCloseDate}")
    #Guest user can filter competitions by Keywords, this is tested in file 05__Public_content.robot

Guest user can see the opening and closing status of competitions
    [Documentation]  IFS-268
    [Tags]    MySQL
    [Setup]  Connect to Database  @{database}
    Get competitions id and set it as suite variable  ${READY_TO_OPEN_COMPETITION_NAME}
    ${openDate}  ${submissionDate} =  Save competition's current dates  ${competitionId}

    Given the user navigates to the page  ${frontDoor}
    Then the user can see the correct date status of the competition  ${READY_TO_OPEN_COMPETITION_NAME}  Opening soon  Opens

    Given Change the open date of the Competition in the database to one day before  ${READY_TO_OPEN_COMPETITION_NAME}
    When the user navigates to the page  ${frontDoor}
    Then the user can see the correct date status of the competition  ${READY_TO_OPEN_COMPETITION_NAME}  Open now  Opened

    Given Change the close date of the Competition in the database to thirteen days  ${READY_TO_OPEN_COMPETITION_NAME}
    When the user navigates to the page  ${frontDoor}
    Then the user can see the correct date status of the competition  ${READY_TO_OPEN_COMPETITION_NAME}  Closing soon  Opened

    [Teardown]  Return the competition's milestones to their initial values  ${competitionId}  ${openDate}  ${submissionDate}

Guest user can filter competitions by Innovation area
    [Documentation]    INFUND-6923
    [Tags]    HappyPath
    [Setup]    the user navigates to the page    ${frontDoor}
    When the user selects the option from the drop-down menu    Space technology    id=innovation-area
    And the user clicks the button/link    jQuery=button:contains("Update results")
    Then the user should see the element    jQuery=a:contains("Transforming big data")
    And the user should not see the element in the paginated list   jQuery=a:contains("${createApplicationOpenCompetition}")
    When the user selects the option from the drop-down menu    Any    id=innovation-area
    And the user clicks the button/link    jQuery=button:contains("Update results")
    Then the user should see the element in the paginated list   jQuery=a:contains("${createApplicationOpenCompetition}")

Guest user can see the public information of an unopened competition
    [Documentation]    INFUND-8714
    [Tags]  Pending
    # TODO IFS-2986
    [Setup]    the user navigates to the page    ${frontDoor}
    Given the user clicks the button/link in the paginated list    link=${READY_TO_OPEN_COMPETITION_NAME}
    Then the user should see the element    jQuery=h1:contains("${READY_TO_OPEN_COMPETITION_NAME}")
    And the user should see the element    jQuery=strong:contains("Competition opens") + span:contains("Saturday 24 February 2018")
    And the user should see the element    jQuery=li:contains("Competition closes")
    And the user should see the element    jQuery=li:contains("Friday 16 April 2018")
    And the user should see the text in the page    This competition has not yet opened.
    And the user should not see the text in the page    Or sign in to continue an existing application
    And the user should see the element    jQuery=.govuk-button:contains("Start new application")

Registration is closed on Non-IFS competitition when the Registration date is in the past
    [Documentation]  IFS-38 IFS-1117
    [Tags]    MySQL
    [Setup]  Connect to Database    @{database}
    Given Change the close date of the Competition in the database to tomorrow  ${NON_IFS_COMPETITION_NAME}
    And the registration date of the non-ifs competition belongs to the past  ${competition_ids['${NON_IFS_COMPETITION_NAME}']}
    When the user navigates to the page  ${server}/competition/${competition_ids['${NON_IFS_COMPETITION_NAME}']}/overview
    Then the user should see the element  jQuery=.warning-alert:contains("Registration has now closed.")

Guest user can see the public information of a competition
    [Documentation]    INFUND-6923
    [Tags]
    [Setup]    the user navigates to the page    ${frontDoor}
    Given the user clicks the button/link in the paginated list    link=${UPCOMING_COMPETITION_TO_ASSESS_NAME}
    Then the user should see the element    jQuery=h1:contains("${UPCOMING_COMPETITION_TO_ASSESS_NAME}")
    And the user should see the element    jQuery=strong:contains("Competition opens") + span:contains("${UPCOMING_COMPETITION_TO_ASSESS_OPEN_DATE}")
    And the user should see the element    jQuery=li:contains("Competition closes")
    And the user should see the element    jQuery=li:contains("${UPCOMING_COMPETITION_TO_ASSESS_CLOSE_DATE_TIME_LONG}")
    And the user should see the text in the page    Or sign in to continue an existing application.
    And the user should see the element    jQuery=.govuk-button:contains("Start new application")

Guest user can see the public Summary of the competition
    [Documentation]    INFUND-6923
    [Tags]
    Given the user clicks the button/link    link=Summary
    Then the user should see the element    jQuery=h3:contains("Description")
    And the user should see the text in the page    Innovate UK is investing up to £15 million in innovation projects to stimulate the new products and services of tomorrow.
    When the user should see the element    jQuery=h3:contains("Funding type")
    Then the user should see the element    jQuery=p:contains("Grant")
    When the user should see the element    jQuery=h3:contains("Project size")
    Then the user should see the element    jQuery=p:contains("£15 million")

Guest user can see the public Eligibility of the competition
    [Documentation]    INFUND-6923
    [Tags]
    Given the user clicks the button/link    link=Eligibility
    Then the user should see the element    jQuery=h3:contains("Lead applicant eligibility")
    And the user should see the text in the page    one SME involved in your proposal carry out your project work, and intend to

Guest user can see the public Scope of the competition
    [Documentation]    INFUND-6923
    [Tags]
    Given the user clicks the button/link    link=Scope
    Then the user should see the element    jQuery=h3:contains("Project scope")
    And the user should see the text in the page    Projects will: harness E&E technologies across the economy develop and scale-up research and development to bring ideas,

Guest user can see the public Dates of the competition
    [Documentation]    INFUND-6923
    [Tags]
    Given the user clicks the button/link    link=Dates
    When the user should see the element    jQuery=dt:contains("${getPrettyMilestoneDate(${UPCOMING_COMPETITION_TO_ASSESS_ID}, "OPEN_DATE")}") + dd:contains("Competition opens")
    And the user should see the element    jQuery=dt:contains("12 May 2016") + dd:contains("Briefing event in Belfast")
    And the user should see the element    jQuery=dt:contains("${UPCOMING_COMPETITION_TO_ASSESS_CLOSE_DATE_TIME}") + dd:contains("Competition closes")
    And the user should see the element    jQuery=dt:contains("${UPCOMING_COMPETITION_TO_ASSESS_NOTIFICATION_DATE}") + dd:contains("Applicants notified")

Guest user can see the public How to apply of the competition
    [Documentation]    INFUND-6923
    [Tags]
    Given the user clicks the button/link    link=How to apply
    When the user should see the element    jQuery=h3:contains("How to apply")
    Then the user should see the text in the page    Collaborators will be sent a link,

Guest user can see the public Supporting information of the competition
    [Documentation]    INFUND-6923
    [Tags]
    Given the user clicks the button/link    link=Supporting information
    When the user should see the element    jQuery=h3:contains("Background and further information")
    Then the user should see the text in the page    However, we sometimes struggle to fully commercialise the opportunities.

Guest user can apply to a competition
    [Documentation]    INFUND-6923
    [Tags]    HappyPath
    [Setup]    the user navigates to the page    ${frontDoor}
    Given the user clicks the button/link in the paginated list    link=${createApplicationOpenCompetition}
    When the user clicks the button/link    link=Start new application
    Then the user should see the element    jQuery=.govuk-button:contains("Sign in")
    And the user should see the element    jQuery=.govuk-button:contains("Create")

*** Keywords ***
Close survey window
    Close Window
    Select Window

the user can see the correct date status of the competition
    [Arguments]    ${competition_name}    ${date_status}    ${open_text}
    the user should see the element    jQuery=h2:contains("${competition_name}") ~ h3:contains("${date_status}") ~ dl dt:contains("${open_text}")

the registration date of the non-ifs competition belongs to the past
    [Arguments]  ${competitionId}
    ${yesterday} =  get yesterday
    execute sql string  UPDATE `${database_name}`.`milestone` SET `date`='${yesterday}' WHERE `competition_id`='${competitionId}' AND `type`='REGISTRATION_DATE';