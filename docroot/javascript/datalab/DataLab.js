//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2014
// All Rights Reserved
//
// Author: Cindy Tipper
// Version: $Revision: 13705 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-11-22 10:55:32 -0500 (Tue, 22 Nov 2016) $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// Constants
// Limit for dragged files is 1M. Arbitrary, yes.
var FILE_SIZE_LIMIT = 1000000;

// jQuery's Onload function
jQuery(document).ready(function() {

        jQuery('.doYouWantToOption').click(openThreeOptions);

        jQuery('#displayAnonIds').click(toggleAnonStudentIds);

        jQuery('.students_per_page').change(changeStudentsPerPage);
        jQuery('.items_per_page').change(changeItemsPerPage);

        // Set up sort menu actions
        jQuery(".sortAnchor").click(sortAnchorToggle);
        jQuery(".sortSubmenu").mouseup(function() { return false });
        jQuery(".sortAnchor").mouseup(function() { return false });
        jQuery(document).mouseup(sortAnchorHide);

        jQuery('#original_sort_link').click(originalSortOrder);
        jQuery('#difficulty_sort_link').click(difficultySort);

        jQuery('#studentShadingInput').click(toggleStudentShading);
        jQuery('#student-score-threshold-input').change(changeStudentScoreThreshold);

        jQuery('#cellShadingInput').click(toggleCellShading);
        jQuery('.threshold-input').change(changeCorrelationThreshold);

        jQuery('#summary_computed_link').click(summaryChanged);
        jQuery('#summary_provided_link').click(summaryChanged);

        // I don't understand the syntax of the dom param but it wraps the
        // table in <div class="datatable-scroll"> which I use to restrict width.
        if (jQuery('#overviewTable').length) {
            var overviewTable = jQuery('#overviewTable').dataTable({
                dom: 'r<"H"lf><"datatable-scroll"t><"F"ip>',
                info: false,
                jQueryUI: true,
                scrollY: "400px",
                scrollX: true,
                scrollCollapse: true,
                paginate: false,
                searching: false,
                ordering: false
            });

            new jQuery.fn.dataTable.FixedColumns(overviewTable, {
                leftColumns: 2,
                rightColumns: 1
            });
        }

        if (jQuery('#correlationTable').length) {
            var correlationTable = jQuery('#correlationTable').dataTable({
                dom: 'r<"H"lf><"datatable-scroll"t><"F"ip>',
                info: false,
                jQueryUI: true,
                scrollY: "400px",
                scrollX: true,
                scrollCollapse: true,
                paginate: false,
                searching: false,
                ordering: false
             });

            new jQuery.fn.dataTable.FixedColumns(correlationTable, {
                leftColumns: 1
            });

            var cronbachsTable = jQuery('#cronbachsAlphaTable').dataTable({
                info: false,
                jQueryUI: true,
                scrollY: "400px",
                scrollCollapse: true,
                paginate: false,
                searching: false,
                ordering: false
            });
        }

        var message = jQuery('#message').val();
        var messageLevel = jQuery('#messageLevel').val();

        if ((message != undefined) && (message != "null")) {
            if (messageLevel == "SUCCESS") {
                successPopup(message);
            } else if (messageLevel == "ERROR") {
                errorPopup(message);
            } else {
                warningPopup(message);
            }
        }

});

function openThreeOptions() {
    var li = jQuery(this).parent();
    var liId = li.attr('id');

    new Ajax.Request("DataLab", {
        parameters: {
            requestingMethod: "openThreeOptions",
            populateGettingStarted: liId
        },
        onComplete: populateThreeOptions,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function populateThreeOptions(transport) {

    var json = transport.responseText.evalJSON(true);
    var liId = json.gettingStartedOption;
    var sampleAnalysisId = json.sampleAnalysisId;
    var exampleVideoUrl = json.exampleVideoUrl;

    var li = jQuery('#' + liId);
    var optionsDiv = jQuery('#three-options-div');

    var opt1 = jQuery('#option-1');
    var opt2 = jQuery('#option-2');
    var opt3 = jQuery('#option-3');

    opt1.attr('href', 'javascript:useMyGradebook("' + liId + '")');
    opt2.attr('href', 'javascript:useSampleGradebook("' + liId + '", ' + sampleAnalysisId + ')');
    opt3.attr('href', exampleVideoUrl);

    // Sigh... one of the 'Do you want to' options has different wording in sub-items.
    if (liId === 'gsOptionAdditional') {
        opt1.html('Analyze my assignment');
        opt2.html('Analyze a sample assignment');
    } else {
        opt1.html('Analyze my gradebook');
        opt2.html('Analyze a sample gradebook');
    }

    li.append(optionsDiv);
    optionsDiv.show();
}

function useMyGradebook(gsOption) {

    new Ajax.Request("DataLab", {
        parameters: {
            requestingMethod: "useMyGradebook",
            updateGettingStartedOption: gsOption
        },
        onComplete: openUploadDialog,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function useSampleGradebook(gsOption, sampleAnalysisId) {
    jQuery(
           '<form id="updateChosenOptionForm" method="post" '
           + 'action="DataLab?analysisId=' + sampleAnalysisId + '">'
           + '<input name="useSampleGradebook" type="hidden" value="true"/>'
           + '<input name="updateGettingStartedOption" type="hidden" value="' + gsOption
           + '"/></form>').appendTo('body').submit();
}

function toggleAnonStudentIds() {
    var analysisId = jQuery("#analysisId").val();
    var isChecked = jQuery('#displayAnonIds').attr('checked') ? "true" : "false";

    // Update DatalabContext.
    new Ajax.Request("DataLab", {
        parameters: {
            requestingMethod: "toggleAnonStudentIds",
            analysisId: analysisId,
            showAnonStudentIds: isChecked
        },
        onComplete: updateStudentDisplay,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function updateStudentDisplay(transport) {

    // Affect change in display.

    var json = transport.responseText.evalJSON(true);
    var numStudents = json.numStudents;
    var studentList = json.studentList;

    var studentIdList = [];
    var studentInfoList = [];
    for (var key in studentList) {
        if (studentList.hasOwnProperty(key)) {
            studentInfoList.push(studentList[key]);
            studentIdList.push(key);
        }
    }

    var predraw = false;
    var overviewTable = jQuery('#overviewTable').dataTable();
    for (var i = 0; i < numStudents; i++) {
        if (i == (numStudents - 1)) { predraw = true; }

        var studentId = studentIdList[i];
        var studentInfo = studentInfoList[i];

        var td = jQuery('#td_student_display_' + studentId);

        overviewTable.fnUpdate(studentInfo, td.parent(), 1, false, predraw);
    }
    overviewTable.fnDraw();
}

function clearOverviewInfo() {
    jQuery('#overview-info-div').hide();
}

function changeAnalysisType() {
    var analysisId = jQuery("#analysisId").val();
    jQuery(
           '<form id="changeAnalysisTypeForm" method="post" '
           + 'action="DataLab?analysisId=' + analysisId
           + '"><input name="toggleAnalysisType" type="hidden" value="true"'
           + '/></form>').appendTo('body').submit();
}

function notImplemented() {
    var msg = "This feature has not been implemented. "
        + "Once implemented, you can save your analysis.";
    warningPopup(msg);
}

function gotoStudent(pageNum) {
    var analysisId = jQuery("#analysisId").val();
    jQuery(
           '<form id="changePageForm" method="post" action="DataLab?analysisId=' + analysisId
           + '"><input name="currentPage" type="hidden" value="' + pageNum
           + '"/></form>').appendTo('body').submit();
}

function gotoItem(panelNum) {
    var analysisId = jQuery("#analysisId").val();
    jQuery(
           '<form id="changePanelForm" method="post" action="DataLab?analysisId=' + analysisId
           + '"><input name="currentPanel" type="hidden" value="' + panelNum
           + '"/></form>').appendTo('body').submit();
}

function changeStudentsPerPage(event) {
    var analysisId = jQuery("#analysisId").val();
    var numRows = jQuery(this).val();
    jQuery(
           '<form id="studentsPerPageForm" method="post" action="DataLab?analysisId='
           + analysisId
           + '"><input name="rowsPerPage" type="hidden" value="' + numRows
           + '"/></form>').appendTo('body').submit();
}

function changeItemsPerPage(event) {
    var analysisId = jQuery("#analysisId").val();
    var numCols = jQuery(this).val();
    jQuery(
           '<form id="itemsPerPageForm" method="post" action="DataLab?analysisId='
           + analysisId
           + '"><input name="colsPerPage" type="hidden" value="' + numCols
           + '"/></form>').appendTo('body').submit();
}

function sortOverview(sortByColumn) {
    var analysisId = jQuery("#analysisId").val();
    jQuery(
           '<form id="sortByForm" method="post" '
           + 'action="DataLab?analysisId=' + analysisId
           + '"><input name="sortBy" type="hidden" value="' + sortByColumn
           + '"/></form>').appendTo('body').submit();
}

function loadOverview(analysisId) {
    var subtab = "Overview";
    loadSubtabWithId(subtab, analysisId);
}

function loadSubtab(subtab) {
    var analysisId = jQuery("#analysisId").val();
    
    // Disabled subtabs will have an analysisId of "null".
    if (analysisId == "null") { return; }

    loadSubtabWithId(subtab, analysisId);
}

function loadSubtabWithId(subtab, analysisId) {
    var thisEle = jQuery('#' + subtab);

    // If this tab is already loaded, nothing to do.
    if (thisEle.hasClass('active')) return;

    jQuery('#nav-tabs-div').children('li').each(function() { jQuery(this).removeClass('active'); });

    thisEle.addClass('active');

    var actionStr = 'action="DataLab?analysisId=' + analysisId;

    if (subtab === 'Home') {
        actionStr = 'action="DataLab';
    }

    jQuery(
           '<form id="loadSubtabForm" method="post" '
           + actionStr
           + '"><input name="subtab" type="hidden" value="' + subtab
           + '"/></form>').appendTo('body').submit();
}

function openUploadDialog() {
    var dialogDiv = document.getElementById('addAnalysisDialog');
    dialogDiv.innerHTML = "";

    var newForm = document.createElement('form');
    newForm.id   = "uploadFileForm";
    newForm.name = "uploadFileForm";
    newForm.action = "DataLab";
    newForm.method = "post";
    newForm.enctype  = "multipart/form-data";
    newForm.encoding = "multipart/form-data";  // IE7
    newForm.onsubmit = "";

    newInput = document.createElement('p');
    newInput.innerHTML = "Choose a file to upload";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name = "fileName";
    newInput.type = "file";
    newInput.id = "file-chooser";
    newForm.appendChild(newInput);

    newForm.appendChild(document.createElement('br'));

    var infoLink = document.createElement('a');
    infoLink.id = "view-file-requirements";
    infoLink.href = "javascript:viewFileRequirements()";
    infoLink.textContent = "View file requirements";
    newForm.appendChild(infoLink);

    // Attach the form to the div
    dialogDiv.appendChild(newForm);

    jQuery('#addAnalysisDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 450,
        height : 225,
        title : "Upload File",
        buttons : [ {
            id : "upload-file-button",
            text : "Upload",
            click : uploadAnalysisFile
        }, {
            id : "cancel-upload-file-button",
            text : "Cancel",
            click : closeUploadDialog
        } ]
    });

    jQuery('#upload-file-button').button('disable');
    jQuery('#file-chooser').change(enableUploadButton);
    jQuery('#addAnalysisDialog').dialog('open');
}

function enableUploadButton() {
    var fileFlag = ((jQuery('#file-chooser').val() != "") &&
                    (jQuery('#file-chooser').val() != undefined));

    if (fileFlag) {
        jQuery('#upload-file-button').button('enable');
    } else {
        jQuery('#upload-file-button').button('disable');
    }
}

function uploadAnalysisFile() {
    jQuery('#drop-file-div').hide();
    jQuery('#save-analysis-div').hide();
    jQuery('.outer-div').hide();

    var fileName = jQuery('#file-chooser').val();
    // Sigh... strip off fake path, if present.
    var index = fileName.lastIndexOf('/');
    if (index == -1) { index = fileName.lastIndexOf('\\'); }
    if (index > 0) {
        fileName = fileName.substring(index + 1);
    }

    jQuery('.outer-div').hide();
    jQuery('.drop-file-info').html("Loading your file... <br/>" + fileName);
    jQuery('#file-loading-div').show();

    jQuery('#uploadFileForm').submit();
    jQuery('#addAnalysisDialog').dialog('close');
}

function closeUploadDialog() {
    jQuery(this).dialog("close");
}

/** Drag/drop support for importing file. */
function dropFile(e) {
    e.preventDefault();
    var files = e.dataTransfer.files;
    var f = files[0];
    var fileType = /text/;
    // Limit fileSize to 1M...
    if (f.size > FILE_SIZE_LIMIT) {
        warningPopup('Specified file is too large (' + f.size
                     + ' bytes). Size limit is ' + FILE_SIZE_LIMIT + ' bytes.');
    } else if (fileType.test(f.type)) {
        var theReader = new FileReader();
        theReader.onload = function(ev) {
            var data = ev.target.result;

            jQuery('.outer-div').hide();
            jQuery('.drop-file-info').html("Loading your file... <br/>" + f.name);
            jQuery('#file-loading-div').show();

            uploadDroppedFile(f, data);
        };
        theReader.readAsText(f);
    } else {
        warningPopup('Invalid file type "' + f.type + '". Please specify a text file.');
    }
}
function allowDrop(e) {
    e.preventDefault();
}
function uploadDroppedFile(theFile, theData) {
    var analysisId = jQuery("#analysisId").val();

    jQuery(
           '<form id="uploadFileForm" method="post" '
           + 'action="DataLab">'
           + '<input name="uploadDroppedFile" type="hidden" value="true"/>'
           + '<input name="fileName" type="hidden" value="' + theFile.name + '"/>'
           + '<input name="fileSize" type="hidden" value="' + theFile.size + '"/>'
           + '<input name="fileContent" type="hidden" value="' + theData
           + '"/></form>').appendTo('body').submit();
}

// Sorting dropdown
function sortAnchorToggle() {
    anchorId = "#".concat(this.id);
    submenuId = "#".concat(this.id.replace("Anchor", "Submenu"));

    //if we're going to show the menu, hide all other menus and set them to low zIndex values, 
    //set this one to high zIndex values but with anchor on top
    if (jQuery(submenuId).is(':visible') != true) {
        jQuery(".sortSubmenu").hide();
        jQuery(".sortAnchor").zIndex('50');
        jQuery(".sortSubmenu").zIndex('50');
        jQuery(anchorId).zIndex('110');
        jQuery(submenuId).zIndex('100');
    }

    jQuery(submenuId).toggle();
}

function sortAnchorHide() {
    jQuery(".sortAnchor").zIndex('50');
    jQuery(".sortSubmenu").zIndex('50');
    jQuery(".sortSubmenu").hide();
}

function originalSortOrder() {
    sortOverview(jQuery('#original_sort_link').val());
}

function difficultySort() {
    sortOverview(jQuery('#difficulty_sort_link').val());
}

function toggleStudentShading() {
    var analysisId = jQuery("#analysisId").val();

    var value = "false";
    if (jQuery('#studentShadingInput').attr('checked')) { value = "true"; }
    jQuery(
           '<form id="studentShadingForm" method="post" '
           + 'action="DataLab?analysisId=' + analysisId
           + '"><input name="studentShading" type="hidden" value="' + value
           + '"/></form>').appendTo('body').submit();
}

function changeStudentScoreThreshold() {
    var analysisId = jQuery("#analysisId").val();

    var threshold = jQuery('#student-score-threshold-input').val();

    jQuery(
           '<form id="changeStuThresholdForm" method="post" '
           + 'action="DataLab?analysisId=' + analysisId
           + '"><input name="studentScoreThreshold" type="hidden" value="' + threshold
           + '"/></form>').appendTo('body').submit();
}

function toggleCellShading() {
    var analysisId = jQuery("#analysisId").val();

    var value = "false";
    if (jQuery('#cellShadingInput').attr('checked')) { value = "true"; }
    jQuery(
           '<form id="cellShadingForm" method="post" '
           + 'action="DataLab?analysisId=' + analysisId
           + '"><input name="cellShading" type="hidden" value="' + value
           + '"/></form>').appendTo('body').submit();
}

function changeCorrelationThreshold() {
    var analysisId = jQuery("#analysisId").val();

    var highThreshold = jQuery('#high-threshold-value').val();
    var lowThreshold = jQuery('#low-threshold-value').val();

    jQuery(
           '<form id="changeCorrelThresholdForm" method="post" '
           + 'action="DataLab?analysisId=' + analysisId
           + '"><input name="highThreshold" type="hidden" value="' + highThreshold
           + '"><input name="lowThreshold" type="hidden" value="' + lowThreshold
           + '"/></form>').appendTo('body').submit();
}

function summaryChanged() {
    var analysisId = jQuery("#analysisId").val();

    jQuery(
           '<form id="changeSummaryForm" method="post" '
           + 'action="DataLab?analysisId=' + analysisId
           + '"><input name="summaryColumn" type="hidden" value="'
           + jQuery('input:radio[name=summary_column]:checked').val()
           + '"/></form>').appendTo('body').submit();
}

function openEmailDialog() {
    var analysisId = jQuery("#analysisId").val();

    // Start by getting list of students to be emailed.
    new Ajax.Request("DataLab", {
        parameters: {
            requestingMethod: "DataLab.openEmailDialog",
            analysisId: analysisId,
            getStrugglingStudentsList: "true"
        },
        onComplete: setupEmailDialog,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function setupEmailDialog(transport) {

    var analysisId = jQuery("#analysisId").val();

    // Get list of struggling students
    var json = transport.responseText.evalJSON(true);
    var numStudents = json.numStudents;
    var failureThreshold = json.failureThreshold;
    var summaryColumnName = json.summaryColumnName;
    if (numStudents == 0) { 
        openNoStudentsDialog(failureThreshold, summaryColumnName);
        return;
    }
        
    var firstStudentName = json.firstStudentName;
    var firstStudentScore = json.firstStudentScore;
    var firstStudentProb = json.firstStudentProb;
    var possibleMax = json.possibleMax;
    var studentList = json.studentList;

    var dialogDiv = document.getElementById('emailStudentsDialog');
    dialogDiv.innerHTML = "";

    var newForm = document.createElement('form');
    newForm.id   = "emailStudentsForm";
    newForm.name = "emailStudentsForm";
    newForm.action = "DataLab";
    newForm.method = "post";
    newForm.enctype  = "multipart/form-data";
    newForm.encoding = "multipart/form-data";  // IE7
    newForm.onsubmit = "";

    newInput = document.createElement('p');
    newInput.innerHTML = "Here is some suggested language for starting an email to your "
        + "at-risk students -- feel free to edit.";
    newForm.appendChild(newInput);

    newInput = document.createElement('div');
    newInput.id = "emailContentDiv";
    newInput.innerHTML = '<textarea rows="8" cols="68">'
        + 'Dear <' + firstStudentName + '>,\n'
        + 'A gradebook analysis application I am using indicates that your latest grades '
        + 'might be indicative of future problems in this course. Given where you stand '
        + 'now (<' + firstStudentScore + '> out of <' + possibleMax
        + '>) the tool estimates that you have a <' + firstStudentProb + '>% chance '
        + 'of failure (receiving a final score below <' + failureThreshold + '>%). '
        + "Let's talk at your earliest convenience.\n"
        + '</textarea>';
    newForm.appendChild(newInput);

    var studentIdList = [];
    var studentInfoList = [];
    for (var key in studentList) {
        if (studentList.hasOwnProperty(key)) {
            studentInfoList.push(studentList[key]);
            studentIdList.push(key);
        }
    }

    var tableEle = document.createElement('table');
    tableEle.id = "studentTableHeader";
    tableEle.innerHTML = "<colgroup>"
        + "<col style='width=50%'/><col style='width=20%'/>"
        + "<col style='width=15%'/><col style='width=15%'/>"
        + "</colgroup><thead>"
        + "<tr><th>Name</th>"
        + "<th>" + summaryColumnName + "</th>"
        + "<th>% of Possible</th>"
        + "<th>Probability of Failure (%)</th></tr>"
        + "</thead>";
    newForm.appendChild(tableEle);

    var tableDiv = document.createElement('div');
    tableDiv.id = "studentTableDiv";

    var tableHtmlText = "<table id='studentTable'><colgroup>"
        + "<col style='width=50%'/><col style='width=20%'/>"
        + "<col style='width=15%'/><col style='width=15%'/>"
        + "</colgroup><tbody>";

    for (var i = 0; i < studentInfoList.length; i++) {
        var TR = "<tr>";
        if (i % 2 == 0) { TR = "<tr class='even'>"; }

        var studentInfo = [];
        studentInfo = studentInfoList[i].split(',');

        tableHtmlText += TR;
        tableHtmlText += "<td>" + studentInfo[0] + "</td>"
            + "<td>" + studentInfo[1] + "</td>"
            + "<td>" + studentInfo[2] + "</td>"
            + "<td>" + studentInfo[3] + "</td>";
        tableHtmlText += "</tr>";
    }
    tableHtmlText += "</tbody></table>";

    tableDiv.innerHTML = tableHtmlText;

    newForm.appendChild(tableDiv);

    // Attach the form to the div
    dialogDiv.appendChild(newForm);

    var cancelButtonStr = "Cancel";

    // If user is looking at one of the sample gradebooks, disable the
    // option to Send Email... and change Cancel button to say 'Close'.
    var maxSampleAnalysisId = jQuery("#maxSampleAnalysisId").val();
    if (analysisId <= maxSampleAnalysisId) {
        cancelButtonStr = "Close";
    }

    jQuery('#emailStudentsDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 565,
        height : 565,
        title : "Email your at-risk students",
        buttons : [ {
            id : "email-students-button",
            text : "Send Email",
            click : sendEmail
        }, {
            id : "cancel-email-button",
            text : cancelButtonStr,
            click : closeEmailDialog
        } ]
    });

    // If a sample gradebook, disable the option to Send Email...
    if (analysisId <= maxSampleAnalysisId) {
        jQuery('#email-students-button').button('disable');
        jQuery('#email-students-button').attr('title', 'Send Email functionality disabled for sample gradebooks.');
    }

    jQuery('#emailStudentsDialog').dialog('open');
}

function sendEmail() {
    //    jQuery('#emailStudentsForm').submit();
    jQuery('#emailStudentsDialog').dialog('close');
    successPopup("Email sent");
}

function closeEmailDialog() {
    jQuery(this).dialog("close");
}

function openNoStudentsDialog(failureThreshold, summaryColumnName) {

    var dialogDiv = document.getElementById('emailStudentsDialog');
    dialogDiv.innerHTML = "";

    var newDiv = document.createElement('div');
    newDiv.id   = "noStudentsDiv";
    newDiv.name = "noStudentsDiv";

    newInput = document.createElement('p');
    newInput.innerHTML = "Assuming a summary column of '" + summaryColumnName
        + "' and a failure threshold of " + failureThreshold
        + ", none of your students were found to be a risk.";
    newDiv.appendChild(newInput);

    // Attach the form to the div
    dialogDiv.appendChild(newDiv);

    jQuery('#emailStudentsDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 400,
        height : 200,
        title : "Email your at-risk students",
        buttons : [ {
            id : "no-students-button",
            text : "OK",
            click : closeEmailDialog
        } ]
    });

    jQuery('#emailStudentsDialog').dialog('open');
}

function editAnalysisName(analysisId) {
    jQuery('#name_span_' + analysisId).hide();
    jQuery('#nameTextAreaDiv_' + analysisId).show();
    jQuery('#nameSaveDiv_' + analysisId).show();
}

function saveNameChange(analysisId) {
    var newName = jQuery('#nameInput_' + analysisId).val();
    jQuery(
           '<form id="editAnalysisNameForm" method="post" '
           + 'action="DataLab">'
           + '<input name="analysisId" type="hidden" value="' + analysisId + '"/>'
           + '<input name="editAnalysisName" type="hidden" value="true"/> '
           + '<input name="newName" type="hidden" value="' + newName + '"/>'
           + '</form>').appendTo('body').submit();
}

function cancelNameChange(analysisId) {
    jQuery('#nameTextAreaDiv_' + analysisId).hide();
    jQuery('#nameSaveDiv_' + analysisId).hide();
    jQuery('#name_span_' + analysisId).show();
}

function analysisDeleteAreYouSure(analysisId) {
    jQuery('#deleteAnalysisDiv_' + analysisId).hide();
    jQuery('#analysisSureDiv_' + analysisId).show();
}

function deleteAnalysis(analysisId) {
    jQuery(
           '<form id="deleteAnalysisForm" method="post" '
           + 'action="DataLab?analysisId=' + analysisId
           + '"><input name="deleteAnalysis" type="hidden" value="true"'
           + '/></form>').appendTo('body').submit();
}

function cancelDeleteAnalysis(analysisId) {
    jQuery('#analysisSureDiv_' + analysisId).hide();
    jQuery('#deleteAnalysisDiv_' + analysisId).show();
}

function sortHome(sortByColumn) {
    jQuery(
           '<form id="homeSortByForm" method="post" '
           + 'action="DataLab"><input name="homeSortBy" type="hidden" value="' + sortByColumn
           + '"/></form>').appendTo('body').submit();
}

function viewFileRequirements() {

    // In case they've opened this from an error popup, close the popup first.
    var messagePopupDiv = document.getElementById("messagePopup");
    if (messagePopupDiv) {
        messagePopupDiv.style.display = "none";
        document.body.removeChild(messagePopupDiv);
    }

    var dialogDiv = document.getElementById('fileRequirementsDialog');
    dialogDiv.innerHTML = "<p>Your gradebook file:</p>"
        + "<ul id='file-requirements-list'>"
        + "<li>is in a tab-delimited format</li>"
        + "<li>has the student name or id in the first column</li>"
        + "<li>has the assessment names in the first row</li>"
        + "<li>has only numeric values in the rest of the file</li>"
        + "<li>optionally has summary grade values in the final column</li>"
        + "</ul>";

    jQuery('#fileRequirementsDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 440,
        height : 200,
        title : "File Requirements"
    });

    jQuery('#fileRequirementsDialog').dialog('open');
}

// Copied from DataShop.js
function errorPopup(message) {
    messagePopup(message, "ERROR");
}

function warningPopup(message) {
    messagePopup(message, "WARNING");
}

function successPopup(message) {
    messagePopup(message, "SUCCESS");
}

/** Pops up an error message in the middle that turns off after 5 seconds */
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
    if (message.length > 100) {
        messagePopupDiv.className = "popupMenu wide";
    }

    contentPara.appendChild(document.createTextNode(message));

    // If error or warning, include info on acceptable file format.
    if ((messageType == "ERROR") || (messageType == "WARNING")) {
        contentPara.appendChild(document.createElement('br'));

        var infoLink = document.createElement('a');
        infoLink.id = "view-file-requirements";
        infoLink.href = "javascript:viewFileRequirements()";
        infoLink.textContent = "View file requirements";
        contentPara.appendChild(infoLink);
    }

    messagePopupDiv.appendChild(contentPara);

    document.body.appendChild(messagePopupDiv);
    if (messagePopupDiv.timeout) {
        clearTimeout(messagePopupDiv.timeout);
    }
    messagePopupDiv.timeout = setTimeout(closeErrorPopup.bindAsEventListener(messagePopupDiv), 5000);
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
