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
    initDropdown();
});

function initDropdown() {
    var dd = new DropDown( jQuery('#dd') );

    jQuery(document).click(function() {
        // all drop-downs
        jQuery('.wrapper-dropdown').removeClass('active');
    });
}

function showRelatedDatasetsAndPapers(goalId) {
    showAnchor = $("rg_show_datasets_" + goalId);
    showAnchor.style.display = "none";
    hideAnchor = $("rg_hide_datasets_" + goalId);
    hideAnchor.style.display = "inline";
    
    if (hasDatasets(goalId)) {
        datasetsDiv = $("rg_datasets_div_" + goalId);
        datasetsDiv.style.display = "inline";
    } else {
        requestDatasetsAndPapers(goalId);
    }
}

function hideRelatedDatasetsAndPapers(goalId) {
    showAnchor = $("rg_show_datasets_" + goalId);
    showAnchor.style.display = "inline";
    hideAnchor = $("rg_hide_datasets_" + goalId);
    hideAnchor.style.display = "none";

    datasetsDiv = $("rg_datasets_div_" + goalId);
    datasetsDiv.style.display = "none";
}

function requestDatasetsAndPapers(goalId) {
    inputField = $("toolNameField");

    new Ajax.Request("ResearchGoals", {
        parameters: {
            requestingMethod: "ResearchGoals.requestDatasetsAndPapers",
            researchGoalsAction: "requestDatasetsAndPapers",
            goalId: goalId,
            ajaxRequest: "true"
        },
        onComplete: handleDatasetsAndPapers,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleDatasetsAndPapers(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        fillInDatasetsDiv(json.goalId, json.datasets);
    } else {
        errorPopup(json.message);
    }
}

function hasDatasets(goalId) {
    datasetsDiv = $("rg_datasets_div_" + goalId);
    if (datasetsDiv.innerHTML.length > 0) {
        return true;
    }
    return false;
}

function fillInDatasetsDiv(goalId, datasets) {
    // Build the HTML
    divHtml = "<table class='datasets_papers'><tr><th>Dataset</th><th>Paper</th></tr>";
    for (i = 0; i < datasets.length; i++) {
        dsHref = 'DatasetInfo?datasetId=' + datasets[i].datasetId;
        divHtml += '<tr>';
        divHtml += '<td class="c1" id="rg_' + goalId + '_ds_' + datasets[i].datasetId + '"</td>';
        divHtml += '<p class="dataset_link">'
        divHtml += '<a class="rg_link" href="' + dsHref + '">';
        divHtml += datasets[i].datasetName;
        divHtml += '</a>';
        if (datasets[i].isPublicFlag) {
        divHtml += '&nbsp;<img title="This is a public dataset." alt="(public)" '
                 + 'id="public-icon-img_rg_'+ goalId + '_ds_' + datasets[i].datasetId
                 + '" src="images/users.gif">';
        } else {
        divHtml += '&nbsp;<img title="This is a private dataset." alt="(private)" '
                 + 'id="public-icon-img_rg_'+ goalId + '_ds_' + datasets[i].datasetId
                 + '" src="images/lock.png">';
        }
        divHtml += '</p>';
        divHtml += '</td><td class="c2">';
        for (j = 0; j < datasets[i].paperList.length; j++) {
            paperHref = 'DownloadPaper?fileName='
                + datasets[i].paperList[j].fileName
                + '&fileId='
                + datasets[i].paperList[j].fileId
                + '&datasetId='
                + datasets[i].datasetId;
            fullCitation = datasets[i].paperList[j].citation;
            //truncatedCitation = fullCitation.substring(0,50);
            truncatedCitation = datasets[i].paperList[j].truncatedCitation;
            divHtml += '<p class="paper_link">';
            divHtml += '<a class="rg_link" href="' + paperHref + '" title="' + fullCitation + '">';
            divHtml += truncatedCitation;
            if (fullCitation > truncatedCitation) {
                divHtml += ' ...';
            }
            //divHtml += datasets[i].paperList[j].fileNameWithSize;
            divHtml += '</a>';
            divHtml += "</p>";
        }
        divHtml += "</td><tr>"
    }
    divHtml += "</table>"

    // Display the HTML
    datasetsDiv = $("rg_datasets_div_" + goalId);
    datasetsDiv.innerHTML = divHtml;
    datasetsDiv.style.display = "inline";
}

//-------------------------------------------------------
// Update the page with new Research Goals after the user
// modifies the Researcher Type.
//-------------------------------------------------------
function setResearcherType(typeId) {
    var rtSelectionElement = jQuery("#rt_selection");
    rtSelectionElement.val(typeId);
    
    var newForm = document.createElement('FORM');
    newForm.id   = "submit_form";
    newForm.name = "submit_form";
    newForm.form = "text/plain";
    newForm.action = "ResearchGoals?typeId="+typeId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "requestingMethod";
    newInput.type  = "hidden";
    newInput.value = "ResearchGoals.setResearcherType";
    newForm.appendChild(newInput);
    
    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

//-------------------------------------------------------
// DropDown fanciness
//-------------------------------------------------------

function DropDown(el) {
    this.dd = el;
    this.placeholder = this.dd.children('span');
    this.opts = this.dd.find('ul.dropdown > li');
    this.val = '';
    this.index = -1;
    this.initEvents();
}
DropDown.prototype = {
    initEvents : function() {
        var obj = this;

        obj.dd.on('click', function(event){
            jQuery(this).toggleClass('active');
            return false;
        });

        obj.opts.on('click',function(){
            var opt = jQuery(this);
            obj.val = opt.text();
            obj.index = opt.index();
            //this line isn't necessary
            //obj.placeholder.text(obj.val);
            
            //Get researcher type id from the LI
            var researcherTypeId = getResearcherTypeId(obj.index);
            //Refresh page with researcher type parameter
            setResearcherType(researcherTypeId);
        });
    },
    getValue : function() {
        return this.val;
    },
    getIndex : function() {
        return this.index;
    }
}

function getRtId() {
    var rtSelectionElement = jQuery("#rt_selection");
    return rtSelectionElement.val();
}

function getResearcherTypeId(listIndex) {
    var listElement = jQuery("ul#rt_dropdown > li").eq(listIndex);
    var elementId = listElement.attr("id");
    var typeId = elementId.substring(elementId.lastIndexOf("_")+1,elementId.length);
    return typeId;
}

function selectResearchGoal(goalId) {
    var typeId = getRtId();
    logGoalPageGoalClicked(typeId, goalId);
    goToGoal(goalId);
}

function goToGoal(goalId) {
    window.location.hash = "#rg_" + goalId;
}

function logGoalPageGoalClicked(typeId, goalId) {
    new Ajax.Request("ResearchGoals", {
        parameters: {
            requestingMethod: "ResearchGoals.logGoalPageGoalClicked",
            researchGoalsAction: "logGoalPageGoalClicked",
            typeId: typeId,
            goalId: goalId,
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
