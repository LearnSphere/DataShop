//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2013
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 12463 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-07-02 13:28:54 -0400 (Thu, 02 Jul 2015) $
// $KeyWordsOff: $
//

var WHAT_CAN_DATASHOP_DO_COOKIE = "what_can_i_do_with_datashop_";
var OPENED = "opened";
var CLOSED = "closed";

// Needed to use both prototype and jQuery
jQuery.noConflict();

// jQuery's onload function
jQuery(document).ready(function() {
    requestTypesAndGoals("all");
});

function showTopDiv() {
    var displayStr = "inline";

    var displayStrInput = $('rg_heading_display');
    if (displayStrInput) { displayStr = displayStrInput.value; }

    topDiv = $("research_goals_heading");
    topDiv.style.display = displayStr;
    
    rgDiv = $("research_goals_home_page");
    rgDiv.style.display = "block";

    var isNewUser = false;
    var newUserFlagInput = $('new_user_flag');
    if (newUserFlagInput) { isNewUser = (newUserFlagInput.value == "true") ? true : false; }

    var isShow = false;

    var remoteUserInput = jQuery('#remote_user');
    var remoteUser = "";
    if (remoteUserInput) { remoteUser = remoteUserInput.val(); }

    var cookieStr = WHAT_CAN_DATASHOP_DO_COOKIE + remoteUser;

    var state = readCookie(cookieStr);
    if (state == null) {
        if (isNewUser) {
            isShow = true;
        }
    } else if (state == OPENED) {
        isShow = true;
    }

    if (isShow) {
        showWhatCanDsDo();
    } else {
        clearWhatCanDsDo();
    }
}

function hideTopDiv() {
    topDiv = $("research_goals_heading");
    topDiv.style.display = "none";
    
    rgDiv = $("research_goals_home_page");
    rgDiv.style.display = "none";
}

function requestTypesAndGoals(typeId) {
    new Ajax.Request("ResearchGoals", {
        parameters: {
            requestingMethod: "ResearchGoals.requestTypesAndGoals",
            researchGoalsAction: "requestTypesAndGoals",
            typeId: typeId,
            ajaxRequest: "true"
        },
        onComplete: handleTypesAndGoals,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function handleTypesAndGoals(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        if (json.types.length > 0) {
            fillInTypesDiv(json.types);
            fillInGoalsDiv(json.typeId, json.goals);
        }
    } else {
        errorPopup(json.message);
    }
}

function hasTypesFilledIn() {
    typesDiv = $("researcher_types");
    if (typesDiv.innerHTML.length > 0) {
        return true;
    }
    return false;
}

function fillInTypesDiv(types) {
    // Build the HTML
    if (types.length > 0) {
        divHtml = '<p class="ima">I\'m a(n)</p>';
        for (i = 0; i < types.length; i++) {
            href = 'javascript:showGoals(' + types[i].id + ')';
            var isParent = false;
            if (types[i].subTypeList.length > 0) {
                isParent = true;
                href = 'javascript:showSubTypes(' + types[i].id + ')';
            }
            divHtml += '<div class="type">';
            divHtml += '<a id="rt_' + types[i].id + '" class="rt_link" href="' + href + '">';
            divHtml += types[i].label;
            divHtml += '</a>';
            if (isParent) {
                hiddenDivHtml = '<div class="rt_sub_types" id="rt_sub_types_' + types[i].id + '"> ';
                hiddenDivHtml += '<ul class="sub_types">';
                for (j = 0; j < types[i].subTypeList.length; j++) {
                    subHref = 'javascript:showGoals(' + types[i].subTypeList[j].id + ')';
                    hiddenDivHtml += '<li class="sub_type"><a id="rt_';
                    hiddenDivHtml += types[i].subTypeList[j].id + '" ';
                    hiddenDivHtml += 'class="rt_link" href="' + subHref + '">';
                    hiddenDivHtml += types[i].subTypeList[j].label;
                    hiddenDivHtml += '</a></li>';
                }
                hiddenDivHtml += '</ul></div>';
                
                divHtml += hiddenDivHtml;
            }
            divHtml += '</div>';
        }
        // Display the HTML
        typesDiv = $("researcher_types");
        typesDiv.innerHTML = divHtml;
        showTopDiv();
    }
}

function showGoals(typeId) {
    requestGoals(typeId);
    rtAnchors = $('researcher_types').select( '.rt_link');
    rtAnchors.each(function(anchor) {
            anchor.removeClassName('selected');
        });
    rtAnchor = $("rt_" + typeId);
    if (rtAnchor) { rtAnchor.addClassName('selected'); }

    // Only log clicks for specific types, not the 'show all'.
    if (typeId) { logHomePageTypeClicked(typeId); }
}

function showSubTypes(typeId) {
    requestGoals(typeId);

    rtAnchors = $('researcher_types').select( '.rt_link');
    rtAnchors.each(function(anchor) {
            anchor.removeClassName('selected');
        });
    rtAnchor = $("rt_" + typeId);
    if (rtAnchor) { rtAnchor.addClassName('selected'); }

    // Only log clicks for specific types, not the 'show all'.
    if (typeId) { logHomePageTypeClicked(typeId); }
}

function requestGoals(typeId) {
    new Ajax.Request("ResearchGoals", {
        parameters: {
            requestingMethod: "ResearchGoalsHomePage.requestGoals",
            researchGoalsAction: "requestGoals",
            typeId: typeId,
            ajaxRequest: "true"
        },
        onComplete: handleGoals,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function handleGoals(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        fillInGoalsDiv(json.typeId, json.goals);
    } else {
        errorPopup(json.message);
    }
}

function fillInGoalsDiv(typeId, goals) {
    typeStr = '';
    if (typeId) {
        typeStr = '?typeId=' + typeId;
    }
    // Build the HTML
    divHtml = '<h3>Here are topics of interest</h3>';
    // Only give the 'show all' link if displaying a subset.
    if (typeId) {
        divHtml += '<div id="show_all"><a href="javascript:showGoals();">(show all)</a></div>';
    }
    divHtml += '<ul>';
    for (i = 0; i < goals.length; i++) {
        //href = 'ResearchGoals' +  typeStr + '#rg_' + goals[i].id;
        href = 'javascript:selectResearchGoal(' + typeId + ',' + goals[i].id + ')';
        divHtml += '<li>';
        divHtml += '<a href="' + href + '">';
        divHtml += goals[i].title;
        divHtml += '</a></li>';
    }
    divHtml += '</ul>';
    // Display the HTML
    goalsDiv = $("research_goals");
    goalsDiv.innerHTML = divHtml;
    goalsDiv.style.display = "inline";
}

function selectResearchGoal(typeId,goalId) {
    var newForm = document.createElement('FORM');
    newForm.id   = "submit_form";
    newForm.name = "submit_form";
    newForm.form = "text/plain";
    if (typeId == undefined) {
        newForm.action = "ResearchGoals#rg_" + goalId;
    } else {
        newForm.action = "ResearchGoals?typeId="+typeId+"#rg_" + goalId;
    }
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "requestingMethod";
    newInput.type  = "hidden";
    newInput.value = "ResearchGoalsHomePage.selectResearchGoal";
    newForm.appendChild(newInput);
    
    newInput = document.createElement('input');
    newInput.name  = "homePage";
    newInput.type  = "hidden";
    newInput.value = true;
    newForm.appendChild(newInput);
    
    newInput = document.createElement('input');
    newInput.name  = "typeId";
    newInput.type  = "hidden";
    newInput.value = typeId;
    newForm.appendChild(newInput);
    
    newInput = document.createElement('input');
    newInput.name  = "goalId";
    newInput.type  = "hidden";
    newInput.value = goalId;
    newForm.appendChild(newInput);
    
    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function logHomePageTypeClicked(typeId) {
    new Ajax.Request("ResearchGoals", {
        parameters: {
            requestingMethod: "ResearchGoalsHomePage.logUserActionTypeClicked",
            researchGoalsAction: "logHomePageTypeClicked",
            typeId: typeId,
            ajaxRequest: "true"
        },
        onComplete: doNothing,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function doNothing() {
}

function clearWhatCanDsDo() {
    jQuery('#what_can_ds_do_div').hide();
    jQuery('#show_what_can_ds_do_div').show();

    var remoteUserInput = jQuery('#remote_user');
    var remoteUser = "";
    if (remoteUserInput) { remoteUser = remoteUserInput.val(); }

    var cookieStr = WHAT_CAN_DATASHOP_DO_COOKIE + remoteUser;
    createCookie(cookieStr, CLOSED, 100);
}

function showWhatCanDsDo() {
    jQuery('#show_what_can_ds_do_div').hide();    
    jQuery('#what_can_ds_do_div').show();

    var remoteUserInput = jQuery('#remote_user');
    var remoteUser = "";
    if (remoteUserInput) { remoteUser = remoteUserInput.val(); }

    var cookieStr = WHAT_CAN_DATASHOP_DO_COOKIE + remoteUser;
    createCookie(cookieStr, OPENED, 100);
}
