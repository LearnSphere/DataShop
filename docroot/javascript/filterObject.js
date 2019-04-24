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

/********************************************************
 *
 *  Filter Object, contains all the basic information
 *  required by a filter.  Has a reference back to it's
 *  owning sample. 
 *
 ********************************************************/

function Filter(cat, attrib, oper, filterStr, theSample) {
    this.category = cat;
    this.attribute = attrib;
    this.filterString = [];
    this.hasError = false;
    
    if (oper == null) { 
        this.operator = "";
    } else {
        this.operator = oper;
    }

    if (filterStr == null) { 
        this.filterString[0] = "";
        this.displayString="";
        this.dbString="";
    } else {
        this.parseFilterString(filterStr);
    }
    
    //easy access fields.
    this.filterCell=null;
    this.operatorCell=null;
    this.filterTableBody=null;
    this.filterBuilder=null;
    this.editButton=null;
    this.viewRow=null;
    this.displayField=null;

    this.sample = theSample;
    this.type = this.getAttributeType();
    
    this.operatorSelect =  null;
    this.inputString = null;
    
    this.orButton=null;
    this.inInputArray=[];

    this.select = null;  //the select box for easy access.
    this.input = null;   //the input box for easy access.
    
    this.openForEdit = false; //indicates whether this filter is open for editing.
}

Filter.prototype.getAttributeType=function() {
    if (this.attribute == 'problemName'
       || this.attribute == 'problemDescription'
       || this.attribute == 'conditionName'
       || this.attribute == 'type'
       || this.attribute == 'levelName'
       || this.attribute == 'levelTitle'
       || this.attribute == 'anonymousUserId'
       || this.attribute == 'schoolName'
       || this.attribute == 'transactionTypeTool'
       || this.attribute == 'transactionTypeTutor'
       || this.attribute == 'transactionSubtypeTool'
       || this.attribute == 'transactionSubtypeTutor'
       || this.attribute == 'customFieldName'
       || this.attribute == 'customFieldValue'
       ) { return TEXT_TYPE; }
    
    if (this.attribute == 'transactionTime') { return DATE_TYPE; }
    
    if (this.attribute == 'attemptAtSubgoal') { return NUMBER_TYPE; }

    return null;
}

Filter.prototype.parseFilterString=function(fString) {

    this.dbString = "" + fString;
    this.displayString = "" + fString;

    if (this.isMultiSelect) {
        //trim off the parenthesis.
        if (fString.charAt(0) == "(") {
            fString = fString.substring(1, fString.length -1);
        }
        if (fString.charAt(fString.length - 1) == ")") {
            fString = fString.substring(0, fString.length -2);
        }

        var stringArrays = fString.split("', "); //split the strings into arrays.
        for (i = 0; i < stringArrays.length; i++) {
            var singleString = stringArrays[i];
            //strip off any additional qoutes
            if (singleString.charAt(0) == "'") {
                singleString = singleString.substring(1, singleString.length);
            }
            if (singleString.charAt(singleString.length -1) == "'") {
                singleString = singleString.substring(0, singleString.length - 1);
            }
            this.filterString[i] = singleString;
        }
    } else {
        filterString[0] = fString;
    }
}


Filter.prototype.focusBackgroundChange=function(event) {
    //this.filter.viewRow.className="onFocusItem";
}

Filter.prototype.focusBackgroundOff=function(event) {
    //this.filter.viewRow.className="";
}

Filter.prototype.selectFilterForEdit=function(e) {
    e = e || window.event;
    var target = e.srcElement || e.target;
    //KILL THE SAFARI BUBBLE!
    if (target.value == "Save" || target.value == "Cancel") return;

    this.filter.sample.selectFilter(this.filter);
}

Filter.prototype.deleteFilter=function(event) {
    this.filter.viewRow.parentNode.removeChild(this.filter.viewRow);
    this.filter.sample.deleteFilter(this.filter);
    this.filter.sample.updatePreview();
    this.parentNode.removeChild(this);
}

Filter.prototype.isMultiSelect=function(operator) {

    if (operator == null) {
        operator = this.operator;
    }

    if (operator == "IN" 
     ||(operator == "=" && this.inInputArray.length > 1)
     || operator == "NOT IN" 
     ||(operator == "!=" && this.inInputArray.length > 1)) {
        return true;
    } else {
        return false;
    }   
}

Filter.prototype.setFilter=function() {
    this.hasError=false;
    this.sample.updateStatus("");
    var valueString = "";
    var dbString = "";
    
    if (this.type == NUMBER_TYPE) {
        this.setNumberType();
    } else if (this.type == TEXT_TYPE) {
        this.setTextType();        
    } else if (this.type == DATE_TYPE) {
        this.setDateType();        
    } else {
        this.sample.updateStatus(
           "<p class=\"error\">The type of this filter ("
           + this.type + ") is unknown.  Cannot continue");
        this.hasError=true;
    }
    // DS973: Sample Selector: only modifying an existing filter does not ask user 'are you sure?'
    // Need to mark the sample object as having a filter change,
    // so that the Are you Sure question will come up.
    if (!this.hasError) {
        this.sample.filterEventFlag = true;
    }
}

Filter.prototype.setDateType = function() {
    var builderOperator = this.operatorSelect.value;
    var builderString = this.inputString.value;
        
    if (builderString != "" && builderString != "'" && builderString != "''") {
        var dateTime = new Date();
        if (dateTime.isDateTime(builderString) != true) {
            this.sample.updateStatus(this.dateFormatFilterError(builderString, dateTime.error));
            this.hasError=true;
            return;
        } else {
            //it's valid
            if (builderString.charAt(0) != "'") { builderString = "'" + builderString; }
            if (builderString.charAt(builderString.length - 1) != "'") {
                builderString = builderString + "'";
            }
        }
    }
        
    this.operator = builderOperator;
    this.displayString = builderString;
    this.dbString = builderString;
                
    //remove operator select; add back text version
    this.operatorSelectToText();
    this.filterInputToText();

    // Close; make click-able again, both link and row
    this.editTextToButton();
    this.editButton.onclick=this.selectFilterForEdit;
    this.viewRow.onclick = this.selectFilterForEdit;
    this.viewRow.className="";
        
    this.sample.updatePreview();
    this.clearBuilder();
}

Filter.prototype.setTextType = function() {
    var builderOperator = this.operatorSelect.value;
    var builderString = ""; //will contain escaped chars.
    var tempDisplayString = "";

    //alert("inInputArray length: " + this.inInputArray.length);
    if (this.inInputArray.length > 0) { 

        this.filterString=[]; //reset the array to remove old values;            
            
        //get first value, which isn't in the array
        if (this.inputString.value != "") {
            tempDisplayString = "'" + this.inputString.value + "'"; 
            builderString = "'" + this.replaceChars(this.inputString.value) + "'"; 
            this.filterString[0]=this.inputString.value;
            offset = 1;
        } else {
            offset = 0;
        }
            
    //the last one will/should be the disabled box so don't bother with it.
    
        for (var i = 0; i < this.inInputArray.length-1; i++) {
            if (i == 0 && this.inputString.value == "") {
                tempDisplayString = tempDisplayString + "'" + this.inInputArray[i].value + "'";
                builderString = builderString + "'" + this.replaceChars(this.inInputArray[i].value) + "'";
                this.filterString[i + offset] = this.inInputArray[i].value;
            } else if (this.inInputArray[i].value != "") {
                tempDisplayString = tempDisplayString + ", '" + this.inInputArray[i].value + "'";
                builderString = builderString + ", '" + this.replaceChars(this.inInputArray[i].value) + "'";
                this.filterString[i + offset] = this.inInputArray[i].value;
            } else {
                offset--;
        }
    }
    } else {
        builderString = this.inputString.value;
        this.filterString[0] = this.inputString.value;
    }

    if ((builderString != "" && builderString != "'" && builderString != "''") ||
        (builderOperator == "IS NULL" || builderOperator == "IS NOT NULL")) {
            
        if ( builderOperator == "LIKE" || builderOperator == "NOT LIKE" ) {
                
            if (builderString.charAt(0) != "%" && builderString.charAt(0) != "'") {
                builderString = "'%" + builderString;
            } else if ( builderString.charAt(0) != "'" ) {
                builderString = "'" + builderString;
            }
                
            if (builderString.charAt(builderString.length - 1) != "%" 
             && builderString.charAt(builderString.length - 1) != "'") {
                builderString = builderString + "%'";
            } else if (builderString.charAt(builderString.length -1) != "'" ) {
                builderString = builderString + "'";
            }
    
            this.filterString[0]=builderString;
    
        } else if (this.isMultiSelect(builderOperator)) {

            if (builderString.charAt(0) != "(") {
               builderString = "(" + builderString;
            }
                
            if (builderString.charAt(builderString.length - 1) != ")") {
                builderString = builderString + ")";
            }
        } else if (builderOperator == "IS NULL" || builderOperator == "IS NOT NULL") {
            builderString = "";
            this.filterString[0]="";
        } else {
           if (builderString.charAt(0) != "'") { builderString = "'" + builderString; }
                
            if (builderString.charAt(builderString.length - 1) != "'") {
                builderString = builderString + "'";
        }
        }
    }

    if (builderOperator == "=" && this.inInputArray.length > 1) {
        this.operator = "IN";
    } else if (builderOperator == "!=" && this.inInputArray.length > 1) {
        this.operator = "NOT IN";
    } else {
        this.operator = builderOperator;
    }

    if (tempDisplayString == "") {
        this.displayString = builderString;
    } else {
        this.displayString = tempDisplayString;
    }

    this.dbString = builderString;    

    //remove operator select; add back text version
    this.operatorSelectToText();
    this.filterInputToText();            

    // Make editable again, both link and row
    this.editTextToButton();
    this.editButton.onclick=this.selectFilterForEdit;
    this.viewRow.onclick = this.selectFilterForEdit;
    this.viewRow.className="";

    //kill inInputArray
    this.inInputArray = [];
    this.orButton = null;

    this.sample.updatePreview();
    this.clearBuilder();
}

Filter.prototype.setNumberType = function() {

    if (this.operatorSelect.value == null) {
        this.sample.updateStatus("<p class=\"error\">Invalid operator selection.</p>");
        return;
    }

    if (isNaN(this.inputString.value)) {
        this.sample.updateStatus(this.numberFilterError(this.inputString.value));
        this.hasError=true;
        return;
    } else {

        //valid stuff
        this.operator = this.operatorSelect.value;
        this.filterString[0] = this.inputString.value;
        this.displayString = this.inputString.value;
        this.dbString = this.inputString.value;

        //remove operator select; add back text version
        this.operatorSelectToText();
        this.filterInputToText();

        // Make editable again, both link and row
        this.editTextToButton();
        this.editButton.onclick=this.selectFilterForEdit;
        this.viewRow.onclick = this.selectFilterForEdit;
        this.viewRow.className="";

        this.sample.updatePreview();
        this.clearBuilder();
    }
}


Filter.prototype.getFilterError = function() {
    var error = "<p class=\"error\">There was an error with a sample filter. <p>";
    
    if (this.type == TEXT_TYPE) {
        error += this.textFilterError(this.displayString, this.operator);
    } else if (this.type == DATE_TYPE) {
        error += this.dateFilterError(this.displayString);    
    } else if (this.type == NUMBER_TYPE) {
        error += this.numberFilterError(this.displayString);
    } else {
        error += "<p>No additional information availible</p>";
    }
    return error;

}

Filter.prototype.numberFilterError = function(filterString) {
    var output = "<p class=\"error\">" +  filterString  + " is not a number, please enter a number</p>";
    return output;
}

Filter.prototype.dateFilterError = function(filterString, operator) {
    var output = "<p class=\"error\"> There was an error with your filter <br />"
               + "Filter: " + filterString + "<br />";
               + "Operator: " + operator + "</p>";
    return output;
}

Filter.prototype.dateFormatFilterError = function(filterString, dateError) {
    var output = "<p class=\"error\">'" +  filterString  + "' is not a correctly formatted date";
    output += "<br />" + dateError + "</p>";

    return output;
}


Filter.prototype.textFilterError = function(filterString, operator) {
    
    var output = "";
    
    if (operator == "IN") {
        output += "<p class=\"error\">" + filterString + " is not correctly formatted</p>";
        output += "\n<p class=\"example\">Filter must be surrounded by"
                + " parenthesis, seperated by a comma, and each item surrounded by single quotes <br />"
                + " Example: ('item one', 'item two', 'item three')</p>";
    } else if (filterString == "LIKE") {
        output += "<p class=\"error\">" + filterString + " is not correctly formatted</p>";
        output += "\n<p class=\"example\">Filter must be surrounded by single qoutes <br />"
                + "Item my have a wildcard character ( % ) on either side.<br />"
                + "Examples: '%item one%'  or  '%item two' or 'item three%' or 'item four'</p>";
    }

    return output;
}

Filter.prototype.clearFilter = function(){
    this.filterString=[];
    this.operator="";
    this.displayFilterBuilder();
}

Filter.prototype.cancelEditThisFilter=function() {
    if (!this.hasError) {
        this.filterInputToText();
        this.operatorSelectToText();
        this.clearBuilder();
        this.viewRow.className="";
        this.editTextToButton();
    }
}

Filter.prototype.filterInputToText=function() {

    this.filterCell.removeChild(this.filterCell.firstChild);    
    filterStringInput = document.createElement('input');
    filterStringInput.name="filterDisplayString";
    filterStringInput.type="text";
    filterStringInput.readOnly="true";
    filterStringInput.className="filterStringInput";

    this.displayField = filterStringInput;
    this.filterCell.appendChild(filterStringInput);
    this.displayField.value = " " + this.displayString;
    // Make editable again, both link and row
    this.viewRow.id="";
    this.editButton.onclick = this.selectFilterForEdit;
    this.viewRow.onclick = this.selectFilterForEdit;
}

Filter.prototype.operatorSelectToText=function() {
    //find which operator is the one for this filter, if any    
    var allOperators = new Array("=","!=","IN", "NOT IN", "LIKE","NOT LIKE","IS NULL",
               "IS NOT NULL","<",">","<=",">=");
    var theOperator = "=";
    
    //TODO why is this loop here?
    for ( i = 0; i < allOperators.length; i++) {
        if (allOperators[i] == this.operator) {
            theOperator = allOperators[i];
        }
    }
    //remove the select, add the text node
    if (this.operatorCell!=null) {
        this.operatorCell.removeChild(this.operatorCell.firstChild);
        this.operatorCell.appendChild(document.createTextNode(this.operator));
        this.operatorCell.className='operatorCell';
    }
}

Filter.prototype.addFirstInput=function(value) {
    //create the fields to hold the actual filter string.
    var row = document.createElement('tr');
    this.inputString = document.createElement('input');
    this.inputString.value = value;

    this.inputString.type = "text";
    this.inputString.id = "filterString_" + new Date().getTime()

    if (this.operator == "IS NULL" || this.operator == "IS NOT NULL") {
        this.inputString.disabled = true;
    }
    var cell = document.createElement('td');
    cell.appendChild(this.inputString);
    row.appendChild(cell);

    var disabledString = (this.operator == "IS NULL" || this.operator == "IS NOT NULL") ? "disabled" : "";
    this.filterTableBody.appendChild(row);

    $(cell).insert('<div class="autocomplete"></div>');
    
    var params = "&field=" + this.attribute
        + "&requestingMethod=filterObject.addFirstInput"
        + "&datasetId=" + dataset
        + "&match_anywhere=false"
        + "&ignore_case=true&count=-1";

    var autocomplete = new Ajax.Autocompleter(this.inputString ,
            $(cell).down('.autocomplete') , 'textSuggest', 
                { parameters : params,
                  paramName : "query",
                  minChars: 0 });
    $(this.inputString).observe('focus', function () { autocomplete.activate() });
        
}

Filter.prototype.orButtonHandler=function() {
    this.addOrInput();    
}

/* Adds a field for another 'IN' item */
Filter.prototype.addOrInput=function(value) {

    var row = document.createElement('tr');
    var cell = document.createElement('td');

    //first pass.. we hope.
    if (this.inInputArray.length == 0 || this.orButton == null) {
        this.orButton = document.createElement('a');
        this.orButton.appendChild(document.createTextNode('Or'));
        this.orButton.className="orButton";
        this.orButton.orPosition=0;
        this.orButton.onclick=this.orButtonHandler.bindAsEventListener(this);
        this.orButton.onmouseout=this.mouseOutHandler.bindAsEventListener(this.orButton);
        this.orButton.onmouseover=this.mouseOverHandler.bindAsEventListener(this.orButton);
        cell.appendChild(this.orButton);    
    } else { //not first pass.. we also hope

        //make sure the previous field has values in it before increment.
        if ((this.inInputArray.length > 1 && this.inInputArray[this.inInputArray.length-2].value != "")
         || (this.inInputArray.length == 1 && this.inputString.value != "")) {
            //enable the input and create a text suggest.

            this.inInputArray[this.inInputArray.length - 1].disabled=false;
            if (this.getAttributeType() == TEXT_TYPE) {
        
                var existingInput = $(this.inInputArray[this.inInputArray.length - 1])
                var existingCell = existingInput.up();
               $(existingCell).insert('<div class="autocomplete"></div>');
                var params = "&field=" + this.attribute
                    + "&requestingMethod=filterObject.addFirstInput"
                    + "&datasetId=" + dataset
                    + "&match_anywhere=false&ignore_case=true&count=-1";
                var autocomplete = new Ajax.Autocompleter(existingInput,
                        $(existingCell).down('.autocomplete') , 'textSuggest', 
                        { parameters : params,
                          requestingMethod: "filterObject.addOrInput",
                          datasetId: dataset,
                          paramName : "query", 
                          minChars: 0 });
                $(existingInput).observe('focus', function () { autocomplete.activate() });
            }
            this.orButton.orPosition = this.orButton.orPosition + 2;
            this.orButton.style.marginTop= this.orButton.orPosition + 'em';
        }
    }

    //check that the previous field actually has value.
    if (this.inInputArray.length == 0
        || (this.inInputArray.length == 1 && this.inputString.value != "") 
        || (this.inInputArray.length > 1 && this.inInputArray[this.inInputArray.length-2].value != "")) {
        
        input = document.createElement('input');
        if (value != undefined) {
            input.value = value;
        }
        input.disabled = true;    
        input.type = "text";
        input.id = "filterString_" + new Date().getTime();
        row.appendChild(cell);
        cell.appendChild(input);
        this.filterTableBody.appendChild(row);
        this.inInputArray[this.inInputArray.length] = input;
    }
    
    this.fireFoxHack();
}


Filter.prototype.clearOrInput=function() {
    for (i = 0; i < this.inInputArray.length; i++) {
        this.filterTableBody.removeChild(this.inInputArray[i].parentNode.parentNode);
    }
    this.inInputArray=[];
    this.orButton = null;
    this.fireFoxHack();
}


/** Remove everything from the filter cell */
Filter.prototype.clearFilterCell=function() {
    while (this.filterCell.firstChild != null) {
        this.filterCell.removeChild(this.filterCell.firstChild);
    }
}

Filter.prototype.editButtonToText=function() {
    editText = document.createTextNode("edit");
    if (this.editButton != null) {
        this.editButton.parentNode.replaceChild(editText,this.editButton);
    }    
}

Filter.prototype.editTextToButton=function() {
    linksCell = this.filterCell.nextSibling;
    linksCell.replaceChild(this.createEditButton(),linksCell.firstChild);
}

Filter.prototype.fireFoxHack = function() {
    //ugly hack to make firefox redraw everythign proper like.
    //var filterCell = document.getElementById('filterListCell');    
    var newRow = document.createElement('tr');
    this.filterCell.parentNode.parentNode.appendChild(newRow);
    var newCell = document.createElement('td');
    newRow.appendChild(newCell);
    this.filterCell.parentNode.parentNode.removeChild(newRow);
}

/********************************************************
 *          VIEW FOR THIS OBJECT
 ********************************************************/

Filter.prototype.createView=function() {
    
    this.viewRow = document.createElement('tr');
    
    this.viewRow.onmouseover = this.focusBackgroundChange;
    this.viewRow.onmouseout = this.focusBackgroundOff;
    
    this.viewRow.onclick = this.selectFilterForEdit;

    var nameCell = document.createElement('td');
    nameCell.className = "columnNameCell";
    nameCell.appendChild(document.createTextNode(ATTRIBUTE_UI_MAP[this.attribute]));
    this.viewRow.appendChild(nameCell);
    this.viewRow.filter = this;
    
    
    //create a cell for the operator select
    var operatorCell = document.createElement('td');

    //find which operator is the one for this filter, if any    
    var allOperators = new Array("=","!=","IN", "NOT IN", "LIKE","NOT LIKE","IS NULL",
               "IS NOT NULL","<",">","<=",">=");
    var theOperator = "=";

    for ( i = 0; i < allOperators.length; i++) {
        if (allOperators[i] == this.operator) {
            theOperator = allOperators[i];
        }
    }
    
    operatorCell.appendChild(document.createTextNode(theOperator));
    operatorCell.className='operatorCell';
    this.operatorCell=operatorCell;
    this.viewRow.appendChild(operatorCell);
    this.viewRow.filter = this;

    //create a cell with a text input for the filter string
    var inputCell = document.createElement('td');
    this.filterCell = inputCell;
    var filterStringInput = document.createElement('input');
    //filterStringInput.name="filterDisplayString";
    filterStringInput.type="text";
    filterStringInput.readOnly="true";
    filterStringInput.className="filterStringInput";
    filterStringInput.value= " " + this.displayString;
    this.displayField = filterStringInput;
    inputCell.appendChild(filterStringInput);
    this.viewRow.appendChild(inputCell);

    //create the edit/delete button at the end of it.
    var deleteCell = document.createElement('td');
    deleteCell.appendChild(this.createEditButton());    
    deleteCell.appendChild(document.createTextNode(' / '));
    deleteCell.appendChild(this.createDeleteButton());

    this.viewRow.appendChild(deleteCell);

    return this.viewRow;
}

Filter.prototype.createEditButton = function() {
    editLink = document.createElement('a');
    this.editButton=editLink;
    editLink.onclick = this.selectFilterForEdit;
    editLink.onmouseover = this.mouseOverHandler.bindAsEventListener(editLink);
    editLink.onmouseout = this.mouseOverHandler.bindAsEventListener(editLink);
    editLink.name="editFilterButton";
    editLink.title="Edit this filter";
    editLink.appendChild(document.createTextNode('edit'));
    editLink.filter=this;
    return editLink;
}

Filter.prototype.createDeleteButton = function() {
    deleteLink = document.createElement('a');
    deleteLink.onclick = this.deleteFilter;
    deleteLink.onmouseover = this.mouseOverHandler.bindAsEventListener(deleteLink);
    deleteLink.onmouseout = this.mouseOverHandler.bindAsEventListener(deleteLink);
    deleteLink.name="deleteFilterButton";
    deleteLink.title="Delete this filter";
    deleteLink.appendChild(document.createTextNode('delete'));
    deleteLink.filter=this;
    return deleteLink;
}

Filter.prototype.clearBuilder = function() {

    //remove everything inside the div.
    if (this.filterBuilder != null) {
    
        //alert("Bad filter!");
        while (this.filterBuilder.firstChild != null) {
            this.filterBuilder.removeChild(this.filterBuilder.firstChild);
        }

        //this.filterCell.removeChild(this.filterBuilder);
        this.filterBuilder=null;
        this.operatorSelect=null;
        this.inputString=null;
        this.inInputArray=[];
    }
    
    this.openForEdit = false;
}

Filter.prototype.displayFilterBuilder = function() {
    // Already displayed, so don't allow it to be displayed again
    this.viewRow.onclick=null;
    this.editButton.onclick=null;
    //this.editButton.parentNode.appendChild(document.createTextNode("edit"));
    this.editButtonToText();
    
    this.clearFilterCell();

    if (this.filterBuilder == null) {
        this.filterBuilder = document.createElement('div');
        this.filterCell.appendChild(this.filterBuilder);
    }

    var theTable = document.createElement('table');
    theTable.id = "filterBuilderTable";
    this.filterTableBody = document.createElement('tbody');
    var theTableHead = document.createElement('thead');
    
    theTable.appendChild(theTableHead);

    if (this.type == TEXT_TYPE) {
        this.displayTextFilterBuilder();
    } else if (this.type == NUMBER_TYPE) {
        this.displayNumberFilterBuilder();
    } else if (this.type == DATE_TYPE) {
        this.displayDateFilterBuilder();
    } else {
        this.sample.updateStatus("<p class=\"error\">Unknown Filter Type '" 
            + this.type + "', unable to edit.</p>");
    }

    //create the fields for set and clear -- and a new table
    table = document.createElement('table');
    tableBody = document.createElement('tbody');
    row = document.createElement('tr');
    cell = document.createElement('td');
    cell.colSpan = 2;
    var input = document.createElement('input');
    input.filter = this;
    input.name = "setFilter";

    input.value = "Save";
    input.type = "button";
    input.className = "saveFilter";
    input.onclick = this.setFilter.bindAsEventListener(this);
    cell.appendChild(input);
    
    input = document.createElement('input');
    input.filter = this;
    input.name = "cancelFilter";
    input.className = "cancelFilter";
    input.value = "Cancel";
    input.type = "button"; 
    input.onclick = this.cancelEditThisFilter.bindAsEventListener(this);
    cell.appendChild(input);
    row.appendChild(cell);
    
    tableBody.appendChild(row);
    table.appendChild(tableBody);
    //this.filterTableBody.appendChild(row);

    theTable.appendChild(this.filterTableBody);
    
    this.filterBuilder.appendChild(theTable);
    this.filterBuilder.appendChild(table); //add a second table so that the Save/Cancel buttons are separate

    this.openForEdit = true;
}


Filter.prototype.displayNumberFilterBuilder= function(){

    //create the for fields for selection.
    row = document.createElement('tr');
    this.operatorSelect = document.createElement('select');
    this.operatorSelect.options[0]= new Option(" = ",'=');
    this.operatorSelect.options[1]= new Option(" \u2260 ","!="); //not equal to 
    this.operatorSelect.options[2]= new Option(" \u003c ","<"); //less than 
    this.operatorSelect.options[3]= new Option(" \u003e ",">"); //greater than
    this.operatorSelect.options[4]= new Option(" \u2264 ","<="); //less than or equal too
    this.operatorSelect.options[5]= new Option(" \u2265 ",">="); //greater than or equal too
    
    cell = document.createElement('td');
    cell.appendChild(this.operatorSelect);
    row.appendChild(cell);
    // Remove the text representation; add the select box for operator
    this.operatorCell.removeChild(this.operatorCell.firstChild);
    this.operatorCell.appendChild(this.operatorSelect);
    // Remove the text representation; add the input field for the filter
    //this.filterCell.removeChild(this.filterCell.firstChild);    
    
    for ( i = 0; i < this.operatorSelect.options.length; i++) {
        if (this.operatorSelect.options[i].value == this.operator) {
            this.operatorSelect.options[i].selected = true;
        }
    }
    
    //create the fields to hold the actual filter string.
    //row = document.createElement('tr');
    //this.inputString = document.createElement('input');
    //input.name = "filterBuilderString";
    //input.id = "filterBuilderString";
    
    //create the fields to hold the actual filter string.
    row = document.createElement('tr');
    this.inputString = document.createElement('input');
    //input.name = "filterBuilderString";    
    this.inputString.value = this.displayString;
    this.inputString.type = "text";
    cell = document.createElement('td');
    cell.appendChild(this.inputString);
    row.appendChild(cell);
    this.filterTableBody.appendChild(row);
}

Filter.prototype.displayTextFilterBuilder = function() {

    this.operatorSelect = document.createElement('select');
    this.operatorSelect.options[0]= new Option(" = ",'=');
    this.operatorSelect.options[1]= new Option(" \u2260 ","!="); //not equal to 
    this.operatorSelect.options[2]= new Option(" In ","IN");
    this.operatorSelect.options[3]= new Option(" Not In ","NOT IN");
    this.operatorSelect.options[4]= new Option("Like","LIKE");
    this.operatorSelect.options[5]= new Option("Not Like","NOT LIKE");    
    
    if (this.attribute == 'problemDescription'
     || this.attribute == 'conditionName'
     || this.attribute == 'type'
      || this.attribute == 'schoolName'
     || this.attribute == 'transactionTypeTool'
     || this.attribute == 'transactionTypeTutor'
     || this.attribute == 'transactionSubtypeTool'
     || this.attribute == 'transactionSubtypeTutor') {    
        this.operatorSelect.options[6]= new Option("Is Null","IS NULL");
        this.operatorSelect.options[7]= new Option("Is Not Null","IS NOT NULL");
    }
    this.operatorSelect.onchange = this.selectonchangeHandler.bindAsEventListener(this);
    
    // Remove the text representation; add the select box for operator
    this.operatorCell.removeChild(this.operatorCell.firstChild);
    this.operatorCell.appendChild(this.operatorSelect);    

    for ( i = 0; i < this.operatorSelect.options.length; i++) {
        if (this.operatorSelect.options[i].value == this.operator) {
            this.operatorSelect.options[i].selected = true;
        }
    }

    if (this.filterString.length > 0) {
        this.addFirstInput(this.filterString[0]);
        // if operator is '=', create an 'or' button; if it was set to IN, support that as well
        if (this.operatorSelect.value == '=' 
         || this.operator == 'IN'
         || this.operator == '!='
         || this.operator == 'NOT IN') {
            //fill back in the values from the filterString array.
            for(brett = 1; brett < this.filterString.length; brett++) {
                this.addOrInput(this.filterString[brett]);
            }
            this.addOrInput();
        }
    } else {
        this.addFirstInput("");
        if (this.operatorSelect.value == '='
        || this.operator == 'IN' 
        || this.operator == '!='
        || this.operator == 'NOT IN') {
            this.addOrInput();
        }
    }
}

Filter.prototype.displayDateFilterBuilder = function() {

    //create the for fields for selection.
    this.operatorSelect = document.createElement('select');
    this.operatorSelect.options[0]= new Option(" = ",'=');
    this.operatorSelect.options[1]= new Option(" \u2260 ","!="); //not equal to 
    this.operatorSelect.options[2]= new Option(" \u003c ","<"); //less than 
    this.operatorSelect.options[3]= new Option(" \u003e ",">"); //greater than
    this.operatorSelect.options[4]= new Option(" \u2264 ","<="); //less than or equal too
    this.operatorSelect.options[5]= new Option(" \u2265 ",">="); //greater than or equal too

    // Remove the text representation; add the select box for operator
    this.operatorCell.removeChild(this.operatorCell.firstChild);
    this.operatorCell.appendChild(this.operatorSelect);
    
    //see if the operator already is set, if so, select correct one in list.
    for ( i = 0; i < this.operatorSelect.options.length; i++) {
        if (this.operatorSelect.options[i].value == this.operator) {
            this.operatorSelect.options[i].selected = true;
        }
    }
    
    //create the fields to hold the actual filter string.
    var row = document.createElement('tr');
    this.inputString = document.createElement('input');
    this.inputString.type = "text";

    //remove the surrounding qoutes if necessary.
    var fString = this.displayString;
    if (fString.charAt(0) == "'") {
        fString = fString.substring(1, fString.length -1);
    }
    if (fString.charAt(fString.length -1) == "'") {
        fString = fString.substring(0, fString.length - 2);
    }
    this.inputString.value = fString;

    var cell = document.createElement('td');
    cell.appendChild(this.inputString);
    row.appendChild(cell);
    this.filterTableBody.appendChild(row);
}

Filter.prototype.selectonchangeHandler = function() {
    var value = this.operatorSelect.options[this.operatorSelect.selectedIndex].value;
    
    if (value == "=" || value == "IN" || value == "!=" || value == "NOT IN") {
        if (this.orButton == null) {
            this.addOrInput();    
        }
    } else {
        this.clearOrInput();
    }
    
    if (value == "IS NULL" || value == "IS NOT NULL") {
        this.inputString.value = "";
        this.inputString.disabled = true;
        //alert(this.inputString.value);
    } else {
        this.inputString.disabled = false;
    }
}


Filter.prototype.mouseOverHandler = function(){
    //simply use this to reference the object.
    //aka  this.style.color = "#00000";
    this.style.cursor="pointer";
}

Filter.prototype.mouseOutHandler = function(){
    //simply use this to reference the object.
    //aka  this.style.color = "#00000";
    this.style.cursor="default";
}

Filter.prototype.replaceChars = function(entry) {
    out = "'"; // replace this
    add = "''"; // with this
    temp = "" // temporary holder

    for(pos = 0; pos < entry.length; pos++) {
        if (entry.substring(pos, (pos + 1)) == out) {
            temp += add;
        } else {
            temp += entry.substring(pos, (pos + 1));
        }
    }
    return temp;
}
