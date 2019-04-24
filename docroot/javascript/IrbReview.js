//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2014
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 10671 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2014-02-27 21:08:16 -0500 (Thu, 27 Feb 2014) $
// $KeyWordsOff: $
//

function irbReviewShowDatasets(projectId) {
    jQuery('#showDatasetsLink_' + projectId).hide();
    jQuery('#hideDatasetsLink_' + projectId).show();
    
    if (hasDatasets(projectId)) {
        jQuery('#datasetsRow_' + projectId).show("slow");
    } else {
        requestDatasets(projectId);
    }
    jQuery('.projectTd_' + projectId).addClass('noBorder');
}
function irbReviewHideDatasets(projectId) {
    jQuery('#showDatasetsLink_' + projectId).show();
    jQuery('#hideDatasetsLink_' + projectId).hide();
    jQuery('#datasetsRow_' + projectId).hide("fast");
    jQuery('.projectTd_' + projectId).removeClass('noBorder');
}

function requestDatasets(projectId) {
    inputField = $("toolNameField");

    new Ajax.Request("IRBReview", {
        parameters: {
            requestingMethod: "IrbReview.requestDatasets",
            irbReviewAction: "requestDatasets",
            projectId: projectId,
            ajaxRequest: "true"
        },
        onComplete: handleDatasets,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleDatasets(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        fillInDatasetsDiv(json.projectId, json.datasets);
    } else {
        errorPopup(json.message);
    }
}

function hasDatasets(projectId) {
    datasetsDiv = $("datasetsDiv_" + projectId);
    if (datasetsDiv.innerHTML.length > 0) {
        return true;
    }
    return false;
}

function fillInDatasetsDiv(projectId, datasets) {
    // Build the HTML
    var divHtml = '<table class="datasetsTable">'
            + '<caption>'
            + '<div id="datasetsEditDiv_' + projectId + '" class="datasetsEditDiv">'
            + '<a href="javascript:displayEditModeDatasets(' + projectId + ')">edit</a>'
            + '</div>'
            + '<div id="datasetsSaveCancelDiv_' + projectId + '" style="display:none">'
            + '<input id="datasetsSaveButton_' + projectId + '" type="button" '
                + 'onclick="javascript:saveChangesDatasets(' + projectId + ')" '
                + 'value="Save" name="datasetsButton">'
            + '<input id="datasetsCancButton_' + projectId + '" type="button" '
                + 'onclick="javascript:cancChangesDatasets(' + projectId + ')" '
                + 'value="Cancel" name="datasetsButton">'
            + '</div>'
            + '</caption>'
            + '<tr>'
            + '<th>Dataset</th>'
            + '<th>Appears Anon</th>'
            + '<th>IRB Uploaded</th>'
            + '<th>Has Study Data</th>'
            + '<th>Dates</th>'
            + '<th>Data Last Modified</th>'
            + '</tr>';
    for (i = 0; i < datasets.length; i++) {
        var suffix = "proj_" + projectId + "_ds_" + datasets[i].datasetId;
        var dsHref = 'DatasetInfo?datasetId=' + datasets[i].datasetId;

        if (i%2 == 0) {
            divHtml += '<tr>';
        } else {
            divHtml += '<tr class="odd">';
        }
        divHtml += '<td class="c1" id="irb_' + suffix + '">';
        divHtml += '<div name="irb_div_view_' + suffix + '" id="irb_dataset_name_div_view_' + suffix + '">';
        divHtml += '<p class="dataset_link">'
        divHtml += '<a class="irb_link" href="' + dsHref + '">';
        divHtml += datasets[i].datasetName;
        divHtml += '</a></p></div>';
        divHtml += '<div name="irb_div_edit_' + suffix + '" id="irb_dataset_name_div_view_' + suffix
                       + '" style="display:none">' + datasets[i].datasetName + '</div>';
        divHtml += '</td>';
        
        divHtml += '<td class="c2">'
        divHtml += '<div name="irb_div_view_' + suffix + '" id="irb_appears_anon_div_view_' + suffix + '">' + datasets[i].appearsAnon + '</div>';
        divHtml += '<div name="irb_div_edit_' + suffix + '" id="irb_appears_anon_div_edit_' + suffix + '" style="display:none">';
        divHtml += addAppearsAnonSelect("irb_appears_anon_select_" + suffix, datasets[i].appearsAnon);
        divHtml += '</div></td>';

        divHtml += '<td class="c3">'
        divHtml += '<div name="irb_div_view_' + suffix + '" id="irb_irb_uploaded_div_view_' + suffix + '">' + datasets[i].irbUploaded + '</div>';
        divHtml += '<div name="irb_div_edit_' + suffix + '" id="irb_irb_uploaded_div_edit_' + suffix + '" style="display:none">';
        divHtml += addIrbUploadedSelect("irb_irb_uploaded_select_" + suffix, datasets[i].irbUploaded);
        divHtml += '</div></td>';
        
        divHtml += '<td class="c4">'
        divHtml += '<div name="irb_div_view_' + suffix + '" id="irb_has_study_data_div_view_' + suffix + '">' + datasets[i].hasStudyData + '</div>';
        divHtml += '<div name="irb_div_edit_' + suffix + '" id="irb_has_study_data_div_edit_' + suffix + '" style="display:none">';
        divHtml += addHasStudyDataSelect("irb_has_study_data_select_" + suffix, datasets[i].hasStudyData);
        divHtml += '</div></td>';
        
        var dlmDate = datasets[i].dataLastModifiedDate;
        var dlmTime = datasets[i].dataLastModifiedTime;
        
        divHtml += '<td class="c5">' + datasets[i].dates + '</td>';
        divHtml += '<td class="c6" title="' + dlmTime + '">' + dlmDate + '</td>';
        divHtml += "</tr>";
    }
    divHtml += "</table>";

    // Set the HTML in the Div
    datasetsDiv = $("datasetsDiv_" + projectId);
    datasetsDiv.innerHTML = divHtml;
    
    // Display the Table Row
    jQuery('#datasetsRow_' + projectId).show("slow");
    
}

function addAppearsAnonSelect(mySelId, myValue) {
    var myVals = new Array("N/A", "Yes", "No", "Not reviewed", "More info needed");
    var myStrs = new Array("N/A", "Yes", "No", "Not reviewed", "More info needed");
    return addSelect(mySelId, myValue, myVals, myStrs);
}

function addIrbUploadedSelect(mySelId, myValue) {
    var myVals = new Array("TBD", "Yes", "No", "N/A");
    var myStrs = new Array("TBD", "Yes", "No", "N/A");
    return addSelect(mySelId, myValue, myVals, myStrs);
}

function addHasStudyDataSelect(mySelId, myValue) {
    var myVals = new Array("Not Specified", "Yes", "No");
    var myStrs = new Array("Not Specified", "Yes", "No");
    return addSelect(mySelId, myValue, myVals, myStrs);
}

function addSelect(selectId, value, theVals, theStrs) {
    var selectHtml = '<select id="' + selectId + '">';
    for (j = 0; j < theVals.length; j++) {
        if (value == theVals[j]) {
            selectHtml += '<option selected value="' + theVals[j] + '">' + theStrs[j] + '</option>';
        } else {
            selectHtml += '<option value="' + theVals[j] + '">' + theStrs[j] + '</option>';
        }
    }
    selectHtml += '</select>';
    return selectHtml;
}

function displayEditModeDatasets(projectId) {
    jQuery('#datasetsEditDiv_' + projectId).hide();
    jQuery('#datasetsSaveCancelDiv_' + projectId).show();
    
    var viewDivs = jQuery("div[name*='irb_div_view_proj_" + projectId + "']");
    var editDivs = jQuery("div[name*='irb_div_edit_proj_" + projectId + "']");
    
    //viewDivs.children().css( "background-color", "red" );
    //editDivs.children().css( "background-color", "red" );

    for (i = 0; i < viewDivs.length; i++) {
        jQuery(viewDivs[i]).hide();
    }
    for (i = 0; i < editDivs.length; i++) {
        jQuery(editDivs[i]).show();
    }
}

function displayViewModeDatasets(projectId) {
    jQuery('#datasetsEditDiv_' + projectId).show();
    jQuery('#datasetsSaveCancelDiv_' + projectId).hide();
    
    var viewDivs = jQuery("div[name*='irb_div_view_proj_" + projectId + "']");
    var editDivs = jQuery("div[name*='irb_div_edit_proj_" + projectId + "']")

    for (i = 0; i < viewDivs.length; i++) {
        jQuery(viewDivs[i]).show();
    }
    for (i = 0; i < editDivs.length; i++) {
        jQuery(editDivs[i]).hide();
    }
}


function cancChangesDatasets(projectId) {
    var appearsAnonDivs = jQuery("div[id*='irb_appears_anon_div_view_proj_" + projectId + "']");
    var appearsAnonSelects = jQuery("select[id*='irb_appears_anon_select_proj_" + projectId + "']");
    for (i = 0; i < appearsAnonDivs.length; i++) {
        jQuery(appearsAnonSelects[i]).val(jQuery(appearsAnonDivs[i]).html());
    }
    
    var irbUploadedDivs = jQuery("div[id*='irb_irb_uploaded_div_view_proj_" + projectId + "']");
    var irbUploadedSelects = jQuery("select[id*='irb_irb_uploaded_select_proj_" + projectId + "']");
    for (i = 0; i < irbUploadedDivs.length; i++) {
        jQuery(irbUploadedSelects[i]).val(jQuery(irbUploadedDivs[i]).html());
    }
    
    var hasStudyDataDivs = jQuery("div[id*='irb_has_study_data_div_view_proj_" + projectId + "']");
    var hasStudyDataSelects = jQuery("select[id*='irb_has_study_data_select_proj_" + projectId + "']");
    for (i = 0; i < hasStudyDataDivs.length; i++) {
        jQuery(hasStudyDataSelects[i]).val(jQuery(hasStudyDataDivs[i]).html());
    }

    displayViewModeDatasets(projectId);
}

function saveChangesDatasets(projectId) {
    //format of theData is: "1,yes,TBD,not specified-2,no,N/A,yes";
    var theData = "";

    // get the data and save changes
    var appearsAnonSelects  = jQuery("select[id*='irb_appears_anon_select_proj_" + projectId + "']");
    var irbUploadedSelects  = jQuery("select[id*='irb_irb_uploaded_select_proj_" + projectId + "']");
    var hasStudyDataSelects = jQuery("select[id*='irb_has_study_data_select_proj_" + projectId + "']");
    
    for (i = 0; i < appearsAnonSelects.length; i++) {
        var aaSelect = jQuery(appearsAnonSelects[i]);
        var iuSelect = jQuery(irbUploadedSelects[i]);
        var hsSelect = jQuery(hasStudyDataSelects[i]);
        var idArr = aaSelect.attr("id").split("_");
        var id = idArr[idArr.length-1];
        theData += id + "," + aaSelect.val();
        theData += "," + iuSelect.val();
        theData += "," + hsSelect.val();
        theData += "-";
    }
    
    new Ajax.Request("IRBReview", {
        parameters: {
            requestingMethod: "IrbReview.saveChangesDatasets",
            irbReviewAction: "saveChangesDatasets",
            projectId: projectId,
            theData: theData,
            ajaxRequest: "true"
        },
        onComplete: handleSaveChangesDatasets,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function handleSaveChangesDatasets(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        successPopup(json.message);
        var projectId = json.projectId;
        
        var appearsAnonDivs = jQuery("div[id*='irb_appears_anon_div_view_proj_" + projectId + "']");
        var appearsAnonSelects = jQuery("select[id*='irb_appears_anon_select_proj_" + projectId + "']");
        for (i = 0; i < appearsAnonDivs.length; i++) {
            jQuery(appearsAnonDivs[i]).html(jQuery(appearsAnonSelects[i]).val());
        }
        
        var irbUploadedDivs = jQuery("div[id*='irb_irb_uploaded_div_view_proj_" + projectId + "']");
        var irbUploadedSelects = jQuery("select[id*='irb_irb_uploaded_select_proj_" + projectId + "']");
        for (i = 0; i < irbUploadedDivs.length; i++) {
            jQuery(irbUploadedDivs[i]).html(jQuery(irbUploadedSelects[i]).val());
        }
        
        var hasStudyDataDivs = jQuery("div[id*='irb_has_study_data_div_view_proj_" + projectId + "']");
        var hasStudyDataSelects = jQuery("select[id*='irb_has_study_data_select_proj_" + projectId + "']");
        for (i = 0; i < hasStudyDataDivs.length; i++) {
            jQuery(hasStudyDataDivs[i]).html(jQuery(hasStudyDataSelects[i]).val());
        }
        
        jQuery("#projectNeedsAttentionSpan_" + projectId).html(json.needsAttention);
        
    } else {
        errorPopup(json.message);
    }
    displayViewModeDatasets(projectId);
}

