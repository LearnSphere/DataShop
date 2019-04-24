//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2014
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 11262 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2014-06-25 14:21:23 -0400 (Wed, 25 Jun 2014) $
// $KeyWordsOff: $
//

/**
 * Build and submit a form to go to the Dataset Info -> Problem List page for the given dataset.
 */
function goToDatasetProblemList(datasetId) {
    if (datasetId != null) {
        var servletName = "DatasetInfo"
        var newForm = document.createElement('FORM');
        newForm.setAttribute('name',   'KCModels_details_link');
        newForm.setAttribute('id',     'KCModels_details_link');
        newForm.setAttribute('form',   'text/plain');
        newForm.setAttribute('action', servletName + "?datasetId=" + datasetId);
        newForm.setAttribute('method', 'POST');

        var hiddenInput = document.createElement('input');
        hiddenInput.name  = "subtab";
        hiddenInput.type  = "hidden";
        hiddenInput.value = "problemList";
        newForm.appendChild(hiddenInput);

        document.getElementsByTagName('body').item(0).appendChild(newForm);
        newForm.submit();
    }
}