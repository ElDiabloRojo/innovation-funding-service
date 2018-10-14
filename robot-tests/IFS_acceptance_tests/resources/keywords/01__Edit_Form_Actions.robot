*** Settings ***
Resource          ../defaultResources.robot

*** Keywords ***
# Checkbox
the user selects the checkbox
    [Arguments]    ${checkbox}
    ${status}    ${value}=    Run Keyword And Ignore Error Without Screenshots    Element Should Be Visible    css=[id="${checkbox}"]:not(:checked) ~ label, [name="${checkbox}"]:not(:checked) ~ label
    Execute Javascript    jQuery('form label a').contents().unwrap();    # we cannot click the checkbox itself as it is hidden, however if we click the label it will click the anchor in the label, therefore I remove the <a> before submit, but keep the text
    Run Keyword If    '${status}' == 'PASS'    Click Element    css=[id="${checkbox}"] ~ label, [name="${checkbox}"] ~ label
    # Error checking
    the user should not see an error in the page
    Sleep  400ms

the user unselects the checkbox
    [Arguments]    ${checkbox}
    ${status}    ${value}=    Run Keyword And Ignore Error Without Screenshots    Element Should Be Visible    css=[id="${checkbox}"]:checked ~ label,[name="${checkbox}"]:checked ~ label
    Execute Javascript    jQuery('form label a').contents().unwrap();    # we cannot click the checkbox itself as it is hidden, however if we click the label it will click the anchor in the label, therefore I remove the <a> before submit, but keep the text
    Run Keyword If    '${status}' == 'PASS'    Click Element    css=[id="${checkbox}"] ~ label,[name="${checkbox}"] ~ label
    # Error checking
    the user should not see an error in the page
    Sleep  400ms

the user should see that the checkbox is disabled
    [Arguments]    ${checkbox}
    Wait Until Element Is Visible Without Screenshots    css=[id="${checkbox}"][disabled="disabled"]:checked ~ label, [name="${checkbox}"][disabled="disabled"]:checked ~ label
    # Error checking
    the user should not see an error in the page

the user should see that the checkbox is selected
    [Arguments]    ${checkbox}
     Wait Until Element Is Visible Without Screenshots    css=[id="${checkbox}"][checked="checked"]:checked ~ label, [name="${checkbox}"][checked="checked"]:checked ~ label
     # Error checking
     the user should not see an error in the page

the user should not see the checkbox
    [Arguments]    ${checkbox}
    Wait Until Element Is Not Visible Without Screenshots    css=[id="${checkbox}"]:checked ~ label, [name="${checkbox}"]:checked ~ label
    # Error checking
    the user should not see an error in the page

# Radio Buttons
the user selects the radio button
    [Arguments]    ${RADIO_BUTTON}    ${RADIO_BUTTON_OPTION}
    the user should see the element    css=[name^="${RADIO_BUTTON}"][value="${RADIO_BUTTON_OPTION}"] ~ label, [id="${RADIO_BUTTON_OPTION}"] ~ label
    Click Element     css=[name^="${RADIO_BUTTON}"][value="${RADIO_BUTTON_OPTION}"] ~ label, [id="${RADIO_BUTTON_OPTION}"] ~ label
    # Error checking
    the user should not see an error in the page
    Sleep  400ms

the user sees that the radio button is selected
    [Arguments]    ${RADIO_BUTTON}    ${SELECTION}
    Wait Until Element Is Visible Without Screenshots    css=[name="${RADIO_BUTTON}"][value="${SELECTION}"]:checked ~ label, [id="${SELECTION}"]:checked ~ label
    #[contains(@class,"selected")]
    # Error checking
    the user should not see an error in the page

# Focus
the user moves focus to the element
    [Arguments]    ${element}
    # Error checking
    the user should not see an error in the page
    Wait Until Element Is Visible Without Screenshots    ${element}
    Set Focus To Element    ${element}

the user moves the mouse away from the element
    [Arguments]    ${element}
    # Error checking
    the user should not see an error in the page
    Wait Until Element Is Visible Without Screenshots    ${element}
    mouse out    ${element}

The user enters text to a text field
    [Arguments]    ${TEXT_FIELD}    ${TEXT_INPUT}
    Wait Until Element Is Visible Without Screenshots    ${TEXT_FIELD}
    Clear Element Text    ${TEXT_FIELD}
    Wait Until Keyword Succeeds Without Screenshots    10    200ms    input text    ${TEXT_FIELD}    ${TEXT_INPUT}
    Mouse Out    ${TEXT_FIELD}
    Run Keyword And Ignore Error Without Screenshots    Set Focus To Element    link=Sign out
    Wait for autosave

The user enters large text to a text field
    # Note that here we give extra time of execution compared to keyword "The user enters text to a text field"
    [Arguments]    ${TEXT_FIELD}    ${TEXT_INPUT}
    Wait Until Element Is Visible Without Screenshots    ${TEXT_FIELD}
    Clear Element Text    ${TEXT_FIELD}
    Wait Until Keyword Succeeds Without Screenshots    10    1500ms    input text    ${TEXT_FIELD}    ${TEXT_INPUT}
    Mouse Out    ${TEXT_FIELD}
    Run Keyword And Ignore Error Without Screenshots    Set Focus To Element    link=Sign out
    Wait for autosave

the user sees the text in the element
    [Arguments]    ${element}    ${text}
    Wait Until Element Is Visible Without Screenshots    ${element}
    Wait Until Keyword Succeeds Without Screenshots    10    500ms    element should contain    ${element}    ${text}

the user clears the text from the element
    [Arguments]    ${element}
    Wait Until Element Is Visible Without Screenshots    ${element}
    clear element text    ${element}
    the user should not see an error in the page

the user sees the text in the text field
    [Arguments]    ${textfield}    ${text}
    Wait Until Element Is Visible Without Screenshots    ${textfield}
    Wait Until Keyword Succeeds Without Screenshots    10    200ms    textfield should contain    ${textfield}    ${text}

The user enters multiple strings into a text field
    [Arguments]    ${field}    ${string}    ${multiplicity}
    #Keyword uses custom IfsLibrary keyword "repeat string"
    ${concatenated_string} =    repeat string    ${string}    ${multiplicity}
    Wait Until Element Is Visible Without Screenshots   ${field}
    Wait Until Keyword Succeeds Without Screenshots     30s    200ms    Input Text    ${field}    ${concatenated_string}
    Mouse Out                                           ${field}
    Run Keyword And Ignore Error Without Screenshots    Set Focus To Element    link=Sign out
    Wait for autosave

# DropDown
the user selects the option from the drop-down menu
    [Arguments]    ${option}    ${drop-down}
    Wait Until Element Is Visible Without Screenshots    ${drop-down}
    Wait Until Element Is Enabled   ${drop-down}
    Select From List By Label    ${drop-down}    ${option}
    mouse out    ${drop-down}
    # Error checking
    the user should not see an error in the page

the user selects the index from the drop-down menu
    [Arguments]    ${option}    ${drop-down}
    Wait Until Element Is Visible Without Screenshots    ${drop-down}
    Wait Until Element Is Enabled   ${drop-down}
    Select From List By Index    ${drop-down}    ${option}
    mouse out    ${drop-down}
    # Error checking
    the user should not see an error in the page

the user should see the option in the drop-down menu
    [Arguments]    ${option}    ${drop-down}
    wait until element is visible without screenshots    ${drop-down}
    ${drop-down-options}=    get list items    ${drop-down}
    list should contain value    ${drop-down-options}    ${option}
    mouse out    ${drop-down}
    # Error checking
    the user should not see an error in the page


the user should see the dropdown option selected
    [Arguments]    ${option}    ${drop-down}
    List Selection Should Be    ${drop-down}    ${option}
    # Error checking
    the user should not see an error in the page

Remove previous rows
    [Arguments]  ${element}
    :FOR    ${i}    IN RANGE  10
    # The sleep of 200 ms is actually for speed, originally the test used "should not see the element" however that made it wait for 10 seconds on every loop.
    \  sleep    200ms
    \  ${STATUS}    ${VALUE}=    Run Keyword And Ignore Error Without Screenshots    the user should see the element    ${element}
    \  Log    ${status}
    \  Exit For Loop If  '${status}'=='FAIL'
    \  run keyword if  '${status}'=='PASS'  the user clicks the button/link  ${element}
    \  ${i} =  Set Variable  ${i + 1}