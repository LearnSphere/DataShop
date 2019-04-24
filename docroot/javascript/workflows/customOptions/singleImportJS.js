/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 *
 * Options pane interface for import data component.
 *
 * Peter Schaldenbrad pschalde@cs.cmu.edu
 */

var datasetLink;
var datasetName;
var fileId;
var isAdminUser;


/**
 * Use the saved state of the component options to propagate what was previously in the options pane
 */
function propagateLoadedData(state) {
    datasetLink = parent.componentSpecificOptionsJson.datasetLink;
    datasetName = parent.componentSpecificOptionsJson.datasetName;
    fileId = state.fileId;
    jQuery('#filePath').val(state.fileId);

    setCurrentFile(state.fileName);

    setCurrentFileType(state.importFileType);

    if (!parent.isView) {
        // This is your workflow, propagate the rest of the data
        setSelectedFileType(state.fileTypeSelection);

        setImportLocation(state.uploadLocation);

        setSearchDataset(state.searchDatasetsString);

        setFileNameTitle(state.importFileNameTitle);

        setDatasetListSelection(state.datasetListSelection);

        if (state.userAgreedToIRB != undefined) {
            setUserAgreedToIRB(state.userAgreedToIRB);
        } else {
            setUserAgreedToIRB(false);
        }

    } else {
        // You are only viewing the workflow.  Only display the file name and type
        // Eliminate the other html
        componentIsView();
    }
}

/**
 * The user is only viewing the workflow.  Remove unecessary information from the interface.
 */
function componentIsView() {
    jQuery('#chooseFileBoxId').html('');
    jQuery('#chooseTypeBoxId').html('');
    jQuery('.wfRadioButtonGroup ').remove();
    jQuery('.tabBody').css('padding', '2em');
    jQuery('.tabBody td').css('padding', '0px');
    jQuery('.tabLeftColumn').remove();
}

/**
 * Save the state of the options interface
 */
function saveComponent() {
    parent.dirtyOptionPane = true;
    let state = getImportComponentState();
    parent.saveData('Import', state);
}

///////////////////////////////f////
////Initialize JS Functionality////
///////////////////////////////////

/**
 * Set some JavaScript events for the interface
 */
function initializeFunctionality() {
    //Determine when to save
    jQuery('input[name=importFileType], .dataFileFilter').change(function() {
        saveComponent();
    });

    isAdminUser = parent.getAdminUserFlag();

    fileLocationFunctionality();

    jQuery('#importFile').click(ensureUploadIsAllowed);
    jQuery('#importFile').change(choseNewFile);
    tryToEnableUploadFileButton();

    jQuery('input[id=irbRequirementsMet]').change(function() {
        if (irbRequirementIsMet()) {
            jQuery('#irbRequirementDiv').hide();
            jQuery('#dataTypeDiv, #uploadFileButton').show();
            updateHelperText();
        }
        tryToEnableUploadFileButton();
    });

    jQuery('input[name=importFileType]').change(function() {
        styleSelectedDataType();
        if (getFileTypeSelection() != jQuery('#fileType').val() &&
            jQuery('#fileType').val() != undefined &&
            jQuery('#fileType').val() != "") {
            // Different file type selected, ask if they want to reupload the file with a new type
            reUploadDialog();
        }
    });
}

function getFileSizeLimit(isAdminUser) {

    return isAdminUser ? (1000 * 1024 * 1024) : (400 * 1024 * 1024);
}

function getFileSizeLimitStr(isAdminUser) {

    return isAdminUser ? "1GB" : "400MB";
}

/**
 * Set the even functionality of the location options for data import (datashop or local)
 */
function fileLocationFunctionality() {
    jQuery('input[name=importType]').change(function() {
        changedFileLocation(false);
    });
    changedFileLocation(true);

    jQuery('input[name=importFileType]').change(function() {
        tryToEnableUploadFileButton();
        displayDatasets();
    });
}

/**
 * The location of the file import has changed (datashop or local), display new options
 * accordingly.
 */
function changedFileLocation(isInitializing) {
    let checkedVal = jQuery('input[name=importType]:checked').val();

    // undo the attaching of an upload table if that is up
    hideAttaching();
    let fileName = jQuery('#currentFileName').text();
    if (datasetLink != null && datasetLink != undefined &&
        datasetName != null && datasetName != undefined) {
        updateAttachBlock(datasetLink, datasetName, parent.getComponentId());
    } else if (fileName != null && fileName != undefined && fileName != "") {
        updateAttachBlock('', '', parent.getComponentId());
    }

    if (checkedVal === 'importFile') {
        jQuery('#datashopUploadTable').css('display', 'none');
        jQuery('#irbRequirementDiv').css('display', 'inherit');

        if (isInitializing) {
            // Make sure the attachable datasets are there.
            displayDatasets();
        }

        if (irbRequirementIsMet()) {
            jQuery('#irbRequirementDiv').hide();
            jQuery('#dataTypeDiv, #uploadFileButton').show();
        } else {
            // Show the IRB requirement since user has not checked it yet
            jQuery('#irbRequirementDiv').show();
            jQuery('#uploadFileButton, #dataTypeDiv').css('display', 'none');
        }

        updateHelperText();

    } else if (checkedVal === 'importDataset') {
        jQuery('#uploadFileButton, #dataTypeDiv').css('display', 'none');
        jQuery('#irbRequirementDiv').css('display', 'none');
        jQuery('#datashopUploadTable').css('display', 'inline-block');

        if (!isInitializing) {
            // Set the data type to File since this is default
            jQuery('input[name=importFileType]').prop('checked', false);
            jQuery('#fileRadio').prop('checked', true);
            styleSelectedDataType();
        }
        updateHelperText();
        displayDatasets();
    }

    handleFileLocationTab(checkedVal);
}

/**
 * A user has changed the location of the import file.  Handle the css
 * changes to the tab-like interface of locations
 */
function handleFileLocationTab(clickedItem) {
    if (clickedItem === 'importFile') {
        jQuery('label[for="fileUploadBtn"]').addClass('locationSelected');
        jQuery('label[for="datasetFilesBtn"]').removeClass('locationSelected');

        jQuery('label[for="fileUploadBtn"] > span').addClass('locationSelected');
        jQuery('label[for="datasetFilesBtn"] > span').removeClass('locationSelected');
    } else if (clickedItem === 'importDataset') {
        jQuery('label[for="datasetFilesBtn"]').addClass('locationSelected');
        jQuery('label[for="fileUploadBtn"]').removeClass('locationSelected');

        jQuery('label[for="datasetFilesBtn"] > span').addClass('locationSelected');
        jQuery('label[for="fileUploadBtn"] > span').removeClass('locationSelected');
    }
}

/**
 * runs a check to ensure that both the file type has been selected
 * and the irb agreement is check.  Then will enable or disable
 * the upload new file button
 */
function tryToEnableUploadFileButton() {

    if (jQuery('input[name=importFileType]:checked').val() != undefined) {
        if (irbRequirementIsMet()) {
            // Both requirements met
            enableUploadNewFileButton();
        } else {
            disableUploadNewFileButton();
        }
        jQuery('#selectDataTypeReminder').css('display', 'none');
    } else {
        disableUploadNewFileButton();
    }
}

function disableUploadNewFileButton() {
    jQuery('#uploadFileButton').addClass('disabledFileUploadButton');
    jQuery('#uploadFileButton').off('click');
    if (jQuery('input[name=importFileType]:checked').val() == undefined) {
        jQuery('#selectDataTypeReminder').css('display', 'inherit');
    }
}

function enableUploadNewFileButton() {
    jQuery('#uploadFileButton').removeClass('disabledFileUploadButton');
    jQuery('#selectDataTypeReminder').css('display', 'none');
}

function styleSelectedDataType() {
    jQuery('#importDataTypeForm div label').removeClass('selectedDataType');
    jQuery('#importDataTypeForm input[type="radio"]:checked+label').addClass('selectedDataType');
    updateHelperText();
}

function fileAlreadyUploaded() {
    if (jQuery('#currentFileName').text() == undefined ||
        jQuery('#currentFileName').text() == "") {
        return false;
    }
    return true;
}

function dataTypeIsSelected() {
    if (getFileTypeSelection() == undefined) {
        return false;
    }
    return true;
}

function updateHelperText() {
    let message = '';
    if (!fileAlreadyUploaded()) {
        // hide the "Current file/file type" stuff
        jQuery('.importedFileText').hide();

        if (jQuery('input[name=importType]:checked').val() == "importDataset") {
            // datashop list and no file already selected
            message = "Please select a file.";
        } else if (jQuery('input[name=importType]:checked').val() == "importFile") {
            // Upload local file and no file already uploaded

            if (!irbRequirementIsMet()) {
                message = "Please read our Terms of Use.";
            } else if (!dataTypeIsSelected()) {
                message = "Please select a data type.";
            } else {
                // IRB and Data Type selected but no file already uploaded
                if (getFileTypeSelection() == 'file') {   // 'General File'
                    message = "Please upload a file.";
                } else {
                    message = "Please upload a " + getFileTypeSelection() + " file.";
                }
            }
        }
    } else {
        // Show current file/type stuff
        jQuery('.importedFileText').show();
    }
    jQuery('#whatToDoDiv').text(message);
}

////////////////////
////Get Functions///
////////////////////
function getSavedData() {
    let loadedData = null;
    try {
        loadedData = JSON.parse(parent.loadData("Import"));
    } catch (err) {
        //console.log(err);
    }
    return loadedData
}

/**
 * Get a JSON string of the current state of the option's pane interface
 */
function getImportComponentState() {
    let state = {};

    let importFileName = jQuery('#currentFileName').text();
    state.fileName = importFileName;

    let importFileType = jQuery('#fileType').val();
    state.importFileType = importFileType;

    let importFileNameTitle = jQuery('#currentFileName').attr('title');
    state.importFileNameTitle = importFileNameTitle;

    let fileTypeSelection = getFileTypeSelection();
    state.fileTypeSelection = fileTypeSelection;

    let datasetListSelection = jQuery('.existingDataFile').val();
    if (!(importFileName != null && datasetListSelection == null)) {
        state.datasetListSelection = datasetListSelection;
    } else if (importFileName != null && datasetListSelection == null) {
        state.datasetListSelection = undefined;
    } else if (getSavedData() != null) {
        state.datasetListSelection = getSavedData().datasetListSelection;
    } else {
        state.datasetListSelection = undefined;
    }

    let uploadLocation = jQuery('input[name=importType]:checked').val();
    state.uploadLocation = uploadLocation;

    let searchDatasetsString = jQuery('.dataFileFilter').val();
    state.searchDatasetsString = searchDatasetsString;

    // Dataset info
    state.datasetLink = datasetLink;
    state.datasetName = datasetName;
    state.fileId = jQuery('#filePath').val();

    // User agreed to IRB info
    state.userAgreedToIRB = irbRequirementIsMet() ? "true" : "false";

    return JSON.stringify(state);
}

/**
 * Get the selection of the file type pane
 */
function getFileTypeSelection() {
    return jQuery('input[name=importFileType]:checked').val();
}

/**
 * Return true if the IRB promise is checked... or if they've already loaded
 * a file with this component, which means they already agreed to it earlier.
 */
function irbRequirementIsMet() {
    if (jQuery('input[id=irbRequirementsMet]').attr('checked') == 'checked') {
        return true;
    }
    return false;
}

////////////////////
////Set functions///
////////////////////

/*
    Set the type of the imported data
*/
function setSelectedFileType(type) {
    jQuery('input[name=importFileType]').prop('checked', false);
    jQuery('input[name=importFileType][value=' + type + ']').prop('checked', true);
    styleSelectedDataType();
    updateHelperText();
}

/*
    Set the location of the imported data (datashop, or local file)
*/
function setImportLocation(location) {
    jQuery('input[name=importType]').prop('checked', false);
    jQuery('input[name=importType][value=' + location + ']').prop('checked', true);

    if (location === "importDataset") {
        jQuery('label[for="datasetFilesBtn"]').click();
    } else if (location === "importFile") {
        jQuery('label[for="fileUploadBtn"]').click();
    }
}

function setSearchDataset(searchString) {
    // This is done automatically once the dataset list is populated
}

/**
 * Set the current file for import.  Also, handle updating the attach file to dataset button
 */
function setCurrentFile(fileName) {
    jQuery('#currentFileName').text(fileName);

    if (datasetLink != null && datasetLink != undefined &&
        datasetName != null && datasetName != undefined) {
        updateAttachBlock(datasetLink, datasetName, parent.getComponentId());
    } else if (fileName != null && fileName != undefined) {
        updateAttachBlock('', '', parent.getComponentId());
    }

    updateHelperText();
}

/**
 * Set the current file for import.  Also, handle updating the attach file to dataset button
 */
function setCurrentFileType(fileType) {
    jQuery('#fileType').val(fileType);
    updateHelperText();
}

function setFileNameTitle(title) {
    jQuery('#currentFileName').attr('title', title);
}

function setDatasetListSelection(datasetListSelection) {
    // the dataset list has not been populated yet since it is asynchronous.
    // this wil be set after the list is populated
}

function setUserAgreedToIRB(flag) {
    if (flag == "true") {
        jQuery('input[id=irbRequirementsMet]').prop('checked', true);
    }
}

/////////////////////////////
////DataSet List FUnctions///
/////////////////////////////

/**
 * Populate the table of datasets available for user to import
 */
function displayDatasets() {
    // If the datasets have already been received from the server, just make sure they are filtered
    if (jQuery('.datasetList').html() == '' || jQuery('.datasetList').html() == undefined) {
        // The datasets do not exist.  Get them from the server to display
        // Set it to waiting
        waitingForDatasets();

        requestDatasetsToDisplay(componentId, "import");
    } else {
        // The datasets have already been loaded from the server, just make sure they are filtered
        // correctly
        jQuery(textboxToFilter).keyup();
    }
}

/**
 * Make a call to the server to get a user's available datasets based on the file type selection
 */
function requestDatasetsToDisplay(componentId, componentType) {
    var dsId = null;
    lastCall = "";

    let importDataType = jQuery('input[name=importFileType]:checked').val();

    if (componentId !== undefined && componentId != null && componentId != '') {

        new parent.Ajax.Request('WorkflowEditor', {
            parameters: {
                requestingMethod: 'WorkflowEditorServlet.requestComponentSpecificOptions',
                workflowId: parent.currentDigraph.id,
                digraphObject: parent.stringifyDigraph(parent.currentDigraph),
                dirtyBits: JSON.stringify(parent.dirtyBits),
                componentId: componentId.replace("Data", "Import"),
                componentType: "import",
                componentName: 'file',
                datasetId: dsId
            },
            //beforeSend : showProcessingByType(componentType),
            onComplete: requestDatasetsToDisplayCompleted,
            onException: function(request, exception) {
                parent.wfHideStatusIndicator();
                throw (exception);
            }
        });
    }
}

/**
 * Populate the table of available datasets for the user with the data from the servlet
 */
function requestDatasetsToDisplayCompleted(transport) {
    if (transport !== undefined) {
        json = transport.responseJSON;

        var disabledStr = "";
        if (parent.currentDigraph.isView || parent.workflowIsRunning) {
            disabledStr = "disabled";
        }

        innerHtml = '';
        innerHtmlAttachable = '';
        /////////////////////////////////////////
        // Existing dataset file selector
        projectArray = null;
        if (json.projectArray !== undefined && json.projectArray != null) {
            if (!jQuery.isArray(json.projectArray)) {
                json.projectArray = [json.projectArray];
            }
            projectArray = json.projectArray;
        }

        datasetArray = null;
        if (json.datasetArray !== undefined && json.datasetArray != null) {
            if (!jQuery.isArray(json.datasetArray)) {
                json.datasetArray = [json.datasetArray];
            }
            datasetArray = json.datasetArray;
        }

        fileArray = null;
        if (json.fileArray !== undefined && json.fileArray != null) {
            if (!jQuery.isArray(json.fileArray)) {
                json.fileArray = [json.fileArray];
            }
            fileArray = json.fileArray;
        }

        if (json.fileId !== undefined) {
            fileId = json.fileId;
        }

        if (projectArray !== undefined && projectArray != null && projectArray.length > 0 &&
            datasetArray !== undefined && datasetArray != null && datasetArray.length > 0 &&
            fileArray !== undefined && fileArray != null && fileArray.length > 0 &&
            fileId != null) {

            var gfoResult =
                parent.generateFileOptions(projectArray, datasetArray, fileArray, fileId);
            var fileOptions = gfoResult.fileOptions;
            var hiddenInputs = gfoResult.hiddenInputs;

            innerHtml = innerHtml +
                '<tr class="datasetFilesDiv"><td colspan="2" >' +
                '<select size="11" ' + disabledStr + ' id="wfFileId_' + componentId +
                '" class="existingDataFile" name="wfFileId" >' +
                fileOptions + '</select>' +
                '</td></tr>';
            innerHtml = innerHtml + hiddenInputs;
        }

        //////////////////////////////////////////
        // Dataset File Attachment selector
        myProjectArray = null;
        if (json.myProjectArray !== undefined && json.myProjectArray != null) {
            if (!jQuery.isArray(json.myProjectArray)) {
                json.myProjectArray = [json.myProjectArray];
            }
            myProjectArray = json.myProjectArray;
        }

        myDatasetArray = null;
        if (json.myDatasetArray !== undefined && json.myDatasetArray != null) {
            if (!jQuery.isArray(json.myDatasetArray)) {
                json.myDatasetArray = [json.myDatasetArray];
            }
            myDatasetArray = json.myDatasetArray;
        }

        // If you just made a dataset and tried to attacha file, attach the file now
        if (newDatasetNameToAddTo != null &&
            (newProjectIdToAddTo != null || newProjectNameToAddTo != null)) {
            attachFileToNewDataset(myDatasetArray);
        }

        if (parent.currentDigraph.isView != true) {
            if (myProjectArray !== undefined && myProjectArray != null && myProjectArray.length > 0 &&
                myDatasetArray !== undefined && myDatasetArray != null && myDatasetArray.length > 0) {

                var attachDatasetOptions =
                    parent.generateAttachableDatasetSelect(myProjectArray, myDatasetArray);

                innerHtmlAttachable = innerHtmlAttachable +
                    '<tr class="wfFileUploadRow"><td colspan="2" >' +
                    '<select size="11" ' + disabledStr + ' id="wfDatasetId_' + componentId + '" class="existingDataset" name="wfDatasetId" >' +
                    attachDatasetOptions +
                    '</select>' +
                    '</td></tr>';
            } else {

                innerHtmlAttachable = innerHtmlAttachable + '<tr class="wfFileUploadRow"><td></td><td>' +
                    'You do not have <a href="../../../help?page=requesting-access" target="_blank">edit access</a> to any Projects.</td>';
            }

            // Generate the list of Projects the user has access to for creating new datasets
            myProjectArray.forEach(function(projectObj) {
                let option = jQuery('<option />')
                    .val(projectObj.id)
                    .text(projectObj.projectName);
                jQuery('#existing_project_select').append(option);
            });

        }

        jQuery('.datasetList').html(innerHtml);
        jQuery('.datasetListAttachable').html(innerHtmlAttachable);


        // show hidden files if the type is not a datashop type
        if (isShowAllPotentialMatches()) {
            jQuery('.dataHiddenFileOption').removeAttr('hidden');
        } else if (!isShowAllPotentialMatches()) {
            jQuery('.dataHiddenFileOption').attr('hidden', 'true');
        }

        jQuery(".existingDataFile").filterFilesByText(jQuery('.dataFileFilter'));
        jQuery(".existingDataset").filterDatasetsByText(jQuery('.datasetFilter'));

        let loadedData = getSavedData();
        if (loadedData != null && loadedData != undefined) {
            jQuery('.existingDataFile').val(loadedData.datasetListSelection);
            if (loadedData.searchDatasetsString != null && loadedData.searchDatasetsString !== "") {
                jQuery('.dataFileFilter').val(loadedData.searchDatasetsString);
                jQuery('.dataFileFilter').keyup();
            }
        }

        jQuery('.existingDataFile').change(existingDataFile);
        jQuery('.existingDataset').on('change');
        jQuery('.existingDataset').change(attachUploadToDataset);


        hideAttaching();
        updateHelperText();

        notWaitingForDatasets();
    }
}

function isShowAllPotentialMatches() {
    let dataTypesForNotShowAll = ['student-step', 'transaction', 'student-problem'];
    if (dataTypesForNotShowAll.includes(getFileTypeSelection())) {
        return false;
    } else {
        return true;
    }
}

var textboxToFilter = null;
/**
 * Filter the datasets available in the table by the regEx in the search box
 */
jQuery.fn.filterFilesByText = function(textbox) {
    this.each(function() {
        var options = [];
        var select = this;

        // I'm not sure this bit is needed now that the regex isn't done on the DOM...
        jQuery(select).children().each(function() {
            if (this.tagName !== undefined) {
                if (this.tagName.toLowerCase() == "option") {
                    options.push({ value: jQuery(this).val(), text: jQuery(this).text() });
                } else if (this.tagName.toLowerCase() == "optgroup") {

                    var thisLabel = jQuery(this).attr("label");
                    options.push({ label: thisLabel, text: jQuery(this).text() });

                    jQuery(this).children().each(function() {
                        if (this.tagName.toLowerCase() == "option") {
                            options.push({ optgroup: thisLabel, value: jQuery(this).val(), text: jQuery(this).text() });
                        }
                    });

                }
            }
        });

        prevDatasetFiles = options;
        jQuery(select).data('options', options);

        jQuery(textbox).bind('keyup', function() {

            var optInner = jQuery(select).data('options');

            if (jQuery(textbox).val().trim() == "") {
                jQuery(select).empty();
                options = prevDatasetFiles;
            } else {
                jQuery(select).empty();
            }

            var search = jQuery.trim(jQuery(this).val());
            var regex = new RegExp(search, "gi");

            let fileTypeFilter = dataTypeFilters[getFileTypeSelection()];
            if (fileTypeFilter == null || fileTypeFilter == undefined) {
                fileTypeFilter = new RegExp(".*", "gi");
            }

            // Look for matches on filenames, dataset names and project names
            var matchingFiles = [];
            var fileCount = 0;
            jQuery.each(fileArray, function(i) {
                var fileName = fileArray[i].fileName;
                var datasetName = fileArray[i].datasetName;
                var projectName = fileArray[i].projectName;
                var actualFileName = fileArray[i].actualFileName;

                let matchesSearch = false;
                if ((fileName.match(regex) !== null) ||
                    (datasetName.match(regex) !== null) ||
                    (projectName.match(regex) !== null)) {
                    matchesSearch = true;
                }

                let matchesDataTypeFilter = false;
                if (actualFileName.match(fileTypeFilter) !== null) {
                    matchesDataTypeFilter = true;
                }

                if (matchesSearch && matchesDataTypeFilter) {
                    matchingFiles[fileCount] = fileArray[i];
                    fileCount++;
                }
            });
            var gfoResult = parent.generateFileOptions(projectArray, datasetArray, matchingFiles, null);
            var fileOptions = gfoResult.fileOptions;
            jQuery(select).append(jQuery(fileOptions));

            // show hidden files if the type is not a datashop type
            if (isShowAllPotentialMatches()) {
                jQuery('.dataHiddenFileOption').removeAttr('hidden');
            } else if (!isShowAllPotentialMatches()) {
                jQuery('.dataHiddenFileOption').attr('hidden', 'true');
            }

            // Try to select the previously selected file
            let loadedData = getSavedData();
            if (loadedData != null && loadedData != undefined) {
                jQuery('.existingDataFile').val(loadedData.datasetListSelection);
            }
        });
        textboxToFilter = textbox;
        // Trigger the filter so it filters out by the selected data type
        jQuery(textbox).keyup();

    });
};

jQuery.fn.filterDatasetsByText = function(textbox) {
    this.each(function() {
        var options = [];
        var select = this;


        jQuery(select).children().each(function() {
            if (this.tagName !== undefined) {
                if (this.tagName.toLowerCase() == "option") {
                    options.push({ value: jQuery(this).val(), text: jQuery(this).text() });
                } else if (this.tagName.toLowerCase() == "optgroup") {

                    var thisLabel = jQuery(this).attr("label");
                    options.push({ label: thisLabel, text: jQuery(this).text() });

                    jQuery(this).children().each(function() {
                        if (this.tagName.toLowerCase() == "option") {
                            options.push({ optgroup: thisLabel, value: jQuery(this).val(), text: jQuery(this).text() });
                        }
                    });

                }
            }
        });

        prevDatasets = options;
        jQuery(select).data('options', options);

        jQuery(textbox).bind('keyup', function() {

            var optInner = jQuery(select).data('options');

            if (jQuery(textbox).val().trim() == "") {
                jQuery(select).empty();
                options = prevDatasets;
            } else {
                jQuery(select).empty();
            }

            var search = jQuery.trim(jQuery(this).val());
            var regex = new RegExp(search, "gi");

            // Look for matches on dataset names and project names
            var matchingDatasets = [];
            var datasetCount = 0;
            jQuery.each(myDatasetArray, function(i) {
                var datasetName = myDatasetArray[i].datasetName;
                var projectName = myDatasetArray[i].projectName;
                if (datasetName.match(regex) !== null ||
                    (projectName.match(regex) !== null)) {
                    matchingDatasets[datasetCount] = myDatasetArray[i];
                    datasetCount++;
                }
            });
            var datasetOptions = parent.generateAttachableDatasetSelect(myProjectArray, matchingDatasets);
            jQuery(select).append(jQuery(datasetOptions));

        });
    });
};


/**
 * User has selected a new dataset from the dropdown of available files.  Handle this
 */
function existingDataFile() {
    var splitString = this.id.split('_');
    var dsId = null;

    if (splitString.length == 2) {
        var componentId = splitString[1];

        var fileLabel = jQuery('option:selected', this).attr('wfFileType');

        var fileForm = '#importForm_' + componentId;
        var fileIndex = 0;
        var dsFileId = jQuery('option:selected', this).val();
        var fileDatasetId = jQuery('#fileDatasetId_' + dsFileId).val();

        if (this.name == 'wfFileId' && fileLabel != null &&
            jQuery(this).val() != jQuery('#filePath').val()) {
            previousMiniIcon = "Processing";
            new parent.Ajax.Request('WorkflowEditor', {
                parameters: {
                    requestingMethod: 'WorkflowEditorServlet.existingFile',
                    workflowId: parent.currentDigraph.id,
                    datasetId: dsId,
                    componentId: componentId,
                    wfFileId: jQuery(this).val(),
                    fileDatasetId: fileDatasetId,
                    fileIndex: fileIndex,
                    fileLabel: fileLabel
                },
                onComplete: uploadFileCompleted,
                onSuccess: parent.wfShowStatusIndicatorModal(false, "Processing..."),
                onException: function(request, exception) {
                    parent.wfHideStatusIndicator();
                    throw (exception);
                }
            });

        }
    }
}

/**
 * Used to reupload the current file when the file type changes
 */
function reUploadCurrentFile() {
    var dsId = null;
    var fileLabel = getFileTypeSelection();

    var fileForm = '#importForm_' + componentId;
    var fileIndex = 0;
    var fileDatasetId = null;

    if (fileLabel != null) {
        previousMiniIcon = "Processing";
        new parent.Ajax.Request('WorkflowEditor', {
            parameters: {
                requestingMethod: 'WorkflowEditorServlet.existingFile',
                workflowId: parent.currentDigraph.id,
                datasetId: dsId,
                componentId: componentId,
                wfFileId: jQuery('#filePath').val(),
                fileDatasetId: fileDatasetId,
                fileIndex: fileIndex,
                fileLabel: fileLabel
            },
            onComplete: uploadFileCompleted,
            onSuccess: parent.wfShowStatusIndicatorModal(false, "Processing..."),
            onException: function(request, exception) {
                parent.wfHideStatusIndicator();
                throw (exception);
            }
        });

    }
}

function ensureUploadIsAllowed(event) {
    if (getFileTypeSelection() == undefined ||
            getFileTypeSelection() == "" ||
            !irbRequirementIsMet()) {
        // Not allowed to upload until the file type is selected and irb agreed to
        errorDialog('Data Type Not Selected', 'Please click on the apprpriate data type.');
        event.preventDefault();
    }
}
var fileToUpload;

function choseNewFile() {
    fileToUpload = this;

    if (fileToUpload != undefined) {
        uploadLocalFile();
    }
}

/**
 * Upload a file from the user's machine
 */
function uploadLocalFile() {
    if (parent.workflowIsRunning) {
        parent.showCancelWarning();
    } else {
        if (fileToUpload == undefined || fileToUpload == null) {
            alert('Please choose a file');
            return;
        }
        var dsId = null;

        var fileForm = '#importForm';
        var fileIndex = jQuery('#fileIndex').val();
        var fileLabel = getFileTypeSelection();

        if (fileToUpload.name == 'file') {

            var size = jQuery(fileToUpload)[0].files[0].size;
            if ((size !== undefined) &&
                (size > getFileSizeLimit(isAdminUser))) {
                var msg = "File size exceeds " + getFileSizeLimitStr(isAdminUser) + " allowance.";
                updateFollowingUploadFailure(fileForm, msg);
                return;
            }

            var formData = new FormData();
            formData.append('file', jQuery(fileToUpload)[0].files[0]);
            formData.append('fileIndex', fileIndex);
            formData.append('fileLabel', fileLabel);
            formData.append('requestingMethod', 'WorkflowEditorServlet.fileUpload');
            formData.append('workflowId', parent.currentDigraph.id);
            formData.append('componentId', componentId);
            formData.append('dsId', dsId);

            jQuery.ajax({
                url: '../../../WorkflowEditor', //Server script to process data
                type: 'POST',
                // Form data
                data: formData,
                beforeSend: function() {
                    parent.processComponentStatusInfoLocked = true;
                    let fileSize = this.data.get('file').size;
                    if (fileSize > 10000000) {
                        // Display the progress bar if the file is larger than 10mb
                        parent.wfShowStatusIndicatorModalProgressBar(true, "Uploading...", "Progress:", 0);
                    }
                },
                xhr: function() {
                    var myXhr = $.ajaxSettings.xhr();
                    if (myXhr.upload) {
                        myXhr.upload.addEventListener('progress', fileUploadProgress, false);
                    }
                    return myXhr;
                },
                // Options to tell jQuery not to process data or worry about content-type.
                async: true,
                cache: false,
                contentType: false,
                processData: false
            }).done(uploadFileCompleted).fail(error_uploadFile);
        }
    }
}

/**
 * Handler for updating the progress of uploading a file from the local machine
 * @param e - event from ajax, with the amount of bytes uploaded
 */
function fileUploadProgress(e) {
    if (e.lengthComputable) {
        var max = e.total;
        var current = e.loaded;
        var percent = (current * 100) / max;

        parent.wfUpdateStatusIndicatorModalProgressBar(percent);
    }
}

function reUploadDialog() {
    let dialogHtml = 'Would you like to re-upload the current file with this new file type?';
    let dialogTitle = 'Re-Upload File?';
    jQuery('<div />', {
        id: componentId + "reUploadDialog"
    }).html(dialogHtml).dialog({
        open: function() {
            jQuery('.ui-button').focus();
        },
        autoOpen: true,
        autoResize: true,
        resizable: false,
        width: '30%',
        modal: true,
        title: dialogTitle,
        buttons: {
            'Re-Upload': function() {
                jQuery(this).dialog('close');
                reUploadCurrentFile();
            },
            'No': function() {
                jQuery(this).dialog('close');
            }
        },
        open: function() {
            jQuery(this).height((jQuery(this).height() + DIALOG_TITLE_PADDING_OFFSET) + 'px');
        },
        close: function() {
            jQuery(this).remove();
        }
    });
}
/**
 * Options File upload completed handler. File uploads are restricted to 1 file option per component.
 */
function uploadFileCompleted(data) {
    parent.wfHideStatusIndicator();
    if (!parent.workflowIsRunning) {
        var fileForm = null;

        if (jQuery('input[name=importType]:checked').val() != "importDataset") {
            //De-select whatever was chosen in the "Import DataShop Files" list
            jQuery(".existingDataFile option:selected").prop("selected", false);
        }

        if (data !== undefined && data.responseText !== undefined) {
            json = data.responseJSON;
            data = json;
        }

        if ((data.error_flag !== undefined) && (data.message !== undefined)) {
            fileForm = "";
            updateFollowingUploadFailure(fileForm, data.message);
        }

        if (data.componentId !== undefined && data.componentId != null) {
            var componentId = data.componentId;
            var message = null;
            // The option file upload completed. 1 upload allowed per import prevents unecessary complexity.
            if (data.success == "true") {
                // Display a successful message
                uploadSuccessMessage();

                fileForm = "";
                jQuery(fileForm + ' ' + '#filePath').val(data.fileId);
                setCurrentFile(data.fileName);
                setCurrentFileType(data.fileLabel);
                jQuery(fileForm + ' ' + '#currentFileName').attr('title', data.fileName);
                jQuery(fileForm + ' ' + '#fileIndex').val(data.fileIndex);
                jQuery(fileForm + ' ' + '#fileLabel').val(data.fileLabel);
                // The files object must be an array; recast as array if needed
                if (parent.componentOptions[componentId].files === undefined) {
                    parent.componentOptions[componentId].files = [];
                } else {
                    if (!jQuery.isArray(parent.componentOptions[componentId].files)) {
                        parent.componentOptions[componentId].files = [parent.componentOptions[componentId].files];
                    }
                }

                // The newly uploaded file
                var newFile = {};
                newFile.file = {};
                newFile.file.file_path = data.fileId;
                newFile.file.file_name = data.fileName;
                newFile.file.index = data.fileIndex;
                newFile.file.label = data.fileLabel;

                if (JSON.stringify(data.columnHeaders) != '"[]"') {
                    // This file is a tab-delimited or CSV file with headers.
                    newFile.file.metadata = {};
                    newFile.file.metadata.header = data.columnHeaders;
                }
                // Use a container so that the json <-> xml will be valid and unambiguous.
                var fileContainer = {};
                fileContainer[newFile.file.label] = newFile.file;
                parent.componentOptions[componentId].files = fileContainer;
                //parent.componentOptions[componentId].files = [];
                //parent.componentOptions[componentId].files.push(fileContainer);

                // File upload: set dirty bit
                parent.dirtyBits[componentId] = parent.DIRTY_FILE;

                if (data.datasetLink !== undefined && data.datasetLink != "") {
                    message = parent.SELECTED_DATASET_FILE_MESSAGE;
                } else {
                    message = parent.UPLOADED_NEW_FILE_MESSAGE;
                }

            } else {
                fileForm = "";

                // Error uploading file. Reset values.
                updateFollowingUploadFailure(fileForm, data.message);
                jQuery('#fileSuccessMessage').html('Failed to upload file.');
            }

            // We must set isWorkflowSaved before calling updateAttachBlock.
            parent.isWorkflowSaved = false;
            // We must set isWorkflowSaved before calling updateAttachBlock.
            updateAttachBlock(data.datasetLink, data.datasetName, componentId);
            datasetLink = data.datasetLink;
            datasetName = data.datasetName;

            var fileInfoString = "File: " + data.fileName;
            if (data.projectName !== undefined && data.projectName != '') {
                projectName = data.projectName;
                fileInfoString = 'Project: ' + data.projectName + '&#013;&#010;' +
                    'Dataset: ' + data.datasetName + '&#013;&#010;' +
                    'File: ' + data.fileName;
            } else if (data.datasetName !== undefined && data.datasetName != '') {
                fileInfoString = 'Dataset: ' + data.datasetName + '&#013;&#010;' +
                    'File: ' + data.fileName;
            }
            // To unescape chars in the fileName title:
            var unescapedTitle = jQuery('<div/>').html(fileInfoString).text();
            jQuery('#currentFileName').attr('title', unescapedTitle);

            parent.lastCall = "uploadFileCompleted";

            saveComponent();

            // For custom options, set the changed components to changed state
            parent.customOptionsHandleOptionsUpdate(data.componentId, false);

            parent.saveTemporaryWorkflowTransient(parent.currentDigraph.id, message, null, null, null);

        } else if (data.success == "false") {
            wfInfoDialog('fileUploadDialog', data.message, 'Error');
        } else {
            parent.testLoggedOut(data);
        }
    }
}

function updateFollowingUploadFailure(fileForm, message) {
    setCurrentFile('');
    setCurrentFileType('');
    jQuery(fileForm + ' ' + '#currentFileName').attr('title', '');
    jQuery(fileForm + ' ' + '#fileIndex').val('');
    jQuery(fileForm + ' ' + '#fileLabel').val('');

    var dialogHtml = '' + message + '<br/>';

    wfInfoDialog('fileUploadDialog', dialogHtml, 'Error');
}

function uploadSuccessMessage() {
    /*var regex1 = new RegExp(parent.fileUploadPattern,"gi");
    var regex2 = new RegExp(parent.datasetFilePattern,"gi");
    if (message.length > 0 && message.match(regex1) !== null) {
        parent.createWfTimerDialog("finishedDialog", "Upload completed", "Success");
        setTimeout(function() {
            jQuery('#finishedDialog').dialog('close')
        }, 4000);
    }
    if (message.length > 0 && message.match(regex2) !== null) {
        parent.createWfTimerDialog("finishedDialog", "File selected", "Success");
        setTimeout(function() {
            jQuery('#finishedDialog').dialog('close')
        }, 4000);
    }*/
    jQuery('#fileSuccessMessage').html('Success! File Selected.');
}

/**
 * Options File upload error handler.
 */
function error_uploadFile() {
    parent.wfHideStatusIndicator();
    var dialogHtml = 'Your file upload has failed. '
        + datashopHelpInfo;
    wfInfoDialog('uploadFailedDialog', dialogHtml, 'Error');
    parent.monitorComponentStatus();
}

function waitingForDatasets() {
    jQuery('.datasetList').css('display', 'none');
    jQuery('#waitingForDatasetGif').css('display', 'table');
}

function notWaitingForDatasets() {
    jQuery('.datasetList').css('display', 'table');
    jQuery('#waitingForDatasetGif').css('display', 'none');
}

////////////////////////////////////
////Import Data Type  Functions/////
////////////////////////////////////

var importDataTypeFormReady = false;

var dataTypeFilters = {};

function getDataTypesXmlFile() {
    new parent.Ajax.Request('WorkflowEditor', {
        parameters: {
            requestingMethod: 'WorkflowEditorServlet.getImportDataTypeXml',
            workflowId: parent.currentDigraph.id,
            componentId: parent.getComponentId()
        },
        onComplete: getDataTypesXmlFileCompleted,
        beforeSend: parent.wfShowStatusIndicatorModal(false, "Processing..."),
        onSuccess: parent.wfHideStatusIndicator,
        onException: function(request, exception) {
            parent.wfHideStatusIndicator();
            throw (exception);
        }
    });
}

function getDataTypesXmlFileCompleted(data) {
    let json = data.responseJSON;
    if (json == null) {
        // Could not get the type xml.  Probably because the wf isView
        importDataTypeFormReady = true;
        return;
    }

    let xml = json.dataTypeXml;

    let parser = new DOMParser();
    let xmlDoc = parser.parseFromString(xml, "text/xml");

    let data_types = xmlDoc.getElementsByTagName("data_types");

    let datatypes = data_types[0].childNodes;

    let html = "";

    for (let i = 0; i < datatypes.length; i++) {
        if (datatypes[i].nodeName === "data_type") {
            html += dataTypeXmlToHtml(datatypes[i], 0);
        }
    }

    jQuery(document).ready(function() {
        jQuery("#importDataTypeForm").html(html);
    });

    importDataTypeFormReady = true;
}

/**
 * Recursive function to turn the xml of the import data types into a list for the interface
 */
function dataTypeXmlToHtml(xml, tierNum) {
    // Handle current data type
    let typeAttr = xml.childNodes;
    let id = "";
    let display_name = "";
    let filterRegex = "";
    let childDataTypes = [];
    let description = '';

    for (let i = 0; i < typeAttr.length; i++) {
        let node = typeAttr[i];
        if (node.nodeName === "id") {
            id = node.textContent;
        } else if (node.nodeName === "display_name") {
            display_name = node.textContent;
        } else if (node.nodeName === "filter_regex") {
            filterRegex = node.textContent;
        } else if (node.nodeName === "data_type") {
            childDataTypes.push(node);
        } else if (node.nodeName === "description") {
            description = node.textContent;
        }
    }
    // Create the html for this data type
    let html = createDataTypeHtml(id, display_name, tierNum, description);

    // Save the filter regex
    dataTypeFilters[id] = new RegExp(filterRegex);

    // If this data type has child data types, iterate through them recursively too
    //let children = tier.childNodes;
    for (let i = 0; i < childDataTypes.length; i++) {
        let node = childDataTypes[i];
        if (node.nodeName === "data_type") {
            // Recursive call
            html += dataTypeXmlToHtml(node, tierNum + 1);
        }
    }

    return html;
}

function createDataTypeHtml(id, display_name, tierNum, description) {
    let singleSpace = '&nbsp;';
    let indentation = singleSpace.repeat(tierNum * 6);

    let checked = "";
    if (display_name === "File") {
        checked = " checked ";
    }

    let html = '<div class="dataTypeRadioDiv" for="' + id + 'Radio">' + indentation +
        '<input type="radio" id="' + id + 'Radio" name="importFileType" value="' + id +
        '" ' + checked + '> <label for="' + id + 'Radio" ' + ' title="' + description + '" >' +
        display_name + '</label> </div>';

    return html;
}

/////////////////////////////////////////
////Attach Upload to Dataset Buttons/////
/////////////////////////////////////////


function updateAttachBlock(datasetLink, datasetName, componentId) {
    if (!parent.workflowIsRunning) {
        var attachBlock = "";
        var digitPattern = "[0-9]+";
        var regex = new RegExp(digitPattern, "gi");

        if (datasetLink.length > 0 && datasetLink.match(regex) !== null) {
            // File is already linked to a dataset
            attachBlock = 'Attached dataset: <div class="dataset_linked">' +
                '<a href="../../../Files?datasetId=' + datasetLink + '" target="_blank">' +
                '<label for="datasetLinkButton">' + datasetName +
                '</label></div>';
        } else {
            // File is not yet attached
            attachBlock = '<div class="addDatasetLink dataset_not_linked">' +
                '<label>Add to dataset?</label>' +
                '</label></div>';
        }

        jQuery('#datasetLinkDiv').remove();
        if (jQuery('#componentDatasetInfo').length > 0) {
            jQuery('#componentDatasetInfo').html(attachBlock);
        }
        jQuery('.addDatasetLink').off('click');
        hideAttaching();

        jQuery('.addDatasetLink').click(function() {
            var digitPattern = "[0-9]+";
            var regex = new RegExp(digitPattern, "gi");
            var hasFile = false;
            if ((jQuery('#filePath').val() != undefined) && jQuery('#filePath').val().match(regex) !== null) {
                hasFile = true;
                jQuery('#componentDatasetInfo').show();
            }
            if (parent.isWorkflowSaved === false && hasFile) {
                parent.promptForSaving(jQuery('.addDatasetLink'));
            } else if (!hasFile) {
                var dialogHtml = '' + "You must upload a file before you can add the file to a dataset." + ' <br/>';
                wfInfoDialog('fileUploadDialog', dialogHtml, 'Error');
            } else {
                showAttachOptions();
                jQuery('.addDatasetLink').hide();
                jQuery('#datashopUploadTable').hide();
                jQuery('.localFileUpload, #irbRequirementDiv').hide();
            }
        });

        if ((jQuery('#filePath').val() != undefined) && jQuery('#filePath').val().match(regex) !== null) {
            // File has been uploaded already
            jQuery('#componentDatasetInfo').show();
        }
    } else {
        parent.showCancelWarning();
    }
}

/**
 * Handles the Attach to Dataset option for Import File Uploads.
 */
function attachUploadToDataset() {
    if (parent.workflowIsRunning) {
        parent.showCancelWarning();
    } else {
        var digitPattern = "[0-9]+";
        var regex = new RegExp(digitPattern, "gi");
        var hasFile = false;
        if ((jQuery('#filePath').val() != undefined) && jQuery('#filePath').val().match(regex) !== null) {
            hasFile = true;
        }

        if (!hasFile) {
            var dialogHtml = '' + "You must upload a file before you can add the file to a dataset." + ' <br/>';
            wfInfoDialog('fileUploadDialog', dialogHtml, 'Error');
        } else if (parent.isWorkflowSaved === false) {
            parent.promptForSaving(jQuery('.addDatasetLink'));
        } else {
            // attach file to the dataset
            var dsId = null;

            let componentId = parent.getComponentId();

            var fileLabel = jQuery('#fileType').val();;

            var fileIndex = 0;

            if ((this.name == 'wfDatasetId' || newDatasetIdToAddTo != null) && fileLabel != null) {
                // attaching to upload
                var wfFileId = jQuery('#filePath').val();

                let wfDatasetId = null;
                if (newDatasetIdToAddTo != null) {
                    // Adding file to dataset that was just created
                    wfDatasetId = newDatasetIdToAddTo;
                    newDatasetIdToAddTo = null;
                } else {
                    wfDatasetId = jQuery(this).val();
                }

                new parent.Ajax.Request('WorkflowEditor', {
                    parameters: {
                        requestingMethod: 'WorkflowEditorServlet.existingDataset',
                        workflowId: parent.currentDigraph.id,
                        wfFileId: wfFileId,
                        datasetId: dsId,
                        componentId: componentId,
                        wfDatasetId: wfDatasetId,
                        fileIndex: fileIndex,
                        fileLabel: fileLabel
                    },
                    onComplete: attachDatasetFileCompleted,
                    beforeSend: parent.wfShowStatusIndicator("Attaching..."),
                    onSuccess: parent.wfHideStatusIndicator,
                    onException: function(request, exception) {
                        parent.wfHideStatusIndicator();
                        throw (exception);
                    }
                });

            }
        }
    }
}

/**
 * Options File upload completed handler. File uploads are restricted to 1 file option per component.
 */
function attachDatasetFileCompleted(data) {
    if (data !== undefined && data.responseJSON !== undefined) {
        data = data.responseJSON;
    }

    parent.wfHideStatusIndicator();

    if (data.componentId !== undefined && data.componentId != null) {

        var componentId = data.componentId;

        // The option dataset attachment completed.
        if (data.success == "true") {
            var dialogHtml = '' + data.message + ' <br/>';
            wfInfoDialog('fileUploadDialog', dialogHtml, 'Success');

            if (data.datasetId !== "") {
                updateAttachBlock(data.datasetId, data.datasetName, componentId);
            }

            var fileInfoString = "File: " + data.fileName;
            if (data.projectName !== undefined && data.projectName != '') {
                projectName = data.projectName;
                fileInfoString = 'Project: ' + data.projectName + '&#013;&#010;' +
                    'Dataset: ' + data.datasetName + '&#013;&#010;' +
                    'File: ' + data.fileName;
            } else if (data.datasetName !== undefined && data.datasetName != '') {
                fileInfoString = 'Dataset: ' + data.datasetName + '&#013;&#010;' +
                    'File: ' + data.fileName;
            }
            // To unescape chars in the fileName title:
            var unescapedTitle = jQuery('<div/>').html(fileInfoString).text();
            jQuery('#currentFileName').attr('title', unescapedTitle);

            datasetName = data.datasetName;
            datasetLink = data.datasetId;

        } else {
            var dialogHtml = '' + data.message + ' <br/>';
            wfInfoDialog('fileUploadDialog', dialogHtml, 'Error');
        }
        parent.monitorComponentStatus();
    } else {
        parent.testLoggedOut(data);
    }
    jQuery('.localFileUpload').show();
    if (!irbRequirementIsMet()) {
        jQuery('#irbRequirementDiv').show();
    }
    if (jQuery('input[name=importFileType]:checked').val() != undefined) {
        jQuery('#selectDataTypeReminder').css('display', 'none');
    }

    // Finished attaching, set the stuff to show
    validNewDataset = true;
    doneAttaching();
}

function showAttachOptions() {
    jQuery('#addToDataset, #cancelButton, #attachingFileReminder').show();
    jQuery('#addingThisFile').text(jQuery('#currentFileName').text());
    jQuery('.importedFileText, #dataTypeDiv').hide();

    // Show the proper options
    if (jQuery('input[name="addToDatasetLocation"]:checked').val() == "existingDataset") {
        jQuery('.wfFileUploadRow').show();
        jQuery('#attachToNewDatasetDiv').hide();
    } else if (jQuery('input[name="addToDatasetLocation"]:checked').val() == "newDataset") {
        jQuery('.wfFileUploadRow').hide();
        jQuery('#attachToNewDatasetDiv, #upload_dataset_form').show();

        // From new or existing project?
        jQuery('#new_project_div, #existing_project_list_div').hide();
        let newProject = jQuery('input[name="project_group"]:checked').val() == "new";
        if (newProject) {
            jQuery('#new_project_div').show();
        } else {
            // Existing project
            jQuery('#existing_project_list_div').show();
        }
    }

    // Hide the tabs
    jQuery('.wfRadioButtonGroup').hide();
}

function doneAttaching(event) {
    if (validNewDataset) {
        jQuery('#addToDataset, #cancelButton, #attachingFileReminder').hide();
        jQuery('.wfRadioButtonGroup, .importedFileText').show();
        changedFileLocation(false);
        validNewDataset = false;
    }
}

function cancelAttaching(event) {
    jQuery('#addToDataset, #cancelButton, #attachingFileReminder').hide();
    jQuery('.wfRadioButtonGroup, .importedFileText').show();
    changedFileLocation(false);
    validNewDataset = false;
}

function hideAttaching() {
    jQuery('#addToDataset, #cancelButton, #attachingFileReminder').hide();
    jQuery('.wfRadioButtonGroup, .importedFileText').show();
}

/** when we create a new project we don't know the id yet to add to */
var newDatasetNameToAddTo = null;
var newDatasetIdToAddTo = null;
var newProjectIdToAddTo = null;
var newProjectNameToAddTo = null;
var validNewDataset = false;

function submitNewDataset(event) {
    if (!ensureValidNewDataset()) {
        validNewDataset = false;
        event.preventDefault();
        return;
    }
    validNewDataset = true;

    // Do this ajax call instead of form submit so new page does not open
    event.preventDefault();
    submitNewDatasetForm();

    newDatasetNameToAddTo = jQuery('input#datasetName').val();

    let newProject = jQuery('input[name="project_group"]:checked').val() == "new";
    if (newProject) {
        newProjectNameToAddTo = jQuery('input#new_project_name').val();
    } else {
        // Existing project
        newProjectIdToAddTo = jQuery('#existing_project_select').val();
    }

    // Request a refresh of datasets in ds list.  It will then add the file to the ds when it knows the id
    jQuery('.datasetList').html('');
    displayDatasets();
}

/**
 * Necessary to avoid going to the DatasetInfo page on submit
 * @returns
 */
function submitNewDatasetForm(){
    $.ajax({
        url:'../../../UploadDataset',
        type:'post',
        data:$('#upload_dataset_form').serialize(),
        success:function(){;
        }
    });
}

function ensureValidNewDataset() {
    let newProject = jQuery('input[name="project_group"]:checked').val() == "new";
    if (newProject) {
        let newProjectName = jQuery('input#new_project_name').val();
        if (newProjectName == null ||
            newProjectName == '') {
            errorDialog('Invalid Project Name', 'Please enter a valid project name.');
            return false;
        }
        if (projectNameExists(newProjectName)) {
            errorDialog('Duplicate Project Name', 'Please enter a project name that does not already exist.');
            return false;
        }
    } else {
        // Existing project
        if (jQuery('#existing_project_select').val() == null) {
            errorDialog('No Project', 'Please select a project to add the dataset to.');
            return false;
        }
    }

    let newDatasetName = jQuery('input#datasetName').val();
    if (datasetNameExists(newDatasetName)) {
        errorDialog('Duplicate dataset Name', 'Please enter a dataset name that does not already exist.');
        return false;
    }

    if (newDatasetName == null ||
        newDatasetName == "") {
        errorDialog('Invalid Dataset Name', 'Please enter a valid dataset name.');
        return false;
    }

    if (jQuery('#domainLearnlab').val() == 'dummy') {
        errorDialog('No Domain', 'Please select a Domain/LearnLab.');
        return false;
    }

    return true;
}

function projectNameExists(name) {
    let alreadyExists = false;
    myProjectArray.forEach(function(project) {
        if (name == project.projectName) {
            alreadyExists = true;
        }
    });
    projectArray.forEach(function(project) {
        if (name == project.projectName) {
            alreadyExists = true;
        }
    });
    return alreadyExists;
}

function datasetNameExists(name) {
    let alreadyExists = false;
    myDatasetArray.forEach(function(dataset) {
        if (name == dataset.datasetName) {
            alreadyExists = true;
        }
    });
    datasetArray.forEach(function(dataset) {
        if (name == dataset.datasetName) {
            alreadyExists = true;
        }
    });
    return alreadyExists;
}

function errorDialog(dialogTitle, dialogHtml) {
    jQuery('<div />', {
        id: componentId + "errorDialog"
    }).html(dialogHtml).dialog({
        open: function() {
            jQuery('.ui-button').focus();
        },
        autoOpen: true,
        autoResize: true,
        resizable: false,
        width: '30%',
        modal: true,
        title: dialogTitle,
        buttons: {
            'Okay': function() {
                jQuery(this).dialog('close');
            }
        },
        open: function() {
            jQuery(this).height((jQuery(this).height() + DIALOG_TITLE_PADDING_OFFSET) + 'px');
        },
        close: function() {
            jQuery(this).remove();
        }
    });
}

function attachFileToNewDataset(datasetArray) {
    datasetArray.forEach(function(dataset) {
        if (dataset.datasetName == newDatasetNameToAddTo &&
            dataset.projectId == newProjectIdToAddTo) {
            newDatasetIdToAddTo = dataset.id;
        }
        if (dataset.datasetName == newDatasetNameToAddTo &&
            dataset.projectName == newProjectNameToAddTo) {
            newDatasetIdToAddTo = dataset.id;
        }
    });
    if (newDatasetIdToAddTo == null) {
        errorDialog('Dataset not created',
            'The dataset was not created.  Ensure it has a unique name.');
        validNewDataset = false;
        return;
    }
    newDatasetNameToAddTo = null;
    newProjectIdToAddTo = null;
    newProjectNameToAddTo = null;

    attachUploadToDataset();
}
