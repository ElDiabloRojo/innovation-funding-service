IFS.competitionManagement.eligibility = (function () {
  'use strict'
  return {
    init: function () {
      IFS.competitionManagement.eligibility.setOverrideFundingRulesVisibility()
      jQuery(document).on('change', 'input[name="researchCategoriesApplicable"]', function () {
        IFS.competitionManagement.eligibility.handleResearchCategoriesApplicableChange(this)
      })
      jQuery(document).on('change', 'input[name="overrideFundingRules"]', function () {
        IFS.competitionManagement.eligibility.handleOverrideFundingRulesChange(this)
      })
    },
    handleResearchCategoriesApplicableChange: function (el) {
      var researchCategoriesApplicable = jQuery(el)
      var status = researchCategoriesApplicable.val() === 'true'
      IFS.competitionManagement.eligibility.setOverrideFundingRulesVisibility()
      if (!status) {
        jQuery('#override-funding-rules').find('input[name="overrideFundingRules"]').prop('checked', false)
      }
    },
    handleOverrideFundingRulesChange: function (el) {
      var handleOverrideFundingRules = jQuery(el)
      var status = handleOverrideFundingRules.val() === 'true'

      IFS.competitionManagement.eligibility.setContainerVisibility('#funding-level-override', status)
    },
    setOverrideFundingRulesVisibility: function () {
      var researchCategoriesFalseIsChecked = jQuery('input[name="researchCategoriesApplicable"][value="false"]').is(':checked')
      var overrideFundingRulesTrueIsChecked = jQuery('input[name="overrideFundingRules"][value="true"]').is(':checked')

      IFS.competitionManagement.eligibility.setContainerVisibility('#override-funding-rules', !researchCategoriesFalseIsChecked)
      IFS.competitionManagement.eligibility.setContainerVisibility('#funding-level-override', overrideFundingRulesTrueIsChecked)
      IFS.competitionManagement.eligibility.setContainerVisibility('#funding-level', researchCategoriesFalseIsChecked)
    },
    setContainerVisibility: function (id, visible) {
      var container = jQuery(id)
      if (visible) {
        container.attr('aria-hidden', 'false')
        container.find('select').prop('disabled', false)
      } else {
        container.attr('aria-hidden', 'true')
        container.find('select').val('').prop('disabled', true)
      }
    }

  }
})()
