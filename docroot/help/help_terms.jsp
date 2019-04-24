<%  // Author: Brett Leber
    // Version: $Revision: 14058 $
    // Last modified by: $Author: mkomisin $
    // Last modified on: $Date: 2017-04-27 06:40:05 -0400 (Thu, 27 Apr 2017) $
    // $KeyWordsOff: $
 %>

    <h2>Glossary</h2>
    <p>To promote and exemplify <acronym title="Pittsburgh Science of
    Learning Center">PSLC</acronym>'s theoretical framework, the
    DataShop web application uses terms defined by researchers in the <a
    href="http://www.learnlab.org/research/wiki/">PSLC theoretical
    hierarchy</a>. These terms are defined in the PSLC theory wiki <a
    href="http://www.learnlab.org/research/wiki/index.php/Category:Glossary">glossary</a>,
    but are re-defined here in terms of the DataShop web application. In addition, DataShop
    introduces new terms that expand upon existing terminology.
    Researchers are encouraged to explore both the <a
    href="http://www.learnlab.org/research/wiki/index.php/Category:Glossary">theory
    wiki glossary</a> and the current page to enhance their use of
    DataShop.</p>
    
    <ul class="concise">
        <li><a href="#problem">Problem</a></li>
        <li><a href="#step">Step</a></li>
        <li><a href="#transaction">Transaction</a></li>
        <li><a href="#kc">Knowledge Component</a></li>
        <li><a href="#opportunity">Opportunity</a></li>
        <li><a href="#learning_curve">Learning Curve</a></li>
        <li><a href="#error_rate">Error Rate</a></li>
        <li><a href="#assist_score">Assistance Score</a></li>
        <li><a href="#observation">Observation</a></li>
        <li><a href="#problem_view">Problem View</a></li>
        <li><a href="#kc_model">Knowledge Component Model</a></li>
        <li><a href="#opp_cutoff">Opportunity Cutoff</a></li>
        <li><a href="#std_dev_cutoff">Standard deviation cutoff</a></li>
        <li><a href="#tx_duration">Transaction Duration</a></li>
        <li><a href="#step_duration">Step Duration</a></li>
        <li><a href="#correct_step_duration">Correct Step Duration</a></li>
        <li><a href="#error_step_duration">Error Step Duration</a></li>
        <li><a href="#problem_hierarchy">Problem Hierarchy</a></li>
        <li><a href="#problem_event">Problem Event</a></li>
        <li><a href="#afm">AFM</a></li>
        <li><a href="#perf_profiler">Performance Profiler</a></li>
        <li><a href="#error_report">Error Report</a></li>
    </ul>
    
    <h3>Key Terms</h3>
    <p>Five key terms used in DataShop form the building blocks for
    other more complex terms. These are <strong>problem</strong>, 
    <strong>step</strong>, <strong>transaction</strong>,
    <strong>knowledge component</strong>, and
    <strong>opportunity</strong>.
    To more concretely define these terms, we'll use the following
    scenario:</p>
    <div class="scenario">
        <p>Using a computer tutor for geometry, a student completes a
        problem where she is asked to find the area of a piece of scrap
        metal left over after removing a circular area (the end of a
        can) from a metal square (<a href="images/help/making-cans.png" 
            class="figure" target="_blank">Figure-1</a>). The student 
        enters everything in the worksheet except for the row labels, 
        and column and 'Unit' labels for the first three columns.</p> 
        
        <p>The tutor records the student's actions and stores them in a
        log file, which is imported and analyzed by DataShop. DataShop
        creates two tables: a transaction table and a step table.
        <a href="help/help_terms-tables.jsp#table-1" 
                class="figure" target="_blank">Table-1</a> and 
        <a href="help/help_terms-tables.jsp#table-2" 
                class="figure" target="_blank">Table-2</a> illustrate 
        portions of these tables.</p>
    </div>
    
    <div style="float:left;margin-left:1em;width:305px">
    <p style="font-style:oblique;font-size:.8em;width:14em;
        margin-left:auto;margin-right:auto;margin-bottom:0">
        Click image for larger version</p>
    <p><a href="images/help/making-cans.png" target="_blank" 
    class="clickableImage"><img src="images/help/making-cans-small.png" alt="Carnegie Learnings 
        Cognitive Tutor 2005: Making Cans" /></a></p>
    <p><strong>Figure-1:</strong> An example problem from Carnegie Learning's Cognitive Tutor
    2005: Making Cans</strong></p>
    </div>
    <ul class="concise" style="float:left;width:20em;margin-top:5em">
        <li><a href="help/help_terms-tables.jsp#table-1" 
                target="_blank">Table-1: Transaction table excerpt</a></li>
        <li><a href="help/help_terms-tables.jsp#table-2"
                target="_blank">Table-2: Step table excerpt</a></li>
        <li><a href="videos/making_cans/making_cans.jsp">
        Video: Solving "Making Cans", Question
        1 (1m:27s)</a></li>
    </ul>

    <dl style="clear:left">
        <dt id="problem">Problem</dt>
            <dd><p>A problem is a task for a student to perform that
            typically involves multiple steps. In the example above, the
            problem asks the student to find the area of a piece of
            scrap metal left over after removing a circular area (the
            end of a can) from a metal square 
            (<a href="images/help/making-cans.png" 
            class="figure" target="_blank">Figure-1</a>).
            </p>
            
            <p>In language domains, such tasks are
            more often called <em>activities</em> or <em>exercises</em> rather than 
            <em>problems</em>. A language activity, for example, could
            involve finding and correcting all of the grammatical errors
            in a paragraph.</p></dd>

        <dt id="step">Step</dt>
            <dd><p>A step is an observable part of the solution to a 
            problem. Because steps are observable, they are partly 
            determined by the user interface available to the student 
            for solving the problem.</p>

            <p>In the example problem above, the correct
            steps for the first question are:</p>
        
            <ul class="concise" style="margin-bottom:1em">
                <li>find the radius of the end of the can (a circle)
                </li>
                <li>find the length of the square ABCD</li>
                <li>find the area of the end of the can</li>
                <li>find the area of the square ABCD</li>
                <li>find the area of the left-over scrap</li>
            </ul>
        
            <p>This whole collection of steps comprises the solution.
            The last step can be considered the 'answer', and the others
            as 'intermediate' steps.</p>
        
            <p>It is not required, however, that a student complete a
            problem by performing only the correct steps&mdash;the
            student might request a hint from the tutor, or enter an
            incorrect value. How do we characterize the actions of a
            student that is working towards performing a step correctly?
            These actions are referred to as transactions and
            attempts.</p>
        </dd>
        
        <dt id="transaction">Transaction</dt> 
            <dd> <p>A transaction is an interaction between the student 
            and the tutoring system.</p>
            <p>Students may make incorrect entries or ask for hints
            before getting a step correct. Each hint request, incorrect attempt, or
            correct attempt is a transaction; and a step can involve one or more
            transactions.</p>
            
            <p>In <a href="help/help_terms-tables.jsp#table-1" 
            class="figure">Table-1</a> above, a list of transactions
            is displayed for a single student working on two tutor
            problems. Transaction 9 of 
            <a href="help/help_terms-tables.jsp#table-1" 
            class="figure">Table-1</a> shows that the student
            correctly entered a '4' in the tutor
            interface&mdash;specifically, in the first column of the row
            labeled 'Question 1' (see 
            <a href="images/help/making-cans.png" 
            class="figure">Figure-1</a>). Transaction 10 shows
            the correct entry of the '8' in the next column. In both
            cases, there is one transaction for each step. In
            transactions 11 and 12, we see incorrect entries ('32','4')
            for the 'Area of the scrap metal'. The student returns to
            this step in transaction 15 and correctly enters 13.76, but
            first finds the areas of the square and circle (transactions
            13 and 14). The step <strong>(SCRAP-METAL-AREA Q1)</strong>
            therefore involved three transactions. These multiple 
            transactions are also known as <strong>attempts</strong>
            for the given step.</p>
            
            <p>In <a href="help/help_terms-tables.jsp#table-2" 
            class="figure">Table-2</a>, transactions have been 
            consolidated and displayed by student and step, producing a student-step stable.
            In line 6 of <a href="help/help_terms-tables.jsp#table-2" 
            class="figure">Table-2</a>, we see '0' in Total Incorrects for the
            step corresponding with transaction 9. In line 10, we see the
            two incorrects on step (SCRAP-METAL-AREA Q1) corresponding
            with transactions 11 and 12. Line 3 shows the two incorrects
            and one hint corresponding with transactions 3&ndash;5.</p>

            <p>A transaction can be either &ldquo;tutored&rdquo; or &ldquo;untutored&rdquo;. A tutored
            transaction is one in which the tutoring system identifies the student action as toward a step.
            Examine the &ldquo;Tutor Response Type&rdquo; column of a transaction export to determine this:
            tutored transactions have a value in this column, while untutored transactions do not. (Other 
            step-related columns such as Tutor Response Subtype, Step Name, Attempt At Step, and Outcome should
            also be blank for an untutored transaction.)</p>

    </dd>
        
        <dt id="kc">Knowledge Component</dt>
            <dd>
            <p>A knowledge component is a piece of information that can
            be used to accomplish tasks, perhaps along with other
            knowledge components. Knowledge component is a
            generalization of everyday terms like concept, principle,
            fact, or skill, and cognitive science terms like schema,
            production rule, misconception, or facet.</p>
            
            <p>Each step in a problem require the student to know
            something, a relevant concept or skill, to perform that step
            correctly. In DataShop, each step can be labeled with a
            hypothesized knowledge component needed&mdash;see the last
            column of <a href="help/help_terms-tables.jsp#table-2" 
            class="figure">Table-2</a> to see example KC labels. In line
            9 of <a href="help/help_terms-tables.jsp#table-2" 
            class="figure">Table-2</a>, the researcher has hypothesized 
            that the student
            needs to know CIRCLE-AREA to find POG-AREA. In line 10,
            the COMPOSE-AREAS knowledge component is hypothesized to be
            be needed to find the SCRAP-METAL-AREA.</p>

            <p>In <a href="help/help_terms-tables.jsp#table-2" 
            class="figure">Table-2</a> above, the knowledge
            components are shown in the last column. Every knowledge 
            component is associated with one or more steps.</p>
            
            <p>In DataShop, one or more knowledge components can be
            associated with a step. This association is typically
            originally defined by the problem author, but researchers
            can provide alternative knowledge components and
            associations with steps, also known as a <a
            href="#kc_model">Knowledge Component Model</a>.</p>
            </dd>

        <dt id="opportunity">Opportunity</dt>
        <dd><p>An opportunity is a chance for a 
        student to demonstrate whether he or she has learned a given 
        knowledge component. An opportunity
        exists each time a step is present with the associated 
        knowledge component.</p>
        
        <p>In column <strong>Opportunity Count</strong> of
        <a href="help/help_terms-tables.jsp#table-2" 
            class="figure">Table-2</a>, the opportunity count increases by one
        each time the student encounters a step with the associated
        knowledge component.</p>
        
        <p>An opportunity is both a test of whether a student knows a
        knowledge component and a chance for the student to learn it.
        While students may make multiple attempts at a step or request
        hints from a tutor (these are transactions), the whole set of
        attempts are considered a single opportunity. As
        a student works through steps in problems (and multiple problems), they will have
        multiple opportunities to apply a knowledge component.</p>
        </dd>
        
        <dt id="learning_curve">Learning Curve</dt>
        <dd><p>A learning curve is a line graph displaying 
        opportunities across the x-axis,
        and a measure of student performance along the y-axis. As a learning curve visualizes student 
        performance over time, it should reveal improvement in student 
        performance as opportunity count (ie, practice with a given
        knowledge component) increases.</p>
        <p>Measures of student performance available in learning
        curves are <a href="#error_rate">Error Rate</a>, 
        <a href="#assist_score">Assistance Score</a>, number of incorrects or hints, 
        <a href="#step_duration">Step Duration</a>, <a href="#correct_step_duration">Correct Step 
        Duration</a>, and <a href="#error_step_duration">Error Step Duration</a>.</p>
        </dd>

        <dt id="error_rate">Error Rate</dt>
        <dd><p>The percentage of students that asked for a hint or were
        incorrect on their first attempt. For example, an error rate of
        45% means that 45% of students asked for a hint or performed
        an incorrect action on their first attempt.</p>
        
        <p>For each student on each step, the error rate is computed
        as follows:</p>
        <ul class="concise">
            <li>If Incorrects = 0 or Hints = 0, the Error Rate = 0;</li>
            <li>If Incorrects &gt; 0 or Hints &gt; 0, the Error Rate = 1.</li>
        </ul>
        <p>See <a href="help/help_terms-tables.jsp#table-2" 
            class="figure">Table-2</a> for examples.</p>
        <p>Both incorrect actions (errors of commission) and hint
        requests (errors of omission&mdash;the student did not know how
        to perform the step on his or her own) are considered errors.</p>
        </dd>
        
        <dt id="assist_score">Assistance Score</dt>
        <dd>
        <p>For a given opportunity, the number of incorrect attempts
        plus hint requests equals the assistance score.</p>
        <p>In the example above of a single student working on a 
        problem, assistance score is derived by simply adding the 
        number of wrong attempts and the hints requested for each step.
        </p>
        </dd>

        <dt id="observation">Observation</dt>
        <dd>
        <p>An observation is a group of transactions for a particular student working on a
        particular step within a problem view. If within these constraints there is only one 
        transaction recorded, an observation will still exist for that single transaction.</p>

        <p>Put another way, an observation is available each time a student takes an opportunity to
        demonstrate a knowledge component.</p>

        <p>In the sample data shown in <a href="help/help_terms-tables.jsp#table-2" 
            class="figure">Table-2</a>, there are four observations for knowledge component 
        <strong>circle-area</strong> and four for <strong>compose-areas</strong>.</p>

        <p>On a larger sample of 10 students, if all ten students use a
        tutor that presents two opportunities to demonstrate Knowledge
        Component A, there would be ten observations per opportunity,
        and 20 total observations.</p></dd>
    
        <dt id="problem_view">Problem View</dt>
        <dd><p>The number of times the student encountered the problem so far. 
        This counter increases with each instance of the same problem.  This number is based on 
        the notion of a distinct problem: DataShop identifies
        similar problems as distinct when the problem name is different or when two problems with the same name
        don't have identical versions of the following logged values:</p>
        <ul class="concise">
            <li>problem name</li>
            <li>context</li>
            <li>tutor_flag (whether or not the problem or activity is tutored) and "other" field</li>
        </ul>
        <p>For more on the logging of these fields, see the <a
            href="/dtd/guide/context_message.html#element.dataset"> description of the "problem"
        element</a> in the Guide to the Tutor Message Format.</p>

        <p>See also the <a href="help?page=export#table-by-student-step">definition of problem view in the context
        of a student-step table</a>.</p>
        </dd>
        
        <dt id="kc_model">Knowledge Component Model</dt>
        <dd><p>A list of mappings between each step and one or more 
        knowledge components; also known as a Transfer Model or a Skill Model.</p>
        
        <p>In <strong>Figure-2</strong> below, two learning curves with different
        knowledge component models are shown.</p>
        
        <div class="figure-center" style="width:713px">
        <img src="images/help/learning_curves-two_kc_models.png"
            style="border:none"/>
        <p><strong>Figure-2:</strong> Two learning curves, each with
        a different knowledge component model applied.</p>
        </div>
        </dd>

        <dt id="opp_cutoff">Opportunity Cutoff</dt>
        <dd><p>A minimum and/or maximum number of opportunities a student must have
        had with a knowledge component for the student-knowledge
        component pair to be included in the data. This filtering mechanism is available 
        on the learning curve report.</p></dd>
        
        <dt id="std_dev_cutoff">Standard Deviation Cutoff</dt>
        <dd><p>The number of standard deviations above and below the mean for which to include data points. 
        Data points (observations) falling outside the specified number of standard deviations are dropped 
        from the graph</p></dd> 
        
        <dt id="tx_duration">Transaction Duration</dt>
        <dd><p>Duration of the transaction in seconds. This is the time of the current transaction minus 
        that of the preceding transaction or problem start event&mdash;whichever is closer in time to the current transaction. 
        If this difference is greater than 10 minutes, DataShop reports the duration as null (a dot). If the current 
        transaction is preceded by neither another transaction or a problem start event, duration is shown as null. 
        The duration is formatted without decimal places if the two times used in the calculation were without
        millisecond precision.</p>
        <p>See <a href="help?page=stepRollup#s-s_rollup_example">examples of how transaction durations are calculated</a>.</p>
        </dd>
        
        <dt id="step_duration">Step Duration</dt>
        <dd><p>Previously called "Assistance Time", Step Duration is the total length of time spent on a step.
        It is calculated by adding all of the durations for transactions that were attributed to a given step.
        Step Duration differs from previous latency calculations in DataShop in that it's 
        derived by summing transaction durations, not finding the difference between two points in time 
        (step start time and the last correct attempt), which can attribute elapsed time to multiple steps if they
        occurred in an interleaving order.</p> 
        <p>See <a href="help?page=stepRollup#s-s_rollup_example">examples of how Step Duration is calculated</a>.</p>
        </dd>

        <dt id="correct_step_time">Correct Step Duration</dt>
        <dd>
        <p>The <a href="#step_duration">step duration</a> if the first attempt for the step was
        correct. Correct Step Duration might also be described as "reaction time" since it's the
        duration of time from the previous transaction or problem start event to the first correct
        attempt. This measure was previously labeled "Correct Step Time (sec)". Note that Correct Step
        Duration is equal to Step Duration when the first attempt by the student is correct.</p>
        <p>See <a href="help?page=stepRollup#s-s_rollup_example">examples of how Correct Step
        Duration is calculated</a>.</p>
        </dd>

        <dt id="error_step_duration">Error Step Duration</dt>
        <dd><p>The <a href="#step_duration">step duration</a> if the first attempt for the step was
        an error (an incorrect attempt or hint request).</p>
        </dd>
    
        <dt id="problem_hierarchy">Problem Hierarchy</dt>
        <dd><p>The hierarchy of curriculum levels containing the problem. For example, a problem
        might be contained in a Unit A, Section B hierarchy.</p></dd>
        
        <dt id="problem_event">Problem Event</dt>
        <dd><p>A class of events that describe changes at the problem level (eg, problem started,
        skipped, resumed, etc). Currently DataShop only uses "START_PROBLEM" problem events,
        which are used to determine problem start time and other time-related values. An example of
        this usage is shown on the <a href="help?page=stepRollup#s-s_rollup_example">Student-Step Rollup page</a>.</p></dd>

        <dt id="afm">AFM</dt>
        <dd><p>Acronym for <a href="help?page=modelValues">Additive Factor Model</a>, a logistic regression method which 
        uses a set of customized Item-Response models to predict how a student will perform for each knowledge 
        component on each learning opportunity. AFM was developed at Carnegie Mellon by Hao Cen, Kenneth Koedinger, 
        and Brian Junker. In DataShop, the AFM algorithm is run over each KC model of each dataset, producing data that populates
        predicted learning curves and other reports.</p>
        <p>View the <a href="help?page=modelValues#equation">AFM formula</a>.</p></dd>
        
	<dt id="perf_profiler">Performance Profiler</dt>
        <dd><p>A multi-purpose DataShop tool for examining data by various measures and at varying levels
        of granularity.</p></dd>
        
        <dt id="error_report">Error Report</dt>
        <dd><p>A DataShop report that provides summaries of student performance;
        actual values students entered and the feedback they received; and at-a-glance information on problem 
        coverage.</p></dd>
    </dl>

