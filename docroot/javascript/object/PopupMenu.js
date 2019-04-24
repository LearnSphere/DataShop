//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Kyle A Cunningham
// Version: $Revision: 7245 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2011-11-09 10:12:24 -0500 (Wed, 09 Nov 2011) $
// $KeyWordsOff: $
//

/********************************************************************** 
* Popup Menu object for use in PP and LC tools (moved from
*     PerformanceProfiler.js into its own class. 
**********************************************************************/
function PopupMenu(menuTitle, elementId, optionsArray, x_position, y_position, onSelect, theCurrentSelection) {
    this.optionsArray = optionsArray;
    this.x_position = x_position;
    this.y_position = y_position;
    this.onSelect = onSelect;
    this.menuTitle = menuTitle;
    this.elementId = elementId;
    this.currentSelection = theCurrentSelection;
    this.contentDiv = null;
    
    this.mousedOver = false;
    this.spanList = new Array();
    this.isTiming = false;
    
    this.createView();
}


PopupMenu.prototype.createView = function() {
    this.contentDiv = $("popMenu" + this.elementId);
    if (!this.contentDiv) {
        this.contentDiv = document.createElement("div");
    } else {
        return;
    }
    
    this.contentDiv.className="popupMenu";
    this.contentDiv.id = "popMenu" + this.elementId;
    var divStyle = this.contentDiv.style;
    divStyle.position = "absolute";
    divStyle.top  = (this.y_position - 50) + "px";
    divStyle.left = (this.x_position + 15) + "px";
    if (this.menuTitle) {
        title = document.createElement('h1');
        title.className="popupTitle";
        title.appendChild(document.createTextNode(this.menuTitle));
        this.contentDiv.appendChild(title);
    }

    for(i = 0; i < this.optionsArray.length; i++) {
        span = document.createElement('span');
        if (this.optionsArray[i] == this.currentSelection) {
            span.isCurrentSelection = true;
            span.className="currentSelectionPopupItem";
        } else {
            span.isCurrentSelection = false;
            span.className="nonSelectedPopupItem";
        }
        span.onmouseover = this.itemMouseoverHandler.bindAsEventListener(span);
        span.onmouseout = this.itemMouseoutHandler.bindAsEventListener(span);
        span.onclick = this.itemOnclickHandler.bindAsEventListener(this);
        span.appendChild(document.createTextNode(this.optionsArray[i]));
        span.id = 'menu_item_' + this.optionsArray[i];
        this.contentDiv.appendChild(span);
        this.spanList[i] = span;
    }

    document.body.appendChild(this.contentDiv);
    this.contentDiv.onmouseover = this.mouseoverHandler.bindAsEventListener(this);
    this.contentDiv.onmouseout = this.mouseoutHandler.bindAsEventListener(this);
    this.refreshTimeout();
}

PopupMenu.prototype.closePopup = function() {
    if (this.contentDiv) {
        this.contentDiv.style.display="none";
        try {
            document.body.removeChild(this.contentDiv);
        } catch(err) {
            //do nothing as it just means it wasn't found 
        }
        if (this.isTiming) {
            clearTimeout(this.isTiming);
            this.isTiming = false;
        }
    }
}

PopupMenu.prototype.refreshTimeout = function(time) {
    var timeout = 2000
    if (time) timeout = time;
    
    if (this.isTiming) {
        clearTimeout(this.isTiming);
        this.isTiming = false;
    }
    this.isTiming = setTimeout(this.closePopup.bindAsEventListener(this), timeout);
}

PopupMenu.prototype.mouseoverHandler = function(e) {
    document.onmousemove = this.mouseMoveHandler.bindAsEventListener(this);
    if (this.isTiming) {
        clearTimeout(this.isTiming);
        this.isTiming = false;
    }
}

PopupMenu.prototype.mouseoutHandler = function(e) {
    
}

PopupMenu.prototype.mouseMoveHandler = function(e) {
    if (!e) var e = window.event;
	var tg = (window.event) ? e.srcElement : e.target;
    
    if (tg.className == 'popupMenu') return;
    var tgPar = tg.parentNode;
    while(tgPar.nodeName != 'BODY' && tgPar.className != 'popupMenu') {
        tgPar = tgPar.parentNode;
    }
    if (tgPar.className == 'popupMenu') return;

    this.refreshTimeout(500);
    document.onmousemove = getMouseXY;
}

PopupMenu.prototype.itemMouseoverHandler = function(e) {
    if (!this.isCurrentSelection) {
        this.className="selectedPopupItem";
    }
    this.style.cursor="pointer";
    this.isSelectedItem = true;
}

PopupMenu.prototype.itemMouseoutHandler = function(e) {
    if (!this.isCurrentSelection) {
        this.className="nonSelectedPopupItem";
    }
    this.style.cursor="default";
    this.isSelectedItem = false;
}

PopupMenu.prototype.itemOnclickHandler = function(e) {
    for( i = 0; i < this.spanList.length; i++) {
        span = this.spanList[i];
        if (span.isSelectedItem) {
            this.onSelect(span.firstChild.nodeValue);
        }
    }
    this.closePopup();
}
