jQuery.noConflict();
var datashopHelpInfo = 'Please try again or contact '
    + '<a href="datashop-help@lists.andrew.cmu.edu">DataShop Help</a>'
    + ' if the problem persists.'
var LOG_CHANGE_OPTION = "Option changed";
var LOG_ADD_COMPONENT_CONNECTION = "Add component connection";
var LOG_DELETE_COMPONENT_CONNECTION = "Delete component connection";
var LOG_ADD_COMPONENT = "Add component";
var LOG_DELETE_COMPONENT = "Delete component";
var LOG_RENAME_COMPONENT = "Rename component";
var LOG_COPY_COMPONENTS = "Copy selected component(s)";
var LOG_OPEN_COMPONENT_OPTIONS = 'Open component options';
var LOG_SHOW_ALL_POTENTIAL_IMPORTS = 'Show all potential imports';
var LOG_HIDE_ALL_POTENTIAL_IMPORTS = 'Hide all potential imports';
var LOG_CLICK_COMPONENT_MENU = 'Expand/collapse component menu';

var fileUploadPattern = "Your import file";
var datasetFilePattern = "You have selected an existing dataset file";
var SELECTED_DATASET_FILE_MESSAGE = 'You have selected an existing dataset file. <ul><li>Users with access to that dataset will be '
    + 'able to view data and results if your workflow is shared.</li></ul>';
var UPLOADED_NEW_FILE_MESSAGE = 'Your import file is not currently attached to a dataset. <ul><li>Files attached to datasets ' +
'are available to users with access to the dataset even if the workflow is private.</li></ul>';

// No debug info text.
var NO_DEBUG_LOG_TEXT = "No matching rows"
// Once per workflow viewing, warn the user about sensitive data if they click the debug icon.
var confirmedSensitiveDataWarning = false;

// WorkflowAuthoring variables.
var WF_lastMouseClick = {
    x: 0,
    y: 0
};
// Shift and mouse-drag to select multiple components,
// or shift and click components.
var shiftDown = false;
var connectionColor = '#ffd58b';
var lastCall = "";
var globalWorkflowTimer = null;
var globalWorkflowTimerRefresh = 5000;
var globalDebugWindowTimer = null;
var globalDebugWindowTimerRefresh = 5000;
var statusIndicatorTimer = null;
var statusIndicatorHideDelay = 500;
// 4 secs min because creating connections on touchpads is a bit harder (slower) than with a regular mouse
var openPreviewDelay = 1500;
var closePreviewDelay = 1000;

var mouseoverNodeTimer = null;

var instance = null;
var workflowId = null;
var editorIsOpen = false;
var workflowIsRunning = false;
var startedRunningTime = 0;
var feedbackWorkflowId = null;
var unrequestedProjects = null;
var accessibleProjects = null;
var pendingRequestProjects = null;
var reRequestProjects = null;
var nonShareableProjects = null;
var hasUnownedPrivateFiles = null;
var isLoggedIn = true;
var componentTypeHierarchy = null;
var componentTitleMap = {};

/**
 * WorkflowResults.js updates component status icons,
 * populates the workflow message bar with component feedback,
 * and displays the results of a select component in the results div.
 */

/** A list of files for a given (displayed) component results subtab. */
var fileList = null;
var visIndex = 0;

/** The results message. */
var jsonMessage = null;
/** The results options info. */
var jsonOptions = null;
/** The results ordering info. */
var jsonComponentDepths = null;
/** The default or selected component results subtab. */

/** Whether or not errors were returned in the results. */
var errorsExist = null;
/** JQX Grids on the page. */
var gridCount = 1;

// WorkflowEditor variables.

var dirtyBits = {};
var DIRTY_FILE = 'DIRTY_FILE';
var DIRTY_ADD_CONNECTION = 'DIRTY_ADD_CONNECTION';
var DIRTY_DELETE_CONNECTION = 'DIRTY_DELETE_CONNECTION';
var DIRTY_OPTION = 'DIRTY_OPTION';
var DIRTY_SELECTION = 'DIRTY_SELECTION';

// String to look for in logs to get progress messages for components ex. "45% Complete"
var COMPONENT_PROGRESS_INDICATOR = '%Progress::';

var componentInputEndpoints = {};
var componentOutputEndpoints = {};
var componentOptionDependencies = {};
// Height and width set in editWorkflow function.
var workspaceWidth, workspaceHeight;

// Used in handling the layout offsets for drag-n-drop components.

// Zoom of the workspace;
var percentZoom = 1.0;

// Component height, width, and node placement coefficients
var componentWidth = 180;
var componentHeight = 100;
var componentHeightHalved = componentHeight / 2;
var componentHeightQuartered = componentHeight / 4;
jsPlumbInitialNodeOffset = 0.05;
var nodeGapCoeff = 0.33;
var componentHeightCoeff = 0.48;


var componentCounter = {};

var componentIdTypeMap = {};

/* Workflow diagram data saved in JavaScript variables. */
// Workflow data: components.
var currentDigraph = null;
// Workflow data: component options.
var componentOptions = {};
// Workflow data: datasets.
var wfDatasets = null;


/* Global variables for diagram control. */
// Whether or not the workflow diagram has been fully initialized
var isInitialized = false;
var isMessageBarInitialized = false;
// Keep track of whether we are connecting to or from a workflow component.
var in2out = false;
var out2in = false;

// Whether the user has clicked the Save button or modified the diagram.
var isWorkflowSaved = true;
var isWorkflowExecutionPending = false;

//These will be populated by an AJAX request to the servlet.
var allowedConnections = {};
allowedConnections['import'] = [];
allowedConnections['data'] = [];
allowedConnections['analysis'] = ['import', 'transform'];
allowedConnections['visualization'] = ['analysis', 'transform'];
allowedConnections['export'] = ['import', 'analysis', 'visualization', 'transform'];
allowedConnections['transform'] = ['import', 'analysis', 'visualization'];
allowedConnections['template'] = ['import', 'analysis', 'transform'];

// Define connectors and endpoints for jsPlumb components.
var leftAnchor0 = [ 0.03, 0.42, -1, 0 ];
var leftAnchor1 = [ 0.03, 0.752, -1, 0 ];
var leftAnchor2 = [ 0.03, 1.084, -1, 0 ];
var leftAnchor3 = [ 0.03, 1.416, -1, 0 ];

var rightAnchor0 = [ 0.997, 0.42, 1, 0 ];
var rightAnchor1 = [ 0.997, 0.752, 1, 0 ];
var rightAnchor2 = [ 0.997, 1.084, 1, 0 ];
var rightAnchor3 = [ 0.997, 1.416, 1, 0 ];
var specialComponentHeight = {};

var connectionDropOptions = {
    tolerance : 'touch',
    hoverClass : 'dropHover',
    activeClass : 'dragActive'
}, connectorPaintStyle = {
    lineWidth : 7,
    strokeStyle : '#ffd58b',
    joinstyle : 'round',
    outlineColor : 'none',
    outlineWidth : 2
}, connectorHoverStyle = {
    lineWidth : 7,
    strokeStyle : '#fda429',
    outlineWidth : 2,
    outlineColor : 'none'
}, endpointHoverStyle = {
    fillStyle : '#fda429',
    strokeStyle : '#fda429'
};

function deconstruct() {

    lastCall = "";
    clearInterval(globalWorkflowTimer);
    clearInterval(statusIndicatorTimer);

    dirtyBits = {};
    componentInputEndpoints = {};
    componentOutputEndpoints = {};


      componentCounter = {};

      componentIdTypeMap = {};

      currentDigraph = null;
      componentOptions = {};


      isInitialized = false;
      in2out = false;
      out2in = false;

      isWorkflowSaved = true;

      fileList = null;
      visIndex = 0;

      /** The results message. */
      jsonMessage = null;
      jsonOptions = null;
      jsonComponentDepths = null;
      /** Whether or not errors were returned in the results. */
      errorsExist = null;
      /** JQX Grids on the page. */
      gridCount = 1;

      jQuery('.advOptDialogClass').dialog('close');
      jQuery('#componentOutputDataPreview').dialog('close');
      jQuery('.process-component').remove();

      jQuery('#wfStatusBar').html('');
      jQuery('#messageBar').html('');
      jQuery('#process-div').html('<div class="noSelect component-selection-header"'
          + ' style="position: relative; left: 15px; top: 15px;">'
          + '<div id="workspaceTitle" class="workspaceTitle">Workspace</div>'
          + '</div>'
          + '<div id="wfDiagram"></div>');

      // Reset all jsplumb variables
      jsPlumb.reset();
      // Then reload the workflows list.
      forwardToWorkflowList();
  }


// Function to initialize and populate the Workflow Diagram.
function editWorkflow(json, lastErrorMessage, isDsAdmin, wfState, componentInfoDivs, componentMenuXml) {

    // Workflow diagram position attributes.
    var windowWidth = jQuery(window).width();
    var maxWidth = windowWidth; // this will make the dialog width 80% of the screen
    var windowHeight = jQuery(window).height();
    var maxHeight = windowHeight; // this will make the dialog height 80% of the screen
    window.scrollTo(0, 0); // scrolls windows to top

    var isBlankWorkflow = false;


    var isSaveAsNew = false;

    // Workflow diagram data.

    var workflowName = null;
    var lastUpdated = null;
    var isShared = null;
    var workflowXml = null;
    var components = [];
    var isView = false;

    // Instantiate the workflow diagram data, as well as the currentDigraph object
    // with workflow data from the database.

    var previousResultsExist = false;

    if (wfState == "running") {
        workflowIsRunning = true;
    }

    if (json !== undefined) {

        editorIsOpen = true;
        if ((json.isView != null) && (json.isView == 'true')) {
            isView = true;
        }

        if (json !== undefined && json.isSaveAsNew !== undefined) {
            isSaveAsNew = json.isSaveAsNew;
        }

        if (json !== undefined && json.isBlankWorkflow !== undefined) {
            isBlankWorkflow = json.isBlankWorkflow;
        }

        // Add component tree to the LHS component menu.
        if (componentMenuXml !== undefined) {
            addComponentTreeLhsMenu(componentMenuXml, componentInfoDivs, isView);
        }

        if ((json.isLoggedIn != null) && (json.isLoggedIn == 'true')) {
            isLoggedIn = true;
        }

        if (json.feedbackId != null) {
            feedbackWorkflowId = json.feedbackId;
        }

        if (json.isShared != null) {
            isShared = json.isShared;
        }

        if (json.status !== undefined && json.status == "empty") {
            var dialogHtml = 'Your request cannot be processed. Please contact us if you feel you received this message in error.';
            wfInfoDialog('noInfo', dialogHtml, 'Error');
            return;
        }

        if (json.workflow != null && json.workflow != '') {

            workflowId = json.workflow.id;
            workflowName = json.workflow.name;
            lastUpdated = json.workflow.lastUpdated;
            components = json.workflow.components;

            if (json.componentInputEndpoints !== undefined) {
                componentInputEndpoints = JSON.parse(json.componentInputEndpoints.toLowerCase());
            }
            if (json.componentOutputEndpoints !== undefined) {
                componentOutputEndpoints = JSON.parse(json.componentOutputEndpoints.toLowerCase());
            }
            if (json.fullOptionConstraintMap !== undefined) {
                componentOptionDependencies = JSON.parse(json.fullOptionConstraintMap);
            }

            if (json.results !== undefined && json.results != 'null') {
                previousResultsExist = true;
            }

            var digraphObject = new Object();
            digraphObject.name = workflowName;
            digraphObject.id = workflowId;
            digraphObject.components = [];
            digraphObject.isShared = isShared;
            digraphObject.lastUpdated = lastUpdated;
            digraphObject.isView = isView;
            digraphObject.annotations = [];

            // Update global 'wfDatasets' var.
            updateWfDatasets(json);

            currentDigraph = digraphObject;
        }


        // Workflow exists in database.
        if (workflowId != null && workflowName != null && lastUpdated != null) {


            /**
             * Check for workflows_detached.jsp,
             * IF corresponding class 'detached' is present on div#main apply
             * correct top position parameters to dialog
             */
            var dialogTopPositioning = 123;
            if(jQuery('#main').hasClass('detached')){
                dialogTopPositioning = 70;
            }

            var theButtons = new Array();

            /**
             * Workflow sizing parameters
             */
            jQuery('#workflow-main').dialog({
                resizable : false,
                draggable: false,
                hide: 'fade',
                dialogClass: 'uiTitleBar',
                width : '100vw',
                height : 0,
                collision: 'none',
                title : 'Workflow: ' + workflowName,
                position : { my: "center", at: "top+74", of: window }, //['center', 100 ] ,//
                closeOnEscape: false,
                buttons: theButtons,
                open: function(event, ui) {
                    //jQuery(this).parent().css('top', '74px !important');
                    jQuery(this).parent().css('height', '100vh');
                    jQuery('#workflow-main').css('height', '96vh');
                },
                create : function(event, ui) {
                    jQuery(event.target).parent().css('position', 'fixed');
                    jQuery('#workflow-main').append(jQuery(
                        '<img alt="Close workflow" id="closeWorkflowsButton" src="css/images/close.svg" />'));

                    jQuery('#closeWorkflowsButton').click(function() {
                        // Gets called even if workflowId is invalid
                        saveAndCloseCurrentWorkflow(workflowId, null)
                    });

                    if (isLoggedIn) {
                        unrequestedProjects = jQuery('body').data("unrequestedProjects");
                        accessibleProjects = jQuery('body').data("accessibleProjects");
                        pendingRequestProjects = jQuery('body').data("pendingRequestProjects");
                        nonShareableProjects = jQuery('body').data("nonShareableProjects");
                        reRequestProjects = jQuery('body').data("reRequestProjects");
                        hasUnownedPrivateFiles = jQuery('body').data("hasUnownedPrivateFiles");
                        globalWfPaperCount = jQuery('body').data("paperCount");

                        var hasRequestableProjects = false;
                        if (getObjectCount(unrequestedProjects) > 0
                                || getObjectCount(pendingRequestProjects) > 0 || getObjectCount(reRequestProjects) > 0) {
                            hasRequestableProjects = true;
                        }

                        var isLocked = false;
                        if (hasUnownedPrivateFiles || getObjectCount(nonShareableProjects) > 0) {
                            isLocked = true;
                        }

                        if (isView && isLocked && !hasRequestableProjects) {
                            var lockedString = null;
                            var lockedTitle = null;

                            if (getObjectCount(accessibleProjects) > 0) {
                                // Locked *
                                lockedString = "Locked*";
                                lockedTitle = 'This workflow contains both private and accessible data.';
                            } else {
                                // No access-granted projects and no requestable projects
                                lockedString = "Locked";
                                lockedTitle = 'This workflow contains private data.';
                            }
                            var rowIndex = 0;
                            jQuery('#wfStatusBar').append(jQuery(
                                '<a id="wfAccessRequestPrivateRow_' + rowIndex + '" class="wfAccessRequestPrivateLink wfAccess locked"'
                                    + 'title="' + lockedTitle + '">' + lockedString + '</a>'));
                        } else {

                            var accessibleCount = getObjectCount(accessibleProjects);
                            var hasUnrequestedProjects = false;
                            var uprCount = getObjectCount(unrequestedProjects);
                            var reRequestCount = getObjectCount(reRequestProjects);
                            var projectNameBuffer = '';
                            var projectIdBuffer = '';
                            var totalRequestCount = uprCount + reRequestCount;
                            if (uprCount > 0 || reRequestCount > 0) {
                                var thisCount = 0;
                                hasUnrequestedProjects = true;
                                if (uprCount > 0) {
                                jQuery.each(unrequestedProjects, function(pIndex, projName) {
                                    projectNameBuffer = projectNameBuffer + projName;
                                    projectIdBuffer = projectIdBuffer + pIndex;
                                    if (thisCount < totalRequestCount -1) {
                                        projectNameBuffer = projectNameBuffer + ', ';
                                        projectIdBuffer = projectIdBuffer + ',';
                                    }
                                    thisCount++;
                                } );
                                }
                                if (reRequestCount > 0) {
                                jQuery.each(reRequestProjects, function(pIndex, projName) {
                                    projectNameBuffer = projectNameBuffer + projName;
                                    projectIdBuffer = projectIdBuffer + pIndex;
                                    if (thisCount < totalRequestCount -1) {
                                        projectNameBuffer = projectNameBuffer + ', ';
                                        projectIdBuffer = projectIdBuffer + ',';
                                    }
                                    thisCount++;
                                } );
                                }
                                var buttonLabel = null;
                                if (uprCount > 0) {
                                    buttonLabel = "Request";
                                    if (isLocked) {
                                        buttonLabel = "Request*";
                                    }
                                } else {
                                    buttonLabel = "Re-Request";
                                    if (isLocked) {
                                        buttonLabel = "Re-Request*";
                                    }
                                }
                                var rowIndex = 0;
                                if (projectNameBuffer != '') { //alert(projectNameBuffer);
                                    jQuery('#wfStatusBar').append(jQuery(
                                        '<a id="wfAccessRequestRow_' + rowIndex + '" class="wfAccessRequestLink wfAccess request"'
                                        + 'title="Access Required: ' + projectNameBuffer + '">' + buttonLabel + '</a>'
                                        + '<input type="hidden" id="wfAccessRequestProjectList_' + rowIndex + '" value="' + projectIdBuffer + '" />'));
                                }
                            }

                            var prCount = getObjectCount(pendingRequestProjects);
                            projectNameBuffer = '';
                            projectIdBuffer = '';
                            if (prCount > 0 && !hasUnrequestedProjects) {
                                var thisCount = 0;
                                jQuery.each(pendingRequestProjects, function(pIndex, projName) {
                                    projectNameBuffer = projectNameBuffer + projName;
                                    projectIdBuffer = projectIdBuffer + pIndex;
                                    if (thisCount < prCount -1) {
                                        projectNameBuffer = projectNameBuffer + ', ';
                                        projectIdBuffer = projectIdBuffer + ',';
                                    }
                                    thisCount++;
                                } );
                                var rowIndex = 0;
                                if (projectNameBuffer != '') { //alert(projectNameBuffer);
                                    jQuery('#wfStatusBar').append(jQuery(
                                            '<a href="AccessRequests" target="_blank">'
                                            + '<a id="wfAccessRequestPendingRow_' + rowIndex + '" class="wfAccessRequestPendingLink wfAccess pending"'
                                            + 'title="Pending Requests: ' + projectNameBuffer + '" >Pending</a>'
                                            + '</a>'));
                                }
                            }
                        }
                    }
                },
                close : function() {
                    // Gets called even if workflowId is invalid
                    saveAndCloseCurrentWorkflow(workflowId, null);
                }
            });

            /*jQuery('.nav-sidebar').accordion({
                heightStyle: "content",
                autoHeight: false,
                clearStyle: true,
                collapsible: true,
                icons: { "header": "ui-icon-triangle-1-w", "activeHeader": "ui-icon-triangle-1-s" }
            });*/


            // jQuery Enscroll plugin
            /*jQuery('.advOptDialogClass').enscroll({
                verticalTrackClass: 'track1',
                verticalHandleClass: 'handle1',
                drawScrollButtons: true,
                scrollUpButtonClass: 'scroll-up1',
                scrollDownButtonClass: 'scroll-down1',
                addPaddingToPane: true,
                pollChanges: true
            });
            jQuery('#process-selector-div').enscroll({
                verticalTrackClass: 'track1',
                verticalHandleClass: 'handle1',
                drawScrollButtons: true,
                scrollUpButtonClass: 'scroll-up1',
                scrollDownButtonClass: 'scroll-down1',
                addPaddingToPane: false,
                pollChanges: true
            }); */


            // Turn off scroll bar on document
            jQuery('html, body').css({
                'overflow': 'hidden',
                'height': '100%'
            });

            // Global vars for zoom
            workspaceWidth = jQuery('#process-div').width();
            workspaceHeight = jQuery('#process-div').height();


            /* jsPlumb diagram. */
            instance = jsPlumb.getInstance({
                DragOptions : {
                    cursor : 'pointer',
                    zIndex : 1000
                },
                Container : 'process-div'
            });

            if (!isView) {
                /* jQuery Draggable workflow components (left-hand components menu). */
                jQuery('.draggableComponent').draggable({
                    scroll: true,
                    containment : '#body',
                    cursor : 'move',
                    snap : '#process-div',
                    helper : 'clone',
                    appendTo: 'body',
                    start: function(event) {
                        WF_lastMouseClick.x = event.clientX;
                        WF_lastMouseClick.y = event.clientY;
                    },

                    drag: function(event, ui) {


                        var original = ui.originalPosition;

                        ui.position = {
                            left: event.clientX - WF_lastMouseClick.x  + original.left,
                            top:  event.clientY - WF_lastMouseClick.y  + original.top
                        };

                    }
                });
                jQuery('.draggableComponent').mouseup(function(e) {
                    in2out = false;
                    out2in = false;
                });
                jQuery('.draggableComponent').mousedown(function(e) {
                    in2out = false;
                    out2in = false;

                });
            }


            /* Initializing the diagram with jsPlumb batch function. */
            instance.batch(connectionHandler);

            var annotations = json.workflow.annotations;
            loadAnnotationFromDB(annotations);

            // Populate the jsPlumb diagram with components from the database.
            if ((components !== undefined && components != null) &&
                (components.component !== undefined && components.component != null)) {
                // If only one component exists, ensure that JavaScript treats it as an array.
                if (!jQuery.isArray(components.component)) {
                    components.component = [ components.component ];
                }

                // Using the info from the database, manipulate the diagram nodes.
                if (components.component.length > 0) {
                    jQuery.each(components.component, function() {
                        var restrictedAccessTag = false;
                        var componentTypeId = this.component_type.toLowerCase();
                        var componentTitleClass = 'componentTitle';
                        var componentTitleText = componentTitleMap[this.component_name.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase()];

                        var mainComponentPanelText = this.component_id_human + '';

                        if (this.restricted !== undefined) {
                            // component is restricted, user cannot access
                            restrictedAccessTag = true;
                        }

                        if (restrictedAccessTag && isView) { // !isView is not needed here, but consistency wins.
                            componentTitleClass = 'componentTitleLocked';
                            componentTitleText = componentTitleText + ' (Locked)';
                        }

                        // Create the process component (AKA div, AKA node).
                        jQuery('<div/>', {
                            id : this.component_id
                        }).appendTo('#process-div');
                        var processComponent = jQuery('#' + this.component_id);
                        var id = this.component_id;
                        // The inset title-- e.g., Analysis, Import, etc.
                        jQuery('<div/>', {
                            id : 'title_' + this.component_id,
                            class : componentTitleClass,
                            title : componentTitleText,
                            text : componentTitleText
                        }).appendTo('#' + id);

                        jQuery('<span/>', {
                            class : 'compName',
                            text : this.component_name
                        }).appendTo('#' + id);

                        componentIdTypeMap[this.component_id] = this.component_type.toLowerCase();

                        jQuery('<div id="componentSubtitleEdit_' + this.component_id
                            + '" class="componentSubtitle"><div id="humanReadableId_' + this.component_id + '" class="componentSubtitle" >'
                                + mainComponentPanelText + '<div class="renameWorkflowIcon" id="renameIcon_' + this.component_id
                                + '"><img src="images/pencil.png" /></div></div></div>')
                            .appendTo('#' + id);

                        initRenameComponentToolTip(this.component_id);

                        var componentStatusDiv = jQuery('<div class="componentStatusDiv" id="componentStatusDiv_' + id + '" >'
                                + '<span id="componentStatus_'+ id + '" ></span>'
                                + '</div>');
                        componentStatusDiv.appendTo(processComponent);

                        // Add the preview and debug icons
                        var componentPreviewDiv = jQuery('<span class="openComponentPreviewIcon" id="componentPreview_'
                                + processComponent.attr('id') + '"></span>');

                        if (!restrictedAccessTag || !isView) {
                           // component is not restricted, user can access
                           componentPreviewDiv.appendTo(processComponent);
                        }

                        if (!restrictedAccessTag && (!isView || isDsAdmin)) {
                            // Add the component debug info icon
                            var componentDebugInfoDiv = jQuery('<span class="openComponentDebugInfoIcon" id="componentDebug_'
                                + processComponent.attr('id') + '"></span>');
                            componentDebugInfoDiv.appendTo(processComponent);
                        }
                        // Add the preview form
                        var dsId = null;
                        var resultsForm = jQuery('<form id="requestResults_' + this.component_id + '" method="post" action="WorkflowResults" target="_blank">'
                            + '<input type="hidden" name="requestingMethod" value="WorkflowResultsServlet.displayPreviousResults" />'
                            + '<input type="hidden" name="workflowId" value="' + currentDigraph.id + '" />'
                            + '<input type="hidden" name="datasetId" value="' + dsId + '" />'
                            + '<input type="hidden" name="componentId" value="' + this.component_id + '" />'
                            + '<input type="hidden" name="componentName" value="' + this.component_name + '" />'
                            + '<input type="hidden" name="componentType" value="' + this.component_type + '" />'
                            + '</form>');

                            jQuery(resultsForm).appendTo(processComponent);

                         // The options gear icon.
                         if (!restrictedAccessTag || !isView) {
                            jQuery('<span class="openComponentOptionsGear" id="componentOptions_'
                                    + this.component_id
                                    + '" ></span>').appendTo(processComponent);
                         }

                        if (!isView) {
                            jQuery('<span class="deleteComponentIcon"></span>').appendTo('#' + id);
                        }

                        // The delete component icon.
                        if (!restrictedAccessTag || !isView) {
                            jQuery('#componentPreview_' + this.component_id).click(requestResultsInNewWindow);
                            jQuery('#componentDebug_' + this.component_id).click(requestDebugInfoFromButton);
                            jQuery('#renameIcon_' + this.component_id).on('click', renameHumanReadableId);
                        }

                        jQuery('#componentOptions_' + this.component_id).click(openAdvancedOptionsDialog);

                        // Manipulate the component node.
                        jQuery(processComponent).attr('id', this.component_id);
                        jQuery(processComponent).attr('name', this.component_type);
                        jQuery(processComponent).attr('class', 'process-component');
                        jQuery(processComponent).css('position', 'absolute');
                        jQuery(processComponent).css('width', componentWidth + 'px');
                        jQuery(processComponent).css('height', componentHeight + 'px');
                        jQuery(processComponent).css('left', this.left + 'px');
                        jQuery(processComponent).css('top', this.top + 'px');

                        /* jQuery Draggable workflow components. */
                        // These components are created from a previously saved workflow.
                        // and placed into the workspace when opening the editor.
                        jQuery(processComponent).draggable({
                            scroll: true,
                            containment : '#process-div',
                            cursor : 'move',
                            snap : '#process-div',
                            start: startFix,
                            drag: dragFix
                        });

                        jQuery(processComponent).mouseup(function(e) {
                            in2out = false;
                            out2in = false;
                        });

                        // Allow the user to save the new position of the component
                        moveComponentFunctionality(processComponent);

                        // Component options were found; Store them in the global variable, componentOptions.
                        if (this.options !== undefined) {
                            componentOptions[this.component_id] = this.options;
                        }
                        // Advanced component options
                        initComponentOptionsDiv(processComponent);


                    }); // end of for-each component
                }

                // Save all of the connection and option info to the global variable, currentDigraph.
                var digraphObject = new Object();
                digraphObject.name = workflowName;
                digraphObject.id = workflowId;
                digraphObject.lastUpdated = lastUpdated;
                digraphObject.isShared = isShared;
                digraphObject.components = [];
                digraphObject.isView = isView;
                digraphObject.annotations = currentDigraph.annotations;
                currentDigraph = digraphObject;

                // Add Javascript object connections
                if (components.component.length > 0) {
                    jQuery.each(components.component, function() {
                        var thisComponent = this;
                        var componentObject = new Object();
                        componentObject.workflow_id = this.workflow_id;
                        componentObject.component_id = this.component_id;
                        componentObject.component_id_human = this.component_id_human;
                        componentObject.component_type = this.component_type;
                        componentObject.component_name = this.component_name;
                        componentObject.top = this.top;
                        componentObject.left = this.left;
                        componentObject.connections = new Object();
                        componentObject.connections.connection = [];
                        componentObject.options = [];
                        componentObject.inputs = [];
                        componentObject.outputs = [];
                        componentObject.errors = [];

                        if (componentOptions[componentObject.component_id] !== undefined) {
                            componentObject.options = componentOptions[componentObject.component_id];
                        }

                        if (this.connections != null && this.connections.connection != null) {
                            if (!jQuery.isArray(this.connections.connection)) {
                                this.connections.connection = [ this.connections.connection ];
                            }
                            // For each connection
                            if (this.connections.connection.length > 0) {
                                jQuery.each(this.connections.connection, function(index, value) {
                                    var connection = null;
                                    if (value.to !== undefined && value.to != this.component_id) {
                                        connection = {
                                            'to' : value.to,
                                            'index' : value.index,
                                            'frindex' : value.frindex
                                        };

                                    } else if (value.from !== undefined && value.from != thisComponent.component_id) {
                                        connection = {
                                            'from' : value.from,
                                            'index' : value.index,
                                            'tindex' : value.tindex
                                        };
                                    }
                                    if (connection != null) {
                                        componentObject.connections.connection.push(connection);
                                    }
                                });
                            }
                        }

                        currentDigraph.components.push(componentObject);

                    });
                }
                var toNodeCount = {};
                var fromNodeCount = {};


                // Setup component endpoints (input/output nodes)

                jQuery.each(components.component, function() {
                    var minComponentHeight = componentHeight;
                    var thisComponent = this;

                    var endpointsKey = thisComponent.component_type.toLowerCase()
                    + "-" + thisComponent.component_name.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase();
                    var id = thisComponent.component_id;


                    // Handle output endpoints


                    if (componentOutputEndpoints[endpointsKey] !== undefined && componentOutputEndpoints[endpointsKey] > 0) {
                        var newComponentHeight = componentHeight;
                        var newComponentHeightCoeff = componentHeightCoeff;
                        var heightFromOutputs = componentOutputEndpoints[endpointsKey] * componentHeightQuartered + componentHeightHalved;
                        if (heightFromOutputs > newComponentHeight) {
                            newComponentHeight = heightFromOutputs;
                            newComponentHeightCoeff = jsPlumbInitialNodeOffset + componentHeightCoeff / componentOutputEndpoints[endpointsKey];
                            if (newComponentHeight > minComponentHeight) {
                                minComponentHeight = newComponentHeight;
                            }
                        }
                        var newNodeGapCoeff = nodeGapCoeff;
                        // Decrease gap between nodes if there are more than 3 outputs.
                        if (componentOutputEndpoints[endpointsKey] > 3) { newNodeGapCoeff = 0.23; }

                        var fillStyle = connectionColor;
                            var offsetMultiplier = 0;
                            for (var outIndex = 0 ; outIndex < componentOutputEndpoints[endpointsKey]; outIndex++) {



                                    var outputLabel = componentOutputEndpoints[endpointsKey + '_list'][outIndex];
                                    var newAnchor = [ 0.997, newComponentHeightCoeff + (offsetMultiplier * newNodeGapCoeff), 1, 0 ];

                                    var sourceEndpoint = {
                                            endpoint : 'Dot',
                                            uniqueEndpoint : true,
                                            paintStyle : {
                                                strokeStyle : fillStyle,
                                                fillStyle : 'transparent',
                                                radius : 12,
                                                lineWidth : 4
                                            },
                                            deleteEndpointsOnDetach: false,
                                            isSource : true,
                                            maxConnections : -1,
                                            connector : [ 'Flowchart', {
                                                stub : [ 10, 10 ],
                                                gap : 14,
                                                cornerRadius : 15,
                                                alwaysRespectStubs : true
                                            } ],
                                            connectorStyle : connectorPaintStyle,
                                            hoverPaintStyle : endpointHoverStyle,
                                            connectorHoverStyle : connectorHoverStyle,
                                            dragOptions : {},
                                            dropOptions : {},
                                            overlays : [ [ 'Label', {
                                                location : [ 1.25 + 0.07 * outputLabel.length, 0 ],
                                                label : outputLabel,
                                                cssClass : 'endpointSourceLabel'
                                            } ] ]
                                    };

                                    // The source endpoint (Out connector).
                                    var mySource = jsPlumb.addEndpoint(id, {
                                        uuid : id + "_" + outIndex + '_source',
                                        anchor : newAnchor,
                                        connectorOverlays : [ [ 'Arrow', {
                                            width : 15,
                                            length : 30,
                                            location : 1,
                                            id : 'arrow'
                                        } ], [ 'Label', {
                                            label : '',
                                            id : 'label'
                                        } ] ],
                                        events:{
                                            // Display all node labels when hovering over a node
                                            mouseout:function(labelOverlay, originalEvent) {
                                                jQuery('.endpointSourceLabel, .endpointTargetLabel').removeClass('endpointHover');
                                            },
                                            mouseover:function(labelOverlay, originalEvent) {
                                                jQuery('.endpointSourceLabel, .endpointTargetLabel').addClass('endpointHover');
                                            }
                                        }
                                    }, sourceEndpoint);
                                    setupSourceEndpoint(mySource, id, outIndex);
                                    offsetMultiplier += 1;

                            }
                    }


                    if (componentInputEndpoints[endpointsKey] !== undefined && componentInputEndpoints[endpointsKey] > 0) {
                        var newComponentHeight = componentHeight;
                        var newComponentHeightCoeff = componentHeightCoeff;
                        var fillStyle = connectionColor;
                        var heightFromOutputs = componentInputEndpoints[endpointsKey] * componentHeightQuartered + componentHeightHalved;
                        if (heightFromOutputs > newComponentHeight) {
                            newComponentHeight = heightFromOutputs;
                            newComponentHeightCoeff = jsPlumbInitialNodeOffset + componentHeightCoeff / componentInputEndpoints[endpointsKey];
                            if (newComponentHeight > minComponentHeight) {
                                minComponentHeight = newComponentHeight;
                            }
                        }

                        var newNodeGapCoeff = nodeGapCoeff;
                        // Decrease gap between nodes if there are more than 3 inputs.
                        if (componentInputEndpoints[endpointsKey] > 3) { newNodeGapCoeff = 0.23; }

                            for (var inIndex = 0 ; inIndex < componentInputEndpoints[endpointsKey]; inIndex++) {

                                var inputLabel = componentInputEndpoints[endpointsKey + '_list'][inIndex];
                                var newAnchor = [ 0.03, newComponentHeightCoeff + (inIndex * newNodeGapCoeff), -1, 0 ];

                                var specifiedTargetEndpoint = {
                                    endpoint : 'Dot',
                                    uniqueEndpoint : true,
                                    paintStyle : {
                                        fillStyle : fillStyle,
                                        radius : 15
                                    },
                                    deleteEndpointsOnDetach: false,
                                    hoverPaintStyle : endpointHoverStyle,
                                    dropOptions : connectionDropOptions,
                                    dragOptions : {},
                                    isTarget : true,
                                    maxConnections : -1,
                                    overlays : [ [ 'Label', {
                                        location : [ -0.1 * inputLabel.length, 0 ],
                                        label : inputLabel,
                                        cssClass : 'endpointTargetLabel'
                                    } ] ]
                                };

                                var myTarget = jsPlumb.addEndpoint(id, {
                                    uuid : id + "_" + inIndex  + '_target',
                                    anchor : newAnchor,
                                    events:{
                                        // Display all node labels when hovering over a node
                                        mouseout:function(labelOverlay, originalEvent) {
                                            jQuery('.endpointSourceLabel, .endpointTargetLabel').removeClass('endpointHover');
                                        },
                                        mouseover:function(labelOverlay, originalEvent) {
                                            jQuery('.endpointSourceLabel, .endpointTargetLabel').addClass('endpointHover');
                                        }
                                    }
                                }, specifiedTargetEndpoint);

                            }
                    }
                    jQuery('#' + id).css('height', minComponentHeight + 'px');
                    specialComponentHeight[id] = minComponentHeight;

                }); // end of for each component

                var currentAnchors = null;
                // Configure and style jsPlumb connections/connectors.
                if (components.component.length > 0) {
                    drawingMultipleConnections = true;
                    drawConnections(components.component);
                    drawingMultipleConnections = false;
                }

                jsPlumb.setContainer("process-div");

                // Ensure that the component endpoints have labels if not connected
                handleDisplayInputOutputTags();

            } else {
                // No components exist in the process-div.
                jQuery('<div id="workspaceHint">Drag components into the workspace to begin.</div>')
                        .appendTo('#process-div');
            }

            // Add additional features to the Workflow dialog button pane (top div)
            buttonPaneDivs(previousResultsExist, isView, isDsAdmin);


            // Disable all text selection on the diagram, itself, and change pointers for mouse-over.
            addDiagramStyleOptions();

            // Make delete icon click-able.
            jQuery('.deleteComponentIcon').click(deleteComponentConfirm);

            if (!workflowIsRunning) {
                loadPreviousResults();
            }
            // If current workflow is valid, start the component status monitor.
            // ** See WorkflowAuthoring.js for function "monitorComponentStatus()"
            wfDisableSaveButton();

            lastCall = "preload";
            // This handles pre-processing so that the latest meta-data
            // is used to populate options
            if (!isView) {
                preloadWorkflow(currentDigraph.id, isSaveAsNew);
            }

            isWorkflowSaved = true;

            if (lastErrorMessage !== undefined && lastErrorMessage != null
                    && lastErrorMessage.component_message_map !== undefined) {
                handleErrorMap(lastErrorMessage);
            }

            if (isView) {
                jQuery('.deleteComponentIcon').css('cursor', 'grab');
            }

            // Do once after workflow has been initialized
            isInitialized = true;

            // jsPlumb repaint diagram.
            jsPlumb.repaintEverything();

            if (isView) {
                monitorComponentStatus();
            }

        } // end of if workflowId != null


        wfHideStatusIndicator();

        // A WF has been loaded so update the Help content.
    //        updateHelpContent();

    } // end of if json !== undefined
    jQuery(window).resize(resetWorkflowSizes);
    resetWorkflowSizes();

    // Initialize the component area selector (includes shift+click or shift+area_select)
    initComponentAreaSelector();

} // end of editWorkflow function

function updateWfDatasets(json) {
    wfDatasets = [];
    if (json.datasets !== undefined && json.datasets != 'null' && Object.keys(json.datasets).length != 0) {
        wfDatasets = json.datasets.dataset;
        if (!jQuery.isArray(wfDatasets)) { wfDatasets = [ wfDatasets ]; }
    }
}

function updateHelpContent() {

    if (theHelpWindow == undefined) {
        theHelpWindow = new HelpWindow.Base("helpButton");
    }

    // Help content should now be specific to WF create/edit.
    theHelpWindow.updateContent($("help-workflows"));
}

/**
 * Function to make sure that widths and offsets are correct for screen sizes
 */
function resetWorkflowSizes() {
    // Set the left offset of the workflow editor (because the left hand panel is either 230px or 16.5%)
    let leftHandMenuWidth = jQuery('#process-selector-div').width();
    jQuery('#wfStatusBar').css('left', leftHandMenuWidth);
    jQuery('#process-div').css('left', leftHandMenuWidth);
    jQuery('#wfMessageBar').css('left', leftHandMenuWidth);

    // Set the width which is the size of the document minus the left hand bar
    // Don't forget to account for zoom
    let workflowEditorWidth = jQuery(document).width() - leftHandMenuWidth;

    jQuery('#wfStatusBar').css('width', workflowEditorWidth);
    jQuery('#wfMessageBar').css('width', workflowEditorWidth - parseInt(jQuery('#wfMessageBar').css('padding-left')) -
        parseInt(jQuery('#wfMessageBar').css('padding-right'))); //Subtract padding

    jQuery('#process-div').css('width', (81/percentZoom) + 'vw');
    jQuery('#process-div').css('height', (70/percentZoom) + 'vh');
}

// Adds additional features to the Workflow dialog button pane (bottom div).
function buttonPaneDivs(previousResultsExist, isView, isDsAdmin) {
    // The Shareable radio button and Last Updated value.
    var isSharedChecked = '', isPrivateChecked = '';
    if (currentDigraph.isShared == 'true') {
        isSharedChecked = 'checked="true"';
        isPrivateChecked = '';
    } else {
        isSharedChecked = '';
        isPrivateChecked = 'checked="true"';
    }
    var lastUpdated = currentDigraph.lastUpdated;

    // Title Bar
    var wfTitle = jQuery('<div id="wfTitleBar"><span>'+ (currentDigraph.name) +'</span>'
            + '<div id="wf_datasets_link"></div></div>');
    if (wfDatasets.size() > 0) {
        wfTitle = jQuery('<div id="wfTitleBar"><span>'+ (currentDigraph.name)
                         +'</span>'
                         + '<div id="wf_datasets_link"><a href="javascript:openWfDatasetsDialog('
                         + workflowId + ')">datasets</a></div>'
                         + '</div>');
    }
    wfTitle.appendTo('#wfStatusBar');

    var wfStatusInfoDiv = jQuery('<div id="wfStatusInfoDiv" />');
    if (!isView) {

        jQuery(
            '<div id="isSharedOptionEditor"><span class="wfInfoLabel">Accessibility: </span>'
                    + '<input type="radio" id="editorIsSharedRadioId" name="isSharedRadioButton" value="true" '
                    + isSharedChecked
                    + ' />'
                    + '<label class="optionLabel" for="editorIsSharedRadioId">Public</label>'
                    + '<input type="radio" id="editorIsPrivateRadioId" name="isSharedRadioButton" value="false" '
                    + isPrivateChecked + '/>'
                    + '<label class="optionLabel" for="editorIsPrivateRadioId">Private</label></div>')
            .appendTo(wfStatusInfoDiv);
    }


    // The button panel
    var wfExecutionDiv = jQuery('<div id="wfExecutionDiv" />');
    wfExecutionDiv.appendTo('#wfStatusBar');

    // The status div
    jQuery('<div id="lastUpdatedDiv"><span class="wfInfoLabel">Last updated: </span>'
                    + '<span id="lastUpdatedLabel">' + lastUpdated + '</span></div>').appendTo(
            wfStatusInfoDiv);
    wfStatusInfoDiv.appendTo('#wfStatusBar');
    jQuery('input[name|="isSharedRadioButton"]').click(updateSharedSelection);

    var titleString = "Processing";
    previousMiniIcon = titleString;
    jQuery('#componentStatusIcon').remove();
    jQuery('<div/>', {
        id : 'componentStatusIcon'
    }).appendTo('#wfStatusInfoDiv').html(
        '<span class="wfInfoLabel">Status: </span>' +
        '<span class="wfStatusLabel">' + titleString + '<img src="images/waiting.gif" />'
            + '</span>');

    // Fixes issue with hidden scroll bar in the top status pane
    ////var updatedStatusBarWidth = jQuery(document).width() - jQuery('#process-selector-div').width()
    ////    - getScrollBarWidth();
    ////jQuery('#wfStatusBar').css('width', updatedStatusBarWidth);

    /*jQuery('#wfStatusBar').enscroll({
        verticalTrackClass: 'track1',
        verticalHandleClass: 'handle1',
        drawScrollButtons: false,
        scrollUpButtonClass: 'scroll-up1',
        scrollDownButtonClass: 'scroll-down1',
        addPaddingToPane: false,
        showOnHover: true,
        pollChanges: false
    });*/

    // The Workflow buttons
    if (!isView) {

        // 'Link' button
        addExecutionButton(wfExecutionDiv, "Link", "linkWorkflowButton", "ui-icon-wf-link-button");

        showLinkMenu();

        // 'Save' button
        addExecutionButton(wfExecutionDiv, "Save", "saveWorkflowButton", "ui-icon-wf-save-button");
        jQuery('#saveWorkflowButton').click(function() {
            lastCall = "saveButtonPress";
            saveOpenOptions();
            saveCurrentWorkflow(workflowId, null, null, null, null);
        });
    } else {
        // Add papers link if isView and papers exist.
        if (globalWfPaperCount != null && globalWfPaperCount > 0) {
            addExecutionButton(wfExecutionDiv, "Papers", "showPapers", "ui-icon-wf-paper-button");
            jQuery('#showPapers').bind('click', function(e) {
                openLinkPaperDialog();
            });

        }

    }

    // 'Save As' button
    addExecutionButton(wfExecutionDiv, "Save As", "saveAsWorkflowButton", "ui-icon-wf-save-as-button");
    jQuery('#saveAsWorkflowButton').click(function() {
        var viewWorkflowId = workflowId;
        if (workflowId == null || jQuery.isEmptyObject(workflowId)) {
            viewWorkflowId = feedbackWorkflowId;
        }
        wfSaveAsNewWorkflow(viewWorkflowId);
    });

    if (!isView) {
        // 'Run' button
        addExecutionButton(wfExecutionDiv, "Run", "wfRunButton", "ui-icon-wf-run-button");
        jQuery('#wfRunButton').click(runWorkflow);
    }

    var dsId = null;
    var resultsForm = '<form id="requestResults" method="post" action="WorkflowResults" target="_blank">'
        + '<input type="hidden" name="requestingMethod" value="WorkflowResultsServlet.displayPreviousResults" />'
        + '<input type="hidden" name="workflowId" value="' + currentDigraph.id + '" />'
        + '</form>';


    // Results window button
    jQuery('<div class="wfBlueButton" id="wfResults"><span class="wfBtnText">Results</span></div>').appendTo(wfExecutionDiv);
    jQuery(resultsForm).appendTo(wfExecutionDiv);
    jQuery('#wfResults').button({
        icons : {
            primary : "ui-icon-wf-results-button ui-icon-wf-std-button"
        },
        text: true
    });

    if (!previousResultsExist) {
        jQuery('#wfResults').button({
            disabled: true
        });

        jQuery('.openComponentPreviewIcon').css('opacity', '0.4');
        jQuery('.openComponentDebugInfoIcon').css('opacity', '0.4');
    }

    jQuery('#wfResults').click(function() { jQuery('#requestResults').submit(); return; });

    jQuery('<div id="wfZoomDiv"><div id="zoomLabel">100%</div><div id="zoomSlider"/></div>').appendTo(jQuery('#workflow-content'));
    jQuery('#zoomSlider').slider({
    range: "min",
        orientation: "vertical",
        min: 50,
        max: 100,
        value: 100,
        step: 5,
        slide: function( event, ui ) {
            //var zoomLevel = jQuery('#zoomSlider').slider( "value" );
            jQuery('#zoomLabel').text( ui.value + '%');
            updateZoomLevel(ui.value);
        }
    });


    // Mouseover effects for status bar (top bar) buttons
    jQuery('#wfExecutionDiv > div > .ui-button-icon').hover(
            function(){jQuery(this).css('opacity', 0.5)},
            function(){jQuery(this).css('opacity', 1)}
    );

    // Add tooltip to public/private radio
    initWorkflowPrivacyCheckbox();

}

function replaceExecutionButton(wfExecutionDiv, title, buttonId, oldButtonId, buttonClass) {

    jQuery('#' + oldButtonId).replaceWith('<div class="wfBlueButton" id=' + buttonId + '><span class="wfBtnText">' + title + '</span></div>');
    jQuery('#' + buttonId).button({
            icons : {
                primary : buttonClass + " ui-icon-wf-std-button"
                    },
            text: true
                });
    jQuery('#' + buttonId).mouseover(function() { jQuery('.' + buttonClass)
                .toggleClass(buttonClass + '-light');
        });
    jQuery('#' + buttonId).mouseout(function() { jQuery('.' + buttonClass)
                .toggleClass(buttonClass + '-light');
        });
    if (buttonId == 'cancelWorkflowButton') {
        jQuery('#' + buttonId).click(function() {
            lastCall = "cancelButtonPress";
            cancelWorkflowInEditor(currentDigraph.id);
        });
    } else if (buttonId == 'wfRunButton') {
        jQuery('#' + buttonId).off('click');
        jQuery('#' + buttonId).click(runWorkflow);
    }
}

function addExecutionButton(wfExecutionDiv, title, buttonId, buttonClass) {

    jQuery('<div class="wfBlueButton" id=' + buttonId + '><span class="wfBtnText">' + title + '</span></div>').appendTo(wfExecutionDiv);
    jQuery('#' + buttonId).button({
            icons : {
                primary : buttonClass + " ui-icon-wf-std-button"
                    },
            text: true
                });
    jQuery('#' + buttonId).mouseover(function() { jQuery('.' + buttonClass)
                .toggleClass(buttonClass + '-light');
        });
    jQuery('#' + buttonId).mouseout(function() { jQuery('.' + buttonClass)
                .toggleClass(buttonClass + '-light');
        });
}

var currentDatasetOption = null;

/** Queries the owner's projects and datasets (if they are the owner) to eventually
 * populate the "select dataset" dialog for component options of type "dataset". */
function populateDatasetDialog() {
    currentDatasetOption = jQuery(this).attr('id');
    new Ajax.Request('WorkflowEditor', {
            parameters : {
                requestingMethod: 'WorkflowEditorServlet.populateDatasetDialog',
                workflowId : workflowId
            },
            onComplete : populateDatasetDialogCompleted,
            beforeSend : wfShowStatusIndicatorModal(false, "Getting datasets..."),
            onSuccess : wfHideStatusIndicator,
            onException : function(request, exception) {
                wfHideStatusIndicator(); throw(exception);
            }
    });
}

/** Used to populate the "select dataset" dialog for component options of type "dataset". */
function populateDatasetDialogCompleted(transport) {
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
    var innerHtml = '<div class="componentOptionsHeader">Select dataset:</div>'
        + '<div class="selectDatasetDiv" id="datasetDiv_' + currentDigraph.id + '">'
        + '<table class="selectDatasetTable">'
        + '<col width="20%"><col width="78%">';

    if (currentDigraph.isView != true) {
        if (myProjectArray !== undefined && myProjectArray != null && myProjectArray.length > 0
            && myDatasetArray !== undefined && myDatasetArray != null && myDatasetArray.length > 0) {

            var selectDatasetOptions =
                generateAttachableDatasetSelect(myProjectArray, myDatasetArray);

            var isMultipleDataset = '';
            if (jQuery('#' + currentDatasetOption).hasClass('MultiDataset')) {
                isMultipleDataset = ' multiple';
            }

            var disabledStr = "";
            if (currentDigraph.isView || workflowIsRunning) { disabledStr = "disabled"; }

            innerHtml = innerHtml + '<tr class="wfFileUploadRow"><td id="dsSelected"></td><td>'
                + '<input ' + disabledStr
                + ' type="text" class="datasetFilter" maxlength="100" size="50"'
                + ' title="Filter by Dataset or Project">'
                + '</td></tr>';

            innerHtml = innerHtml
                + '<tr class="wfFileUploadRow"><td colspan="2" >'
                + '<select' + isMultipleDataset + ' size="16" ' + disabledStr + ' id="componentDatasetSelect_' + currentDigraph.id + '" class="datasetSelectBox" name="componentDatasetSelect" >'
                + selectDatasetOptions
                + '</select>'
                + '</td></tr>';
        } else {

            innerHtml = innerHtml + '<tr class="wfFileUploadRow"><td></td><td>'
                + 'You do not have <a href="help?page=requesting-access" target="_blank">view access</a> to any Projects.</td>';
        }
    }
    innerHtml = innerHtml + '</table></div>';

    if (jQuery('#componentSelectDatasetsDialog').length > 0) {
        jQuery('#componentSelectDatasetsDialog').dialog('close');
    }

    var windowWidth = jQuery(window).width();
    var maxWidth = windowWidth * 0.6;
    var windowHeight = jQuery(window).height();
    var maxHeight = windowHeight * 0.75;

    jQuery('<div />', {
         id : 'componentSelectDatasetsDialog'
    }).html(innerHtml).dialog({
         open : function() {
             jQuery('.ui-button').focus();
             jQuery('.datasetSelectBox').on('change');
             jQuery(".datasetSelectBox").filterDatasetsByText(jQuery('.datasetFilter'));
             jQuery(".datasetFilter").hint("auto-hint");
             theSelect = jQuery('#componentDatasetSelect_' + currentDigraph.id);
             jQuery(theSelect).children().each(function() {
                     jQuery(this).dblclick(function() {
                             repopulateDatasetOption();
                             jQuery('#componentSelectDatasetsDialog').dialog('close');
                         });
                 });
             datasetSelectChange();
             jQuery('#componentDatasetSelect_' + currentDigraph.id).change(datasetSelectChange);
         },
         autoOpen : true,
         autoResize : true,
         resizable : true,
         width : maxWidth,
         height : maxHeight,
         modal : true,
         title : 'Select dataset',
         buttons : {
             'OK' : function() {
                 repopulateDatasetOption();
                 jQuery(this).dialog('close');
             },
             'Cancel' : function() {
                 currentDatasetOption = null;
                 jQuery(this).dialog('close');
             }
         },
         close : function() {
             currentDatasetOption = null;
             jQuery(this).remove();
         }
     });
}

function datasetSelectChange() {
    var theSelect = jQuery('#componentDatasetSelect_' + currentDigraph.id);
    var datasetSelCount = 0;
    var datasetNames = '';
    theSelect.find('option:selected').each(function(){
        if (datasetSelCount > 0) { datasetNames += ", "; }
        datasetNames += '' + jQuery(this).text() + ' (id=' + jQuery(this).val() + ')';
        datasetSelCount += 1;
    });
    jQuery('#dsSelected').html('Dataset(s): ' + datasetNames);
}

function repopulateDatasetOption() {
    var datasetIds = "";
    var theSelect = jQuery('#componentDatasetSelect_' + currentDigraph.id);

    theSelect.find('option:selected').each(function(){
            if (datasetIds.length > 0) { datasetIds += ","; }
            datasetIds += jQuery(this).val();
        });

    if (currentDatasetOption != null) {
        jQuery('#' + currentDatasetOption).val(datasetIds);
        var optDialogContainerId = jQuery('#' + currentDatasetOption)
            .parent().parent().parent().parent().parent().parent().attr("id");
        if (!workflowIsRunning && optDialogContainerId !== undefined) {
            var splitString = optDialogContainerId.split('_');
            var componentId = splitString[1];

            // Do not auto-save or auto-validate on text-field option changes except for dirty bits
            dirtyOptionPane = true;
            isWorkflowSaved = false;
            dirtyBits[componentId] = DIRTY_OPTION;

            lastCall = "changeSelectListener";

            saveOpenOptions();
            wfEnableSaveButton();
        }
    }

    currentDatasetOption = null;
}

function updateZoomLevel(zoomLevel) {
    percentZoom = zoomLevel / 100.0;
    // Adjust editorZoomCurrent so we can get back to original scaling later (< 100%)
    var divWidth = workspaceWidth;
    var divHeight = workspaceHeight;
    var divPosition = jQuery('#process-div').position();
    // Width in pixels
    var newWidth = divWidth * (1.0 / percentZoom);
    // Height in pixels
    var newHeight = divHeight * (1.0 / percentZoom);



    jQuery('#process-div').css({
      '-webkit-transform':'scale(' + percentZoom + ')',
      '-moz-transform':'scale(' + percentZoom + ')',
      '-ms-transform':'scale(' + percentZoom + ')',
      '-o-transform':'scale(' + percentZoom + ')',
      'transform':'scale(' + percentZoom + ')',
      '-moz-transform-origin': '0 0',
      '-webkit-transform-origin': '0 0',
      '-moz-transform-origin': '0 0'
    });

    jQuery('#process-component').animate({
        transform: 'scale(' + percentZoom + ')'
    });

    jQuery('#process-div').width((81/percentZoom) + "vw");
    jQuery('#process-div').height((70/percentZoom) + "vh");

    jsPlumb.setZoom(percentZoom);


}

function startFix(event, ui) {
    ui.position.left = 0;
    ui.position.top = 0;
}

// keep track of all components' original top/left
function dragFix(event, ui) {

    var changeLeft = ui.position.left - ui.originalPosition.left; // find change in left
    var newLeft = ui.originalPosition.left + changeLeft / percentZoom; // adjust new left by our percentZoom

    var changeTop = ui.position.top - ui.originalPosition.top; // find change in top
    var newTop = ui.originalPosition.top + changeTop / percentZoom; // adjust new top by our percentZoom

    ui.position.left = newLeft;
    ui.position.top = newTop;

    if (draggedComponent && originalPos[ui.helper[0].id] !== undefined) {

        changeLeft = newLeft - originalPos[ui.helper[0].id].left;
        changeTop = newTop - originalPos[ui.helper[0].id].top;

        jQuery(selectedComponents).each(function(selInd, selVal) {
            if (ui.helper[0].id != selVal) {
                var otherCompPos = jQuery('#' + selVal).position();
                jQuery('#' + selVal).css('left', originalPos[selVal].left + changeLeft);
                jQuery('#' + selVal).css('top', originalPos[selVal].top + changeTop);
            }
        });

    }

    draggedComponent = true;

    jsPlumb.repaintEverything();
}

/* Record the position when a component is moused down to determine if it is a drag or click */
var mousePositionOnMousedownX;
var mousePositionOnMousedownY;

/* Threshold/leeway to determine a click from a drag (in pixels) */
var DRAG_THRESHOLD = 5;

/**
 * Add the handlers to a component so when you move it, it enables the save button
 */
function moveComponentFunctionality(processComponent) {
    // Record the position when the mouse was down.  This way, when the mouse is up,
    // you can determine whether it was a drag or a click
    jQuery(processComponent).mousedown(function(e) {
        mousePositionOnMousedownX = e.pageX;
        mousePositionOnMousedownY = e.pageY;
    });

    //The mouse is up now, if it was a drag, enable the save button
    jQuery(processComponent).mouseup(function(e) {
        // Allow the user to save the new position of the component
        if (mousePositionOnMousedownX != undefined && mousePositionOnMousedownY != undefined) {
            // It is considered a drag if the component moves more than the DRAG_THRESHOLD
            if (Math.abs(e.pageX - mousePositionOnMousedownX) > DRAG_THRESHOLD ||
                    Math.abs(e.pageY - mousePositionOnMousedownY) > DRAG_THRESHOLD) {
                isWorkflowSaved = false;
                lastCall = "processComponent.mouseup";
                updateComponentPositions();
                wfEnableSaveButton();
            }
        }
        mousePositionOnMousedownX = undefined;
        mousePositionOnMousedownY = undefined;
    });
}



function wfEnableSaveButton() {
    jQuery('#saveWorkflowButton').button('enable');

    if (isWorkflowExecutionPending === true) {
        jQuery('#wfRunButton .wfBtnText').html('Save<br/>& Run');
    }
    markAsUnsaved();
}

function wfDisableSaveButton() {
    jQuery('#saveWorkflowButton').button('disable');
    jQuery('#wfRunButton .wfBtnText').text('Run');

    markAsSaved();
}

function updateSharedSelection() {
    if (!workflowIsRunning) {
        if ((currentDigraph !== undefined) &&
            (jQuery('input:radio[name|="isSharedRadioButton"]:checked').val() !== undefined)) {
            if (currentDigraph.isShared != jQuery('input:radio[name|="isSharedRadioButton"]:checked')
                    .val()) {
                currentDigraph.isShared = jQuery('input:radio[name|="isSharedRadioButton"]:checked').val();
                isWorkflowSaved = false;
                wfEnableSaveButton();

            }

        }
    }
}

function preconditionNotMetError(dialogHtml) {
    wfInfoDialog('preconditionNotMetDialog', dialogHtml, 'Error');
}






/**********************************************************************************
 *  Component-specific functions (drag/drop, delete component, rename component).
 *
 **********************************************************************************/
function climbTree(treeNode, nodeName) {
    var parent = jQuery('#component-tree-div').jstree(true).get_parent(treeNode);
    // Either keep climbing or return the highest container in the tree, e.g. Import/Analysis/etc
    if (parent == "#") {
        return treeNode;
    } else {
        return climbTree(parent, treeNode);
    }
}

/**
 * Handles dragging/dropping components from the left to the right side of the workflow,
 * as well as the dragging of existing components.
 */
function handleDropEvent(event, draggable, isPaste, newId) {
    if (workflowIsRunning) {
        showCancelWarning();
    } else {
        // Get the selected draggable component.

        var draggableParent = jQuery('#' + draggable[0].id).parent();
        if (draggable[0].id === "data_import-draggable" || isPaste) {
            draggableParent = jQuery('#' + draggable[0].id);
        }



        // draggableParent.id example: Import_Tab-Delimited-draggable
        parentDiv = draggableParent.closest('.contextArea').attr('id');

        var unescapedCompName = jQuery('#' + draggable[0].id + ' .compName').text();
        var escapedCompName = jQuery('#' + draggable[0].id + ' .compName').text()
            .toLowerCase().replace(/[^a-zA-Z0-9]+/gi, "_").trim();

        // When a component is dropped onto the workspace, update the Recently Used menu for the
        // component's top-most container, e.g. Import/Analysis/etc.
        // Ensure that the dragged component is not an annotation first
        if (!isPaste && parentDiv != 'process-selector-div'
                && draggable[0].id != 'annotation-adder-draggable'
                    && draggable[0].id != 'data_import-draggable_anchor') {
            jQuery('.jstree-anchor').filter(function() {

                var filterCompName = jQuery('#' + this.id + ' .compName').text();
                var filterEscCompName = jQuery('#' + this.id + ' .compName').text().trim().toLowerCase().replace(/[^a-zA-Z0-9]+/gi, "_");
                var recentPattern = new RegExp("recent_.*","gi"); // recent created from traverse don't have "recent_" in id

                if (filterEscCompName  == escapedCompName && this.id.match(recentPattern) === null) {
                    // j1_1 etc jQuery(this).parent().attr('id');
                    var ancestorId = climbTree(this, escapedCompName);
                    if (ancestorId.id === undefined) {

                        var foundRecentlyUsedFolder = false;
                        jQuery('#' + ancestorId + ' .jstree-anchor').filter(function(){
                            var isRecentlyUsed = jQuery('#' + this.id).text().trim().toLowerCase();
                            if (isRecentlyUsed == 'recently used') {
                                foundRecentlyUsedFolder = true;
                            }
                        });

                        if (!foundRecentlyUsedFolder) {
                            var position = 'first';
                            var parent = jQuery('#component-tree-div').jstree(true).get_node(ancestorId);
                            var newNode = {
                                    li_attr : { "class": "jstree-node no_dragging jstree-open" },
                                    text : "Recently Used"
                                    //icon : iconImage
                            };

                            jQuery('#component-tree-div').jstree("create_node", parent, newNode, position, false, false);
                        }

                        jQuery('#' + ancestorId + ' .jstree-anchor').filter(function(){
                            var isRecentlyUsed = jQuery('#' + this.id).text().trim().toLowerCase();
                            if (isRecentlyUsed == 'recently used') {
                                foundRecentlyUsedFolder = true;

                                var position = 'first';
                                var parent = jQuery('#component-tree-div').jstree(true)
                                    .get_node(jQuery(this).parent().attr('id'));

                                var compType = jQuery('#' + draggable[0].id).parent().attr('name');
                                //var parent = jQuery(this).parent().attr('id');
                                var compNameSpan = jQuery('<span class="compName">' + escapedCompName + '</span>');

                                var newNode = {
                                        li_attr : { "class": "noSelect ui-draggable draggableComponent",
                                            "id" : "recent_" + compType + "_" + escapedCompName + "-draggable",
                                            "name" : compType },
                                        text : unescapedCompName + '<span class="compName">' + unescapedCompName + '</span>'
                                            + '<span style="display: none" id="recent_' + compType + "_" + escapedCompName
                                                + '_tooltip_content" class="wfComponentTooltip">'
                                                + componentInfoDivObject[escapedCompName] + '</span>',
                                        icon : componentIconImage
                                };

                                var nodeAlreadyExists = jQuery('#component-tree-div').jstree(true)
                                    .get_node('#' + "recent_" + compType + "_" + escapedCompName
                                        + "-draggable");

                                if (nodeAlreadyExists !== false) {

                                    var oldNode = jQuery('#component-tree-div').jstree(true)
                                        .get_node('#' + "recent_" + compType + "_" + escapedCompName + "-draggable");
                                    jQuery('#component-tree-div').jstree("delete_node", oldNode);
                                }
                                jQuery('#component-tree-div').jstree("create_node", parent, newNode, position, false, false);

                                if (jQuery("#component-lhs-search-input").val().trim() != ""
                                    && jQuery("#component-lhs-search-input").val().trim() != "Filter by name or description") {
                                    // If the user is actively filtering components, do not put anything in recently used
                                    jQuery('#component-lhs-search-input').keyup();
                                }

                                if (parent.children.length > 5) {
                                    //
                                    var child = jQuery('#component-tree-div').jstree(true)
                                        .get_node(parent.children[5]);
                                    if (child !== undefined && child != null) {
                                        jQuery('#component-tree-div').jstree("delete_node", child);
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }

        initializeTooltips('.wfComponentTooltip');

        // The component div.
        var processComponent = null;

        var top = null;
        var left = null;

        if (!isPaste && parentDiv == 'process-div' && !draggable[0].id.includes('annotation-adder-draggable')) {

            // The component has already been dropped onto the workspace.
            processComponent = draggableParent;

            left = parseInt(draggableParent.css('left'));
            processComponent.css('left', left);
            top = parseInt(draggableParent.css('top'));
            processComponent.css('top', top);
        } else if (!isPaste && parentDiv == 'process-selector-div' && draggable[0].id.includes('annotation-adder-draggable')) {
            placeNewAnnotation(event, draggable);
            return; //placeNewAnnotation will handle saving
        } else if (isPaste || parentDiv == 'process-selector-div') {

            // Height of datashop header
            var headerHeight = 0;
            if (jQuery('body > #ls-header').length > 0) {
                headerHeight = jQuery('body > #ls-header').outerHeight();
            }

            // Height of help button <tr>
            var helpRowHeight = 0; // Ignored for now in LS

            var draggableZoomFactor = (1.16 / (percentZoom + 0.16));

            var dragDropOffsetY = jQuery('#wfStatusBar').height()
                + headerHeight + helpRowHeight;

            // Handle x offsets for drop
            var componentCenterX = componentWidth * 0.5 * draggableZoomFactor;
            var startOfComponentX = jQuery('#process-selector-div').width();
            var dragDropOffsetX = componentCenterX;

            // The component is being dropped onto the workspace for the first time.

            // div name is the highest-level component type, e.g. Import, Visualization, etc.
            var divName = draggableParent.attr('name');
            // Get the human-readable title
            var tempCopyOfDraggable = jQuery(draggable[0]).clone(false,false);
            tempCopyOfDraggable.find('span').remove();
            // Get the title from the left-hand component menu
            var componentTitle =  tempCopyOfDraggable.text();
            if (isPaste) {
                // Get the title from the existing component instance
                componentTitle = jQuery('#' + draggable[0].id + ' .componentTitle').text();

                var importPattern = "Import\(.*\)";
                var regex = new RegExp(importPattern,"gi");
                // In case it's an Import type
                if (componentTitle.match(regex) !== null) {
                    componentTitle = "Import " + jQuery('#' + draggable[0].id + ' .componentTitle .importTypeTitle span').text();
                }
            }

            processComponent = jQuery('<div id="' + draggable[0].id + '" name="' + divName + '" class="process-component ui-draggable ui-draggable-handle jsplumb-endpoint-anchor jsplumb-connected">'
                    + '<div id="title_' + draggable[0].id + '" title="' + componentTitle
                    + '" class="componentTitle">' + componentTitle + '</div>'
                    + '<span class="compName">' + unescapedCompName + '</span>'
                    + '</div>');


            // Setup the new component id (component type + lowestAvailableNumber, e.g. import2)
            var componentTypeId = processComponent.attr("name").toLowerCase();

            // Setup a human readable name

            // Is number 1 available?
            componentCounter[componentTypeId] = 1;
            // Unique component id

            // Generate a 6-character (zero padded) random number
            var numRand = ("00000" + Math.floor(Math.random() * 999999)).slice (-6);

            // Set new component Id
            var componentId = formatHeading(componentTypeId)
                + "-" + componentCounter[componentTypeId] + "-x" + numRand ;
            if (isPaste) {
                componentId = newId;
            }

            // It will probably be unique, but let's make sure.
            while (jQuery('#' + componentId).length > 0) {
                numRand = ("00000" + Math.floor(Math.random() * 999999)).slice (-6);
                componentId = formatHeading(componentTypeId)
                + "-" + componentCounter[componentTypeId] + "-x" + numRand ;
            }

            // Set the new human readable id
            var componentReadableId = formatHeading(componentTypeId)
                + " #" + componentCounter[componentTypeId] ;

            // Increment number until we find next available integer for human readable id, e.g. Import #3
            var readableIdExists = true;
            while (readableIdExists) {
                readableIdExists = false;

                jQuery('.componentSubtitle').each(function() {
                    //alert(jQuery(this).text() + "==" + componentReadableId);
                    if (jQuery(this).text() == componentReadableId) {
                        readableIdExists = true;
                    }
                });

                if (readableIdExists) {
                    componentCounter[componentTypeId] = componentCounter[componentTypeId] + 1;
                    componentReadableId = formatHeading(componentTypeId) + " #" + componentCounter[componentTypeId] ;
                }

            }


            // Add to the diagram and add the simple (immediately displayed) options to the component.
            processComponent.appendTo('#process-div');

            // Add the new subtitle to the component.
            jQuery('<div id="componentSubtitleEdit_' + componentId
                    + '" class="componentSubtitle"><div id="humanReadableId_' + componentId + '" class="componentSubtitle" >'
                        + componentReadableId + '<div class="renameWorkflowIcon" id="renameIcon_' + componentId + '"><img src="images/pencil.png" />'
                        + '</div></div></div>')
                    .appendTo(processComponent);
            initRenameComponentToolTip(componentId);

            componentIdTypeMap[componentId] = componentTypeId;

            // Set the new componentId.
            processComponent.attr('id', componentId);

            processComponent.attr('class', 'process-component');

            processComponent.css('position', 'absolute');
            processComponent.css('width', componentWidth + 'px');
            processComponent.css('height', componentHeight + 'px');

            if (isPaste) { // event is null in this case
                var maxTop = jQuery('#process-div').height() - jQuery('#wfMessageBar').height()
                    - jQuery('#wfStatusBar').height() - jQuery('#ls-header').height()
                    - componentHeight;
                // This is a Pasted component
                cOffsetLeft = parseInt(draggableParent.css('left'));

                processComponent.css('left', cOffsetLeft);
                // Paste the new components just below the bottom-most components,
                // while maintaining their relative positioning
                overallOffset = highestY - (selectedLowestY - lowestY);
                cOffsetTop = parseInt(draggableParent.css('top')) + overallOffset + componentHeight;
                if (cOffsetTop > maxTop) {
                    //jQuery('#process-div').css('height', cOffsetTop + componentHeight);
                }
                processComponent.css('top', cOffsetTop);
            } else { // event is not null in this case
                // This was Dragged from the component menu
                if(event.pageX > jQuery('#process-selector-div').width()) {
                    left = event.pageX * draggableZoomFactor
                        - dragDropOffsetX
                        - jQuery(document).scrollLeft()
                        - startOfComponentX;
                    //left = parseInt(draggable.css('left'), 10);
                    processComponent.css('left', left);
                }

                top = event.pageY * draggableZoomFactor
                    - dragDropOffsetY
                    - jQuery(document).scrollTop()
                    + jQuery('#process-div').scrollTop();
                //top = parseInt(draggable.css('top'), 10);
                processComponent.css('top', top);

            }

            // Make dropped/dragged component draggable, but keep it contained to the workspace

            // These are components that were dropped onto the workspace.
            jQuery(processComponent).draggable({
                scroll: true,
                containment : '#process-div',
                cursor : 'move',
                snap : '#process-div',
                start: startFix,
                drag: dragFix
            });

            // Allow the user to save the new position of the component
            moveComponentFunctionality(processComponent);

        } // end of brand new component conditional


        // Create or update the info in the global variable, currentDigraph.
        var divPosition = jQuery(processComponent).position();
        var id = jQuery(processComponent).attr('id');
        var componentType = jQuery(processComponent).attr('name').toLowerCase();

        if (top == null && left == null) {
            top = parseFloat(divPosition.top);
            left = parseFloat(divPosition.left);
        }



        // Since this is a new component, we must set it up.
        isWorkflowExecutionPending = true;
        // Add the gear icon.
        var openOptionsDiv = jQuery('<span class="openComponentOptionsGear" id="componentOptions_'
                + id + '" ></span>');
        openOptionsDiv.appendTo(processComponent);
        // Add the trashcan (delete) icon.
        var deleteComponentDiv = jQuery('<span class="deleteComponentIcon"></span>');
        deleteComponentDiv.appendTo('#' + id);
        deleteComponentDiv.click(deleteComponentConfirm);

        // Add the advanced options gear icon to the component
        initComponentOptionsDiv(processComponent);

        // Add the Status icon
        var statusString = 'Status: Ready';
        var statusClass = 'componentStatusIcon_ready wfStatusIcon';
        var componentStatusDiv = jQuery('<div class="componentStatusDiv" id="componentStatusDiv_' + id + '">'
            + '<span class="' + statusClass + '" id="componentStatus_'+ id
                + '" >' + statusString + '</span>'
                + '</div>');
        componentStatusDiv.appendTo('#' + id);

        // Add the preview icon
        var dsId = null;
        var resultsForm = jQuery('<form id="requestResults_' + id + '" method="post" action="WorkflowResults" target="_blank">'
            + '<input type="hidden" name="requestingMethod" value="WorkflowResultsServlet.displayPreviousResults" />'
            + '<input type="hidden" name="workflowId" value="' + currentDigraph.id + '" />'
            + '<input type="hidden" name="datasetId" value="' + dsId + '" />'
            + '<input type="hidden" name="componentId" value="' + id + '" />'
            + '<input type="hidden" name="componentName" value="' + unescapedCompName + '" />'
            + '<input type="hidden" name="componentType" value="' + componentType + '" />'
            + '</form>');
        var componentPreviewDiv = jQuery('<span class="openComponentPreviewIcon" id="componentPreview_'
            + id + '" ></span>');

        componentPreviewDiv.appendTo(processComponent);

        var componentDebugInfoDiv = jQuery(
            '<span class="openComponentDebugInfoIcon" id="componentDebug_'
                + id + '" ></span>');
        componentDebugInfoDiv.appendTo(processComponent);
        jQuery('#' + id + ' .openComponentDebugInfoIcon').hide();

        jQuery('#' + id + ' .openComponentPreviewIcon').css('opacity', '0.4');
        jQuery('#' + id + ' .openComponentDebugInfoIcon').css('opacity', '0.4');

        jQuery(resultsForm).appendTo('#process-div');

        jQuery('#componentPreview_' + id).click(requestResultsInNewWindow);

        jQuery('#componentDebug_' + id).click(requestDebugInfoFromButton);

        jQuery('#componentOptions_' + id).click(openAdvancedOptionsDialog);


        if (!jQuery.isArray(currentDigraph.components)) {
            currentDigraph.components = [ currentDigraph.components ];
        }

        // Search through all components in the global variable to find the one with this id.
        var foundComponent = null;

        if (currentDigraph.components.length > 0) {
            jQuery.each(currentDigraph.components, function() {
                var thisComponent = this;
                if (this.component_id == id) {
                    foundComponent = this;

                }
            });
        }

        // No component was found in the global variable, currentDigraph.
        if (foundComponent == null) {
            // Renaming function for the human-readable id
            jQuery('#renameIcon_' + componentId).on('click', renameHumanReadableId);

            //alert("newcomponent: " + id);

            var humanReadableId = jQuery('#humanReadableId_' + id).text();
            // Push the newly added component to the global variable, currentDigraph,
            // and setup the endpoint (connector) overlays.
            var componentObject = new Object();
            componentObject.component_id = id;
            componentObject.component_id_human = humanReadableId;
            componentObject.workflow_id = currentDigraph.id;
            componentObject.component_type = componentType;
            componentObject.component_name = unescapedCompName;
            componentObject.top = top;
            componentObject.left = left;
            componentObject.connections = [];
            componentObject.options = {};
            componentObject.inputs = [];
            componentObject.outputs = [];
            componentObject.errors = [];

            logWorkflowComponentAction(currentDigraph.id, null,
                id, escapedCompName, componentType, humanReadableId,
                    null, null, null, LOG_ADD_COMPONENT, "");

            currentDigraph.components.push(componentObject);
            var minComponentHeight = componentHeight;

                var endpointsKey = componentObject.component_type.toLowerCase()
                + "-" + componentObject.component_name.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase();

                // Handle output endpoints
                //alert(JSON.stringify(componentOutputEndpoints) + '  ::  ' + endpointsKey);


                if (componentOutputEndpoints[endpointsKey] !== undefined && componentOutputEndpoints[endpointsKey] > 0) {
                    var fillStyle = connectionColor;
                    var newComponentHeight = componentHeight;
                    var newComponentHeightCoeff = componentHeightCoeff;
                    var heightFromOutputs = componentOutputEndpoints[endpointsKey] * componentHeightQuartered + componentHeightHalved;
                    if (heightFromOutputs > newComponentHeight) {
                        newComponentHeight = heightFromOutputs;
                        newComponentHeightCoeff = jsPlumbInitialNodeOffset + componentHeightCoeff / componentOutputEndpoints[endpointsKey];
                        if (newComponentHeight > minComponentHeight) {
                            minComponentHeight = newComponentHeight;
                        }
                    }
                    var newNodeGapCoeff = nodeGapCoeff;
                    // Decrease gap between nodes if there are more than 3 outputs.
                    if (componentOutputEndpoints[endpointsKey] > 3) { newNodeGapCoeff = 0.23; }

                    var minComponentHeight = newComponentHeight;

                        var offsetMultiplier = 0;
                        for (var outIndex = 0 ; outIndex < componentOutputEndpoints[endpointsKey]; outIndex++) {

                                var outputLabel = componentOutputEndpoints[endpointsKey + '_list'][outIndex];
                                var newAnchor = [ 0.997, newComponentHeightCoeff + (offsetMultiplier * newNodeGapCoeff), 1, 0 ];

                                var sourceEndpoint = {
                                    endpoint : 'Dot',
                                    uniqueEndpoint : true,
                                    paintStyle : {
                                        strokeStyle : fillStyle,
                                        fillStyle : 'transparent',
                                        radius : 12,
                                        lineWidth : 4
                                    },
                                    deleteEndpointsOnDetach: false,
                                    isSource : true,
                                    maxConnections : -1,
                                    connector : [ 'Flowchart', {
                                        stub : [ 10, 10 ],
                                        gap : 14,
                                        cornerRadius : 15,
                                        alwaysRespectStubs : true
                                    } ],
                                    connectorStyle : connectorPaintStyle,
                                    hoverPaintStyle : endpointHoverStyle,
                                    connectorHoverStyle : connectorHoverStyle,
                                    dragOptions : {},
                                    dropOptions : {},
                                    overlays : [ [ 'Label', {
                                        location : [ 1.25 + 0.07 * outputLabel.length, 0 ],
                                        label : outputLabel,
                                        cssClass : 'endpointSourceLabel'
                                    } ] ]
                                };

                                // The source endpoint (Out connector).
                                var mySource = jsPlumb.addEndpoint(id, {
                                    uuid : id + "_" + outIndex + '_source',
                                    anchor : newAnchor,
                                    connectorOverlays : [ [ 'Arrow', {
                                        width : 15,
                                        length : 30,
                                        location : 1,
                                        id : 'arrow'
                                    } ], [ 'Label', {
                                        label : '',
                                        id : 'label'
                                    } ] ]
                                }, sourceEndpoint);
                                setupSourceEndpoint(mySource, id, outIndex);
                                offsetMultiplier += 1;

                        }
                }

                if (componentInputEndpoints[endpointsKey] !== undefined && componentInputEndpoints[endpointsKey] > 0) {
                    var fillStyle = connectionColor;
                    var newComponentHeight = componentHeight;
                    var newComponentHeightCoeff = componentHeightCoeff;
                    var heightFromOutputs = componentInputEndpoints[endpointsKey] * componentHeightQuartered + componentHeightHalved;
                    if (heightFromOutputs > newComponentHeight) {
                        newComponentHeight = heightFromOutputs;
                        newComponentHeightCoeff = jsPlumbInitialNodeOffset + componentHeightCoeff / componentInputEndpoints[endpointsKey];
                        if (newComponentHeight > minComponentHeight) {
                            minComponentHeight = newComponentHeight;
                        }
                    }

                    var newNodeGapCoeff = nodeGapCoeff;
                    // Decrease gap between nodes if there are more than 3 outputs.
                    if (componentInputEndpoints[endpointsKey] > 3) { newNodeGapCoeff = 0.23; }

                        for (var inIndex = 0 ; inIndex < componentInputEndpoints[endpointsKey]; inIndex++) {

                            var inputLabel = componentInputEndpoints[endpointsKey + '_list'][inIndex];
                            var newAnchor = [ 0.03, newComponentHeightCoeff + (inIndex * newNodeGapCoeff), -1, 0 ];

                            var specifiedTargetEndpoint = {
                                endpoint : 'Dot',
                                uniqueEndpoint : true,
                                paintStyle : {
                                    fillStyle : fillStyle,
                                    radius : 15
                                },
                                deleteEndpointsOnDetach: false,
                                hoverPaintStyle : endpointHoverStyle,
                                dropOptions : connectionDropOptions,
                                dragOptions : {},
                                isTarget : true,
                                maxConnections : -1,
                                overlays : [ [ 'Label', {
                                    location : [ -0.1 * inputLabel.length, 0 ],
                                    label : inputLabel,
                                    cssClass : 'endpointTargetLabel'
                                } ] ]
                            };

                            var myTarget = jsPlumb.addEndpoint(id, {
                                uuid : id + "_" + inIndex + '_target',
                                anchor : newAnchor,
                            }, specifiedTargetEndpoint);

                        }
                }

               // checkComponentOptions(componentObject.component_id,
               //         componentObject.component_type, componentObject.component_name);

                // Important line for Data/Import
                dirtyBits[id] = DIRTY_OPTION;

                isWorkflowSaved = false;
                lastCall = "handleDropEvent";

                if (!workflowIsRunning && !isPaste) {
                    saveTemporaryWorkflowTransient(currentDigraph.id, null, null, null, null);
                }

                if (isPaste) {
                    var regex1 = new RegExp("Data-.*","gi");
                    var regex2 = new RegExp("Import-.*","gi");
                    var isData = false;
                    if (id.match(regex1) !== null || id.match(regex2) !== null) {
                        isData = true;
                    }
                    if (!isData) {
                        dirtyBits[id] = DIRTY_SELECTION;
                    }
                }

                jQuery('#' + id).css('height', minComponentHeight + 'px');
                specialComponentHeight[id] = minComponentHeight;
                wfEnableSaveButton();

               // buildConnections(currentDigraph.components);
        } // end of if (foundComponent == null)

        if (foundComponent != null && parentDiv == 'process-div') {
            foundComponent.top = top + jQuery('#process-div').scrollTop();
            foundComponent.left = left + jQuery('#process-div').scrollLeft();
            isWorkflowSaved = false;
            wfEnableSaveButton();
        }

        // Now that a component exists, hide the drag/drop hint.
        jQuery('#workspaceHint').hide();
        // Show node labels if the node has no connections.  Otherwise they only show on hover
        handleDisplayInputOutputTags();
        // Repaint the diagram.
        jsPlumb.repaintEverything();
    }
} // end of handleDropEvent

function buildConnections(component) {

        var minComponentHeight = componentHeight;
        var thisComponent = component;

        var endpointsKey = thisComponent.component_type.toLowerCase()
        + "-" + thisComponent.component_name.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase();
        var id = thisComponent.component_id;


        // Handle output endpoints


        if (componentOutputEndpoints[endpointsKey] !== undefined && componentOutputEndpoints[endpointsKey] > 0) {
            var newComponentHeight = componentHeight;
            var newComponentHeightCoeff = componentHeightCoeff;
            var heightFromOutputs = componentOutputEndpoints[endpointsKey] * componentHeightQuartered + componentHeightHalved;
            if (heightFromOutputs > newComponentHeight) {
                newComponentHeight = heightFromOutputs;
                newComponentHeightCoeff = jsPlumbInitialNodeOffset + componentHeightCoeff / componentOutputEndpoints[endpointsKey];
                if (newComponentHeight > minComponentHeight) {
                    minComponentHeight = newComponentHeight;
                }
            }
            var newNodeGapCoeff = nodeGapCoeff;
            // Decrease gap between nodes if there are more than 3 outputs.
            if (componentOutputEndpoints[endpointsKey] > 3) { newNodeGapCoeff = 0.23; }

            var fillStyle = connectionColor;
                var offsetMultiplier = 0;
                for (var outIndex = 0 ; outIndex < componentOutputEndpoints[endpointsKey]; outIndex++) {



                        var outputLabel = componentOutputEndpoints[endpointsKey + '_list'][outIndex];
                        var newAnchor = [ 0.997, newComponentHeightCoeff + (offsetMultiplier * newNodeGapCoeff), 1, 0 ];

                        var sourceEndpoint = {
                                endpoint : 'Dot',
                                uniqueEndpoint : true,
                                paintStyle : {
                                    strokeStyle : fillStyle,
                                    fillStyle : 'transparent',
                                    radius : 12,
                                    lineWidth : 4
                                },
                                deleteEndpointsOnDetach: false,
                                isSource : true,
                                maxConnections : -1,
                                connector : [ 'Flowchart', {
                                    stub : [ 10, 10 ],
                                    gap : 14,
                                    cornerRadius : 15,
                                    alwaysRespectStubs : true
                                } ],
                                connectorStyle : connectorPaintStyle,
                                hoverPaintStyle : endpointHoverStyle,
                                connectorHoverStyle : connectorHoverStyle,
                                dragOptions : {},
                                dropOptions : {},
                                overlays : [ [ 'Label', {
                                    location : [ 1.25 + 0.07 * outputLabel.length, 0 ],
                                    label : outputLabel,
                                    cssClass : 'endpointSourceLabel'
                                } ] ]
                        };

                        // The source endpoint (Out connector).
                        var mySource = jsPlumb.addEndpoint(id, {
                            uuid : id + "_" + outIndex + '_source',
                            anchor : newAnchor,
                            connectorOverlays : [ [ 'Arrow', {
                                width : 15,
                                length : 30,
                                location : 1,
                                id : 'arrow'
                            } ], [ 'Label', {
                                label : '',
                                id : 'label'
                            } ] ],
                            events:{
                                // Display all node labels when hovering over a node
                                mouseout:function(labelOverlay, originalEvent) {
                                    jQuery('.endpointSourceLabel, .endpointTargetLabel').removeClass('endpointHover');
                                },
                                mouseover:function(labelOverlay, originalEvent) {
                                    jQuery('.endpointSourceLabel, .endpointTargetLabel').addClass('endpointHover');
                                }
                            }
                        }, sourceEndpoint);
                        setupSourceEndpoint(mySource, id, outIndex);
                        offsetMultiplier += 1;

                }
        }


        if (componentInputEndpoints[endpointsKey] !== undefined && componentInputEndpoints[endpointsKey] > 0) {
            var newComponentHeight = componentHeight;
            var newComponentHeightCoeff = componentHeightCoeff;
            var fillStyle = connectionColor;
            var heightFromOutputs = componentInputEndpoints[endpointsKey] * componentHeightQuartered + componentHeightHalved;
            if (heightFromOutputs > newComponentHeight) {
                newComponentHeight = heightFromOutputs;
                newComponentHeightCoeff = jsPlumbInitialNodeOffset + componentHeightCoeff / componentInputEndpoints[endpointsKey];
                if (newComponentHeight > minComponentHeight) {
                    minComponentHeight = newComponentHeight;
                }
            }

            var newNodeGapCoeff = nodeGapCoeff;
            // Decrease gap between nodes if there are more than 3 inputs.
            if (componentInputEndpoints[endpointsKey] > 3) { newNodeGapCoeff = 0.23; }

                for (var inIndex = 0 ; inIndex < componentInputEndpoints[endpointsKey]; inIndex++) {

                    var inputLabel = componentInputEndpoints[endpointsKey + '_list'][inIndex];
                    var newAnchor = [ 0.03, newComponentHeightCoeff + (inIndex * newNodeGapCoeff), -1, 0 ];

                    var specifiedTargetEndpoint = {
                        endpoint : 'Dot',
                        uniqueEndpoint : true,
                        paintStyle : {
                            fillStyle : fillStyle,
                            radius : 15
                        },
                        deleteEndpointsOnDetach: false,
                        hoverPaintStyle : endpointHoverStyle,
                        dropOptions : connectionDropOptions,
                        dragOptions : {},
                        isTarget : true,
                        maxConnections : -1,
                        overlays : [ [ 'Label', {
                            location : [ -0.1 * inputLabel.length, 0 ],
                            label : inputLabel,
                            cssClass : 'endpointTargetLabel'
                        } ] ]
                    };

                    var myTarget = jsPlumb.addEndpoint(id, {
                        uuid : id + "_" + inIndex  + '_target',
                        anchor : newAnchor,
                        events:{
                            // Display all node labels when hovering over a node
                            mouseout:function(labelOverlay, originalEvent) {
                                jQuery('.endpointSourceLabel, .endpointTargetLabel').removeClass('endpointHover');
                            },
                            mouseover:function(labelOverlay, originalEvent) {
                                jQuery('.endpointSourceLabel, .endpointTargetLabel').addClass('endpointHover');
                            }
                        }
                    }, specifiedTargetEndpoint);

                }
        }
        specialComponentHeight[id] = minComponentHeight;
        jQuery('#' + id).css('height', minComponentHeight + 'px');

}


/** Delete component confirmation dialog. */
function deleteComponentConfirm() {
    if (workflowIsRunning) {
        showCancelWarning();
    } else {
        var component = this;
        if (!currentDigraph.isView) {
            jQuery('<div />', {
                id : 'wfDeleteComponentConfirmationDialog'
            }).html(
                'Are you sure you want to delete the component?')
            .dialog({
                open : function() {
                    jQuery('.ui-button').focus();
                },
                autoOpen : true,
                autoResize : true,
                resizable : false,
                width : 400,
                height : "auto",
                title : 'Delete Component Confirmation',
                buttons : {
                    'Yes' : function() {
                        deleteComponent(component);
                        jQuery(this).dialog('close');
                    },
                    'No' : function() {
                        jQuery(this).dialog('close');
                    }
                },
                close : function() {
                    jQuery(this).remove();
                }
            });

        }
    }
}

// Delete the component.
function deleteComponent(component) {
    if (!workflowIsRunning) {
        var parentDiv = jQuery(component).parent();
        var componentId = parentDiv.attr('id');
        var componentType = parentDiv.attr('name').toLowerCase();

        instance.removeAllEndpoints(parentDiv.attr('id'));

        // Delete component
        jsPlumb.remove(parentDiv);

        var components = currentDigraph.components;
        if (components !== undefined && components != null) {
            if (!jQuery.isArray(components)) {
                components = [ components ];
            }

            if (components.length > 0) {
                jQuery.each(components, function(index) {

                    if (components[index] !== undefined && components[index].component_id == componentId) {

                        // close options dialog or preview dialog for components, if open
                        jQuery('.advOptDialogClass').dialog('close');
                        jQuery('#componentOutputDataPreview').dialog('close');

                        logWorkflowComponentAction(currentDigraph.id, null,
                            components[index].component_id, components[index].component_name,
                                components[index].component_type, components[index].component_id_human,
                                    null, null, null, LOG_DELETE_COMPONENT, "");

                        delete componentOptions[componentId];
                        delete dirtyBits[componentId];

                        components.remove(index);


                        // break the each loop if we find the object to remove
                        return false;
                    }
                });
            }
            isWorkflowSaved = false;

            lastCall = "deleteComponent";
            saveTemporaryWorkflowTransient(currentDigraph.id, null, null, null, null);

        }

        if (currentDigraph.hasOwnProperty('components') && currentDigraph.components !== undefined
                && currentDigraph.components.length == 0) {
            jQuery('#workspaceHint').show();
        }
    }
}


function renameHumanReadableId() {
    if (workflowIsRunning) {
        showCancelWarning();
    } else {
        var parentDiv = jQuery(this).parent();
        var splitString = this.id.split('_');

        if (splitString.length == 2) {
            var componentId = splitString[1];
            var containerDiv = jQuery('#' + componentId);

            var newDiv = '<div id="componentSubtitleEdit_' + componentId + '" class="componentSubtitle"'
                + '"><input type="text" title="New Name" id="editHumanReadableId_' + componentId + '"'
                + ' class="humanReadableTextField" value="' + jQuery('#humanReadableId_' + componentId).text() + '" /><br/>'
                + '<input type="button" id="cancelNameButton_' + componentId + '" class="editHumanReadableCancelButton"' + ' value="Cancel" />'
                + '<input type="button" id="saveNameButton_' + componentId + '" class="editHumanReadableSaveButton"' + ' value="Rename" />'
                + '</div>';

            jQuery(parentDiv).parent().remove();
            jQuery(newDiv).appendTo(containerDiv);

            jQuery('#saveNameButton_' + componentId).click(saveHumanReadbleId);
            jQuery('#cancelNameButton_' + componentId).click(cancelHumanReadbleId);
            jQuery('#renameIcon_' + componentId).off('click');
            jQuery('.humanReadableTextField').hint('auto-hint');

            // Close tool tip
            jQuery('.toolTip').remove();

        }
    }
}

function saveHumanReadbleId() {

    var parentDiv = jQuery(this).parent();
    var splitString = this.id.split('_');

    if (splitString.length == 2) {
        var componentId = splitString[1];
        var containerDiv = jQuery('#' + componentId);

        if (currentDigraph.components != null && currentDigraph.components.length > 0) {
            jQuery.each(currentDigraph.components, function() {
                if (this.component_id == componentId) {
                    var humanReadableId = jQuery('#editHumanReadableId_' + componentId).val();
                    if (humanReadableId === undefined || humanReadableId == null) {
                        return;
                    }
                    var componentTypeId = this.component_type.toLowerCase();
                    this.component_id_human = humanReadableId;

                    logWorkflowComponentAction(currentDigraph.id, null,
                        this.component_id, this.component_name,
                            this.component_type, humanReadableId,
                                    null, null, null, LOG_RENAME_COMPONENT, "");


                    var newName = '<div id="componentSubtitleEdit_' + this.component_id
                            + '" class="componentSubtitle"><div id="humanReadableId_' + this.component_id + '" class="componentSubtitle" >'
                                + humanReadableId + '<div class="renameWorkflowIcon" id="renameIcon_' + this.component_id + '"><img src="images/pencil.png" />'
                                + '</div></div></div>';

                    parentDiv.remove();

                    jQuery(newName).appendTo('#' + this.component_id);

                    jQuery('#renameIcon_' + this.component_id).on('click', renameHumanReadableId);

                    initRenameComponentToolTip(this.component_id);

                    isWorkflowSaved = false;
                    wfEnableSaveButton();


                    //lastCall = "handleDropEvent"
                    //saveCurrentWorkflow(currentDigraph.id);
                }
            });
        }
    }
}


function cancelHumanReadbleId() {

    var parentDiv = jQuery(this).parent();
    var splitString = this.id.split('_');

    if (splitString.length == 2) {
        var componentId = splitString[1];

        if (currentDigraph.components != null && currentDigraph.components.length > 0) {
            jQuery.each(currentDigraph.components, function() {
                if (this.component_id == componentId) {
                    var humanReadableId = this.component_id_human;
                    var componentTypeId = this.component_type.toLowerCase();

                    var newName = '<div id="componentSubtitleEdit_' + this.component_id
                            + '" class="componentSubtitle"><div id="humanReadableId_' + this.component_id + '" class="componentSubtitle" >'
                                + humanReadableId + '<div class="renameWorkflowIcon" id="renameIcon_' + this.component_id + '"><img src="images/pencil.png" />'
                                + '</div></div></div>';

                    parentDiv.remove();

                    jQuery(newName).appendTo('#' + this.component_id);
                    jQuery('#renameIcon_' + this.component_id).on('click', renameHumanReadableId);
                    initRenameComponentToolTip(this.component_id);
                }
            });
        }
    }
}

/**
 * Nodes that do not have connections should always have a visible label.  Nodes with connections
 * should only display their label when hovering over a node.  This makes the workflow Interface
 * less busy.
 */
function handleDisplayInputOutputTags() {
    var labelsWithConnections = [];

    // Get a list of the overlays/labels that describe nodes that have connections
    var anEndpoint = jQuery('.jsplumb-endpoint')[0];
    var overlays = jsPlumb.select(jQuery(anEndpoint).attr('id')).getOverlay();
    if (overlays != undefined && overlays != null) {
        for (var i = 0; i < overlays.length; i++) {
            var overlay = overlays[i];

            if (overlay == null || overlay == undefined) {
                continue;
            }

            var connection = overlay[1];
            if (connection == null || connection == undefined) {
                continue;
            }

            var endpoints = connection.endpoints;
            if (endpoints != undefined && endpoints != null) {
                for (var j = 0; j < endpoints.length; j++) {
                    var endpoint = endpoints[j];
                    if (endpoint == null || endpoint == undefined) {
                        continue;
                    }

                    var canvas = endpoint.canvas;

                    // If the node is connected, add it to a list and ensure it is not visible without
                    // hovering
                    if (jQuery(canvas).hasClass('jsplumb-endpoint-connected')) {
                        var label = canvas.nextElementSibling;
                        labelsWithConnections.push(label);
                        if (jQuery(label).hasClass('endpointNotConnected')) {
                            jQuery(label).removeClass('endpointNotConnected');
                        }
                    }
                }
            }
        }
    }

    // Make the labels of nodes that aren't connected be opaque
    jQuery('.jsplumb-overlay').each(function() {
        if (!labelsWithConnections.includes(this)) {
            jQuery(this).addClass('endpointNotConnected');
        }
    });
}

/*******************************************************************************
 * Workflow component options functions.
 *
 ******************************************************************************/

/** Add options to the component. This is called every time a new component
 * is dragged into the workspace or everytime a component is added
 * to the workspace when populating the workflow editor upon opening.
 * @param parentDiv the component div
 */
function initComponentOptionsDiv(parentDiv) {

    //alert(jQuery(parentDiv).attr("name") + ", " + jQuery(parentDiv).attr("id"));
    // Called when component is first brought onto the page.
    var componentType = parentDiv.attr("name");
    var componentId = parentDiv.attr("id");
    if (componentType !== undefined && componentId !== undefined) {

        if (componentOptions[componentId] === undefined) {
            componentOptions[componentId] = new Object();
        }

        // Setup the simple options to update when changed.
        var thisDiv = createAdvancedOptionsDiv(componentId);
        thisDiv.appendTo('#' + componentId);

    }
}

// Create the options div.
function createAdvancedOptionsDiv(componentId) {
    var componentInterfaceId = 'componentInterface_' + componentId;
    var advancedOptionsButtonId = 'advancedOptions_' + componentId;
    var thisDiv = null;

    thisDiv = jQuery('<div class="componentOptionFields"><form class="genericInputForm"><br/>'
            + '<input type="hidden" id="' + componentInterfaceId + '" />'
            + '<div class="wfBlueButton" name="advancedOptionsButton" id="'
            + advancedOptionsButtonId + '" /><br/>'
            + '</form></div>');

    return thisDiv;
}

/**
 * Handles the Options File Available files drop-down for dataset-associated workflows.
 */
function existingOptionsFile() {
    var splitString = this.id.split('_');
    var dsId = null;

    if (splitString.length == 2) {
        var componentId = splitString[1];

        var thisComponent = null;
        var fileLabel = null;
        var endpointsKey = null;
        if (currentDigraph.components != null && currentDigraph.components.length > 0) {
            jQuery.each(currentDigraph.components, function() {
                if (this.component_id == componentId) {
                    thisComponent = this;
                    return false;
                }
            });
        }

        endpointsKey = thisComponent.component_type.toLowerCase()
                        + "-" + thisComponent.component_name.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase();

        if (componentOutputEndpoints[endpointsKey] > 0) {
            for (var outIndex = 0 ; outIndex < componentOutputEndpoints[endpointsKey]; outIndex++) {
                fileLabel = componentOutputEndpoints[endpointsKey + '_list'][outIndex];
            }
        }

        var fileForm = '#importForm_' + componentId;
        var fileIndex = 0;
        var dsFileId = jQuery('option:selected', this).val();
        var fileDatasetId = jQuery('#fileDatasetId_' + dsFileId).val();

        if (this.name == 'wfFileId' && fileLabel != null) {
            previousMiniIcon = "Processing";
            new Ajax.Request('WorkflowEditor', {
                parameters : {
                    requestingMethod: 'WorkflowEditorServlet.existingFile',
                    workflowId : currentDigraph.id,
                    datasetId : dsId,
                    componentId : componentId,
                    wfFileId: jQuery(this).val(),
                    fileDatasetId: fileDatasetId,
                    fileIndex: fileIndex,
                    fileLabel: fileLabel
                },
                onComplete : uploadOptionsFileCompleted,
                beforeSend : wfShowStatusIndicatorModal(false, "Processing..."),
                onSuccess : wfHideStatusIndicator,
                onException : function(request, exception) {
                    wfHideStatusIndicator(); throw(exception);
                }
            });

        }
    }

}

/**
 * Handles the Attach to Dataset option for Import File Uploads.
 */
function attachUploadToDataset() {
    if (workflowIsRunning) {
        showCancelWarning();
    } else {
        var digitPattern = "[0-9]+";
        var regex = new RegExp(digitPattern,"gi");
        var hasFile = false;
        if (jQuery('#filePath').val().length > 0 && jQuery('#filePath').val().match(regex) !== null) {
            hasFile = true;
        }

        if (!hasFile) {
            var dialogHtml = '' + "You must upload a file before you can add the file to a dataset." + ' <br/>';
            wfInfoDialog('fileUploadDialog', dialogHtml, 'Error');
        } else if (isWorkflowSaved === false) {
            promptForSaving();
        } else {
            // attach file to the dataset
            var splitString = this.id.split('_');
            var dsId = null;

            if (splitString.length == 2) {
                var componentId = splitString[1];

                var thisComponent = null;
                var fileLabel = null;
                var endpointsKey = null;
                if (currentDigraph.components != null && currentDigraph.components.length > 0) {
                    jQuery.each(currentDigraph.components, function() {
                        if (this.component_id == componentId) {
                            thisComponent = this;
                            return false;
                        }
                    });
                }

                endpointsKey = thisComponent.component_type.toLowerCase()
                                + "-" + thisComponent.component_name.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase();

                if (componentOutputEndpoints[endpointsKey] > 0) {
                    for (var outIndex = 0 ; outIndex < componentOutputEndpoints[endpointsKey]; outIndex++) {
                        fileLabel = componentOutputEndpoints[endpointsKey + '_list'][outIndex];
                    }
                }

                var fileForm = '#importForm_' + componentId;
                var fileIndex = 0;

                if (this.name == 'wfDatasetId' && fileLabel != null) {
                    var wfFileId = jQuery('#filePath').val();

                    new Ajax.Request('WorkflowEditor', {
                        parameters : {
                            requestingMethod: 'WorkflowEditorServlet.existingDataset',
                            workflowId : currentDigraph.id,
                            wfFileId : wfFileId,
                            datasetId : dsId,
                            componentId : componentId,
                            wfDatasetId: jQuery(this).val(),
                            fileIndex: fileIndex,
                            fileLabel: fileLabel
                        },
                        onComplete : attachDatasetFileCompleted,
                        beforeSend : wfShowStatusIndicator("Attaching..."),
                        onSuccess : wfHideStatusIndicator,
                        onException : function(request, exception) {
                            wfHideStatusIndicator(); throw(exception);
                        }
                    });

                }
            }
        }
    }
}

/**
 * Handles the Options File Upload button.
 */
function uploadOptionsFile() {

    if (workflowIsRunning) {
        showCancelWarning();
    } else {
         var splitString = this.id.split('_');
         var dsId = null;

         if (splitString.length == 2) {
             var componentId = splitString[1];

             var fileForm = '#importForm_' + componentId;
             var fileIndex = jQuery(fileForm + ' ' + '#fileIndex').val();
             var fileLabel = jQuery(fileForm + ' ' + '#fileLabel').val();

             if (this.name == 'file') {

                 var formData = new FormData();
                 formData.append('file', jQuery(this)[0].files[0]);
                 formData.append('fileIndex', fileIndex);
                 formData.append('fileLabel', fileLabel);
                 formData.append('requestingMethod', 'WorkflowEditorServlet.fileUpload');
                 formData.append('workflowId', currentDigraph.id);
                 formData.append('componentId', componentId);
                 formData.append('dsId', dsId);

                 jQuery.ajax({
                     url : 'WorkflowEditor', //Server script to process data
                     type : 'POST',
                     // Form data
                     data : formData,
                     beforeSend : function () {
                         processComponentStatusInfoLocked = true;
                         wfShowStatusIndicatorModal(true, "Uploading...");
                     },
                     // Options to tell jQuery not to process data or worry about content-type.
                     async : true,
                     cache : false,
                     contentType : false,
                     processData : false
                 }).done(uploadOptionsFileCompleted).fail(error_uploadOptionsFile);
             }
         }
    }
}

/**
 * Options File upload error handler.
 */
function error_uploadOptionsFile() {
    wfHideStatusIndicator();
    var dialogHtml = 'Your file upload has failed. '
        + datashopHelpInfo;
    wfInfoDialog('uploadFailedDialog', dialogHtml, 'Error');
    monitorComponentStatus();
}


/**
 * Options File upload completed handler. File uploads are restricted to 1 file option per component.
 */
function uploadOptionsFileCompleted(data) {
    if (!workflowIsRunning) {
        var fileForm = null;

        if (data !== undefined && data.responseText !== undefined) {
            json = data.responseText.evalJSON(true);
            data = json;
            //fileForm = '#dsFileForm_' + json.componentId;
        } else {
            //fileForm = '#importForm_' + data.componentId;
        }

        wfHideStatusIndicator();

        if (data.componentId !== undefined && data.componentId != null) {

            fileForm = '#importForm_' + data.componentId;

            var componentId = data.componentId;
            var message = null;
            // The option file upload completed. 1 upload allowed per import prevents unecessary complexity.
            if (data.success == "true") {
                jQuery(fileForm + ' ' + '#filePath').val(data.fileId);
                jQuery(fileForm + ' ' + '#fileName').val(data.fileName);
                jQuery(fileForm + ' ' + '#fileName').attr('title', data.fileName);
                jQuery(fileForm + ' ' + '#fileIndex').val(data.fileIndex);
                jQuery(fileForm + ' ' + '#fileLabel').val(data.fileLabel);
                // The files object must be an array; recast as array if needed
                if (componentOptions[componentId].files === undefined) {
                    componentOptions[componentId].files = [];
                } else {
                    if (!jQuery.isArray(componentOptions[componentId].files)) {
                        componentOptions[componentId].files = [ componentOptions[componentId].files ];
                    }
                }

                // The newly uploaded file
                var newFile = {};
                newFile.file = {};
                newFile.file.file_path = data.fileId;
                newFile.file.file_name = data.fileName;
                newFile.file.index = data.fileIndex;
                newFile.file.label = data.fileLabel;

                if (JSON.stringify(data.columnHeaders) != '"[]"') {
                    // This file is a tab-delimited or CSV file with headers.
                    newFile.file.metadata = {};
                    newFile.file.metadata.header = data.columnHeaders;
                }
                // Use a container so that the json <-> xml will be valid and unambiguous.
                var fileContainer = {};
                fileContainer[newFile.file.label] = newFile.file;
                componentOptions[componentId].files = [];
                componentOptions[componentId].files.push(fileContainer);
                //alert(JSON.stringify(componentOptions[componentId].files[data.fileIndex]));

                // File upload: set dirty bit
                dirtyBits[componentId] = DIRTY_FILE;

                if (data.datasetLink !== undefined && data.datasetLink != "") {
                    message = SELECTED_DATASET_FILE_MESSAGE;
                } else {
                    message = UPLOADED_NEW_FILE_MESSAGE;
                }

            } else {
                // Error uploading file. Reset values.
                jQuery(fileForm + ' ' + '#fileName').val('');
                jQuery(fileForm + ' ' + '#fileName').attr('title', '');
                jQuery(fileForm + ' ' + '#fileIndex').val('');
                jQuery(fileForm + ' ' + '#fileLabel').val('');

                var dialogHtml = '' + data.message + '<br/>';

                wfInfoDialog('fileUploadDialog', dialogHtml, 'Error');

            }


            // We must set isWorkflowSaved before calling updateAttachBlock.
            isWorkflowSaved = false;
            // We must set isWorkflowSaved before calling updateAttachBlock.
            updateAttachBlock(data.datasetLink, data.datasetName, componentId);

            var fileInfoString = "File: " + data.fileName;
            if (data.projectName !== undefined && data.projectName != '') {
                projectName = data.projectName;
                fileInfoString = 'Project: ' + data.projectName + '&#013;&#010;'
                    + 'Dataset: ' + data.datasetName + '&#013;&#010;'
                    + 'File: ' + data.fileName;
            } else if (data.datasetName !== undefined && data.datasetName != '') {
                fileInfoString = 'Dataset: ' + data.datasetName + '&#013;&#010;'
                + 'File: ' + data.fileName;
            }
            // To unescape chars in the fileName title:
            var unescapedTitle = jQuery('<div/>').html(fileInfoString).text();
            jQuery('#fileName').attr('title', unescapedTitle);

            lastCall = "uploadOptionsFileCompleted";

            // For custom options, set the changed components to changed state
            customOptionsHandleOptionsUpdate(data.componentId, false);

            saveTemporaryWorkflowTransient(currentDigraph.id, message, null, null, null);

        } else if (data.success == "false") {
            wfInfoDialog('fileUploadDialog', data.message, 'Error');
        } else {
            testLoggedOut(data);
        }
    }
}
function updateAttachBlock(datasetLink, datasetName, componentId) {
    if (!workflowIsRunning) {
        var attachBlock = "";
        var digitPattern = "[0-9]+";
        var regex = new RegExp(digitPattern,"gi");



        if (datasetLink.length > 0 && datasetLink.match(regex) !== null) {
            // File is already linked to a dataset
            attachBlock = '<div id="datasetLinkDiv" class="dataset_linked">'
                + '<a href="Files?datasetId=' + datasetLink + '" target="_blank">'
                + '<img alt="Link to Dataset which contains the file" id="datasetLinkButton" title="Open Dataset Info" src="images/dataset_linked.png" />'
                + '<label for="datasetLinkButton">File is attached to dataset ' + datasetName + '</label>'
                '</a></div>';
        } else {
            // File is not yet attached
            attachBlock = '<div class="addDatasetLink"><div id="datasetLinkDiv" class="dataset_not_linked">'
                + '<img id="datasetLinkButton" src="images/dataset_not_linked.png" title="Attach upload to dataset" />'
                + '<label for="datasetLinkButton">Attach upload to dataset</label>'
                + '</div></div>';
        }

        jQuery('#datasetLinkDiv').remove();
        if (jQuery('#componentDatasetInfo').length > 0) {
            jQuery('#componentDatasetInfo').html(attachBlock);
        }
        jQuery('.addDatasetLink').off('click');
        jQuery('.wfFileUploadRow').hide();

        jQuery('.addDatasetLink').click(function() {
            var digitPattern = "[0-9]+";
            var regex = new RegExp(digitPattern,"gi");
            var hasFile = false;
            if (jQuery('#filePath').val().length > 0 && jQuery('#filePath').val().match(regex) !== null) {
                hasFile = true;
            }
            if (isWorkflowSaved === false && hasFile) {
                promptForSaving();
            } else if (!hasFile) {
                var dialogHtml = '' + "You must upload a file before you can add the file to a dataset." + ' <br/>';
                wfInfoDialog('fileUploadDialog', dialogHtml, 'Error');
            } else {
                jQuery('.wfFileUploadRow').show();
                jQuery('.addDatasetLink').hide();
                ////jQuery('.existingDataFile').hide();
            }
        });
    } else {
        showCancelWarning();
    }
}


function promptForSaving(addDatasetButton) {
    jQuery('<div />', {
        id : 'wfSaveConfirmationDialog'
    }).html(
            'You must save your changes before you can attach the file to a dataset. Save now?').dialog({
        open : function() {
            jQuery('.ui-button').focus();
        },
        autoOpen : true,
        autoResize : true,
        resizable : false,
        width : 600,
        height : 245,
        modal : true,
        title : 'Do you wish to save?',
        buttons : {
            'Yes' : function() {
                jQuery(this).dialog('close');
                lastCall = "saveButtonPress";
                saveOpenOptions();
                saveCurrentWorkflow(currentDigraph.id, null, null, null, null);
                jQuery('.wfFileUploadRow').show();
                ////jQuery('.existingDataFile').hide();
                jQuery('.addDatasetLink').hide();
                jQuery(".existingDataset option").prop("selected", false);
                // Global object to click when saving is completed
                addToDatasetButton = addDatasetButton;

            },
            'Cancel' : function() {
                jQuery(".existingDataset option").prop("selected", false);
                jQuery(this).dialog('close');
            }
        },
        close : function() {
            jQuery(this).remove();
        }
    });

}
/**
 * Options File upload completed handler. File uploads are restricted to 1 file option per component.
 */
function attachDatasetFileCompleted(data) {


    if (data !== undefined && data.responseText !== undefined) {
        json = data.responseText.evalJSON(true);
        data = json;

    }

    wfHideStatusIndicator();

    if (data.componentId !== undefined && data.componentId != null) {

        var componentId = data.componentId;

        // The option dataset attachment completed.
        if (data.success == "true") {
            var dialogHtml = '' + data.message + ' <br/>';
            wfInfoDialog('fileUploadDialog', dialogHtml, 'Success');

            if (data.datasetId !== "") {
                updateAttachBlock(data.datasetId, data.datasetName, componentId);
            }

            var fileInfoString = "File: " + data.fileName;
            if (data.projectName !== undefined && data.projectName != '') {
                projectName = data.projectName;
                fileInfoString = 'Project: ' + data.projectName + '&#013;&#010;'
                    + 'Dataset: ' + data.datasetName + '&#013;&#010;'
                    + 'File: ' + data.fileName;
            } else if (data.datasetName !== undefined && data.datasetName != '') {
                fileInfoString = 'Dataset: ' + data.datasetName + '&#013;&#010;'
                + 'File: ' + data.fileName;
            }
            // To unescape chars in the fileName title:
            var unescapedTitle = jQuery('<div/>').html(fileInfoString).text();
            jQuery('#fileName').attr('title', unescapedTitle);

        } else {
            var dialogHtml = '' + data.message + ' <br/>';
            wfInfoDialog('fileUploadDialog', dialogHtml, 'Error');
        }
        monitorComponentStatus();
    } else {
        testLoggedOut(data);
    }
}

function requestResultsInNewWindow() {
    var splitString = this.id.split('_');
    var submitted = false;
    if (currentDigraph != null && currentDigraph.hasOwnProperty('id')) {
        if (splitString.length == 2) {
            var componentId = splitString[1];
            if (componentId !== undefined && componentId != null) {
                jQuery("#requestResults_" + componentId).submit();
                return;
            }
        }
    }
    if (submitted == false) {
        jQuery("requestResults").submit();
    }
}

function requestDebugInfoFromButton() {
    var splitString = this.id.split('_');
    if (splitString.length == 2) {
        var componentId = splitString[1];
        if (componentId !== undefined && componentId != null) {
            requestDebugInfo(componentId);
        }
    } else {
        // invalid component id
    }
}

/**
 * Check the logging statements to see if the component logged a progress
 * update.  Display the update if it exists.
 * @param componentId
 */
function updateComponentProgress(componentId) {
    // Request the debugging lines that contain the special progress indicator
    new Ajax.Request('WorkflowEditor', {
        parameters : {
            requestingMethod: 'WorkflowEditorServlet.requestDebugInfo',
            workflowId : workflowId,
            componentId : componentId,
            textFilter : '.*' + COMPONENT_PROGRESS_INDICATOR + '.*'
        },
        onComplete : retrieveProgressInfo
    });
}

/**
 * With the debug statements for a particular component that is currently running,
 * display the most recent progress message.
 * @param transport - all the logging lines that contain the special progress indicator
 */
function retrieveProgressInfo (transport) {
    let json = transport.responseJSON;
    if (json == null) {
        return;
    }

    let componentId = json.componentId;

    let textData = json.textData;
    if (textData != null) {
        let mostRecentProgressMessage = null;
        let mostRecentProgressMessageTime = 0;

        // Loop through the log files to find the most recent message
        Object.keys(textData).forEach(function(logFileName) {
           let logFileMessages = textData[logFileName];
           if (logFileMessages != null) {
               // Find the most recent, non-empty log statement
               for (let i = logFileMessages.length; i >= 0; i--) {
                   let message = logFileMessages[i];

                   if (message != null && message.length > 0) {
                       let messageTimeStr = message.replace(/(.*@)(.*)(@.*)/, '$2');
                       messageTime = Date.parse(messageTimeStr);
                       if (messageTime != NaN
                               && messageTime > mostRecentProgressMessageTime // Most recent message
                               && messageTime > startedRunningTime) // Message was logged after the workflow started running
                       {
                           // Remove the time from the message
                           mostRecentProgressMessage = message.replace('@' + messageTimeStr + '@', '');
                           mostRecentProgressMessageTime = messageTime;
                       }
                   }
               }
           }
        });

        // If there exists a progress message, display it on the component
        if (mostRecentProgressMessage != null && componentId != null) {
            // Isolate the progress message from the rest of the logging line
            let splitMessage = mostRecentProgressMessage.split(COMPONENT_PROGRESS_INDICATOR);
            if (splitMessage.length >= 2) {
                let progressMessage = splitMessage[1];
                let messageHtml = jQuery('<span />').addClass('componentProgressMessage')
                    .text(progressMessage);
                let componentDiv = jQuery('#componentStatus_' + componentId);
                if (componentDiv.hasClass('componentStatusIcon_running') && progressMessage.length > 0) {
                    // Only update the status if the component is still running
                    componentDiv.html(messageHtml);
                }
            }
        }
    }
}

function requestDebugInfo(componentId) {

    var submitted = false;
    if (currentDigraph != null && currentDigraph.hasOwnProperty('id')) {
        if (componentId !== undefined && componentId != null) {
            var divId = 'debugInfoDiv';
            // If div exists, we only need to update the dialog text
            if (jQuery('#' + divId).length > 0 || confirmedSensitiveDataWarning) {
                // If the user entered text in the textFilter input, then filter by textFilter.
                var debugTextFilter = "";
                if (jQuery('#debugFilterText').val() != null && jQuery('#debugFilterText').val() != '') {
                    debugTextFilter = '(?i).*' + jQuery('#debugFilterText').val() + '.*';
                }

                new Ajax.Request('WorkflowEditor', {
                    parameters : {
                        requestingMethod: 'WorkflowEditorServlet.requestDebugInfo',
                        workflowId : workflowId,
                        componentId : componentId,
                        textFilter : debugTextFilter
                    },
                    onComplete : retrieveDebugInfo
                });
            } else {
            // The div does not exist. Create a new one, but warn the user that sensitive data may be present.
                jQuery('<div />', {
                    id : 'wfDeleteComponentConfirmationDialog'
                }).html(
                    'Debugging logs may contain sensitive or private information '
                        + ' generated by your data.')
                .dialog({
                    open : function() {
                        jQuery('.ui-button').focus();
                    },
                    autoOpen : true,
                    autoResize : true,
                    resizable : false,
                    width : 400,
                    height : "auto",
                    title : 'Warning: Sensitive Data',
                    buttons : {
                        'Cancel' : function() {
                            jQuery(this).dialog('close');
                        },
                        'OK' : function() {
                            confirmedSensitiveDataWarning = true;
                            new Ajax.Request('WorkflowEditor', {
                                parameters : {
                                    requestingMethod: 'WorkflowEditorServlet.requestDebugInfo',
                                    workflowId : workflowId,
                                    componentId : componentId
                                },
                                onComplete : retrieveDebugInfo
                            });
                            jQuery(this).dialog('close');
                        }
                    },
                    close : function() {
                        jQuery(this).remove();
                    }
                });
            }

        }
    }
}

function scrollToAnchor(anchorId) {
    if (jQuery('#' + anchorId).length > 0) {
        var anchorTop = jQuery('#' + anchorId).offset().top - jQuery('#debugInfoDiv').offset().top - 100;
        jQuery('#debugInfoDiv').animate({ scrollTop: anchorTop });
    }
}

function resetDebugInfoScroll() {
    if (jQuery('#debugInfoDiv').length > 0) {
        jQuery('#debugInfoDiv').animate({ scrollTop: 0 });
    }
}

function setDebugInfoEventTriggers() {
    jQuery('.debugAnchorLink').off('click');
    jQuery('.debugAnchorLink').click(function() {
        var anchorId = 'anchor_' + jQuery(this).text().replace(/[.]/gi, "\\.");
        scrollToAnchor(anchorId);
        return false;
    });

    jQuery('.goBackAnchorLink').off('click');
    jQuery('.goBackAnchorLink').click(function() {
        resetDebugInfoScroll();
        return false;
    });

    jQuery('.debugDownloadDiv').mouseover(function() {
        jQuery('.downloadIcon', this).attr('src', 'css/images/download_hl.svg');
    });
    jQuery('.debugDownloadDiv').mouseout(function() {
        jQuery('.downloadIcon', this).attr('src', 'css/images/download.svg');
    });
}

function retrieveDebugInfo(transport) {

    var json = transport.responseText.evalJSON(true);

    if (jQuery('#debugMiniWaitingIcon').length > 0) {
        jQuery('#debugMiniWaitingIcon').remove();
    }

    if (json != null && json.error_flag !== undefined && json.error_flag == "true") {
        wfTimerDialog("Error", "Debug info not found: " + json.message, lsWorkflowListDialogTime);
    } else if (json != null && json.success !== undefined) {

        var textAsString = "";
        if (json.textData !== undefined && json.textData != null) {

            textAsString = getDebugInfoAsString(json);
        }
        var windowWidth = jQuery(window).width();
        var maxWidth = windowWidth * 0.9;
        var windowHeight = jQuery(window).height();
        var maxHeight = windowHeight * 0.9;

        // If lineCount is 0, NO_DEBUG_LOG_TEXT doesn't need a big window.
        if (textAsString == NO_DEBUG_LOG_TEXT) {
            maxWidth = 407;
            maxHeight = 122;
        }

        var divId = 'debugInfoDiv';
        // The dialog exists, so just refresh the text.
        if (jQuery('#' + divId).length > 0) {
            // Refresh existing dialog text.
            jQuery('.debugInfoText').html(textAsString);
            setDebugInfoEventTriggers();
        } else {
            var debugInfoHeader = '<div id="debugFilterTextDiv">Search: '
                + '<input title="Use plain text or regular expressions to search the log files." '
                    + 'type="text" id="debugFilterText" /></div>';
            // Open a new dialog with the debug info.
            clearInterval(globalDebugWindowTimer);
            jQuery('<div id=\'' + divId + '\'><div class="debugInfoHeader">' + debugInfoHeader + '</div>'
                    + '<div class="debugInfoText">'
                        + textAsString
                        + '</div></div>')
                .dialog({
                resizable : true,
                width : maxWidth,
                height : maxHeight,
                modal : true,
                title : "Debugging Info (" + json.componentId + ")",
                open : function() {
                    setDebugInfoEventTriggers();

                    jQuery('#debugFilterText').on('input',function(e) {
                        jQuery('#debugMiniWaitingIcon').remove();
                        jQuery('#debugFilterTextDiv').append(''
                            + ' <img id="debugMiniWaitingIcon" src="images/waiting.gif" />'
                                + '');
                    });
                    // Refresh the text by calling requestDebugInfo every globalDebugWindowTimerRefresh seconds
                    globalDebugWindowTimer = setInterval(function() {
                        requestDebugInfo(json.componentId);
                    }, globalDebugWindowTimerRefresh);
                },
                close : function() {
                    // Clear the interval timer and remove the div.
                    clearInterval(globalDebugWindowTimer);
                    jQuery(this).remove();
                }
            });
        }
    }
}


function getDebugInfoAsString(json) {

    var textAsString = "";
    var evenRow = false;

    if (json.numFiles > 1) {
        textAsString = textAsString + '<div class="debugAnchorHeader">Outline</div>';
        jQuery.each(json.textData, function(fileName, fileLines) {
            textAsString = textAsString + '<div class="debugFileOutlineRow"><div class="debugAnchorLink" '
                + 'id="anchorLink_' + fileName + '">' + fileName + '</div>'
                + '<a class="debugFileLink" href="LearnSphere?wfDebugId=' + json.workflowId
                    + '&componentId=' + json.componentId + '&debugFile=' + fileName + '">'
                    + '</a></div>'; // <img title="Download" src="images/disk.png" />
        });
        textAsString = textAsString + "<hr/>";
    }

    // The textFilter is optional and was applied in the servlet if it exists.
    jQuery.each(json.textData, function(fileName, fileLines) {
        var lineCount = 0;
        textAsString = textAsString + '<div class="debugFileRow">'
            + '<span id="anchor_' + fileName + '" class="debugAnchorHeader">' + fileName + '</span>'
            + '<a class="debugFileLink" href="LearnSphere?wfDebugId=' + json.workflowId
            + '&componentId=' + json.componentId + '&debugFile=' + fileName + '">'
            + '<div class="debugDownloadDiv">'
            + '<img class="downloadIcon" title="Download full log" src="css/images/download.svg" /> [Download full log]</a></div>'
            + '</div>';

        jQuery.each(fileLines, function(lineIndex) {
            if (lineIndex != null) {
                var dLine = fileLines[lineIndex];
                if (dLine != null && dLine != "") {
                    var warnPattern1 = new RegExp(".*WARN:.*","gi");
                    var warnPattern2 = new RegExp(".*WARNING:.*","gi");
                    var debugLineColor = '';
                    if (dLine.match(warnPattern1) !== null || dLine.match(warnPattern2) !== null) {
                        debugLineColor = 'warningDebugLine ';
                    }

                    var errorPattern = new RegExp(".*ERROR:.*","gi");
                    if (dLine.match(errorPattern) !== null) {
                        debugLineColor = 'errorDebugLine ';
                    }

                    highlight = '';
                    if (evenRow) {
                        textAsString = textAsString + '<div class="' + debugLineColor + 'debugRowHighlighted">';
                    } else {
                        textAsString = textAsString + '<div class="' + debugLineColor + '">';
                    }
                    textAsString = textAsString + dLine + "<br>";

                    textAsString = textAsString + "</div>";

                    evenRow = !evenRow;
                    lineCount++;
                }
            }
        });
        if (lineCount == 0) {
            textAsString = textAsString + NO_DEBUG_LOG_TEXT;
        } else {
            textAsString = textAsString + '<div class="goBackAnchorLink">Back to top</div><br/><br/>';

        }
    });
    // If lineCount is 0, the user sees NO_DEBUG_LOG_TEXT

    return textAsString;
}

function getComponentIdFromEndpointId(endpointId) {
    var splitEndpointIdTarget = endpointId.split("_");
    var targetComponentId = null;
    if (splitEndpointIdTarget != null && splitEndpointIdTarget.length > 1) {
        targetComponentId = splitEndpointIdTarget[0];
    }
}

var isDataPreviewDialogOpen = 0;


var previewIsStarted = false;
var previewIsDisplayed = false;

var jsPlumbEventFiresTwiceHack = 0;
function setupSourceEndpoint(mySource, selectedComponentId, selectedOutIndexId) {
    mySource.bind("dblclick", function(endpoint) {

        if (jsPlumbEventFiresTwiceHack == 0) {
            jsPlumbEventFiresTwiceHack = 1;
            jQuery('#componentOutputDataPreview').dialog("close");
            jQuery('.jqx-menu-wrapper').remove();

            if (previewIsStarted == false) {

                previewIsStarted = true;
                previewIsDisplayed = false;

                previewOutputData(selectedComponentId, selectedOutIndexId);

            }
        } else {
            jsPlumbEventFiresTwiceHack = 0;
        }
    });
}

function checkWorkflowConnections(connection) {
    var passing = true;
    var connAllowed = false;

    if (connection == undefined) {
        return true;
    }

    var targetComponentId = getComponentIdFromEndpointId(connection.targetId);
    var sourceComponentId = getComponentIdFromEndpointId(connection.sourceId);


    if (allowedConnections[componentIdTypeMap[targetComponentId]] !== undefined) {

        jQuery.each(allowedConnections[componentIdTypeMap[targetComponentId]], function (precondIndex, precondValue) {

            if (componentIdTypeMap[sourceComponentId] == precondValue) {

                connAllowed = true;
            }
        });
    }

    if (!connAllowed) {
        passing = false;
    }
    return true;
}

function forwardToWorkflowList() {
    var dsId = null;

    var datasetIdParam = getParam('datasetId', window.location.href);
    if (datasetIdParam != null && datasetIdParam.trim() != '') {
        dsId = datasetIdParam;
    }

    if (dsId != null && dsId != '') {
        window.location.href = "LearnSphere?datasetId=" + dsId;
        return;
    }

    window.location.href = "LearnSphere";
}


function logWorkflowComponentAction(workflowId, dsId,
        componentId, componentName, componentType, humanReadableId,
        nodeIndex, workflowFileId, datasetFileId, action, info) {
    new Ajax.Request('LearnSphere', {
        parameters : {
            requestingMethod: 'ManageWorkflowsServlet.logComponentAction',
            workflowId : workflowId,
            datasetId : dsId,
            componentId : componentId,
            componentName : componentName,
            componentType : componentType,
            humanReadableId : humanReadableId,
            nodeIndex : nodeIndex,
            workflowFileId : workflowFileId,
            datasetFileId : datasetFileId,
            action : action,
            info : info
        },
        onComplete : function() {

        }
    });
}


function logWorkflowAction(workflowId, dsId,
        newWorkflowId, action, info) {
    new Ajax.Request('LearnSphere', {
        parameters : {
            requestingMethod: 'ManageWorkflowsServlet.logWorkflowAction',
            workflowId : workflowId,
            datasetId : dsId,
            newWorkflowId : newWorkflowId,
            action : action,
            info : info
        },
        onComplete : function() {

        }
    });
}

function getScrollBarWidth() {
    var outer = jQuery('<div>').css({visibility: 'hidden', width: 100, overflow: 'scroll'}).appendTo('body'),
        widthWithScroll = jQuery('<div>').css({width: '100%'}).appendTo(outer).outerWidth();
    outer.remove();
    return 100 - widthWithScroll;
};
