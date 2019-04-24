/** Contains the tags of the workflows in the list on the home page */
var workflowListTags = {}; // {wfId : [tags]}

/** temporary list to hold tags of workflows in the list while you are editing them */
var tempTagList = [];

/** maximum number of common tags that appears in the search panel */
var MAX_COMMON_TAGS = 7;

/** List of the existing workflows */
var existingWorkflowTags = null;

/** Tags in the search saved in the WorkflowContext */
var workflowContextTagsString = null;

/** Regular expression used to determine which characters are allowed in tags */
var TAG_CHARS_REGEX = /^[ A-Za-z0-9-_@*+.$]+$/;

/**
 * Returns the html of the list of current tags and the input to add tags
 * @param wfId - id of the workflow being modified, or null if new workflow
 * @returns
 */
function getAddTagsHtml(wfId, isSearch) {
    let search = '';
    if (isSearch) {search = 'Search';}

    let tagDiv = jQuery('<div id="workflowTagsDiv' + search + '"></div>');

    let tagSpan = jQuery('<span id="wfTags">Tags: <span id="wfTagsEmpty">empty</span></span>');
    if (isSearch) {
        tagSpan = jQuery('<span id="wfTags' + search + '"></span>');
    }

    // Add tags if this is not a new workflow and the workflow already has some tags
    if (wfId != null) {
        tagSpan.attr('workflow_id', wfId);

        let thisWfTags = workflowListTags[wfId];
        for (let i = 0; i < thisWfTags.length; i++) {
            tagSpan.append(createTagHtml(thisWfTags[i], isSearch));
        }

        // When editing tags in this dialog, modify this temp list until save/cancel
        tempTagList = JSON.parse(JSON.parse(JSON.stringify(workflowListTags[wfId])));
    }

    tagDiv.append(tagSpan);

    var placeholderStr = "";
    if (!isSearch) {
        placeholderStr = 'placeholder="Create or add tags."';
    }
    // Add the input to let users add more tags
    let inputDiv = jQuery('<div id="searchTagsDiv' + search + '"></div>');
    let tagInput = jQuery('<input type="text" class="hasPlaceholder" id="wfTagInput' + search + '"'
        + 'value=""' + placeholderStr +' />');
    let addTagButton = jQuery('<button id="addTagButton' + search + '" class="ui-button">+</button>');

    inputDiv.append(tagInput).append(addTagButton);

    tagDiv.append(inputDiv);

    return tagDiv;
}

/**
 * The workflow list just got loaded. Add the tags to an object to keep track of them.
 * @param wfRows - json object contain data from the server about the workflows in the list
 * @returns
 */
function setWorkflowTagsObject(wfRows) {
    // New page of wf's came in, clear old object
    workflowListTags = {};

    jQuery(wfRows).each(function (wfRowIndex, wfRowObj) {
        let wfId = wfRowObj.id;

        let tagsStr = wfRowObj.tags;
        let tagsAr = [];
        if (tagsStr != null ) {
            tagsAr = JSON.parse(tagsStr);
        }
        workflowListTags[wfId] = tagsAr;
    });
}

/**
 * Create the html for each tag box
 * @param tagName
 * @returns
 */
function createTagHtml(tagName, isSearch) {
    let search = '';
    if (isSearch) {search = 'Search';}

    return jQuery('<div class="wfTag' + search + '" title="' + tagName + '">'
           + tagName + '<span class="deleteTag' + search + '">&#10005;</span></div>');
}

/**
 * Initializes the tag input auto complete and initializes the delete and add
 * tag buttons.
 * @returns
 */
function initWorflowTagFunctionality(isSearch) {
    let search = '';
    if (isSearch) {search = 'Search';}

    // Auto Complete with tags that have already been made
    if (existingWorkflowTags != null) {
        let existingTags = [];
        existingWorkflowTags.forEach(function(tagCountObj) {
            existingTags.push(tagCountObj.tagNames[0]);
        });
        jQuery('#wfTagInput' + search).autocomplete({
            source : existingTags,
            minLength: 0
        });
    }

    // Add list of common tags if it's a search
    if (isSearch) {
        addCommonTags(isSearch);
    }

    // When you hit enter on input or click the add button, add the tag
    if (isSearch) {
        jQuery('#wfTagInput' + search).keyup(function(e){
            if(e.keyCode == 13) {
                addTagFromInput(true);
            }
        });
    } else {
        jQuery('#wfTagInput').keyup(function(e){
            if(e.keyCode == 13) {
                addTagFromInput(false);
            }
        });
    }

    if (isSearch) {
        jQuery('#addTagButton' + search).click(function() {
            addTagFromInput(true);
        });
    } else {
        jQuery('#addTagButton' + search).click(function() {
            addTagFromInput(false);
        });
    }

    // Initialize the delete button on the tag
    initializeDeleteButton(search);

    if (isSearch) {
        jQuery('#wfTagInput' + search).click(function() { jQuery('#wfTagInputSearch').autocomplete("search", ""); });
    } else {
        jQuery('#wfTagInput').click(function() { jQuery('#wfTagInput').autocomplete("search", ""); });
    }

    // If tags exist, don't display 'empty' string.
    if (jQuery('#wfTags').children('.wfTag').length > 0) {
        jQuery('span#wfTagsEmpty').css('display', 'none');
    }
}

function addCommonTags(isSearch) {
    let search = '';
    if (isSearch) {search = 'Search';}

    // Add the common tags in a list
    let i = 0;
    if (existingWorkflowTags != null) {
        existingWorkflowTags.forEach(function(tag) {
            if (i++ >= MAX_COMMON_TAGS) {
                return;
            }
            let commonTagHtml = jQuery('<div class="commonTagRow"><span class="commonTag'
                    + search + '" title="' + tag.tagNames[0] + '">'
                    + tag.tagNames[0] + '</span></div>');
            jQuery('#workflowTagsDiv' + search).append(commonTagHtml);
        });
    }

    // When you click on these common tags, it adds them to the search
    jQuery('.commonTag' + search).click(function() {
        let tagText = jQuery(this).text();

        // Toggle the tag if clicked
        if (getWorkflowTags(isSearch).includes(tagText)) {
            // Remove the tag, it is already included in search
            jQuery('.wfTag' + search).each(function() {
                if (jQuery(this).attr('title') == tagText) {
                    jQuery(this).find('.deleteTag' + search).click();
                }
            });
            jQuery(this).removeClass('selectedCommonTag');
        } else {
            // Tag hasn't been already added, so add it
            addTag(tagText, isSearch);
            jQuery(this).addClass('selectedCommonTag');
        }
    });
}

/**
 * Initialize the functionality of the delete button
 * @param search - boolean denoting the search tags vs adding tags in a dialog
 */
function initializeDeleteButton(isSearch) {
    let search = '';
    if (isSearch) {search = 'Search';}

    jQuery('.deleteTag' + search).off(); // Clear click to avoid double event handlers
    if (!isSearch) {
        jQuery('.deleteTag').click(function() {
            // Remove from global tags object
            let wfId = jQuery(this).closest('#wfTags').attr('workflow_id');
            if (wfId != null) {
                // You are modifying an existing workflow
                let indexOfTag = jQuery('.wfTag').index(jQuery(this).closest('.wfTag'));
                tempTagList.splice(indexOfTag, 1);
            }

            // Remove from the common tags, if one of the common tags
            let tagText = jQuery(this).closest('.wfTag').attr('title');
            unselectCommonTag(tagText, false);

            // Delete the html
            jQuery(this).closest('.wfTag').remove();

            // Determine if any tags are left...
            if (jQuery('#wfTags').children('.wfTag').length == 0) {
                jQuery('span#wfTagsEmpty').css('display', '');
            }
        });
    } else {
        jQuery('.deleteTagSearch').click(function() {
            // Remove from the common tags, if one of the common tags
            let tagText = jQuery(this).closest('.wfTagSearch').attr('title');
            unselectCommonTag(tagText, true);

            // Delete the html
            jQuery(this).closest('.wfTagSearch').remove();
            // Update value of search filter
            jQuery('#workflowTagsDivSearch').val(getWorkflowTags(true).toString());
            // Search
            jQuery('#wfSearch').click();
        });
    }
}

/**
 * See if tag is a common tag that is already selected, then unselect it
 * @param tagText - tag to be unselected
 * @param isSearch
 */
function unselectCommonTag(tagText, isSearch) {
    let search = '';
    if (isSearch) {search = 'Search';}

    jQuery('.commonTag' + search).each(function() {
        if (jQuery(this).text() == tagText) {
            jQuery(this).removeClass('selectedCommonTag');
        }
    });
}

/**
 * See if tag is a common tag, then mark it as selected
 * @param tagText - tag to be selected
 * @param isSearch
 */
function selectCommonTag(tagText, isSearch) {
    let search = '';
    if (isSearch) {search = 'Search';}

    jQuery('.commonTag' + search).each(function() {
        if (jQuery(this).text() == tagText) {
            jQuery(this).addClass('selectedCommonTag');
        }
    });
}

/**
 * Get a list of the tags
 * @param isSearch - if true, get the tags from the search panel
 * @returns
 */
function getWorkflowTags(isSearch) {
    let search = '';
    if (isSearch) {search = 'Search';}

    let tagList = [];
    jQuery('.wfTag' + search).each(function() {
        tagList.push(htmlEncode(jQuery(this).attr('title')));
    });

    return tagList;
}

/**
 * Take the string that is in the input, and make it into a tag.
 */
function addTagFromInput(isSearch) {
    let search = '';
    if (isSearch) {search = 'Search';}

    let newTagValue = jQuery('#wfTagInput' + search).val();
    if (newTagValue == null || newTagValue == '') {
        return;
    }

    jQuery('span#wfTagsEmpty').css('display', 'none');
    addTag(newTagValue, isSearch);
}

/**
 * Take the string that is in the input, and make it into a tag.
 */
function addTag(newTagValue, isSearch) {
    let search = '';
    if (isSearch) {search = 'Search';}

    if (newTagValue == null || newTagValue == '') {
        return;
    }

    // Eliminate trailing and preceeding white space
    newTagValue = newTagValue.trim();

    // Determine if it is a valid tag
    if (!isValidTagName(newTagValue)) {
        promptInvalidTagValue(newTagValue);
        return;
    }

    // See if the tag is already in the list of tags for this workflow/search
    let tagsAlreadyIn = getWorkflowTags(isSearch);
    if (tagsAlreadyIn != null && tagsAlreadyIn.length > 0) {
        if (tagsAlreadyIn.includes(newTagValue)) {
            let dialogHtml = 'Tag, "' + newTagValue + '", is already in the tag list.';
            let title = 'Tag Redundancy';
            let timeMS = 3000;

            wfTimerDialog(title, dialogHtml, timeMS);
            return;
        }
    }

    let newTag = createTagHtml(newTagValue, isSearch);
    jQuery('#wfTags' + search).append(newTag);

    // If this is a common tag, select it
    selectCommonTag(newTagValue, isSearch);

    // If modifying the wf change the global list of tags
    let wfId = jQuery('#wfTags' + search).attr('workflow_id');
    if (wfId != null) {
        // You are modifying an existing workflow
        tempTagList.push(newTagValue);
    }

    initializeDeleteButton(isSearch);


    // Remove the text from the input
    jQuery('#wfTagInput' + search).val('');

    if (isSearch) {
        // Update value of search filter
        jQuery('#workflowTagsDivSearch').val(getWorkflowTags(true).toString());
        // Search
        jQuery('#wfSearch').click();
    }
}

/**
 * Format the tags to be put into the tooltip containing the workflow description
 * @param workflow_id
 * @returns String of html to be inserted into the tooltip
 */
function getTagsForTooltip(workflow_id) {
    let tagHtml = jQuery('<div class="tagsInTooltip"></div>');

    let tags = workflowListTags[workflow_id];
    if (tags != null) {
        tags.forEach(function(tag) {
            tagHtml.append(jQuery('<span class="tagInTooltip">'+tag+'</span>'));
        });
    }

    return tagHtml.prop('outerHTML');
}

/**
 * Determine if the tags were modified in the settings popup
 * @param workflowId
 * @returns true if the tags were modified
 */
function tagsWereModified(workflowId) {
    let tagsArrayIsSame = true;

    let existingTags = workflowListTags[workflowId];
    let currTags = tempTagList;

    if (currTags != null && existingTags != null) {
        tagsArrayIsSame = currTags.length === existingTags.length
                && currTags.sort().every(function(value, index) { return value === existingTags.sort()[index]});
    }

    // If the tags were modified and you added one, put it in the popular tags list
    if (!tagsArrayIsSame) {
        currTags.forEach(function(newTag) {
            if (!existingTags.includes(newTag)) {
                // Check if newTag is already in the list of common tags
                let alreadyCommonTag = false;
                jQuery('.commonTagSearch').each(function() {
                    let commonTag = jQuery(this).attr('title');
                    if (commonTag == newTag) {
                        alreadyCommonTag = true;
                    }
                });
                if (alreadyCommonTag) {
                    return;
                }
                // not already in the list, add it to the list of common tags
                let newTagObj = {tagNames: [newTag], count: -1};
                existingWorkflowTags.unshift(newTagObj);
            }
        });
        // Remove previous common Tags, then re add them with the new tags.  Keep selections
        let selectedCommonTags = [];
        jQuery('.selectedCommonTag').each(function() {
            selectedCommonTags.push(jQuery(this).attr('title'));
        });
        jQuery('.commonTagRow').remove();
        addCommonTags(true);
        // Re select the tags that were selected before
        selectedCommonTags.forEach(function(tag) {
           selectCommonTag(tag, true);
        });
    }

    return !tagsArrayIsSame;
}

function removeAllTags(isSearch) {
    if (isSearch) {
        jQuery('.deleteTagSearch').click();
    } else {
        jQuery('.deleteTag').click();
    }
}

/**
 * Determine whether a string is a valid tag name
 * @param tagName - String tag name in question
 * @returns true if tagName is a valid name of a tag
 */
function isValidTagName(tagName) {
    if (tagName.match(TAG_CHARS_REGEX) == tagName) {
        return true;
    }
    return false;
}

/**
 * Show a dialog to let the user know that the tag value is invalid.
 * @param tag - The invalid tag
 */
function promptInvalidTagValue(tag) {
    let dialogHtml = '"' + tag + '" is an invalid value for a tag.'
        + '<br/> Valid tag characters include numbers, letters, underscores, hyphens, periods, *, +, $, @, and spaces.';
    let title = 'Invalid Tag';
    let timeMS = 10000;

    wfTimerDialog(title, dialogHtml, timeMS);
}

/**
 * Add the html and functionality to search via tags
 */
function createSearchViaTags() {
    let tagsHtml = getAddTagsHtml(null, true);

    jQuery('.searchTagDiv').html(tagsHtml);

    initWorflowTagFunctionality(true);

    // Add tags to the search from the WorkflowContext
    if (workflowContextTagsString != null) {
        let workflowContextTags = workflowContextTagsString.split(",");
        workflowContextTags.forEach(function(tag) {
            addTag(tag, true);
        });
    }
}








