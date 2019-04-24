
var dialogIsDisplayed = false;

function refreshWorkflowComponentDefinitions() {
    new Ajax.Request('WorkflowEditor', {
        parameters : {
            requestingMethod: 'WorkflowEditorServlet.refreshWorkflowComponentDefinitions'
        },
        onComplete : refreshWorkflowComponentDefinitionsCompleted,
        beforeSend : wfShowStatusIndicator("Reloading..."),
        onSuccess : wfHideStatusIndicator,
        onException : function(request, exception) {
            wfHideStatusIndicator(); throw(exception);
        }
    });
}

function refreshWorkflowComponentDefinitionsCompleted(transport) {
    if (transport !== undefined && transport.responseText !== undefined) {
        json = transport.responseText.evalJSON(true);
        if (json.refreshed !== undefined && json.refreshed == "true") {
            wfTimerDialog("The workflow componenet definitions have been refreshed for all users.");
        } else if (json.error_flag !== undefined && json.message !== undefined) {
            wfTimerDialog(json.message);
        } else {
            testLoggedOut(json);
        }
    }
}

function getObjectCount(jsonObjects) {
    var uprKey = null;
    var uprCount = 0;
    if (jsonObjects !== undefined && jsonObjects != null) {
        for(uprKey in jsonObjects) {
          if(jsonObjects.hasOwnProperty(uprKey)) {
            uprCount++;
          }
        }
    }
    return uprCount;
}

function disableNonWorkflowLinks() {
    jQuery(document).click(function(event) {
        if(editorIsOpen && !jQuery(event.target).closest('.ui-dialog').length) {
            if(jQuery('.ui-dialog').is(":visible")) {
                var parentNode = event.target.parentNode;
                var linkClicked = false;
                if (parentNode !== undefined && parentNode != null
                        && ((parentNode.className !== undefined
                                && parentNode.className == 'logo')
                        || (parentNode.parentNode !== undefined && parentNode.parentNode != null
                            && parentNode.parentNode.className !== undefined
                                && parentNode.parentNode.className == 'logo'))) {
                    linkClicked = true;
                    link = 'index.jsp';

                } else if (event.target.tagName.toLowerCase() === 'a') {
                    linkClicked = true;
                    link = event.target.href;
                }

                if(isWorkflowSaved == false && linkClicked) {
                    // event.target.href; //this is the url where the anchor tag
                    // points to.
                    event.preventDefault();
                    saveAndCloseCurrentWorkflow(currentDigraph.id, link);

                } else if (linkClicked) {
                    event.preventDefault();

                    jQuery('<div />', {
                        id : 'wfLeavePage'
                    }).html(
                            'Do you really want to leave the workflow editor?').dialog({
                        create : function() {
                            jQuery('.ui-button').focus();
                        },
                        autoOpen : true,
                        autoResize : true,
                        resizable : false,
                        width : 600,
                        height : 245,
                        modal : true,
                        title : 'Workflow Editor',
                        buttons : {
                            'Yes' : function() {
                                window.location = link;
                                jQuery(this).dialog('close');
                            },
                            'Cancel' : function() {
                                jQuery(this).dialog('close');
                            }
                        },
                        close : function() {
                            jQuery(this).remove();
                        }
                    });
                }
            }
        }
    });
}

var defaultSubtab = null;
var wfResultsMessageState = "";

/*
 * This method builds the results dialog and updates the workflow components
 * with feedback information. @param populateResultsSubtabFlag whether or not to
 * display the results dialog @param transport the servlet response
 */
function processWorkflowResults(populateResultsSubtabFlag, transport) {

    jQuery('.openComponentPreviewIcon').css('opacity', '1.0');
    jQuery('.openComponentDebugInfoIcon').css('opacity', '1.0');

    if (transport !== undefined) {
        markAsStopped();
        //jQuery('#cancelWorkflowButton').button('disable');
        jQuery('#wfRunButton').button('enable');
        var json = null;
        errorsExist = false;
        fileList = null;
        defaultSubtab = null;
        wfResultsMessageState = "";
        var windowWidth = jQuery(window).width();
        var maxWidth = windowWidth * 0.99; // this will make the dialog width
                                            // 99% of the screen
        var windowHeight = jQuery(window).height();
        var maxHeight = windowHeight * 0.99; // this will make the dialog
                                                // height 99% of the screen
        var divExists = false;
        if (jQuery('#workflowResultsDialog').length > 0) {
            jQuery('#workflowResultsDialog')
                .html('<div id="workflowSubtabDivContainer"><div id="workflowSubtabDiv"></div></div>'
                    + '<div id="workflowResultsDiv">The component you selected has no saved results.</div>');
        } else {

            jQuery('<div />', {
                id : 'workflowResultsDialog'
            })
            .html('<div id="workflowSubtabDivContainer"><div id="workflowSubtabDiv"></div></div>'
                + '<div id="workflowResultsDiv"></div>')
            .appendTo('#contents')

        }


        var wfMessage = "Workflow successfully completed.";
        var success = true;

        // If opening from a form in a new tab, the json object is the transport
        // object
        if (populateResultsSubtabFlag) {
            json = transport;
            // A separate results page also does not have the original workflows
            // currentDigraph object.
            // Create a dummy object with the correct workflow id for the few
            // methods that use it.
            if (currentDigraph === undefined || currentDigraph == null) {
                currentDigraph = new Object();
                currentDigraph.id = json.workflowId;
            }
        } else {
            // Otherwise we get the json object from an AJAX response
            json = transport.responseText.evalJSON(true);

        }

        dirtyBits = {};
        var hasErrors = false;
        var hasWarnings = false;
        jQuery('.showWarnings').hide();

        if (json !== undefined && json.workflowId != null && json.workflowId != '' && json.message != null
                && (json.error_flag === undefined || json.error_flag == null || json.error_flag == "false")) {

            // Component-specific messages were found.
            if (json.genericMessageMap !== undefined && json.genericMessageMap.component_message_map !== undefined) {
                var warningComponents = {};
                jQuery(json.genericMessageMap.component_message_map).each(function (msgMapIndex, msgMapArray) {

                    for (mapComponentId in msgMapArray) {
                        var decrementLines = 2;
                        var linesToGo = 0;
                        if (msgMapArray[mapComponentId] !== undefined
                                && msgMapArray[mapComponentId].component_message_container !== undefined) {
                            jQuery(msgMapArray[mapComponentId].component_message_container).each(function(messageIndex, messageText) {

                                if (decrementLines > 0 && messageText.error !== undefined) {
                                    var humanReadableId = mapComponentId;
                                    if (jQuery('#humanReadableId_' + mapComponentId).length > 0) {
                                        humanReadableId = jQuery('#humanReadableId_' + mapComponentId).text();
                                    }

                                    if (warningComponents[mapComponentId] === undefined) {
                                        warningComponents[mapComponentId] = '';
                                    }
                                    warningComponents[mapComponentId] += '' + humanReadableId
                                        + ": " + messageText.error + '<br/>';
                                    hasWarnings = true;
                                    decrementLines -= 1;
                                } else {
                                    linesToGo += 1;
                                }
                            });
                        }
                        if (decrementLines <= 0) {
                            warningComponents[mapComponentId] += ' and ' + linesToGo + ' more...';
                        }
                    }
                });
                ///mck1b
            }


            // Component-specific Errors were found.
            if (json.errorMessageMap !== undefined && json.errorMessageMap.component_message_map !== undefined) {
                jQuery(json.errorMessageMap.component_message_map).each(function (msgMapIndex, msgMapArray) {

                    for (mapComponentId in msgMapArray) {

                        if (msgMapArray[mapComponentId] !== undefined
                                && msgMapArray[mapComponentId].component_message_container !== undefined) {
                            jQuery(msgMapArray[mapComponentId].component_message_container).each(function(messageIndex, messageText) {

                                if (messageText.error !== undefined) {
                                    hasErrors = true;
                                    var humanReadableId = mapComponentId;
                                    if (jQuery('#humanReadableId_' + mapComponentId).length > 0) {
                                        humanReadableId = jQuery('#humanReadableId_' + mapComponentId).text();
                                    }

                                    jQuery('<span class="wfErrorMessage" >'
                                            + humanReadableId + ": " + messageText.error
                                            + '</span><br/>').appendTo('#wfMessageBarText');

                                    if (jQuery('#' + mapComponentId).length > 0) {
                                        jQuery('#componentStatus_' + mapComponentId).text('Status: Error');
                                        jQuery('#componentStatus_' + mapComponentId).attr('class', 'componentStatusIcon_error wfStatusIcon');
                                    }

                                }
                            });
                        }
                    }
                });

            }

            if (jQuery('#workflowResultsDialog').length > 0 && jQuery('#workflowResultsDialog').hasClass('ui-dialog-content') === true) {
                jQuery('#workflowResultsDialog').dialog("hide");
            }

            if (json.message !== undefined
                    && (json.success === undefined || json.success == "true")) {
                jsonMessage = json.message;
                isView = json.isView;
            }
            if (json.componentDepths !== undefined) {
                jsonComponentDepths = json.componentDepths;
            }

            if (populateResultsSubtabFlag === true) {

                // jQuery('#workflowResultsDialog').dialog("open");
                // jQuery('#workflowResultsDialog').css("width", maxWidth - 35);

                if (json.componentId !== undefined && json.componentId != null) {
                    defaultSubtab = new Object();
                    defaultSubtab["componentId"] = json.componentId;
                    defaultSubtab["componentName"] = json.componentName;
                    defaultSubtab["componentType"] = json.componentType;
                    wfResultsMessageState = json.wfResultsMessageState;
                    componentOptionDependencies = json.componentOptionDependencies;
                }
            }

        } else {
            hasErrors = true;
            if (json.message !== undefined) {
                jQuery('<span class="wfErrorMessage" >'
                        + 'Workflow : ' + json.message
                        + '</span><br/>').appendTo('#wfMessageBarText');
            }
        }

        // Make sure Results button is enabled.
        jQuery('#wfResults').button({
            disabled: false
        });

    }

    if (populateResultsSubtabFlag === false && hasErrors) {
        isWorkflowExecutionPending = true;
        wfTimerDialog("<b>One or more of your components encountered an error.</b>");
    } else if (populateResultsSubtabFlag === false && !hasErrors) {
        // Add a success message to the component messages.
        if(!(jQuery('body').data("lastErrorMessage") !== undefined
            && jQuery('body').data("lastErrorMessage").component_message_map !== undefined
            && jQuery('body').data("lastErrorMessage").component_message_map.length > 0)) {
            var otherHtmlInfo = jQuery('#wfMessageBarText').html();
            jQuery('#wfMessageBarText').html('<span name="statusInfo" class="wfStatusSuccess">'
                + wfMessage
                    + '</span><br/>' + otherHtmlInfo);

        }
    }

    if (jsonComponentDepths !== undefined && jsonComponentDepths != null) {
        componentErrorsFound();
    }
    // Select default tab if we're on the results page
    if (populateResultsSubtabFlag) {
        if (defaultSubtab != null && defaultSubtab["componentId"] !== undefined) {
            jQuery('#' + "wfResultsSubtab_" + defaultSubtab["componentId"] + '').click();
        }
    } else {
        markAsSaved();
        getComponentStates();
    }

    if (jQuery('#workflowResultsDiv').length == 0 && currentDigraph != null) {
        var level = null;
        var buttonState = null;
        var wfEditLevel = jQuery('body').data("wfEditLevel");
        var wfButtonState = jQuery('body').data("buttonState");
        if (wfEditLevel !== undefined && wfEditLevel != null && wfEditLevel.level !== undefined && wfEditLevel.level != "null") {
            level = wfEditLevel.level;
        }
        if (wfEditLevel !== undefined && wfEditLevel != null && wfEditLevel.buttonState !== undefined && wfEditLevel.buttonState != "null") {
            buttonState = wfEditLevel.buttonState;
        }

        // TBD: Additional dialog texts.
        var restrictedDataDialog = 'This workflow contains both locked and shareable access data. You'
            + ' may click on the <b>Request Access</b> button to request access to the shareable'
            + ' data. You can also use the <b>Save As</b> feature to copy the workflow as a'
            + ' template for use with your own data.';
        var permRequiredDialog = 'The workflow you are trying to access uses restricted access data.'
            + ' You may click on the <b>Request Access</b> button to request access to the shareable'
            + ' data. You can also use the <b>Save As</b> feature to copy the workflow as a'
            + ' template for use with your own data.';
        var pendingDialog = 'The workflow you are trying to access uses restricted access data.'
            + ' After 24 hours, you may re-request access to the shareable'
            + ' data. You can also use the <b>Save As</b> feature to copy the workflow as a'
            + ' template for use with your own data.';

        if (level != null && level.toLowerCase() == "view" && populateResultsSubtabFlag) {
            var dialogHtml = 'This workflow is owned by another user. You cannot edit it directly,'
                + ' but you may explore its options and results. You can also use the <b>Save As</b>'
                + ' feature to copy the workflow as a template for use with your own data.';
            wfInfoDialog('workflowEditInfoDialog', dialogHtml, 'View Only');

            jQuery(".renameWorkflowIcon").hide();
            jQuery(".renameWorkflowIcon").off();

        } else if (level == null) {
            var dialogHtml = null;
            if (buttonState == 'partialView') {
                dialogHtml = 'Some data used in this workflow is private. You can view'
                    + ' the partial workflow and results or use the <b>Save As</b> feature to copy the workflow as'
                    + ' a template for use with your own data.';
            } else if (buttonState == 'unattachedFiles' || buttonState == 'nonshareable') {
                dialogHtml = 'The data used in this workflow is private. You can view the'
                    + ' workflow and use the <b>Save As</b> feature to copy the workflow as'
                    + ' a template for use with your own data.';
            } else if (buttonState != null && buttonState == 'noData') {
                dialogHtml = 'No data is contained in this workflow.';
            } else if (buttonState != null && (buttonState == 'shareable' || buttonState == 'reRequest')) {
                dialogHtml = permRequiredDialog;
            } else if (buttonState != null && buttonState == 'pending') {
                dialogHtml = pendingDialog;
            }

            jQuery(".renameWorkflowIcon").hide();
            jQuery(".renameWorkflowIcon").off();

            wfInfoDialog('workflowEditInfoDialog', dialogHtml, 'Warning');
        }
    }
}

/**
 * Sort the results by the Human Readable (user-specified) Component name. By
 * default, the human readable name is akin to Import #1, Analysis #4, etc.
 * However, it may contain any user-defined name.
 *
 * @param data
 * @returns {Array}
 */
function sortByHumanReadable(data, depths){
    var values = [];
    var maxDepth = 0;

    // The node depth in the graph is always g.t.e. zero
    for (var nDepth in depths) {
        if (depths[nDepth] !== undefined) {
            if (maxDepth < depths[nDepth]) {
                maxDepth = depths[nDepth];
            }
        }
    }
    for (var depthCt = 0; depthCt <= maxDepth; depthCt++) {
        var foundKey = false;
        for(var compIndex in data) {

            var thisNode = data[compIndex];

            if (!jQuery.isArray(thisNode)) {
                thisNode = [ thisNode ];
            }
            // thisNode can contain multiple outputs for each component
            // so we push them into values to make a unique list in order
            // of component depth
            for(var compIndex2 in thisNode) {
                if(thisNode[compIndex2] !== undefined
                        && thisNode[compIndex2].component_id_human !== undefined) {
                    var thisDepth = depths[thisNode[compIndex2].component_id];
                    if (thisDepth == depthCt) {
                        foundKey = true;
                        values.push(thisNode[compIndex2].component_id_human);
                    }
                }
            }
        }

    }



    if (!foundKey) {
        values.push("");
    }
    return values;
}

function componentErrorsFound() {

    var errorsExist = false;
    // Populate the results div and autoload the selected div,
    // or if none are selected, show the first div. The subtabs will be
    // displayed alphabetically by
    // component name, e.g. Analysis #1, Analysis #2, Import #1, Import #2, ...
    var humanReadableIdsSorted = sortByHumanReadable(jsonMessage, jsonComponentDepths);
    if (!jQuery.isArray(humanReadableIdsSorted)) {
        humanReadableIdsSorted = [ humanReadableIdsSorted ];
    }

    if (jQuery('#wfComponentMessages').length > 0) {
        jQuery('#wfComponentMessages').remove();
    }

    jQuery('<div id="wfComponentMessages"></div>').appendTo("#wfMessageBarText");
    var inputElements = jsonMessage;
    if (!jQuery.isArray(inputElements)) {
        inputElements = [ inputElements ];
    }


    // None of the components returned any data. They could not be run
    // due to invalid component options or inputs.
    if (humanReadableIdsSorted.length == 0) {
        jQuery('.wfStatusIcon').text('Status: Error');
        jQuery('.wfStatusIcon').attr('class', 'componentStatusIcon_error wfStatusIcon');

        errorsExist = true;
    }


    // Components returned some data.
    for(var i = 0; i< humanReadableIdsSorted.length; i++) {

        var humanReadableId = humanReadableIdsSorted[i];

        if (humanReadableId !== undefined) {

            for(var outContainer = 0; outContainer < inputElements.length; outContainer++) {
                for(var outIndex in inputElements[outContainer]) {
                    if(inputElements[outContainer][outIndex] !== undefined) {
                        if (!jQuery.isArray(inputElements[outContainer][outIndex])) {
                            inputElements[outContainer][outIndex] = [ inputElements[outContainer][outIndex] ]
                        }
                        for (var outSubIndex in inputElements[outContainer][outIndex]) {

                            var curElement = inputElements[outContainer][outIndex][outSubIndex];
                            if (humanReadableId == curElement.component_id_human) {

                                var componentType = inputElements[outContainer].component_type;
                                var componentName = curElement.component_name;
                                var componentIdHumanReadable = curElement.component_id_human;
                                var componentId = curElement.component_id;
                                var isRestricted = (curElement.restricted !== undefined);

                                if (defaultSubtab == null) {
                                    defaultSubtab = new Object();
                                    defaultSubtab["componentId"] = componentId;
                                    defaultSubtab["componentName"] = componentName;
                                    defaultSubtab["componentType"] = componentType;
                                }

                                var errors = curElement.errors;

                                if (componentId !== undefined) {

                                    if (errors !== undefined && errors.length > 0) {

                                        jQuery('#wfMessageBarText').html('<span name="statusInfo" class="wfStatusSuccess">'
                                            + JSON.stringify(curElement.errors)
                                                + '</span><br/>');

                                        // There are component specific errors
                                        jQuery('#componentStatus_' + componentId).text('Status: Error');
                                        jQuery('#componentStatus_' + componentId).attr('class', 'componentStatusIcon_error wfStatusIcon');

                                        jQuery('<span name="statusInfo" class="wfStatusError">Component '
                                            + componentIdHumanReadable + " detected errors:<br/>"
                                            + errors + '</span><br/>').appendTo('#wfComponentMessages');

                                        errorsExist = true;
                                        isWorkflowExecutionPending = true;

                                    } else {
                                        jQuery('#componentStatus_' + componentId).text('Status: Completed');
                                        jQuery('#componentStatus_' + componentId).attr('title', 'Status: Completed');
                                        jQuery('#componentStatus_' + componentId).attr('class', 'componentStatusIcon_on wfStatusIcon');


                                    }

                                }


                                var divId = "wfResultsSubtab_" + componentId;
                                var subtabHeading = componentIdHumanReadable;

                                if (isRestricted && isView) {
                                    // User cannot access component
                                    subtabHeading = subtabHeading + '<tt class="wfStatusError"> (RESTRICTED)</tt>';
                                } else if (isRestricted && !isView) {
                                    // User cannot access component
                                    subtabHeading = subtabHeading + '<tt class="wfStatusReady"> (READY)</tt>';
                                } else {
                                    // User can access component
                                    if (errors !== undefined && errors.length > 0 && errors == "No workflow results.") {
                                        subtabHeading = subtabHeading + '<tt class="wfStatusWarning"> (NO RESULTS)</tt>';
                                    } else if (errors !== undefined && errors.length > 0) {
                                        subtabHeading = subtabHeading + '<tt class="wfStatusError"> (FAILURE)</tt>';
                                    } else {
                                        subtabHeading = subtabHeading + ' (<tt class="wfStatusSuccess">' + componentTitleMap[componentName.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase()] + '</tt>) ';
                                    }
                                }

                                if ((jQuery('#' + divId).length <= 0)) {
                                    jQuery('<div id="' + divId + '" class="wfSubtab">'
                                            + subtabHeading + '</div>').appendTo('#workflowSubtabDiv');
                                    jQuery('#' + divId + '').click(populateResultsSubtab);
                                }

                            }
                        }

                    }
                }
            }
        }


    }

    return errorsExist;
}


/**
 * Populate the result window's selected subtab (or default subtab). If fileId
 * is given, only return the preview for the file.
 *
 */
function previewOutputData(selectedComponentId, selectedOutIndexId) {

    var inputElements = jsonMessage;
    if (!jQuery.isArray(inputElements)) {
        inputElements = [ inputElements ];
    }
    var optionsDisplayed = false;
    var elapsedTimeDisplayed = false;
    for(var outContainer = 0; outContainer < inputElements.length; outContainer++) {
        for(var outIndex in inputElements[outContainer]) {
            var nodeIndex = outIndex.replace(/output/g, '');
            if (nodeIndex == selectedOutIndexId) {
                if(inputElements[outContainer][outIndex] !== undefined) {

                    if (!jQuery.isArray(inputElements[outContainer][outIndex])) {
                        inputElements[outContainer][outIndex] = [ inputElements[outContainer][outIndex] ];
                    }

                    for (var outSubIndex in inputElements[outContainer][outIndex]) {

                        var curElement = inputElements[outContainer][outIndex][outSubIndex];

                        // alert(JSON.stringify(value));
                        var componentId = curElement.component_id;
                        var componentIdHuman = curElement.component_id_human;
                        var componentType = curElement.component_type;
                        var componentName = curElement.component_name;
                        var elapsedSecs = curElement.elapsed_seconds;
                        var errors = curElement.errors;

                        if (componentId == selectedComponentId) {
                            var parentId = 'metaInfoDiv-' + componentId + '-' + nodeIndex;
                            var parentDiv = '#' + parentId;

                            if (curElement.files !== undefined && !jQuery.isEmptyObject(curElement.files)) {

                                var keys = Object.keys(curElement.files);
                                jQuery(keys).each(function(fileIndex, fileValue) {

                                    fileList = curElement.files[fileValue];

                                    if (!jQuery.isArray(fileList)) {
                                        fileList = [ fileList ];
                                    }

                                    var fileLabels = new Object();

                                    for ( var f = 0; f < fileList.length; f++) {
                                        if (fileLabels[fileList[f].label] !== undefined) {
                                            fileLabels[fileList[f].label] += 1;
                                        } else {
                                            fileLabels[fileList[f].label] = 1;
                                        }
                                    }
                                    // alert(JSON.stringify(fileLabels));

                                    var hasFileDownloads = false;


                                    // for each file type
                                    for (var fType in fileLabels) {
                                        // display the files of that type
                                        for ( var f = 0; f < fileList.length; f++) {
                                            var fileType = "";
                                            if (fileList[f].label !== undefined && fileList[f].label != null) {
                                                fileType = fileList[f].label;
                                            }


                                            if (fType == fileType) {

                                                if (fileType == "image") {

                                                    if (jQuery('#metaInfoDiv-' + componentId).length <= 0) {
                                                        var windowWidth = jQuery(window).width();
                                                        var maxWidth = windowWidth * 0.6; // this
                                                                                            // will
                                                                                            // make
                                                                                            // the
                                                                                            // dialog
                                                                                            // width
                                                                                            // 60%
                                                                                            // of
                                                                                            // the
                                                                                            // screen
                                                        var windowHeight = jQuery(window).height();
                                                        var maxHeight = windowHeight * 0.85; // this
                                                                                                // will
                                                                                                // make
                                                                                                // the
                                                                                                // dialog
                                                                                                // height
                                                                                                // 75%
                                                                                                // of
                                                                                                // the
                                                                                                // screen
                                                        if (!previewIsDisplayed) {

                                                            jQuery('<div id="componentOutputDataPreview"/>')
                                                              .html('<div id="' + 'metaInfoDiv-' + componentId + '" class="workflowResultMetaHeader">'
                                                                + '<div id="' + parentId + '" /></div>')
                                                              .dialog({
                                                                dialogClass: "componentOutputDataPreviewDiv",
                                                                open : function() {
                                                                    jQuery('.ui-button').focus();
                                                                    setOutsideOptionPanelClick(componentId);
                                                                },
                                                                autoOpen : true,
                                                                width : maxWidth,
                                                                height : maxHeight,
                                                                top: maxHeight + 0.1 * windowHeight,
                                                                left: maxWidth * 0.2,
                                                                title : 'Preview', // capitalized
                                                                                    // in
                                                                                    // css
                                                                close : function() {
                                                                    dataPreviewCloseHandler();
                                                                    jQuery(this).remove();
                                                                }
                                                            });
                                                            isDataPreviewDialogOpen = 1;
                                                            previewIsStarted = false;
                                                            previewIsDisplayed = true;
                                                        }
                                                    }

                                                    jQuery('<br/><li>File (' + fileType + '): '
                                                            + '<a id="importFile" href="LearnSphere?downloadId='
                                                            + fileList[f].file_path + '" >'
                                                            + fileList[f].file_name + '</a>'
                                                            + '<br/>' + 'Index: ' + fileList[f].index
                                                            + '<br/>' + 'Label: ' + fileType
                                                            + '</li></div>'
                                                        ).appendTo(parentDiv);
                                                    jQuery('<br/>').appendTo(parentDiv);

                                                    jQuery('<div id="wfImageBrowserDiv">'
                                                            + '<img id="visResultImage" width="500" src="LearnSphere?visualizationId=' + fileList[f].file_path
                                                            + '" /> <br/>'
                                                            + '</div>'
                                                        ).appendTo(parentDiv);
                                                    jQuery('<br/>').appendTo(parentDiv);

                                                } else if (fileType == "inline-html") {
                                                    // Generic preview file
                                                    // method (it handles
                                                    // different types)
                                                    var isCompactPreview = true;
                                                    previewFileInline(componentId, fileList[f].file_path, nodeIndex, isCompactPreview);
                                                } else if (fileList.length > 0 && fileType != "html") {
                                                    // Generic preview file
                                                    // method (it handles
                                                    // different types)
                                                    var isCompactPreview = true;
                                                    previewFileInline(componentId, fileList[f].file_path, nodeIndex, isCompactPreview);

                                                }

                                            }

                                        }

                                    }

                                });


                            } else {
                                isDataPreviewDialogOpen = 0;
                                previewIsStarted = false;
                                previewIsDisplayed = false;
                                createWfTimerDialog('wfTimerNoPreview',
                                    'You do not have access to this component\'s data.',
                                        'No Access');
                            }
                        }
                    }
                }
            }
        }
    }
}

function dataPreviewCloseHandler() {
    previewIsDisplayed = false;
    isDataPreviewDialogOpen = 0;
}

/**
 * Populate the result window's selected subtab (or default subtab).
 */
function populateResultsSubtab() {

    jQuery('.wfSubtab').attr('class', 'wfSubtab');
    jQuery(this).attr('class', 'wfSubtab selected');
    var selectedDivId = jQuery(this).attr('id');

    jQuery('#workflowResultsDiv').html('');
    var inputElements = jsonMessage;
    if (!jQuery.isArray(inputElements)) {
        inputElements = [ inputElements ];
    }
    var optionsDisplayed = false;
    var elapsedTimeDisplayed = false;
    for(var outContainer = 0; outContainer < inputElements.length; outContainer++) {
        var outputNameArray = [];
        var nameCount = 0;
        for(var outIndex in inputElements[outContainer]) {
            outputNameArray[nameCount++] = outIndex;
        }
        outputNameArray.sort();
        for (var nameIndex = 0; nameIndex < outputNameArray.length; nameIndex++) {
            var outIndex = "output" + nameIndex;
            var nodeIndex = outIndex.replace(/output/g, '');;
            if(inputElements[outContainer][outIndex] !== undefined) {
                if (!jQuery.isArray(inputElements[outContainer][outIndex])) {
                    inputElements[outContainer][outIndex] = [ inputElements[outContainer][outIndex] ];
                }

                for (var outSubIndex in inputElements[outContainer][outIndex]) {

                    var curElement = inputElements[outContainer][outIndex][outSubIndex];

                    // alert(JSON.stringify(value));
                    var componentId = curElement.component_id;
                    var componentIdHuman = curElement.component_id_human;
                    var componentType = curElement.component_type;
                    var componentName = curElement.component_name;
                    var elapsedSecs = curElement.elapsed_seconds;
                    var errors = curElement.errors;
                    var isRestricted = (curElement.restricted !== undefined);
                    var hasComponentError = false;
                    var wfNoResults = false;
                    if (errors !== undefined && errors.length > 0) {
                        if (errors == "No workflow results.") {
                            wfNoResults = true;
                        } else {
                            hasComponentError = true;
                        }
                    }


                    if ('wfResultsSubtab_' + componentId == selectedDivId) {

                        if (wfResultsMessageState != "" ) {
                            if (wfResultsMessageState == "new") {
                                if (wfNoResults && isView) {
                                    jQuery('#workflowResultsDiv').html('<div class="results_warning" id="statusDiv-' + componentId + '">'
                                            + 'This component has not yet been run by the workflow owner.' + '</div>'
                                        );
                                } else if (wfNoResults) {
                                    jQuery('#workflowResultsDiv').html('<div class="results_warning" id="statusDiv-' + componentId + '">'
                                            + 'This component has not been executed.' + '</div>'
                                        );
                                }
                            } else if (wfResultsMessageState == "error") {
                                jQuery('#workflowResultsDiv').html('<div class="results_warning" id="statusDiv-' + componentId + '">'
                                        + 'This workflow encountered an error during its last execution.' + '</div>'
                                    );
                            } else if (wfResultsMessageState == "running" || wfResultsMessageState == "running_dirty") {
                                jQuery('#workflowResultsDiv').html('<div class="results_warning" id="statusDiv-' + componentId + '">'
                                        + 'This workflow is currently running.' + '</div>'
                                    );
                            }
                            wfResultsMessageState = "";
                        } else {
                            if (wfNoResults) {
                                if (wfNoResults && isView) {
                                    jQuery('#workflowResultsDiv').html('<div class="results_warning" id="statusDiv-' + componentId + '">'
                                            + 'This component has not yet been run by the workflow owner.' + '</div>'
                                        );
                                } else if (wfNoResults) {
                                    jQuery('#workflowResultsDiv').html('<div class="results_warning" id="statusDiv-' + componentId + '">'
                                            + 'This component has not been executed.' + '</div>'
                                        );
                                }
                            } else if (hasComponentError) {
                                jQuery('#workflowResultsDiv').html('<div class="results_warning" id="statusDiv-' + componentId + '">'
                                        + 'This workflow encountered an error during its last execution.' + '</div>'
                                    );
                            } else if (isRestricted) {
                                jQuery('#workflowResultsDiv').html('<div class="results_warning" id="statusDiv-' + componentId + '">'
                                        + 'This component\'s results are only available to some users.' + '</div>'
                                    );
                            }
                        }


                        if (!elapsedTimeDisplayed) {
                            elapsedTimeDisplayed = true;
                            if (!isRestricted || !isView) {
                                jQuery('<div id="infoDiv-' + componentId + '">'
                                    + '' + componentTitleMap[componentName.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase()]
                                    + ' ' + formatHeading(componentType)  + ' ('
                                    + formatHeading(componentIdHuman) + ') completed in ' + elapsedSecs + 's.').appendTo('#workflowResultsDiv');
                            } else {
                                jQuery('<div id="infoDiv-' + componentId + '">'
                                        + formatHeading(componentIdHuman) + ' (' + formatHeading(componentType)
                                        + ': ' + componentTitleMap[componentName.replace(/[^a-zA-Z0-99]+/gi, "_").toLowerCase()] + ')'
                                        + ' contains private or non-shareable data.').appendTo('#workflowResultsDiv');

                            }

                            jQuery('<div id="metaInfoDiv-' + componentId + '" /></div>').appendTo('#workflowResultsDiv');
                        }
                        // Add input meta data
                        if (curElement.inputmeta !== undefined && !jQuery.isEmptyObject(curElement.inputmeta)
                                && curElement.inputmeta != "") {
                            jQuery('<div class="workflowResultMetaHeader">Inputs</div>')
                                .appendTo('#metaInfoDiv-' + componentId + '');
                            // alert(JSON.stringify(curElement.inputmeta));
                            var keys = Object.keys(curElement.inputmeta);
                            jQuery(keys).each(function(inputIndex, inputKey) {
                                    jQuery(
                                            '<div>'
                                            + inputKey + ': '
                                            + curElement.inputmeta[inputKey]
                                            + '</div>'
                                        ).appendTo('#metaInfoDiv-' + componentId + '');
                            });
                        }

                        var optTypesKey = componentType.toLowerCase() + '-' + componentName.toLowerCase();
                        optTypesKey = optTypesKey.replace(/ /g, '_');
                        var optionDataTypes = null;
                        var optInfo = jQuery('body').data("wfOpt");
                        var opts = null;
                        if (optInfo != null && optInfo[optTypesKey] !== undefined) {
                            optionDataTypes = optInfo[optTypesKey];
                        }

                        // Add Options Meta Data
                        let hideTheseOptions = testOptionDependenciesResultsPage(componentId, componentName, curElement.optionmeta);
                        if (optionsDisplayed == false && curElement.optionmeta !== undefined && !jQuery.isEmptyObject(curElement.optionmeta)
                                && curElement.optionmeta != "") {
                            optionsDisplayed = true;
                            jQuery('<div class="workflowResultMetaHeader"><h2>Component Options</h2></div>')
                                .appendTo('#metaInfoDiv-' + componentId + '');
                            var optionMetaDataTable =
                                    '<table class="wfSmallTable"><tr>'
                                    + '<th>Option Name</th>'
                                    + '<th>Data Type</th>'
                                    + '<th>Value</th>'
                                    + '</tr>';
                            var keys = Object.keys(curElement.optionmeta);
                            jQuery(keys).each(function(optionIndex, optionKey) {
                                var optionType = "";
                                if (optionDataTypes != null) {
                                    jQuery(optionDataTypes).each(function(optionTypeIndex) {
                                        if (optionDataTypes[optionTypeIndex].name == optionKey) {
                                            optionType = optionDataTypes[optionTypeIndex].type;
                                        }
                                    });
                                }
                                let noDisplayString = '';
                                if (hideTheseOptions.includes(optionKey)) {
                                    noDisplayString = ' style="display:none" ';
                                }
                                    optionMetaDataTable = optionMetaDataTable + '<tr' + noDisplayString + '>'
                                        + '<td>' + optionKey + '</td>'
                                        + '<td>' + optionType + '</td>'
                                        + '<td>' + curElement.optionmeta[optionKey] + '</td>'
                                        + '</tr>';
                            });
                            optionMetaDataTable = optionMetaDataTable + '</table>';
                            jQuery(optionMetaDataTable).appendTo('#metaInfoDiv-' + componentId);
                        }

                        if (curElement.files !== undefined && !jQuery.isEmptyObject(curElement.files)
                                && curElement.files != "") {

                            var keys = Object.keys(curElement.files);
                            jQuery(keys).each(function(fileIndex, fileValue) {

                                fileList = curElement.files[fileValue];

                                if (!jQuery.isArray(fileList)) {
                                    fileList = [ fileList ];
                                }

                                var fileLabels = new Object();

                                for ( var f = 0; f < fileList.length; f++) {
                                    if (fileLabels[fileList[f].label] !== undefined) {
                                        fileLabels[fileList[f].label] += 1;
                                    } else {
                                        fileLabels[fileList[f].label] = 1;
                                    }
                                }
                                // alert(JSON.stringify(fileLabels));

                                var hasFileDownloads = false;
                                var parentId = 'metaInfoDiv-' + componentId + '-' + nodeIndex;
                                var parentDiv = '#' + parentId;


                                // for each file type
                                for (var fType in fileLabels) {

                                    jQuery('<div id="' + parentId + '" class="workflowResultMetaHeader"></div>')
                                        .appendTo('#metaInfoDiv-' + componentId + '');
                                    // display the files of that type
                                    for ( var f = 0; f < fileList.length; f++) {
                                        var fileType = "";
                                        if (fileList[f].label !== undefined && fileList[f].label != null) {
                                            fileType = fileList[f].label;
                                        }

                                        // Only used if hasFileDownloads is true
                                        // alert(fType +', ' + fileType);
                                        if (fType == fileType) {
                                            jQuery('<br/>').appendTo(parentDiv);
                                            var fileInfo =
                                                '<table class="wfSmallTable"><tr>'
                                                + '<th>Output File</th>'
                                                + '<th>Index</th>'
                                                + '<th>File Type</th>'
                                                + '</tr>';

                                            if (fileType == "image") {
                                                jQuery('<br/><li>File (' + fileType + '): '
                                                        + '<a id="importFile" href="LearnSphere?downloadId='
                                                        + fileList[f].file_path + '" >'
                                                        + fileList[f].file_name + '</a>'
                                                        + '<br/>' + 'Index: ' + fileList[f].index
                                                        + '<br/>' + 'Label: ' + fileType
                                                        + '</li></div>'
                                                    ).appendTo(parentDiv);
                                                jQuery('<br/>').appendTo(parentDiv);
                                                jQuery('<div id="wfImageBrowserDiv">'
                                                        + '<img id="visResultImage" width="500" src="LearnSphere?visualizationId=' + fileList[f].file_path
                                                        + '" /> <br/>'
                                                        + '</div>'
                                                    ).appendTo(parentDiv);
                                                jQuery('<br/>').appendTo(parentDiv);

                                            } else if (fileType == "inline-html" || fileType == "pdf") {
                                                // Generic preview file method
                                                // (it handles different types)
                                                var isCompactPreview = false;
                                                previewFileInline(componentId, fileList[f].file_path, nodeIndex, isCompactPreview);
                                                jQuery('<br/>').appendTo(parentDiv);
                                            } else if (fileList.length > 0) {


                                                fileInfo = fileInfo + '<tr>'
                                                        + '<td>' + '<a id="importFile" href="LearnSphere?downloadId='
                                                    + fileList[f].file_path + '" >'
                                                    + fileList[f].file_name + '</a>' + '</td>'
                                                    + '<td>' + fileList[f].index + '</td>'
                                                    + '<td>' + fileType + '</td>'
                                                    + '</tr>';


                                                jQuery(fileInfo).appendTo(parentDiv);

                                                hasFileDownloads = true;

                                                // Generic preview file method
                                                // (it handles different types)
                                                if (fileType != "html") {
                                                    var isCompactPreview = false;
                                                    previewFileInline(componentId, fileList[f].file_path, nodeIndex, isCompactPreview);
                                                    jQuery('<br/>').appendTo(parentDiv);
                                                }
                                            }

                                        }

                                    }

                                    if (hasFileDownloads) {
                                        jQuery('</table>').appendTo('#metaInfoDiv-' + componentId + '');
                                    }

                                }

                            });


                            if (hasComponentError && !wfNoResults) {
                                jQuery(
                                        '<div id="wfResultsFileDiv">'
                                        + '<strong class="wfStatusError">Execution haulted.<br/>' + errors + '<br/>'
                                        + '</div><br/><br/>'
                                    ).appendTo('#metaInfoDiv-' + componentId + '');
                            }
                        }
                    }
                }
            }

        }
    }
}


function formatHeading(str) {
    var newStr;
    if (str != null && str.length > 0) {
        newStr = str.replace(/^[a-z]/g, function(m) {
            return m.toUpperCase()
        }).replace(/_/g, ' ');
    } else {
        newStr = str;
    }

    return newStr;
}



function prevImage() {
    if (fileList == null || fileList === undefined) {
        return;
    }
    if (visIndex > 0) {
        visIndex--;
   } else {
       visIndex = fileList.length - 1;
   }
    jQuery('#visResultImage').attr('src', 'LearnSphere?visualizationId=' + fileList[visIndex].file_path);
}

function nextImage() {
    if (fileList == null || fileList === undefined) {
        return;
    }
    if (visIndex < fileList.length - 1) {
        visIndex++;
    } else {
        visIndex = 0;
    }
    jQuery('#visResultImage').attr('src', 'LearnSphere?visualizationId=' + fileList[visIndex].file_path);
}


function firstImage() {
    if (fileList == null || fileList === undefined) {
        return;
    }
    visIndex = 0;
    jQuery('#visResultImage').attr('src', 'LearnSphere?visualizationId=' + fileList[visIndex].file_path);
}

function lastImage() {
    if (fileList == null || fileList === undefined) {
        return;
    }
    visIndex = fileList.length - 1;

    jQuery('#visResultImage').attr('src', 'LearnSphere?visualizationId=' + fileList[visIndex].file_path);
}


/* Used to resize the iframe used for inline-html file types. */
function frameSize(width, height){
    // use viewport height here because we can't know the size of the iframe
    jQuery('.visFrame').css('width', '80vw');
    jQuery('.visFrame').css('height', '60vh');
}


function showFilePreview(transport) {
    var json = null;
    if (transport !== undefined) {
        json = transport.responseText.evalJSON(true);

        // If an error occurred during preview, show the message
        if (json.message != null && json.success !== undefined
                && json.success == "false") {
            var dialogHtml = '' + json.message + '';
             wfInfoDialog('FailedDialog', dialogHtml, 'Error');
        } else if (json.data != null && json.headerMetadata != null && json.columnMetadata != null
                && json.success !== undefined && json.success == "true") {
            openFilePreview(json, 0.85);
        } else if (json.textData != null
                && json.success !== undefined && json.success == "true") {
            openTextPreview(json, 0.85);
        } else if (fileType.equalsIgnoreCase("inline-html")) {

        } else if (json.isImage !== undefined) {
            otherPreview(json, fileName);
        } else if (json.bytes !== undefined && json.bytes != null) {
            otherPreview(json);
        } else {
            testLoggedOut(json);
        }

    }
}


function showFilePreviewInline(transport) {
    var json = null;
    if (transport !== undefined) {
        var previewDivId = null;
        json = transport.responseText.evalJSON(true);

        if (json.componentId !== undefined) {

            var componentId = json.componentId;
            var divId = 'jqxgridInline' + gridCount;
            var columnHeaderDiv = 'columntablejqxgridInline' + gridCount;

            var specificNode = '';
            if (json.nodeId !== undefined && json.nodeId != null && json.nodeId != 'null') {
                specificNode = '-' + json.nodeId;
            }

            var filePreviewDivId = '#metaInfoDiv-' + componentId + '' + specificNode;

            var isCompactPreview = false;
            if (json.isCompactPreview !== undefined) {
                isCompactPreview = json.isCompactPreview;
            }
            var widthCoeff = 0.8;   // default inline size
            var heightCoeff = 0.65;
            if (isCompactPreview == true) {
                widthCoeff = 0.58;   // see beginning of previewOutputData to
                                        // match those maxWidth/maxHeight
                heightCoeff = 0.73;
            }
            var windowWidth = jQuery(window).width();
            var maxWidth = windowWidth * widthCoeff; // this will make the
                                                        // dialog width a % of
                                                        // the screen w
            var windowHeight = jQuery(window).height();
            var maxHeight = windowHeight * heightCoeff; // this will make the
                                                        // dialog height a % of
                                                        // the screen h
            var maxDivHeight = windowHeight * 0.
            if (json.data !== undefined && json.data != null
                    && json.headerMetadata != null) {
                var parentId = 'metaInfoDiv-' + componentId + '' + specificNode;
                var parentDiv = '#' + parentId;


                if (isCompactPreview == true) {
                    isDataPreviewDialogOpen = 1;
                    jQuery('<div id="componentOutputDataPreview"/>')
                      .html('<div id="' + 'metaInfoDiv-' + componentId + '" class="workflowResultMetaHeader">'
                        + '<div id="' + parentId + '" /></div>')
                      .dialog({
                        dialogClass: "componentOutputDataPreviewDiv",
                        open : function() {
                            jQuery('.ui-button').focus();
                            setOutsideOptionPanelClick(componentId);
                        },
                        autoOpen : true,
                        width : maxWidth,
                        height : windowHeight,
                        top: maxHeight + 0.1 * windowHeight,
                        left: maxWidth * 0.2,
                        title : 'Preview', // capitalized in css
                        close : function() {
                            dataPreviewCloseHandler();
                            jQuery(this).remove();
                        }
                    });

                }


                var marginTopSheet = 0;
                if (isCompactPreview == false) {
                    marginTopSheet = 30;
                    jQuery('<div><a target="_blank" href="javascript:previewFile(' + json.fileId
                        + ')">Open in new window</a></div><br/>')
                            .appendTo(filePreviewDivId);
                }

                jQuery('<div id="' + divId + 'WidgetInline"><div id="' + divId + '"></div><div style="font-size: 12px;'
                        + 'font-family: Verdana, Geneva, \'DejaVu Sans\', sans-serif; margin-top: ' + marginTopSheet + 'px; ">'
                        + '<div id="cellbegineditevent"></div><div style="margin-top: 10px;" id="cellendeditevent">'
                        + '</div></div></div><br/><br/></div>')
                            .appendTo(filePreviewDivId);

                jQuery('#' + divId + 'WidgetInline').css('width', (maxWidth + 20) + 'px');
                jQuery('#' + divId + 'WidgetInline').css('height', (maxHeight + 20) + 'px');
                jQuery('#' + divId).css('width', (maxWidth + 20) + 'px');
                jQuery('#' + divId).css('height', (maxHeight + 20) + 'px');

                createSpreadSheetHtml(divId, columnHeaderDiv, json, widthCoeff, heightCoeff, isCompactPreview);

            } else if (json.htmlFile !== undefined && json.htmlFile != null) {
                if (isCompactPreview) {
                    if (!dialogIsDisplayed) {
                        dialogIsDisplayed = true;
                        jQuery('<div id=\'' + divId + 'Widget\'><div>Output #' + json.nodeId
                                + ' - File [' + json.fileId + ']</div><div class="htmlPreviewDiv" />'
                                + '<a target="_blank" href="LearnSphere?htmlId=' + json.htmlFile + '">Open in new window</a><br/>'
                                + '<iframe class="visFrame" src="LearnSphere?htmlId=' + json.htmlFile + '" />'
                                + '<br/></div></div>')
                            .dialog({
                            resizable : true,
                            width : maxWidth,
                            height : maxHeight,
                            title : "Html Preview",
                            close : function() {
                                jQuery(this).remove();
                                dialogIsDisplayed = false;
                            }
                        }).attr('id', 'previewResultsInlineDialog');
                    } else {
                        // preview dialog is already open for this component so append
                        jQuery('#previewResultsInlineDialog').append('<div>Output #' + json.nodeId
                                + ' - File [' + json.fileId + ']</div><div class="htmlPreviewDiv" />'
                                + '<a target="_blank" href="LearnSphere?htmlId=' + json.htmlFile + '">Open in new window</a><br/>'
                                + '<iframe class="visFrame" src="LearnSphere?htmlId=' + json.htmlFile + '" />'
                                + '<br/></div>');
                    }
                } else {
                    jQuery('<div id=\'' + divId + 'Widget\'><div>Output #'
                                + json.nodeId + '</div><div class="htmlPreviewDiv" /></div>')
                        .appendTo(filePreviewDivId);

                    jQuery('<a target="_blank" href="LearnSphere?htmlId=' + json.htmlFile + '">Open in new window</a><br/>'
                            + '<iframe class="visFrame" src="LearnSphere?htmlId=' + json.htmlFile + '" />'
                            + '<br/>')
                        .appendTo('#' + divId + 'Widget');
                }

                frameSize(maxWidth, maxHeight);

            } else if (json.bytes !== undefined && json.bytes != null) {
                if (isCompactPreview) {
                    otherPreviewInline(json);
                } else {
                    otherPreview(json);
                }
            } else if (json.textData !== undefined && json.textData != null
                    && json.textData.lines !== undefined && json.textData.lines != null) {

                var textAsString = "";
                jQuery.each(json.textData.lines, function(index) {
                   textAsString += (json.textData['lines'][index]).escapeHTML() + "<br>";
                });
                textAsString = supportTabsAndMultipleSpacesHtml(textAsString);
                if (isCompactPreview) {
                    if (!dialogIsDisplayed) {
                        dialogIsDisplayed = true;
                        jQuery('<div id=\'' + divId + 'Widget\'><div>Output #' + json.nodeId
                                + ' - File [' + json.fileId + ']</div><div class="textPreviewDiv">'
                                + textAsString
                                    + '</div></div>')
                            .dialog({
                                resizable : true,
                                width : maxWidth,
                                height : maxHeight,
                                title : "Text Preview",
                                close : function() {
                                    dialogIsDisplayed = false;
                                    jQuery(this).remove();
                                }
                            }).attr('id', 'previewResultsInlineDialog');
                    } else {
                        // preview dialog is already open for this component so append
                        jQuery('#previewResultsInlineDialog').append('<div>Output #' + json.nodeId
                                + ' - File [' + json.fileId + ']</div><div class="textPreviewDiv">'
                                + textAsString
                                + '</div>');
                    }
                } else {

                    jQuery('<div class="textPreviewDiv">'
                            + textAsString
                            + '</div>').appendTo('#metaInfoDiv-' + json.componentId + '' + specificNode);
                }
            }


            // If an error occurred during preview, show the message
            if (json.message != null && json.success !== undefined
                    && json.success == "false") {
                if (isCompactPreview == true) {
                    wfTimerDialog("No preview data found", lsWorkflowListDialogTime);
                } else {
                    var embedHtml = '' + json.message + '';
                    jQuery(filePreviewDivId).append(embedHtml);
                }
            } else {
                gridCount += 1;
            }
        } else {
            testLoggedOut(json);
        }
    }
    previewIsStarted = false;
    previewIsDisplayed = true;
}

/**
 * Processing for text in the text preview. Replaces tabs and spaces with
 * '&ensp;'
 */
function supportTabsAndMultipleSpacesHtml(str) {
    var newStr = '';

    var lines = str.split('\n');
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i];

        var c = 0;
        for (var j = 0; j < line.length; j++) {
            var char = line.charAt(j);

            if (char == '\t') {
                for (var k = 0; k < 4-c; k++) {newStr+='&ensp;';}
            } else if (char == ' ') {
                newStr += '&ensp;';
            } else {
                newStr += char;
            }
        }
        if (i != lines.length - 1) {newStr += '\n';}
    }
    return newStr;
}

function openFilePreview(json, displaySizeRatio) {

    var windowWidth = jQuery(window).width();
    var maxWidth = windowWidth * displaySizeRatio; // this will make the dialog
                                                    // width 80% of the screen
    var windowHeight = jQuery(window).height();
    var maxHeight = windowHeight * displaySizeRatio; // this will make the
                                                        // dialog height 80% of
                                                        // the screen

    var divId = 'jqxgrid' + gridCount;
    var columnHeaderDiv = 'columntablejqxgrid' + gridCount;
    gridCount += 1;

    jQuery('<div id=\'' + divId + 'Widget\'><div id="' + divId + '"></div>'
           + '<div style="font-size: 12px; font-family: Verdana, Geneva, \'DejaVu Sans\', sans-serif; margin-top: 30px;">'
           + '<div id="cellbegineditevent"></div><div style="margin-top: 10px;" id="cellendeditevent"></div></div></div>')
        .dialog({

            autoResize : true,
            resizable : true,
            modal : true,
            width : maxWidth,
            height : maxHeight,
            title : "Table View",
            buttons : {
                /*
                 * 'Save' : function() { alert("Saving changes to data (not yet
                 * implemented)"); },
                 */
                'Close' : function() {
                    jQuery(this).dialog('close');
                }
            },
            close : function() {
                jQuery(this).remove();
            }
        });

    createSpreadSheetHtml(divId, columnHeaderDiv, json, displaySizeRatio, displaySizeRatio, false);
}

function openTextPreview(json, displaySizeRatio) {

    var windowWidth = jQuery(window).width();
    var maxWidth = windowWidth * displaySizeRatio; // this will make the dialog
                                                    // width 80% of the screen
    var windowHeight = jQuery(window).height();
    var maxHeight = windowHeight * displaySizeRatio; // this will make the
                                                        // dialog height 80% of
                                                        // the screen

    if (json.textData !== undefined && json.textData != null
            && json.textData.lines !== undefined && json.textData.lines != null) {

        var textAsString = "";
        jQuery.each(json.textData.lines, function(index) {
           textAsString += (json.textData['lines'][index]).escapeHtml() + "<br>";
        });


    }

    jQuery('<div>Text Preview</div><div class="textPreviewDiv">'
            + textAsString
            + '</div>').appendTo('#metaInfoDiv-' + json.componentId + '' + specificNode);

}

function otherPreviewInline(json) {
    if (json.downloadUrl !== undefined && json.downloadUrl !== undefined) {
        jQuery('<div>' + json.downloadUrl + ' (' + formatBytes(json.bytes) + ')</div>')
            .dialog({
            width: 400,
            autoResize : true,
            resizable : true,
            modal : false,
            title : "Preview Unavailable",
            buttons : {
                'Close' : function() {
                    jQuery(this).dialog('close');
                }
            },
            close : function() {
                jQuery(this).remove();
            }
        });
    }
}

function otherPreview(json) {
    if (json.downloadUrl !== undefined && json.downloadUrl !== undefined) {

        if (json.nodeId !== undefined && json.nodeId != null && json.nodeId != 'null') {
            var specificNode = '-' + json.nodeId;

            jQuery('<div>' + json.downloadUrl + ' (' + formatBytes(json.bytes) + ')</div>')
            .appendTo('#metaInfoDiv-' + json.componentId + '' + specificNode);

        }
    }
}

function createSpreadSheetHtml(divId, columnHeaderDiv, json, widthCoeff, heightCoeff, isCompactPreview) {

    if (json.data != null) {

        var source = {
            localdata: json.data,
            datatype: "json",
            updaterow: function (rowid, rowdata, commit) {
                // synchronize with the server - send update command
                // call commit with parameter true if the synchronization with
                // the server is successful
                // and with parameter false if the synchronization failder.
                commit(true);
            },
            datafields : json.headerMetadata.datafields,
        };

        var dataAdapter = new jQuery.jqx.dataAdapter(source);
        // alert(JSON.stringify(json.data));
        // alert(JSON.stringify(json.columnMetadata.columns));
        // alert(JSON.stringify(json.headerMetadata.datafields));

        // initialize jqxGrid
        var percentWidth = 100;
        var percentHeight = 95;

        jQuery('#' + divId).jqxGrid(
        {
            width: percentWidth.toFixed(0) + "%",
            height: percentHeight.toFixed(0) + "%",
            source: dataAdapter,
            editable: false,
            sortable: true,
            theme: 'energyblue',
            columnsresize: true,
            columnsreorder: true,
            enabletooltips: true,
            selectionmode: 'multiplecellsadvanced',
            columns: json.columnMetadata.columns,
            ready: function () {

            }
        });
        patchJqxGridForAutosizing(divId, isCompactPreview);
        // events
        jQuery('#' + divId).on('cellbeginedit', function (event) {
            var args = event.args;
            jQuery("#cellbegineditevent").text("Event Type: cellbeginedit, Column: " + args.datafield + ", Row: "
                + (1 + args.rowindex) + ", Value: " + args.value);
        });

        jQuery('#' + divId).on('cellendedit', function (event) {
            var args = event.args;
            jQuery("#cellendeditevent").text("Event Type: cellendedit, Column: " + args.datafield + ", Row: "
                + (1 + args.rowindex) + ", Value: " + args.value);
        });
    }
}

function patchJqxGridForAutosizing(divId, isCompactPreview) {

    jQuery('#' + divId).jqxGrid('autoresizecolumns');
    // jQuery('#' + divId).css('border', 'none');
    jQuery('#' + divId).jqxGrid({ width: '98%' });
    jQuery('#' + divId).jqxGrid({ height: '90%' });

    var rows = jQuery('#' + divId).jqxGrid('getboundrows');
    var headerRowHeight = jQuery('#' + divId).jqxGrid('columnsheight');
    if (rows !== undefined && rows.length > 0) {
        jQuery('#' + divId).jqxGrid({ height: '95%' });

    }

    if (jQuery('#componentOutputDataPreview').length > 0) {
        // a lot of work to persuade jqx and jquery to work together on this one
        jQuery('#componentOutputDataPreview').css('height', '80%');

        // Dialog width/height
        jQuery('.componentOutputDataPreviewDiv').css('width', '74%');
        jQuery('.componentOutputDataPreviewDiv').css('height', '70%');
        jQuery('.componentOutputDataPreviewDiv').css('top', '80px');
    }

    var childDiv = divId + 'WidgetInline';
    jQuery('#' + childDiv).css('width', '98%');
    jQuery('#' + childDiv).css('height', '70%');
}

var tooltipsInitialized = {};
function initializeTooltips(domId) {

    var options = new Array();

    options['extraClasses'] = 'wfTooltipDiv';
    options['timeout'] = '100000';
    options['delay'] = '200';
    // options['fixed'] = 'true';

    // get list of component tooltips
    if (jQuery(domId).length > 0) {

        jQuery(domId).each(function() {
                var tooltipId = jQuery(this).attr('id');
                if (tooltipsInitialized[tooltipId] !== undefined
                        || tooltipsInitialized[tooltipId] != true) {
                    tooltipsInitialized[tooltipId] = true;
                    var idRoot = tooltipId.substring(0, tooltipId.indexOf("_tooltip_content"));
                    var componentId = idRoot + "-draggable";
                    var content = jQuery(this).html();

                    if (jQuery('#' + componentId).length > 0) {
                        /*if (content != null && content == "") {
                            content = '<span id="' + idRoot + '_tooltip_content" class="wfComponentTooltip">'
                                + '<div class="infoBlock"><h2>Author</h2></div><div class="infoBlock">'
                                + '<h2>Abstract</h2></div><div class="infoBlock"><h2>Details</h2>'
                                + ''
                                + '</div><div class="infoBlock"><h2>Inputs</h2>'
                                + '<b></b>'
                                + '</div><div class="infoBlock"><h2>Outputs</h2>'
                                + '<b></b>'
                                + '</div></span>';

                        }*/

                        if (content != null && content != "") {
                            new ToolTip(componentId, content, options);
                        }
                    }
                }
            });
    }
}

// ---------------------------------------
// Collapse/Expand Recommended Workflows
// ---------------------------------------

function collapseRecWorkflows() {
    jQuery('#rwCollapseImage').hide();
    jQuery('#rwExpandImage').show();
    jQuery('#recommended-workflows-page-table').hide();
}
function expandRecWorkflows() {
    jQuery('#rwCollapseImage').show();
    jQuery('#rwExpandImage').hide();
    jQuery('#recommended-workflows-page-table').show();
}
