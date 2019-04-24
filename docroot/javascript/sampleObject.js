//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 12840 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-12-18 12:12:17 -0500 (Fri, 18 Dec 2015) $
// $KeyWordsOff: $
//

function Sample() {
    this.filterArray = new Array();
    this.name = "";
    this.nameField = null;

    this.description = "";
    this.descriptionField = null;

    this.isGlobal = false;
    this.isGlobalField = null;

    this.sampleId = null;
    this.statusString = "";
    this.isOwner;
    this.aggregateProgressBar;

    /** Indicated if a change has occurred with respect to a filter. */
    this.filterEventFlag = false;

    this.fadedBackgroundDiv = null; //the faded background div for easy access.

    var browser = navigator.userAgent.toLowerCase();
    this.isOpera     = browser.indexOf("opera")!= -1;

    this.isSamplesPage = false;
}

Sample.prototype.loadSample=function(sampleId){
    if (sampleId != null) {
        this.isSamplesPage = true;
        //build the parameters string
        new Ajax.Request(window.location.href, {
            parameters: {
                requestingMethod: "sampleObject.loadSample",
                datasetId: dataset,
                loadSample: "true",
                s2dSelector: "true",
                sampleSelectorId: sampleId,
                ds_content: "samples",
                ds_request: "content"
            },
            onComplete: this.proccessLoad.bindAsEventListener(this),
            onException: function (request, exception) { throw(exception); }
        });
    }
}

//loads the sample information
//sample information should be returned as a tab delineated file with...
//SampleId[tab]isOwner[tab]Sample Name[tab]Sample Description[tab]Is Global[newline]
//followed by a filter list...
//FilterClass[tab]FilterAttribute[tab]FilterString[tab]FilterOperator[newline]
Sample.prototype.proccessLoad=function(transport){

    //indexes for sample info
    var idIndex = 0;
    var isOwnerIndex = 1;
    var nameIndex = 2;
    var descIndex = 3;
    var isGlobalIndex = 4;

    //indexes for filter info
    var classIndex = 0;
    var attribIndex = 1;
    var stringIndex = 2;
    var operIndex = 3;

    var response = transport.responseText;
    var responseArrays = response.split("\n");
    var sampleInfoArray = responseArrays[0].split("\t");


    //set the sample information first.
    this.sampleId = sampleInfoArray[idIndex];
    this.name =  sampleInfoArray[nameIndex];
    this.description = sampleInfoArray[descIndex];

    if (sampleInfoArray[isGlobalIndex] == "true"){
        this.isGlobal = true;
    } else {
        this.isGlobal = false;
    }

    if (sampleInfoArray[isOwnerIndex] == "true" ) {
        this.isOwner = true;
    } else {
        this.isOwner = false;
    }
    for (h = 1; h < responseArrays.length; h++) {
        filterInfoArray = responseArrays[h].split("\t");
        this.addFilter(filterInfoArray[classIndex], filterInfoArray[attribIndex],
                                 filterInfoArray[operIndex], filterInfoArray[stringIndex]);
    }

    this.createView();
}

Sample.prototype.addFilterToList = function(filter) {
    var filterListBody = document.getElementById('filterListBody');
    filterListBody.appendChild(filter.createView());
    this.fireFoxHack();

}

Sample.prototype.fireFoxHack = function() {
    //ugly hack to make firefox redraw everythign proper like.
    var filterCell = document.getElementById('filterListCell');
    var newRow = document.createElement('tr');
    filterCell.parentNode.parentNode.appendChild(newRow);
    var newCell = document.createElement('td');
    newRow.appendChild(newCell);
    filterCell.parentNode.parentNode.removeChild(newRow);
}


Sample.prototype.updatePreview = function() {
    var postParams = {
        requestingMethod: "sampleObject.updatePreview",
        datasetId: dataset,
        samplePreviewUpdate: "true"};
    var filterArray = this.filterArray;

    for (i = 0; i < filterArray.length; i++) {
        var filter = filterArray[i];
        postParams["filterClass_" + i] = filter.category;
        postParams["filterAttribute_" + i] = filter.attribute;
        if (filter.dbString == undefined || filter.dbString == null) {
            postParams["filterString_" + i] = "";
        } else {
            postParams["filterString_" + i] = filter.dbString;
        }

        if (filter.operator == undefined || filter.operator == null) {
            postParams["filterOperator_" + i] = "";
        } else {
            postParams["filterOperator_" + i] = filter.operator;
        }
        postParams["filterPosition_" + i] = i;
    }

    new Ajax.Request(window.location.href, {
        parameters: postParams,
        onComplete: this.displayPreview.bindAsEventListener(this),
        onException: function (request, exception) { throw(exception); }
    });
}


Sample.prototype.displayPreview = function(transport) {

    var successIndex = 0
    var errorTextIndex = 1;
    var classIndex = 1;
    var attribIndex = 2;
    var operIndex = 3;
    var filterStringIndex = 4;


    var response = transport.responseText;

    var previewDiv = document.getElementById('dataPreview');

    responseArray = response.split('|');

    if (responseArray[0] == "FILTER_ERROR") {

        var output = "";

        for (i=0; i < this.filterArray.length; i++) {
            if ( this.filterArray[i].category == responseArray[classIndex]
              && this.filterArray[i].attribute == responseArray[attribIndex]
              && this.filterArray[i].operator == responseArray[operIndex]) {
                output = this.filterArray[i].getFilterError();
                this.filterArray[i].hasError=true;
                this.selectFilter(this.filterArray[i]);
            }
        }

        this.updateStatus(output);
        previewDiv.innerHTML = "";

    } else if (responseArray[0] == "ERROR") {

        var output = "<p class=\"example\">Unable to get example information.</p>";
        this.updateStatus(output);
        previewDiv.innerHTML = "";
        if ($("saveSampleButton")) { $("saveSampleButton").disabled = true; }

    } else if (responseArray[0] == "MAX_LENGTH_ERROR") {
        var output = "<p class=\"example\">" + responseArray[1] + "</p>";
        this.updateStatus(output);
        previewDiv.innerHTML = "";
        if ($("saveSampleButton")) { $("saveSampleButton").disabled = true; }

    } else {
        previewDiv.innerHTML = response;
    }
}


Sample.prototype.save = function(saveAsNew) {

    if ($("sampleSureDiv")) { $("sampleSureDiv").hide(); }
    saveAsNew = (saveAsNew == null) ? false : saveAsNew;

    //build the parameters string
    var postParams = {
        requestingMethod: "sampleObject.save",
        datasetId: dataset,
        sampleSave: "true"};

    //add in the sample variables.
    if (this.sampleId != null && this.isOwner == true && !saveAsNew) {
        postParams['sampleSelectorId'] = this.sampleId
    }

    if ($F('sampleNameInput') == null || $F('sampleNameInput') == "") {
        this.updateStatus("<p class=\"error\">Sample must have a name.</p>");
        if ($("saveSampleButton")) { $("saveSampleButton").disabled = false; }
        if ($("saveAsNewButton")) { $("saveAsNewButton").disabled = false; }
        return;
    }

    postParams['sampleName'] = $F('sampleNameInput');

    if ($F('sampleDescriptionInput') == null) {
        postParams['sampleDescription'] = ""
    } else {
        postParams['sampleDescription'] = $F('sampleDescriptionInput');
    }

    var isGlobal = ($F('sampleGlobal')) ? true : false;

    postParams['isGlobal'] = isGlobal;

    //make sure all the filters are saved.
    for (m = 0; m < this.filterArray.length; m++) {
        if(this.filterArray[m].openForEdit == true) {
            this.filterArray[m].setFilter();
            if (this.filterArray[m] && this.filterArray[m].hasError) {
                if ($("saveSampleButton")) { $("saveSampleButton").disabled = false; }
                if ($("saveAsNewButton")) { $("saveAsNewButton").disabled = false; }
                return;
            }
        }
    }

    //add in filter information.
    for (i = 0; i < this.filterArray.length; i++) {

        if(this.filterArray[i].openForEdit == true) {
            this.filterArray[i].setFilter();
        }

        if (this.filterArray[i].category == null) {
            continue;
        }

        if (this.filterArray[i].attribute == null) {
            continue;
        }

        //postParams = postParams + "&filterClass_" + i + "=" + encodeURIComponent(this.filterArray[i].category);
        postParams['filterClass_' + i] = this.filterArray[i].category;
        //postParams = postParams + "&filterAttribute_" + i + "=" + encodeURIComponent(this.filterArray[i].attribute);
        postParams['filterAttribute_' + i] = this.filterArray[i].attribute;
        //postParams = postParams + "&filterString_" + i + "=" + encodeURIComponent(this.filterArray[i].dbString);
        postParams['filterString_' + i] = this.filterArray[i].dbString;
        //postParams = postParams + "&filterOperator_" + i + "=" + encodeURIComponent(this.filterArray[i].operator);
        postParams['filterOperator_' + i] = this.filterArray[i].operator;
        //postParams = postParams + "&filterPosition_" + i + "=" + encodeURIComponent(i);
        postParams['filterPosition_' + i] = i;
    }

    new Ajax.Request(window.location.href, {
        parameters: postParams,
        onComplete: this.proccessSave.bindAsEventListener(this),
        onException: function (request, exception) { throw(exception); }
    });
}


Sample.prototype.proccessSave=function(transport) {
    var response = transport.responseText;
    responseArray = response.split('|');
    this.proccessSaveResponse(responseArray);
}

Sample.prototype.proccessSaveResponse=function(responseArray) {
    if (responseArray[0] == "ERROR") {
        if ($("saveSampleButton")) { $("saveSampleButton").disabled = false; }
        if ($("saveAsNewButton")) { $("saveAsNewButton").disabled = false; }

        var output = "<p class=\"error\">" + responseArray[1] + "</p>";
        this.updateStatus(output);
    } else if (responseArray[0] == "SUCCESS") {
        var output = "<p class=\"correct\">" + responseArray[1] + "</p>";
        this.isOwner=true; //sample is now owned by the user.
        this.sampleId=responseArray[2];
        this.closeSampleSelector();

        if(responseArray[3] == "AGGREGATING") {
            if (!this.aggregateProgressBar) {
                var progressBarParams = {
                        allowCancel: false,
                        checkParam: 'aggregator_check',
                        cancelParam: 'aggregator_cancel',
                        explanationText: "Calculating sample data..."
                    };
                if (this.isSamplesPage) {
                    progressBarParams = {
                            allowCancel: false,
                            checkParam: 'aggregator_check',
                            cancelParam: 'aggregator_cancel',
                            explanationText: "Calculating sample data...",
                            s2dSelector: "true"
                        }
                }
                aggregateProgressBar =
                    new ProgressBar(
                        this.successAndClose.bindAsEventListener(this),
                        window.location.href,
                        progressBarParams
                        );
            }

        } else {
            this.successAndClose();
        }
    } else {
        var output = "<p class=\"error\">An unknown error occurred while trying to save your sample.</p>";
        this.updateStatus(output);
    }
}

Sample.prototype.successAndClose= function() {
        var saveDiv = document.getElementById("sampleSave");
        if (saveDiv == null) {
            var saveDiv = document.createElement('div');
        }
        saveDiv.id = "sampleSave";
        saveDiv.style.position="absolute";
        saveDiv.style.width="100%";
        saveDiv.style.textAlign="center";
        saveDiv.style.top="200px";
        saveDiv.style.left="0px";

        var centeredSaveDiv = document.createElement('div');
        centeredSaveDiv.style.position="relative";
        centeredSaveDiv.style.width="400px";
        centeredSaveDiv.style.height="200px";
        centeredSaveDiv.style.margin="0 auto";
        saveDiv.appendChild(centeredSaveDiv);

        var saveP = document.createElement('p');
        saveP.className="correct";
        saveP.appendChild(document.createTextNode(responseArray[1]));
        centeredSaveDiv.appendChild(saveP);

        document.getElementsByTagName('body').item(0).appendChild(saveDiv);
        setTimeout(this.clearSaveDiv.bindAsEventListener(this), 2000);
        //selectSample(this.sampleId); //this does the necessary page refresh to get the data
        //DS956:  (Report content doesn't display after creating a new sample)
        //Not sure why it was doing the 'refreshSampleList' instead of the 'selectSample'.
        //Maybe there is something I still do not understand. [ads 8/13/09]
        refreshSampleList(this.sampleId); //this now does a full page refresh [ads 8/19/2009]
}

Sample.prototype.clearSaveDiv=function() {
    var saveDiv = document.getElementById("sampleSave");
    if (saveDiv != null) {
        saveDiv.parentNode.removeChild(saveDiv);
    }
}

/**
 *  This will clear out the sample selector and empty displays.
 */
Sample.prototype.clear=function() {

    this.clearOld();

    this.nameField.value = "";
    this.descriptionField.value = "";
    this.isGlobalField.checked = false;

    this.updatePreview();
}

/**
 *  This function is for clearing out all the
 *  old items w/o updating displays.
 */
Sample.prototype.clearOld=function() {

    this.deleteAllFilters();
    this.name = "";
    this.description = "";
    this.isGlobal = false;
    this.statusString = "";
    this.sampleId = null;
    this.isOwner = true;
}

Sample.prototype.selectFilter=function(filter){
    /* Can allow for more than one selected filter now */
    for(i=0; i< this.filterArray.length; i++) {

        if(filter == this.filterArray[i]) {
            this.filterArray[i].viewRow.id="selectedFilter";
        } else {
            this.filterArray[i].viewRow.id="";
        }
    }

    filter.viewRow.className="selectedFilter";
    this.editFilterItem(filter);
}

Sample.prototype.editFilterItem=function(filter) {

    this.updateStatus("");

    if (filter != null) {
        filter.displayFilterBuilder();
    } else {
        var filter = new Filter();
        filter.clearBuilder();
    }
}

Sample.prototype.displayEmptyFilterBuilder=function() {
    var filterBuilderDiv = document.getElementById('filterBuilder');

    //remove everything inside the div.
    while (filterBuilderDiv.firstChild != null) {
        filterBuilderDiv.removeChild(filterBuilderDiv.firstChild);
    }
}

Sample.prototype.updateStatus = function(text){
    this.statusString = text;
    this.displayStatus();
}

Sample.prototype.displayStatus= function() {
    var statusDiv = document.getElementById('sampleStatus');
    statusDiv.innerHTML = this.statusString;
}

Sample.prototype.deleteAllFilters = function() {
    var filterList = document.getElementById('filterListBody');
    if (filterList != null) {
        while (filterList.firstChild != null) {
            filterList.removeChild(filterList.firstChild);
        }
    }

    if (this.filterArray.length < 1) {
        var filter = new Filter();
        filter.clearBuilder();
        filter = null;
    } else {
        this.filterArray[0].clearBuilder();
    }

    this.filterArray = new Array();
}


/********************************************************************
 *
 *          Event handlers for changing sample information.
 *
 *  NOTE: The event handlers' "this" does not refer to the sample
 *    object, but to the object on which the event was invoked.
 *
 ********************************************************************/

Sample.prototype.addFilter=function(cat, attrib, oper, fString) {
    filter = new Filter(cat, attrib, oper, fString, this)
    this.filterArray.push(filter);
    return filter;
}


Sample.prototype.deleteSelectedFilter=function() {

    var selectedRow = document.getElementById("selectedFilter");
    if (selectedRow != null) {
        selectedRow.filter.sample.deleteFilter(selectedRow.filter);
        selectedRow.filter.sample.updatePreview();
        selectedRow.parentNode.removeChild(selectedRow);
    }
}


Sample.prototype.deleteFilter=function(filter) {

    if (filter.viewRow.id == "selectedFilter") {
        filter.clearBuilder();
    }

    for (i = 0; i < this.filterArray.length; i++) {
        if (this.filterArray[i] == filter) {
            filterIndex = i;
            if (filterIndex == 0) {
                this.filterArray.shift();
            } else if (filterIndex == this.filterArray.length-1) {
                this.filterArray.pop();
            } else {

                //get the lower portion of the list.
                var lowerList = this.filterArray.slice(0, filterIndex);

                //get the upper portion of the list.
                var upperList = this.filterArray.slice(filterIndex+1, this.filterArray.length);

                this.filterArray[filterIndex] = null;
                //join the upper and lower into the new filterArray.
                this.filterArray = new Array();
                for (i = 0; i < lowerList.length; i++) {
                    this.filterArray.push(lowerList[i]);
                }
                for (i = 0; i < upperList.length; i++) {
                    this.filterArray.push(upperList[i]);
                }
            }
        }
    }
    this.filterEventFlag = true;
}

Sample.prototype.clearThisSample = function() {
    this.sample.clear();
}


Sample.prototype.addNewFilter = function() {
    cat = $('filterCategory').options[$('filterCategory').selectedIndex].value;
    attrib = $('columnName').options[$('columnName').selectedIndex].value;
    filter = this.addFilter(cat, attrib);
    this.addFilterToList(filter);
    this.updatePreview();
    this.fireFoxHack();
    this.filterEventFlag = true;
}


//Takes the category selection and displays the proper column(s).
Sample.prototype.selectFilterColumnList = function() {

    var category = $('filterCategory').options[$('filterCategory').selectedIndex].value;

    if (category == "transaction") {
        $('columnName').options.length = 0;
        $('columnName').options[0]= new Option('Time Stamp','transactionTime');
        $('columnName').options[1]= new Option('Attempt Number','attemptAtSubgoal');
        $('columnName').options[2]= new Option('Student Response Type','transactionTypeTool');
        $('columnName').options[3]= new Option('Tutor Response Type','transactionTypeTutor');
        $('columnName').options[4]= new Option('Student Response Subtype','transactionSubtypeTool');
        $('columnName').options[5]= new Option('Tutor Response Subtype','transactionSubtypeTutor');
    } else if (category == "condition") {
        $('columnName').options.length = 0;
        $('columnName').options[0]= new Option('Name','conditionName');
        $('columnName').options[1]= new Option('Type','type');
    } else if (category == "datasetLevel") {
        $('columnName').options.length = 0;
        $('columnName').options[0]= new Option('Name','levelName');
        $('columnName').options[1]= new Option('Title','levelTitle');
    } else if (category == "school") {
        $('columnName').options.length = 0;
        $('columnName').options[0]= new Option('Name','schoolName');
    } else if (category == "student") {
        $('columnName').options.length = 0;
        $('columnName').options[0]= new Option('Anon Id','anonymousUserId');
    } else if (category == "problem") {
        $('columnName').options.length = 0;
        $('columnName').options[0]= new Option('Name','problemName');
        $('columnName').options[1]= new Option('Description','problemDescription');
    } else if (category == "customField") {
        $('columnName').options.length = 0;
        $('columnName').options[0]= new Option('Name','customFieldName');
        $('columnName').options[1]= new Option('Value','customFieldValue');
    } else {
        this.updateStatus("<p class=\"error\">Unknown Filter Category. This error should never occur, please "
           + "contact the datashop team with an explantiona of how you managed to get here.</p>");
    }

    $('columnName').selectedIndex=0;

}

/**
 * Save the sample - either a new sample or save to a modified sample.
 * If the sample is modified, display an "Are you sure?" confirmation.
 * The following confirmation options are possible:
 *    Yes: continue as normal
 *    No: remove the confirmation dialogue and display the buttons.
 *    Save As New: save as a new sample
 *
 */
Sample.prototype.saveTheSample = function() {
    var saveSampleButtonValue = $("saveSampleButton").value;
    // we only want to show the "Are you sure?" message if a change was made to the filters.
    if (saveSampleButtonValue != null
            && saveSampleButtonValue == "Save Changes" && this.filterEventFlag) {
        this.hideButtons();

        new Insertion.Top("saveSample", '<div id="sampleSureDiv"></div>');
        var userMessage = 'Saving this sample will overwrite the existing sample '
           + ' and cannot be canceled.  Are you sure you wish to save?';
        new Insertion.Top("sampleSureDiv", '<span>' + userMessage + '<br/></span>');
        new Insertion.Bottom("sampleSureDiv", '<a id="confirmSaveYes">Yes</a>');
        new Insertion.Bottom("sampleSureDiv", '&nbsp/&nbsp');
        new Insertion.Bottom("sampleSureDiv", '<a id="confirmSaveNo">No</a>');
        new Insertion.Bottom("sampleSureDiv", '&nbsp/&nbsp');
        new Insertion.Bottom("sampleSureDiv", '<a id="confirmSaveAsNew">Save As New</a>');

        // now set the click observers
        Event.observe("confirmSaveYes", 'click', this.confirmSave.bindAsEventListener(this));
        Event.observe("confirmSaveNo", 'click', this.rescindSave.bindAsEventListener(this));
        Event.observe("confirmSaveAsNew", 'click', this.saveTheSampleAsNew.bindAsEventListener(this));

    } else {
        if ($("saveSampleButton")) { $("saveSampleButton").disabled = true; }
        if ($("saveAsNewButton")) { $("saveAsNewButton").disabled = true; }
        this.save();
    }
}

Sample.prototype.saveTheSampleAsNew = function() {
    this.showButtons();
    if ($("saveSampleButton")) { $("saveSampleButton").disabled = true; }
    if ($("saveAsNewButton")) { $("saveAsNewButton").disabled = true; }
    this.save(true);
}

Sample.prototype.closeSampleSelector = function() {
    if ($('sampleManager')) {
       $('sampleManager').parentNode.removeChild($('sampleManager'));
       //Remove the modal background div
       $('modalBackgroundDiv').remove();
    }
    this._restoreHelpWindowContents();
}

Sample.prototype.confirmSave = function() {
    this.save();
    this.showButtons();
    if ($("saveSampleButton")) { $("saveSampleButton").disabled = true; }
    if ($("saveAsNewButton")) { $("saveAsNewButton").disabled = true; }
    if ($("cancelSampleButton")) { $("cancelSampleButton").disabled = true; }

}

Sample.prototype.rescindSave = function() {
    $("sampleSureDiv").hide();
    this.showButtons();
}

Sample.prototype.debugMe = function() {

    a = "Sample Name: " + $F('sampleNameInput')
      + " Sample Description: " + $F('sampleDescriptionInput')
      + " Sample Share: " + $F('sampleGlobal')
      + " Sample Id: " + this.sample.sampleId
      + " Status: " + this.sample.statusString
      + " Owner? : " + this.sample.isOwner;

    for(i = 0; i < this.sample.filterArray.length; i++) {
        a = a + "\nFilter:  \n"
           + " Category: " + this.sample.filterArray[i].category
           + " Attribute: " + this.sample.filterArray[i].attribute
           + " \ndisplayString: " + this.sample.filterArray[i].displayString
           + " \ndbString: " + this.sample.filterArray[i].dbString
           + " Operator: " + this.sample.filterArray[i].operator
           + " Type: " + this.sample.filterArray[i].type
           + " Open For Edit: " + this.sample.filterArray[i].openForEdit;
           + " \nFilter String(s): ";
           for(j=0; j < this.sample.filterArray[i].filterString.length; j++) {
               a = a + "[" + j + "]" +this.sample.filterArray[i].filterString[j] + " ";
           }
    }

    alert(a);
}

    /**
     * Private methods to toggle and close help window and to save and restore the contents.
     */
    Sample.prototype._openHelpWindow = function() {
        theHelpWindow.toggle();
        Event.observe("helpWindowCloseButton", 'click',
                this._closeHelpWindow.bindAsEventListener(this));
    }
    Sample.prototype._saveHelpWindowContents = function() {
        theHelpWindow.save();
        theHelpWindow.updateContentRegardless($("help-sample-selector"));
    }
    Sample.prototype._restoreHelpWindowContents = function() {
        theHelpWindow.restore();
    }
    Sample.prototype._closeHelpWindow = function() {
        theHelpWindow.closeHelpWindow();
    }

/********************************************************
 *          VIEW FOR THIS OBJECT
 ********************************************************/

Sample.prototype.createView=function(){

    if ($('sampleManager') != null) { return; }

    if (theManageKCSetsDialog) {
        theManageKCSetsDialog.close();
    }

    this._saveHelpWindowContents();


    //Create an opaque background div for this modal dialog
    new Insertion.Top(document.body, '<div id="modalBackgroundDiv"></div>');

    $('modalBackgroundDiv').setStyle({
        margin: 0,
        padding: 0,
        border: 'none',
        display: 'block',
        width: '100%',
        height: '500px',
        background: '#000',
        position: 'absolute',
        top: 0,
        left: 0,
        zIndex: 2,
        color: '#fff',
        opacity:0.3
    });

    // stretch overlay to fill page
    var arrayPageSize = getPageSize();
    $('modalBackgroundDiv').setStyle({
        width: arrayPageSize[0] + 'px',
        height: arrayPageSize[1] + 'px'
    });

    new Insertion.Bottom(document.body, '<div id="sampleManager" class="window"><div>');

    //close button in the upper right corner
    new Insertion.Bottom("sampleManager", '<a id="closeButton"></a>');
    new Insertion.Bottom("closeButton",
        '<img src="images/close.gif" alt="Close Sample Selector" title="Close Sample Selector" />');

    Event.observe("closeButton", 'click', this.closeSampleSelector.bindAsEventListener(this));

    new Insertion.Bottom("sampleManager", '<div id="sampleInfoHelpButtonDiv"></div>');
    new Insertion.Bottom("sampleInfoHelpButtonDiv", '<a id="sampleInfoHelpButton"></a>');
    new Insertion.Bottom("sampleInfoHelpButton",
        '<img src="images/help_16.gif" alt="Help" title="Help for Sample Selector" />');

    Event.observe("sampleInfoHelpButton", 'click', this._openHelpWindow.bindAsEventListener(this));

    new Insertion.Bottom("sampleManager", '<h1>Sample Selector</h1>');
    new Insertion.Bottom("sampleManager", '<div id="sampleStatus"></div>');

    /**** BEGIN SAMPLE INFO ****/

    new Insertion.Bottom("sampleManager", '<div id="sampleInfo"></div>');
    new Insertion.Bottom("sampleInfo",
      '<table id="sampleInfoTable"><tbody id="sampleInfoTableBody"></tbody></table>');

    checkedString = (this.isGlobal) ? ' checked="checked" ' : '';
    new Insertion.Bottom("sampleInfoTableBody",
         '<tr><td><label>Name*</label></td>'
       + '<td><input id="sampleNameInput" maxlength="100" type="text" value="' + this.name + '" /></td></tr>'
       + '<tr><td><label>Description</label></td>'
       + '<td><input id="sampleDescriptionInput" maxlength="255" type="text" value="' + this.description + '" /></td></tr>'
       + '<tr><td><label>Share this sample?</label></td>'
       + '<td><input id="sampleGlobal" type="checkbox"' + checkedString + '/></td></tr>');

    /**** BEGIN FILTER INFO ****/
    new Insertion.Bottom("sampleManager", '<div id="filterInfo"></div>');
    new Insertion.Bottom("filterInfo", "<h2></h2>");

    new Insertion.Bottom("filterInfo",
         '<table id="filterInfoTable"><thead></thead><tbody id="filterInfoTableBody">'
       + ' <tr id="filterTableRow">'
       + '   <td id="columnCategoriesCell" class="columnCategoriesCell"></td>'
       + '   <td id="addRemove"></td>'
       + '   <td id="filterListCell"></td>'
       + ' </tr> '
       + '<tbody></table>');

    /**** column categories ****/
    new Insertion.Bottom($("columnCategoriesCell"),
        '<label>Column Categories</label>'
      + '<select id="filterCategory">'
      + '  <option value="condition">Condition</option>'
      + '  <option value="customField">Custom Field</option>'
      + '  <option value="datasetLevel">Dataset Level</option>'
      + '  <option value="problem">Problem</option>'
      + '  <option value="school">School</option>'
      + '  <option value="student">Student</option>'
      + '  <option value="transaction">Tutor Transaction</option>'
      + '</select><br />'
      + '<select id="columnName" size="5" name="columnAttributes"></select>');

    Event.observe("filterCategory", 'change', this.selectFilterColumnList.bindAsEventListener(this));

    new Insertion.Bottom("addRemove", '<input type="button" id="addButton" value="Add >>" />');

    Event.observe("addButton", 'click', this.addNewFilter.bindAsEventListener(this));

    /**** filter list ****/

    new Insertion.Bottom("filterListCell",
          '<table id="filterList">'
        + '<thead><tr>'
        + '  <th>Column</th>'
        + '  <th>Operator</th>'
        + '  <th>Filter</th>'
        + '  <th class="actionCell"></th>'
        + '</tr></thead>'
        + '<tbody id="filterListBody"></tbody>'
        + '</table>');

    for (k = 0; k < this.filterArray.length; k++) {
        $("filterListBody").appendChild(this.filterArray[k].createView());
    }

    /**** BEGIN SAVE/CLEAR/CANCEL BUTTONS ****/
    new Insertion.Bottom("filterInfoTableBody", '<tr><td id="saveSample" colspan="4"></td></tr>');

    saveAsNewString = '<input type="button" id="saveAsNewButton" value="Save As New" />';
    if (this.sampleId != null && this.isOwner == true) {
        new Insertion.Bottom("saveSample", '<input type="button" id="saveSampleButton" value="Save Changes" />');
        new Insertion.Bottom("saveSample", saveAsNewString);
    } else if (this.sampleId != null && this.isOwner == false) {
        new Insertion.Bottom("saveSample", saveAsNewString);
    } else {
        new Insertion.Bottom("saveSample", '<input type="button" id="saveSampleButton" value="Save Sample" class="native-button" />');
    }

    //add the event handlers for the "save" and "saveAsNew" buttons
    if ($('saveSampleButton')) {
        Event.observe("saveSampleButton", 'click', this.saveTheSample.bindAsEventListener(this));
    }
    if ($('saveAsNewButton')) {
        Event.observe("saveAsNewButton", 'click', this.saveTheSampleAsNew.bindAsEventListener(this));
    }


    //add the cancel button and it's even handler.
    new Insertion.Bottom("saveSample", '<input type="button" id="cancelSampleButton" value="Cancel" />');
    Event.observe("cancelSampleButton", 'click', this.closeSampleSelector.bindAsEventListener(this));

    /**** BEGIN SAMPLE PREVIEW ****/

    new Insertion.Bottom("filterInfo", '<div id="dataPreview"><p>Loading Data Preview<p></div>');

    /**** DEBUG BUTTON ****/
    input = document.createElement('input');
    input.type="button";
    input.onclick=this.debugMe;
    input.value="Debug Me";
    input.sample = this;
    //filterInfoDiv.appendChild(input);

    /**** TURN OFF LAODING WINDOW && DISPLAY SAMPLE SELECTOR ****/

    //remove the loading window.
    var loadingDiv = document.getElementById("sampleLoad");
    if (loadingDiv != null) {
        loadingDiv.parentNode.removeChild(loadingDiv);
    }

    this.updatePreview();
    this.updateStatus("");

    //set an initial list of filter options.
    this.selectFilterColumnList();
}

Sample.prototype.showButtons = function() {
    if ($("saveSampleButton")) { $("saveSampleButton").show(); }
    if ($("saveAsNewButton")) { $("saveAsNewButton").show(); }
    if ($("cancelSampleButton")) { $("cancelSampleButton").show(); }
}

Sample.prototype.hideButtons = function() {
    if ($("saveSampleButton")) { $("saveSampleButton").hide(); }
    if ($("saveAsNewButton")) { $("saveAsNewButton").hide(); }
    if ($("cancelSampleButton")) { $("cancelSampleButton").hide(); }
}

