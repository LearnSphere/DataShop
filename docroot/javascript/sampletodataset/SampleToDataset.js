//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2014
// All Rights Reserved
//
// Author: Mike Komisin
// Version: $Revision:  $
// Last modified by: $Author:  $
// Last modified on: $Date:  $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

jQuery.fn.ignore = function(sel){
      return this.clone().find(sel).remove().end();
    };

// jQuery's Onload function
jQuery(document).ready(
        function() {

          // Make the radioLinks into a jQuery buttonset
          jQuery("#s2d-submit").click(saveSampleAsDataset);
          jQuery('input#dataset-name').keyup(checkDatasetNameExists);
          jQuery('input#dataset-name').attr('autocomplete', 'off');
          initTruncatorFields(jQuery('#s2d_sample').val());
          checkDatasetNameExists();
        });

function checkDatasetNameExists() {
    var dsName =  jQuery("input#dataset-name").val();
    var sampleId = jQuery('#s2d_sample').val();
    new Ajax.Request(window.location.href, {
        parameters: {
            ajaxRequest: "true",
            requestingMethod: "checkDatasetNameExists",
            datasetName: dsName,
            s2d_sample: sampleId,
            checkDatasetName: "true"
        },
        onComplete: updateDatasetNameInput,
        onException: function (request, exception) { throw(exception); }
    });

}

function updateDatasetNameInput(transport) {
      var json = transport.responseText.evalJSON(true);

      if (jQuery("input#dataset-name").val() == undefined
              || jQuery.trim(jQuery("input#dataset-name").val()).length == 0) {

          jQuery("p#nameExists").text("Please enter a valid name");
          jQuery("#s2d-submit").attr("disabled", true);
      } else if (json.datasetNameExists == "false") {

          jQuery("p#nameExists").text("");
          jQuery("#s2d-submit").removeAttr("disabled");
      } else if (json.datasetNameExists == "true") {

          jQuery("p#nameExists").text("Dataset Name Exists");
          jQuery("#s2d-submit").attr("disabled", true);
      } else {

          jQuery("p#nameExists").text("Invalid Name");
          jQuery("#s2d-submit").attr("disabled", true);
      }
}

//
// Export functions
//
var progressBar = false;
function saveSampleAsDataset() {
    var sampleId = jQuery('#s2d_sample').val();
    var dsName = jQuery("#dataset-name").val();
    var dsDesc = jQuery("#dataset-desc").ignore('span').text();
    var myRadio = jQuery('input[name=includeKCMs]');
    var includeKCMs = myRadio.filter(':checked').val();

    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "SampleToDataset.startExport",
            s2d_sample: sampleId,
            s2d_save_action: "true",
            s2d_ds_name: dsName,
            s2d_ds_desc: dsDesc,
            tx_export: "true",
            includeKCMs: includeKCMs,
            export_start: "true" },
        onComplete: s2dDisplayProgressBar,
        onException: function (request, exception) { throw(exception); }
    });

}


function s2dDisplayProgressBar(transport) {
    var sampleId = jQuery('#s2d_sample').val();
    var dsName = jQuery("#dataset-name").val();
    var dsDesc = jQuery("#dataset-desc").ignore('span').text();
    var myRadio = jQuery('input[name=includeKCMs]');
    var includeKCMs = myRadio.filter(':checked').val();

    var json = null;
    if (transport !== undefined) {
        json = transport.responseText.evalJSON(true);
    }

    if (json != null && json.redirectTo != null) {
        window.location.href = json.redirectTo;

    } else if (json != null && json.status == "error") {
        errorPopup(json.message);
    } else {

        window.setTimeout(function() {
        new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "SampleToDataset.startExport",
            s2d_sample: sampleId,
            s2d_save_action: "true",
            s2d_ds_name: dsName,
            s2d_ds_desc: dsDesc,
            tx_export: "true",
            includeKCMs: includeKCMs,
            export_check: "true" },
            onComplete: s2dDisplayProgressBar,
            onException: function (request, exception) { throw(exception); }
        });
        }, 1500);


        if (!progressBar) {
            var extraParamsArray = {};
            extraParamsArray['s2d_sample'] = sampleId;
            extraParamsArray['s2d_ds_name'] = dsName;
            extraParamsArray['s2d_ds_desc'] = dsDesc;
            var options = { extraParams: extraParamsArray };
            progressBar = new ProgressBar(s2dDisplayProgressBar, window.location.href, options);
        }
    }
}


// Initialize the fields to be truncated
function initTruncatorFields(sampleId) {

    options = new Array();
    options.numLines = 7;

    if ($("dataset-desc")) {
       new Truncator($('dataset-desc'), options);
    }

    truncatorOptionsArray = new Array();
    truncatorOptionsArray.maxLength = 75;
    truncatorOptionsArray.numLines = 4;

    //init the editable text fields

    var options = new Array();
    options.maxLength = 65000;
    options.truncatorOptions = truncatorOptionsArray;

    if ($("dataset-desc")) {
        new InlineEditor($('dataset-desc'), 'textarea', 'SampleToDataset?s2d_sample=' + sampleId, options);
    }

}

