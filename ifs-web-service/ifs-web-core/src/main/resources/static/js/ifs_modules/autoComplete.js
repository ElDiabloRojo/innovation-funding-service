IFS.core.autoComplete = (function () {
  'use strict'
  var s // private alias to settings
  return {
    settings: {
      autoCompleteElement: '[data-auto-complete]',
      autoCompletePlugin: accessibleAutocomplete // eslint-disable-line
    },
    init: function () {
      s = this.settings
      var autoCompleteElement = jQuery(s.autoCompleteElement)
      if (autoCompleteElement.length > 0) {
        var showAllValues = autoCompleteElement.children('option').length <= 20
        s.autoCompletePlugin.enhanceSelectElement({
          selectElement: autoCompleteElement[0],
          showAllValues: showAllValues,
          defaultValue: '',
          confirmOnBlur: false
        })
      }
    }
  }
})()
