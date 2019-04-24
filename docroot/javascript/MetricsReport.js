//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2016
// All Rights Reserved
//
// Author: Cindy Tipper
// $KeyWordsOff: $
//

/** Initialize all javascript items necessary for the Metrics Report */
function initMetricsReport() {

    var options = new Array();
    options['extraClasses'] = 'infoDiv';
    options['timeout'] = '100000';
    options['delay'] = '100';
    options['extraClasses'] = 'lcNavToolTip';

    // LearnLab tooltips
    learnlabUnspecifiedTooltipContent = $('learnlab_unspecified_tooltip_content').innerHTML;
    learnlabOtherTooltipContent = $('learnlab_other_tooltip_content').innerHTML;

    jQuery('.learnlab_unspecified_tooltip').each(function(i, obj) {
            var spanId = jQuery(this).attr('id');
            new ToolTip(spanId, learnlabUnspecifiedTooltipContent, options);
        });
    jQuery('.learnlab_other_tooltip').each(function(i, obj) {
            var spanId = jQuery(this).attr('id');
            new ToolTip(spanId, learnlabOtherTooltipContent, options);
        });


    // Domain tooltips
    domainUnspecifiedTooltipContent = $('domain_unspecified_tooltip_content').innerHTML;
    domainOtherTooltipContent = $('domain_other_tooltip_content').innerHTML;

    jQuery('.domain_unspecified_tooltip').each(function(i, obj) {
            var spanId = jQuery(this).attr('id');
            new ToolTip(spanId, domainUnspecifiedTooltipContent, options);
        });
    jQuery('.domain_other_tooltip').each(function(i, obj) {
            var spanId = jQuery(this).attr('id');
            new ToolTip(spanId, domainOtherTooltipContent, options);
        });
}

onloadObserver.addListener(initMetricsReport);
