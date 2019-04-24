
var wf_included_source = true;
var DIALOG_TITLE_PADDING_OFFSET = 30;

/**********************************************************************************
 *  Common feedback functions.
 *
 **********************************************************************************/

function formatHeading(str) {
    var newStr;
    if (str != null && str.length > 0) {
        newStr = str.replace(/^[a-z]/g, function(m) {
            return m.toUpperCase()
        }).replace(/_/g, ' ');
    } else {
        newStr = str;
    }

    return newStr;
}

//Generic info dialog.
function wfInfoDialog(dialogId, dialogHtml, dialogTitle) {
 jQuery('<div />', {
     id : dialogId
 }).html(dialogHtml).dialog({
     open : function() {
         jQuery('.ui-button').focus();
     },
     autoOpen : true,
     autoResize: true,
     resizable : false,
     width : '30%',

     modal : true,
     title : dialogTitle,
     buttons : {
         'OK' : function() {
             jQuery(this).dialog('close');
         }
     },
     open : function() {
         jQuery(this).height((jQuery(this).height() + DIALOG_TITLE_PADDING_OFFSET) + 'px');
     },
     close : function() {
         jQuery(this).remove();
     }
 });
}

function wfInfoDialogAdvanced(dialogId, dialogHtml, dialogTitle, isModal) {
     jQuery('<div />', {
         id : dialogId
     }).html(dialogHtml).dialog({
         open : function() {
             jQuery('.ui-button').focus();
         },
         autoOpen : true,
         autoResize: true,
         resizable : false,
         width : '30%',

         modal : isModal,
         title : dialogTitle,
         buttons : {
             'OK' : function() {
                 jQuery(this).dialog('close');
             }
         },
         open : function() {
             jQuery(this).height((jQuery(this).height() + DIALOG_TITLE_PADDING_OFFSET) + 'px');
         },
         close : function() {
             jQuery(this).remove();
         }
     });
    }

function createWfTimerDialog(dialogId, dialogHtml, dialogTitle) {
 var midWidth = jQuery(document).width() / 2 - 285 / 2;
 var quarterHeight = jQuery(document).height() / 4;
 jQuery('<div />', {
     id : dialogId
 }).html(dialogHtml).dialog({
     autoOpen : true,
     autoResize: true,
     resizable : false,
     open: function() {
         //jQuery(this).parents(".ui-dialog:first")
         //    .find(".ui-dialog-titlebar-close").remove();
         //jQuery('.ui-button').focus();
     },
     resizable : false,
     width : 285,
     modal : false,
     title : dialogTitle,
     close : function() {
         jQuery(this).remove();
     }
 });
}


function wfTimerDialog(dialogHtml, timeMS) {
    createWfTimerDialog('wfTimerDialog', dialogHtml, 'Warning');
    setTimeout(function() {
            jQuery('#wfTimerDialog').dialog('close')
        }, timeMS);
}

function wfTimerDialog(dialogHtml) {
    createWfTimerDialog('wfTimerDialog', dialogHtml, 'Warning');
    setTimeout(function() {
            jQuery('#wfTimerDialog').dialog('close')
        }, 3500);
}

function wfTimerDialog(title, dialogHtml, timeMS) {
    createWfTimerDialog('wfTimerDialog', dialogHtml, title);
    setTimeout(function() {
            jQuery('#wfTimerDialog').dialog('close')
        }, timeMS);
}

function isAlphaNumeric(key) {
    if((57>=key && key>=48)
        || (122>=key && key>=96)
        || (90>=key && key>=65)
        || (key == 8) || (key == 46)
        || (key == 13) || (key == 32)) {
          return true;
    } else {
          return false;
    }
}

// Can we fix IE11 issues? Sigh.
function isThisIE() {
    // Pre-version 11, this was sufficient.
    if (Prototype.Browser.IE) { return true; }

    var ua = navigator.userAgent;

    // With IE 11, the string 'Trident' appears in this.
    if (ua.indexOf('Trident') > 0) { return true; }

    // With Microsoft Edge...
    if (ua.indexOf('Edge') > 0) { return true; }

    // Safari... oddly enough, the userAgent for Chrome includes 'Safari'
    if ((ua.indexOf('Safari') > 0)
        && (ua.indexOf('Chrome') < 0)) { return true; }

    return false;
}

function getAdminUserFlag() {
    return jQuery('input#adminUserFlag').val() == "true" ? true : false;
}

/* Function taken from https://stackoverflow.com/questions/979975/how-to-get-the-value-from-the-get-parameters#1099670
 * getParam('dummyParam', 'hxxp://example.com/?dummyParam=arbitraryValue')
 */
function getParam( name, url ) {
    if (!url) url = location.href;
    name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
    var regexS = "[\\?&]"+name+"=([^&#]*)";
    var regex = new RegExp( regexS );
    var results = regex.exec( url );
    return results == null ? null : results[1];
}

function copyCurrentLocationToClipboard() {
    var dummy = document.createElement('input'),
    text = window.location.href;
    document.body.appendChild(dummy);
    dummy.value = text;
    dummy.select();
    document.execCommand('copy');
    document.body.removeChild(dummy);
    showLinkCopiedDialog("copiedLinkDialog",
        "You can now paste the link using ctrl-v or ⌘-v.",
            "Copied to Clipboard");
}

/* Share link / share url widget */
function shareLink() {
    // Hide share link
    jQuery('.lsShareLink').slideUp(0);

    jQuery('.linkMenu').slideUp(0);

    // Hide wf list menu if one exists
    jQuery('.wfHamburgerList a').slideUp(0);

    // Hide wf folder list menu if one exists
    jQuery('.wfFolderHamburgerList a').slideUp(0);

     // Hide wf component selection area menu if one exists
     jQuery('.wfCompSelectList a').slideUp(0);

    jQuery(".lsShareLink")
        .slideDown(300);

    jQuery('#curLink').text(window.location.href);

    jQuery('#copyLinkButton').button({
        label : "Copy",
        classes: {
               "ui-button": "copy-button-class"
        }
    });
    jQuery('#copyLinkButton').click(copyCurrentLocationToClipboard);


    jQuery('#cancelCopyLinkButton').button({
        label : "Cancel",
        classes: {
               "ui-button": "cancel-button-class"
        }
    });

    jQuery('body').off('click');
    jQuery('body').click(function(evt) {

        if (evt.target.id != "curLink") {
            jQuery('.lsShareLink').slideUp(0);
        } else {
            selectText('curLink');
        }
    });
}

/* Select all inner text given a div id, used for share URL widget.  */
function selectText(node) {
    node = document.getElementById(node);

    if (document.body.createTextRange) {
        const range = document.body.createTextRange();
        range.moveToElementText(node);
        range.select();
    } else if (window.getSelection) {
        const selection = window.getSelection();
        const range = document.createRange();
        range.selectNodeContents(node);
        selection.removeAllRanges();
        selection.addRange(range);
    } else {
        console.warn("Could not select text in node: Unsupported browser.");
    }
}

function showLinkCopiedDialog(dialogId, dialogHtml, dialogTitle) {
     var midWidth = jQuery(document).width() / 2 - 285 / 2;
     var quarterHeight = jQuery(document).height() / 4;

     jQuery('<div />', {
         id : dialogId
     }).html(dialogHtml).dialog({
         autoOpen : true,
         autoResize: true,
         resizable : true,
         position: {
             my: 'top',
             at: 'top',
             of: jQuery('.shareIcon')
         },
         open: function() {
             setTimeout(function() {
                 jQuery('#' + dialogId).dialog('close')
             }, lsWorkflowListDialogTime);
         },
         resizable : false,
         width : 285,
         modal : false,
         title : dialogTitle,
         close : function() {
             jQuery(this).remove();
         }
     });
    }
