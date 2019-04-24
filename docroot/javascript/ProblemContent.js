//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2014
// All Rights Reserved
//
// Author: Cindy Tipper
// Version: $Revision: 11362 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2014-07-22 17:10:35 -0400 (Tue, 22 Jul 2014) $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(function() {

        // 'Problem List' subtab...
        jQuery('#prob_list_search_by').keyup(searchProblems); 
        jQuery('#prob_list_search_button').click(searchProblems);
        // Create a 'hint' for the 'Problem List' search box
        jQuery('#prob_list_search_by').hint('auto-hint');
        jQuery('#prob_list_clear_button').click(plSearchByClear);
        jQuery('.prob_list_rows_per_page').change(changeRowsPerPage);
        jQuery('#prob_list_pc_select').change(changeProblemContent);

        var plMessage = jQuery('#plMessage').val();
        var plMessageLevel = jQuery('#plMessageLevel').val();

        if ((plMessage != undefined) && (plMessage != "null")) {
            if (plMessageLevel == "SUCCESS") {
                successPopup(plMessage);
            } else if (plMessageLevel == "ERROR") {
                errorPopup(plMessage);
            } else {
                messagePopup(plMessage);
            }
        }

        // 'Problem Content' subtab...
        jQuery('#pc_search_by').keyup(searchContentVersions); 
        jQuery('#pc_search_button').click(searchContentVersions);
        // Create a 'hint' for the 'Content Version' search box
        jQuery('#pc_search_by').hint('auto-hint');
        jQuery('#pc_clear_button').click(pcSearchByClear);
        jQuery('#pc_conversion_tool_select').change(selectConversionTool);
        jQuery('#pc_content_version_select').change(selectContentVersion);

        var pcMessage = jQuery('#pcMessage').val();
        var pcMessageLevel = jQuery('#pcMessageLevel').val();

        if ((pcMessage != undefined) && (pcMessage != "null")) {
            if (pcMessageLevel == "SUCCESS") {
                successPopup(pcMessage);
            } else if (pcMessageLevel == "ERROR") {
                errorPopup(pcMessage);
            } else {
                messagePopup(pcMessage);
            }
        }
});

//---------------------------
// Problem List
//---------------------------
function searchProblems(event) {

    var key = "";
    if (this.id == 'prob_list_search_by') {
        key = event.keyCode || event.which;
    }
    var testEvent = (key === 13 & this.id == 'prob_list_search_by')
        | this.id == 'prob_list_search_button';
    if (testEvent) {
        var searchString = jQuery('#prob_list_search_by').val();
        if (searchString == jQuery('#prob_list_search_by').attr('title')) {
            searchString = "";
        }

        plSearchBy(searchString);
    }
    return false;
}
function plSearchByClear() {
    jQuery('#prob_list_search_by').val("");
    plSearchBy("");
}
function plSearchBy(searchString) {
    jQuery(
           '<form id="searchStringForm" method="post" '
           + 'action="ProblemList?datasetId=' + dataset
           + '"><input name="searchBy" type="hidden" value="' + searchString
           + '"/></form>').appendTo('body').submit();
}
function changeRowsPerPage(event) {
    var numRows = jQuery(this).val();
    jQuery(
           '<form id="rowsPerPageForm" method="post" action="ProblemList?datasetId=' + dataset
           + '"><input name="rowsPerPage" type="hidden" value="' + numRows
           + '"/></form>').appendTo('body').submit();
}
function changeProblemContent(event) {
    var pcOption = jQuery(this).val();
    jQuery(
           '<form id="problemContentForm" method="post" action="ProblemList?datasetId=' + dataset
           + '"><input name="problemContent" type="hidden" value="' + pcOption
           + '"/></form>').appendTo('body').submit();
}
function gotoProblemList(pageNum) {
    jQuery(
           '<form id="changePageForm" method="post" action="ProblemList?datasetId=' + dataset
           + '"><input name="currentPage" type="hidden" value="' + pageNum
           + '"/></form>').appendTo('body').submit();
}

function downloadAll() {
    jQuery(
           '<form id="downloadAllForm" method="post" action="ProblemList?datasetId=' + dataset
           + '"><input name="downloadAll" type="hidden" value="true"/></form>')
        .appendTo('body').submit();
}

//---------------------------
// View Problem
//---------------------------
function viewProblem(problemId) {
    new Ajax.Request("ProblemContent", {
            asynchronous: false,
            parameters: {
                requestingMethod: "ProblemContent.viewProblem",
                viewProblem: "true",
                problemId: problemId},
            onComplete: displayProblemContentInfo,
            onException: function (request, exception) {
                throw(exception);
            }
        });

}

function displayProblemContentInfo(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.status == "SUCCESS") {
        var pcId = json.pcId;
        var problemName = json.problemName;
        var win = window.open();
        win.document.open();
        win.document.location = 'ProblemContent?pcId=' + pcId + '#' + problemName;
        win.document.write('');
        win.document.close();
    } else if (json.status == "ERROR") {
        errorPopup(json.message);
    } else {
        warningPopup(json.message);
    }
}

//---------------------------
// Problem Content
//---------------------------
function searchContentVersions(event) {

    var key = "";
    if (this.id == 'pc_search_by') {
        key = event.keyCode || event.which;
    }
    var testEvent = (key === 13 & this.id == 'pc_search_by')
        | this.id == 'pc_search_button';
    if (testEvent) {
        var searchString = jQuery('#pc_search_by').val();
        if (searchString == jQuery('#pc_search_by').attr('title')) {
            searchString = "";
        }

        pcSearchBy(searchString);
    }
    return false;
}
function pcSearchByClear() {
    jQuery('#pc_search_by').val("");
    pcSearchBy("");
}
function pcSearchBy(searchString) {
    jQuery(
           '<form id="searchStringForm" method="post" '
           + 'action="ProblemContent?datasetId=' + dataset
           + '"><input name="searchBy" type="hidden" value="' + searchString
           + '"/></form>').appendTo('body').submit();
}
function sortMappedContent(sortByColumn) {
    jQuery(
           '<form id="sortByForm" method="post" '
           + 'action="ProblemContent?datasetId=' + dataset
           + '"><input name="sortBy" type="hidden" value="' + sortByColumn
           + '"/></form>').appendTo('body').submit();
}
function selectConversionTool() {
    var toolName = jQuery('#pc_conversion_tool_select option:selected').val();

    jQuery(
           '<form id="conversionToolForm" method="post" '
           + 'action="ProblemContent?datasetId=' + dataset
           + '"><input name="conversionTool" type="hidden" value="' + toolName
           + '"/></form>').appendTo('body').submit();
}
function selectContentVersion() {
    var versionId = jQuery('#pc_content_version_select option:selected').val();

    // Enable the 'Add' button.
    jQuery('#add_content_version').removeAttr('disabled');

    // Hide all of the others...
    jQuery('.content_version_data').hide();

    // ... display this one.
    jQuery('#pc_content_version_' + versionId).show();
}
function addContentVersion() {
    var versionId = jQuery('#pc_content_version_select option:selected').val();

    jQuery(
           '<form id="addContentVersionForm" method="post" '
           + 'action="ProblemContent?datasetId=' + dataset
           + '"><input name="addContentVersion" type="hidden" value="true">'
           + '<input name="contentVersionId" type="hidden" value="' + versionId
           + '"/></form>').appendTo('body').submit();
}
function showDeleteAreYouSure(versionId) {
    jQuery('#pc_delete_map_' + versionId).hide();
    jQuery('#pc_delete_map_areYouSure_' + versionId).show();
}
function closeAreYouSure(versionId) {
    jQuery('#pc_delete_map_areYouSure_' + versionId).hide();
    jQuery('#pc_delete_map_' + versionId).show();
}
function deleteContentVersion(versionId) {
    jQuery('#pc_delete_map_areYouSure_' + versionId).hide();
    jQuery(
           '<form id="addContenVersionForm" method="post" '
           + 'action="ProblemContent?datasetId=' + dataset
           + '"><input name="deleteContentVersion" type="hidden" value="true">'
           + '<input name="contentVersionId" type="hidden" value="' + versionId
           + '"/></form>').appendTo('body').submit();
}
function displayMappingError() {
    modalErrorPopup("Unexpected error from the server. Please try again and contact "
                    + "the DataShop team if the error persists.");
}

//-----------------------------------------
// General purpose.
//  Pulling in Datashop.js broke things...
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
