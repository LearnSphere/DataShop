//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 12488 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-08-04 16:30:36 -0400 (Tue, 04 Aug 2015) $
// $KeyWordsOff: $
//
// Javascript initialization and custom needs for the Performance Profiler report.


/** AJAX request to update the performance profiler content. */
function updateProfiler() {
    $('performanceProfiler').innerHTML="<h1>Loading...</h1>"
    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "PerformanceProfiler.updateProfiler",
            datasetId: dataset,
            getPerformanceProfiler: "true"
        },
        onComplete: displayProfiler,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** Update the content */
function displayProfiler(transport) {
	var erContentDiv = $('performanceProfiler');
	erContentDiv.innerHTML=transport.responseText;
	document.body.style.cursor="default";
	//For QA testing.
	loadedForQA("true");
	//updateOptions();
	initPpLimitKeyHandler();
}

function updateSortImage() {
    var ppSortAscending = $("ppSortAscending");
    if (ppSortAscending.src.indexOf("up.gif") > 0) {
        ppSortAscending.src = "images/grid/down.gif"
        ppSortAscending.title = "Descending";
    } else {
        ppSortAscending.src = "images/grid/up.gif"
        ppSortAscending.title = "Ascending";
    }
    updateContent();
}
function getSortDirection() {
    var ppSortAscending = $("ppSortAscending");
    if (ppSortAscending.src.indexOf("down.gif") > 0) {
        return "false";
    }
	return "true";
}

/** Gets the current values from the "viewBy" options and makes a call to update the content. */
function updateContent() {

    var ppViewByType = $("ppPerformanceMetric");
    var ppViewByCategory = $("ppViewBy");
    
    var ppSortBy = $("ppSortBy");
    var ppDisplayPredicted = $("ppDisplayPredicted");
    var ppTopLimit = $("ppTopLimit");
    var ppBottomLimit = $("ppBottomLimit");
    var ppDisplayUnmapped = $("ppDisplayUnmapped");
    

    var postParams = {
    	requestingMethod: "PerformanceProfiler.updateContent",
        datasetId: dataset,
        getPerformanceProfiler: "true",
        
        ppViewByType: ppViewByType.options[ppViewByType.selectedIndex].value,
        ppViewByCategory: ppViewByCategory.options[ppViewByCategory.selectedIndex].value,
        
        ppSortBy: ppSortBy.options[ppSortBy.selectedIndex].value,
        ppSortAscending: getSortDirection(),
        minStudents: $F("minStudents"),
        minProblems:$F("minProblems"),
        minSkills: $F("minSkills"),
        minSteps: $F("minSteps")
    }

    if (ppTopLimit) {
        postParams['ppTopLimit'] = ppTopLimit.value;
    }
    if (ppBottomLimit) {
    	postParams ['ppBottomLimit'] = ppBottomLimit.value;
    }
    if (ppDisplayPredicted) {
    	postParams['ppDisplayPredicted'] = ppDisplayPredicted.checked;
    }

    modelList.displaySecondary();
    
    if (ppDisplayUnmapped) {
    	postParams['ppDisplayUnmapped'] = ppDisplayUnmapped.checked;
    }

    var contentDiv = $('performanceProfiler');
    contentDiv.innerHTML="<h1>Loading... </h1>";
    document.body.style.cursor="wait";
	
    new Ajax.Request(window.location.href, {
        parameters: postParams,
        onComplete: displayProfiler,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function updateOptions() {
    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "PerformanceProfiler.updateOptions",
            datasetId: dataset,
        	getProfilerOptions: "true"},
        onComplete: displayProfilerOptions,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** Updates the side-bar options by display selections*/
function displayProfilerOptions(transport) {
	var ppNavDiv = $('pp_nav');
	ppNavDiv.innerHTML=transport.responseText;
	
	//For QA testing.
	loadedForQA("true");
}

function initPPChangeHandlers() {
    $("ppPerformanceMetric").observe('change', updateContent);
    $("ppViewBy").observe('change', updateContent);
    
    $("ppSortBy").observe('change', updateContent);
    $("ppDisplayPredicted").observe('click', updateContent);
    $("ppSortAscending").observe('click', updateSortImage);
    $("ppDisplayUnmapped").observe('click', updateContent);

    $("minStudentsClear").observe('click', function() { $("minStudents").value = ""; });
    $("minStepsClear").observe('click', function() { $("minSteps").value = ""; });
    $("minProblemsClear").observe('click', function() { $("minProblems").value = ""; });
    $("minSkillsClear").observe('click', function() { $("minSkills").value = ""; });

    $("minSkills", "minSteps", "minStudents", "minProblems").each(
        function (item) { item.observe('keyup', ppLimitKeyHandler) });    
}

function initPpLimitKeyHandler() {
    ppTopLimitInput = $("ppTopLimit")
    if (ppTopLimitInput) {
        ppTopLimitInput.onkeyup=ppLimitKeyHandler.bindAsEventListener(ppTopLimitInput);
    }

    ppBottomLimitInput = $("ppBottomLimit");
    if (ppBottomLimitInput) {
        ppBottomLimitInput.onkeyup=ppLimitKeyHandler.bindAsEventListener(ppBottomLimitInput);
    }
}

function ppLimitKeyHandler(e) {
    var code;

    if (!e) var e = window.event;
    if (e.keyCode) code = e.keyCode;
    else if (e.which) code = e.which;
	  
    if (this.value.length > 0) {
        if (isNaN(this.value)) {
            errorPopup("'" + this.value + "' is not a number, please enter a number.");
            this.value="";
        } else if (!isNaN(this.value) && this.value < 0) {
            errorPopup("'" + this.value + "' must be greater to or equal to zero.");
            this.value="";
        } else if (code == 110 || code == 190) { //check for a "."
            errorPopup("'" + this.value + "' must be an integer.");
            this.value = this.value.substring(0, this.value.length-1);
        }
    }
    if (code == 13) { updateContent(); }
}

/**
 * Takes an array the is TYPE, Selected, then the options.
 * Parses this list and opens a new pop up menu.
 */
function ppDisplayDomainMenu(itemArray) {
    actualArray = new Array();
    //the list does not include the first two items.
    for (i = 2; i < itemArray.length; i++) {
        actualArray[i-2] = itemArray[i];
    }
    new PopupMenu("View By (Domain Selection)", "DomainSelection", actualArray,
    		mouse_x, mouse_y, updateDomainMenu, itemArray[1]);
}

function updateDomainMenu(optionSelect) { 	
    var erContentDiv = $('performanceProfiler');
	erContentDiv.innerHTML="<h1>Loading...</h1>"

    new Ajax.Request(window.location.href, {
        parameters: {
    	    requestingMethod: "PerformanceProfiler.updateDomainMenu",
            datasetId: dataset,
            getPerformanceProfiler: "true",
            ppViewByCategory: optionSelect
        },
        onComplete: displayProfiler,
        onException: function (request, exception) {
            throw(exception);
        }
    });
    
	//TBD Should we change the nav box here?
    var ppViewByCategory = $("ppViewBy");
    ppViewByCategory.value = optionSelect;
}

/**
 * Takes an array the is TYPE, Selected, then the options.
 * Parses this list and opens a new pop up menu.
 */
function ppDisplayRangeMenu(itemArray) {
    actualArray = new Array(); 
    //the list does not include the first two items.
    for (i = 2; i < itemArray.length; i++) {
        actualArray[i-2] = itemArray[i];
    }
    new PopupMenu("Performance Metric (Range Selection)", "RangeSelection", actualArray,
    		mouse_x, mouse_y, updateRangeMenu, itemArray[1]);
}

function updateRangeMenu(optionSelect) {     
	$('performanceProfiler').innerHTML="<h1>Loading...</h1>"

    new Ajax.Request(window.location.href, {
        parameters: {
    	    requestingMethod: "PerformanceProfiler.updateRangeMenu",
            datasetId: dataset,
            getPerformanceProfiler: "true",
            ppViewByType: optionSelect
        },
        onComplete: displayProfiler,
        onException: function (request, exception) {
            throw(exception);
        }
    });
    
	//TBD Should we change the nav box here?
    var ppViewByType = $("ppPerformanceMetric");
    ppViewByType.value = optionSelect;
}

function clearPpTopLimit() { 
    var ppTopLimit = $("ppTopLimit");
    ppTopLimit.value="";
    updateContent();
}

function clearPpBottomLimit() { 
    var ppBottomLimit = $("ppBottomLimit");
    ppBottomLimit.value="";
    updateContent();
}

/** Initialize all javascript items necessary for the ErrorReport */
function initPerformanceProfiler() {
    initDefaultNavigation();
    
    modelList.displaySecondary();
 
    new NavigationBox.Base("pp_nav");
    createManageKCSetsDialog();

    problemObserver.addListener(updateProfiler);
    skillObserver.addListener(updateProfiler);
    skillObserver.addListener(updateSetModifier);
    studentObserver.addListener(updateProfiler);

    //For QA testing.
    loadedForQA("true");
    updateProfiler();
}

onloadObserver.addListener(initPerformanceProfiler);
