<%@ include file="/header-help.jspf"%>

    <h2>Importing New Data</h2>

    <ul class="concise">
        <li><a href="#upload-process">Upload process</a>
        <li><a href="#new-project">Creating a new project</a></li>
        <li><a href="#import-process">Import process</a></li>
    </ul>


    <p>In addition to logging data directly to the DataShop logging database, you can import data to
    create a new dataset in DataShop. To begin the import process, upload a new dataset from the 
    <a href="UploadDataset">Upload a dataset</a> page. If you've never uploaded a dataset before, DataShop 
    will prompt you to request permission to do so.</p>

    <p>You can upload a file in either of the following formats:</p>
    
    <div id="format_buttons_div" class="clearfix">
        <div id="td_format_button_div" class="formatButton">
            <a id="td_format_button" type="submit" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only" 
            role="button" aria-disabled="false" href="help?page=importFormatTd"><span class="ui-button-text">Import file format (tab-delimited)</span></a>
        </div>
        
        <div id="xml_format_button_div" class="formatButton">
            <a id="xml_format_button" type="submit" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only" 
            role="button" aria-disabled="false" href="help?page=importFormatXml"><span class="ui-button-text">Import file format (XML)</span></a>
        </div>
    </div>
    
    <p>Before uploading your file(s), you should verify that it meets the format requirements.</p>

    <h3 id="upload-process">Upload process</h3>
    
    <ol>
        <li>Specify the project that should contain this new dataset. You can choose an existing
            project, <a href="CreateProject">specify a new one</a>, or choose one later.
        </li>
        <li>Name your dataset. The name must be unique amongst datasets that have been loaded
            already. You can change the dataset name later.</li>
        <li>(Optional) Add transaction data&mdash;see <a href="#import-process">Import Process</a> below.</li>
    </ol>
    
    <h4 id="transaction-data">Transaction data</h4>

    <p>
        On the upload page, you will be asked to specify whether you want to upload <strong><a
            href="help?page=terms#transaction">transaction data</a></strong>. Transaction data is data in either
        of the above two formats. If you want to create a dataset that will hold file attachments (of
        any format), or if you want to create the dataset as a placeholder and add transaction data
        later, choose <strong>No transaction data now</strong>.
    </p>

    <h4 id="de-identify-reqs">De-identification requirements</h4>
    
    <p>Data uploaded to DataShop must be de-identified. That is, the identity of human subjects
        referenced in the data must not be discoverable.</p>
    
    <p>If your file is entirely de-identified, choose the first option, <strong>I certify that all data in
        this file including the content of the "Anon Student Id" column is de-identified</strong>.</p>
    
    <p>
        If your file is de-identified <em>except</em> for the identifiers present in the Anon Student Id
        column, select the second option, <strong>I certify that all data in this file except the content of
        the "Anon Student Id" column is de-identified.</strong> DataShop will de-identify that column for you,
        substituting the identifiers in that column with anonymous ones. (You can later obtain a mapping
        from DataShop identifiers to the original identifiers by <a href="help?page=contact">emailing us</a>.)
    </p>
    
    <h3 id="new-project">Creating a new project</h3>
    
    <p>
        A project is primarily a container for a group of related datasets. In addition, access to
        datasets is granted by project. You can create a new project from the upload page or the <a
            href="CreateProject">Create a project page</a>. When specifying a new project, you will be
        asked to specify a data collection type. Those options are described on our <a
            href="help?page=irb">IRB page</a>.
    </p>
    
    <h3 id="import-process">Import process</h3>
    
    <p>The import process is as follows:</p>

    <ol>
        <li><strong>Upload</strong> one or more files (as a .ZIP file) to be imported as a dataset.</li>
        <li>DataShop will perform a quick verification of the file's first 100 lines and display the results.* 
        You will need to correct any errors that are found. If any potential issues are found, you will be asked 
        to decide if you want to continue.</li>
        <li>After the initial verification completes, the dataset will appear in your <a
            href="index.jsp?datasets=mine">Import Queue</a> as <strong>Queued for Verification</strong>,
        where a separate process will verify the dataset in its entirety.*</li>
        <li>When verification is complete, you will receive an email with the verification results. The status 
        for your dataset will update in your <a href="index.jsp?datasets=mine">Import Queue</a>. 
        When your dataset is loaded, you will be notified via email.</li>
        <li>After your dataset is loaded, we ask that you examine the dataset and then <strong>release it</strong>. 
        When you release a dataset, it inherits the permissions of its project (those who can access the project can then access
            this dataset) and becomes visible in the main index of datasets.</li>
    </ol>
    
    <p>* Tab-delimited files only. XML files are verified by DataShop staff.</p>
    
    <!-- <h3 id="formats">Import file formats</h3> 
    
    <h4 id="tab-delim">DataShop tab-delimited format</h4>

    <h4 id="xml">DataShop Tutor Message XML format</h4>-->


