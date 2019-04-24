//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 11781 $
// Last modified by: $Author: mkomisin $
// Last modified on: $Date: 2014-11-25 16:29:31 -0500 (Tue, 25 Nov 2014) $
// $KeyWordsOff: $
//

/********************
   PROGRESS BAR OBJECT
*********************/

function ProgressBar(onCompleteCallback, aUrl, options) {

    this.url = (aUrl) ? aUrl : window.location;
    this.onCompleteCallback = (onCompleteCallback) ? onCompleteCallback : false;

    this.div = null;
    this.percent = 0;
    this.refreshTime = 3000; //refresh time in MS

    this.fillerDiv;
    this.fillerText;

    this.options = {
        allowCancel: true,
        checkParam: "export_check",
        cancelParam: "export_cancel",
        explanationText: "Processing your request ...",
        onErrorFunction: null,
        s2d_sample: null,
        extraParams: {}
    }

    Object.extend(this.options, options || { });
    this.init();
}

/*
 * Initialize the ProgressBar Object.  If this is a tx_export request, make a call
 *    to the export servlet to determine if a cached export file is available.
 *    Otherwise, finish up the initialization.
 */
ProgressBar.prototype.init = function() {
    this.createView();
    this.setTiming();

}

/**
 * Responsible for pinging the server to determine the status of the request.<b>
 * Sends a 'check' parameter to the servlet, expecting a JSON object in return.
 */
ProgressBar.prototype.update = function() {
    var postParams = {};
    postParams[this.options.checkParam] = "true";
    postParams['requestingMethod'] = "ProgressBar.update";
    postParams['datasetId'] = dataset;
    if (this.options.s2dSelector == "true") {
        postParams['s2dSelector'] = "true"
    }


    if (!this.isUpdating) {
        this.isUpdating = true;
        new Ajax.Request(this.url, {
            requestHeaders: {Accept: 'application/json;charset=UTF-8'},
            parameters: Object.extend(postParams, this.options.extraParams || {}),
            onComplete: this.recordPercent.bindAsEventListener(this),
            onException: function (request, exception) { throw(exception); }
        });
    } else {
        this.setTiming();
    }
}

/**
 * Responsible for updating the % complete on the ProgressBar.  Expects a JSON
 * object containing a "status" and "message".<b>
 * @param transport the transport object returned from the Ajax call.
 */
ProgressBar.prototype.recordPercent = function(transport) {
    var json = transport.responseText.evalJSON(true);
    this.isUpdating = false;
    this.percent = +json.status;
    if (this.percent >= 0) {
        this.updateProgressMessage(json.message);
        this.displayPercent();
    } else {
        this.displayError(json.message);
    }
}

/**
 * Update the status message for a ProgressBar object.
 * @param message the message to display on the screen.
 */
ProgressBar.prototype.updateProgressMessage = function(message) {
    var messageArea = $('messageArea');
    if (messageArea) {
        messageArea.removeChild(messageArea.firstChild);
        messageArea.appendChild(document.createTextNode(message));
    } else {
        //FIXME should we do something here?
    }
}

/**
 * Draw the % complete area on the ProgressBar
 */
ProgressBar.prototype.displayPercent = function() {
    this.fillerDiv.style.width=this.percent * 3 + "px";

    if (this.fillerText.firstChild) {
        this.fillerText.removeChild(this.fillerText.firstChild);
    }

    this.fillerText.appendChild(document.createTextNode(this.percent + "%"));

    if (this.percent == 100) {
        this.finishAndClose();
        if (this.isTiming) {
            clearTimeout(this.isTiming);
            this.isTiming = false;
        }
    } else {
        this.setTiming();
    }
}

/**
 * Responsible for displaying an error message on the ProgressBar.
 *     % complete bar is colored red, error message is printed and a
 *     "close" button is enabled.
 */
ProgressBar.prototype.displayError = function(message) {
    if (this.isTiming) {
        clearTimeout(this.isTiming);
        this.isTiming = false;
    }

    if ((message == '') || (message === undefined)) {
        message = "An error occurred while processing your request.";
    }
    this.fillerDiv.style.width="300px";
    this.fillerDiv.style.backgroundColor="red";
    this.fillerDiv.style.height="20px"


    if (this.fillerText.firstChild) {
        this.fillerText.removeChild(this.fillerText.firstChild);
    }

    this.fillerText.appendChild(document.createTextNode("Error"));

    contentDiv = document.getElementById("progressBarExtra");
    if (contentDiv) {
        if (contentDiv.firstChild) {
           contentDiv.removeChild(contentDiv.firstChild);
        }

        para = document.createElement('p');
        para.appendChild(document.createTextNode(message));
        para.style.textAlign="center";

        para.appendChild(document.createElement('br'));
        para.appendChild(document.createElement('br'));
        this.button = document.createElement('input');
        this.button.type="submit";
        this.button.value = "Close";
        this.button.onclick=this.cancelExport.bindAsEventListener(this);
        para.appendChild(this.button);

        if (contentDiv.firstChild) {
            contentDiv.replaceChild(para, contentDiv.firstChild);
        } else {
            contentDiv.appendChild(para);
        }

    } else {
        this.closeAll();
    }

    if (this.options.onErrorFunction) {
        this.options.onErrorFunction.call();
    }
}

ProgressBar.prototype.closeAll = function() {
    mainDiv = $("progressBarWrapperDiv");
    if (mainDiv) {
        while (mainDiv.firstChild) {
            mainDiv.removeChild(mainDiv.firstChild);
        }
        mainDiv.parentNode.removeChild(mainDiv);
    }
    killMe(this);
}

ProgressBar.prototype.finishAndClose = function() {
    if (this.onCompleteCallback) { this.onCompleteCallback.call(); }
    setTimeout(this.closeAll.bindAsEventListener(this), 1000);
}

ProgressBar.prototype.setTiming = function() {
    if (this.isTiming) {
        clearTimeout(this.isTiming);
        this.isTiming = false;
    }
    this.isTiming = setTimeout(this.update.bindAsEventListener(this), this.refreshTime);
}


ProgressBar.prototype.cancelExport = function() {
    if (this.isTiming) {
        clearTimeout(this.isTiming);
        this.isTiming = false;
    }
    var postParams = {}
    postParams[this.options.cancelParam] = "true";
    postParams['requestingMethod'] = "ProgressBar.cancelExport";
    postParams['datasetId'] = dataset;
    new Ajax.Request(this.url, {
        parameters: Object.extend(postParams, this.options.extraParams || { }),
        onComplete: this.closeAll.bindAsEventListener(this),
        onException: function (request, exception) { throw(exception); }
    });
}

ProgressBar.prototype.createView = function() {
    //the following 2 divs are used for creating a centering div.
    var wrapperDiv = document.createElement('div');
    wrapperDiv.id = "progressBarWrapperDiv";

    var progressBarDiv = document.createElement('div');
    progressBarDiv.id = "progressBarDiv";
    wrapperDiv.appendChild(progressBarDiv);

    //create the progress bar.
    var progressBar = document.createElement('div');
    progressBar.id ="progressBar";
    progressBarDiv.appendChild(progressBar);

    //create the "filler" which will go up to the length of the progress bar.
    this.fillerDiv = document.createElement('div');
    this.fillerDiv.id = "progressBarFiller";
    progressBar.appendChild(this.fillerDiv);

    //create the text inside the progress bar.
    this.fillerText = document.createElement('p');
    this.fillerText.appendChild(document.createTextNode("0%"));
    progressBar.appendChild(this.fillerText);

    //create the extra content div.
    var contentDiv = document.createElement('div');
    contentDiv.id = "progressBarExtra";
    progressBarDiv.appendChild(contentDiv);

    var messageArea = document.createElement('p');
    messageArea.id = "messageArea";
    messageArea.appendChild(document.createTextNode(this.options.explanationText));

    contentDiv.appendChild(messageArea);

    para = document.createElement('p');
    para.style.textAlign="center";
    contentDiv.appendChild(messageArea);
    if (this.options.allowCancel) {
        this.button = document.createElement('input');
        this.button.type="submit";
        this.button.value="cancel";
        this.button.onclick=this.cancelExport.bindAsEventListener(this);
        para.appendChild(this.button);
    }

    contentDiv.appendChild(para);
    document.getElementsByTagName('body').item(0).appendChild(wrapperDiv);

    //set the padding to be equal to 200 + the amount scrolled so that the
    //progress bar always appears in the middle of the screen.
    var scrollFromTop = (document.documentElement.scrollTop) ?
                             document.documentElement.scrollTop :
                             document.body.scrollTop;
    var padding = scrollFromTop + 200;
    $("progressBarWrapperDiv").style.paddingTop= padding + "px";
}

function killMe(theProgressBar) {
    theProgressBar = false;
}