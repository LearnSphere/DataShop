//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2012
// All Rights Reserved
//
// Author: Cindy Tipper
// Version: $Revision: 13027 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-03-28 12:32:22 -0400 (Mon, 28 Mar 2016) $
// $KeyWordsOff: $
//

// Prototype-style trim function (from TermsAdminManage.js)
String.prototype.trim = function () {
    return this.replace(/^\s*/, "").replace(/\s*$/, "");
}

jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(function() {
});

function uploadPaper(datasetId) {

    // Get the div...
    var uploadDiv = document.getElementById('uploadPaperDialog');
    uploadDiv.innerHTML = "";

    var f = document.createElement('form');
    f.name = "uploadPaperForm";
    f.id = "uploadPaperForm";
    f.action = "Upload?datasetId=" + datasetId;
    f.method = "post";
    f.enctype = "multipart/form-data";
    f.encoding = "multipart/form-data";  // needed for IE7
    f.onsubmit = "";

    var hiddenInput = document.createElement('input');
    hiddenInput.id = "datasetId";
    hiddenInput.name = "datasetId";
    hiddenInput.value = datasetId;
    hiddenInput.type = "hidden";
    f.appendChild(hiddenInput);

    var p1 = document.createElement('p');
    p1.innerHTML = "Choose a file to upload *";
    f.appendChild(p1);

    var chooseFile = document.createElement('input');
    chooseFile.name = "fileName";
    chooseFile.type = "file";
    chooseFile.id = "paper-file-chooser";
    f.appendChild(chooseFile);

    var h6 = document.createElement('h6');
    h6.innerHTML = "File size must not exceed " + getFileSizeLimitStr();
    h6.id = "fileSizeWarning";
    f.appendChild(h6);

    var p2 = document.createElement('p');
    p2.innerHTML = "Citation";
    f.appendChild(p2);

    var citation = document.createElement('textarea');
    citation.id = "paperCitation";
    citation.name = "paperCitation";
    citation.className = "citation";
    citation.rows = 4;
    citation.cols = 20;
    f.appendChild(citation);

    var p3 = document.createElement('p');
    p3.innerHTML = "Abstract";
    f.appendChild(p3);

    var abstract = document.createElement('textarea');
    abstract.id = "paperAbstract";
    abstract.name = "paperAbstract";
    abstract.rows = 4;
    abstract.cols = 20;
    f.appendChild(abstract);

    var br = document.createElement('br');
    f.appendChild(br);

    var preferredCitation = document.createElement('input');
    preferredCitation.id = "preferredCitation";
    preferredCitation.name = "preferredCitation";
    preferredCitation.type = "checkbox";
    f.appendChild(preferredCitation);

    var pcLabel = document.createElement('label');
    pcLabel.innerHTML = " Preferred citation?";
    pcLabel.htmlFor = "preferredCitation";
    f.appendChild(pcLabel);
    
    var publicPaperInfo = document.createElement('div');
    publicPaperInfo.id = "public-paper-upload-info";
    publicPaperInfo.className = "infoMessage";
    publicPaperInfo.innerHTML = '<img src="images/globe.png" />' +
      '<p><b>This paper will be public on the web.</b></p>';
    f.appendChild(publicPaperInfo);

    // Finally, attach the form to the div...
    uploadDiv.appendChild(f);

    jQuery('#uploadPaperDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 440,
        height : 545,
        title : "Upload Paper",
        buttons : [ {
            id : "paper-button-upload",
            text : "Upload",
            click : paperUpload
        }, {
            id : "paper-button-cancel",
            text : "Cancel",
            click : closeDialog
        } ]
    });

    // Preferred Citation only an option if Citation specified.
    jQuery('#preferredCitation').attr('disabled', 'true');
    jQuery('#paperCitation').keyup(enablePreferredCitation);

    jQuery('#paper-button-upload').button('disable');
    jQuery('#paper-file-chooser').change(enablePaperUploadButton);
    jQuery('#uploadPaperDialog').dialog('open');
}

function uploadFile(datasetId) {

    // Get the div...
    var uploadDiv = document.getElementById('uploadFileDialog');
    uploadDiv.innerHTML = "";

    var f = document.createElement('form');
    f.name = "uploadFileForm";
    f.id = "uploadFileForm";
    f.action = "Upload?datasetId=" + datasetId;
    f.method = "post";
    f.enctype = "multipart/form-data";
    f.encoding = "multipart/form-data";  // needed for IE7
    f.onsubmit = "";

    var hiddenInput = document.createElement('input');
    hiddenInput.id = "datasetId";
    hiddenInput.name = "datasetId";
    hiddenInput.value = datasetId;
    hiddenInput.type = "hidden";
    f.appendChild(hiddenInput);

    var p1 = document.createElement('p');
    p1.innerHTML = "Choose a file to upload *";
    f.appendChild(p1);

    var chooseFile = document.createElement('input');
    chooseFile.name = "fileName";
    chooseFile.type = "file";
    chooseFile.id = "file-file-chooser";
    f.appendChild(chooseFile);

    var h6 = document.createElement('h6');
    h6.innerHTML = "File size must not exceed " + getFileSizeLimitStr();
    h6.id = "fileSizeWarning";
    f.appendChild(h6);

    var p2 = document.createElement('p');
    p2.innerHTML = "Title";
    f.appendChild(p2);

    var title = document.createElement('textarea');
    title.id = "fileTitle";
    title.name = "fileTitle"
    title.className = "title";
    title.rows = 4;
    title.cols = 20;
    f.appendChild(title);
    
    var maxLenTitle = document.createElement('div');
    maxLenTitle.id = "fileTitleMaxLen";
    maxLenTitle.className = "maxlen";
    maxLenTitle.innerHTML = "Enter no more than 255 characters.";
    f.appendChild(maxLenTitle);

    var p3 = document.createElement('p');
    p3.innerHTML = "Description";
    f.appendChild(p3);

    var description = document.createElement('textarea');
    description.id = "fileDescription";
    description.name = "fileDescription";
    description.rows = 4;
    description.cols = 20;
    f.appendChild(description);

    var maxLenDesc = document.createElement('div');
    maxLenDesc.id = "fileDescMaxLen";
    maxLenDesc.className = "maxlen";
    maxLenDesc.innerHTML = "Enter no more than 500 characters.";
    f.appendChild(maxLenDesc);

    // Finally, attach the form to the div...
    uploadDiv.appendChild(f);

    jQuery('#uploadFileDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 440,
        height : 540,
        title : "Upload File",
        buttons : [ {
            id : "file-button-upload",
            text : "Upload",
            click : fileUpload
        }, {
            id : "file-button-cancel",
            text : "Cancel",
            click : closeDialog
        } ]
    });

    jQuery('#file-button-upload').button('disable');
    jQuery('#file-file-chooser').change(enableFileUploadButton);
    jQuery('textarea#fileTitle').keyup(enableFileUploadButton);
    jQuery('textarea#fileDescription').keyup(enableFileUploadButton);

    jQuery('#uploadFileDialog').dialog('open');

}

function uploadExternalAnalysis(datasetId) {

    // Get the div...
    var uploadDiv = document.getElementById('uploadExternalAnalysisDialog');
    uploadDiv.innerHTML = "";

    var f = document.createElement('form');
    f.name = "uploadExternalAnalysisForm";
    f.id = "uploadExternalAnalysisForm";
    f.action = "Upload?datasetId=" + datasetId;
    f.method = "post";
    f.enctype = "multipart/form-data";
    f.encoding = "multipart/form-data";  // needed for IE7
    f.onsubmit = "";

    var hiddenInput = document.createElement('input');
    hiddenInput.id = "datasetId";
    hiddenInput.name = "datasetId";
    hiddenInput.value = datasetId;
    hiddenInput.type = "hidden";
    f.appendChild(hiddenInput);

    var p1 = document.createElement('p');
    p1.innerHTML = "Choose a file to upload *";
    f.appendChild(p1);

    var chooseFile = document.createElement('input');
    chooseFile.name = "fileName";
    chooseFile.type = "file";
    chooseFile.id = "ea-file-chooser";
    f.appendChild(chooseFile);

    var h6 = document.createElement('h6');
    h6.innerHTML = "File size must not exceed " + getFileSizeLimitStr();
    h6.id = "fileSizeWarning";
    f.appendChild(h6);

    var p2 = document.createElement('p');
    p2.innerHTML = "Title *";
    f.appendChild(p2);

    var title = document.createElement('textarea');
    title.id = "fileTitle";
    title.name = "externalAnalysisTitle"
    title.className = "title";
    title.rows = 4;
    title.cols = 20;
    f.appendChild(title);
    
    var maxLenTitle = document.createElement('div');
    maxLenTitle.id = "fileTitleMaxLen";
    maxLenTitle.className = "maxlen";
    maxLenTitle.innerHTML = "Enter no more than 255 characters.";
    f.appendChild(maxLenTitle);

    var p3 = document.createElement('p');
    p3.innerHTML = "Description";
    f.appendChild(p3);

    var description = document.createElement('textarea');
    description.id = "fileDescription";
    description.name = "fileDescription";
    description.rows = 4;
    description.cols = 20;
    f.appendChild(description);
    
    var maxLenDesc = document.createElement('div');
    maxLenDesc.id = "fileDescMaxLen";
    maxLenDesc.className = "maxlen";
    maxLenDesc.innerHTML = "Enter no more than 500 characters.";
    f.appendChild(maxLenDesc);

    var p4 = document.createElement('p');
    p4.innerHTML = "Relevant KC Model";
    f.appendChild(p4);

    // Combo-box of KC Models already exists as a hidden div... add it here.
    var kcModelDiv = document.getElementById('kcModelDivHidden_' + datasetId);
    var kcModelDivCopy = kcModelDiv.cloneNode(true);
    kcModelDivCopy.id = 'kcModelDiv_' + datasetId;
    kcModelDivCopy.style.display = "inline";
    f.appendChild(kcModelDivCopy);

    var p5 = document.createElement('p');
    p5.innerHTML = "Statistical Model Used";
    f.appendChild(p5);

    var statModel = document.createElement('input');
    statModel.id = "externalAnalysisStatModel";
    statModel.name = "externalAnalysisStatModel";
    statModel.className = "externalAnalysisStatModel";
    statModel.type = "text";
    f.appendChild(statModel);

    var maxLenStat = document.createElement('div');
    maxLenStat.id = "eaStatMaxLen";
    maxLenStat.className = "maxlen";
    maxLenStat.innerHTML = "Enter no more than 100 characters.";
    f.appendChild(maxLenStat);

    // Finally, attach the form to the div...
    uploadDiv.appendChild(f);

    jQuery('#uploadExternalAnalysisDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 440,
        height : 600,
        title : "Upload External Analysis",
        buttons : [ {
            id : "ea-button-upload",
            text : "Upload",
            click : externalAnalysisUpload
        }, {
            id : "ea-button-cancel",
            text : "Cancel",
            click : closeDialog
        } ]
    });

    jQuery('#ea-button-upload').button('disable');
    jQuery('#ea-file-chooser').change(enableEAUploadButton);
    jQuery('textarea#fileTitle').keyup(enableEAUploadButton);
    jQuery('textarea#fileDescription').keyup(enableEAUploadButton);
    jQuery('input#externalAnalysisStatModel').keyup(enableEAUploadButton);
    jQuery('#uploadExternalAnalysisDialog').dialog('open');
}

// Function used by uploadPaper function
function enablePreferredCitation() {
    // If text area is empty or not defined, then disable check-box
    if ((jQuery('textarea#paperCitation').val() != "")
            && (jQuery('textarea#paperCitation').val() != undefined)) {
        jQuery('#preferredCitation').removeAttr('disabled');
    }

}

function enablePaperUploadButton() {
    var fileFlag = ((jQuery('#paper-file-chooser').val() != "") && (jQuery(
            '#paper-file-chooser').val() != undefined));

    if (fileFlag) {
        jQuery('#paper-button-upload').button('enable');
    } else {
        jQuery('#paper-button-upload').button('disable');
    }
}

function enableEAUploadButton() {
    var fileFlag = ((jQuery('#ea-file-chooser').val() != "") && (jQuery(
            '#ea-file-chooser').val() != undefined));
    var titleFlag = jQuery('textarea#fileTitle').length > 0 // exists
            && jQuery("textarea#fileTitle").val().trim().length > 0 // has text
            && jQuery("textarea#fileTitle").val().length <= 255; // <= 255
    var titleLenFlag = jQuery("textarea#fileTitle").val().length <= 255; // <= 255;
    var descFlag = jQuery("textarea#fileDescription").val().length <= 500;
    var statModelFlag = jQuery("#externalAnalysisStatModel").val().length <= 100;

    if (titleLenFlag) {
    	jQuery("#fileTitleMaxLen").css('color', 'gray');
    }
    if (descFlag) {
        jQuery("#fileDescMaxLen").css('color', 'gray');
    }
    if (statModelFlag) {
        jQuery("#eaStatMaxLen").css('color', 'gray');
    }

    if (fileFlag && titleFlag && descFlag && statModelFlag) {
        jQuery('#ea-button-upload').button('enable');
    } else {
        jQuery('#ea-button-upload').button('disable');
        if (!titleLenFlag) {
        	jQuery("#fileTitleMaxLen").css('color', '#E00000');
        }
        if (!descFlag) {
        	jQuery("#fileDescMaxLen").css('color', '#E00000');
        }
        if (!statModelFlag) {
        	jQuery("#eaStatMaxLen").css('color', '#E00000');
        }
    }
}

function enableFileUploadButton() {
    var fileFlag = ((jQuery('#file-file-chooser').val() != "") && (jQuery(
            '#file-file-chooser').val() != undefined));
    var titleFlag = jQuery("textarea#fileTitle").val().length <= 255;
    var descFlag = jQuery("textarea#fileDescription").val().length <= 500;

    if (titleFlag) {
    	jQuery("#fileTitleMaxLen").css('color', 'gray');
    }
    if (descFlag) {
        jQuery("#fileDescMaxLen").css('color', 'gray');
    }
    
    if (fileFlag && titleFlag && descFlag) {
        jQuery('#file-button-upload').button('enable');
    } else {
        jQuery('#file-button-upload').button('disable');
        if (!titleFlag) {
        	jQuery("#fileTitleMaxLen").css('color', '#E00000');
        }
        if (!descFlag) {
        	jQuery("#fileDescMaxLen").css('color', '#E00000');
        }
    }
}

function paperUpload() {
    jQuery('#uploadPaperForm').submit();
    jQuery('#uploadPaperDialog').dialog('close');
}

function fileUpload() {
    jQuery('#uploadFileForm').submit();
    jQuery('#uploadFileDialog').dialog('close');
}

function externalAnalysisUpload() {
    jQuery('#uploadExternalAnalysisForm').submit();
    jQuery('#uploadExternalAnalysisDialog').dialog('close');

    // Do we need to hide the kcModel div too?
    closeKcModelDiv(dataset);
}

function closeKcModelDiv(datasetId) {
    var kcModelDiv = document.getElementById('kcModelDiv_' + datasetId);
    if (kcModelDiv != null) {
        kcModelDiv.style.display = "none";
    }
}

// A simple close modal dialog function to make reading easier elsewhere.
function closeDialog() {
    jQuery(this).dialog("close");
}

// Get file size limit.
function getFileSizeLimitStr() {
    var isAdmin = jQuery('input#adminUserFlag').val() == "true" ? true : false;
    var limitStr = "400MB.";
    if (isAdmin) { limitStr = "1GB."; }

    return limitStr;
}