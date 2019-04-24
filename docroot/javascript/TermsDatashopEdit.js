//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2011
// All Rights Reserved
//
// Author: Mike Komisin
// Version: $Revision: 7368 $
// Last modified by: $Author: mkomi $
// Last modified on: $Date: 2011-12-01 11:36:38 -0500 (Thu, 01 Dec 2011) $
// $KeyWordsOff: $
//

String.prototype.trim = function () {
    return this.replace(/^\s*/, "").replace(/\s*$/, "");
}

var myLoadedText = "";
Event.observe(window, 'load',
    function(){
        Event.observe($("save_terms_button"), 'click', saveAndPublishHandler);
        Event.observe($("terms-text"), 'keyup', textareaModified);
        myLoadedText = $("terms-text").value;
        toggleAnchor($("curlink"), true);
    }
);

function textareaModified() {
    if ( $("terms-text").value.trim() == myLoadedText.trim() ) {
        $("save_terms_button").disabled = true;
        toggleAnchor($("curlink"), true);
    }
    else {
        $("save_terms_button").disabled = false;
        toggleAnchor($("curlink"), false);
    }
}

function toggleAnchor(link, disable) {
    if (link != null) {
        if(disable){
            var href = link.getAttribute("href");
            if(href && href != "" && href != null){
                link.setAttribute('href_bak', href);
            }
            link.removeAttribute('href');
            link.setAttribute('class', "disabled");
        }
        else{
            link.setAttribute('href', link.attributes['href_bak'].nodeValue);
            link.setAttribute('class', "");
        }
    }
}

function loadSelectedTerms(touVersionId) {

    var newForm = document.createElement('form');
    newForm.setAttribute('name', 'versions_form');
    newForm.setAttribute('id', 'versions_form');
    newForm.setAttribute('form', 'text/plain');
    newForm.setAttribute('action', 'ManageTerms');
    newForm.setAttribute('method', 'POST');
    
    var newInput = document.createElement('input');
    newInput.name="select_terms";
    newInput.type="hidden";
    newForm.appendChild(newInput);
    
    newInput = document.createElement('input');
    newInput.name="edit_ds_terms";
    newInput.type="hidden";
    newForm.appendChild(newInput);
    
    newInput = document.createElement('input');
    newInput.name="version_id";
    newInput.type="hidden";
    newInput.value= touVersionId;
    newForm.appendChild(newInput);
    
    newInput = document.createElement('input');
    newInput.name="terms-text";
    newInput.type="hidden";
    newInput.value= $("terms-text").value;
    newForm.appendChild(newInput);
    
    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
    
}

function saveAndPublishHandler() {
    var confirmation =
        '<div id="save-and-publish-div" style="font-family: georgia; font-size: 13;">'
        + 'Saving and publishing the DataShop Terms of Use will<br>'
        + 'affect all users of DataShop. Are you really sure you<br>'
        + 'want to publish a new version of the terms of use?<br><br>'
        + '<a href="javascript:saveTerms()">Yes, do it</a> | <a href="javascript:cancelSaveAndPublish()">No, get me out of here</a>'
        + '</div>';
    $("save-and-publish-div").replace(confirmation);
}

function cancelSaveAndPublish() {
    var save = '<div id="save-and-publish-div">'
        + '<button type="button" id="save_terms_button" onclick="saveAndPublishHandler();">Save and Publish</button>'
        + '</div>';
    $("save-and-publish-div").replace(save);
}

function saveTerms() {
    document.forms['edit_ds_terms_form'].submit();
}
