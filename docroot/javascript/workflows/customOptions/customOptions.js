/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 *
 * Functions and variables for the custom option interfaces for components
 *
 * Peter Schaldenbrad pschalde@cs.cmu.edu
*/

var htmlFileDir = "javascript/workflows/customOptions/";

/*
    List of custom interfaces and the names of their HTML files.
    The keys are the names that go in the component's XSD and the values are the names
    of the HTML files in workflows/customOptions.
*/
var customInterfaceMap = {
    "TetradGraphEditor": "tetradGraphVisualizationEditor.html",
    "TetradKnowledge": "tetradKnowledge.html",
    "DetectorTester": "detectorTester.html",
    "Import": "importComponent.html",
    "Anonymize": "hash.html",
    "DiscourseDbSelector": "DiscourseDbSelector.html"
};

var componentInputRequesting = false;  //true if the function is currently requesting input data
var componentMetadataRequesting = false; //true if function is currently requesting metadata
var componentInput = null; //stores the input data upon request
var componentMetadata = null; //stores the metadata upon request

var setOptionsPaneWidth = {}; //So custom Opt Interfaces can set the size of pane

var upstreamChanged = []; //True iff a component has changed upstream from the component since opening the options last

var componentSpecificOptionsJson = null; //The Json returned from requestComponentSpecificOptions

/*
      Determine whether the option type is a custom option
*/
function needsCustomOptionsInterface(optionsType) {
      if (optionsType in customInterfaceMap) {
           return true;
      }
      return false;
}

/*
    Request to get the data coming into the component.
    @param nodeIndex node index to find input
    @param fileIndex file index at input node
    @param numLinesToGet the number of lines from input file to get (-1 is entire file);
    return is stored in var componentInput.  componentInputRequesting is true while the
        function is waiting for the data and false once it receives it.
*/
function getComponentInput(nodeIndex, fileIndex, numLinesToGet) {
    componentInputRequesting = true;

    var parentDiv = jQuery(this).parent();

    var componentIdPreSplit = jQuery('.advOptDialogClass').attr("id");
      var componentId = componentIdPreSplit.split("_")[1];

    var componentType = jQuery(parentDiv).attr("name");

    var componentTypeId = 'componentInterface_' + componentId;

    var ret = undefined;

    if (componentId !== undefined && componentId != null && componentId != '') {
        // Send an AJAX request to save the currentDigraph to the database.
        new Ajax.Request('WorkflowEditor', {
            parameters : {
                requestingMethod : 'WorkflowEditorServlet.getComponentInput',
                workflowId : currentDigraph.id,
                digraphObject : stringifyDigraph(currentDigraph),
                dirtyBits: JSON.stringify(dirtyBits),
                componentId : componentId,
                componentType : componentType,
                nodeIndex : nodeIndex,
                fileIndex : fileIndex,
                numLinesToGet : numLinesToGet
            },
            //beforeSend : showProcessingByType(componentType),
            //onSuccess : function(response) {
            //    ret = getComponentInputCompleted(response);
            //},
            onComplete : function(response) {
                componentInput =  getComponentInputCompleted(response);
                componentInputRequesting = false;
            },
            onException : function(request, exception) {
                //throw(exception);
            }
        });
    }
}

function getComponentInputCompleted(transport) {
    if (transport !== undefined) {
        json = transport.responseText.evalJSON(true);

        if (json.workflowId !== undefined
            && json.componentId !== undefined
                && json.componentInput !== undefined) {

            var componentType = json.componentType;
            var componentName = json.componentName;
            var componentId = json.componentId;

            var componentInputStr = json.componentInput;

            //console.log("returned component input:\n" + componentInputStr);
            return componentInputStr;
        }
    }
    return null;
}

/*
    Request to get the meta data coming into the component.
    @param nodeIndex node index to find input
    @param fileIndex file index at input node
    return is stored in var componentMetadata.  componentMetadataRequesting is true while the
        function is waiting for the data and false once it receives it.
*/
function getComponentMetaDataInput(nodeIndex, fileIndex) {
    componentMetadataRequesting = true;

    var parentDiv = jQuery(this).parent();

    var componentIdPreSplit = jQuery('.advOptDialogClass').attr("id");
    var componentId = componentIdPreSplit.split("_")[1];

    var componentType = jQuery(parentDiv).attr("name");

    var componentTypeId = 'componentInterface_' + componentId;

    var ret = undefined;

    if (componentId !== undefined && componentId != null && componentId != '') {
        // Send an AJAX request to save the currentDigraph to the database.
        new Ajax.Request('WorkflowEditor', {
            parameters : {
                requestingMethod : 'WorkflowEditorServlet.getComponentMetadata',
                workflowId : currentDigraph.id,
                digraphObject : stringifyDigraph(currentDigraph),
                dirtyBits: JSON.stringify(dirtyBits),
                componentId : componentId,
                componentType : componentType,
                nodeIndex : nodeIndex,
                fileIndex : fileIndex,
            },
            //beforeSend : showProcessingByType(componentType),
            //onSuccess : function(response) {
            //  ret = getComponentInputCompleted(response);
            //},
            onComplete : function(response) {
                componentMetadataRequesting = false;
                componentMetadata =  getComponentMetadataCompleted(response);
            },
            onException : function(request, exception) {

            }
        });
    }
}

function getComponentMetadataCompleted(transport) {
    if (transport !== undefined) {
        json = transport.responseText.evalJSON(true);

        if (json.workflowId !== undefined
            && json.componentId !== undefined
                && json.componentMetaData !== undefined) {

            var componentType = json.componentType;
            var componentName = json.componentName;
            var componentId = json.componentId;

            var componentMetadataStr = json.componentMetaData;
            //console.log(componentMetadataStr);

            var metaDataJson = null;
            try {
                metaDataJson = JSON.parse(componentMetadataStr);
                return metaDataJson;
            } catch (error) {
                return null;
            }
            return null;
        }
    }
    return null;
}

function getHtmlForCustomOptInterface(jsonValue, json) {
    var htmlFileLocation = '';
    var type = jsonValue.type;

    componentSpecificOptionsJson = json;

    setOptionsPaneWidth[json.componentId] = undefined;

      if (jsonValue.type === "TetradGraphEditor") {
        setOptionsPaneWidth[json.componentId] = "80%";
      } else if (jsonValue.type === "TetradKnowledge") {
    setOptionsPaneWidth[json.componentId] = "60%";
    } else if (jsonValue.type === "DetectorTester") {
        setOptionsPaneWidth[json.componentId] = "45%";
    } else if (jsonValue.type === "Import") {
        setOptionsPaneWidth[json.componentId] = "70%"
    } else if (jsonValue.type === "Anonymize") {
        setOptionsPaneWidth[json.componentId] = "40%"
    }
      return '<iframe style="width:99%;height:99%" src="' +
                htmlFileDir + customInterfaceMap[type] +
                '"></iframe>';
}

/**
 * Gets the data saved from saveData(data).  Returns null if no data is saved.
 */
function loadData(optionsType) {
    var componentIdPreSplit = jQuery('.advOptDialogClass').attr("id");
      var componentId = componentIdPreSplit.split("_")[1];

      var data = componentOptions[componentId][optionsType];
      if (data !== undefined && data !== null) {
          return data.replace(/%NEW_LINE%/g, "\n");
      }
      return null;
}

function saveData(optionsType, data) {
    //Leave a marker for newlines so they don't get lost when saving
  if (typeof(data) === "string") {
      data = data.replace(/\n/g, "%NEW_LINE%");
  }

    jQuery(".wfAdvOpt").val(data);

    var componentIdPreSplit = jQuery('.advOptDialogClass').attr("id");
    var componentId = componentIdPreSplit.split("_")[1];

    componentOptions[componentId][optionsType] = data;

    dirtyOptionPane = true;
    isWorkflowSaved = false;
    dirtyBits[componentId] = DIRTY_OPTION;
    upstreamChange[componentId] = false;

    saveOpenOptions();
    wfEnableSaveButton();
}

/**
 * Delete the data saved using saveData().
 */
function deleteSavedData(componentId, optionsType) {
    if (componentOptions[componentId] != undefined && componentOptions[componentId] != null ) {
        if (componentOptions[componentId][optionsType] != undefined && componentOptions[componentId][optionsType] != null) {
            componentOptions[componentId][optionsType] = undefined;
        }
    }
}

/**
 * Returns true if there was a change to the workflow upstream from the specified component
 * Within a custom options js file, the component id can be found doing:
 * var componentIdPreSplit = jQuery('.advOptDialogClass', parent.document).attr("id");
 * var componentId = componentIdPreSplit.split("_")[1];
 */
function upstreamChange(componentId) {
    if (upstreamChange[componentId] !== true) {
        return false;
    }
    return true;
}

/**
 * Handle the new status updates after an edit to the workflow.  If something
 * changes upstream from a compomponent with custom options, mark the component
 * as being changed using the array upstreamChange
 */
function customOptionsHandleOptionsUpdate(componentId, downstream) {
    // If the component is a custom options component and it's downstream from a component
    // that has options that have changed, mark it as being changed
    if (downstream) {
        componentOptionsType = Object.keys(componentOptions[componentId]);
        //console.log(componentOptionsType);
        componentOptionsType.forEach(function(type) {
            if (type in customInterfaceMap) {
                //console.log('deleteing');
                //deleteSavedData(componentId, type);

                upstreamChange[componentId] = true;

                if (type == 'TetradGraphEditor') {
                    deleteSavedData(componentId, 'TetradGraphEditor');
                }
            }
        });
    }

    // Travel through downstream components recursively to see if they are custom options
    // If so delete their data
    var digraphCopy = jQuery.extend(true, {}, currentDigraph);
    var components = digraphCopy.components;

    components.forEach(function(component) {
        if (component.component_id == componentId) {
            var connections = component.connections;

            if (connections.length == undefined) {
                connections = [connections];
            }
            if (connections != null) {
                connections.forEach(function(connection) {
                    connection = connection.connection;

                    if (connection != undefined && connection != null) {
                        if (connection.length == undefined) {
                            connection = [connection];
                        }

                        connection.forEach(function(direction){
                            var to = direction.to;
                            if (to != null && to != undefined) {
                                customOptionsHandleOptionsUpdate(to, true);
                            }
                        });
                    }
                });
            }
        }
    });
}


/**
 * Get the id of the component whose options are open
 */
function getComponentId() {
    let idWithStuff = jQuery('.fileInputForm').attr('id');
    return idWithStuff.replace(/importForm_/g, "");
}
