/**
 * This file contains operations for component selection, component copy/paste,
 * and other component operations.
 */

// A list of the currently selected components.
var selectedComponents = [];
var componentCopyBuffer = [];
/* Mapping of source (copied) to target (pasted) components. */
var cMapping = {};
/* The smallest and biggest 'top' value before a paste operation commences. */
var lowestY = 99999;
var highestY = 0;
var selectedLowestY = 99999;
var drawingMultipleConnections = false;
var originalPos = {};
var draggedComponent = false;

/* Initializes the variables needed to handle selection of components
 * either by Click or Select_Area; Holding down shift retains previously selected components.
 */
function initComponentAreaSelector() {
    var zoomCorrection = 14;

    var startingPos = [];

    if (currentDigraph != null && currentDigraph.isView !== undefined && currentDigraph.isView !== true) {
        jQuery('#process-div')
        // Mouse Down
        .mousedown(function (downEvt) {
            if (downEvt.which == 1
                && !jQuery(downEvt.target).hasClass('wfCompSelectList')
                    && !jQuery(downEvt.target).hasClass('wfCompSelectLink')) {
                isShiftKeyPressed(downEvt);
                // Unselect components if the user clicks outside one of the following DOM objects.
                if (jQuery(downEvt.target).attr('class') !== undefined
                        && !jQuery(downEvt.target).hasClass('process-component')
                        && !jQuery(downEvt.target).hasClass('componentTitle')
                        && !jQuery(downEvt.target).parent().hasClass('importTypeTitle')
                        && !jQuery(downEvt.target).hasClass('componentSubtitle')
                        && !jQuery(downEvt.target).hasClass('workflowAnnotation')
                        && !jQuery(downEvt.target).parent().hasClass('workflowAnnotation')
                        && !jQuery(downEvt.target).parent().hasClass('process-component')) {

                    jQuery('#compSelectBorder').remove();

                    if (!shiftDown) {
                        // Remove components from the list unless shift is held down.
                        selectedComponents = [];

                        initSelectAreaContextMenu(false);
                        jQuery('.process-component').each(function(pComp) {
                            jQuery(this).css('border', '');
                            jQuery(this).css('width', (componentWidth) + 'px');
                            jQuery(this).css('height', (specialComponentHeight[jQuery(this).attr('id')] - 4) + 'px');
                            jQuery(this).removeClass('selectedComponent');
                            instance.removeFromPosse(jQuery(this), "selectedPosse");
                        });

                        highlightComponentConnections();
                    }

                    startingPos = [downEvt.pageX, downEvt.pageY];

                // Else ONLY select the component if one of the following DOM objects is clicked.
                }
            }
        })
        // Mouse Move
        .mousemove(function (moveEvt) {
            if (!(Math.abs(moveEvt.pageX - startingPos[0]) < 5 && Math.abs(moveEvt.pageY === startingPos[1]) < 5)) {

                // Draw the selection area rectangle.
                if (jQuery().length <= 0) {
                    jQuery('#compSelectBorder').remove();
                    jQuery('<div id="compSelectBorder" />').appendTo('#process-div');
                }

                // Allow them to go left or right when creating a select area rectangle.
                var startX = startingPos[0];
                var startY = startingPos[1];
                var endX = moveEvt.pageX;
                var endY = moveEvt.pageY;

                if (moveEvt.pageX < startingPos[0]) {
                    endX = startX;
                    startX = moveEvt.pageX;
                }
                if (moveEvt.pageY < startingPos[1]) {
                    endY = startY;
                    startY = moveEvt.pageY;
                }

                jQuery('#compSelectBorder').height((endY - startY)  / percentZoom - zoomCorrection);
                jQuery('#compSelectBorder').width((endX - startX)  / percentZoom);
                jQuery('#compSelectBorder').css('top', ((startY - jQuery('#ls-header').height() - jQuery('#wfStatusBar').height())  / percentZoom - zoomCorrection + jQuery('#process-div').scrollTop()) + 'px');
                jQuery('#compSelectBorder').css('left', ((startX - jQuery('#process-selector-div').width()) / percentZoom + jQuery('#process-div').scrollLeft()) + 'px');
            }
        })
        // Mouse Up.
        .mouseup(function (upEvt) {
            if (upEvt.which == 1
                && !jQuery(upEvt.target).hasClass('wfCompSelectList')
                    && !jQuery(upEvt.target).hasClass('wfCompSelectLink')
                    && jQuery(upEvt.target).attr('class') !== undefined
                    && !jQuery(upEvt.target).hasClass('process-component')
                    && !jQuery(upEvt.target).hasClass('componentTitle')
                    && !jQuery(upEvt.target).parent().hasClass('importTypeTitle')
                    && !jQuery(upEvt.target).hasClass('componentSubtitle')) {

                // The user selected components using the selection rectangle.

                isShiftKeyPressed(upEvt);

                // Allow them to go left or right when creating a select area rectangle.
                var startX = startingPos[0];
                var startY = startingPos[1];
                var endX = upEvt.pageX;
                var endY = upEvt.pageY;

                if (upEvt.pageX < startingPos[0]) {
                    endX = startX;
                    startX = upEvt.pageX;
                }
                if (upEvt.pageY < startingPos[1]) {
                    endY = startY;
                    startY = upEvt.pageY;
                }

                // Finish mouse drag
                jQuery('#compSelectBorder').remove();

                // For each component, check its x,y bounds relative to the start and end pos.
                jQuery('.process-component').each(function(pComp) {

                    dVw = (jQuery(this).position().left + jQuery('#process-div').position().left)  / percentZoom;
                    dVh = (jQuery(this).position().top + jQuery('#ls-header').height() + jQuery('#wfStatusBar').height())  / percentZoom + zoomCorrection;

                    if (dVw >= startX / percentZoom && dVw <= endX / percentZoom
                          && dVw + jQuery(this).width() >= startX && dVw + jQuery(this).width() <= endX / percentZoom
                        && dVh >= startY / percentZoom && dVh <= endY / percentZoom
                          && dVh + jQuery(this).height() >= startY / percentZoom && dVh + jQuery(this).height() <= endY / percentZoom) {
                        jQuery(this).css('border', '2px solid #F9BE41'); // purple is #8D46FF
                        jQuery(this).css('width', (componentWidth - 4) + 'px');
                        jQuery(this).css('height', (specialComponentHeight[jQuery(this).attr('id')] - 4) + 'px');
                        jQuery(this).addClass('selectedComponent');
                        selectedComponents.push(jQuery(this).attr('id'));
                        instance.addToPosse([jQuery(this)], "selectedPosse");
                        // Init right-click on selected component context menu.
                        initSelectAreaContextMenu(true);
                    } else if (!shiftDown) {
                        // do nothing
                    }
                });
            } else if (upEvt.which == 1 && ((jQuery(upEvt.target).attr('class') !== undefined
                    && jQuery(upEvt.target).hasClass('process-component'))
                    || jQuery.contains(jQuery('.process-component'), jQuery(upEvt.target))
                    || jQuery(upEvt.target).hasClass('componentTitle')
                    || jQuery(upEvt.target).parent().hasClass('importTypeTitle')
                    || jQuery(upEvt.target).hasClass('componentSubtitle'))) {

                // User clicked on component (and not on options gear or rename icon, etc).

                var properDiv = null;
                if (jQuery(upEvt.target).attr('class') !== undefined
                        && jQuery(upEvt.target).hasClass('process-component')) {
                    properDiv = jQuery(upEvt.target);
                } else {
                    properDiv = jQuery(upEvt.target).closest('.process-component');
                }

                // Component was clicked (with or without shift):

                // Shift was not down and we're not dragging components.
                if (!shiftDown && !draggedComponent) {
                    // Remove components from the list unless shift is held down.
                    selectedComponents = [];
                    initSelectAreaContextMenu(false);
                    jQuery('.process-component').each(function(pComp) {
                        jQuery(this).css('border', '');
                        jQuery(this).css('width', (componentWidth) + 'px');
                        jQuery(this).css('height', (specialComponentHeight[jQuery(this).attr('id')] - 4) + 'px');
                        jQuery(this).removeClass('selectedComponent');
                        instance.removeFromPosse(jQuery(this), "selectedPosse");
                    });

                    selectedComponents.push(properDiv.attr('id'));
                    properDiv.css('border', '2px solid #F9BE41'); // purple is #8D46FF
                    properDiv.css('width', (componentWidth - 4) + 'px');
                    properDiv.css('height', (specialComponentHeight[properDiv.attr('id')] - 4) + 'px');
                    properDiv.addClass('selectedComponent');
                    instance.addToPosse([properDiv], "selectedPosse");
                    // Init right-click on selected component context menu.
                    initSelectAreaContextMenu(true);

                    highlightComponentConnections();

                } else if (!draggedComponent) {
                // Shift is down and we're not dragging components
                    // Else, check to see if it's already selected or not, and if it is, then remove it.
                    if (jQuery.inArray(properDiv.attr('id'), selectedComponents) != -1) { // it was already selected; unselect.
                        var unselectComponentId = null;
                        jQuery(selectedComponents).each(function(selIndex) {
                            if (selectedComponents[selIndex] == properDiv.attr('id')) {
                                unselectComponentId = properDiv.attr('id');
                            }
                        });
                        if (unselectComponentId != null) {
                            selectedComponents = selectedComponents.filter(function(unsel) { return unsel !== unselectComponentId; })
                            jQuery('#' + unselectComponentId).css('border', '');
                            jQuery('#' + unselectComponentId).css('width', (componentWidth) + 'px');
                            jQuery('#' + unselectComponentId).css('height', specialComponentHeight[unselectComponentId] + 'px');
                            jQuery('#' + unselectComponentId).removeClass('selectedComponent');
                            instance.removeFromPosse(jQuery('#' + unselectComponentId), "selectedPosse");
                        }
                    } else { // it was not yet selected
                        selectedComponents.push(properDiv.attr('id'));
                        properDiv.css('border', '2px solid #F9BE41'); // purple is #8D46FF
                        properDiv.css('width', (componentWidth - 4) + 'px');
                        properDiv.css('height', (specialComponentHeight[properDiv.attr('id')] - 4) + 'px');
                        properDiv.addClass('selectedComponent');
                        instance.addToPosse([properDiv], "selectedPosse");
                        // Init right-click on selected component context menu.
                        initSelectAreaContextMenu(true);
                    }

                    highlightComponentConnections();
                }

                draggedComponent = false;
            }
            startingPos = [];

            highlightComponentConnections();


            jQuery('.process-component').each(function(selInd, selObj) {
                var selVal = jQuery(selObj).attr('id');
                var otherCompPos = jQuery('#' + selVal).position();
                originalPos[selVal] = {};
                originalPos[selVal].left = otherCompPos.left / percentZoom;
                originalPos[selVal].top = otherCompPos.top / percentZoom;
            });
            jQuery('.workflowAnnotation').each(function(selInd, selObj) {
                var selVal = jQuery(selObj).attr('id');
                var otherCompPos = jQuery('#' + selVal).position();
                originalPos[selVal] = {};
                originalPos[selVal].left = otherCompPos.left / percentZoom;
                originalPos[selVal].top = otherCompPos.top / percentZoom;
            });
        });

        jQuery(document)
            .keydown(function (e) { // todo: for some reason, control (17) doesn't work
                if (e.keyCode == 16 || e.keyCode == 17 || e.keyCode == 91 || e.keyCode == 93 || e.keyCode == 224) {
                    shiftDown = true;
                }
        });

        // Create drop-down menu.
        initSelectAreaContextMenu(false);

    }

}

/**
 * Show a context menu when right-click a selected component.
 * @returns
 */
function initSelectAreaContextMenu(isComponentSelected) {

    jQuery('#process-div').off('contextmenu');

    jQuery('#process-div').bind("contextmenu", function(e) {

        if (jQuery('.wfCompSelectList').length > 0) {
            jQuery('.wfCompSelectList').remove();
        }

        var allComponentsSelected = true;
        jQuery('.process-component').each(function(pComp) {
            if (!jQuery(this).hasClass('selectedComponent')) {
                allComponentsSelected = false;
                return;
            }
        });

        var osControlChar = '';
        var isProbablyMac = navigator.platform.toUpperCase().indexOf('MAC') !== -1;
        if (isProbablyMac ==  'MAC') {
            osControlChar = '⌘';
        } else {
            osControlChar = 'ctrl';
        }
        var compSelectMenuHtml = '<span class="noSelect wfCompSelectList">';

        var selectAllToggle = '';
        if (allComponentsSelected) {
            selectAllToggle = ' disableRightClickContext';
        }
        compSelectMenuHtml = compSelectMenuHtml
            + '<a class="wfCompSelectLink' + selectAllToggle + '" name="selectedComponentActions" href="javascript:selectAllComponents();">Select All <span class="hotkeySpan">' +
            osControlChar + '-a</span></a>';

        var copyComponentsToggle = '';
        if (!isComponentSelected) {
            copyComponentsToggle = ' disableRightClickContext';
        }
        compSelectMenuHtml = compSelectMenuHtml
            + '<a class="wfCompSelectLink' + copyComponentsToggle + '" name="selectedComponentActions" href="javascript:copyComponents();">Copy <span class="hotkeySpan">' +
            osControlChar + '-c</span></a>';

        var pasteComponentsToggle = '';
        if (componentCopyBuffer.length === 0) {
            pasteComponentsToggle = ' disableRightClickContext';
        }
        compSelectMenuHtml = compSelectMenuHtml
            + '<a class="wfCompSelectLink' + pasteComponentsToggle + '" name="selectedComponentActions" href="javascript:pasteComponents();">Paste <span class="hotkeySpan">' +
            osControlChar + '-v</span></a>';

        var deleteComponentsToggle = '';
        if (!isComponentSelected) {
            deleteComponentsToggle = ' disableRightClickContext';
        }
        compSelectMenuHtml = compSelectMenuHtml
            + '<a class="wfCompSelectLink' + deleteComponentsToggle + '" name="selectedComponentActions" href="javascript:deleteSelectedComponentsConfirm();">Delete <span class="hotkeySpan">' +
            osControlChar + '-d</span></a>';

        compSelectMenuHtml = compSelectMenuHtml + '</span>';
        jQuery(compSelectMenuHtml).appendTo('#process-div');

        // Open components menu

        var pageX = (e.pageX - jQuery('#process-selector-div').width() + jQuery('#process-div').scrollLeft() - 30) / percentZoom;
        var pageY = (e.pageY - jQuery('#ls-header').height() - jQuery('#wfStatusBar').height() + jQuery('#process-div').scrollTop() - 30) / percentZoom;

        expandSelectedComponentsMenu(pageX, pageY, e, isComponentSelected);

        // Stop the right-click event from getting to the browser.
        return false;
    });

    jQuery(document).click(function() {
        jQuery('#componentSelectionMenu').remove();
    });

}

function expandSelectedComponentsMenu(pageX, pageY, e, isComponentSelected) {

    // Hide open "share link" span if one exists
    jQuery('.lsShareLink').slideUp(0);

    jQuery('.linkMenu').slideUp(0);

    jQuery(".wfCompSelectList a").dequeue().slideUp(0);
//alert(jQuery(this).offset().left);
    jQuery('.wfCompSelectList').css('left', (pageX + 40) + 'px');
    jQuery('.wfCompSelectList').css('top', (pageY) + 'px');

    jQuery(".wfCompSelectList a[name=selectedComponentActions]")
        .slideDown(300);
    jQuery('.disableRightClickContext').removeAttr('href');
    jQuery('body').off('click');
    jQuery('body').click(function(evt) {

        if (evt.target.className != "hamburgerIcon") {
            jQuery(".wfCompSelectList a").dequeue().slideUp(0);
        }
    });

}


function isShiftKeyPressed(e) {
    var evt = e || window.event;
    if (evt.shiftKey || evt.ctrlKey) {
        shiftDown = true;
    } else {
        shiftDown = false;
    }
}

/**
 * Copy selected components.
 */
function copyComponents() {
    var logStr = "";
    jQuery(selectedComponents).each(function(selInd, selVal) {
        logStr = logStr + " " + selVal;
    });

    logWorkflowAction(currentDigraph.id, null,
        null, LOG_COPY_COMPONENTS, logStr);

    componentCopyBuffer = selectedComponents;
}


/** Delete selected components confirmation dialog. */
function deleteSelectedComponentsConfirm() {
    if (workflowIsRunning) {
        showCancelWarning();
    } else {
        var component = this;
        if (!currentDigraph.isView) {
            jQuery('<div />', {
                id : 'wfDeleteComponentConfirmationDialog'
            }).html(
                'Are you sure you want to delete the selected component(s)?')
            .dialog({
                open : function() {
                    jQuery('.ui-button').focus();
                },
                autoOpen : true,
                autoResize : true,
                resizable : false,
                width : 400,
                height : "auto",
                title : 'Delete Component(s) Confirmation',
                buttons : {
                    'Yes' : function() {
                        deleteSelectedComponents();
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



/**
 * Paste new components from whatever is in the componentCopyBuffer.
 * Deleted components should somehow be contained.
 * Maybe make the copy buffer more advanced so that it can
 * retain the data for components not in the UI (deleted) but still in componentCopyBuffer.
 */
function pasteComponents() {

    selectedLowestY = 99999;
    var componentsInCopyBuffer = [];
    jQuery(componentCopyBuffer).each(function(selInd, selVal) {
        jQuery('.process-component').each(function(compInd, compObj) {
            if (compObj.id == selVal) {
                componentsInCopyBuffer.push(compObj);
                if (parseFloat(jQuery(compObj).css('top')) < selectedLowestY) {
                    selectedLowestY = parseFloat(jQuery(compObj).css('top'));
                }
            }
        });
    });

    cMapping = {};

    lowestY = 99999;
    highestY = 0;
    jQuery('.process-component').each(function(pcInd, pcObj) {

        if (parseFloat(jQuery(pcObj).css('top')) < lowestY) {
            lowestY = parseFloat(jQuery(pcObj).css('top'));
        } else if (parseFloat(jQuery(pcObj).css('top')) > highestY) {
            highestY = parseFloat(jQuery(pcObj).css('top'));
        }
    });

    jQuery(componentsInCopyBuffer).each(function(selInd, selVal) {

        var component = selVal;
        if (!workflowIsRunning) {
            var componentDiv = jQuery(component);
            var componentId = componentDiv.attr('id');
            var componentType = componentDiv.attr('name').toLowerCase();


            var components = currentDigraph.components;
            if (components !== undefined && components != null) {
                if (!jQuery.isArray(components)) {
                    components = [ components ];
                }

                if (components.length > 0) {

                    jQuery.each(components, function(index) {

                        if (components[index] !== undefined && components[index].component_id == componentId) {

                            // Generate a 6-character (zero padded) random number
                            var numRand = ("00000" + Math.floor(Math.random() * 999999)).slice (-6);

                            componentCounter[componentType] = 1;
                            var readableIdExists = true;
                            while (readableIdExists) {
                                readableIdExists = false;

                                jQuery('.componentSubtitle').each(function() {
                                    //alert(jQuery(this).text() + "==" + componentReadableId);
                                    componentReadableId = formatHeading(componentType)
                                    + "-" + componentCounter[componentType] + "-x" + numRand ;
                                    if (jQuery(this).text() == componentReadableId) {
                                        readableIdExists = true;
                                    }
                                });

                                if (readableIdExists) {
                                    componentCounter[componentType] = componentCounter[componentType] + 1;
                                    componentReadableId = formatHeading(componentType) + " #" + componentCounter[componentType];
                                }

                            }

                            // Set new component Id
                            var newComponentId = formatHeading(componentType)
                                + "-" + componentCounter[componentType] + "-x" + numRand ;

                            cMapping[componentId] = newComponentId;

                            // break the each loop if we find the object to copy
                            return false;
                        }
                    });

                    jQuery.each(components, function(index) {

                        if (components[index] !== undefined && components[index].component_id == componentId) {

                            var newComp = jQuery('#' + componentId);
                            var newId = cMapping[componentId];
                            handleDropEvent(null /* event is null here */, newComp, true, newId);

                            // break the each loop if we find the object to copy
                            return false;
                        }
                    });

                }
                isWorkflowSaved = false;

            }

            if (currentDigraph.hasOwnProperty('components') && currentDigraph.components !== undefined
                    && currentDigraph.components.length == 0) {
                jQuery('#workspaceHint').show();
            }
        } else {
            var dialogHtml = '' + "Cannot paste component(s). Workflow is running." + ' <br/>';
            wfInfoDialog('pasteComponentsDialog', dialogHtml, 'Error');
        }
    });









    jQuery(componentsInCopyBuffer).each(function(selInd, selVal) {
        var component = selVal;
        if (!workflowIsRunning) {
            var componentDiv = jQuery(component);
            var componentId = componentDiv.attr('id');
            var componentType = componentDiv.attr('name').toLowerCase();


            var components = currentDigraph.components;
            if (components !== undefined && components != null) {
                if (!jQuery.isArray(components)) {
                    components = [ components ];
                }

                if (components.length > 0) {


                    jQuery.each(components, function(index) {

                        if (components[index] !== undefined && components[index].component_id == componentId) {
                            // Call copyConnections(sourceId, newComponentId)
                            copyConnections(componentId, cMapping[componentId]);
                        }
                    });


                }
                isWorkflowSaved = false;

            }

            if (currentDigraph.hasOwnProperty('components') && currentDigraph.components !== undefined
                    && currentDigraph.components.length == 0) {
                jQuery('#workspaceHint').show();
            }
        } else {
            var dialogHtml = '' + "Cannot paste component(s). Workflow is running." + ' <br/>';
            wfInfoDialog('pasteComponentsDialog', dialogHtml, 'Error');
        }
    });

    saveTemporaryWorkflowTransient(currentDigraph.id, null, null, null, null);

    // draw gui connections
    drawingMultipleConnections = true;
    drawConnections(currentDigraph.components);
    drawingMultipleConnections = false;
}

// Diagram connections between nodes.
var connections = [];

// The updateConnections function is called whenever a connection
// is added or removed.
function updateConnections(info, remove) {

    // The jsPlumb diagram div.
    var wfDiagramDiv = document.getElementById('wfDiagram');
    var showConnectionInfo = function(s) {
        wfDiagramDiv.innerHTML = s;
        wfDiagramDiv.style.display = 'block';
    };
    var hideConnectionInfo = function() {
        wfDiagramDiv.style.display = 'none';
    };


    var addedConnection = false;
    var deletedConnection = false;
    // jsPlumb function to get the connection data.
    conn = info.connection;
    var updatedComponentId = null;
    var fromInfo = null;

    var targetEndpoint = null;
    if (info.targetEndpoint !== undefined) {
        targetEndpoint = info.targetEndpoint.id;
    }

    var sourceEndpoint = null;
    if (info.sourceEndpoint !== undefined) {
        sourceEndpoint = info.sourceEndpoint.id;
    }

    if (!remove) {

        connections.push(conn);
        // If this is an invalid connection, then instantly remove it and return
        if (!checkWorkflowConnections(conn)) {
            connections.pop();
            return;
        } else {
            // Dirty connection (add)
            if (isInitialized && !workflowIsRunning) {
                dirtyBits[conn.targetId] = DIRTY_ADD_CONNECTION;
                addedConnection = true;
                updatedComponentId = conn.targetId;
                customOptionsHandleOptionsUpdate(updatedComponentId, true);
            }
        }

    } else {
        var idx = -1;
        for (var i = 0; i < connections.length; i++) {
            if (connections[i] == conn) {
                idx = i;
                break;
            }
        }
        if (idx != -1) {
            // Dirty connection (delete)
            if (isInitialized && !workflowIsRunning) {
                dirtyBits[connections[idx].targetId] = DIRTY_DELETE_CONNECTION;
                deletedConnection = true;
                updatedComponentId = connections[idx].targetId;
                // For custom options, set the changed component to changed state
                customOptionsHandleOptionsUpdate(updatedComponentId, true);
                //for customUI
                componentOptions[updatedComponentId] = {};
            }
            connections.splice(idx, 1);
        }
    }

    // The digraph object contains the data for the connections/nodes.
    var digraphObject = new Object();
    digraphObject.name = currentDigraph.name;
    digraphObject.id = workflowId;
    digraphObject.components = [];
    digraphObject.annotations = [];
    digraphObject.isShared = currentDigraph.isShared;
    digraphObject.lastUpdated = currentDigraph.lastUpdated;
    digraphObject.isView = currentDigraph.isView;
    var updatedComponentName = null;
    var updatedComponentType = null;
    var updatedHumanReadableId = null;
    // For each diagram component, populate the currentDigraph global variable.
    if (jQuery('.process-component').length > 0) {
        jQuery('.process-component').each(function(index, value) {

            // Node position info.
            var divPosition = jQuery(value).position();
            var id = jQuery(value).attr('id');

            var componentType = jQuery(value).attr('name').toLowerCase();

            var draggableParent = jQuery('#' + id);
            var compName = jQuery('#' + id + '  .compName').text();

            var humanReadableId = jQuery('#humanReadableId_' + id).text();
            if (updatedComponentId == id) {
                updatedComponentNameDbFriendly = compName;
                updatedComponentType = componentType;
                updatedHumanReadableId = humanReadableId;
            }
            var top = parseFloat(divPosition.top);
            var left = parseFloat(divPosition.left);
            // The component info to be stored in the JavaScript variable (currentDigraph).
            var componentObject = new Object();
            componentObject.component_id = id;
            componentObject.component_id_human = humanReadableId;
            componentObject.component_type = componentType;
            componentObject.component_name = compName;
            componentObject.workflow_id = workflowId;
            componentObject.top = top;
            componentObject.left = left;
            componentObject.connections = new Object();
            componentObject.connections.connection = [];
            componentObject.options = [];
            componentObject.inputs = [];
            componentObject.outputs = [];
            componentObject.errors = [];
            // Populate options for this component.
            if (componentOptions[id] !== undefined) {
                componentObject.options = componentOptions[id];
            }

            // Populate connection info for this component.
            if (connections.length > 0) {
                for (var j = 0; j < connections.length; j++) {
                    var connection = null;
                    //alert('connection: ' + connections[j].endpoints[0].getUuid() + ", " + connections[j].endpoints[1].getUuid());
                    var sourceSplit = connections[j].endpoints[0].getUuid().split('_');
                    var sourceId = sourceSplit[0];
                    var sourceIndex = sourceSplit[1];
                    var targetSplit = connections[j].endpoints[1].getUuid().split('_');
                    var targetId = targetSplit[0];
                    var targetIndex = targetSplit[1];

                    if (sourceId == id && sourceEndpoint != null) {
                        connection = {
                            'to' : targetId,
                            'index' : targetIndex,
                            'frindex' : sourceIndex
                        };
                    }
                    if (targetId == id && targetEndpoint != null) {
                        connection = {
                            'from' : sourceId,
                            'index' : sourceIndex,
                            'tindex' : targetIndex
                        };
                        fromInfo = 'from : ' + sourceId + ', index : ' + sourceIndex;
                    }
                    // if source and target (to and from) are not the same, add connection
                    var testingConnection = connection;
                    if (connection != null) {

                        if (testingConnection != null) {
                            var connectionFound = false;
                            if (componentObject.connections.connection !== undefined
                                    && componentObject.connections.connection.length > 0) {
                                jQuery.each(componentObject.connections.connection, function(index, value) {

                                    if (value.to !== undefined && connection.to !== undefined) {
                                        if (value.frindex !== undefined
                                            && value.to == connection.to
                                                && value.index == connection.index
                                                    && value.frindex == connection.frindex) {
                                            connectionFound = true;
                                        }
                                    } else if (value.from !== undefined && connection.from !== undefined) {
                                        if (value.tindex !== undefined
                                            && value.from == connection.from
                                                && value.index == connection.index
                                                    && value.tindex == connection.tindex) {
                                            connectionFound = true;
                                        }
                                    }

                                });

                            }

                            if (!connectionFound) {
                                componentObject.connections.connection.push(connection);
                            }

                        }

                    }
                }
            }

            digraphObject.components.push(componentObject);

        }); // end of for-each process-component div

    }

    if (updatedComponentId != null && addedConnection) {
        logWorkflowComponentAction(currentDigraph.id, null,
                updatedComponentId, updatedComponentNameDbFriendly, updatedComponentType, updatedHumanReadableId,
                        null, null, null, LOG_ADD_COMPONENT_CONNECTION, fromInfo);
    } else if (updatedComponentId != null && deletedConnection) {
        logWorkflowComponentAction(currentDigraph.id, null,
                updatedComponentId, updatedComponentNameDbFriendly, updatedComponentType, updatedHumanReadableId,
                        null, null, null, LOG_DELETE_COMPONENT_CONNECTION, "");
    }

    // Now the digraphobject variable has been populated, keep it in a global variable.
    digraphObject.annotations = currentDigraph.annotations;
    currentDigraph = digraphObject;
    updateSharedSelection();

    // Only show labels for nodes that have no connections
    handleDisplayInputOutputTags();

    if (!drawingMultipleConnections) {
        finishUpdates();
    }

}; // end of updateConnections function


function finishUpdates() {

    if (isInitialized && !workflowIsRunning) {
        isWorkflowSaved = false;
        lastCall = "batch";
        saveTemporaryWorkflowTransient(currentDigraph.id, null, null, null, null);
    }

}

function connectionHandler() {  // jsPlumb instance.batch function definition.


    if (!currentDigraph.isView) {
        //bind click listener; delete connections on click
        instance.bind('click', function(conn) {
            instance.detach(conn);
        });


        // bind beforeDetach interceptor: will be fired when the click handler above calls detach, and the user
        // will be prompted to confirm deletion.
        instance.bind('beforeDetach', function(conn) {
            //return confirm('Delete connection?');

            /** Delete connection confirmation dialog. */
            if (!currentDigraph.isView && !workflowIsRunning) {
                jQuery('<div />', {
                    id : 'wfDeleteComponentConfirmationDialog'
                }).html(
                    'Are you sure you want to delete the connection?')
                .dialog({
                    open : function() {
                        jQuery('.ui-button').focus();
                    },
                    autoOpen : true,
                    autoResize : true,
                    resizable : false,
                    width : 400,
                    height : "auto",
                    title : 'Delete Connection',
                    buttons : {
                        'Yes' : function() {
                            jsPlumb.detach(conn);
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

            } else if (workflowIsRunning) {
                showCancelWarning();
            }

            return false;
        });


        // bind to connection and connectionDetached events, and update the list of connections.
        jsPlumb.bind('connection', function(info, originalEvent) {
            wfEnableSaveButton();
            updateConnections(info);
        });
        jsPlumb.bind('connectionDetached', function(info, originalEvent) {

            isWorkflowSaved = false;
            //validateCurrentWorkflow(currentDigraph.id);
            wfEnableSaveButton();
            updateConnections(info, true);
        });

        jsPlumb.bind('connectionMoved', function(info, originalEvent) {
            //  In a future release of jsplumb this extra connection event will not be fired.

            isWorkflowSaved = false;
            //validateCurrentWorkflow(currentDigraph.id);
            wfEnableSaveButton();
            updateConnections(info, true);
        });
    }

} // end of jsPlumb instance.batch definition.

function drawConnections(components) {


    jQuery.each(components, function() {

        var thisComponent = this;

        // Get the connection. If only one exists, ensure JavaScript treats it as an array.
        if (thisComponent.connections != null && thisComponent.connections.connection != null) {
            if (!jQuery.isArray(thisComponent.connections.connection)) {
                thisComponent.connections.connection = [ thisComponent.connections.connection ];
            }

            // Draw each connection.
            var thisConn = thisComponent.connections.connection;
            if (thisConn.length > 0) {
                jQuery.each(thisConn, function(index, value) {

                    if (value.to !== undefined) {
                        var leftAnchorPoint = null;
                        var rightAnchorPoint = null;

                        // add GUI connection
                        if (value.index !== undefined) {
                            if (value.index == "0") {
                                rightAnchorPoint = rightAnchor0;
                            } else if (value.index == "1") {
                                rightAnchorPoint = rightAnchor1;
                            } else if (value.index == "2") {
                                rightAnchorPoint = rightAnchor2;
                            } else if (value.index == "3") {
                                rightAnchorPoint = rightAnchor3;
                            }
                        }

                        if (value.frindex !== undefined) {
                            if (value.frindex == 0) {
                                leftAnchorPoint = leftAnchor0;
                            } else if (value.frindex == 1) {
                                leftAnchorPoint = leftAnchor1;
                            } else if (value.frindex == 2) {
                                leftAnchorPoint = leftAnchor2;
                            } else if (value.frindex == 3) {
                                leftAnchorPoint = leftAnchor3;
                            }
                        } else {
                            // Support older workflows without frindex/tindex
                            if (value.index == "0") {
                                currentAnchors = [ rightAnchor0, leftAnchor0 ];
                            } else if (value.index == "1") {
                                currentAnchors = [ rightAnchor1, leftAnchor0 ];
                            }
                        }

                        if (leftAnchorPoint != null && rightAnchorPoint != null) {
                            currentAnchors = [ rightAnchorPoint, leftAnchorPoint ];
                        }

                        var endpointsKey = thisComponent.component_type.toLowerCase()
                        + "-" + thisComponent.component_name.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase();

                        if (componentOutputEndpoints[endpointsKey] > 0) {
                            for (var outIndex = 0 ; outIndex < componentOutputEndpoints[endpointsKey]; outIndex++) {



                                // new ones have no connections.connection.*
                                jQuery.each(components, function() {

                                    var toComponent = this;
                                    if (toComponent.component_id == value.to && toComponent.connections != null && toComponent.connections.connection != null) {
                                        if (!jQuery.isArray(toComponent.connections.connection)) {
                                            toComponent.connections.connection = [ toComponent.connections.connection ];
                                        }

                                        var toConn = toComponent.connections.connection;
                                        if (toConn.length > 0 && value.to == toComponent.component_id) {
                                            jQuery.each(toConn, function(fromIndex, fromValue) {
                                                var srcIndex = null;
                                                if (value.frindex !== undefined) {
                                                    srcIndex = fromValue.index;
                                                } else {
                                                    srcIndex = outIndex;
                                                }

                                                if (fromValue.from !== undefined && fromValue.from == thisComponent.component_id
                                                        && ((value.frindex !== undefined && fromValue.index == value.frindex
                                                                && fromValue.index == outIndex && fromValue.tindex == value.index) // stmt part for new schema
                                                        || (value.frindex === undefined && fromValue.index == outIndex))) {    // stmt part for old schema

                                                    var connExists = false;
                                                    var connected = jsPlumb.getConnections();
                                                    // If we're pasting components, then we don't want to redraw existing connections.
                                                    jQuery.each(connected, function (e, s) {
                                                        var uuids = s.getUuids();
                                                        if (uuids[0] == thisComponent.component_id + "_" + srcIndex + "_source"
                                                                && uuids[1] == value.to + "_" + value.index + "_target") {
                                                            connExists = true;
                                                        }
                                                    });
                                                    // Draw if the connection doesn't already exist.
                                                    if (!connExists) {
                                                        jsPlumb.connect({   // the to index of the from node should match it
                                                            uuids: [ thisComponent.component_id + "_" + srcIndex + "_source", value.to + "_" + value.index + "_target" ],
                                                            //source : thisComponent.component_id, // + "_0_source",
                                                            //target : value.to, // + "_" + value.index + "_target",
                                                            detachable : true,
                                                            deleteEndpointsOnDetach : false,
                                                            anchors : currentAnchors,
                                                            endpoint : [ 'Dot', {
                                                                strokeStyle : 'transparent',
                                                                fillStyle : 'transparent',
                                                                radius : 1,
                                                                lineWidth : 0
                                                            } ],
                                                            overlays : [ [ 'Arrow', {
                                                                width : 15,
                                                                length : 30,
                                                                location : 1,
                                                                id : 'arrow'
                                                            } ], [ 'Label', {
                                                                label : '',
                                                                id : 'label'
                                                            } ] ],
                                                            paintStyle : {
                                                                lineWidth : 7,
                                                                strokeStyle : '#ffd58b',
                                                                joinstyle : 'round',
                                                                outlineColor : 'white',
                                                                outlineWidth : 2,
                                                            },
                                                            maxConnections : -1,
                                                            hoverPaintStyle : {
                                                                lineWidth : 7,
                                                                strokeStyle : '#fda429',
                                                                outlineWidth : 2,
                                                                outlineColor : 'white'
                                                            },
                                                            connector : [ 'Flowchart', {
                                                                stub : [ 10, 10 ],
                                                                gap : 15,
                                                                cornerRadius : 15,
                                                                alwaysRespectStubs : true,
                                                                fillStyle : 'transparent'
                                                            } ]
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });



                            }
                        }

                    }

                });
            }
        }

    });

    finishUpdates();
}

/**
 * Delete selected components.
 */
function deleteSelectedComponents() {
    jQuery(selectedComponents).each(function(selInd, selVal) {
        var component = '#' + selVal;
        if (!workflowIsRunning) {
            var componentDiv = jQuery(component);
            var componentId = componentDiv.attr('id');
            var componentType = componentDiv.attr('name').toLowerCase();

            instance.removeAllEndpoints(componentDiv.attr('id'));

            // Delete component
            jsPlumb.remove(componentDiv);

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

            }

            lastCall = "deleteComponent";
            saveTemporaryWorkflowTransient(currentDigraph.id, null, null, null, null);

            if (currentDigraph.hasOwnProperty('components') && currentDigraph.components !== undefined
                    && currentDigraph.components.length == 0) {
                jQuery('#workspaceHint').show();
            }
        } else {
            var dialogHtml = '' + "Cannot delete component(s). Workflow is running." + ' <br/>';
            wfInfoDialog('deleteComponentsDialog', dialogHtml, 'Error');
        }
    });

}

function copyConnections(sourceId, targetId) {
    if (sourceId != targetId) {
      jQuery(currentDigraph.components).each(function(srcInd, srcObj) {
          if (srcObj !== undefined) {
              var cons = srcObj.connections;
              // source component
              if (srcObj.component_id == sourceId) {
                  // there never is targetObj.component_id because it hasn't been created yet--
                  // might need to wait til after it's dropped to call copy connections?
                  jQuery(currentDigraph.components).each(function(targetInd, targetObj) {
                      // new pasted component
                      if (targetObj !== undefined && targetObj.component_id == targetId) {
                          // Deep clone the javascript object
                          if (jQuery.isArray(srcObj.options)) {
                              targetObj.options = jQuery.extend(true, {}, srcObj.options[0]);
                          } else {
                              targetObj.options = jQuery.extend(true, {}, srcObj.options);
                          }
                          componentOptions[targetId] = targetObj.options;
                          targetObj.top = parseFloat(srcObj.top) + 10;
                          targetObj.left = parseFloat(srcObj.left) + 10;
                          targetObj.connections = new Object();
                          targetObj.connections.connection = [];

                          jQuery(cons).each(function(connArrId, connArr) {

                              jQuery(connArr).each(function(connId, connObj) {
                                  // Get connection
                                  var cbArr = connObj.connection;

                                  jQuery(cbArr).each(function(cbId, cb) {
                                      // GUI Connection objects

                                      var endpointsKey = targetObj.component_type.toLowerCase()
                                                          + "-" + targetObj.component_name.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase();


                                      if (componentInputEndpoints[endpointsKey] !== undefined && componentInputEndpoints[endpointsKey] > 0) {

                                      }
                                      if (componentOutputEndpoints[endpointsKey] !== undefined && componentOutputEndpoints[endpointsKey] > 0) {

                                      }

                                      // JSON Connection info
                                      if (cb.to !== undefined) {
                                          var newTo = cMapping[cb.to];
                                          var newConnection = {
                                              'to' : newTo,
                                              'index' : cb.index,
                                              'frindex' : cb.frindex
                                          };
                                          targetObj.connections.connection.push(newConnection);
                                      }
                                      if (cb.from !== undefined) {
                                          var newFrom = cMapping[cb.from];
                                          var newConnection = {
                                              'from' : newFrom,
                                              'index' : cb.index,
                                              'tindex' : cb.tindex
                                          };
                                          targetObj.connections.connection.push(newConnection);
                                      }

                                  });


                              });
                          });
                      }
                  });
              }
          }
      });
    }
}

function selectAllComponents() {
    selectedComponents = [];
    jQuery('.process-component').each(function(pComp) {
        jQuery(this).css('border', '2px solid #F9BE41'); // purple is #8D46FF
        jQuery(this).css('width', (componentWidth - 4) + 'px');
        jQuery(this).addClass('selectedComponent');
        selectedComponents.push(jQuery(this).attr('id'));
        instance.addToPosse([jQuery(this)], "selectedPosse");
    });

    // Get all component coordinates for multi-component move
    jQuery('.process-component').each(function(selInd, selObj) {
        var selVal = jQuery(selObj).attr('id');
        var otherCompPos = jQuery('#' + selVal).position();
        originalPos[selVal] = {};
        originalPos[selVal].left = otherCompPos.left / percentZoom;
        originalPos[selVal].top = otherCompPos.top / percentZoom;
    });
    jQuery('.workflowAnnotation').each(function(selInd, selObj) {
        var selVal = jQuery(selObj).attr('id');
        var otherCompPos = jQuery('#' + selVal).position();
        originalPos[selVal] = {};
        originalPos[selVal].left = otherCompPos.left / percentZoom;
        originalPos[selVal].top = otherCompPos.top / percentZoom;
    });

    initSelectAreaContextMenu(true);

    highlightComponentConnections();
}


function highlightComponentConnections() {
    // Reset component connection styles.
    jQuery('.process-component').each(function(selInd1, selVal1) {
        jQuery('.process-component').each(function(selInd2, selVal2) {
            if (selVal1 != selVal2) {
                var existingConnections = jsPlumb.getConnections({
                    source: selVal1 ,
                    target: selVal2
                });
                var c1 = existingConnections;

                for (var i = 0; i < existingConnections.length; i++) {
                    var conn1 = existingConnections[i];
                    var ps = jQuery.extend(true, {}, conn1.getPaintStyle());
                    ps.strokeStyle = '#ffd58b';
                    conn1.setPaintStyle(ps);
                    conn1.repaint();
                }
            }
        });
    });

   // Update selected component connection styles.
    jQuery(selectedComponents).each(function(selInd1, selVal1) {
        jQuery(selectedComponents).each(function(selInd2, selVal2) {
            if (selVal1 != selVal2) {
                var existingConnections = jsPlumb.getConnections({
                    source: selVal1 ,
                    target: selVal2
                });
                var c1 = existingConnections;

                for (var i = 0; i < existingConnections.length; i++) {
                    var conn1 = existingConnections[i];
                    var ps = jQuery.extend(true, {}, conn1.getPaintStyle());
                    ps.strokeStyle = '#60507A';
                    conn1.setPaintStyle(ps);
                    conn1.repaint();
                }
            }
        });
    });

}


