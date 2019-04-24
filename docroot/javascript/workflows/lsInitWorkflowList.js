jQuery.noConflict();

var isSearchDirty = false;
var currentComponentList = null;
var currentAuthorList = null;

jsPlumb.ready(function() {

    var isResultsPage = false;
    var isEditor = false;
    var isListing = false;
    var currentPage = null;
    if (jQuery('body').data("wfState") != null) {
        isEditor = true;
        currentPage = 'editor';
    } else if (jQuery('body').data("wfResults") == null) {
        isListing = true;
        currentPage = 'listing';
    } else if (jQuery('body').data("wfResults") != null) {
        isResultsPage = true;
        currentPage = 'results';
    }

    // Search by Component list
    if (jQuery('body').data("componentList") != null) {
        currentComponentList = jQuery('body').data("componentList");
    }

    if (jQuery('body').data("existingWorkflowTags") != null) {
        existingWorkflowTags = jQuery('body').data("existingWorkflowTags");
        // Create the search by tags
        createSearchViaTags();
    }

    // Search by author list
    if (jQuery('body').data("authorList") != null) {
        currentAuthorList = jQuery('body').data("authorList");
    }

    jQuery.curCSS = function(element, prop, val) {
        return jQuery(element).css(prop, val);
    };

    jQuery('.feedback a').click(createFeedbackPrompt);

    jQuery('.refreshWorkflowComponentDefinitions').click(refreshWorkflowComponentDefinitions);

    /* Search bindings. */
    jQuery('#wf_search_by').hint("auto-hint");

    jQuery('#wfClearSearch').button({
         label : "Clear",
         classes: {
                "ui-button": "create-button-class"
         }
     });
    jQuery('#wfClearSearch').click(clearSearchAttributes);

    jQuery('#wfSearch').button({
        label : "Search",
        classes: {
               "ui-button": "create-button-class"
        }
    });
    jQuery('#wfSearch').click(saveSearchFilters);


    // colorScrollBars();
    // With IE 11, the string 'Trident' appears in this.
    var ua = navigator.userAgent;
    if (Prototype.Browser.IE || ua.indexOf('Trident') > 0 || ua.indexOf('Edge') > 0) {
        // Our version of jQuery doesn't support resizable in Edge/IE
    } else {
        jQuery("#wfMessageBar").resizable({handles: 'n'});
    }

    /* Workflow list */
    var loadPanel = jQuery('body').data("loadPanel");
    if (loadPanel === undefined) {
        loadPanel = 'my-workflows-panel';
    }



    // Tabbed workflows page
    jQuery('#wfTabDiv').tabs({
        collapsible: false
    });

    // If empty, disable my workflows
    //jQuery('#wfTabDiv').tabs( 'option', 'disable', 1 );
    // If empty, disable shared workflows tab (tab 2)

    // Enable the shared workflows tab (tab 2)
    //jQuery('#wfTabDiv').tabs( 'option', 'enable', 2 );

    // On activate, display the contents
    jQuery('#wfTabDiv').tabs({
        activate: function(event, ui) {
            if (ui !== undefined && ui.newPanel !== undefined
                    && jQuery(ui.newPanel).attr('id') !== undefined) {

                var panelId = jQuery(ui.newPanel).attr('id');
                // My Workflows
                if (panelId == 'my-workflows-panel') {
                    // No 'Data Access' search on this panel
                    jQuery('#wfSearchAccess').css('display', 'none');

                    // Disable author and hide legend
                    jQuery('#wf_search_author').prop('disabled', true);
                    //jQuery('#wfLegend').css('display', 'none');

                    // Get workflow-folder rows
                    fetchWorkflowRows(panelId, null, null);
                    jQuery('.wfCreateFolderDiv').css('display', '');

                    jQuery('body').data("loadPanel", panelId);

                // Shared Workflows
                } else if (panelId == 'shared-workflows-panel') {
                    // Enable author and show legend
                    jQuery('#wf_search_author').prop('disabled', false);
                    //jQuery('#wfLegend').css('display', '');

                    // Get workflow-folder rows
                    fetchWorkflowRows(panelId, null, null);
                    jQuery('.wfCreateFolderDiv').css('display', 'none');
                    jQuery('body').data("loadPanel", panelId);

                // Recommended Workflows
                } else if (panelId == 'recommended-workflows-panel') {
                    // Enable author and show legend
                    jQuery('#wf_search_author').prop('disabled', false);
                    // jQuery('#wfLegend').css('display', '');

                    // Get workflow-folder rows
                    fetchWorkflowRows(panelId, null, null);
                    jQuery('.wfTabAddFolderIcon').css('display', 'none');
                    jQuery('body').data("loadPanel", panelId);

                }
            }
        }
    });

    jQuery('a[href="#' + loadPanel + '"]').click();

    if (loadPanel == 'my-workflows-panel') {
        fetchWorkflowRows(loadPanel, null, null);
    }

    jQuery('.wfCreateFolderDiv').click(function() {
        createFolderDialog();
    });

    jQuery('#wfTabDiv').tabs('option', 'disable', 1);

    // Date pickers should
    jQuery('#wf_search_date_lower').datepicker({
          onSelect: function(selectedDate) {
                updateSearchFilter(this);
                saveSearchFilters();
              }
            });
    jQuery('#wf_search_date_upper').datepicker({
        onSelect: function(selectedDate) {
            updateSearchFilter(this);
            saveSearchFilters();
          }
        });

    // Enter should submit the search from any inputs (not date pickers)
    jQuery.each(searchInputs, function(siIndex, siString) {
        if (siString != 'wf_search_date_lower' && siString != 'wf_search_date_upper') {
            jQuery('#' + siString).keyup(updateSearch);
        } else {
            jQuery('#' + siString).keyup(function(e) {
                if (e.keyCode == 8 || e.keyCode == 46) {
                    jQuery.datepicker._clearDate(this);
                    saveSearchFilters();
                }
            });
        }
    });

    //disabled: jQuery('.searchCheckbox input').click(wfSearchFiltersUpdate);

    jQuery('.searchPaperCheckbox input').click(wfSearchFiltersUpdate);

    jQuery('.removeSearchFilter').click(removeSearchFilter);

    // Auto-complete on by component
    //jQuery(currentComponentList).each(function() {
    //});

    // Auto-complete by component
    jQuery('#wf_search_component').autocomplete({
        source : currentComponentList,
        minLength: 0,
        select: function(event, ui) {
            // When a component is selected: Set the value, then search
            if (ui != null && ui.item != null && ui.item.value != null) {
                jQuery('#wf_search_component').val(ui.item.value);
                jQuery('#wfSearch').click();
            }
        }
    });

    jQuery('#wf_search_component').click(function() { jQuery('#wf_search_component').autocomplete("search", ""); });



    // Auto-complete by author
    /*jQuery('#wf_search_author').autocomplete({
        source : currentAuthorList
    });*/

    jQuery('.lsShareLink').slideUp(0);

    //getLoggedInUser();

    // end of jsPlumb.ready
});

