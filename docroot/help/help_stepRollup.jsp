
<h2>Student-Step Rollup</h2>

<ul class="concise">
    <li><a href="#column-desc">Column Descriptions</a></li>
    <li><a href="#predicted-error-rate">&ldquo;Predicted Error Rate&rdquo; and how it's calculated</a></li>
    <li><a href="#s-s_rollup_example">Student-Step Rollup Example</a></li>
</ul>

<p>The student-step rollup table aggregates data by student-step: each row represents a student
attempting to complete a step. Within each sample, rows are ordered by student, then time of the 
first correct attempt (&ldquo;Correct Transaction Time&rdquo;) or, in the absence of a correct attempt, 
the time of the final transaction on the step (&ldquo;Step End Time&rdquo;).

<p>Knowledge components are not shown by default. To include them, click the checkbox "Knowledge
Components" at the left of the screen.</p>

<h4>To display the Student-Step Rollup report:</h4>
<ol>
	<li>Click the <strong>Learning Curve</strong> tab at the top of the screen.</li>
	<li>Click the subtab <strong>Student-Step Rollup</strong>.</li>
</ol>

<p>A student-step pair can appear on more than one row. This can happen if the step has more
than one knowledge component associated with it (in which case the row is a duplicate except for the
knowledge component field) or if the student saw the same problem more than once&mdash;there you
would see the Problem View number increase.</p>

<p>As on the Export page, you can export your data using the export button. The Student-Step
Rollup export includes only your selected sample(s), and reflects the chosen knowledge component
models; however, it includes all knowledge components and students within those samples. To include
a subset of knowledge components and/or students, define a new sample using the sample selector, and
include only that sample.)</p>

<h3 id="column-desc">Column Descriptions</h3>
<%@ include file="table-student-step_rollup.jspf" %>

<p>See the <a href="#s-s_rollup_example">Student-Step Rollup Example</a> for a
visual description of how step times, step durations, and correct step durations are calculated.</p>

<h3 id="predicted-error-rate">&ldquo;Predicted Error Rate&rdquo; and how it's calculated</h3>
<p>Predicted error rate is the probability of the student making an error (incorrect action or hint request) on a step, 
as predicted by the Additive Factor Model algorithm.</p>

<div class="figure-center">
<img src="images/help/inverse_logit_AFM_datashop.png" alt="Additive Factor Model (AFM)" 
    title="Additive Factor Model (AFM)" style="border:none" />
</div>

<p>where</p>

<ul class="concise" style="list-style: none">
    <li>&Upsilon;<sub>ij</sub> = the response of student i on item j</li>
    <li>&theta;<sub>i</sub> = coefficient for proficiency of student i</li>
    <li>&beta;<sub>k</sub> = coefficient for difficulty of knowledge component k</li>
    <li>&gamma;<sub>k</sub> = coefficient for the learning rate of knowledge component k</li>
    <li>&Tau;<sub>ik</sub> = the number of practice opportunities student i has had on the knowledge component k</li>
</ul>

<p>and</p>

<div class="figure-center">
    <img src="images/help/lfa-afm-1.png" alt="Additive Factor Model (AFM)" style="border:none" />
</div>

<ul class="concise" style="list-style: none">
    <li>&Kappa; = the total number of knowledge components in the Q-matrix</li>
</ul>

<p><strong>Note:</strong></p>

<ul class="concise">
    <li>The &Tau;<sub>ik</sub> parameter estimate (the number of practice opportunities student i 
    has had on the knowledge component k) is constrained to be greater or equal to 0.</li>
    <li>User proficiency parameters (&theta;<sub>i</sub>) are fit using a Penalized Maximum 
    Likelihood Estimation method (PMLE) to overcome over fitting. User proficiencies are seeded 
    with normal priors and PMLE penalizes the oversized student parameters in the joint estimation 
    of the student and the skill parameters.</li>
</ul>

<p>The intuition of this model is that the probability of a student getting a step correct is
proportional to the amount of required knowledge the student knows, plus the "easiness" of that
knowledge component, plus the amount of learning gained for each practice opportunity.</p>

<p>The term "Additive" comes from the fact that a linear combination of
knowledge component parameters determines logit(<em>p<sub>ij</sub></em>) in the equation.</p>

<p>You can view model parameter values and see measures of how well the AFM statistical model
fits the data on the <a href="help?page=modelValues">Model Values</a> report (a subtab of Learning Curve).</p>

<p>For more information on the AFM algorithm, see the <a href="help?page=modelValues">Model Values</a> help page.
For assistance interpreting the predicted error rate, you may also <a href="help?page=contact">contact
us</a>.</p>

<h3 id="s-s_rollup_example">Student-Step Rollup Example</h3>
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
