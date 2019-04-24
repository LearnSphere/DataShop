//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 12814 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-12-08 10:39:54 -0500 (Tue, 08 Dec 2015) $
// $KeyWordsOff: $
//
function initDatasetSelection() {
	//init the dataset selector dropdown
	var theSelect = document.getElementById("dsSelectorSelect");
	if (theSelect!=null) {
		theSelect.onchange = selectChanged.bindAsEventListener(theSelect);
	}
 }

//Handles selection changes on the Dataset Selector combo box
function selectChanged(theElement) {
    var theSelect;
    theSelect = this;

    //Build the form.
    var newForm = document.createElement('FORM');
    newForm.setAttribute('name', 'ds_select_form');
    newForm.setAttribute('id', 'ds_select_form');
    newForm.setAttribute('form', 'text/plain');
    newForm.setAttribute('method', 'POST');

    var newInput = document.createElement('input');
    newInput.name="datasetId";
    newInput.type="hidden";
    newInput.value="?datasetId=" + theSelect.value;
    newForm.appendChild(newInput);
    document.getElementsByTagName('body').item(0).appendChild(newForm);
    
    if (theSelect.value == "Other..." || theSelect.value == "") {
        location.replace("index.jsp");
    } else {
        var windowLocation = window.location;
        var pathname = windowLocation.pathname;
        var host = windowLocation.protocol + "//" + windowLocation.host + pathname; // includes the port number for localhost testing
        currentURL = windowLocation.href;

        // if we are at /, index.jsp, login or help, change URL to go to DatasetInfo
        if ((currentURL.search("index.jsp") != -1)
                || (currentURL == host)
                || (pathname == "/") // not redundant if user on home screen makes ajax tab changes appending # 
                || (currentURL.search("help") != -1)
                || (currentURL.search("login") != -1) 
                || (currentURL.search("AccessRequests") != -1)
                || (currentURL.search("AccountProfile") != -1)
                || (currentURL.search("AdminDomainLearnLab") != -1)
                || (currentURL.search("ChangePassword") != -1)
                || (currentURL.search("CreateProject") != -1)
                || (currentURL.search("DiscourseInfo") != -1)
                || (currentURL.search("DiscourseExport") != -1)
                || (currentURL.search("ExternalTools") != -1)
                || (currentURL.search("ForgotPassword") != -1)
                || (currentURL.search("ImportQueue") != -1)
                || (currentURL.search("IRBReview") != -1)
                || (currentURL.search("LoggingActivity") != -1)
                || (currentURL.search("ManageTerms") != -1)
                || (currentURL.search("ManageUsers") != -1)
                || (currentURL.search("MetricsReport") != -1)
                || (currentURL.search("PasswordReset") != -1)
                || (currentURL.search("Project") != -1)
                || (currentURL.search("UploadDataset") != -1)
                || (currentURL.search("WebServicesCredentials") != -1)) {
            newURL = currentURL.substring(0, currentURL.lastIndexOf("/")+1);
            newURL += "DatasetInfo?datasetId=" + theSelect.value;
            newForm.setAttribute('action', newURL);
            newForm.submit();
        // otherwise, go to the same report with the new dataset
        } else {
            lastSlashPos = currentURL.lastIndexOf("/")+1;
            questionPos = currentURL.indexOf("?");
            if (questionPos == -1) {
                questionPos = currentURL.length;
            }
            newURL = currentURL.substring(0, currentURL.lastIndexOf("/")+1);
            newURL += currentURL.substring(lastSlashPos,questionPos);
            newURL += "?datasetId=" + theSelect.value;
            newForm.setAttribute('action', newURL);
            newForm.submit();
        }
    }
    return true;
}

//We don't use the next three functions currently
function selectClicked() {
	this.changed = true;
}

function selectFocussed() {
	this.initValue = this.value;
	return true;
}

function selectKeyed(e) {
	var theEvent;
	var keyCodeTab = "9";
	var keyCodeEnter = "13";
	var keyCodeEsc = "27";
	
	if (e) {
		theEvent = e;
	} else {
		theEvent = event;
	}

	if ((theEvent.keyCode == keyCodeEnter || theEvent.keyCode == keyCodeTab) && this.value != this.initValue) {
		this.changed = true;
		selectChanged(this);
	} else if (theEvent.keyCode == keyCodeEsc) {
		this.value = this.initValue;
	} else {
		this.changed = false;
	}

	return true;
}

onloadObserver.addListener(initDatasetSelection);