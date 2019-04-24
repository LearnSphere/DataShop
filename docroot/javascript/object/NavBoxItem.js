//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2007
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 10940 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2014-04-25 12:59:47 -0400 (Fri, 25 Apr 2014) $
// $KeyWordsOff: $
//

/**
 * A single item in a navigation list.  Mostly a dumb item that only knows
 * how to display itself, display tool tips, and whether or not it's selected.
 * 
 * DEPENDENCIES: prototype v1.6
 */
var NavBoxItem = Class.create();
NavBoxItem.prototype = {

    /**
     * Constructor
     * @param listId (Required) - the id or actual element, expected to be the id of a div
     *    containing an unordered list.
     * @param item (Required) - an list item as a JSON object with 
     *    > item.id - the database identifier of the item,
     *    > item.name - the display name of the item
     *    > item.isSelected - whether the item is selected
     * @param options (Optional) - additional options for the menu.
     */
    initialize: function(listId, item, options) {
        this.list = $(listId);
        this.itemId = item.id;
        this.name = item.name;
        this.isSelected = item.isSelected;
        this.item = false; //initially set the HTML element to false.
        
        //update any default values if there are some.
        this.options = {
            multiSelectMode : true,
            mouseoverInfo   : false,
            mouseoverInfoURL: false,
            nameLength      : 18
        };
        Object.extend(this.options, options || { });

        this.htmlString = this.createView();
        
    },

    /**
     * This will set whether or not this is a selected item
     * based on the passed in boolean. Works by adding or removing the "selectedItem"
     * CSS class from this item.
     * @param isSelected boolean indicating whether this item is selected.
     */
    setSelected: function(isSelected) {
        if (isSelected && !this.isSelected) {
            this.isSelected = true;
            $(this.item).addClassName("selectedItem");
        } else if (!isSelected && this.isSelected) {
            this.isSelected = false;
            $(this.item).removeClassName("selectedItem");
        }
    },

    /**
     * Create the HTML required for this item.  Do not do the actual insert at this point.  All
     * navigation items will be inserted as a single insert in the NavigationBox class for 
     * performance reasons.
     */
    createView: function() {    
        var linkCSSClassName = (this.options.multiSelectMode) ? "multiSelect" : "singleSelect";
    	var listCSSClassName = (this.isSelected) ? "selectedItem" : "";
    	var toDisplay = this.name.truncate(this.options.nameLength).escapeHTML().gsub('"', '&quot;');;
    	var itemName = this.name.escapeHTML().gsub('"', '&quot;');

        var titleString = (this.options.mouseoverInfo == true) ? "" : 'title="' + itemName + '" ';
        

        var htmlString = 
            '<li class="' + listCSSClassName + '">' +
            '<a class="' + linkCSSClassName + '" name="' + itemName + '" ' + titleString + '>'
            + toDisplay + '</a>'
            + '</li>';

        return htmlString;
    },

    /**
     * Get the HTML that for this item.
     */
    getHTML: function() {
        return this.htmlString;
    },

    /**
     * Initialize the observers on the passed in DOM Item (since the create view only returns
     * the HTML of this item and doesn't do the actual insert itself.
     * @param domItem the HTML list item that was created as a result of the HTML in create view. 
     */
    initObservers: function(domItem) {
        this.item = $(domItem);
        $(this.item).observe('mouseover', this._mouseoverHandler.bindAsEventListener(this));
        $(this.item).observe('mouseout', function(event) { this.removeClassName("hover"); });
        //Q: Wait! where is the 'click' handler???
        //A: There is none in this class. The click handler for these "dumb" 
        //objects lies out in the interface list.  Since the main list needs to know when 
        //and if the items are clicked on to call an update to the server the clicking is handled
        //out there.  But since mouseover and mouseout are really functions that don't need to know
        //anything other than "the mouse is (not) over this item" they can be handled here.
        
        if (this.options.mouseoverInfo == true) {
            if (this.toolTip == null) {
                var options = new Array();
                options['extraClasses'] = 'infoDiv';
                options['fixed'] = 'true';
                options['onCreate'] = function (tt) {
                     // make the Ajax call to update, but do it on a delay so that
                     // the initial stuff has a chance to drawn on the page and be
                     // updated properly.      
                     new Ajax.Request(this.options.mouseoverInfoURL, {
                        parameters: {
                            requestingMethod: "NavBoxItem.initObservers",
                            datasetId: dataset,
                            ajaxRequest: "true",
                            navigationUpdate: "true",
                            list: this.list.id,
                            action: "getInfo",
                            itemId: this.itemId
                        },
                        onComplete: function(transport) {
                             this.toolTip.replaceContent(transport.responseText);
                        }.bindAsEventListener(this),
                        onException: function (request, exception) {
                            throw(exception);
                        }
                    });
                }.bind(this);
                this.toolTip = new ToolTip(this.item, 'Retrieving Info...', options);
            }
        }
    },


    /**
     * Handler function which fires whenever a list item's "mouseover" event fires.
     * It is responsible for turning on the "hover" class and any tool-tip that may exist
     * for this list item.
     * @param event the mouse event
     */
    _mouseoverHandler: function(event) {
        this.item.addClassName("hover");
    }
};