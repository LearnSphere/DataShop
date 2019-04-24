//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2008
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 9209 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2013-04-25 10:04:52 -0400 (Thu, 25 Apr 2013) $
// $KeyWordsOff: $
//

/** 
 * File uploader that uses a hidden iFrame that takes the results of the upload
 * and hides the actual post from the page making it feel like an AJAX upload,
 * which does not require a full page reload.
 *
 * DEPENDENCIES: prototype v1.6
 */
var FileUploader = Class.create();
FileUploader.prototype = {

    /**
     * Constructor
     * @param form (Required) - the id of the upload form
     * @param options (Optional) - additional options for the menu.
     *   > onStart - function to call when the upload starts
     *   > onComplete - function to call when the upload finishes, works in the format
     *          onComplete(results) where the results are the server return after the 
     *          upload completes which get placed in the iFrame.
     */
    initialize: function(id, options) {
        this.form = $(id);
        
        //convenience holder for the iFrame DOM object.
        this.iFrame = false;
          
        //update any default values if there are some.
        this.options = {
            onStart: false,
            onComplete: false
        };
        Object.extend(this.options, options || { });

        this.createFrame();
        
        if (typeof(this.options.onStart) == 'function') {
            this.options.onStart();
        }
        
    },
    
    /**
     * Creates the iFrame and submits the form after
     * setting the target of the form to the frame.
     */
    createFrame: function() {
        //get a random number for the IFrame ID
        var iFrameId = 'u_l_f_' + Math.floor(Math.random() * 99999);

        //create the iFrame
        var iframeHTML = '<iframe style="display:none" src="about:blank" id="' + iFrameId + '"'
            + ' name="' + iFrameId + '"></iframe>';

        $(document.body).insert(iframeHTML);        
        this.iFrame = $(iFrameId);

        //set the target of the form as the iFrameId
        this.form.setAttribute('target', iFrameId);

        //final add an observer so we know when it's done loading.
        this.iFrame.observe('load', this.loaded.bindAsEventListener(this));

        this.form.submit();
    },

    /**
     * Function called once the upload completes and the results are loaded to
     * the iFrame window.
     */
    loaded : function() {

        //get the IFrame contents for each browser type.
        if (this.iFrame.contentDocument) {
            var doc = this.iFrame.contentDocument;
        } else if (this.iFrame.contentWindow) {
            var doc = this.iFrame.contentWindow.document;
        } else {
            var doc = window.frames[iframeId].document;
        }

        if (typeof(this.options.onComplete) == 'function') {
            this.options.onComplete(doc.body.innerHTML);
        }
    }
};
