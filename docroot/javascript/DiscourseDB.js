//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2015
// All Rights Reserved
//
// Author: Cindy Tipper
// Version: $Revision: 13128 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-04-19 12:46:09 -0400 (Tue, 19 Apr 2016) $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(function() {
});

var discourseId;

function initializeDiscourseId() {
    discourseId = jQuery('#discourseId').val();
}

function importDiscourse() {
    new Ajax.Request("DiscourseInfo", {
        parameters: {
            requestingMethod: "DiscourseDB.importDiscourse",
            discourseId: "1",
            ajaxRequest: "true",
            importDiscourse: "true"
        },
        onComplete: discourseImported,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function discourseImported(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        successPopup(json.message);
    } else {
        errorPopup(json.message);
    }
}

function exportDiscourse(discourseId) {
    var newForm = document.createElement('FORM');
    newForm.id   = "export_discourse_form";
    newForm.name = "export_discourse_form";
    newForm.form = "text/plain";
    newForm.action = "DiscourseExport";
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "discourseDbAction";
    newInput.type  = "hidden";
    newInput.value = "ExportDiscourse";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "discourseId";
    newInput.type  = "hidden";
    newInput.value = discourseId;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

