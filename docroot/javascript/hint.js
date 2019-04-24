/**
 * Carnegie Mellon University, Human-Computer Interaction Institute Copyright
 * 2012 All Rights Reserved
 */

// Allows the user of hints for text field inputs
// (taken from http://stackoverflow.com/users/240215/val)
jQuery.fn.hint = function(blurClass) {
    return this.focus(function() {
        if (this.value == this.title) {
            jQuery(this).val("").removeClass(blurClass);
        }
    }).blur(function() {
        if (!this.value.length) {
            jQuery(this).val(this.title).addClass(blurClass);
        }
    }).blur();
};
