<%// Author: Alida Skogsholm
            // Version: $Revision: 5537 $
            // Last modified by: $Author: bleber $
            // Last modified on: $Date: 2009-06-22 12:12:08 -0400 (Mon, 22 Jun 2009) $
            // $KeyWordsOff: $
%>

<h2>Error Report</h2>

<p>To simplify analysis, the Error Report examines data from student <strong>first
attempts only</strong>.</p>

<p>In the Error Report, you can view information aggregated by problem step or knowledge
component.</p>
<ul class="concise">
    <li><a href="#viewing-by-problem">Viewing by problem</a></li>
    <li><a href="#viewing-by-kc">Viewing by knowledge component</a></li>
</ul>

<p>The Error Report provides:</p>
<ul class="concise">
    <li>summaries of student performance by step or knowledge component</li>
    <li>actual values students entered and the feedback they received</li>
    <li>at-a-glance information on problem coverage&mdash;the number of students exposed to a
    particular step or knowledge component, the knowledge components associated with each step, and
    the the problems that contain each knowledge component.</li>
</ul>

<h3 id="viewing-by-problem">Viewing by problem</h3>
<p>When viewing by problem, you can view only one problem at a time. Change the problem by clicking a
problem's name from the scrollable list in the navigation sidebar.</p>
<p>The current problem is broken down by step, and lists the various attempts students made.</p>
<p>At the top of the page, the full problem name is shown.</p>

<h4>Steps</h4>
<p>A problem is commonly broken down into multiple steps. For each step, a list of details about
that step are displayed.</p>
<dl>
    <dt>Step Name</dt>
    <dd>The first line of step information is the step name. The format of the step name varies
    across tutor technologies and logging implementations. CTAT tutors, for example, characterize
    step name as the widget the student interacted with (ie, the 'selection') and the action the
    student performed on that widget. (Student input&mdash;the third component of the
    selection-action-input format&mdash;is not displayed in this portion of the table since it
    varies by student response. Instead, if it exists, it appears under the <strong>Answer</strong>
    column of each row in the <strong>Attempts</strong> table.)</dd>

    <dt>Number of Students</dt>
    <dd>The total number of students that attempted this step of the problem.</dd>

    <dt>Knowledge Component(s)</dt>
    <dd>The knowledge component(s) that are associated with this step of the problem.</dd>

    <dt>Sample</dt>
    <dd>The name of the sample(s) from which the shown data comes. The sample name here will
    one of the selected samples shown in the upper-left corner of the sidebar.</dd>
</dl>

<h4>Attempts</h4>
<div class="figure"><img src="images/help/error_report_colors.png" alt="Evaluation types" />
<p class="imgcaption">Figure 1: Evaluation Types</p>
</div>

<dl>
    <dt>Evaluation</dt>
    <dd>Student attempts are classified into three evaluation types: correct, incorrect, and
    hint. Each evaluation type corresponds with a background color (see Figure 1).</dd>

    <dt>Number of Students</dt>
    <dd>The number of students that attempted this step. Expressed in the form: count
    (percentage of students that attempted this step).</dd>

    <dt>Answer</dt>
    <dd>The answer that the student provided on his or her first attempt for this step</dd>

    <dt>Feedback</dt>
    <dd>The feedback message provided by the tutor to the student.</dd>

    <dt>Classification</dt>
    <dd>Used to further describe the student-problem interaction (e.g., some tutors employ
    multiple hint and error levels).</dd>
</dl>

<h3 id="viewing-by-kc">Viewing by knowledge component</h3>
    <p>When viewing by knowledge component, you can select multiple
    knowledge components, but each will be shown in its own table row.</p>
    <p>Values shown are aggregates by knowledge component. From these
    you can tell, for instance, the more difficult knowledge components
    by examining the percentages on corrects, errors, and hints.</p>

<h4>Knowledge Component</h4>
<dl>
    <dt>Knowledge Component (KC) Name</dt>
    <dd>Displayed in bold type.</dd>

    <dt>Number of Observations</dt>
    <dd>The total number of times a student took an opportunity to demonstrate the knowledge
    component.</dd>

    <dt>Problem(s)</dt>
    <dd>The problem(s) that contain steps with this knowledge component.</dd>

    <dt>Sample</dt>
    <dd>The name of the sample(s) from which the shown data comes. The sample name here will
    one of the selected samples shown in the upper-left corner of the sidebar.</dd>
</dl>
<h4>Aggregate Values</h4>
<dl>
    <dt>Evaluation</dt>
    <dd>Student attempts are classified into three evaluation types: correct, incorrect, and
    hint. Each evaluation type corresponds with a background color (see Figure 1).</dd>

    <dt>Number of Observations</dt>
    <dd>The number of times a student took an opportunity to demonstrate the knowledge
    component, broken down by evaluation. Expressed in the form: count (percentage of observations
    for this KC).</dd>
</dl>

<p style="margin:0"><img src="images/help/er-example-stoich4.png" alt="Error Report example" style="border:none" /></p>