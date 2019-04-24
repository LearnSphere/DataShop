//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 15403 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2018-07-25 16:03:24 -0400 (Wed, 25 Jul 2018) $
// $KeyWordsOff: $
//

/**
 * Skill model list object
 * This object knows how to create the view for and make all calls
 * to the server to faciliate the changing of skill models
 */
function SkillModelList(aDiv, aSecondaryFlag) {

    this.listDiv = aDiv;
    this.nameField = null;

    if (aDiv.id != 'stepExportSkillModel') {
        this.listDiv.insert(
            '<div id="kcModelNavHeader" class="navigationBoxHeader"><h2>KC Models</h2>' +
            '</div>');
    }

    if (aSecondaryFlag) {
        this.displaySecondaryFlag = aSecondaryFlag;
    } else {
        this.displaySecondaryFlag = false;
    }

    this.primaryModelList = null;
    this.secondaryModelList = null;

    var browser = navigator.userAgent.toLowerCase();
    this.isOpera = browser.indexOf("opera")!= -1;
    this.isIE = navigator.appName ==("Microsoft Internet Explorer");

    this.primaryRow = null;
    this.secondaryRow = null;

    this.skillModelSelect;
    this.secondarySkillModelSelect;

    this.skillModelsNavBox;

    this.initLists();
}

/**
 * Initialize both sets of lists via an ajax call that goes to
 * initSkillModelList and initSecondarySkillModelList in this object
 */
SkillModelList.prototype.initLists = function(){

    new Ajax.Request('nav', {
        parameters: {
            requestingMethod: "SkillModelList.initLists",
            datasetId: dataset,
            getSkillModelList: "primary"
        },
        onComplete:
            this.initSkillModelList.bindAsEventListener(this),
        onException: function (request, exception) {
            throw(exception);
        }
    });

    new Ajax.Request('nav', {
        parameters: {
            requestingMethod: "SkillModelList.initLists",
            datasetId: dataset,
            getSkillModelList: "secondary"
        },
        onComplete: this.initSecondarySkillModelList.bindAsEventListener(this),
        onException: function (request, exception) {
            throw(exception);
        }
    });

}

/**
 * Takes an ajax reponse in the form of a javascript array from the server
 * containing the information required to build the PRIMARY skill model list
 */
SkillModelList.prototype.initSkillModelList = function(transport){
    var data = transport.responseText.evalJSON(true);
    this.primaryModelList = eval(data.kcmList);
    this.kcmSortBy = data.kcmSortBy;
    this.kcmSortByAsc = data.kcmSortByAsc;
    if (this.primaryModelList && this.secondaryModelList) {
        this.createView(this.kcmSortBy, this.kcmSortByAsc);
    }

}

/**
 * Takes an ajax reponse in the form of a javascript array from the server
 * containing the information required to build the SECONDARY skill model list
 */
SkillModelList.prototype.initSecondarySkillModelList = function(transport){
    var data = transport.responseText.evalJSON(true);
    this.secondaryModelList = eval(data.kcmList);
    if (this.primaryModelList && this.secondaryModelList) {
        this.createView(this.kcmSortBy, this.kcmSortByAsc);
    }
}

/**
 * Gets the currently selected skill models as a 2 size array.
 * Returns an array of size 2 where the first element is the primary selected model
 * and the second element is the secondary.
 */
SkillModelList.prototype.getSelectedModels = function() {
    var listArray = new Array();
    if (this.skillModelSelect) {
        listArray[0] = this.skillModelSelect.options[this.skillModelSelect.selectedIndex].text;

        if (this.displaySecondaryFlag) {
            listArray[1] = this.secondarySkillModelSelect.options[this.secondarySkillModelSelect.selectedIndex].text;
        }
    }
    return listArray;
}

/**
 * Turns on the display for the secondary skill model.  The dom objects always
 * exist for the secondary skill models, they are just turned off via CSS.
 */
SkillModelList.prototype.displaySecondary = function() {
    if (this.secondaryRow && this.skillModelSelect.options.length > 1) {
        if (this.isIE) {
            this.secondaryRow.style.display="block";
        } else {
            this.secondaryRow.style.display="table-row";
        }
    }
    this.displaySecondaryFlag = true;
}

/**
 * Turns off the secondary skill model list by setting the CSS to display="none"
 */
SkillModelList.prototype.hideSecondary = function() {
    if (this.secondaryRow) {
        this.secondaryRow.style.display="none";
    }
    this.displaySecondaryFlag = false;
}

SkillModelList.prototype.refresh = function() {
    this.initLists();
}

/**
 * Creates all the DOM objects needed for both the primary and secondary skill models.
 * Use the information in this object to correctly create both lists and attach it to the
 * navigation panel.
 */
SkillModelList.prototype.createView = function(kcmSortBy, kcmSortByAsc){

    if (this.primaryModelList.length == 0) {
        para = document.createElement('p');
        para.appendChild(document.createTextNode('No knowledge component models exist for this dataset.'));
        this.listDiv.appendChild(para);
        return;
    }

    // If this was called by way of 'refresh', remove existing tables...
    // ... but first make note of skill_model_select state.
    var modelSelector = document.getElementById("skill_model_select");
    var smsFlag = false;
    if (modelSelector) {
        smsFlag = modelSelector.disabled;
    }

    var tableList = this.listDiv.getElementsByTagName("table");
    for (var i = 0; i < tableList.length; i++) {
        this.listDiv.removeChild(tableList[i]);
    }

    //create the table that positions
    table = document.createElement('table');
    this.listDiv.appendChild(table);

    tbody = document.createElement('tbody');
    table.appendChild(tbody);

    this.primaryRow = document.createElement('tr');
    this.secondaryRow = document.createElement('tr');

    tbody.appendChild(this.primaryRow);
    tbody.appendChild(this.secondaryRow);

    //create the primary list.
    if (this.listDiv.id != 'stepExportSkillModel') {
        cell = document.createElement('td');
        cell.id = "primary_kc_model_label";
        this.primaryRow.appendChild(cell);

        var primaryLabel = document.createElement('label');
        primaryLabel = document.createTextNode("Primary");
        cell.appendChild(primaryLabel);
    }




    cell = document.createElement('td');
    this.primaryRow.appendChild(cell);

    this.skillModelSelect = document.createElement('select');
    this.skillModelSelect.id = "skill_model_select";
    this.skillModelSelect.className = "skill_model_select";
    this.skillModelSelect.name = "skill_model";
    this.skillModelSelect.disabled = smsFlag;
    this.skillModelSelect.onchange = this.selectSkillModelHandler.bindAsEventListener(this);
    this.skillModelSelect.title = "Sorted by " + kcmSortBy + ", " + kcmSortByAsc + ". To change, visit the KC Models page by clicking 'details' above.";
    this.skillModelSelect.options.length = 0;
    for (var i = 0; i < this.primaryModelList.length; i++) {
        toDisplay = this.primaryModelList[i][2];
        if (toDisplay.length > 15) {
            toDisplay = toDisplay.substr(0,12) + "..."
        } else {
            toDisplay = toDisplay;
        }
        this.skillModelSelect.options[i] =
                new Option(toDisplay, this.primaryModelList[i][0]);

        this.skillModelSelect.options[i].title = this.primaryModelList[i][2];
        if (this.primaryModelList[i][1]) {
            this.skillModelSelect.selectedIndex=i;
        }
    }
    cell.appendChild(this.skillModelSelect);

    //create the secondary list.
    cell = document.createElement('td');
    this.secondaryRow.appendChild(cell);

    var secondaryLabel = document.createElement('label');
    secondaryLabel = document.createTextNode("Secondary");

    cell.appendChild(secondaryLabel);

    cell = document.createElement('td');
    this.secondaryRow.appendChild(cell);

    this.secondarySkillModelSelect = document.createElement('select');
    this.secondarySkillModelSelect.id = "secondary_skill_model_select";
    this.secondarySkillModelSelect.name = "skill_model";
    this.secondarySkillModelSelect.onchange = this.selectSecondarySkillModelHandler.bindAsEventListener(this);
    this.secondarySkillModelSelect.title = "Sorted by " + kcmSortBy + ", " + kcmSortByAsc + ". To change, visit the KC Models page by clicking 'details' above.";
    this.secondarySkillModelSelect.options.length = 0;
    this.secondarySkillModelSelect.options[0] = new Option("None", "none_selected");
    for (var i = 0; i < this.secondaryModelList.length; i++) {

        toDisplay = this.secondaryModelList[i][2];
        if (toDisplay.length > 15) {
            toDisplay = toDisplay.substr(0,12) + "..."
        } else {
            toDisplay = toDisplay;
        }
        this.secondarySkillModelSelect.options[i+1] =
            new Option(toDisplay, this.secondaryModelList[i][0]);

        this.secondarySkillModelSelect.options[i+1].title = this.secondaryModelList[i][2];
        if (this.secondaryModelList[i][1]) {
            this.secondarySkillModelSelect.selectedIndex=i+1;
        }
    }
    cell.appendChild(this.secondarySkillModelSelect);

    if (this.primaryModelList.length < 2 || this.displaySecondaryFlag == false) {
        this.secondaryRow.style.display="none";
    }

    if (this.listDiv.id == 'skillModels') {
        if (this.skillModelsNavBox == undefined) {
            this.skillModelsNavBox = new NavigationBox.Base("skillModels");

            $("skillModels").down('span').insert({before :  '<span id="kcModelsLinkLink" title="Go to the KC Models page" class="wrench"><a href="javascript:goToKcModelsPage()">details</a></span>'});
        }
    }
}

/**
 * Build and submit a form for the primary or secondary skill model selection.
 */
function skillModelForm(nameId, inputName, modelId) {
    // get the dataset id from the url in the browser address bar
    // (we don't want the entire URL because it might contain a topSkillId)
    var url_parts = /.*\/(.*)\?.*datasetId=(\d+).*/.exec(window.location.href);
    var datasetId = url_parts[2];
    var servletName = url_parts[1];
    var newForm = document.createElement('FORM');
    newForm.setAttribute('name', nameId);
    newForm.setAttribute('id', nameId);
    newForm.setAttribute('form', 'text/plain');
    newForm.setAttribute('action', servletName + "?datasetId=" + datasetId);
    newForm.setAttribute('method', 'POST');

    var newInput = document.createElement('input');
    newInput.name = inputName;
    newInput.type = "hidden";
    newInput.value = modelId;

    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

/**
 * Event handler called whenever the secondary skill model onchange event fires.
 * It simply grabs the current selected options, builds a "fake" form and posts it
 * to the current page.  This will result in a full page refresh whenever a skill model
 * is changed.
 */
SkillModelList.prototype.selectSecondarySkillModelHandler = function() {
    var modelId = this.secondarySkillModelSelect.options[this.secondarySkillModelSelect.selectedIndex].value;

    if (modelId != null) {
        skillModelForm('secondary_skill_model_select_form', "secondarySkillModelId", modelId);
    }
}

/**
 * Event handler called whenever the primary skill model onchange event fires.
 * It simply grabs the current selected options, builds a "fake" form and posts it
 * to the current page.  This will result in a full page refresh whenever a skill model
 * is changed.
 */
SkillModelList.prototype.selectSkillModelHandler = function() {
    var modelId = this.skillModelSelect.options[this.skillModelSelect.selectedIndex].value;

    if (modelId != null) {
        skillModelForm('skill_model_select_form', "skillModelId", modelId);
    }
}

/**
 * Build and submit a form to go to the Dataset Info -> KC Models page for the given dataset.
 */
function goToKcModelsPage() {
    // get the dataset id from the url in the browser address bar
    // (we don't want the entire URL because it might contain a topSkillId)
    var url_parts = /.*\/(.*)\?.*datasetId=(\d+).*/.exec(window.location.href);
    var datasetId = url_parts[2];
    var servletName = "DatasetInfo"
    var newForm = document.createElement('FORM');
    newForm.setAttribute('name',   'KCModels_details_link');
    newForm.setAttribute('id',     'KCModels_details_link');
    newForm.setAttribute('form',   'text/plain');
    newForm.setAttribute('action', servletName + "?datasetId=" + datasetId);
    newForm.setAttribute('method', 'POST');

    var hiddenInput = document.createElement('input');
    hiddenInput.name  = "subtab";
    hiddenInput.type  = "hidden";
    hiddenInput.value = "models";
    newForm.appendChild(hiddenInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

