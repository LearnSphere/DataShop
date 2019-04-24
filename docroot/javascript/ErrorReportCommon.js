//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2015
// All Rights Reserved
//
// Author: Cindy Tipper
// Version: $Revision: 12835 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-12-14 13:17:49 -0500 (Mon, 14 Dec 2015) $
// $KeyWordsOff: $
//

// Jump to 'Error Report' page, with correct problem selected.
function errorReport(problemId) {
    new Ajax.Request(window.location.href, {
           parameters: {
                 requestingMethod: "ProblemContent.errorReport",
                 datasetId: dataset,
                 ajaxRequest: "true",
                 navigationUpdate: "true",
                 list: "problems",
                 action: "select",
                 itemId: problemId,
                 multiSelect: "false"
               },
           requestHeaders: {Accept: 'application/json;charset=UTF-8'},
           onComplete: updateViewBy,
           onException: function (request, exception) {
               throw(exception);
           }
    });
}

function updateViewBy(transport) {
    new Ajax.Request("ErrorReport", {
            parameters: {
                requestingMethod: "ErrorReportCommon.updateViewBy",
                datasetId: dataset,
                error_report_view_by: "problem"
            },
            onComplete: gotoErrorReport,
            onException: function (request, exception) {
                throw(exception);
            }
    });
}

function gotoErrorReport(transport) {
    window.location.href = "ErrorReport?datasetId=" + dataset;
}

