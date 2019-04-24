jQuery.noConflict();

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
    jQuery.curCSS = function(element, prop, val) {
        return jQuery(element).css(prop, val);
    };

    recentComponents = jQuery('body').data("recentComponentsJson");

    buildComponentTitleMap(jQuery('body').data("wfComponentMenuJson"), null);
    editWorkflow(jQuery('body').data("wfXml"),
        jQuery('body').data("lastErrorMessage"),
            jQuery('body').data('isDsAdmin'),
                jQuery('body').data('wfState'),
                    jQuery('body').data("wfSupportingXml"),
                        jQuery('body').data("wfComponentMenuJson"));

    if (jQuery('body').data('wfState') == undefined && jQuery('body').data('wfState') == null
            || jQuery('body').data('wfState') != 'running') {
        processWorkflowResults(true, jQuery('body').data("wfResults"));
    }

    jQuery('.wfAccessRequestLink').click(createWfAccessRequest);

    unrequestedProjects = jQuery('body').data("unrequestedProjects");
    accessibleProjects = jQuery('body').data("accessibleProjects");
    pendingRequestProjects = jQuery('body').data("pendingRequestProjects");
    reRequestProjects = jQuery('body').data("reRequestProjects");
    nonShareableProjects = jQuery('body').data("nonShareableProjects");
    globalWfPaperCount = jQuery('body').data("paperCount");

    hasUnownedPrivateFiles = jQuery('body').data("hasUnownedPrivateFiles");

    jQuery('.component-selection-header').click(function() {
        if (jQuery(this).attr('name') !== undefined && jQuery(this).attr('name').trim() != '') {
            logWorkflowComponentAction(currentDigraph.id, null,
                null, null, jQuery(this).attr('name'), null,
                        null, null, null, LOG_CLICK_COMPONENT_MENU, "");
        }
    });

    jQuery('.feedback a').click(createFeedbackPrompt);

    // colorScrollBars();
    // With IE 11, the string 'Trident' appears in this.
    var ua = navigator.userAgent;
    if (Prototype.Browser.IE || ua.indexOf('Trident') > 0 || ua.indexOf('Edge') > 0) {
        // Our version of jQuery doesn't support resizable in Edge/IE
    } else {
        jQuery("#wfMessageBar").resizable({handles: 'n'});
    }

    jQuery('.lsShareLink').slideUp(0);
    jQuery('.linkMenu').slideUp(0);
    if (currentDigraph.isView !== undefined && currentDigraph.isView !== true) {
        jQuery(document).keydown(function(evt) {

            var isInput = false;
            var activeObjects = jQuery(document.activeElement);
            if (activeObjects.length > 0) {
                var activeObject = activeObjects[0];
                if (activeObject !== null && activeObject.tagName !== undefined
                        && (activeObject.tagName.toLowerCase() == 'textarea'
                            || activeObject.tagName.toLowerCase() == 'text'
                            || activeObject.tagName.toLowerCase() == 'input')) {
                    isInput = true;
                }
            }
            var key = undefined;
            var possible = [ evt.key, evt.keyIdentifier, evt.keyCode, evt.which ];

            while (key === undefined && possible.length > 0)
            {
                key = possible.pop();
            }

            if (!isInput && key !== undefined && (key == '65' || key == '97' ) && (evt.ctrlKey || evt.metaKey) && !(evt.altKey)) {
                // A pressed
                evt.preventDefault();
                selectAllComponents();
                return false;
            } else if (!isInput && key !== undefined && (key == '67' || key == '99' ) && (evt.ctrlKey || evt.metaKey) && !(evt.altKey)) {
                // C pressed
                evt.preventDefault();
                copyComponents();
                return false;
            } else if (!isInput && key !== undefined && (key == '68' || key == '100' ) && (evt.ctrlKey || evt.metaKey) && !(evt.altKey)) {
                // D pressed
                evt.preventDefault();
                deleteSelectedComponents();
                return false;
            } else if (!isInput && key !== undefined && (key == '86' || key == '118' ) && (evt.ctrlKey || evt.metaKey) && !(evt.altKey)) {
                // V pressed
                evt.preventDefault();
                pasteComponents();
                return false;
            } else if (isInput) { return; } // Other keys that could be used (requires discussion)
              /*else if (key !== undefined && (key == '82' || key == '114' ) && (evt.ctrlKey || evt.metaKey) && !(evt.altKey)) {
                // R pressed
                evt.preventDefault();
                saveAndRunCurrentWorkflow(currentDigraph.id);
                return false;
            } else if (key !== undefined && (key == '83' || key == '115' ) && (evt.ctrlKey || evt.metaKey) && !(evt.altKey)) {
                // S pressed
                evt.preventDefault();
                if (!isWorkflowSaved) {
                    saveCurrentWorkflow(currentDigraph.id, null, null, null, null);
                }
                return false;
            } else if (key !== undefined && (key == '88' || key == '120' ) && (evt.ctrlKey || evt.metaKey) && !(evt.altKey)) {
                // X pressed
                evt.preventDefault();
                copyComponents();
                return false;
            }*/

            return true;
        });
    }

    /*  Alternative way to handle hotkeys for copy/cut/paste (in case future browser issues cause the above keydown code to malfunction).
     * However, these cannot account for other custom keys, i.e. A and D.
    jQuery(document).bind('copy', function() {
        copyComponents();
    });

    jQuery(document).bind('cut', function() {
        copyComponents();
    });
    jQuery(document).bind('paste', function() {
        pasteComponents();
    });*/


    //getLoggedInUser();

    // end of jsPlumb.ready
});
