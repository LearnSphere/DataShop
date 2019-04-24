/**
 * Carnegie Mellon University, Human-Computer Interaction Institute Copyright
 * 2012 All Rights Reserved
 */

// This file contains JavaScript functions used with access_requests.jsp
// Dependencies: javascript/lib/jquery-1.7.1.min.js,
// javascript/lib/jquery-ui-1.8.17.custom.min.js
// Maximum character length for the reason textarea
var MaxLen = 255;

var viewText =
  "view access (ability to view analyses, use tools, and export data)";
var editText =
  "edit access (view access plus the ability to add files, add KC models, and create samples) - <strong>recommended</strong>";
var adminText =
  "admin access (edit access plus the ability to edit project and dataset metadata, grant/change access, upload datasets, and manage IRB documentation)";

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(
        function() {

          // Make the radioLinks into a jQuery buttonset
          jQuery("#radioLinks").buttonset();
          // Bind functions to clicks
          jQuery("input:radio[name=radioLink]").click(changeSubtab);
          jQuery("input[name=ar_show_admins]").click(showAdmins);

          // Set javascript bindings for show/hide request links
          jQuery('span[class=showOlderUserRequests]').click(
                  bindShowUserRequests);
          jQuery('span[class=hideOlderUserRequests]').click(
                  bindHideUserRequests);
          jQuery('span[class=showOlderProjectRequests]').click(
                  bindShowProjectRequests);
          jQuery('span[class=hideOlderProjectRequests]').click(
                  bindHideProjectRequests);

          // Request Dialog
          jQuery('.requestDialog').dialog({
              modal : true,
              autoOpen : false,
              width : 540,
              height : 620,
              title : "Access Request",
              buttons : [ {
                        id : "request-access-button",
                        text : "Request Access",
                        click : requestAccess
                    }, {
                        id : "cancel-ra-button",
                        text : "Cancel",
                        click : closeDialog
              } ]
          });

          // Make project public dialog from project page
          jQuery('.projectPublicDialog').dialog({
              modal : true,
              autoOpen : false,
              width : 500,
              height : 300,
              title : "Make Project Public",
              buttons : [ {
                        id : "project-public-button",
                        text : "Make public",
                        click : submitProjectPublic
                    }, {
                        id : "no-button",
                        text : "Cancel",
                        click : closeDialog
              } ]
          });

          // Make project public dialog from project request table
          jQuery('.voteProjectPublicDialog').dialog({
              modal : true,
              autoOpen : false,
              width : 500,
              height : 300,
              title : "Vote to Make Project Public",
              buttons : [ {
                        id : "vote-proj-public-button",
                        text : "Yes",
                        click : voteProjectPublic
                    }, {
                        id : "deny-proj-public-button",
                        text : "No",
                        click : denyProjectPublic
                }, {
                      id : "cancel-proj-public-button",
                      text : "Cancel",
                      click : closeDialog
              } ]
          });

          // Make new access row (administrator)
          jQuery('.newAccessRowDialog').dialog({
              modal : true,
              autoOpen : false,
              width : 550,
              height : 600,
              title : "New Access Row",
              buttons : [ {
                        id : "new-access-row-button",
                        text : "Save",
                        click : newAccessRow
                    }, {
                        id : "cancel-na-button",
                        text : "Cancel",
                        click : closeDialog
              } ]
          });

          // Response Dialog
          jQuery('.responseDialog').dialog({
              modal : true,
              autoOpen : false,
              width : 540,
              height : 675,
              title : "Response",
              buttons : [ {
                        id : "respond-button",
                        text : "Respond",
                        click : respond
                    }, {
                        id : "cancel-respond-button",
                        text : "Cancel",
                        click : closeDialog
              } ]
          });

          // Assign the open dialog function to the project public links
          jQuery('.project_public_link').click(createProjectPublic);

          // Assign the open dialog function to the request links
          jQuery('.request_link').click(createRequest);

          // Assign the open dialog function to the response links
          jQuery('.response_link').click(createResponse);

          // Assign the open dialog function to the response links
          jQuery('.export_link').click(exportReport);

          // Assign the open dialog function to the new access row links
          jQuery('.newaccess_link').click(createNewAccessRow);

          // Expand and collapse history rows in the project request list
          jQuery('.collapsed').click(triggerCollapseExpand);

          // Rows per page for project request lists
          jQuery('#ar_rows_per_page').change(rowsPerPage);

          // Filter by status for project request lists
          jQuery('#ar_filter_by').change(filterBy);

          // Search for project requests by username or project name
          jQuery('#ar_search_string').keyup(searchBy);
          jQuery('#ar_search_button').click(searchBy);
          // Create a 'hint' for the search box
          jQuery("#ar_search_string").hint("auto-hint");

          // Sorts columns based on user-selected column header
          jQuery(".sortByColumn").click(selectSort);

          // hide all project request list histories on page load
          jQuery('.historyList').hide();

          // 'Request Access' is disabled until 'reason' given.
          jQuery(".ui-dialog-buttonpane button:contains('Request Access')").button('disable');

        });

// Submit user-specified sort request
function selectSort(columnName) {
  jQuery(
          '<form id="selectSortForm" method="post" action="AccessRequests">'
                  + '<input id="sortBy" name="sortBy"'
                  + ' type="hidden" value="' + this.id + '"/></form>')
  // now append jQuery-created form to body and submit
  .appendTo('body').submit();
}

/* Begin create dialogs */

// Assign parameter values and responses functions for Ajax request
function createProjectPublic() {
  new Ajax.Request("AccessRequests", {
      parameters : {
          requestingMethod : "createProjectPublic",
          makePublicButtonId : this.id
      },
      requestHeaders : {
        Accept : 'application/json;charset=UTF-8'
      },
      beforeSend : showStatusIndicator("Please wait..."),
      onSuccess : hideStatusIndicator,
      onComplete : createProjectPublicAjaxListener,
      onException : function(request, exception) {
        throw (exception);
      }
  });
  return false;
}

// Assign parameter values and response functions for Ajax request
function createNewAccessRow() {
  new Ajax.Request("AccessRequests", {
      parameters : {
        requestingMethod : "createNewAccessRow"
      },
      requestHeaders : {
        Accept : 'application/json;charset=UTF-8'
      },
      beforeSend : showStatusIndicator("Please wait..."),
      onSuccess : hideStatusIndicator,
      onComplete : createNewAccessRowAjaxListener,
      onException : function(request, exception) {
        throw (exception);
      }
  });
  return false;
}

// Assign parameter values and response functions for Ajax request
function createRequest() {
  new Ajax.Request("AccessRequests", {
      parameters : {
          requestingMethod : "createRequest",
          requestButtonId : this.id
      },
      requestHeaders : {
        Accept : 'application/json;charset=UTF-8'
      },
      beforeSend : showStatusIndicator("Please wait..."),
      onSuccess : hideStatusIndicator,
      onComplete : createRequestAjaxListener,
      onException : function(request, exception) {
        throw (exception);
      }
  });
  return false;
}

// Assign parameter values and response functions for Ajax request
function createResponse() {
    var queryStr = "input[id='" + this.id + "']";
    var currentTab = "";

    if (jQuery('#perm-current-tab').length) {
        // Invoked from 'Project Permissions' page.
        currentTab = jQuery('#perm-current-tab').val();

        // If a row in the 'Requests for Access' table...
        if (this.className.indexOf('request-access') > 0) {
            currentTab = 'request-access';
        }
    } else if (jQuery('#ar-current-tab').length) {
        // Invoked from 'Access Requests' page.
        currentTab = jQuery('#ar-current-tab').val();
    }

    if (currentTab != "") {
        queryStr = queryStr.concat("[class='" + currentTab + "']");
    }
    var requestUserId = jQuery(queryStr + "[name='userId']").val();
    var projectId = jQuery(queryStr + "[name='projectId']").val();
  new Ajax.Request("AccessRequests", {
      parameters : {
          requestingMethod : "createResponse",
          requestUserId : requestUserId,
          projectId : projectId,
          rowId : this.id
      },
      requestHeaders : {
        Accept : 'application/json;charset=UTF-8'
      },
      beforeSend : showStatusIndicator("Please wait..."),
      onSuccess : hideStatusIndicator,
      onComplete : createResponseAjaxListener,
      onException : function(request, exception) {
        throw (exception);
      }
  });
  return false;
}

/* End create dialogs */

/* Begin Ajax Listeners */

// Create the project public modal window if Ajax request successful
function createProjectPublicAjaxListener(transport) {
  var json = transport.responseText.evalJSON(true);
  if (json.msg == "success") {
    openProjectPublicDialog(json);
  } else if (json.msg == "error") {
    showErrorDialog(json.cause);
  }
}

// Create the project public modal window if Ajax request successful
function createNewAccessRowAjaxListener(transport) {
  var json = transport.responseText.evalJSON(true);
  if (json.msg == "success") {
    openNewAccessRowDialog(json);
  } else if (json.msg == "error") {
    showErrorDialog(json.cause);
  }
}

// Create the request modal window if Ajax request successful
function createRequestAjaxListener(transport) {

  var json = transport.responseText.evalJSON(true);
  if (json.msg == "success") {
    openRequestDialog(json);
  } else if (json.msg == "error") {
    showErrorDialog(json.cause);
  }
}

// Create the response modal window if Ajax request successful
function createResponseAjaxListener(transport) {
  var json = transport.responseText.evalJSON(true);
  if (json.msg == "success") {
    if (json.isMakePublic == true) {
      voteProjectPublicDialog(json);
    } else {
      openResponseDialog(json);
    }
  } else if (json.msg == "error") {
    showErrorDialog(json.cause);
  }
}

/* End Ajax Listeners */

/* Begin Dialog definitions */

// Opens a modal window populated by the previous request, if one exists
function openProjectPublicDialog(json) {
  // Get the make project public dialog div from the page
  var div = document.getElementById('projectPublicDialog');
  div.innerHTML = "";
  var otherParty = json.otherParty;

  var input = document.createElement('input');
  input.id = "modalWinProjectId";
  input.name = "modalWinProjectId";
  input.type = "hidden";
  input.value = json.projectId;
  div.appendChild(input);

  var p = document.createElement('p');
  p.id = "headerLabel";
  p.className = "boldface";
  p.innerHTML = "Are you sure you want to ";
        if (otherParty != null) {
            p.innerHTML += "vote to ";
        }
        p.innerHTML += "make this project public?";
  div.appendChild(p);

  var p = document.createElement('p');
  p.id = "noteLabel";
  p.className = "boldface";
  p.innerHTML = "Note:";
  div.appendChild(p);

  var p = document.createElement('p');
  p.id = "notes";
  var infoString = "<ul><li>a public project can be accessed by anyone who is logged into DataShop.</li>"
          + "<li>once you make a project public, you can only make it private again by contacting DataShop staff.</li>";
  if (otherParty != null) {
    infoString = infoString
            + "<li>the "
            + otherParty
            + " must also agree to make this project public for this change to take effect.</li>";
  }

  p.innerHTML = infoString + "</ul>";
  div.appendChild(p);

        // Change button text if voting...
        if (otherParty != null) {
            jQuery('#project-public-button').html("<span class='ui-button-text'>Vote to make public</span>");
        }

  jQuery('#projectPublicDialog').dialog('open');
}

// Opens a modal window populated by the previous request, if one exists
function voteProjectPublicDialog(json) {

  // Get the make project public dialog div from the page
  var div = document.getElementById('voteProjectPublicDialog');
  div.innerHTML = "";
  var otherParty = json.otherParty;

  var input = document.createElement('input');
  input.id = "modalWinProjectId";
  input.name = "modalWinProjectId";
  input.type = "hidden";
  input.value = json.projectId;
  div.appendChild(input);

  var input = document.createElement('input');
  input.id = "modalWinUserId";
  input.name = "modalWinUserId";
  input.type = "hidden";
  input.value = "%";
  div.appendChild(input);

  var input = document.createElement('input');
  input.id = "rowId";
  input.name = "rowId";
  input.type = "hidden";
  input.value = json.rowId;
  div.appendChild(input);

  var p = document.createElement('p');
  p.id = "edit_only";
  p.innerHTML = '<input type="radio" name="modalWinLevelGroup" value="edit" checked>';
  div.appendChild(p);
  jQuery("#edit_only").hide();

  var p = document.createElement('p');
  p.id = "headerLabel";
  p.className = "boldface";
  p.innerHTML = "Select 'Yes' to make the project public or 'No' to keep it private.";
  div.appendChild(p);

  var p = document.createElement('p');
  p.id = "noteLabel";
  p.className = "boldface";
  p.innerHTML = "Note:";
  div.appendChild(p);

  var p = document.createElement('p');
  p.id = "notes";
  var infoString = "<ul><li>a public project can be accessed by anyone who is logged into DataShop.</li>"
          + "<li>once you make a project public, you can only make it private again by contacting DataShop staff.</li>";
  if (otherParty != null) {
    infoString = infoString
            + "<li>the "
            + otherParty
            + " must also agree to make this project public for this change to take effect.</li>";
  }

  p.innerHTML = infoString + "</ul>";
  div.appendChild(p);

  jQuery('#voteProjectPublicDialog').dialog('open');
}

function disableViewEdit() {
  if (jQuery("#selectWinUserId option:selected").val() == "%") {
    jQuery('#new_admin_access').attr('disabled', true);
    jQuery('#new_admin_access_label').addClass('disabledLabel');
    jQuery('input:radio[name=modalWinLevelGroup][value=view]').click();
  } else {
    jQuery('#new_admin_access').attr('disabled', false);
    jQuery('#new_admin_access_label').removeClass('disabledLabel');
    jQuery('input:radio[name=modalWinLevelGroup][value=view]').click();
  }
}

// Opens a modal window populated users and projects for administrator
function openNewAccessRowDialog(json) {

  var userList = json.userList;
  var projectList = json.projectList;

  // Get the make project public dialog div from the page
  var newAccessDiv = document.getElementById('newAccessRowDialog');
  newAccessDiv.innerHTML = "";

  var p = document.createElement('p');
  p.className = "boldface";
  p.innerHTML = "User: ";
  newAccessDiv.appendChild(p);

  var selectUser = document.createElement('select');
  selectUser.id = "selectWinUserId";
  selectUser.name = "selectWinUserId";
  newAccessDiv.appendChild(selectUser);

  var keys = [];
  // Sort the hash map of Users returned in JSON
  for ( var key in userList) {
    if (userList.hasOwnProperty(key)) {
      keys.push(key);
    }
  }
  keys.sort();
  for (i in keys) {
    var key = keys[i];
    var value = userList[key];
    if (value != undefined && value != null) {
      var option = document.createElement('option');
      option.value = value;
      option.appendChild(document.createTextNode(key));
      selectUser.appendChild(option);
    }
  }

  var p = document.createElement('p');
  p.className = "boldface";
  p.innerHTML = "Project: ";
  newAccessDiv.appendChild(p);

  var selectProject = document.createElement('select');
  selectProject.id = "selectWinProjectId";
  selectProject.name = "selectWinProjectId";
  newAccessDiv.appendChild(selectProject);

  var keys = [];
  // Sort the hash map of Projects returned in JSON
  for ( var key in projectList) {
    if (projectList.hasOwnProperty(key)) {
      keys.push(key);
    }
  }
  keys.sort();
  for (i in keys) {
    var key = keys[i];
    var value = projectList[key];
    if (value != undefined && value != null) {
      var option = document.createElement('option');
      option.value = value;
      option.appendChild(document.createTextNode(key));
      selectProject.appendChild(option);
    }
  }

  var p = document.createElement('p');
  p.className = "boldface";
  p.innerHTML = "Access type: ";
  newAccessDiv.appendChild(p);

  var p = document.createElement('p');
    p.innerHTML = '<table>'
     + '<tr><td><input type="radio" name="modalWinLevelGroup" id="new_view_access" value="view" checked /></td>'
     + '<td><label for="new_view_access">Allow ' + viewText + '</label></td></tr>'

     + '<tr><td><input type="radio" name="modalWinLevelGroup" id="new_edit_access" value="edit" /></td>'
     + '<td><label for="new_edit_access">Allow ' + editText + '</label></td></tr>'

     + '<tr><td><input type="radio" name="modalWinLevelGroup" id="new_admin_access" value="admin" /></td>'
     + '<td><label id="new_admin_access_label" for="new_admin_access">Allow ' + adminText + '</label></td></tr>'

     + '<tr><td><input type="radio" name="modalWinLevelGroup" id="new_deny_access" value="deny" /></td>'
     + '<td><label for="new_deny_access">Deny access</label></td></tr>'
     + '<table>';
  newAccessDiv.appendChild(p);

  var p = document.createElement('p');
  p.innerHTML = "<strong>Your Reason</strong> &nbsp;&nbsp;&nbsp;&nbsp;"
    + "<input type=\"checkbox\" name=\"shareReasonFlag\" id=\"shareReasonFlag\" checked>"
    + "Share this reason with the requester."
  newAccessDiv.appendChild(p);

  var d = document.createElement('div');
  d.className = "textAreaDiv";

  var textarea = document.createElement('textarea');
  textarea.name = "NewAccessRowReason";
  textarea.id = "NewAccessRowReason";
  textarea.rows = "5";
  textarea.cols = "60";
  textarea.value = "";
  d.appendChild(textarea);

  var div = document.createElement('div');
  div.className = "charsLeftNewAccessRow";
  div.id = "charsLeftNewAccessRow";
  var charsLeft = MaxLen - textarea.value.length;
  div.innerHTML = charsLeft + " characters remaining";
  d.appendChild(div);
  newAccessDiv.appendChild(d);

  // Disable Edit and Deny when % (Public Access) is the selected user
  jQuery("#selectWinUserId").change(disableViewEdit);
  disableViewEdit();
  // Attach key up listener to reason text area
  jQuery("textarea#NewAccessRowReason").keyup(textareaModNewAccessRow);
  jQuery('#newAccessRowDialog').dialog('open');

}

// Opens a modal window populated by the previous request, if one exists
function openRequestDialog(json) {
  // Get the request dialog div from the page
  var requestDiv = document.getElementById('requestDialog');
  requestDiv.innerHTML = "";

  // Create form elements to add to the div
  var p = document.createElement('p');
  p.id = "projectLabel";
  p.innerHTML = "<span class='boldface'>Project:</span> " + json.project;
  requestDiv.appendChild(p);

  var input = document.createElement('input');
  input.id = "modalWinProjectId";
  input.name = "modalWinProjectId";
  input.type = "hidden";
  input.value = json.projectId;
  requestDiv.appendChild(input);

  var displayDp = true;
  if ((json.pi != null && json.dp != null && json.pi == json.dp)) {
    displayDp = false;
  }

  if (json.pi != null && json.pi != "") {
    var p = document.createElement('p');
    p.id = "piLabel";
    p.innerHTML = "<span class='boldface'>PI:</span> " + json.pi;
    requestDiv.appendChild(p);
  }

  if (json.dp != null && json.dp != "" && displayDp) {
    var p = document.createElement('p');
    p.id = "dpLabel";
    p.innerHTML = "<span class='boldface'>Data Provider:</span> " + json.dp;
    requestDiv.appendChild(p);
  }

  if (json.touName != null && json.version != null && json.terms != null) {
    var p = document.createElement('p');
    p.id = "termsLabel";
    p.innerHTML = "<span class='boldface'>By requesting access to this data, you agree to " +
        "the following terms of use.</span>";
    requestDiv.appendChild(p);

    var effectiveText = "<p><em>Effective " + json.termsEffective + "</em></p>";
    var termsDiv = document.createElement('div');
    termsDiv.name = "terms";
    termsDiv.className = "termsDiv";
    termsDiv.id = "termsDiv";

    termsDiv.innerHTML = effectiveText + json.terms;
    requestDiv.appendChild(termsDiv);

  }

  var p = document.createElement('p');
  p.id = "levelLabel";
  p.innerHTML = "<span class='boldface'>I request:</span>"
  requestDiv.appendChild(p);

  // Default is always 'edit'
  var viewChecked = "";
  var editChecked = "checked";

  var t = document.createElement('div');

  t.innerHTML = '<table><tr><td><input type="radio" name="modalWinLevelGroup" id="request_view_access" value="view" '
          + viewChecked
          + '></td>'
          + '<td><label for="request_view_access">' + viewText + '</label></td></tr>'
          + '<tr><td><input type="radio" name="modalWinLevelGroup" id="request_edit_access" value="edit" '
          + editChecked
          + '></td>'
          + '<td><label for="request_edit_access">' + editText + '</label></td></tr></table>';
  requestDiv.appendChild(t);

  var p = document.createElement('p');
  var requestRecipient = "the PI";
  if (json.ownership == 'pi_ne_dp') {
    requestRecipient = "the PI and data provider";
  } else if (json.ownership == 'pi_eq_dp' || json.ownership == 'pi_only') {
    requestRecipient = "the PI";
  } else if (json.ownership == 'dp_only') {
    requestRecipient = "the data provider";
  } else if (json.ownership == 'none') {
    requestRecipient = "DataShop staff";
  }
  p.innerHTML = "Reason (will be sent to " + requestRecipient
          + "):"
  requestDiv.appendChild(p);

  var d = document.createElement('div');
  d.className = "textAreaDiv";

  var textarea = document.createElement('textarea');
  textarea.name = "RequestReason";
  textarea.id = "RequestReason";
  textarea.rows = "5";
  textarea.cols = "60";
  textarea.value = json.reason;
  d.appendChild(textarea);

  var div = document.createElement('div');
  div.className = "charsLeftRequest";
  div.id = "charsLeftRequest";
  var charsLeft = MaxLen - textarea.value.length;
  div.innerHTML = charsLeft + " characters remaining";
  d.appendChild(div);
  requestDiv.appendChild(d);

  var p = document.createElement('p');
  p.className = "italicized";
  p.innerHTML = "By sending this request, your email address will be shown to the "
          + requestRecipient
          + "."
          + " They may contact you with any follow-up questions."
  requestDiv.appendChild(p);

  jQuery("textarea#RequestReason").keyup(textareaModRequest);
  jQuery('#requestDialog').dialog('open');
  // enable the request access button if the textarea text satisfies the constraints
  textareaModRequest();
}

// Opens a modal window populated by the previous request, if one exists
function openResponseDialog(json) {
  // Get the response dialog div from the page
  var responseDiv = document.getElementById('responseDialog');
  responseDiv.innerHTML = "";

  // Determine if this is an attempt to change an existing level of access.
  var isChangeRequest = false;
  if (json.isChangeRequest == true) {
      isChangeRequest = true;
      jQuery('#responseDialog').dialog('option', 'title', "Access Change Request");
  }

  var isChangeResponse = false;
  if (json.isChangeResponse == true) {
      isChangeResponse = true;
      jQuery('#responseDialog').dialog('option', 'title', "Change Response");
  }

  // Create form elements to add to the div
  var input = document.createElement('input');
  input.id = "modalWinProjectId";
  input.name = "modalWinProjectId";
  input.type = "hidden";
  input.value = json.projectId;
  responseDiv.appendChild(input);

  var input = document.createElement('input');
  input.id = "modalWinUserId";
  input.name = "modalWinUserId";
  input.type = "hidden";
  input.value = json.userId;
  responseDiv.appendChild(input);

  var input = document.createElement('input');
  input.id = "rowId";
  input.name = "rowId";
  input.type = "hidden";
  input.value = json.rowId;
  responseDiv.appendChild(input);

  var p = document.createElement('p');
  p.id = "dateLabel";
  p.innerHTML = "<span class='boldface'>Date:</span> " + json.date;
  responseDiv.appendChild(p);

  var p = document.createElement('p');
  p.id = "projectLabel";
  p.innerHTML = "<span class='boldface'>Project:</span> " + json.project;
  responseDiv.appendChild(p);

  var p = document.createElement('p');
  p.id = "userLabel";
  if ((json.email == null) || (json.email.length == 0)) {
      p.innerHTML = "<span class='boldface'>User:</span> " + json.userName;
  } else {
      p.innerHTML = "<span class='boldface'>User:</span> " + json.userName
          + " <" + '<a class=emailLabel href="mailto:' + json.email + '">'
          + json.email + '</a>' + ">";
  }
  responseDiv.appendChild(p);

  var p = document.createElement('p');
  p.id = "institutionLabel";
  p.innerHTML = "<span class='boldface'>Institution:</span> " + json.institution;
  responseDiv.appendChild(p);

  var userRequest = true;
  if ((json.requester != null) && (json.requester.length > 0)) {
      // Request was made on behalf of the user.
      userRequest = false;

      var p = document.createElement('p');
      p.id = "requesterLabel";
      p.innerHTML = "<span class='boldface'>Requester:</span> " + json.requester;
      responseDiv.appendChild(p);
  }

  if (userRequest && !isChangeRequest && !isChangeResponse) {
      // User's Reason is not available if request made on their behalf.
      var p = document.createElement('p');
      p.id = "reasonLabel";
      p.className = "boldface";
      p.innerHTML = "User's Reason:";
      responseDiv.appendChild(p);

      var p = document.createElement('p');
      p.id = "reasonProvided";
      p.className = "italicized";
      p.innerHTML = '<table><tr><td width="5%"></td>' + '<td >' + json.reason
          + '</td></tr></table>';
      responseDiv.appendChild(p);
  }

  var requestedLevel = json.requestLevel;
  var requestedLabel = userRequest ? "This person requested:" : "Access level requested:";
  if (isChangeRequest || isChangeResponse) {
      requestedLevel = json.currentAccessLevel;
      requestedLabel = "Current Access Level:";
  }

  var p = document.createElement('p');
  p.className = "boldface";
  p.innerHTML = requestedLabel;
  responseDiv.appendChild(p);

  var p = document.createElement('p');
  p.id = "requestLevelLabel";
  p.innerHTML = '<table><tr><td width="5%"></td>' + '<td >'
          + requestedLevel + '</td></tr></table>';
  responseDiv.appendChild(p);

  if (!isChangeRequest) {
      if ((json.firstVoteReason != null) && (json.firstVoteReason.length > 0)) {
          // PI or DP has already seen this and responded.
          var reasonLabel = json.firstVoteRole.toUpperCase() + "'s Reason";
          if (isChangeResponse) {
              reasonLabel += " For Change";
          }

          var p = document.createElement('p');
          p.id = "firstVoteReasonLabel";
          p.className = "boldface";
          p.innerHTML = reasonLabel + ":";
          responseDiv.appendChild(p);

          var p = document.createElement('p');
          p.id = "firstVoteReasonProvided";
          p.className = "italicized";
          p.innerHTML = '<table><tr><td width="5%"></td>' + '<td >' + json.firstVoteReason
              + '</td></tr></table>';
          responseDiv.appendChild(p);
      }
  }

  var respondLabel = isChangeRequest ? "Which access level do you wish to grant?"
      : "How do you want to respond?";
  var p = document.createElement('p');
  p.className = "boldface";
  p.innerHTML = respondLabel;
  responseDiv.appendChild(p);

  var viewChecked = "";
  var editChecked = "";
  var adminChecked = "";
  var denyChecked = "";
  if (json.level == "admin") {
    adminChecked = "checked";
  } else if (json.level == "edit") {
    editChecked = "checked";
  } else if (json.level == "view") {
    viewChecked = "checked";
  } else if (json.level == "deny" || json.level == null) {
    denyChecked = "checked";
  }

  var p = document.createElement('p');
  p.innerHTML = '<table>'
          + '<tr><td><input type="radio" name="modalWinLevelGroup" id="response_view_access" value="view" '
          + viewChecked
          + '></td><td><label for="response_view_access">Allow ' + viewText + '</label></td></tr>'

          + '<tr><td><input type="radio" name="modalWinLevelGroup" id="response_edit_access" value="edit" '
          + editChecked
          + '></td>'
          + '<td><label for="response_edit_access">Allow ' + editText + '</label></td></tr>'

          + '<tr><td><input type="radio" name="modalWinLevelGroup" id="response_admin_access" value="admin" '
          + adminChecked
          + '></td>'
          + '<td><label for="response_admin_access">Allow ' + adminText + '</label></td></tr>'

          + '<tr><td><input type="radio" name="modalWinLevelGroup" id="response_deny_access" value="deny" '
          + denyChecked
          + '></td>'
          + '<td><label for="response_deny_access">Deny access</label></td></tr>'

          + '<table>';

  responseDiv.appendChild(p);

  var p = document.createElement('p');
  //p.innerHTML = "Reason (optional, will not be sent to requester):"
  p.innerHTML = "<strong>Your Reason</strong> &nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"shareReasonFlag\" id=\"shareReasonFlag\" checked>Share this reason with the requester."
  responseDiv.appendChild(p);

  var d = document.createElement('div');
  d.className = "textAreaDiv";

  var textarea = document.createElement('textarea');
  textarea.name = "ResponseReason";
  textarea.id = "ResponseReason";
  textarea.rows = "5";
  textarea.cols = "60";
  textarea.value = json.lastReason;
  d.appendChild(textarea);

  var div = document.createElement('div');
  div.className = "charsLeftResponse";
  div.id = "charsLeftResponse";
  var charsLeft = MaxLen - textarea.value.length;
  div.innerHTML = charsLeft + " characters remaining";
  d.appendChild(div);
  responseDiv.appendChild(d);

  jQuery("textarea#ResponseReason").keyup(textareaModResponse);
  jQuery('#responseDialog').dialog('open');
}

/* End Dialog definitions */

/* Create Ajax Submissions */

// Submits an Ajax request to the servlet to make the project public from
// project page.
function submitProjectPublic() {
  new Ajax.Request("AccessRequests", {
      parameters : {
          requestingMethod : "submitProjectPublic",
          arProjectId : jQuery('input[id=modalWinProjectId]').val()
      },
      requestHeaders : {
        Accept : 'application/json;charset=UTF-8'
      },
      beforeSend : showStatusIndicator("Please wait..."),
      onSuccess : hideStatusIndicator,
      onComplete : getProjectPublicStatus,
      onException : function(request, exception) {
        throw (exception);
      }
  });
  jQuery(this).dialog("close");
  return false;
}

// Submits an Ajax request to the servlet to make the project public from Access
// Request page.
function voteProjectPublic() {
  new Ajax.Request("AccessRequests", {
      parameters : {
          requestingMethod : "voteProjectPublic",
          arProjectId : jQuery('input[id=modalWinProjectId]').val(),
          arRequestor : "%",
          arLevel : "edit",
          arReason : "",
          rowId : jQuery('input[name=rowId]').val()
      },
      requestHeaders : {
        Accept : 'application/json;charset=UTF-8'
      },
      beforeSend : showStatusIndicator("Please wait..."),
      onSuccess : hideStatusIndicator,
      onComplete : getResponseStatus,
      onException : function(request, exception) {
        throw (exception);
      }
  });
  jQuery(this).dialog("close");
  return false;
}

//Submits an Ajax request to the servlet to make the project public from Access
//Request page.
function denyProjectPublic() {
  new Ajax.Request("AccessRequests", {
      parameters : {
          requestingMethod : "denyProjectPublic",
          arProjectId : jQuery('input[id=modalWinProjectId]').val(),
          arRequestor : "%",
          arLevel : "view",
          arReason : "",
          rowId : jQuery('input[name=rowId]').val()
      },
      requestHeaders : {
        Accept : 'application/json;charset=UTF-8'
      },
      beforeSend : showStatusIndicator("Please wait..."),
      onSuccess : hideStatusIndicator,
      onComplete : getResponseStatus,
      onException : function(request, exception) {
        throw (exception);
      }
  });
  jQuery(this).dialog("close");
  return false;
}

// Submits an Ajax request to the servlet to add a new access row
// (administrator).
function newAccessRow() {
  new Ajax.Request("AccessRequests", {
      parameters : {
          requestingMethod : "newAccessRow",
          arProjectId : jQuery('#selectWinProjectId option:selected').val(),
          arRequestor : jQuery('#selectWinUserId option:selected').val(),
          arLevel : jQuery('input[name=modalWinLevelGroup]:checked').val(),
          arShareReasonFlag : jQuery('input[name=shareReasonFlag]:checked').val(),
          arReason : jQuery('textarea#NewAccessRowReason').val(),
          arCreateNewAccessRowFlag : "true",
          rowId : ""
      },
      requestHeaders : {
        Accept : 'application/json;charset=UTF-8'
      },
      beforeSend : showStatusIndicator("Please wait..."),
      onSuccess : hideStatusIndicator,
      onComplete : loadRecentTabOnComplete,
      onException : function(request, exception) {
        throw (exception);
      }
  });
  jQuery(this).dialog("close");
  return false;
}

// Submits an Ajax request to the servlet for the user request.
function requestAccess() {
  new Ajax.Request("AccessRequests", {
      parameters : {
          actualRequestingMethod : "AccessRequest.requestAccess",
          requestingMethod : "submitRequest",
          arProjectId : jQuery('input[id=modalWinProjectId]').val(),
          arLevel : jQuery('input[name=modalWinLevelGroup]:checked').val(),
          arReason : jQuery('textarea#RequestReason').val()
      },
      requestHeaders : {
        Accept : 'application/json;charset=UTF-8'
      },
      beforeSend : showStatusIndicator("Please wait..."),
      onSuccess : hideStatusIndicator,
      onComplete : getRequestStatus,
      timeout : 50,
      onException : function(request, exception) {
        throw (exception);
      }
  });
  jQuery(this).dialog("close");
  return false;
}

// Submits an Ajax request to the servlet submit for the PI/DP response.
function respond() {
  new Ajax.Request("AccessRequests", {
      parameters : {
          requestingMethod : "respond",
          arProjectId : jQuery('input[id=modalWinProjectId]').val(),
          arRequestor : jQuery('input[id=modalWinUserId]').val(),
          arLevel : jQuery('input[name=modalWinLevelGroup]:checked').val(),
          arShareReasonFlag : jQuery('input[name=shareReasonFlag]:checked').val(),
          arReason : jQuery('textarea#ResponseReason').val(),
          rowId : jQuery('input[name=rowId]').val()
      },
      requestHeaders : {
        Accept : 'application/json;charset=UTF-8'
      },
      beforeSend : showStatusIndicator("Please wait..."),
      onSuccess : hideStatusIndicator,
      onComplete : getResponseStatus,
      onException : function(request, exception) {
        throw (exception);
      }
  });
  jQuery(this).dialog("close");
  return false;
}

/* End Ajax Submissions */

/* Begin Ajax Submission supporting functions */

// Reload the page after an Ajax success (used with newAccessRow)
function loadRecentTabOnComplete(transport) {
  var json = transport.responseText.evalJSON(true);
  if (json.msg == "success") {
            window.location = "AccessRequests?accessRequestSubtab=Recent+Activity";
  }
}

// Shows a status indicator while waiting
function showStatusIndicator(titleString) {
  var div = document.createElement('div');
  div.id = "statusIndicator";
  div.className = "statusIndicator";

  jQuery('body').append(div);

  // Status Indicator Popup
  jQuery('.statusIndicator').dialog({
      modal : true,
      autoOpen : false,
      title : titleString,
      width : 120,
      height : 120
  });

  // Create form elements to add to the status indicator
  jQuery('#statusIndicator')
          .html(
                  '<center><img id="waitingIcon" src="images/waiting.gif" /></center>');
  jQuery('#statusIndicator').dialog('open');
  jQuery('#statusIndicator').dialog("option", "stack", true);
  jQuery('#statusIndicator').dialog("option", "resizable", false);
}

// Hides that status indicator
function hideStatusIndicator() {
  jQuery('#statusIndicator').dialog('close');

}

// Ajax response listener for making projects public
function getProjectPublicStatus(transport) {
  var json = transport.responseText.evalJSON(true);
  if (json.msg == "success") {
            // If called from 'Permissions' page...
            var permissionsDiv = document.getElementById('project-permissions-div');
            if (permissionsDiv) {
                // Reload 'Permissions' page.
                refreshPermissionsPage();    // in ProjectPermissions.js
            }
  } else if (json.msg == "error") {
            showErrorDialog(json.cause);
  }
}

// Ajax response listener for access request submission
function getRequestStatus(transport) {
  var json = transport.responseText.evalJSON(true);
  if (json.msg == "success") {
    var status = json.status;
    var level = getStringEquivalent(json.level, false);
    var lastRequest = json.lastRequest;
    var projectId = json.projectId;
    var buttonId = "requestId_" + projectId;
    var buttonTitle = json.buttonTitle;
    var isButtonEnabled = json.isButtonEnabled;
    var buttonDiv = "div[id=requestId_" + projectId
            + "][name=requestButtonDiv]";
    var levelDiv = "div[id=requestId_" + projectId + "][name=levelDiv]";
    var lastRequestDiv = "div[id=requestId_" + projectId
            + "][name=lastRequestDiv]";
    var statusDiv = "div[id=requestId_" + projectId + "][name=statusDiv]";

    if (jQuery(lastRequestDiv).length) { // length not zero if exists
      jQuery(lastRequestDiv).html(lastRequest);
    }
    if (jQuery(levelDiv).length) { // length not zero if exists
      jQuery(levelDiv).html(level);
    }
    if (jQuery(statusDiv).length) { // length not zero if exists
      jQuery(statusDiv).html(getStringEquivalent(status, false));
    }
    if (jQuery(buttonDiv).length) { // length not zero if exists
      if (status == 'approved' || status == 'denied') {
        jQuery(buttonDiv).html("");
      } else {
        if (isButtonEnabled) {
          requestButtonClass = "request_link";
        } else if (!isButtonEnabled) {
          requestButtonClass = "dead_link ui-state-disabled";
        }

        jQuery(buttonDiv)
                .html(
                        "<p><a href=\"#\" id=\""
                                + buttonId
                                + "\" class=\""
                                + requestButtonClass
                                + " ui-state-default ui-corner-all\" name=\""
                                + buttonId
                                + "\" ><span class=\"ui-icon ui-icon-newwin\"></span>"
                                + buttonTitle + "</a></p>");
      }
    }
    if (status == 'approved') {
      showConfirmationDialog("<h4>Your request has been approved.</h4>");
    } else if (status == 'denied') {
      displayAccessReportWarning("<h4>Sorry, your request cannot be approved.</h4><p><em>Please contact DataShop help (datashop-help@lists.andrew.cmu.edu) if you feel this message is in error.</em></p>");
    } else {
      showConfirmationDialog("<p>Your request has been submitted.</p><p><em>You may re-request access in 24 hours if you have not received a response.</em></p>");
    }
  } else if (json.msg == "error") {
    showErrorDialog(json.cause);
  } else if (json.msg == "timeout") {
    alert('timeout');
  }
}

// Ajax response listener for response submission
function getResponseStatus(transport) {
  var ACCESS_REPORT_COL_COUNT = 12;
  var RECENT_ACTIVITY_COL_COUNT = 10;
  var json = transport.responseText.evalJSON(true);

  if (json.msg == "success") {
            // If called from 'Permissions' page...
            var permissionsDiv = document.getElementById('project-permissions-div');
            if (permissionsDiv) {
                updatePermissionsPage(transport);   // in ProjectPermissions.js
            } else {
    // Get the json attributes
    var rowId = json.rowId;
    var currentTab = json.currentTab;
    var projectId = json.projectId;
    var lastActivity = json.lastActivityDate;
    var self = json.self;
    var selfEmail = json.selfEmail;
    var role = json.role;
    var status = json.status;
    var action = json.action;
    var level = getStringEquivalent(json.level, false);
    var myLevel = getStringEquivalent(json.myLevel);
    var lastLevel = getStringEquivalent(json.lastLevel);
    var lastStatus = json.lastStatus;
    var reason = json.reason;
    var buttonTitle = json.buttonTitle;
    var isButtonVisible = json.isButtonVisible;
    var myRequestsCount = json.myRequestsCount;
    var notReviewedCount = json.notReviewedCount;
    var totalCount = json.totalCount;
    var recentCount = json.recentCount;
    var requestorId = json.requestorId;
    var requestorName = json.requestorName;

    if (totalCount != 0) {
      jQuery("#ActivityNotificationFlag")
              .html(
                      "<img src=\"images/flag.png\" title=\"There is recent access-request activity for you to review.\" />");
    }

    var otherParty = "";
    if (role == 'pi') {
      otherParty = 'Data Provider';
    } else if (role == 'dp') {
      otherParty = 'PI';
    }
    var infoStringRecentActivity = 'Your vote has been recorded. '
            + 'However, the access level will not be updated until the '
            + otherParty
            + ' changes their vote from Denied to either View or Edit. '
            + 'Please contact the ' + otherParty
            + ' to resolve this issue.';

    var infoStringAccessReport = 'Your vote has been recorded. '
            + 'However, the access level will not be updated until the '
            + otherParty + ' agrees to the changes made. '
            + 'You can view your actions under the "Recent Activity" tab.';

    // These div's are updated based on AJAX response
    // Use Id and Name to identify them using jQuery
    var buttonDiv = "div[id=" + rowId + "][name=responseButtonDiv]";
    var statusDivId = "div[id=" + rowId + "][name=statusDiv]";
    var modifyAccessDiv = "div[id=" + rowId + "][name=modifyAccessDiv]";
    var lastActivityDiv = "div[id=" + rowId + "][name=lastActivityDiv]";

    // Format history row based on whether or not the project is public
    if (requestorId == "%") {
      myLevel = "Public Access"
    }
    // Account for partial denies that can only be changed by one who denied
    if (action != "deny" && status == 'dp_denied' && role == 'pi') {
      // Open modal window explaining why the status has not change
      displayAccessReportWarning(infoStringRecentActivity);
    } else if (action != "deny" && status == 'pi_denied' && role == 'dp') {
      // Open modal window explaining why the status has not change
      displayAccessReportWarning(infoStringRecentActivity);
    }

    if (currentTab == 'Not Reviewed' || currentTab == 'Pending') {
      // hide the table if there aren't any requests
      if (notReviewedCount == 0) {
        jQuery("table#project-requests-table").hide();
      }

      // Account for all other scenarios
      if (isButtonVisible == 'true') {
        // Show the response button
        jQuery(buttonDiv)
                .html(
                        "<p><a href=\"#\" id=\""
                                + rowId
                                + "\" class=\"response_link ui-state-default-blue ui-corner-all-blue\" name=\""
                                + rowId
                                + "\" ><span class=\"ui-icon ui-icon-blue ui-icon-newwin\"></span>"
                                + buttonTitle + "</a></p>");
        jQuery(buttonDiv).click(createResponse);
      } else {
        jQuery("tr#" + rowId).hide();
      }
    } else if (currentTab == 'Recent Activity') {
      // Status updates computed in servlet determine behavior of response
      // button
      if (isButtonVisible == 'true') {
        // Show the response button
        jQuery(buttonDiv)
                .html(
                        "<p><a href=\"#\" id=\""
                                + rowId
                                + "\" class=\"response_link ui-state-default-blue ui-corner-all-blue\" name=\""
                                + rowId
                                + "\" ><span class=\"ui-icon ui-icon-blue ui-icon-newwin\"></span>"
                                + buttonTitle + "</a></p>");

      } else if (isButtonVisible == 'false') {
        // Change the 'level' field to a link that allows access level
        // modification
        jQuery(buttonDiv).html("");
        jQuery(modifyAccessDiv).html(
                "<p><a href=\"#\" id=\"" + rowId
                        + "\" class=\"response_link\" name=\"" + rowId
                        + "\" >" + level
                        + "<img src=\"images/edit.gif\"></a></p>");
        jQuery(modifyAccessDiv).click(createResponse);

      }
      // Change notification image on response
      var notifyImg = "img[id=" + rowId + "][class=notify-img]";
      if (jQuery(notifyImg).length) {
        jQuery(notifyImg).attr({
          'src' : "images/trans_spacer.gif"
        });
      }

      // Update the status and last activity date
      jQuery(statusDivId).html(getStringEquivalent(status, false));
      jQuery(lastActivityDiv).html(lastActivity);
      updateHistory(rowId, RECENT_ACTIVITY_COL_COUNT, lastActivity,
          self, selfEmail, role, action, myLevel, reason);
    } else if (currentTab == 'Access Report') {
      // Access Report never displays button, only access level
      // modification link
      updateHistory(rowId, ACCESS_REPORT_COL_COUNT, lastActivity,
          self, selfEmail, role, action, myLevel, reason);
      if (status != "pi_approved" && status != "dp_approved") {
        if (action == "deny") {
          level = "Denied";
        }
        jQuery(modifyAccessDiv).html(
                "<p><a href=\"#\" id=\"" + rowId
                        + "\" class=\"response_link\" name=\"" + rowId
                        + "\" >" + level
                        + "<img src=\"images/edit.gif\"></a></p>");
        jQuery(modifyAccessDiv).click(createResponse);

      }
    }
    if ( (lastLevel != level && status == 'approved')
      || (lastStatus != status && (status == 'denied'
        || status == 'pi_denied' || status == 'dp_denied'))) {
      if (requestorName != "%") {
        showConfirmationDialog("<p>Your response has been saved, and an email has been sent to "
                + requestorName + ".</p>");
      }
    }


    // Update the activity notification counts
    if (notReviewedCount > 0) {
      jQuery("#notreviewed_label .ui-button-text:contains('Pending')")
              .html("Pending (" + notReviewedCount + ")");
      jQuery(
              "#notreviewed_label .ui-button-text:contains('Not Reviewed')")
              .html("Not Reviewed (" + notReviewedCount + ")");
    } else if (notReviewedCount == 0) {
      jQuery("#notreviewed_label .ui-button-text:contains('Pending')")
              .html("Pending");
      jQuery(
              "#notreviewed_label .ui-button-text:contains('Not Reviewed')")
              .html("Not Reviewed");
    }

    if (recentCount > 0) {
      jQuery("#recent_label .ui-button-text").html(
              "Recent Activity (" + recentCount + ")");
    } else if (recentCount == 0) {
      jQuery("#recent_label .ui-button-text").html("Recent Activity");
    }

    if (myRequestsCount > 0) {
      jQuery("#MyRequestsCount .ui-button-text").html(
              " (" + myRequestsCount + ")");
    } else if (myRequestsCount == 0) {
      jQuery("#MyRequestsCount .ui-button-text").html("");
    }
            }
  } else if (json.msg == "error") {
    if (json.cause != null) {
      showErrorDialog(json.cause);
    }
  }
}

// A simple close modal dialog function to make reading easier elsewhere.
function closeDialog() {
  jQuery(this).dialog("close");
  jQuery(".ui-dialog-buttonpane button:contains('Request Access')").button(
          'disable');
  jQuery(".ui-dialog-buttonpane button:contains('Respond')").button('enable');
  jQuery(".ui-dialog-buttonpane button:contains('Save')").button('enable');
}

function closeInformationDialog() {
  jQuery(this).dialog("close");
  jQuery(this).empty().remove();
}


/* End Ajax Submission supporting functions */

/* Begin Ajax Response supporting functions */

// A function to show an error dialog.
function showErrorDialog(cause) {
  // Create form elements to add to the div
  var div = document.createElement('div');
  div.id = "showErrorDialog";
  div.className = "informationalDialog";
  div.innerHTML = cause;
  jQuery('body').append(div);
  // Used to show errors with the Ajax requests
  jQuery('#showErrorDialog').dialog({
      modal : true,
      autoOpen : false,
      width : 840,
      height : 340,
      title : "Oops",
      buttons : [ {
                id : "show-error-button",
                text : "Okay",
                click : closeInformationDialog
      } ]
  });

  jQuery('#showErrorDialog').dialog('open');
}

// A function to show a confirmation dialog.
function showConfirmationDialog(info) {
  // Create form elements to add to the div
  var div = document.createElement('div');
  div.id = "confirmationDialog";
  div.className = "informationalDialog";
  div.innerHTML = info;
  jQuery('body').append(div);
  // Used to show errors with the Ajax requests
  jQuery('div[id=confirmationDialog]').dialog({
      modal : true,
      autoOpen : false,
      width : 620,
      height : 325,
      title : "Success",
      buttons : [ {
                id : "show-success-button",
                text : "Okay",
                click : closeInformationDialog
      } ]
  });

  jQuery('div[id=confirmationDialog]').dialog('open');
}

// Similar to showErrorDialog but specific to access modification warnings.
function displayAccessReportWarning(infoString) {
  // Create form elements to add to the div
  var div = document.createElement('div');
  div.id = "modifyAccessDialog";
  div.className = "informationalDialog";
  div.innerHTML = infoString;
  jQuery('body').append(div);
  // Modify Access Dialog used to warn user when status is unchanged
  jQuery('#modifyAccessDialog').dialog({
      modal : true,
      autoOpen : false,
      width : 840,
      height : 340,
      title : "Warning",
      buttons : [ {
                id : "show-warning-button",
                text : "Okay",
                click : closeInformationDialog
      } ]
  });

  jQuery('#modifyAccessDialog').dialog('open');
}

/* End Ajax Response supporting functions */

// Show/Hide project request history for a given row when an image is clicked.
function triggerCollapseExpand() {
  var imgId = "img[id=" + this.id + "][class=collapsed]";
  var divId = "div[id=" + this.id + "][class=historyList]";
  if (jQuery(this).attr('src') == "images/expand.png") {
    jQuery(this).attr({
      'src' : "images/contract.png"
    });
    jQuery("tr[id=" + this.id + "][class=historyList]").show();
  } else {
    jQuery(this).attr({
      'src' : "images/expand.png"
    });
    jQuery("tr[id=" + this.id + "][class=historyList]").hide();
  }
  return false;
}

// Add new actions to the history rows dynamically
function updateHistory(rowId, numCols, date, userName, userEmail, role, action, level,
        reason) {

  if (action == "deny") {
    level = "";
    action = "Denied";
  } else {
    level = getStringEquivalent(level, true);
    action = getStringEquivalent(action, true);
  }
  var headerString = '<tr id="' + rowId + '" name="' + rowId
          + '" class="historyList">' + '<td class="arFirstColumn"></td>'
          + '<td class="arSecondColumn"></td>' + '<td colspan="' + numCols
          + '"><b>History</b></td>' + '</tr>';
  var createString = '<tr id="' + rowId + '" name="' + rowId
          + '" class="historyList">' + '<td class="arFirstColumn"></td>'
          + '<td class="arSecondColumn"></td>' + '<td>' + date + '</td>'
          + '<td><a href="mailto:' + userEmail + '">' + userName + '</a> ('
          + getStringEquivalent(role) + ')</td>' + '<td>' + action + '</td>'
          + '<td>' + level + '</td>' + '<td>' + reason + '</td>'
          + '<td></td>' + '<td></td>' + '<td></td>' + '<td></td>' + '</tr>';

  var historyPlaceHolder = "tr[id=" + rowId + "][name=historyPlaceHolder]";
  jQuery(createString).insertAfter(historyPlaceHolder);
  if (jQuery("tr[id=" + rowId + "][class=historyList]:hidden").length > 0) {
    jQuery("tr[id=" + rowId + "][class=historyList]").hide();
  }
  var historyIconImg = "img[id=" + rowId + "][class=nohistory]";
  if (jQuery(historyIconImg).length) {
    jQuery(historyIconImg).attr({
      'src' : "images/expand.png"
    });
    jQuery(historyIconImg).attr({
      'class' : "collapsed"
    });
    jQuery(".collapsed").click(triggerCollapseExpand);
  }

}

// Submit user-specified request (rows per page)
function rowsPerPage() {
  var numRows = jQuery(this).val();
  jQuery(
          '<form id="rowsPerPageForm" method="post" action="AccessRequests">'
                  + '<input id="ar_rows_per_page" name="ar_rows_per_page"'
                  + ' type="hidden" value="' + numRows + '"/></form>')
          .appendTo('body').submit();
}

// Submit user-specified request (rows per page)
function showAdmins() {
  var value = "false";
  if (this.checked) {
    value = "true";
  }
  jQuery(
          '<form id="showAdminsForm" method="post" action="AccessRequests">'
                  + '<input id="ar_show_admins" name="ar_show_admins"'
                  + ' type="hidden" value="' + value + '" /></form>')
          .appendTo('body').submit();
}

// Submit user-specified request (filter by status)
function filterBy() {
  var filterStatus = jQuery(this).val();
  jQuery(
          '<form id="filterByForm" method="post" action="AccessRequests">'
                  + '<input id="ar_filter_by" name="ar_filter_by"'
                  + ' type="hidden" value="' + filterStatus + '"/></form>')
          .appendTo('body').submit();
}

// Submit user-specified request (search string)
function searchBy(event) {
  var key = "";
  if (this.id == 'ar_search_string') {
    key = event.keyCode || event.which;
  }
  var testEvent = (key === 13 & this.id == 'ar_search_string')
          | this.id == 'ar_search_button';
  if (testEvent) {
    var searchString = jQuery("#ar_search_string").val();
                if (searchString == jQuery("#ar_search_string").attr("title")) {
                    searchString = "";
                }
    jQuery(
            '<form id="searchStringForm" method="post" action="AccessRequests">'
                    + '<input id="ar_search_string" name="ar_search_string"'
                    + ' type="hidden" value="' + replaceHTML(searchString)
                    + '"/></form>').appendTo('body').submit();
  }
  return false;
}

// Submit user-specified request (rows per page)
function exportReport() {
    if (jQuery("input#export_cp_only").attr('checked')) {
        jQuery('<form id="exportCpForm" method="post" action="AccessRequests">'
               + '<input id="accessRequestExportCpReport" name="accessRequestExportCpReport"'
               + ' type="hidden" value="true"/></form>')
            .appendTo('body').submit();
    } else {
        jQuery(
          '<form id="exportForm" method="post" action="AccessRequests">'
                  + '<input id="accessRequestExportReport" name="accessRequestExportReport"'
                  + ' type="hidden" value="export"/></form>')
          .appendTo('body').submit();
    }
}

/*
 * The modal dialogs' element names were reused to make the code more efficient,
 * but the following three functions had to be explicitly defined to avoid
 * ambiguity in the event bindings.
 */

// This area makes sure the user cannot submit more than 255 characters
// in the reason text area element; function 1
function textareaModRequest() {
    var reason = jQuery("textarea#RequestReason").val();
    if (reason.length > MaxLen) {
        jQuery(".ui-dialog-buttonpane button:contains('Request Access')")
            .button('disable');
        jQuery("div#charsLeftRequest").css("color", "red");
    } else if (reason.trim().length < 1) {
        jQuery(".ui-dialog-buttonpane button:contains('Request Access')")
            .button('disable');
    } else {
        jQuery(".ui-dialog-buttonpane button:contains('Request Access')")
            .button('enable');
        jQuery("div#charsLeftRequest").css("color", "black");
    }
    var charsLeft = MaxLen - reason.length;
    // Update the chars remaining
    jQuery("div#charsLeftRequest").html(charsLeft + " characters remaining");
}

// This area makes sure the user cannot submit more than 255 characters
// in the reason text area element; function 2
function textareaModResponse() {
  if (jQuery("textarea#ResponseReason").val().length > MaxLen) {
    jQuery(".ui-dialog-buttonpane button:contains('Respond')").button(
            'disable');
    jQuery("div#charsLeftResponse").css("color", "red");
  } else {
    jQuery(".ui-dialog-buttonpane button:contains('Respond')").button(
            'enable');
    jQuery("div#charsLeftResponse").css("color", "black");
  }
  var charsLeft = MaxLen - jQuery("textarea#ResponseReason").val().length;
  // Update the chars remaining
  jQuery("div#charsLeftResponse").html(charsLeft + " characters remaining");
}

// This area makes sure the user cannot submit more than 255 characters
// in the reason text area element; function 3
function textareaModNewAccessRow() {
  if (jQuery("textarea#NewAccessRowReason").val().length > MaxLen) {
    jQuery(".ui-dialog-buttonpane button:contains('Save')").button(
            'disable');
    jQuery("div#charsLeftNewAccessRow").css("color", "red");
  } else {
    jQuery(".ui-dialog-buttonpane button:contains('Save')")
            .button('enable');
    jQuery("div#charsLeftNewAccessRow").css("color", "black");
  }
  var charsLeft = MaxLen - jQuery("textarea#NewAccessRowReason").val().length;
  // Update the chars remaining
  jQuery("div#charsLeftNewAccessRow").html(
          charsLeft + " characters remaining");
}

// Used to convert the various states to display strings
function getStringEquivalent(s, isPastTense) {
  var displayString = null;
  if (s == null) {
    displayString = "";
  } else if ((s == "approve") && isPastTense) {
    displayString = "Approved";
  } else if ((s == "deny") && isPastTense) {
    displayString = "Denied";
  } else if ((s == "request") && isPastTense) {
    displayString = "Requested";
  } else if ((s == "approved")) {
    displayString = "Approved";
  } else if ((s == "denied")) {
    displayString = "Denied";
  } else if ((s == "pi_approved")) {
    displayString = "Approved by PI";
  } else if ((s == "dp_approved")) {
    displayString = "Approved by data provider";
  } else if ((s == "pi_denied")) {
    displayString = "Denied by PI";
  } else if ((s == "dp_denied")) {
    displayString = "Denied by data provider";
  } else if ((s == "not_reviewed")) {
    displayString = "Not Reviewed";
  } else if ((s == "edit")) {
    displayString = "Edit";
  } else if ((s == "view")) {
    displayString = "View";
  } else if (s == "requestor") {
    displayString = "Requester";
  } else if (s == "admin") {
    displayString = "Admin";
  } else if (s == "pi") {
    displayString = "PI";
  } else if (s == "dp") {
    displayString = "Data provider";
  } else if (s == "") {
    displayString = "Public";
  } else {
    displayString = s;
  }
  return displayString;
}

// Changes a sub-tab when one of the radio buttons (in the buttonset) is clicked
function changeSubtab() {
  var subtab = this.id;
  jQuery(
          '<form id="changeSubtab" method="post" action="AccessRequests">'
                  + '<input type="hidden" id="accessRequestSubtab" name="accessRequestSubtab" value="'
                  + this.id + '" /></form>').appendTo('body').submit();
}
// Function which shows older user requests
function bindShowUserRequests() {
  jQuery(
          '<form id="changeShowHideUserRequests" method="post" action="AccessRequests">'
                  + '<input type="hidden" id="ar_show_older_user_req" name="ar_show_older_user_req" value='
                  + '"true" /></form>').appendTo('body').submit();
}
// Function which hides older user requests
function bindHideUserRequests() {
  jQuery(
          '<form id="changeShowHideUserRequests" method="post" action="AccessRequests">'
                  + '<input type="hidden" id="ar_show_older_user_req" name="ar_show_older_user_req" value='
                  + '"false" /></form>').appendTo('body').submit();
}
// Function which shows older projects requests
function bindShowProjectRequests() {
  jQuery(
          '<form id="changeShowHideProjectRequests" method="post" action="AccessRequests">'
                  + '<input type="hidden" id="ar_show_older_project_req" name="ar_show_older_project_req" value='
                  + '"true" /></form>').appendTo('body').submit();
}
// Function which hides older project requests
function bindHideProjectRequests() {
  jQuery(
          '<form id="changeShowHideProjectRequests" method="post" action="AccessRequests">'
                  + '<input type="hidden" id="ar_show_older_project_req" name="ar_show_older_project_req" value='
                  + '"false" /></form>').appendTo('body').submit();
}

// Comparator function used for sorting the project and user lists when
// creating the new access row dialog's dropdown box
function caseInsensitiveSort(a, b) {
  var ret = 0;
  a = a.toLowerCase();
  b = b.toLowerCase();
  if (a > b)
    ret = 1;
  if (a < b)
    ret = -1;
  return ret;
}

//Used to prevent unwanted html characters from getting into search bar
function replaceHTML(str) {
    var result = "";
    if (str !== undefined && str != null && str != "") {
        result = str.replace(/&/g, "&amp;")
              .replace(/</g, "&lt;")
              .replace(/>/g, "&gt;")
              .replace(/\"/g, "&quot;");
    }
    return result;
}
