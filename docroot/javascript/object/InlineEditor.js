//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 10610 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2014-02-21 15:10:45 -0500 (Fri, 21 Feb 2014) $
// $KeyWordsOff: $
//

/**
 * This class is designed to add inline editing to a piece of text.  It adds the ability 
 * transform that text into an editable text field. 
 * @param aElement - the DOM element containing the text to edit. Required
 * @param aFormFieldType - the form field type to use when in edit state. Default: input
 * @param aTarget - a URL target for the Ajax calls. Default: window.location
 * @param options - additional options.
 *       hoverColor: the background color to use when hovering. String of the form 'black' or '#DEDEDE'
 *       toolTips: Boolean to display tool tips. true(default)/false
 *       suggestions: Array of suggestions that will appear as a drop-down list
 *                       as a JSON array with "text" and "value" attributes
 *       suggestionParams: Array of parameters to be placed in an AJAX call to get a JSON
 *                    get and refresh the list of suggestions.
 *       allowOther: Flag indication that the suggestions list is not absolute.
*******************************/
function InlineEditor(aElement, aFormFieldType, aTarget, options) {
    this.element = aElement;  //element which contains the text to edit.

    if (aTarget == null ) {    
        this.target = window.location;        
    } else {
        this.target = aTarget;
    }

    if (!aFormFieldType) { 
        this.formFieldType = 'input';
    } else {
        this.formFieldType = aFormFieldType;
    }

    this.originalText = false;
    this.elementBackgroundColor = null;
    this.undoText = null;

    //$(this.element).setStyle({position: "relative"});

    var browser = navigator.userAgent.toLowerCase();
    this.isIE        = browser.indexOf("msie") != -1;
    this.isOpera     = browser.indexOf("opera")!= -1;
    
    this.handlerCache = new Array();
    this.setOptions(options);
    this.initialize();
}

// Constant used for modifying 'Has Study Data' value.
var STUDY_FLAG_EXTRA_STR = " -- please set this field";

/**
 * Modifies the default options.
 */
InlineEditor.prototype.setOptions = function(options) {
    this.options = {
        hoverColor      : '#EFEFEF',
        toolTips        : true,
        suggestions     : null,
        suggesionParams : null,
        allowOther      : false,
        allowOtherTxt   : 'other',
        maxLength       : 255,
        truncatorOptions: null,
        onSuccess       : null,
        onFailure       : null,
        id              : null
    };
    Object.extend(this.options, options || { });
}

/**
 * Initialize the object, setting state and event listeners.
 */
InlineEditor.prototype.initialize = function() {
    this.state = 'view'; //the current state of this text field.
    this.enableEventHandlers();
    if (this.options.truncatorOptions) {
        // create a new truncator for this object
        this.truncator = new Truncator(this.element, this.options.truncatorOptions);
    }

    // Add edit icon...
    new Insertion.Bottom(this.element, '<img src="images/pencil.png" title="Click to edit" '
                         + ' id="'+ this.element.id + '_edit" class="edit_icon" >');
}

InlineEditor.prototype.enableEventHandlers = function() {
    //cache the handlers so we can disable the events later
    this.handlerCache['mouseover'] = this.mouseoverHandler.bindAsEventListener(this);
    this.handlerCache['mouseout'] = this.mouseoutHandler.bindAsEventListener(this);
    this.handlerCache['click'] = this.clickHandler.bindAsEventListener(this);

    //helps safari not be stupid
    this.element.onmouseover = function () { return false; }
    this.element.onmouseout = function () { return false; }
    this.element.onclick = function () { return false; }    

    Event.observe(this.element, 'mouseover', this.handlerCache['mouseover']);
    Event.observe(this.element, 'mouseout', this.handlerCache['mouseout']);
    Event.observe(this.element, 'click', this.handlerCache['click']);
    
    
    //enable the event handlers for the undo button if it exists.
    if ($(this.element.id + "_undo")) {
       Event.observe(this.element.id + "_undo", 'mouseover', this.undoMouseoverHandler);
       Event.observe(this.element.id + "_undo", 'mouseout', this.undoMouseoutHandler); 
       Event.observe(this.element.id + "_undo", 'click', this.undoClickHandler.bindAsEventListener(this));      
    }

    //enable the event handlers for the edit button if it exists.
    if ($(this.element.id + "_edit")) {
       Event.observe(this.element.id + "_edit", 'mouseover', this.undoMouseoverHandler);
       Event.observe(this.element.id + "_edit", 'mouseout', this.undoMouseoutHandler); 
    }
}

InlineEditor.prototype.disableEventHandlers = function() {
    Event.stopObserving(this.element, 'mouseover', this.handlerCache['mouseover']);
    Event.stopObserving(this.element, 'mouseout', this.handlerCache['mouseout']);
    Event.stopObserving(this.element, 'click', this.handlerCache['click']);
}

/**
 * Switch the state from view to edit, swapping out the element text for an editable form.
 */
InlineEditor.prototype.openForEdit = function() {
    this.state = 'edit'
    this.disableEventHandlers();
    if ($(this.element.id + "_undo")) { $(this.element.id + "_undo").remove(); }

    // get the full text from the truncator object.
    if (this.element.firstChild) {
        //this.originalText = this.element.innerHTML.stripTags();
        if (this.truncator) {
            this.originalText = this.truncator.fullText.stripTags();
        } else {
            this.originalText = this.element.innerHTML.stripTags();
        }
        this.element.innerHTML = "";
        if (!this.originalText) { this.originalText = ''; }
    } else {
        this.originalText = '';
    }

    if (this.options.suggestionParams != null) {
        var indicatorString = "<span id=\"" + this.element.id + "_indictor\">";
        indicatorString += "<img src=\"images/indicator.gif\" title=\"loading, please wait...\" />";
        indicatorString += "</span>";
        new Insertion.Top(this.element, indicatorString);
        this.refreshSuggestions();
    } else {
        this.createView();
    }
}
 
/**
 * Create all the items necessary for the view.
 */
InlineEditor.prototype.createView = function(transport) {

    if ($(this.element.id + "_indictor")) { $(this.element.id + "_indictor").remove(); }

    //add "openInlineEdtior" class to the base element class list.
    this.element.className += " openInlineEditor";

    //create the form element and buttons
    if (this.options.suggestions == null || this.options.allowOther) {
        this.formElement = document.createElement(this.formFieldType);
        this.formElement.name = this.element.id;
        this.formElement.value = this.originalText == " " ? "" : this.originalText;
        this.formElement.maxLength = this.options.maxLength;
        if (this.formFieldType == 'input' 
               && !this.options.suggestions && !this.options.suggestionParams) { 
            this.formElement.className = "inlineTextBox";
        }
    }

    //create the suggestions drop-down list.
    if (this.options.suggestionParams != null || this.options.suggestions != null) {

        var suggs = null;
        if (transport != null) {
            transportJSON  = transport.responseText.evalJSON(true);
            suggs = transportJSON.suggestions;
        } else {
            suggs = this.options.suggestions.evalJSON(true);
        }
        
    var sugId = "suggestBox_" + this.element.id;
    var selectString = "<select class=\"suggestionBox\" id=\"" + sugId + "\"></select>";
        
        new Insertion.Bottom(this.element, selectString);
        this.select = $(sugId);
        this.select.onchange = this.selectSuggestion.bindAsEventListener(this);

        this.select.options.length = 0;
        var selectIndex = -1;

         for (j = 0; j < suggs.length; j++) {
            this.select.options[j] = new Option(suggs[j].text, suggs[j].value);
            this.select.options[j].title = suggs[j].text;
            if (suggs[j].value == this.originalText || suggs[j].text == this.originalText) {
                selectIndex = j;
                if (this.formElement) {
                    this.formElement.disabled = true;
                    this.formElement.value = '';
                }
            }
         }

         if (this.options.allowOther) {
             this.select.options[this.select.options.length]
                 = new Option(this.options.allowOtherTxt, this.options.allowOtherTxt);
             this.select.options[this.select.options.length - 1 ].className = "inlineOther";
             this.select.options[this.select.options.length - 1 ].title = this.options.allowOtherTxt;
             if (selectIndex == -1) {
                selectIndex = this.select.options.length - 1;
                this.formElement.disabled = false;
                this.formElement.value = this.originalText;
            }
         } else {
             if (this.formElement) {
                 this.formElement.disabled = true;
                 this.formElement.value = '';
             }
         }
    
        this.select.focus();
        if (selectIndex >= 0) {
            this.select.selectedIndex = selectIndex;
        }
    }

    if (this.formElement) {    this.element.appendChild(this.formElement); }

    new Insertion.Bottom(this.element, "<br />")
    new Insertion.Bottom(this.element, "<input type='button' value='save' name=\"saveButton\" />");    
    new Insertion.Bottom(this.element, "<input type='button' value='cancel' name=\"cancelButton\" />");
    
    //set the events handling.
    $(this.element).immediateDescendants().each(function(item) {
          if (item.name == "saveButton") {
             //helps safari not be stupid
            item.onclick = function () { return false; }    
              Event.observe(item, 'click', this.save.bindAsEventListener(this));
          }
          if (item.name == "cancelButton") {
             //helps safari not be stupid
            item.onclick = function () { return false; }    
              Event.observe(item, 'click', this.cancel.bindAsEventListener(this));
          }
    }.bind(this));

    if (this.options.suggestions == null && this.options.suggestionParams == null) {
        this.formElement.focus();  
    }
}

InlineEditor.prototype.refreshSuggestions = function() {

    new Ajax.Request(this.target, {
        parameters: this.options.suggestionParams,
        requestHeaders: {Accept: 'application/json;charset=UTF-8'},
        onComplete: this.createView.bindAsEventListener(this),
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/**
 * Select an object form the drop down selection list.   Watches to see if the "other"
 * option is chosen to enable the text field.
 */
InlineEditor.prototype.selectSuggestion = function() {
    if (this.options.allowOther) {
        if(this.select.selectedIndex == (this.select.options.length - 1) ) {
            this.formElement.disabled = false;
        } else {
            this.formElement.disabled = true;
        }
    }
}

/**
 * 
 */
InlineEditor.prototype.undoLastEdit = function() {
    this.undoCallSent = true;
    if (this.undoText) {
        var theValue = this.cleanUndoText();
        new Ajax.Request(this.target, {
            parameters: {
                requestingMethod: "InlineEditor.undoLastEdit",
                datasetId: dataset,
                projectId: projectId,
                ajaxRequest: "true",
                inline_edit: "true",
                field: this.element.id,
                value: theValue,
                id: this.options.id
            },
            onComplete: this.saveResultsAJAXListener.bindAsEventListener(this),
            onException: function (request, exception) {
                throw(exception);
            }
        });
        //new net.ContentLoader(this.target, this.saveResultsListener, null, 'POST', postParams, this);
    } else {
       errorPopup("Last edit unavailible, unable to undo.");
       if ($(this.element.id + "_undo")) { $(this.element.id + "_undo").remove(); }
    }
}

/**
 * Function to clean undoText, if necessary.
 * Sigh. This is specific to 'Has Study Data'.
 */
InlineEditor.prototype.cleanUndoText = function() {

    var tmpUndoText = this.undoText;

    // Strip off unwanted text.
    var index = this.undoText.indexOf(STUDY_FLAG_EXTRA_STR);
    if (index > 0) {
        tmpUndoText = this.undoText.substring(0, index);
    }
    return tmpUndoText;
}

/**
 * Save the results of an edit.  Sends an Ajax call to 
 * the server with the new information.
 */
InlineEditor.prototype.save = function(event) {
    
    var hasSuggs = (this.options.suggestions != null || this.options.suggestionParams != null) ? true : false;
    

    if (hasSuggs) {
        //if selections are allowed use them.
        if (hasSuggs && (!this.options.allowOther
                || this.select.selectedIndex != (this.select.options.length - 1))) {
             var newValue = this.select.options[this.select.selectedIndex].value;
        } else {
             var newValue = this.formElement.value;            
        }  
    } else {
        var newValue = this.formElement.value;
    }
    
    new Ajax.Request(this.target, {
        parameters: {
            requestingMethod: "InlineEditor.save",
            datasetId: dataset,
            projectId: projectId,
            ajaxRequest: "true",
            inline_edit: "true",
            field: this.element.id,
            value: newValue,
            id: this.options.id
        },
        onComplete: this.saveResultsAJAXListener.bindAsEventListener(this),
        onException: function (request, exception) {
            throw(exception);
        }
    });

    Event.stop(event);
}

/**
 * This function catches the results of the Ajax save call, and redirects to the proper results.
 */
InlineEditor.prototype.saveResultsAJAXListener = function(transport) {
    var response = transport.responseText;
    var data = response.evalJSON(true);

    var messageType = data.messageType;
    var message = data.message;
    var value = data.value;

    if (messageType == "ERROR"
            || messageType == 'UNAUTHENTICATED'
            || messageType == 'UNAUTHORIZED') {
        this.processError(message); 

    } else if (messageType == 'SUCCESS') {
        this.processSave(message, value);

    } else if (messageType == 'MESSAGE') {
        this.closeEdit();
        messagePopup(message, "MESSAGE");

    } else {
        this.processError("Unexpected error from the server. "
        + "Please try again. If the error persists, contact the DataShop team.");       
    }
}

/**
 * Process a successful save of the new information.  
 */
InlineEditor.prototype.processSave = function(successMsg, newTxt) {
    this.undoState = (this.undoState == "undo" && this.undoCallSent) ? "redo" : "undo";
    this.undoCallSent = false;

    successPopup(successMsg);
    this.undoText = this.originalText;
    this.originalText = newTxt;

    if (this.truncator) {
        this.truncator.fullText = "" + newTxt;
    }

    this.updateCellIfNec(newTxt);

    this.closeEdit();
    if (this.options.onSuccess) { this.options.onSuccess.call(); }
}

/**
 * Function to update cell attrs, if necessary.
 * Sigh. This is specific to 'Has Study Data'.
 */
InlineEditor.prototype.updateCellIfNec = function(newTxt) {

    // Determine if the 'editRequired' class is needed.
    var theText = "" + newTxt;
    if ((this.element.id == 'studyFlag') && (theText == 'Not Specified')) {
        $(this.element).addClassName("editRequired");
        this.originalText += STUDY_FLAG_EXTRA_STR;
    } else {
        $(this.element).removeClassName("editRequired");
    }
}

/**
 * Process a successful save of the new information.  
 */
InlineEditor.prototype.processError = function(errorTxt) {
    errorPopup(errorTxt);
}


/** 
 * Cancel the edit, switch the form back to the original text.
 */
InlineEditor.prototype.cancel = function(event) {
    Event.stop(event);
    this.closeEdit();
}

/**
 * Remove the various edit elements and restore the text.
 */
InlineEditor.prototype.closeEdit = function() {

    $(this.element).immediateDescendants().each(function (item) {
        item.parentNode.removeChild(item);
    });

    if (this.truncator) {
        this.truncator.init();
    } else {
        this.element.innerHTML = this.originalText == "" ? " " : this.originalText;
    }     
    this.state = 'view';
    //this.element.style.backgroundColor = this.elementBackgroundColor;

    $(this.element).removeClassName("openInlineEditor");
    $(this.element).removeClassName("editHover");

    // Put the edit icon back in.
    this.element.insert({'bottom' : '<img src="images/pencil.png" title="Click to edit" '
                         + ' id="'+ this.element.id + '_edit" class="edit_icon" >'});

    // Even though we want this before the edit icon, floating means it has
    // to be inserted after. 
    if (this.undoText && this.undoState == 'redo') {
        this.element.insert({'bottom' : '<span title="click to undo last edit" '
                    + ' id="'+ this.element.id + '_undo" class="redo_button" >redo</span>'});
    } else if (this.undoText) {
        this.element.insert({'bottom' : '<span title="click to undo last edit" '
                    + ' id="'+ this.element.id + '_undo" class="undo_button" >undo</span>'}); 
    }

    this.enableEventHandlers();
}


/** Event Handling */
InlineEditor.prototype.mouseoverHandler = function(event) {

    $(this.element).addClassName("editHover");
    Event.stop(event);
}

InlineEditor.prototype.mouseoutHandler = function(event) {
    $(this.element).removeClassName("editHover");
    Event.stop(event);
}

InlineEditor.prototype.clickHandler = function(event) {
    if (this.state == "view") { this.openForEdit(); }
    Event.stop(event);
}

InlineEditor.prototype.undoMouseoverHandler = function(event) {
    $(this).addClassName('hover');
    Event.stop(event);
}

InlineEditor.prototype.undoMouseoutHandler = function(event) {
    $(this).removeClassName('hover');
    Event.stop(event);
}

InlineEditor.prototype.undoClickHandler = function(event) {
    if (this.state == "view") { this.undoLastEdit(); }
    Event.stop(event);
}
