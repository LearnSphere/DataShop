 
/** @class IdP Selector UI */
function IdPSelectUIParms(){

    // Read value of DataShop URL in order to correctly set the defaultReturn param.
    var urlInput = $("ds-instance-url");
    var datashopUrl = "http://localhost:8080";
    if (urlInput) {
        datashopUrl = urlInput.value.toLowerCase();
    }

    // Need to strip any trailing bits from the URL, e.g., '/datashop' on QA.
    var index = datashopUrl.lastIndexOf('/');
    var shibHost = datashopUrl.substring(0, index);
    // But if there are no trailing bits, we've lost the URL...
    if ((shibHost == 'https:/') || (shibHost == 'http:/')) {
        shibHost = datashopUrl;
    }

    // With DataLab, it's DataLabSSO instead of webiso. Similarly for LS->Workflows
    var ssoUrl = "/webiso";
    var isDatalab = $('is-datalab');
    var isWorkflows = $('is-workflows');
    if (isDatalab) {
        ssoUrl = "/DataLabSSO";
    } else if (isWorkflows) {
	ssoUrl = "/WorkflowsSSO";
    }

    //
    // Adjust the following to fit into your local configuration
    //
    this.alwaysShow = true;          // If true, this will show results as soon as you start typing
    this.dataSource = '/Shibboleth.sso/DiscoFeed';   // Where to get the data from
    this.defaultLanguage = 'en';     // Language to use if the browser local doesnt have a bundle
    this.defaultLogo = 'images/image-not-found.jpg';  // Replace with your own logo
    this.defaultLogoWidth = 55;
    this.defaultLogoHeight = 55 ;
    this.defaultReturn =
        shibHost + "/Shibboleth.sso/Login?SAMLDS=1&target=" + datashopUrl + ssoUrl;
                                     // If non null, then the default place to send users who are 
                                     // not approaching via the Discovery Protocol for example
    //this.defaultReturn = "https://example.org/Shibboleth.sso/DS?SAMLDS=1&target=https://example.org/secure";
    this.defaultReturnIDParam = null;
    this.helpURL = datashopUrl + '/help?page=access';
    this.ie6Hack = null;             // An array of structures to disable when drawing the pull down (needed to 
                                     // handle the ie6 z axis problem
    this.insertAtDiv = 'idpSelect';  // The div where we will insert the data
    this.maxResults = 10;            // How many results to show at once or the number at which to
                                     // start showing if alwaysShow is false
    this.myEntityID = null;          // If non null then this string must match the string provided in the DS parms
    this.preferredIdP = new Array('https://login.cmu.edu/idp/shibboleth',
                                  'https://sso.memphis.edu/idp/shibboleth',
                                  'urn:mace:incommon:stanford.edu',
                                  'urn:mace:incommon:mit.edu');        // Array of entityIds to always show
    this.hiddenIdPs = null;          // Array of entityIds to delete
    this.ignoreKeywords = false;     // Do we ignore the <mdui:Keywords/> when looking for candidates
    this.ignoreURLParams = true;
    this.showListFirst = false;      // Do we start with a list of IdPs or just the dropdown
    this.samlIdPCookieTTL = 730;     // in days
    this.setFocusTextBox = true;     // Set to false to supress focus 
    this.testGUI = false;


    //
    // Language support. 
    //
    // The minified source provides "en", "de", "pt-br" and "jp".  
    //
    // Override any of these below, or provide your own language
    //
    //this.langBundles = {
    //'en': {
    //    'fatal.divMissing': '<div> specified  as "insertAtDiv" could not be located in the HTML',
    //    'fatal.noXMLHttpRequest': 'Browser does not support XMLHttpRequest, unable to load IdP selection data',
    //    'fatal.wrongProtocol' : 'Policy supplied to DS was not "urn:oasis:names:tc:SAML:profiles:SSO:idpdiscovery-protocol:single"',
    //    'fatal.wrongEntityId' : 'entityId supplied by SP did not match configuration',
    //    'fatal.noData' : 'Metadata download returned no data',
    //    'fatal.loadFailed': 'Failed to download metadata from ',
    //    'fatal.noparms' : 'No parameters to discovery session and no defaultReturn parameter configured',
    //    'fatal.noReturnURL' : "No URL return parameter provided",
    //    'fatal.badProtocol' : "Return request must start with https:// or http://",
    //    'idpPreferred.label': 'Use a suggested selection:',
    //    'idpEntry.label': 'Or enter your organization\'s name',
    //    'idpEntry.NoPreferred.label': 'Enter your organization\'s name',
    //    'idpList.label': 'Or select your organization from the list below',
    //    'idpList.NoPreferred.label': 'Select your organization from the list below',
    //    'idpList.defaultOptionLabel': 'Please select your organization...',
    //    'idpList.showList' : 'Allow me to pick from a list',
    //    'idpList.showSearch' : 'Allow me to specify the site',
    //    'submitButton.label': 'Continue',
    //    'helpText': 'Help',
    //    'defaultLogoAlt' : 'DefaultLogo'
    //}
    //};

    //
    // The following should not be changed without changes to the css.  Consider them as mandatory defaults
    //
    this.maxPreferredIdPs = 5;
    this.maxIdPCharsButton = 33;
    this.maxIdPCharsDropDown = 58;
    this.maxIdPCharsAltTxt = 60;

    this.minWidth = 20;
    this.minHeight = 20;
    this.maxWidth = 115;
    this.maxHeight = 69;
    this.bestRatio = Math.log(80 / 60);

    //
    // Local preferred IdPs
    //
    this.localIdPs = JSON.parse('['
                                + '{ "entityID": "https://login.cmu.edu/idp/shibboleth",'
                                + '"DisplayNames": ['
                                + '{ "value": "Carnegie Mellon University",'
                                + '  "lang": "en"}],'
                                + '"Logos": ['
                                + '{ "value": "https://identity.andrew.cmu.edu/incommon/cmu-181x125.gif",'
                                + '  "height": "125",'
                                + '  "width": "181",'
                                + '  "lang": "en"}]},'
                                + '{ "entityID": "https://sso.memphis.edu/idp/shibboleth",'
                                + '"DisplayNames": ['
                                + '{ "value": "The University of Memphis",'
                                + '  "lang": "en"}],'
                                + '"Logos": ['
                                + '{ "value": "https://sso.memphis.edu/idp/images/uofmlogo.png",'
                                + '  "height": "118",'
                                + '  "width": "350",'
                                + '  "lang": "en"}]},'
                                + '{ "entityID": "urn:mace:incommon:stanford.edu",'
                                + '"DisplayNames": ['
                                + '{ "value": "Stanford University",'
                                + '  "lang": "en"}],'
                                + '"Logos": ['
                                + '{ "value": "images/stanford.png",'
                                + '  "height": "60",'
                                + '  "width": "80",'
                                + '  "lang": "en"}]},'
                                + '{ "entityID": "urn:mace:incommon:mit.edu",'
                                + '"DisplayNames": ['
                                + '{ "value": "Massachusetts Institute of Technology",'
                                + '  "lang": "en"}],'
                                + '"Logos": ['
                                + '{ "value": "images/mit.png",'
                                + '  "height": "55",'
                                + '  "width": "55",'
                                + '  "lang": "en"}]}'
                                + ']');
}
