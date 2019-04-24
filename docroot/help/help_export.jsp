<%// Author: Brett Leber
            // Version: $Revision: 12844 $
            // Last modified by: $Author: ctipper $
            // Last modified on: $Date: 2015-12-21 10:18:16 -0500 (Mon, 21 Dec 2015) $
            // $KeyWordsOff: $
            //

            %>

    <h2>Export: Getting Data Out</h2>

    <p>DataShop's Export function allows you to save your log data out of DataShop and into an
    anonymous, tab-delimited text file. As in the other DataShop reporting tools, the sample
    selector allows you to filter rows based on criteria you define, and apply your own knowledge
    component model to the data before exporting.</p>

        <ul class="concise">
            <li><a href="#exporting">Exporting data</a></li>
            <li><a href="#viewing">Viewing exported data</a></li>
            <li><a href="#descriptions">Column descriptions</a>
                <ul>
                <li><a href="#table-by-tx">By Transaction</a></li>
                <li><a href="#table-by-student-step">By Student-Step</a></li>
                <li><a href="#table-by-student-problem">By Student-Problem</a></li>
                </ul>
            </li>
            <li><a href="#about-data-anonymization">About data anonymization</a></li>
            <li><a href="#ie7-export-download">Export tips for Internet Explorer</a></li>
        </ul>

    <h3 id="exporting">Exporting data</h3>
    <ol>
            <li>Select samples to include in your data by clicking the name of the sample(s) in the
            sidebar. (Bolded sample names are included.) The Export preview will update to reflect the
            changes you've made. If you select more than one sample to export, transactions/steps/problems
            that occur in more than one sample will be duplicated in the export. You can identify them with
            the "Sample" column.</li>
            <li>Examine the "Export File Status" table above the <strong>Export</strong> button to see 1)
            how fast the export process will be, and 2) to gauge the age of the cached export file, if
            one exists. <br />
            - <img src="images/tick.png" alt="Checkmark Icon" class="icon" /> means the sample is cached and
            up-to-date. DataShop will prepare the file for download quickly.<br />
            - <img src="images/hourglass.png" alt="Hourglass Icon" class="icon" /> means the
            sample is not cached, so exporting may take considerably longer.<br />
            - <img src="images/alert.gif" alt="Alert Icon" class="icon" /> means that although the sample is cached,
             either recent KC models or logged data are not yet included in the sample. To find out which,
             hover your mouse cursor over the icon. This sample should be up-to-date within 24 hours.<br />
             <strong>Including at least one sample that is not up-to-date means the export will take longer.</strong><br />
             <strong>Note:</strong> If a sample is not cached, the order of rows in the export preview table may not
             match the order that you will see in the export file (i.e., rows ordered by session, then time).
            </li>
            <li>(Export by Student-Step or Student-Problem only) Select the desired knowledge component model from the knowledge component model
            combobox. The Export Preview will update to display a table with the chosen knowledge component
            model applied.</li>
            <li>Click the <strong>Export</strong> button. When the export process is complete, you will
            be prompted to save a zip file
            containing a folder hierarchy of one or more text files, each containing a tab-delimited export. <br />
            <strong>Note: </strong>The naming convention for the top-level zip file is: the dataset ID (a number visible in the URL
            of each DataShop webpage that uniquely identifies the dataset), the export type, and a timestamp. For exports that are specific to a KC model, the KC model
            ID is also included in the file name. Within the exported zip file, a text file is included for each
            selected sample. The naming convention for these files is: the sample name, sample ID, and the time stamp from when the
            data was cached.</li>
        </ol>

    <h3 id="viewing">Viewing exported data</h3>
    <p>You can now view and edit your file in a text or spreadsheet editor.</p>

    <p><strong>Important:</strong> If you save your data from a spreadsheet editor and would like to
    import the data into DataShop, be sure to preserve the tab-delimited text format of the file.
    </p>

    <div class="warning" id="excel_warning">
    <h3>Microsoft Excel users: potential data loss when opening and saving exported data in Excel
    </h3>
    <p>Newer versions of Microsoft Excel tend to automatically format date/time fields for you. The
    result of this is that timestamp values are presented in a format that <strong>may obscure
    levels of detail in the time data</strong>. If you then save the file from Excel, <strong>you
    will lose this information!</strong></p>
    <h4>To work around this issue and safely view your data in Microsoft Excel:</h4>
    <ol>
        <li>Launch Microsoft Excel.</li>
        <li>Select <strong>File > Open</strong>. <strong>Do not double-click your data file or drag
        it into Excel as you won't be able to complete the following steps.</strong></li>
        <li>Browse for your exported data file and click <strong>Open</strong>. A <em>Text
        Import Wizard</em> will appear.</li>
        <li>On screen 1 of 3 of the <em>Text Import Wizard</em>, ensure that
        <strong>Delimited</strong> is selected as the <i>Original Data Type</i>. Click
        <strong>Next</strong>.</li>
        <li>On screen 2 of 3 of the <em>Text Import Wizard</em>, ensure that <strong>Tab</strong> is
        selected as the only delimiter, and click <strong>Next</strong>.</li>
        <li>On screen 3 of 3 of the <em>Text Import Wizard</em>, select all columns (click the first column,
        hold SHIFT, and click the last column). With all columns selected, change the <em>Column data format</em>
        to <strong>Text</strong>. The preview should update so that the header <strong>Text</strong> appears over each.</li>
        <li>Click <strong>Finish</strong>. You can now view your data in Excel, as Excel now knows
        not to automatically format any columns.</li>
    </ol>
    </div>

        <h3 id="descriptions">Column descriptions</h3>
        <p>Columns of the current export formats are described below.</p>

        <p><strong>Note:</strong> The list and order of columns in any of the export formats can change at any time.
        If you are writing a program that expects the columns in a certain order, be sure to verify the header of the column
        before assuming it's the column you expect.</p>

        <p>See the <a href="help?page=exportFormatHistory">history of changes</a> to these formats.</p>

        <h3 id="table-by-tx">By Transaction</h3>
        <p>Within each sample, rows are ordered by student then by transaction time. If the transaction time is identical for a given student,
        we can't know the real order in which the transactions occurred, so DataShop uses internal database identifiers to
        order the rows consistently.</p>
        <table>
            <colgroup>
                <col style="width:20%" />
                <col style="width:80%" />
            </colgroup>
            <thead>
                <tr>
                    <th>Column</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <th>Row</th>
                    <td>A row counter</td>
                </tr>
                <tr>
                    <th>Sample Name</th>
                    <td>The sample that contains the transaction. If a transaction appears in multiple samples,
                    the transaction will be repeated, but with a different sample name.</td>
                </tr>
                <tr>
                    <th>Transaction Id</th>
                    <td>A unique ID that identifies the transaction. Currently used for annotating transactions with
                    <a href="help?page=customFields">custom fields</a> via
                    <a href="/about/webservices.html">web services</a>.</td>
                </tr>
                <tr>
                    <th>Anon Student Id</th>
                    <td>DataShop-generated anonymous student ID. To obtain original student identifiers or to
                    learn more about data anonymization, see <a href="#about-data-anonymization">About data anonymization</a>
                    below.</td>
                </tr>
                <tr>
                    <th>Session Id</th>
                    <td>A dataset-unique string that identifies the user's session with the tutor.</td>
                </tr>
                <tr>
                    <th>Time</th>
                    <td>Time the transaction occurred. For instance, if a student types "25" and presses return, the 
                    transaction time is at the point in which they press return.</td>
                </tr>
                <tr>
                    <th>Time Zone</th>
                    <td>The local time zone (e.g., EST, PST, US/Eastern).</td>
                </tr>
                <tr>
                    <th>Duration (sec)</th>
                    <td>Duration of the transaction in seconds. This is the time of the current transaction minus
                    that of the preceding transaction or problem start event&mdash;whichever is closer in time to the
                    current transaction. If this difference is greater than 10 minutes, or if the prior transaction occurred
                    during a different user session, DataShop reports the duration as null
                    (a dot). If the current transaction is preceded by neither another transaction or a problem start event,
                    duration is shown as null. The duration is formatted without decimal places if the two times used
                    in the calculation were without millisecond precision.</td>
                <tr>
                    <th>Student Response Type</th>
                    <td>The type of attempt made by the student (e.g., "ATTEMPT" or "HINT_REQUEST").
                    This is logged in the <a href="/dtd/guide/tool_message.html#element.semantic_event">
                    semantic_event</a> element.</td>
                </tr>
                <tr>
                    <th>Student Response Subtype</th>
                    <td>A more detailed classification of the student attempt. For example,
                    the CTAT software describes actions taken by the tutor on behalf of the student
                    as having subtype "tutor-performed".</td>
                </tr>
                <tr>
                    <th>Tutor Response Type</th>
                    <td>The type of response made by the tutor (e.g., "RESULT" or "HINT_MSG").</td>
                </tr>
                <tr>
                    <th>Tutor Response Subtype</th>
                    <td>A more detailed classification of the tutor response.</td>
                </tr>
                <tr>
                    <th>Level (level_type)</th>
                    <td>The problem hierarchy name (e.g., "Understanding Fractions") of the type
                    specified in the column header (e.g., "Unit"). There may be multiple "Level" columns if
                    the problem hierarchy is more than one level deep. Level is logged in
                    the <a href="/dtd/guide/context_message.html#element.dataset.level">
                    level</a> element.</td>
                </tr>
                <tr>
                    <th>Problem Name</th>
                    <td>The name of the problem. Two problems with the same "Problem Name" are
                    considered different "problems" by DataShop if the following logged values are not
                    identical: problem name, context, tutor_flag (whether or not the problem
                    or activity is tutored) and "other" field. These fields are logged in the
                    <a href="/dtd/guide/context_message.html#element.dataset">
                    problem</a> element.</td>
                </tr>
                <tr>
                    <th>Problem View</th>
                    <td>The number of times the student encountered the problem so far. This counter increases
                    with each instance of the same problem. See "Problem View" in the "By Student-Step" table below.</td>
                </tr>
                <tr>
                    <th>Problem Start Time</th>
                    <td>If the problem start time is not given in the original log data, then it is set to the time
                    of the last transaction of the prior problem. If there is no prior problem for the session,
                    the time of the earliest transaction is used. Earliest transaction time is equivalent
                    to the minimum transaction time for the earliest step of the problem. For more detail
                    on how problem start time is determined, see <a href="help?page=export#determining_pst">Determining
                    Problem Start Time</a>.</td>
                </tr>
                <tr>
                    <th>Step Name</th>
                    <td>Formed by concatenating the "selection" and "action". Also see the
                    <a href="help?page=terms#step">glossary entry for "step"</a>.</td>
                </tr>
                <tr>
                    <th>Attempt at Step</th>
                    <td>As of this transaction, the current number of attempts toward the identified step.</td>
                </tr>
                <tr>
                    <th>Outcome</th>
                    <td>The tutor's evaluation of the student's attempt. For example, "CORRECT", "INCORRECT", or "HINT".
                    This is logged in the <a href="/dtd/guide/tutor_message.html#element.action_evaluation">
                    action_evaluation</a> element.</td>
                </tr>
                <tr>
                    <th>Selection</th>
                    <td>A description of the interface element(s) that the student selected or
                    interacted with (for example, "LowestCommonDenominatorCell"). This is logged in the
                    <a href="/dtd/guide/tool_message.html#element.event_descriptor">event_descriptor</a>
                    element.</td>
                </tr>
                <tr>
                    <th>Action</th>
                    <td>A description of the manipulation applied to the selection.</td>
                </tr>
                <tr>
                    <th>Input</th>
                    <td>The input the student submitted (e.g., the text entered, the text of a menu item or a combobox entry).</td>
                </tr>
                <tr>
                    <th>Feedback Text</th>
                    <td>The body of a hint, success, or incorrect action message shown to the student.
                    It is generally a text value, logged in the <a
                        href="/dtd/guide/tutor_message.html#element.tutor_advice">
                    tutor_advice</a> element.</td>
                </tr>
                <tr>
                    <th>Feedback Classification</th>
                    <td>The type of error (e.g., "sign error") or type of hint.</td>
                </tr>
                <tr>
                    <th>Help Level</th>
                    <td>In the case of hierarchical hints, this is the depth of the hint. "1", for example, is an initial hint,
                    while "3" is the third hint.</td>
                </tr>
                <tr>
                    <th>Total Num Hints</th>
                    <td>The total number of hints available. This is logged in the
                    <a href="/dtd/guide/tutor_message.html#element.action_evaluation">action_evaluation</a>
                    element.</td>
                </tr>
                <tr>
                    <th>Condition Name</th>
                    <td>The name of the condition (e.g., "Unworked").</td>
                </tr>
                <tr>
                    <th>Condition Type</th>
                    <td>A condition classification (e.g., "Experimental", "Control"); optional at the time of logging.</td>
                </tr>
                <tr>
                    <th>KC (model_name)</th>
                    <td>The knowledge component for this transaction. It is a member of the knowledge component model
                    named in the column header. One "KC (model_name)" column should appear in the export for each KC model
                    in the dataset.
                    </td>
                </tr>
                <tr>
                    <th>KC Category (model_name)</th>
                    <td>The knowledge component "category" logged by some tutors.
                    It is a member of the knowledge component model named in the column header. One "KC Category (model_name)"
                    column should appear in the export for each KC model in the dataset.</td>
                </tr>
                <tr>
                    <th>School</th>
                    <td>The name of the school where the student used the tutor to create this transaction.</td>
                </tr>
                <tr>
                    <th>Class</th>
                    <td>The name of the class the student was in when he or she
                    used the tutor to create this transaction.</td>
                </tr>
                <tr>
                    <th>CF (custom_field_name)</th>
                    <td>The value of a custom field. This is usually information that did not fit into any of the
                    other logging fields (i.e., any of the other columns), and so was logged in this
                    <a href="/dtd/guide/context_message.html#element.custom_field">special container</a>.
                    </td>
                </tr>
        </tbody>
    </table>


        <h3>By Student-Step</h3>
        <p>Within each sample, rows are ordered by student, then time of the first correct attempt (&ldquo;Correct Transaction Time&rdquo;) or, in the absence of
        a correct attempt, the time of the final transaction on the step (&ldquo;Step End Time&rdquo;).</p>
<%@ include file="table-student-step_rollup.jspf" %>

        <h3 id="table-by-student-problem">By Student-Problem</h3>
        <p>Within each sample, rows are ordered by student, then problem start time.</p>
        <table>
                <colgroup>
                    <col style="width:20%" />
                    <col style="width:80%" />
                </colgroup>
            <thead>
                <tr>
                    <th>Column</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <th>Row</th>
                    <td>A row counter.</td>
                </tr>
                <tr>
                    <th>Sample</th>
                    <td>The sample that includes this problem. If you select more than one sample to export,
                    problems that occur in more than one sample will be duplicated in the export.
                    </td>
                </tr>
                <tr>
                    <th>Anon Student ID</th>
                    <td>The student that worked on the problem.</td>
                </tr>
                <tr>
                    <th>Problem Hierarchy</th>
                    <td>The location in the curriculum hierarchy where this problem occurs.</td>
                </tr>
                <tr>
                    <th>Problem Name</th>
                    <td>The name of the problem.</td>
                </tr>
                <tr>
                    <th>Problem View</th>
                    <td>The number of times the student encountered the problem so far. This counter
                        increases with each instance of the same problem. See "Problem View" in the "By
                        Student-Step" table above.</td>
                </tr>
                <tr>
                    <th>Problem Start Time</th>
                    <td>If the problem start time is not given in the original log data, then it is set
                    to the time of the last transaction of the prior problem. If there is no prior problem
                    for the session, the time of the earliest transaction is used. Earliest transaction time
                    is equivalent to the minimum transaction time for the earliest step of the problem.
                    For more detail on how problem start time is determined, see <a href="help?page=export#determining_pst">Determining
                    Problem Start Time</a>.</td>
                </tr>
                <tr>
                    <th>Problem End Time</th>
                    <td>Derived from the maximum transaction time of the latest step of the problem.</td>
                </tr>
                <tr>
                    <th>Latency (sec)</th>
                    <td>The amount of time the student spent on this problem. Specifically, the
                        difference between the problem start time and the last transaction on this
                        problem.</td>
                </tr>
                <tr>
                    <th>Steps Missing Start Times</th>
                    <td>The number of steps (from the student-step table) with "Step Start Time"
                        values of "null". </td>
                </tr>
                <tr>
                    <th>Hints</th>
                    <td>Total number of hints the student requested for this problem.</td>
                </tr>
                <tr>
                    <th>Incorrects</th>
                    <td>Total number of incorrect attempts the student made on this problem.</td>
                </tr>
                <tr>
                    <th>Corrects</th>
                    <td>Total number of correct attempts the student made for this problem.</td>
                </tr>
                <tr>
                    <th>Avg Corrects</th>
                    <td>The total number of correct attempts / total number of steps in the problem.</td>
                </tr>
            <tr>
                    <th>Steps</th>
                    <td>Total number of steps the student took while working on the problem.</td>
                </tr>
            <tr>
                    <th>Avg Assistance Score</th>
                <td>Calculated as (total hints requested + total incorrect attempts) / total steps.
                    </td>
                </tr>
                <tr>
                    <th>Correct First Attempts</th>
                    <td>Total number of correct first attempts made by the student for this problem.</td>
                </tr>
                <tr>
                    <th>Condition</th>
                    <td>The name and type of the condition the student is assigned to. In the case of a student
                    assigned to multiple conditions (factors in a factorial design), condition names are
                    separated by a comma and space. This differs from the transaction format, which optionally
                    has "Condition Name" and "Condition Type" columns.</td>
                </tr>
                <tr>
                    <th>KCs</th>
                    <td>Total number of KCs practiced by the student for this problem.</td>
                </tr>
                <tr>
                    <th>Steps without KCs</th>
                    <td>Total number of steps in this problem (performed by the student) without an assigned KC.</td>
                </tr>
                <tr>
                    <th>KC List</th>
                    <td>Comma-delimited list of KCs practiced by the student for this problem.</td>
                </tr>
            </tbody>
        </table>

        <h4 id="determining_pv">Determining Problem View</h4>
        <p>Problem View is determined one of three ways:</p>
        <ol>
            <li>If the original log data was in the tutor message format XML, a problem start or restart
            can be indicated in the context message with a START_PROBLEM in the name attribute.
            See <a href="http://pslcdatashop.web.cmu.edu/dtd/guide/context_message.html#context_message.attributes">context
            message attributes</a>.
            <li>If the original log data came from tab-delimited files and the Problem View or Problem Start Time is included</li>
            <li>If no information is given about the Problem View or Problem Start Time in the original log data, then
            DataShop determines when a new instance of the problem occurs by looking for interleaved problems.
            If another problem's transactions occur in between, then the problem view is incremented.</li>
        </ol>

        <h4 id="determining_pst">Determining Problem Start Time</h4>
        <p>Problem Start Time is determined one of three ways:</p>
        <ol>
            <li>If the original log data was in the tutor message format XML, a problem start or restart can be
            indicated in the context message with a START_PROBLEM in the name attribute.  The problem start time
            is set to the time field in the context message. See
            the <a href="http://pslcdatashop.web.cmu.edu/dtd/guide/context_message.html#element.meta">meta element</a>.</li>
            <li>If the original log data came from tab-delimited files and the Problem Start Time is included.</li>
            <li>If no information is given about the Problem Start Time in the original log data, then DataShop determines
            the problem start time to be the time of the last transaction of the prior problem (if one exists) or the
            time of the earliest transaction.</li>
        </ol>

    <h3 id="about-data-anonymization">About data anonymization</h3>
    <p>Exported data is anonymized; real student IDs are replaced with anonymous IDs
    during the export process. Should you wish to obtain identifiable student IDs&mdash;for example, if
    you are the instructor for a course or if the original data was anonymous&mdash;please
        <a href="help?page=contact">contact us </a> so we can confirm that you are
    authorized to view the real student IDs. We will then provide a mapping table from the
    anonymized IDs to the real student IDs.</p>

        <h3 id="ie7-export-download">Export tips for Internet Explorer</h3>
        <p>If downloading of your export file is blocked by Internet Explorer, check your browser's
        security settings:</p>
        <ol>
            <li>Select <strong>Tools &gt; Internet Options</strong></li>
            <li>Select the <strong>Security</strong> tab.</li>
            <li>Select the Internet icon.</li>
            <li>Click <strong>Custom Level...</strong></li>
            <li>Under &ldquo;Downloads&rdquo;, ensure that both <strong>Automatic prompting for
            file downloads</strong> and <strong>File download</strong> settings are set to <strong>Enable</strong>.
            </li>
        </ol>

