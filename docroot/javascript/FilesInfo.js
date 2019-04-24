//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2012
// All Rights Reserved
//
// Author: Cindy Tipper
// Version: $Revision: 10833 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2014-03-24 13:40:24 -0400 (Mon, 24 Mar 2014) $
// $KeyWordsOff: $
//

//
// Add an onload listener to initialize everything for this report.
//
onloadObserver.addListener(initFilesInfo);

jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(function() {

        var message = jQuery('#filesMessage').val();
        var messageLevel = jQuery('#filesMessageLevel').val();

        if ((message != undefined) && (message != "null")) {
            if (messageLevel == "SUCCESS") {
                successPopup(message);
            } else if (messageLevel == "ERROR") {
                errorPopup(message);
            } else {
                messagePopup(message);
            }
        }
});

//
// Initialize the default navigation and content.
//
function initFilesInfo() {
    //find out what kind of content we should be looking at.
    new Ajax.Request("Files", {
        parameters: {
            requestingMethod: "FilesInfo.initFilesInfo",
            datasetId: dataset,
            ajaxRequest: "true",
            files_request: "content",
            files_content: "initialize"
        },
        onComplete: determineContent,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

// The strings papers, external_analyses and files are div ids inside the JSPs
// which are just the content portion of the page and not full pages.  These JSPs are
// 1) files_info_papers.jsp
// 2) files_info_ext_analyses.jsp
// 3) files_info_files.jsp
function determineContent(transport) {
    if (transport.responseText.indexOf("paperInfo") > 0) {
        displayPapers(transport);
    } else if (transport.responseText.indexOf("externalAnalysesInfo") > 0) {
        displayExternalAnalyses(transport);
    } else if (transport.responseText.indexOf("fileInfo") > 0) {
        displayFiles(transport);
    } else { // default
        displayPapers(transport);
    }
}

function initSubtab(contentType) {
    var userLoggedIn = ($('userLoggedIn').value == "false") ? false : true;
    var userAuthorized = ($('userAuthorized').value == "false") ? false : true;
    
    subtabDiv = $("subtab");
    if (!subtabDiv.firstChild) {
        subtabDiv.insert('<a id="papers_subtab_link">Papers</a><span id="paSpan" class="attachmentSpan"></span>');
        $('papers_subtab_link').observe('click', selectPapers);

        // Users must be logged in and authorized to access these sub-tabs.
        if (userLoggedIn && userAuthorized) {
            subtabDiv.insert('<a id="external_analyses_subtab_link">External Analyses</a><span id="eaSpan" class="attachmentSpan"></span>');
            subtabDiv.insert('<a id="files_subtab_link">Files</a><span id="fiSpan" class="attachmentSpan"></span>');

            $('files_subtab_link').observe('click', selectFiles);
            $('external_analyses_subtab_link').observe('click', selectExternalAnalyses);
        } else {
            var titleStr = "You must be logged in to use this feature.";
            if (userLoggedIn) {
                titleStr = "You do not have permission to access this dataset.";
            }
            subtabDiv.insert('<span id="external_analyses_subtab_link" class="disabledItem" title=\"'
                             + titleStr + '\">External Analyses<span id="eaSpan" class="attachmentSpan"></span></span>');
            subtabDiv.insert('<span id="files_subtab_link" class="disabledItem" title=\"'
                             + titleStr + '\">Files<span id="fiSpan" class="attachmentSpan"></span></span>');
        }
    }
    selectSubtab(contentType);
    updateSubTabsWithValues();
}

function updateSubTabsWithValues() {
    // Get numbers from hidden inputs
    var numPapers = jQuery('#numPapers').val();
    var numExternalAnalyses = jQuery('#numExternalAnalyses').val();
    var numFiles = jQuery('#numFiles').val();
    
    // Only display numbers if greater than zero
    if (numPapers > 0) {
        jQuery('#paSpan').html("(" + numPapers + ")");
    } else {
        jQuery('#paSpan').html("");
    }
    if (numExternalAnalyses > 0) {
        jQuery('#eaSpan').html("(" + numExternalAnalyses + ")");
    } else {
        jQuery('#eaSpan').html("");
    }
    if (numFiles > 0) {
        jQuery('#fiSpan').html("(" + numFiles + ")");
    } else {
        jQuery('#fiSpan').html("");
    }
}

function sortFiles(sortByKey) {
    _sortFiles(sortByKey, "true");
}

function _sortFiles(sortByKey, toggleFlag) {
    new Ajax.Request("Files", {
        parameters: {
            requestingMethod: "FilesInfo.sortFiles",
            datasetId: dataset,
            sortBy: sortByKey,
            toggle: toggleFlag,
            ajaxRequest: "true",
            files_request: "content",
            files_content: "files"
        },
        onComplete: displayFiles,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function sortPapers(sortByKey) {
	_sortPapers(sortByKey, "true");
}

function _sortPapers(sortByKey, toggleFlag) {
    new Ajax.Request("Files", {
        parameters: {
            requestingMethod: "FilesInfo.sortPapers",
            datasetId: dataset,
            sortBy: sortByKey,
            toggle: toggleFlag,
            ajaxRequest: "true",
            files_request: "content",
            files_content: "papers"
        },
        onComplete: displayPapers,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function sortExternalAnalyses(sortByKey) {
	_sortExternalAnalyses(sortByKey, "true");
}

function _sortExternalAnalyses(sortByKey, toggleFlag) {
    new Ajax.Request("Files", {
        parameters: {
            requestingMethod: "FilesInfo.sortExternalAnalyses",
            datasetId: dataset,
            sortBy: sortByKey,
            toggle: toggleFlag,
            ajaxRequest: "true",
            files_request: "content",
            files_content: "externalAnalyses"
        },
        onComplete: displayExternalAnalyses,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function selectSubtab(selectedLink) {
    $("papers_subtab_link",
      "external_analyses_subtab_link",
      "files_subtab_link").each(
          function(link) { link.removeClassName("selected"); }
      );

    if (selectedLink == "papers") {
        $("papers_subtab_link").addClassName("selected");
    }  else if (selectedLink == "externalAnalyses") {
        $("external_analyses_subtab_link").addClassName("selected");
    }  else if (selectedLink == "files") {
        $("files_subtab_link").addClassName("selected");
    }  else { // default
        $("papers_subtab_link").addClassName("selected");
    }
}

function selectPapers() {
    selectSubtab("papers");
    requestPapers();
}

function selectExternalAnalyses() {
    selectSubtab("externalAnalyses");
    requestExternalAnalyses();
}

function selectFiles() {
    selectSubtab("files");
    requestFiles();
}

/** AJAX request to get the files and papers content from server. */
function requestPapers() {
    var contentType = "papers";
    new Ajax.Request("Files", {
        parameters: {
            requestingMethod: "FilesInfo.requestPapers",
            datasetId: dataset,
            ajaxRequest: "true",
            files_request: "content",
            files_content: contentType
        },
	beforeSend : showStatusIndicator("Loading..."),
        onSuccess : hideStatusIndicator,
        onComplete: displayPapers,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX response to display the papers content. */
function displayPapers(transport) {
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";

    initSubtab("papers");
    // Update the report-level contextual help content
    theHelpWindow.updateContent($("help-files"));
    loadedForQA("true");
}

/** AJAX request to get the files from the server. */
function requestExternalAnalyses() {
    var contentType = "externalAnalyses";
    new Ajax.Request("Files", {
        parameters: {
            requestingMethod: "FilesInfo.requestExternalAnalyses",
            datasetId: dataset,
            ajaxRequest: "true",
            files_request: "content",
            files_content: contentType
        },
	beforeSend : showStatusIndicator("Loading..."),
        onSuccess : hideStatusIndicator,
        onComplete: displayExternalAnalyses,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX response to display the files content. */
function displayExternalAnalyses(transport) {
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";

    initSubtab("externalAnalyses");
    // Update the report-level contextual help content
    theHelpWindow.updateContent($("help-files"));
    loadedForQA("true");
}

/** AJAX request to get the files from the server. */
function requestFiles() {
    var contentType = "files";
    new Ajax.Request("Files", {
        parameters: {
            requestingMethod: "FilesInfo.requestFiles",
            datasetId: dataset,
            ajaxRequest: "true",
            files_request: "content",
            files_content: contentType
        },
	beforeSend : showStatusIndicator("Loading..."),
        onSuccess : hideStatusIndicator,
        onComplete: displayFiles,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX response to display the files content. */
function displayFiles(transport) {
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";

    initSubtab("files");

    // Update the report-level contextual help content
    theHelpWindow.updateContent($("help-files"));
    loadedForQA("true");
}

//
// Edit Files
//
function makeFileRowEditable(fileId) {
    // Get rid of any status indicator on this page...
    var statusIndicator = $('file_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";

    //Title
    titleTD = $("title_" + fileId);
    var hiddenTitleInputs = titleTD.getElementsByTagName("input");

    // save all hidden inputs, to be restored after edit
    var clonedHiddenInputs = new Array(hiddenTitleInputs.length);
    for (i = 0; i < hiddenTitleInputs.length; i++) {
	clonedHiddenInputs[i] = hiddenTitleInputs[i].cloneNode(false);
    }

    var currentTitle = $('title_input_' + fileId).value.escapeHTML();
    var currentDesc = $('desc_input_' + fileId).value.escapeHTML();

    titleTD.innerHTML = "<textarea id=\"fileTitleInput\" name=\"fileTitle\" rows=\"4\" cols=\"20\">" + currentTitle + "</textarea> <br/> <p>Description</p> <textarea id=\"fileDescInput\" name=\"fileDescription\" rows=\"4\" cols=\"20\">" + currentDesc + "</textarea> <br/> ";
    
    for (i = 0; i < clonedHiddenInputs.length; i++) {
    	titleTD.appendChild(clonedHiddenInputs[i]);
    }

    //Hide/Show File Save/Edit Divs
    fileSaveDiv = $("fileSaveDiv_" + fileId);
    fileSaveDiv.style.display = "inline";

    fileEditDiv = $("fileEditDiv_" + fileId);
    fileEditDiv.style.display = "none";
}

function sendSaveRequest(fileId) {
    new Ajax.Request("Edit", {
         parameters: {
             requestingMethod: "FilesInfo.sendSaveRequest",
             datasetId: dataset,
             fileId: fileId,
             fileTitle: $F("fileTitleInput"),
             fileDescription: $F("fileDescInput")
         },
         onComplete: fileModified,
         onException: function (request, exception) { throw(exception); }
     });
}

function showFullTitle(fileId) {
    // Get rid of any status indicators on this page...
    var statusIndicator = $('ea_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";
    statusIndicator = $('file_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";

    var titleSpan = $('s_title_' + fileId);

    var fullTitle = $('title_full_' + fileId);
    titleSpan.innerHTML = fullTitle.value;

    moreTitleDiv = $('moreTitleDiv_' + fileId);
    moreTitleDiv.style.display = "none";

    lessTitleDiv = $('lessTitleDiv_' + fileId);
    lessTitleDiv.style.display = "inline";
}

function showTruncatedTitle(fileId) {
    // Get rid of any status indicators on this page...
    var statusIndicator = $('ea_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";
    statusIndicator = $('file_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";

    var titleSpan = $('s_title_' + fileId);

    var truncTitle = $('title_trunc_' + fileId);
    titleSpan.innerHTML = truncTitle.value;

    moreTitleDiv = $('moreTitleDiv_' + fileId);
    moreTitleDiv.style.display = "inline";

    lessTitleDiv = $('lessTitleDiv_' + fileId);
    lessTitleDiv.style.display = "none";
}

function showFullDescription(fileId) {

    // Get rid of any status indicators on this page...
    var statusIndicator = $('ea_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";
    statusIndicator = $('file_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";

    var descSpan = $('s_description_' + fileId);

    var fullDesc = $('desc_full_' + fileId);
    descSpan.innerHTML = fullDesc.value;

    moreDescDiv = $('moreDescDiv_' + fileId);
    moreDescDiv.style.display = "none";

    lessDescDiv = $('lessDescDiv_' + fileId);
    lessDescDiv.style.display = "inline";
}

function showTruncatedDesc(fileId) {
    // Get rid of any status indicators on this page...
    var statusIndicator = $('ea_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";
    statusIndicator = $('file_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";

    var descSpan = $('s_description_' + fileId);

    var truncDesc = $('desc_trunc_' + fileId);
    descSpan.innerHTML = truncDesc.value;

    moreDescDiv = $('moreDescDiv_' + fileId);
    moreDescDiv.style.display = "inline";

    lessDescDiv = $('lessDescDiv_' + fileId);
    lessDescDiv.style.display = "none";
}

// limit title text to 255 characters
function limit(textid, length) {
    $(textid).value = $F(textid).substring(0, length - 1);
}

function validateFileTitle() {
    if ($F("fileTitle").length > 255) {
        showFileTitleSure($F("fileTitle").length);
    } else {
        $('embeddedFilesForm').submit();
    }
}

function truncateTitleAndSave(fileId) {
    // limit title text to 255 characters
    limit("fileTitleInput", 255);
    sendSaveRequest(fileId);
}

function truncateDescriptionAndSave(fileId) {
    // limit description text to 500 characters
    limit("fileDescInput", 500);
    sendSaveRequest(fileId);
}

//
// Make AJAX request to save the changes to the file
//
function saveFileChanges(fileId) {
    if ($F("fileTitleInput").length > 255) {
	$("titleSureDiv_" + fileId).style.display = "inline";
	$("fileSaveDiv_" + fileId).style.display = "none";
    } else {
	sendSaveRequest(fileId);
    }
}

//
// Handle AJAX response from saveFileChanges
//
function fileModified(transport) {
    var response = transport.responseText;
    var data = response.evalJSON();
    var messageType = data.messageType;
    var message = data.message;

    if (messageType == "ERROR" || messageType == 'UNAUTHORIZED') {
	errorPopup(message);
    } else if (messageType == 'SUCCESS') {

	// Title and Description are in the same cell now so we
	// need to get both elements before doing any updates
	titleInput = $("fileTitleInput");
	descInput = $("fileDescInput");

	// Likewise, save parent node.
	parentTd = titleInput.parentNode;

	var hiddenTitleInputs = parentTd.getElementsByTagName("input");

	// save all hidden inputs, to be restored after edit
	var clonedHiddenInputs = new Array(hiddenTitleInputs.length);
	for (i = 0; i < hiddenTitleInputs.length; i++) {
	    clonedHiddenInputs[i] = hiddenTitleInputs[i].cloneNode(false);
	}

	var fileId = titleTD.id.substring(6, titleTD.id.length);

	//Title
	var titleTruncated = false;
	var truncatedTitle = "";
	if (titleInput.value.length > 100) {
	    titleTruncated = true;
	    truncatedTitle = titleInput.value.substring(0, 100);
	    truncatedTitle += "...";
	}

	var titleToDisplay = titleTruncated ? truncatedTitle : titleInput.value;

	var titleSpan = document.createElement('span');
	titleSpan.name = "title";
	titleSpan.innerHTML = titleToDisplay.escapeHTML();
	titleSpan.id = "s_title_" + fileId;
	titleSpan.className = "bTitle";
	parentTd.innerHTML = "";
	parentTd.appendChild(titleSpan);

	if (titleTruncated) {
	    var moreDiv = document.createElement('div');
	    moreDiv.className = "moreTitleDiv";
	    moreDiv.id = "moreTitleDiv_" + fileId;
	    var a = document.createElement('a');
	    a.textContent = "more";
	    a.href = "javascript:showFullTitle(" + fileId + ")";
	    moreDiv.appendChild(a);
	    moreDiv.appendChild(document.createElement('br'));
	    parentTd.appendChild(moreDiv);

	    var lessDiv = document.createElement('div');
	    lessDiv.className = "lessTitleDiv";
	    lessDiv.id = "lessTitleDiv_" + fileId;
	    var a = document.createElement('a');
	    a.textContent = "less";
	    a.href = "javascript:showTruncatedTitle(" + fileId + ")";
	    lessDiv.appendChild(a);
	    lessDiv.appendChild(document.createElement('br'));
	    parentTd.appendChild(lessDiv);
	}

	parentTd.appendChild(hiddenWithValueAndId(titleInput.value, "title_input_" + fileId));
	parentTd.appendChild(hiddenWithValueAndId(titleInput.value, "title_full_" + fileId));
	parentTd.appendChild(hiddenWithValueAndId(truncatedTitle, "title_trunc_" + fileId));
	parentTd.appendChild(hiddenWithValueAndId(titleTruncated, "title_truncated_" + fileId));

	//Description
	var descTruncated = false;
	var truncatedDesc = "";
	if (descInput.value.length > 100) {
	    descTruncated = true;
	    truncatedDesc = descInput.value.substring(0, 100);
	    truncatedDesc += "...";
	}

	var descToDisplay = descTruncated ? truncatedDesc : descInput.value;

	parentTd.appendChild(document.createElement('br'));
	var descSpan = document.createElement('span');
	descSpan.name = "description";
	descSpan.innerHTML = descToDisplay.escapeHTML();
	descSpan.id = "s_description_" + fileId;
	parentTd.appendChild(descSpan);

	if (descTruncated) {
	    var moreDiv = document.createElement('div');
	    moreDiv.className = "moreDescDiv";
	    moreDiv.id = "moreDescDiv_" + fileId;
	    var a = document.createElement('a');
	    a.textContent = "more";
	    a.href = "javascript:showFullDescription(" + fileId + ")";
	    moreDiv.appendChild(a);
	    parentTd.appendChild(moreDiv);

	    var lessDiv = document.createElement('div');
	    lessDiv.className = "lessDescDiv";
	    lessDiv.id = "lessDescDiv_" + fileId;
	    var a = document.createElement('a');
	    a.textContent = "less";
	    a.href = "javascript:showTruncatedDesc(" + fileId + ")";
	    lessDiv.appendChild(a);
	    parentTd.appendChild(lessDiv);
	}

	parentTd.appendChild(hiddenWithValueAndId(descInput.value, "desc_input_" + fileId));
	parentTd.appendChild(hiddenWithValueAndId(descInput.value, "desc_full_" + fileId));
	parentTd.appendChild(hiddenWithValueAndId(truncatedDesc, "desc_trunc_" + fileId));
	parentTd.appendChild(hiddenWithValueAndId(descTruncated, "desc_truncated_" + fileId));

	//Hide/Show File Save/Edit Divs
	$("fileSaveDiv_" + fileId).style.display = "none";
	$("titleSureDiv_" + fileId).style.display = "none";
	$("fileEditDiv_" + fileId).style.display = "inline";

	var sortBy = $('filesSortBy').value;
	_sortFiles(sortBy, "false");
	successPopup(message);
    }
}

function showAbstract(fileId) {
    // Get rid of any status indicator on this page...
    var statusIndicator = $('paper_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";

    abstractDiv = $("abstractDiv_" + fileId);
    abstractDiv.style.display = "inline";

    citationOnlyDiv = $("citationOnlyDiv_" + fileId);
    citationOnlyDiv.style.display = "none";
}

function hideAbstract(fileId) {
    // Get rid of any status indicator on this page...
    var statusIndicator = $('paper_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";

    abstractDiv = $("abstractDiv_" + fileId);
    abstractDiv.style.display = "none";

    citationOnlyDiv = $("citationOnlyDiv_" + fileId);
    citationOnlyDiv.style.display = "inline";
}

function fileDeleteAreYouSure(fileId) {
    //Hide/Show File Save/Edit Divs
    fileSaveDiv = $("fileSureDiv_" + fileId);
    fileSaveDiv.style.display = "inline";

    fileEditDiv = $("fileEditDiv_" + fileId);
    fileEditDiv.style.display = "none";
}

function fileDeleteAreYouSureDone(fileId) {
    //Hide/Show File Save/Edit Divs
    fileSaveDiv = $("fileSureDiv_" + fileId);
    fileSaveDiv.style.display = "none";

    fileEditDiv = $("fileEditDiv_" + fileId);
    fileEditDiv.style.display = "inline";
}

function paperDeleteAreYouSure(paperId) {
    //Hide/Show File Save/Edit Divs
    paperSaveDiv = $("paperSureDiv_" + paperId);
    paperSaveDiv.style.display = "inline";

    paperEditDiv = $("paperEditDiv_" + paperId);
    paperEditDiv.style.display = "none";
}

function paperDeleteAreYouSureDone(paperId) {
    //Hide/Show File Save/Edit Divs
    paperSaveDiv = $("paperSureDiv_" + paperId);
    paperSaveDiv.style.display = "none";

    paperEditDiv = $("paperEditDiv_" + paperId);
    paperEditDiv.style.display = "inline";
}

//
// Edit Papers
//
function makePaperRowEditable(paperId) {
    // Get rid of any status indicator on this page...
    var statusIndicator = $('paper_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";

    //Preferred Citation
    preferredCitationTD = $("preferred_citation_" + paperId);
    currentPreferredCitationHidden = preferredCitationTD.getElementsByTagName("input");
    if ((currentPreferredCitationHidden) && (currentPreferredCitationHidden[0])){
        currentPreferredCitationHiddenCopy = currentPreferredCitationHidden[0].cloneNode(false);
        currentPreferredCitation = currentPreferredCitationHidden[0].getAttribute("value");
    }

    tickImage = $('preferred_citation_image');
    var hasChecked = false;
    var checkbox = $('preferredCitationCheckbox');
    if (checkbox != null){
        hasChecked = checkbox.checked;
    }

    if (currentPreferredCitation == "true" && hasChecked == false){
      preferredCitationTD.innerHTML =
    	  "<input type=\"checkbox\"" +
    	  " id=\"preferredCitationCheckbox\"" +
    	  " name=\"preferredCitation_" + paperId + "\"" +
    	  " checked" +
    	  " onClick=\"javascript:singleCheck('preferredCitationCheckbox', this);\"></input>";
    } else if (currentPreferredCitation == "false" || hasChecked == true){
    	 preferredCitationTD.innerHTML =
       	  "<input type=\"checkbox\"" +
       	  " id=\"preferredCitationCheckbox\"" +
       	  " name=\"preferredCitation_" + paperId + "\"" +
       	  " onClick=\"javascript:singleCheck('preferredCitationCheckbox', this);\"></input>";
    }

    preferredCitationTD.appendChild(currentPreferredCitationHiddenCopy);

    // Common parent for citation and abstract as they are
    // now in a single cell.
    citationTD = $("citation_" + paperId);

    //Citation
    hiddenCitationInputs = citationTD.getElementsByTagName("input");
    currentCitationHiddenCopy = hiddenCitationInputs[0].cloneNode(false);
    currentCitation = hiddenCitationInputs[0].getAttribute("value");
    if (currentCitation != null) {
    	currentCitation = currentCitation.escapeHTML();
    } else {
    	currentCitation = "";
    }
    currentAbstractHiddenCopy = hiddenCitationInputs[1].cloneNode(false);
    currentAbstract = hiddenCitationInputs[1].getAttribute("value");
    if (currentAbstract != null) {
    	currentAbstract = currentAbstract.escapeHTML();
    } else {
    	currentAbstract = "";
    }
    citationTD.innerHTML = "<textarea id=\"paperCitationInput\" name=\"paperCitation\" rows=\"4\" cols=\"20\">" + currentCitation + "</textarea> <br/> <p>Abstract</p> <textarea id=\"paperAbstractInput\" name=\"paperAbstract\" rows=\"4\" cols=\"20\">" + currentAbstract + "</textarea>";
    citationTD.appendChild(currentCitationHiddenCopy);
    citationTD.appendChild(currentAbstractHiddenCopy);

    //Hide/Show Paper Save/Edit Divs
    paperSaveDiv = $("paperSaveDiv_" + paperId);
    paperSaveDiv.style.display = "inline";

    paperEditDiv = $("paperEditDiv_" + paperId);
    paperEditDiv.style.display = "none";
}

//
// Make AJAX request to save the changes to the paper
//
function savePaperChanges(paperId) {

    // reset value to not show undo/redo in the Citation field on Overview page
    CITATION_UNDO_STATE = false;
    // get preferred citation value
    var isPreferredCitation = "";
    preferredCitationTD = $("preferred_citation_"+ paperId);
    totalNumOfInputTags = preferredCitationTD.getElementsByTagName("input").length;
    var i = 0;
    for (i = 0; i< totalNumOfInputTags; i++ ){
	inputElement = preferredCitationTD.getElementsByTagName("input")[i];
	if (inputElement.type=="checkbox"){
	    if (inputElement.checked == true){
		isPreferredCitation = "on";
	    }
	} else if (inputElement.type=="hidden") {
	    if (isPreferredCitation == "on"){
		inputElement.value = "true";
	    } else {
		inputElement.value = "false";
	    }
	}
    }

    //set hidden field to false if another citation was set as preferred.
    var elements = document.getElementsByTagName("td");
    var myElement = null, myInputs = null, myImages = null;
    var myHiddenInput = null;

    for (var i = 0; i < elements.length; i ++){
    	// obtain the preferred citation element
    	if ((elements[i].id.indexOf("preferred_citation_") > -1)
    		&& (elements[i].id != "preferred_citation_" + paperId)){
    		myElement = elements[i];
    		// only set value to false if current paper is set to preferred citation
    		if (isPreferredCitation == "on"){
    		    myInputs = myElement.getElementsByTagName("input");
    		    var isEditMode = false;
    		    for (var p = 0; p < myInputs.length; p ++){

    		    	if (myInputs[p].id == "preferredCitationCheckbox"){
    		    		isEditMode = true;
    		    	}
    		    }
    		    for (var p = 0; p < myInputs.length; p ++){

    			    if (myInputs[p].id == "preferred_citation_hidden"){
    				    myHiddenInput = myInputs[p];
    				    if ((myHiddenInput.value == "true") && (!isEditMode)) {
    				        myHiddenInput.value =  "false";
    				    } // end if (myHiddenInput.value == "true")
    			    } // end if (myInputs[p].id == "preferred_citation_hidden")
    		    }	// end for loop
    		} // end if (isPreferredCitation == "on")
    	} // end if
    } // end for loop

    // Citation and Abstract are in the same cell... common parent, paperCitationTD.
    paperCitationTD = $("citation_" + paperId);

    // get citation value
    if ((paperCitationTD) && (paperCitationTD.getElementsByTagName("textarea").length > 0)){
    	paperCitation = paperCitationTD.getElementsByTagName("textarea")[0].value;
    }

    // get abstract value
    if ((paperCitationTD)&& (paperCitationTD.getElementsByTagName("textarea").length > 1)){
    	paperAbstract = paperCitationTD.getElementsByTagName("textarea")[1].value;
    }

    new Ajax.Request("Edit", {
        parameters: {
            requestingMethod: "FilesInfo.savePaperChanges",
            datasetId: dataset,
            paperId: paperId,
            preferredCitation: isPreferredCitation,
            paperCitation: paperCitation,
            paperAbstract: paperAbstract
        },
        onComplete: paperModified,
        onException: function (request, exception) { throw(exception); }
    });
}

//
//Handle AJAX response from savePaperChanges
//
paperModified = function(transport) {
    var response = transport.responseText;
    var data = response.evalJSON();
    var messageType = data.messageType;
    var message = data.message;
    var paperId = data.value;
    var paperCitation = data.paperCitation;
    var paperAbstract = data.paperAbstract;
    var preferredCitation = data.preferredCitation;

    if (messageType == "ERROR" || messageType == 'UNAUTHORIZED') {

	// change hidden field value to false if something is wrong
	var citationTD = $("citation_" + paperId);
	var citationInput = citationTD.getElementsByTagName("textarea")[0];
	if (citationInput){
	    if (citationInput.value == ''){
	    	changeHiddenValue($('preferred_citation_' + paperId), 'preferred_citation_hidden', "false");
	    }
	}
	errorPopup(message);
    } else if (messageType == 'SUCCESS') {

	// Citation and Abstract are in the same cell now so we
	// need to get both elements before doing any updates

	var parentTD = $("citation_" + paperId);

	var citationInput = parentTD.getElementsByTagName("textarea")[0];
	var abstractInput = parentTD.getElementsByTagName("textarea")[1];

	//Citation
	if (citationInput){
	    wrapWithNonTruncatingSpan(citationInput.value, "citation", parentTD, "");
	}

	//Abstract
	if (abstractInput){
	    if (abstractInput.value.length > 0) {
		    addHideAbstractDiv(paperId, parentTD);
	    }
    	addShowAbstractDiv(paperId, abstractInput.value, parentTD);
	}

	//PreferredCitation
	var preferredCitationTD = $("preferred_citation_" + paperId);
	var preferredCitationCheckbox = preferredCitationTD.getElementsByTagName("input")[0];
	if (preferredCitationCheckbox) {
	    removeCheckbox(preferredCitationCheckbox.parentNode);
	    if (preferredCitationCheckbox.checked == true) {
		//remove the tick image if there is any
		removeOtherTickImage();
		// add the tick image to the current object
		addTickImage(preferredCitationTD);
	    }
	}
	//Set hidden text for preferred_citation, used for PREV_PREFERRED_CITATION
	var citationHidden = $('preferred_citation_text')
	if (citationHidden != null) {
	    citationHidden.value = citationInput.value;
	}

	//Hide/Show Paper Save/Edit Divs
	paperSaveDiv = $("paperSaveDiv_" + paperId);
	paperSaveDiv.style.display = "none";

	paperEditDiv = $("paperEditDiv_" + paperId);
	paperEditDiv.style.display = "inline";

	var sortBy = $('papersSortBy').value;
	_sortPapers(sortBy, "false");
	successPopup(message);
    } else {
	errorPopup("Unexpected error from the server. "
		   + "Please try again and contact the DataShop team if the errors persists.");
    }
}

function addHideAbstractDiv(paperId, parentTD) {
    div = document.createElement('div');
    div.id = "citationOnlyDiv_" + paperId;
    div.className = "cell";

    // Would prefer to manipulate DOM but that was broken in IE7,8.
    div.innerHTML = "<a title='s_abstract_" + paperId + "' href='javascript:showAbstract(" + paperId + ")'>show abstract</a>";
    parentTD.appendChild(document.createElement('br'));
    parentTD.appendChild(div);
}

function addShowAbstractDiv(paperId, abstractInput, parentTD)
{
    div = document.createElement('div');
    div.id = "abstractDiv_" + paperId;
    div.className = "showAbstractDiv";

    concatWithNonTruncatingSpan(abstractInput, "abstract", div, false);

    div.appendChild(document.createElement('br'));

    // Would prefer to manipulate DOM but that was broken in IE7,8.
    div.innerHTML = div.innerHTML + "<a title='s_abstract_" + paperId + "' href='javascript:hideAbstract(" + paperId + ")'>hide abstract</a>";
    div.innerHTML = "<br/><b>Abstract</b><br/>" + div.innerHTML;

    parentTD.appendChild(div);
    parentTD.appendChild(hiddenWithValue(abstractInput));
}

//
// Cancel edits to the fields
//
function cancelPaperChanges(paperId) {

    //PreferredCitation -- use value of hidden input to add the image, keeping the hidden input
    var preferredCitationTD = $("preferred_citation_" + paperId);
    //remove the checkbox
    var checkBoxPreferredCitationJs = preferredCitationTD.getElementsByTagName("input");
    preferredCitationTD.removeChild(checkBoxPreferredCitationJs[0]);
    if (preferredCitationTD.getElementsByTagName("input")[0]) {
        currentPreferredCitation = preferredCitationTD.getElementsByTagName("input")[0].getAttribute("value");
    }

    if (currentPreferredCitation == "true") {
    	 removeOtherTickImage();
        //restore image
        addTickImage(preferredCitationTD);
    }

    // Citation and Abstract are in the same cell now so we
    // need to get both elements before doing any updates

    var parentTD = $("citation_" + paperId);

    var citationInput = parentTD.getElementsByTagName("textarea")[0];
    var abstractInput = parentTD.getElementsByTagName("textarea")[1];

    //Citation -- use value of hidden input, and append it, keeping the hidden input
    //remove the first textarea
    parentTD.removeChild(parentTD.getElementsByTagName("textarea")[0]);
    currentCitation = parentTD.getElementsByTagName("input")[0].getAttribute("value");
    // For IE8... sigh.
    if (currentCitation == null) currentCitation = "";

    //Abstract
    //remove the second textarea (which is now the first!)
    parentTD.removeChild(parentTD.getElementsByTagName("textarea")[0]);
    currentAbstract = parentTD.getElementsByTagName("input")[1].getAttribute("value");
    // And again... sigh.
    if (currentAbstract == null) currentAbstract = "";

    // Now update the cell
    wrapWithNonTruncatingSpan(currentCitation, "citation", parentTD, "");
    if (currentAbstract.length > 0) {
        addHideAbstractDiv(paperId, parentTD);
    }
	addShowAbstractDiv(paperId, currentAbstract, parentTD);

    //Hide/Show Paper Save/Edit Divs
    paperSaveDiv = $("paperSaveDiv_" + paperId);
    paperSaveDiv.style.display = "none";

    paperEditDiv = $("paperEditDiv_" + paperId);
    paperEditDiv.style.display = "inline";
}

//
// Display External Analysis File (if appropriate)
//
function displayExternalAnalysis(fileId) {
    // Get rid of any status indicator on this page...
    var statusIndicator = $('ea_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";

    new Ajax.Request("Display", {
         parameters: {
             requestingMethod: "FilesInfo.displayExternalAnalysis",
             datasetId: dataset,
             externalAnalysisId: fileId
         },
         onComplete: displayExternalAnalysisContent,
         onException: function (request, exception) { throw(exception); }
     });

}

//
// Handle AJAX response from displayExternalAnalysis
//
function displayExternalAnalysisContent(transport) {
    var response = transport.responseText;
    var data = response.evalJSON();
    var messageType = data.messageType;
    var message = data.message;
    var eaText = data.value;

    var fileName = data.fileName;
    var fileId = data.fileId;
    var fileTitle = data.fileTitle;
    var datasetId = data.datasetId;

    if (messageType == "ERROR" || messageType == 'UNAUTHORIZED') {
	errorPopup(message);
    } else if (messageType == 'SUCCESS') {

	var theEATable = $("externalAnalysesInfo");
	theEATable.style.display = "none";

	var theEAUploadButton = $("fileUploadButton");
	if (theEAUploadButton != null) {
	    theEAUploadButton.style.display = "none";
	}

	var displayDiv = $("externalAnalysisOutput");
	var titleSpan = $("eaTitleSpan");
	titleSpan.innerHTML = fileTitle;
	
	var downloadLink = $("eaDownloadLink");
	downloadLink.href = "Download?fileName=" + fileName + "&fileId=" + fileId + "&datasetId=" + datasetId;

	var t = $("eaFileTitleTable");
	
	var p = $("eaText");
	if (p.innerText) {
		p.innerText = eaText;
	} else {
		p.innerHTML = eaText;
	}
	p.className = "eaText";
	
	displayDiv.style.display = "inline";
    }
}

//
// Edit External Analyses
//
function makeExtAnalysesRowEditable(fileId) {
    // Get rid of any status indicator on this page...
    var statusIndicator = $('ea_status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";

    //Title
    titleTD = $("title_" + fileId);
    hiddenTitleInputs = titleTD.getElementsByTagName("input");

    // save all hidden inputs, to be restored after edit
    var clonedHiddenInputs = new Array(hiddenTitleInputs.length);
    for (i = 0; i < hiddenTitleInputs.length; i++) {
	clonedHiddenInputs[i] = hiddenTitleInputs[i].cloneNode(false);
    }

    var currentTitle = $('title_input_' + fileId).value.escapeHTML();
    var currentDesc = $('desc_input_' + fileId).value.escapeHTML();

    // Remember the 'fileIsViewable' tag...
    var fileIsViewableInput = $('fileIsViewable_' + fileId);

    titleTD.innerHTML = "<textarea id=\"fileTitleInput\" name=\"eaTitle\" rows=\"4\" cols=\"20\">" + currentTitle + "</textarea> <br/> <p>Description</p> <textarea id=\"fileDescInput\" name=\"eaDescription\" rows=\"4\" cols=\"20\">" + currentDesc + "</textarea> <br/> ";
    
    for (i = 0; i < clonedHiddenInputs.length; i++) {
	titleTD.appendChild(clonedHiddenInputs[i]);
    }

    //Hide/Show File Save/Edit Divs
    extAnalysesSaveDiv = $("extAnalysesSaveDiv_" + fileId);
    extAnalysesSaveDiv.style.display = "inline";

    extAnalysesEditDiv = $("extAnalysesEditDiv_" + fileId);
    extAnalysesEditDiv.style.display = "none";
}

function truncateEATitleAndSave(eaId) {
    // limit title text to 255 characters
    limit("fileTitleInput", 255);
    sendExtAnalysesSaveRequest(eaId);
}

function truncateEADescAndSave(eaId) {
    // limit description text to 500 characters
    limit("fileDescInput", 500);
    sendExtAnalysesSaveRequest(eaId);
}

//
// Make AJAX request to save the changes to the file
//
function saveExtAnalysesChanges(fileId) {
    if ($F("fileTitleInput").length > 255) {
	$("titleSureDiv_" + fileId).style.display = "inline";
	$("extAnalysesSaveDiv_" + fileId).style.display = "none";
    } else if ($F("fileDescInput").length > 500) {
	$("descriptionSureDiv_" + fileId).style.display = "inline";
	$("extAnalysesSaveDiv_" + fileId).style.display = "none";
    } else {
	sendExtAnalysesSaveRequest(fileId);
    }
}

function sendExtAnalysesSaveRequest(fileId) {
    new Ajax.Request("Edit", {
         parameters: {
             requestingMethod: "FilesInfo.sendExtAnalysesSaveRequest",
             datasetId: dataset,
             externalAnalysisId: fileId,
             externalAnalysisTitle: $F("fileTitleInput"),
             fileDescription: $F("fileDescInput")
         },
         onComplete: extAnalysesModified,
         onException: function (request, exception) { throw(exception); }
     });
}

//
// Handle AJAX response from sendExtAnalysesSaveRequest
//
function extAnalysesModified(transport) {

    var response = transport.responseText;
    var data = response.evalJSON();
    var messageType = data.messageType;
    var message = data.message;
    var eaId = data.value;

    if (messageType == "ERROR" || messageType == 'UNAUTHORIZED') {
	errorPopup(message);
    } else if (messageType == 'SUCCESS') {

	// Title and Description are in the same cell so we
	// need to get both elements before doing any updates
	titleInput = $("fileTitleInput");
	descInput = $("fileDescInput");

	// Likewise, save parent node.
	parentTd = titleInput.parentNode;

	var hiddenTitleInputs = parentTd.getElementsByTagName("input");

	// save all hidden inputs, to be restored after edit
	var clonedHiddenInputs = new Array(hiddenTitleInputs.length);
	for (i = 0; i < hiddenTitleInputs.length; i++) {
	    clonedHiddenInputs[i] = hiddenTitleInputs[i].cloneNode(false);
	}

	var fileId = titleTD.id.substring(6, titleTD.id.length);

	// Restore the 'fileIsViewable' tag...
	var fileIsViewableInput = $('fileIsViewable_' + fileId);

	//Title
	var titleTruncated = false;
	var truncatedTitle = "";
	if (titleInput.value.length > 100) {
	    titleTruncated = true;
	    truncatedTitle = titleInput.value.substring(0, 100);
	    truncatedTitle += "...";
	}

	var titleToDisplay = titleTruncated ? truncatedTitle : titleInput.value;
	var currentTitle = titleInput.value.substring(0, 254);

	var titleSpan = document.createElement('span');
	titleSpan.name = "title";
	titleSpan.innerHTML = titleToDisplay.escapeHTML();
	titleSpan.id = "s_title_" + fileId;
	titleSpan.className = "bTitle";
	parentTd.innerHTML = "";

	if (fileIsViewableInput.value == "true") {
	    var a = document.createElement('a');
	    a.href = "javascript:displayExternalAnalysis(" + fileId + ")";
	    parentTd.appendChild(a);
	    a.appendChild(titleSpan);
	} else {
	    parentTd.appendChild(titleSpan);
	}

	if (titleTruncated) {
	    var moreDiv = document.createElement('div');
	    moreDiv.className = "moreTitleDiv";
	    moreDiv.id = "moreTitleDiv_" + fileId;
	    moreDiv.appendChild(document.createElement('br'));
	    var a = document.createElement('a');
	    a.textContent = "more";
	    a.href = "javascript:showFullTitle(" + fileId + ")";
	    moreDiv.appendChild(a);
	    moreDiv.appendChild(document.createElement('br'));
	    parentTd.appendChild(moreDiv);

	    var lessDiv = document.createElement('div');
	    lessDiv.className = "lessTitleDiv";
	    lessDiv.id = "lessTitleDiv_" + fileId;
	    lessDiv.appendChild(document.createElement('br'));
	    var a = document.createElement('a');
	    a.textContent = "less";
	    a.href = "javascript:showTruncatedTitle(" + fileId + ")";
	    lessDiv.appendChild(a);
	    lessDiv.appendChild(document.createElement('br'));
	    parentTd.appendChild(lessDiv);
	}

	parentTd.appendChild(hiddenWithValueAndId(titleInput.value, "title_input_" + fileId));
	parentTd.appendChild(hiddenWithValueAndId(titleInput.value, "title_full_" + fileId));
	parentTd.appendChild(hiddenWithValueAndId(truncatedTitle, "title_trunc_" + fileId));
	parentTd.appendChild(hiddenWithValueAndId(titleTruncated, "title_truncated_" + fileId));

	//Description
	var descTruncated = false;
	var truncatedDesc = "";
	if (descInput.value.length > 100) {
	    descTruncated = true;
	    truncatedDesc = descInput.value.substring(0, 100);
	    truncatedDesc += "...";
	}

	var descToDisplay = descTruncated ? truncatedDesc : descInput.value;

	parentTd.appendChild(document.createElement('br'));
	var descSpan = document.createElement('span');
	descSpan.name = "description";
	descSpan.innerHTML = descToDisplay.escapeHTML();
	descSpan.id = "s_description_" + fileId;
	parentTd.appendChild(descSpan);

	if (descTruncated) {
	    var moreDiv = document.createElement('div');
	    moreDiv.className = "moreDescDiv";
	    moreDiv.id = "moreDescDiv_" + fileId;
	    var a = document.createElement('a');
	    a.textContent = "more";
	    a.href = "javascript:showFullDescription(" + fileId + ")";
	    moreDiv.appendChild(a);
	    parentTd.appendChild(moreDiv);

	    var lessDiv = document.createElement('div');
	    lessDiv.className = "lessDescDiv";
	    lessDiv.id = "lessDescDiv_" + fileId;
	    var a = document.createElement('a');
	    a.textContent = "less";
	    a.href = "javascript:showTruncatedDesc(" + fileId + ")";
	    lessDiv.appendChild(a);
	    parentTd.appendChild(lessDiv);
	}

	parentTd.appendChild(hiddenWithValueAndId(descInput.value, "desc_input_" + fileId));
	parentTd.appendChild(hiddenWithValueAndId(descInput.value, "desc_full_" + fileId));
	parentTd.appendChild(hiddenWithValueAndId(truncatedDesc, "desc_trunc_" + fileId));
	parentTd.appendChild(hiddenWithValueAndId(descTruncated, "desc_truncated_" + fileId));

	titleTD.appendChild(fileIsViewableInput);

	//Hide/Show File Save/Edit Divs
	$("extAnalysesSaveDiv_" + fileId).style.display = "none";
	$("titleSureDiv_" + fileId).style.display = "none";
	$("descriptionSureDiv_" + fileId).style.display = "none";
	$("extAnalysesEditDiv_" + fileId).style.display = "inline";

	var sortBy = $('extAnalysesSortBy').value;
	_sortExternalAnalyses(sortBy, "false");
	successPopup(message);
    }
}

//
// Cancel edits to the fields
//
function cancelExtAnalysesChanges(fileId) {

    // Title and Description are in the same cell so we
    // need to get both elements before doing any updates

    // Common parent element
    titleTD = $("title_" + fileId);

    //Title
    //remove the first textarea
    titleTD.removeChild(titleTD.getElementsByTagName("textarea")[0]);
    var currentTitle = $('title_input_' + fileId).value;

    //Description
    //remove the second textarea (which is now the first!)
    titleTD.removeChild(titleTD.getElementsByTagName("textarea")[0]);
    var currentDesc = $('desc_input_' + fileId).value;

    var titleTruncatedDiv = $('title_truncated_' + fileId);
    if (titleTruncatedDiv && (titleTruncatedDiv.value == "true")) {
	currentTitle = $('title_trunc_' + fileId).value;
    }

    var descTruncatedDiv = $('desc_truncated_' + fileId);
    if (descTruncatedDiv && (descTruncatedDiv.value == "true")) {
	currentDesc = $('desc_trunc_' + fileId).value;
    }

    // Restore the 'fileIsViewable' tag...
    var fileIsViewableInput = $('fileIsViewable_' + fileId);

    var hiddenTitleInputs = titleTD.getElementsByTagName("input");

    // save all hidden inputs, to be restored after edit
    var clonedHiddenInputs = new Array(hiddenTitleInputs.length);
    for (i = 0; i < hiddenTitleInputs.length; i++) {
	clonedHiddenInputs[i] = hiddenTitleInputs[i].cloneNode(false);
    }

    // Now update cell
    titleTD.innerHTML = "";

    var titleSpan = document.createElement('span');
    titleSpan.name = "title";
    titleSpan.innerHTML = currentTitle.escapeHTML();
    titleSpan.id = "s_title_" + fileId;
    titleSpan.className = "bTitle";
    if (fileIsViewableInput.value == "true") {
	var a = document.createElement('a');
	a.href = "javascript:displayExternalAnalysis(" + fileId + ")";
	titleTD.appendChild(a);
	a.appendChild(titleSpan);
    } else {
	titleTD.appendChild(titleSpan);
    }
    titleTD.appendChild(document.createElement('br'));

    if (titleTruncatedDiv && (titleTruncatedDiv.value == "true")) {
	var moreDiv = document.createElement('div');
	moreDiv.className = "moreTitleDiv";
	moreDiv.id = "moreTitleDiv_" + fileId;
	moreDiv.appendChild(document.createElement('br'));
	var a = document.createElement('a');
	a.textContent = "more";
	a.href = "javascript:showFullTitle(" + fileId + ")";
	moreDiv.appendChild(a);
	moreDiv.appendChild(document.createElement('br'));
	titleTD.appendChild(moreDiv);

	var lessDiv = document.createElement('div');
	lessDiv.className = "lessTitleDiv";
	lessDiv.id = "lessTitleDiv_" + fileId;
	lessDiv.appendChild(document.createElement('br'));
	var a = document.createElement('a');
	a.textContent = "less";
	a.href = "javascript:showTruncatedTitle(" + fileId + ")";
	lessDiv.appendChild(a);
	lessDiv.appendChild(document.createElement('br'));
	titleTD.appendChild(lessDiv);
    }

    var descSpan = document.createElement('span');
    descSpan.name = "description";
    descSpan.innerHTML = currentDesc.escapeHTML();
    descSpan.id = "s_description_" + fileId;
    titleTD.appendChild(descSpan);

    if (descTruncatedDiv && (descTruncatedDiv.value == "true")) {
	var moreDiv = document.createElement('div');
	moreDiv.className = "moreDescDiv";
	moreDiv.id = "moreDescDiv_" + fileId;
	var a = document.createElement('a');
	a.textContent = "more";
	a.href = "javascript:showFullDescription(" + fileId + ")";
	moreDiv.appendChild(a);
	titleTD.appendChild(moreDiv);

	var lessDiv = document.createElement('div');
	lessDiv.className = "lessDescDiv";
	lessDiv.id = "lessDescDiv_" + fileId;
	var a = document.createElement('a');
	a.textContent = "less";
	a.href = "javascript:showTruncatedDesc(" + fileId + ")";
	lessDiv.appendChild(a);
	titleTD.appendChild(lessDiv);
    }

    // Restore hidden inputs.
    for (i = 0; i < clonedHiddenInputs.length; i++) {
	titleTD.appendChild(clonedHiddenInputs[i]);
    }

    //Hide/Show file Save/Edit Divs
    $("extAnalysesSaveDiv_" + fileId).style.display = "none";
    $("titleSureDiv_" + fileId).style.display = "none";
    $("descriptionSureDiv_" + fileId).style.display = "none";
    $("extAnalysesEditDiv_" + fileId).style.display = "inline";
}

function extAnalysesDeleteAreYouSure(eaId) {
    //Hide/Show File Save/Edit Divs
    eaSaveDiv = $("extAnalysesSureDiv_" + eaId);
    eaSaveDiv.style.display = "inline";

    eaEditDiv = $("extAnalysesEditDiv_" + eaId);
    eaEditDiv.style.display = "none";
}

function extAnalysesDeleteAreYouSureDone(eaId) {
    //Hide/Show File Save/Edit Divs
    eaSaveDiv = $("extAnalysesSureDiv_" + eaId);
    eaSaveDiv.style.display = "none";

    eaEditDiv = $("extAnalysesEditDiv_" + eaId);
    eaEditDiv.style.display = "inline";
}

//
// Cancel edits to the fields
//
function cancelFileChanges(fileId) {

    // Title and Description are in the same cell now so we
    // need to get both elements before doing any updates

    // Common parent element
    titleTD = $("title_" + fileId);

    //Title
    //remove the first textarea
    titleTD.removeChild(titleTD.getElementsByTagName("textarea")[0]);
    var currentTitle = $('title_input_' + fileId).value;

    //Description
    //remove the second textarea (which is now the first!)
    titleTD.removeChild(titleTD.getElementsByTagName("textarea")[0]);
    var currentDesc = $('desc_input_' + fileId).value;

    var titleTruncatedDiv = $('title_truncated_' + fileId);
    if (titleTruncatedDiv && (titleTruncatedDiv.value == "true")) {
    	currentTitle = $('title_trunc_' + fileId).value;
    }

    var descTruncatedDiv = $('desc_truncated_' + fileId);
    if (descTruncatedDiv && (descTruncatedDiv.value == "true")) {
    	currentDesc = $('desc_trunc_' + fileId).value;
    }

    var hiddenTitleInputs = titleTD.getElementsByTagName("input");

    // save all hidden inputs, to be restored after edit
    var clonedHiddenInputs = new Array(hiddenTitleInputs.length);
    for (i = 0; i < hiddenTitleInputs.length; i++) {
    	clonedHiddenInputs[i] = hiddenTitleInputs[i].cloneNode(false);
    }

    // Now update cell
    var titleSpan = document.createElement('span');
    titleSpan.name = "title";
    titleSpan.innerHTML = currentTitle.escapeHTML();
    titleSpan.id = "s_title_" + fileId;
    titleSpan.className = "bTitle";
    titleTD.innerHTML = "";
    titleTD.appendChild(titleSpan);
    titleTD.appendChild(document.createElement('br'));

    if (titleTruncatedDiv && (titleTruncatedDiv.value == "true")) {
	var moreDiv = document.createElement('div');
	moreDiv.className = "moreTitleDiv";
	moreDiv.id = "moreTitleDiv_" + fileId;
	var a = document.createElement('a');
	a.textContent = "more";
	a.href = "javascript:showFullTitle(" + fileId + ")";
	moreDiv.appendChild(a);
	moreDiv.appendChild(document.createElement('br'));
	titleTD.appendChild(moreDiv);

	var lessDiv = document.createElement('div');
	lessDiv.className = "lessTitleDiv";
	lessDiv.id = "lessTitleDiv_" + fileId;
	var a = document.createElement('a');
	a.textContent = "less";
	a.href = "javascript:showTruncatedTitle(" + fileId + ")";
	lessDiv.appendChild(a);
	lessDiv.appendChild(document.createElement('br'));
	titleTD.appendChild(lessDiv);
    }

    var descSpan = document.createElement('span');
    descSpan.name = "description";
    descSpan.innerHTML = currentDesc.escapeHTML();
    descSpan.id = "s_description_" + fileId;
    titleTD.appendChild(descSpan);

    if (descTruncatedDiv && (descTruncatedDiv.value == "true")) {
	var moreDiv = document.createElement('div');
	moreDiv.className = "moreDescDiv";
	moreDiv.id = "moreDescDiv_" + fileId;
	var a = document.createElement('a');
	a.textContent = "more";
	a.href = "javascript:showFullDescription(" + fileId + ")";
	moreDiv.appendChild(a);
	titleTD.appendChild(moreDiv);

	var lessDiv = document.createElement('div');
	lessDiv.className = "lessDescDiv";
	lessDiv.id = "lessDescDiv_" + fileId;
	var a = document.createElement('a');
	a.textContent = "less";
	a.href = "javascript:showTruncatedDesc(" + fileId + ")";
	lessDiv.appendChild(a);
	titleTD.appendChild(lessDiv);
    }

    // Restore hidden inputs.
    for (i = 0; i < clonedHiddenInputs.length; i++) {
	titleTD.appendChild(clonedHiddenInputs[i]);
    }

    //Hide/Show file Save/Edit Divs
    $("fileSaveDiv_" + fileId).style.display = "none";
    $("titleSureDiv_" + fileId).style.display = "none";
    $("fileEditDiv_" + fileId).style.display = "inline";
}

// Replace HTML contents of td with a non-Truncatorized version of input
// and a hidden input preserving the original value.
function wrapWithNonTruncatingSpan(input, name, td, clazz) {
    span = document.createElement('span');
    span.name = name;
    span.innerHTML = input.escapeHTML();
    span.id = td.id + "_span";
    span.className = clazz;
    td.innerHTML = "";
    td.appendChild(span);
    td.appendChild(hiddenWithValue(input));
}

// Concatenate to existing HTML contents...
// ... used with wrapWithNonTruncatingSpan()
function concatWithNonTruncatingSpan(input, name, td, needBreak) {
    if (needBreak) td.appendChild(document.createElement('br'));
    span = document.createElement('span');
    span.name = name;
    span.innerHTML = input.escapeHTML();
    span.id = "s_" + td.id;
    td.appendChild(span);
    td.appendChild(hiddenWithValue(input));
}

//Add a tick image to the cell.
function addTickImage(td) {
    var id = "preferred_citation_image";
    var otherValue = "Preferred paper for citation";
    var image = new Element("img", { "src": "images/tick.png",
    	                            "id": id,
    	                            "alt": otherValue,
    	                            "title": otherValue } );
    td.appendChild(image);
}

//Remove an image element in a TD element
function removeOtherTickImage() {
    var img = $("preferred_citation_image");
    if (img != null) {
	preferredCitationTD = img.parentNode;
	preferredCitationTD.removeChild(img);
	// set hidden field value to false;
	changeHiddenValue(preferredCitationTD, 'preferred_citation_hidden', "false");
    }
}

//Remove check box element in a TD element
function removeCheckbox(td) {
    var checkBox = td.getElementsByTagName("input");
    td.removeChild(checkBox[0]);
}

// Change the hidden field value given an element id
function changeHiddenValue(parent, id, val) {
	var myInputs = parent.getElementsByTagName("input");
	for (var i = 0; i < myInputs.length; i++) {
		if (myInputs[i].id == id) {
			myInputs[i].value = val;
			break;
		}
	}
}

// Create a hidden input element with value
function hiddenWithValueAndId(value, id) {
    hidden = document.createElement("input");
    hidden.setAttribute("type","hidden");
    hidden.setAttribute("value", value);
    hidden.setAttribute("id", id);
    return hidden;
}

// Create a hidden input element with value
function hiddenWithValue(value) {
    hidden = document.createElement("input");
    hidden.setAttribute("type","hidden");
    hidden.setAttribute("value", value);
    return hidden;
}

function singleCheck (fieldName, current) {
	var objInputs = document.forms['embeddedPapersForm'].getElementsByTagName('input');

	if(!objInputs)
		return;

	var countInputs = objInputs.length;

	if(!countInputs)
		objInputs.checked = true;
	else
		for(var i = 0; i < countInputs; i++) {
			var currentObj = objInputs[i];
			if (currentObj.type == 'checkbox'){
			    if (current != currentObj) {
			    	currentObj.checked = false;
			    }
			}
		}
}

function showStatusIndicator(titleString) {
  var div = document.createElement('div');
  div.id = "uploadStatusIndicator";
  div.className = "uploadStatusIndicator";

  jQuery('body').append(div);

  //Status Indicator Popup
  jQuery('.uploadStatusIndicator').dialog({
      modal : true,
      autoOpen : false,
      title : titleString,
      width : 120,
      height : 120
  });

  // Create form elements to add to the status indicator
  jQuery('#uploadStatusIndicator').html('<center><img id="waitingIcon" src="images/waiting.gif" /></center>');
  jQuery('#uploadStatusIndicator').dialog('open');
  jQuery('#uploadStatusIndicator').dialog( "option", "stack", true );
  jQuery('#uploadStatusIndicator').dialog( "option", "resizable", false );
}

function hideStatusIndicator() {
  jQuery('#uploadStatusIndicator').dialog('close');

}
