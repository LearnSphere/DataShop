<%          // Author: Brett Leber
            // Version: $Revision: 9376 $
            // Last modified by: $Author: bleber $
            // Last modified on: $Date: 2013-06-10 15:15:27 -0400 (Mon, 10 Jun 2013) $
            // $KeyWordsOff: $
            //

            %>

    <h2>Learning Curve Algorithm</h2>
    <ul class="concise">
        <li><a href="#kc-attribution">Knowledge component attribution</a></li>
        <li><a href="#step-time-attribution">Step time and opportunity attribution</a></li>
        <li><a href="#plotting">Learning curve plotting</a></li>
        <li><a href="#aggregate-curves">Creating aggregate curves</a></li>
        <li><a href="#edge-cases">Edge cases in determining KC or step-time attribution</a></li>
    </ul>
       
    <p>To draw a learning curve, DataShop calculates individual points based on step aggregate values,
    which can be seen in the student-step rollup table. As the table name implies, these aggregate
    values are calculated by single student per single step. Two key values in these aggregates are the
    knowledge components (KCs) attributed to the student-step attempts, and the time attributed to the
    student step, or <strong>step time</strong>.</p>
    
    <h4 id="kc-attribution">Knowledge component attribution</h4>
    <p>Most attempts in the transaction data have a knowledge component (KC) associated with them. In
    Table 1 (below), three attempts are shown.</p>
    <table class="short-tx-table">
        <thead>
            <tr><th>Tx #</th><th>Time</th><th>Step</th><th>Evaluation</th><th>KC</th></tr>
        <caption>Table 1. Transaction sample for step X</caption>
            </thead>
        <tbody>
        <tr><td>1</td><td>11:51</td><td>Step X</td><td>Hint</td><td>KC B</td></tr>
        <tr><td>2</td><td>11:52</td><td>Step X</td><td>Incorrect</td><td>KC B</td></tr>
        <tr><td>3</td><td>11:53</td><td>Step X</td><td>Correct</td><td>KC A</td></tr>
        </tbody>
    </table>
    
    <p>In this example, the first two transactions are error attempts on step X. The tutor attributed
    them to KC B. The third transaction is a correct attempt on step X, which the tutor attributed to KC
    A.</p>
    
    <p>Datashop categorizes this KC error attribution information as the tutor's <i>best guess</i> as to which
    KC(s) the student was working toward. For the student-step described in Table 1, DataShop attributes
    KC A to the step; KC B is not attributed to the step. Since the correct attempt existed for the
    step, DataShop attributed only the KC from the correct attempt, and assumed that the preceding
    attempts were really toward KC A.</p>
    
    <p><strong>KC attribution rule 1:</strong> If a correct attempt exists for a student-step, attribute
    all KCs on that correct attempt to the student-step.</p>
    
    <p>A correct attempt may not exist for a student-step. In Table 2, no correct attempt exists for that
    student-step.</p>
    
    <table class="short-tx-table">
        <thead>
            <tr><th>Tx #</th><th>Time</th><th>Step</th><th>Evaluation</th><th>KC</th></tr>
        <caption>Table 2. Transaction sample for step Y</caption>
            </thead>
        <tbody>
        <tr><td>4</td><td>11:55</td><td>Step Y</td><td>Incorrect</td><td>KC A</td></tr>
        <tr><td>5</td><td>11:56</td><td>Step Y</td><td>Hint</td><td>KC B</td></tr>
        <tr><td>6</td><td>11:57</td><td>Step Y</td><td>Incorrect</td><td>KC A</td></tr>
        </tbody>
    </table>
    
    <p>If no correct attempt exists for the student-step, DataShop assigns the KCs from <strong>all
    error attempts</strong> to that step. For step Y, the KCs assigned are A and B.</p>
    
    <p><strong>KC attribution rule 2:</strong> If no correct attempt exists for a student-step,
    attribute the union of all KCs from all error attempts on that step to the student-step.</p>
    
    <h4 id="step-time-attribution">Step time and opportunity attribution</h4>
    
    <p>DataShop also needs to attribute a time to each step so that it can identify and sort
    opportunities.</p>
    
    <p>For a given student-step in which a correct attempt exists, DataShop attributes the time 
    of the <strong>first correct transaction</strong> as the step time. For a student-step in which 
    no correct attempt exists, DataShop assigns the <strong>maximum time for all error attempts</strong> 
    as the step time. The student-steps are then ordered by the step time to determine the opportunities for the KCs.</p>
    
    <p>In the example in Table 1 below, a correct attempt exists.</p>
    
    <table class="short-tx-table">
        <thead>
            <tr><th>Tx #</th><th>Time</th><th>Step</th><th>Evaluation</th><th>KC</th></tr>
        <caption>Table 1. Transaction sample for step X</caption>
            </thead>
        <tbody>
        <tr><td>1</td><td>11:51</td><td>Step X</td><td>Hint</td><td>KC B</td></tr>
        <tr><td>2</td><td>11:52</td><td>Step X</td><td>Incorrect</td><td>KC B</td></tr>
        <tr><td>3</td><td><strong>11:53</strong></td><td>Step X</td><td>Correct</td><td>KC A</td></tr>
        </tbody>
    </table>
    
    <p>For step X, the step time is therefore "11:53", the time of the first correct attempt.</p>
    
    <p>In Table 2, no correct attempt exists.</p>
    
    <table class="short-tx-table">
        <thead>
            <tr><th>Tx #</th><th>Time</th><th>Step</th><th>Evaluation</th><th>KC</th></tr>
        <caption>Table 2. Transaction sample for step Y</caption>
            </thead>
        <tbody>
        <tr><td>4</td><td>11:55</td><td>Step Y</td><td>Incorrect</td><td>KC A</td></tr>
        <tr><td>5</td><td>11:56</td><td>Step Y</td><td>Hint</td><td>KC B</td></tr>
        <tr><td>6</td><td><strong>11:57</strong></td><td>Step Y</td><td>Incorrect</td><td>KC A</td></tr>
        </tbody>
    </table>
    
    <p>For step Y, the step time is therefore "11:57", the maximum time of the incorrect attempts.</p>
    
    <p>The resulting opportunity counts for KCs A and B across steps X and Y would be:</p>
    <table class="short-tx-table">
        <thead>
            <tr><th>Step</th><th>KC</th><th>Opportunity</th></tr>
            <caption>Table 3. Step table showing opportunity counts for KCs A and B</caption>
            </thead>
        <tbody>
        <tr><td>Step X</td><td>KC A</td><td>1</td></tr>
        <tr><td>Step Y</td><td>KC A, KC B</td><td>2, 1</td></tr>
        </tbody>
    </table>
    
    <p>KC A on step X receives opportunity 1 because its step time (11:53) comes before the step time 
    for KC A on step Y (11:57). KC B receives opportunity 1 because it only appears once, with step Y at 11:57.
    Step Y has KCs A and B associated with it due to KC attribution rule 2 (see above). The opportunity counts are also 
    incremented independently for the two KCs on step Y.</p>
    
    <h4 id="plotting">Learning curve plotting</h4>
    
    <p>With the KC and step-time attribution determined, DataShop can then plot points in a
    learning curve.</p>
    
    <p>We can draw a simple error rate learning curve for KC A based on the six transactions in Tables
    1 and 2.</p>
    
    <div class="figure-center" style="width:476px">
    <img src="images/help/lc-er-example.png" alt="ER learning curve example" />
    </div>
    
    <p>This graph can be summarized as: On the first opportunity to demonstrate KC A (step X), the first
    attempt was a hint request (ie, an error). On the second opportunity (step Y), the first attempt was
    an incorrect attempt (ie, an error). As error rate is either 0 or 1 for a step, we have two error
    rates of 1 (100%).</p>
    
    <p>An assistance score (incorrect attempts plus hint requests) graph would look like the following:</p>
    
    <div class="figure-center" style="width:474px">
    <img src="images/help/lc-as-example.png" alt="Assistance Score learning curve example" />
    </div>
    
    <h4 id="aggregate-curves">Creating aggregate curves</h4>
    
    <p>With data for individual student-steps stored, DataShop can create aggregate graphs (KC A across
    all students, for example) by simply computing an average for each opportunity. Viewing by student
    or KC means computing an average for a subset of all data points at each opportunity.</p>
    
    <h4 id="edge-cases">Edge cases in determining KC or step-time attribution</h4>
    
    <p>In some data, a single step may repeat for a given student. DataShop determines the boundary
    between opportunities by examining problem start events. A problem start event between two student actions
    toward the same step means that the second action is toward a new, unique opportunity.</p>
    
    <p>A problem start event can be indicated in an XML log or tab-delimited file that 
    describes the tutoring session. In XML, a problem start event is indicated by a context message 
    with a <a href="http://pslcdatashop.web.cmu.edu/dtd/guide/context_message.html#context_message.attributes">name 
    attribute of "START_PROBLEM"</a>. In a tab-delimited file, a problem start event is indicated by
    an increment of the "Problem View" column or a new value in the "Problem Start Time" column.</p>
    
    </div>
