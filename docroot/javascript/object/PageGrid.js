//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 12811 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-12-03 10:02:26 -0500 (Thu, 03 Dec 2015) $
// $KeyWordsOff: $
//

/**
 * This class is designed to create a pagable table that can move forward and backwards
 * through a set of data.
 * @param containerIdString - ID of the containing element.
 * @param url - URL for the server calls for data.
 * @param options
 *   limit - initial limit
 *   offset - initial offset
 *   exportCall - function to call for an export
 *   maxNum - the maximum number of items (will be automatically set during init data call)
 */


function addColumnHeaderToolTips() {

    jQuery('span[class=cf_header]').each(function(e) {
        var params = { extraClasses: "infoDiv" };
        var id = '#cf_tooltip_' + jQuery(this).attr('id');
        var toolTipText = jQuery(id).html();
        new ToolTip(this, toolTipText, params);
    });
}

var PageGrid = Class.create();
PageGrid.prototype = {

  /**
   * Constructor
   * @param containerIdString (Required) the HTML element id for the element that will contain the page-grid.
   * @param title (Required) the title of the grid.
   * @param url (Required) the URL that the ajax calls will access.
   * @param options (Optional)
   *   - limit      : 10        the number of rows to view.
   *   - offset     : 0         the starting position.
   *   - exportCall : false     function to call to export the graph.
   *   - exportAllowed : true flag indicating if the Export is allowed
   *   - maxNum     : 'unknown' the maximum number of elements in the grid.
   */
  initialize: function(containerIdString, title, url, options) {
    this.containerIdString = $(containerIdString).id;
    this.containerElement = $(containerIdString);
    this.url = url;
    this.title = title;

    this.cache = new GridCache();
    this.options = {
      limit       : 10,
      offset      : 0,
      exportCall  : false,
      exportAllowed  : "true",
      maxNum      : 'unknown',
      truncate    : 80,
      extraParams : {}
    };
    Object.extend(this.options, options || { });

    //Because .bindAsEventListener returns a unique instance when
    //invoked, to remove an event handler properly we need to be able to
    //access the "actual" instance of the variable.  This cache holds those
    //actual instances.
    this.handlerCache = new Array();

    this.containerElement.addClassName("data_grid_container");
    this.createView();
    this._setLocationWithinViewport(this.containerElement, $(this.containerIdString + "_loading"));
    this.initControls();
    this.getData(0, true);

  },

  /**
   * Adds the HTML elements to the DOM that are required for this grid.
   */
  createView: function() {
        var horizControlsStr = "horizontal_controls";
        if (isThisIE()) {
            horizControlsStr = "ie_horizontal_controls";
        }
    new Insertion.Top(this.containerElement,
       "<div class=\"" + horizControlsStr + "\" id=\"" + this.containerIdString + "_horizontal_controls\">"
       + "<span class=\"range_wrapper\"><span id=\"" + this.containerIdString + "_current_range\"></span>&nbsp;of "
       + "<span id=\"" + this.containerIdString + "_max\">" + this.options.maxNum.toString() + "</span></span>"
       + "<span id=\"" + this.containerIdString + "_horizontal_controls_start\" class=\"control start\">First</span> | "
       + "<span id=\"" + this.containerIdString + "_horizontal_controls_back\" class=\"control back\">Back</span> | "
       + "<span id=\"" + this.containerIdString + "_horizontal_controls_next\" class=\"control next\">Next</span> | "
       + "<span id=\"" + this.containerIdString + "_horizontal_controls_end\" class=\"control end\">Last</span>"
       + "</div>");

    $(this.containerIdString + "_horizontal_controls").makePositioned();
    this.updateControlsPosition();

    new Insertion.Top(this.containerElement,
       "<div class=\"loading\" id=\"" + this.containerIdString + "_loading\">Loading Data...</div>");

    new Insertion.Top(this.containerElement,
       "<div class=\"vertical_controls\" id=\"" + this.containerIdString + "_vertical_controls\">&nbsp;"
         + "<span id=\"" + this.containerIdString + "_vertical_controls_start\""
           + " class=\"control start\" title=\"First\"></span>"
         + "<span id=\"" + this.containerIdString + "_vertical_controls_back\""
           + " class=\"control back\" title=\"Back\"></span>"
         + "<span id=\"" + this.containerIdString + "_vertical_controls_next\""
           + " class=\"control next\" title=\"Next\"></span>"
         + "<span id=\"" + this.containerIdString + "_vertical_controls_end\""
           + " class=\"control end\" title=\"Last\"></span>"
       + "</div>");

    var extraControlsString = "<div class=\"extra_controls\" id=\"" + this.containerIdString + "_extra_controls\">";

    var disabledStr = (this.options.exportAllowed == "false") ? "disabled" : "";

    extraControlsString += (this.options.exportCall !== false)
        ? "<input type=\"button\" value=\"Export " + this.title + "\" id=\""
          + this.containerIdString + "_export\" class=\"native-button\" "
          + disabledStr + " />"
        : "";

    extraControlsString += "<label>Rows per page</label>"
      + "<select id=\"" + this.containerIdString + "_limit_select\">"
        + "<option value=\"10\">10</option>"
        + "<option value=\"25\">25</option>"
        + "<option value=\"50\">50</option>"
        + "<option value=\"100\">100</option>"
      + "</select></div>";

    new Insertion.Top(this.containerElement, extraControlsString);

    if (isThisIE()) {
        new Insertion.Top(this.containerElement,
           "<div class=\"info shortinfo\" id=\"ie_export_info\"><div class=\"imagewrapper\">"
           + "<img src=\"images/info.gif\"></div>"
           + "<p>If you're having trouble downloading the export, see <a "
           + "href=\"help?page=export#ie7-export-download\">&ldquo;Export Tips for Internet Explorer&rdquo;</a>.</p></div>");
    }

    new Insertion.Top(this.containerElement,
       "<div class=\"total_rows_wrapper\" id=\""
       + this.containerIdString + "_total_rows_text\">Number of rows: "
       + "<span id=\"" + this.containerIdString + "_total_rows_value\"></span> "
       + "</div>");
  },

  /**
   * Add observers to any extra controls that are not part of the main controls.
   */
  initControls: function() {
    //don't worry about forward/back controls as they get set when "getData" is called
    Event.observe(this.containerIdString + "_limit_select", "change",
                                    this.setLimit.bindAsEventListener(this));
    if (this.options.exportCall !== false) {
        Event.observe(this.containerIdString + "_export", "click",  this.options.exportCall);
    }

    Event.observe(window, 'scroll', this.updateControlsPosition.bindAsEventListener(this));
    Event.observe(window, 'resize', this.updateControlsPosition.bindAsEventListener(this));
  },

  /**
   * Moves the horizontal controls to follow the edge of the screen.  This
   * keeps the controls available to use.
   */
  updateControlsPosition: function() {
  //first get the horizontal controls
    var controls = $(this.containerIdString + "_horizontal_controls");
    if (!controls) { return; };
    docRight = document.viewport.getScrollOffsets()[0] + document.viewport.getWidth();
    //get how far offset to the right the controls currently are
    //and remove the "px" from the end.
    calcRightValue = function() {
      return (controls.cumulativeOffset()[0] + controls.getWidth()) - docRight;
    }
    getCurrentRight = function() {
      curr = controls.getStyle('right');
      return +curr.substring(0,(curr.length - 2));
    }
    setRight = function(newRight) {
       controls.setStyle({right:newRight + "px"});
    }
    var value = calcRightValue();
    //max value is the maximum allowed "right" value, which is the length of the container
    //minus the width of the controls and some additional px. so as not to overlap the other controls.
    //TODO get the actual width of the extra controls and the current controls.
    var maxValue = ($(this.containerIdString).getWidth() - (400 + controls.getWidth()));
    currentRight = getCurrentRight();
    if (currentRight==0) {
      setRight(maxValue);
     value = calcRightValue();
     currentRight = getCurrentRight();
    }

    value += currentRight + 10;
    value = (value < 0) ? 0 : value;
    value = (value > maxValue) ? maxValue : value;
    setRight(value);
  },

  /**
   * Initialize a single control of a given type by adding the appropriate observer.
   * @param className - the CSS class name of the control to init.
   * @param changeValue - the "change" which is either "start"/"end" or a number
   * for the amount of change in the offset. This value will be passed into getData
   */
  initSingleControlType: function(className, changeValue) {

      //get the controls by the CSS class name.  This will get all controls of this type.
      var ctlArray = this.containerElement.select('[class*="' + className + '"]');
      for (i = 0; i < ctlArray.length; i++ ) {
          var theControl = ctlArray[i];
          var parentId = theControl.parentNode.id;

          //get the click event handler if it already exists.
          //see the note in the init on why these are cached.
          var clickHandler = null;
          if (!this.handlerCache[className + "_click_" + parentId]) {
              clickHandler = function(event) {
                  // use the limit as changeValue if it has been changed in other sub tab
                if ((className != 'start') && (className != 'end')
                    && (this.options.limit != changeValue)){
                  if (changeValue < 0){
                    changeValue = -this.options.limit;
                  } else {
                    changeValue = this.options.limit;
                  }
                }

                  this.getData(changeValue);
                  var element = Event.element(event)
                  if (!($w(element.className.gsub('_', ' '))).include("disabled")) {
                      this._updateControlClass(element, "hover");
                  }
                  Event.stop(event);
              }.bindAsEventListener(this);
              this.handlerCache[className + "_click_" + parentId] = clickHandler;
          } else {
              clickHandler = this.handlerCache[className + "_click_" + parentId];
          }

        var mouseoverHandler = null;
        if (!this.handlerCache[className + "_mouseover_" + parentId]) {
            mouseoverHandler = this.controlMouseOver.bindAsEventListener(this);
            this.handlerCache[className + "_mouseover_" + parentId] = mouseoverHandler;
        } else {
            mouseoverHandler = this.handlerCache[className + "_mouseover_" + parentId];
        }

        var mouseoutHandler = null;
        if (!this.handlerCache[className + "_mouseout_" + parentId]) {
            mouseoutHandler = this.controlMouseOut.bindAsEventListener(this);
            this.handlerCache[className + "_mouseout_" + parentId] = mouseoutHandler;
        } else {
            mouseoutHandler = this.handlerCache[className + "_mouseout_" + parentId];
        }


        //helps safari not be stupid
        theControl.onmouseover = function () { return false; }
        theControl.onmouseout = function () { return false; }
        theControl.onclick = function () { return false; }

        //just to be safe disable any observer that already exists.
        Event.stopObserving(theControl, "mousedown", clickHandler);
        Event.stopObserving(theControl, "mouseover", mouseoverHandler);
        Event.stopObserving(theControl, "mouseout", mouseoutHandler);

        Event.observe(theControl, "mousedown", clickHandler);
        Event.observe(theControl, "mouseover", mouseoverHandler);
        Event.observe(theControl, "mouseout", mouseoutHandler);

        this._updateControlClass(theControl);
    }
  },

   /**
   * Disables a control of a given type.
   * @param className - the class name of the control to init.
   * @param clearHandlerCache - boolean of whether to clear the handler cache or not.
   */
  disableSingleControlType: function(className, clearHandlerCache) {
    var ctlArray = this.containerElement.select('[class*="' + className + '"]');

    for (i = 0; i < ctlArray.length; i++ ) {
      var theControl = ctlArray[i];
      var parentId = theControl.parentNode.id;

      //get the event handler instances from the cache.
      //see the note in the init on why these are cached.
      var clickHandler = this.handlerCache[className + "_click_" + parentId];
      var mouseoverHandler = this.handlerCache[className + "_mouseover_" + parentId];
      var mouseoutHandler = this.handlerCache[className + "_mouseout_" + parentId];

      //remove the event handlers
      Event.stopObserving(theControl, "mousedown", clickHandler);
      Event.stopObserving(theControl, "mouseover", mouseoverHandler);
      Event.stopObserving(theControl, "mouseout", mouseoutHandler);

      if (clearHandlerCache) {
        this.handlerCache[className + "_click_" + parentId] = null;
        this.handlerCache[className + "_mouseover_" + parentId] = null;
        this.handlerCache[className + "_mouseout_" + parentId] = null;
      }

      this._updateControlClass(theControl, "disabled");
    }
  },

  /**
   * Mouse out handler which changes classes.
   */
  controlMouseOver: function(event) {
      this._updateControlClass(Event.element(event), "hover");
      Event.stop(event);
  },

  /**
   * Mouse out handler which changes classes.
   */
  controlMouseOut: function(event) {
      this._updateControlClass(Event.element(event));
      Event.stop(event);
  },

  /**
   * Remove the subclass which is done as an _ for IE6 stupid .class.class CSS
   * problems.
   */
  _updateControlClass: function(ctrl, classExtension) {
    var classes = $w(ctrl.className.gsub('_', ' '));
    ctrl.className = classes[0] + " " + classes[1]
        + ((classExtension) ? "_" + classExtension : "");

  },

  /**
   * Handler to reset the current limit on # of rows to view.
   */
  setLimit: function(event) {

    //disable the current controls with the current limit.
    this.disableSingleControlType("back", true);
    this.disableSingleControlType("next", true);

    this.handlerCache["next_click"] = null;
    this.handlerCache["back_click"] = null;


    //set the new limit
    this.options.limit = +$F(this.containerIdString + "_limit_select");
    //re-get the data with no change.
    //this.getData(0);
    this.getData(0, false, true);
    Event.stop(event);
  },

  /**
   * Updates the controls to reflect the changes from the offset change.  Disabling and enabling
   * the controls as necessary.
   * @param offsetChange the change to the offset.
   */
  updateControls: function(offsetChange) {
    var max = (this.options.maxNum != 'unknown') ? this.options.maxNum : false;

    this.disableSingleControlType("back");
    this.disableSingleControlType("next");
    this.disableSingleControlType("start");
    this.disableSingleControlType("end");
    //make sure the limit/offset are within bounds.
    if (offsetChange == "start") {
        this.options.offset = 0;
        this.initSingleControlType("next", this.options.limit);
        if (this.options.max != 'unknown') { this.initSingleControlType("end", "end"); }
    } else if (offsetChange == "end") {
        this.options.offset = max - this.options.limit;
        this.initSingleControlType("start", "start");
        this.initSingleControlType("back", -this.options.limit);
    } else {
     //   alert("else offsetChange " + offsetChange + " this.options.offset: " + this.options.offset);
        this.options.offset = this.options.offset + offsetChange;

        this.initSingleControlType("next", this.options.limit);
        this.initSingleControlType("back", -this.options.limit);
        this.initSingleControlType("start", "start");
        if (this.options.maxNum != 'unknown') {
            this.initSingleControlType("end", "end");
        }
    }
    //check that the new offset didn't push it outside the allowed range.
    if (max) {
        if (this.options.offset >= (max - this.options.limit)) {
           this.options.offset = max - this.options.limit;
           this.disableSingleControlType("next");
           this.disableSingleControlType("end");
           this.initSingleControlType("start");
           this.initSingleControlType("back");
        }
    } else {
       this.disableSingleControlType("end");
    }

    if (this.options.offset <= 0) {
       this.options.offset = 0;
       this.disableSingleControlType("start");
       this.disableSingleControlType("back");
       this.initSingleControlType("next", this.options.limit);
       if (max) { this.initSingleControlType("end", "end"); }
    }
  },

  /**
   * Get data for the grid either from the cache or from the server
   * @param offsetChange the amount to change the offset.
   * @param getMax if set to true will also retrieve the expect max number.
   */
  getData: function(offsetChange, getMax, limitHasChanged) {
    //turn on the "loading..." div.
    if (loadedForQA) { loadedForQA(false); }
    this._setLocationWithinViewport(this.containerElement, $(this.containerIdString + "_loading"));
    $(this.containerIdString + "_loading").show();

    this.updateControls(offsetChange);

    //check that the data does not already exist in the cache.
    var cacheData = this.cache.getSubset(this.options.limit, this.options.offset);
    if (cacheData !== false) {
      // send an update to the server so we know what the user-set limit was
      // also, we don't care if updateContent happens before return from AJAX because
      // we are only doing the call to store the updated limit on the server.
      var myAjax = new Ajax.Request(this.url, {
            parameters: {
                  requestingMethod: "PageGrid.getData",
                  datasetId: dataset,
                  ajaxRequest: "true",
                  getDataGrid: this.containerElement.id,
                  updateLimitOnly: true,
                  limit: this.options.limit
            },
            requestHeaders: {Accept: 'application/json;charset=UTF-8'},
            onComplete: this.updateContent(cacheData),
            onException: function (request, exception) {
                throw(exception);
            }
        });
      //this.updateContent(cacheData);
    } else {
       if (getMax !== true) { getMax = false; }
       if (limitHasChanged !== true) {limitHasChanged = false; }
       //Make the AJAX call to go get the data.
       var myAjax = new Ajax.Request(this.url, {
           parameters: {
                 requestingMethod: "PageGrid.getData",
                 datasetId: dataset,
                 ajaxRequest: "true",
                 getDataGrid: this.containerElement.id,
                 limit: this.options.limit,
                 offset: this.options.offset,
                 limitHasChanged: limitHasChanged,
                 getMax: getMax
               },
           requestHeaders: {Accept: 'application/json;charset=UTF-8'},
           onComplete: this.getDataAjaxListener.bindAsEventListener(this),
           onException: function (request, exception) {
               throw(exception);
           }
       });
    }
  },

  /**
   * Ajax Listener for the getData call.  Parses the transport as a JSON object
   * setting the cache headers, and the max if it was part of the call.
   * @param transport - Prototype AJAX transport object.
   */
  getDataAjaxListener: function(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.max) {
        this.options.maxNum = +json.max;
        $(this.containerIdString + "_total_rows_value").innerHTML = json.maxDisplay;
        $(this.containerIdString + "_max").innerHTML = json.maxDisplay;
        this.updateControls(0); //reset controls with no offset change.
    }
    this.cache.setHeaders(json.headers); //set the headers array in the cache.
    this.updateContent(json);
    addColumnHeaderToolTips();
  },

  /**
   * This function works by either being theAJAX getData listener, or if the
   * transport is null, it will use the cache information passed in.
   * When the response with data comes back from the AJAX getData call
   * or the information already exists in the cache this function parses the results.
   * @param transport the AJAX transport object.
   */
  updateContent: function(data) {

    // check that the data grid body still exists and that the user
    // did not navigate away from the expected page.
    //if (!$("data_grid_body")) { return; }

    //remove all children from the body element.
    if ($(this.containerIdString).down("table")) {
        $(this.containerIdString).down("table").remove();
    }

    //hide the loading div
    $(this.containerIdString + "_loading").hide();

    var bodyString = "";
    if (!data || !data.rows || data.rows.length == 0) {
        msg = data.validationMessage ? data.validationMessage : "No results, please verify your selections";
        $(this.containerElement).insert('<div class="info shortinfo">'
            + '<div class="imagewrapper"><img src="images/info_32.gif"/></div>'
            + '<p>' + msg + '</p>'
            + '</div>');
        this._hideControls();
    } else {
        // Create a temporary var to hold the data so that we can
        // take advantage of jQuery's .text() and .html() to handle ML.
        var tmpEle = jQuery('<p id="tmpEle"></p>');
        for (var i = 0, numRows = data.rows.length; i < numRows; i++) {
            var rowClass = (i%2) ? "even": "odd";
            var row = $A(data.rows[i]);

            this.cache.addRow(row, (this.options.offset + i) );
            var rowString = "<tr class=\"" + rowClass + "\">";
            for (j = 0, rowLen = row.length; j < rowLen; j++) {
                tmpEle.text(row[j].toString().truncate(this.options.truncate));
                classString = "col_" + j;
                rowString += "<td class=\"" + classString + "\" >"
                    + tmpEle.html() + "</td>";
            }
            rowString += "</tr>"
            bodyString += rowString;
        }
        tmpEle.remove();

        $(this.containerElement).insert(
           "<table class=\"data_grid_table\" id=\"" + this.containerIdString + "_data_grid_table\">"
           + "<thead><tr>" + this._getHeaderString(data.headers) + "</tr></thead>"
           + "<tbody id=\"" + this.containerIdString + "_data_grid_body\">" + bodyString + "</tbody></table>");

        // make sure the rows dropdown value is in sync with the number of rows in the table
      var selectElement = $(this.containerIdString + "_limit_select");
      for (var i=0;i< selectElement.options.length;i++){
            if (selectElement.options[i].value == data.limit){
              selectElement.selectedIndex = i;
            }
      }

        //update the current range.
        var endValue = 0;
        // this check is necessary because we can get to updateContent() through a call for
        // data for by using cached data - cached data does not have a data.limit value.
        if (data.limit != null) { this.options.limit = data.limit; }
        if (this.options.offset + this.options.limit > this.options.maxNum) {
            endValue = this.options.maxNum;
        } else {
            endValue = this.options.offset + this.options.limit;
        }
        $(this.containerIdString + "_current_range").innerHTML =
            (this.options.offset + 1) + "-" + endValue;
        // if it's the last row, disable next page and next rows controls
        if (endValue == this.options.maxNum) {
            this.disableSingleControlType("next");
            this.disableSingleControlType("end");
        }
        this._showControls();
    }
    if (loadedForQA) {loadedForQA(true); }
  },

  /**
   * Gets the headers are a <tr><th>... </th></tr> string from the headers
   * array. Note: This function allows the headers array to be multidimensional
   * allowing for multiple header rows.
   */
  _getHeaderString: function(headers) {
      var headerString = "";
      for (var i = 0; i < headers.length; i++) {
         if (Object.isArray(headers[i])) {
            headerString += this._getHeaderString(headers[i])
            if (i < ( headers.length - 1)) { headerString += "</tr><tr>"; }
         } else {
           //TODO fix up so that it sets colspan="x" for consecutive empty headers.
            classString = "col_" + i;
            headerString += "<th class=\"" + classString + "\" >" + headers[i] + "</th>";
         }
      }
      return headerString;
  },

  _hideControls: function() {
    $(this.containerIdString + '_horizontal_controls').hide();
    $(this.containerIdString + '_vertical_controls').hide();
    $(this.containerIdString + '_extra_controls').hide();
    $(this.containerIdString + '_total_rows_text').hide();
    if($('ie_export_info')) { $('ie_export_info').hide(); }
  },

  _showControls: function() {
    $(this.containerIdString + '_horizontal_controls').show();
    $(this.containerIdString + '_vertical_controls').show();
    $(this.containerIdString + '_extra_controls').show();
    $(this.containerIdString + '_total_rows_text').show();
    if($('ie_export_info')) { $('ie_export_info').show(); }
    this.updateControlsPosition();
  },

    // Put the given element within the viewport if it is not already
    _setLocationWithinViewport: function (mainElement, loadingElement) {
        if (!this._withinViewport(loadingElement)) {
            var vpOffset = document.viewport.getScrollOffsets(),

            loadingX = 100 + vpOffset[0];
            loadingY = 100 + vpOffset[1];

            finalX = loadingX + "px";
            finalY = loadingY + "px";

            loadingElement.setStyle({top:finalY,left:finalX});
        }
    },

    // Copied this function from the web: http://thinkweb2.com/projects/prototype/category/documentviewport/
    _withinViewport: function(el) {
        var elOffset = el.cumulativeOffset(),
            vpOffset = document.viewport.getScrollOffsets(),
            elDim = el.getDimensions(),
            vpDim = document.viewport.getDimensions();
        if (elOffset[1] + elDim.height < vpOffset[1] || elOffset[1] > vpOffset[1] + vpDim.height ||
            elOffset[0] + elDim.width < vpOffset[0]  || elOffset[0] > vpOffset[0] + vpDim.width) {
            return false;
        }
        return true;
    }

}; //end of PageGrid Class

/**
 * Cache for the PageGrid that stores the results retrieved from the server
 * To allows faster browsing and prevent unnecessary calls to the server.
 */
var GridCache = Class.create();
GridCache.prototype = {

  /** Constructor */
  initialize: function() {
      this.rows = new Array();
      this.headers = new Array();
  },

  /**
   * Get the cache for a given limit and offset.
   * @param limit - the number of rows to retrieve.
   * @param offset - the starting position.
   * @return false if not all data found, otherwise a JSON of the headers and requested rows.
   */
  getSubset: function(limit, offset) {
    toReturn = new Array();
    for (var i = offset; i < (offset + limit); i++) {
        if (this.rows[i] != null) {
            toReturn.push(this.rows[i]);
        } else {
            return false;
        }
    }

    return ("{ headers:" + this.headers.toJSON().toString()
          + ", rows:" + toReturn.toJSON().toString() + " }").evalJSON();
  },

  /**
   * Add a row to the cache.
   * @param row - the row to add.
   * @param position - the position of the row.
   */
  addRow: function(row, position) {
      this.rows[position] = row;
  },

  /**
   * Set the header values.
   * @param headers - array of header titles.
   */
  setHeaders: function(headers) {
      this.headers = headers;
  },

  /**
   * Return the Cache as a JSON object with the header information and all rows.
   */
  toJSON: function() {
    return ("{ headers:" + this.headers.toJSON().toString()
          + ", rows:" + this.rows.toJSON().toString() + " }").evalJSON();
  }
};
