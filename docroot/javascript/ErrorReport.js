//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 12486 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-08-04 11:03:12 -0400 (Tue, 04 Aug 2015) $
// $KeyWordsOff: $
//

var currentView = null;

function viewByProblem(input) {
    if (input.value == "skill" && currentView != "skill") {
        postParams="error_report_view_by=" + input.value;

        new Ajax.Request(window.location.href, {
            parameters: {
                requestingMethod: "ErrorReport.viewByProblem",
                datasetId: dataset,
                error_report_view_by: input.value},
            onComplete: updateErrorReport,
            onException: function (request, exception) {
                throw(exception);
            }
        });

        listDiv = document.getElementById('problems');
        if (listDiv != null) {
            clearContents(listDiv);
            listDiv.id="skills";
            problemList = null;
            sampleObserver.removeListener(refreshProblemList);

            var options = {multiSelect:true, observer:skillObserver};
            skillList = new NavigationBox.Component(
                       "skills", "Knowledge Components", window.location.pathname, options);
            sampleObserver.addListener(refreshSkillList);  
            $("skills").down('span').insert({before : '<span id="navManageKCsetsButton" title="Add, edit, or load KC sets" class="wrench"></span>'});
            createManageKCSetsDialog();
            currentView = input.value;
        }
    } else if (input.value == "problem" && currentView != "problem") {

        new Ajax.Request(window.location.href, {
            parameters: {
                requestingMethod: "ErrorReport.viewByProblem",
                datasetId: dataset,
                error_report_view_by: input.value},
            onComplete: updateErrorReport,
            onException: function (request, exception) {
                throw(exception);
            }
        });

        listDiv = document.getElementById('skills');
        if (listDiv != null) {
            clearContents(listDiv);
            listDiv.id="problems";
            skillList = null;
            sampleObserver.removeListener(refreshSkillList);

            var options = {multiSelect:false, observer:problemObserver, itemInfo:true};
            skillList = new NavigationBox.Component(
                       "problems", "Problems", window.location.pathname, options);
            sampleObserver.addListener(refreshSkillList);  
            currentView = input.value;
        }
    }
}

/** AJAX request to update the error report content. */
function updateErrorReport(transport) {
    //build the parameters string
    var postParams = "getErrorReport=true";

    var erContentDiv = document.getElementById('errorReportDiv');
    erContentDiv.innerHTML="<h1>Loading...</h1>"

    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "ErrorReport.updateErrorReport",
            datasetId: dataset,
            getErrorReport: "true"},
        onComplete: displayErrorReport,
        onException: function (request, exception) {
            //alert("Something went wrong... \n\nException:"  + exception);
            throw(exception);
        }
    });
}

/** Display the error report */
function displayErrorReport(transport) {
    var erContentDiv = document.getElementById('errorReportDiv');
    erContentDiv.innerHTML=transport.responseText;

    //For QA testing.
    loadedForQA("true");
}

/** Initialize all javascript items necessary for the ErrorReport */
function initErrorReport() {
    initDefaultNavigation(false);

    problemObserver.addListener(updateErrorReport);
    skillObserver.addListener(updateErrorReport);
    skillObserver.addListener(updateSetModifier);

    new NavigationBox.Base("er_nav");
    createManageKCSetsDialog();

    //For QA testing.
    loadedForQA("true");
}

onloadObserver.addListener(initErrorReport);
