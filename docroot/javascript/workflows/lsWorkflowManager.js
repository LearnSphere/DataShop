

//Delete workflow dialog.
function wfDeleteWorkflow(workflowId) {

 if (workflowId != null) {
     var workflowName = jQuery('#workflowNameInput_' + workflowId).val();

     jQuery('<div />', {
         id : 'wfDeleteConfirmationDialog'
     }).html(
             'Are you sure you want to delete the workflow "' + workflowName
                     + '"?<br />Deleted workflows cannot be recovered.').dialog({
         open : function() {
             jQuery('.ui-button').focus();
         },
         autoOpen : true,
         autoResize : true,
         resizable : false,
         width : 600,
         height : 245,
         modal : true,
         title : 'Delete Workflow',
         buttons : {
             'Yes' : function() {
                 wfConfirmDeleteWorkflow(workflowId);
                 jQuery(this).dialog('close');
             },
             'No' : function() {
                 jQuery(this).dialog('close');
             }
         },
         close : function() {
             jQuery(this).remove();
         }
     });

 } else {
     alert('Workflow id cannot be null.');
 }
}

//Delete the workflow confirmation.
function wfConfirmDeleteWorkflow(workflowId) {
 var dsId = null;
 if (jQuery('#datasetId') !== undefined && jQuery('#datasetId').val() != '') {
     dsId = jQuery('#datasetId').val();
 }

 if (workflowId != null) {
     new Ajax.Request('LearnSphere', {
         parameters : {
             requestingMethod : 'ManageWorkflowsServlet.deleteWorkflow',
             workflowId : workflowId,
             datasetId : dsId
         },
         onComplete : workflowDeleted,
         beforeSend : wfShowStatusIndicatorModal(false, "Deleting..."),
         onSuccess : wfHideStatusIndicator,
         onException : function(request, exception) {
             wfHideStatusIndicator(); throw(exception);
         }
     });

 } else {
     alert('Workflow id cannot be null.');
 }
}

//After deleting a workflow, reload the workflows page.
function workflowDeleted(transport) {
     var workflowAsJson = null;
     var json = null;
     if (transport !== undefined) {
         json = transport.responseText.evalJSON(true);
         var searchFilters = {};
         var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");
         fetchWorkflowRows(panelId, null, null);
    }
}

function createWfOnEnter(event) {
    var key = "";
    key = event.keyCode || event.which;
    if (key === 13) {
        wfShowStatusIndicatorModal(false, "Loading...");
        wfSubmitNewWorkflow(jQuery('#newWorkflowId').val(),
            jQuery('#newWorkflowName').val(),
            jQuery('#moveFolder option:selected').val(),
            jQuery('#newWorkflowDescription').val(),
            jQuery('input:radio[name|="isSharedRadioButton"]:checked').val(),
            jQuery('#newFolderName').val(),
            getWorkflowTags(false));
        jQuery('.ui-dialog').dialog('close');
    }

    // For other keyboard events, enable the Save button once name is set.
    if (jQuery('#newWorkflowName').val().length > 0) {
        jQuery('#new-wf-save-button').button('enable');
    } else {
        jQuery('#new-wf-save-button').button('disable');
    }
}

//The new workflow dialog (triggered when the "Create new workflow" button is clicked).
function createNewWorkflowPrompt(parentDiv, workflowName, workflowDescription, isShared, transport) {

    //     jQuery('<br/>').appendTo(parentDiv);

     // Workflow Name
     jQuery(
             '<div id="workflowNameDiv">'
             + 'Name:<br />'
                     + '<input type="text" id="newWorkflowName" name="newWorkflowName" class="hasPlaceholder"'
                         + ' size="50" title="Enter a new workflow name" maxlength="100" placeholder="Enter a name (max 100 characters)" />'
                     + '</div>').appendTo(parentDiv);
     jQuery('<br/>').appendTo(parentDiv);

     // Folder select box
     if (transport !== undefined && transport.responseText !== undefined) {
         json = transport.responseText.evalJSON(true);
         if (json.success !== undefined && json.success == true) {
              jQuery(getMyFoldersHtml(json)).appendTo(parentDiv);

         }
     }

     // Description
     jQuery(
             '<div id="workflowDescriptionDiv">'
             + 'Description:<br />'
             + '<textArea id="newWorkflowDescription" name="newWorkflowDescription" class="hasPlaceholder"'
             + ' cols="50" rows="5" maxlength="255" placeholder="Enter a description (max 255 characters)"/>'
             + '</div>').appendTo(parentDiv);

     jQuery(getAddTagsHtml(null, false)).appendTo(parentDiv);

     jQuery('<br/>').appendTo(parentDiv);


     // Public or Private
     var isSharedChecked = '';
     if (isShared == 'true') {
         isSharedChecked = 'checked="true"';
     } else {
         isSharedChecked = '';
     }

     jQuery(
         '<div id="isSharedOption">'
         + '<input type="checkbox" id="isSharedRadioId" name="isSharedRadioButton" value="true" '
         + isSharedChecked
         + ' />'
         + '<label class="optionLabel" for="isSharedRadioId"> Public?</label></div>'
         + '</div>').appendTo(parentDiv);

}

function initWorkflowPrivacyCheckbox() {

    var options = new Array();
    var sharedOptionId = 'isSharedOption';

    options['extraClasses'] = 'wfTooltipDiv';
    options['timeout'] = '100000';
    options['delay'] = '200';

    if (jQuery('#' + sharedOptionId).length > 0) {
        new ToolTip(sharedOptionId, 'A "Public" workflow can be viewed by all users, '
            + 'but only those with access to the data contained in the workflow can view the data or results.',
                options);
    }
}

function initRenameComponentToolTip(componentId) {
    var options = new Array();

    options['extraClasses'] = 'wfTooltipDiv';
    options['timeout'] = '100000';
    options['delay'] = '400';
    options['style'] = 'width: 10em;';

    if (jQuery('#'+componentId+' .renameWorkflowIcon').length > 0) {
        new ToolTip('renameIcon_'+componentId, 'Click to rename this component',
                options);
    }
}

//The feedback dialog (triggered when the Feedback button is clicked).
function createFeedbackPrompt() {
    var parentDiv = jQuery('<div />', {
         id : 'createFeedback'
     });
    parentDiv.appendTo('body');

     jQuery('<div id="workflowFeedbackDiv">'
            + "<p>Questions or comments? Features you'd like to see? Use this form to send your feedback to the developers.</p><p>We'd love to hear what you think and we'll get back to you as soon as we can.</p>"
            + "<p>Feedback:</p>"
            + '<textarea id="workflowFeedback" name="workflowFeedback"'
            + ' cols="50" rows="10" class="feedbackTxt"/>'
            + '</div>').appendTo(parentDiv);

     jQuery('<div id="feedbackLen">' + MAX_FEEDBACK_LENGTH +
            ' characters remaining</div>').appendTo(parentDiv);

     jQuery("textarea#workflowFeedback").keyup(feedbackModify);

     parentDiv.dialog({
         open : function() {
             jQuery('.ui-button').focus();
         },
         autoResize : true,
         resizable : false,
         width : 615,
         height : 'auto',
         modal : true,
         title : 'Feedback',
         buttons : [ {
             id : "feedback-send-button",
             text : 'Send',
             click : function() {
                 submitUserFeedback(jQuery('#workflowFeedback').val());
                 jQuery(this).dialog('close');
             }
         }, {
             id : "feedback-close-button",
             text : 'Close',
             click : function() {
                 jQuery(this).dialog('close');
             }
         } ],
         close : function() {
                 jQuery(this).remove();
         }
     });

    // Initially, button is disabled.
    jQuery("#feedback-send-button").button('disable');

    parentDiv.dialog('open');
}

var MAX_FEEDBACK_LENGTH = 2000;

function feedbackModify() {
    var enableFlag = false;

    var feedbackFieldLen = jQuery("textarea#workflowFeedback").val().length;

    // Feedback must be specified...
    if (feedbackFieldLen > 0) {
        enableFlag = true;
    }

    var charsLeft = MAX_FEEDBACK_LENGTH - jQuery("textarea#workflowFeedback").val().length;

    // ... and less than MAX_FEEDBACK_LENGTH characters.
    if (feedbackFieldLen > MAX_FEEDBACK_LENGTH) {
        jQuery("div#feedbackLen").css("color", "red");
        enableFlag = false;

        // Update the chars remaining
        charsLeft = Math.abs(charsLeft);
        jQuery("div#feedbackLen").html(charsLeft + " too many characters");
    } else {
        jQuery("div#feedbackLen").css("color", "black");
        enableFlag = enableFlag && true;

        // Update the chars remaining
        jQuery("div#feedbackLen").html(charsLeft + " characters remaining");
    }


    if (enableFlag) {
        jQuery("#feedback-send-button").button('enable');
    } else {
        jQuery("#feedback-send-button").button('disable');
    }
}

function submitUserFeedback(feedback) {

    var dsId = null;
    if (jQuery('#datasetId') !== undefined && jQuery('#datasetId').val() != '') {
        dsId = jQuery('#datasetId').val();
    }

    var wfId = null;
    if (currentDigraph !== undefined) { wfId = currentDigraph.id; }

    new Ajax.Request('WorkflowUserFeedback', {
            parameters : {
                requestingMethod : 'submitUserFeedback',
                datasetId : dsId,
                workflowId : wfId,
                feedback : feedback
            },
            onComplete : showThankYou,
            onException : function(request, exception) {
                throw (exception);
            }
    });
}

function showThankYou(transport) {
    json = transport.responseText.evalJSON(true);

    feedback = json.feedback;
    date = json.date;

    var parentDiv = jQuery('<div />', {
         id : 'feedbackTy'
     });

     jQuery('<div id="thankYouDiv">'
            + "<p>We appreciate your feedback.</p>"
            + "<p id='tyDate'>Received on:" + date + "</p>"
            + "<p id='tyFeedback'>" + feedback + "</p>"
            + '</div>').appendTo(parentDiv);

     parentDiv.dialog({
         open : function() {
             jQuery('.ui-button').focus();
         },
         autoOpen : true,
         autoResize : true,
         resizable : false,
         width : 540,
         height : 'auto',
         modal : true,
         title : 'Thank you',
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

//Query the database to get info for an existing workflow so that
//we can populate the Save as New workflow dialog.
function wfSaveAsNewWorkflow(workflowId) {


  var datasetIdParam = '';
  var dsId = getParam('datasetId', window.location.href);
  if (dsId != null && dsId.trim() != '') {
      datasetIdParam = '&datasetId=' + dsId;
  }

 if (workflowId != null) {
     var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");

     new Ajax.Request('LearnSphere', {
         parameters : {
             requestingMethod : 'ManageWorkflowsServlet.fetchMyFolders',
             workflowId : workflowId,
             datasetId : dsId,
             panelId : panelId
         },
         onComplete : populateSaveAsNewWorkflowDiv,
         beforeSend : wfShowStatusIndicatorModal(false, "Loading..."),
         onSuccess : wfHideStatusIndicator,
         onException : function(request, exception) {
             wfHideStatusIndicator(); throw(exception);
         }
     });

 } else {
     alert('Workflow id cannot be null.');
 }
}

var openDigraph;

// 'Save As' version used from within open WF.
function saveAsNewWorkflow(workflowId) {

    // Make note of currentDigraph...
    openDigraph = currentDigraph;

    wfSaveAsNewWorkflow(workflowId);
}

//After the user clicks the Save as New Workflow icon, populate the new workflow div
//using some values from the existing workflow.
function populateSaveAsNewWorkflowDiv(transport) {
 var workflowAsJson = null;
 var json = null;
 if (transport !== undefined) {
     json = transport.responseText.evalJSON(true);
     if (json.workflowId !== undefined && json.workflowId != null) {
         var workflowId = json.workflowId;
         if (jQuery('#newWorkflowId').length > 0) {
             jQuery('#newWorkflowId').val(json.workflowId);
         }
         var lastUpdated = json.lastUpdated;
         var isShared = json.isShared;

         var parentDiv = jQuery('<div><input type="hidden" id="newWorkflowId" name="newWorkflowId" value="'
                 + workflowId + '" /></div>', {
             id : 'saveAsNewWorkflowDiv'
         });

         createNewWorkflowPrompt(parentDiv, '', '', isShared, transport);

         parentDiv.dialog({
             open : function() {
                 jQuery('#newWorkflowName').focus();
                 jQuery('#newWorkflowName').keyup(createWfOnEnter);
                 jQuery('#moveFolder').change(createNewFolderInput);
                 initWorkflowPrivacyCheckbox();
                 initWorflowTagFunctionality(false);
             },
             autoOpen : true,
             autoResize : true,
             resizable : false,
             width : 600,
             height : 'auto',
             modal : true,
             title : 'Save As New Workflow',
             buttons : [ {
                 id: 'new-wf-save-button',
                 text : 'Save As New',
                 click : function() {
                     wfShowStatusIndicatorModal(false, "Loading...");
                     wfSubmitNewWorkflow(workflowId,
                         jQuery('#newWorkflowName').val(),
                         jQuery('#moveFolder option:selected').val(),
                         jQuery('#newWorkflowDescription').val(),
                         jQuery('input:checkbox[name="isSharedRadioButton"]').is(':checked'),
                         jQuery('#newFolderName').val(),
                         getWorkflowTags(false));
                     // update newly-created WF with current UI state
                     jQuery(this).dialog('close');
             } } , {
                 id: 'new-wf-close-button',
                 text : 'Close',
                 click : function() {
                     jQuery(this).dialog('close');
             } } ]
             ,
             close : function() {
                 jQuery(this).remove();
             }
         });

         jQuery('#new-wf-save-button').button('disable');
     } else {
         testLoggedOut(json);
     }
 }

}

//Submit a new workflow to be created.
function wfSubmitNewWorkflow(workflowId, workflowName, workflowFolderId,
        workflowDescription, isShared, newFolderName, workflowTags) {

 if (workflowName.trim().length > 100) {
     wfInfoDialog("wfNameTooLongId",
             'Workflow name exceeds limit.',
             "Workflow Name is Too Long");
     return;
 }

 var dsId = null;

 var datasetIdParam = getParam('datasetId', window.location.href);
 if (datasetIdParam != null && datasetIdParam.trim() != '') {
     dsId = datasetIdParam;
 }

 // Html encode the name so it is xml compatible (for characters such as & and <)
 workflowName = workflowName;
 workflowDescription = workflowDescription;
 var digraphInput = null;
 // If we're save a new workflow, don't use the current digraph
 if (openDigraph !== undefined) {
     digraphInput = jQuery('<input type="hidden" name="digraphObject" />');
     digraphInput.val(JSON.stringify(openDigraph));
     var test1 = digraphInput.val();
     var test2;
 }
 new Ajax.Request('LearnSphere', {
     parameters : {
         requestingMethod: 'ManageWorkflowsServlet.saveAsNewWorkflow',
         workflowId: workflowId,
         workflowName: workflowName,
         folderId: workflowFolderId,
         newFolderName: newFolderName,
         workflowDescription: workflowDescription,
         workflowTags: getWorkflowTags(false),
         isShared: isShared,
         isSaveAsNew: 'true',
         datasetId: dsId,
         digraphObject: digraphInput

     },
     onComplete : function(transport) {
         var wfSuccess = false;
         if (transport !== undefined && transport.responseText !== undefined) {
            json = transport.responseText.evalJSON(true);

            var regex = new RegExp("[0-9]+","g");

            if (json.workflowId !== undefined && json.workflowId.toString().match(regex) !== null) {
                wfSuccess = true;
                var dsParam = '';
                if (dsId != null) {
                    dsParam = "&datasetId=" + dsId;
                }

                var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");
                fetchWorkflowRows(panelId, null, null);

                // Open new workflow in new window
                window.location.href = "LearnSphere?workflowId=" + json.workflowId + dsParam;
            }
         }

         if (!wfSuccess) {
             wfTimerDialog("Failure", json.message, lsWorkflowListDialogTime);
         }
     },
     beforeSend : wfShowStatusIndicator("Creating..."),
     onSuccess : wfHideStatusIndicator,
     onException : function(request, exception) {
         wfHideStatusIndicator(); throw(exception);
     }
 });



  // Reset 'openDigraph' variable
  openDigraph = null;
}

function populateCreateNewWorkflowDiv() {
    var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");
    new Ajax.Request('LearnSphere', {
        parameters : {
            requestingMethod: 'ManageWorkflowsServlet.fetchMyFolders',
            workflowId: null,
            folderId: null,
            panelId: panelId
        },
        onComplete : createNewWorkflow,
        beforeSend : wfShowStatusIndicator("Creating..."),
        onSuccess : wfHideStatusIndicator,
        onException : function(request, exception) {
            wfHideStatusIndicator(); throw(exception);
        }
    });
}

//Call the servlet to create a brand new workflow (used by Create new, as well
//as Save as New).

function createNewWorkflow(transport) {
 var parentDiv = jQuery('<div />', {
     id : 'createNewWorkflowDiv'
 });
 var folderId = null;

 createNewWorkflowPrompt(parentDiv, '', '', false, transport);
 parentDiv.dialog({
     open : function() {
         jQuery('#newWorkflowName').focus();
         jQuery('#newWorkflowName').keyup(createWfOnEnter);
         jQuery('#moveFolder').change(createNewFolderInput);
         initWorkflowPrivacyCheckbox();
         initWorflowTagFunctionality(false);
     },
     autoOpen : true,
     autoResize : true,
     resizable : false,
     width : 600,
     height : 'auto',
     modal : true,
     title : 'Create Workflow',
     buttons : [ {
         id: 'new-wf-save-button',
         text : 'Save',
         click : function() {
             wfSubmitNewWorkflow(null,
                 jQuery('#newWorkflowName').val(),
                 jQuery('#moveFolder option:selected').val(),
                 jQuery('#newWorkflowDescription').val(),
                 jQuery('input:checkbox[name="isSharedRadioButton"]').is(':checked'),
                 jQuery('#newFolderName').val(),
                 getWorkflowTags(false));
             jQuery(this).dialog('close');
         } } , {
         id: 'new-wf-close-button',
         text : 'Close',
         click : function() {
             jQuery(this).dialog('close');
         } } ]
     ,
     close : function() {
         jQuery(this).remove();
     }
 });

    jQuery('#new-wf-save-button').button('disable');
}

// Support renaming of workflow
function wfModifyWorkflowSettings(workflowId, isView) {

    var divTitle = 'Modify';
    var readOnly = '';
    var divDescLimit = '<div id="wfDescriptionMaxLen">Enter no more than 255 characters.</div>';
    var killClick = '';

    if (isView == "true") {
        // Not My Workflows
        divTitle = "Details";
        readOnly = ' readonly ';
        divDescLimit = '';
        killClick = ' onclick="return false;" ';
    }
    // Get current Values NOT FROM GLOBAL BUT ON THE FLY
    var cName = jQuery('#workflowNameInput_'+workflowId).val().trim();
    // Need to use .contents().get(0).nodeValue to avoid getting the tag values in the description
    var cDescription = jQuery('#workflowNameDiv_'+workflowId+' .wfDes .tooltiptext').attr('value').trim();
    var cShared = jQuery('#workflowGlobalInput_'+workflowId).val();

    var parentDiv = jQuery('<div />', {
        id : 'modifyTitleDes'
    });

    parentDiv.appendTo('body');

    jQuery('<div id="workflowNameDiv">'
             + 'Name:<br />'
             + '<input type="text" id="newWFName_' + workflowId + '" name="newWorkflowName"'
             + ' size="50" title="Enter a new workflow name" maxlength="100" value="'
             + htmlEncode(cName) + '"' + readOnly + '>'
             + '</div>').appendTo(parentDiv);
     jQuery('<br/>').appendTo(parentDiv);

     jQuery('<div id="workflowDescriptionDiv">'
             + 'Description:<br />'
             + '<textArea id="newWFDescription_' + workflowId + '" name="newWorkflowDescription"'
             + ' cols="50" rows="5" maxlength="255"' + readOnly + '>'
             + htmlEncode(cDescription)
             + '</textarea>'
         + divDescLimit
             + '</div>').appendTo(parentDiv);
     jQuery('<br/>').appendTo(parentDiv);

     jQuery(getAddTagsHtml(workflowId, false)).appendTo(parentDiv);

     var isSharedChecked = '';
     if (cShared == 'true') {
         isSharedChecked = 'checked="true"';
     } else {
         isSharedChecked = '';
     }

     jQuery(
         '<div id="isSharedOption">'
         + '<input type="checkbox" id="isSharedRadioId" name="isSharedRadioButton" value="true" '
         + isSharedChecked
         + '' + killClick + ' />'
         + '<label class="optionLabel" for="isSharedRadioId"> Public?</label></div>'
         + '</div>').appendTo(parentDiv);

     parentDiv.dialog({
         open : function() {
             jQuery('#newWorkflowName').focus();
             initWorkflowPrivacyCheckbox();
             if (isView == "true") {
                 jQuery('#modify-send-button').hide();
             }
             initWorflowTagFunctionality(false);
         },
         autoResize : true,
         resizable : false,
         width : 615,
         height : 'auto',
         modal : true,
         title : divTitle,
         buttons : [ {
             id : "modify-send-button",
             text : 'Save',
             click : function() {
                 wfSaveMetaData(workflowId, cName, cDescription, cShared);
                 workflowListTags[jQuery('#wfTags').attr('workflow_id')] = tempTagList;
                 jQuery(this).dialog('close');
             }
         }, {
             id : "modify-close-button",
             text : 'Close',
             click : function() {
                 jQuery(this).dialog('close');
             }
     } ],
         close : function() {
             jQuery(this).remove();
         }

     });

    parentDiv.dialog('open');
}


function wfMoveWorkflow(transport) {

    if (transport !== undefined && transport.responseText !== undefined) {
        json = transport.responseText.evalJSON(true);


        if (json.success !== undefined && json.success == true) {
            var workflowId = json.workflowId;
        // Support renaming of workflow

            // Workflow panel (tab)
            var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");

            // Get current Values
            var cName = jQuery('#workflowNameInput_'+workflowId).val().trim();

            var parentDiv = jQuery('<div />', {
                id : 'moveWorkflowDialog'
            });

            parentDiv.appendTo('body');

            jQuery('<div id="workflowNameDiv">'
                     + 'Name:<br />'
                     + '<input type="text" id="newWFName_' + workflowId + '" name="newWorkflowName"'
                     + ' size="50" title="Modify the workflow name" maxlength="100" value="'
                     + htmlEncode(cName) +'" readonly>'
                     + '</div>').appendTo(parentDiv);
             jQuery('<br/>').appendTo(parentDiv);
             // Workflow Folder

             jQuery(getMyFoldersHtml(json)).appendTo(parentDiv);

             jQuery('#moveFolder').change(createNewFolderInput);


             parentDiv.dialog({
                 open : function() {
                     jQuery('#newWorkflowName').focus();
                     initWorkflowPrivacyCheckbox();
                 },
                 autoResize : true,
                 resizable : false,
                 width : 615,
                 height : 'auto',
                 modal : true,
                 title : 'Move To',
                 buttons : [ {
                     id : "move-send-button",
                     text : 'Save',
                     click : function() {
                         var folderId = jQuery('#moveFolder option:selected').val();
                         var newFolderName = null;
                         if (jQuery('#newFolderName').length > 0
                                 && jQuery('#newFolderName').val().trim() != '') {
                             newFolderName = jQuery('#newFolderName').val().trim();
                         }
                         moveWorkflow(workflowId, folderId, newFolderName);
                         jQuery(this).dialog('close');
                     }
                 }, {
                     id : "move-close-button",
                     text : 'Close',
                     click : function() {
                         jQuery(this).dialog('close');
                     }
             } ],
                 close : function() {
                     jQuery(this).remove();
                 }

             });

            parentDiv.dialog('open');
        } else {
            // Success is false
            wfTimerDialog("Failure", json.message, lsWorkflowListDialogTime);
        }

    }
}

function getMyFoldersHtml(json) {

    var myFolders = '';
    if (json.myFolders !== undefined && json.myFolders != '') {
        jQuery(json.myFolders).each(function(thisIndex, thisFolder){
            // Create a select list (options) for all of My Folders.
            for (folderName in thisFolder) {
                var isSelected = '';
                // If the folder is in the map
                if (thisFolder.hasOwnProperty(folderName)) {
                    var folderId = thisFolder[folderName];
                    // If specified, pre-select the appropriate folder
                    if (json.folderId !== undefined && json.folderId == folderId) {
                        isSelected = 'selected="selected"';
                    }
                    myFolders = myFolders + '<option value="' + folderId + '" ' + isSelected + '>' + folderName + '</option>';
                }
            }
        });
    }

    return '<div id="workflowFolderDiv">'
        + 'Folder:<br />'
        + '<select id="moveFolder" name="moveFolder" >'
        + myFolders
        + '<option value="">' + noFolderTitle + '</option>'
        + '<option value="0">' + newFolderTitle + '</option>'
        + '</select>'
        + '</div>';

}

//Support renaming of workflow
function createFolderDialog() {

 var parentDiv = jQuery('<div />', {
     id : 'createFolderDialog'
 });

 parentDiv.appendTo('body');

 jQuery('<div id="createFolderDiv">'
          + 'Folder Name:<br />'
          + '<input type="text" id="newFolderName"'
          + ' size="50" title="Enter a new folder name" maxlength="100" >'
          + '</div>').appendTo(parentDiv);
  jQuery('<br/>').appendTo(parentDiv);


  parentDiv.dialog({
      open : function() {
          jQuery('#newFolderName').focus();

      },
      autoResize : true,
      resizable : false,
      width : 615,
      height : 'auto',
      modal : true,
      title : 'Create New Folder',
      buttons : [ {
          id : "create-folder-send-button",
          text : 'Save',
          click : function() {
              var folderName = jQuery('#newFolderName').val().trim();

              createFolder(folderName);
              jQuery(this).dialog('close');
          }
      }, {
          id : "create-folder-close-button",
          text : 'Close',
          click : function() {
              jQuery(this).dialog('close');
          }
  } ],
      close : function() {
          jQuery(this).remove();
      }

  });

 parentDiv.dialog('open');
}

function createFolder(folderName) {

    if (folderName != '') {

        new Ajax.Request('LearnSphere', {
            parameters : {
                requestingMethod: 'ManageWorkflowsServlet.createFolder',
                folderName: folderName
            },
            onComplete : function(transport) {
                var json = transport.responseText.evalJSON(true);
                if (json.message !== undefined) {
                    var messageTitle = "Folder creation failed";
                    if (json.success !== undefined && json.success === true) {
                        messageTitle = "Success";
                    }
                    wfTimerDialog(messageTitle, json.message, lsWorkflowListDialogTime);
                }
                var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");
                fetchWorkflowRows(panelId, null, null);
            },
            beforeSend : wfShowStatusIndicator("Creating Folder..."),
            onSuccess : wfHideStatusIndicator,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
        });
    } else {
        wfTimerDialog("Folder name cannot be empty.");
    }
}


function renameFolderDialog(folderId, folderName) {

    var parentDiv = jQuery('<div />', {
       id : 'renameFolderDialog'
    });

    parentDiv.appendTo('body');

    jQuery('<div id="renameFolderDiv">'
            + 'Rename folder: '
            + '<input type="text" id="folderName" value="'
                + escapeDomValue(folderName).replace(/\\'/g, "'").replace(/[\\]+/g, '') + '" cols="70" />'
            + '</div>').appendTo(parentDiv);
    jQuery('<br/>').appendTo(parentDiv);


    parentDiv.dialog({
        open : function() {
            jQuery('#folderName').focus();
            jQuery('#folderName').keyup(function() {
                var value = jQuery('#folderName').val();
                jQuery('#folderName').val(value);
            });
        },
        autoResize : true,
        resizable : false,
        width : 615,
        height : 'auto',
        modal : true,
        title : 'Rename Folder',
        buttons : [ {
            id : "renameFolderButton",
            text : 'Save',
            click : function() {

                renameFolder(folderId, jQuery('#folderName').val());
                jQuery(this).dialog('close');
            }
        }, {
            id : "cancelRename",
            text : 'Cancel',
            click : function() {
                jQuery(this).dialog('close');
            }
    } ],
        close : function() {
            jQuery(this).remove();
        }

    });

    parentDiv.dialog('open');
}

function renameFolder(folderId, folderName) {

      new Ajax.Request('LearnSphere', {
          parameters : {
              requestingMethod: 'ManageWorkflowsServlet.renameFolder',
              folderId: folderId,
              folderName: folderName
          },
          onComplete : function(transport) {
              var json = transport.responseText.evalJSON(true);
              if (json.message !== undefined) {
                  var messageTitle = "Folder rename failed";
                  if (json.success !== undefined && json.success === true) {
                      messageTitle = "Success";
                  }
                  wfTimerDialog(messageTitle, json.message, lsWorkflowListDialogTime);
              }
              var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");
              fetchWorkflowRows(panelId, null, null);
          },
          beforeSend : wfShowStatusIndicator("Saving..."),
              onSuccess : wfHideStatusIndicator,
              onException : function(request, exception) {
                  wfHideStatusIndicator(); throw(exception);
              }
          });

    }

function deleteFolderDialog(folderId, folderName) {

    var parentDiv = jQuery('<div />', {
       id : 'deleteFolderDialog'
    });

    parentDiv.appendTo('body');

    jQuery('<div id="deleteFolderDiv">'
            + 'Are you sure you want to delete the folder, "' + folderName + '"? '
            + '<br />Deleting the folder will delete all workflows in it.'
            + '</div>').appendTo(parentDiv);
    jQuery('<br/>').appendTo(parentDiv);


    parentDiv.dialog({
        open : function() {
            jQuery('#delete-folder-yes-button').focus();

        },
        autoResize : true,
        resizable : false,
        width : 615,
        height : 'auto',
        modal : true,
        title : 'Create New Folder',
        buttons : [ {
            id : "delete-folder-yes-button",
            text : 'Yes',
            click : function() {

                deleteFolder(folderId, folderName);
                jQuery(this).dialog('close');
            }
        }, {
            id : "delete-folder-no-button",
            text : 'No',
            click : function() {
                jQuery(this).dialog('close');
            }
    } ],
        close : function() {
            jQuery(this).remove();
        }

    });

    parentDiv.dialog('open');
}

function deleteFolder(folderId, folderName) {



  new Ajax.Request('LearnSphere', {
      parameters : {
          requestingMethod: 'ManageWorkflowsServlet.deleteFolder',
          folderId: folderId,
          folderName: folderName
      },
      onComplete : function(transport) {
          var json = transport.responseText.evalJSON(true);
          if (json.message !== undefined) {
              var messageTitle = "Folder deletion failed";
              if (json.success !== undefined && json.success === true) {
                  messageTitle = "Success";
              }
              wfTimerDialog(messageTitle, json.message, lsWorkflowListDialogTime);
          }
          var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");
          fetchWorkflowRows(panelId, null, null);
      },
      beforeSend : wfShowStatusIndicator("Creating Folder..."),
          onSuccess : wfHideStatusIndicator,
          onException : function(request, exception) {
              wfHideStatusIndicator(); throw(exception);
          }
      });

}



function removeSearchFilter() {

    var thisElemId = jQuery(this).attr('id');
    new Ajax.Request('LearnSphere', {
        parameters : {
            requestingMethod: 'ManageWorkflowsServlet.removeSearchFilter',
            filterName: jQuery(this).attr('name')
        },
        onComplete : function(transport) {
            var json = transport.responseText.evalJSON(true);
            if (json.message !== undefined) {
                //var messageTitle = "Folder creation Failed";
                if (json.success !== undefined && json.success === true) {
                    jQuery('#selectedDatasetDiv').remove();
                    jQuery('#datasetId').remove();
                    window.location.href = "LearnSphere";
                } else {
                    wfTimerDialog("The filter could not be removed.");
                }
            }

        },
        beforeSend : wfShowStatusIndicator("Creating Folder..."),
        onSuccess : wfHideStatusIndicator,
        onException : function(request, exception) {
            wfHideStatusIndicator(); throw(exception);
        }
    });

}

function moveWorkflow(workflowId, folderId, newFolderName) {
    new Ajax.Request('LearnSphere', {
        parameters : {
            requestingMethod: 'ManageWorkflowsServlet.moveWorkflow',
            workflowId: workflowId,
            folderId: folderId,
            newFolderName: newFolderName
        },
        onComplete : function(transport) {
            var json = transport.responseText.evalJSON(true);
            if (json.message !== undefined) {
                var messageTitle = "Move failed";
                if (json.success !== undefined && json.success === true) {
                    messageTitle = "Success";
                }
                wfTimerDialog(messageTitle, json.message, lsWorkflowListDialogTime);
            }
            var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");
            fetchWorkflowRows(panelId, null, null);
        },
        beforeSend : wfShowStatusIndicator("Moving..."),
        onSuccess : wfHideStatusIndicator,
        onException : function(request, exception) {
            wfHideStatusIndicator(); throw(exception);
        }
    });
}


function createNewWorkflowInFolder(folderId) {
    createWorkflowOnFetchFolders(folderId, null);
}

function createWorkflowOnFetchFolders(folderId, workflowId) {
    var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");
    new Ajax.Request('LearnSphere', {
        parameters : {
            requestingMethod: 'ManageWorkflowsServlet.fetchMyFolders',
            workflowId: workflowId,
            folderId: folderId,
            panelId: panelId
        },
        onComplete : createNewWorkflow,
        beforeSend : wfShowStatusIndicator("Creating..."),
        onSuccess : wfHideStatusIndicator,
        onException : function(request, exception) {
            wfHideStatusIndicator(); throw(exception);
        }
    });
}

function fetchMyFolders(workflowId) {
    var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");
    new Ajax.Request('LearnSphere', {
        parameters : {
            requestingMethod: 'ManageWorkflowsServlet.fetchMyFolders',
            workflowId: workflowId,
            panelId: panelId
        },
        onComplete : wfMoveWorkflow,
        beforeSend : wfShowStatusIndicator("Moving..."),
        onSuccess : wfHideStatusIndicator,
        onException : function(request, exception) {
            wfHideStatusIndicator(); throw(exception);
        }
    });
}

var nameChanged = false;
var descChanged = false;
var shareabilityChanged = false;
var tagsChanged = false;

function wfSaveMetaData(workflowId, existingName, existingDesc, existingShared) {

    var newName = jQuery('input#newWFName_' + workflowId).val().trim();
    nameChanged = (newName != existingName) ? true : false;

    var newDesc = jQuery('textarea#newWFDescription_' + workflowId).val().trim();
    descChanged = (newDesc != existingDesc) ? true : false;

    var newShared = jQuery('input:checkbox[name="isSharedRadioButton"]').is(':checked');
    shareabilityChanged = (newShared != existingShared) ? true : false;

    tagsChanged = tagsWereModified(workflowId);

    if (newName.length > 0) {
        if (workflowId !== undefined && workflowId != '') {
            new Ajax.Request('LearnSphere', {
                parameters : {
                    requestingMethod : 'ManageWorkflowsServlet.editMetaData',
                    workflowId : workflowId,
                    newWorkflowName : newName,
                    newDescription : newDesc,
                    newGlobalFlag : newShared,
                    tags : getWorkflowTags(false)
                },
                onComplete : updateWorkflowMetaData,
                onSuccess : wfHideStatusIndicator,
                onException : function(request, exception) {
                    wfHideStatusIndicator(); throw(exception);
                }
            });
        }
    } else {
        errorPopup("You must specify a name for the workflow.");
    }
}

function updateWorkflowMetaData(transport) {
    if (nameChanged) { updateWorkflowName(transport); }
    if (descChanged) { updateWorkflowDesc(transport); }
    if (tagsChanged) { updateWorkflowDesc(transport); }
    if (shareabilityChanged) { updateWorkflowShared(transport); }

    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        successPopup(json.message);
    } else {
        errorPopup(json.message);
    }
}

function updateWorkflowShared(transport) {

    var json = transport.responseText.evalJSON(true);
    var wfId = json.workflowId;

    if (json.level == "SUCCESS") {
        // Update divs with new shared status.
        jQuery('#workflowGlobalInput_'+ wfId).val(json.isGlobal);

        if (json.isGlobal) {
            // My Workflows
            jQuery('#workflowNameDiv_' + wfId + ' .globalDemarcation').text('*');
            // Public/Recommended WorkflowsA
            jQuery('#WorkflowGlobalTag_' + wfId).replaceWith(
            '<span id="WorkflowGlobalTag_' + wfId
                + '" title="This workflow data is shared or public." class="wfAccess public">SHARED</span>');
        } else {
            // My Workflows
            jQuery('#workflowNameDiv_' + wfId + ' .globalDemarcation').text('');
            // Public/Recommended WorkflowsA
            jQuery('#WorkflowGlobalTag_' + wfId).replaceWith(
                    '<span id="WorkflowGlobalTag_' + wfId
                    + '" title="This workflow data is private." class="wfAccess private">PRIVATE</span>');
        }

    }
}

function updateWorkflowName(transport) {
    var json = transport.responseText.evalJSON(true);
    var wfId = json.workflowId;
    if (json.level == "SUCCESS") {

        // Update divs with new name.
        jQuery('#wfNameField_' + wfId).val(json.workflowName);
        jQuery('#workflowNameInput_' + wfId).val(json.workflowName);
        jQuery('#workflowName_' + wfId).html(getTruncatedNameIfNec(json.workflowName));
        jQuery('#workflowName_' + wfId).attr('title', htmlDecode(json.workflowName));
    } else {
        // Revert change.
        var hiddenValue = jQuery('input#workflowNameInput_' + wfId).val();
        jQuery('#wfNameField_' + wfId).val(hiddenValue);
    }
}

function getTruncatedNameIfNec(workflowName) {
    if (workflowName.length <= 30) { return workflowName; }
    var result = workflowName.substring(0, 30);
    result += "...";
    return result;
}

function wfCancelNameChange(workflowId) {
    var hiddenValue = jQuery('input#workflowNameInput_' + workflowId).val();
    jQuery('#wfNameField_' + workflowId).val(hiddenValue);
}

function updateWorkflowDesc(transport) {

    var json = transport.responseText.evalJSON(true);
    var wfId = json.workflowId;

    if (json.level == "SUCCESS") {
        // Update divs with new description/tags.
        let descAndTags = urlToAnchorTag(json.description, 'linkInDescription') + getTagsForTooltip(wfId);

        jQuery('#wfDescId_' + wfId + ' span').html(descAndTags);
        jQuery('#wfDescId_' + wfId + ' span').attr('value', json.description);
        // Used for hiding the i icon
        jQuery('#workflowDescInput_' + wfId).val(json.description);

        // If approrpirate, display 'info' icon w/tooltip.
        var styleStr = 'display: initial';
        if (json.description.length == 0 && workflowListTags[wfId].length == 0) {
            styleStr = "display: none";
        }
        jQuery('#wfDescId_' + wfId).attr('style', styleStr);
    } else {
        // Revert change.
        var hiddenValue = jQuery('input#workflowDescInput_' + wfId).val();
        jQuery('#wfDescId_' + wfId).val(hiddenValue);
    }

}

function wfCancelDescChange(workflowId) {
    var hiddenValue = jQuery('input#workflowDescInput_' + workflowId).val();
    jQuery('#wfDescId_' + workflowId).val(hiddenValue);
}


function wfSelectGlobalSort() {
    wfSelectSort(jQuery(this).text(), true);
}

function wfSelectMySort() {
    wfSelectSort(jQuery(this).text(), false);
}

function wfSelectSort(sortBy, isGlobal) {
    var wfPage = window.location.href.match(/.*LearnSphere[#]*/g);
    if (wfPage != null && wfPage.length > 0) {
    // For now (DIBBs, 1/11-12/17) only sorting by 'Workflow Name'
    sortBy = 'Workflow Name';
        jQuery(
                '<form id="ManageWorkflowsSort" method="post" action="LearnSphere">'
                + '<input name="sortBy" type="hidden" value="' + sortBy + '"/> '
                + '<input name="isGlobal" type="hidden" value="' + isGlobal + '"/>'
                + '</form>').appendTo('body').submit();
    } else {
        var dsId = null;
        if (jQuery('#datasetId') !== undefined && jQuery('#datasetId').val() != '') {
            dsId = jQuery('#datasetId').val();
        }
        if (dsId != null) {
            new Ajax.Request(window.location.href, {
                parameters: {
                    requestingMethod: "WorkflowManager.wfSelectSort",
                    datasetId: dsId,
                    ajaxRequest: "true",
                    ds_request: "content",
                    ds_content: "workflows",
                    subtab: "workflows",
                    isGlobal: isGlobal,
                    sortBy: sortBy
                },
                onComplete: displayWorkflows,
                onException: function (request, exception) {
                    throw(exception);
                }
            });
        }
    }

}
//Submit user-specified sort request to the detached workflow jsp
// or to the dataset-embedded workflow jsp.
function wfSelectSort1() {

    var dsId = null;
    if (jQuery('#datasetId') !== undefined && jQuery('#datasetId').val() != '') {
        dsId = jQuery('#datasetId').val();
    }
    new Ajax.Request("LearnSphere", {
        parameters: {
            datasetId: null,
            sortBy: jQuery(this).text()
        },
        onComplete: function(transport) { jQuery('#workflows-page-table').html(transport.responseText);  },
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

//-----------------------------------------
// General purpose.
//  Avoiding pulling-in Datashop.js...
//-----------------------------------------
function errorPopup(message) {
    messagePopup(message, "ERROR");
}

function warningPopup(message) {
    messagePopup(message, "WARNING");
}

function successPopup(message) {
    messagePopup(message, "SUCCESS");
}

/** Pops up an error message in the middle that turns off after 3 seconds */
function messagePopup(message, messageType) {
    if (!messageType) { messageType = "ERROR"; }

    messagePopupDiv = document.getElementById("messagePopup");
    if (!messagePopupDiv) {
        messagePopupDiv = document.createElement("div");
    }
    messagePopupDiv.className="popupMenu";
    messagePopupDiv.id = "messagePopup";
    contentPara = document.createElement('P');
    if (messageType == "ERROR") {
        contentPara.className="errorPopupContent";
    } else if (messageType == "SUCCESS") {
        contentPara.className="successPopupContent";
    } else if (messageType == "WARNING") {
        contentPara.className="warningPopupContent";
    } else {
        contentPara.className="messagePopupContent";
    }
    contentPara.appendChild(document.createTextNode(message));
    messagePopupDiv.appendChild(contentPara);

    document.body.appendChild(messagePopupDiv);
    if (messagePopupDiv.timeout) {
        clearTimeout(messagePopupDiv.timeout);
    }
    messagePopupDiv.timeout = setTimeout(closeErrorPopup.bindAsEventListener(messagePopupDiv), 4000);
}
function closeErrorPopup() {
    if (this) {
        this.style.display="none";
        try {
            document.body.removeChild(this);
        } catch(err) {
            //do nothing as it just means it wasn't found
        }
    }
}


//Assign parameter values and response functions for Ajax request
function createWfAccessRequest() {
var splitString = this.id.split('_');
var rowIndexer = splitString[1];
var projectList = '';
if (jQuery('.wfAccessRequestLink').length > 0
 && jQuery('#wfAccessRequestProjectList_' + rowIndexer).val() != '') {
 projectList = jQuery('#wfAccessRequestProjectList_' + rowIndexer).val();
}
previousMiniIcon = "Processing";
new Ajax.Request("AccessRequests", {
   parameters : {
       requestingMethod : "createMultiRequest",
       wfAccessRequestProjectList : projectList,
       rowIndex : rowIndexer
   },
   requestHeaders : {
     Accept : 'application/json;charset=UTF-8'
   },
   beforeSend : wfShowStatusIndicatorModal(false, "Requesting..."),
   onSuccess : wfHideStatusIndicator,
   onComplete : createWfRequestAjaxListener,
   onException : function(request, exception) {
     throw (exception);
   }
});
return false;
}

//Create the request modal window if Ajax request successful
function createWfRequestAjaxListener(transport) {

var json = transport.responseText.evalJSON(true);
if (json.msg == "success") {
 openRequestDialog(json);
} else if (json.msg == "error") {
 showErrorDialog(json.cause);
}
}


var requestMaxLength = 500;
var termsByProjectId = [];
//Opens a modal window populated by the previous request, if one exists
function openRequestDialog(json) {
if (jQuery('#wfRequestAccessDialog').length > 0) {
   jQuery('#wfRequestAccessDialog').dialog("close");
}
if (json !== undefined && json.jsonArray !== undefined) {
   var termsSelectSize = json.jsonArray.length;
   if (termsSelectSize > 8) {
       termsSelectSize = 8;
   } else if (termsSelectSize < 4) {
       termsSelectSize = 4;
   }

   var windowHeight = jQuery(window).height();
   var maxHeight = windowHeight * 0.94; // this will make the dialog height 80% of the screen
   if (maxHeight > 700) {
       maxHeight = 700;
   }
   var windowWidth = jQuery(window).width();
   var maxWidth = windowWidth * 0.3;
   if (maxWidth < 600) {
       maxWidth = 600;
   }


   var projectNames = '';
   var projectIds = '';
   var projectCount = 0;
   var projectTermsDiv = '<div class="wfRequestAccessElement" ><div>Projects</div><div>'
       + '<select multiple size="' + termsSelectSize + '" id="projectTermsSelectBox">';
   var termsDiv = '<div class="wfRequestAccessElement"><div id="termsTextArea">';

   var isProjectSelected = false;
   var selectedProjectId = null;

   jQuery.each(json.jsonArray, function(reqIndex, reqItem) {
      //alert(JSON.stringify(reqItem));
      projectNames = projectNames + reqItem['project'];
      projectIds = projectIds + reqItem['projectId'];
      if (projectCount < json.jsonArray.length - 1) {
          projectNames = projectNames + ', ';
          projectIds = projectIds + ',';
      }
      projectCount++;
      if (selectedProjectId == null) {
          isProjectSelected = 'selected=\"selected"';
          selectedProjectId = reqItem.projectId;
      } else {
          isProjectSelected = "";
      }

      if (reqItem.touName !== undefined && reqItem.touName != null) {
          projectTermsDiv = projectTermsDiv + '<option class="touProjectName" value="' + reqItem.projectId
          + '" title="' + reqItem.project + '" ' + isProjectSelected + ' >' + reqItem.project + ': '
          + reqItem.touName + ' (' + reqItem.termsEffective + ')'
          + '</option>';
      } else {
          projectTermsDiv = projectTermsDiv + '<option class="touProjectName" value="' + reqItem.projectId
              + '" title="' + reqItem.project + '" ' + isProjectSelected + ' >' + reqItem.project + ': No Terms'
              + '</option>';
      }
      if (reqItem.terms !== undefined && reqItem.terms != null) {
          termsByProjectId[reqItem.projectId] = reqItem.terms;
      } else {
          termsByProjectId[reqItem.projectId] = "No terms of use are available for this project.";
      }
   });

   projectTermsDiv = projectTermsDiv + '</select></div></div>';
   termsDiv = termsDiv + '</div></div>';

   // New dialog
   jQuery('<div />', {
       id : 'wfRequestAccessDialog'
   }).html(
       '')
   .dialog({
       open : function() {
           jQuery('.ui-button').focus();
       },
       autoOpen : true,
       autoResize : true,
       resizable : true,
       width : maxWidth,
       modal: true,
       height : maxHeight,
       title : 'Request Access to Data',
       buttons : {
           'Request Access' : function() {
               requestMultipleProjectsAccess(projectIds, json.rowIndex);
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

   // Get the request dialog div from the page
   var requestDiv = document.getElementById('wfRequestAccessDialog');
   requestDiv.innerHTML = "";

   var pluralProjects = '';
   if (projectNames != '') {
       pluralProjects = 's';
   }
   // Create form elements to add to the div
   var p = document.createElement('p');
   p.id = "projectLabel";
   p.innerHTML = '<span class="boldface">Project' + pluralProjects + ':</span> ' + projectNames;
   requestDiv.appendChild(p);

   var input = document.createElement('input');
   input.id = "projectIdList";
   input.name = "projectIdList";
   input.type = "hidden";
   input.value = projectIds;
   requestDiv.appendChild(input);


   var p = document.createElement('p');
   p.id = "levelLabel";
   p.innerHTML = "<span class='boldface'>I request:</span>"
   requestDiv.appendChild(p);

   var wfViewText = ' view access (ability to view analyses, import data, and export files)';
   var wfEditText =
       " edit access (view access plus the ability to add files, add KC models, and create samples)";

   // Default for workflows is 'view'
   var viewChecked = "checked";
   var editChecked = "";

   var t = document.createElement('div');

   t.innerHTML = '<table><tr><td><input type="radio" name="modalWinLevelGroup" id="request_view_access" value="view" '
           + viewChecked
           + '></td>'
           + '<td><label for="request_view_access">' + wfViewText + '</label></td></tr>'
           + '<tr><td><input type="radio" name="modalWinLevelGroup" id="request_edit_access" value="edit" '
           + editChecked
           + '></td>'
           + '<td><label for="request_edit_access">' + wfEditText + '</label></td></tr></table>';
   requestDiv.appendChild(t);

   p.innerHTML = 'Reason (<span class="requiredFormField"><tt>Required</tt></span>)';
   requestDiv.appendChild(p);


   var d = document.createElement('div');
   d.className = "textAreaDiv";

   var textarea = document.createElement('textarea');
   textarea.name = "RequestReason";
   textarea.id = "RequestReason";
   textarea.rows = "5";
   textarea.cols = "60";
   textarea.value = '';
   d.appendChild(textarea);

   var div = document.createElement('div');
   div.className = "charsLeftRequest";
   div.id = "charsLeftRequest";
   var charsLeft = requestMaxLength - textarea.value.length;
   div.innerHTML = charsLeft + " characters remaining";
   d.appendChild(div);
   requestDiv.appendChild(d);

   var termsInstructions = '<p><span id="agreement">'
       + '<input type="checkbox" id="termsAgree" />'
       + '<label for="termsAgree"> By requesting access to the data, I agree to the following terms of use. '
       + 'Upon submission, your email address will be shown to the '
       + 'project PI and DP.'
       + ' They may contact you with follow-up questions.</label>'
       + '</span></p>';

   if ((json.datashopToU !== undefined) && (json.datashopToU)) {

       termsInstructions = '<p><span id="agreement">'
           + '<input type="checkbox" id="termsAgree" />'
           + '<label for="termsAgree"> By requesting access to the data, I agree to the DataShop terms of use. '
           + '</label>'
           + '</span></p>';
   }

   if (selectedProjectId != null) {
       jQuery('#wfRequestAccessDialog').append('<div class="borderedDiv">' + termsInstructions
               + '<div id="wfProjectAccessDiv">' + projectTermsDiv + termsDiv + '</div></div>');
       jQuery('#termsTextArea').html(termsByProjectId[selectedProjectId]);
   }

   jQuery('#projectTermsSelectBox').change(updateTermsOfUse);
   jQuery('#termsAgree').change(testSubmitButton);

   jQuery("textarea#RequestReason").keyup(textareaWfModRequest);
   jQuery('#wfRequestAccessDialog').dialog('open');
   // enable the request access button if the textarea text satisfies the constraints
   textareaWfModRequest();
}
}

function showErrorDialog(cause) {
//Create form elements to add to the div
var div = document.createElement('div');
div.id = "showErrorDialog";
div.className = "informationalDialog";
div.innerHTML = cause;
jQuery('body').append(div);
//Used to show errors with the Ajax requests
jQuery('#showErrorDialog').dialog({
    modal : true,
    autoOpen : false,
    autoResize : true,
    resizable : false,
    width : 840,
    height : 340,
    title : "Oops",
    buttons : [ {
              id : "show-error-button",
              text : "Okay",
              click : function() { jQuery(this).dialog("close");
                      jQuery(this).empty().remove();
              }
    } ]
});

jQuery('#showErrorDialog').dialog('open');
}

function requestMultipleProjectsAccess(projectIds, rowIndex) {

 new Ajax.Request('AccessRequests', {
         parameters : {
             requestingMethod : 'requestMultipleProjectsAccess',
             projectIds : projectIds,
             arReason : jQuery("textarea#RequestReason").val(),
             arLevel : 'view',
             rowIndex : rowIndex
         },
         onComplete : multiProjectAccessRequestSubmitted,
         onException : function(request, exception) {
             throw (exception);
         }
 });
}

function multiProjectAccessRequestSubmitted(transport) {
 var json = null;
 if (transport !== undefined) {
     json = transport.responseText.evalJSON(true);
 }
 if (json.msg !== undefined && json.msg == "success" && json.rowIndex !== undefined) {
     var requestedDivId = 'wfAccessRequestRow_' + json.rowIndex;
     if (jQuery('#' + requestedDivId).length > 0) {
         var projectIdString = json.projectIdString;
         var projectNameString = json.projectNameString;
         // For each row that has a request link
         jQuery('.wfAccessRequestLink').each(function(reqRowIndex, reqRowObject) {

             var newProjectList = '';
             var newProjectNameList = '';

             // Get the arbitrary index of the row
             var splitString = reqRowObject.id.split('_');
             var rowIndex = splitString[1];

             // Get the project names and project ids contained in that img title and hidden input
             var projectNames = jQuery('#wfAccessRequestRow_' + rowIndex).attr('title').replace("Access Required: ", "");
             var projectIds = jQuery('#wfAccessRequestProjectList_' + rowIndex).val();
             // Iterate over the requested project ids
             var requestedProjectIds = projectIdString.split(",");
             // Iterate over the hidden input project ids
             var splitProjectIds = projectIds.split(',');
             jQuery(splitProjectIds).each(function(splitProjectId) {
                 var matchingId = null;

                 jQuery(requestedProjectIds).each(function(reqProjectId) {
                     if (requestedProjectIds[reqProjectId].trim() == splitProjectIds[splitProjectId].trim()) {
                         matchingId = splitProjectIds[splitProjectId];
                     }
                 });
                 if (matchingId == null) {
                     if (newProjectList != '') {
                         newProjectList = newProjectList + ', ';
                     }
                     newProjectList = newProjectList + splitProjectIds[splitProjectId];
                 }
             });

              // Iterate over the requested project names
             var requestedProjectNames = projectNameString.split(",");
             var pendingProjectString = '';
             // Iterate over the current project names in the UI
             var splitProjectNames = projectNames.split(',');
             jQuery(splitProjectNames).each(function(splitProjectName) {
                 var matchingName = null;
                 jQuery(requestedProjectNames).each(function(reqProjectName) {
                     if (requestedProjectNames[reqProjectName].trim() == splitProjectNames[splitProjectName].trim()) {
                         matchingName = splitProjectNames[splitProjectName];
                     }
                 });
                 if (matchingName == null) {
                     if (newProjectNameList != '') {
                         newProjectNameList = newProjectNameList + ', ';
                     }
                     newProjectNameList = newProjectNameList + splitProjectNames[splitProjectName];
                 } else {
                     if (pendingProjectString != '') {
                         pendingProjectString = pendingProjectString + ', ';
                     }
                     pendingProjectString = pendingProjectString + splitProjectNames[splitProjectName];
                 }
             });
             var pendingProjectList = '';
             if (jQuery('#wfAccessRequestPendingRow_' + rowIndex).length > 0) {
                 pendingProjectList = jQuery('#wfAccessRequestPendingRow_' + rowIndex).attr('title');
                 if (pendingProjectString !== undefined && pendingProjectString != '') {
                     jQuery('#wfAccessRequestPendingRow_' + rowIndex).attr('title', pendingProjectList + ", " + pendingProjectString);
                 }
               } else {
                   pendingProjectList = jQuery('#wfAccessRequestPendingRow_' + rowIndex).attr('title');

                   jQuery('#wfAccessRequestPendingRow_' + rowIndex).replaceWith(jQuery(
                    '<a href="AccessRequests" target="_blank"></a>'
                    + '<a href="#" id="wfAccessRequestPendingRow_' + rowIndex + '" class="wfAccessRequestPendingLink wfAccess pending" '
                    + 'title="Pending Requests: ' + pendingProjectString + '">Pending</a>'));

               }

             if (newProjectList != '') {
                 jQuery('#wfAccessRequestProjectList_' + rowIndex).val(newProjectList);
                 jQuery('#wfAccessRequestRow_' + rowIndex).attr('title', 'Access Required: ' + newProjectNameList);
             } else {
                 jQuery('#wfAccessRequestProjectList_' + rowIndex).remove();
                 jQuery('#wfAccessRequestRow_' + rowIndex).remove();
             }

         });
     }

     wfInfoDialog('wfRequestAccessDialog', json.message, "Success");

 } else if (json.msg !== undefined && json.msg != "success") {
     wfInfoDialog('wfRequestAccessDialog', json.message, "Error");
 } else {
     var infoStr = 'An error has occurred during the request. Please make sure you are logged in, '
         + 'and if the problem persists, please contact help.';
     wfInfoDialog('wfRequestAccessDialog', infoStr, "Error");
 }

}

function updateTermsOfUse() {
 var selectedProject = jQuery('#projectTermsSelectBox option:selected').val();
 if (termsByProjectId[selectedProject] !== undefined) {
     jQuery('#termsTextArea').html(termsByProjectId[selectedProject]);
 }


}

function testSubmitButton() {
 var isAgreed = jQuery('input:checkbox[id|="termsAgree"]:checked').val();
 var reason = jQuery("textarea#RequestReason").val();

 if (isAgreed && reason.trim().length > 0 && reason.trim().length <= requestMaxLength) {
     jQuery(".ui-dialog-buttonpane button:contains('Request Access')")
     .button('enable');
 } else {
     jQuery(".ui-dialog-buttonpane button:contains('Request Access')")
     .button('disable');
 }
}

//This area makes sure the user cannot submit more than 255 characters
//in the reason text area element; function 1
function textareaWfModRequest() {
 var reason = jQuery("textarea#RequestReason").val();
 if (reason.length > requestMaxLength) {
     jQuery(".ui-dialog-buttonpane button:contains('Request Access')")
         .button('disable');
     jQuery("div#charsLeftRequest").css("color", "red");
 } else if (reason.trim().length < 1) {
     jQuery(".ui-dialog-buttonpane button:contains('Request Access')")
         .button('disable');
 } else {
     var isAgreed = jQuery('input:checkbox[id|="termsAgree"]:checked').val();
     if (isAgreed) {
         jQuery(".ui-dialog-buttonpane button:contains('Request Access')")
             .button('enable');
         jQuery("div#charsLeftRequest").css("color", "black");
     }
 }
 var charsLeft = requestMaxLength - reason.length;
 // Update the chars remaining
 jQuery("div#charsLeftRequest").html(charsLeft + " characters remaining");
}

