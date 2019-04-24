<%@ include file="/header-help.jspf"%>

    <h2>XML format</h2>

    <p>You can import data as XML text files using the <a href="UploadDataset">Upload Dataset page</a>. 
    You should first verify that the XML files you've created are
    well-formed and conform to the <a href="/dtd/guide/index.html">Tutor Message format</a> 
    version 2 or 4. The Tutor message format is a standardized format for logging student-tutor interaction data. 
    Validating your XML against this format ensures that your data can be properly stored and interpreted by DataShop.</p>
    
    <p>DataShop provides a <a class="ulink"
            href="/about/xmlvalidator.html" target="_top">command-line validation tool</a> for
        validating Tutor Message XML. We strongly encourage you to use this tool for validation.</p>

    <p>After running the tool and fixing any errors it reports, upload your file as described 
    in the <a href="help?page=import#import-process">Importing New Data page</a>.</p>
    <div style="clear:left"></div>
