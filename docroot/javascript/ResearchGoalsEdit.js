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

// Needed to use both prototype and jQuery
jQuery.noConflict();

// jQuery's onload function
jQuery(document).ready(function() {
});

//------------------------
//----- Page Refresh -----
//------------------------

function rgPageRefresh() {
  var newForm = document.createElement('FORM');
  newForm.id   = "rgPageRefresh";
  newForm.name = "rgPageRefresh";
  newForm.form = "text/plain";
  newForm.action = "ResearchGoalsEdit";
  newForm.method = "post";
  document.getElementsByTagName('body').item(0).appendChild(newForm);
  newForm.submit();
}

//----------------------------------------
//----- Researcher Type : Edit Label -----
//----------------------------------------

function rtShowEditSpan(typeId) {
    jQuery("#rt_view_span_" + typeId).hide();
    jQuery("#rt_edit_span_" + typeId).show();
}
function rtHideEditSpan(typeId) {
    jQuery("#rt_view_span_" + typeId).show();
    jQuery("#rt_edit_span_" + typeId).hide();
}
function rtEditType(typeId) {
    rtShowEditSpan(typeId);
}
function rtCancelEditType(typeId) {
    rtHideEditSpan(typeId);
    jQuery("#rt_field_" + typeId).val( jQuery("#rt_hidden_field_" + typeId).val() );
}
function rtSaveEditType(typeId) {
    rtHideEditSpan(typeId);
    var newLabel = jQuery("#rt_field_" + typeId).val();
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rtSaveEditType",
            ajaxAction: "rtSaveEditType",
            typeId: typeId,
            newLabel: newLabel,
            ajaxRequest: "true"
        },
        onComplete: handleRtSaveEditType,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleRtSaveEditType(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        successPopup(json.message);
        jQuery("#rt_view_field_" + json.typeId).html(json.newLabel);
        jQuery("#rt_field_" + json.typeId).val(json.newLabel);
        jQuery("#rt_hidden_field_" + json.typeId).val(json.newLabel);
    } else {
        errorPopup(json.message);
    }
}

//-----------------------------------------
//----- Researcher Type : Delete Type -----
//-----------------------------------------

function rtShowDeleteAreYouSure(typeId) {
    jQuery("#rt_delete_link_span_" + typeId).hide();
    jQuery("#rt_delete_sure_span_" + typeId).show();
}
function rtHideDeleteAreYouSure(typeId) {
    jQuery("#rt_delete_sure_span_" + typeId).hide();
    jQuery("#rt_delete_link_span_" + typeId).show();
}
function rtDeleteType(typeId) {
    rtHideDeleteAreYouSure(typeId);

    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rtDeleteType",
            ajaxAction: "rtDeleteType",
            typeId: typeId,
            ajaxRequest: "true"
        },
        onComplete: handleRtDeleteType,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleRtDeleteType(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        successPopup(json.message);
        rgPageRefresh();
    } else {
        errorPopup(json.message);
    }
}

//--------------------------------------
//----- Researcher Type : Add Type -----
//--------------------------------------

function rtOpenTypeDialog() {
    var dialogDiv = document.getElementById('rtAddTypeDialog');
    dialogDiv.innerHTML = "";
    var html
        = '<label id="newTypeLabel" for="newTypeInput">New Researcher Type</label>'
        + '<input id="newTypeInput" type="text" name="newTypeInput">';
    dialogDiv.innerHTML = html;
    
    jQuery('#rtAddTypeDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        title : "Add Researcher Type",
        width : 450,
        buttons : [ {
            id : "rt-add-type-save-button",
            text : "Save",
            click : rtAddType
        }, {
            id : "rt-add-type-cancel-button",
            text : "Cancel",
            click : rtAddTypeCancel
        } ]
    });

    jQuery('#rtAddTypeDialog').dialog('open');
}
function rtAddTypeCancel() {
    jQuery(this).dialog('close');
}
function rtAddType() {
    var typeLabel = jQuery('#newTypeInput').val();
    
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rtAddType",
            ajaxAction: "rtAddType",
            typeLabel: typeLabel,
            ajaxRequest: "true"
        },
        onComplete: handleRtAddType,
        onException: function (request, exception) {
            throw(exception);
        }
    });
    
    jQuery(this).dialog('close');
}
function handleRtAddType(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        successPopup(json.message);
        rgPageRefresh();
    } else {
        errorPopup(json.message);
    }
}

//----------------------------------------
//----- Researcher Type : Show Goals -----
//----------------------------------------

function rtShowGoals(typeId) {
    requestRtAction("rtShowGoals", typeId, null);
}

//----------------------------------------
//----- Researcher Type : Add Goals ------
//----------------------------------------

function rtAddGoal(typeId) {
    rtGetOtherGoals(typeId);
}
function rtGetOtherGoals(typeId) {
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rtGetOtherGoals",
            ajaxAction: "rtGetOtherGoals",
            typeId: typeId,
            ajaxRequest: "true"
        },
        onComplete: handleGetOtherGoals,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleGetOtherGoals(transport) {
    var json = transport.responseText.evalJSON(true);
    rtOpenAddGoalDialog(json.typeId, json.otherGoals);
}
function rtOpenAddGoalDialog(typeId, otherGoals) {
    var dialogDiv = document.getElementById('rtAddGoalDialog');
    dialogDiv.innerHTML = "";
    
    var html = '<input id="typeId" type="hidden" value="' + typeId + '"</input>';
    html += '<select multiple="multiple" id="rt-add-goal-select">';
    for (i = 0; i < otherGoals.length; i++) {
        html += '<option value="' + otherGoals[i].id + '">'
             + otherGoals[i].title + '</option>';
    }
    html += '</select>'
        
    dialogDiv.innerHTML = html;
    
    jQuery('#rt-add-goal-select').attr("size", otherGoals.length);
    
    jQuery('#rtAddGoalDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        title : "Add Research Goals",
        width : 450,
        buttons : [ {
            id : "rt-add-goal-save-button",
            text : "Save",
            click : rtAddGoalSave
        }, {
            id : "rt-add-goal-cancel-button",
            text : "Cancel",
            click : rtAddGoalCancel
        } ]
    });

    jQuery('#rtAddGoalDialog').dialog('open');
}
function rtAddGoalCancel() {
    jQuery(this).dialog('close');
}
function rtAddGoalSave() {
    var typeId = jQuery('#typeId').val();
    
    var goalList = jQuery('#rt-add-goal-select').val().join();
    
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rtAddGoal",
            ajaxAction: "rtAddGoal",
            typeId: typeId,
            goalList: goalList,
            ajaxRequest: "true"
        },
        onComplete: handleRtGoals,
        onException: function (request, exception) {
            throw(exception);
        }
    });
    
    jQuery(this).dialog('close');
}

//-----------------------------------------
//----- Researcher Type : Remove Goal -----
//-----------------------------------------

function rtRemoveGoal(typeId, goalId) {
    requestRtAction("rtRemoveGoal", typeId, goalId);
}

//----------------------------------------------------------------
//----- Researcher Type : Utility Ajax call to refresh goals -----
//----------------------------------------------------------------

function requestRtAction(action, typeId, goalId) {
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.requestRtAction",
            ajaxAction: action,
            typeId: typeId,
            goalId: goalId,
            ajaxRequest: "true"
        },
        onComplete: handleRtGoals,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleRtGoals(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        html = '<table>';
        for (i = 0; i < json.goals.length; i++) {
            var idx = i + 1;
            html += '<tr>'
                  + '<td>' + idx + '. </td>'
                  + '<td>' + json.goals[i].title + '</td>'
                  + '<td><a href="javascript:rtRemoveGoal('+json.typeId+','+json.goals[i].id+')" class="rt-basic-link">remove</a></td>'
                  + '</tr>';
        }
        html += '</table>'
              + '<p><a href="javascript:rtAddGoal('+json.typeId+')" class="rt-basic-link">add goals</a></p>';

        element = $("rt_goals_row_td_" + json.typeId);
        element.innerHTML = html;

        jQuery("#rt_goals_row_" + json.typeId).show();
        jQuery("#rt_show_goals_link_" + json.typeId).html("show goals (" + json.goals.length + ")");
        jQuery("#rt_show_goals_link_" + json.typeId).hide();
        jQuery("#rt_hide_goals_link_" + json.typeId).show();
    } else {
        errorPopup(json.message);
    }
}

//--------------------------------------
//----- Research Goal : Hide Goals -----
//--------------------------------------

function rtHideGoals(typeId) {
   jQuery("#rt_goals_row_" + typeId).hide();
   jQuery("#rt_show_goals_link_" + typeId).show();
   jQuery("#rt_hide_goals_link_" + typeId).hide();
}

//--------------------------------------------------------------------
//------------------ Research Goal Section ---------------------------
//--------------------------------------------------------------------

//------------------------------------
//----- Research Goal : Add Goal -----
//------------------------------------

function rgAdd() {
    rgOpenAddGoalDialog();
}
function rgOpenAddGoalDialog() {
  var dialogDiv = document.getElementById('rgAddGoalDialog');
  dialogDiv.innerHTML = "";
  var html
      = '<label id="newGoalLabel" for="newGoalInput">New Research Goal</label>'
      + '<input id="newGoalInput" type="text" name="newGoalInput">';
  dialogDiv.innerHTML = html;
  
  jQuery('#rgAddGoalDialog').dialog({
      modal : true,
      autoOpen : false,
      resizable : false,
      title : "Add Research Goal",
      width : 450,
      buttons : [ {
          id : "rg-add-goal-save-button",
          text : "Save",
          click : rgAddGoalSave
      }, {
          id : "rg-add-goal-cancel-button",
          text : "Cancel",
          click : rgAddGoalCancel
      } ]
  });

  jQuery('#rgAddGoalDialog').dialog('open');
}
function rgAddGoalCancel() {
  jQuery(this).dialog('close');
}
function rgAddGoalSave() {
  var goalTitle = jQuery('#newGoalInput').val();
  
  new Ajax.Request("ResearchGoalsEdit", {
      parameters: {
          requestingMethod: "ResearchGoalsEdit.rgAddGoal",
          ajaxAction: "rgAddGoal",
          goalTitle: goalTitle,
          ajaxRequest: "true"
      },
      onComplete: handleRgAddGoal,
      onException: function (request, exception) {
          throw(exception);
      }
  });
  
  jQuery(this).dialog('close');
}
function handleRgAddGoal(transport) {
  var json = transport.responseText.evalJSON(true);
  if (json.flag == "success") {
      successPopup(json.message);
      rgPageRefresh();
  } else {
      errorPopup(json.message);
  }
}

//------------------------------------------
//----- Researcher Goal : Deleted Goal -----
//------------------------------------------

function rgShowDeleteAreYouSure(goalId) {
  jQuery("#rg_delete_link_span_" + goalId).hide();
  jQuery("#rg_delete_sure_span_" + goalId).show();
}
function rgHideDeleteAreYouSure(goalId) {
  jQuery("#rg_delete_sure_span_" + goalId).hide();
  jQuery("#rg_delete_link_span_" + goalId).show();
}
function rgDeleteGoal(goalId) {
    rgHideDeleteAreYouSure(goalId);

    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rgDeleteGoal",
            ajaxAction: "rgDeleteGoal",
            goalId: goalId,
            ajaxRequest: "true"
        },
        onComplete: handleRgDeleteGoal,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleRgDeleteGoal(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        successPopup(json.message);
        rgPageRefresh();
    } else {
        errorPopup(json.message);
    }
}

//--------------------------------------
//----- Research Goal : Edit Title -----
//--------------------------------------

function rgShowEditSpan(goalId) {
    jQuery("#rg_view_span_" + goalId).hide();
    jQuery("#rg_edit_span_" + goalId).show();
}
function rgHideEditSpan(goalId) {
    jQuery("#rg_view_span_" + goalId).show();
    jQuery("#rg_edit_span_" + goalId).hide();
}
function rgEditGoal(goalId) {
    rgShowEditSpan(goalId);
}
function rgCancelEditGoal(goalId) {
    rgHideEditSpan(goalId);
    jQuery("#rg_field_" + goalId).val( jQuery("#rg_hidden_field_" + goalId).val() );
}
function rgSaveEditGoal(goalId) {
    rgHideEditSpan(goalId);
    var newTitle = jQuery("#rg_field_" + goalId).val();
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rgSaveEditGoal",
            ajaxAction: "rgSaveEditGoal",
            goalId: goalId,
            newTitle: newTitle,
            ajaxRequest: "true"
        },
        onComplete: handleRgSaveEditGoal,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleRgSaveEditGoal(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        successPopup(json.message);
        jQuery("#rg_view_field_" + json.goalId).html(json.newTitle);
        jQuery("#rg_field_" + json.goalId).val(json.newTitle);
        jQuery("#rg_hidden_field_" + json.goalId).val(json.newTitle);
    } else {
        errorPopup(json.message);
    }
}

//--------------------------------------------
//----- Research Goal : Edit Description -----
//--------------------------------------------

function rgShowEditDescSpan(goalId) {
    jQuery("#rg_view_desc_span_" + goalId).hide();
    jQuery("#rg_edit_desc_span_" + goalId).show();
}
function rgHideEditDescSpan(goalId) {
    jQuery("#rg_view_desc_span_" + goalId).show();
    jQuery("#rg_edit_desc_span_" + goalId).hide();
}

function rgEditGoalDesc(goalId) {
    rgShowEditDescSpan(goalId);
}
function rgCancelEditGoalDesc(goalId) {
    rgHideEditDescSpan(goalId);
    rgRequestDesc(goalId);
}

function rgRequestDesc(goalId) {
    var newDesc = jQuery("#rg_desc_field_" + goalId).val();
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rgRequestDesc",
            ajaxAction: "rgRequestDesc",
            goalId: goalId,
            ajaxRequest: "true"
        },
        onComplete: handleRgRequstDesc,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleRgRequstDesc(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        jQuery("#rg_view_desc_span_" + json.goalId).html(json.newDesc);
        jQuery("#rg_desc_field_" + json.goalId).val(json.newDesc);
    } else {
        errorPopup(json.message);
    }
}

function rgSaveEditGoalDesc(goalId) {
    var newDesc = jQuery("#rg_desc_field_" + goalId).val();
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rgSaveEditGoalDesc",
            ajaxAction: "rgSaveEditGoalDesc",
            goalId: goalId,
            newDesc: newDesc,
            ajaxRequest: "true"
        },
        onComplete: handleRgSaveEditGoalDesc,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleRgSaveEditGoalDesc(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        successPopup(json.message);
        jQuery("#rg_view_desc_span_" + json.goalId).html(json.newDesc);
        jQuery("#rg_desc_field_" + json.goalId).val(json.newDesc);
        rgHideEditDescSpan(json.goalId);
    } else {
        errorPopup(json.message);
    }
}

//------------------------------------------
//-----  Research Goal : Show Papers   -----
//------------------------------------------

function rgShowPapers(goalId) {
    jQuery("#rg_show_papers_link_" + goalId).hide();
    jQuery("#rg_hide_papers_link_" + goalId).show();
    rgGetPapers(goalId);
}
function rgHidePapers(goalId) {
    jQuery("#rg_papers_title_" + goalId).hide();
    jQuery("#rg_papers_row_" + goalId).hide();
    jQuery("#rg_show_papers_link_" + goalId).show();
    jQuery("#rg_hide_papers_link_" + goalId).hide();
}

function rgGetPapers(goalId) {
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rgGetPapers",
            ajaxAction: "rgGetPapers",
            goalId: goalId,
            ajaxRequest: "true"
        },
        onComplete: handleRgGetPapers,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleRgGetPapers(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        fillInPapersDiv(json.goalId, json.papers);
    } else {
        errorPopup(json.message);
    }
}
function fillInPapersDiv(goalId, papers) {
    // Build the HTML
    html = '<td></td>'
         + '<td colspan="2">'
         + '<table id="rg_papers_table_" class="rg_papers_table">';
    for (i = 0; i < papers.length; i++) {
        var datasetId = papers[i].paperDto.datasetId;
        var paperId = papers[i].paperDto.paperId;
        var idx = i + 1;
        var citation = papers[i].paperDto.citation;
        if (citation == null || citation.length == 0) {
        	citation = "[blank citation]"
        }
        html += '<tr>'
              + '<td>' + idx + '</td>'
              + '<td id="rg_papers_citation_td_' + paperId + '" class="rg_papers_citation_td">'
                  + citation + '</td>'
              + '<td><a class="rt-basic-link" href="javascript:rgRemovePaper('
                  + goalId + ","
                  + datasetId + ","
                  + paperId + ')">remove</a></td>'
              + '</tr>';
    }
    html += '</table>'
          //+ '<p><a class="rt-basic-link" href="#">add paper</a></p>'
          + '</td>';

    // Display the HTML
    jQuery("#rg_papers_row_" + goalId).html(html);
    jQuery("#rg_papers_title_" + goalId).show();
    jQuery("#rg_papers_row_" + goalId).show();
}

//-------------------------------------------
//-----  Research Goal : Remove Paper   -----
//-------------------------------------------

function rgRemovePaper(goalId, datasetId, paperId) {
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rgRemovePaper",
            ajaxAction: "rgRemovePaper",
            goalId: goalId,
            datasetId: datasetId,
            paperId: paperId,
            ajaxRequest: "true"
        },
        onComplete: handleRgRemovePaper,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleRgRemovePaper(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        successPopup(json.message);
        fillInPapersDiv(json.goalId, json.papers);
        jQuery("#rg_show_papers_link_" + json.goalId).html("show papers (" + json.papers.length + ")");
    } else {
        errorPopup(json.message);
    }
}

//------------------------------------------
//-----   Research Goal : Show Types   -----
//------------------------------------------

function rgShowTypes(goalId) {
    jQuery("#rg_show_types_link_" + goalId).hide();
    jQuery("#rg_hide_types_link_" + goalId).show();
    rgGetTypes(goalId);
    
}
function rgHideTypes(goalId) {
    jQuery("#rg_types_title_" + goalId).hide();
    jQuery("#rg_types_row_" + goalId).hide();
    jQuery("#rg_show_types_link_" + goalId).show();
    jQuery("#rg_hide_types_link_" + goalId).hide();
}

function rgGetTypes(goalId) {
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rgGetTypes",
            ajaxAction: "rgGetTypes",
            goalId: goalId,
            ajaxRequest: "true"
        },
        onComplete: handleRgGetTypes,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleRgGetTypes(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        var html = rgFillInTypes(json.goalId, json.typeList);
        jQuery("#rg_types_row_" + json.goalId).html(html);
        jQuery("#rg_types_title_" + json.goalId).show();
        jQuery("#rg_types_row_" + json.goalId).show();
    } else {
        errorPopup(json.message);
    }
}
function rgFillInTypes(goalId, types) {
    var html = '<td></td>'
             + '<td colspan="2">'
             + '<table id="rg_types_table_' + goalId + '" class="rg_types_table">';
    for (i = 0; i < types.length; i++) {
        var idx = i + 1;
        html += '<tr>'
              + '<td>' + idx + '.</td>'
              + '<td id="rg_' + goalId + '_types_label_td_' + types[i].id +'" class="rg_types_label_td">'
                  + types[i].label + '</td>'
              + '<td><a href="javascript:rgRemoveType('
                  + goalId + ',' + types[i].id +')" class="rt-basic-link">remove</a></td>'
              + '</tr>';
    }
    html += '</table>'
          //+ '<p><a class="rt-basic-link" href="javascript:rgAddType(' + goalId + ')">add type</a></p>'
          + '</td>';
    return html;
}

//------------------------------------------
//------ Research Goal : Remove Type -------
//------------------------------------------

function rgRemoveType(goalId, typeId) {
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.rgRemoveType",
            ajaxAction: "rgRemoveType",
            typeId: typeId,
            goalId: goalId,
            ajaxRequest: "true"
        },
        onComplete: handleRgGetTypes,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

//------------------------------------------
//-------- Paper with Goals : Get Goals ----
//------------------------------------------

function paperHideGoals(datasetId, paperId) {
    jQuery("#papers_goals_show_link_" + paperId).show();
    jQuery("#papers_goals_hide_link_" + paperId).hide();
    jQuery("#papers_goals_row_" + paperId).hide();
}
function paperGetGoals(datasetId, paperId) {
    jQuery("#papers_goals_show_link_" + paperId).hide();
    jQuery("#papers_goals_hide_link_" + paperId).show();

    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.paperGetGoals",
            ajaxAction: "paperGetGoals",
            datasetId: datasetId,
            paperId: paperId,
            ajaxRequest: "true"
        },
        onComplete: handlePaperGetGoals,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handlePaperGetGoals(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        if (jQuery("#papers_goals_row_" + json.paperId).length > 0) {
            var html = paperFillInGoals(json.datasetId, json.paperId, json.goals);
            jQuery("#papers_goals_row_" + json.paperId).html(html);
            jQuery("#papers_goals_row_" + json.paperId).show();
            jQuery("#papers_goals_show_link_" + json.paperId).html("show papers (" + json.goals.length + ")");
        } else {
            rgPageRefresh();
        }
    } else {
        errorPopup(json.message);
    }
}
function paperFillInGoals(datasetId, paperId, goals) {
    html = '<td></td>'
         + '<td colspan="2">'
         + '<table><tbody>';
    for (i = 0; i < goals.length; i++) {
        var idx = i + 1;
        html += '<tr>'
              + '<td>' + idx + '</td>'
              + '<td>' + goals[i].title + '</td>'
              + '<td><a href="javascript:paperRemoveGoal(' + goals[i].id + ',' + datasetId + ',' + paperId
                  + ')" class="rt-basic-link">remove</a></td>'
              + '</tr>';
    }
    html += '</tbody></table>'
          + '<p><a href="javascript:paperAddGoals(' + datasetId + ',' + paperId +')" '
          + 'class="rt-basic-link">add goals</a></p>';
          + '</td>';
    return html;
}

//-------------------------------------------
//------ Paper with Goals : Add Goals -------
//-------------------------------------------

function paperAddGoals(datasetId, paperId) {
    paperGetOtherGoals(datasetId, paperId);
}
function paperGetOtherGoals(datasetId, paperId) {
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.paperGetOtherGoals",
            ajaxAction: "paperGetOtherGoals",
            datasetId: datasetId,
            paperId: paperId,
            ajaxRequest: "true"
        },
        onComplete: handlePaperGetOtherGoals,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handlePaperGetOtherGoals(transport) {
    var json = transport.responseText.evalJSON(true);
    paperOpenAddGoalDialog(json.datasetId, json.paperId, json.otherGoals);
}
function paperOpenAddGoalDialog(datasetId, paperId, otherGoals) {
    var dialogDiv = document.getElementById('paperAddGoalsDialog');
    dialogDiv.innerHTML = "";
    
    var html = '<input id="datasetId" type="hidden" value="' + datasetId + '"</input>';
    html += '<input id="paperId" type="hidden" value="' + paperId + '"</input>';
    html += '<select multiple="multiple" id="paper-add-goal-select">';
    for (i = 0; i < otherGoals.length; i++) {
        html += '<option value="' + otherGoals[i].id + '">'
             + otherGoals[i].title + '</option>';
    }
    html += '</select>'
        
    dialogDiv.innerHTML = html;
    
    jQuery('#paper-add-goal-select').attr("size", otherGoals.length);
    
    jQuery('#paperAddGoalsDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        title : "Add Research Goals",
        width : 450,
        buttons : [ {
            id : "paper-add-goal-save-button",
            text : "Save",
            click : paperAddGoalsSave
        }, {
            id : "paper-add-goal-cancel-button",
            text : "Cancel",
            click : paperAddGoalsCancel
        } ]
    });

    jQuery('#paperAddGoalsDialog').dialog('open');
}
function paperAddGoalsCancel() {
    jQuery(this).dialog('close');
}
function paperAddGoalsSave() {
    var datasetId = jQuery('#datasetId').val();
    var paperId = jQuery('#paperId').val();
    var goalList = jQuery('#paper-add-goal-select').val().join();
    
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.paperAddGoalsSave",
            ajaxAction: "paperAddGoals",
            datasetId: datasetId,
            paperId: paperId,
            goalList: goalList,
            ajaxRequest: "true"
        },
        onComplete: handlePaperGetGoals,
        onException: function (request, exception) {
            throw(exception);
        }
    });
    
    jQuery(this).dialog('close');
}

//-------------------------------------------
//------ Paper with Goals : Remove Goal -----
//-------------------------------------------

function paperRemoveGoal(goalId, datasetId, paperId) {
    new Ajax.Request("ResearchGoalsEdit", {
        parameters: {
            requestingMethod: "ResearchGoalsEdit.paperRemoveGoal",
            ajaxAction: "paperRemoveGoal",
            goalId: goalId,
            datasetId: datasetId,
            paperId: paperId,
            ajaxRequest: "true"
        },
        onComplete: handlePaperGetGoals,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
