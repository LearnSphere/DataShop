//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 7245 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2011-11-09 10:12:24 -0500 (Wed, 09 Nov 2011) $
// $KeyWordsOff: $
//

/*************************************************
    Step Rollup Preview/Export Object
**************************************************/
function StepRollupPreview(aDiv, aUrl, theModels, options) {
    this.id       = aDiv.id;
    this.displayDiv  = aDiv;
    this.url      = aUrl;    
    
    if (theModels) {
        this.skillModelList = theModels;
    } else {
        this.skillModelList = new Array();
    }
    
    this.displaySkills = false;
    this.displayLFAScore = false;

    var browser = navigator.userAgent.toLowerCase();
    this.isIE        = browser.indexOf("msie") != -1;
    this.isOpera     = browser.indexOf("opera")!= -1;

    this.viewPort = null; //helper var for easy access to the viewport div.
    this.tableId = null; //helper id of the table that will be the live grid.
    this.numResults = null; //number of results for the current setup.

    this.ricoGrid = null;

    this.setOptions(options);
    this.initialize();  
}

StepRollupPreview.prototype.setOptions = function(options) {
      this.options = {
         numRows: 10,
         sizeMax: 1000
      };
      Object.extend(this.options, options || { });
}

StepRollupPreview.prototype.initialize= function() {
    
    if (document.getElementById('sri_displaySkills').checked == true) {
        this.displaySkills = true;
    } else {
        this.displaySkills = false;
    }
        
    if (document.getElementById('sri_displayLFAScore').checked == true) {
        this.displaySkills = true;
        this.displayLFAScore = true;
    } else {
        this.displayLFAScore = false;
    }

    new Ajax.Request(this.url, {
        parameters: { 
            	requestingMethod: "StepRollupPreview.initialize",
                datasetId: dataset,
        	getResultsSize: "true"},
        onComplete: this.proccessInitResults.bindAsEventListener(this),
        onException: function (request, exception) { throw(exception); }
    });
}

StepRollupPreview.prototype.getHeaders = function() {

    var header = null;
    
    if (this.displaySkills) {
        header = this.getDefaultHeader().concat(this.getSkillsHeader());
    }
   
    if (this.displayLFAScore) {
        if (header == null) {
            header = (defaultHeader.concat(this.getSkillsHeader())).concat(this.getLFAScoreHeader());
        } else {
            header = header.concat(this.getLFAScoreHeader());
        }
    }
    
    if (header == null) {
        header = this.getDefaultHeader();
    }
    
    return header;
}


StepRollupPreview.prototype.getDefaultHeader = function() {
    var defaultHeader = new Array(
        new Array('#', '3'),
        new Array('Sample', '8'),
        new Array('Anon Student Id', '8'),
        new Array('Problem Hierarchy', '8'),
        new Array('Problem', '8'),
        new Array('Problem View', '5'),
        new Array('Step Name', '8'),
        new Array('Step Time', '8'),
        new Array('First Attempt', '4'),
        new Array('Incorrects', '3'),
        new Array('Hints', '3'),
        new Array('Corrects', '3')
    );
    return defaultHeader;
}

StepRollupPreview.prototype.getSkillsHeader = function() {
    var skillsHeader = new Array(
        new Array('Knowledge Component(s)', '10'),
        new Array('Opp. #', '3')
    );
    return skillsHeader
}

StepRollupPreview.prototype.getLFAScoreHeader = function() {
    var lfaScoreHeader = new Array(
        new Array('Predicted Error Rate', '5')
    );
    return lfaScoreHeader;
}

StepRollupPreview.prototype.getLFANumbersHeader = function() {
    var lfaNumbersHeader = new Array(
        new Array('Student Intercept', '4'),
        new Array('Model Intercept', '4'),
        new Array('KC Intercept', '4'),
        new Array('KC Slope', '4')
    );
    return lfaNumbersHeader;
}

StepRollupPreview.prototype.getTableWidth = function() {
    numModels = 0;
    for (i = 0; i < this.skillModelList.length; i++) {
        if (SkillModelList[i] != "None") {
            numModels++;
        }
    }

    var width = 0;

    headerList = this.getDefaultHeader();
    for (i = 0; i < headerList.length; i++) {
        width += +(headerList[i][1]);
    }
    
    if (this.displaySkills) {
        headerList = this.getSkillsHeader();
        for (i = 0; i < headerList.length; i++) {
            width += +(headerList[i][1] * numModels);
        }
    }
    
    if (this.displayLFAScore) {
        headerList = this.getLFAScoreHeader();
        for (i = 0; i < headerList.length; i++) {
            width += +(headerList[i][1] * numModels);
        }
    }
    return width + "em";
}

StepRollupPreview.prototype.proccessInitResults = function(transport) {
    var results = transport.responseText;
    if (isNaN(results)) {
        this.displayMessage(results);
    } else {
        this.displayLiveGrid(results);
    }
    
}

StepRollupPreview.prototype.displayMessage= function(message) {
     
    var messageDiv = document.createElement("div");
    messageDiv.className="info shortinfo";
    var imageWrapperDiv = document.createElement("div");
    imageWrapperDiv.className="imagewrapper";
    var image = document.createElement("img");
    image.src = "images/info_32.gif";
    imageWrapperDiv.appendChild(image);
    messageDiv.appendChild(imageWrapperDiv);
    var para = document.createElement("p");
    para.appendChild(document.createTextNode(message));
    messageDiv.appendChild(para);
     
    this.displayDiv.appendChild(messageDiv);
}

StepRollupPreview.prototype.displayLiveGrid= function(num) {
    
    
    this.numResults = num;
    
    //alert(this.callingObj.tableId + " size: " + numResults + " :: " + this.callingObj.options.numRows);
    //set the # of results to at least 1.
    
    var displayRows = this.options.numRows;
    if (this.numResults < this.options.numRows) {
        displayRows = this.numResults;
    }
    
    this.createView(displayRows); //create the view and the blank table.

    if (this.numResults != 0) {
        rg = new Rico.LiveGrid(this.tableId, displayRows,
                this.numResults, this.url, null);
        rg.resetContents();
        rg.setRequestParams("blah=" + new Date());
        rg.fetchBuffer(0);
        rg.requestContentRefresh(0);
    }

    //For QA testing.
    loadedForQA("true");
}

StepRollupPreview.prototype.refresh= function(header){
    
    this.headers = header;
    
    //remove everything.
    while(this.displayDiv.firstChild != null) {
        this.displayDiv.removeChild(this.displayDiv.firstChild);
    }
    this.initialize();
}

StepRollupPreview.prototype.exportButtonHandler= function(){
    startExport();
}

StepRollupPreview.prototype.createHeaderTable= function() {

    var headerTable = document.createElement('table');
    headerTable.className="fixedTable";
    headerTable.id = "exportHeaderTable";
    //headerTable.style.border="1px solid black";
    headerTable.style.width = this.getTableWidth();
    
	var headerTableBody = document.createElement('tbody');
	headerTable.appendChild(headerTableBody);

    var headerRow = document.createElement('tr');
    var modelRow = document.createElement('tr');
    
    headerList = this.getDefaultHeader();
    for (i = 0; i < headerList.length; i++) {       
        headerCell = document.createElement('th');
        headerCell.appendChild(document.createTextNode(headerList[i][0]));
        //headerCell.className="cell";
        headerCell.style.width= headerList[i][1] + "em";       
        headerCell.style.minWidth=headerList[i][1] + "em";
        headerCell.style.borderRight="1px solid gray";
        headerCell.style.borderTop="1px solid gray";
        headerCell.style.borderBottom="1px solid gray";
        headerRow.appendChild(headerCell);

        modelCell = document.createElement('th');
        modelCell.style.width= headerList[i][1] + "em";       
        modelCell.style.minWidth=headerList[i][1] + "em";
        if (i == headerList.length - 1) {
            modelCell.style.borderRight="1px solid gray";
        } else {
            modelCell.style.borderRight="1px solid #cfdef2";
        }
        modelCell.style.borderTop="1px solid gray";
        modelCell.style.borderBottom="1px solid gray";
        modelRow.appendChild(modelCell);
    }

    for (i = 0; i < this.skillModelList.length; i++) {    
        currentModel = this.skillModelList[i];
        if (currentModel == "None") {
            continue;
        }
        if (currentModel) {
            //display skills
            if (this.displaySkills) {

                headerList = this.getSkillsHeader();
                for (j = 0; j < headerList.length; j++) {       
                    headerCell = document.createElement('th');
                    headerCell.appendChild(document.createTextNode(headerList[j][0]));
                    headerCell.style.width=headerList[j][1] + "em";
                    headerCell.style.borderRight="1px solid gray";
                    headerCell.style.borderTop="1px solid gray";
                    headerCell.style.borderBottom="1px solid gray";
                    headerCell.style.minWidth=headerList[j][1] + "em";
                    headerRow.appendChild(headerCell);

                    modelCell = document.createElement('th');
                    modelCell.style.width=headerList[j][1] + "em";
                    if (j == 0) {
                        modelCell.appendChild(document.createTextNode(this.skillModelList[i]));
                        modelCell.style.borderRight="1px solid #cfdef2";
                    } else if ( j == headerList.length -1 && !this.displayLFAScore) {
                        modelCell.style.borderRight="1px solid gray";
                    } else {                      
                        modelCell.style.borderRight="1px solid #cfdef2";
                    }
                    modelCell.style.borderTop="1px solid gray";
                    modelCell.style.borderBottom="1px solid gray";
                    modelCell.style.minWidth=headerList[j][1] + "em";
                    modelRow.appendChild(modelCell);                   
                }
                //modelCell.style.width = modelCellWidth + "em";
               //modelCell.colSpan = headerList.length;
            }
            
            //display the LFA Score
            if (this.displayLFAScore) {
                headerList = this.getLFAScoreHeader();
                for (j = 0; j < headerList.length; j++) {       
                    headerCell = document.createElement('th');
                    headerCell.appendChild(document.createTextNode(headerList[j][0]));
                    //header.className="cell";
                    headerCell.style.width=headerList[j][1] + "em";
                    headerCell.style.borderRight="1px solid gray";
                    headerCell.style.borderTop="1px solid gray";
                    headerCell.style.borderBottom="1px solid gray";
                    headerCell.style.minWidth=headerList[j][1] + "em";
                    headerRow.appendChild(headerCell);
                    
                    modelCell = document.createElement('th');
                    modelCell.style.width=headerList[j][1] + "em";
                    if ( j == headerList.length -1) {
                        modelCell.style.borderRight="1px solid gray";
                    } else {                      
                        modelCell.style.borderRight="1px solid #cfdef2";
                    }
                    modelCell.style.borderTop="1px solid gray";
                    modelCell.style.borderBottom="1px solid gray";
                    modelCell.style.minWidth=headerList[j][1] + "em";
                    modelRow.appendChild(modelCell);     
                    
                }
            }
        }
    }

    if (this.displaySkills) {
        headerTableBody.appendChild(modelRow);
    }
    headerTableBody.appendChild(headerRow);
    return headerTable;
}

StepRollupPreview.prototype.getStyledCell= function(aWidth, isEven) {
    //alert(aWidth);
    
    cell = document.createElement('td');
    cell.style.width=aWidth + "em";
    if (isEven) {
        cell.className="even";
    } else { 
        cell.className="";
    }
    return cell;
}

StepRollupPreview.prototype.createGridTable= function(numDisplayRows){
    var previewTable = document.createElement('table');
    previewTable.id = "exportPreviewTable";
    previewTable.className = "exportPreviewTable";

    this.tableId = previewTable.id;
    previewTable.style.width=this.getTableWidth();       
    //previewTable.style.background="red"; //useful for debugging

	var previewTableBody = document.createElement('tbody');
	previewTable.appendChild(previewTableBody);
        
    //create the header table.
    var headerTable = this.createHeaderTable();
    this.viewPort.appendChild(headerTable);

    
    //create an empty set of rows to be populated.
    var isEvenRow = true;
    //alert(numDisplayRows);
    for( i=0; i <= numDisplayRows+1; i++) {
        row = document.createElement('tr');
        headerList = this.getDefaultHeader();
        for (j = 0; j < headerList.length; j++) {       
            row.appendChild(this.getStyledCell(headerList[j][1], isEvenRow));
        }
    
        for (k = 0; k < this.skillModelList.length; k++) {
            currentModel = this.skillModelList[k];
            if (currentModel == "None") {
                continue;
            }
            if (currentModel) {

                //display skills
                if (this.displaySkills) {
                    headerList = this.getSkillsHeader();
                    for (j = 0; j < headerList.length; j++) {       
                        row.appendChild(this.getStyledCell(headerList[j][1], isEvenRow));
                    }
                }
                
                //display the LFA Score
                if (this.displayLFAScore) {
                    headerList = this.getLFAScoreHeader();
                    for (j = 0; j < headerList.length; j++) {       
                        row.appendChild(this.getStyledCell(headerList[j][1], isEvenRow));
                    }
                }
            }
        }
        previewTableBody.appendChild(row);
        if (isEvenRow) {
            isEvenRow = false;
        } else {
            isEvenRow = true;
        }
    }
    return previewTable;
}

StepRollupPreview.prototype.createView= function(displayRows){

    this.exportButton = document.createElement('input');
    this.exportButton.type="button";
    this.exportButton.value="Export Student-Step Rollup";
    this.exportButton.id="exportStepRollup";
    this.exportButton.onclick = this.exportButtonHandler.bindAsEventListener(this);
    this.displayDiv.appendChild(this.exportButton);

    var numResultsPara = document.createElement('p');
    numResultsPara.appendChild(document.createTextNode("Number of Results:" + this.numResults));
    this.displayDiv.appendChild(numResultsPara);

    this.viewPort = document.createElement('div');
    this.viewPort.id="viewPort";
    //this.viewPort.style.float="left";
    //this.viewPort.style.height="16em";
   // this.viewPort.style.border="1px solid black";
    this.viewPort.style.padding="0 1.5em 0 0";
    //this.viewPort.style.background="yellow";
    this.displayDiv.appendChild(this.viewPort);

    //build an empty table.
    this.viewPort.appendChild(this.createGridTable(displayRows));

    var clearDiv = document.createElement("div");
    clearDiv.style.clear="both";
    this.displayDiv.appendChild(clearDiv);
}
