jQuery.noConflict();
var DISABLED_BG_COLOR = '#FFD9D9';
jQuery(document).ready(function() {

    var gridValues = {};
    jQuery('#componentForm .formItem').change(function() {

      // Send an AJAX request to save the currentDigraph to the database.
         var ids = jQuery(this).attr('id').split('_');
         var componentId = ids[1];
         sendAjaxRequest(jQuery(this).attr('id'), componentId);
    });

    jQuery('#componentForm .formItemText').keyup(function(event) {

        var key = "";
        key = event.keyCode || event.which;

        var ids = jQuery(this).attr('id').split('_');
        var componentId = ids[1];

        if (key === 13) {
            sendAjaxRequest(jQuery(this).attr('id'), componentId);
        } else if ((key >= 48 && key <= 90) || key == 8 || key == 46) {

            var newButtons = "<span id=\"saveOrCancel_" + componentId + "\" class=\"saveOrCancelButtonClass\">"
                + "<a href=\"javascript:removeButtons('" + jQuery(this).attr('id') + "', '" + componentId + "')\">Cancel</a> / "
                + " <a href=\"javascript:sendAjaxRequest('" + jQuery(this).attr('id') + "', '" + componentId + "')\">Save</a>"
                + "</span>";
            if (jQuery(this).parent() != null && jQuery('#saveOrCancel_' + componentId).length <= 0) {
                jQuery(this).parent().append(newButtons);
            }
        }
    });

    jQuery('#componentForm .formItemText').click(function() {
        var newWidth = ((this.value.length + 1) * 8);
        var oldWidth = jQuery(this).css('width').replace("px", "");
        jQuery('.formItemText').css('width', '');
        jQuery('.formItemText').parent().css('width', '');
        if (newWidth > oldWidth) {
            jQuery(this).parent().css('width', newWidth + 'px');
            jQuery(this).css('width', newWidth + 'px');
        }
    });

    jQuery('.formItem').change(function() {
        if (this.value == "false") {
            jQuery(this).css('background-color', DISABLED_BG_COLOR);
        } else {
            jQuery(this).css('background-color', '#FFFFFF');
        }
    });

    refreshUI();

    /* Tooltip screws up click handler.
    jQuery('#componentForm .formItemText').each(function(formItemIndex, formItem) {
         var options = new Array();
         new ToolTip(formItem.id, jQuery('#' + formItem.id).attr('value'), options);
    });*/

});

function refreshUI() {
    jQuery('.formItem').each(function() {
        if (this.value == "false") {
            jQuery(this).css('background-color', DISABLED_BG_COLOR);
        } else {
            jQuery(this).css('background-color', '#FFFFFF');
        }
    });
}

function removeButtons(thisId, componentId) {
    jQuery('#saveOrCancel_' + componentId).remove();
    sendAjaxRefresh(thisId, componentId);
}

function sendAjaxRequest(thisId, componentId) {
    new Ajax.Request('ManageComponents', {
        parameters : {
            requestingMethod : 'updateComponentField',
            fieldId : jQuery('#' + thisId).attr('name'),
            componentId : componentId,
            value : jQuery('#' + thisId).val()
        },
        onComplete : function() { removeButtons(thisId, componentId); },
        beforeSend : wfShowLoadingPopup(true, "Updating..."),
        onSuccess : wfHideLoadingPopup,
        onException : function(request, exception) {
            wfHideLoadingPopup(); throw(exception);
        }
    });
}


function sendAjaxRefresh(thisId, componentId) {
    new Ajax.Request('ManageComponents', {
        parameters : {
            requestingMethod : 'refreshComponentField',
            fieldId : jQuery('#' + thisId).attr('name'),
            componentId : componentId,
            value : ''
        },
        onComplete : refreshValues,
        beforeSend : wfShowLoadingPopup(true, "Updating..."),
        onSuccess : wfHideLoadingPopup,
        onException : function(request, exception) {
            wfHideLoadingPopup(); throw(exception);
        }
    });
}

function refreshValues(transport) {
    var workflowAsJson = null;
    var json = null;
    var dsId = null;
    if (transport !== undefined) {
       json = transport.responseText.evalJSON(true);
       if (json.componentId !== undefined && json.componentId != null) {
           if (json.fieldId !== undefined && json.fieldId != null) {
               if (json.value !== undefined && json.value != null) {
                   jQuery('#' + json.fieldId + '_' + json.componentId).val(json.value);
               }
           }
       }
    }
}
//Shows a status indicator while waiting
function wfShowLoadingPopup(titleString) {
  if (jQuery('#updateComponentStatus').length <= 0) {

    jQuery('<div/>', {
        id : 'updateComponentStatus'
    }).appendTo('body');

    // Status Indicator Popup
    jQuery('#updateComponentStatus').html(
            '<center><img id="waitingIcon" src="images/waiting.gif" /></center>').dialog({
        closeOnEscape: false,
        open: function(event, ui) {
            //jQuery(".ui-dialog-titlebar-close", ui.dialog | ui).hide();
        },
        modal : false,
        autoOpen : false,
        title : titleString,
        width : 155,
        height : 175,
        create : function() {
            jQuery('#updateComponentStatus').css('border', 'solid 1px black');
        },
        close : function() {
            jQuery(this).remove();
        }
    });

    jQuery('#updateComponentStatus').dialog('open');
    jQuery('#updateComponentStatus').dialog("option", "stack", true);
    jQuery('#updateComponentStatus').dialog("option", "resizable", false);
  }
}


//Hides that status indicator
function wfHideLoadingPopup() {

  jQuery('#updateComponentStatus').remove();

}
