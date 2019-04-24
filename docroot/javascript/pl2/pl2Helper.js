
//var SYSTEM_RESOURCE = "system resource";

var DIALOG_TITLE_PADDING_OFFSET = 30;


/**
 * Given a resource id, display the resource in a popup after getting the
 * resource from the server
 * @param resourceId
 * @returns
 */
function displayResource(resourceId, resourceType, isMostRecentSession) {

    // Get the resource link then display it in displayResourceGetCompleted()
    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "PL2Servlet.getResourceLink",
            resourceType: resourceType,
            resourceId: resourceId,
            isMostRecentSession: isMostRecentSession,
            ajaxRequest: "true"
        },
        onComplete: displayResourceGetCompleted,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/**
 * Resource has been received from the server.  Open it in a dialog.
 * @param data - response from server
 */
function displayResourceGetCompleted(data) {
    let json = data.responseJSON;
    let links = [];
    if (json != undefined) {
        if (json.resourceLinks != undefined) {
            links = json.resourceLinks;
        }
    }

    if (links == null || links.length == 0) {
        alert('There is currently no information about this resource.');
        return;
    }

    let dialogId = 'ResourcePopup_' + data.request.parameters.resourceId;
    openResourceId = dialogId;

    let dialogHtml = jQuery('<a />')
            .text('Open in new window')
            .attr('href',  links[0])
            .attr('target', '_blank')[0]
            .outerHTML;
    dialogHtml += jQuery('<iframe />')
            .addClass('resourceIframe')
            .attr('src', links[0])[0]
            .outerHTML;

    let isMostRecentSession = data.request.parameters.isMostRecentSession;
    // If this is in the resource library, add the add resource button
    let buttons = {
          'Add Resource' : function() {
              addResourceToPlan(jQuery(this).attr('resourceType'),
                      jQuery(this).attr('resourceId'),
                      jQuery('body').data("planId"));
              //alert('This feature is coming soon :)');
          },
          'Cancel' : function() {
              jQuery(this).dialog('close');
          }
    }
    if (jQuery('.resourceLibrary').length == 0) {
        // Not in the resource library, in student dashboard page
        if (!isMostRecentSession) {
            buttons = {};
        } else {
            // This is the most recent session, you can delete the resource from the plan
            buttons = {
                'Remove Resource From Plan' : function() {
                    removeResourceFromPlan(jQuery(this).attr('resourceType'),
                            jQuery(this).attr('resourceId'),
                            jQuery('body').data("planId"));
                    //alert('This feature is coming soon :)');
                },
                'Cancel' : function() {
                    jQuery(this).dialog('close');
                }
            }
        }
    }

    jQuery('<div />', {
        id : dialogId
    }).html(dialogHtml).dialog({
        open : function() {
            jQuery('.ui-button').focus();
        },
        autoOpen : true,
        autoResize: true,
        resizable : true,
        minWidth: 300,
        minHeight: 300,
        width: 'auto',
        modal : true,
        buttons : buttons,
        open : function() {
            // Click outside to close
            jQuery('.ui-widget-overlay').bind('click', function() {
                jQuery("#" + openResourceId).dialog('close');
            });

            jQuery('.ui-dialog-titlebar-close').hide();
        },
        close : function() {
            jQuery(this).remove();
        }
    }).attr('resourceId', data.request.parameters.resourceId)
    .attr('resourceType', data.request.parameters.resourceType);
}

/**
 * Set functionality for any resources on the page
 * if you click on them, it will give you a popup
 */
jQuery(document).ready(function() {
    jQuery('.resource').off('click');
    jQuery('.resource').click(resourceClicked);
});

/**
 * Resource clicked, display it
 * @param resource - target clicked
 * @returns
 */
function resourceClicked(resource) {
    displayResource(jQuery(this).attr('value'),
            jQuery(this).attr('resourceType'),
            jQuery(this).parent().hasClass('mostRecentSession'));
}

/**
 * Submit a form to add a resource to a student's plan
 * @param resourceType
 * @param resourceId
 * @param planId
 * @returns refreshes page
 */
function addResourceToPlan(resourceType, resourceId, planId) {
    let form = jQuery('<form />')
            .attr('id', 'addResourceToPlanForm')
            .attr('method', 'post')
            .attr('action', 'PL2');
    form.append(jQuery('<input />')
            .attr('type', 'hidden')
            .attr('name', 'requestingMethod')
            .attr('value', 'PL2Servlet.addResourceToPlan'));
    form.append(jQuery('<input />')
            .attr('type', 'hidden')
            .attr('name', 'resourceType')
            .attr('value', resourceType));
    form.append(jQuery('<input />')
            .attr('type', 'hidden')
            .attr('name', 'resourceId')
            .attr('value', resourceId));
    form.append(jQuery('<input />')
            .attr('type', 'hidden')
            .attr('name', 'planId')
            .attr('value', planId));
    form.append(jQuery('<input />')
            .attr('type', 'hidden')
            .attr('name', 'studentId')
            .attr('value', jQuery('body').data('studentId')));
    form.append(jQuery('<input />')
            .attr('type', 'hidden')
            .attr('name', 'advisorId')
            .attr('value', jQuery('body').data('advisorId')));
    form.append(jQuery('<input />')
            .attr('type', 'hidden')
            .attr('name', 'strategyOptionId')
            .attr('value', jQuery('body').data('strategyOptionId')));
    form.append(jQuery('<input />')
            .attr('type', 'hidden')
            .attr('name', 'sessionId')
            .attr('value', jQuery('body').data('sessionId')));
    jQuery('body').append(form);
    jQuery('#addResourceToPlanForm').submit();
}


/**
 * Submit a form to remove a resource from a plan, then refresh page
 * @param resourceType
 * @param resourceId
 * @param planId
 */
function removeResourceFromPlan(resourceType, resourceId, planId) {
    let form = jQuery('<form />')
        .attr('id', 'removeResourceFromPlanForm')
        .attr('method', 'post')
        .attr('action', 'PL2');

    form.append(jQuery('<input type="hidden" name="requestingMethod" value="PL2Servlet.removeResourceFromPlan" />'));
    form.append(jQuery('<input type="hidden" name="resourceId" value="' + resourceId + '" />'));
    form.append(jQuery('<input type="hidden" name="resourceType" value="' + resourceType + '" />'));
    form.append(jQuery('<input type="hidden" name="advisorId" value="' + jQuery('body').data('advisorId') + '" />'));
    form.append(jQuery('<input type="hidden" name="studentId" value="' + jQuery('body').data('studentId') + '" />'));
//    form.append(jQuery('<input type="hidden" name="strategyOptionId" value="' + jQuery('body').data('strategyOptionId') + '" />'));
//    form.append(jQuery('<input type="hidden" name="sessionId" value="' + jQuery('body').data('sessionId') + '" />'));
    form.append(jQuery('<input type="hidden" name="planId" value="' + planId + '" />'));

    jQuery('body').append(form);
    jQuery('#removeResourceFromPlanForm').submit();
}


/**
 * Open dialog to edit the weekly objective
 */
function editObjectiveDialog() {

    let buttons = {
            'Save Objective' : function() {
                updateObjective(jQuery('body').data("planId"));
            },
            'Cancel' : function() {
                jQuery(this).dialog('close');
            }
    }

    let dialogId = 'EditObjectivePopup_' + jQuery('body').data("planId");
    editObjectiveId = dialogId;

    let dialogHtml = jQuery('<div />');

    dialogHtml.append(jQuery('<div />')
        .text('Type a new objective for this week: ')
        .attr('class', 'newObjectiveDescription'));


    dialogHtml.append(jQuery('<textarea />')
        .attr('id', 'newObjective')
        .attr('class', 'dialogTextBox')
        .attr('placeholder', 'Enter a new objective')
        .attr('maxlength','2048'));

    let dialogTitle = 'New Objective';
    jQuery('<div />', {
        id : dialogId
    }).html(dialogHtml).dialog({
        open : function() {
            jQuery('.ui-button').focus();
        },
        autoOpen : true,
        autoResize: true,
        resizable : true,
        minWidth: 300,
        minHeight: 300,
        width: 'auto',
        modal : true,
        dialogClass: 'editObjectiveDialog',
        title : dialogTitle,
        buttons : buttons,
        open : function() {
            // Click outside to close
            jQuery('.ui-widget-overlay').bind('click', function() {
                jQuery("#" + editObjectiveId).dialog('close');
            });

            //jQuery('.ui-dialog-titlebar-close').hide();
        },
        close : function() {
            jQuery(this).remove();
        }
    });
}

/**
 * Submit a form to update the weekly objective. Refresh page.
 * @param planId
 */
function updateObjective(planId) {
    let form = jQuery('<form />')
        .attr('id', 'updateObjectiveForm')
        .attr('method', 'post')
        .attr('action', 'PL2');
    form.append(jQuery('<input />')
        .attr('type', 'hidden')
        .attr('name', 'requestingMethod')
        .attr('value', 'PL2Servlet.updateObjective'));
    form.append(jQuery('<input />')
        .attr('type', 'hidden')
        .attr('name', 'planId')
        .attr('value', planId));
    form.append(jQuery('<input />')
        .attr('type', 'hidden')
        .attr('name', 'newObjective')
        .attr('value', jQuery('#newObjective').val()));
    form.append(jQuery('<input />')
        .attr('type', 'hidden')
        .attr('name', 'studentId')
        .attr('value', jQuery('body').data('studentId')));
    form.append(jQuery('<input />')
        .attr('type', 'hidden')
        .attr('name', 'advisorId')
        .attr('value', jQuery('body').data('advisorId')));
    jQuery('body').append(form);
    jQuery('#updateObjectiveForm').submit();
}

/**
 * Open a dialog to add a reflection
 */
function addReflectionDialog() {
    let sessionId = jQuery(this).attr('sessionId');
    let buttons = {
            'Add Reflection' : function() {
                //updateObjective(jQuery('body').data("planId"));
                addReflection(jQuery('#' + addReflectionId).attr('sessionId'),
                        jQuery('#newReflection').val(), false);
            },
            'Cancel' : function() {
                jQuery(this).dialog('close');
            }
    }

    let dialogId = 'addReflectionPopup_' + sessionId;
    addReflectionId = dialogId;

    let dialogHtml = jQuery('<div />');

    dialogHtml.append(jQuery('<div />')
        .text('Type a reflection about the student: ')
        .attr('class', 'newObjectiveDescription'));


    dialogHtml.append(jQuery('<textarea />')
        .attr('id', 'newReflection')
        .attr('class', 'dialogTextBox')
        .attr('placeholder', 'Enter a reflection or note')
        .attr('maxlength','2048'));


    let dialogTitle = 'New Reflection';
    jQuery('<div />', {
        id : dialogId
    }).html(dialogHtml).dialog({
        open : function() {
            jQuery('.ui-button').focus();
        },
        autoOpen : true,
        autoResize: true,
        resizable : true,
        minWidth: 300,
        minHeight: 300,
        width: 'auto',
        modal : true,
        dialogClass: 'editObjectiveDialog',
        title : dialogTitle,
        buttons : buttons,
        open : function() {
            // Click outside to close
            jQuery('.ui-widget-overlay').bind('click', function() {
                jQuery("#" + addReflectionId).dialog('close');
            });
        },
        close : function() {
            jQuery(this).remove();
        }
    }).attr('sessionId', sessionId);
}


/**
 * Submit a form to add a new reflection about a student. Then refresh page.
 * @param sessionId - session in which the reflection was made
 * @param newReflectionText - text of the new reflection
 * @param isBackground - whether or not the reflection is background information
 */
function addReflection(sessionId, newReflectionText, isBackground) {
    let form = jQuery('<form />')
        .attr('id', 'addReflectionForm')
        .attr('method', 'post')
        .attr('autocomplete', 'off')
        .attr('action', 'PL2');
    if (isBackground) {
        form.append(jQuery('<input />')
            .attr('type', 'hidden')
            .attr('name', 'requestingMethod')
            .attr('value', 'PL2Servlet.addBackground'));
    } else {
        form.append(jQuery('<input />')
            .attr('type', 'hidden')
            .attr('name', 'requestingMethod')
            .attr('value', 'PL2Servlet.addReflection'));
    }
    form.append(jQuery('<input />')
        .attr('type', 'hidden')
        .attr('name', 'sessionId')
        .attr('value', sessionId));
    form.append(jQuery('<input />')
        .attr('type', 'hidden')
        .attr('autocomplete', 'off')
        .attr('name', 'newReflectionText')
        .attr('value', jQuery('#newReflection').val()));
    form.append(jQuery('<input />')
        .attr('type', 'hidden')
        .attr('name', 'studentId')
        .attr('value', jQuery('body').data('studentId')));
    form.append(jQuery('<input />')
        .attr('type', 'hidden')
        .attr('name', 'advisorId')
        .attr('value', jQuery('body').data('advisorId')));
    jQuery('body').append(form);
    jQuery('#addReflectionForm').submit();
}

/**
 * Dialog for adding background information
 */
function addBackgroundDialog() {
    let sessionId = jQuery(this).attr('sessionId');
    let buttons = {
            'Add Background' : function() {
                addReflection(jQuery('#' + addBackgroundId).attr('sessionId'),
                        jQuery('#newReflection').val(), true);
            },
            'Cancel' : function() {
                jQuery(this).dialog('close');
            }
    }

    let dialogId = 'addBackgroundPopup_' + sessionId;
    addBackgroundId = dialogId;

    let dialogHtml = jQuery('<div />');

    dialogHtml.append(jQuery('<div />')
        .text('Type a note about the background of the student: ')
        .attr('class', 'newObjectiveDescription'));


    dialogHtml.append(jQuery('<textarea />')
        .attr('id', 'newReflection')
        .attr('class', 'dialogTextBox')
        .attr('placeholder', 'Enter a reflection or note')
        .attr('maxlength','2048'));

    let dialogTitle = 'New Background';
    jQuery('<div />', {
        id : dialogId
    }).html(dialogHtml).dialog({
        open : function() {
            jQuery('.ui-button').focus();
        },
        autoOpen : true,
        autoResize: true,
        resizable : true,
        minWidth: 300,
        minHeight: 300,
        width: 'auto',
        modal : true,
        dialogClass: 'editObjectiveDialog',
        title : dialogTitle,
        buttons : buttons,
        open : function() {
            // Click outside to close
            jQuery('.ui-widget-overlay').bind('click', function() {
                jQuery("#" + addBackgroundId).dialog('close');
            });
        },
        close : function() {
            jQuery(this).remove();
        }
    }).attr('sessionId', sessionId);
}


/**
 * Dialog for creating a new resource
 */
function createResourceDialog() {
    let planId = jQuery('body').data("planId");
    let advisorId = jQuery('body').data("advisorId");
    let studentId = jQuery('body').data("studentId");
    let strategyOptionId = jQuery('body').data("strategyOptionId");
    let sessionId = jQuery('body').data("sessionId");

    let buttons = {
            'Create Resource' : function() {
                createResource(jQuery('body').data("advisorId"),
                        jQuery('body').data("studentId"),
                        jQuery('body').data("strategyOptionId"),
                        jQuery('body').data("planId"),
                        jQuery('body').data("sessionId"),
                        jQuery('#newResourceSubject').val(),
                        jQuery('#newResourceUrl').val());
            },
            'Cancel' : function() {
                jQuery(this).dialog('close');
            }
    }

    let dialogId = 'createResourcePopup_' + sessionId;
    createResourceId = dialogId;

    let dialogHtml = jQuery('<div />');

    dialogHtml.append(jQuery('<div />')
        .text('Type a short description of this resource')
        .attr('class', 'newObjectiveDescription'));

    dialogHtml.append(jQuery('<textarea />')
        .attr('id', 'newResourceSubject')
        .attr('class', 'dialogTextBox')
        .attr('placeholder', 'Short description of resource')
        .attr('maxlength','2048'));

    dialogHtml.append(jQuery('<div />')
        .text('Enter the URL of the link for this resource')
        .attr('class', 'newObjectiveDescription'));

    dialogHtml.append(jQuery('<input />')
        .attr('id', 'newResourceUrl')
        .attr('class', 'dialogTextInput')
        .attr('placeholder', 'url')
        .attr('maxlength','256'));

    let dialogTitle = 'New Resource';
    jQuery('<div />', {
        id : dialogId
    }).html(dialogHtml).dialog({
        open : function() {
            jQuery('.ui-button').focus();
        },
        autoOpen : true,
        autoResize: true,
        resizable : true,
        minWidth: 300,
        minHeight: 300,
        width: 'auto',
        modal : true,
        dialogClass: 'editObjectiveDialog',
        title : dialogTitle,
        buttons : buttons,
        open : function() {
            // Click outside to close
            jQuery('.ui-widget-overlay').bind('click', function() {
                jQuery("#" + dialogId).dialog('close');
            });
        },
        close : function() {
            jQuery(this).remove();
        }
    });
}

/**
 * Create a resource by submitting a form
 * @param advisorId
 * @param studentId
 * @param strategyOptionId
 * @param planId
 * @param sessionId
 * @param subject
 * @param url
 */
function createResource(advisorId, studentId, strategyOptionId, planId, sessionId, subject, url) {
    let form = jQuery('<form />')
        .attr('id', 'createResourceForm')
        .attr('method', 'post')
        .attr('action', 'PL2');

    form.append(jQuery('<input type="hidden" name="requestingMethod" value="PL2Servlet.createResource" />'));
    form.append(jQuery('<input type="hidden" name="advisorId" value="' + advisorId + '" />'));
    form.append(jQuery('<input type="hidden" name="studentId" value="' + studentId + '" />'));
    form.append(jQuery('<input type="hidden" name="strategyOptionId" value="' + strategyOptionId + '" />'));
    form.append(jQuery('<input type="hidden" name="sessionId" value="' + sessionId + '" />'));
    form.append(jQuery('<input type="hidden" name="planId" value="' + planId + '" />'));
    form.append(jQuery('<input type="hidden" name="subject" value="' + subject + '" />'));
    form.append(jQuery('<input type="hidden" name="attachmentPath" value="' + url + '" />'));

    jQuery('body').append(form);
    jQuery('#createResourceForm').submit();
}

/**
 * Get the html for the goal update dialog
 * @param stuFirstName - first name of the student that this goal is for
 */
function updateGoalHtml(stuFirstName) {
    let goalSettingDiv = jQuery('<div class="kickoffQuestion goalInfo" />');

    let currentGrade = 5.3; //TODO: support this dynamically
    let timeRec = 30;
    let problemRec = 30;
    let accRec = 30;

    let stuCurrentGrade = jQuery(
            '<div><div class="questionText">' + stuFirstName + ' is currently at a </div>'
            + '<input id="currentGrade" type="number" value="' + currentGrade + '" />'
            + '<div class="questionText"> Math grade level.</div></div>');

    let stuTargetGrade = jQuery(
            '<div> <div class="questionText">' + stuFirstName + '\'s target grade level is </div>'
            + '<input id="targetGrade" type="number" value="' + Math.floor(1 + currentGrade) + '" />'
            + '</div>');

    let completionDate = jQuery(
            '<div> <div class="questionText">This goal should be achieved by: </div>'
            + '<input type="text" id="goal_completion_date"  />'
            + '</div>');

    let meetingFrequency = jQuery(
            '<div style="display:none"> You meet '
            + '<select id="meetingFreq"><option value="per_week">Weekly</option></select><br/>'
            + 'on this/these days:<div class="checkboxContainer daysOfWeekCheckbox">'
                + '<input type="checkbox" value="monday"    id="monday" checked>   <label for="monday" class="selectedResource">   Monday</label><br/>'
                + '<input type="checkbox" value="tuesday"   id="tuesday">  <label for="tuesday">  Tuesday</label><br/>'
                + '<input type="checkbox" value="wednesday" id="wednesday"><label for="wednesday">Wednesday</label><br/>'
                + '<input type="checkbox" value="thursday"  id="thursday"> <label for="thursday"> Thursday</label><br/>'
                + '<input type="checkbox" value="friday"    id="friday">   <label for="friday">   Friday</label><br/>'
            + '</div>'
            + '</div>');

//    let meetingFrequency = jQuery(
//            '<div> You meet '
//            + '<select id="meetingFreq"><option value="per_week">Weekly</option></select><br/>'
//            + 'on this day (Select Monday for the demo):<select id="meetingDOW" class="checkboxContainer daysOfWeekCheckbox">'
//                + '<option value="monday">   Monday</option>'
//                + '<option value="tuesday">  Tuesday</option>'
//                + '<option value="wednesday">Wednesday</option>'
//                + '<option value="thursday"> Thursday</option>'
//                + '<option value="friday">   Friday</option>'
//            + '</select>'
//            + '</div>');



    let recommendationDiv = jQuery(
          '<div><div class="questionText">The PL<span class="exponent">2</span> App recommends that '
            +  stuFirstName + ': </div></div>');

    let objective = jQuery(
          '<div id="objective">'
          + '<span>Spends <input id="timeRec" type="number" value="'+timeRec+'"> minutes per week working in Mathia</span><br/>'
          + '<span>Or completes <input id="problemsRec" type="number" value="'+problemRec+'"> problems</span><br/>'
          + '<span>While getting about <input id="accRec" type="number" value="'+accRec+'">% of the problems correct</span></div>');

    goalSettingDiv.append(stuCurrentGrade)
            .append(stuTargetGrade)
            .append(completionDate)
            .append(meetingFrequency)
            .append(recommendationDiv)
            .append(objective);

    return goalSettingDiv;
}



/**
 * Dialog for updating the goal of a student
 */
function updateGoalDialog() {
    let buttons = {
            'Save' : function() {
                createNewGoal();
            },
            'Cancel' : function() {
                jQuery(this).dialog('close');
            }
    }

    let dialogId = 'EditGoalPopup_' + jQuery('body').data("planId");
    editGoalId = dialogId;

    let stuFirstName = jQuery('body').data("studentFirstName");
    let grade = 'TODO: add grade';

    let dialogHtml = updateGoalHtml(stuFirstName);

    jQuery('<div />', {
        id : dialogId
    }).html(dialogHtml).dialog({
        open : function() {
            jQuery('.ui-button').focus();
        },
        autoOpen : true,
        autoResize: true,
        resizable : true,
        minWidth: 300,
        minHeight: 300,
        width: 'auto',
        modal : true,
        dialogClass: 'editObjectiveDialog',
        buttons : buttons,
        open : function() {
            // Instantiate date picker
            jQuery('#goal_completion_date').datepicker(
                    { defaultDate: 60 }); // TODO: set options for the picker
            jQuery('#goal_completion_date').datepicker('setDate', 60);

            // Click outside to close
            jQuery('.ui-widget-overlay').bind('click', function() {
                jQuery("#" + editGoalId).dialog('close');
            });
        },
        close : function() {
            jQuery(this).remove();
        }
    });
}

/**
 * Submit a form that creates a new goal for a student
 */
function createNewGoal() {
    // Create the form to create the new goal on the server
    let form = jQuery('<form />')
        .attr('id', 'createNewGoalForm')
        .attr('method', 'post')
        .attr('action', 'PL2');

    form.append(jQuery('<input type="hidden" name="requestingMethod" value="PL2Servlet.createNewGoal" />'));
    form = appendGoalUpdatesAsInputs(form);
    form.append(jQuery('<input type="hidden" name="advisorId" value="' + jQuery('body').data('advisorId') + '" />'));
    form.append(jQuery('<input type="hidden" name="studentId" value="' + jQuery('body').data('studentId') + '" />'));

    jQuery('body').append(form);
    jQuery('#createNewGoalForm').submit();
}



/**
 * The advisor has submitted the kickoff interview questions, submit via a form
 */
function submitInterviewQuestions() {
    let form = jQuery('<form />')
            .attr('id', 'submitKickoffInterviewForm')
            .attr('method', 'post')
            .attr('action', 'PL2');

    form.append(jQuery('<input type="hidden" name="requestingMethod" value="PL2Servlet.submitKickoffInterview" />'));

    // Go through the questions and add them to the form
    let qAndAs = [];
    let backgroundReflections = [];
    questions.forEach(function(question) {
        let qAndA = {};
        qAndA.question_id = question.id;
        qAndA.question_text = question.question_text;

        if (question.type === "textarea") {
            qAndA.answer_id = 'TextArea';
            qAndA.answer_text = jQuery('#' + question.id + ' textarea').val();
        } else if (question.type === "number") {
            qAndA.answer_id = 'Number';
            qAndA.answer_text = jQuery('#' + question.id + ' input[type="number"]').val();
        } else if (question.type === "multiCheckbox") {
            qAndA.answer_id = [];
            qAndA.answer_text = [];
            jQuery('#' + question.id + ' input:checked').each(function() {
                qAndA.answer_id.push(question.id);
                if (jQuery(this).hasClass('openEnded')) {
                    qAndA.answer_text.push(jQuery('#' + jQuery(this).attr('id') + 'openEnded').val());
                } else {
                    qAndA.answer_text.push(jQuery("label[for='" + jQuery(this).attr('id') + "']").text());
                }
            });
        }
        qAndAs.push(JSON.stringify(qAndA));

        if (question.is_background) { // Add these to the background reflections in the DB
            let ans = qAndA.answer_text;
            if (!Array.isArray(ans)) {
                ans = [ans]
            }
            ans.forEach(function(a) {
                if (a != undefined && a.length > 0) {
                    backgroundReflections.push(a);
                }
            });
        }
    });

    form.append(jQuery('<input type="hidden" name="questionsAndAnswers"/>')
            .attr('value', '['+qAndAs+']'));
            //.attr('value', JSON.stringify(qAndAs)));

    form.append(jQuery('<input type="hidden" name="backgroundReflections" />')
            .attr('value', '['+backgroundReflections+']'));

    form.append(jQuery('<input type="hidden" name="studentId" value="' + jQuery('body').data('studentId') + '" />'));
    form.append(jQuery('<input type="hidden" name="advisorId" value="' + jQuery('body').data('advisorId') + '" />'));

    // Get the new goal and objective
    form = appendGoalUpdatesAsInputs(form);

    jQuery('body').append(form);
    jQuery('#submitKickoffInterviewForm').submit();
}

/**
 * Append the goal updating information to a form
 * @param form - html form that doesn't have goal info yet
 * @returns the form with appended inputs "newGoal" and "newObjective"
 */
function appendGoalUpdatesAsInputs(form) {
    let goalGrade = jQuery('#targetGrade').val();
    let goalCompletionDate = jQuery('#goal_completion_date').val();
    let meetingFrequency = jQuery('#meetingFreq').val();
    let timeObjective = jQuery('#timeRec').val();
    let accObjective = jQuery('#accRec').val();
    let probObjective = jQuery('#problemsRec').val();

    form.append(jQuery('<input type="hidden" name="newGoalGrade" value="' + goalGrade + '" />'));
    form.append(jQuery('<input type="hidden" name="newGoalCompletionDate" value="' + goalCompletionDate + '" />'));
    form.append(jQuery('<input type="hidden" name="newObjectiveUnits" value="' + meetingFrequency + '" />'));
    form.append(jQuery('<input type="hidden" name="newObjectiveTime" value="' + timeObjective + '" />'));
    form.append(jQuery('<input type="hidden" name="newObjectivePropCorrect" value="' + accObjective + '" />'));
    form.append(jQuery('<input type="hidden" name="newObjectiveProblemsCompleted" value="' + probObjective + '" />'));

    return form
}


/**
 * Cookie handler function.  Sets a variable name, value and # days to keep it.
 */
function createCookie(name,value,days) {
    if (days) {
        var date = new Date();
        date.setTime(date.getTime()+(days*24*60*60*1000));
        var expires = "; expires="+date.toGMTString();
    } else {
        var expires = "";
    }
    document.cookie = name+"="+value+expires+"; path=/";
}

/**
 * Read a cookie given it's name
 * @param name - name of the cookie
 * @returns value of the cookie or null if it doesn't exist
 */
function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
}

/**
 * Delete a cookie
 * @param name
 */
function eraseCookie(name) {
    createCookie(name,"",-1);
}

/** Cookie name of the secret used for reverse hashing */
var HASH_SECRET_COOKIE_NAME = 'hash_secret_cookie';

/**
 * Get the value of the secret used for hashing
 */
function getHashSecret() {
    return readCookie(HASH_SECRET_COOKIE_NAME);
}

/**
 * Set the secret used in reverse hashing names
 * @param secret
 */
function setHashSecret(secret) {
    if (secret != null) {
        createCookie(HASH_SECRET_COOKIE_NAME, secret, 100);
    }
}

/**
 * If the hash secret has not been set, open a dialog to allow the user to set it
 */
function ensureHashSecretIsSet() {
    let secret = getHashSecret();

    if (secret == null) {
        openHashSecretDialog();
    }
}

function clearHashSecret() {
    eraseCookie(HASH_SECRET_COOKIE_NAME);
}

/**
 * Open a dialog that allows a user to enter a new hash secret
 */
function openHashSecretDialog() {
    let buttons = {
            'Save' : function() {
                setHashSecret(jQuery('#newHashSecret').val());
                jQuery(this).remove();
                ensureHashSecretIsSet();
            }
    }

    let dialogId = 'NewHashSecretPopup';
//    newHashDialogId = dialogId;

    // TODO: Ensure that the key is 8 characters long
    let dialogHtml = jQuery('<div class="newHashSecretDiv" />');

    dialogHtml.append(jQuery('<div class="newHashSecretTitle" />')
            .text('Please enter a secret key to decrypt student names'));

    dialogHtml.append(jQuery('<div class="newHashSecretDescription">')
            .text('This key is used to ensure that names are anonymous on the PL2 server.  '
                    + 'If you do not have the 8 character key, please contact your institution administrator'));

    dialogHtml.append(jQuery('<div>New Key: <input id="newHashSecret" /></div>'));

    jQuery('<div />', {
        id : dialogId
    }).html(dialogHtml).dialog({
        open : function() {
            jQuery('#newHashSecret').focus();
        },
        autoOpen : true,
        autoResize: true,
        resizable : true,
        minWidth: 300,
        minHeight: 300,
        width: 'auto',
        modal : true,
        dialogClass: 'editObjectiveDialog',
        buttons : buttons,
        open : function() {
//             Click outside to close
//            jQuery('.ui-widget-overlay').bind('click', function() {
//                jQuery("#" + editGoalId).dialog('close');
//            });
        },
        close : function() {
            jQuery(this).remove();
            ensureHashSecretIsSet();
        }
    });
}


function newSessionDialog() {
    let buttons = {
            'Submit' : function() {
                createNewSession();
            },
            'Cancel' : function() {
                jQuery(this).dialog('close');
            }
    }

    let dialogId = 'new_session_popup';
    newSessionPopupId = dialogId;

    let dialogHtml = jQuery('#start_session_questions').html();//jQuery('<div>An input <input type="text"/></div>');

    jQuery('<div />', {
        id : dialogId
    }).html(dialogHtml).dialog({
        open : function() {
            jQuery('#').focus();
        },
        autoOpen : true,
        autoResize: true,
        resizable : true,
        minWidth: 300,
        minHeight: 300,
        width: 'auto',
        modal : true,
        dialogClass: 'editObjectiveDialog',
        buttons : buttons,
        open : function() {
            // Click outside to close
            jQuery('.ui-widget-overlay').bind('click', function() {
                jQuery("#" + newSessionPopupId).dialog('close');
            });
        },
        close : function() {
            jQuery(this).remove();
        }
    });
}

function createNewSession() {
    let form = jQuery('<form />')
        .attr('id', 'startNewSessionForm')
        .attr('method', 'post')
        .attr('action', 'PL2');

    form.append(jQuery('<input type="hidden" name="requestingMethod" value="PL2Servlet.startNewSession" />'));

    form.append(jQuery('<input type="hidden" name="studentId" value="' + jQuery('body').data('studentId') + '" />'));
    form.append(jQuery('<input type="hidden" name="advisorId" value="' + jQuery('body').data('advisorId') + '" />'));

    // Go through the questions and add them to the form
    let qAndAs = [];
    let backgroundReflections = [];
    start_questions.forEach(function(question) {
        console.log(question);
        let qAndA = {};
        qAndA.question_id = question.id;
        qAndA.question_text = question.question_text;

        if (question.type === "textarea") {
            qAndA.answer_id = 'TextArea';
            qAndA.answer_text = jQuery('#new_session_popup #' + question.id + ' textarea').val();
        } else if (question.type === "number") {
            qAndA.answer_id = 'Number';
            qAndA.answer_text = jQuery('#new_session_popup #' + question.id + ' input[type="number"]').val();
        } else if (question.type === "multiCheckbox") {
            qAndA.answer_id = [];
            qAndA.answer_text = [];
            jQuery('#new_session_popup #' + question.id + ' input:checked').each(function() {
                qAndA.answer_id.push(question.id);
                if (jQuery(this).hasClass('openEnded')) {
                    qAndA.answer_text.push(jQuery('#new_session_popup #' + jQuery(this).attr('id') + 'openEnded').val());
                } else {
                    qAndA.answer_text.push(jQuery("#new_session_popup label[for='" + jQuery(this).attr('id') + "']").text());
                }
            });
        }
        qAndAs.push(JSON.stringify(qAndA));

        if (question.is_background) { // Add these to the background reflections in the DB
            let ans = qAndA.answer_text;
            if (!Array.isArray(ans)) {
                ans = [ans]
            }
            ans.forEach(function(a) {
                if (a != undefined && a.length > 0) {
                    backgroundReflections.push(a);
                }
            });
        }
    });

    form.append(jQuery('<input type="hidden" name="questionsAndAnswers"/>')
            .attr('value', '['+qAndAs+']'));

    form.append(jQuery('<input type="hidden" name="backgroundReflections" />')
            .attr('value', '['+backgroundReflections+']'));

    jQuery('body').append(form);
    jQuery('#startNewSessionForm').submit();
}

function endSessionInterviewDialog() {
    let buttons = {
            'Submit' : function() {
                submitEndSessionInterview();
            },
            'Cancel' : function() {
                jQuery(this).dialog('close');
            }
    }

    let dialogId = 'end_session_popup';
    endSessionPopupId = dialogId;

    let dialogHtml = jQuery('#end_session_questions').html();

    jQuery('<div />', {
        id : dialogId
    }).html(dialogHtml).dialog({
        open : function() {
            jQuery('#').focus();
        },
        autoOpen : true,
        autoResize: true,
        resizable : true,
        minWidth: 300,
        minHeight: 300,
        width: 'auto',
        modal : true,
        dialogClass: 'editObjectiveDialog',
        buttons : buttons,
        open : function() {
            // Click outside to close
            jQuery('.ui-widget-overlay').bind('click', function() {
                jQuery("#" + endSessionPopupId).dialog('close');
            });
        },
        close : function() {
            jQuery(this).remove();
        }
    });
}

function submitEndSessionInterview() {
    let form = jQuery('<form />')
        .attr('id', 'endSessionInterviewForm')
        .attr('method', 'post')
        .attr('action', 'PL2');

    form.append(jQuery('<input type="hidden" name="requestingMethod" value="PL2Servlet.submitEndSessionInterview" />'));

    form.append(jQuery('<input type="hidden" name="studentId" value="' + jQuery('body').data('studentId') + '" />'));
    form.append(jQuery('<input type="hidden" name="advisorId" value="' + jQuery('body').data('advisorId') + '" />'));

    // Go through the questions and add them to the form
    let qAndAs = [];
    let backgroundReflections = [];
    end_questions.forEach(function(question) {
        let qAndA = {};
        qAndA.question_id = question.id;
        qAndA.question_text = question.question_text;

        if (question.type === "textarea") {
            qAndA.answer_id = 'TextArea';
            qAndA.answer_text = jQuery('#end_session_popup #' + question.id + ' textarea').val();
        } else if (question.type === "number") {
            qAndA.answer_id = 'Number';
            qAndA.answer_text = jQuery('#end_session_popup #' + question.id + ' input[type="number"]').val();
        } else if (question.type === "multiCheckbox") {
            qAndA.answer_id = [];
            qAndA.answer_text = [];
            jQuery('#end_session_popup #' + question.id + ' input:checked').each(function() {
                qAndA.answer_id.push(question.id);
                if (jQuery(this).hasClass('openEnded')) {
                    qAndA.answer_text.push(jQuery('#end_session_popup #' + jQuery(this).attr('id') + 'openEnded').val());
                } else {
                    qAndA.answer_text.push(jQuery("#end_session_popup label[for='" + jQuery(this).attr('id') + "']").text());
                }
            });
        }
        qAndAs.push(JSON.stringify(qAndA));

        if (question.is_background) { // Add these to the background reflections in the DB
            let ans = qAndA.answer_text;
            if (!Array.isArray(ans)) {
                ans = [ans]
            }
            ans.forEach(function(a) {
                if (a != undefined && a.length > 0) {
                    backgroundReflections.push(a);
                }
            });
        }
    });

    form.append(jQuery('<input type="hidden" name="questionsAndAnswers"/>')
            .attr('value', '['+qAndAs+']'));

    form.append(jQuery('<input type="hidden" name="backgroundReflections" />')
            .attr('value', '['+backgroundReflections+']'));

    jQuery('body').append(form);
    jQuery('#endSessionInterviewForm').submit();
}