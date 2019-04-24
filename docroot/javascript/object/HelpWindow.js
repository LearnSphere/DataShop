//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2008
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 13128 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-04-19 12:46:09 -0400 (Tue, 19 Apr 2016) $
// $KeyWordsOff: $
//

/** 
 * Help Window JavaScript Object.
 * There are three main divs: helpWindowHeader, helpWindowContent, helpWindowFooter
 *
 * DEPENDENCIES: prototype v1.6, script.aculo.us dragdrop.js v1.8.0
 */
var HelpWindow = { }; // like a global binder

HelpWindow.Base = Class.create({

    _COOKIE_PREFIX: "ds_help_window_",
    _LOCATION_OFFSET: 10,
    
    /**
     * Constructor.  Creates the window and restores it to its previous
     * size and location and fills in the content.
     */
    initialize: function(handle, options) {
        this.helpButton = $(handle); //this is the help button 
        this.state = this._getState();
        this._create(this.state);
        this.restore();
    },
    
    /**
     * Updates the content of the help window.
     */
    updateContentRegardless: function(content) {
        var myContent = content.innerHTML;
        if (dataset != null) {
            if (!myContent.include("datasetId=" + dataset)) {
                myContent = myContent.gsub("page=", "datasetId=" + dataset + "&page=");
            }
        }

        $("helpWindowContent").update( myContent );
        this._getHelpWindowFooterTopicLink(content);
        this._getTopic(content);

    },
    updateContent: function(content) {
        if (this.contentSaved && this.contentSaved == true) {
            $("hiddenHelpContent").update( content.innerHTML );
             //Should we do anything here for the helpContentFooter link? 
        } else {
            var myContent = content.innerHTML;
            if (dataset != null) {
                if (!myContent.include("datasetId=" + dataset)) {
                    myContent = myContent.gsub("page=", "datasetId=" + dataset + "&page=");
                }
            }

            $("helpWindowContent").update( myContent );
            this._getHelpWindowFooterTopicLink(content);
            this._getTopic(content);
        }
    },
    _getHelpWindowFooterTopicLink: function(content) {
        var helpLinkSpan = content.select('.helpTopicLink');
        var helpLinkSpanContent = "";
        if (helpLinkSpan[0]) {
            helpLinkSpanContent = helpLinkSpan[0].innerHTML;
            if (dataset != null) {
                if (!helpLinkSpanContent.include("datasetId=" + dataset)) {
                    helpLinkSpanContent = helpLinkSpanContent.gsub("page=", "datasetId=" + dataset + "&page=");
                }
            } else if (projectId != null) {
                if (!helpLinkSpanContent.include("id=" + projectId)) {
                    helpLinkSpanContent = helpLinkSpanContent.gsub("page=", "id=" + projectId + "&page=");
                }
            } else if (discourseId != null) {
                if (!helpLinkSpanContent.include("discourseId=" + discourseId)) {
                    helpLinkSpanContent = helpLinkSpanContent.gsub("page=", "discourseId=" + discourseId + "&page=");
                }
            }
        }
        $("helpWindowTopicLink").innerHTML = helpLinkSpanContent;

    },
    _getTopic: function(content) {
    
        var downOneH2 = content.down('h2');
        if (downOneH2) {
            this.topic = downOneH2.innerHTML;
      
        } else {
            this.topic = ""; //Temporary state, doesn't seem harmful
            
        }
    },

    /**
     * Save the current content into the hidden help content div.
     * This is used, for example, by the Sample Selector java script
     * code to save the report's help and show the sample selector
     * help content instead.  Use the restore method to put it back
     * when the Sample Selector is closed.
     */
    save: function() {
        this.contentSaved = true;
        var content = $("helpWindowContent");
        $("hiddenHelpContent").update( content.innerHTML );
    },
    
    /**
     * Restore the previous help content.  Used in conjunction
     * with the save method by the Sample Selector code.
     */
    restore: function() {
        this.contentSaved = false;
        var hhc = $("hiddenHelpContent");
        if (hhc) {
            var downOneDiv = hhc.down('div');
            if (downOneDiv) {
                this.updateContent(downOneDiv);
            } else {
                this.updateContent(hhc);
            }
        }
    },
    
    /**
     * Public method to open the help window.
     */
    open: function() {
        this._openWindow();
    },
    
    /**
     * Public method to close the help window.
     */
    closeHelpWindow: function() {
        this._closeWindow();
    },
    
    /**
     * Used by the main help button to toggle the help window from open to closed.
     */
    toggle: function() {
        if (this.state == "hide") {
            this._openWindow();
        } else {
            this._closeWindow();
        }
    },
    
    /**
     * Used by help documentation pages to restore the help button.
     */
    showHelpButton: function() {
        this.helpButton.show();
    },
    
    /**
     * Used by help documentation pages to hide the help button.
     */
    hideHelpButton: function() {
        this.helpButton.hide();
    },
    
    /**
     * Private method to create the help window.  Restores to previous
     * state (open vs. closed), size and location.
     */
    _create: function(state) {
        var hideStyle = "";
        if (state == "hide") {
            hideStyle = ' style="display:none"';
            this._setHelpButtonToClosed();
        } else {
            this._setHelpButtonToOpen();
        }

        // create top div
        //$(document.body).insert(new Element('div', {className: 'window' + hideStyle}));
        $(document.body).insert('<div id="helpWindow" class="window"' + hideStyle + '><div>');
	    //$('layout').insert('<div>&nbsp;</div>');
	
	    this.helpWindow = $("helpWindow");
        this.helpWindow.insert('<div id="helpWindowHeader"></div>');
    	this.helpWindowHeader = $("helpWindowHeader");

        //close button in the upper right corner
        this.helpWindowHeader.insert('<a id="helpWindowCloseButton"></a>');
        $("helpWindowCloseButton").insert(
            '<img src="images/close.gif" alt="Close Help Window" title="Close Help Window" />');
        Event.observe(this.helpWindowHeader, 'mouseover', function(){this.addClassName('hover')});
        Event.observe(this.helpWindowHeader, 'mouseout',  function(){this.removeClassName('hover')});
        
        Event.observe("helpWindowCloseButton", 'click', this._closeWindow.bindAsEventListener(this));

        this.helpWindowHeader.insert('<h1>Help</h1>');
        this.helpWindow.insert('<div id="helpWindowContent"></div>');
        this.helpWindow.insert('<div id="helpWindowFooter"></div>');
        
        $("helpWindowContent").insert( 
            '<p>Sorry, help isn\'t available for this page.</p><br><br>');

        idString = "";
        if (dataset != null) {
            idString = "?datasetId=" + dataset;
        } else if (projectId != null) {
            idString = "?id=" + projectId;
        } else if (discourseId != null) {
            idString = "?discourseId=" + discourseId;
        }
        
        $("helpWindowFooter").insert('<div id="helpWindowTopicLink"></div>');
        $("helpWindowFooter").insert('<p id="helpWindowDocHome" class="helpWindowFooter">'
            + '<img src="images/book.png" /> <a href="help' + idString + '">'
            + 'Documentation Home</a></p>');
        /** Not this release.
        $("helpWindowFooter").insert('<img id="helpWindowResize" src="images/resize.png" alt="Resize" title="Resize Help Window" />');
        **/
        
        Event.observe(this.helpButton, 'click', this.toggle.bindAsEventListener(this));
        Event.observe(this.helpButton, 'mouseover', this._mouseoverHandler.bindAsEventListener(this));
        Event.observe(this.helpButton, 'mouseout', this._mouseoutHandler.bindAsEventListener(this));
        
        this._restoreLocation();
        
        new Draggable(this.helpWindow, {
            handle: this.helpWindowHeader,
            onStart: function(draggable, event) {
            }.bind(this),
            onDrag: function (draggable, event) {
            }.bind(this),
            onEnd: function(draggable, event) {
                this._saveLocation();
            }.bind(this)
        });
        
        /** Resizing window. Not doing yet.
        this._restoreSize();
        new Draggable($("helpWindowResize"), {
            snap: function(x, y, draggable) {
                function constrain(n, lower, upper) {
                    if (n > upper) return upper;
                    return (n < lower ? lower : n);
                };
                elementDims = draggable.element.getDimensions();
                parentDims = Element.getDimensions($("helpWindow"));
                var posInfo = $("helpWindow").viewportOffset();
                this.winX = posInfo[0];
                this.winY = posInfo[1];
                var lowerX = 0 - this.winX;
                var lowerY = 0 - this.winY;
                var upperX = parentDims.width - elementDims.width;
                var upperY = parentDims.height - elementDims.height;
                return [
                   constrain(x, lowerX, upperX),
                   constrain(y, lowerY, upperY)];
            }.bind(this),
            onStart: function(draggable, event) {
                this.mouseStartX = event.clientX;
                this.mouseStartY = event.clientY;
                var posInfo = $("helpWindow").viewportOffset();
                this.winX = posInfo[0];
                this.winY = posInfo[1];
            }.bind(this),
            onDrag: function (draggable, event) {
            }.bind(this),
            onEnd: function(draggable, event) {
                this._saveSize();
            }.bind(this)
        });
        **/
    },

    /**
     * Private method to set the help button to the closed state.
     */
    _setHelpButtonToClosed: function() {
        this.helpButton.removeClassName('open');
        this.helpButton.addClassName('closed');
    },

    /**
     * Private method to set the help button to the open state.
     */
    _setHelpButtonToOpen: function() {
        this.helpButton.removeClassName('closed');
        this.helpButton.addClassName('open');
    },

    /**
     * Private method to open the help window.
     */
    _openWindow: function() {
        this.helpWindow.show();
        this._restoreLocation();
        this.state = "show";
        this._setHelpButtonToOpen();
        this._saveState(this.state);
        this._getTopic( $("helpWindowContent") );
        this._logUserAction("Open", this.topic);
    },

    /**
     * Private method to close the help window.
     */
    _closeWindow: function() {
        if (this.state != "hide") {
            this.helpWindow.hide();
            this.state = "hide";
            this._setHelpButtonToClosed();
            this._saveState(this.state);
            this._logUserAction("Close", this.topic);
        }
    },
    
    /**
     * Private method to show the tool tip.
     */
    _mouseoverHandler: function(event) {
        this.helpButton.addClassName("hover");
        options = new Array();
        options['delay'] = '800';
        options['timeout'] = '5000';

        if (this.toolTip == null) {
            this.toolTip = new ToolTip(this.helpButton,
                '<p>Click to learn more about this page.</p>', options);
            //this.toolTip.createView();
        }
        
        //this.toolTip.showToolTip();
    },
    
    /**
     * Private method to hide the tool tip.
     */
    _mouseoutHandler: function(event) {
        this.helpButton.removeClassName("hover");
        //this.toolTip.hideToolTip();
    },
    
    /**
     * Private method to log the user's request for report-level help by making an ajax call.
     */
     _logUserAction: function(action, help_topic) {
        
        new Ajax.Request("help", {
            parameters: {
            	requestingMethod: "HelpWindow._logUserAction",
                datasetId: dataset,
                logUserAction: action,
                topic: help_topic
            },
            onComplete: function (transport) {}, // do nothing on return
            onException: function (request, exception) {
                throw(exception);
            }
        });
    },

    /**
     * This section is for storing and retrieving the values stored
     * in the cookies, which are state, size and location.
     */
    _saveState: function (value) {
        createCookie(this._COOKIE_PREFIX + this.helpButton.id + "_state", value, 100);
    },

    _saveSize: function () {
        var height = this.helpWindow.style.height;
        var width  = this.helpWindow.style.width;
        createCookie(this._COOKIE_PREFIX + this.helpButton.id + "_height", height, 100);
        createCookie(this._COOKIE_PREFIX + this.helpButton.id + "_width",  width, 100);
    },

    _saveLocation: function () {
        var x = this.helpWindow.getStyle('left');
        var y = this.helpWindow.getStyle('top');
        createCookie(this._COOKIE_PREFIX + this.helpButton.id + "_x", x, 100);
        createCookie(this._COOKIE_PREFIX + this.helpButton.id + "_y", y, 100);
    },

    _getState: function () {
        return readCookie(this._COOKIE_PREFIX + this.helpButton.id + "_state");
    },

    _restoreSize: function () {
        var height = readCookie(this._COOKIE_PREFIX + this.helpButton.id + "_height");
        var width  = readCookie(this._COOKIE_PREFIX + this.helpButton.id + "_width");
    },

    /**
     * If there are cookies for the previous x and y location of the help window,
     * then place the window at that location.  Use the _setLocation method to place
     * the window otherwise.
     */
    _restoreLocation: function () {
        var cookieX = readCookie(this._COOKIE_PREFIX + this.helpButton.id + "_x");
        var cookieY = readCookie(this._COOKIE_PREFIX + this.helpButton.id + "_y");

        this._setLocation(this.helpWindow, this.helpWindowHeader, cookieX, cookieY);
    },
    
    /**
     * Put the window at its last location if it is in the viewport,
     * otherwise center the window in the viewport, unless the viewport is too small,
     * then reposition the window at 10,10.
     * Be sure to keep the header of the window, which is the drag handle, in the viewport.
     * (Note that this method is dupliated in ManageKCSets.js until we can find one place for it.)
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
});  //end class HelpWindow.Base

//moved this to Datashop.js, not sure why
//onloadObserver.addListener(initHelpWindow);
