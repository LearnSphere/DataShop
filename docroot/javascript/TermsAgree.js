//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2011
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 7422 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2011-12-16 14:35:13 -0500 (Fri, 16 Dec 2011) $
// $KeyWordsOff: $
//

//
// Enable the continue button.
//
var myLoadedText = "";
Event.observe(window, 'load',
    function(){
        Event.observe($("agreeCheckbox"), 'click', enableContinueButton);
    }
);

function enableContinueButton() {
    var agreeCheckbox = $("agreeCheckbox");
    if (agreeCheckbox) {
        var continueButton = $("continueButton");
        if (continueButton) {
            if (agreeCheckbox.checked) {
                continueButton.disabled = false;
            } else {
                continueButton.disabled = true;
            }
        }
    }
}
