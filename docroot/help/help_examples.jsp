<%// Author: Alida Skogsholm
            // Version: $Revision: 5665 $
            // Last modified by: $Author: bleber $
            // Last modified on: $Date: 2009-08-11 15:00:33 -0400 (Tue, 11 Aug 2009) $
            // $KeyWordsOff: $
            //

            %>

    <h2>Examples</h2>
    <ul class="concise">
        <li><a href="#s-s_rollup_example">Student-Step Rollup Example</a></li>
        <li><a href="#lc_examples">Learning Curve Examples</a></li>
        <li><a href="#er_examples">Error Report Examples</a></li>
    </ul>
       
    <h2 id="s-s_rollup_example">Student-Step Rollup Example</h2>
    <p>This example demonstrates how DataShop calculates step start time, step end time, step duration,
    and correct step duration for a student on a series of steps.</p>
    <p>To follow the example, refer to the timeline representation of steps and the table of calculated times (both below), 
    and the <a href="help?page=stepRollup">definitions of student-step rollup fields</a>. Note that steps alternately appear 
    above and below the gray line to improve the readability of the example.</p>
    
    <p><img src="images/help/step_rollup_timeline.png" alt="" style="border:none" /></p>
    <table style="margin:2em 0 2em 2em">
        <colgroup>
            <col style="width:5em" />
            <col style="width:8em" />
            <col style="width:8em" />
            <col style="width:12em" />
            <col style="width:12em" />
            <col style="width:20em" />
        </colgroup>
        <thead>
            <th class="header-align">Step #</th>
            <th class="header-align">Start Time</th>
            <th class="header-align">End Time</th>
            <th class="header-align">Step Duration (sec)</th>
            <th class="header-align">Correct Step Duration (sec)</th>
            <th class="header-align">Notes</th>
        </thead>
        <tbody>
            <tr>
                <td>1<sub>1</sub></td>
                <td>15:32</td>
                <td>15:42</td>
                <td>10</td>
                <td>null</td>
                <td>A <a href="help?page=terms#problem_event">problem event</a> precedes the first
                transaction for the step. DataShop uses the problem event time as the step start
                time. The step end time is the time of the last attempt on the step. No attempt is
                correct for this step, so the sum of the durations is the total length of time spent on
                the step, and there is no Correct Step Duration.</td>
        </tr>
            <tr>
                <td>1<sub>2</sub></td>
                <td>15:45</td>
                <td>15:49</td>
                <td>4</td>
                <td>null</td>
                <td>A problem event signifies a new instance of the same problem; it is used as the step start time. 
                The correct attempt is not the first attempt, so again there is no Correct Step Duration.</td>
            </tr>
            <tr>
                <td>2</td>
                <td>null</td>
                <td>46:00</td>
                <td>null</td>
                <td>null</td>
                <td>No problem event precedes the first attempt for the step and the preceding
                transaction is more than 10 minutes before the first transaction on the step. Given this,
                DataShop does not calculate a step start time, nor a Step Duration or Correct Step
                Duration.
            </tr>
            <tr>
                <td>3</td>
                <td>46:00</td>
                <td>46:05</td>
                <td>5</td>
                <td>5</td>
                <td>No problem event precedes the first attempt, but the preceding transaction's
                time is less than 10 minutes prior so it is used as the step start time. Correct Step Duration
                and Step Duration are equivalent because the first transaction is a correct attempt.</td>
            </tr>
            <tr>
                <td>4</td>
                <td>46:06</td>
                <td>46:25</td>
                <td>4+3+3=10</td>
                <td>null</td>
                <td>Step 4 is interrupted by attempts toward Step 5. DataShop excludes time spent
                toward Step 5 in its calculation of total time spent on Step 4. The step duration
                is the sum of the durations for transactions at 46:10 (4s), 46:13 (3s), and 46:25 (3s).</td>
            </tr>
            <tr>
                <td>5</td>
                <td>46:13</td>
                <td>46:22</td>
                <td>9</td>
                <td>null</td>
                <td>No problem event precedes the first attempt, but the preceding transaction's
                time is less than 10 minutes prior so it is used as the step start time.</td>
            </tr>
        </tbody>
    </table>
    
    <h2 id="lc_examples">Learning Curve Examples</h2>
    <p><img style="border:none" src="images/help/ui-example-stoich4.png" /></p>
    <p><img style="border:none" src="images/help/lc-example-assist-stoich4.png" /></p>
    <p><img style="border:none" src="images/help/lc-example-er-stoich4.png" /></p>  

        
    <h2 id="er_examples">Error Report Example</h2>
    <p><img style="border:none" src="images/help/er-example-stoich4.png" /></p>
