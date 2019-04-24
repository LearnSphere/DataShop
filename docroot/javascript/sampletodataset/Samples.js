//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2013
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 7724 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2012-05-14 09:03:23 -0400 (Mon, 14 May 2012) $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it.
jQuery.noConflict();


//jQuery's Onload function
jQuery(document).ready(
function() {
  // Bind the sample history button to the getSampleHistory function
  jQuery('.s2dCollapsed').live("click", s2dTriggerCollapseExpand);
  //hide all project request list histories on page load
  jQuery('table[id=sample-history]').hide();
});

var s2dSample = null;
var fadedBackgroundDiv = null;


//Show/Hide project request history for a given row when an image is clicked.
function s2dTriggerCollapseExpand() {

    var imgId = "img[id=" + this.id + "][class=s2dCollapsed]";
    var tableId = "table[id=" + this.id + "][class=sample-history]";
    if (jQuery(this).attr('src') == "images/expand.png") {
        jQuery(this).attr({
            'src' : "images/contract.png"
        });
        var historyTable = jQuery("table[id=" + this.id + "][class=sample-history]");
        if (historyTable.html() != null && historyTable.html().trim() != "") {
            historyTable.show();
        } else {
            var parsedIdString = (this.id).split("_");
            var sampleId = parsedIdString[parsedIdString.length - 1];
            if (sampleId != null) {

                new Ajax.Request("Samples", {
                    parameters : {
                        requestingMethod: "SamplesServlet.getSampleHistory",
                        sampleId: sampleId,
                        tableId: tableId,
                        datasetId : dataset
                    },
                    onComplete : showSampleHistory,
                    onException : function(request, exception) {
                        throw (exception);
                    }
                });

            }
        }
    } else {
        jQuery(this).attr({
            'src' : "images/expand.png"
        });
        jQuery("table[id=" + this.id + "][class=sample-history]").hide();
    }
    return false;
}

function showSampleHistory(transport) {

    var json = null;
    var table = "";
    if (transport !== undefined) {
        json = transport.responseText.evalJSON(true);
    }
    if (json.filterText != null && json.filterText != "") {
        table = '<tr><td colspan="12" class="historyHeader"><b>Filters</b></td></tr>'
            + '<tr><td colspan="12">' + json.filterText + '</td></tr>';

    }
    table += '<tr><td colspan="12" class="historyHeader"><b>History</b></td></tr>';

    if (json.tableId != null && json.sampleHistoryItems != null) {

        for (var i = 0; i < json.sampleHistoryItems.length; i++) {
            var histRow = json.sampleHistoryItems[i];
            var userLink = "", timeAsString = "", info = "";
            if (histRow.userLink != undefined) {
                userLink = histRow.userLink;
            }
            if (histRow.timeAsString != undefined) {
                timeAsString = histRow.timeAsString;
            }
            if (histRow.info != undefined) {
                info = histRow.info;
            }
            table += '<tr>'
            + '<td class="arFirstColumn"></td><td class="arSecondColumn"></td>'
            + '<td>' + timeAsString + '</td>'
            + '<td>' + userLink + '</td>'
            + '<td colspan="8">' + info + '</td>';
        }
        jQuery(json.tableId).html(table);
    }

    if (json.status == "error") {
        if (json.message != undefined) {
            messagePopup(json.message, "MESSAGE");
        } else {
            messagePopup("There was an issue retrieving the history for this sample.", "MESSAGE");
        }
    } else {
        jQuery("table[id=" + this.id + "][class=sample-history]").show();
    }
}

function s2dRenameSample(sampleId, confirmSave) {
    if (sampleId != null && $('sampleName_' + sampleId) != null) {
        var sampleName = $('sampleName_' + sampleId).value;
        new Ajax.Request("DatasetInfo?datasetId=" + dataset, {
            parameters: {
                requestingMethod: "Samples.s2dSaveSample",
                sampleId: sampleId,
                sampleName: sampleName,
                s2dSelector: "true",
                sampleSelectorId: sampleId
            },
            onComplete: s2dSaveSample2,
            onException: function (request, exception) { throw(exception); }
        });
        //messagePopup("Aggregating sample '" + sampleName + "'... (This may take a few moments)", "MESSAGE");
    }
}

function s2dEditSample(sampleId) {

    if (s2dSample == null) {
        s2dSample = new Sample();

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
            s2dSample.loadSample(sampleId);
        } else {
            s2dSample.createView();
        }
    } else {
        s2dSample.clearOld();
        if (sampleId != null) {
            s2dSample.loadSample(sampleId);
        } else {
            s2dSample.createView();
        }
    }
}


function s2dDeleteSample(sampleId) {
    var sampleName = $('sampleName_' + sampleId).value;
    confirmDelete = confirm("Are you sure you want to delete sample '" + sampleName + "'?");
    if (sampleId != null && confirmDelete == true) {
        new Ajax.Request(window.location.href, {
            parameters: {
                requestingMethod: "sampleSelector.deleteSample",
                datasetId: dataset,
                deleteSample: "true",
                s2dSelector: "true",
                sampleSelectorId: sampleId
            },
            onComplete: this.s2dDeleteStatus,
            onException: function (request, exception) { throw(exception); }
        });
        messagePopup("Deleting sample '" + sampleName + "'... (This may take a few moments)", "MESSAGE");
    }
}

function s2dDeleteStatus(transport){

    var response = transport.responseText;
    var infoArray = response.split("\t");

    if (infoArray[0] == "SUCCESS"){
        messagePopup("Sample successfully deleted.", "SUCCESS");
        refreshSampleList(null);
        messagePopup("Refreshing window.", "MESSAGE");
    } else if (infoArray[0] == "ERROR"){
        errorPopup("There was an error deleting the sample: \n" + infoArray[1]);
    }
}

function confirm2(sampleId, question) {

    jQuery('<div id="s2dConfirmationDialog"></div>')
        .html(question)
        .dialog({
            open: function() { jQuery('.ui-button').focus(); }, 
            autoOpen: true,
            width : 600,
            height : 350,
            modal: true,
            title: 'Save Sample as Dataset',
            buttons: {
                "Yes": function () {
                    s2dSaveSampleComplete(sampleId);
                    jQuery(this).dialog("close");
                },
                "No": function () {
                    jQuery(this).dialog("close");
                }
            },
            close: function () {
                jQuery(this).remove();
            }
        });
}

function s2dSaveSample(sampleId, confirmSave) {
    if (sampleId != null && $('sampleName_' + sampleId) != null) {
        var sampleName = $('sampleName_' + sampleId).value;
        new Ajax.Request("DatasetInfo?datasetId=" + dataset, {
            parameters: {
                requestingMethod: "Samples.s2dSaveSample",
                sampleId: sampleId,
                sampleName: sampleName,
                s2dSelector: "true",
                sampleSelectorId: sampleId
            },
            onComplete: s2dSaveSample2,
            onException: function (request, exception) { throw(exception); }
        });
    }
}

function s2dSaveSample2(transport) {
    var json = null;
    if (transport !== undefined) {
        json = transport.responseText.evalJSON(true);
    }

    if (json.requestDatashopEditRole == "true") {
        requestDatashopEditRole(true);
    }

    if (json.status == "error") {
        messagePopup(json.message, "WARNING");
    }

    var sampleName = json.sampleName;
    var sampleId = json.sampleId;
    var previouslyCreatedDatasets = json.previouslyCreatedDatasets;

    var datasetList = "";
    if (previouslyCreatedDatasets != "" && previouslyCreatedDatasets != undefined) {
        datasetList = '<p class="s2d-dialog-header">Datasets created from this sample:</p>'
        + '<p class="s2d-dialog-datasets">' + previouslyCreatedDatasets + '</p>';
    }

    if (sampleName =="All Data" && datasetList != "") {
        var message = '<p class="s2d-dialog-header">'
            + 'Are you sure you want to create a dataset using the "All Data" sample?</p>'
            + '<p class="s2d-dialog-bullet">Using the "All Data" sample to create a new dataset will include'
            + ' all transactions from the source dataset.</p>'
            + datasetList;
        confirm2(sampleId, message);
    } else if (datasetList != "") {
        var message = '<p class="s2d-dialog-header">'
            + 'Are you sure you want to create another dataset using this sample?</p>'
            + datasetList;
        confirm2(sampleId, message);
    } else if (sampleName == "All Data") {
        var message = '<p class="s2d-dialog-header">'
            + 'Are you sure you want to create a dataset using the "All Data" sample?</p>'
            + '<p class="s2d-dialog-bullet">Using the "All Data" sample to create a new dataset will include'
            + ' all transactions from the source dataset.</p>';
        confirm2(sampleId, message);
    } else {
        s2dSaveSampleComplete(sampleId);
    }
    // The function calls above only create the dialog and are non-blocking so any code
    // in this function placed after the calls to confirm2 will be executed immediately.
}

function s2dSaveSampleComplete(sampleId) {

    if (sampleId != null) {
        new Ajax.Request("DatasetInfo?datasetId=" + dataset, {
            parameters: {
                requestingMethod: "sampleSelector.aggSample",
                datasetId: dataset,
                aggSample: "true",
                s2dSelector: "true",
                sampleSelectorId: sampleId
            },
            onComplete: testSaveSample,
            onException: function (request, exception) { throw(exception); }
        });
    }
}

function testSaveSample(transport) {
    var response = transport.responseText;


    var infoArray = response.split("|");
    // The sample is up to date, we don't have to aggregate
    if (infoArray[0] == "requestDatashopEditRole") {
        requestDatashopEditRole(true);
    } else if ((infoArray[0] == "SUCCESS" && infoArray[3] != "AGGREGATING")
            || (response.status == 100)) {
        var sampleId = infoArray[2];
        var sampleName = $('sampleName_' + sampleId).value;
        //messagePopup(infoArray[1], "MESSAGE");


        if (sampleId != null) {
            jQuery(
              '<form id="saveSampleToDatasetForm" method="post" action="SampleToDataset?datasetId='
                + dataset + '">'
              + '<input id="s2d_' + sampleId + '" name="s2d_sample"'
              + ' type="hidden" value="' + sampleId + '"/>'
              + '</form>')
            // now append jQuery-created form to body and submit
            .appendTo('body').submit();

        }

    // An error occurred while testing if sample was aggregated
    } else if (infoArray[0] == "ERROR"){
        errorPopup("There was an error while aggregating the sample: \n" + infoArray[1]);

    // The sample needs to be aggregated or is currently being aggregated.
    } else if (infoArray[0] == "SUCCESS" && infoArray[3] == "AGGREGATING") {
        getAggStatus(infoArray[2]);
    }

}

var sampleId = null;
var s2dProgressBar = false;
function s2dDisplayProgressBar(transport) {

    var json = null;
    if (transport !== undefined) {
        json = transport.responseText.evalJSON(true);
    }

    // If completed, save the sample as a new dataset
    if (json.status == 100) {
        s2dSaveSampleComplete(sampleId);

    // Show a popup error if something went wrong
    } else if (json != null && json.status == "error") {
        errorPopup(json.message);

    // Display the progress bar while we await aggregation
    } else {

        if (!s2dProgressBar) {
            var extraParamsArray = {};
            extraParamsArray['allowCancel'] = false;
            extraParamsArray['s2d_sample'] = sampleId;
            extraParamsArray['datasetId'] = dataset;
            extraParamsArray['s2dSelector'] = "true";
            extraParamsArray['aggregator_check'] = "true";
            var options = { extraParams: extraParamsArray };

            s2dProgressBar = new ProgressBar(s2dDisplayProgressBar, "DatasetInfo?datasetId=" + dataset, options);
        }

        // Refresh the progress bar every 1.5 seconds
        window.setTimeout(function() {
        new Ajax.Request("DatasetInfo?datasetId=" + dataset, {
        parameters: {
            requestingMethod: "sampleSelector.aggCheck",
            datasetId: dataset,
            aggregator_check: "true",
            s2dSelector: "true",
            sampleSelectorId: sampleId },
            onComplete: s2dDisplayProgressBar,
            onException: function (request, exception) { throw(exception); }
        });
        }, 1500);
    }
}

// Gets the status of the aggregator (runs aggregator_check)
function getAggStatus(sampleId) {
    if (sampleId != null) {
        this.sampleId = sampleId;
        new Ajax.Request("DatasetInfo?datasetId=" + dataset, {
            parameters: {
                requestingMethod: "sampleSelector.aggSample",
                datasetId: dataset,
                aggregator_check: "true",
                s2dSelector: "true",
                sampleSelectorId: sampleId
            },
            onComplete: s2dDisplayProgressBar,
            onException: function (request, exception) { throw(exception); }
        });
        //messagePopup("Aggregating sample '" + sampleName + "'... (This may take a few moments)", "MESSAGE");
    }
}
