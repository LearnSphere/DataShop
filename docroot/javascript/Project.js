//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2009
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 12986 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-03-16 12:38:01 -0400 (Wed, 16 Mar 2016) $
// $KeyWordsOff: $
//

var TRY_ME_OUT_DATASETS_COOKIE  =  "ds_show_try_me_out_datasets_flag";
var TRY_ME_OUT_DATASETS_NEITHER =  "neither";
var TRY_ME_OUT_DATASETS_OPEN    =  "opened";
var TRY_ME_OUT_DATASETS_CLOSED  =  "closed";

//
// Show the try-me-out-datasets box.
//
function showTryMeOutDatasets() {
    if ($('try-me-out-datasets')) {
        $('try-me-out-datasets').hide();
    }
    if ($('show-try-me-out-datasets')) {
        $('show-try-me-out-datasets').show();
    }
    saveTryMeOutDatasetsState(TRY_ME_OUT_DATASETS_OPEN);
}

//
// Hide the try-me-out-datasets box and show the link only.
//
function hideTryMeOutDatasets() {
    if ($('try-me-out-datasets')) {
        $('try-me-out-datasets').show();
    }
    if ($('show-try-me-out-datasets')) {
        $('show-try-me-out-datasets').hide();
    }
    saveTryMeOutDatasetsState(TRY_ME_OUT_DATASETS_CLOSED);
}

//
// Save the state of the try-me-out-datasets box to a cookie.
//
function saveTryMeOutDatasetsState(value) {
    createCookie(this.TRY_ME_OUT_DATASETS_COOKIE, value, 100);
}

//
// Get the state of the try-me-out-datasets box from a cookie.
//
function getTryMeOutDatasetsState() {
    var state = readCookie(this.TRY_ME_OUT_DATASETS_COOKIE);
    if (!state) {
        // default if not cookie found
        state = TRY_ME_OUT_DATASETS_NEITHER;
    }
    return state;
}

//
// Initialize the state of the try-me-out-datasets box if the user has changed it already.
//
function initTryMeOutDatasetsState() {
   var state = getTryMeOutDatasetsState();
   if (state == TRY_ME_OUT_DATASETS_CLOSED) {
       hideTryMeOutDatasets();
   } else if (state == TRY_ME_OUT_DATASETS_OPEN) {
       showTryMeOutDatasets();
   }
   // else do not change current state
}

//
// Constants for the Project Announcements feature.
//
var PROJECT_ANNOUNCEMENTS_COOKIE  = "ds_project_announcements_flag";
var PROJECT_ANNOUNCEMENTS_NEITHER = "neither";
var PROJECT_ANNOUNCEMENTS_OPEN    = "opened";
var PROJECT_ANNOUNCEMENTS_CLOSED  = "closed";
var PROJECT_ANNOUNCEMENTS_DAYS    = 1;

//
// Show the project-announcements box.
//
function showProjectAnnouncements() {
    if ($('project-announcements')) {
        $('project-announcements').hide();
    }
    if ($('show-project-announcements')) {
        $('show-project-announcements').show();
    }
    saveProjectAnnouncementsState(PROJECT_ANNOUNCEMENTS_OPEN);
}

//
// Hide the project-announcements box and show the link only.
//
function hideProjectAnnouncements() {
    if ($('project-announcements')) {
        $('project-announcements').show();
    }
    if ($('show-project-announcements')) {
        $('show-project-announcements').hide();
    }
    saveProjectAnnouncementsState(PROJECT_ANNOUNCEMENTS_CLOSED);
}

//
// Save the state of the project-announcements box to a cookie.
//
function saveProjectAnnouncementsState(value) {
    createCookie(this.PROJECT_ANNOUNCEMENTS_COOKIE, value, PROJECT_ANNOUNCEMENTS_DAYS);
}

//
// Get the state of the project-announcements box from a cookie.
//
function getProjectAnnouncementsState() {
    var state = readCookie(this.PROJECT_ANNOUNCEMENTS_COOKIE);
    if (!state) {
        // default if not cookie found
        state = PROJECT_ANNOUNCEMENTS_NEITHER;
    }
    return state;
}

//
// Initialize the state of the project-announcements box if the user has changed it already.
//
function initProjectAnnouncementsState() {
   var state = getProjectAnnouncementsState();
   if (state == PROJECT_ANNOUNCEMENTS_CLOSED) {
       hideProjectAnnouncements();
   } else if (state == PROJECT_ANNOUNCEMENTS_OPEN) {
       showProjectAnnouncements();
   }
   // else do not change current state
}

