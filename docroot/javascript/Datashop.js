//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 14411 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2017-10-20 09:37:51 -0400 (Fri, 20 Oct 2017) $
// $KeyWordsOff: $
//

//NOTE: This script should always be included first!

//CONSTANTS
//Location of the home page for the site.
var HOME = "index.jsp";

/**
Observer object for any items which you will wish to observe.
Simple create a new observer object, and invoke it when you would fire a custom event.
Adding listeners occurs through the addListener()
*/
function Observer(name) {
	this.name = name;
	this.listeners = new Array();
}

Observer.prototype.addListener=function(listener) {
	//alert("adding listener to " + this.name + " :: " + listener);
	this.listeners[this.listeners.length]=listener;
}

Observer.prototype.removeListener=function(listener) {
    var temp = new Array();
    
    while (this.listeners.length > 0) {
        var func = this.listeners.shift();
        if (func != listener) {
            temp.push(func);
        } else {
            //alert("removing listener from " + this.name + " :: " + listener);
        }
    }
    this.listeners = temp;
}

Observer.prototype.invoke=function() {
	//alert("invoking observer named: " + this.name);
	for(var i=0;  i< this.listeners.length; i++) {
		var func=this.listeners[i];
		func.call();
	}
}

//Observer pattern implementation of the on load functionality for
//the onload function.  To add a function to the onload simply
//call onloadObserver.addListener(<listener function>);

var onloadObserver = new Observer("onload");

var dataset = null;
var projectId = null;
var discourseId = null;

window.onload=function() {
	createQAElement();  //can comment out when not QA testing.
	dataset = gup();
	onloadObserver.invoke();
}

function gup() {
//  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]datasetId=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( window.location.href );
  if( results == null )
    return null;
  else
    return results[1];
}

/** Store only instance of the help window. */
var theHelpWindow;
onloadObserver.addListener(initHelpWindow);

function initHelpWindow() {
    if (theHelpWindow == undefined) {
        theHelpWindow = new HelpWindow.Base("helpButton");
    }
}

/** Sets the loaded for QA hidden element */
function createQAElement() {
    var input = document.createElement("input");
    input.id="loadedForQA";
    input.value="false";
    input.type="hidden";
    document.getElementsByTagName('body').item(0).appendChild(input);
}

/** Sets the value of the hidden QA field */
function loadedForQA(newValue) {
    var input = document.getElementById("loadedForQA");
    if (input != null) {
        input.value=newValue;
    } else {
        alert("ERROR: loadedForQa not found");
    }
}

/** Helper function that removes all children of the passed in value */
function clearContents(obj) {
	while (obj.firstChild != null) {
		obj.removeChild(obj.firstChild);
	}
}

function errorPopup(message) {
    messagePopup(message, "ERROR");
}

function warningPopup(message) {
    messagePopup(message, "WARNING");
}

function successPopup(message) {
    messagePopup(message, "SUCCESS");
}

/** Pops up an error message in the middle that turns off after 3 seconds */
function messagePopup(message, messageType) {
    if (!messageType) { messageType = "ERROR"; }

    messagePopupDiv = document.getElementById("messagePopup");
    if (!messagePopupDiv) {
        messagePopupDiv = document.createElement("div");
    }
    messagePopupDiv.className="popupMenu";
    messagePopupDiv.id = "messagePopup";
    contentPara = document.createElement('P');
    if (messageType == "ERROR") {
        contentPara.className="errorPopupContent";
    } else if (messageType == "SUCCESS") {
        contentPara.className="successPopupContent";       
    } else if (messageType == "WARNING") {
        contentPara.className="warningPopupContent";       
    } else {
        contentPara.className="messagePopupContent"; 
    }
    contentPara.appendChild(document.createTextNode(message));
    messagePopupDiv.appendChild(contentPara);

    document.body.appendChild(messagePopupDiv);
    if (messagePopupDiv.timeout) {
        clearTimeout(messagePopupDiv.timeout);
    }
    messagePopupDiv.timeout = setTimeout(closeErrorPopup.bindAsEventListener(messagePopupDiv), 4000);
}

function closeErrorPopup() {
    if (this) {
        this.style.display="none";
        try {
            document.body.removeChild(this);
        } catch(err) {
            //do nothing as it just means it wasn't found 
        }
    }
}

function modalSuccessPopup(message) {
    modalMessagePopup(message, "SUCCESS");
}
function modalWarningPopup(message) {
    modalMessagePopup(message, "WARNING");
}
function modalErrorPopup(message) {
    modalMessagePopup(message, "ERROR");
}

/** Create a model dialog using JQuery. */
function modalMessagePopup(message, messageType) {
	
	//Create div for dialog here
    var dialogDiv = document.createElement('div');
    dialogDiv.id   = "modalMessageDialog";
    dialogDiv.name = "modalMessageDialog";
    
    //Add dialog div to the body of the document
    document.getElementsByTagName('body').item(0).appendChild(dialogDiv);
    
    //Create a paragraph element, with image if necessary
    var p = document.createElement('p');
    var imgSrc = "";
    if (messageType == "ERROR") {
        imgSrc = "images/close_32.gif";
        p.innerHTML = "<img id=\"modalMessageDialogImg\" src=\"" + imgSrc + "\">" + message;
    } else if (messageType == "WARNING") {
        imgSrc = "images/alert_32.gif";
        p.innerHTML = "<img id=\"modalMessageDialogImg\" src=\"" + imgSrc + "\">" + message;
    } else {
        p.innerHTML = message;
    }
    dialogDiv.appendChild(p);
	
    //Create dialog with button to close it, nothing more
    jQuery("#modalMessageDialog").dialog({
        modal: true,
        buttons : [ {
            id : "model-message-popup-ok-button",
            text : "OK",
            click : function() {
            	        jQuery("#modalMessageDialog").dialog('close');
                    }
        } ]
    });
}

/** Following tracks the mouse movement across the screen. */
var mouse_x = 0;
var mouse_y = 0;

var IE = document.all?true:false

if (!IE) document.captureEvents(Event.MOUSEMOVE)

/** Determines and sets the current position of the mouse cursor*/
function getMouseXY(e) {
    e = e || window.event;
    var cursor = {x:0, y:0};
    if (e.pageX || e.pageY) {
        cursor.x = e.pageX;
        cursor.y = e.pageY;
    } 
    else if (document.body && document.documentElement) {
        cursor.x = e.clientX + (document.documentElement.scrollLeft || document.body.scrollLeft) 
            - document.documentElement.clientLeft;
        cursor.y = e.clientY + (document.documentElement.scrollTop || document.body.scrollTop) 
            - document.documentElement.clientTop;
    }

    mouse_x = cursor.x;
    mouse_y = cursor.y;
    return true
}

document.onmousemove = getMouseXY;


function overlib(str, id) {

    //check to see if it's a special call.
    var strArrays = str.split("\t");
    if(strArrays.length > 0 && strArrays[0]=="PP_DOMAIN_MENU_TITLE") {
        ppDisplayDomainMenu(strArrays);
    } else if(strArrays.length > 0 && strArrays[0]=="PP_RANGE_MENU_TITLE") {
        ppDisplayRangeMenu(strArrays);
    } else if(strArrays.length > 0 && strArrays[0]=="LC_TYPE_MENU_TITLE") {
        lcDisplayTypeMenu(strArrays);	
    } else {
        infoDiv = document.getElementById("overlibInfoDiv");
        if (!infoDiv) {
            infoDiv = document.createElement("div");
        } else {
            while(infoDiv.firstChild != null) {
                 infoDiv.removeChild(infoDiv.firstChild);
            }
        }

        if (id) {
            area = $(id);
            try {
                area.onmouseover = false;
                area.onmouseout = false;
                area.writeAttribute({ onmouseover: false, onmouseout: false });
            } catch(err) { /* for some reason this throws an error, but still works */ }
            
            //tell the tool tip to zebra stripe the table when it creates it.
            var onCreateFunc = function(toolTip) {
                var count = 0;
                $(toolTip.toolTipId).select('tr').each(function (row, count) {
                    row.addClassName((count % 2) ? 'even' : 'odd');
                    count++; 
                });
            }

            tt = new ToolTip(id, str,
                             {extraClasses: 'graphTip', onCreate: onCreateFunc, fixed: 'true'});
            tt.mouseOverHandler();
        } else {
            p = document.createElement('p');
            //p.appendChild(document.createTextNode(str));
            p.innerHTML = str;
            infoDiv.style.display="block";
            infoDiv.appendChild(p);

            infoDiv.className="overlibInfoDiv";
            infoDiv.id = "overlibInfoDiv";          
            var divStyle = infoDiv.style;
            divStyle.position = "absolute";           
            divStyle.top  = (mouse_y - 20) + "px";
            divStyle.left = (mouse_x + 5) + "px";
            document.body.appendChild(infoDiv);
        }
    }
}

function nd() {
    infoDiv = document.getElementById("overlibInfoDiv");
    if (infoDiv) {
        infoDiv.style.display="none";
        document.body.removeChild(infoDiv);
    }
}


/**
 * Method to convert newlines to break elements.
 * @param text the text to modify
 * @returns the converted text
 */
function nl2br(text){
	text = escape(text);
	if (text.indexOf('%0D%0A') > -1) { 
        re_nlchar = /%0D%0A/g ;
        text = text.replace(re_nlchar,'<br>')
	} else if(text.indexOf('%0A') > -1) {
        re_nlchar = /%0A/g ;
        text = text.replace(re_nlchar,'<br>')
	} else if(text.indexOf('%0D') > -1) {
        re_nlchar = /%0D/g ;
        text = text.replace(re_nlchar,'<br>')
	}
	return unescape(text);
}

/**
 * Method to convert break elements to newlines.
 * @param text the text to modify
 * @returns the converted text
 */
function br2nl(text){
	text = text.escapeHTML();
	return unescape( text.gsub('&lt;br&gt;',"\n") );
}

/**
 * Method to change plain URLs to HTML anchors.
 * Expecting given text to not contain any HTML already.
 * @param text the text to modify
 * @returns the converted text
 */
function fixUrls(text) {
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
}

/**
 * Cookie handler function.  Sets a variable name, value and # days to keep it.
 */
function createCookie(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	} else {
	    var expires = "";
	}
	document.cookie = name+"="+value+expires+"; path=/";
}

function readCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

function eraseCookie(name) {
	createCookie(name,"",-1);
}

/**
 * 
 */
function insertEncodedEmail() {
    var encodedEmail = jQuery('#ds-help-email').val();

    if (encodedEmail == undefined) {
        // Default to original...
        encodedEmail = "qngnfubc-uryc@yvfgf.naqerj.pzh.rqh";
    }

    var decodedEmail = encodedEmail.replace(/[a-zA-Z]/g, charEncode);

    // Display a portion of the decoded email on the 'Contact us' page.
    var index = decodedEmail.indexOf("@");
    var cutoff = (index < 5) ? index : 5;
    var toDisplay = decodedEmail.substring(0, cutoff) + "..."
        + decodedEmail.substring(index, decodedEmail.length);

    document.write("<a href='mailto:" + decodedEmail + "' rel=nofollow'>");
    document.write(toDisplay + "</a>");
}

function charEncode(c) {
    return String.fromCharCode((c<="Z"?90:122)>=(c=c.charCodeAt(0)+13)?c:c-26);
}

/**
 *  The following is additions to the Date object.  Most specifically it is a parser of 
 *  a Date-Time field of the format YYYY-MM-DD HH:MM:SS.
 *
 *  This is a HEAVY modification of a script from... 
 *  DHTML date validation script. Courtesy of SmartWebby.com (http://www.smartwebby.com/dhtml/)
 */
Date.prototype.error= "";
Date.prototype.dateChar= "-"; //char for separating dates
Date.prototype.timeChar= ":"; //char for separating times.
Date.prototype.minYear=1900; //min allowed year for isValidDate.
Date.prototype.maxYear=2100; //max allowed year for isValidDateate.

//takes an integer representation of the month and returns the number of days in that month.
Date.prototype.getDaysInMonth = function (month) {
	
	if (month == 4 || month == 6 || month == 9 || month == 11) {
		return 30;
	} else if (month == 2) {
		if (this.getFullYear() != null) {
			return this.daysInFebruary(this.getFullYear());
		} else {
			return 29;
		}
	} else {
		return 31;
	}
}

//returns the number of days in February for a given year.
//will use the year in the object if none is passed in.
Date.prototype.daysInFebruary = function(year){

    if (year == null && this.getFullYear() != null) {
    	year = this.getFullYear();
    }
	// February has 29 days in any year evenly divisible by four,
    // EXCEPT for centurial years which are not also divisible by 400.
    return (((year % 4 == 0) && ( (!(year % 100 == 0)) || (year % 400 == 0))) ? 29 : 28 );
}

//checks that each char in a string is a number.
//Not really part of the date class, but didn't know where else to put it.
Date.prototype.isInteger = function(s){
	var i;
    for (i = 0; i < s.length; i++){   
        // Check that current character is number.
        var c = s.charAt(i);
        if (((c < "0") || (c > "9"))) return false;
    }
    // All characters are numbers.
    return true;
}

//looks at a dateTime of the format YYYY-DD-MM HH:MM:SS.
//verifies that the string is valid and that the date is valid.
Date.prototype.isDateTime = function(dateString){

	//break the dateString into a time and date.
	dateTimeArray = dateString.split(" ");
	if (dateTimeArray.length != 2) {
	    this.error = "Invalid Date and Time '" + dateString + "'. "
		    + "<br />Format must be : "
		    + "yyyy" + this.dateChar + "mm" + this.dateChar + "dd"
		    + " hh" + this.timeChar + "mm" + this.timeChar + "ss.";
		return false;
	}
	
	if (!this.isValidDate(dateTimeArray[0]) ) {
		return false;
	}

	if (!this.isValidTime(dateTimeArray[1]) ) {
		return false;
	}

	return true;

}

//takes a String of the format HH:MM:SS amd makes sure its a valid time.
Date.prototype.isValidTime = function(timeString) {


	//break the time portion into the hour, minutes, seconds.
	timeArray = timeString.split(this.timeChar);
	if (timeArray.length != 3) {
		this.invalidTimeError(timeString);
		return false;
	}
	
		//check that that date portion of the string is valid.
	var strHour=timeArray[0];
	var strMinute=timeArray[1];
	var strSecond=timeArray[2];

	//remove leading zeros.
	if (strHour.charAt(0)=="0" && strHour.length > 1) {
	  	strHour=strHour.substring(1);
	}
	if (strMinute.charAt(0)=="0" && strMinute.length>1) {
	  	strMinute=strMinute.substring(1);
	}
	if (strSecond.charAt(0)=="0" && strSecond.length>1) {
	  	strSecond=strSecond.substring(1);
	}
	
	//convert the strings to integers.
	if ( this.isInteger(strHour) ) {
		hour=parseInt(strHour);
	} else {
		this.invalidTimeError(timeString);
		return false;
	}
	
	if ( this.isInteger(strMinute) ) {
		minutes=parseInt(strMinute);
	} else {
		this.invalidTimeError(timeString);
		return false;
	}
	
	if ( this.isInteger(strSecond) ) {
		seconds=parseInt(strSecond);
	} else {
		this.invalidTimeError(timeString);
		return false;
	}
	
	//verify the values.
	if ( hour > 23 || hour < 0 ) {
		this.invalidTimeRangeError(hour, "hour");
		return false;
	} else {
		this.setHours(hour);
	}
	
	if ( minutes > 59 || minutes < 0 ) {
		this.invalidTimeRangeError(minutes, "minutes");
		return false;
	} else {
		this.setMinutes(minutes);
	}
	
	if ( seconds > 59 || seconds < 0 ) {
		this.invalidTimeRangeError(seconds, "seconds");
		return false;
	} else {
		this.setSeconds(seconds);
	}
	
	this.setMilliseconds(0); //sets the milliseconds to zero.
	
	return true;
}



//takes a String of the format YYYY-MM-DD and checks to make
//sure it's a valid date.  If it is valid it sets this date object
//to the given values of the month, day and year.
//returns true if the date is valid, and false if it is not.
Date.prototype.isValidDate = function(dateString) {

	//break the date portion into the month, day, year.
	dateArray = dateString.split(this.dateChar);
	if (dateArray.length != 3) {
		this.invalidDateError(dateString);
		return false;
	}

	//check that that date portion of the string is valid.
	var strMonth=dateArray[1];
	var strDay=dateArray[2];
	var strYear=dateArray[0];

	if (strDay.charAt(0)=="0" && strDay.length>1) {
	  	strDay=strDay.substring(1);
	}
	
	if (strMonth.charAt(0)=="0" && strMonth.length>1) {
		strMonth=strMonth.substring(1);
	}

	if ( this.isInteger(strMonth) ) {
		month=parseInt(strMonth);
	} else {
		this.invalidMonthError(strMonth);
		return false;
	}
	
	if ( this.isInteger(strDay) ) {
		day=parseInt(strDay);
	} else {
		this.invalidDayError(strYear);
		return false;
	}
	
	if ( this.isInteger(strYear) ) {
		year=parseInt(strYear);
	} else {
		this.invalidYearError(strYear);
		return false;
	}

	if (strYear.length != 4 
	 || year==0
     || year<this.minYear
     || year>this.maxYear) {
		this.invalidYearError(strYear);
		return false;
	} else {
		this.setFullYear(year);
	}

	if (strMonth.length<1 || month<1 || month>12){
		this.invalidMonthError(strMonth);
		return false;
	} else {
		this.setMonth(month-1);
	}
	
	if (strDay.length<1 || day<1 || day > this.getDaysInMonth(month)) {
		this.invalidDayError(strDay);
		return false;
	} else {
		this.setDate(day);
	}

	return true;
}

/************************************
*          TIME/DATE  ERRORS
*************************************/

Date.prototype.invalidDateError = function(dateString) {
	this.error = "Invalid Date '" + dateString + "'. "
		+ "<br />The date format must be : "
		+ "yyyy" + this.dateChar + "mm" + this.dateChar + "dd";
}

Date.prototype.invalidDayError = function(dayString) {
	this.error = "Invalid Day '" + dayString + "'. "
		+ "Please enter a valid day.";
}

Date.prototype.invalidMonthError = function(monthString) {
	this.error = "Invalid Month '" + monthString + "'. "
		+ "Please enter a valid month.";
}

Date.prototype.invalidYearError = function(yearString) {
	
	this.error = "Invalid Year '" + yearString + "'. "
		+ "<br />Please enter a valid year between " + this.minYear + " and " + this.maxYear + ".";
}

Date.prototype.invalidTimeError = function(timeString) {
	this.error = "Invalid Time '" + timeString + "'. "
		+ "<br />The time format must be : "
		+ "'hh" + this.timeChar + "mm" + this.timeChar + "ss'.";
}

Date.prototype.invalidTimeRangeError = function(number, type) {
	this.error = "Invalid " + type + " '" + number + "'. "
		+ "<br />Please enter a valid number for " + type + ".";
} 


function pause(numberMillis) {
    var now = new Date();
    var exitTime = now.getTime() + numberMillis;
    while (true) {
        now = new Date();
        if (now.getTime() > exitTime)
            return;
    }
}

/** 
 * From Lightbox2 by Lokesh Dhakar
 * Lightbox2 is licensed under a Creative Commons Attribution 2.5 License
 * Function calculates the page size (size including scroll offsets) across browsers
 */
function getPageSize() {

    var xScroll, yScroll;
  
    if (window.innerHeight && window.scrollMaxY) { 
        xScroll = window.innerWidth + window.scrollMaxX;
        yScroll = window.innerHeight + window.scrollMaxY;
    } else if (document.body.scrollHeight > document.body.offsetHeight){ // all but Explorer Mac
        xScroll = document.body.scrollWidth;
        yScroll = document.body.scrollHeight;
    } else { // Explorer Mac...would also work in Explorer 6 Strict, Mozilla and Safari
        xScroll = document.body.offsetWidth;
        yScroll = document.body.offsetHeight;
    }

    var windowWidth, windowHeight;
  
    if (self.innerHeight) { // all except Explorer
    if(document.documentElement.clientWidth){
        windowWidth = document.documentElement.clientWidth; 
    } else {
        windowWidth = self.innerWidth;
    }
    windowHeight = self.innerHeight;
    } else if (document.documentElement && document.documentElement.clientHeight) { // Explorer 6 Strict Mode
        windowWidth = document.documentElement.clientWidth;
        windowHeight = document.documentElement.clientHeight;
    } else if (document.body) { // other Explorers
        windowWidth = document.body.clientWidth;
        windowHeight = document.body.clientHeight;
    }
  
    // for small pages with total height less then height of the viewport
    if(yScroll < windowHeight){
        pageHeight = windowHeight;
    } else { 
        pageHeight = yScroll;
    }
 
    // for small pages with total width less then width of the viewport
    if(xScroll < windowWidth){ 
        pageWidth = xScroll;  
    } else {
        pageWidth = windowWidth;
    }

    return [pageWidth,pageHeight];
}

// Can we fix IE11 issues? Sigh.
function isThisIE() {
    // Pre-version 11, this was sufficient.
    if (Prototype.Browser.IE) { return true; }

    var ua = navigator.userAgent;

    // With IE 11, the string 'Trident' appears in this.
    if (ua.indexOf('Trident') > 0) { return true; }

    // With Microsoft Edge...
    if (ua.indexOf('Edge') > 0) { return true; }

    // Safari... oddly enough, the userAgent for Chrome includes 'Safari'
    if ((ua.indexOf('Safari') > 0)
        && (ua.indexOf('Chrome') < 0)) { return true; }

    return false;
}