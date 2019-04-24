
var urlPattern =
    new RegExp('(http|ftp|https)://[a-z0-9\-_]+(\.[a-z0-9\-_]+)+([a-z0-9\-\.,@\?^=%&;:/~\+#]*[a-z0-9\-@\?^=%&;/~\+#])?', 'i');
var globalWfPaperCount = 0;

function getWorkflowPaperCount(workflowId) {

    new Ajax.Request('WorkflowAttributes', {
            parameters : {
                requestingMethod: 'WorkflowAttributesServlet.getWorkflowPaperCount',
                workflowId : workflowId
            },
            onComplete : function(transport) {
                json = transport.responseText.evalJSON(true);

                if (json.paperCount !== undefined && json.paperCount != null) {
                    workflowPaperCount = json.workflowPaperCount;

                    if (currentDigraph.isView) {
                        if (workflowPaperCount > 0) {
                            var wfExecutionDiv = jQuery('#wfExecutionDiv');
                            addExecutionButton(wfExecutionDiv, "Papers", "showPapers", "ui-icon-wf-paper-button");
                            jQuery('#showPapers').bind('click', function(e) {
                                openLinkPaperDialog();
                            });
                        }
                    }
                }
            },
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
    });
}


function generateAttachableDatasetSelect(projects, matchingDatasets) {


    var datasetList = [];
    if (jQuery('#' + currentDatasetOption).length > 0 && jQuery('#' + currentDatasetOption).val().length > 0) {
        var multiPattern = new RegExp(".+,.+","gi");
        var digitPattern = new RegExp("[0-9]+");
        if (jQuery('#' + currentDatasetOption).val().match(multiPattern)) {
            datasetList = jQuery('#' + currentDatasetOption).val().split(",");
        } else if (jQuery('#' + currentDatasetOption).val().match(digitPattern)) {
            datasetList.push(jQuery('#' + currentDatasetOption).val());
        }
    }
    var projectAdded = false;
    var datasetAdded = false;
    var fileOptions = '';
    for (var projectCount = 0; projectCount < projects.length; projectCount++) {
        var project = projects[projectCount];

        projectAdded = false;
        for (var datasetCount = 0; datasetCount < matchingDatasets.length; datasetCount++) {
            var dataset = matchingDatasets[datasetCount];

            if (dataset.projectId == project.id) {
                if (!projectAdded) {
                    fileOptions = fileOptions
                        + '<optgroup title="'
                        + project.projectName + '"label="Project: ' + project.projectName + '">';
                    projectAdded = true;
                }

                var dsLabel = 'Dataset: ' + dataset.datasetName + ' (id=' + dataset.id + ') \n' +
                'Start Time: ' + dataset.startTime + ' \n' +
                'End Time: ' + dataset.endTime + ' \n' +
                'Last Modified: ' + dataset.dataLastModified;

                var isOptSelected = '';
                if (!jQuery.isArray(datasetList)) {
                    datasetList = [ datasetList ];
                }
                for (var i = 0; i < datasetList.length; i++) {
                       if (datasetList[i] == dataset.id) {
                           isOptSelected = ' selected="selected"';
                       }
                }

                if (dataset.datasetName.length < maxFileNameLength) {
                    fileOptions = fileOptions + '<option  title="'
                    + dsLabel + '" value="' + dataset.id + '" ' + isOptSelected + '>'
                        + dataset.datasetName + '</option>';
                } else {
                    var subLen = dataset.datasetName.length;
                    fileOptions = fileOptions + '<option title="'
                    + dsLabel + '" value="' + dataset.id + '" ' + isOptSelected + '>'
                        + dataset.datasetName.substr(0, 40) + '...'
                        + dataset.datasetName.substr(dataset.datasetName.length - 15, dataset.datasetName.length)
                        + '</option>';
                }
            }
        }
        if (matchingDatasets.length > 0) {

            fileOptions = fileOptions + '</optgroup>';
        }
    }

    return fileOptions;
}


function generateAttachablePaperSelect(matchingPapers, workflowId) {

    var paperOptions = '';

    for (var paperCount = 0; paperCount < matchingPapers.length; paperCount++) {
        var paper = matchingPapers[paperCount];
        if (paper != null && paper.title !== undefined && paper.paperId !== undefined
                && paper.workflowId !== undefined && paper.workflowId != workflowId) {
            var dsLabel = 'Paper: ' + paper.title + ' (id=' + paper.paperId + ') \n';
            var isOptSelected = '';

            var paperLabel = '';
            if (paper.url !== undefined && paper.url != null) {
                paperLabel = ' (<b>url:</b> ' + paper.url + ')';
            } else {
                paperLabel = ' (' + paper.fileName + ')';
            }
            var ptitle = paper.title + '' +  paperLabel;

            if (ptitle.length < maxFileNameLength) {
                paperOptions = paperOptions + '<option  title="'
                + dsLabel + '" value="' + paper.paperId + '" ' + isOptSelected + '>'
                    + ptitle + '</option>';
            } else {
                var subLen = ptitle.length;
                paperOptions = paperOptions + '<option title="'
                + dsLabel + '" value="' + paper.paperId + '" ' + isOptSelected + '>'
                    + ptitle.substr(0, 40) + '...'
                    + ptitle.substr(ptitle.length - 15, ptitle.length)
                    + '</option>';
            }
        }
    }


    return paperOptions;
}


function generateAttachedPaperDiv(matchingPapers, workflowId) {

    var paperDiv = '<div id="existingLinksDiv">';
    var toggleOddRow = false;
    for (var paperCount = 0; paperCount < matchingPapers.length; paperCount++) {
        var paper = matchingPapers[paperCount];
        if (paper != null && paper.title !== undefined && paper.paperId !== undefined
                && paper.workflowId !== undefined && (paper.workflowId == workflowId || workflowId == null)) {

            var wfPaperUrl = '';
            if (paper.url !== undefined && paper.url != null) {
                wfPaperUrl = paper.url;
            } else {
                wfPaperUrl = 'WorkflowAttributes?paperId=' + paper.paperId;
            }

            var paperLabel = '';
            if (paper.url !== undefined && paper.url != null) {
                paperLabel = ' (<b>url:</b> ' + paper.url + ')';
            } else {
                paperLabel = ' (' + paper.fileName + ')';
            }

            var pTitle = paper.title + '' + paperLabel;

            var datePublishedStr = '';
            if (paper.publishDate != null) {
                datePublishedStr = '<br/><span class="wfPaperAttrib">'
                    + formatPaperDate(paper.publishDate) + '</span>';
            }

            var abstractAndCitation = '';
            if (currentDigraph.isView || workflowId == null) {
                if (paper.citation != null && paper.citation.trim() != '') {
                    abstractAndCitation = abstractAndCitation
                        + '<br/><span class="wfPaperAttrib">'
                        + paper.citation + '</span>';
                }
                if (paper.paperAbstract != null && paper.paperAbstract.trim() != '') {
                    abstractAndCitation = abstractAndCitation
                        + '<br/><span class="wfPaperAttrib">Abstract: '
                        + paper.paperAbstract + '</span>';
                }
            }

            var oddRowStr = '';
            if (toggleOddRow) {
                oddRowStr = ' existingWfPaperLinksOddRow'
            }
            toggleOddRow = !toggleOddRow;

            var newTitle = null;
            if (pTitle.length < maxFileNameLength) {
                newTitle = pTitle;
            } else {
                newTitle = pTitle.substr(0, 40) + '...'
                + pTitle.substr(pTitle.length - 15, pTitle.length);
            }

            paperDiv = paperDiv + '<span class="existingWfPaperLinks' + oddRowStr + '">'
            + '<a class="wfPaperLink" title="Open in New Window" target="_blank" href="' + wfPaperUrl + '">'
            + newTitle + '</a> '
            + '<a href="javascript:editPaper(' + paper.paperId + ')"><img class="editBtn" src="images/edit.gif" '
            + 'alt="edit" title="Edit" /></a>';
            if (workflowId == null) {
                paperDiv = paperDiv + '<a href="javascript:deletePaper(' + paper.paperId
                    + ')"><img class="unlinkBtn" src="images/delete.gif" '
                    + 'alt="unlink" title="Delete" /></a>';
            } else {
                paperDiv = paperDiv + '<a href="javascript:unlinkPaper(' + paper.paperId
                + ')"><img class="unlinkBtn" src="images/close.gif" '
                + 'alt="unlink" title="Unlink" /></a>';
            }
            paperDiv = paperDiv + datePublishedStr
            + abstractAndCitation + '</span>';
        }
    }
    paperDiv = paperDiv + '</div>';

    return paperDiv;
}



/** Link Button functions in the Workflow editor. */

function openLinkDatasetDialog() {
    populateDatasetLinkDialog(currentDigraph.id);
}

function openLinkPaperDialog() {
    populatePaperLinkDialog(currentDigraph.id);
}

function showLinkMenu() {
    jQuery('#linkWorkflowButton').off('click');

    jQuery('#linkWorkflowButton').bind('click', function(e) {

        if (jQuery('.linkMenu').length > 0) {
            jQuery('.linkMenu').remove();
        }

        var compSelectMenuHtml = '<span class="noSelect linkMenu">';

        compSelectMenuHtml = compSelectMenuHtml
            + '<a class="linkMenuObject" id="linkDatasetButton" href="javascript:openLinkDatasetDialog();">Link Dataset</a>'
            + '<a class="linkMenuObject" id="linkPaperButton" href="javascript:openLinkPaperDialog();">Link Paper</a>';

        compSelectMenuHtml = compSelectMenuHtml + '</span>';
        jQuery(compSelectMenuHtml).appendTo('body');

        var divPosition = jQuery('#linkWorkflowButton').position();
        var headerHeight = 0;
        if (jQuery('body > #ls-header').length > 0) {
            headerHeight = jQuery('body > #ls-header').outerHeight();
        }
        var startOfComponentX = jQuery('#process-selector-div').width();
        var pageX = parseFloat(divPosition.left) + parseFloat(startOfComponentX) + 20;
        var pageY = parseFloat(divPosition.top) + parseFloat(headerHeight) + 20;

        expandLinkMenu(pageX, pageY, e);

        jQuery('body').click(function(evt) {
            if (jQuery(evt.target).parent().attr('id') != "linkWorkflowButton") {
                jQuery('.linkMenu').slideUp(0);
            }
        });

    });

}


function expandLinkMenu(pageX, pageY, e) {

    // Hide open "share link" span if one exists
    jQuery('.lsShareLink').slideUp(0);

    jQuery('.linkMenu').slideUp(0);

    jQuery(".wfCompSelectList a").dequeue().slideUp(0);
//alert(jQuery(this).offset().left);
    jQuery('.linkMenu').css('left', (pageX) + 'px');
    jQuery('.linkMenu').css('top', (pageY) + 'px');

    jQuery(".linkMenu").slideDown(300);

}




/** Link dataset functions. */

function populateDatasetLinkDialog(workflowId) {

    new Ajax.Request('WorkflowEditor', {
            parameters : {
                requestingMethod: 'WorkflowEditorServlet.populateDatasetLinkDialog',
                workflowId : workflowId
            },
            onComplete : populateDatasetLinkDialogCompleted,
            beforeSend : wfShowStatusIndicatorModal(false, "Processing..."),
            onSuccess : wfHideStatusIndicator,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
    });
}



function populateDatasetLinkDialogCompleted(transport) {
    json = transport.responseText.evalJSON(true);

    myProjectArray = null;
    if (json.myProjectArray !== undefined && json.myProjectArray != null) {
        if (!jQuery.isArray(json.myProjectArray)) {
            json.myProjectArray = [ json.myProjectArray ];
        }
        myProjectArray = json.myProjectArray;
    }

    myDatasetArray = null;
    if (json.myDatasetArray !== undefined && json.myDatasetArray != null) {
        if (!jQuery.isArray(json.myDatasetArray)) {
            json.myDatasetArray = [ json.myDatasetArray ];
        }
        myDatasetArray = json.myDatasetArray;
    }

    var datasetFormId = 'datasetForm_' + currentDigraph.id;
    var innerHtml = '<div class="componentOptionsHeader">Select dataset(s) to have a reference to this workflow:</div>'
        + '<div class="wfSelList" id="wfSelList_' + currentDigraph.id + '">'
        + '<table class="selectDatasetTable">'
        + '<col width="20%"><col width="78%">';

    if (currentDigraph.isView != true) {
        if (myProjectArray !== undefined && myProjectArray != null && myProjectArray.length > 0
            && myDatasetArray !== undefined && myDatasetArray != null && myDatasetArray.length > 0) {

            var attachDatasetOptions =
                generateAttachableDatasetSelect(myProjectArray, myDatasetArray);

            var disabledStr = "";

            innerHtml = innerHtml + '<tr class="wfSelFilter"><td>Filter Datasets</td><td>'
                + '<input ' + disabledStr
                + ' type="text" class="datasetFilter" maxlength="100" size="40"'
                + ' title="Filter by Dataset or Project">'
                + '</td></tr>';

            innerHtml = innerHtml
                + '<tr class="wfSelFilter"><td colspan="2" >'
                + '<select multiple ' + disabledStr + ' id="wfDatasetSelect_' + currentDigraph.id
                    + '" class="existingDataset" name="wfDatasetSelect" >'
                + attachDatasetOptions
                + '</select>'
                + '</td></tr>';
        } else {

            innerHtml = innerHtml + '<tr class="wfSelFilter"><td></td><td>'
                + 'You do not have <a href="help?page=requesting-access" target="_blank">edit access</a> to any Projects.</td>';
        }
    }
    innerHtml = innerHtml + '</table></div>';

    if (jQuery('#wfSelectDatasetsDialog').length > 0) {
        jQuery('#wfSelectDatasetsDialog').dialog('close');
    }

    var dialogHeight = jQuery(document).height() * 0.7;
    var dialogWidth = jQuery(document).width() * 0.7;

    jQuery('<div />', {
         id : 'wfSelectDatasetsDialog'
    }).html(innerHtml).dialog({
         open : function() {
             jQuery('.ui-button').focus();
             jQuery('.existingDataset').on('change');
             jQuery(".existingDataset").filterDatasetsByText(jQuery('.datasetFilter'));
             jQuery(".datasetFilter").hint("auto-hint");
             theSelect = jQuery('#wfDatasetSelect_' + currentDigraph.id);
             jQuery(theSelect).children().each(function() {
                     jQuery(this).dblclick(function() {
                             linkDataset();
                             jQuery('#wfSelectDatasetsDialog').dialog('close');
                         });
                 });

         },
         autoOpen : true,
         autoResize : true,
         resizable : true,
         width : dialogWidth,
         height : dialogHeight,
         modal : true,
         title : 'Link workflow to dataset',
         buttons : {
             'OK' : function() {
                 linkDataset();
                 jQuery(this).dialog('close');
             },
             'Cancel' : function() {
                 jQuery(this).dialog('close');
             }
         },
         close : function() {
             jQuery(this).remove();
         }
     });
}

function linkDataset() {
    var datasetIds = "";
    var theSelect = jQuery('#wfDatasetSelect_' + currentDigraph.id);
    theSelect.find('option:selected').each(function(){
            if (datasetIds.length > 0) { datasetIds += ","; }
            datasetIds += jQuery(this).val();
        });

    new Ajax.Request('WorkflowAttributes', {
            parameters : {
                requestingMethod: 'WorkflowAttributesServlet.linkDataset',
                workflowId : currentDigraph.id,
                datasetIds : datasetIds
            },
            onComplete : linkDatasetCompleted,
            beforeSend : wfShowStatusIndicatorModal(false, "Processing..."),
            onSuccess : wfHideStatusIndicator,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
    });

}

function linkDatasetCompleted(transport) {

    json = transport.responseText.evalJSON(true);

    // Update global 'wfDatasets' var.
    updateWfDatasets(json);

    var innerHtml = "";
    if (wfDatasets.size() > 0) {
        innerHtml = '<a href="javascript:openWfDatasetsDialog(' + workflowId + ')">datasets</a>';
    }

    jQuery('#wf_datasets_link').html(innerHtml);
}

function unlinkDataset(datasetId) {

    new Ajax.Request('WorkflowAttributes', {
            parameters : {
                requestingMethod: 'WorkflowAttributesServlet.unlinkDataset',
                workflowId : currentDigraph.id,
                datasetId : datasetId
            },
            onComplete : unlinkDatasetCompleted,
            beforeSend : wfShowStatusIndicatorModal(false, "Processing..."),
            onSuccess : wfHideStatusIndicator,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
    });
}

function unlinkDatasetCompleted(transport) {

    json = transport.responseText.evalJSON(true);

    // Update global 'wfDatasets' var.
    updateWfDatasets(json);

    var innerHtml = "";
    if (wfDatasets.size() > 0) {
        innerHtml = '<a href="javascript:openWfDatasetsDialog(' + workflowId + ')">datasets</a>';
    }

    jQuery('#wf_datasets_link').html(innerHtml);

    if (wfDatasets.size() > 0) {
        openWfDatasetsDialog(workflowId);
    } else {
        jQuery('#wfDatasetsDialog').dialog('close');
    }
}


function openWfDatasetsDialog(wfId) {

    if (jQuery('#wfDatasetsDialog').length > 0) { jQuery('#wfDatasetsDialog').dialog('close'); }

    var dialogHtml = '<table id="wfDatasetsList">';
    jQuery.each(wfDatasets, function(index) {
            var ds = wfDatasets[index];
            dialogHtml += '<tr class="wfDataset">';
            dialogHtml += '<td><a target="_blank" href="DatasetInfo?datasetId=' + ds.id + '">' + ds.name + '</a></td>';
            dialogHtml += '<td class="wfUnlink"><img src="images/cross.png" onclick="javascript:unlinkDataset(' + ds.id + ')" /></td>';
            dialogHtml += '</tr>';
        });
    dialogHtml += "</table>";

    jQuery('<div />', {
         id : 'wfDatasetsDialog'
    }).html('Datasets which have a reference to this workflow:' + dialogHtml).dialog({
         open : function() {
             jQuery('.ui-button').focus();
         },
         autoOpen : true,
         autoResize : true,
         resizable : true,
         width : 400,
         height : 300,
         modal : true,
         title : 'Datasets Links',
         buttons : {
             'OK' : function() {
                 jQuery(this).dialog('close');
             }
         },
         close : function() {
             jQuery(this).remove();
         }
     });
}


/** Link paper functions. */

function updateMyPaperArray(workflowId) {

    new Ajax.Request('WorkflowAttributes', {
            parameters : {
                requestingMethod: 'WorkflowAttributesServlet.populatePaperLinkDialog',
                workflowId : workflowId
            },
            onComplete : updateMyPaperArrayCompleted,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
    });
}

function updateMyPaperArrayCompleted(transport) {
    json = transport.responseText.evalJSON(true);

    if (json.myPaperArray !== undefined && json.myPaperArray != null) {
        if (!jQuery.isArray(json.myPaperArray)) {
            json.myPaperArray = [ json.myPaperArray ];
        }
        myPaperArray = json.myPaperArray;
    }
    var isWfPaperExistingDisabled = 'wfButtonDisabled ';
    if (myPaperArray !== undefined && myPaperArray != null
            && myPaperArray.length > 0) {
        isWfPaperExistingDisabled = '';
    }

    var paperBtnText = 'Linked Paper(s)';
    if (!currentDigraph.isView) {
        paperBtnText = 'Link Paper(s)';
    }
    var innerHtml = '<div class="lhMenu">'
            + '<div class="' + isWfPaperExistingDisabled + 'wfPaperBtn" id="wfPaperExisting">' + paperBtnText + '</div>';

    if (!currentDigraph.isView) {
        innerHtml = innerHtml
        + '<div class="' + isWfPaperExistingDisabled + 'wfPaperBtn" id="wfPaperEdit">My Papers</div>'
        + '<div class="wfPaperBtn" id="wfPaperUpload">Upload Paper</div>'
            + '<div class="wfPaperBtn" id="wfPaperUrl">Submit URL</div>';
    }

    innerHtml = innerHtml + '</div>'
        + '<div class="rhDiv">'
            + '<div id="myPapersDiv"><span id="wfPaperMessage"></span></div>'
        + '</div>';


    if (myPaperArray !== undefined && myPaperArray != null
           && myPaperArray.length > 0) {
        // Owner or non-owner can see papers (owner gets unattached papers, too)
        jQuery('#myPapersDiv').html(getPaperExisting(myPaperArray));

        jQuery('#wfPaperExisting').addClass('wfPaperBtnSelected');
        jQuery('#wfPaperExisting').removeClass('wfButtonDisabled');
        jQuery('#wfPaperEdit').removeClass('wfButtonDisabled');

        if (currentDigraph.isView != true) {
            // Only owner needs select box for unattached papers.
            jQuery('#wfPaperUpload').removeClass('wfPaperBtnSelected');
            jQuery('#wfPaperUrl').removeClass('wfPaperBtnSelected');
            jQuery('#wfPaperEdit').removeClass('wfPaperBtnSelected');

            jQuery('.existingPaper').on('change');
            jQuery(".existingPaper").filterPapersByText(jQuery('.paperFilter'));


            theSelect = jQuery('#wfPaperSelect_' + currentDigraph.id);
            jQuery(theSelect).children().each(function() {
                jQuery(this).dblclick(function() {
                    linkPaper();
                });
            });

            setAttachButtonListening();
        } else {
            jQuery('.unlinkBtn').each(function() { jQuery(this).remove();  });
            jQuery('.editBtn').each(function() { jQuery(this).remove();  });
        }
    } else {

        jQuery('#wfPaperMessage').html('<i>Please choose an option.</i>');
        jQuery('#wfPaperExisting').addClass('wfButtonDisabled');
        jQuery('#wfPaperExisting').removeClass('wfPaperBtnSelected');
        jQuery('#wfPaperEdit').addClass('wfButtonDisabled');
        jQuery('#wfPaperEdit').removeClass('wfPaperBtnSelected');
        //mckc
    }

}


function populatePaperLinkDialog(workflowId) {

    new Ajax.Request('WorkflowAttributes', {
            parameters : {
                requestingMethod: 'WorkflowAttributesServlet.populatePaperLinkDialog',
                workflowId : workflowId
            },
            onComplete : populatePaperLinkDialogCompleted,
            beforeSend : wfShowStatusIndicatorModal(false, "Fetching..."),
            onSuccess : wfHideStatusIndicator,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
    });
}



function populatePaperLinkDialogCompleted(transport) {
    json = transport.responseText.evalJSON(true);

    if (json.myPaperArray !== undefined && json.myPaperArray != null) {
        if (!jQuery.isArray(json.myPaperArray)) {
            json.myPaperArray = [ json.myPaperArray ];
        }
        myPaperArray = json.myPaperArray;
    }
    var isWfPaperExistingDisabled = 'wfButtonDisabled ';
    if (myPaperArray !== undefined && myPaperArray != null
            && myPaperArray.length > 0) {
        isWfPaperExistingDisabled = '';
    }

    var innerHtml = '<div class="lhMenu">';
    if (!currentDigraph.isView) {
        innerHtml = innerHtml
            + '<div class="' + isWfPaperExistingDisabled + 'wfPaperBtn" id="wfPaperExisting">Link Paper(s)</div>';
    }


    if (!currentDigraph.isView) {
        innerHtml = innerHtml
        + '<div class="' + isWfPaperExistingDisabled + 'wfPaperBtn" id="wfPaperEdit">My Papers</div>'
        + '<div class="wfPaperBtn" id="wfPaperUpload">Upload Paper</div>'
            + '<div class="wfPaperBtn" id="wfPaperUrl">Submit URL</div>';
    }

    innerHtml = innerHtml + '</div>'
        + '<div class="rhDiv">'
            + '<div id="myPapersDiv"><span id="wfPaperMessage"></span></div>'
        + '</div>';


    if (jQuery('#wfSelectPapersDialog').length > 0) {
        jQuery('#wfSelectPapersDialog').dialog('close');
    }

    var dialogHeight = jQuery(document).height() * 0.88;
    var dialogWidth = jQuery(document).width() * 0.88;
    var dialogTitle = 'Papers';

    jQuery('<div />', {
         id : 'wfSelectPapersDialog'
    }).html(innerHtml).dialog({
         open : function() {
             jQuery('.ui-button').focus();

             jQuery('#wfPaperExisting').click(function() {
                 updateMyPaperArray(currentDigraph.id);
             });

             updateMyPaperArray(currentDigraph.id);

             if (currentDigraph.isView) {
                 jQuery('.lhMenu').css('width', '1vw');
                 jQuery('.rhDiv').css('width', '90%');
             }

             jQuery('#wfPaperUpload').click(function() {
                 jQuery('#myPapersDiv').html(getPaperUploadDiv());
                 jQuery('#wfPaperPublishDate').datepicker({
                     onSelect: function(selectedDate) {
                         updatePaperDate(this);
                     }
                 });
                 setSubmitButtonListening(false);


                 jQuery('#wfPaperUploadBtn').change(paperUpload);
                 jQuery('#wfPaperUpload').addClass('wfPaperBtnSelected');
                 jQuery('#wfPaperExisting').removeClass('wfPaperBtnSelected');
                 jQuery('#wfPaperUrl').removeClass('wfPaperBtnSelected');
                 jQuery('#wfPaperEdit').removeClass('wfPaperBtnSelected');
             });

             jQuery('#wfPaperUrl').click(function() {
                 jQuery('#myPapersDiv').html(getPaperUrlDiv());
                 jQuery('#wfPaperPublishDate').datepicker({
                     onSelect: function(selectedDate) {
                         updatePaperDate(this);
                     }
                 });
                 jQuery('#wfPaperLinkText').hint('auto-hint');
                 setSubmitButtonListening(true);
                 jQuery('#wfPaperUrl').addClass('wfPaperBtnSelected');
                 jQuery('#wfPaperExisting').removeClass('wfPaperBtnSelected');
                 jQuery('#wfPaperEdit').removeClass('wfPaperBtnSelected');
                 jQuery('#wfPaperUpload').removeClass('wfPaperBtnSelected');
             });

             jQuery('#wfPaperEdit').click(function() {

                 editMyPaperArray(currentDigraph.id);

                 jQuery('#wfPaperEdit').addClass('wfPaperBtnSelected');
                 jQuery('#wfPaperExisting').removeClass('wfPaperBtnSelected');
                 jQuery('#wfPaperUrl').removeClass('wfPaperBtnSelected');
                 jQuery('#wfPaperUpload').removeClass('wfPaperBtnSelected');
             });

         },
         autoOpen : true,
         autoResize : true,
         resizable : true,
         width : dialogWidth,
         height : dialogHeight,
         modal : true,
         title : dialogTitle,
         buttons : {

             'Close' : function() {
                 jQuery(this).dialog('close');
             }
         },
         close : function() {
             jQuery(this).remove();
         }
     });
}


function editMyPaperArray(workflowId) {

    new Ajax.Request('WorkflowAttributes', {
            parameters : {
                requestingMethod: 'WorkflowAttributesServlet.populatePaperLinkDialog',
                workflowId : workflowId
            },
            onComplete : editMyPaperArrayCompleted,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
    });
}

function editMyPaperArrayCompleted(transport) {
    json = transport.responseText.evalJSON(true);

    if (json.myPaperArray !== undefined && json.myPaperArray != null) {
        if (!jQuery.isArray(json.myPaperArray)) {
            json.myPaperArray = [ json.myPaperArray ];
        }
        myPaperArray = json.myPaperArray;
    }
    var isWfPaperExistingDisabled = 'wfButtonDisabled ';
    if (myPaperArray !== undefined && myPaperArray != null
            && myPaperArray.length > 0) {
        isWfPaperExistingDisabled = '';
    }
    var innerHtml = '<div class="lhMenu">'
            + '<div class="' + isWfPaperExistingDisabled + 'wfPaperBtn" id="wfPaperExisting">Link Paper(s)</div>';

    if (!currentDigraph.isView) {
        innerHtml = innerHtml
        + '<div class="' + isWfPaperExistingDisabled + 'wfPaperBtn" id="wfPaperEdit">My Papers</div>'
        + '<div class="wfPaperBtn" id="wfPaperUpload">Upload Paper</div>'
            + '<div class="wfPaperBtn" id="wfPaperUrl">Submit URL</div>';
    }

    innerHtml = innerHtml + '</div>'
        + '<div class="rhDiv">'
            + '<div id="myPapersDiv"><span id="wfPaperMessage"></span></div>'
        + '</div>';


    if (myPaperArray !== undefined && myPaperArray != null
           && myPaperArray.length > 0) {
        // Owner or non-owner can see papers (owner gets unattached papers, too)
        jQuery('#myPapersDiv').html(getPapersForEdit(myPaperArray));

        jQuery('#wfPaperExisting').removeClass('wfPaperBtnSelected');

        if (currentDigraph.isView != true) {
            // Only owner needs select box for unattached papers.
            jQuery('#wfPaperEdit').addClass('wfPaperBtnSelected');
            jQuery('#wfPaperEdit').removeClass('wfButtonDisabled');

            jQuery('#wfPaperUpload').removeClass('wfPaperBtnSelected');
            jQuery('#wfPaperUrl').removeClass('wfPaperBtnSelected');


        } else {

        }
    } else {

        jQuery('#myPapersDiv').html('<i>Please choose an option.</i>');

        jQuery('#wfPaperExisting').addClass('wfButtonDisabled');
        jQuery('#wfPaperExisting').removeClass('wfPaperBtnSelected');
        jQuery('#wfPaperEdit').addClass('wfButtonDisabled');
        jQuery('#wfPaperEdit').removeClass('wfPaperBtnSelected');

    }

}

function getPaperUploadDiv() {
    return '<fieldset class="wfPaperSubsection"><legend class="wfPaperLegend">Upload</legend>'
    + '<div class="workflowPaperButtons">'
    + '<label class="custom-paper-upload">'
    + '<input type="file" name="paper" id="wfPaperUploadBtn" style="display: none;"/>'
    + '<input type="button" value="Browse..." onclick="document.getElementById(\'wfPaperUploadBtn\').click();" />'
    + '<span id="wfPaperFileName"></span>'
    + '</label><br/>'
    + '<div class="wfPaperRow">Paper Title<span class="reqSpan">*</span>: '
    + '<input type="text" class="reqClass" id="wfPaperTitle" /></div><br/>'
    + '<div class="wfPaperRow">Publish Date: <input type="text" id="wfPaperPublishDate" /></div><br/>'
    + '<div class="wfPaperRow">Citation: <textarea rows="1" id="wfPaperCitation" /></div><br/>'
    + '<div class="wfPaperRow"><br/>Abstract: <textarea rows="2" id="wfPaperAbstract" /></div><br/>'
    + '<div class="wfPaperRow"><div id="wfPaperFinished" class="wfSubmitBtn wfSubmitBtnDisabled">Submit</div></div>'
    + '</div></fieldset>';
}


var paperData = null;

/**
 * Handles the Options File Upload button.
 */
function paperUpload() {

    if (this.name == 'paper') {

        var formData = new FormData();
        formData.append('file', jQuery(this)[0].files[0]);
        formData.append('requestingMethod', 'WorkflowAttributesServlet.paperUpload');
        formData.append('workflowId', currentDigraph.id);
        jQuery('#wfPaperFileName').text(jQuery(this)[0].files[0].name);
        paperData = formData;
        jQuery('#wfPaperTitle').keyup();
    }
}

function setAttachButtonListening() {
    jQuery('#wfPaperSelect_' + currentDigraph.id).change(function() {
        attachButtonUpdate(jQuery(this));
    });
    attachButtonUpdate(jQuery('#wfPaperSelect_' + currentDigraph.id));
}
function attachButtonUpdate(selBox) {
    var selCount = 0;
    selBox.find('option:selected').each(function(){
            selCount++;
    });

    if (selCount == 0) {
        jQuery('.wfSubmitBtn').off('click');
        jQuery('.wfSubmitBtn').addClass('wfSubmitBtnDisabled');
        enableSubmitBtn = false;
    } else {
        jQuery('.wfSubmitBtn').off('click');
        jQuery('.wfSubmitBtn').click(linkPaper);
        jQuery('.wfSubmitBtn').removeClass('wfSubmitBtnDisabled');
    }
}


function setSubmitButtonListening(isUrl) {
    jQuery('.reqClass').keyup(function() {
        var enableSubmitBtn = true;
        if (isUrl && jQuery('#wfPaperLinkText').length > 0 && urlPattern.test(jQuery('#wfPaperLinkText').val()) ){
            // passed test
        } else if (isUrl && jQuery('#wfPaperLinkText').length > 0) {
            enableSubmitBtn = false;
        } else if (!isUrl) {
            // paper upload (not url)

        }

        jQuery('.reqClass').each(function() {


            if (jQuery(this).val().trim() == '' || !enableSubmitBtn
                    || (!isUrl && jQuery('#wfPaperFileName').text().trim() == '')) {
                jQuery('.wfSubmitBtn').off('click');
                jQuery('.wfSubmitBtn').addClass('wfSubmitBtnDisabled');
                enableSubmitBtn = false;
            } else {
                jQuery('.wfSubmitBtn').off('click');
                if (isUrl && enableSubmitBtn) {
                    jQuery('.wfSubmitBtn').click(urlSubmit);
                    jQuery('.wfSubmitBtn').removeClass('wfSubmitBtnDisabled');
                } else if (!isUrl && enableSubmitBtn) {
                    jQuery('.wfSubmitBtn').click(paperUploadSubmit);
                    jQuery('.wfSubmitBtn').removeClass('wfSubmitBtnDisabled');
                }

            }
        });

    });
}

function setUpdateButtonListening(isUrl) {
    if (isUrl) {
        jQuery('.wfPaperRow input').keyup(updatePaperUrl);
        jQuery('.wfPaperRow textarea').keyup(updatePaperUrl);
        jQuery('#wfPaperPublishDate').keyup(updatePaperUrl);
    } else {
        jQuery('.wfPaperRow input').keyup(updatePaperFile);
        jQuery('.wfPaperRow textarea').keyup(updatePaperFile);
        jQuery('#wfPaperPublishDate').keyup(updatePaperFile);
    }
}
function updatePaperUrl() {
    updateButtonTrigger(true);
}
function updatePaperFile() {
    updateButtonTrigger(false);
}
function updateButtonTrigger(isUrl) {
    var enableSubmitBtn = true;
    if (isUrl && jQuery('#wfPaperLinkText').length > 0 && urlPattern.test(jQuery('#wfPaperLinkText').val()) ){
        // passed test
    } else if (isUrl && jQuery('#wfPaperLinkText').length > 0) {
        enableSubmitBtn = false;
    } else if (!isUrl) {
        // paper upload (not url)
    }

    jQuery('.reqClass').each(function() {


        if (jQuery(this).val().trim() == '' || !enableSubmitBtn) {
            jQuery('.wfSubmitBtn').off('click');
            jQuery('.wfSubmitBtn').addClass('wfSubmitBtnDisabled');
            enableSubmitBtn = false;
        } else {
            jQuery('.wfSubmitBtn').off('click');
            if (isUrl && enableSubmitBtn) {
                jQuery('.wfSubmitBtn').click(urlSubmit);
                jQuery('.wfSubmitBtn').removeClass('wfSubmitBtnDisabled');
            } else if (!isUrl && enableSubmitBtn) {
                jQuery('.wfSubmitBtn').click(paperUploadSubmit);
                jQuery('.wfSubmitBtn').removeClass('wfSubmitBtnDisabled');
            }

        }
    });
}

function updatePaper() {
    var urlStr = null
    if (jQuery('#wfPaperLinkText').length > 0 && urlPattern.test(jQuery('#wfPaperLinkText').val()) ){
        urlStr = jQuery('#wfPaperLinkText').val().trim();
    }
    var currPaperId = null;
    if (jQuery('#currentPaperId').length > 0) {
        new Ajax.Request('WorkflowAttributes', {
                parameters : {
                    requestingMethod: 'WorkflowAttributesServlet.updatePaper',
                    workflowId : currentDigraph.id,
                    paperId : currPaperId,
                    url : urlStr,
                    title : jQuery('#wfPaperTitle').val().trim(),
                    publishDate : jQuery('#wfPaperPublishDate').val().trim(),
                    citation : jQuery('#wfPaperCitation').val().trim(),
                    paperAbstract : jQuery('#wfPaperAbstract').val().trim()
                },
                onComplete : urlSubmitCompleted,
                beforeSend : wfShowStatusIndicatorModal(false, "Submitting..."),
                onSuccess : wfHideStatusIndicator,
                onException : function(request, exception) {
                    wfHideStatusIndicator(); throw(exception);
                }
        });
    } else {
        updateMyPaperArray(currentDigraph.id);
    }
}

function updatePaperDate(myInputElem) {

    var value = jQuery(myInputElem).val();
    jQuery(myInputElem).val(value);
    jQuery(myInputElem).keyup();
}


function paperUploadSubmit() {
    if (paperData == null) {
        paperData = new FormData();
        paperData.append('requestingMethod', 'WorkflowAttributesServlet.paperUpload');
        paperData.append('workflowId', currentDigraph.id);
    }
    if (jQuery('#currentPaperId').length > 0 && jQuery('#currentPaperId').val().trim() != '') {
        paperData.append('paperId', jQuery('#currentPaperId').val().trim());
    }
    paperData.append('title', jQuery('#wfPaperTitle').val().trim());
    paperData.append('publishDate', jQuery('#wfPaperPublishDate').val().trim());
    paperData.append('citation', jQuery('#wfPaperCitation').val().trim());
    paperData.append('paperAbstract', jQuery('#wfPaperAbstract').val().trim());
    jQuery.ajax({
        url : 'WorkflowAttributes', // Server script to process data
        type : 'POST',
        // Form data
        data : paperData,
        beforeSend : function() {
            wfShowStatusIndicatorModal(true, "Uploading...");
        },
        // Options to tell jQuery not to process data or worry about
        // content-type.
        async : true,
        cache : false,
        contentType : false,
        processData : false
    }).done(paperUploadCompleted).fail(error_uploadPaper);
}

/**
 * Options File upload error handler.
 */
function error_uploadPaper() {
    wfHideStatusIndicator();
    var dialogHtml = 'Your paper upload has failed. '
        + datashopHelpInfo;
    wfInfoDialog('uploadFailedDialog', dialogHtml, 'Error');
    monitorComponentStatus();
}

function paperUploadCompleted(data) {
    if (data !== undefined && data.responseText !== undefined) {
        json = data.responseText.evalJSON(true);
        data = json;
    }

    wfHideStatusIndicator();

    if (data.success == "true") {

        console.log('File successfully uploaded.');

        fileForm = '#paperUploadForm';

        var message = null;
        // The option file upload completed. 1 upload allowed per import prevents unecessary complexity.

        jQuery('.rhDiv').html('<div id="myPapersDiv"><div id="' + fileForm + '">'
                + 'New paper added!<br/>'
                + '<a href="WorkflowAttributes?paperId=' + data.paperId + '" id="paperLink">'
                + '<div id="paperTitle">' + data.title + '<div></a>'
                + '<div></div>');

        lastCall = "uploadPaperCompleted";

        updateMyPaperArray(currentDigraph.id);

    } else if (data.success == "false") {

        var dialogHtml = '' + data.message + '<br/>';
        jQuery('#myPapersDiv').html(data.message);
    } else {
        testLoggedOut(data);
    }
    paperData = null;
}

function getPaperUrlDiv() {
    return '<fieldset class="wfPaperSubsection"><legend class="wfPaperLegend">URL</legend>'
    + '<div class="workflowPaperButtons">'
    + '<div class="wfPaperRow">URL<span class="reqSpan">*</span>: '
    + '<input type="text" class="reqClass" title="Must use http:// or https://" id="wfPaperLinkText" /></div><br/>'
    + '<div class="wfPaperRow">Paper Title<span class="reqSpan">*</span>: '
            + '<input type="text" class="reqClass" id="wfPaperTitle" /></div><br/>'
    + '<div class="wfPaperRow">Publish Date: <input type="text" id="wfPaperPublishDate" /></div><br/>'
    + '<div class="wfPaperRow">Citation: <textarea rows="1" id="wfPaperCitation" /></div><br/>'
    + '<div class="wfPaperRow"><br/>Abstract: <textarea rows="2" id="wfPaperAbstract" /></div><br/>'
    + '<div class="wfPaperRow"><div id="wfPaperLinkBtn" class="wfSubmitBtn wfSubmitBtnDisabled">Submit</div></div>'
+ '</div></fieldset>';
}



function urlSubmit() {
    var paperIdStr = null;
    if (jQuery('#currentPaperId').length > 0 && jQuery('#currentPaperId').val().trim() != '') {
        paperIdStr = jQuery('#currentPaperId').val().trim();
    }
    new Ajax.Request('WorkflowAttributes', {
            parameters : {
                requestingMethod: 'WorkflowAttributesServlet.urlSubmit',
                workflowId : currentDigraph.id,
                paperId : paperIdStr,
                url : jQuery('#wfPaperLinkText').val().trim(),
                title : jQuery('#wfPaperTitle').val().trim(),
                publishDate : jQuery('#wfPaperPublishDate').val().trim(),
                citation : jQuery('#wfPaperCitation').val().trim(),
                paperAbstract : jQuery('#wfPaperAbstract').val().trim()
            },
            onComplete : urlSubmitCompleted,
            beforeSend : wfShowStatusIndicatorModal(false, "Submitting..."),
            onSuccess : wfHideStatusIndicator,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
    });

}

function urlSubmitCompleted(data) {
    if (data !== undefined && data.responseText !== undefined) {
        json = data.responseText.evalJSON(true);
        data = json;
    } else {
        wfInfoDialog('urlFailedDialog', 'URL submission failed.'
            + datashopHelpInfo, 'Error');
    }

    wfHideStatusIndicator();

    if (data.success == "true") {

        console.log('Paper submitted.');

        fileForm = '#paperUploadForm';

        var message = null;
        // The option file upload completed. 1 upload allowed per import prevents unecessary complexity.
        if (data.url !== undefined && data.url != null) {
            jQuery('.rhDiv').html('<div id="myPapersDiv"><div id="' + fileForm + '">'
                    + 'New URL submitted!<br/><a href="' + data.url + '" id="paperLink">'
                    + '<div id="paperTitle">' + data.title + '<div></a>'
                    + '<div></div>');

            updateMyPaperArray(currentDigraph.id);
        }

        lastCall = "urlSubmitCompleted";

    } else if (data.success == "false") {

        var dialogHtml = '' + data.message + '<br/>';
        jQuery('#myPapersDiv').html(data.message);
    } else {
        testLoggedOut(data);
    }
}

function getPaperExisting(myArray) {
    var myHtml = '<div class="wfPaperUpper">';
    if (myArray !== undefined && myArray != null
            && myArray.length > 0) {

        var selBoxSize = 0;
        var currentlyAttachedCount = 0;
        for (var paperCount = 0; paperCount < myArray.length; paperCount++) {
            var paper = myArray[paperCount];
            if (paper != null && paper.title !== undefined && paper.paperId !== undefined
                    && paper.workflowId !== undefined) {
                if (paper.workflowId != currentDigraph.id) {
                    selBoxSize++;
                } else {
                    currentlyAttachedCount++;
                }
            }
        }
        if (selBoxSize > 11) {
            selBoxSize = 11;
        }

        if (currentlyAttachedCount > 0) {
            var attachedPaperDiv = generateAttachedPaperDiv(myArray, currentDigraph.id);
            myHtml = myHtml + '<div class="wfPaperHeader">Attached Papers: </div>' + attachedPaperDiv;
        }

        myHtml = myHtml + '</div>';


        if (selBoxSize > 0) {
            myHtml = myHtml
                    + '<div class="wfPaperLower">'
                    + '<div class="wfPaperHeader">Select paper(s) to associate with this workflow:</div>'
                    + '<div class="wfSelList" id="wfSelList_' + currentDigraph.id
                    + '">' + ''
                    + '';

            var disabledStr = "";

            var attachPaperOptions = generateAttachablePaperSelect(myArray, currentDigraph.id);

            myHtml = myHtml
                    + '<span class="paperFilter">Filter Papers</span>'
                    + '<input ' + disabledStr
                    + ' type="text" class="paperFilter" maxlength="100" size="40"'
                    + ' title="Filter by Title">' + '</span>';

            myHtml = myHtml + ''
                    + '<select multiple size="' + selBoxSize + '" ' + disabledStr
                    + ' id="wfPaperSelect_' + currentDigraph.id
                    + '" class="existingPaper" name="wfPaperSelect" >'
                    + attachPaperOptions + '</select>' + '';

            myHtml = myHtml + '</div><div>'
                + '<div id="wfLinkPaperBtn" class="wfSubmitBtn">Attach</div>'
                + "</div>";

            myHtml = myHtml + '</div>';
        }

    } else {

    }
    return myHtml;
}

function getPapersForEdit(myArray) {
    var myHtml = '<div class="wfPaperUpper">';
    if (myArray !== undefined && myArray != null
            && myArray.length > 0) {

        if (myArray.length > 0) {
            var attachedPaperDiv = generateAttachedPaperDiv(myArray, null);
            myHtml = myHtml + '<div class="wfPaperHeader">My Papers: </div>' + attachedPaperDiv;
        }

        myHtml = myHtml + '</div>';


    } else {

    }
    return myHtml;
}

function linkPaper() {
    var paperIds = "";
    var theSelect = jQuery('#wfPaperSelect_' + currentDigraph.id);
    theSelect.find('option:selected').each(function(){
            if (paperIds.length > 0) { paperIds += ","; }
            paperIds += jQuery(this).val();
        });

    new Ajax.Request('WorkflowAttributes', {
            parameters : {
                requestingMethod: 'WorkflowAttributesServlet.linkPaper',
                workflowId : currentDigraph.id,
                paperIds : paperIds
            },
            onComplete : linkPaperCompleted,
            beforeSend : wfShowStatusIndicatorModal(false, "Processing..."),
            onSuccess : wfHideStatusIndicator,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
    });

}

function linkPaperCompleted(transport) {

    json = transport.responseText.evalJSON(true);

    updateMyPaperArray(currentDigraph.id);

    if (json.success != "true") {
        var dialogHtml = 'Your paper could not be attached. '
            + datashopHelpInfo;
        wfInfoDialog('paperFailedDialog', dialogHtml, 'Error');
        updateMyPaperArray(currentDigraph.id);
    }
}

function unlinkPaper(paperId) {

    new Ajax.Request('WorkflowAttributes', {
            parameters : {
                requestingMethod: 'WorkflowAttributesServlet.unlinkPaper',
                workflowId : currentDigraph.id,
                paperId : paperId
            },
            onComplete : unlinkPaperCompleted,
            beforeSend : wfShowStatusIndicatorModal(false, "Processing..."),
            onSuccess : wfHideStatusIndicator,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
    });
}

function unlinkPaperCompleted(transport) {

    json = transport.responseText.evalJSON(true);

    updateMyPaperArray(currentDigraph.id);

    if (json.success != "true") {
        var dialogHtml = 'Your paper could not be unattached. '
            + datashopHelpInfo;
        wfInfoDialog('paperFailedDialog', dialogHtml, 'Error');
    }
}

function formatPaperDate(dateStr) {
    if (dateStr != null) {
        dateStr = jQuery.datepicker.formatDate('M dd, yy', new Date(dateStr));
    }
    return dateStr;
}

var prevPapers = null;
jQuery.fn.filterPapersByText = function(textbox) {
    this.each(function() {
        var options = [];
        var select = this;


        jQuery(select).children().each(function() {
            if (this.tagName !== undefined) {
                if (this.tagName.toLowerCase() == "option") {
                    options.push({ value: jQuery(this).val(), text: jQuery(this).text() });
                } else if (this.tagName.toLowerCase() == "optgroup") {

                    var thisLabel = jQuery(this).attr("label");
                    options.push({ label: thisLabel, text: jQuery(this).text() });

                    jQuery(this).children().each(function() {
                        if (this.tagName.toLowerCase() == "option") {
                            options.push({ optgroup: thisLabel, value: jQuery(this).val(), text: jQuery(this).text() });
                        }
                    });

                }
            }
        });

        prevPapers = options;
        jQuery(select).data('options', options);

        jQuery(textbox).bind('keyup', function() {

            var optInner = jQuery(select).data('options');

            if (jQuery(textbox).val().trim() == "") {
                jQuery(select).empty();
                options = prevPapers;
            } else {
                jQuery(select).empty();
            }

            var search = jQuery.trim(jQuery(this).val());
            var regex = new RegExp(search, "gi");
            var matchingPapers = [];
            var paperCount = 0;
            // Look for matches on dataset names and project names
            jQuery.each(myPaperArray, function(i) {
                var wfPaperTitle = myPaperArray[i].title;
                var wfPaperUrl = myPaperArray[i].url;
                if ((wfPaperTitle !== undefined && wfPaperTitle.match(regex) !== null) ||
                    (wfPaperUrl !== undefined && wfPaperUrl.match(regex) !== null)) {
                    matchingPapers[paperCount++] = myPaperArray[i];
                }
            });
            var paperOptions = generateAttachablePaperSelect(matchingPapers, currentDigraph.id);
            jQuery(select).append(jQuery(paperOptions));

            theSelect = jQuery('#wfPaperSelect_' + currentDigraph.id);
            jQuery(theSelect).children().each(function() {
                jQuery(this).dblclick(function() {
                    linkPaper();
                });
            });

            setAttachButtonListening();

        });
    });
};

function wfPaperGoBack() {
    jQuery('#' + lastPaperButton).click();
}

var lastPaperButton = null;
function editPaper(paperId) {
    if (jQuery('.wfPaperBtnSelected').length > 0) {
        lastPaperButton = jQuery('.wfPaperBtnSelected').attr('id');
        jQuery('.wfPaperBtnSelected').removeClass('wfPaperBtnSelected');
    }

    new Ajax.Request('WorkflowAttributes', {
            parameters : {
                requestingMethod: 'WorkflowAttributesServlet.editPaper',
                workflowId : currentDigraph.id,
                paperId : paperId
            },
            onComplete : editPaperCompleted,
            beforeSend : wfShowStatusIndicatorModal(false, "Processing..."),
            onSuccess : wfHideStatusIndicator,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
    });

}

function editPaperCompleted(transport) {

    json = transport.responseText.evalJSON(true);


    if (json.success == "true") {
        populatePaperEditDiv(json, jQuery('#myPapersDiv'));
    }
}

function populatePaperEditDiv(json, myPapersDiv) {
    var paperId = json.paperId;
    var url = json.url;
    var title = json.title;
    var authorNames = json.authorNames; // placeholder
    var publishDate = json.publishDate;
    var paperAbstract = json.paperAbstract;
    var citation = json.citation;
    var fileName = json.fileName;

    var isUrl = null;
    if (url != null && url.trim() != '') {
        isUrl = true;
    } else {
        isUrl = false;
    }

    var publishDateStr = '';

    if (publishDate != null && publishDate.trim() != '') {
        publishDateStr = ' value="' + publishDate + '" ';
    }
    if (citation == null) {
        citation = '';
    }
    if (paperAbstract == null) {
        paperAbstract = '';
    }

    var innerHtml = '<fieldset class="wfPaperSubsection"><legend class="wfPaperLegend">Edit Paper Attributes</legend>'
    + '<div class="workflowPaperButtons">';

    if (isUrl) {
        innerHtml = innerHtml + '<div class="wfPaperRow">URL<span class="reqSpan">*</span>: '
            + '<input type="text" class="reqClass" title="Must use http:// or https://" id="wfPaperLinkText" '
            + 'value="' + url + '" /></div><br/>';
    } else {
        innerHtml = innerHtml + '<label class="custom-paper-upload">'
        + '<input type="file" name="paper" id="wfPaperUploadBtn" style="display: none;" />'
        + '<span id="wfPaperFileName">' + fileName + '</span>'
        + '</label><br/>';
    }

    innerHtml = innerHtml + '<div class="wfPaperRow">Paper Title<span class="reqSpan">*</span>: '
        + '<input type="text" class="reqClass" id="wfPaperTitle" value="' + title + '" /></div><br/>';

    innerHtml = innerHtml
            + '<div class="wfPaperRow">Publish Date: <input type="text" id="wfPaperPublishDate" ' + publishDateStr + ' /></div><br/>';

    innerHtml = innerHtml
            + '<div class="wfPaperRow">Citation: <textarea rows="1" id="wfPaperCitation" >'
                + citation.trim() + '</textarea></div><br/>';

    innerHtml = innerHtml
            + '<div class="wfPaperRow"><br/>Abstract: <textarea rows="2" id="wfPaperAbstract" >'
            + paperAbstract.trim() + '</textarea></div><br/>';

    innerHtml = innerHtml + '<div class="wfPaperRow">'
            + '<div id="wfPaperUpdateBtn" class="wfSubmitBtn wfSubmitBtnDisabled">'
            + 'Update</div>'
            + '<div id="wfCancelEditBtn" class="wfCancelBtn">Cancel</div>'
        + '</div>'
        + '</div></fieldset>';

    myPapersDiv.html(innerHtml);

    if (isUrl) {
        jQuery('#wfPaperLinkText').hint('auto-hint');
    } else {
        jQuery('#wfPaperUploadBtn').change(paperUpload);
    }
    jQuery('#wfCancelEditBtn').click(wfPaperGoBack);

    jQuery('#wfPaperPublishDate').datepicker({
        onSelect: function(selectedDate) {
            updatePaperDate(this);
        }
    });

    jQuery('<input type="hidden" id="currentPaperId" value="' + json.paperId + '" />').appendTo('#myPapersDiv');

    setUpdateButtonListening(isUrl);

}




function deletePaper(paperId) {
    if (paperId != null) {
        new Ajax.Request('WorkflowAttributes', {
                parameters : {
                    requestingMethod: 'WorkflowAttributesServlet.deletePaper',
                    workflowId : currentDigraph.id,
                    paperId : paperId
                },
                onComplete : deletePaperCompleted,
                beforeSend : wfShowStatusIndicatorModal(false, "Deleting..."),
                onSuccess : wfHideStatusIndicator,
                onException : function(request, exception) {
                    wfHideStatusIndicator(); throw(exception);
                }
        });
    }
}

function deletePaperCompleted(transport) {

    json = transport.responseText.evalJSON(true);

    updateMyPaperArray(currentDigraph.id);

    if (json.success != "true") {
        var dialogHtml = 'Your paper could not be deleted. '
            + datashopHelpInfo;
        wfInfoDialog('paperDeleteDialog', dialogHtml, 'Error');
    } else {
        var dialogHtml = 'Paper deleted: ' + json.title + ' (' + json.paperId + ')';
        wfInfoDialog('paperDeletedDialog', dialogHtml, 'Success');
    }

    editMyPaperArray(currentDigraph.id);
}
