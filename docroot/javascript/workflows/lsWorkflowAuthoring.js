jQuery.noConflict();
var currentMiniIcon = null;
var previousMiniIcon = null;
var dirtyOptionPane = false;
var workflowRunDelayStarted = false;
var prevDatasetFiles = [];
var prevDatasets = [];
var processComponentStatusInfoLocked = false;
var maxFileNameLength = 55;
var dirtySelectDialog = false;
var backupMultiSelect = {};
// 15 characters from the end, so 40
var secondaryTruncationStartIndex = maxFileNameLength - 15;

var addToDatasetButton = null;

function monitorWorkflowStatus() {
    clearInterval(globalWorkflowTimer);
    globalWorkflowTimer = setInterval ( getWorkflowStates, globalWorkflowTimerRefresh );
}

function monitorComponentStatus() {
    //alert('component_stop_mon');
    clearInterval(globalWorkflowTimer);
    getComponentStates();
    globalWorkflowTimer = setInterval ( getComponentStates, globalWorkflowTimerRefresh );
}


function markAsUnsaved() {
    //alert('mark_unsaved');
    clearInterval(globalWorkflowTimer);
    var titleString = "Unsaved";
    previousMiniIcon = titleString;
    jQuery('#componentStatusIcon').remove();
    jQuery('<div/>', {
        id : 'componentStatusIcon'
    }).appendTo('#wfStatusInfoDiv').html(
            '<span class="wfInfoLabel">Workflow Status: </span>' +
            '<span class="wfStatusLabel">' + titleString + '<img id="miniWaitingIcon" src="images/alert.gif" />'
                + '</span>');
}


function markAsSaved() {

    var titleString = "Saved";
    previousMiniIcon = titleString;
    jQuery('#componentStatusIcon').remove();
    jQuery('<div/>', {
        id : 'componentStatusIcon'
    }).appendTo('#wfStatusInfoDiv').html(
            '<span class="wfInfoLabel">Workflow Status: </span>' +
            '<span class="wfStatusLabel">' + titleString + '<img id="miniWaitingIcon" src="images/disk.png" />'
                + '</span>');
    jQuery('#miniWaitingIcon').css('opacity', 0.4);
}

function markAsExecuting() {

    var titleString = 'Executing';

    currentMiniIcon = titleString;
    jQuery('#componentStatusIcon').remove();
    jQuery('<div/>', {
        id : 'componentStatusIcon'
    }).appendTo('#wfStatusInfoDiv').html(
            '<span class="wfInfoLabel">Workflow Status: </span>' +
            '<span class="wfStatusLabel">' + titleString + '<img id="miniWaitingIcon" src="images/waiting.gif" />'
                + '</span>');

    workflowIsRunning = true;
    startedRunningTime = new Date();
    workflowRunDelayStarted = true;
    setInterval( function() { workflowRunDelayStarted = false; } , 5000 ) ;

    wfShowStatusIndicatorModal(false, "Executing...");
}

function markAsStopped() {

//    jQuery('input[name=isSharedRadioButton]').button('enable');
    workflowIsRunning = false;

}

/**********************************************************************************
 *  Workflow Create, Save, Edit, Delete functions.
 *
 **********************************************************************************/

function saveOpenOptions() {
    if (dirtyOptionPane && !workflowIsRunning) {
        var optDialogContainerId = jQuery('.advOptDialogClass').attr("id");
        if (optDialogContainerId !== undefined) {
            // Save options to server when options box is closed and validate
            var splitString = optDialogContainerId.split('_');
            var componentId = splitString[1];
            isWorkflowSaved = false;
            updateAdvancedOptions(componentId);
        }
    }
}

function saveOpenOptionsAndValidate() {
    if (dirtyOptionPane && !workflowIsRunning) {
        var optDialogContainerId = jQuery('.advOptDialogClass').attr("id");
        if (optDialogContainerId !== undefined) {
            // Save options to server when options box is closed and validate
            var splitString = optDialogContainerId.split('_');
            var componentId = splitString[1];
            isWorkflowSaved = false;
            dirtyOptionPane = false;
            updateAdvancedOptionsAndValidate(componentId);

        }
        return true;
    }
    return false;
}

function saveOpenOptionsAndValidateBeforeNewDialog(componentIdNew, componentType, componentName) {
    if (dirtyOptionPane && !workflowIsRunning) {

        var optDialogContainerId = jQuery('.advOptDialogClass').attr("id");
        if (optDialogContainerId !== undefined) {
            // Save options to server when options box is closed and validate
            var splitString = optDialogContainerId.split('_');
            var componentId = splitString[1];
            isWorkflowSaved = false;
            dirtyOptionPane = false;
            updateAdvancedOptionsAndValidateBeforeNewDialog(componentId, componentIdNew, componentType, componentName);

        } else {
            requestComponentSpecificOptions(componentIdNew, componentType, componentName);
        }
    } else {
        requestComponentSpecificOptions(componentIdNew, componentType, componentName);
    }
}

function saveAndCloseCurrentWorkflow(workflowId, newLink) {
    if (currentDigraph.id != null) {
        if (workflowIsRunning) {
            showCancelWarningAndExit();
        } else {

            if (isWorkflowSaved === false && currentDigraph.isView === false) {
                jQuery('<div />', {
                    id : 'wfSaveConfirmationDialog'
                }).html(
                        'You have unsaved changes in your workflow. Do you wish to save them before closing?').dialog({
                    open : function() {
                        jQuery('.ui-button').focus();
                    },
                    autoOpen : true,
                    autoResize : true,
                    resizable : false,
                    width : 420,
                    height : 'auto',
                    modal : true,
                    title : 'Save and Close Workflow',
                    buttons : {
                        'Yes' : function() {
                            jQuery(this).dialog('close');
                            saveAndCloseCurrentWorkflowContinue(currentDigraph.id, newLink);
                        },
                        'No' : function() {
                            if (newLink == null) {
                                forwardToWorkflowList();
                            } else {
                                window.location = newLink;
                            }
                        }
                    },
                    close : function() {

                    }
                });
            } else {
                forwardToWorkflowList();
            }
        }
    }
}

//Saves the current workflow to the database.
function saveAndCloseCurrentWorkflowContinue(workflowId, newLink) {
  clearInterval(globalWorkflowTimer);
  clearInterval(statusIndicatorTimer);
  saveOpenOptions();

  var titleString = "Processing";
  previousMiniIcon = titleString;
  jQuery('#componentStatusIcon').remove();
  jQuery('<div/>', {
     id : 'componentStatusIcon'
  }).appendTo('#wfStatusInfoDiv').html(
         '<span class="wfInfoLabel">Workflow Status: </span>' +
         '<span class="wfStatusLabel">' + titleString + '<img id="miniWaitingIcon" src="images/waiting.gif" />'
             + '</span>');

  lastCall = 'saveButtonPress';

  var dsId = null;
  // The global variable currentDigraph exists and is defined.
  if (currentDigraph !== undefined) {
     // Just being paranoid, check that the workflowId is that of the digraph being saved
     if (currentDigraph.id == workflowId) {
         updateDigraphBeforeSaving(workflowId);
         if (newLink == null) {
         // Send an AJAX request to save the currentDigraph to the database.
             new Ajax.Request('WorkflowEditor', {
                 parameters : {
                     requestingMethod : 'WorkflowEditorServlet.saveCurrentWorkflow',
                     workflowId : currentDigraph.id,
                     datasetId : dsId,
                     persist : 'true',
                     digraphObject : stringifyDigraph(currentDigraph),
                     dirtyBits: JSON.stringify(dirtyBits)
                 },
                 onComplete : forwardToWorkflowList,
                 beforeSend : wfShowStatusIndicatorModal(true, "Processing..."),
                 onSuccess : wfHideStatusIndicator,
                 onException : function(request, exception) {
                     wfHideStatusIndicator(); throw(exception);
                 }
             });
         } else {
             // Send an AJAX request to save the currentDigraph to the database.
             new Ajax.Request('WorkflowEditor', {
                 parameters : {
                     requestingMethod : 'WorkflowEditorServlet.saveCurrentWorkflow',
                     workflowId : currentDigraph.id,
                     datasetId : dsId,
                     persist : 'true',
                     digraphObject : stringifyDigraph(currentDigraph),
                     dirtyBits: JSON.stringify(dirtyBits)
                 },
                 onComplete : function() { window.location = newLink; },
                 beforeSend : wfShowStatusIndicatorModal(true, "Processing..."),
                 onSuccess : wfHideStatusIndicator,
                 onException : function(request, exception) {
                     wfHideStatusIndicator(); throw(exception);
                 }
             });
         }
     }
  } else {
     alert('Workflow cannot be empty.');
  }
}

function saveAndRunCurrentWorkflow(workflowId) {

    if (workflowIsRunning) {
        showCancelWarning();
    } else {
         lastCall = "saveButtonPress";
         saveOpenOptions();

         var dsId = null;
         // The global variable currentDigraph exists and is defined.
         if (currentDigraph !== undefined) {
             // Just being paranoid, check that the workflowId is that of the digraph being saved
             if (currentDigraph.id == workflowId) {
                 updateDigraphBeforeSaving(workflowId);

                 // Send an AJAX request to save the currentDigraph to the database.
                 new Ajax.Request('WorkflowEditor', {
                     parameters : {
                         requestingMethod : 'WorkflowEditorServlet.saveCurrentWorkflow',
                         workflowId : currentDigraph.id,
                         datasetId : dsId,
                         digraphObject : stringifyDigraph(currentDigraph),
                         dirtyBits: JSON.stringify(dirtyBits),
                         persist : 'true',
                     },
                     onComplete : saveAndRunCurrentWorkflowCompleted,
                     beforeSend : wfShowStatusIndicatorModal(true, "Saving..."),
                     onSuccess : wfHideStatusIndicator,
                     onException : function(request, exception) {
                         wfHideStatusIndicator(); throw(exception);
                     }
                 });
             }
         } else {
             alert('Workflow name cannot be empty.');
         }
    }
}

//After saving the currentDigraph to the database, update the dialog's
//"Last updated" and disable the save button. Enable the close button.
function saveAndRunCurrentWorkflowCompleted(transport) {
    var workflowAsJson = null;
    var json = null;
    var dsId = null;
    if (transport !== undefined) {

        dirtyBits = {};
       json = transport.responseText.evalJSON(true);
       if (json.workflow !== undefined && json.workflow != null) {
           workflowAsJson = json.workflow;
           // disable saved button; enable it later if the digraph is changed
           var lastUpdated = json.workflow.lastUpdated;
           jQuery('#lastUpdatedLabel').text(lastUpdated);

           wfDisableSaveButton();
           isWorkflowSaved = true;
           monitorComponentStatus();

           replaceExecutionButton('#wfExecutionDiv', 'Cancel', "cancelWorkflowButton", "wfRunButton", "ui-icon-wf-cancel-button");

           new Ajax.Request('WorkflowEditor', {
               parameters : {
                   requestingMethod : 'WorkflowEditorServlet.runWorkflow',
                   workflowId : currentDigraph.id,
                   datasetId : dsId
               },
               onComplete : processRunResults,
               beforeSend : markAsExecuting(),
               onSuccess : wfHideStatusIndicator,
               onException : function(request, exception) {
                   markAsStopped();
                   wfHideStatusIndicator(); throw(exception);
               }
           });
       } else if (json.state !== undefined && json.state != null && json.state == 'running') {
           showCancelWarning();
       } else {
           testLoggedOut(json);
       }
    }
}

function cancelWorkflowInEditor(workflowId) {
    var dsId = null;
    // The global variable currentDigraph exists and is defined.
    if (currentDigraph !== undefined) {
        // Just being paranoid, check that the workflowId is that of the digraph being saved
        if (currentDigraph.id == workflowId) {

            wfHideStatusIndicator() ;
            wfShowStatusIndicatorModal(false, "Canceling...");
            // Send an AJAX request to save the currentDigraph to the database.
            new Ajax.Request('WorkflowEditor', {
                parameters : {
                    requestingMethod : 'WorkflowEditorServlet.cancelAndSaveCurrentWorkflow',
                    workflowId : currentDigraph.id,
                    datasetId : dsId,
 // "persist : false" means do NOT save the workflow once we get into cancelAndSaveCurrentWorkflow-- this is correct; will rename method in refactor later
                    persist : 'false',
                    digraphObject : stringifyDigraph(currentDigraph),
                    dirtyBits: JSON.stringify(dirtyBits)
                },
                onException : function(request, exception) {
                    wfHideStatusIndicator(); throw(exception);
                }
            });
        }
    } else {
        alert('Workflow cannot be empty.');
    }
}

/** Cancel the workflow. */
function cancelWorkflow(workflowId) {
    jQuery('<div />', {
        id : 'wfSaveConfirmationDialog'
    }).html(
            'Are you sure you wish to cancel the running workflow?').dialog({
        open : function() {
            jQuery('.ui-button').focus();
        },
        autoOpen : true,
        autoResize : true,
        resizable : false,
        width : 420,
        height : 'auto',
        modal : true,
        title : 'Cancel running workflow',
        buttons : {
            'Yes' : function() {
                jQuery(this).dialog('close');

                // Send an AJAX request to cancel the workflow
                new Ajax.Request('LearnSphere', {
                    parameters : {
                        requestingMethod : 'ManageWorkflowsServlet.cancelWorkflow',
                        workflowId : workflowId
                    },
                    onComplete : cancelWorkflowCompleted,
                    beforeSend : wfShowStatusIndicatorModal(true, "Canceling..."),
                    onSuccess : wfHideStatusIndicator,
                    onException : function(request, exception) {
                        wfHideStatusIndicator(); throw(exception);
                    }
                });
            },
            'No' : function() {
                jQuery(this).dialog('close');
            }
        },
        close : function() {

        }
    });

}


/** The request to cancel the workflow has been executed. Follow up with the response. */
function cancelWorkflowCompleted(transport) {
  var workflowAsJson = null;
  var json = null;
  if (transport !== undefined && transport != null && transport.responseText !== undefined) {
      dirtyBits = {};
      json = transport.responseText.evalJSON(true);
      if (json.success != null && json.success == true) {
          var dialogHtml = "Canceling workflow, '" + json.workflowName
              + "' (" + json.workflowId + "). This can take up to a minute.<br/>"
              + "<center><strong><u>"
              + "<a href=\"javascript:wfEditWorkflow(" + json.workflowId + ")\">View workflow: " + json.workflowName + " (" + json.workflowId + ")</a>"
              + "</u></strong></center>"
          wfInfoDialog('cancelDialog', dialogHtml, 'Workflow Canceled');
          jQuery('#wf-cancel_' + json.workflowId).remove();
      } else if (json.success == null || json.success == false) {
          var dialogHtml = "An error has occurred while trying to cancel the workflow, '" + json.workflowName
              + "' (" + json.workflowId + "). Please refresh this page and try again."
           wfInfoDialog('cancelDialog', dialogHtml, 'Workflow Cancel Failed');
      } else {
          testLoggedOut(json);
      }
  }
}

function showCancelWarning() {
    wfTimerDialog("The workflow cannot be modified until you cancel the running workflow.",
            lsWorkflowListDialogTime);
}

function showCancelWarningAndExit() {
    jQuery('<div />', {
        id : 'wfSaveConfirmationDialog'
    }).html(
            'The workflow will continue running even after you close the editor.').dialog({
        open : function() {
            jQuery('.ui-button').focus();
        },
        autoOpen : true,
        autoResize : true,
        resizable : false,
        width : 600,
        height : 245,
        modal : true,
        title : 'Workflow is Running',
        buttons : {
            'Okay' : function() {
                forwardToWorkflowList();
                jQuery(this).dialog('close');
            }
        },
        close : function() {
            jQuery(this).remove();
        }
    });
}

function preloadWorkflow(workflowId, isSaveAsNew) {
    clearInterval(globalWorkflowTimer);
    clearInterval(statusIndicatorTimer);
    wfShowStatusIndicatorModal(false, "Processing...");
    var dsId = null;
    // The global variable currentDigraph exists and is defined.
    if (currentDigraph !== undefined) {
        // Just being paranoid, check that the workflowId is that of the digraph being saved
        if (currentDigraph.id == workflowId) {


            // Send an AJAX request to save the currentDigraph to the database.
            new Ajax.Request('WorkflowEditor', {
                parameters : {
                    requestingMethod : 'WorkflowEditorServlet.preloadWorkflow',
                    workflowId : currentDigraph.id,
                    datasetId : dsId,
                    persist : 'false',
                    digraphObject : stringifyDigraph(currentDigraph),
                    dirtyBits: JSON.stringify(dirtyBits)
                },
                onComplete : function(transport) {
                    saveCurrentWorkflowCompleted(transport, null, null, null, null, true, isSaveAsNew);
                },
                beforeSend : wfShowStatusIndicatorModal(false, "Loading..."),
                onSuccess : wfHideStatusIndicator,
                onException : function(request, exception) {
                    wfHideStatusIndicator(); throw(exception);
                }
            });
        }
    } else {
        alert('Workflow cannot be empty.');
    }
}

/**
 * Saves the current workflow to the temporary database.
 * If given a value for componentIdNew, the method will open the options panel for componentIdNew
 * after the save has completed, as this is a transient function. Optionally, if message is passed,
 * then a 2-3 second dialog window appears after the save has completed with the full message.
 * @param workflowId the workflow id
 * @param message any message window to open
 * @param componentIdNew if this is given, it will attempt to open the options panel for
 * the component after the save completes
 * @param componentType the component type, e.g. Analysis
 * @param componentName the component name, e.g. AFM
 * @returns
 */
function saveTemporaryWorkflowTransient(workflowId, message, componentIdNew, componentType, componentName) {

    clearInterval(globalWorkflowTimer);
    clearInterval(statusIndicatorTimer);

    dirtyOptionPane = false;


    var i = isWorkflowExecutionPending;
    var dsId = null;
    // The global variable currentDigraph exists and is defined.
    if (currentDigraph !== undefined) {
        // Just being paranoid, check that the workflowId is that of the digraph being saved
        if (currentDigraph.id == workflowId) {
            updateDigraphBeforeSaving(workflowId);
            wfShowStatusIndicatorModal(false, "Processing...");
            // Send an AJAX request to save the currentDigraph to the database.
            new Ajax.Request('WorkflowEditor', {
                parameters : {
                    requestingMethod : 'WorkflowEditorServlet.saveCurrentWorkflow',
                    workflowId : currentDigraph.id,
                    datasetId : dsId,
                    persist : false,
                    digraphObject : stringifyDigraph(currentDigraph),
                    dirtyBits: JSON.stringify(dirtyBits)
                },
                onComplete : function(transport) {
                    saveCurrentWorkflowCompleted(transport, message, componentIdNew, componentType, componentName, true, false);
                },
                onSuccess : function() {
                    //wfHideStatusIndicator();
                },
                onException : function(request, exception) {
                    wfHideStatusIndicator(); throw(exception);
                }
            });
        }
    } else {
        alert('Workflow cannot be empty.');
    }

}

// Saves the current workflow to the database.
function saveCurrentWorkflow(workflowId, message, componentId, componentType, componentName) {
    clearInterval(globalWorkflowTimer);
    clearInterval(statusIndicatorTimer);
    lastCall = "saveButtonPress";
    saveOpenOptions();
    wfShowStatusIndicatorModal(false, "Processing...");
    var titleString = "Processing";
    previousMiniIcon = titleString;
    jQuery('#componentStatusIcon').remove();
    jQuery('<div/>', {
        id : 'componentStatusIcon'
    }).appendTo('#wfStatusInfoDiv').html(
            '<span class="wfInfoLabel">Workflow Status: </span>' +
            '<span class="wfStatusLabel">' + titleString + '<img id="miniWaitingIcon" src="images/waiting.gif" />'
                + '</span>');

    var dsId = null;
    // The global variable currentDigraph exists and is defined.
    if (currentDigraph !== undefined) {
        // Just being paranoid, check that the workflowId is that of the digraph being saved
        if (currentDigraph.id == workflowId) {
            updateDigraphBeforeSaving(workflowId);

            // Send an AJAX request to save the currentDigraph to the database.
            new Ajax.Request('WorkflowEditor', {
                parameters : {
                    requestingMethod : 'WorkflowEditorServlet.saveCurrentWorkflow',
                    workflowId : currentDigraph.id,
                    datasetId : dsId,
                    persist : 'true',
                    digraphObject : stringifyDigraph(currentDigraph),
                    dirtyBits: JSON.stringify(dirtyBits)
                },
                onComplete : function(transport) {
                    saveCurrentWorkflowCompleted(transport, message, componentId, componentType, componentName, false, false);
                },
                beforeSend : wfShowStatusIndicatorModal(false, "Processing..."),
                onSuccess : wfHideStatusIndicator,
                onException : function(request, exception) {
                    wfHideStatusIndicator(); throw(exception);
                }
            });
        }
    } else {
        alert('Workflow cannot be empty.');
    }
}



// After saving the currentDigraph to the database, update the dialog's
// "Last updated" and disable the save button. Enable the close button.
function saveCurrentWorkflowCompleted(transport, message, componentIdNew, componentType, componentName, validate, isSaveAsNew) {
    var workflowAsJson = null;
    var json = null;
    if (transport !== undefined && transport != null && transport.responseText !== undefined) {
        dirtyBits = {};
        json = transport.responseText.evalJSON(true);
        if (json.workflow !== undefined && json.workflow != null) {
            workflowAsJson = json.workflow;
           // alert('a' + JSON.stringify(currentDigraph)) ;
            workflowId = json.workflow.id;
            workflowName = json.workflow.name;
            lastUpdated = json.workflow.lastUpdated;
            isShared = json.isShared;
            components = json.workflow.components;

            if (json.workflow.components !== undefined && json.workflow.components != null
                    && (lastCall == "uploadOptionsFileCompleted" || lastCall == "batch" || lastCall == "uploadFileCompleted")) {
                jQuery.each(json.workflow.components, function() {
                jQuery.each(this, function(c2Index, c2Item) {
                    //alert('json1: ' + JSON.stringify(c2Item));
                    jQuery.each(currentDigraph.components, function(c1Index, c1Item) {
                        if (c1Item !== undefined && c1Item.component_id == c2Item.component_id) {
                            var thisComponent = c1Item;

                            thisComponent = c2Item;

                            if (thisComponent.connections === undefined) {
                                thisComponent.connections = []
                            } else if (!jQuery.isArray(thisComponent.connections)) {
                                thisComponent.connections = [ thisComponent.connections ];
                            }

                            if (thisComponent.options === undefined) {
                                thisComponent.options = {};
                            } else if (!jQuery.isArray(thisComponent.options)) {
                                thisComponent.options = [ thisComponent.options ];
                            }




                            if (thisComponent.inputs === undefined) {
                                thisComponent.inputs = []
                            } else if (!jQuery.isArray(thisComponent.inputs)) {
                                thisComponent.inputs = [ thisComponent.inputs ];
                            }
                            if (thisComponent.outputs === undefined) {
                                thisComponent.outputs = []
                            } else if (!jQuery.isArray(thisComponent.outputs)) {
                                thisComponent.outputs = [ currentDigraph.components.outputs ];
                            }
                            if (thisComponent.errors === undefined) {
                                thisComponent.errors = []
                            } else if (!jQuery.isArray(thisComponent.errors)) {
                                thisComponent.errors = [ thisComponent.errors ];
                            }

                            delete currentDigraph.components[c1Index];

                            //alert('this push(' + thisComponent.component_id +'): ' + JSON.stringify(thisComponent));
                            currentDigraph.components.push(thisComponent);

                        }
                    });
                });
                });

            }



         //   alert('new: ' + JSON.stringify(currentDigraph)) ;


            // disable saved button; enable it later if the digraph is changed
            var lastUpdated = json.workflow.lastUpdated;
            jQuery('#lastUpdatedLabel').text(lastUpdated);

            if (lastCall !== undefined
                    && (lastCall == 'preload' || lastCall == "saveButtonPress")) {
                wfDisableSaveButton();
                isWorkflowSaved = true;
            } else {
                wfEnableSaveButton();
            }

            if (validate && lastCall != 'preload') {
                getValidationAndOptionData(currentDigraph.id, message, componentIdNew, componentType, componentName, isSaveAsNew);
            } else {
                monitorComponentStatus();
            }

            // If this call to save was because the user clicked "Add to dataset?" re-click the button now that it's saved
            if (addToDatasetButton != null) {
                addToDatasetButton.click();
                addToDatasetButton = undefined;
            }


        } else if (json.state !== undefined && json.state != null && json.state == 'running') {
            jQuery('<div />', {
                id : 'wfSaveConfirmationDialog'
            }).html(
                    json.message).dialog({
                        open : function() {
                    jQuery('.ui-button').focus();
                },
                autoOpen : true,
                autoResize : true,
                resizable : false,
                width : 600,
                height : 245,
                modal : true,
                title : 'Workflow is Running',
                buttons : {
                    'Okay' : function() {
                        jQuery(this).dialog('close');
                    }
                },
                close : function() {
                    jQuery(this).remove();
                }
            });
        } else {
            testLoggedOut(json);
        }
    }
}

/** Calls the processWorkflowResults function in WorkflowResults.js
 * but does not open the results viewer. */
function processRunResults(transport) {
    if (transport !== undefined && transport != null && transport.responseText !== undefined) {

        var json = transport.responseText.evalJSON(true);

        if (json.message !== undefined) {
            // Do not auto-open results window. Simply update the components and message bar.
            initComponentStates(transport);

        } else {
            testLoggedOut(json);
        }

        replaceExecutionButton('#wfExecutionDiv', 'Run', "wfRunButton",
                "cancelWorkflowButton", "ui-icon-wf-run-button");
    }
}

/** Calls the processWorkflowResults function in WorkflowResults.js,
 * and opens the results viewer. */
function openResults(transport) {
    if (transport !== undefined) {
        var json = transport.responseText.evalJSON(true);
        if (json.success == "true") {
            var showResults = true;
            processWorkflowResults(showResults, transport);
        }
    }
}

/**
 * Tests the workflow for errors and returns feedback to the user via the message pane.
 * It does everything the runWorkflow method does
 * except for running the components' programs (or run() methods if not using programs).
 * @param workflowId the workflowId
 */
function getValidationAndOptionData(workflowId, message, componentIdNew, componentType, componentName, isSaveAsNew) {

    if (isMessageBarInitialized) {
        jQuery('#wfMessageBarText').html('');
    }
    checkWorkflowConnections();

    var dsId = null;
    // The global variable currentDigraph exists and is defined.
    if (currentDigraph !== undefined) {

        // Just being paranoid, check that the workflowId is that of the digraph being saved
        if (currentDigraph.id == workflowId) {

            //updateDigraphBeforeSaving(workflowId);

            // Send an AJAX request to save the currentDigraph to the database.
            new Ajax.Request('WorkflowEditor', {
                parameters : {
                    requestingMethod : 'WorkflowEditorServlet.validateWorkflow',
                    workflowId : currentDigraph.id,
                    digraphObject : stringifyDigraph(currentDigraph),
                    dirtyBits: JSON.stringify(dirtyBits),
                    datasetId : dsId,
                    isWorkflowSaved : isWorkflowSaved,
                    isSaveAsNew : isSaveAsNew
                },
                onComplete : function(transport) { getValidationAndOptionDataCompleted(transport, message, componentIdNew, componentType, componentName); },
                beforeSend : function() { wfShowStatusIndicatorModal(false, "Validating..."); },
                onSuccess : function() {

                    wfHideStatusIndicator();

                    if (isWorkflowSaved) {
                        markAsSaved();
                    } else {
                        markAsUnsaved();
                    }

                },
                onException : function(request, exception) {
                    wfHideStatusIndicator(); throw(exception);
                }
            });



        }
    } else {
        alert('Workflow name cannot be empty.');
    }
}

/**
 * After validating the workflow, update the workflow status and enable the save button.
 * Also, update the component status icons to 'ready'.
 */
function getValidationAndOptionDataCompleted(transport, message, componentIdNew, componentType, componentName) {
 var json = null;
 processComponentStatusInfoLocked = false;
 if (isMessageBarInitialized) {
     jQuery('#wfMessageBarText').html('');
 }
 if (transport !== undefined) {
     json = transport.responseText.evalJSON(true);

     if (currentDigraph != null && json.workflowId !== undefined && json.workflowId != null
             && json.workflowId == currentDigraph.id) {

         jQuery('.componentStatusIcon_error').each(function (compStatus) {
             // Workflow is now valid, remove error messages.
             jQuery(this).text('Status: Ready');
             jQuery(this).attr('class', 'componentStatusIcon_ready wfStatusIcon');
             jQuery('#' + jQuery(this).parent().attr('id') + ' .openComponentDebugInfoIcon').hide();
             isWorkflowExecutionPending = true;
         });

         // disable saved button; enable it later if the digraph is changed
         var isValid = json.isValid;
         if (isValid == "true" && jQuery('#wfMessageBarText').text().trim().length == 0) {
             jQuery('<span id="statusInfo" >Workflow configuration valid.</span>').appendTo('#wfMessageBarText');
         } else {
             handleErrorMap(json.errorMessageMap)
         }
         // initialize the flag that handles overwriting the message bar
         isMessageBarInitialized = true;

         if (componentIdNew != null) {
             requestComponentSpecificOptions(componentIdNew, componentType, componentName);
         } else {
             monitorComponentStatus();
         }
     } else {
         testLoggedOut(json);
     }

 }
}

function handleErrorMap(errorMap) {

    if (errorMap !== undefined && errorMap != null) {
        // Not a valid workflow

        if (errorMap.component_message_map !== undefined) {
            jQuery(errorMap.component_message_map).each(function (msgMapIndex, msgMapArray) {
                //alert(JSON.stringify(msgMapArray));

//IMPORTANT: mapComponentId == "workflow" , for errors at the wf level
                for (mapComponentId in msgMapArray) {
                    //alert(mapComponentId + ", " + msgMapArray[mapComponentId]);
                    if (msgMapArray[mapComponentId] !== undefined
                            && msgMapArray[mapComponentId].component_message_container !== undefined) {
                        jQuery(msgMapArray[mapComponentId].component_message_container).each(function(messageIndex, messageText) {
                            var humanReadableId = mapComponentId;
                            if (jQuery('#humanReadableId_' + mapComponentId).length > 0) {
                                humanReadableId = jQuery('#humanReadableId_' + mapComponentId).text();
                            }
                            if (messageText.error !== undefined) {
//IMPORTANT: mapComponentId == "workflow" , for errors at the wf level
                                jQuery('<span class="wfErrorMessage" >'
                                        + humanReadableId + ": " + messageText.error
                                        + '</span><br/>').appendTo('#wfMessageBarText');

                                if (jQuery('#' + mapComponentId).length > 0) {
                                    jQuery('#showWarnings_' + mapComponentId).hide();
                                    jQuery('#componentStatus_' + mapComponentId).text('Error');
                                    jQuery('#componentStatus_' + mapComponentId).attr('class', 'componentStatusIcon_error wfStatusIcon');
                                }

                            } else if (messageText.warning !== undefined) {

                            } else if (messageText.info !== undefined) {

                            } else if (messageText.debug !== undefined) {

                            }
                        });
                    }
                }
            });

        }
    } else {
        jQuery('<span id="statusInfo" >An unknown error has occurred. Please contact DataShop Help.'
            + '.</span>').appendTo('#wfMessageBarText');
    }
}

function previewFile(fileId) {
    var dsId = null;
    // The global variable currentDigraph exists and is defined.

            // Send an AJAX request to save the currentDigraph to the database.
            new Ajax.Request('WorkflowResults', {
                parameters : {
                    requestingMethod : 'WorkflowResultsServlet.filePreview',
                    workflowId : currentDigraph.id,
                    fileId : fileId,
                    datasetId : dsId,
                },
                onComplete : showFilePreview,
                beforeSend : wfShowStatusIndicatorModal(false, "Loading..."),
                onSuccess : wfHideStatusIndicator,
                onException : function(request, exception) {
                    wfHideStatusIndicator(); throw(exception);
                }
            });
}


function previewFileInline(componentId, fileId, nodeId, isCompactPreview) {
    var dsId = null;
    // The global variable currentDigraph and component id are defined.
    //alert(componentId);
    if (componentId != null) {
            // Send an AJAX request to save the currentDigraph to the database.
            new Ajax.Request('WorkflowResults', {
                parameters : {
                    requestingMethod : 'WorkflowResultsServlet.filePreview',
                    workflowId : currentDigraph.id,
                    componentId: componentId,
                    fileId : fileId,
                    datasetId : dsId,
                    nodeId : nodeId,
                    isCompactPreview : isCompactPreview
                },
                onComplete : showFilePreviewInline,
                beforeSend : wfShowStatusIndicatorModal(false, "Loading..."),
                onSuccess : wfHideStatusIndicator,
                onException : function(request, exception) {
                    wfHideStatusIndicator(); throw(exception);
                }
            });
    }
}



// Open advanced options dialog.
function openAdvancedOptionsDialog() {

    var parentDiv = jQuery(this).parent();
    var splitString = this.id.split('_');
    var componentType = jQuery(parentDiv).attr("name");

    if (splitString.length == 2) {
        var componentId = splitString[1];
        var componentName = jQuery('#' + componentId + ' .compName').text();

        var componentTypeId = 'componentInterface_' + componentId;

        logWorkflowComponentAction(currentDigraph.id, null,
            componentId, componentName, componentType, null,
                null, null, null, LOG_OPEN_COMPONENT_OPTIONS, "");
        if (!workflowIsRunning) {
            // Create a div with the component-specific options
            saveOpenOptionsAndValidateBeforeNewDialog(componentId, componentType, componentName);
            //requestComponentSpecificOptions(componentId, componentType, componentName);
        } else {
            requestComponentSpecificOptions(componentId, componentType, componentName);
            showCancelWarning();
        }
    }
}


function requestComponentSpecificOptions(componentId, componentType, componentName) {
    var dsId = null;
    lastCall = "";

    if (componentId !== undefined && componentId != null && componentId != '') {

        clearInterval(globalWorkflowTimer);
        processComponentStatusInfoLocked = true;
        // Send an AJAX request to save the currentDigraph to the database.
        new Ajax.Request('WorkflowEditor', {
            parameters : {
                requestingMethod : 'WorkflowEditorServlet.requestComponentSpecificOptions',
                workflowId : currentDigraph.id,
                digraphObject : stringifyDigraph(currentDigraph),
                dirtyBits: JSON.stringify(dirtyBits),
                componentId : componentId,
                componentType : componentType,
                componentName : componentName,
                datasetId : dsId
            },
            beforeSend : wfShowStatusIndicatorModal(false, "Loading..."),
            onComplete : requestComponentSpecificOptionsCompleted,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
        });
    }
}

// Dataset Export and File selector
var projectArray = null;
var datasetArray = null;
var fileArray = null;


// Dataset File Attachment selector
var myProjectArray = null;
var myDatasetArray = null;

/* Update the 'advanced options' dialog values. */
function requestComponentSpecificOptionsCompleted(transport) {
    if (jQuery('.advOptDialogClass').length > 0) {
        jQuery('.advOptDialogClass').dialog('close');
    }

    if (workflowIsRunning) {
        wfTimerDialog("Options cannot be changed while the workflow is running.", lsWorkflowListDialogTime);
    }
    var advOptHeightMultiplier = 6;
    //jQuery('#wfRunButton').off('click');
    jQuery('body').off('click');

    if (transport !== undefined) {
        json = transport.responseText.evalJSON(true);

        if (json.workflowId !== undefined
            && json.componentId !== undefined
                && json.componentOptions !== undefined
                    && (json.componentOptions.option !== undefined
                            || json.componentOptions.file !== undefined)) {

            var componentType = json.componentType;
            var componentName = json.componentName;
            var componentId = json.componentId;
            var pageLocalComponentOptions = componentOptions[componentId];
            var componentInterfaceId = 'componentInterface_' + componentId;
            // Get the component 'advanced options' json for this component.
            var options = json.componentOptions.option;
            var files = json.componentOptions.file;
            // Selected (or uploaded) file ID
            var fileId = null;
            var projectName = null;

            var disabledStr = "";
            if (currentDigraph.isView || workflowIsRunning) { disabledStr = "disabled"; }

            var fileFormId = 'importForm_' + componentId;

            var innerHtml = '<div class="componentOptionsHeader">Parameters</div>'
                + '<div class="componentOptionsDiv" id="componentOptionsDiv_' + componentId + '">'
                    + '<form class="fileInputForm" id="' + fileFormId + '">'
                    + '<table class="advancedOptionsTable">'
                    + '<col width="20%"><col width="78%">';

            var lastJsonValueType = "";
            var formulaFieldDisplayed = false;
            var haveFormulaField = false;
            var formulaDisplayInputfieldVal = "";
            var feHiddenInputInitVal = "";
            var reHiddenInputInitVal = "";
            var responseVal = "";
            var formulaWidth = 40;
            let containsPrivateOptions = false;
            let privOptInd = 0;

            if (options !== undefined) {
                // Create standard options
                jQuery(options).each(function(index, jsonValue) {
                    jQuery(pageLocalComponentOptions).each(function(localIndex, localValue) {
                        if (jsonValue.ref === undefined
                                && jsonValue.name !== undefined) {
                            if (jsonValue.displayName === undefined || jsonValue.displayName == '' || jsonValue.displayName.toLowerCase() == 'null') {
                                jsonValue.displayName = jsonValue.name;
                            }

                            var enumSelect = '';
                            var isMultiSelect = '';
                            //write the formula field if last field is either FixedEffectspanel or randomEffectsPanel and current is not
                            if ((lastJsonValueType == "FixedEffectsPanel" || lastJsonValueType == "RandomEffectsPanel")
                                && (jsonValue.type != "FixedEffectsPanel" && jsonValue.type != "RandomEffectsPanel" )
                                && formulaFieldDisplayed == false && haveFormulaField == true) {
                                formulaDisplayInputfieldVal = getFormulaDisplayValue(feHiddenInputInitVal, reHiddenInputInitVal, responseVal);
                                innerHtml = innerHtml
                                            + '<tr id="wfOptRow_' + jsonValue.name + '"><td class="workflowOptionIds">Formula'
                                            + ':</td><td><textarea rows="4" cols="' + formulaWidth + '" disabled name="formulaDisplayInputfield">' + formulaDisplayInputfieldVal + '</textarea></td></tr>';
                                formulaFieldDisplayed  = true;
                            }

                              //If this component uses a custom interface for options, set it
                            if (needsCustomOptionsInterface(jsonValue.type)) {
                                innerHtml = innerHtml + getHtmlForCustomOptInterface(jsonValue, json);
                            }

                            if (jsonValue.enum !== undefined && jsonValue.enum != '') {

                                var defaultSelected = jsonValue.defaultValue;
                                var enumClass = 'wfAdvOpt';
                                if (jsonValue.type == "FileInputHeader") {
                                    enumClass = 'wfAdvOptMetadata';
                                }
                                if (jsonValue.type == "MultiFileInputHeader") {
                                    enumClass = 'wfAdvOptMetadataMulti';
                                    isMultiSelect = 'multiple ';
                                }

                                // For larger select boxes, open them in a non-modal dialog.
                                //except for linear modeling and family type
                                if (jsonValue.enum.option.length > 5) {
                                    if (json.componentName != "linear_modeling" || jsonValue.name != "family") {
                                            enumClass += " wfAdvOptSelectDialog";
                                        }
                                }

                                // This is an enumerated element.
                                // Build an html select box. If it contains a
                                // default value, that option will be selected.
                                var enumId = String(jsonValue.name);
                                enumSelect =
                                    '<select ' + isMultiSelect + disabledStr + ' class="' + enumClass + '" id="' + enumId + '">';

                                // glm uses a custom interface
                                if (jsonValue.type == "FixedEffectsPanel") {
                                    haveFormulaField = true;
                                        var returnedValFromGLMjs = getHtmlForFixedEffects(jsonValue, componentId, disabledStr, feHiddenInputInitVal);
                                    feHiddenInputInitVal = returnedValFromGLMjs[0];
                                    enumSelect = returnedValFromGLMjs[1];
                                    formulaWidth = returnedValFromGLMjs[2];
                                    } //end of if jsonValue.type is "FixedEffectsPanel"
                                else if (jsonValue.type == "RandomEffectsPanel") {
                                            haveFormulaField = true;
                                            var returnedValFromGLMjs = getHtmlForRandomEffects(jsonValue, componentId, disabledStr, reHiddenInputInitVal);
                                        reHiddenInputInitVal = returnedValFromGLMjs[0];
                                        enumSelect = returnedValFromGLMjs[1];
                                } //end of else if jsonValue.type is "RandomEffectsPanel"

                                var enumOptions = '';
                                var isUnsavedOption = false;
                                var selectedFound = false;
                                // For each component options enum from the server (jsonValue)
                                jQuery(jsonValue.enum).each(function(enumIndex, enumValue) {
                                    // For each value in the enum type
                                    jQuery(enumValue.option).each(function(enumIndex2, enumValue2) {
                                        if (localValue[jsonValue.name] !== undefined &&
                                                !jQuery.isArray(localValue[jsonValue.name])) {
                                            localValue[jsonValue.name] = [ localValue[jsonValue.name] ];
                                        }
                                        jQuery(localValue[jsonValue.name]).each(function(enumIndex3, enumValue3) {
                                            if (enumValue3 !== undefined && enumValue3 == enumValue2.value) {
                                                isUnsavedOption = true;
                                                selectedFound = true;
                                                defaultSelected = enumValue2.value;
                                                return false;
                                            }
                                            if (enumValue2.selected !== undefined) {
                                                selectedFound = true;
                                                defaultSelected = enumValue2.value;
                                                return false;
                                            }
                                        });
                                    });
                                });

                                jQuery(jsonValue.enum).each(function(enumIndex, enumValue) {
                                    jQuery(enumValue.option).each(function(enumIndex2, enumValue2) {
                                        var isSelected = '';

                                        if ((defaultSelected != jsonValue.defaultValue || !selectedFound) && isUnsavedOption) {
                                            if (localValue[jsonValue.name] !== undefined &&
                                                    !jQuery.isArray(localValue[jsonValue.name])) {
                                                localValue[jsonValue.name] = [ localValue[jsonValue.name] ];
                                            }
                                            jQuery(localValue[jsonValue.name]).each(function(enumIndex3, enumValue3) {
                                                if (enumValue3 !== undefined && enumValue3 == enumValue2.value) {
                                                    // This component's select box has changed.
                                                    isSelected = "selected='selected'";
                                                    defaultSelected = enumValue2.value;
                                                    return false;
                                                }
                                            });
                                        } else if (enumValue2.selected !== undefined) {
                                            // Uses the default (or label)
                                            isSelected = "selected='selected'";
                                        }

                                        // Use the selected detector as the default detector
                                        if (jsonValue.displayName === "Detectors" &&
                                                enumValue2.selected === 'selected' &&
                                                (componentOptions[componentId][jsonValue.name] === undefined ||
                                                jQuery.isEmptyObject(componentOptions[componentId]))) {
                                            defaultSelected = enumValue2.value;
                                        }

                                        //set responseVal for FixedEffectsPanel and RandomEffectsPanel
                                        if (jsonValue.name == "response" && isSelected == "selected='selected'") {
                                            responseVal = enumValue2.value;
                                        }
                                        if (jsonValue.type != "FixedEffectsPanel" && jsonValue.type != "RandomEffectsPanel") {
                                            enumOptions = enumOptions + '<option value="' + String(enumValue2.value) + '" '
                                                + isSelected + ' title="' + String(enumValue2.content) + '">' + String(enumValue2.content) + '</option>';
                                        }
                                    });
                                });

                                 if (jsonValue.type != "FixedEffectsPanel" && jsonValue.type != "RandomEffectsPanel") {
                                    enumSelect = String(enumSelect) + enumOptions + '</select>';
                                }

                                backupMultiSelect[enumId] = enumSelect;

                                innerHtml = innerHtml
                                    + '<tr id="wfOptRow_' + jsonValue.name + '"><td class="workflowOptionIds">' + String(formatHeading(jsonValue.displayName))
                                    + ':</td><td>' + enumSelect + '</td></tr>';

                                if (jQuery.isEmptyObject(componentOptions[componentId])
                                        || componentOptions[componentId][jsonValue.name] === undefined) {
                                    componentOptions[componentId][jsonValue.name] = defaultSelected;
                                    isWorkflowSaved = false;
                                    saveTemporaryWorkflowTransient(currentDigraph.id, null, null, null, null);
                                    wfEnableSaveButton();
                                }
                            } else {
                                // This is a simple element.
                                var defaultHint = '';
                                var defaultValue = '';
                                if (componentOptions[componentId] !== undefined && componentOptions[componentId][jsonValue.name] !== undefined
                                        && componentOptions[componentId][jsonValue.name].length > 0) {


                                    defaultValue = htmlEncode(componentOptions[componentId][jsonValue.name]);
                                } else if (jsonValue.defaultValue !== undefined) {
                                    defaultHint = ' title="Default: ' + String(jsonValue.defaultValue) + '"';
                                    defaultValue = htmlEncode(jsonValue.defaultValue);
                                    if (jQuery.isEmptyObject(componentOptions[componentId])
                                            || componentOptions[componentId][jsonValue.name] === undefined) {
                                        componentOptions[componentId][jsonValue.name] = jsonValue.defaultValue;
                                    }
                                }

                                let inputType = "text";
                                let classIfPrivate = "";
                                let lockIconHtml = "";
                                let inputWidthAdjustment = "";
                                let attrIfPrivate = '';

                                if (jsonValue.isPrivate) {
                                    // This is a private option.
                                    containsPrivateOptions = true;
                                    attrIfPrivate = 'privateoption="true"';
                                    lockIconHtml = '<img id="privOptLockIconId' + privOptInd
                                            + '" class="privOptLockIcon" src="images/lock_black.png">';
                                    inputWidthAdjustment = ' style="width: calc(100% - 35px)" ';
                                    privOptInd++;
                                    if (isView) {
                                        // User is just viewing the wf, so they cannot see the option
                                        inputType = "password";
                                        // The default value is already sanitized but just make it 6 blank dots so it looks nice
                                        defaultValue = "......";
                                    } else {
                                        // User is the editor so they can see the option and can toggle to hide it
                                        inputType = "text";
                                    }
                                }

                                var htmlElementType = null;

                                if (jsonValue.type == 'Dataset') {
                                    htmlElementType = '<input ' + disabledStr + ' type="' + inputType + '" ' + defaultHint
                                    + ' class="wfAdvOpt componentDatasetOption" ' + inputWidthAdjustment + attrIfPrivate + ' id="' + String(jsonValue.name) + '" value="'
                                    + defaultValue + '"/>';
                                } else if (jsonValue.type == 'MultiDataset') {
                                    htmlElementType = '<input ' + disabledStr + ' type="' + inputType + '" ' + defaultHint
                                    + ' class="wfAdvOpt componentDatasetOption MultiDataset" ' + inputWidthAdjustment + attrIfPrivate + ' id="' + String(jsonValue.name) + '" value="'
                                    + defaultValue + '"/>';
                                } else {
                                    // Simple element
                                    htmlElementType = '<input ' + disabledStr + ' type="' + inputType + '" ' + defaultHint
                                    + ' class="wfAdvOpt" ' + inputWidthAdjustment + attrIfPrivate + ' id="' + String(jsonValue.name) + '" value="'
                                    + defaultValue + '"/>';
                                }

                                htmlElementType = htmlElementType + lockIconHtml;
                                innerHtml = innerHtml
                                    + '<tr id="wfOptRow_' + jsonValue.name + '"><td>' + String(formatHeading(jsonValue.displayName))
                                    + ':</td><td>' + htmlElementType + '</td></tr>';
                            }
                        } // end of option type
                        //write the formula field if last field is either FixedEffectspanel or randomEffectsPanel and current is not
                       if ((lastJsonValueType == "FixedEffectsPanel" || lastJsonValueType == "RandomEffectsPanel")
                                && (jsonValue.type == "FixedEffectsPanel" || jsonValue.type == "RandomEffectsPanel" )
                                && formulaFieldDisplayed == false && haveFormulaField == true) {
                                formulaDisplayInputfieldVal = getFormulaDisplayValue(feHiddenInputInitVal, reHiddenInputInitVal, responseVal);
                                innerHtml = innerHtml
                                            + '<tr id="wfOptRow_' + jsonValue.name + '"><td class="workflowOptionIds">Formula'
                                            + ':</td><td><textarea rows="4" cols="' + formulaWidth + '" disabled name="formulaDisplayInputfield">' + formulaDisplayInputfieldVal + '</textarea></td></tr>';
                                formulaFieldDisplayed  = true;

                        }

                        lastJsonValueType = jsonValue.type;

                    });
                });
                if (containsPrivateOptions && !isView) {
                    innerHtml = innerHtml + '<div class="privateOptionBoxDiv"><input type="checkbox" id="displayPrivateOptionsBox" checked>'
                            + 'Display private options</div>';
                }
                if (formulaFieldDisplayed == false && haveFormulaField == true) {
                    formulaDisplayInputfieldVal = getFormulaDisplayValue(feHiddenInputInitVal, reHiddenInputInitVal, responseVal);
                    innerHtml = innerHtml
                        + '<tr id="wfOptRow_' + jsonValue.name + '"><td class="workflowOptionIds">Formula'
                        + ':</td><td><textarea rows="4" cols="' + formulaWidth + '" disabled name="formulaDisplayInputfield">' + formulaDisplayInputfieldVal + '</textarea></td></tr>';
                formulaFieldDisplayed  = true;

                }
            } // end of "if (options !== undefined)"

            if (files !== undefined) {
                var arrayIndex = 0;
                jQuery(files).each(function(index, value) {

                    if (value.option !== undefined) {

                        // This is a complex file element.
                        var file = {};
                        file.file_path = '';
                        file.file_name = '';
                        file.index = 0;
                        // Otherwise, the type and label can have default values.

                        jQuery(value.option).each(function(fileAttribIndex, fileAttribValue) {
                            if (fileAttribValue.name == "label") {
                                file.label = fileAttribValue.defaultValue;
                            }
                            if (fileAttribValue.name == "index") {
                                file.index = fileAttribValue.defaultValue;
                            }
                            if (fileAttribValue.name == "subindex") {
                                file.subindex = fileAttribValue.defaultValue;
                            }
                            if (fileAttribValue.name == "file_path") {
                                file.file_path = fileAttribValue.defaultValue;
                            }
                            if (fileAttribValue.name == "file_name") {
                                file.file_name = fileAttribValue.defaultValue;
                            }


                        });

                        if (file.index !== undefined && file.index != null && file.index.length > 0) {
                        } else {
                            file.index = 0;
                        }


                        // If the option file upload already exists, we'll use that information
                        if (file.label !== undefined) {
                            if (componentOptions[componentId] === undefined) {
                                componentOptions[componentId] = new Object();
                            }
                            if (componentOptions[componentId].files === undefined) {
                                componentOptions[componentId].files = [];
                            }

                            // Use a container so that the json <-> xml will be valid and unambiguous.
                            var fileContainer = {};
                            fileContainer[file.label] = file;

                            if (!jQuery.isArray(componentOptions[componentId].files)) {
                                componentOptions[componentId].files = [ componentOptions[componentId].files ];
                            }

                            componentOptions[componentId].files = [];

                            componentOptions[componentId].files.push(fileContainer);
                        }

                        var truncatedFileName = '';
                        if (file.file_name !== undefined && file.file_name != null && file.file_name.length > 0) {
                            truncatedFileName = file.file_name.substring(0, 40) + '..."';
                        }

                        var existingFilesFound = false;
                        var existingDatasetsFound = false; // Determines if the user has access to datasets
                        var datasetFilesChecked = '';
                        var uploadFileChecked =  'checked="true"';

                        if (json.projectArray !== undefined && json.projectArray != null) {
                            if (json.datasetArray !== undefined && json.datasetArray != null) {
                                if (json.fileArray !== undefined && json.fileArray != null && json.fileArray.length > 0) {
                                    datasetFilesChecked =  'checked="true"';
                                    uploadFileChecked = '';
                                    existingFilesFound = true;
                                }
                            }
                        }

                        var fileOptionRadioGroup = '';
                        if (existingFilesFound == true) {
                            fileOptionRadioGroup = '<div class="wfRadioButtonGroup">'
                            + '<input type="radio" name="importType" value="datasetFiles" id="datasetFilesBtn" '
                            + datasetFilesChecked + '><label for="datasetFilesBtn">Dataset Files</label>'
                            + '<input type="radio" name="importType" value="fileUpload" id="fileUploadBtn" '
                            + uploadFileChecked + '><label for="fileUploadBtn">Upload File</label></div>'
                        }

                        innerHtml = innerHtml
                            // Option radio button (file uploads vs server-side files)
                            + '<tr><td colspan="2">'
                            + fileOptionRadioGroup
                            + '</td></tr>';


                        var attachFileHtml = '<tr class="wfFileUploadRow"><td>Choose a dataset: </td>'
                            + '<td>Attaching a file to a dataset makes the file available to anyone with access to the dataset, even if the workflow is private.'
                            + '</td></tr>';

                        // Upload file hidden items
                        innerHtml = innerHtml
                            + '<input type="hidden" id="filePath" value="' + file.file_path + '" />'
                            + '<input type="hidden" id="fileIndex" value="' + file.index + '" />'
                            + '<input type="hidden" id="fileLabel" value="' + file.label + '" />'
                            + '<input type="hidden" id="workflowId" value="' + currentDigraph.id + '" />'
                            + '</td></tr>'

                            + '<tr class="importUploadDiv"><td>'
                            + '</td><td>';

                        if (!currentDigraph.isView) {
                            innerHtml = innerHtml
                            + '<label class="custom-file-upload">'
                            + '<input ' + disabledStr + ' type="file" class="importDataFile" name="file" id="'
                            + 'importFile_' + componentId + '" />'
                            + 'Upload New File' + '</label>';
                        }

                        innerHtml = innerHtml
                            + '</td></tr>'
                            + '</td></tr>';

                        // Current import file
                        var fileInfoString = "File: " + file.file_name;
                        if (json.projectName !== undefined && json.projectName != '') {
                            projectName = json.projectName;
                            fileInfoString = 'Project: ' + json.projectName + '&#013;&#010;'
                                + 'Dataset: ' + json.datasetName + '&#013;&#010;'
                                + 'File: ' + file.file_name;
                        } else if (json.datasetName !== undefined && json.datasetName != '') {
                            fileInfoString = 'Dataset: ' + json.datasetName + '&#013;&#010;'
                            + 'File: ' + file.file_name;
                        }

                        innerHtml = innerHtml
                            + '<tr id="importCurrentFileRow"><td>'
                            + 'Current file:</td><td><input ' + disabledStr + ' type="text" title="'
                            + fileInfoString + '" name="fileName" readonly' + ' id="fileName"'
                            + ' class="fileUploadDisplayText" value="' + truncatedFileName + '" /></td></tr>'
                            + '<tr><td></td><td>'
                            + '<div id="componentDatasetInfo"/>'
                            + '</td></tr>';

                        innerHtml = innerHtml
                        // Attach file to dataset div
                        + '<tr><td></td><td>'
                        + attachFileHtml
                        + '</td></tr>';

                        var wfnIdentifier = "a";
                        if (file.file_name !== undefined && file.file_name.length > 0) {
                            wfnIdentifier = "a different";
                        }

                        if (existingFilesFound) {
                            // Other import option (server-side files)
                            innerHtml = innerHtml + '<tr class="datasetFilesDiv">'
                                + '<td colspan="2">Choose ' + wfnIdentifier + ' file for import:'
                                + '</td></tr>';

                            innerHtml = innerHtml + '<tr class="datasetFilesDiv"><td>Search Datasets</td><td>'
                                + '<input ' + disabledStr
                                    + ' type="text" class="dataFileFilter" maxlength="100" size="40"'
                                        + ' title="Filter Samples By Name, Dataset or Project">'
                                + '</td></tr>';
                            if (componentType.toLowerCase() == "import") {
                                var potMatchChecked = '';
                                if (componentName.toLowerCase() == "file"
                                                || componentName.toLowerCase() == "text"
                                                    || componentName.toLowerCase() == "tab_delimited") {
                                    potMatchChecked = 'checked="checked"';
                                }

                                innerHtml = innerHtml + '<tr class="datasetHiddenFilesDiv"><td colspan="2">'
                                + '<input ' + disabledStr
                                + ' type="checkbox" name="dataHiddenFileFilter" id="dataHiddenFileFilter" ' + potMatchChecked + '>'
                                + '  <label for="dataHiddenFileFilter">Show all potential matches</label>'
                                + '</td></tr>';
                            }
                        }

                        if (json.myProjectArray !== undefined && json.myProjectArray != null) {
                            if (json.myDatasetArray !== undefined && json.myDatasetArray != null) {
                                existingDatasetsFound = true;
                            }
                        }

                        /////////////////////////////////////////
                        // Existing dataset file selector
                        projectArray = null;
                        if (json.projectArray !== undefined && json.projectArray != null) {
                            if (!jQuery.isArray(json.projectArray)) {
                                json.projectArray = [ json.projectArray ];
                            }
                            projectArray = json.projectArray;
                        }

                        datasetArray = null;
                        if (json.datasetArray !== undefined && json.datasetArray != null) {
                            if (!jQuery.isArray(json.datasetArray)) {
                                json.datasetArray = [ json.datasetArray ];
                            }
                            datasetArray = json.datasetArray;
                        }

                        fileArray = null;
                        if (json.fileArray !== undefined && json.fileArray != null) {
                            if (!jQuery.isArray(json.fileArray)) {
                                json.fileArray = [ json.fileArray ];
                            }
                            fileArray = json.fileArray;
                        }

                        if (json.fileId !== undefined) {
                            fileId = json.fileId;
                        }

                        if (projectArray !== undefined && projectArray != null && projectArray.length > 0
                            && datasetArray !== undefined && datasetArray != null && datasetArray.length > 0
                                && fileArray !== undefined && fileArray != null && fileArray.length > 0
                                && fileId != null) {

                            var gfoResult =
                                generateFileOptions(projectArray, datasetArray, fileArray, fileId);
                            var fileOptions = gfoResult.fileOptions;
                            var hiddenInputs = gfoResult.hiddenInputs;

                            innerHtml = innerHtml
                            + '<tr class="datasetFilesDiv"><td colspan="2" >'
                                + '<select size="11" ' + disabledStr + ' id="wfFileId_' + componentId + '" class="existingDataFile" name="wfFileId" >'
                                + fileOptions
                                + '</select>'

                                + '</td></tr>';
                            innerHtml = innerHtml + hiddenInputs;
                        }

                        //////////////////////////////////////////
                        // Dataset File Attachment selector
                        myProjectArray = null;
                        if (json.myProjectArray !== undefined && json.myProjectArray != null) {
                            if (!jQuery.isArray(json.myProjectArray)) {
                                json.myProjectArray = [ json.myProjectArray ];
                            }
                            myProjectArray = json.myProjectArray;
                        }

                        myDatasetArray = null;
                        if (json.myDatasetArray !== undefined && json.myDatasetArray != null) {
                            if (!jQuery.isArray(json.myDatasetArray)) {
                                json.myDatasetArray = [ json.myDatasetArray ];
                            }
                            myDatasetArray = json.myDatasetArray;
                        }

                        if (currentDigraph.isView != true) {
                            if (myProjectArray !== undefined && myProjectArray != null && myProjectArray.length > 0
                                && myDatasetArray !== undefined && myDatasetArray != null && myDatasetArray.length > 0) {

                                var attachDatasetOptions =
                                    generateAttachableDatasetSelect(myProjectArray, myDatasetArray);

                                innerHtml = innerHtml + '<tr class="wfFileUploadRow"><td>Filter Datasets</td><td>'
                                    + '<input ' + disabledStr
                                    + ' type="text" class="datasetFilter" maxlength="100" size="40"'
                                    + ' title="Filter by Dataset or Project">'
                                    + '</td></tr>';

                                innerHtml = innerHtml
                                    + '<tr class="wfFileUploadRow"><td colspan="2" >'
                                    + '<select size="11" ' + disabledStr + ' id="wfDatasetId_' + componentId + '" class="existingDataset" name="wfDatasetId" >'
                                    + attachDatasetOptions
                                    + '</select>'
                                    + '</td></tr>';
                            } else {

                                innerHtml = innerHtml + '<tr class="wfFileUploadRow"><td></td><td>'
                                + 'You do not have <a href="help?page=requesting-access" target="_blank">edit access</a> to any Projects.</td>';
                            }
                        }
                    }
                });
            }

            innerHtml = innerHtml + '</table></form></div>';


            if (json.componentCitation !==undefined && json.componentCitation !== "") {
                innerHtml = innerHtml + '<div class="componentInfoHeader">Component Information</div>'
                    + '<div class="componentOptionsPartition">'
                    + '<div class="infoDiv" id="componentSourceDiv_' + componentId + '">'
                    + '<a target="_blank" href="' + json.componentCitation + '">'
                    + 'View on GitHub'
                    + '<img src="images/information.png" class="leftPadded"/>'
                    + '</a>'
                    + '</div>';
            }


            // Info div
            if (json.componentInfo !== undefined && json.componentInfo !== "") {
                innerHtml = innerHtml
                    + '<div class="infoDiv" id="componentInfoDiv_' + componentId + '">'
                    + json.componentInfo + '</div></div>';
            }


            // The var, advOptHeightMultiplier, affects the min and max element height for
            // each option element in the 'advanced options' dialog.
            if ((options !== undefined && options.length > 10)
                    || (files !== undefined && files.length > 10)) {
                advOptHeightMultiplier = 10;
            } else {
                advOptHeightMultiplier = 6;
            }

            jQuery('input[class=wfAdvOpt]').off('keyup');
            jQuery('input[class=wfAdvOpt]').off('change');
            jQuery('select[class=wfAdvOpt]').off('change');
            jQuery('select[class=wfAdvOptMetadata]').off('change');
            jQuery('.importDataFile').off('change');
            jQuery('.existingDataFile').off('change');
            jQuery('.existingDataset').off('change');

            // Now, open the populated 'advanced options' dialog.
            //adjust dialog size if it's for glm/glmr component
            var widthMeasure = "33%";
            if (haveFormulaField == true) {
                widthMeasure = "60%";
            }else if (setOptionsPaneWidth[json.componentId] !== undefined) {
              widthMeasure = setOptionsPaneWidth[json.componentId];
            }

            var humanReadableName = getHumanReadableComponentName(componentId);

            var dialogTitle = componentTitleMap[componentName.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase()] + ' Options';
            if ((humanReadableName !== undefined) && (humanReadableName.length > 0)) {
                dialogTitle += ' (' + humanReadableName + ')';
            }
            jQuery('<div />', {
                id : 'advancedOptionsDialog_' + componentId,
                class : 'advOptDialogClass'
            }).html(innerHtml).dialog({
                open : function() {
                    jQuery('.ui-button').focus();
                    if (!currentDigraph.isView && !workflowIsRunning) {
                        jQuery('input[class=wfAdvOpt]').on('keyup');
                        jQuery('input[class=wfAdvOpt]').on('change');
                        jQuery('select[class=wfAdvOpt]').on('change');

                        jQuery('.importDataFile').on('change');
                        jQuery('.importDataFile').change(uploadOptionsFile);

                        // If the select box is big, open it in a non-modal dialog.
                        jQuery('.wfAdvOptSelectDialog').click(openSelectInDialog);

                        jQuery('.componentDatasetOption').click(populateDatasetDialog);

                        jQuery('.existingDataset').on('change');
                        jQuery('.existingDataset').change(attachUploadToDataset);

                        jQuery('.existingDataFile').on('change');
                        jQuery('.existingDataFile').change(existingOptionsFile);

                        jQuery('input[class=wfAdvOpt]').keyup(optionKeyListener);
                        jQuery('input[class=wfAdvOpt]').bind('focusout', optionKeyListenerLogger);
                        jQuery('select[class=wfAdvOpt]').change(changeSelectListener);
                        jQuery('select[class=wfAdvOptMetadata]').change(changeSelectListener);
                        jQuery('select[class=wfAdvOptMetadataMulti]').click(changeSelectListener);

                        jQuery(".existingDataFile").filterFilesByText(jQuery('.dataFileFilter'));
                        jQuery(".existingDataset").filterDatasetsByText(jQuery('.datasetFilter'));
                        jQuery(".dataFileFilter").hint("auto-hint");
                        jQuery(".datasetFilter").hint("auto-hint");

                        jQuery('.wfRadioButtonGroup').buttonset();
                        jQuery('input[name=importType]').change(changeImportType);
                        changeImportType();

                        if (jQuery('#dataHiddenFileFilter').length > 0) {
                            jQuery('#dataHiddenFileFilter').click(refreshImportList);
                        }
                        jQuery('.dataHiddenFileOption').attr('hidden', 'true');

                        jQuery('.wfFileUploadRow').hide();

                        jQuery('label').click(function() {
                            labelID = jQuery(this).attr('for');
                            jQuery('#datasetFiles').trigger('click');
                        });

                        refreshImportList();
                    }

                    setOutsideOptionPanelClick(componentId);

                    testOptionDependencies(componentId);

                    if (json.datasetLink !== undefined && json.datasetLink != "") {
                        // dataset is linked, show indicator
                        updateAttachBlock(json.datasetLink, json.datasetName, componentId);
                    } else {
                        // no dataset is linked, show link action icon
                        updateAttachBlock("", "", componentId);
                        jQuery('.addDatasetLink').hide();
                    }
                    // Resizable option panels in the option pane
                    jQuery(".componentOptionsDiv").resizable({handles: 's'});
                    jQuery(".componentOptionsPartition").resizable({handles: 's'});
                    // Set the handles option after initialization
                    jQuery('#advancedOptionsDialog_' + componentId).parent().resizable({handles: "sw"});

                    /*jQuery('.ui-resizable').enscroll({
                        verticalTrackClass: 'track1',
                        verticalHandleClass: 'handle1',
                        drawScrollButtons: true,
                        scrollUpButtonClass: 'scroll-up1',
                        scrollDownButtonClass: 'scroll-down1',
                        addPaddingToPane: false,
                        pollChanges: true
                    });*/
                },
                autoOpen : true,
                autoResize : false,
                resizable : true,
                width : widthMeasure,
                // height = height of window - (height of statusbar + header)
                height: jQuery(window).height() - jQuery('#wfStatusBar').outerHeight() - jQuery('#ls-header').outerHeight(),
                position: { my: "right top", at: "right top", of: "#process-div", within: "body"  },
                modal : false,
                title : dialogTitle,
                beforeClose : function() {
                    if (dirtyOptionPane && !workflowIsRunning) {
                        saveTemporaryWorkflowTransient(workflowId, null, null, null, null);
                    }
                },
                close : function() {
                    //jQuery('#wfRunButton').click(runWorkflow);
                    jQuery(this).remove();
                    // Close any of the select dialogs...
                    jQuery('.selectNonModal').dialog('close');
                }
            });

            // Add functionality to show private options when the checkbox is checked
            jQuery('#displayPrivateOptionsBox').change(function() {
                if (jQuery(this).is(":checked")) {
                    //jQuery('.privateOption').attr('type', 'text');
                    jQuery('input[privateoption="true"]').attr('type', 'text');
                } else {
                    //jQuery('.privateOption').attr('type', 'password');
                    jQuery('input[privateoption="true"]').attr('type', 'password');
                }
            });

            // Add tooltips to private options if they exist
            jQuery('.privOptLockIcon').each(function() {
                var options = new Array();
                options['extraClasses'] = 'privateOptionToolTip';
                options['timeout'] = '100000';
                options['delay'] = '200';

                if (isView) {
                    new ToolTip(jQuery(this).attr('id'), 'This option is private.  Only the owner of the workflow can see its value.', options);
                } else {
                    new ToolTip(jQuery(this).attr('id'), 'This option is private.  Only you, the owner of the workflow, can see its value.', options);
                }
            });

            // clone large selects... hide and then remove all but first
            jQuery('.wfAdvOptSelectDialog').each(function() {
                var thisId = this.id;
                var largeSelect = jQuery(this).clone();
                var largeSelectId = thisId + '_hiddenSelect';
                largeSelect.attr('id', largeSelectId);
                largeSelect.addClass('ignorableOption');
                largeSelect.hide();
                jQuery(largeSelect).appendTo('#' + 'advancedOptionsDialog_' + componentId);

                if (!jQuery(this).hasClass('wfAdvOptMetadataMulti')) {
                    if (!workflowIsRunning) {
                        jQuery(this).find('option').not(':selected').remove();

                        var cellDiv = jQuery(this).parent();
                        var newDiv = jQuery('<div id="' + this.id + '" class="' + jQuery(this).attr('class') +
                                            '"><p>' + jQuery(this).text() + '</p></div>');
                        cellDiv.html("");
                        newDiv.appendTo(cellDiv);

                        newDiv.click(openSelectInDialog);
                    }
                }
            });

            wfHideStatusIndicator();
            processComponentStatusInfoLocked = false;
            monitorComponentStatus();

        } else if (json.workflowId !== undefined
            && json.componentId !== undefined
                && json.componentOptions !== undefined) {
            var innerHtml = '';
            var componentType = json.componentType;
            var componentName = json.componentName;
            var componentId = json.componentId;

            if (json.componentCitation !==undefined && json.componentCitation !== "") {
                innerHtml = innerHtml + '<div class="componentInfoHeader">Component Information</div>'
                    + '<div class="componentOptionsPartition">'
                    + '<div class="infoDiv" id="componentSourceDiv_' + componentId + '">'
                    + '<a target="_blank" href="' + json.componentCitation + '">'
                    + 'View on GitHub'
                    + '<img src="images/information.png" class="leftPadded"/>'
                    + '</a>'
                    + '</div>';
            }


            // Info div
            if (json.componentInfo !== undefined && json.componentInfo !== "") {
                innerHtml = innerHtml
                    + '<div class="infoDiv" id="componentInfoDiv_' + componentId + '">'
                    + json.componentInfo + '</div></div>';
            }

            var humanReadableName = getHumanReadableComponentName(componentId);

            var dialogTitle = componentTitleMap[componentName.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase()] + ' Options';
            if ((humanReadableName !== undefined) && (humanReadableName.length > 0)) {
                dialogTitle += ' (' + humanReadableName + ')';
            }
            // Now, open the populated 'advanced options' dialog.
            jQuery('<div />', {
                id : 'advancedOptionsDialog_' + componentId,
                class : 'advOptDialogClass'
            }).html(innerHtml).dialog({
                create : function() {

                    setOutsideOptionPanelClick(componentId);

                    // Resizable option panels in the option pane
                    jQuery(".componentOptionsDiv").resizable({handles: 's'});
                    jQuery(".componentOptionsPartition").resizable({handles: 's'});

                },
                open : function() {

                    jQuery('#advancedOptionsDialog_' + componentId).parent().resizable({handles: "sw"});

                    testOptionDependencies(componentId);
                },
                autoResize : false,
                resizable : false,
                autoOpen : true,
                width : "33%",
                // height = height of window - (height of statusbar + header)
                height: jQuery(window).height() - jQuery('#wfStatusBar').outerHeight() - jQuery('#ls-header').outerHeight(),
                position: { my: "right top", at: "right top", of: "#process-div", within: "body"  },
                modal : false,
                title : dialogTitle,
                beforeClose : function() {
                    if (dirtyOptionPane && !workflowIsRunning) {
                        saveTemporaryWorkflowTransient(workflowId, null, null, null, null);
                    }
                },
                close : function() {
                    jQuery(this).remove();
                    // Close any of the select dialogs...
                    jQuery('.selectNonModal').dialog('close');
                }
            });

            // clone large selects... hide and then remove all but first
            jQuery('.wfAdvOptSelectDialog').each(function() {
                    var thisId = this.id;
                    var largeSelect = jQuery(this).clone();
                    var largeSelectId = thisId + '_hiddenSelect';
                    largeSelect.attr('id', largeSelectId);
                    largeSelect.addClass('ignorableOption');
                    largeSelect.hide();
                    jQuery(largeSelect).appendTo('#' + 'advancedOptionsDialog_' + componentId);

                    if (!jQuery(this).hasClass('wfAdvOptMetadataMulti')) {
                        if (!workflowIsRunning) {
                            jQuery(this).find('option').not(':selected').remove();

                            var cellDiv = jQuery(this).parent();
                            var newDiv = jQuery('<div id="' + this.id + '" class="' + jQuery(this).attr('class') +
                                                '"><p>' + jQuery(this).text() + '</p></div>');
                            cellDiv.html("");
                            newDiv.appendTo(cellDiv);

                            newDiv.click(openSelectInDialog);
                        }
                    }
                });
            wfHideStatusIndicator();
            processComponentStatusInfoLocked = false;
            monitorComponentStatus();
        } else {
            testLoggedOut(json);
        }
    }
    if (componentType == "import") {
        // The old import components have bad spacing.  This will help fix that.
        jQuery('.advancedOptionsTable td:first-child').css('width', '30%');
    }
}

function getHumanReadableComponentName(componentId) {

    // Walk the components to get the human-readable component name.

    var humanReadableName = "";
    var components = currentDigraph.components;
    if (!jQuery.isArray(components)) {
        components = [ components ];
    }

    if (components.length > 0) {
        jQuery.each(components, function(index) {

                if (components[index] !== undefined && components[index].component_id == componentId) {

                    humanReadableName = components[index].component_id_human;

                    // break the each loop if we find the object we need
                    return false;
                }
            });
    }

    return humanReadableName;
}

function enableMultiSelect(selectId) {
    if (jQuery('#' + selectId).hasClass('wfAdvOptMetadataMulti')) {
        jQuery('#' + selectId).removeAttr('disabled');
    }
}

function disableAndClearMultiSelect(selectId) {
    if (jQuery('#' + selectId).hasClass('wfAdvOptMetadataMulti')) {
        jQuery('#' + selectId).attr('disabled', true);
        jQuery('#' + selectId + " option:selected").prop("selected", false);
    }
}

function openSelectInDialog(event) {

    // Close other select dialogs... if any are open.
    jQuery('.selectNonModal').dialog('close');

    // Id of 'shortened' select. Use it to find hidden clone.
    var thisId = this.id;
    disableAndClearMultiSelect(thisId);

    var hiddenSelectId = thisId + '_hiddenSelect';
    var hiddenSelect = jQuery('#' + hiddenSelectId);


    // Id of select to be used in dialog.
    var theSelectId = thisId + '_select';
    var dialogId = 'selectNonModal_' + thisId;

    jQuery(this).change(function() {

        if (!workflowIsRunning && dirtySelectDialog) {
            var optDialogContainerId = jQuery('.advOptDialogClass').attr("id");
            if (optDialogContainerId !== undefined) {
                // Save options to server when options box is closed and validate
                var splitString = optDialogContainerId.split('_');
                var componentId = splitString[1];
                isWorkflowSaved = false;
                jQuery('.wfAdvOptMetadataMulti').not('.ignorableOption').each(function(index, componentOption) {
                    var key = jQuery(this).attr('id');
                    if (key != "file" && jQuery('#' + key).length > 0) {
                        var optItem = { key : jQuery('#' + key).val() };
                        componentOptions[componentId][key] = jQuery('#' + key).val();
                        if (jQuery('#' + key).is('div')) {
                            // look at the hiddenSelect and get value for option that matches...
                            // this is necessary in case option text and value differ
                            var pVal = jQuery('#' + key + ' > p').html();
                            componentOptions[componentId][key] =
                                jQuery('#' + key + '_hiddenSelect > option:contains("' + pVal + '")').val();
                        }
                    }
                 });

                jQuery('.wfAdvOptMetadata').not('.ignorableOption').each(function(index, componentOption) {
                    var key = jQuery(this).attr('id');

                    if (key != "file" && jQuery('#' + key).length > 0) {
                        var optItem = { key : jQuery('#' + key).val() };
                        componentOptions[componentId][key] = jQuery('#' + key).val();
                        if (jQuery('#' + key).is('div')) {
                            // look at the hiddenSelect and get value for option that matches...
                            // this is necessary in case option text and value differ
                            var pVal = jQuery('#' + key + ' > p').html();
                            componentOptions[componentId][key] =
                                jQuery('#' + key + '_hiddenSelect > option:contains("' + pVal + '")').val();
                        }
                    }
                 });

                jQuery('.wfAdvOpt').not('.ignorableOption').each(function(index, componentOption) {
                    var key = jQuery(this).attr('id');

                    if (key != "file" && jQuery('#' + key).length > 0) {
                        var optItem = { key : jQuery('#' + key).val() };
                        componentOptions[componentId][key] = jQuery('#' + key).val();
                        if (jQuery('#' + key).is('div')) {
                            // look at the hiddenSelect and get value for option that matches...
                            // this is necessary in case option text and value differ
                            var pVal = jQuery('#' + key + ' > p').html();
                            componentOptions[componentId][key] =
                                jQuery('#' + key + '_hiddenSelect > option:contains("' + pVal + '")').val();
                        }
                    }
                 });

        setComponentOptionsForGLMPanel(componentId);

                dirtyBits[componentId] = DIRTY_SELECTION;
                lastCall = "closeOptionDialog";

                testOptionDependencies(componentId);

                var dsId = null;

                dirtySelectDialog = false;

                new Ajax.Request('WorkflowEditor', {
                    parameters : {
                        requestingMethod : 'WorkflowEditorServlet.updateDirtyBits',
                        workflowId : currentDigraph.id,
                        datasetId : dsId,
                        dirtyBits: JSON.stringify(dirtyBits)
                    },
                    //onComplete: monitorComponentStatus,
                    onSuccess: function() {
                        if (!workflowIsRunning) {
                            saveTemporaryWorkflowTransient(workflowId, null, null, null, null);
                        }

                    },
                    onException : function(request, exception) {
                        wfHideStatusIndicator(); throw(exception);
                    }
                });
            }

        }


    });

    // Get the option label.
    var rowDiv = jQuery(this).parent().parent();
    var cellDiv = rowDiv.children('td.workflowOptionIds');
    var optionLabel = cellDiv.html();
    optionLabel = optionLabel.substring(0, optionLabel.lastIndexOf(':'));

    var selectSize = 0;
    var maxLen = 0;
    jQuery('#' + hiddenSelectId + ' > option').each(function(index){
            if (this.value.length > maxLen) { maxLen = this.value.length; }
            selectSize++;
        });

    if (selectSize > 10) { selectSize = 10; }

    var optDialogContainerId = jQuery(this).parent().parent().parent().parent().parent().parent().attr("id");
    if (optDialogContainerId !== undefined) {
        var splitString = optDialogContainerId.split('_');
        var componentId = splitString[1];

        jQuery(hiddenSelect.children()).each(function(index, selectOption) {
            jQuery(selectOption).removeAttr('selected');
        });
        jQuery(componentOptions[componentId]).each(function(index, componentOption) {
            var c = componentOption;
            var optionValues = componentOption[thisId];

            if (!jQuery.isArray(optionValues)) {
                optionValues = [ optionValues ];
            }

            jQuery(optionValues).each(function(index, currentlySelected) {

                jQuery(hiddenSelect.children()).each(function(index, selectOption) {
                    var s = selectOption;
                    var selectValues = jQuery(selectOption).val();
                    if (selectValues == currentlySelected) {
                        jQuery(selectOption).attr('selected', 'true');
                    }
                });

            });

        });
    }

    var theSelect = jQuery('#' + hiddenSelectId).clone();
    theSelect.attr('id', theSelectId);
    theSelect.addClass('ignorableOption');
    theSelect.attr('size', selectSize);
    theSelect.css('width', '100%');
    theSelect.show();

    // Corresponds to hitting "OK" in the select popup dialog.
    var theSelectId = theSelect.attr('id');
    jQuery(theSelect).children().each(function() {
            jQuery(this).dblclick(function() {

                updateOriginalSelect(theSelectId);
                enableMultiSelect(thisId);
                jQuery('#' + dialogId).dialog('close');

            });
        });

    // Create the dialog
    jQuery('body').off('click');
    jQuery('<div/>', {
            id : dialogId,
            class : 'selectNonModal'
    }).appendTo('body');

    jQuery('<p/>', {
            id : 'optionSearchLabel',
            text: 'Search options'
    }).appendTo('#' + dialogId);

    jQuery('<input/>', {
            id : 'selectOptionFilter',
            type : 'text',
            maxLength : '100',
            size : '30',
            title : 'Filter options...'
    }).appendTo('#' + dialogId);

    jQuery('<div/>', {
            id : 'selectDiv'
    }).appendTo('#' + dialogId);

    theSelect.appendTo('#selectDiv');

    var dialogWidth = maxLen * 7.5;
    if (dialogWidth > 500) { dialogWidth = 500; }
    if (dialogWidth < 360) { dialogWidth = 360; }


    jQuery('#' + dialogId).dialog({
          modal : false,
          autoOpen : false,
          title : optionLabel,
          width : dialogWidth,
          buttons : {
              'OK' : function() {
                  updateOriginalSelect(theSelectId);
                  enableMultiSelect(thisId);
                  jQuery(this).dialog('close');
              },
              'Cancel' : function() {
                  var updateFlag = false;
                  updateOriginalSelect(theSelectId, updateFlag);
                  enableMultiSelect(thisId);
                  jQuery(this).dialog('close');
              },
          },
          close : function() {
              var updateFlag = false;
              updateOriginalSelect(theSelectId, updateFlag);
              enableMultiSelect(thisId);
              jQuery(this).remove();
          }
      });

    jQuery(theSelect).filterOptionsByText(jQuery('#selectOptionFilter'), dialogId);
    jQuery('#selectOptionFilter').hint("auto-hint");



    jQuery('#' + dialogId).dialog('open');
    jQuery('#statusIndicatorModal').dialog("option", "resizable", false);
}

jQuery.fn.filterOptionsByText = function(textbox, dialogId) {
    this.each(function() {
        var options = [];
        var select = this;

        jQuery(select).children().each(function() {
            if (this.tagName !== undefined) {
                if (this.tagName.toLowerCase() == "option") {
                    options.push({value: jQuery(this).val(), text: jQuery(this).text()});
                }
            }
        });

        var fullOpts = options;
        jQuery(select).data('options', options);

        jQuery(textbox).bind('keyup', function() {
            if (jQuery(textbox).val().trim() == "") {
                jQuery(select).empty();
                options = fullOpts;
            } else {
                jQuery(select).empty();
            }

            var search = jQuery.trim(jQuery(this).val());
            var regex = new RegExp(search,"gi");

            // Look for matches on options
            var matchingOptions = [];
            var count = 0;
            jQuery(fullOpts).each(function() {
                    var optVal = this.value;
                    var optText = this.text;

                    if ((optVal.match(regex) !== null) ||
                        (optText.match(regex) !== null)) {
                        matchingOptions[count] = this;
                        count++;
                    }
            });
            jQuery(select).append(jQuery(generateMatchingOptions(matchingOptions)));

            var theSelectId = jQuery(select).attr("id");
            jQuery(select).children().each(function() {
                    jQuery(this).dblclick(function() {

                            updateOriginalSelect(theSelectId);
                            jQuery('#' + dialogId).dialog('close');
                    });
                });

            });

    });
};

// Turn array of value/text pairs into list of options
function generateMatchingOptions(matches) {
    var result = '';

    jQuery(matches).each(function() {
            result = result
                + '<option value="' + String(this.value) + '">'
                + String(this.text) + '</option>';
        });

    return result;
}

function updateOriginalSelect(selectId, updateFlag) {
    // thisId is the id of the select in the dialog
    var origSelectId = selectId.substring(0, selectId.indexOf("_select"));
    var selectedValue = jQuery('#' + selectId).val();

    var origSelect = jQuery('#' + origSelectId);

    if (updateFlag === undefined || updateFlag === true) {
        if (jQuery(origSelect).hasClass('wfAdvOptMetadataMulti')) {

            jQuery('#' + origSelectId + ' option').prop('selected', false).removeAttr('selected');
            jQuery(selectedValue).each(function(index, value) {
                jQuery('#' + origSelectId + ' option[value="' + value + '"]').prop('selected', true);
                jQuery('#' + origSelectId + ' option[value="' + value + '"]').attr('selected', 'selected');
            });


        } else {
            // Here we will use the text in case they differ...
            var selectedText = jQuery('#' + selectId + " option:selected").text();
            var newHtml = '<p>' + selectedText + '</p>';
            jQuery(origSelect).html(newHtml);
        }

        backupMultiSelect[origSelectId] = jQuery(origSelect).prop("outerHTML");

        // Update hidden clone
        var hiddenSelectId = origSelectId + '_hiddenSelect';
        var hiddenSelect = jQuery('#' + hiddenSelectId);

        jQuery('#' + hiddenSelectId).val(selectedValue);

        dirtySelectDialog = true;
        jQuery('#' + origSelectId).trigger('change');


    } else if (jQuery(origSelect).hasClass('wfAdvOptMetadataMulti')) {
        restorePreviousMultiSelect(origSelect);
    }
}

function restorePreviousMultiSelect(selectInput) {
    jQuery(selectInput).replaceWith(backupMultiSelect[selectInput.attr('id')]);
    jQuery('.wfAdvOptSelectDialog').off('click');
    jQuery('.wfAdvOptSelectDialog').click(openSelectInDialog);
}

function refreshImportList() {
    if (jQuery('#dataHiddenFileFilter').length > 0) {
        if (jQuery('#dataHiddenFileFilter').prop('checked') == true) {
            jQuery('.dataHiddenFileOption').removeAttr('hidden');
        } else if (jQuery('#dataHiddenFileFilter').prop('checked') != true) {
            jQuery('.dataHiddenFileOption').attr('hidden', 'true');
        }
    }
}
function changeImportType() {

    var importOptionSelected = jQuery('input[name=importType]:checked').val();

    if (importOptionSelected == 'datasetFiles') {
        jQuery('.datasetFilesDiv').show();
        jQuery('.wfFileUploadRow').hide();
        jQuery('.datasetHiddenFilesDiv').show();
        jQuery('.importUploadDiv').hide();
        jQuery('.importDatabaseDiv').hide();

        jQuery('.addDatasetLink').hide();
        jQuery('.wfFileUploadRow').hide();


    } else if (importOptionSelected == 'database') {
        // This database button was removed but will be added back when database imports are once again in trunk
        jQuery('.importDatabaseDiv').show();
        jQuery('.importUploadDiv').hide();
        jQuery('.datasetFilesDiv').hide();
        jQuery('.wfFileUploadRow').hide();
        jQuery('.datasetHiddenFilesDiv').hide();

        jQuery('.addDatasetLink').show();

    } else {    // default import option: importOptionSelected == 'fileUpload'
        jQuery('.importUploadDiv').show();
        jQuery('.importDatabaseDiv').hide();
        jQuery('.datasetFilesDiv').hide();
        jQuery('.wfFileUploadRow').hide();
        jQuery('.datasetHiddenFilesDiv').hide();

        jQuery('.addDatasetLink').show();
    }

    if (currentDigraph.isView) {
        jQuery('.wfFileUploadRow').hide();
        jQuery('.importUploadDiv').hide();
    }
}

function disableSaveOptionsButton() {
    jQuery('#saveOptionsButton').button('disable');
    jQuery('#saveOptionsButton').css('border', '');
}

//Update component options.
function updateAdvancedOptions(componentId) {
 customOptionsHandleOptionsUpdate(componentId, false);

 var componentName = null;

 jQuery('.wfAdvOpt').not('.ignorableOption').each(function(index, componentOption) {
     var key = jQuery(this).attr('id');

     if (key != "file" && jQuery('#' + key).length > 0) {
           var optItem = { key : jQuery('#' + key).val() };
           componentOptions[componentId][key] = jQuery('#' + key).val();
           if (jQuery('#' + key).is('div')) {
               // look at the hiddenSelect and get value for option that matches...
               // this is necessary in case option text and value differ
               var pVal = jQuery('#' + key + ' > p').html();
               componentOptions[componentId][key] =
                   jQuery('#' + key + '_hiddenSelect > option:contains("' + pVal + '")').val();
           }
     }

 });

 jQuery('.wfAdvOptMetadata').not('.ignorableOption').each(function(index, componentOption) {
    var key = jQuery(this).attr('id');

    if (key != "file" && jQuery('#' + key).length > 0) {
        var optItem = { key : jQuery('#' + key).val() };
        componentOptions[componentId][key] = jQuery('#' + key).val();
        if (jQuery('#' + key).is('div')) {
            // look at the hiddenSelect and get value for option that matches...
            // this is necessary in case option text and value differ
            var pVal = jQuery('#' + key + ' > p').html();
            componentOptions[componentId][key] =
                jQuery('#' + key + '_hiddenSelect > option:contains("' + pVal + '")').val();
        }
    }
 });

 jQuery('.wfAdvOptMetadataMulti').not('.ignorableOption').each(function(index, componentOption) {
    var key = jQuery(this).attr('id');
    if (key != "file" && jQuery('#' + key).length > 0) {
        var optItem = { key : jQuery('#' + key).val() };
        componentOptions[componentId][key] = jQuery('#' + key).val();
        if (jQuery('#' + key).is('div')) {
            // look at the hiddenSelect and get value for option that matches...
            // this is necessary in case option text and value differ
            var pVal = jQuery('#' + key + ' > p').html();
            componentOptions[componentId][key] =
                jQuery('#' + key + '_hiddenSelect > option:contains("' + pVal + '")').val();
        }
    }
 });
  setComponentOptionsForGLMPanel(componentId);
  testOptionDependencies(componentId);

     var dsId = null;
     new Ajax.Request('WorkflowEditor', {
         parameters : {
             requestingMethod : 'WorkflowEditorServlet.updateDirtyBits',
             workflowId : currentDigraph.id,
             datasetId : dsId,
             dirtyBits: JSON.stringify(dirtyBits)
         },
         onComplete: monitorComponentStatus,
         onException : function(request, exception) {
             wfHideStatusIndicator(); throw(exception);
         }
     });

}

//Update component options.
function updateAdvancedOptionsAndValidate(componentId) {
    wfShowStatusIndicatorModal(false, "Validating...");
    jQuery('.wfAdvOpt').not('.ignorableOption').each(function(index, componentOption) {
       var key = jQuery(this).attr('id');

         if (key != "file" && jQuery('#' + key).length > 0) {
             var optItem = { key : jQuery('#' + key).val() };
               componentOptions[componentId][key] = jQuery('#' + key).val();
               if (jQuery('#' + key).is('div')) {
                   // look at the hiddenSelect and get value for option that matches...
                   // this is necessary in case option text and value differ
                   var pVal = jQuery('#' + key + ' > p').html();
                   componentOptions[componentId][key] =
                       jQuery('#' + key + '_hiddenSelect > option:contains("' + pVal + '")').val();
               }
         }
     });

    jQuery('.wfAdvOptMetadata').not('.ignorableOption').each(function(index, componentOption) {
       var key = jQuery(this).attr('id');

         if (key != "file" && jQuery('#' + key).length > 0) {
             var optItem = { key : jQuery('#' + key).val() };
               componentOptions[componentId][key] = jQuery('#' + key).val();
               if (jQuery('#' + key).is('div')) {
                   // look at the hiddenSelect and get value for option that matches...
                   // this is necessary in case option text and value differ
                   var pVal = jQuery('#' + key + ' > p').html();
                   componentOptions[componentId][key] =
                       jQuery('#' + key + '_hiddenSelect > option:contains("' + pVal + '")').val();
               }
         }
     });

    jQuery('.wfAdvOptMetadataMulti').not('.ignorableOption').each(function(index, componentOption) {
       var key = jQuery(this).attr('id');

         if (key != "file" && jQuery('#' + key).length > 0) {
             var optItem = { key : jQuery('#' + key).val() };
               componentOptions[componentId][key] = jQuery('#' + key).val();
               if (jQuery('#' + key).is('div')) {
                   // look at the hiddenSelect and get value for option that matches...
                   // this is necessary in case option text and value differ
                   var pVal = jQuery('#' + key + ' > p').html();
                   componentOptions[componentId][key] =
                       jQuery('#' + key + '_hiddenSelect > option:contains("' + pVal + '")').val();
               }
         }
    });
   setComponentOptionsForGLMPanel(componentId);
   testOptionDependencies(componentId);

   var dsId = null;
   new Ajax.Request('WorkflowEditor', {
       parameters : {
           requestingMethod : 'WorkflowEditorServlet.updateDirtyBits',
           workflowId : currentDigraph.id,
           datasetId : dsId,
           dirtyBits: JSON.stringify(dirtyBits)
       },
       //onComplete: monitorComponentStatus,
       onSuccess: function() {
           if (!workflowIsRunning) {
               saveTemporaryWorkflowTransient(workflowId, null, null, null, null);
           }

       },
       onException : function(request, exception) {
           wfHideStatusIndicator(); throw(exception);
       }
   });
}


//Update component options.
function updateAdvancedOptionsAndValidateBeforeNewDialog(componentId, componentIdNew, componentType, componentName) {

    jQuery('.wfAdvOpt').not('.ignorableOption').each(function(index, componentOption) {
     var key = jQuery(this).attr('id');

       if (key != "file" && jQuery('#' + key).length > 0) {
           var optItem = { key : jQuery('#' + key).val() };
             componentOptions[componentId][key] = jQuery('#' + key).val();
             if (jQuery('#' + key).is('div')) {
                 // look at the hiddenSelect and get value for option that matches...
                 // this is necessary in case option text and value differ
                 var pVal = jQuery('#' + key + ' > p').html();
                 componentOptions[componentId][key] =
                     jQuery('#' + key + '_hiddenSelect > option:contains("' + pVal + '")').val();
             }
       }
    });

    jQuery('.wfAdvOptMetadata').not('.ignorableOption').each(function(index, componentOption) {
     var key = jQuery(this).attr('id');

       if (key != "file" && jQuery('#' + key).length > 0) {
           var optItem = { key : jQuery('#' + key).val() };
             componentOptions[componentId][key] = jQuery('#' + key).val();
             if (jQuery('#' + key).is('div')) {
                 // look at the hiddenSelect and get value for option that matches...
                 // this is necessary in case option text and value differ
                 var pVal = jQuery('#' + key + ' > p').html();
                 componentOptions[componentId][key] =
                     jQuery('#' + key + '_hiddenSelect > option:contains("' + pVal + '")').val();
             }
       }
    });

    jQuery('.wfAdvOptMetadataMulti').not('.ignorableOption').each(function(index, componentOption) {
     var key = jQuery(this).attr('id');

       if (key != "file" && jQuery('#' + key).length > 0) {
           var optItem = { key : jQuery('#' + key).val() };
             componentOptions[componentId][key] = jQuery('#' + key).val();
             if (jQuery('#' + key).is('div')) {
                 // look at the hiddenSelect and get value for option that matches...
                 // this is necessary in case option text and value differ
                 var pVal = jQuery('#' + key + ' > p').html();
                 componentOptions[componentId][key] =
                     jQuery('#' + key + '_hiddenSelect > option:contains("' + pVal + '")').val();
             }
       }
    });
     setComponentOptionsForGLMPanel(componentId);
     var dsId = null;
     new Ajax.Request('WorkflowEditor', {
         parameters : {
             requestingMethod : 'WorkflowEditorServlet.updateDirtyBits',
             workflowId : currentDigraph.id,
             datasetId : dsId,
             dirtyBits: JSON.stringify(dirtyBits)
         },
         onComplete: function() {
             if (!workflowIsRunning) {
                 saveTemporaryWorkflowTransient(workflowId, null, componentIdNew, componentType, componentName);
             }
         },
         onException : function(request, exception) {
             wfHideStatusIndicator(); throw(exception);
         }
     });
}


/**********************************************************************************
 *   Run Workflow functions.
 *
 **********************************************************************************/

function runWorkflow() {
    jQuery('#wfMessageBarText').html('');
    var dsId = null;

    var hasComponents = false;
    if (currentDigraph != null && currentDigraph.hasOwnProperty('components')
            && currentDigraph.components !== undefined && currentDigraph.components.length > 0) {
        hasComponents = true;

    }

    if (hasComponents == false) {
        var dialogHtml = 'Your workflow is empty. <br/>'
                + 'A workflow must contain at least one component before it can be executed.';
        wfInfoDialog('noComponentsDialog', dialogHtml, 'Warning');
    } /*else if (areWorkflowConditionsMet == false) {
        var dialogHtml = 'Your workflow has errors. <br/>'
            + 'Please fix those highlighted issues before running your workflow.';
        wfInfoDialog('wfUnsavedDialog', dialogHtml, 'Error');

    }*/ else if (currentDigraph != null && currentDigraph.hasOwnProperty('id')) {
        if (currentDigraph.id != null) {
            // Disable the run button if no execution is pending

            //jQuery('#cancelWorkflowButton').button('enable');
            replaceExecutionButton('#wfExecutionDiv', 'Cancel', "cancelWorkflowButton", "wfRunButton", "ui-icon-wf-cancel-button");

            //jQuery('#wfRunButton').button('disable');


            if (isWorkflowSaved === false) {
                    saveAndRunCurrentWorkflow(currentDigraph.id);
            } else {
                if (workflowIsRunning) {
                    showCancelWarning();
                } else {
                    new Ajax.Request('WorkflowEditor', {
                        parameters : {
                            requestingMethod : 'WorkflowEditorServlet.runWorkflow',
                            workflowId : currentDigraph.id,
                            datasetId : dsId
                        },
                        onComplete : processRunResults,
                        beforeSend : markAsExecuting(),
                        onSuccess : wfHideStatusIndicator,
                        onException : function(request, exception) {
                            markAsStopped();
                            wfHideStatusIndicator(); throw(exception);
                        }
                    });
                }
            }
        }
    }
}

function loadPreviousResults() {
    if (currentDigraph != null && currentDigraph.hasOwnProperty('id') && !jQuery.isEmptyObject(currentDigraph.id)) {
        var dsId = null;
        new Ajax.Request('WorkflowResults', {
            parameters : {
                requestingMethod : 'WorkflowResultsServlet.displayPreviousResults',
                workflowId : currentDigraph.id,
                datasetId : dsId,
                editorWindow : true
            },
            onComplete : processRunResults,
            beforeSend : wfShowStatusIndicatorModal(false, "Loading..."),
            onSuccess : wfHideStatusIndicator,
            onException : function(request, exception) {
                markAsStopped();
                wfHideStatusIndicator(); throw(exception);
            }
        });
    }
}


function displayPreviousResults() {
    var dsId = null;
    if (currentDigraph != null && currentDigraph.hasOwnProperty('id') && !jQuery.isEmptyObject(componentOptions[this.component_id])) {
        new Ajax.Request('WorkflowResults', {
            parameters : {
                requestingMethod : 'WorkflowResultsServlet.displayPreviousResults',
                workflowId : currentDigraph.id,
                datasetId : dsId
            },
            onComplete : openResults,
            beforeSend : wfShowStatusIndicatorModal(false, "Loading..."),
            onSuccess : wfHideStatusIndicator,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
        });
    }
}

// Disable all text selection on the diagram, itself, and change pointers for mouse-over.
function addDiagramStyleOptions() {
    jQuery(function() {
        jQuery('._jsPlumb_overlay').css('cursor', 'default');
        jQuery('._jsPlumb_endpoint').css('cursor', 'pointer');

    });
}

//Shows a status indicator while waiting
function wfShowStatusIndicator(titleString) {

    // Status Indicator Popup
    if (titleString == null || titleString.trim() == '') {
        titleString = "Status";
    }

    jQuery('#componentStatusIcon').remove();
    jQuery('<div/>', {
        id : 'componentStatusIcon'
    }).appendTo('#wfStatusInfoDiv').html(
            '<span class="wfInfoLabel">Workflow Status: </span>' +
            '<span class="wfStatusLabel">' + titleString + '<img id="miniWaitingIcon" src="images/waiting.gif" />'
                + '</span>');

}

//Shows a status indicator while waiting
function wfShowStatusIndicatorModal(isBlocking, titleString) {
    if (jQuery('#statusIndicatorModal').length <= 0) {

      jQuery('body').off('click');
      jQuery('<div/>', {
          id : 'statusIndicatorModal'
      }).appendTo('body');

      // Status Indicator Popup
      jQuery('#statusIndicatorModal').html(
              '<center><img id="waitingIcon" src="images/waiting.gif" /></center>').dialog({
          closeOnEscape: false,
          open: function(event, ui) {
              //jQuery(".ui-dialog-titlebar-close", ui.dialog | ui).hide();
          },
          modal : isBlocking,
          autoOpen : false,
          title : titleString,
          width : 175,
          height : 175,
          close : function() {
              jQuery(this).remove();
          }
      });

      jQuery('#statusIndicatorModal').dialog('open');
      jQuery('#statusIndicatorModal').dialog("option", "stack", true);
      jQuery('#statusIndicatorModal').dialog("option", "resizable", false);


    }
}

/**
 * Shows a dialog with a progress bar on it.
 * @param isBlocking - a boolean for if the screen should be disabled or not while dialog is alive
 * @param titleString - displayed at the top of the dialog
 * @param bodyString - displayed in the body of the dialog above the progress bar
 * @param progressPercent - a number [0-100] indicating percent of progress
 * @returns
 */
function wfShowStatusIndicatorModalProgressBar(isBlocking, titleString, bodyString, progressPercent) {
    if (jQuery('#statusIndicatorModal').length <= 0) {

      jQuery('body').off('click');
      jQuery('<div/>', {
          id : 'statusIndicatorModal'
      }).appendTo('body');

      // Status Indicator Popup
      jQuery('#statusIndicatorModal').html(
              bodyString + '<center><div id="progressBar"><div id="progressAmount"></div></div></center>').dialog({
          closeOnEscape: false,
          open: function(event, ui) {
              //jQuery(".ui-dialog-titlebar-close", ui.dialog | ui).hide();
          },
          modal : isBlocking,
          autoOpen : false,
          title : titleString,
          width : 175,
          height : 175,
          close : function() {
              jQuery(this).remove();
          }
      });

      jQuery('#statusIndicatorModal').dialog('open');
      jQuery('#statusIndicatorModal').dialog("option", "stack", true);
      jQuery('#statusIndicatorModal').dialog("option", "resizable", false);
    }
}

/**
 * Update the progress bar with a new percent complete
 * @param progressPercent - a number [0-100] indicating percent of progress
 * @returns
 */
function wfUpdateStatusIndicatorModalProgressBar(progressPercent) {
    if (jQuery('#statusIndicatorModal').length > 0) {
      jQuery('#progressAmount').animate({'width' : progressPercent + '%'}, 100);
    }
}


//Hides that status indicator
function wfHideStatusIndicator() {

    jQuery('body').on('click');

    jQuery('#statusIndicatorModal').remove();

}

// Array Remove function - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
    var rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
};

function generateFileOptions(projects, datasets, files, fileId) {


    var projectAdded = false;
    var datasetAdded = false;
    var fileOptions = '';
    var hiddenInputs = '';
    for (var projectCount = 0; projectCount < projects.length; projectCount++) {
        var project = projects[projectCount];

        projectAdded = false;
        for (var datasetCount = 0; datasetCount < datasets.length; datasetCount++) {
            var dataset = datasets[datasetCount];

            datasetAdded = false;
            if (dataset.projectId == project.id && files.length > 0) {

                for (var fileCount = 0; fileCount < files.length; fileCount++) {
                    //alert(JSON.stringify(files[fileCount]));
                    var dsFile = files[fileCount];
                    if (dsFile.datasetId == dataset.id) {
                        if (!projectAdded) {
                            fileOptions = fileOptions
                                + '<optgroup title="'
                                + project.projectName + '" class="projectLabelInList" label="Project: ' + project.projectName + '">';
                            projectAdded = true;
                        }
                        if (!datasetAdded) {
                            fileOptions = fileOptions
                                + '<optgroup title="'
                                + dataset.datasetName + '" class="datasetLabelInList" label="Dataset: ' + dataset.datasetName + '">';
                            datasetAdded = true;
                        }

                        var optionClass = "";
                        if (dsFile.isFileTypeDescendant != "true") {
                            optionClass = 'dataHiddenFileOption';
                        } else {
                            optionClass = 'descendantFile';
                        }

                        var fileSelected = "";
                        if (fileId != null && fileId == dsFile.id) {
                            fileSelected = 'selected="selected"';
                        }

                        if (dsFile.fileName.length < maxFileNameLength) {
                            var fileSizeFormatted = formatBytes(dsFile.fileSize);
                            fileOptions = fileOptions + '<option  title="'
                            + dsFile.fileName + '" value="' + dsFile.id + '" class="'
                             + optionClass + '" ' + fileSelected + ' wfFileType="' + dsFile.wfFileType + '">'
                                + dsFile.fileName + ' (' + fileSizeFormatted + ')</option>';
                        } else {
                            var subLen = dsFile.fileName.length;

                            fileOptions = fileOptions + '<option title="'
                            + dsFile.fileName + '" value="' + dsFile.id + '" class="'
                            + optionClass + ' ' + fileSelected + ' wfFileType="' + dsFile.wfFileType + '">'
                                + dsFile.fileName.substr(0, 40) + '...'
                                + dsFile.fileName.substr(dsFile.fileName.length - 15, dsFile.fileName.length)
                                + ' (' + fileSizeFormatted + ')</option>';
                        }
                        hiddenInputs = hiddenInputs +
                            '<input type="hidden" id="fileDatasetId_' + dsFile.id + '" value="' + dataset.id + '"/>';
                    }
                }
                fileOptions = fileOptions + '</optgroup>';
                fileOptions = fileOptions + '</optgroup>';
            }
        }
    }

    return {
        fileOptions: fileOptions,
        hiddenInputs: hiddenInputs
    };
}


jQuery.fn.filterFilesByText = function(textbox) {
    this.each(function() {
        var options = [];
        var select = this;

        // I'm not sure this bit is needed now that the regex isn't done on the DOM...
        jQuery(select).children().each(function() {
            if (this.tagName !== undefined) {
                if (this.tagName.toLowerCase() == "option") {
                    options.push({value: jQuery(this).val(), text: jQuery(this).text()});
                } else if (this.tagName.toLowerCase() == "optgroup") {

                    var thisLabel = jQuery(this).attr("label");
                    options.push({label: thisLabel, text: jQuery(this).text()});

                    jQuery(this).children().each(function() {
                        if (this.tagName.toLowerCase() == "option") {
                            options.push({optgroup: thisLabel, value: jQuery(this).val(), text: jQuery(this).text()});
                        }
                    });

                }
            }
        });

        prevDatasetFiles = options;
        jQuery(select).data('options', options);

        jQuery(textbox).bind('keyup', function() {

            var optInner = jQuery(select).data('options');

            if (jQuery(textbox).val().trim() == "") {
                jQuery(select).empty();
                options = prevDatasetFiles;
            } else {
                jQuery(select).empty();
            }

            var search = jQuery.trim(jQuery(this).val());
            var regex = new RegExp(search,"gi");

            // Look for matches on filenames, dataset names and project names
            var matchingFiles = [];
            var fileCount = 0;
            jQuery.each(fileArray, function(i) {
                    var fileName = fileArray[i].fileName;
                    var datasetName = fileArray[i].datasetName;
                    var projectName = fileArray[i].projectName;
                    if ((fileName.match(regex) !== null)
                        || (datasetName.match(regex) !== null)
                        || (projectName.match(regex) !== null)) {
                        matchingFiles[fileCount] = fileArray[i];
                        fileCount++;
                    }
            });
            var gfoResult = generateFileOptions(projectArray, datasetArray, matchingFiles, null);
            var fileOptions = gfoResult.fileOptions;
            jQuery(select).append(jQuery(fileOptions));
            if (jQuery('#dataHiddenFileFilter').length > 0) {
                if (jQuery('#dataHiddenFileFilter').prop('checked') == true) {
                    jQuery('.dataHiddenFileOption').removeAttr('hidden');
                } else if (jQuery('#dataHiddenFileFilter').prop('checked') != true) {
                    jQuery('.dataHiddenFileOption').attr('hidden', 'true');
                }
            }
        });

    });
};

jQuery.fn.filterDatasetsByText = function(textbox) {
    this.each(function() {
        var options = [];
        var select = this;


        jQuery(select).children().each(function() {
            if (this.tagName !== undefined) {
                if (this.tagName.toLowerCase() == "option") {
                    options.push({value: jQuery(this).val(), text: jQuery(this).text()});
                } else if (this.tagName.toLowerCase() == "optgroup") {

                    var thisLabel = jQuery(this).attr("label");
                    options.push({label: thisLabel, text: jQuery(this).text()});

                    jQuery(this).children().each(function() {
                        if (this.tagName.toLowerCase() == "option") {
                            options.push({optgroup: thisLabel, value: jQuery(this).val(), text: jQuery(this).text()});
                        }
                    });

                }
            }
        });

        prevDatasets = options;
        jQuery(select).data('options', options);

        jQuery(textbox).bind('keyup', function() {

            var optInner = jQuery(select).data('options');

            if (jQuery(textbox).val().trim() == "") {
                jQuery(select).empty();
                options = prevDatasets;
            } else {
                jQuery(select).empty();
            }

            var search = jQuery.trim(jQuery(this).val());
            var regex = new RegExp(search,"gi");

            // Look for matches on dataset names and project names
            var matchingDatasets = [];
            var datasetCount = 0;
            jQuery.each(myDatasetArray, function(i) {
                    var datasetName = myDatasetArray[i].datasetName;
                    var projectName = myDatasetArray[i].projectName;
                    if (datasetName.match(regex) !== null
                        || (projectName.match(regex) !== null)) {
                        matchingDatasets[datasetCount] = myDatasetArray[i];
                        datasetCount++;
                    }
            });
            var datasetOptions = generateAttachableDatasetSelect(myProjectArray, matchingDatasets);
            jQuery(select).append(jQuery(datasetOptions));

        });
    });
};

// Listen to text option changes
function optionKeyListener(event) {
  var key = "";
  key = event.keyCode || event.which;
  var testEvent = (key === 13 || key === 9);

  var optDialogContainerId = jQuery(this).parent().parent().parent().parent().parent().parent().attr("id");
  if (!workflowIsRunning && optDialogContainerId !== undefined) {
      var splitString = optDialogContainerId.split('_');
      var componentId = splitString[1];

      // Do not auto-save or auto-validate on text-field option changes except for dirty bits
      dirtyOptionPane = true;
      isWorkflowSaved = false;
      dirtyBits[componentId] = DIRTY_OPTION;

      lastCall = "closeOptionDialog";

      saveOpenOptions();
      wfEnableSaveButton();
  }

  return false;
}

function changeSelectListener() {
    if (!workflowIsRunning) {

        if (jQuery(this).hasClass("wfAdvOpt")) {
            var optDialogContainerId = jQuery(this).parent().parent().parent().parent().parent().parent().attr("id");
            if (optDialogContainerId !== undefined) {

                // The option text has changed so remember to save it if we change focus or close the dialog
                var splitString = optDialogContainerId.split('_');
                var componentId = splitString[1];
                lastCall = "changeSelectListener";
                dirtyOptionPane = true;
                isWorkflowSaved = false;
                dirtyBits[componentId] = DIRTY_SELECTION;

                lastCall = "closeOptionDialog";
                saveOpenOptions();
                wfEnableSaveButton();
            }
        } else if (jQuery(this).hasClass("wfAdvOptMetadata")) {
            var optDialogContainerId = jQuery(this).parent().parent().parent().parent().parent().parent().attr("id");
            if (optDialogContainerId !== undefined) {
                // The option text has changed so remember to save it if we change focus or close the dialog
                var splitString = optDialogContainerId.split('_');
                var componentId = splitString[1];
                lastCall = "changeSelectListener";
                dirtyOptionPane = true;
                isWorkflowSaved = false;
                dirtyBits[componentId] = DIRTY_SELECTION;

                lastCall = "closeOptionDialog";
                saveOpenOptionsAndValidate();
                wfEnableSaveButton();
            }
        } else if (jQuery(this).hasClass("wfAdvOptMetadataMulti")) {
            var optDialogContainerId = jQuery(this).parent().parent().parent().parent().parent().parent().attr("id");
            if (optDialogContainerId !== undefined) {
                // The option text has changed so remember to save it if we change focus or close the dialog
                var splitString = optDialogContainerId.split('_');
                var componentId = splitString[1];
                lastCall = "changeSelectListener";
                dirtyOptionPane = true;
                isWorkflowSaved = false;
                dirtyBits[componentId] = DIRTY_SELECTION;

                lastCall = "closeOptionDialog";
                saveOpenOptionsAndValidate();
                wfEnableSaveButton();
            }
        }
    }
}


function optionKeyListenerLogger(event) {
    var optDialogContainerId = jQuery(this).parent().parent().parent().parent().parent().parent().attr("id");
    if (optDialogContainerId !== undefined) {
        var splitString = optDialogContainerId.split('_');
        var componentId = splitString[1];
        var isOptionChanged = true;
        if (componentOptions[componentId] !== undefined) {
            // e.g. [Analysis-1-x123456][model]
            if (componentOptions[componentId][jQuery(this).attr('id')] !== undefined) {
                if (jQuery(this).val() == componentOptions[componentId][jQuery(this).attr('id')]) {
                    isOptionChanged = false;
                }
            }
        }
    }
}



function getWorkflowStates() {
    var dsId = null;
    new Ajax.Request('LearnSphere', {
        parameters : {
            requestingMethod : 'ManageWorkflowsServlet.getWorkflowStates',
            datasetId : dsId
        },
        onComplete : processWorkflowStatusInfo,
        onException : function(request, exception) {
            wfHideStatusIndicator(); throw(exception);
        }
    });
}

function processWorkflowStatusInfo(transport) {
    if (transport !== undefined) {

        json = transport.responseText.evalJSON(true);

        if (json.success == true && json.workflows !== undefined && json.workflows != null) {
            jQuery.each(json.workflows, function(index, value) {
               jQuery('#wfStatus' + index).html(value);
            });
        }
    }
}


function getComponentStates() {
    //clearInterval(statusIndicatorTimer);

    var dsId = null;
    var workflowIdToQuery = currentDigraph.id;
    if (currentDigraph.id === undefined || currentDigraph.id === null
            || jQuery.isEmptyObject(currentDigraph.id)) {
        workflowIdToQuery = feedbackWorkflowId;
    }
    new Ajax.Request('WorkflowEditor', {
        parameters : {
            requestingMethod : 'WorkflowEditorServlet.getComponentStates',
            workflowId : workflowIdToQuery,
            datasetId : dsId
        },
        onComplete : processComponentStatusInfo,
        onException : function(request, exception) {
            //wfHideStatusIndicator();
            throw(exception);
        }
    });
}



function processComponentStatusInfo(transport) {
    if (!processComponentStatusInfoLocked) {
    //alert(isWorkflowSaved);
        if (isWorkflowSaved == false) {
            // Stop updating component status until saved
            markAsUnsaved();

            //return false;
        }

        // {"loggedOut":true,"success":true}
        if (transport !== undefined) {

            json = transport.responseText.evalJSON(true);
            //alert(JSON.stringify(json));

            var workflowRunFlag = false;
            jQuery('.process-component').each(function(component) {
                var currentComponentId = jQuery(this).attr('id');
                var found = false;
                var isRunButtonDisabled = false;
                if (json.componentStates !== undefined && json.componentStates != null) {

                    jQuery.each(json.componentStates, function(componentId, status) {

                       if (currentComponentId == componentId) {
                           found = true;
                           //alert(status + currentComponentId);
                           jQuery('#showWarnings_' + componentId).hide();
                           if (status == "new") {
                               jQuery('#componentStatus_' + componentId).text('Ready');
                               jQuery('#componentStatus_' + componentId).attr('class', 'componentStatusIcon_ready wfStatusIcon');
                               jQuery('#' + componentId + ' .openComponentDebugInfoIcon').hide();
                               isWorkflowExecutionPending = true;
                           } else if (status == "running") {
                               // Display any progress messages that the running component might have logged
                               updateComponentProgress(componentId);
                               // If the component is not displaying a progress message, display "Running"
                               if (jQuery('#componentStatus_' + componentId + ' .componentProgressMessage').length == 0) {
                                   jQuery('#componentStatus_' + componentId).text('Running');
                               }
                               jQuery('#componentStatus_' + componentId).attr('class', 'componentStatusIcon_running wfStatusIcon');
                               replaceExecutionButton('#wfExecutionDiv', 'Cancel', "cancelWorkflowButton", "wfRunButton", "ui-icon-wf-cancel-button");
                               jQuery('#' + componentId + ' .openComponentDebugInfoIcon').show();
                               workflowRunFlag = true;
                           } else if (status == "running_dirty") {
                               jQuery('#componentStatus_' + componentId).text('Stopping');
                               jQuery('#componentStatus_' + componentId).attr('class', 'componentStatusIcon_off wfStatusIcon');
                               jQuery('#' + componentId + ' .openComponentDebugInfoIcon').show();
                               workflowRunFlag = true;
                           } else if (status == "error") {
                               jQuery('#componentStatus_' + componentId).text('Error');
                               jQuery('#componentStatus_' + componentId).attr('class', 'componentStatusIcon_error wfStatusIcon');
                               jQuery('#' + componentId + ' .openComponentDebugInfoIcon').show();
                               isWorkflowExecutionPending = true;
                           } else if (status == "completed_warn") {
                               jQuery('#componentStatus_' + componentId).text('Completed');
                               jQuery('#componentStatus_' + componentId).attr('class', 'componentStatusIcon_warn wfStatusIcon');
                               jQuery('#' + componentId + ' .openComponentDebugInfoIcon').show();
                               jQuery('#' + componentId + ' .openComponentDebugInfoIcon').addClass('highlightDebugBtn');
                               jQuery('#' + componentId + ' .openComponentDebugInfoIcon').attr('title', "Has Warning(s)");
                               if (currentDigraph.isView !== undefined && !currentDigraph.isView) {
                                   jQuery('#showWarnings_' + componentId).remove();
                                   jQuery('#componentStatusDiv_' + componentId).append('<span class="showWarningsBtn" id="showWarnings_' + componentId + '">Warnings</span>');
                                   jQuery('#showWarnings_' + componentId).click(function() { showWarningsDialog(componentId); });
                               } else {
                                   jQuery('#componentStatus_' + componentId).text('Completed (warnings)');
                               }
                           }

                           if (status != "completed_warn") {
                               jQuery('#' + componentId + ' .openComponentDebugInfoIcon').removeClass('highlightDebugBtn');
                               jQuery('#' + componentId + ' .openComponentDebugInfoIcon').attr('title', "");
                           }
                       }
                    });

                    if (found == false) {
                        jQuery('#showWarnings_' + currentComponentId).hide();
                        jQuery('#' + currentComponentId + ' .openComponentDebugInfoIcon').removeClass('highlightDebugBtn');
                        jQuery('#' + currentComponentId + ' .openComponentDebugInfoIcon').attr('title', "");
                        jQuery('#componentStatus_' + currentComponentId).text('Completed');
                        jQuery('#componentStatus_' + currentComponentId).attr('class', 'componentStatusIcon_on wfStatusIcon');
                        jQuery('#' + currentComponentId + ' .openComponentDebugInfoIcon').show();
                    }

                    if (isWorkflowExecutionPending === true && !workflowRunFlag) {
                            if (isWorkflowSaved) {
                                jQuery('#wfRunButton .wfBtnText').html('Run');
                            } else {
                                jQuery('#wfRunButton .wfBtnText').html('Save<br/>& Run');
                                jQuery('#wfRunButton').button('enable');
                            }
                    }
                } else {
                    testLoggedOut(json);
                }

                if (json.componentWarnings !== undefined && json.componentWarnings != null) {

                    ///mck1b
                } else {

                }
            });

            if (!workflowRunFlag && !workflowRunDelayStarted) {
                workflowIsRunning = false;
            } else if (workflowRunFlag) {
                workflowIsRunning = true;
                isRunButtonDisabled = true;
                isWorkflowExecutionPending = false;
            }
        }
    }

}

function initComponentStates(transport1) {
    //clearInterval(statusIndicatorTimer);

    var dsId = null;
    var workflowIdToQuery = currentDigraph.id;
    if (currentDigraph.id === undefined || currentDigraph.id === null
            || jQuery.isEmptyObject(currentDigraph.id)) {
        workflowIdToQuery = feedbackWorkflowId;
    }
    new Ajax.Request('WorkflowEditor', {
        parameters : {
            requestingMethod : 'WorkflowEditorServlet.getComponentStates',
            workflowId : workflowIdToQuery,
            datasetId : dsId
        },
        onComplete : function(transport) { initComponentStatusInfo(transport1, transport); },
        onException : function(request, exception) {
            //wfHideStatusIndicator();
            throw(exception);
        }
    });
}

function initComponentStatusInfo(transport1, transport) {
    markAsStopped();
    //alert(isWorkflowSaved);
    if (isWorkflowSaved == false) {
        // Stop updating component status until saved
        markAsUnsaved();

        //return false;
    }

    if (transport !== undefined) {

        json = transport.responseText.evalJSON(true);
        //alert(JSON.stringify(json));

        // Put labels on import titles so you know what they're importing
        addLabelsToImportTitles(transport1);

        jQuery('.process-component').each(function(component) {
            var currentComponentId = jQuery(this).attr('id');
            var found = false;
            var isRunButtonDisabled = false;
            if (json.componentStates !== undefined && json.componentStates != null) {
                jQuery.each(json.componentStates, function(componentId, status) {

                   if (currentComponentId == componentId) {
                       found = true;
                       jQuery('#showWarnings_' + componentId).hide();
                       //alert(status + currentComponentId);
                       if (status == "new") {
                           jQuery('#componentStatus_' + componentId).text('Ready');
                           jQuery('#componentStatus_' + componentId).attr('class', 'componentStatusIcon_ready wfStatusIcon');
                           isWorkflowExecutionPending = true;
                       } else if (status == "running") {
                           jQuery('#componentStatus_' + componentId).text('Running');
                           jQuery('#componentStatus_' + componentId).attr('class', 'componentStatusIcon_running wfStatusIcon');
                           replaceExecutionButton('#wfExecutionDiv', 'Cancel', "cancelWorkflowButton", "wfRunButton", "ui-icon-wf-cancel-button");
                           isRunButtonDisabled = true;
                       } else if (status == "running_dirty") {
                           jQuery('#componentStatus_' + componentId).text('Stopping');
                           jQuery('#componentStatus_' + componentId).attr('class', 'componentStatusIcon_off wfStatusIcon');
                       } else if (status == "error") {
                           jQuery('#componentStatus_' + componentId).text('Error');
                           jQuery('#componentStatus_' + componentId).attr('class', 'componentStatusIcon_error wfStatusIcon');
                           isWorkflowExecutionPending = true;
                       } else if (status == "completed_warn") {
                           jQuery('#componentStatus_' + componentId).text('Completed');
                           jQuery('#componentStatus_' + componentId).attr('class', 'componentStatusIcon_warn wfStatusIcon');
                           jQuery('#' + componentId + ' .openComponentDebugInfoIcon').show();
                           jQuery('#' + componentId + ' .openComponentDebugInfoIcon').addClass('highlightDebugBtn');
                           jQuery('#' + componentId + ' .openComponentDebugInfoIcon').attr('title', "Has Warning(s)");
                           if (currentDigraph.isView !== undefined && !currentDigraph.isView) {
                               jQuery('#showWarnings_' + componentId).remove();
                               jQuery('#componentStatusDiv_' + componentId).append('<span class="showWarningsBtn" id="showWarnings_' + componentId + '">Warnings</span>');
                               jQuery('#showWarnings_' + componentId).click(function() { showWarningsDialog(componentId); });
                           } else {
                               jQuery('#componentStatus_' + componentId).text('Completed (warnings)');
                           }
                       }

                       if (status != "completed_warn") {
                           jQuery('#' + componentId + ' .openComponentDebugInfoIcon').removeClass('highlightDebugBtn');
                           jQuery('#' + componentId + ' .openComponentDebugInfoIcon').attr('title', "");
                       }
                   }
                });
                if (isRunButtonDisabled) {
                    isWorkflowExecutionPending = false;
                }
                if (found == false) {
                    jQuery('#showWarnings_' + currentComponentId).hide();
                    jQuery('#' + currentComponentId + ' .openComponentDebugInfoIcon').removeClass('highlightDebugBtn');
                    jQuery('#' + currentComponentId + ' .openComponentDebugInfoIcon').attr('title', "");
                    jQuery('#componentStatus_' + currentComponentId).text('Completed');
                    jQuery('#componentStatus_' + currentComponentId).attr('class', 'componentStatusIcon_on wfStatusIcon');
                }

                if (isWorkflowExecutionPending === true) {
                        if (isWorkflowSaved) {
                            jQuery('#wfRunButton .wfBtnText').html('Run');
                        } else {
                            jQuery('#wfRunButton .wfBtnText').html('Save<br/>& Run');
                            jQuery('#wfRunButton').button('enable');
                        }
                }
            } else {
                testLoggedOut(json);
            }
        });
        if (isWorkflowExecutionPending === true) {
            jQuery('#wfRunButton').button('enable');
        } else {
            jQuery('#wfRunButton').button('disable');
        }
    }
    var showResults = false;
    processWorkflowResults(showResults, transport1);
}


/**
* Add the label of the type of file the import component is importing to make it obvious
* from the workflow editor what kind of import it is.
* @param transport - from getting the component states after running/changing the workflow
*/
function addLabelsToImportTitles(transport) {
   // Ensure the output array exists
   let outputAr = [];
   try {
       let message = transport.responseJSON.message;
       let keys = Object.keys(message);
       keys.forEach(function(key) {
           let ar = message[key];
           if (!Array.isArray(ar)) {
               outputAr.push(ar);
           } else {
               ar.forEach(function(outputObj) {
                   outputAr.push(outputObj);
               });
           }
       });
   } catch (e) { console.log(e);}

   // Loop through output.  Make sure the import displays the proper output type
   outputAr.forEach(function(output) {
       let component_id = output.component_id;

       let filesAr = output.files;
       if (!Array.isArray(filesAr)) {
           filesAr = [filesAr];
       }

       if (filesAr != null && component_id != null) {
           filesAr.forEach(function(files){
               let keys = Object.keys(files);

               keys.forEach(function(key) {
                   let file = files[key];
                   let fileInd = parseInt(file.index);
                   let label = file.label;
                   if (fileInd != null && label != null) {
                       // If this is the output of an import component, add the type to the title
                       if (component_id.substring(0,5) == "Data-") {
                           let titleHtml = jQuery('<div />').addClass('importTypeTitle').text('Import');
                           titleHtml.append(jQuery('<span />').text('('+label+')').css('margin-left','5px').css('font-size','12px'));
                           jQuery('#' + component_id + ' .componentTitle').html(titleHtml);
                       }
                   }
               });
           });
       }
   });
}



/**********************************************************************************
*  Functions specifically for the JavaScript object, currentDigraph.
*
**********************************************************************************/

/**
 * Special stringify function for the digraph object.  Use this as opposed
 * to JSON.stringify() to avoid having elements like <1>,<2>etc. in the workflow
 * xml when the json arrays are converted to xml
 */
function stringifyDigraph(digraph) {
    return JSON.stringify(ensureMetadataHeadersAreArray(digraph));
}

/**
 * There was a bug that would make the headers appear to be an object rather than an
 * array when stringifying leading to <1>, <2> elements in xml.  This ensures that they
 * are arrays.
 * @return digraph object with correct format for headers
 */
function ensureMetadataHeadersAreArray(digraph) {
    let components = digraph.components;
    if (components === undefined) {
        return digraph;
    }
    // Loop through Import components
    for (let i = 0; i < components.length; i++) {
        let component = components[i];
        if (component != undefined) {
            if (component.component_name == "Import") {
                if (component.options === undefined) {
                    continue;
                } else if (component.options.files === undefined) {
                    continue;
                }
                let files = component.options.files;
                let fileKeys = Object.keys(files);
                for (let j = 0; j < fileKeys.length; j++) {
                    let fileKey = fileKeys[j];
                    let file = files[fileKey];
                    if (file != null) {
                        let metadata = file.metadata;
                        if (metadata != undefined) {
                            let header = metadata.header;
                            if (header != undefined) {
                                // If they have a header metadata element, ensure that it is an array
                                // by making a new array.  Seems redundant, but it is somehow necessary
                                let newHeaderAr = [];
                                for (let k = 0; k < header.length; k++) {
                                    newHeaderAr.push(header[k]);
                                }
                                delete digraph.components[i].options.files[fileKey].metadata;
                                digraph.components[i].options.files[fileKey].metadata = {};
                                digraph.components[i].options.files[fileKey].metadata.header = newHeaderAr;
                            }
                        }
                    }
                }
            }
        }
    }
    return digraph;
}

/**
 * Save the component positions to the componentOptions and currentDigraph objects
 */
function updateComponentPositions() {
    // The last call was moving the component.
    if (lastCall == 'processComponent.mouseup') {

        // You need the scale of the workspace incase the user zoomed
        var workspaceDiv = jQuery('#process-div')[0];
        var scale = workspaceDiv.getBoundingClientRect().width / workspaceDiv.offsetWidth;

        if (jQuery('.process-component').length > 0) {
            jQuery('.process-component').each(function(index, value) {

                // Node position info.
                var divPosition = jQuery(value).position();
                var id = jQuery(value).attr('id');

                var draggableParent = jQuery('#' + id);
                var compName = jQuery('#' + id + '  .compName').text();
                componentOptions[id].top = Math.round(parseFloat(divPosition.top) / scale);
                componentOptions[id].left = Math.round(parseFloat(divPosition.left) / scale);
            });
        }
        if (currentDigraph.components != null && currentDigraph.components.length > 0) {
            jQuery.each(currentDigraph.components, function(componentInd, component) {
                if (component != undefined && component != null) {
                    component.top = componentOptions[component.component_id].top;
                    component.left = componentOptions[component.component_id].left;
                }
            });
        }
    }
}

function updateDigraphBeforeSaving(workflowId) {

 // Delete object if it's empty.
 if (currentDigraph.hasOwnProperty('components')
         && currentDigraph.components !== undefined
         && currentDigraph.components.length == 0) {
     //alert("delete components");
     delete currentDigraph.components;
 }


 // Populate component options using the global variable, componentOptions.
 if (currentDigraph.components != null && currentDigraph.components.length > 0) {
     jQuery.each(currentDigraph.components, function() {
         var cId = this.component_id;
         if (componentOptions[this.component_id] !== undefined && componentOptions[this.component_id] != null
                 && !jQuery.isEmptyObject(componentOptions[this.component_id])) {
             //alert('OPTIONco(' + this.component_id + '): ' + JSON.stringify(componentOptions[this.component_id]));
             //alert('OPTIONdig(' + this.component_id + '): ' + JSON.stringify(this.options));
             //alert("Updating digraph options: " + this.component_id + " " + JSON.stringify(componentOptions[this.component_id]));
             this.options = componentOptions[this.component_id];
         }

     });
 }

}

function initCreateButton(isEnabled) {

 jQuery('#createNewWorkflowButton').button({
     label : "Create new workflow",
     classes: {
            "ui-button": "create-button-class"
     }
 });
 if (!isEnabled) {
     jQuery('#createNewWorkflowButton').button('disable');
 } else {
     jQuery('#createNewWorkflowButton').click(populateCreateNewWorkflowDiv);
 }

 // Sorts columns based on user-selected column header
 jQuery(".wfSortByColumn").click(wfSelectGlobalSort);
 jQuery(".wfMySortByColumn").click(wfSelectMySort);
}

function setOutsideOptionPanelClick(componentId) {
    jQuery('body').off('click');
    jQuery('body').click(function(evt){
        if (jQuery('#' + 'advancedOptionsDialog_' + componentId).length > 0
                && jQuery('#' + 'advancedOptionsDialog_' + componentId).has(evt.target).length <= 0
                && jQuery('#' + 'wfRunButton').has(evt.target).length <= 0) {
            jQuery('.advOptDialogClass').dialog('close');
            jQuery('.selectNonModal').dialog('close');
        }
        if (jQuery('.componentOutputDataPreviewDiv').length > 0
                && jQuery('.componentOutputDataPreviewDiv').has(evt.target).length <= 0) {
            jQuery('#componentOutputDataPreview').dialog('close');
        }
    });
}

function testLoggedOut(json) {
    if (json != null && json.loggedOut != null && json.loggedOut !== undefined
             && json.loggedOut === true) {
        if (jQuery('#loggedOutDialog').length == 0) {
            clearInterval(globalDebugWindowTimer);
            clearInterval(globalWorkflowTimer);
            clearInterval(statusIndicatorTimer);
            wfInfoDialog('loggedOutDialog',
                'Your session has timed out. Please <u><a href="login">Log in</a></u> to continue.',
                    'Login Session Timed Out');
        }
    }
}

function formatBytes(bytes) {
    if(bytes < 1024) return bytes + " Bytes";
    else if(bytes < 1048576) return(bytes / 1024).toFixed(0) + " KB";
    else if(bytes < 1073741824) return(bytes / 1048576).toFixed(0) + " MB";
    else return(bytes / 1073741824).toFixed(2) + " GB";
}

function htmlEncode(str) {
    if (str !== undefined && str != null) {
        return str.toString()
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
    } else {
        return str;
    }
 }

function htmlDecode(str) {
    if (str !== undefined && str != null) {
        return str.toString()
        .replace(/&amp;/g, "&")
        .replace(/&lt;/g, "<")
        .replace(/&gt;/g, ">")
        .replace(/&quot;/g, "\"")
        .replace(/&#039/g, "'");
    } else {
        return str;
    }
 }

function showWarningsDialog(componentId) {
    if (!confirmedSensitiveDataWarning) {
    jQuery('<div />', {
        id : 'wfDeleteComponentConfirmationDialog'
    }).html(
        'Debugging logs may contain sensitive or private information '
            + ' generated by your data.')
    .dialog({
        open : function() {
            jQuery('.ui-button').focus();
        },
        autoOpen : true,
        autoResize : true,
        resizable : false,
        width : 400,
        height : "auto",
        title : 'Warning: Sensitive Data',
        buttons : {
            'Cancel' : function() {
                jQuery(this).dialog('close');
            },
            'OK' : function() {
                confirmedSensitiveDataWarning = true;
                new Ajax.Request('WorkflowEditor', {
                    parameters : {
                        requestingMethod: 'WorkflowEditorServlet.requestDebugInfo',
                        workflowId : workflowId,
                        componentId : componentId,
                        textFilter : '(?i).*WARN[a-zA-Z]*:.*'
                    },
                    onComplete : retrieveDebugWarnings
                });
                jQuery(this).dialog('close');
            }
        },
        close : function() {
            jQuery(this).remove();
        }
    });
    } else {
        new Ajax.Request('WorkflowEditor', {
            parameters : {
                requestingMethod: 'WorkflowEditorServlet.requestDebugInfo',
                workflowId : workflowId,
                componentId : componentId,
                textFilter : '(?i).*WARN[a-zA-Z]*:.*'
            },
            onComplete : retrieveDebugWarnings
        });
    }

}


function retrieveDebugWarnings(transport) {

    var json = transport.responseText.evalJSON(true);

    if (jQuery('#debugMiniWaitingIcon').length > 0) {
        jQuery('#debugMiniWaitingIcon').remove();
    }

    if (json != null && json.error_flag !== undefined && json.error_flag == "true") {
        wfTimerDialog("Error", "Debug info not found: " + json.message, lsWorkflowListDialogTime);
    } else if (json != null && json.success !== undefined) {

        var textAsString = "";
        if (json.textData !== undefined && json.textData != null) {

            textAsString = getDebugInfoAsString(json);
        }
        var windowWidth = jQuery(window).width();
        var maxWidth = windowWidth * 0.9;
        var windowHeight = jQuery(window).height();
        var maxHeight = windowHeight * 0.9;

        // If lineCount is 0, NO_DEBUG_LOG_TEXT doesn't need a big window.
        if (textAsString == NO_DEBUG_LOG_TEXT) {
            maxWidth = 407;
            maxHeight = 122;
        }

        var divId = 'debugInfoDiv';
        // The dialog exists, so just refresh the text.
        if (jQuery('#' + divId).length > 0) {
            // Refresh existing dialog text.
            jQuery('.debugInfoText').html(textAsString);
            setDebugInfoEventTriggers();
        } else {
            var debugInfoHeader = '<div id="debugFilterTextDiv">Search: '
                + '<input title="Use plain text or regular expressions to search the log files." '
                    + 'type="text" id="debugFilterText" /></div>';
            // Open a new dialog with the debug info.
            clearInterval(globalDebugWindowTimer);
            jQuery('<div id=\'' + divId + '\'><div class="debugInfoHeader">' + debugInfoHeader + '</div>'
                    + '<div class="debugInfoText">'
                        + textAsString
                        + '</div></div>')
                .dialog({
                resizable : true,
                width : maxWidth,
                height : maxHeight,
                modal : true,
                title : "Debugging Info (" + json.componentId + ")",
                open : function() {
                    jQuery('#debugFilterText').val('WARN[a-zA-Z]*:');
                    setDebugInfoEventTriggers();

                    jQuery('#debugFilterText').on('input',function(e) {
                        jQuery('#debugMiniWaitingIcon').remove();
                        jQuery('#debugFilterTextDiv').append(''
                            + ' <img id="debugMiniWaitingIcon" src="images/waiting.gif" />'
                                + '');
                    });
                    // Refresh the text by calling requestDebugInfo every globalDebugWindowTimerRefresh seconds
                    globalDebugWindowTimer = setInterval(function() {
                        requestDebugInfo(json.componentId);
                    }, globalDebugWindowTimerRefresh);
                },
                close : function() {
                    // Clear the interval timer and remove the div.
                    clearInterval(globalDebugWindowTimer);
                    jQuery(this).remove();
                }
            });
        }
    }
}


