
var searchInputs = new Array("wf_search_by",
        "wf_search_author",
        "wf_search_component",
        "wf_search_date_lower",
        "wf_search_date_upper",
        "wf_search_access_shared",
        "wf_search_access_no_auth",
        "wf_has_papers",
        "wf_search_access_request",
        "workflowTagsDivSearch");


function updateSearch(event) {
    var key = "";
    key = event.keyCode || event.which;

    if (isAlphaNumeric(key)) {

        updateSearchFilter(this);

        if (key == 13) {
            // User hits Enter key
            jQuery('#wf_search_component').autocomplete('close');
            saveSearchFilters();
        }
    }

}

function saveSearchFilters() {
    var searchFilters = {};
    jQuery.each(searchInputs, function(siIndex, siString) {
        if (jQuery('#' + siString).val() !== undefined
                && siString != 'wf_search_access_shared' && siString != 'wf_search_access_no_auth'
                    && siString != 'wf_has_papers') {
            var value = escapeDomValue(jQuery('#' + siString).val());
            if (value.trim() != 'Quick Search...') {
                searchFilters[siString] = value;
            } else {
                searchFilters[siString] = '';
            }
        } else {
            if (jQuery('#' + siString).prop('checked') ) {
                searchFilters[siString] = true;
            } else {
                searchFilters[siString] = false;
            }
        }
    });

    var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");
    fetchWorkflowRows(panelId, 1, searchFilters);
}

function updateSearchFilter(myInputElem) {
    var searchFilters = {};
    isSearchDirty = true;

    var value = jQuery(myInputElem).val();

    searchFilters[jQuery(myInputElem).attr('id')] = value;
    jQuery(myInputElem).val(value);

}

function checkSearchTerms() {
    var disableClearSearchButton = true;
    jQuery.each(searchInputs, function(siIndex, siString) {
        if (jQuery('#' + siString).val().trim() != ''
            && jQuery('#' + siString).val().trim() != 'Quick Search...') {
            disableClearSearchButton = false;
        }
    });

    if (disableClearSearchButton) {
        jQuery('#wfClearSearch').button('disable');
    }
}

function clearSearchAttributes() {

    var searchFilters = {};
    jQuery.each(searchInputs, function(siIndex, siString) {
        jQuery('#' + siString).val('');
        searchFilters[siString] = '';
    });

    searchFilters["wf_search_dataset"] = '';

    jQuery('#wf_search_by').text( "Quick Search...");
    jQuery('#wf_search_by').hint("auto-hint");

    jQuery('#wf_search_access_request').prop('checked', true);
    jQuery('#wf_search_access_no_auth').prop('checked', true);
    jQuery('#wf_search_access_shared').prop('checked', true);
    jQuery('#wf_has_papers').prop('checked', false);

    removeAllTags(true);
    jQuery('#wfTagInputSearch').val('');

    isSearchDirty = false;

    var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");
    fetchWorkflowRows(panelId, 1, searchFilters);

}

function wfSearchFiltersUpdate() {

    var isChecked = false;

    var checkboxId = jQuery(this).attr('id');
    if (jQuery(this).prop('checked') == true) {
        isChecked = true;
    }

    var searchFilters = {};

    searchFilters[checkboxId] = isChecked;

    isSearchDirty = true;

    var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");
    fetchWorkflowRows(panelId, 1, searchFilters);

}

