//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2007
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 7245 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2011-11-09 10:12:24 -0500 (Wed, 09 Nov 2011) $
// $KeyWordsOff: $
//

/** 
 * Shell object for the navigation menu 
 *
 * DEPENDENCIES: prototype v1.6, script.aculo.us dragdrop.js v1.8.0
 * */
var NavigationBox = { };
var MAX_TO_DISPLAY = 500;

/**
 * This contains the bare minumum of functionality for a navigation menu.
 * All left hand side navigation menus should implement this class.
 */
NavigationBox.Base = Class.create({
    
    /**
     * Constructor
     * @param handle (Required) - the id or actual element, expected to be the id of a div
     *   of a navigation box.
     * @param options (Optional) - additional options for the box.
     */
    initialize: function(handle, options) {
        this.handle = $(handle);                
        this.options = {
            isResizable : false,
            maxSize     : 300,
            minSize     : 150
        };
        Object.extend(this.options, options || { });
        
        this.options.minSize = (this.handle.getHeight() < this.options.minSize) ? 
            this.handle.getHeight() + 6 : this.options.minSize;

        this._addControls();
    },

    /** Public Helper function to collapse the box. */
    collapseMenu: function () { this._toggleMenu(true); },

    /** Public Helper function to expand the box. */
    expandMenu: function () { this._toggleMenu(false); },

    /** 
     * Refresh the state of the menu to make sure everything that is supposed to be hidden
     * is actually hidden, and that everything that is supposed to be shown is shown.
     */
    refreshViewState: function () {
        var collapseState = readCookie("nav_menu_" + this.handle.id + "_state");
        if (collapseState == "hide") {
            this.collapseMenu();
        } else {
            this.expandMenu();
            createCookie("nav_menu_" + this.handle.id + "_state", "show", 100);
        }
    },

    /**
     * Adds the default controls for any menu.
     */
    _addControls: function() {
        
        //look to see if the collapse state is saved as a cookie, if it is use that,
        //else default to show.
        var collapseState = readCookie("nav_menu_" + this.handle.id + "_state");
        var spanClassName = (collapseState == "hide") ? "expand" : "collapse";

        //look at the first H2 which is the title and insert the expand/collapse button.        
        this.handle.down('h2').insert({after : '<span class="icon ' + spanClassName + '"></span>'});

        //get the button we just inserted.
        var iconSpan = this.handle.down('span.icon');

        iconSpan.observe('click', this._menuToggleHandler.bindAsEventListener(this));
        iconSpan.observe('mouseover', function (event) { $(this).addClassName('hover');});
        iconSpan.observe('mouseout', function (event) { $(this).removeClassName('hover');});
        
        if (this.options.isResizable) {
            this.handle.insert('<span class="resize_bar"></span>');
            new Draggable(this.handle.down("span.resize_bar"),
               {
                   constraint:'vertical',
                   snap: function(x, y) {
                       var min = 0 - (this.startSize - this.options.minSize);
                       return [
                          x,
                          y < min ? min : ((y + this.startSize) > this.options.maxSize
                               ? (this.options.maxSize - this.startSize) : y)];
                   }.bind(this),
                   onStart: function(dragable, event) {
                       this.mouseStart = event.clientY;
                       this.startSize = this.handle.getHeight();
                   }.bind(this),
                   onDrag: function (dragable, event) {
                         if (this.mouseStart) {
                             var newSize = this.startSize + (event.clientY - this.mouseStart);
                             this.updateHeight(newSize);
                         }
                   }.bind(this),
                   onEnd: function(dragable, event) {
                        createCookie("nav_menu_" + this.handle.id + "_size",
                            this.handle.style.height , 100);
                   }.bind(this)
               });
        }

        if (collapseState == "hide") { 
            this.collapseMenu();
        } else {
            createCookie("nav_menu_" + this.handle.id + "_state", "show", 100);
        }
    },

    updateHeight: function(size) {
        if (this.options.maxSize && size > this.options.maxSize) {
            this.handle.style.height = this.options.maxSize + "px";
        } else if (this.options.minSize && size < this.options.minSize) {
            this.handle.style.height = this.options.minSize + "px";
        } else {
            this.handle.style.height = size + "px";
        }

    },

    /**
     * Event handler for the expand/collapse button.
     * @param event prototype Event.
     */
    _menuToggleHandler: function(event) {
        //looks to see if the current icon is the "collapse" icon, if it
        //is we know the menu is currently expanded.
        if (event.element().hasClassName('collapse')) {
            this.collapseMenu();
        } else {
            this.expandMenu();
        }
    },

    /**
     * Toggles the menu between expanded and collapsed.
     * @param collapseMenu boolean indicating whether to collapse the menu
     *    will expand the menu if set to false, collapse if set to true.
     */
    _toggleMenu: function (displayMenu) {
        var iconElement = this.handle.down("span.icon");
        
        if (displayMenu) {
            iconElement.removeClassName('collapse');
            iconElement.addClassName('expand');
            iconElement.title = "Click here to expand the menu";
            invoker = "hide";
        } else {
            iconElement.removeClassName('expand');
            iconElement.addClassName('collapse');
            iconElement.title = "Click here to collapse the menu";
            invoker = "show";
        }
    
        //hide all the children except for the first h2 which is the title bar.
        var children = this.handle.childElements();
        children = children.without(this.handle.down('div.navigationBoxHeader'), this.handle.down('p.itemCount'));
        children.invoke(invoker);

        //save this state as a cookie.. mm... yummy cookie.
        createCookie("nav_menu_" + this.handle.id + "_state", invoker, 100);
    }
});  //END OF CLASS NavigationBox.Base

/**
 * This is a full blown navigation menu with all selectable elements.
 * Extends the NavigatoinMenu.Base
 */
NavigationBox.Component = Class.create(NavigationBox.Base, {
    
    /**
     * Constructor
     * @param $super - NavigatoinBox.Base (automatically included, user can ignore).
     * @param handle (Required) - the id or actual element, expected to be the id of a div
     *   of a navigation box.
     * @param title (Required) - text title of the navigation box.
     * @param url (Required) - URL for the ajax calls to get the item list and select/deselect them.
     * @param options (Optional) - additional options for the box.
     */
    initialize: function($super, handle, title, url, options) { 
        this.handle = $(handle);
        this.title = title;
        this.url = url
        this.itemList = [];
     
        this.options = {
            objectClassName : 'listObject',
            timer           : 1000,
            multiSelect     : true,
            itemInfo        : false,
            itemInfoURL     : url,
            observer        : false
        };
        Object.extend(this.options, options || { });

        this._createView();
        
        //finally call the NavigationBox.Base constructor with the handle.
        $super(handle, this.options);
    },

    /**
     * Adds the elements to the DOM that are required for the menu. Also initializes
     * all event handling.
     */
    _createView: function() {
        this.handle.insert("<div class=\"navigationBoxHeader\"><h2>" + this.title + "</h2></div>");
        
        if (this.options.multiSelect) {
            this.handle.insert(
                '<p><a id="' + this.handle.id +  '_select_all">select all</a> | '
              + '<a id="' + this.handle.id +  '_deselect_all">deselect all</a></p>');

            var selectAll = $(this.handle.id +  '_select_all');
            selectAll.observe('click', this._selectAllClickHandler.bindAsEventListener(this));
            selectAll.observe('mouseover', function (event) { $(this).addClassName('hover');});
            selectAll.observe('mouseout', function (event) { $(this).removeClassName('hover');});

            var deselectAll = $(this.handle.id +  '_deselect_all');
            deselectAll.observe('click', this._deselectAllClickHandler.bindAsEventListener(this));            
            deselectAll.observe('mouseover', function (event) { $(this).addClassName('hover'); } );
            deselectAll.observe('mouseout', function (event) { $(this).removeClassName('hover'); });
        }
    
        this.handle.insert('<ul class="interfaceList" id="'+ this.handle.id + '_list"></ul>');

        this.handle.insert('<p id="' 
            + this.handle.id + '_itemCount" class="itemCount"></p>');

        this._getItemList();
    },

    /**
     * Wipe out and recreate the item list.
     */
    refreshList: function () {
        this.handle.down('ul').remove();
        this.handle.insert('<ul class="interfaceList" id="'+ this.handle.id + '_list"></ul>');
        this._getItemList();
        //DS706 commenting this out for DS706.
        //Leaving in line commented out for now as I wonder how on earth this was ever right.
        //Maybe there is something I still do not understand. [ads 8/12/09]
        //this.invokeObserver();
    },

    /**
     * Makes the ajax call to get the list of items for this menu.
     */
    _getItemList: function () {        
        new Ajax.Request(this.url, {
            parameters: {
                requestingMethod: "NavigationBox._getItemList",
                datasetId: dataset,
                ajaxRequest: "true",
                navigationUpdate: "true",
                list: this.handle.id,
                action: "refresh",
                multiSelect: escape(this.options.multiSelect)
            },
            requestHeaders: {Accept: 'application/json;charset=UTF-8'},
            onComplete: this._getItemListAJAXListener.bindAsEventListener(this),
            onException: function (request, exception) {
                throw(exception);
            }
       });
    },


    /**
     * Listener for the ajax call to get the list of items for this menu along
     * with their selected state.
     * @param transport Prototype transport object which contains the JSON response text.
     */
    _getItemListAJAXListener: function(transport){
        var json = transport.responseText.evalJSON(true);
    	this.navBoxItems = $A(new Array());
        //make sure the item list is actually an array.
        var itemArray = $A(json.itemList);

        //the set of options for each iterface item.
    	var options = {
    	    multiSelectMode: this.options.multiSelect,
    	    mouseoverInfo: this.options.itemInfo,
    	    mouseoverInfoURL: this.options.itemInfoURL
    	}

        var itemHTMLString = "";
    	// DS931: need to store total number of items and total selected to account for display
    	// cutoffs.
    	this.itemsTotal = itemArray.size();
    	this.itemsSelectedTotal = itemArray.select(function(item) { return item.isSelected }).size();
        //for each item in the array, create a new ListObject, and initialize the observer.
    	// DS931: limit to first MAX_TO_DISPLAY items, to prevent script timeout errors
        itemArray.slice(0, MAX_TO_DISPLAY).each(function(item) {
            var newItem = new NavBoxItem(this.handle.id, item, options);
            itemHTMLString += newItem.getHTML();
            this.navBoxItems.push(newItem);
        }.bind(this));
        this.handle.down('ul').insert(itemHTMLString);


        //now that they are added, go ahead an init their observers.
        var counter = 0;
        this.handle.select('li').each(function(domItem) {
            var navBoxItem = this.navBoxItems[counter];
            navBoxItem.initObservers(domItem);
            navBoxItem.item.observe('click',this._itemClickHandler.bindAsEventListener(this, navBoxItem));
            counter++;
        }.bind(this));

        this.displayCounts();
        this.refreshViewState();
    },
    
    /**
     * Display the #selected / total # at the bottom of the list.
     */
    displayCounts: function() {
        var displayCountsStr = this.itemsSelectedTotal + "/" + this.itemsTotal + " selected.";
        
        if (this.itemsTotal > this.navBoxItems.length) {
        	displayCountsStr += "<br/>(Showing the first " + this.navBoxItems.length + ")";
        }
        $(this.handle.id + "_itemCount").update(displayCountsStr);
    },
    
    /**
     * Invokes an observer of this navigation list if the options have changed.  Only invokes
     * after a set amount of time (in milliseconds) given by this.options.timer.
     * @param transport required by some ajax calls, but not used.
     */
    invokeObserver: function(transport) {
        if (!this.options.observer) return;

        if (this.options.timer < 1) {   
            this.options.observer.invoke();
        } else {
            if (this.isTiming) {
                clearTimeout(this.isTiming);
                this.isTiming = false;
            }
            this.isTiming = setTimeout(
                  this.options.observer.invoke.bindAsEventListener(this.options.observer), this.options.timer);
        }
    },
    
    /**
     * Go through and set each of the list elements to selected/unselected.
     * @param isSelected boolean indicating whether each element should be selected or not.
     */
    updateAll: function(isSelected) {
        this.navBoxItems.each(function (item) { item.setSelected(isSelected); });
        // DS931: update total items selected
        this.itemsSelectedTotal = isSelected ? this.itemsTotal : 0;
        this.displayCounts();
    },

    /**
     * Click handler for a individual list item.  
     * @param event the event
     * @param item the item clicked on.
     */
    _itemClickHandler: function(event, item) {
        if (!this.options.multiSelect) {
            //if it's already selected and it's not a multi select do nothing.
            if (item.isSelected) { return; }
            
            //deselect all the others and select just the one.
            this.updateAll(false);
            item.setSelected(true);
        } else {
            item.setSelected(item.isSelected ? false : true);
        }
        // DS931: update total items selected
        this.itemsSelectedTotal += item.isSelected ? 1 : -1;
        this._syncWithServer(item.itemId);
        this.displayCounts();
    },

    /**
     * When an item is selected/unselected update the information on the server to reflect that change.
     * @param itemId the database identifier of the item who's state is being synced.
     */
    _syncWithServer: function(itemId) {
       this.syncInProgress++; 
       new Ajax.Request(this.url, {
           parameters: {
                 requestingMethod: "NavigationBox._syncWithServer",
                 datasetId: dataset,
                 ajaxRequest: "true",
                 navigationUpdate: "true",
                 list: this.handle.id,
                 action: "select",
                 itemId: itemId,
                 multiSelect: this.options.multiSelect
               },
           requestHeaders: {Accept: 'application/json;charset=UTF-8'},
           onComplete: this.invokeObserver.bindAsEventListener(this),
           onException: function (request, exception) {
               throw(exception);
           }
       });
    },

    /**
     * Event handler for the "deselect all" link.
     */
    _deselectAllClickHandler: function() {
       new Ajax.Request(this.url, {
           parameters: {
                 requestingMethod: "NavigationBox._deselectAllClickHandler",
                 datasetId: dataset,
                 ajaxRequest: "true",
                 navigationUpdate: "true",
                 list: this.handle.id,
                 action: "deselectAll"
               },
           requestHeaders: {Accept: 'application/json;charset=UTF-8'},
           onComplete: this._deselectAllAjaxListener.bindAsEventListener(this),
           onException: function (request, exception) {
               throw(exception);
           }
       });
    },

    /**
     * Listener to for the "deselect all" ajax call.
     * @param transport AJAX call transport, required but not used.
     */
    _deselectAllAjaxListener: function(transport) {
        this.updateAll(false);
        this.invokeObserver();
    },

    /**
     * Event handler for the "select all" link.
     */
    _selectAllClickHandler: function() {
       new Ajax.Request(this.url, {
           parameters: {
                 requestingMethod: "NavigationBox._selectAllClickHandler",
                 datasetId: dataset,
                 ajaxRequest: "true",
                 navigationUpdate: "true",
                 list: this.handle.id,
                 action: "selectAll"
               },
           requestHeaders: {Accept: 'application/json;charset=UTF-8'},
           onComplete: this._selectAllAjaxListener.bindAsEventListener(this),
           onException: function (request, exception) {
               throw(exception);
           }
       });
    },

    /**
     * Listener to for the "select all" ajax call.
     * @param transport AJAX call transport, required but not used.
     */
    _selectAllAjaxListener: function(transport) {
        this.updateAll(true);
        this.invokeObserver();
    }
}); //END OF CLASS NavigationBox.Component
