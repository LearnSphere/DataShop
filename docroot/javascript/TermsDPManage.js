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

Event.observe(window, 'load',
    function(){
        Event.observe($("selectTou"), 'change', addSelectHandler);
    }
);

function addSelectHandler() {
    var mySelect = $("selectTou");
    var manageButton = $("manage_terms_button");

    <!-- Edit button  -->
    if (mySelect.value == '') {
        manageButton.disabled = true;
    }
    else {
        manageButton.disabled = false;
    }
}
