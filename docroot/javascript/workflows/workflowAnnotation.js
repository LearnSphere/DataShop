/*
 * Author: Peter Schaldenbrand
 * Functions to add annotation feature to workflows
 * Carnegie Mellon University
 * Human Computer Interaction Institute 2018
 */

var annotationHeight = 100;
var annotationWidth = 180;
var isDragging = false;

/**
 * Take the annotations received from the workflowXML in the workflow table in the DB and
 * load them into currentDigraph.  Also add them to the workflow interface
 * @param annotations - json info on annotations
 */
function loadAnnotationFromDB(annotations) {
    currentDigraph.annotations = [];
    if (annotations == undefined || annotations == null || Object.keys(annotations).length == 0) {
        return;
    }
    annotations = annotations.annotation;

    //case for only one annotation
    if (annotations.length == undefined) {
        currentDigraph.annotations.push(annotations);
        paintAnnotation(annotations);
    }

    // Add the annotations to the currentDigraph and paint them into the interface
    for (var i = 0; i < annotations.length; i++) {
        var annotation = annotations[i];

        currentDigraph.annotations.push(annotation);

        paintAnnotation(annotation);
    }

    setAnnotationEditFunctionality('.editAnnotationIcon');
    setAnnotationEditFunctionalityDoubleClick('.annotationWfPreview');

    if (!currentDigraph.isView){
        //handle delete component
        jQuery('.deleteAnnotationIcon').click(deleteAnnotationConfirm);
    } else {
        // Change cursor on annotations so users know they can't move them
        //jQuery('.annotationTitle').css('background','#27375d');
    }
}

/**
 * Add an annotation to the workflow interface
 * @param annotation - object with annotation_id, text, left, right
 */
function paintAnnotation(annotation) {
    var processAnnotation = null;

    var top = null;
    var left = null;

    jQuery('<div/>', {
        id : 'annotation_'+annotation.annotation_id
    }).appendTo('#process-div');
    var processAnnotation = jQuery('#' + 'annotation_'+annotation.annotation_id);

    // Add to the diagram and add the simple (immediately displayed) options to the component.
    processAnnotation.appendTo('#process-div');

    processAnnotation.attr('class', 'process-component');
    processAnnotation.attr('class', 'workflowAnnotation');

    processAnnotation.attr('title', 'Double click to view/edit');

    processAnnotation.css('position', 'absolute');
    processAnnotation.css('width', annotationWidth + 'px');
    processAnnotation.css('height', annotationHeight + 'px');

    // So that the bottom corner of the annotation looks like it is bent over, do this weird background trick
    var annotationBackgroundDivs = '<div class="annotationBackgroundLeft"></div>'
            + '<div class="annotationBackgroundRight"></div>';
    processAnnotation.append(annotationBackgroundDivs);

    //processAnnotation.html('<div class="annotationTitle componentTitle">Annotation</div>');
    processAnnotation.append(createAnnotationContentsHtml(annotation.text));

    // If the text is too large for the annotation, display a "View More" button
    let previewSelect = '#annotation_' + annotation.annotation_id + ' .annotationWfPreview'
    if (jQuery(previewSelect)[0].scrollHeight > jQuery(previewSelect).innerHeight()) {
        let viewMoreButton = jQuery('<span class="editAnnotationIcon viewMoreAnnotation" >' +
                '(view entire note)</span>');
        jQuery(processAnnotation).append(viewMoreButton);
    }

    jQuery(previewSelect).val(annotation.text);

    //add exit button if user can edit wf
    if (!currentDigraph.isView) {
        processAnnotation.append('<span class="deleteAnnotationIcon"></span>');
    }

    //add edit button if they can view it, otherwise add view button
    if (!currentDigraph.isView) {
        processAnnotation.append('<span>' +
                '<img class="editAnnotationIcon" src="images/pencilNoBox.png"></span>');
    } else {
        processAnnotation.append('<span>' +
                '<img class="editAnnotationIcon" src="images/eyeIcon.png"></span>');
    }

    // Set the top and left css values
    var addPx = "";
    var top = annotation.top + "";
    if (top.substring(top.length-2, top.length) !== "px") {
        addPx = "px";
    }
    processAnnotation.css('top', top + addPx);

    addPx = "";
    var left = annotation.left + "";
    if (left.substring(left.length-2, left.length) !== "px") {
        addPx = "px";
    }
    processAnnotation.css('left', left + addPx);

    // Make dropped/dragged annotations draggable, but keep it contained to the workspace
    jQuery(processAnnotation).draggable({
    //jQuery('#annotation_'+annotation.annotation_id+' .annotationWfPreview').draggable({
        scroll: true,
        containment : '#process-div',
        cursor : 'move',
        snap : '#process-div',
        start: startFix,
        drag: dragFix
    });

    // Handle moving a annotation
    jQuery('.workflowAnnotation').mousedown(function(){
        isDragging = false;
    }).mousemove(function() {
        isDragging = true;
    }).mouseup(function(e) {
        var wasDragging = isDragging;
        isDragging = false;
        if (wasDragging) {
            moveAnnotation(e);
        }
    });

    setHoverEditAnnotationButtons(processAnnotation);

}


/**
 * Put the text of the annotation into html
 * @param text - String text of the annotation
 * @return html - contents of annotation for interface
 */
function createAnnotationContentsHtml(text) {
    // If text hasn't been instantiated, make it an empty string
    if (text === undefined || text === null || typeof(text) != 'string') {
        text = '';
    }

    var html = '';

    // Handle Links:
    var linkedText = Autolinker.link( text, {
        newWindow: true,
        urls: true,
        className: 'linkInAnnotation'
    } );
    html += '<div scrolling="no" class="annotationWfPreview">'+linkedText+'</div>'
    return html;
}

function setHoverEditAnnotationButtons(processAnnotation) {
    // If the annotation is empty, let the edit/delete icons persist so the user knows how to edit them
    if (!(jQuery('#' + processAnnotation.attr('id') + ' .annotationWfPreview').val().length > 0)) {
        jQuery('#' + processAnnotation.attr('id') + ' span .editAnnotationIcon').show();
        jQuery('#' + processAnnotation.attr('id') + ' .deleteAnnotationIcon').show();
    }

    processAnnotation.hover(
        function() {
            var id = jQuery(this).attr('id');
            jQuery('#' + id + ' .deleteAnnotationIcon').fadeIn(500);
            jQuery('#' + id + ' span .editAnnotationIcon').fadeIn(500);
        },
        function() {
            var id = jQuery(this).attr('id');
            // Only hide the edit icon if the annotation has text in it.
            if (jQuery('#' + id + ' .annotationWfPreview').val().length > 0) {
                jQuery('#' + id + ' .deleteAnnotationIcon').fadeOut(500);
                jQuery('#' + id + ' span .editAnnotationIcon').fadeOut(500);
            }
        }
    );
}

/**
 * Set the functionality to edit an annotation.
 * @param selector - jQuery selector for the annotation or annotations that need the functionality
 */
function setAnnotationEditFunctionality(selector) {
    jQuery(selector).off('click');
    jQuery(selector).click(openEditAnnotation);
}
function setAnnotationEditFunctionalityDoubleClick(selector) {
    jQuery(selector).off('dblclick');
    jQuery(selector).dblclick(openEditAnnotation);
}


function openEditAnnotation(e) {
    var workflowId = currentDigraph.id;

    var annotationId = e.target.parentNode.parentNode.id;
    if (annotationId.includes('annotation_')) {
      annotationId = annotationId.replace('annotation_', '');
    } else {
      annotationId = e.target.parentNode.id.replace('annotation_', '');
    }

    var parentDiv = jQuery('<div><input type="hidden" id="editAnnotationId" name="editAnnotationId" value="' +
        workflowId + '" /></div>', {
            id: 'editAnnotationDiv'
        });

    editAnnotaionPrompt(parentDiv);

    if (!currentDigraph.isView) {
        parentDiv.dialog({
            open: function() {
                editAnnotationSupportTabs()
                jQuery('#newAnnotationText').focus();
                jQuery('#newAnnotationText').keyup(function() {
                    //submitAnnotationEdit(annotationId, jQuery('#newAnnotationText').val());
                    //jQuery(this).dialog('close');
                    if (jQuery('#newAnnotationText').val().length >= 2048) {
                        jQuery('#maxLen').css('color', 'red');
                    } else {
                        jQuery('#maxLen').css('color', 'black');
                    }
                });
                //initWorkflowPrivacyCheckbox();
            },
            autoOpen: true,
            autoResize: true,
            resizable: false,
            width: 600,
            height: 'auto',
            modal: true,
            title: 'Edit Annotation',
            buttons: {
                'Save Annotation': function() {
                    submitAnnotationEdit(jQuery('#newAnnotationText').attr('name'),
                        jQuery('#newAnnotationText').val());
                    // Update newly-created annotation
                    jQuery(this).dialog('close');
                },
                'Close': function() {
                    jQuery(this).dialog('close');
                }
            },
            close: function() {
                jQuery(this).remove();
            }
        });
    } else {
        // Workflow is view only
        parentDiv.dialog({
            open: function() {
                jQuery('#newAnnotationText').focus();
                // Make it not editable
                jQuery('#newAnnotationText').attr('readonly', 'readonly');
            },
            autoOpen: true,
            autoResize: true,
            resizable: false,
            width: 600,
            height: 'auto',
            modal: true,
            title: 'View Annotation',
            buttons: {
                'Close': function() {
                    jQuery(this).dialog('close');
                }
            },
            close: function() {
                jQuery(this).remove();
            }
        });
    }

    let textToDisplay = jQuery('#annotation_' + annotationId + ' .annotationWfPreview').val();
    if (typeof(textToDisplay) == "string") {
        jQuery('#newAnnotationText').text(
            jQuery('#annotation_' + annotationId + ' .annotationWfPreview').val());
    } else {
        jQuery('#newAnnotationText').text('');
    }

    jQuery('#newAnnotationText').attr('name', annotationId)
}

/**
 * The edit annotation dialog (triggered when the annotation is clicked).
 */
function editAnnotaionPrompt(parentDiv) {
    jQuery('<br/>').appendTo(parentDiv);

    jQuery(
         '<div id="annotationTextDiv">'
                 + '<textArea id="newAnnotationText" name="newAnnotationText"'
                     + ' cols="22" rows="5" maxlength="2048" placeholder="Type Annotation Text Here"/>'
                 + '</div><div id="maxLen">Enter no more than 2048 characters</div>').appendTo(parentDiv);

    jQuery('<br/>').appendTo(parentDiv);
}

/**
 * Textareas do not support tabs, this should make tabs work, but it needs work
 */
function editAnnotationSupportTabs() {

    //NOT IMPLEMENTED YET, NEEDS WORK

    // Support tabs
    textarea = jQuery('#newAnnotationText');
     textarea.keydown(function(event) {
        // Support tab on textarea
        if (event.keyCode == 9) { // Tab was pressed
            //console.log('tab');
            var text = textarea.val();
            var pos = textarea.prop('selectionStart');

            var textWithTab = text.slice(0, pos) + '    ' + text.slice(pos, text.length);
            var newpos = pos + 4;

            textarea.val(textarea.val()+ "    ");
            setSelectionRange(textarea, newpos, newpos);
            return false;
        }
    });
}

function setSelectionRange(input, selectionStart, selectionEnd) {
  if (input.setSelectionRange) {
    input.focus();
    input.setSelectionRange(selectionStart, selectionEnd);
  }
  else if (input.createTextRange) {
    var range = input.createTextRange();
    range.collapse(true);
    range.moveEnd('character', selectionEnd);
    range.moveStart('character', selectionStart);
    range.select();
  }
}

/**
 * Submit a new annotation edit.
 * @param annotationId - id of the annotation that has been edited
 * @param annotationText - new text for the annotation
 */
function submitAnnotationEdit(annotationId, annotationText) {
    // Save new annotation to currentdigraph
    for(var i = 0; i < currentDigraph.annotations.length; i++) {
        var note = currentDigraph.annotations[i];
        if (note.annotation_id == annotationId) {
            note.text = annotationText;
        }
    }

    //Now the edit icon will hover
    if (annotationText.length > 0) {
        jQuery('#_annotation' + annotationId + ' span .editAnnotationIcon').css('display', 'inherit');
    }

    // Allow for saving
    isWorkflowSaved = false;
    lastCall = "submitAnnotationEdit";
    saveTemporaryWorkflowTransient(currentDigraph.id, null, null, null, null);
    wfEnableSaveButton();

    // Update the text of the annotation preview in the workflow
    var linkedText = Autolinker.link( annotationText, {
        newWindow: true,
        urls: true,
        className: 'linkInAnnotation'
    } );
    jQuery('#annotation_' + annotationId + ' .annotationWfPreview').html(linkedText);
    jQuery('#annotation_' + annotationId + ' .annotationWfPreview').val(annotationText);

    // If the text is too large for the annotation, display a "View More" button
    let previewSelect = '#annotation_' + annotationId + ' .annotationWfPreview'
    if (jQuery(previewSelect)[0].scrollHeight > jQuery(previewSelect).innerHeight()) {
        let viewMoreButton = jQuery('<span class="editAnnotationIcon viewMoreAnnotation" >' +
                '(view entire note)</span>');
        jQuery('#annotation_' + annotationId).append(viewMoreButton);
    }

    // Open edit screen when clicking view more
    setAnnotationEditFunctionality('#annotation_' + annotationId + ' .editAnnotationIcon');

    // Add the functionality to view the whole note when clicking "(view entire note)"
    /*setTimeout(function(){setAnnotationEditFunctionality('#annotation_' +
        annotationId + ' .annotationWfPreview .editAnnotationIcon');
    }, 200);*/
    setAnnotationEditFunctionality('#annotation_' +
        annotationId + ' .annotationWfPreview .editAnnotationIcon');
    /* commented out because it sets a reduntant event listener
    setAnnotationEditFunctionalityDoubleClick('#annotation_' +
        annotationId + ' .annotationWfPreview');*/
}

/**
 * Handle the movement/dragging of annotations accross the screen
 * @param e - the movement event
 */
function moveAnnotation(e) {
    var note = e.currentTarget;
    var noteId = jQuery(note).attr('id').replace('annotation_','');

    // Update the left and top css attributes of the moved annotation
    for (var i = 0; i < currentDigraph.annotations.length; i++) {
        var currNote = currentDigraph.annotations[i];
        if (currNote.annotation_id == noteId) {
            currNote.left = jQuery(note).css('left');
            currNote.top = jQuery(note).css('top');
            break;
        }
    }

    if (!currentDigraph.isView) {
        // Allow the user to save the new position of the annotation
        isWorkflowSaved = false;
        lastCall = "moveAnnotation";
        wfEnableSaveButton();
    }
}

/*
 * When a new annnotation is dragged onto the screen, this function is
 * called to create the new box.
 */
function placeNewAnnotation(event, draggable) {
    if (currentDigraph.annotations == null || currentDigraph.annotations == undefined) {
        // Annotations has not be instantiated yet
        currentDigraph.annotations = [];
    }

    var draggableParent = jQuery('#' + draggable[0].id).parent();
    parentDiv = draggableParent.closest('.contextArea').attr('id');
    var compName = jQuery('#' + draggable[0].id + ' .compName').text();

    // The annotation div.
    var processAnnotation = null;

    var top = null;
    var left = null;

    // Height of datashop header
    var headerHeight = 0;
    if (jQuery('body > #ls-header').length > 0) {
        headerHeight = jQuery('body > #ls-header').outerHeight();
    }

    // Height of help button <tr>
    var helpRowHeight = 0; // Ignored for now in LS

    var draggableZoomFactor = (1.16 / (percentZoom + 0.16));

    var componentCenterY = annotationHeight * 0.25 * draggableZoomFactor;
    var dragDropOffsetY = jQuery('#wfStatusBar').height()
        + headerHeight + helpRowHeight;

    // Handle x offsets for drop
    var componentCenterX = annotationWidth * 0.5 * draggableZoomFactor;
    var startOfComponentX = jQuery('#process-selector-div').width();
    var dragDropOffsetX = componentCenterX;

    // The annotation is being dropped onto the workspace for the first time.
    //processAnnotation = draggable.clone(false, false);
    var divName = draggableParent.attr('name');
    processAnnotation = jQuery('<div id="annotation" name="' + divName + '" class="process-component ui-draggable ui-draggable-handle jsplumb-endpoint-anchor jsplumb-connected">'
            + '<div id="title_' + draggable[0].id + '" class="componentTitle">' + compName + '</div>'
            + '<span class="compName">' + compName + '</span>'
            + '</div>');

    // Setup the temporary annotation id ('annotation' + random number that is not existent in dom)
    var annotationStr = 'annotation';//processAnnotation.attr("name").toLowerCase();

    // Unique annotation id
    // Generate a 6-character (zero padded) random number
    var numRand = ("00000" + Math.floor(Math.random() * 999999)).slice (-6);

    // Set temp annotation Id
    var annotationId = formatHeading(annotationStr)
        + "-" + numRand ;

    // It will probably be unique, but let's make sure.
    while (jQuery('#' + annotationId).length > 0) {
        numRand = ("00000" + Math.floor(Math.random() * 999999)).slice (-6);
        annotationId = formatHeading(annotationStr)
            + "-" + numRand ;
    }

    // Create the new annotation in the database.  When this is complete, use the new id created in db
    var newNoteObj = {'text': '', 'tempId': annotationId};
    new Ajax.Request('WorkflowEditor', {
        parameters : {
            requestingMethod: 'WorkflowEditorServlet.createAnnotation',
            workflowId : currentDigraph.id,
            newAnnotation : newNoteObj
        },
        onComplete : createAnnotationCompleted,
        beforeSend : wfShowStatusIndicatorModal(false, "Processing..."),
        onSuccess : wfHideStatusIndicator,
        onException : function(request, exception) {
            wfHideStatusIndicator(); throw(exception);
        }
    });

    // Calculate the left and top css attributes of this new annotation
    if(event.pageX > jQuery('#process-selector-div').width()) {
        left = event.pageX * draggableZoomFactor
            - dragDropOffsetX
            - jQuery(document).scrollLeft()
            - startOfComponentX;
        //left = parseInt(draggable.css('left'), 10);
        processAnnotation.css('left', left);
    }

    top = event.pageY * draggableZoomFactor
        - dragDropOffsetY
        - jQuery(document).scrollTop()
        + jQuery('#process-div').scrollTop();
    //top = parseInt(draggable.css('top'), 10);
    processAnnotation.css('top', top);

    // Make dropped/dragged annotation draggable, but keep it contained to the workspace
    jQuery(processAnnotation).draggable({
        scroll: true,
        containment : '#process-div',
        cursor : 'move',
        snap : '#process-div',
        start: startFix,
        drag: dragFix
    });

    // Create new annotation object to save to digraph
    var newAnnotationObj = {};
    newAnnotationObj.annotation_id = annotationId;
    newAnnotationObj.left = left;
    newAnnotationObj.top = top;
    newAnnotationObj.text = '';

    // Save new annotation and add it to the wf interface
    addNewAnnotationToCurrentDigraph(newAnnotationObj);
    paintAnnotation(newAnnotationObj);

    //dirtyBits[id] = DIRTY_OPTION;
    isWorkflowSaved = false;
    lastCall = "handleDropEvent";
    saveTemporaryWorkflowTransient(currentDigraph.id, null, null, null, null);

    wfEnableSaveButton();

    return processAnnotation;
}

/**
 * Callback after creating the component in the db
 */
function createAnnotationCompleted(data) {
    var newAnnotationId = data.responseJSON.annotation_id;

    var tempId = data.request.parameters.newAnnotation.tempId;

    // Get the new annotation and set it's ID
    var allNotes = currentDigraph.annotations;
    for (var i = 0; i < allNotes.length; i++) {
        var currNote = allNotes[i];
        if (currNote.annotation_id == tempId) {
            currNote.annotation_id = newAnnotationId;
            break;
        }
    }
    // Change the ID of the temporary ID for the annotation
    jQuery('#annotation_'+tempId).attr('id', 'annotation_'+newAnnotationId);

    // Add ability to edit new annotation
    //setAnnotationEditFunctionality('#annotation_'+newAnnotationId+' .annotationWfPreview');
    setAnnotationEditFunctionality('#annotation_'+newAnnotationId+' span .editAnnotationIcon');
    setAnnotationEditFunctionalityDoubleClick('#annotation_'+newAnnotationId+' .annotationWfPreview');

    // Handle delete component
    jQuery('#annotation_'+newAnnotationId+' .deleteAnnotationIcon').click(deleteAnnotationConfirm);
}

/**
 * Add new Annotation to current digraph
 */
function addNewAnnotationToCurrentDigraph(newAnnotation) {
    currentDigraph.annotations.push(newAnnotation);
}

/**
 * Display dialogue to confirm if user wants to delete an annotation
 */
function deleteAnnotationConfirm() {
    var parentDiv = this.closest('.workflowAnnotation');
    var annotationId = jQuery(parentDiv).attr('id').replace('annotation_','');
    if (!currentDigraph.isView) {
        jQuery('<div />', {
            id : 'wfDeleteAnnotationConfirmationDialog'
        }).html(
            'Are you sure you want to delete the annotation?')
        .dialog({
            open : function() {
                jQuery('.ui-button').focus();
            },
            autoOpen : true,
            autoResize : true,
            resizable : false,
            width : 400,
            height : "auto",
            title : 'Delete Annotation Confirmation',
            buttons : {
                'Yes' : function() {
                    deleteAnnotation(annotationId);
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
    }
}

/**
 * Delete annotation from digraph.  When the user saves the wf, the
 * annotation will be deleted from the database
 */
function deleteAnnotation(annotationId) {
    // Remove from interface
    jQuery('#annotation_'+annotationId).remove();

    // Remove from Digraph
    var allNotes = currentDigraph.annotations;
    for (var i = 0; i < allNotes.length; i++) {
        if (allNotes[i].annotation_id == annotationId) {
            allNotes.splice(i, 1);
        }
    }

    isWorkflowSaved = false;

    lastCall = "deleteAnnotation";
    saveTemporaryWorkflowTransient(currentDigraph.id, null, null, null, null);
    wfEnableSaveButton();
}
