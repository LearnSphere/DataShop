//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 12840 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-12-18 12:12:17 -0500 (Fri, 18 Dec 2015) $
// $KeyWordsOff: $
//

//this array maps the attribute to display text for the interface.
var ATTRIBUTE_UI_MAP = new Array();
ATTRIBUTE_UI_MAP['problemName'] = "Problem Name";
ATTRIBUTE_UI_MAP['problemDescription'] = "Problem Description";
ATTRIBUTE_UI_MAP['transactionTime'] = "Time Stamp";
ATTRIBUTE_UI_MAP['attemptAtSubgoal'] = "Attempt Number";
ATTRIBUTE_UI_MAP['conditionName'] = "Condition Name";
ATTRIBUTE_UI_MAP['type'] = "Condition Type";
ATTRIBUTE_UI_MAP['levelName'] = "Level Name";
ATTRIBUTE_UI_MAP['levelTitle'] = "Level Title";
ATTRIBUTE_UI_MAP['schoolName'] = "School Name";
ATTRIBUTE_UI_MAP['anonymousUserId'] = "Anon Student Id";
ATTRIBUTE_UI_MAP['transactionTypeTool'] = "Student Response Type";
ATTRIBUTE_UI_MAP['transactionTypeTutor'] = "Tutor Response Type";
ATTRIBUTE_UI_MAP['transactionSubtypeTool'] = "Student Response Subtype";
ATTRIBUTE_UI_MAP['transactionSubtypeTutor'] = "Tutor Response Subtype";
ATTRIBUTE_UI_MAP['customFieldName'] = "Custom Field Name";
ATTRIBUTE_UI_MAP['customFieldValue'] = "Custom Field Value";

//collection to types
var TEXT_TYPE = "text";
var DATE_TYPE = "date";
var NUMBER_TYPE = "number";


var theSample = null;
var fadedBackgroundDiv = null;

function editSample(sampleId) {

    if (theSample == null) {
        theSample = new Sample();

        if (sampleId != null) {
            //create a "loading screen" in case the load takes awhile.
            //TODO style info should probably be moved to a style sheet.
            var loadingCenterDiv = document.createElement('div');
            loadingCenterDiv.id = "sampleLoad";
            loadingCenterDiv.style.position="absolute";
            loadingCenterDiv.style.width="100%";
            loadingCenterDiv.style.textAlign="center";
            loadingCenterDiv.style.top="200px";
            loadingCenterDiv.style.left="0px";

            var loadingDiv = document.createElement('div');
            loadingDiv.style.position="relative";
            loadingDiv.style.width="400px";
            loadingDiv.style.hieght="200px";
            loadingDiv.style.margin="0 auto";
            loadingDiv.style.backgroundColor="white";
            loadingDiv.style.border="2px solid black";

            var loadingP = document.createElement('p');
            loadingP.appendChild(document.createTextNode("Loading Sample Information"));
            loadingP.style.padding="50px";
            loadingP.style.color="black";
            loadingDiv.appendChild(loadingP);
            loadingCenterDiv.appendChild(loadingDiv);

            document.getElementsByTagName('body').item(0).appendChild(loadingCenterDiv);
            theSample.loadSample(sampleId);
        } else {
            theSample.createView();
        }
    } else {
        theSample.clearOld();
        if (sampleId != null) {
            theSample.loadSample(sampleId);
        } else {
            theSample.createView();
        }
    }
}


function deleteSample(sampleId) {
	var sampleName = $('sampleName_' + sampleId).value;
    confirmDelete = confirm("Are you sure you want to delete sample '" + sampleName + "'?");
    if (sampleId != null && confirmDelete == true) {
        new Ajax.Request(window.location.href, {
            parameters: {
            	requestingMethod: "sampleSelector.deleteSample",
                datasetId: dataset,
                deleteSample: "true",
                sampleSelectorId: sampleId
            },
            onComplete: this.deleteStatus,
            onException: function (request, exception) { throw(exception); }
        });
        messagePopup("Deleting sample '" + sampleName + "'... (This may take a few moments)", "MESSAGE");
    }
}

function deleteStatus(transport){

    var response = transport.responseText;
    var infoArray = response.split("\t");

    if (infoArray[0] == "SUCCESS"){
        messagePopup("Sample successfully deleted.", "SUCCESS");
        refreshSampleList(null);
        messagePopup("Refreshing window.", "MESSAGE");
    } else if (infoArray[0] == "ERROR"){
        errorPopup("There was an error deleting the sample: \n" + infoArray[1]);
    } else {
        //do nothing, perhaps a page refresh occurred
    }
}
