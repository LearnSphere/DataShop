//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2009
// All Rights Reserved
//
// Author: Kyle Cunningham
// Version: $Revision: 12491 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-08-05 16:38:04 -0400 (Wed, 05 Aug 2015) $
// $KeyWordsOff: $
//
// This javascript file contains all javascript for the Learning Curve Point Info Deatils
// feature.  LC Point Info as well as the rest of Learning Curve javascript is contained
// in LearningCurve.js (including the functions for hide/show).
//

var sortHelper = null;

/**
 * Get the LCPI details element (table).
 * @return the LCPI details element.
 */
function infoDetails() {
    return $('pointInfoDetailsValues').firstDescendant();
}

/**
 * Get the LCPI details action links.<b>
 * @return the LCPI details action links element.
 */
function lcpidActionLinks() {
    return $('pointInfoDetailsActionLinks');
}

/**
 * Get the LCPID select action paragraph.
 * @return the LCPID select paragraph.
 */
function actionSelect() {
    return $('lcpidActionSelect');
}

/**
 * Get the LCPID remove action paragraph.
 * @return the LCPID remove paragraph.
 */
function actionRemove() {
    return $('lcpidActionRemove');
}

/**
 * Get the LCPID select action link.
 * @return the LCPID select link.
 */
function actionSelectLink() {
    return $('lcpidActionSelectLink');
}

/**
 * Get the LCPID remove action link.
 * @return the LCPID remove link.
 */
function actionRemoveLink() {
    return $('lcpidActionRemoveLink');
}


/**
 * Get the pointInfoSkill, problem, etc areas.
 * @return an array of pointInfo areas for use.
 */
function pointInfoAreas() {
    return new Array($('pointInfoSkills'),
        $('pointInfoProblems'),
        $('pointInfoSteps'),
        $('pointInfoStudents'));

} // end pointInfoAreas()

/**
 * For each clickable LCPID measure, add an observer and set the
 * hover class.
 */
function registerLCPIDObservers() {
    pointInfoAreas().each( function(area) {
        area.observe('click', pointInfoDetailsClickHandler);
      if (!area.hasClassName('i_am_selected')) {
          area.addClassName('pointInfoDetailsActive');
      }
    });
} // end registerLCPIDObservers()

/**
 * For each clickable LCPID measure, remove the observer and the
 * hover class.
 */
function deregisterLCPIDObservers() {
    pointInfoAreas().each( function(area) {
        area.stopObserving('click', pointInfoDetailsClickHandler);
      area.removeClassName('.pointInfoDetailsActive');
      area.removeClassName('i_am_selected');
    });
} // end deregisterLCPIDObservers()

/**
 * Get the topId from the URL (we don't care if it is skill or student).
 * @return the top id (either topSkillId or topStudentId).
 */
function getTopId() {
   var url = window.location.search;
    if (url.indexOf("top") != -1) {
        if (url.indexOf("null") != -1) {
            // all data graph
            return null;
        } else {
            var topId =  /.*top\w+Id=(\d+).*/.exec(window.location.href)[1];
            return topId;
        }
    }
} // end getTopId

/**
 * Handles the 'click' event for each LCPID clickable measure (skill, problem, step & student).
 * First determine which measure was clicked, then call pointInfoDetailsRequest().
 * @param e the click event
 */
function pointInfoDetailsClickHandler(e) {
    var element = e.element();
    var measure;
    if (element.id == "pointInfoSkills") {
      measure = "skill";
    } else if (element.id == "pointInfoProblems") {
      measure = "problem";
    } else if (element.id == "pointInfoSteps") {
      measure = "step";
    } else if (element.id == "pointInfoStudents") {
        measure = "student";
    }
    pointInfoDetailsRequest(measure);

} // end pointInfoDetailsClickHandler()

/**
 * Process a request for LCPI Details.
 * @param measure the measure for details retrieval.
 * @param sortBy how the details should be sorted.
 * @param direction the sort direction.
 * Note: this method can be called without any params (which means we are relying on
 *	values stored in the context or defaults).
 */
function pointInfoDetailsRequest(measure, sortBy, direction) {
    // display the loading animation in the infoDetails area.
    var content = "<tr><td><img src='images/indicator.gif'/></td></tr>";
    infoDetails().update(content);
    $('pointInfoCol3').show();
    new Ajax.Request("LCPID", {
            parameters: {
                      requestingMethod: "LCPID.pointInfoDetailsRequest",
                      datasetId: dataset,
              type: "lcpid",
              topId: getTopId(),
              selectedMeasure: measure,
              sortBy: sortBy,
              datasetId: dataset,
              direction: direction },
      requestHeaders: { Accept: 'application/json;charset=UTF-8' },
      onSuccess: function(transport) {
          response = transport.responseText.evalJSON();
          pointInfoDetailsCallback(response);
         },
        onException: function(request, exception) { throw(exception); }
    });
} // end pointInfoDetailsRequest

/**
 * Handle the pointInfoDetailsRequest callback.  Determine which measure has been
 * selected by examining the response object.  If it was problem, then call the
 * server to gather problem hierarchy information.  Otherwise, populate the LCPID
 * area.
 * @param lcpid the server response, which has been evaulated from JSON into
 * 	a javascript object.
 */
function pointInfoDetailsCallback(lcpid) {
    if (lcpid.selectedMeasure == "problem" || lcpid.selectedMeasure == "step")  {
      problemHierarchyRequest(lcpid);
    } else {
      populateLCPID(response, null);
    }
} // end pointInfoDetailsCallback()

/**
 * Make a request to the server for the list of problem hierarchies so
 * we can populate the problem tooltips in the LCPID area.  Upon return
 * from the server, resort to LCPID display.
 * @param lcpid the lcpid object from the server.
 */
function problemHierarchyRequest(lcpid) {
    new Ajax.Request("LCPID", {
          parameters: {
                      requestingMethod: "LCPID.pointInfoDetailsRequest",
                      datasetId: dataset,
              type: "hierarchy",
                    datasetId: dataset,
              problemIds: lcpid.problemIds },
      requestHeaders: { Accept: 'application/json;charset=UTF-8' },
      onSuccess: function(transport) {
          hierarchy = transport.responseText.evalJSON();
          populateLCPID(lcpid, hierarchy.hierarchiesById);
         },
        onException: function(request, exception) { throw(exception); }
    });
} // end problemHierarchyRequest()

/**
 * Display the LCPI Details.
 * @param lcpid the lcpid object from the server [required].
 * @param hierarchy a list of problem ids and hierarchies from the server [optional].
 */
function populateLCPID(lcpid, hierarchy) {
    var measure = lcpid.selectedMeasure;
    var curveType = lcpid.curveType;
    var infoList = lcpid.infoDetailsList;
    var sortBy = lcpid.sortBy;
    var sortDirection = lcpid.sortDirection;
    var problemSelected = measure == "problem" ? true : false;

    highlightSelectedMeasure(measure);
    // clear out the infoDetails area
    infoDetails().update();

    // walk through the details list and build the table.
    for (var position = 0; position < infoList.length; position++) {
        var id = infoList[position].id
        var problemId = infoList[position].problemId;
        var name = infoList[position].name;
        var value = infoList[position].value;
        var freq = infoList[position].frequency;
        var spanId = measure + "_" + id;
        var KCs = infoList[position].kCs;
        var secondaryKCs = infoList[position].secondaryKCs;
        var content = "<tr>";
            content += "<td class='pointInfoDetailsName'>";
            content += "<span id='"+ spanId + "' class='pointInfoDetailsId'>";
            content += infoList[position].name.truncate(25);
            content += "</span></td>";
            content += "<td class='pointInfoDetailsValue'>";
            content += value;
            content += "</td>";
            content += "<td class='pointInfoDetailsFreq'>";
            content += freq;
            content += "</td>";
        content += "</tr>";
        infoDetails().insert({'bottom': content});
        // now add the tool tip.
        generateToolTip(measure, spanId, id, problemId, name,
                        value, hierarchy, freq, KCs, secondaryKCs);
    } // end for
    // insert the new row.
    $('pointInfoDetailsCol2Header').down().update(curveType);

    // create a sort helper to set up the arrows, click handlers, etc.
    if (sortHelper != null) {
      sortHelper.reset(measure, sortBy, sortDirection);
    } else {
      sortHelper = new SortHelper(measure, sortBy, sortDirection);
    }

    // if there are more than 50, show appropriate message and links
    updatePointInfoDetailsMessageAndLinks(measure);

    // call the Hide/Show servlet to set our state to 'show'
    hideShowRequest({
        requestingMethod: "LCPID.populateLCPID",
        datasetId: dataset,
        linkId: "pointInfoDetailsClose",
        hideShow: $("pointInfoDetailsClose").up().visible() ? "show" : "hide",
        selectedMeasure: measure
    });

} // end populateLCPID()

/**
 * Given a measure, highlight its 'area' and set the rest back to white.
 * @param measure the selected measure (student, skill, etc).  Passing in a null
 *	will result in reseting everything.
 */
function highlightSelectedMeasure(measure) {
    $$('.infoRow').each(function(row) {
        if (measure == null) {
            // remove the highlight (if there) and set the links to active. (this means the info details
            // box was closed)
            row.removeClassName("pointInfoDetailsSelected");
            row.down(1).addClassName("pointInfoDetailsActive");
        } else {
            var rowId = row.id.toLowerCase();
            if (rowId.include(measure)) {
                if (!row.hasClassName(".pointInfoDetailsSelected")) {
                  row.addClassName("pointInfoDetailsSelected");
                  row.down(1).removeClassName("pointInfoDetailsActive");
                  row.down(1).addClassName("i_am_selected");
                }
            } else {
                row.removeClassName("pointInfoDetailsSelected");
              row.down(1).addClassName("pointInfoDetailsActive");
            }
        }
    });
} // end highlightSelectedMeasure()

/**
 * Helper function to generate ToolTip javascript objects for each row
 * in the lcpid table.
 * @param measure the selected measure.
 * @param spanId the unique id for the span to which the tooltip will belong.
 * @param id the unique id of the measure (student_id, problem_id, etc), used
 * 	to get the corresponding problem hierarchy out of the hierarchy list.
 * @param problemId the id of the problem assigned to this measure (can be null)
 * @param name the name of the measure.
 * @param value the value for the selected curveType.
 * @param hierarchy the problem hierarchy (only used in the case where the selected
 * 	measure is 'problem' and can be null).
 * @param freq the number of observations
 * @param KCs if defined, the KCs for the Step
 * @param secondaryKCs if secondary KCM selected, the KCs for the Step
 */
function generateToolTip(measure, spanId, id, problemId, name,
                         value, hierarchy, freq, KCs, secondaryKCs)
{
    var params = { extraClasses: "infoDiv", fixed: "true" };

    // get rid of 'fixed' and look for '<input>' in content...

    if (hierarchy == null) {
        toolTipText = "<p><strong>Name : </strong>" + name + "</p>";
    } else {
      if (measure == 'problem') {
          toolTipText = hierarchy[id];
      } else {
          // measure is step.
          toolTipText = "<p><strong>Step Name : </strong>" + name + "</p>";
          toolTipText += customizeHierarchyText(hierarchy[problemId]);
      }
    }

    var styleStr = "\"word-wrap: break-word\"";
    if (KCs == undefined) {
        KCs = "KCs are defined at the Step Level";
        styleStr = "\"font-style: italic; word-wrap: break-word\"";
    }

    toolTipText += "<p><strong>KCs : </strong><div><p style=" + styleStr + ">"
        + KCs + "</p></div></p>";

    if ((secondaryKCs != undefined) && (secondaryKCs != "")) {
        toolTipText += "<p><strong>Secondary Model KCs : </strong><div><p style=\"word-wrap: break-word\">"
            + secondaryKCs + "</p></div></p>";
    }

    toolTipText += "<hr style='border: 0; height: 1px; background: #bebebe'>";
    toolTipText += "<p><strong>Value : </strong>" + value + "</p>";
    toolTipText += "<p><strong>Observations : </strong>" + freq + "</p>";

    new ToolTip(spanId, toolTipText, params);
} // end generateToolTip()

/**
 * In the case of including problem hierarchy information in the tooltip for Steps
 * we need to clarify the text so it is not confusing to the user.
 * @param hierarchyText the problem hierachy text to modify.
 * @return the modified hierarchy text for inclusion in the tooltip.
 */
function customizeHierarchyText(hierarchyText) {
    hierarchyText = hierarchyText.sub('Name :', 'Problem Name :');
    hierarchyText = hierarchyText.sub('Description :', 'Problem Description :');
    return hierarchyText;
} // end customizeHierarchyText()

/**
 * Based on which measure was selected, examine the "count" for that measure
 * and update the links. If the count is greater than 50, display a message
 * in the LCPID area. Otherwise, make sure no message is displayed.
 */
function updatePointInfoDetailsMessageAndLinks(measure) {
    if (measure == 'skill') {
      setMessageAndLinks($('pointInfoSkills').innerHTML);
    } else if (measure == 'problem') {
      setMessageAndLinks($('pointInfoProblems').innerHTML);
    } else if (measure == 'step') {
        setMessageAndLinks($('pointInfoSteps').innerHTML);
    } else if (measure == 'student') {
      setMessageAndLinks($('pointInfoStudents').innerHTML);
    }
} // end updatePointInfoDetailsMessageAndLinks()

/**
 * Check the count and display the links. If it is > 50, display the
 * LCPID message and update the links to include the limit in the title,
 * otherwise make sure the message is not displayed.
 */
function setMessageAndLinks(actualCount) {
    if (actualCount != '-') {
      if (parseInt(actualCount) > 50) {
            $('pointInfoDetailsMessage').update("Only showing the first 50 rows.");
            updateLCPIDLinks(true);
      } else {
          $('pointInfoDetailsMessage').update();
          updateLCPIDLinks(false);
      }
    }
} // end setMessageAndLinks()

/**
 * Set the lcpid 'action' links to the correct value, given the selected
 * measure.
 * @param isAboveThreshold whether or not the number of items is above the threshold
 * @param measure the selected measure
 */
function updateLCPIDLinks(isAboveThreshold) {
    var selectSentence = "Select only these XXX";
    var removeSentence = "Remove these XXX from the current selection";
    var replaceString = "XXX";
    var students = "students";
    var skills = "KCs";
    var threshold ="50";

    var measure = sortHelper.getMeasure();

    if (measure == "student" || measure == "skill") {
      if (measure == "student") {
          if (isAboveThreshold) {
            students = threshold + " " + students;
          }
        actionSelectLink().update(selectSentence.replace(replaceString, students));
          actionRemoveLink().update(removeSentence.replace(replaceString, students));
      } else {
          if (isAboveThreshold) {
            skills = threshold + " " + skills;
          }
          actionSelectLink().update(selectSentence.replace(replaceString, skills));
          actionRemoveLink().update(removeSentence.replace(replaceString, skills));
      }
      // now set the observer
      Event.observe(actionSelectLink(), 'click', processNavUpdate);
      Event.observe(actionRemoveLink(), 'click', processNavUpdate);
        actionSelect().show();
        actionRemove().show();
    } else {
       actionSelectLink().update();
       actionRemoveLink().update();
       actionSelect().hide();
       actionRemove().hide();
       actionSelectLink().stopObserving('click', processNavUpdate);
       actionRemoveLink().stopObserving('click', processNavUpdate);
    }
} // end updateLCPIDLinks()

/**
 * Users can select/deselect a chunk of students or skills within the LCPID area.  This
 * function handles events fired from selecting one of those options.  Since this causes
 * a navigation update the AJAX callback must refresh the learning curve graph.
 * @param e the click event.
 */
function processNavUpdate(e) {
    var element = e.element();
    var action;
    var measure = sortHelper.getMeasure();

    if (element.hasClassName("actionRemoveLink")) {
      action = "deselectSet"
    } else {
      action = "selectSet";
    }

    if (measure == "student") {
        studentList.expandMenu();
    } else {
        skillList.expandMenu();
    }

    var ids = new Array();
    var position = 0;
    $$('.pointInfoDetailsId').each( function(span) {
        // span ids are of the form <measure>_<unique_id>
      var splitId = span.id.split("_");
      ids[position] = splitId[1];
      position++
    });
    // adding an 's' to measure because that is what the navigation helper expects.  Should change this.
    var params = {
            requestingMethod: "LCPID.pointInfoDetailsRequest",
            datasetId: dataset,
          type: "navUpdate",
            ajaxRequest: true,
            action: action,
            list: measure + "s",
            ids: ids };

    new Ajax.Request("LCPID", {
      parameters: params,
      requestHeaders: { Accept: 'application/json;charset=UTF-8' },
      onSuccess: function(transport) { processNavUpdateCallback(); },
        onException: function(request, exception) { throw(exception); }
    });
} // end processNavUpdate()

/**
 * Callback method for the AJAX request to update the skill or student navigation.
 * Since the navigation items have changed, we need to do a form submit to update the graph.
 */
function processNavUpdateCallback() {
    var myForm = document.createElement('FORM');
    myForm.setAttribute('name', 'lcpid_update_form');
    myForm.setAttribute('id', 'lcpid_update_form');
    myForm.setAttribute('form', 'text/plain');
    myForm.setAttribute('action', window.location.href);
    myForm.setAttribute('method', 'POST');

    var datasetIdInput = document.createElement('input');
    datasetIdInput.name ="datasetId";
    datasetIdInput.type ="hidden";
    datasetIdInput.value =dataset;

    myForm.appendChild(datasetIdInput);

    document.getElementsByTagName('body').item(0).appendChild(myForm);
    myForm.submit();
} // end processNavUpdateCallback()

/**
 * Clear out the LCPI Details area.  Also, de-highlight the selected measure.
 */
function clearLCPID() {
    infoDetails().update();
    // clean out sortHelper?
    //highlightSelectedMeasure(null);
    $$('.infoRow').each(function(row) {
        row.removeClassName("pointInfoDetailsSelected");
        //row.down(1).addClassName("pointInfoDetailsActive");
      row.down(1).removeClassName("pointInfoDetailsActive");
    });
} // end clearLCPID()

