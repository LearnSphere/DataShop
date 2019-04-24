//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Kyle Cunningham
// Version: $Revision: 9790 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2013-08-12 17:54:10 -0400 (Mon, 12 Aug 2013) $
// $KeyWordsOff: $
//

/**
 * This class is used to truncate chunks of text larger than a specified character limit.
 * When a block of text is too long, 2 <spans> are created - one containing a link to expand
 * the text, another to shrink it. 
 * @param theElement - the element to which the Truncator should be applied.
 * @param maxLength - length at which text should be truncated.
 */
var Truncator = Class.create();
Truncator.prototype = {
  /**
   * Initializes the Truncator object.  
   **/
  initialize: function(theElement, options) {
    this.element = $(theElement);
    this.fullText = this.element.innerHTML;
    var browser = navigator.userAgent.toLowerCase();
    this.isIE = browser.indexOf("msie") != -1;
    this.setOptions(options);
    this.init();
  },

  setOptions: function(options) {
    this.options = {
      maxLength: 75,  numLines: 3,
      expand: "more", shrink: "less"
    };
    Object.extend(this.options, options || { });
  },

  /**
   * Called by initialize() to determine if the element contains text that should be truncated.  
   * Will set up the expanded text and truncated text spans.  If it's not over the limit, show
   * the full text.
   **/
  init: function() {
  	if (this.fullText != null) {
  		if (this.fullText.length >= (this.options.maxLength * this.options.numLines)) {
  			this.truncText = this.fullText.truncate(this.options.maxLength * this.options.numLines);  
  			
  			//create an empty span to put the text in.
  	        this.element.innerHTML = '<span id="' + this.element.id + '_txt"></span>';
  	       
  	        //add both controls initially hidden
  	        this.expandSpanId = 'expand_span_' + this.element.id;
  	        this.truncSpanId = 'trunc_span_' + this.element.id;
  	        
            expandSpan = '<span id="' + this.expandSpanId
  			                    + '" class=\"control next\" title="expand">' + this.options.expand + '</span>';
  			truncSpan = '<span id="' + this.truncSpanId
  			                    + '" class=\"control back\" title="shrink">' + this.options.shrink + '</span>';
  			
  	        new Insertion.Bottom(this.element.id, expandSpan);
  	        new Insertion.Bottom(this.element.id, truncSpan);
            //init the event handling on the controls
            this.setExpandControl();
            this.setTruncControl();
  			this.truncate();
  		} else {
  			this.element.innerHTML = this.nl2br(this.textToLinks(this.fullText)); 
  		}
  	} 
  },
  
  /**
   * Replaces newline characters with break tags.  The Preceding space is an IE7 fix, which
   * doesn't recognize consecutive break statements without a character in between.
   */
  nl2br: function (text){
		text = escape(text);
		if(text.indexOf('%0D%0A') > -1){
			re_nlchar = /%0D%0A/g;
			text = text.replace(re_nlchar,'&nbsp;<br />')
		} else if(text.indexOf('%0A') > -1){
			re_nlchar = /%0A/g;
			text = text.replace(re_nlchar,'&nbsp;<br />')
		} else if(text.indexOf('%0D') > -1){
			re_nlchar = /%0D/g;
			text = text.replace(re_nlchar,'&nbsp;<br />')
		}
		return unescape(text);
  },
  
  /**
   * Turns http:// or https:// into clickable anchors and breaks long links in half
   * Used for formatting only, does not affect the actual text when it's modified. 
   **/
  textToLinks: function(text) {
	//strip html. needed as the code below breaks with embedded html.
	text = text.replace(/<.*?>/g, '');
	
  	//replace http:// or https:// with anchor version
  	if (text.indexOf('http://') > -1 || text.indexOf('https://') > -1) {
  		var re = /http(s?):\/\/(\S+)/gi;
  	    text = text.replace(re,'<a href=\"http$1:\/\/$2\" target=\"_blank\">http$1:\/\/$2<\/a>');
  	}

  	noWidthSpace = '&#8203;';
  	
  	//split on breaks
  	text.split('\n').each(function(line) {
  		//find all links on the line and put in an array
  		var re = /(<a[^<>]+>)?[^<>]*<\/a>/gi;
  		var matchesArray = line.match(re);
  		
  		if (matchesArray != null && matchesArray.length > 0){
  			matchesArray.each(function(linkText) {
  				//extract the URL
  				var re = /<a[^<>]+>([^<>]*)?<\/a>/i;
  				var urltext = linkText.match(re);

  				// let's see if it's a long URL
  				var theurltext = urltext[1];
  				if (theurltext.length > 70) {
  					//insert space every 70 characters
  					var newurl = "";
  					var loopCount = 0;
  					for (var i=0; i+70<theurltext.length; i+=70) {
  						var sub1 = theurltext.substr(i,70);
  						var sub2 = theurltext.substr(i+70);
  						if (i==0) {
  							newurl = sub1 + noWidthSpace + sub2;
  						} else {
  							newurl = newurl.substr(0,i+loopCount) + sub1 + noWidthSpace + sub2;
  						}
  						loopCount+=7; // noWidthSpace is 7 chars
  					}
  					
  					//sub new url for old
  					var regex = new RegExp("(<a[^<>]+>)("+RegExp.escape(theurltext)+")(<\/a>)","ig");
  					text = text.replace(regex,'$1'+newurl+'$3');
  				}
  			});
  		}
  	});
  	
  	return text;
  },
  
  /**
   * Writes the innerHTML containing truncated text into the element.
   **/
  truncate: function(event) {
	$(this.element.id + '_txt').innerHTML = this.nl2br(this.textToLinks(this.truncText)) + "<br/>";
	$(this.expandSpanId).show();
	$(this.truncSpanId).hide();
  	if (event) { Event.stop(event); }
  },
  
  /**
   * First cleans the text, then writes the innerHTML containing the full text into the element.
   **/
  expand: function(event) {	
	$(this.element.id + '_txt').innerHTML = this.nl2br(this.textToLinks(this.fullText)) + "<br />";
	$(this.expandSpanId).hide();
	$(this.truncSpanId).show();
  	if (event) { Event.stop(event);	}
  },
  
  /**
   * Sets the click, mouseover and mouseout controls for the expanded text span.  
   **/
  setExpandControl: function() {
  	var element = $(this.expandSpanId);
  	element.onclick = function () { return false; }
  	element.onmouseover = function () { return false; }
    element.onmouseout = function () { return false; }
  	
  	Event.observe(element, 'click', this.expand.bindAsEventListener(this));
  	Event.observe(element, 'mouseover', this.controlExpandMouseOver.bindAsEventListener(this));
  	Event.observe(element, 'mouseout', this.controlExpandMouseOut.bindAsEventListener(this));
  },
  
  /**
   * Sets the click, mouseover and mouseout controls for the truncated text span.
   **/
  setTruncControl: function() {
  	var element = $(this.truncSpanId);
  	element.onclick = function () { return false; }
  	element.onmouseover = function () { return false; }
    element.onmouseout = function () { return false; }
  	
  	Event.observe(element, 'click', this.truncate.bindAsEventListener(this));
  	Event.observe(element, 'mouseover', this.controlTruncMouseOver.bindAsEventListener(this));
  	Event.observe(element, 'mouseout', this.controlTruncMouseOut.bindAsEventListener(this));
  },  

  /**
   * Responsible for setting the appropriate class (style) onmouseover of the expanded text span.
   **/
  controlExpandMouseOver: function() {
     element = $(this.expandSpanId);
     element.removeClassName("hover");
 	 element.addClassName("hover");
  },
  
  /**
   * Responsible for setting the appropriate class (style) onmouseout of the expanded text span.
   **/
  controlExpandMouseOut: function(event) {
  	 element = $(this.expandSpanId);
 	 element.removeClassName("hover");
 	 element.removeClassName("disabled");
  }, 
  
  /**
   * Responsible for setting the appropriate class (style) onmouseover of the truncated text span.
   **/
  controlTruncMouseOver: function(id) {
     element = $(this.truncSpanId);
     element.removeClassName("hover");
 	 element.addClassName("hover");
  },
  
  /**
   * Responsible for setting the appropriate class (style) onmouseout of the truncated text span.
   **/
  controlTruncMouseOut: function() {
  	 element = $(this.truncSpanId);
 	 element.removeClassName("hover");
  } 
}
