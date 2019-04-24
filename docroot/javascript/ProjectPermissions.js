//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2013
// All Rights Reserved
//
// Author: Cindy Tipper
// Version: $Revision: 14003 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2017-03-14 10:39:37 -0400 (Tue, 14 Mar 2017) $
// $KeyWordsOff: $
//

// Constants for tab labels.
var CURRENT_PERMISSIONS = "Current Permissions";
var ACCESS_REPORT = "Access Report";

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(function() {

        jQuery("#add-new-user-field").keyup(enableAddUser);

        // Create a 'hint' for the input box
        jQuery("#add-new-user-field").hint("auto-hint");

        // Initially disabled...
        jQuery('#add-new-user-button').button('disable');

        jQuery('#perm_ar_search_by').keyup(searchAccessReport);
        jQuery('#perm_ar_search_button').click(searchAccessReport);
        // Create a 'hint' for the search box
        jQuery("#perm_ar_search_by").hint("auto-hint");

        jQuery('#perm_cp_search_by').keyup(searchCurrentPermissions);
        jQuery('#perm_cp_search_button').click(searchCurrentPermissions);
        // Create a 'hint' for the search box
        jQuery("#perm_cp_search_by").hint("auto-hint");

        jQuery('#perm_ar_rows_per_page').change(changeArRowsPerPage);
        jQuery('#perm_cp_rows_per_page').change(changeCpRowsPerPage);

        jQuery('#perm_ar_show_admins').click(changeShowAdmins);
        jQuery('#perm_cp_show_admins').click(changeShowAdmins);
});

function enableAddUser() {
    jQuery('#add-new-user-button').removeAttr('disabled');
}
function disableAddUser() {
    jQuery('#add-new-user-button').attr('disabled', 'true');
}

function addNewUserAccess(projectId) {
    var newUser = jQuery('#add-new-user-field').val();
    var authLevel = jQuery('#add-new-user-level').val();

    var newForm = document.createElement('FORM');
    newForm.id   = "add_new_user_access_form";
    newForm.name = "add_new_user_access_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectPermissions?id=" + projectId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "addNewUserAccess";
    newInput.id  = "addNewUserAccess";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "newUser";
    newInput.type  = "hidden";
    newInput.value = newUser;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "authLevel";
    newInput.type  = "hidden";
    newInput.value = authLevel;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function requestNewUserAccess(projectId) {
    var newUser = jQuery('#add-new-user-field').val();
    var authLevel = jQuery('#add-new-user-level').val();
    var isAdmin = jQuery('input#adminUserFlag').val() == "true" ? true : false;

    // If user is DataShop Admin, use AccessRequest->newAccessRow functionality.
    if (isAdmin) {
        new Ajax.Request("AccessRequests", {
	    parameters : {
            actualRequestingMethod : "ProjectPermissions.requestNewUserAccess",
	        requestingMethod : "newAccessRow",
	        arProjectId : projectId,
                arRequestor : newUser,
	        arLevel : authLevel,
                arCreateNewAccessRowFlag : "true",
                rowId : "ignored"
	    },
	    requestHeaders : {
		    Accept : 'application/json;charset=UTF-8'
	    },
	    onComplete : handleNewAccessRow,
	    onException : function(request, exception) {
		    throw (exception);
	    }
        });
    } else {
        new Ajax.Request("AccessRequests", {
	    parameters : {
            actualRequestingMethod : "ProjectPermissions.requestNewUserAccess",
	        requestingMethod : "submitRequest",
	        arProjectId : projectId,
	        arLevel : authLevel,
	        newUser : newUser
	    },
	    requestHeaders : {
		    Accept : 'application/json;charset=UTF-8'
	    },
	    onComplete : handleSubmitRequest,
	    onException : function(request, exception) {
		    throw (exception);
	    }
        });
    }
}

// Ajax response listener for newAccessRow
function handleNewAccessRow(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.msg == "success") {
        getResponseStatus(transport);    // in AccessRequests.js
    } else if (json.msg == "error") {
        errorPopup(json.cause);
    }

    // Clear input field and disable button.
    jQuery('#add-new-user-field').val("");
    disableAddUser();
}

// Ajax response listener for access request submission
function handleSubmitRequest(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.msg == "success") {
        voteNewUserAccess(json.projectId, json.level);
    } else if (json.msg == "error") {
        errorPopup(json.cause);
    }

    // Clear input field and disable button.
    jQuery('#add-new-user-field').val("");
    disableAddUser();
}

function voteNewUserAccess(projectId, authLevel) {
    var newUser = jQuery('#add-new-user-field').val();
    var theDp = jQuery('#project-perm-dp').val();
    var newAccessRowFlag = "false";
    if (theDp.length == 0) {
        newAccessRowFlag = "true";
    }

    new Ajax.Request("AccessRequests", {
	    parameters : {
	        requestingMethod : "respond",
	        arProjectId : projectId,
	        arRequestor : newUser,
                arLevel : authLevel,
                arCreateNewAccessRowFlag : newAccessRowFlag,
                rowId : "ignored"
	    },
	    requestHeaders : {
		    Accept : 'application/json;charset=UTF-8'
	    },
            onComplete : getResponseStatus,    // in AccessRequests.js
	    onException : function(request, exception) {
		    throw (exception);
	    }
    });
}

function viewLevelDescriptions() {

    var dialogDiv = document.getElementById('levelDescriptionsDialog');
    dialogDiv.innerHTML = "<table id='auth-level-desc-table'><tbody>"
        + "<tr><th>Permission</th><th>Admin</th><th>Edit</th><th>View</th></tr>"
        + "<tr><td>View existing samples</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "</tr>"
        + "<tr><td>Export data, use analysis tools, and download files</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "</tr>"
        + "<tr><td>Use data in workflows</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "</tr>"
        + "<tr><td>Create samples</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "</tr>"
        + "<tr><td>Add knowledge component models</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "</tr>"
        + "<tr><td>Add papers, external analyses, and other types of files</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "</tr>"
        + "<tr><td>Add custom fields</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "</tr>"
        + "<tr><td>Create KC sets</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "</tr>"
        + "<tr><td>Create new datasets from existing samples</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "</tr>"
        + "<tr><td>Edit dataset and project meta information</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "</tr>"
        + "<tr><td>Add/remove datasets to/from this project</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "</tr>"
        + "<tr><td>Rename datasets in this project</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "</tr>"
        + "<tr><td>Manage project access</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "</tr>"
        + "<tr><td>Manage IRB documentation</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "</tr>"
        + "<tr><td>Submit project for a review of its shareability</td>"
        + "<td><img src=\"images/tick.png\" alt=\"yes\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "<td><img src=\"images/cross.png\" alt=\"no\" /></td>"
        + "</tr>"
        + "</tbody></table>";

    jQuery('#levelDescriptionsDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 550,
        height : 400,
        title : "Level Descriptions Table"
    });

    jQuery('#levelDescriptionsDialog').dialog('open');
}

function showCurrentPermissions(projectId) {
    jQuery('#currentPermissionsDiv').show();
    jQuery('#accessReportDiv').hide();
    jQuery('#currentPermissionsLink').css("text-decoration", "none");
    jQuery('#accessReportLink').css("text-decoration", "underline");

    setCurrentTab(projectId, CURRENT_PERMISSIONS);
}
function showAccessReport(projectId) {
    jQuery('#accessReportDiv').show();
    jQuery('#currentPermissionsDiv').hide();
    jQuery('#accessReportLink').css("text-decoration", "none");
    jQuery('#currentPermissionsLink').css("text-decoration", "underline");

    setCurrentTab(projectId, ACCESS_REPORT);
}
function setCurrentTab(projectId, tabName) {
    jQuery('#perm-current-tab').val(tabName);
    new Ajax.Request("ProjectPermissions", {
        parameters: {
            requestingMethod: "setCurrentTab",
            id: projectId,
            currentTab: tabName
        },
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function sortRequestsForAccess(projectId, sortByKey) {

    var newForm = document.createElement('FORM');
    newForm.id = "sort_project_perm_form";
    newForm.name = "sort_project_perm_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectPermissions?id=" + projectId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "id";
    newInput.type  = "hidden";
    newInput.value = projectId;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "sortRequestsForAccess";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "sortBy";
    newInput.type  = "hidden";
    newInput.value = sortByKey;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}
function sortAccessReport(projectId, sortByKey) {

    var newForm = document.createElement('FORM');
    newForm.id = "sort_project_perm_form";
    newForm.name = "sort_project_perm_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectPermissions?id=" + projectId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "id";
    newInput.type  = "hidden";
    newInput.value = projectId;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "sortAccessReport";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "sortBy";
    newInput.type  = "hidden";
    newInput.value = sortByKey;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}
function sortCurrentPermissions(projectId, sortByKey) {

    var newForm = document.createElement('FORM');
    newForm.id = "sort_project_perm_form";
    newForm.name = "sort_project_perm_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectPermissions?id=" + projectId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "id";
    newInput.type  = "hidden";
    newInput.value = projectId;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "sortCurrentPermissions";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "sortBy";
    newInput.type  = "hidden";
    newInput.value = sortByKey;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}
function searchAccessReport(event) {

    var key = "";
    if (this.id == 'perm_ar_search_by') {
        key = event.keyCode || event.which;
    }
    var testEvent = (key === 13 & this.id == 'perm_ar_search_by')
        | this.id == 'perm_ar_search_button';
    if (testEvent) {
        var searchString = jQuery("#perm_ar_search_by").val();
        if (searchString == jQuery("#perm_ar_search_by").attr("title")) {
            searchString = "";
        }
        var projectId = jQuery("#perm_project_id").val();
        jQuery(
               '<form id="searchStringForm" method="post" action="ProjectPermissions?id=' + projectId
               + '"><input name="searchAccessReport" type="hidden" value="true"/> '
               + '<input name="searchBy" type="hidden" value="' + searchString
               + '"/></form>').appendTo('body').submit();
    }
    return false;
}
function searchCurrentPermissions(event) {

    var key = "";
    if (this.id == 'perm_cp_search_by') {
        key = event.keyCode || event.which;
    }
    var testEvent = (key === 13 & this.id == 'perm_cp_search_by')
        | this.id == 'perm_cp_search_button';
    if (testEvent) {
        var searchString = jQuery("#perm_cp_search_by").val();
        if (searchString == jQuery("#perm_cp_search_by").attr("title")) {
            searchString = "";
        }
        var projectId = jQuery("#perm_project_id").val();
        jQuery(
               '<form id="searchStringForm" method="post" action="ProjectPermissions?id=' + projectId
               + '"><input name="searchCurrentPermissions" type="hidden" value="true"/> '
               + '<input name="searchBy" type="hidden" value="' + searchString
               + '"/></form>').appendTo('body').submit();
    }
    return false;
}
function changeArRowsPerPage(event) {
    var numRows = jQuery(this).val();
    var projectId = jQuery("#perm_project_id").val();
    jQuery(
           '<form id="rowsPerPageForm" method="post" action="ProjectPermissions?id=' + projectId
           + '"><input name="rowsPerPageAccessReport" type="hidden" value="true"/> '
           + '<input name="rowsPerPage" type="hidden" value="' + numRows
           + '"/></form>').appendTo('body').submit();
}
function changeCpRowsPerPage(event) {
    var numRows = jQuery(this).val();
    var projectId = jQuery("#perm_project_id").val();
    jQuery(
           '<form id="rowsPerPageForm" method="post" action="ProjectPermissions?id=' + projectId
           + '"><input name="rowsPerPageCurrentPermissions" type="hidden" value="true"/> '
           + '<input name="rowsPerPage" type="hidden" value="' + numRows
           + '"/></form>').appendTo('body').submit();
}
function gotoAccessReport(pageNum) {
    var projectId = jQuery("#perm_project_id").val();
    jQuery(
           '<form id="changePageForm" method="post" action="ProjectPermissions?id=' + projectId
           + '"><input name="currentPageAccessReport" type="hidden" value="true"/> '
           + '<input name="currentPage" type="hidden" value="' + pageNum
           + '"/></form>').appendTo('body').submit();
}
function gotoCurrentPermissions(pageNum) {
    var projectId = jQuery("#perm_project_id").val();
    jQuery(
           '<form id="changePageForm" method="post" action="ProjectPermissions?id=' + projectId
           + '"><input name="currentPageCurrentPermissions" type="hidden" value="true"/> '
           + '<input name="currentPage" type="hidden" value="' + pageNum
           + '"/></form>').appendTo('body').submit();
}
function changeShowAdmins(event) {
    var showAdmins = this.checked;
    var projectId = jQuery("#perm_project_id").val();
    jQuery(
           '<form id="showAdminsForm" method="post" action="ProjectPermissions?id=' + projectId
           + '"><input name="showAdmins" type="hidden" value="' + showAdmins
           + '"/></form>').appendTo('body').submit();
}
function updatePermissionsPage(transport) {
    // Need to update Permissions-specific session attrs following
    // changes handled by AccessRequests. Form submission will also
    // take care of reloading the page with new content.
    var json = transport.responseText.evalJSON(true);

    // Determine if update is because new user was added but must be voted on.
    if (json.rowId == "ignored") {
        var message = "";
        var messageLevel = "";
        if (json.msg == "success") {
            var otherParty = "";
            if ((json.role == 'pi') && (json.dp.length != 0)) {
                otherParty = 'data provider';
            } else if ((json.role == 'dp') && (json.pi.length != 0)) {
                otherParty = 'PI';
            }
            if (otherParty.length > 0) {
                message = "New user added, but the " + otherParty +
                    " must also approve this change.";
            } else {
                message = "New user successfully added.";
            }
            messageLevel = "SUCCESS";
        } else {
            message = json.cause;
            messageLevel = "ERROR";
        }

        var newForm = document.createElement('FORM');
        newForm.id   = "update_session_form";
        newForm.name = "update_session_form";
        newForm.form = "text/plain";
        newForm.action = "ProjectPermissions?id=" + projectId;
        newForm.method = "post";
        
        var newInput = document.createElement('input');
        newInput.name  = "updateSession";
        newInput.id  = "updateSessions";
        newInput.type  = "hidden";
        newInput.value = "true";
        newForm.appendChild(newInput);
        
        newInput = document.createElement('input');
        newInput.name  = "message";
        newInput.type  = "hidden";
        newInput.value = message;
        newForm.appendChild(newInput);
        
        newInput = document.createElement('input');
        newInput.name  = "messageLevel";
        newInput.type  = "hidden";
        newInput.value = messageLevel;
        newForm.appendChild(newInput);
        
        document.getElementsByTagName('body').item(0).appendChild(newForm);
        newForm.submit();
    } else {
        // If user was responding to a request, all we need is a refresh.
        refreshPermissionsPage();
    }
}
function refreshPermissionsPage() {
    // Reload page to update fields.
    window.location.assign(window.location.href);
}
