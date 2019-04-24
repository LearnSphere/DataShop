//
// Carnegie Mellon University
// Copyright 2008
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 13064 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-04-11 11:57:05 -0400 (Mon, 11 Apr 2016) $
// $KeyWordsOff: $
//

//GLOBALS
var modelsJSON = null;
var isCanceled = false; 
var modelExporProgressBar;
var popup = false;
var timeout = false;
var lfaDone = false;
var importActions = {
    rename: "Import Renamed",
    skip: "Skip",
    overwrite: "Overwrite existing"
};

/**
 * Initialize all the various controls for each model which handles the styles and 
 * opens/closes the import/export box.
 */
function initModelControls() {
    $("content").select('.control').each(
        function(ctrl) { initSingleControl(ctrl); }
    );
    
    $("modelValuesTable").select('.detailsLink').each(
        function(ctrl) {
            ctrl.observe('click', function() {
                var model_id = ctrl.readAttribute('model_id');
                var model_order = ctrl.readAttribute('model_order');
                $(model_order + "_details").toggle();
                this.update(($(model_order + "_details").visible()) ? "<img src=\"images/contract.png\">" : "<img src=\"images/expand.png\">");
            });
        }
    );
    
    $("show_more_control").observe('click', function() {
        $("warning_text_long").toggle();
            this.update(($("warning_text_long").visible()) ? "less" : "more");
        });
       

    $("model_close_btn").observe('click', closeDialogue);   
                 
    $('model_import_cancel_btn').observe('click', cancelImport);

    $("model_export_btn").observe('click', startModelExport);

    if ($("model_toolbox_export")) {
        $("model_toolbox_export").observe('click', function() { openExportWindow(-1); });
    }

    if ($("model_toolbox_import")) {
        $("model_toolbox_import").observe('click', openImportWindow);
    }
    
    $("modelValuesTable").select('.modelName').each(
        function(ctrl) {
            var modelId = ctrl.readAttribute('model_id');
            var modelOrder = ctrl.readAttribute('model_order');
            if (modelId) {
                ctrl.editor = null;
                ctrl.editor = new InlineEditor(ctrl, null,'KcModel', { 
                    maxLength: 50,
                    id:modelId,
                    onSuccess: updateExportNames.bind(ctrl) });
            }

            var params = { extraClasses: "infoDiv kcm_tooltip" };
            var toolTipText = $("tooltip_content_"+modelOrder).innerHTML;
            new ToolTip("kcm_id_name_"+modelOrder, toolTipText, params);
        }
    );
    
    
    $("modelValuesTable").select('.deleteModel').each( 
        function(ctrl){
            var modelId = ctrl.readAttribute('model_id');
            if (modelId) {
                ctrl.observe('click', function(event) {
                    //hide all the current controls
                        this.up().childElements().each(function (item) { item.hide(); });
                        //add delete dialog
                        this.up().insert("<span>delete this model? </span><br />"
                            + '<span class="control yes">yes</span>'
                            + '<span> / </span>'
                            + '<span class="control no">no</span>');
                            
                        this.up().down(".yes").observe('click', function () {
                            deleteModel(this.up().down().readAttribute('model_id'));
                        });
                        this.up().down(".yes").observe('mouseover',
                            function() { this.setStyle({ cursor: 'pointer'}); });
                        this.up().down(".yes").observe('mouseout',
                            function() { this.setStyle({ cursor: 'auto'}); });
                            
                        this.up().down(".no").observe('click', function () {
                            this.up().childElements().each(function (item) {
                        if (item.visible()) {
                            item.remove();
                        } else {
                            item.show();
                        }
                            });
                        });
                        this.up().down(".no").observe('mouseover',
                            function() { this.setStyle({ cursor: 'pointer'}); });
                        this.up().down(".no").observe('mouseout',
                            function() { this.setStyle({ cursor: 'auto'}); });
                });
            }//end of if modelId
        }
    );
    
    
    new Field.Observer('model_import_file' , 0.3, observeImportFileField);
        
    $("model_import_btn").observe('click', uploadModelFile);    
    
    //DS1295: KC Model Sort
    $("kcmSortBy").observe('change', requestKCModels); //in DatasetInfo.js
    $("kcmSortAscending").observe('change', requestKCModels); //in DatasetInfo.js
}

/**
 * Function that on a successful update of the name rewrites the name in
 * the kc model export select to reflect the new name.
 */
function updateExportNames() {
    $("modelValuesTable").select('.modelName').each(
        function(ctrl) {
            var modelId = ctrl.readAttribute('model_id');
            var modelOrder = ctrl.readAttribute('model_order');
        }
    );

    if (!this.editor) { return; }
    var oldName = this.editor.undoText;
    var newName = this.editor.originalText;

    $A($("export_model_select").options).each(function(option) {
        if (option.innerHTML == oldName) { option.innerHTML = newName; }
    });    
}

/**
 * Close the model export/import dialogue and show the toolbox.
 */
function closeDialogue() {
    $("model_import_file").value = '';
    $("toolbox").show();
    $("model_dialogue").hide();
}

/**
 * Pays attention to the model_input_file field in the form.  If the field does not
 * have a value (user has not selected a file), then the verify button should not be 
 * active.  If a value is there, make sure the user can verify and cancel.
 */
function observeImportFileField() {
    if ($F('model_import_file').blank()) {
        $('model_import_btn').disabled = true;
        enableCloseButton(closeDialogue);
    } else {
        if ($("model_import_btn").disabled == true) {
            $("model_import_btn").show();
            $("model_import_btn").disabled = false;
            $("model_import_cancel_btn").value = "Cancel";
            $("model_import_cancel_btn").show();
            $("model_import_cancel_btn").stopObserving('click');
            $("model_import_cancel_btn").observe('click', cancelImport);
            $("model_import_cancel_btn").disabled = false;
            $("model_import_log").innerHTML = "";
        }
    }
}

/**
 * Cancels the model import.  Provides the option to close the dialogue.
 */
function cancelImport() {
    isCanceled = true;
    $('model_import_file').disabled = false;
    
    //send an ajax request to the servlet canceling the current upload.
    new Ajax.Request("KcModel", {
        parameters: {
            requestingMethod: "KcModel.cancelImport",
                datasetId: dataset,
            cancel: "true",
            models: modelsJSON },
        requestHeaders: {Accept: 'application/json;charset=UTF-8'},
        onComplete: cancelImportAjaxListener,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/**
 * onComplete listener for cancelImport().  Logs the cancel message to the import console
 * and renables buttons based on previous user state. 
 */
function cancelImportAjaxListener(transport) {
    var json = transport.responseText.evalJSON(true);
    writeErrorToLog('Import canceled.');
    if (json.message && json.message != "") {
            writeToLog(json.message);
    }

    // if user has canceled a verify, reset the button to verify
    if ($('model_import_btn').value == 'Import' 
            || $('model_import_btn').value == 'Verify') {
        enableVerifyButton(); 
        enableCloseButton(requestKCModels); //in DatasetInfo.js
    } else {
        $('model_import_file').value = '';
        $('model_import_btn').hide();
        enableCloseButton(requestKCModels); //in DatasetInfo.js
    }
} // end cancelImportAjaxListener()

/*
 * Enable the model import button to verify.
 */
function enableVerifyButton() {
    $('model_import_btn').value = 'Verify';
    $('model_import_btn').disabled = false;
    $('model_import_btn').stopObserving('click');
    $('model_import_btn').observe('click', uploadModelFile);
}

/*
 * Enable the close button with the supplied action.
 */
function enableCloseButton(action) {
    $('model_import_cancel_btn').disabled = false;
    $('model_import_cancel_btn').value = "Close";
    $('model_import_cancel_btn').stopObserving('click');
    $('model_import_cancel_btn').observe('click', action);
}

function initSingleControl(ctrl) {
    ctrl = $(ctrl);   
    ctrl.observe('mouseover', function() { this.setStyle({ cursor: 'pointer'}); });
    ctrl.observe('mouseout', function() { this.setStyle({ cursor: 'auto'}); });
    var model_id = ctrl.readAttribute('model_id');
    if (model_id && ctrl.innerHTML == "export") {
        ctrl.observe('click', function(event) { 
            openExportWindow(this.readAttribute('model_id'));
        });
    } else if (model_id && ctrl.innerHTML == "delete") {
        ctrl.observe('click', function(event) {
            //hide all the current controls
            this.up().childElements().each(function (item) { item.hide(); });
            this.up().insert("<span>delete this model? </span><br />"
                + '<span class="control yes">yes</span>'
                + '<span> / </span>'
                + '<span class="control no">no</span>');



            this.up().down(".yes").observe('click', function () {
                deleteModel(this.up().down().readAttribute('model_id'));
            });
            this.up().down(".yes").observe('mouseover',
                function() { this.setStyle({ cursor: 'pointer'}); });
            this.up().down(".yes").observe('mouseout',
                function() { this.setStyle({ cursor: 'auto'}); });


            this.up().down(".no").observe('click', function () {
                this.up().childElements().each(function (item) {
                    if (item.visible()) {
                        item.remove();
                    } else {
                        item.show();
                    }
                });
            });

            this.up().down(".no").observe('mouseover',
                function() { this.setStyle({ cursor: 'pointer'}); });
            this.up().down(".no").observe('mouseout',
                function() { this.setStyle({ cursor: 'auto'}); });
        });
    } else if (model_id && ctrl.innerHTML == "rename") {
        ctrl.observe('click', function(event) {
            var nameSpan = ctrl.up('.model').down('.modelName');
            if (nameSpan && nameSpan.editor && nameSpan.editor.state == "view") {
                nameSpan.editor.openForEdit();
            }
        });
    }
}

function startModelExport() {
    if (!modelExporProgressBar) {
        var selectedModels = $F("export_model_select");
        
        var selecteModelsParam = "";
        var counter = 0;
        selectedModels.each(function (id) {
            counter++;
            selecteModelsParam = selecteModelsParam + id;
            selecteModelsParam = (counter >= selectedModels.size())
                    ? selecteModelsParam : selecteModelsParam  + ", ";
        });

        new Ajax.Request("KcModel", {
            parameters: {
            requestingMethod: "KcModel.startModelExport",
                datasetId: dataset,
                export_start: "true",
                models: selectedModels.toJSON()
            },
            onComplete: function (transport) {
                 if (!modelExporProgressBar) {
                    modelExporProgressBar = 
                        new ProgressBar(getFinalStepModelExportProduct, 'KcModel');
                 }
            },
            onException: function (request, exception) {
                throw(exception);
            }
        });
    } else {
        if (modelExporProgressBar != null) {
            modelExporProgressBar.cancelExport();
            modelExporProgressBar.closeAll();
        } 
        modelExporProgressBar = false;
        startModelExport();
    }
}

/** 
 * When the export is done call this function to get the actual file.
 */
function getFinalStepModelExportProduct() {

    var newForm = document.createElement('FORM');
    newForm.setAttribute('name', 'export_form');
    newForm.setAttribute('id', 'export_form');
    newForm.setAttribute('form', 'text/plain');
    newForm.setAttribute('action', 'KcModel');
    newForm.setAttribute('method', 'POST');

    var newInput = document.createElement('input');
    newInput.name="get_export_file";
    newInput.type="hidden";
    newForm.appendChild(newInput);
    
    newInput = document.createElement('input');
    newInput.name="datasetId";
    newInput.type="hidden";
    newInput.value=dataset
    newForm.appendChild(newInput);
    
    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

/** 
 * When the export is done call this function to get the actual file.
 */
function deleteModel(modelId) {

    var newForm = document.createElement('FORM');
    newForm.setAttribute('name', 'export_form');
    newForm.setAttribute('form', 'text/plain');
    newForm.setAttribute('action', 'KcModel');
    newForm.setAttribute('method', 'POST');

    var newInput = document.createElement('input');
    newInput.name="model_delete";
    newInput.type="hidden";
    newInput.value=modelId;
    newForm.appendChild(newInput);
    
    newInput = document.createElement('input');
    newInput.name="datasetId";
    newInput.type="hidden";
    newInput.value=dataset
    newForm.appendChild(newInput);
    
    var source = "DatasetInfo";
    if ($("learningCurves")) {
         source = "LearningCurve";
    }
    newInput = document.createElement('input');
    newInput.name="source";
    newInput.type="hidden";
    newInput.value=source;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function openExportWindow(modelId) {

    var options = $("export_model_select").select("option");
    options.each(function (item) {
        if (item.readAttribute('value') == modelId) {
            item.selected = true;
        } else {
            item.selected = false;
        }
    });

    if ($("model_dialogue_import")) {
        $("model_dialogue_import").addClassName("control");
    }
    $("model_dialogue_export").removeClassName("control");

    $("model_dialogue_export").stopObserving('click');
    $("model_dialogue_export").stopObserving('mouseover');
    $("model_dialogue_export").stopObserving('mouseout');
    
    if ($("model_dialogue_import")) {
        initSingleControl("model_dialogue_import");
        $("model_dialogue_import").observe('click', openImportWindow);
    }
    
    $("toolbox").hide();
    $("model_import").hide();
    $("model_export").show();
    $("model_dialogue").show();
}

function openImportWindow() {
    isCanceled = false;

    $("model_import_btn").show();
    $("model_import_btn").disabled = true;
    enableCloseButton(closeDialogue);;
    $("model_dialogue_export").addClassName("control");
    
    if ($("model_dialogue_import")) {
        $("model_dialogue_import").removeClassName("control");
        $("model_dialogue_import").stopObserving('click');
        $("model_dialogue_import").stopObserving('mouseover');
        $("model_dialogue_import").stopObserving('mouseout');
    }
    
    initSingleControl("model_dialogue_export");
    $("model_dialogue_export").observe('click', openExportWindow);

    $("toolbox").hide();
    $("model_export").hide();
    $("model_import").show();
    $("model_dialogue").show();
}

function uploadModelFile() {
    $('model_import_btn').disabled = true;
    
    // we could have reached this point by canceling a verify and then attempting
    // to verify again.  If this is the case, make sure to reset the cancel button.
    if ($('model_import_cancel_btn').value == 'Close') {
        $('model_import_cancel_btn').value = 'Cancel';
        $('model_import_cancel_btn').stopObserving('click');
        $('model_import_cancel_btn').observe('click', cancelImport);
    }
    
    var options = {
        onStart : function() { 
            writeToLog('Uploading file...');
            //return true; 
        },
        onComplete : function(results) {
            //cast the results to a JSON object.
            results = results.evalJSON(true);

            if (results.outcome == "SUCCESS") {
                writeToLog('Upload completed successfully');
                startModelVerify();
            } else {
                if (results.message && results.message != "") {
                    writeErrorToLog(results.message);
                } else {
                    writeErrorToLog('An error occurred during upload, but unable to determine error. '
                        + ' Please try again and contact the DataShop'
                        + ' team if the errors persists.');
                }
                writeToLog('Import stopped.');
                $('model_import_btn').disabled = false;
            }
            //return true;
        }
    };

    new FileUploader("model_import_form", options);
}

function startModelVerify() {
    // user could have canceled mid-verify and attempt to verify again.  Make sure
    // isCanceled flag is reset, otherwise import will not complete.
    if (isCanceled) { isCanceled = false; }
    writeToLog('Verifying uploaded file...');

    new Ajax.Request('KcModel', {
        parameters: {
            requestingMethod: "KcModel.startModelVerify",
            datasetId: dataset,
            verifyModel: "true"
        },
        requestHeaders: {Accept: 'application/json;charset=UTF-8'},
        onComplete: this.modelVerifyAjaxListener,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function modelVerifyAjaxListener(transport) {
    try {
        var json = transport.responseText.evalJSON(true);
    } catch (exception) {
        writeErrorToLog('Unexpected results returned from the server.'
            + ' Please try again and contact the DataShop'
            + ' team if the errors persists.');
        $('model_import_btn').disabled = false;
    }

    if (json.outcome == "ERROR"){
        writeErrorToLog('An error occurred during verification.');
        if (json.message && json.message != "") {
            writeErrorToLog(json.message);
        } else {
            writeToLog('Unable to determine error.  Please try again and contact the DataShop'
                + ' team if the errors persists.');
        }

        $('model_import_btn').disabled = false;
        return;
    } else if (json.outcome == "SUCCESS") {
        writeToLog("File verification complete.");
        popup = false;
        timeout = false;

        $('model_import_btn').disabled = true;
        $('model_import_btn').value = "Import";
        parseUploadModels(json.models, 0);
        return;
    } else if (json.outcome == "WARNING") {
        writeWarningToLog("File verification complete, with warnings:");
        writeWarningToLog(json.message);
        popup = false;
        timeout = false;

        $('model_import_btn').disabled = true;
        $('model_import_btn').value = "Import";
        parseUploadModels(json.models, 0);
        return;
    } else {
        writeErrorToLog('Unexpected results returned from the server.'
            + ' Please try again and contact the DataShop'
            + ' team if the errors persists.');
        $('model_import_btn').disabled = false;
    }
}

/**
 * See KCModelHelper.java for where the JSON object is created.
 */
function parseUploadModels(models, current) {

    if (popup && popup.isCanceled) { popup = false; return; }

    if (current < models.length && (!popup || popup.isFinished)) {
        if (popup) {
            models[current] = popup.getModel();
            current++;
            popup = false;
        }

        if (models[current]) {
            //if it has an id we know it's an existing model
            //and requires more user input.
            if (models[current].id) {
                popup = new ModelPopup(models[current]);
            //if it has a name length, then the name is too long
            } else if (models[current].nameLength) {
                popup = new ModelPopup(models[current]);
            } else {
                models[current].action = "Import New"
                current++;
            }
        }
    }
    
    //still waiting for user input.
    if (current < models.length || (popup && !popup.isFinished) ) {
        if (timeout) { clearTimeout(timeout); }
        setTimeout(function () { parseUploadModels(models, current); }, 500);
    
    //all the models are finished.
    } else if ( current >= models.length) {
        //if the cover is there, remove it.
        if ($('model_popup_cover')) { $('model_popup_cover').remove(); }

        writeToLog("<strong>Import File Summary:</strong>");
        var allSkipped = true;
        models.each(
            function (model) {
                if (model.action != importActions.skip) { allSkipped = false; }
                writeToLog(" * " + model.action + ' model "' + model.name + '"');
            }
        );

        if (allSkipped) {
            writeErrorToLog('All models were skipped, nothing to import.');
            $('model_import_btn').disabled = false;
            $('model_import_btn').value = "Verify";
        } else {
            writeToLog('If the information above is correct, press the "Import" '
                + 'button to continue. Otherwise, '
                + 'press "Cancel" to stop the import process for this file.');

            $('model_import_file').disabled = true;

            $('model_import_btn').disabled = false;
            $('model_import_btn').stopObserving('click');
            $('model_import_btn').observe('click', function () { finishImport(models) });
        }
    }
}

/**
 * Import file has been uploaded and verified, user has confirmed all 
 * questions about import, now tell the server the user changes and tell it
 * to start importing.
 */
function finishImport(models) {
    $('model_import_btn').disabled = true;

    //rebuild the JSON object for transport to the server.
    modelsJSON = '{"models": ' + models.toJSON() + '}';

    new Ajax.Request('KcModel', {
        parameters: {
            requestingMethod: "KcModel.finishImport",
            datasetId: dataset,
            model_import: "start",
            models: modelsJSON
        },
        requestHeaders: {Accept: 'application/json;charset=UTF-8'},
        onComplete: function () {
                writeToLog('Beginning insertion process.');
                writeToLog('Processing file... 0%');
                monitorImportProgress();
            },
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function monitorImportProgress() {
    if (isCanceled) { return; }
    
    new Ajax.Request('KcModel', {
        parameters: {
            requestingMethod: "KcModel.monitorImportProgress",
            datasetId: dataset,
            model_import: "check"
        },
        onComplete: monitorImportProgressListener,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function monitorImportProgressListener(transport) {
    
    var json = transport.responseText.evalJSON(true)
    
    if (json.currentStatus == "ok") {
        
        if (json.percent == 100) {
            $('model_import_cancel_btn').disabled = true;
            
            writeToLog("Processing file... 100%", {update: true});
            writeToLog("Initial import process complete!")

            if (json.totalRows) {
                writeToLog(json.totalRows + ' rows in the file were processed.');
            }

            if (json.numRowsNotImported && json.numRowsNotImported > 0) {
                writeErrorToLog(json.numRowsNotImported + ' rows were skipped as the '
                    + 'matching step was not found');
            }
            writeToLog('Running additive factor model (AFM) and '
                    + 'building sample information for new models. '
                    + '<br />(<strong>Note</strong>: this may take a very long time '
                    + 'depending on the size of the dataset!)');
            writeToLog("AFM in progress.");

            monitorLFAAndAggProgress();
        } else {
            writeToLog("Processing file... " + json.percent + "%", {update: true});
            setTimeout(function () { monitorImportProgress(); }, 2000);
        }
    } else {
        writeErrorToLog('Error during import, aborting.');
        if (json.message) {
            writeErrorToLog('Error Message: ' + json.message);
            
            if (json.totalRows) {
                writeErrorToLog('Error seems to have occurred on line: ' + json.totalRows);
            }
            
            if (json.numRowsNotImported && json.numRowsNotImported > 0) {
                writeErrorToLog(json.numRowsNotImported 
                    + ' rows were skipped as the matching step was not found');
            }
        } else {
            writeErrorToLog('Unable to determine error, please contact the DataShop team.');
        }
        
        $("model_import_btn").value = "Verify";
        $("model_import_btn").stopObserving('click');
        $("model_import_btn").observe('click', uploadModelFile);
        $("model_import_btn").disabled = false;
    }
}

function monitorLFAAndAggProgress(params) {
    if (isCanceled) { return; }

    if (!params) { params = {check_lfa_and_progress: "true"}; }
    params['requestingMethod'] = "KcModel.monitorLFAAndAggProgress";
    params['datasetId'] = dataset;
    new Ajax.Request('KcModel', {
        parameters: params,
        requestHeaders: {Accept: 'application/json;charset=UTF-8'},
        onComplete: monitorLFAAndAggProgressListener,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}


var lfaString = "AFM in progress."
function monitorLFAAndAggProgressListener(transport) {
    var json = transport.responseText.evalJSON(true);

    var checkAgain = true;
    if (json.lfaStatus  == "FINISHED" && json.aggStatus == "FINISHED") {
        writeToLog('Processing data... 100%', {update: true});
        writeToLog('Import complete. Your new KC model is ready for use.');
        writeToLog('Click refresh to see your new model.');
        checkAgain = false;

        //change the button to a refresh.
        $("model_import_btn").value = "Refresh";
        $("model_import_btn").stopObserving('click');
        $("model_import_btn").observe('click', requestKCModels); //in DatasetInfo.js
        $("model_import_btn").disabled = false;

    } else if (json.lfaStatus == "FINISHED" && !lfaDone) {
        lfaDone = true;
        writeToLog('AFM Complete!', {update: true});
        writeToLog('Building reporting tables... 0%');
    } else if (json.aggStatus == "RUNNING") {
        if (json.aggPercent == 100) {
            writeToLog("Building reporting tables... 100%", {update: true});
            writeToLog('Import process complete');
        } else if (json.aggPercent >= 0) {
            writeToLog("Building reporting tables... " + json.aggPercent + "%", {update: true});
        }
    } else if (json.lfaStatus == "ERROR" || json.aggStatus == "ERROR") {
        writeToLog('Oops! There was an unexpected error trying'
            + ' to finish the import processes, please contact the DataShop team', {cssClass: "error" });
        checkAgain = false;
    } else if (json.lfaStatus == "RUNNING") {
        lfaString = (lfaString.length < 20) ? lfaString + "." : "AFM in progress."
        writeToLog(lfaString, {update: true});
    }

    if (checkAgain) { setTimeout(function () { monitorLFAAndAggProgress(); }, 2000); }
}

/**
 * Helper function that writes to the log with the "error" css class set.
 */
function writeErrorToLog(message, options) {
    if (options) {
        Object.extend(options, {cssClass: "error"});
    } else {
        options = {cssClass: "error"};
    }   
    writeToLog(message, options);
}

/**
 * Helper function that writes to the log with the "warning" css class set.
 */
function writeWarningToLog(message, options) {
    if (options) {
        Object.extend(options, {cssClass: "warning"});
    } else {
        options = {cssClass: "warning"};
    }   
    writeToLog(message, options);
}

/**
 * Helper function to add additional bits to the log file and automagically scroll the window
 * to the bottom as new stuff is appended.
 */
function writeToLog(message, options) {
    var logOptions = {
      update   : false,
      cssClass : ""
    };
    Object.extend(logOptions, options || { });
    
    var logDiv = $('model_import_log')

    if (logOptions.update) {
        logDiv.select('p').last().update(message); 
    } else {
        logDiv.insert('<p class="' + logOptions.cssClass  + '">' + message + '</p>');
    }

    logDiv.scrollTop = logDiv.scrollHeight;
}

/** 
 * Convience object for handling the popups to gather more informatation about 
 * what to do for overwrites, etc.
 * 
 * DEPENDENCIES: prototype v1.6
 */
var ModelPopup = Class.create();
ModelPopup.prototype = {

    /**
     * Constructor
     * @param model JSON object represention of the model (See KCModelHelper.java)
     *    model.nameLength - the number of characters in the KC Model name if over 50
     *    model.isOwner - "true" / "false" indicator of whether the current user 
     *                    is the owner of this model.
     *    model.positions - array with the column numbers in the model file.
     *    model.id - database identifier if this model already exists in the db.
     *    model.existingModels - array of all existing model names for the dataset
     *                           used to prevent unintentional overwrites of an existing model.
     */
    initialize: function(model) {
        this.model = model;

        this.isFinished = false;
        this.isCanceled = false;

        this.createView();
    },
     
    /**
     * Returns the model for this popup.
     * @return model JSON object of the model.
     */
    getModel: function() {
        return this.model;
    },
    
    /**
     * Create the necessary DOM elements to display the popup and initialize the
     * user input observers. 
     */
    createView: function() {
        var ownerString = (this.model.isOwner === true) ?
            "You own the model." : "You don't own this model.";
            
       
        var height = $('model_dialogue').getHeight();
        var width = $('model_dialogue').getWidth();

        //if the cover doesn't already exist add it.
        var htmlString = ($('model_popup_cover')) ?
            "" : '<div id="model_popup_cover" ' 
                   + 'style="height:' + height + 'px; width:' + width + 'px;">&nbsp;</div>';
        
        if (this.model.nameLength) {
        htmlString = htmlString
            +    '<div id="model_popup">'
            +    '<h1>Import Options</h1>'
            +    '<p>KC Model name "' + this.model.name + '" is too long. </p>';
        } else {
        htmlString = htmlString
            +    '<div id="model_popup">'
            +    '<h1>Import Options</h1>'
            +    '<p>KC Model "' + this.model.name + '" already exists. ' + ownerString + '</p>';
        }
        
        if (this.model.isOwner === true) {
            htmlString +=
                 '<input type="radio" name="import_option" id="overwrite_model_choice" value="' + importActions.overwrite + '" />'
               + '<label for="overwrite_model_choice">Overwrite. This is not reversible!</label><br />'
        }            

        var renameString = (this.model.isOwner === true) ? "Don't overwrite&mdash;use this model name instead:" : "Use this model name instead:"

        htmlString = htmlString
            +    '<input type="radio" name="import_option" id="rename_model_choice" value="' + importActions.rename + '" />'
            +    '<label for="rename_model_choice">' + renameString + '</label><br />'
            +    '<input type=text" name="rename" value="" id="model_rename_field" maxlength="50" /><br />'
            +    '<input type="radio" name="import_option" id="skip_model_choice" value="' + importActions.skip + '" />'
            +    '<label for="skip_model_choice">Skip this model (do not import)</label><br /><br />'
            +    '<input type="button" value="continue" id="model_popup_continue" />'
            +    '<input type="button" value="cancel" id="model_popup_cancel" />'   
            + '</div>';

        $('model_dialogue').insert(htmlString);
        $('model_popup_cover').setOpacity(0.5);
        
        $('model_popup_continue').observe('click', this.continueBtnListener.bindAsEventListener(this));
        $('model_popup_cancel').observe('click', this.cancelBtnListener.bindAsEventListener(this));

        
    },

    /**
     * Listener function for when the continue button is pressed by the user.
     */
    continueBtnListener: function() {

        //removes any existing error messages in the popup
        $('model_popup').select('p.error').invoke('remove');

        //get the selection option.
        var importOptions = $('model_popup').select('[name="import_option"]');
        var selected = null;
        importOptions.each(function(input) { selected = ($F(input)) ? $F(input) : selected;});

        if (!selected) {
            this.displayError('Please indicate your preference with this model in order to continue.');
            return;
        }

        if (selected == importActions.rename) {
            var newName = $F('model_rename_field').strip();
            if (newName.empty()) {
                this.displayError('Please enter a new name in the field.');
                return;
            } else if ($A(this.model.existingModels).indexOf(newName) >= 0) {
                this.displayError('Model name "' + newName
                            + '" is already in use, please enter a different unique name.');
                return;   
            } else {
                var REGEX_KCM_NOT_ALLOWED_CHARS = "[^\\sA-Za-z0-9_-]";
                var regex = new RegExp(REGEX_KCM_NOT_ALLOWED_CHARS);
                if (regex.test(newName)) {
                    this.displayError('Invalid character(s) found in model name "' + newName
                                      + '". Valid characters include space, dash, '
                                      + 'underscore, letters and numbers.');
                    return;   
                } else {
                    this.model.name = newName;
                }
            }
        }

        this.model.action = selected;
        $('model_popup').remove();
        this.isFinished = true;
    },

    /**
     * Listener funciton for when the cancel button is pressed by the user.
     */
    cancelBtnListener: function() {
        $('model_popup').remove();
        $('model_popup_cover').remove();
        this.isCanceled = true;
        writeToLog('Import canceled.');
        $('model_import_btn').value = "Verify";
        $('model_import_btn').disabled = false;
    },

    /**
     * Displays an error message within the popup.
     * @param message the message to display.
     */
    displayError: function(message) {
       $('model_popup').down('h1').insert({after:'<p class="error">' + message + '</p>'}); 
    }
}
