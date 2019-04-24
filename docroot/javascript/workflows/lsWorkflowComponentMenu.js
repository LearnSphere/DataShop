/* I modified the jstree search method to allow for shallow or deep searches. */
var deepSearch = true;
/* Component tree Import menu is open by default. */
var componentTreeOpen = 0;
/* Label when mouseover Components in the LHS component menu (tree). */
var componentLabelText = 'Drag components onto the workspace';
/* Label when mouseover Folders in the LHS component menu (tree). */
var folderLabelText = 'Expand / Collapse';
/* No Drag Allowed for Folders in the LHS component menu (tree). */
var noDraggingClass = 'no_dragging';
/* Used to build the LHS component menu (tree). */
var lhsComponentTree = "";
/* Recently used components for this user. */
var recentComponents = "{ }";
/* Image icons for the LHS component menu (tree). */
var iconImages = { 'Import' : 'css/images/import.svg',
        'Data' : 'css/images/import.svg',
        'Database' : 'css/images/database.svg',
        'Transform' : 'css/images/transform.svg',
        'Analysis' : 'css/images/search.svg',
        'Visualization' : 'css/images/vis.svg',
        'Export' : 'css/images/export.svg',
        'Tetrad' : 'css/images/tetrad.svg',
        'Contribute' : 'css/images/contribute.svg' };
var componentIconImage = "css/images/component_icon.svg";

var actualCountTypeCount = 0;
var componentInfoDivObject = null;

// The compCount is used in the recursive string builder
// and determines which folders have children.
// If no children, then they are put into hiddenFolders.
var compCount = 0;
var hiddenFolders = new Array();
hiddenFolders.push('#cId_Import');

/* Let certain components be "pinned" meaning they are always present in LH menu regardless of the search */
var pinnedComponents = ["annotation"];

function addComponentTreeLhsMenu(component_menu, componentInfoDivs, isView) {
    // sort menu based on array indices

    jQuery("#component-lhs-search-input").val('');
    jQuery("#component-lhs-search-input").hint("auto-hint");

    componentInfoDivObject = componentInfoDivs;
    componentTypeHierarchy = '<ul>' + traverseComponentHierarchy(component_menu[0].component_menu, 0, null, null) + '</ul>';
    jQuery('#component-tree-div').append(jQuery(componentTypeHierarchy));
    jQuery(hiddenFolders).each(function(fIndex, fId) {
        jQuery(fId).remove();
    });
    jQuery('#component-tree-div').jstree({
        "types" : {
            "folder" : {
                "icon" : "icon-folder-open"
            },
            "file" : {
                "icon" : "icon-file"
            }
        },
        "core" : {
           // so that create works
            "check_callback" : function(callback, node, node_parent, node_position, more) {
                if (callback == 'move_node') {
                    if (node_parent.type == 'process-div') {
                        return true;
                    } else {
                        return false;
                    }
                } else if (callback == 'create_node') {
                    return true;
                }
            },
            "force_text" : false, /* The '.compName' span is required for maintaining a reference to the component key.
                 The attribute, force_text, is false by default, but it's worth being explicit, here. */
            'multiple': false
        },
        "search": {
            "case_insensitive": true,
            "show_only_matches" : true
        },

        "dnd": {
            check_while_dragging: true,
            is_draggable : function (nodes) {
                var i = 0, j = nodes.length;
                for(; i < j; i++) {
                   if(this.get_node(nodes[i], true).hasClass('no_dragging')) {
                       return false;
                   }
                }
                return true;
            }
        },

        "plugins" : [ "dnd","types","crrm", "search" ]

      });


        jQuery('#component-tree-div').jstree("set_theme", "default");

      // After opening folder
      // "#j1_3" is the folder of the old import components
      jQuery("#component-tree-div").jstree("hide_node", jQuery("#j1_3"));
      jQuery("#component-tree-div").on("after_open.jstree", function (e, data) {
          initializeTooltips('.wfComponentTooltip');

          /*// Update resize handle:
              jQuery('#process-selector-div .ui-resizable-e').css('height',
              jQuery('#process-selector-div').prop('scrollHeight')); */
          jQuery('#component-tree-div').css('height', "calc(80vh - 200px)");
      });

      // After closing folder
      jQuery("#component-tree-div").on("after_close.jstree", function (e, data) {
          /*// Update resize handle:
          jQuery('#process-selector-div').resizable('destroy');
          jQuery('#process-selector-div').resizable({handles: 'e'});

          jQuery('#process-selector-div .ui-resizable-e').css('height',
              jQuery('#process-selector-div').prop('scrollHeight'));
          */
          jQuery('#component-tree-div').css('height', "calc(80vh - 200px)");
      });

      jQuery("#component-lhs-search-input").keyup(function() {
          var searchString = jQuery(this).val();
          jQuery('#component-tree-div').jstree('search', searchString);
          initializeTooltips('.wfComponentTooltip');
          if (searchString.trim() == '') {

          } else {
              jQuery('.hideOnSearch').css('display', 'none');
          }

          // If there are no results for this search, tell the user.
          jQuery('.noComponentsMatchSearch').remove();
          if (searchString.trim().length != 0 && jQuery('.jstree-hidden').length == 1) {
              // There is a non null search string and all the components are visible (except for the old Import directory)
              // This most likely means that there was no match (though it's possible there is a weird case)
              if(!jQuery('#component-lhs-search-input').hasClass('auto-hint')) {
                  // Ensure that we are not searching the auto-hint ("Filter by name or description")
                  jQuery('#component-tree-div').prepend('<div class="noComponentsMatchSearch">No components match your search</div>');
              }
          }

          jQuery("#component-tree-div").jstree("hide_node", jQuery("#j1_3"));

          // Let certain components be "pinned" meaning they are always present regardless of the search
          pinnedComponents.forEach(function(pinnedCompName) {
              jQuery('.draggableComponent[name="' + pinnedCompName + '"]').removeClass('jstree-hidden');
          });
      });


      jQuery('#component-lhs-search-clear-button').click(function() {
          jQuery("#component-lhs-search-input").val('');
          jQuery("#component-lhs-search-input").hint("auto-hint");
          jQuery('#component-lhs-search-input').keyup();
          jQuery('.hideOnSearch').css('display', '');
          jQuery("#component-tree-div").jstree("hide_node", jQuery("#j1_3"));
      });

      jQuery('#component-lhs-deep-search-checkbox').change(function() {

          if (jQuery('#component-lhs-deep-search-checkbox').prop('checked') == true) {
              deepSearch = false;
              jQuery('#component-lhs-search-input').keyup();
          } else {
              deepSearch = true;
              jQuery('#component-lhs-search-input').keyup();
          }
      });

      if (jQuery('#component-lhs-deep-search-checkbox').prop('checked') == true) {
          deepSearch = false;
      } else {
          deepSearch = true;
      }

      jQuery('#component-lhs-search-input').keyup();

      jQuery('button').on('click', function () {
        jQuery('#component-tree-div').jstree(true).select_node('child_node_1');
        jQuery('#component-tree-div').jstree('select_node', 'child_node_1');
        jQuery.jstree.reference('#component-tree-div').select_node('child_node_1');
      });

      if (!isView) {
          jQuery('.draggableComponent')
              .on('mousedown', function (e) {
                  let thisId = jQuery(this).attr('id');
                  let dbCompName = jQuery('#' + thisId + ' .compName').text();
                  let humanReadableCompName = componentTitleMap[
                          dbCompName.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase()];
                  // Handle case for Annotations.  They are not defined in componentTitleMap
                  if (humanReadableCompName == undefined && dbCompName == 'Annotation') {
                      humanReadableCompName = "Annotation";
                  }

                  return jQuery.vakata.dnd.start(e, { 'jstree' : false,
                      'obj' : jQuery(this),
                      'nodes' : [{ id : true, text: jQuery(this).text() }] },
                      '<div id="jstree-dnd" class="jstree-default"><i class="jstree-icon jstree-er"></i>'
                      + humanReadableCompName + '</div>');
          });


          jQuery(document)
              .on('dnd_move.vakata.jstree', function (e, data) {
                  data.helper.find('.jstree-icon').parent().removeClass('jstree-drag-component').addClass('jstree-drag-component');
                  ///data.helper.find('.jstree-drag-component').css( {top: -10, left: -70, position:'relative'} );
                  var t = jQuery(data.event.target);

                  if(!t.closest('.jstree').length) {
                      if (t.closest('#process-div').length) {
                          data.helper.find('.jstree-icon').removeClass('jstree-er').addClass('jstree-ok');
                      } else {
                          data.helper.find('.jstree-icon').removeClass('jstree-ok').addClass('jstree-er');
                      }
                  } else {
                      data.helper.find('.jstree-icon').removeClass('jstree-ok').addClass('jstree-er');
                  }

                  data.helper.find('.jstree-icon').parent()
                      .css('position', 'absolute')
                      .css('left', '-70px');

          });

          jQuery(document)
              .on('dnd_stop.vakata', function (e, data) {
                  var t = jQuery(data.event.target);
                  if (!t.closest('.jstree').length) {
                      if (t.closest('#process-div').length) {
                          handleDropEvent(data.event, jQuery(data.element).clone(), false, null);
                      }
                  }
          });
      } else { // end of !isView
          jQuery(document)
          .on('dnd_stop.vakata', function (e, data) {
              wfTimerDialog("You can only drag components onto a workflow that you own<br/> "
                  + "Try \"Save As\" to make a private copy of this workflow.", 2500);
      });
      }

      jQuery('#component-tree-div').on('select_node.jstree', function (e, data) {
          data.instance.toggle_node(data.node);
      });

      /*jQuery("#component-tree-div").jstree("open_node", jQuery("#j1_1"));*/
      jQuery("#component-tree-div").on("open_node.jstree", function (e, data) {
          // alert(data.node.id);
          componentTreeOpen++;
      });
      jQuery("#component-tree-div").on("close_node.jstree", function (e, data) {
          // alert(data.node.id);
          componentTreeOpen--;
      });


      jQuery("#component-tree-div").bind('ready.jstree', function(event, data) {
          var jQuerytree = jQuery(this);
          jQuery(jQuerytree.jstree().get_json(jQuerytree, {
              flat: true
            }))
            .each(function(index, value) {
                var node = jQuery("#component-tree-div").jstree().get_node(this.id);
                var lvl = node.parents.length;
                var idx = index;
                if (lvl == 1) {
                    actualCountTypeCount++;
                }
            });
        });

      jQuery('#expandCloseComponentTree').click(function(event) {
          if (componentTreeOpen == 0) {
              jQuery("#component-tree-div").jstree('open_all');
              componentTreeOpen = actualCountTypeCount;
          } else {
              jQuery("#component-tree-div").jstree('close_all');
              componentTreeOpen = 0;
          }
      });
      /*// Update resize handle:
      jQuery('#process-selector-div').resizable({handles: 'e'}); */


} // end of function

// Build the ordered menu from the json object.
// The specialized object uses indexing and arrays to maintain cardinality.
function traverseComponentHierarchy(jsonObj, level, parent, root, idModifier) {
    if( typeof jsonObj == "object" ) {

        Object.entries(jsonObj).forEach(([compId, componentName]) => {
            var compObject = jsonObj[compId];
            var keys = Object.keys(componentName);

            if (typeof compObject != "object" && parent != null && componentName == "Recently_Used") {
                var dbFriendlyComponentType = root.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase();
                // Add recently used menu items for the component type root (e.g., Import/Analysis/etc),
                // if any exist.
                if (recentComponents[dbFriendlyComponentType] !== undefined
                        && recentComponents[dbFriendlyComponentType] != null
                            && recentComponents[dbFriendlyComponentType].length > 0) {
                    lhsComponentTree = lhsComponentTree + '<li class="' + noDraggingClass + ' hideOnSearch">'
                        // use default icon for recently used (a blue folder in the current theme)
                        // + ' data-jstree=\'{"icon":"' + componentIconImage + '" }\'>' // use this for a custom icon
                            + componentName.replace(/_/g, " ") + '<ul>' + '';

                    traverseComponentHierarchy(recentComponents[dbFriendlyComponentType],
                        level + 1, parent, root, "recent_");

                    lhsComponentTree = lhsComponentTree + '</li></ul>';
                }

            } else if ((typeof compObject != "object" && parent != null)
                    || (typeof compObject != "object" && compObject == "Import")) {
                var tempRoot = root;
                if (compObject == "Import") {
                    tempRoot = "Data";
                    idPrefix = "";
                }
                // Child node
                var infoToolDiv = '';
                var dbFriendlyCompName = componentName.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase();
                var idPrefix = "";
                if (idModifier !== null && idModifier !== undefined) {
                    idPrefix = idModifier;
                }

                if (componentInfoDivObject != null && componentInfoDivObject[dbFriendlyCompName] != null) {
                    infoToolDiv = '<span style="display: none"'
                        + ' id="' + idPrefix + tempRoot.toLowerCase() + "_" + componentName.toLowerCase() + '_tooltip_content" class="wfComponentTooltip" >'
                            + componentInfoDivObject[dbFriendlyCompName]  + '</span>';
                }

                lhsComponentTree = lhsComponentTree + '<li '
                    + ' data-jstree=\'{"icon":"' + componentIconImage + '" }\' id="'
                        + idPrefix + tempRoot.toLowerCase() + '_' + componentName.toLowerCase() + '-draggable" name="' + tempRoot.toLowerCase()
                           + '" class="noSelect ui-draggable draggableComponent">' + componentTitleMap[dbFriendlyCompName] + ''
                               + '<span class="compName">' + componentTitleMap[dbFriendlyCompName] + '</span>' + infoToolDiv + '</li>';
                compCount++;
            } else { // is of type object
                var rootComponentType = '';
                var iconImage = iconImages[keys[0]];
                lhsComponentTree = lhsComponentTree + '<li id="cId_' + keys[0] + '" class="' + noDraggingClass + '"'
                    + ' data-jstree=\'{"icon":"' + iconImage + '" }\'>' + keys[0].replace(/_/g, " ") + '<ul>' + '';

                compCount = 0;
                traverseComponentHierarchy(
                        componentName[keys[0]], level + 1, keys[0], keys[0], null);

                lhsComponentTree = lhsComponentTree + '</li></ul>';

                if (compCount == 0) {
                    hiddenFolders.push('#cId_' + keys[0]);
                }

            }
        });
    }

    if (level == 0) {
        // Add on the annotation draggable
        let infoToolDiv1 = '<span style="display: none" id="annotation_tooltip_content" class="wfComponentTooltip"></span>';
        lhsComponentTree = '<li '
                + ' data-jstree=\'{"icon":"' + 'css/images/annotation_icon.svg' + '" }\' id="annotation-adder-draggable"'
                + ' name="annotation'
                + '" class="noSelect ui-draggable draggableComponent">' + 'Annotation' + ''
                + '<span class="compName">' + 'Annotation' + '</span>' + infoToolDiv1 + '</li>'
                + lhsComponentTree;
        return lhsComponentTree;
    }
}

/* Build the map of system-friendly component names to human-readable component names. */
function buildComponentTitleMap(jsonObj, parent) {
    if( typeof jsonObj == "object" ) {

        Object.entries(jsonObj).forEach(([compId, componentName]) => {
            var compObject = jsonObj[compId];
            if (typeof compObject != "object" && parent != null) {
                componentTitleMap[componentName.replace(/[^a-zA-Z0-9]+/gi, "_").toLowerCase()] =
                    componentName.replace(/_/g, " ");
            } else {
                var keys = Object.keys(componentName);
                buildComponentTitleMap(componentName[keys[0]], keys[0]);
            }
        });
    }
}
