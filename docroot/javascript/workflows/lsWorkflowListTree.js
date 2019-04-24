/* I modified the jstree search method to allow for shallow or deep searches. */
var deepSearch = true;
/* Component tree Import menu is open by default. */
var workflowTreeOpen = 0;

/* No Drag Allowed for Folders in the LHS component menu (tree). */
var emptyClass = '';
/* Used to build the LHS component menu (tree). */
var workflowListTree = "";
/* Recently used components for this user. */
var recentWorkflows = "{ }";

var folderCount = 0;
var workflowInfoDivObject = null;

/* Let certain components be "pinned" meaning they are always present in LH menu regardless of the search */
var pinnedWorkflows = ["annotation"];

function addWorkflowListTree(wfListDiv, workflowInfoDivs) {
    // sort menu based on array indices

    //jQuery("#component-lhs-search-input").val('');
    //jQuery("#component-lhs-search-input").hint("auto-hint");

    workflowInfoDivObject = workflowInfoDivs;
    //componentTypeHierarchy = '<ul>' + traverseComponentHierarchy(component_menu[0].component_menu, 0, null, null) + '</ul>';
    //jQuery('#' + wfListDiv).append(jQuery(componentTypeHierarchy));
    /*
    jQuery('#' + wfListDiv).jstree({

        "core" : {
            "themes": {
                "icons" : true,
                "dots" : false
            },
           // so that create works
            "check_callback" : function(callback, node, node_parent, node_position, more) {
                if (callback == 'move_node') {
                    if (node_parent.type == 'folder_node'
                        || node_parent.type == 'root') {
                        //alert('true');
                        return true;
                    } else {
                        //alert('false');
                        return false;
                    }
                } else if (callback == 'create_node') {
                    alert('create');
                    return true;
                }
            },
            "force_text" : false,
            'multiple': false
        },
        "search": {
            "case_insensitive": true,
            "show_only_matches" : true
        },
        "types" : {
            "#" : {
                  "max_children" : 1,
                  "max_depth" : 4,
                  "valid_children" : ["root"]
            },
            "root" : {
                "valid_children" : ["leaf_node", "folder_node"],
                "icon" : false
                //"icon" : "css/images/component_icon.svg"
            },
            "leaf_node" : {
                "valid_children" : [],
                "icon" : false
            },
            "folder_node" : {
                  "valid_children" : ["leaf_node"],
                  "icon" : false
                  //,"icon" : "css/images/component_icon.svg"
            }
        },
        "dnd": {
            check_while_dragging: true,
            is_draggable : function (nodes) {
                var i = 0, j = nodes.length;
                //for(; i < j; i++) {
                 //  if(this.get_node(nodes[i], true).hasClass('no_dragging')) {
                 //      return false;
                 //  }
                //}
                return true;
            }
        },

        "plugins" : [ "dnd", "types", "themes", "search" ]

      });

      jQuery('#' + wfListDiv).jstree("set_theme", "workflow-list");

      jQuery('#' + wfListDiv).on('close_node.jstree', function (e, data) {
          if (data.node.parent == '#') {
              setTimeout(function() {
                  data.instance.open_node(data.node);
              }, 0);
          }
        }); // end of close_node

      // Hide the dots on the left of the tree (children are indented)
      jQuery('#' + wfListDiv).jstree().hide_dots();*/

      // After opening folder
      // "#j1_3" is the folder of the old import components
      //jQuery('#' + wfListDiv).jstree("hide_node", jQuery("#j1_3"));
      //jQuery('#' + wfListDiv).on("after_open.jstree", function (e, data) {
          //initializeTooltips('.wfComponentTooltip');

          /*// Update resize handle:
              jQuery('#process-selector-div .ui-resizable-e').css('height',
              jQuery('#process-selector-div').prop('scrollHeight')); */
     // });

      // After closing folder
      //jQuery('#' + wfListDiv).on("after_close.jstree", function (e, data) {
          /*// Update resize handle:
          jQuery('#process-selector-div').resizable('destroy');
          jQuery('#process-selector-div').resizable({handles: 'e'});

          jQuery('#process-selector-div .ui-resizable-e').css('height',
              jQuery('#process-selector-div').prop('scrollHeight'));
          */
      //});

      /*jQuery("#component-lhs-search-input").keyup(function() {
          var searchString = jQuery(this).val();
          jQuery('#' + wfListDiv).jstree('search', searchString);
          initializeTooltips('.wfComponentTooltip');
          if (searchString.trim() == '') {

          } else {
              jQuery('.hideOnSearch').css('display', 'none');
          }

          jQuery('#' + wfListDiv).jstree("hide_node", jQuery("#j1_3"));

          // Let certain components be "pinned" meaning they are always present regardless of the search
          pinnedWorkflows.forEach(function(pinnedCompName) {
              jQuery('.draggableComponent[name="' + pinnedCompName + '"]').removeClass('jstree-hidden');
          });
      });


      jQuery('#component-lhs-search-clear-button').click(function() {
          jQuery("#component-lhs-search-input").val('');
          jQuery("#component-lhs-search-input").hint("auto-hint");
          jQuery('#component-lhs-search-input').keyup();
          jQuery('.hideOnSearch').css('display', '');
          jQuery('#' + wfListDiv).jstree("hide_node", jQuery("#j1_3"));
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

      jQuery('#component-lhs-search-input').keyup();*/

      /*jQuery('button').on('click', function () {
        jQuery('#' + wfListDiv).jstree(true).select_node('child_node_1');
        jQuery('#' + wfListDiv).jstree('select_node', 'child_node_1');
        jQuery.jstree.reference('#' + wfListDiv).select_node('child_node_1');
      });*/
/*

      jQuery(document)
          .on('dnd_move.vakata.jstree', function (e, data) {
              //data.helper.find('.jstree-icon').parent().removeClass('jstree-drag-component').addClass('jstree-drag-component');
              ///data.helper.find('.jstree-drag-component').css( {top: -10, left: -70, position:'relative'} );
              var t = jQuery(data.event.target);

              if(t.closest('.jstree').length) {
                  data.helper.find('.jstree-icon').removeClass('jstree-ok').addClass('jstree-ok');
              } else {
                  data.helper.find('.jstree-icon').removeClass('jstree-er').addClass('jstree-er');
              }

      });

      jQuery(document)
          .on('dnd_stop.vakata', function (e, data) {
              var t = jQuery(data.event.target);
      });

      jQuery('#' + wfListDiv + ' .wfLabel').on("click", "a",
          function() {
              //document.location.href = this;
          }
      );

      jQuery('#' + wfListDiv).on('select_node.jstree', function (e, data) {
          data.instance.toggle_node(data.node);
      });


      jQuery('#' + wfListDiv).on("open_node.jstree", function (e, data) {
          // alert(data.node.id);
          workflowTreeOpen++;
      });
      jQuery('#' + wfListDiv).on("close_node.jstree", function (e, data) {
          // alert(data.node.id);
          workflowTreeOpen--;
      });

      jQuery('#' + wfListDiv).on('select_node.jstree', function (e, data) {
              data.instance.deselect_node(data.node);

        }).jstree();

      jQuery(function () {
          jQuery('#tree_menu').on('changed.jstree', function (e, data) {
                console.log(data.node.id);
            });
        });

      jQuery('#' + wfListDiv).bind('ready.jstree', function(event, data) {
          var jQuerytree = jQuery(this);
          jQuery(jQuerytree.jstree().get_json(jQuerytree, {
              flat: true
            }))
            .each(function(index, value) {
                var node = jQuery('#' + wfListDiv).jstree().get_node(this.id);
                var lvl = node.parents.length;
                var idx = index;
                if (lvl == 1) {
                    folderCount++;
                }
            });
        });

      jQuery('#' + wfListDiv).on('ready.jstree', function () {
          jQuery('#' + wfListDiv).off("click.jstree", ".jstree-anchor");

      });*/

      // Hide per-workflow menus
      jQuery('.wfHamburgerList a').slideUp(0);

      jQuery(".hamburgerSpan").click(function() {

          // Hide open "per-folder link" span if one exists
          jQuery('.wfFolderHamburgerList a').slideUp(0);

          // Hide open "share link" span if one exists
          jQuery('.lsShareLink').slideUp(0);

          jQuery(".wfHamburgerList a").dequeue().slideUp(0);

          jQuery(".wfHamburgerList a[name=rowActions" + jQuery(this).attr('name') + "]")
              .slideDown(300);

          jQuery('.wfHamburgerList').css('left', (jQuery(this).position().left + 40) + 'px');

          jQuery('body').off('click');
          jQuery('body').click(function(evt) {

              if (evt.target.className != "hamburgerIcon") {
                  jQuery(".wfHamburgerList a").dequeue().slideUp(0);
              }
          });
      });

      jQuery(".hamburgerSpan img").hover(function() {
          jQuery(this).attr('src', 'css/images/hamburger_highlight.svg');
      });
      jQuery(".hamburgerSpan img").mouseout(function() {
          jQuery(this).attr('src', 'css/images/hamburger.svg');
      });
      //, function() {
        //  jQuery(".wfHamburgerList a[name=rowActions" + jQuery(this).attr('name') + "]")
          //    .delay(5000)
            //  .slideUp(500);
      //});

      jQuery('.wfFolderHamburgerList a').slideUp(0);

      jQuery(".folderHamburgerSpan").click(function() {

          // Hide open "per-workflow link" span if one exists
          jQuery('.wfHamburgerList a').slideUp(0);

          // Hide open "share link" span if one exists
          jQuery('.lsShareLink').slideUp(0);

          jQuery(".wfFolderHamburgerList a").dequeue().slideUp(0);
//alert(jQuery(this).offset().left);
          jQuery('.wfFolderHamburgerList').css('left', (jQuery(this).position().left + 40) + 'px');

          jQuery(".wfFolderHamburgerList a[name=rowActions" + jQuery(this).attr('name') + "]")
              .slideDown(300);

          jQuery('body').off('click');
          jQuery('body').click(function(evt) {

              if (evt.target.className != "hamburgerIcon") {
                  jQuery(".wfFolderHamburgerList a").dequeue().slideUp(0);
              }
          });
      });

      jQuery(".folderHamburgerSpan img").hover(function() {
          jQuery(this).attr('src', 'css/images/horiz_ellipse_highlighted.svg');
          jQuery(this).css('width', '25px');
          jQuery(this).css('height', '25px');
      });
      jQuery(".folderHamburgerSpan img").mouseout(function() {
          jQuery(this).attr('src', 'css/images/horiz_ellipse.svg');
      });

} // end of function


