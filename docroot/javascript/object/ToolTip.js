//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 12210 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-04-16 10:39:30 -0400 (Thu, 16 Apr 2015) $
// $KeyWordsOff: $
//

/**
 * This class is designed to create a tooltip that displays the content provided to it.
 * @param element - ID of the element to apply the tool tip too.
 * @param contentString - string of HTML which can be placed inside the tool-tip.  Follows the
 *                        follows the prototype Insertion rules for evaluating javascripts.
 * @param url - URL for the server calls for data.
 * @param options
 *   delay - amount of time after mouseover to wait before showing the tooltip in milliseconds. Default = 250.
 *   style - string of style rules
 *   extraClasses - string of additional css classes to apply to the primary object.
 *   timeout - amount of time a tool-tip will remain visible in milliseconds. Default = 10000;
 *   onCreate - onCreate function. Default = null.
 *   fixed - flag to indicate tooltip should remain up
 *
 * Example with an optional option set:
 *   options = new Array();
 *   options['style'] = 'background-color:blue;';
 *
 *   new ToolTip('myElement', '<p>A test</p><ul><li>item1</li><li>item2</li></ul>', options);
 *
 */
var ToolTip = Class.create();
ToolTip.prototype = {
  initialize: function(element, contentString, options) {
    //create a random random number for this tool-top between 1 and 1,000,000
    // note: valid element ids must not start with a number!
    // changed to have tool_tip_ come first to avoid this problem.
    this.toolTipId =  "tool_tip_" + Math.floor(Math.random()*1000001);

    this.contentString = contentString;
    this.element = $(element);
    this.delayStart = false;

    this.setOptions(options);
    this.init();
  },

  setOptions: function(options) {

    // If toolTip is 'fixed', extend default timeout to 30 seconds if not set.
    if ((options.fixed) && (!options.timeout)) {
        options.timeout = 30000;
    }

    this.options = {
      delay        : 250,
      style        : '',
      extraClasses : '',
      timeout      : 10000,
      onCreate     : null,
      fixed        : false
    };
    Object.extend(this.options, options || { });
  },

  /**
   * Init the event listeners.  These are "observers" which will not override any existing
   * event listeners on the object.
   */
  init: function() {
      Event.observe(this.element, 'mouseover', this.mouseOverHandler.bindAsEventListener(this));
      Event.observe(this.element, 'mouseout', this.mouseOutHandler.bindAsEventListener(this));
  },

  /**
   * Create the actual tool-tip.  By default the tool-tip is hidden to prevent
   * the flickering of an incorrectly placed tool-tip.
   * @param xStyle - the positional information for the style.left information. (Optional)
   * @param yStyle - the positional information for the style.top informatoin. (Optional)
   */
  createView: function() {
      if (!$(this.toolTipId)) {
        htmlString = "<div id=\"" + this.toolTipId +"\" class=\"toolTip ";
        if (this.options.extraClasses && Object.isArray(this.options.extraClasses)) {
            $A(this.options.extraClasses).each(
                function (cssClass, htmlString) { htmlString += ' ' + cssClass; });
        } else {
            htmlString += this.options.extraClasses + " \" ";
        }

        var styleString = '';
        if (this.options.style) { styleString = this.options.style; }
        htmlString += " style=\"" + styleString + " display: none;\" "
        htmlString += " ></div>";

        $(document.body).insert(htmlString);
        $(this.toolTipId).insert("<div>" + this.contentString + "</div>");
        this.updatePosition();
        if (this.options.timeout && this.options.timeout > 0) {

            if (this.timeOutTimer) { clearTimeout(this.timeOutTimer); }

            this.timeOutTimer =
                setTimeout(this.hideToolTip.bindAsEventListener(this), this.options.timeout);
        }

                if (this.options.fixed) {
                    // Register 'mouseleave' handler for those tooltips we want
                    // to stay visible, i.e., those with buttons for ProblemContent.
                    Event.observe($(this.toolTipId), 'mouseout',
                                  this.mouseLeaveHandler.bindAsEventListener(this));
                }

        var onCreate = this.options.onCreate;
                if (Object.isFunction(onCreate)) {
                    onCreate(this);
                    //because we only wanna run this once.
                    this.options.onCreate = false;
                };
      }
  },

  /**
   * Replace the content of the tooltip with the new content. Maintains the current tool-tip
   * position.
   * @param content - the new content, can be HTML and/or JAVASCRIPT.
   */
  replaceContent: function(content) {
    //because sometimes the info comes back before the start delay fires,
    //so just go ahead and end the delay and show it.
    if (this.delayStart !== false) {
        clearTimeout(this.delayStart);
        this.delayStart = false;
        isVis = true;
    } else {
        //store whether or not the tool-tip is currently visable.
        var isVis = $(this.toolTipId).visible();
    }

    //remove the old tool HTML and recreate the view.
    this.contentString = content;
    if ($(this.toolTipId)) { $(this.toolTipId).remove(); }

    if (isVis) { this.showToolTip(); }
  },

  /**
   * Event handler for the mouseover action.  Keeps a timeout function if the user is no longer
   * moused over the base element.  This helps prevent problems where the user's mouse is no longer
   * over the element, but the mouseout event did not fire for some reason.
   */
  mouseOverHandler: function() {
      if (this.options.delay && this.options.delay > 0) {
          if (this.delayStart == false) {
            this.delayStart = setTimeout(this.showToolTip.bindAsEventListener(this), this.options.delay);
          }
    }
  },

  /**
   * Event handler for mouseout actions.
   */
  mouseOutHandler: function() {
    if (this.delayStart !== false) {
        clearTimeout(this.delayStart);
        this.delayStart = false;
    }
    if (!this.options.fixed) {
        this.hideToolTip();
    }
  },

  /**
   * Handler to mimic 'mouseout' for the tooltip itself.
   */
  mouseLeaveHandler: function(e) {
        if (!e) var e = window.event;

        // Get target, which, for 'mouseout' is the element we are leaving.
        var tg = (window.event) ? e.srcElement : e.target;
        if (tg.id) {
            var tgId = tg.id;
            // If target isn't tooltip, we obviously didn't leave the tooltip...
            if (!tgId.startsWith("tool_tip_")) {
                return;
            }
        }

        // Continue to show the tooltip if the node being entered (reltg)
        // is a descendent of the tooltip node -- whose id is this.toolTipId.
        var reltg = (e.relatedTarget) ? e.relatedTarget : e.toElement;
        while (reltg.id != this.toolTipId && reltg.nodeName != 'BODY') {
            reltg = reltg.parentNode;

            if (reltg.id && reltg.id == this.toolTipId) {
                // We found an ancestor node that matches the tooltip id.
                return;
            }
        }

        // Elvis has left the tooltip.
        this.hideToolTip();
    },

  /**
   * Hides the tool-tip and disables the mouseMoveHandler.
   */
  hideToolTip: function() {
    if ($(this.toolTipId)) { $(this.toolTipId).hide(); }
  },

  showToolTip: function() {

        // Ensure no other tooltips are showing...
        var visibleTips = $(document.body).getElementsByClassName("toolTip");
        for (var i = 0; i < visibleTips.length; i++) {
            visibleTips[i].hide();
        }

    //kill the timeout of it's hanging around for some reason.
    if (this.delayStart !== false) {
        clearTimeout(this.delayStart);
        this.delayStart = false;
    }

    //if it doesn't exist, create the element and set it's initial position.
      if (!$(this.toolTipId)) { this.createView();}

      //if it's not visible, show it.
      if (!$(this.toolTipId).visible()) {
          $(this.toolTipId).show();
          this.adjustSize();
        //because they may have changed the view port, or updated the size of the tool tip.
        //go ahead and update the positioning of it.
        this.updatePosition();

    }

      //set the timout for hiding.
      if (this.options.timeout && this.options.timeout > 0) {
          if (this.timeOutTimer) {
              clearTimeout(this.timeOutTimer);
              if (!isThisIE()) { this.timeOutTimer = false; }
        }
        this.timeOutTimer =
            setTimeout(this.hideToolTip.bindAsEventListener(this), this.options.timeout);

      }
  },

  /**
   * Because the size of the content can be bigger (aka long ass names) we can
   * adjust the size of the main div to reflect the content.  This seems to only
   * work once the content is visible.
   */
  adjustSize: function() {
      var maxContentWidth = 0;
      $(this.toolTipId).descendants().each(
          function (element) {
              var elementWidth = element.getWidth();
              maxContentWidth = maxContentWidth < elementWidth ? elementWidth: maxContentWidth;
          }
      );

      if ($(this.toolTipId).getWidth() < maxContentWidth + 10) {
          var newWidth = maxContentWidth + 40;
          $(this.toolTipId).setStyle({width: newWidth + "px"});
      }
  },

  /**
   * Update the positioning of the tooltip in case the base element moved on you.
   */
  updatePosition: function() {
        this.element.makePositioned();
        $(this.toolTipId).removeClassName("leftAlign");

        //just get the dimensions once.
        var elementDimensions = this.element.getDimensions();

        var xPosition = 0;
        var yPosition = 0;

        // Sigh. Google, while still WebKit, is behaving differently and
        // the tooltips are in the wrong spot.
        var isChrome = false;
        var vendor = window.navigator.vendor;
        if (vendor != undefined) {
            isChrome = vendor.startsWith("Google");
        }

        //image map "area" requires some special work. We need to look at the coordinate attribute
        //and offset our tool tip accordingly because otherwise it just aligns to the image.
        var imageMapXOffset = 0;
        var imageMapYOffset = 0;
        if (this.element.nodeName.toUpperCase() == "AREA") {
            var coordinates = [];
            this.element.readAttribute('coords').scan(/\w+/, function(match){ coordinates.push(match[0])});

            if (isThisIE()) {
                //IE does not get the position of the map in the page, get the
                //position information for the image instead then.
                var img = this.element.up().next('img')
                if (img) {
                    img.makePositioned();
                    imageMapYOffset = img.cumulativeOffset()[1];
                    imageMapXOffset = img.cumulativeOffset()[0];
                }

                xPosition = (this.element.cumulativeOffset()[0])
                    + elementDimensions.width + imageMapXOffset ;

                //first get the y value of the top of base element then add half
                //the height to find the middle of it.
                yPosition = (this.element.cumulativeOffset()[1])
                    + (elementDimensions.height * .5) + imageMapYOffset;

            } else if ((Prototype.Browser.WebKit) && !isChrome) {
                var img = this.element.up().next('img')
                if (img) {
                    img.makePositioned();
                    imageMapYOffset = -(img.cumulativeOffset()[1] + (+coordinates[1])
                        + ((+coordinates[3] - +coordinates[1]) / 2));
                    imageMapXOffset = -(img.cumulativeOffset()[0] + (+coordinates[2]));
                }
            } else {
                imageMapYOffset = (elementDimensions.height * .5) - ((+coordinates[1] + +coordinates[3]) / 2);
                imageMapXOffset = (elementDimensions.width - +coordinates[2]);

            }
        }

        //get the position of the base element, add the width of it
        if ((xPosition == 0) && (yPosition == 0)) {
            xPosition =
                (this.element.cumulativeOffset()[0] - this.element.cumulativeScrollOffset()[0])
                + document.viewport.getScrollOffsets()[0]
                + elementDimensions.width - imageMapXOffset;

            //first get the y value of the top of base element then add half the
            //height to find the middle of it.
            yPosition =
                (this.element.cumulativeOffset()[1] - this.element.cumulativeScrollOffset()[1])
                + document.viewport.getScrollOffsets()[1]
                + (elementDimensions.height * .5) - imageMapYOffset;

        }
        //now take half the height of the actual tool tip and subtract that from the y
        yPosition = yPosition - ($(this.toolTipId).getHeight() * .5);


        //now that we know where we want to put it, find out if we have room for it.
        if (!this._fitsOnScreen(xPosition)) {

            //oh no! no room, put it on the left instead.
            if (isThisIE()) {
                xPosition = xPosition - (elementDimensions.width + $(this.toolTipId).getWidth());
            } else if ((Prototype.Browser.WebKit) && !isChrome) {

                if (this.element.nodeName.toUpperCase() == "AREA") {
                    xPosition = xPosition - ((+coordinates[2] - +coordinates[0]) + $(this.toolTipId).getWidth());
                } else {
                    xPosition = xPosition - (elementDimensions.width + $(this.toolTipId).getWidth());
                }
            } else {
                if (this.element.nodeName.toUpperCase() == "AREA") {
                    imageMapXOffset = +coordinates[0] + imageMapXOffset;
                }
                //need to reduce imageMapXOffset by 5 pixels to make sure the xPosition is on the left side of map boarder
                xPosition = xPosition + (imageMapXOffset - 5)
                        - (elementDimensions.width + $(this.toolTipId).getWidth());
            }

            $(this.toolTipId).addClassName("leftAlign");
        }

        $(this.toolTipId).style.top = yPosition + "px";
        $(this.toolTipId).style.left = xPosition + "px";


        this.element.undoPositioned();
  },

  /**
   * Checks that the tool tip will fit on the left inside the viewport. Returns
   * true if it fits, false otherwise.
   * @param xPosition the current or hopeful xPosition of the tool tip.
   */
  _fitsOnScreen: function(xPosition) {
      var toolTipWidth = $(this.toolTipId).getWidth();
      var viewportWidth = document.viewport.getWidth();
      return ((xPosition + toolTipWidth) <= viewportWidth );
  }

}
