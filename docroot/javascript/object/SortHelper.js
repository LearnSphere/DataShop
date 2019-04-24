//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2009
// All Rights Reserved
//
// Helper class for sorting the LCPI Details table.  Since the LCPID is updated via AJAX calls 
// (mostly), this object must be reset after a new call. Otherwise it will contain incorrect
// information.  
//
// Author: Kyle Cunningham
// Version: $Revision: 5870 $
// Last modified by: $Author: kcunning $
// Last modified on: $Date: 2009-11-06 13:54:48 -0500 (Fri, 06 Nov 2009) $
// $KeyWordsOff: $
//

var SortHelper = Class.create();
SortHelper.prototype  = {
    
    /**
     * Constructor.  
     * @param measure the selected measure from the user (student, skill, etc) [required].
     * @param sortBy how the lcpi details are sorted (by measure of curveTypeValue) [required].
     * @param direction the direction of the sort (ASC or DESC) [required].
     */
    initialize: function(measure, sortBy, direction) {
        this.measure = measure;
        this.sortBy = sortBy;
        this.direction = direction;
        this.nameColumnClass = "nameColumn";
        this.valueColumnClass = "valueColumn";
        this.freqColumnClass = "freqColumn";
        //this._debugMe();
        this._updateHeaders();
        this._initEventHandlers();
    }, // end initialize() 

    /**
     * Helper function to print out the details of this class as a string.
     */
    _debugMe: function() {
    	var msg = "SortHelper\nmeasure::" + this.measure;
    	msg += "\nsortBy::" + this.sortBy + "\ndirection::" + this.direction;
    	console.log(msg); // in _debugMe function
    },
    
    /**
     * Get the selected measure.
     */
    getMeasure: function() { return this.measure; },
    
    /**
     * Since this object is created once and reused across several ajax calls, it is
     * necessary to reinitialize certain values.  This method takes care of this.
     * @param measure the selected measure from the user.
     * @param sortBy how the lcpi details are sorted.
     * @param direction the direction of the sort.
     */
    reset: function(measure, sortBy, direction) {
        this.measure = measure;
        this.sortBy = sortBy;
        this.direction = direction;
        //this._debugMe();
        this._updateHeaders();
        this._initEventHandlers();
    },
    
    /**
     * Initialize the column headings for the LCPID table.  Show the appropriate
     * sort arrow based on the sortBy and direction values. 
     */
    _updateHeaders: function() {
        if ($('sortImage')) {
            $('sortImage').remove();
        }
        var sortImage = document.createElement('img');
        sortImage.id = "sortImage";
        this.nameColumn = $('pointInfoDetailsCol1Header');
        this.valueColumn = $('pointInfoDetailsCol2Header');
        this.freqColumn = $('pointInfoDetailsCol3Header');
        if (this.direction == "ASC") {
            	sortImage.src = "images/grid/up.gif";
            } else {
            	sortImage.src = "images/grid/down.gif";
            }
        if (this.sortBy == "measureName") {
            this.nameColumn.insert({'bottom': sortImage});
            // adding after insert so IE does not complain.
            $('sortImage').addClassName('nameColumn');
        } else if (this.sortBy == "curveTypeValue" ) {
            this.valueColumn.insert({'bottom': sortImage});
            $('sortImage').addClassName('valueColumn');
        } else {
            this.freqColumn.insert({'bottom': sortImage});
            $('sortImage').addClassName('freqColumn');
        }
    }, // end updateHeaders()

    /**
     * Initialize the event handlers for this object.  Currently, the name and value
     * column headings have the same 'click' handler assigned to them.
     */
    _initEventHandlers: function() {
        this.nameColumn.stopObserving('click', this.handler);
        this.valueColumn.stopObserving('click', this.handler);
        this.freqColumn.stopObserving('click', this.handler);
        this.handler = this._handleSort.bindAsEventListener(this);
        this.nameColumn.observe('click', this.handler);
        this.valueColumn.observe('click', this.handler);
        this.freqColumn.observe('click', this.handler);
    }, // end initEventHandlers()
    
    /**
     * Handle a sort request.  First, determine which column was clicked.  Second,
     * determine what the sortBy and direction should be.  Third, make a 
     * pointInfoDetailsRequest to update the content. 
     * @param e the event.	
     */
    _handleSort: function(e) {
        var element = e.element();
        var sortBy;
        var direction;
        // determine which column was clicked.
        if (element.hasClassName('nameColumn')) {
            sortBy = "measureName";
        } else if (element.hasClassName('valueColumn')) {
            sortBy = "curveTypeValue";
        } else {
            sortBy = "frequency";
        }
        if (this.sortBy == sortBy) {
            // we are resorting based on the same measure, so flip the direction
            if (this.direction == "ASC") {
                direction = "DESC";
            } else {
            	direction = "ASC";
            }
        } else {
            // user changed the sortBy column so maintain direction
            direction = this.direction;
        }
        pointInfoDetailsRequest(this.measure, sortBy, direction);
    } // end handleSort()
    
}; // end SortHelper


