<%// Author: Alida Skogsholm
            // Version: $Revision: 15393 $
            // Last modified by: $Author: ctipper $
            // Last modified on: $Date: 2018-07-25 13:11:57 -0400 (Wed, 25 Jul 2018) $
            // $KeyWordsOff: $
%>

<h2>Learning Curve</h2>

<p>A learning curve visualizes changes in student performance over time.
The line graph displays opportunities across the x-axis,
and a measure of student performance along the y-axis. A good learning curve
reveals improvement in student performance as opportunity count
(i.e., practice with a given knowledge component) increases. It can also
"describe performance at the start of training, the rate at which learning occurs, and the
flexibility with which the acquired skills can be used" (Koedinger and Mathan 2004).</p>

<ul class="concise">
    <li><a href="#lc-types">Learning curve types</a></li>
    <li><a href="#viewing">Viewing different curves</a></li>
    <li><a href="#point-info">Viewing the details of a point on the curve</a></li>
    <li><a href="#opp-cutoff">Opportunity Cutoff</a></li>
    <li><a href="#stdev-cutoff">Standard Deviation Cutoff</a></li>
    <li><a href="#predicted">Predicted Learning Curve</a></li>
    <li><a href="#lc-cat">Learning curve categorization</a></li>
</ul>

<p>See also <a href="help?page=learningCurveExamples">Learning Curve 
Examples</a> and <a href="help?page=learningCurveAlgorithm">Learning Curve Algorithm</a>,
and our <a href="http://www.youtube.com/watch?v=UW2UeKKRbCY"
        rel="external" target="_blank">intro to learning curves video</a> (11m:15s).</p>

    <h3 id="lc-types">Learning Curve types</h3>
    <p>You can view a learning curve <span class="screen-item">by student
    </span>or <span class="screen-item">by knowledge component (KC)</span>.</p>
    <table>
    <tbody>
    <tr>
        <th>By Knowledge Component</th>
        <td>View an average across all selected KCs, or view a curve for an
        individual KC. In the "all selected KCs" graph, each point 
        is an average across data for all selected students and KCs.
        In a graph for an individual KC, each point is an average across 
        all selected students.</td>
    </tr>
    <tr>
        <th>By Student</th>
        <td>
        View an average across all selected students, or view a curve for an
        individual student. In the "all selected students" graph, each point 
        is an average across data for all selected students and KCs.
        In a graph for an individual student, each point is an average across 
        all selected KCs.</td>
    </tr>
    </tbody>
    </table>

    <p>Toggle the inclusion of knowledge components or students by 
    clicking their name in the navigation boxes on the left.</p>

    <p>Change the measure of student performance by
    hovering over the y-axis on the graph and clicking the new
    measure.</p>
    
    <p>Measures of student performance are described below. Regardless
    of metric, each point on the graph is an average across all selected
    knowledge components and students.</p>
    
    <table>
        <thead>
            <tr><th>Measure</th><th>Description</th></tr>
        </thead>
        <tbody>
            <tr>
                <th>Assistance Score</th>
                <td>The number of incorrect attempts plus hint requests for a 
                given opportunity</td>
            </tr>
            <tr>
                <th>Error Rate</th>
                <td>The percentage of students that asked for a hint or
                were incorrect <i>on their first attempt</i>. For example,
                an error rate of 45% means that 45% of students asked for a
                hint or performed an incorrect action on their first
                attempt. Error rate differs from assistance score in that it
                provides data based only on the first attempt. As such, an
                error rate provides no distinction between a student that
                made multiple incorrect attempts and a student that made
                only one.</td>
            </tr>
            <tr>
                <th>Number of Incorrects</th>
                <td>The number of incorrect attempts for each opportunity</td>
            </tr>
            <tr>
                <th>Number of Hints</th>
                <td>The number of hints requested for each opportunity</td>
            </tr>
            <tr>
                <th>Step Duration</th>
                <td>The elapsed time of a step in seconds, calculated by adding all of the
                durations for transactions that were attributed to the step.</td>
            </tr>
            <tr>
                <th>Correct Step Duration</th>
                <td>The step duration if the first attempt for the step was correct. 
                The duration of time for which students are "silent", with
                respect to their interaction with the tutor, before they complete the step
                correctly. This is often called "reaction time" (on correct trials) in the 
                psychology literature. If the first attempt is an error (incorrect attempt
                or hint request), the observation is dropped.</td>
            </tr>
            <tr>
                <th>Error Step Duration</th>
                <td>The step duration if the first attempt for the step was an error (hint
                request or incorrect attempt). If the first attempt is a correct attempt, 
                the observation is dropped.</td>
            </tr>
        </tbody>
    </table>

<h3 id="viewing">Viewing different curves</h3>

<h4>To switch between learning curve types:</h4>

<ul class="concise">
	<li>Move your mouse pointer over the y-axis of the graph and click the new measure.</li>
</ul>

<h4>To switch between knowledge component and student views:</h4>

<ul class="concise">
	<li>Select the desired view from the portion of the navigation side-bar.</li>
</ul>

<h4>To examine a single knowledge component or student:</h4>

<ul class="concise">
	<li>Select its thumbnail from the gallery of available graphs on the bottom portion of the
	screen. The main graph then updates.</li>
</ul>

<p>Available graphs are provided based on the selected samples,
students, and knowledge components. (The default sample is titled 'All
Data'.)</p>

<p>To compare conditions or other groups of data, you might define a number of
samples that are subsets of 'All Data'. For more information on
creating and modifying samples, see <a href="help?page=sampleSelector">Sample Selector</a>.</p>

<h3 id="point-info">Viewing the details of a point on the curve</h3>
<p>Explore a single point on the curve by clicking it. You can then navigate points on the curve by
using the previous- and next-opportunity arrows (<img src="images/arrow-left.png" class="icon" />
and <img src="images/arrow-right.png" class="icon" />) beneath the graph. Change the selected line
in the graph by using the sample drop-down.</p>

<p>Each change of the selected point updates the point information beneath the graph. This 
displays the point's value and observation count, as well as counts of the various units of
analysis&mdash;unique KCs, problems, steps, and students&mdash;that compose the point.</p>

<p>Click a count to see values for observations composing a point. Values shown in the table are
averaged by unit of analysis, and exclude dropped or null observations. The "Obs" column displays
the number of observations for each of the items in the table. This can be helpful because it
signifies if the value for the row itself is composed of more than one data point, as well as
the how the value for the item is weighted in determining the point value shown in the 
learning curve graph.</p>

<p>For views showing values by student or KC, links below the table allow you to change the
selection of items in the main navigation boxes based on the values composing the point.</p>

<p>There are a few things to keep in mind when comparing the point details values with the 
total number of observations for a point and the value for that point:</p>

<ul class="concise">
    <li>Multiple observations often fall under a single KC, problem, or step
    (but not student&mdash;there is only one observation per opportunity for a student), as
    indicated in the "Obs" column. In these cases, the number you see for that row is an average.</li>
    <li>Multiple KCs might be attributed to a single step, showing more KCs
    than there are observations.</li>
    <li>In no case should the number of problems, steps, or students exceed
    the number of observations (although future data might invalidate this claim, such as data
    attributing multiple possible steps to a single student action).</li>
    <li>To calculate the point value from the values in the details box, you need to find the
    average while taking into account the frequency of each item, indicated by the number in the 
    "Obs" column.</li>
    <li>Dropped observations can remove data points from the calculation of the point value.
    These observations will not be shown in the details box. The number of dropped observations are
    shown in parentheses after the number of included observations, and are the result of a <a
        href="#stdev-cutoff">standard deviation cutoff</a>.</li>
</ul>


<h3 id="opp-cutoff">Opportunity Cutoff</h4>
<p>When examining a learning curve, it may be useful to limit which student/knowledge component
pairs are included in the graph based on the number of opportunities students had with the knowledge
component. DataShop calls this the <a class="jargon" href="help?page=terms#opp_cutoff">opportunity
    cutoff</a>. For example, specifying an opportunity cutoff max value of 5 would remove
student/knowledge component pairs where students had more than 5 opportunities with the chosen
knowledge component(s). This may remove outliers from the data and provide a better means for
analysis.</p> 

<p>You can set a minimum and/or maximum opportunity cutoff by entering numbers in the learning curve
navigation and pressing <strong>Refresh Graph</strong>. </p>

<h3 id="stdev-cutoff">Standard Deviation Cutoff</h3>

<p>For latency curves (&ldquo;Step Duration&rdquo;, &ldquo;Correct Step Duration&rdquo;, and
&ldquo;Error Step Duration&rdquo;), you can set a <strong>standard deviation cutoff</strong>. This
is the number of standard deviations above and below the mean for which to include data points. Data
points (observations) falling outside the specified standard deviation are dropped from the graph;
the x-axis (number of opportunities) is not affected.</p>

<p>Standard deviation for an opportunity is calculated based on data for all knowledge
components in the current knowledge-component model and the currently selected students. Therefore,
changing the selected KCs will not affect the standard deviation values but changing the selected
students may.</p>

<p><strong>Note:</strong> If you set both a standard deviation cutoff and min and/or max opportunity cutoff,
DataShop calculates the standard deviation <strong>before</strong> applying the opportunity cutoff(s).</p>


<h3 id="predicted">Predicted Learning Curve</h3>

<p>The empirical learning curves (average observed errors of a skill over each learning
opportunity) calculated directly from the data contain lots of noise and take the form of wiggly
lines. This noise comes from various places, such as recording errors, or the environment where the
students worked. The predicted learning curve is much smoother. It is computed using the 
<a href="help?page=modelValues">Additive Factor Model (AFM)</a>, which uses a set of customized Item-Response models to predict how a
student will perform for each skill on each learning opportunity. The predicted learning curves are
the average predicted error of a skill over each of the learning opportunities. As much of the noise
is filtered out by the AFM models, the predicted learning curves are much smoother than the
empirical learning curves.</p>

<p>While the empirical learning curve may give a visual clue as to how well a
student may do over a set of learning opportunities, the predicted curves allow for a
more precise prediction of a success rate at any learning opportunity.</p>

<p>There are several ways to use the predicted learning curves. One is to measure how much
practice is needed to master a skill. When you see a learning curve that starts high and ends high,
students probably finished the curriculum without mastering the skill corresponding to that learning
curve. On the other hand, a learning curve that starts low and ends low with lots of learning
opportunities probably implies that that the skill is easy and students were over-practicing it. For
a detailed example, see <a
    href="http://www.learnlab.org/uploads/mypslc/publications/lfa%20efficiency%20study%203.61%20use%20the%20official%20template.pdf">Is
Over Practice Necessary? Improving Learning Efficiency with the Cognitive Tutor through Educational
Data Mining (Cen, Koedinger, and Junker 2007)</a>.</p>

<p>The second use of predicted learning curves is to find a better set of skills that matches the
student learning. An ideal predicted learning curve should be smooth and downward sloping. If a
learning curve is too flat, goes up, or is too wiggly, the corresponding skill is probably not well-chosen 
and worth refining. For reference, see <a 
    href="http://www.learnlab.org/uploads/mypslc/publications/learning_factor_analysis_5.2.pdf">Learning 
    Factors Analysis - A General Method for Cognitive Model Evaluation and Improvement (Cen, Koedinger, 
    and Junker 2006)</a>.</p>

<h4>To view the predicted learning curve (Error Rate learning curve only):</h4>
<ul class="concise">
	<li>Select "View Predicted" from the learning curve navigation box.</li>
</ul>

<p>In DataShop, AFM computes the statistics of a cognitive model including AIC, BIC, the
coefficients of student proficiency, initial knowledge component difficulty, and knowledge component
learning rate, generating the probability of success on each trial on different knowledge components. You can
view the values of these parameters on the <a href="help?page=modelValues">Model Values</a> report (Learning Curve &gt; Model Values).</p>

<p>For more information on the AFM algorithm, see the <a href="help?page=modelValues">Model Values help page</a>.</p>

<h3 id="lc-cat">Learning curve categorization</h3>
<p>DataShop categorizes learning curves (KCs) into one of four categories described below. To turn on categorization,
select the checkbox <strong>Categorize Curves</strong>. <strong>Learning curve categorization is only available for Error Rate 
learning curves displayed by KC (not student) when only one sample is selected.</strong></p>

<p>The categorization algorithm first discards points in each curve based on the <strong>student threshold</strong>.
If a point has fewer than that number of students, it is ignored. Within the points remaining:
<ul class="concise">
   <li>If the number of points is below the <strong>opportunity threshold</strong>,
   then that curve has <strong>too little data</strong>.</li>
   <li>If a point on the curve ever dips beneath the <strong>low error threshold</strong>, 
   then the curve is <strong>low and flat</strong>.</li>
   <li>If the last point of the curve is above the <strong>high error threshold</strong>, 
   then the curve is <strong>still high</strong>.</li>
   <li>If the slope of the predicted learning curve (as determined by the AFM algorithm) is below 
   the <strong>AFM slope threshold</strong>, then the curve shows <strong>no learning</strong>.</li>
</ul>
    
<p>The defaults for the low and high error thresholds are 20% and 40%, respectively. The 20% figure comes from Benjamin S. Bloom's <a href="https://files.eric.ed.gov/fulltext/ED053419.pdf" target="_blank">"Learning for Mastery" (1968)</a>, where he found that 20% of students were already attaining mastery levels in their learning without improved learning techniques. The 40% is a more arbitrary default. Both thresholds can be adjusted.</p>

<h4>Categories</h4>
<ul class="concise">
    <li><strong>low and flat</strong>: students likely received too much practice for these KCs. The low error rate
    shows that students mastered the KCs but continued to receive tasks for them. Consider reducing 
    the required number of tasks or change your system's knowledge-tracing parameters (if it uses knowledge-tracing)
    so that students get fewer opportunities with these KCs.</li>
    <li><strong>no learning</strong>: the slope of the predicted learning curve shows no apparent learning for these KCs. 
    Explore whether the KC can be split into multiple KCs (via the <a href="help?page=kcm#creating">KC Model export/import</a>) 
    that better account for variation in difficulty and transfer of learning that may be occurring across the tasks (problem steps) 
    currently labeled by this KC. Video <a href="http://www.youtube.com/watch?v=UW2UeKKRbCY">tutorial 1</a> shows the process
    of identifying a KC worth splitting, while <a href="http://www.youtube.com/watch?v=wT1mfXembnM">tutorial 2</a> shows the process
    of exporting, modifying, and importing a KC model. The video <a href="http://www.youtube.com/watch?v=qCR2mxcIb6M">Exploratory 
    analysis with DataShop</a> also shows this type of analysis.</li>
    <li><strong>still high</strong>: students continued to have difficulty with these KCs. Consider increasing opportunities 
    for practice.</li>
    <li><strong>too little data</strong>: students didn't practice these KCs enough for the data to be interpretable. 
    You might consider adding more tasks for these KCs or merging KCs (via the <a href="help?page=kcm#creating">KC Model export/import</a>).
    Video <a href="http://www.youtube.com/watch?v=wT1mfXembnM">tutorial 2</a> shows the process of merging KCs.</li>
    <li><strong>good</strong>: these KCs did not fall into any of the above "bad" or "at risk" categories. Thus, these are "good" learning 
    curves in the sense that they appear to indicate substantial student learning.</li>
</ul>