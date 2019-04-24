//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2009
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 15738 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2018-12-04 16:35:34 -0500 (Tue, 04 Dec 2018) $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(function() {

        // Manage Problem Content page
        jQuery('#manage_pc_cv_search_by').keyup(searchContentVersions);
        jQuery('#manage_pc_cv_search_button').click(searchContentVersions);
        jQuery('#manage_pc_ds_search_by').keyup(searchDatasets);
        jQuery('#manage_pc_ds_search_button').click(searchDatasets);
        jQuery('#manage_pc_cv_clear_button').click(pcCVSearchByClear);
        jQuery('#manage_pc_ds_clear_button').click(pcDSSearchByClear);
        jQuery('#manage_pc_tool_select').change(selectConversionTool);
        jQuery('#manage_pc_mapped_select').change(selectMapped);

        // Why aren't these working???
        jQuery('#manage_pc_cv_search_by').hint('auto-hint');
        jQuery('#manage_pc_ds_search_by').hint('auto-hint');

        // Manage DataShopInstance page
        jQuery('#ds-url-input').keyup(updateSubmitAndReset);
        jQuery('#sendmail-input').change(updateSubmitAndReset);
        jQuery('#remote-name-input').keyup(updateSubmitAndReset);
        jQuery('#remote-url-input').keyup(updateSubmitAndReset);
        jQuery('#help-email-input').keyup(updateSubmitAndReset);
        jQuery('#rm-email-input').keyup(updateSubmitAndReset);
        jQuery('#bucket-email-input').keyup(updateSubmitAndReset);
        jQuery('#smtp-host-input').keyup(updateSubmitAndReset);
        jQuery('#smtp-port-input').keyup(updateSubmitAndReset);
        jQuery('#smtp-user-input').keyup(updateSubmitAndReset);
        jQuery('#smtp-password-input').keyup(updateSubmitAndReset);

        jQuery('#is-slave-input').change(isSlaveChanged);
        jQuery('#use-ssl-smtp-input').change(useSslSmtpChanged);

        jQuery('#slave-id-input').keyup(updateSubmitAndReset);
        jQuery('#master-user-input').keyup(updateSubmitAndReset);
        jQuery('#master-url-input').keyup(updateSubmitAndReset);
        jQuery('#master-schema-input').keyup(updateSubmitAndReset);
        jQuery('#slave-apitoken-input').keyup(updateSubmitAndReset);
        jQuery('#slave-secret-input').keyup(updateSubmitAndReset);

        jQuery('#gh-client-id-input').keyup(updateSubmitAndReset);
        jQuery('#gh-client-secret-input').keyup(updateSubmitAndReset);
        jQuery('#wfc-dir-input').keyup(updateSubmitAndReset);
        jQuery('#wfc-slave-instance').keyup(updateSubmitAndReset);
        jQuery('#wfc-heap-size').keyup(updateSubmitAndReset);

        jQuery('#admin-reset').click(resetForm);
});

//
// Handles the Which Datasets radio button.
//
function getAdminDatasets() {

    var myForm = document.createElement('FORM');
    myForm.setAttribute('name',   'admin_domain_learnlab_save_form');
    myForm.setAttribute('id',     'admin_domain_learnlab_save_form');
    myForm.setAttribute('form',   'text/plain');
    myForm.setAttribute('action', window.location.href);
    myForm.setAttribute('method', 'POST');

    var input   = document.createElement('input');
    input.name  = "admin_action";
    input.type  = "hidden";
    input.value = "get_datasets";
    myForm.appendChild(input);

    var excludedFlagInput   = document.createElement('input');
    excludedFlagInput.name  = "excluded_flag";
    excludedFlagInput.type  = "hidden";
    var excludedFlagForm = $('which_datasets_form');
    excludedFlagInput.value = excludedFlagForm.which_datasets_rb[0].checked;
    myForm.appendChild(excludedFlagInput);

    document.getElementsByTagName('body').item(0).appendChild(myForm);
    myForm.submit();
}

//
// Handles the Domain/LearnLab option menu.  Saves change immediately.
//
function saveDomainLearnLabChange(elementId, datasetId) {
    var theSelect = $(elementId);

    var myForm = document.createElement('FORM');
    myForm.setAttribute('name',   'admin_domain_learnlab_save_form');
    myForm.setAttribute('id',     'admin_domain_learnlab_save_form');
    myForm.setAttribute('form',   'text/plain');
    myForm.setAttribute('action', window.location.href);
    myForm.setAttribute('method', 'POST');

    var input   = document.createElement('input');
    input.name  = "admin_action";
    input.type  = "hidden";
    input.value = "save_domain_learnlab";
    myForm.appendChild(input);

    var datasetIdInput   = document.createElement('input');
    datasetIdInput.name  = "dataset_id";
    datasetIdInput.type  = "hidden";
    datasetIdInput.value = datasetId;
    myForm.appendChild(datasetIdInput);

    var domainLearnLabInput   = document.createElement('input');
    domainLearnLabInput.name  = "domain_learnlab";
    domainLearnLabInput.type  = "hidden";
    domainLearnLabInput.value = theSelect.value;
    myForm.appendChild(domainLearnLabInput);

    var excludedFlagInput   = document.createElement('input');
    excludedFlagInput.name  = "excluded_flag";
    excludedFlagInput.type  = "hidden";
    var excludedFlagForm = $('which_datasets_form');
    excludedFlagInput.value = excludedFlagForm.which_datasets_rb[0].checked;
    myForm.appendChild(excludedFlagInput);

    document.getElementsByTagName('body').item(0).appendChild(myForm);
    myForm.submit();
}

//
// Handles the Junk Flag option menu.  Saves change immediately.
//
function saveJunkFlagChange(elementId, datasetId) {
    var theSelect = $(elementId);

    var myForm = document.createElement('FORM');
    myForm.setAttribute('name',   'admin_junk_flag_save_form');
    myForm.setAttribute('id',     'admin_junk_flag_save_form');
    myForm.setAttribute('form',   'text/plain');
    myForm.setAttribute('action', window.location.href);
    myForm.setAttribute('method', 'POST');

    var input   = document.createElement('input');
    input.name  = "admin_action";
    input.type  = "hidden";
    input.value = "save_junk_flag";
    myForm.appendChild(input);

    var datasetIdInput   = document.createElement('input');
    datasetIdInput.name  = "dataset_id";
    datasetIdInput.type  = "hidden";
    datasetIdInput.value = datasetId;
    myForm.appendChild(datasetIdInput);

    var junkFlagInput   = document.createElement('input');
    junkFlagInput.name  = "junk_flag";
    junkFlagInput.type  = "hidden";
    junkFlagInput.value = theSelect.value;
    myForm.appendChild(junkFlagInput);

    var excludedFlagInput   = document.createElement('input');
    excludedFlagInput.name  = "excluded_flag";
    excludedFlagInput.type  = "hidden";
    var excludedFlagForm = $('which_datasets_form');
    excludedFlagInput.value = excludedFlagForm.which_datasets_rb[0].checked;
    myForm.appendChild(excludedFlagInput);

    document.getElementsByTagName('body').item(0).appendChild(myForm);
    myForm.submit();
}

//---------------------------
// Manage Problem Content
//---------------------------
function searchContentVersions(event) {

    var key = "";
    if (this.id == 'manage_pc_cv_search_by') {
        key = event.keyCode || event.which;
    }
    var testEvent = (key === 13 & this.id == 'manage_pc_cv_search_by')
        | this.id == 'manage_pc_cv_search_button';
    if (testEvent) {
        var searchString = jQuery('#manage_pc_cv_search_by').val();
        if (searchString == jQuery('#manage_pc_cv_search_by').attr('title')) {
            searchString = "";
        }

        pcCVSearchBy(searchString);
    }
    return false;
}
function pcCVSearchByClear() {
    jQuery('#manage_pc_cv_search_by').val("");
    pcCVSearchBy("");
}
function pcCVSearchBy(searchString) {
    jQuery(
           '<form id="searchStringForm" method="post" '
           + 'action="ManageProblemContent"> '
           + '<input name="formRequest" type="hidden" value="true"/> '
           + '<input name="contentVersionSearchBy" type="hidden" value="' + searchString
           + '"/></form>').appendTo('body').submit();
}
function searchDatasets(event) {

    var key = "";
    if (this.id == 'manage_pc_ds_search_by') {
        key = event.keyCode || event.which;
    }
    var testEvent = (key === 13 & this.id == 'manage_pc_ds_search_by')
        | this.id == 'manage_pc_ds_search_button';
    if (testEvent) {
        var searchString = jQuery('#manage_pc_ds_search_by').val();
        if (searchString == jQuery('#manage_pc_ds_search_by').attr('title')) {
            searchString = "";
        }

        pcDSSearchBy(searchString);
    }
    return false;
}
function pcDSSearchByClear() {
    jQuery('#manage_pc_ds_search_by').val("");
    pcDSSearchBy("");
}
function pcDSSearchBy(searchString) {
    jQuery(
           '<form id="searchStringForm" method="post" '
           + 'action="ManageProblemContent"> '
           + '<input name="formRequest" type="hidden" value="true"/> '
           + '<input name="datasetSearchBy" type="hidden" value="' + searchString
           + '"/></form>').appendTo('body').submit();
}
function sortConversions(sortByColumn) {
    jQuery(
           '<form id="sortByForm" method="post" '
           + 'action="ManageProblemContent"> '
           + '<input name="formRequest" type="hidden" value="true"/> '
           + '<input name="sortBy" type="hidden" value="' + sortByColumn
           + '"/></form>').appendTo('body').submit();
}
function selectConversionTool() {
    var toolName = jQuery('#manage_pc_tool_select option:selected').val();

    jQuery(
           '<form id="conversionToolForm" method="post" '
           + 'action="ManageProblemContent"> '
           + '<input name="formRequest" type="hidden" value="true"/> '
           + '<input name="conversionTool" type="hidden" value="' + toolName
           + '"/></form>').appendTo('body').submit();
}
function selectMapped(event) {
    var pcOption = jQuery(this).val();
    jQuery(
           '<form id="problemContentForm" method="post" '
           + 'action="ManageProblemContent"> '
           + '<input name="formRequest" type="hidden" value="true"/> '
           + '<input name="problemContent" type="hidden" value="' + pcOption
           + '"/></form>').appendTo('body').submit();
}
function showDeleteAreYouSure(versionId) {
    jQuery('#manage_pc_delete_' + versionId).hide();
    jQuery('#manage_pc_delete_areYouSure_' + versionId).show();
}
function closeAreYouSure(versionId) {
    jQuery('#manage_pc_delete_areYouSure_' + versionId).hide();
    jQuery('#manage_pc_delete_' + versionId).show();
}
function deleteConversion(versionId) {
    jQuery(
           '<form id="deletePcConversionForm" method="post" '
           + 'action="ManageProblemContent"> '
           + '<input name="formRequest" type="hidden" value="true"/> '
           + '<input name="deletePcConversion" type="hidden" value="true">'
           + '<input name="pcConversionId" type="hidden" value="' + versionId
           + '"/></form>').appendTo('body').submit();
}
function showDatasets(versionId) {
    jQuery('#manage_pc_show_datasets_' + versionId).hide();

    if ($('dataset_div_' + versionId).innerHTML.length > 0) {
        jQuery('#manage_pc_dataset_list_' + versionId).show();
    } else {
        requestDatasets(versionId);
    }
}
function hideDatasets(versionId) {
    jQuery('#manage_pc_show_datasets_' + versionId).show();
    jQuery('#manage_pc_dataset_list_' + versionId).hide();
}
function requestDatasets(versionId) {
    new Ajax.Request("ManageProblemContent", {
        parameters: {
            requestingMethod: "Admin.requestDatasets",
            action: "requestDatasets",
            pcConversionId: versionId
        },
        onComplete: handleDatasets,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleDatasets(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        fillInDatasetsDiv(json.versionId, json.datasets);
    } else {
        errorPopup(json.message);
    }
}
function fillInDatasetsDiv(versionId, datasets) {
    // Build the HTML
    var divHtml = '<table id="pc_datasets_table_'
        + versionId
        + '" class="pc_datasets_table">'
        + '<tr>'
        + '<th>Dataset</th>'
        + '<th>Num Problems Mapped</th>'
        + '<th>Status</th>'
        + '</tr>';

    for (i = 0; i < datasets.length; i++) {
        var dsHref = 'DatasetInfo?datasetId=' + datasets[i].datasetId;

        if (i%2 == 0) {
            divHtml += '<tr>';
        } else {
            divHtml += '<tr class="odd">';
        }
        divHtml += '<td><p>';
        divHtml += '<a href="' + dsHref + '">';
        divHtml += datasets[i].datasetName;
        divHtml += '</a></p></td>';

        divHtml += '<td><p>';
        divHtml += '<span>' + datasets[i].numProblemsMapped + '</span>';
        divHtml += '</p></td>';

        divHtml += '<td><p>';
        divHtml += '<span>' + datasets[i].status + '</span>';
        divHtml += '</p></td>';

        divHtml += "</tr>";
    }
    divHtml += "</table>";

    // Set the HTML in the Div
    datasetsDiv = $("dataset_div_" + versionId);
    datasetsDiv.innerHTML = divHtml;

    jQuery('#manage_pc_dataset_list_' + versionId).show();
}

function updateSubmitAndReset() {
    if (jQuery('#admin-reset').attr('disabled') == 'disabled') {
        jQuery('#admin-reset').removeAttr('disabled');
    }

    // Ensure user/password specified if useSslSmtp is true.
    if (jQuery('#use-ssl-smtp-input').attr('checked') == 'checked') {
        var user = jQuery('#smtp-user-input').val();
        var password = jQuery('#smtp-password-input').val();
        if ((user.length > 0) && (password.length > 0)) {
            jQuery('#admin-submit').removeAttr('disabled');
            jQuery('#smtp-user-label').css('color', '');
            jQuery('#smtp-password-label').css('color', '');
        } else {
            jQuery('#admin-submit').attr('disabled', 'disabled');
            var color = (user.length == 0) ? 'red' : '';
            jQuery('#smtp-user-label').css('color', color);
            color = (password.length == 0) ? 'red' : '';
            jQuery('#smtp-password-label').css('color', color);
        }
    } else {
        jQuery('#admin-submit').removeAttr('disabled');
    }
}

function resetForm() {
    jQuery('#InstanceForm')[0].reset();

    // Update instance.properties per 'isSlave' or 'useSsl' attrs.
    updateInstanceProps();
    updateSslSmtpProps();

    jQuery('#admin-submit').attr('disabled', 'disabled');
    jQuery('#admin-reset').attr('disabled', 'disabled');
}

function updateInstanceProps() {
    if (jQuery('#is-slave-input').attr('checked') == 'checked') {
        jQuery('#slave-id-input').removeAttr('disabled');
        jQuery('#master-user-input').removeAttr('disabled');
        jQuery('#master-url-input').removeAttr('disabled');
        jQuery('#master-schema-input').removeAttr('disabled');
        jQuery('#slave-apitoken-input').removeAttr('disabled');
        jQuery('#slave-secret-input').removeAttr('disabled');
    } else {
        jQuery('#slave-id-input').attr('disabled', 'disabled');
        jQuery('#master-user-input').attr('disabled', 'disabled');
        jQuery('#master-url-input').attr('disabled', 'disabled');
        jQuery('#master-schema-input').attr('disabled', 'disabled');
        jQuery('#slave-apitoken-input').attr('disabled', 'disabled');
        jQuery('#slave-secret-input').attr('disabled', 'disabled');
    }
}

function updateSslSmtpProps() {
    if (jQuery('#use-ssl-smtp-input').attr('checked') == 'checked') {
        jQuery('#smtp-user-row').removeAttr('style');
        jQuery('#smtp-password-row').removeAttr('style');
        jQuery('#smtp-user-input').removeAttr('disabled');
        jQuery('#smtp-password-input').removeAttr('disabled');
    } else {
        jQuery('#smtp-user-row').attr('style', 'display:none');
        jQuery('#smtp-password-row').attr('style', 'display:none');
        jQuery('#smtp-user-input').attr('disabled', 'disabled');
        jQuery('#smtp-password-input').attr('disabled', 'disabled');
    }
}

function isSlaveChanged() {
    updateInstanceProps();
    updateSubmitAndReset();
}

function useSslSmtpChanged() {
    updateSslSmtpProps();
    updateSubmitAndReset();
}
