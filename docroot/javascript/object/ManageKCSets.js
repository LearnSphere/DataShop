//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2008
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 10726 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2014-03-05 14:19:51 -0500 (Wed, 05 Mar 2014) $
// $KeyWordsOff: $
//

/** 
 * KC Sets Dialog JavaScript Object.
 */
var ManageKCSets = { }; // like a global binder

ManageKCSets.Base = Class.create({

    _COOKIE_PREFIX: "ds_manage_kc_sets_",
    
    /**
     * Constructor.  Creates the dialog and restores it to its previous
     * size and location and fills in the content.
     */
    initialize: function(handle, options) {
        this.theButton = $(handle);
        this.url = "Set";
        this.state = this._getState();
        this._create(this.state);
    },
    
    /**
     * Activate the wrench.
     */
    activateButton: function(theButton) {
        Event.observe(theButton, 'click', this.toggle.bindAsEventListener(this));
    },
    
    /**
     * Public method to open the dialog.
     */
    open: function() {
        this._openWindow();
    },
    
    /**
     * Public method to close the dialog.
     */
    close: function() {
        this._closeWindow();
    },
    
    /**
     * Used by the button to toggle the dialog from open to closed.
     */
    toggle: function() {
        if (this.state == "hide") {
            this._openWindow();
        } else {
            this._closeWindow();
        }
    },
    
    /**
     * Add the 'modified' text to the right places from the skill observer.
     */
    setSetModifiedFlag: function() {
        var div = $("manageKCsetsExistingSetsList");
        var rows = div.select("tr");
        for (var idx = 0; idx < rows.size(); idx++) {
            var cell = rows[idx].firstDescendant();
            var setInfo = this.setList[idx];
            
            //first, check if a rename is in progress, and cancel it
            var okDiv = $("renameOk_" + idx);
            if (okDiv) {
                this._renameCancel(idx);
            }
            //check if a delete is in progress, and cancel it
            var yesDiv = $("deleteTdYes_" + idx);
            if (yesDiv) {
                this._deleteNo(idx);
            }
            
            // Fix the loaded string as necessary
            var loadedString = setInfo.getLoadedString();
            var currentString = cell.innerHTML;
            if (loadedString.include("loaded") && !currentString.include("modified")) {
               var newString =  cell.innerHTML.sub("loaded", "loaded, modified");
               cell.innerHTML = newString;
               setInfo.setLoadedString(setInfo.getLoadedString().sub("loaded", "loaded, modified"));
               
               var loadTdCell   = $("loadTd_" + idx);
               loadTdCell.observe('click',   this._loadSet.bindAsEventListener(this));
               loadTdCell.addClassName("setLink");
            }
        }
    },
    
    /**
     * Private method to create the dialog.  Restores to previous
     * state (open vs. closed), size and location.
     */
    _create: function(state) {
        var hideStyle = "";
        if (state == "hide") {
            hideStyle = ' style=\"display:none"';
        } else {
            this._saveHelpWindowContents();
        }

        // create top div
	    $(document.body).insert('<div id="manageKCsets" class="window"' + hideStyle + '><div>');

        $("manageKCsets").insert('<div id="manageKCsetsHeader"></div>');
        
        //close button in the upper right corner
        $("manageKCsetsHeader").insert('<a id="manageKCsetsCloseImage"></a>');
        $("manageKCsetsHeader").insert('<a id="manageKCsetsHelpImage"></a>');

        $("manageKCsetsCloseImage").insert(
            '<img src="images/close.gif" alt="Close Dialog" title="Close Dialog" />');
        $("manageKCsetsHelpImage").insert(
            '<img src="images/help_16.gif" alt="Help for using KC Sets" title="Help for using KC Sets" />');
        
        Event.observe("manageKCsetsCloseImage",
                'click', this._closeWindow.bindAsEventListener(this));
        Event.observe("manageKCsetsHelpImage",
                'click', this._openHelpWindow.bindAsEventListener(this));
        
        $("manageKCsetsHeader").insert('<h1 id="manageKCsetsHeaderH1">Manage KC Sets</h1>');
        Event.observe($("manageKCsetsHeader"), 'mouseover', function(){this.addClassName('hover')});
        Event.observe($("manageKCsetsHeader"), 'mouseout',  function(){this.removeClassName('hover')});
        
        $("manageKCsets").insert('<div id="manageKCsetsContent"></div>');
        
        $("manageKCsetsContent").insert('<h2>Existing KC Sets</h2>');
        $("manageKCsetsContent").insert('<div id="manageKCsetsExistingSetsList"></div>');
        
        var datasetHasEditFlagInput = $(datasetHasEditFlag);
        if (datasetHasEditFlagInput && datasetHasEditFlagInput.value == "true") {
            $("manageKCsetsContent").insert('<h2>Save the selected KCs to a KC set</h2>');
            //note that sometimes the dom doesn't keep up with the inserts so multiple inserts could fail in IE
            //therefore building a string and doing one insert instead of multiple here
            var tableHtml = '<table id="manageKCsetsTable"><tbody>'
                     + '<tr><td><input id="manageKCsetsName" size="45" type="text"></td>'
                     + '<td id="manageKCsetsSaveButtonCell">'
                     + '<input id="manageKCsetsSaveButton" class="native-button" type="button" value="Save"></input>'
                     + '</td></tr></tbody></table>';
            $("manageKCsetsContent").insert(tableHtml);
            Event.observe($("manageKCsetsSaveButton"), 'click', this._saveSet.bindAsEventListener(this));
        } else {
            $("manageKCsetsContent").insert("<br/>");
        }

        $("manageKCsets").insert('<div id="manageKCsetsInfo"></div>');
        
        
        this._restoreLocation();
        
        new Draggable($("manageKCsets"), {
            handle: $("manageKCsetsHeader"),
            onStart: function(draggable, event) {
            }.bind(this),
            onDrag: function (draggable, event) {
            }.bind(this),
            onEnd: function(draggable, event) {
                this._saveLocation();
            }.bind(this)
        });
        
        this._getSetsAjaxRequest();
    },

    _updateInfoArea: function(message) {
        var infoArea = $("manageKCsetsInfo");
        infoArea.innerHTML = "";
        infoArea.insert('<p>' + message + '</p>');
    },
    _clearInfoArea: function(content) {
        var infoArea = $("manageKCsetsInfo");
        infoArea.innerHTML = "";
    },

    /**
     * Private method to open the dialog.
     */
    _openWindow: function() {
        $("manageKCsets").show();
        this._restoreLocation();
        this.state = "show";
        this._saveState(this.state);
        this._setNameOpen(this.loadedSetName);
        this._saveHelpWindowContents();
    },

    /**
     * Private method to close the dialog.
     */
    _closeWindow: function() {
        if (this.state == "show") {
            $("manageKCsets").hide();
            this.state = "hide";
            this._saveState(this.state);
            this._updateInfoArea('');
            this._restoreHelpWindowContents();
        }
    },
    
    /**
     * Private methods to toggle and close help window and to save and restore the contents. 
     */
    _openHelpWindow: function() {
        theHelpWindow.toggle();
        Event.observe("helpWindowCloseButton", 'click',
                this._closeHelpWindow.bindAsEventListener(this));
    },
    _saveHelpWindowContents: function() {
        theHelpWindow.save();
        theHelpWindow.updateContentRegardless($("help-kc-sets"));
    },    
    _restoreHelpWindowContents: function() {
        theHelpWindow.restore();
    },    
    _closeHelpWindow: function() {
        theHelpWindow.closeHelpWindow();
    },    
    
    /**
     * Sends an Ajax request to request a list of sets for the given KCM.
     */
    _getSetsAjaxRequest: function() {
        new Ajax.Request(this.url, {
            parameters: {
                requestingMethod: "ManageKCSets._getSetsAjaxRequest",
                datasetId: dataset,
                ds_set_request: "get_list"
            },
            requestHeaders: {Accept: 'application/json;charset=UTF-8'},
            onComplete: this._getSetsAjaxListener.bindAsEventListener(this),
            onException: function (request, exception) {
                throw(exception);
            }
       });
    },
    
    /**
     * Handles the AJAX response with a list of sets by taking apart
     * the JSON object returned.
     */
    _getSetsAjaxListener: function(transport) {
        var data = transport.responseText.evalJSON(true);
        var div = $("manageKCsetsExistingSetsList");
        
        // clear current table
        div.innerHTML = "";
        
        // create new array to store information
        this.setList = new Array;

        // create new table
        var table = '<table><col id="kcSetDescriptionCol" /><col id="kcSetLoadCol" /><col id="kcSetRenameCol" /><col id="kcSetDeleteCol" />';
        this.numSets = data.sets.length;
        for (var i = 0; i < this.numSets; i++) {
            var row = $A(data.sets[i]);
            var ownerFlag = row[3];
            var setInfo = new SetInfo.Base(row[0], row[1], row[2], row[3], row[4], row[5]);
            this.setList[i] = setInfo;
            var nameTdId = "nameTd_" + i;
            var loadTdId = "loadTd_" + i;
            var renameTdId = "renameTd_" + i;
            var deleteTdId = "deleteTd_" + i;
            if (row[5].include("loaded") && !row[5].include("modified")) {
            table += '<tr><td id="' + nameTdId + '" class="selected"> ' + row[1] + ' [' + row[4] +  ' KCs] ' + row[5] + '</td>';
            table += '<td id="' + loadTdId     + '" title="Load this set">load</td>';
            } else {
            table += '<tr><td id="' + nameTdId + '"> ' + row[1] + ' [' + row[4] +  ' KCs] ' + row[5] + '</td>';
            table += '<td id="' + loadTdId     + '" class="setLink" title="Load this set">load</td>';
            }
            if (ownerFlag) {
            table += '<td id="' + renameTdId   + '" class="setLink" title="Rename this set">rename</td>';
            table += '<td id="' + deleteTdId   + '" class="setLink" title="Delete this set">delete</td></tr>';
            } else {
            table += '<td id="' + renameTdId   + '" title="Rename this set">rename</td>';
            table += '<td id="' + deleteTdId   + '" title="Delete this set">delete</td></tr>';
            }
            
        }
        table += '</table>';
        div.insert(table);

        // Walk through the DOM and add the observers for the name TDs
        var rows = div.select("tr");
        for (var i = 0; i < rows.size(); i++) {
            var nameCell = rows[i].firstDescendant();
            nameCell.observe('click', this._setNameClick.bindAsEventListener(this));
            nameCell.observe('mouseover', this._setNameMouseOver.bindAsEventListener(this));
            nameCell.observe('mouseout', this._setNameMouseOut.bindAsEventListener(this));

            var loadCell = nameCell.next();
            if (loadCell.hasClassName("setLink")) {
                loadCell.observe('click', this._loadSet.bindAsEventListener(this));
            }
            var renameCell = loadCell.next();
            if (renameCell.hasClassName("setLink")) {
                renameCell.observe('click', this._renameSet.bindAsEventListener(this));
            }
            var deleteCell = renameCell.next();
            if (deleteCell.hasClassName("setLink")) {
                deleteCell.observe('click', this._deleteSet.bindAsEventListener(this));
            }
        }
        //do not show the success message in this case
        //but update the loaded set name
        this._updateLoadedSetName(data.setName, data.modifiedFlag);
        if ($("manageKCsetsName")) {
            $("manageKCsetsName").value = this.loadedSetName;
        }
        
        //update the title of the dialog with the selected KCM name
        var headerH1 = $("manageKCsetsHeaderH1");
        if (data.modelName) {
            headerH1.innerHTML = "Manage KC Sets for '" + data.modelName + "' KC Model";
        }
    },
        
    _setNameClick: function(event) {
        var div = $("manageKCsetsExistingSetsList");
        
        // update the background for the selected row
        var rows = div.select("tr");
        for (var i = 0; i < rows.size(); i++) {
            var cell = rows[i].firstDescendant();
            if (cell == event.element()) {
                event.element().addClassName("selected");
            } else {
                cell.removeClassName("selected");
            }
        }
        
        // get the set name of selected row and put in text field
        var idx = Number(event.element().id.split('_')[1]);
        var name = this.setList[idx].getName();
        if ($("manageKCsetsName")) {
            $("manageKCsetsName").value = name;
        }
    },
    
    _setNameOpen: function(loadedSetName) {
        var div = $("manageKCsetsExistingSetsList");
        
        // update the background for the selected row
        var rows = div.select("tr");
        for (var i = 0; i < rows.size(); i++) {
            var cell = rows[i].firstDescendant();
            var setInfo = this.setList[i];
            if (loadedSetName == setInfo.getName()) {
                cell.addClassName("selected");
            } else {
                cell.removeClassName("selected");
            }
        }
        
        if ($("manageKCsetsName")) {
            $("manageKCsetsName").value = loadedSetName;
        }
    },
    
    _setNameMouseOver: function(event) {
        event.element().addClassName("hover");
    },
    _setNameMouseOut: function(event) {
        event.element().removeClassName("hover");
    },
    
    /**
     * The method that is called on first click of the save button.
     */
    _saveSet: function() {
        var setName = $("manageKCsetsName").value;
        var isMyDataset = false;
        for (var idx = 0; idx < this.numSets; idx++) {
            var setInfo = this.setList[idx];
            if (setName == setInfo.getName() && setInfo.isOwner()) {
                isMyDataset = true;
                break;
            }
        }
        if (isMyDataset) {
            var yesId = "saveYes";
            var noId  = "saveNo";
            $("manageKCsetsSaveButtonCell").innerHTML = 'Overwrite "' + setInfo.getName() + '"? '
                    + 'This will replace the set for good!<br /> <span id="'
        	    + noId + '" class="setLink">no</span> / <span id="'
        	    + yesId + '" class="setLink">yes</span>';
            $(yesId).observe('click', this._saveYes.bindAsEventListener(this));
            $(noId).observe('click', this._saveNo.bindAsEventListener(this));
        } else {
            this._saveSetAjaxRequest();
        }
    },

    /**
     * Save the set and reset the dialog.
     */
    _saveYes: function() {
        this._saveSetAjaxRequest();
        this._saveNo();
    },

    /**
     * Reset the dialog but do not save the set.
     */
    _saveNo: function() {
        $("manageKCsetsSaveButtonCell").innerHTML = "";
        $("manageKCsetsSaveButtonCell").insert('<input id="manageKCsetsSaveButton" class="native-button" type="button" value="Save"></input><br>');
        Event.observe($("manageKCsetsSaveButton"), 'click', this._saveSet.bindAsEventListener(this));
    },

    /**
     * Sends an Ajax request to save the set with the given name.
     */
    _saveSetAjaxRequest: function() {
        var setName = $("manageKCsetsName").value;
        new Ajax.Request(this.url, {
            parameters: {
                requestingMethod: "ManageKCSets._saveSetAjaxRequest",
                datasetId: dataset,
                ds_set_request: "save",
                ds_set_name: setName
            },
            requestHeaders: {Accept: 'application/json;charset=UTF-8'},
            onComplete: this._saveSetAjaxListener.bindAsEventListener(this),
            onException: function (request, exception) {
                throw(exception);
            }
       });
    },
    
    /**
     * Handles the AJAX response to the save set command by calling the
     * same method to populate the table as the get sets.
     */
    _saveSetAjaxListener: function(transport) {
        var data = transport.responseText.evalJSON(true);
        if (data.successFlag) {
            this._getSetsAjaxListener(transport);
        }
        this._updateInfoArea(data.message);
    },
    
    /**
     * Loads the set. 
     */
    _loadSet: function(event) {
        this._loadSetFormSubmit(event);
    },
    
    /**
     * Sends an Form Submit (page refresh) to get a list of sets for the given KCM.
     */
    _loadSetFormSubmit: function(event) {
        var idx = Number(event.element().id.split('_')[1]);
        var setInfo = this.setList[idx];
        this._selectSkillSet(setInfo.getId());
    },
    
    /**
     * Makes a call to load/select the set as a full page refresh.
     */
    _selectSkillSet: function(setId) {
	if (setId != null) {
		var setSelectForm = document.createElement('FORM');
		setSelectForm.setAttribute('name', 'skill_set_select_form');
		setSelectForm.setAttribute('id', 'skill_set_select_form');
		setSelectForm.setAttribute('form', 'text/plain');
		setSelectForm.setAttribute('action', window.location.href);
		setSelectForm.setAttribute('method', 'POST');
	
		var setIdInput = document.createElement('input');
		setIdInput.name="skillSetId";
		setIdInput.type="hidden";
		setIdInput.value=setId;
		
		var datasetIdInput = document.createElement('input');
		datasetIdInput.name="datasetId";
		datasetIdInput.type="hidden";
		datasetIdInput.value=dataset;
		
		setSelectForm.appendChild(setIdInput);
		setSelectForm.appendChild(datasetIdInput);
		
		document.getElementsByTagName('body').item(0).appendChild(setSelectForm);
		setSelectForm.submit();
	}
    },
    
    _getRowIdx: function(event) {
        return Number(event.element().id.split('_')[1]);
    },

    _unlinkRow: function(idx) {
        var loadTdCell   = $("loadTd_" + idx);
        var renameTdCell = $("renameTd_" + idx);
        var deleteTdCell = $("deleteTd_" + idx);

        loadTdCell.stopObserving('click');
        renameTdCell.stopObserving('click');
        deleteTdCell.stopObserving('click');

        loadTdCell.removeClassName("setLink");
        renameTdCell.removeClassName("setLink");
        deleteTdCell.removeClassName("setLink");
    },
    

    _relinkRow: function(idx) {
        var loadTdCell   = $("loadTd_" + idx);
        var renameTdCell = $("renameTd_" + idx);
        var deleteTdCell = $("deleteTd_" + idx);
        
        var setInfo = this.setList[idx];
        var loadedString = setInfo.getLoadedString();
        if (setInfo.getLoadedString() != "(loaded)") { 
            loadTdCell.observe('click',   this._loadSet.bindAsEventListener(this));
            loadTdCell.addClassName("setLink");
        }
        renameTdCell.observe('click', this._renameSet.bindAsEventListener(this));
        deleteTdCell.observe('click', this._deleteSet.bindAsEventListener(this));
        
        renameTdCell.addClassName("setLink");
        deleteTdCell.addClassName("setLink");
    },
    
    /**
     * Rename the set.  First change the row.
     */
    _renameSet: function(event) {
        var idx = this._getRowIdx(event);
        var nameTdDiv = $("nameTd_" + idx);
        var renameTdCell = $("renameTd_" + idx);
        var setInfo = this.setList[idx];
        setInfo.setNameInnerHTML(nameTdDiv.innerHTML);
        this._unlinkRow(idx);
        
        var inputId   = "renameInput_" + idx;
        var okId      = "renameOk_" + idx;
        var cancelId  = "renameCancel_" + idx;
        
        var inputHtml = '<input id="' + inputId  + '" class="rename-field" size="45" type="text">'
         + '<input id="' + okId     + '" class="native-button" type="button" value="OK"></input>'
         + '<input id="' + cancelId + '" class="native-button" type="button" value="Cancel"></input>';
        nameTdDiv.innerHTML = nameTdDiv.innerHTML + inputHtml;

        var okDiv = $(okId);
        var cancelDiv = $(cancelId);
        
        okDiv.observe('click', this._renameOk.bindAsEventListener(this));
        cancelDiv.observe('click', this._renameCancelEvent.bindAsEventListener(this));
    },

    /**
     * Get the new name and put everything back.
     */
    _renameOk: function(event) {
        var idx = this._getRowIdx(event);
        var inputId   = "renameInput_" + idx;
        var oldSetName = this.setList[idx].getName();
        var newSetName = $(inputId).value;
        this._renameSetAjaxRequest(oldSetName, newSetName);
        this._renameCancelEvent(event);
    },

    /**
     * Put everything back the way it was.
     */
    _renameCancelEvent: function(event) {
        var idx = this._getRowIdx(event);
        this._renameCancel(idx);
    },
    _renameCancel: function(idx) {
        var setInfo = this.setList[idx];
        $("nameTd_" + idx).innerHTML =  setInfo.getNameInnerHTML();
        this._relinkRow(idx);
    },
    
    /**
     * Get the delete div for a row in the table given an event.
     */
    _getRenameDiv: function(event) {
        var idx = Number(event.element().id.split('_')[1]);
        var tdId = "renameTd_" + idx;
        var div = $(tdId);
        return div;
    },
    
    /**
     * Sends an Ajax request to rename a set.
     */
    _renameSetAjaxRequest: function(oldSetName, newSetName) {
        new Ajax.Request(this.url, {
            parameters: {
                requestingMethod: "ManageKCSets._renameSetAjaxRequest",
                datasetId: dataset,
                ds_set_request: "rename",
                ds_set_name: oldSetName,
                ds_set_new_name: newSetName
            },
            requestHeaders: {Accept: 'application/json;charset=UTF-8'},
            onComplete: this._renameSetAjaxListener.bindAsEventListener(this),
            onException: function (request, exception) {
                throw(exception);
            }
       });
    },
    
    /**
     * Handles the AJAX response for renaming a set.
     * Expect a list of sets in a JSON object.
     */
    _renameSetAjaxListener: function(transport) {
        //if successful, update the list of sets and show the message
        var data = transport.responseText.evalJSON(true);
        if (data.successFlag) {
            this._getSetsAjaxListener(transport);
        }
        this._updateInfoArea(data.message);
    },
    
    /**
     * Change the delete link to 'delete this set? yes / no'.
     */
    _deleteSet: function(event) {
        var idx = Number(event.element().id.split('_')[1]);
        this._unlinkRow(idx);
        var loadTdCell   = $("loadTd_" + idx);
        var renameTdCell = $("renameTd_" + idx);
        var deleteTdCell = $("deleteTd_" + idx);
        loadTdCell.setStyle({display:'none'});
        renameTdCell.setStyle({display:'none'});
        deleteTdCell.setAttribute("colspan", 3);
        
        var yesId = "deleteTdYes_" + idx;
        var noId  = "deleteTdNo_" + idx;

        deleteTdCell.innerHTML = 'delete this set?  <span id="'
        	+ noId + '" class="setLink">no</span> / <span id="'
        	+ yesId + '" class="setLink">yes</span>';

        $(noId).observe('mouseup', this._deleteNoEvent.bindAsEventListener(this));
        $(yesId).observe('mouseup', this._deleteYes.bindAsEventListener(this));       
    },

    /**
     * Delete the set and change the are-you-sure prompt back to delete link.
     */
    _deleteYes: function(event) {
        var setName = this._getSetName(event);
        this._deleteSetAjaxRequest(setName);
        this._deleteNoEvent(event);
    },

    /**
     * Change the are-you-sure prompt back to delete link.
     */
    _deleteNoEvent: function(event) {
        var idx = Number(event.element().id.split('_')[1]);
        this._deleteNo(idx);
    },
    _deleteNo: function(idx) {
        var loadTdCell   = $("loadTd_" + idx);
        var renameTdCell = $("renameTd_" + idx);
        var deleteTdCell = $("deleteTd_" + idx);
        deleteTdCell.innerHTML = "delete";
        this._relinkRow(idx);
        if (isThisIE()) {
            loadTdCell.setStyle({display:'block'});
            renameTdCell.setStyle({display:'block'});
        } else {
            loadTdCell.setStyle({display:'table-cell'});
            renameTdCell.setStyle({display:'table-cell'});
        }
        deleteTdCell.setAttribute("colspan", 1);
    },
    
    /**
     * Get the set name for a row in the table given an event.
     */
    _getSetName: function(event) {
        var idx = Number(event.element().id.split('_')[1]);
        var setName = this.setList[idx].getName();
        return setName;
    },

    /**
     * Sends an Ajax request to delete the set with the given name.
     */
    _deleteSetAjaxRequest: function(setName) {
        new Ajax.Request(this.url, {
            parameters: {
                requestingMethod: "ManageKCSets._deleteSetAjaxRequest",
                datasetId: dataset,
                ds_set_request: "delete",
                ds_set_name: setName
            },
            requestHeaders: {Accept: 'application/json;charset=UTF-8'},
            onComplete: this._deleteSetAjaxListener.bindAsEventListener(this),
            onException: function (request, exception) {
                throw(exception);
            }
       });
    },
    
    /**
     * Handles the AJAX response for deleting a set.
     * Expect a list of sets in a JSON object.
     */
    _deleteSetAjaxListener: function(transport) {
        //update the list of sets and show the message
        var data = transport.responseText.evalJSON(true);
        if (data.successFlag) {
            this._getSetsAjaxListener(transport);
        }
        this._updateInfoArea(data.message);
    }, 
    
    _updateLoadedSetName: function(loadedSetName, modifiedFlag) {
        if (loadedSetName.length > 0) {
            $("contentSetName").innerHTML = "KC Set: " + loadedSetName + "&nbsp;";
            var modSpan = $("contentSetNameModified");
            if (modSpan && modSpan != null) {
                if (modifiedFlag) {
                    modSpan.innerHTML = "(modified)";
                } else {
                    modSpan.innerHTML = "";
                }
            }
        } else {
            var contentSetName = $("contentSetName");
            if (contentSetName && contentSetName != null) {
                $("contentSetName").innerHTML = "";
            }
            var modSpan = $("contentSetNameModified");
            if (modSpan && modSpan != null) {
                modSpan.innerHTML = "";
            }
        }
        if ($("manageKCsetsName")) {
            $("manageKCsetsName").value = loadedSetName;
        }
        this.loadedSetName = loadedSetName;
    },
    
    /**
     * This section is for storing and retrieving the values stored
     * in the cookies, which are state, size and location.
     */
    _saveState: function (value) {
        createCookie(this._COOKIE_PREFIX + this.theButton.id + "_state", value, 100);
    },

    _saveSize: function () {
        var height = this.theButton.style.height;
        var width = this.theButton.style.width;
        createCookie(this._COOKIE_PREFIX + this.theButton.id + "_height", height, 100);
        createCookie(this._COOKIE_PREFIX + this.theButton.id + "_width", width, 100);
    },

    _saveLocation: function () {
        var x = $("manageKCsets").getStyle('left');
        var y = $("manageKCsets").getStyle('top');
        createCookie(this._COOKIE_PREFIX + this.theButton.id + "_x", x, 100);
        createCookie(this._COOKIE_PREFIX + this.theButton.id + "_y", y, 100);
    },

    _getState: function () {
        var state = readCookie(this._COOKIE_PREFIX + this.theButton.id + "_state");
        if (!state) {
            state = "hide"; // default if not cookie found
        }
        return state;
    },

    _restoreSize: function () {
        var height = readCookie(this._COOKIE_PREFIX + this.theButton.id + "_height");
        var width  = readCookie(this._COOKIE_PREFIX + this.theButton.id + "_width");
    },

    _restoreLocation: function () {
        var x = readCookie(this._COOKIE_PREFIX + this.theButton.id + "_x");
        var y = readCookie(this._COOKIE_PREFIX + this.theButton.id + "_y");
        this._setLocation($("manageKCsets"), $("manageKCsetsHeader"), x, y);
    },
    
    /**
     * Put the window at its last location if it is in the viewport,
     * otherwise center the window in the viewport, unless the viewport is too small,
     * then reposition the window at 10,10.
     * Be sure to keep the header of the window, which is the drag handle, in the viewport.
     * (Note that this method is dupliated in HelpWindow.js until we can find one place for it.)
     */
    _setLocation: function (theWindow, theWindowHeader, cookieX, cookieY) {
        var winX = -1;
        var winY = -1;
        
        if (cookieX && cookieY) {
            var winX = cookieX.substring(0, cookieX.length - 2);
            var winY = cookieY.substring(0, cookieY.length - 2);
        }

        var winWidth = theWindow.getWidth();
        var winHeight = theWindow.getHeight();
        var winHeaderHeight = theWindowHeader.getHeight();
        var viewPortWidth  = document.viewport.getWidth();
        var viewPortHeight = document.viewport.getHeight();
            
        if (winX < (20 - winWidth) ||
            winX > (viewPortWidth - 20)|| 
            winY < 0 || 
            winY > (viewPortHeight - 20))
        {
           // center the window if the view port is bigger than the help window
           winX = (viewPortWidth/2) - (winWidth/2)
           winY = (viewPortHeight/2) - (winHeight/2)

           // otherwise, place at 10,10
           if (winX < 0) winX = 10;
           if (winY < 0) winY = 10;

           cookieX = winX + "px";
           cookieY = winY + "px";
        }
        
        theWindow.setStyle({top:cookieY,left:cookieX});
    }
});  //end class ManageKCSets.Base

var SetInfo = { };
SetInfo.Base = Class.create({
    initialize: function(id, name, description, ownerFlag, numSkills, loadedString) {
       this.id = id;
       this.name = name;
       this.description = description;
       this.ownerFlag = ownerFlag;
       this.numSkills = numSkills;
       this.loadedString = loadedString;
    },
    getId: function() {
       return this.id;
    },
    getName: function() {
       return this.name;
    },
    getNumSkills: function() {
       return this.numSkills;
    },
    getLoadedString: function() {
       return this.loadedString;
    },
    setLoadedString: function(newLoadedString) {
       this.loadedString = newLoadedString;
    },
    isOwner: function() {
       return this.ownerFlag;
    },
    getNameInnerHTML: function() {
       return this.nameInnerHTML;
    },
    setNameInnerHTML: function(html) {
       this.nameInnerHTML = html;
    }
});  //end class SetInfo.Base

/**
 * This is the instance of the dialog.
 */
var theManageKCSetsDialog;

/**
 * This is called by the LearningCurve.js, ... and others to create the dialog when needed.
 */
function createManageKCSetsDialog() {
    var theButton = $("navManageKCsetsButton");
    if (theButton && theButton != null) {
        var theHeader = $("manageKCsetsHeader");
        if (theHeader == null) {
            theManageKCSetsDialog = new ManageKCSets.Base(theButton);
            theManageKCSetsDialog.activateButton(theButton);
        }
    }
}

function closeManageKCSetsDialog() {
    theManageKCSetsDialog.close();
}

/**
 * This is called when the user selects/deselects a skill in the Skill NavBox.
 */
function updateSetModifier() {
    var nameSpan = $("contentSetName");
    var modSpan  = $("contentSetNameModified");
    if (nameSpan && nameSpan != null && modSpan && modSpan != null) {
        // if there is a selected set
        if (nameSpan.innerHTML.length > 0) {
            modSpan.innerHTML = "(modified)";
        }
    }
    if (theManageKCSetsDialog && theManageKCSetsDialog != null) {
        theManageKCSetsDialog.setSetModifiedFlag();
    }
}
