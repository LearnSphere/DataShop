function IdPSelectRemote() {

    var idpData;

    this.loadDataSource = function(parms) {

        remoteDataSource = parms.dataSource;

        if (!load(remoteDataSource)) {
            return;
        }
        
        idpSelectUI.updateIdPEntryAndDropDownListTiles(idpData);
    };

    /**
       Loads the data used by the IdP selection UI.  Data is loaded 
       from a JSON document fetched from the given url.
      
       @param {Function} failureCallback A function called if the JSON
       document can not be loaded from the source.  This function will
       passed the {@link XMLHttpRequest} used to request the JSON data.
    */
    var load = function(dataSource){
        var xhr = null;

        try {
            xhr = new XMLHttpRequest();
        } catch (e) {}
        if (null == xhr) {
            //
            // EDS24. try to get 'Microsoft.XMLHTTP'
            //
            try {
                xhr = new ActiveXObject("Microsoft.XMLHTTP");
            } catch (e) {}
        }
        if (null == xhr) {
            //
            // EDS35. try to get 'Microsoft.XMLHTTP'
            //
            try {
                xhr = new  ActiveXObject('MSXML2.XMLHTTP.3.0');
            } catch (e) {}
        }
        if (null == xhr) {
            fatal(getLocalizedMessage('fatal.noXMLHttpRequest'));
            return false;
        }

        if (isIE()) {
            //
            // cache bust (for IE)
            //
            dataSource += '?random=' + (Math.random()*1000000);
        }

        //
        // Grab the data
        //
        xhr.open('GET', dataSource, false);
        if (typeof xhr.overrideMimeType == 'function') {
            xhr.overrideMimeType('application/json');
        }
        xhr.send(null);
        
        if(xhr.status == 200){
            //
            // 200 means we got it OK from as web source
            // if locally loading its 0.  Go figure
            //
            var jsonData = xhr.responseText;
            if ((jsonData === null) || (jsonData == "")) {
                return false;
            }

            //
            // Parse it
            //

            idpData = JSON.parse(jsonData);

        } else if(xhr.status == 500) {
            // Sigh. Call failed and we can't use AccessFilter to block it
            // when running with Apache (like we can with Tomcat). Why?
            return false;
        }else{
            fatal(getLocalizedMessage('fatal.loadFailed') + dataSource);
            return false;
        }
        return true;
    };

    /**
     * We need to cache bust on IE.  So how do we know?  Use a bigger hammer.
     */
    var isIE = function() {
        if (null == navigator) {
            return false;
        }
        var browserName = navigator.appName;
        if (null == browserName) {
            return false;
        }
        return (browserName == 'Microsoft Internet Explorer') ;
    } ;
}
(new IdPSelectRemote()).loadDataSource(new IdPSelectUIParms());
