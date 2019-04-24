//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2013
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 10435 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

//
// Initialize some things for the Admin - Manage Users List page
//
function initAdminUserListPage() {
    // Set up date chooser tool
    jQuery('#createdFrom').datepicker();
}

//
// Clear the fields
//
function clearForm() {
    jQuery('#UserQueryForm  input[type="text"]').val("");
    jQuery('#UserQueryForm  input[type="checkbox"]').attr('checked', false);
    jQuery('#adminFlag_all').attr('selected', 'selected');
}