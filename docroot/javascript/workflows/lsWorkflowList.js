
var lsWorkflowListDialogTime = 4000;
var pageDisplayLimit = 10;
var pageChunkCount = 3;
var noFolderTitle = 'No Folder';
var newFolderTitle = 'Create New Folder';

/* Fetches workflows using pageNumber (int) pageLimit (int), and filters (hashmap) */
function fetchWorkflowRows(panelId, pageNumber, filters) {

    let dsId = "";
    var datasetIdParam = getParam('datasetId', window.location.href);
    if (datasetIdParam != null && datasetIdParam.trim() != '') {
        dsId = datasetIdParam;
    }

    new Ajax.Request('LearnSphere', {
        parameters : {
            requestingMethod: 'ManageWorkflowsServlet.fetchWorkflowRows',
            datasetId: dsId,
            panelId: panelId,
            pageNumber: pageNumber,
            filters: JSON.stringify(filters)
        },
        onComplete : createWorkflowRows,
        beforeSend : wfShowStatusIndicatorModal(false, "Loading..."),
        onSuccess : wfHideStatusIndicator,
        onException : function(request, exception) {
            wfHideStatusIndicator(); throw(exception);
        }
    });
}

function createWorkflowRows(transport) {
    if (transport !== undefined && transport.responseText !== undefined) {
        var json = transport.responseText.evalJSON(true);
        // Clear
        var wfTabName = '';
        var panelId = json.panelId;
        jQuery('.workflowTabDiv').jstree("destroy").empty();
        //jQuery('#' + json.panelId).html('<ul><li id="innerRoot-' + json.panelId + '" data-jstree=\'{"type":"root", "opened": true}\'>'
        jQuery('#' + panelId).html('<ul><li id="innerRoot-' + panelId + '" >'
                + '</li></ul>');

        var accessRowMap = {};
        if (json.accessRowsJson !== undefined) {
            jQuery(json.accessRowsJson).each(function (wfRowIndex, wfRowObj) {
                //console.log(JSON.stringify(wfRowObj));
                accessRowMap[wfRowObj.id] = wfRowObj;
            });

        }

        var isAdmin = false;
        if (jQuery('input#adminUserFlag').length > 0
                && jQuery('input#adminUserFlag').val() == "true") {
            isAdmin = true;
        }
        var isMyWorkflows = false;
        if (panelId == 'my-workflows-panel') {
            isMyWorkflows = true;
        }

        if (json.wfRowsJson !== undefined) {
            var wfFolderIds = {};

            var firstNonEmptyFolder = true;
            var lastFolder = '';
            var firstRow = true;
            var hasFolders = false;

            setWorkflowTagsObject(json.wfRowsJson);

            jQuery(json.wfRowsJson).each(function (wfRowIndex, wfRowObj) {
                // For each unique folder
                var isNewFolder = false;
                var hasFolder = false;
                var isWorkflow = false;

                if (wfRowObj.folderId !== undefined) {
                    var isEmptyFolderRow = false;
                    if (wfRowObj.id === undefined) {
                        isEmptyFolderRow = true;
                    } else {
                        isWorkflow = true;
                    }
                    hasFolder = true;
                    hasFolders = true;
                    if (!isEmptyFolderRow
                        && (lastFolder != wfRowObj.folderName + '' + wfRowObj.ownerId || lastFolder == '')
                            ) {

                        jQuery('#innerRoot-' + panelId).append('<hr class="wfListHr" />');
                        lastFolder = wfRowObj.folderName + '' + wfRowObj.ownerId;
                    }

                    if (wfFolderIds[wfRowObj.folderId] === undefined) {

                        var attachFolderInfo = '';
                        if (isEmptyFolderRow) {
                            // empty folder row
                            attachFolderInfo = '<div class="emptyFolderDiv">- 0 items in folder -</div>';
                        }

                        isNewFolder = true;
                        wfFolderIds[wfRowObj.folderId] = wfRowObj.folderName;

                        var folderHtml = '';

                        folderHtml = folderHtml + '<ul class="rootFolderLevel" ><li id="wfFolder-' + wfRowObj.folderId + '" >'
                          + '<div class="wfFolderRow">'
                          + '<div class="wfFolderName">' + escapeHtmlMarkers(wfRowObj.folderName) + '</div>';



                        if (isMyWorkflows || isAdmin) {
                            folderHtml = folderHtml
                                + '<span class="folderHamburgerSpan" name="' + wfRowObj.folderId + '" >'
                                + '<img class="hamburgerIcon" src="css/images/horiz_ellipse.svg" />'
                                + '</span>';

                            folderHtml = folderHtml + '<span class="wfFolderHamburgerList">';

                            folderHtml = folderHtml
                                + '<a class="wfFolderHamburgerLink" name="rowActions' + wfRowObj.folderId
                                + '" href="javascript:createNewWorkflowInFolder(\''
                                + wfRowObj.folderId + '\');">Add New Workflow</a>';

                            folderHtml = folderHtml
                                + '<a class="wfFolderHamburgerLink" name="rowActions' + wfRowObj.folderId
                                + '" href="javascript:renameFolderDialog(\''
                                + wfRowObj.folderId + '\', \'' + escapeDomValue(wfRowObj.folderName)
                                    .replace(/[\\]+/g, '\\') + '\');">Rename Folder</a>';

                            folderHtml = folderHtml
                                + '<a class="wfFolderHamburgerLink" name="rowActions' + wfRowObj.folderId
                                + '" href="javascript:deleteFolderDialog(\''
                                + wfRowObj.folderId + '\', \'' + escapeDomValue(wfRowObj.folderName)
                                    .replace(/[\\]+/g, '\\') + '\');">Delete Folder</a>';

                            folderHtml = folderHtml + '</span>';
                        }

                        folderHtml = folderHtml + attachFolderInfo;

                        jQuery('#innerRoot-' + panelId).append(folderHtml);

                        firstRow = false;

                    }
                } else if (wfRowObj.id !== undefined) {
                    isWorkflow = true;

                    if (wfFolderIds['LS_ROOT_FOLDER_5892340'] === undefined) {
                          //  && panelId != 'recommended-workflows-panel') {
                        wfFolderIds['LS_ROOT_FOLDER_5892340'] = noFolderTitle;
                        var hrElem = '';
                        if (lastFolder != '') {
                            hrElem = '<hr class="wfListHr" />';
                        }

                        var folderHtml = hrElem + '<ul class="rootFolderLevel" ><li id="wfFolder-0" >'
                            + '<div class="wfFolderRow">'
                            + '<div class="wfFolderName">' + noFolderTitle + '</div>'
                            + '</div></li></ul>';

                        if (hasFolders) {
                            jQuery('#innerRoot-' + panelId).append(folderHtml);
                        }

                        firstRow = false;
                    }

                }

                if (!isEmptyFolderRow) {

                    // workflow row
                    var addTo = '#wfFolder-0';
                    if (!hasFolders && wfRowObj.folderId === undefined) {
                        addTo = '#innerRoot-' + panelId;
                    } else if (wfRowObj.folderId !== undefined) {
                        addTo = '#wfFolder-' + wfRowObj.folderId;
                    }
                    jQuery(addTo).append(
                        createWorkflowRow(wfRowObj, accessRowMap[wfRowObj.id], !hasFolder,
                            isWorkflow, isMyWorkflows, isAdmin, panelId));

                    if (isNewFolder) {
                        //jQuery('#innerRoot-' + panelId).append('</ul></li>');
                    }

                    firstRow = false;

                } else {
                    if (isNewFolder) {
                        jQuery('#innerRoot-' + panelId).append('</ul></li>');
                    }

                }
            });

            addWorkflowListTree(panelId, null);

            jQuery('.wfAuthorName').click(function() {
                var authName = jQuery(this).text();
                jQuery('#wf_search_author').val(authName);
                saveSearchFilters();
            });

            jQuery('.wfAccessRequestLink').off('click');
            jQuery('.wfAccessRequestLink').click(createWfAccessRequest);

            // Pagination
            var workflowsThisPanel = 0;

            jQuery('#my-workflows-count').html("(" + json.myWorkflowCount.toLocaleString() + ")");
            jQuery('#shared-workflows-count').html("(" + json.sharedWorkflowCount.toLocaleString() + ")");
            jQuery('#recommended-workflows-count').html("(" + json.recommendedWorkflowCount.toLocaleString() + ")");

            if (panelId == 'my-workflows-panel') {
                workflowsThisPanel = json.myWorkflowCount;

            } else if (panelId == 'shared-workflows-panel') {
                workflowsThisPanel = json.sharedWorkflowCount;

            } else if (panelId == 'recommended-workflows-panel') {
                workflowsThisPanel = json.recommendedWorkflowCount;

            }

            var totalPages = Math.floor((workflowsThisPanel + json.pageLimit - 1) / json.pageLimit);
            var pageHtml = '';

            if (totalPages > 1 && json.pageNumber != 1) {
                pageHtml += '<button type="submit" class="pageLink" value="' + 1 + '" ><<</button>';
                pageHtml += '<button type="submit" class="pageLink" value="' + (json.pageNumber - 1) + '" ><</button>';
            }

            for (var p = 1; p <= totalPages; p++) {

                var isNearCurrentPage = false;
                var isEdgePage = false;
                if (p > json.pageNumber - pageChunkCount
                            && p < json.pageNumber + pageChunkCount) {
                    isNearCurrentPage = true;
                }
                if (json.pageNumber <= pageChunkCount) {
                    if (p < 2 * pageChunkCount) {
                        isEdgePage = true;
                    }
                } else if (json.pageNumber >= totalPages - pageChunkCount + 1) {
                    if (p > totalPages - 2 * pageChunkCount + 1) {
                        isEdgePage = true;
                    }
                }

                if (totalPages > 1) {
                    if ( (isNearCurrentPage || isEdgePage)) {
                        if (p == json.pageNumber) {
                            pageHtml += '<span class="highlightedPage">' + p + '</span>';
                        } else {
                            pageHtml += '<button type="submit" class="pageLink" value="' + p + '" >' + p + '</button>';
                        }
                    } else {
                        if (json.pageNumber <= pageChunkCount) {
                            if (p == 2 * pageChunkCount) {
                                pageHtml += '<span class="highlightedPage">...</span>';
                            }
                        } else if (json.pageNumber >= totalPages - pageChunkCount + 1) {

                            if (p == totalPages - 2 * pageChunkCount + 1) {
                                pageHtml += '<span class="highlightedPage">...</span>';
                            }
                        } else {
                            if (p == json.pageNumber - pageChunkCount || p == json.pageNumber + pageChunkCount) {
                                pageHtml += '<span class="highlightedPage">...</span>';
                            }
                        }
                        continue;
                    }
                }

            }

            if (totalPages > 1 && json.pageNumber < totalPages) {
                pageHtml += '<button type="submit" class="pageLink" value="' + (json.pageNumber + 1) + '" >></button>';
                pageHtml += '<button type="submit" class="pageLink" value="' + totalPages + '" >>></button>';
            }

            if (panelId == 'my-workflows-panel') {
                jQuery('.wfCreateFolderDiv').html('<img class="wfTabAddFolderIcon" src="css/images/add_folder.svg"'
                  + 'title="Create New Folder" height="24" width="24" />Create Folder');

                jQuery('.wfLegend').css('display', '');

                // No 'Data Access' search on this panel
                jQuery('#wfSearchAccess').css('display', 'none');

            } else {
                jQuery('.wfCreateFolderDiv').css('display', 'none');
                jQuery('.wfLegend').css('display', 'none');

                // Show 'Data Access' search on this panel
                jQuery('#wfSearchAccess').css('display', '');
            }

            jQuery('.pageDiv').html(pageHtml);
            jQuery('.pageLink').click(function() {
                var panelId = jQuery("#wfTabDiv .ui-tabs-panel:visible").attr("id");

                fetchWorkflowRows(panelId, jQuery(this).val(), null);
            });

        } else if (json.error_flag !== undefined && json.message !== undefined) {
            wfTimerDialog(json.message);
        } else {
            testLoggedOut(json);
        }
    }
}

function padNumber(p_num) {
    if (p_num >= 0 && p_num <= 9) {
        return '0' + p_num;
    }
    return p_num;
}

function createWorkflowRow(wfRowObj, accessRow, isRootFolderLevel,
        isWorkflow, isMyWorkflows, isAdmin, panelId) {

    var date = new Date(wfRowObj.updated);
    var amPmHours = date.getHours() > 12 ? date.getHours() - 12 : date.getHours();
    var amPmFlag = date.getHours() >= 12 ? 'pm' : 'am';
    var formattedDate = padNumber(date.getMonth() + 1) + '/' + padNumber(date.getDate()) + '/' +  date.getFullYear()
        + ' ' + amPmHours + ':' + padNumber(date.getMinutes()) + ' ' + amPmFlag;
        //  + ':' + padNumber(date.getSeconds()
    var dataAccessHtml = '';
    if (panelId != 'my-workflows-panel' && accessRow !== undefined && accessRow.dataAccessHtml !== undefined) {
        dataAccessHtml = accessRow.dataAccessHtml;
    }

    var globalDemarcation = '';
    if ((isAdmin || panelId == 'my-workflows-panel') && wfRowObj.globalFlag) {
        globalDemarcation = '<span class="globalDemarcation">*</span>';
    } else {
        globalDemarcation = '<span class="globalDemarcation"></span>';
    }


    var ulListClass = '';
    if (isWorkflow && isRootFolderLevel) {
        ulListClass = ' class="workflowListClass rootFolderLevel" '
    } else if (isWorkflow) {
        ulListClass = ' class="workflowListClass" '
    } else if (isRootFolderLevel) {
        ulListClass = ' class="rootFolderLevel" ';
    }

    var datasetIdParam = '';
    var dsId = getParam('datasetId', window.location.href);
    if (dsId != null && dsId.trim() != '') {
        datasetIdParam = '&datasetId=' + dsId;
    }

    var hamburgerSpan = '<span class="wfHamburgerList">'
            + '<a class="wfHamburgerLink" name="rowActions' + wfRowObj.id + '" title="Open" href="LearnSphere?workflowId='
                + wfRowObj.id + datasetIdParam + '">Open</a>'
            + '<a class="wfHamburgerLink" name="rowActions' + wfRowObj.id + '" title="Save As New Workflow" href="javascript:wfSaveAsNewWorkflow(\''
                + wfRowObj.id + '\');">Save As</a>';

    if (isMyWorkflows || isAdmin) {
        hamburgerSpan = hamburgerSpan
            + '<a class="wfHamburgerLink" name="rowActions' + wfRowObj.id + '" title="Move To Folder" href="javascript:fetchMyFolders(\''
                + wfRowObj.id + '\');">Move To Folder</a>';
    }

    if (isMyWorkflows || isAdmin) {
        var isViewFlag = 'false';
        hamburgerSpan = hamburgerSpan
            + '<a class="wfHamburgerLink"  name="rowActions' + wfRowObj.id + '" title="Workflow Settings" href="javascript:wfModifyWorkflowSettings(\''
                + wfRowObj.id + '\', \'' + isViewFlag + '\');">Settings</a>';
    } else {
        var isViewFlag = 'true';
        hamburgerSpan = hamburgerSpan
        + '<a class="wfHamburgerLink"  name="rowActions' + wfRowObj.id + '" title="Workflow Details" href="javascript:wfModifyWorkflowSettings(\''
            + wfRowObj.id + '\', \'' + isViewFlag + '\');">Details</a>';
    }

    if (isMyWorkflows || isAdmin) {
        hamburgerSpan = hamburgerSpan
            + '<a class="wfHamburgerLink" name="rowActions' + wfRowObj.id + '" title="Delete Workflow" href="javascript:wfDeleteWorkflow(\''
                + wfRowObj.id + '\');">Delete</a>';
    }
    if ((wfRowObj.state == 'running') && (isMyWorkflows || isAdmin)) {
        hamburgerSpan = hamburgerSpan
            + '<a class="wfHamburgerLink" name="rowActions' + wfRowObj.id + '" title="Cancel Workflow" href="javascript:cancelWorkflow(\''
                + wfRowObj.id + '\');">Cancel</a>';
    }

    hamburgerSpan = hamburgerSpan + '</span>';
    var noDescOrTags = 'display: none';
    if ((wfRowObj.desc !== null && wfRowObj.desc.length > 0)
            || (workflowListTags[wfRowObj.id] != null && workflowListTags[wfRowObj.id].length > 0)) {
        noDescOrTags = '';
    }

    var wfStateIcon = '';
    if (wfRowObj.state == 'new') {
        // Don't display the Ready status icon
        //wfStateIcon = '<img class="wfListState" src="images/component_ready.png" title="Ready" / >';
    } else if (wfRowObj.state == 'running') {
        wfStateIcon = '<img class="wfListState" src="images/component_running.png" title="Running" / >';
    } else if (wfRowObj.state == 'running_dirty') {
        wfStateIcon = '<img class="wfListState" src="images/component_running.png" title="Canceling" / >';
    } else if (wfRowObj.state == 'error') {
        wfStateIcon = '<img class="wfListState" src="images/component_error.png" title="Errors" / >';
    } else if (wfRowObj.state == 'success') {
        wfStateIcon = '<img class="wfListState" src="images/component_on.png" title="Successfully completed" / >';
    }

    var authorNameSpan = '<span class="wfAuthorName">' + wfRowObj.ownerId + '</span>';
    if (panelId == 'my-workflows-panel') {
        authorNameSpan = '';
    }

    let wfDescWithLinks = urlToAnchorTag(wfRowObj.desc, 'linkInDescription');
    var hasPapers = '';
    if (wfRowObj.paperCount > 0) {
        hasPapers = ' <img class="wfPaperIcon" src="images/page_white_text.png" title="' + wfRowObj.paperCount + ' papers" />';
    }
    var workflowRow =
      //'<ul><li data-jstree=\'{"type":"leaf_node"}\'><div class="wfItemDiv" id="workflows_' + wfRowObj.id + '" name="workflows_' + wfRowObj.id + '">'
        '<ul ' + ulListClass + '><li id="workflows_' + wfRowObj.id + '" name="workflows_' + wfRowObj.id + '">'
        + '<div class="wfNameRow" >'

            + '' + dataAccessHtml + ''
        /*    + '<div class="wfIconDiv"><span>'
            + '<span class="wfSaveAsNewIconContainer"><a class="ls_wf-add" title="Save as New Workflow" href="javascript:wfSaveAsNewWorkflow(\'' + wfRowObj.id + '\');">Save Workflow</a></span>'
            + '<a class="ls_wf-delete" title="Delete Workflow" href="javascript:wfDeleteWorkflow(\'' + wfRowObj.id + '\');">Delete Workflow</a>'
            + '</span></div>'*/

            + '<div class="wfName">'
                + '<input type="hidden" id="workflowNameInput_' + wfRowObj.id + '" value="' + wfRowObj.name + '">'
                + '<input type="hidden" id="workflowGlobalInput_' + wfRowObj.id + '" value="' + wfRowObj.globalFlag + '">'
                + '<div id="workflowNameDiv_' + wfRowObj.id + '">'
                    + '<span class="wfLabel primaryInfo">'
                    + '<a id="workflowName_' + wfRowObj.id + '" title="' + wfRowObj.name
                    + '" href="LearnSphere?workflowId=' + wfRowObj.id
                        + datasetIdParam + '">'
                        + getTruncatedNameIfNec(wfRowObj.name) + '</a>'
                    + globalDemarcation
                    + '</span>'
                    + '<span class="hamburgerSpan" name="' + wfRowObj.id + '" >'
                    + '<img class="hamburgerIcon" src="css/images/hamburger.svg" />'
                    + '</span>'

                    + '<span class="wfDes" id="wfDescId_' + wfRowObj.id + '" style="' + noDescOrTags + '">'
                    + '<img src="images/ds_wf-info.svg" class="ls_wf-info" alt="Description">'
                    + '<span class="tooltiptext" value="'+wfRowObj.desc+'">' + wfDescWithLinks + getTagsForTooltip(wfRowObj.id) + '</span>'
                    + '</span>'

                    + '<span class="wfState" id="wfStateId_' + wfRowObj.id + '" >'
                    + wfStateIcon
                    + '</span>'

                    + '<span class="wfHasPapers" id="wfPapersId_' + wfRowObj.id + '" >'
                    + hasPapers
                    + '</span>'

                    + hamburgerSpan
                + '</div>'
            + '</div>'
        + '</div>'
        + '<div class="wfSecondaryRow">'
            + '<div class="secondaryInfo">'
            + authorNameSpan + formattedDate + ''
            + '</div>'
        + '</div>'
    /* mck tags placeholder   + '<div class="wfTertiaryRow">'
            + '<div class="secondaryInfo">'
            + '<span class="boundingBox">#classifiers</span><span class="boundingBox">#bayesian network</span><span class="boundingBox">#k-folds x-validation</span>' //+ '' + wfRowObj.tags + ''
            + '</div>'
        + '</div>' */

      + '</div></li></ul>'; // end of wfFolderRow


    return workflowRow;
}

function attachNewFolderNameInput(divId) {
    if (jQuery('#' + divId).length > 0 && jQuery('#createFolderDiv').length == 0) {
        jQuery('#' + divId).append('<div id="createFolderDiv">'
                  + 'Folder Name:<br />'
                  + '<input type="text" id="newFolderName"'
                  + ' size="50" title="Enter a new folder name" maxlength="100" >'
                  + '</div>');
    }
}

function createNewFolderInput() {
    var folderText = jQuery('#moveFolder option:selected').text();
    if (folderText == newFolderTitle) {
        attachNewFolderNameInput('workflowFolderDiv');
    }
}

function escapeDomValue(a_string) {
    return a_string
    .replace(/[\\]+[^']/g, '\\\\')
    .replace(/\\'/g, '\'')
    .replace(/&#39;/g, '\'')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '\\\'')
    .replace(/&apos;/g, '\\\'');
}
function escapeHtmlMarkers(a_string) {
    return a_string
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

/**
 * Convert url's within text to anchor tags with a given css class.
 * @param text - string that possibly contains url's to convert
 * @param cssClass - associated css class for the outputted anchor tags.  Use empty string if you don't want this.
 * @returns string of html that was the input text with urls converted to anchor tags
 */
function urlToAnchorTag(text, cssClass) {
    return Autolinker.link( text, {
        newWindow: true,
        urls: true,
        className: cssClass
    } );
}
