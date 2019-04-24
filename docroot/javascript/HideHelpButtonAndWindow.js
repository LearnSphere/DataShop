//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2008
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 7680 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2012-04-26 10:14:33 -0400 (Thu, 26 Apr 2012) $
// $KeyWordsOff: $
//

/**
 * Help Page initializer.
 * Close the help window (if its open) and remove the help button.
 */
function initHelpPage() {
    theHelpWindow.closeHelpWindow();
    theHelpWindow.hideHelpButton();
}

// Add an onload listener to initialize this report.
onloadObserver.addListener(initHelpPage);
