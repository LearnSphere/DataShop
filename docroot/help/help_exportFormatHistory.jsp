<%// Author: Brett Leber
            // Version: $Revision: 10078 $
            // Last modified by: $Author: bleber $
            // Last modified on: $Date: 2013-10-02 10:56:18 -0400 (Wed, 02 Oct 2013) $
            // $KeyWordsOff: $
            //

            %>

<h2>Export Format History</h2>
<p>The following is a list of changes to the various export formats, arranged by time of the change.</p>

<div class="format-history">

<h3>Transaction format</h3>
<h4 class="date">October 2013</h4>
<ul>
    <li>Added <em>Transaction Id</em> column.</li>
    <li><em>Level(level_name)</em> changed to <em>Level (level_name)</em></li>
    <li><em>KC(model_name)</em> changed to <em>KC (model_name)</em></li>
    <li><em>KC Category(model_name)</em> changed to <em>KC Category (model_name)</em></li>
    <li><em>CF(custom_field_name)</em> changed to <em>CF (custom_field_name)</em></li>
</ul>

<h4 class="date">June 2013</h4>
<ul>
    <li>Removed ".0" from the end of timestamps in the columns <em>Time</em> and 
    <em>Problem Start Time</em>.</li>
</ul>

<h4 class="date">July 2011</h4>
<ul>
    <li>Added <em>Problem View</em> column.</li>
    <li>Added <em>Problem Start Time</em> column.</li>
</ul>

<h4 class="date">January 2010</h4>
<ul>
    <li>Renamed <em>#</em> to <em>Row</em>.</li>
    <li>Renamed <em>Total # Hints</em> to <em>Total Num Hints</em>.</li>
</ul>

<h4 class="date">August 2009</h4>
<ul>
    <li>Added <em>Duration (sec)</em> column.</li>
</ul>

<h3>Student-step format</h3>
<h4 class="date">October 2013</h4>
<ul>
    <li><em>Knowledge Component(model_name)</em> changed to <em>KC (model_name)</em>, with multiple
    KCs on a step separated by two tildes ("~~"). </li>
    <li><em>Opportunity(model_name)</em> changed to <em>Opportunity (model_name)</em>, with multiple
    KCs on a step separated by two tildes ("~~").</li>
    <li><em>Predicted Error Rate(model_name)</em> changed to <em>Predicted Error Rate (model_name)</em>, with multiple
    KCs on a step separated by two tildes ("~~").</li>
</ul>

<h4 class="date">June 2013</h4>
<ul>
    <li>Removed ".0" from the end of timestamps in the columns <em>Step Start Time</em>, 
    <em>First Transaction Time</em>, <em>Correct Transaction Time</em>, and <em>Step End Time</em>.
    </li>
</ul>

<h4 class="date">January 2010</h4>
<ul>
    <li>Renamed <em>#</em> to <em>Row</em>.</li>
    <li>Added <em>Condition</em>.</li>
</ul>

<h4 class="date">August 2009</h4>
<ul>
    <li>Renamed <em>Assistance Time (sec)</em> to <em>Step Duration (sec)</em>.</li>
    <li>Renamed <em>Correct Step Time (sec)</em> to <em>Correct Step Duration (sec)</em>.</li>
    <li>Added <em>Error Step Duration (sec)</em> column.</li>
    <li>Removed <em>Step Time</em>, which was the time of the first correct attempt
    or, in the absence of a correct attempt, the final transaction on that step.</li>
    <li>Added <em>Correct Transaction Time</em>.</li>
</ul>

<h3>Student-problem format</h3>
<h4 class="date">October 2013</h4>
<ul>
    <li><em>Avg Correct</em> changed to <em>Avg Corrects</em></li>
</ul>

<h4 class="date">June 2013</h4>
<ul>
    <li>Removed ".0" from the end of timestamps in the columns <em>Problem Start Time</em> and
    <em>Problem End Time</em>.</li>
    <li>Added <em>Condition</em> column.
</ul>

<h4 class="date">January 2010</h4>
<ul>
    <li>Renamed <em>#</em> to <em>Row</em>.</li>
</ul>

<h3>KC Model export</h3>
<h4 class="date">October 2013</h4>
<ul>
    <li><em>KC Model (model_name)</em> changed to <em>KC (model_name)</em></li>
    <li><em>Avg. Incorrects</em> changed to <em>Avg Incorrects</em></li>
    <li><em>Avg. Hints</em> changed to <em>Avg Hints</em></li>
    <li><em>Avg. Corrects</em> changed to <em>Avg Corrects</em></li>
    <li><em>Avg. Step Duration (sec)</em> changed to <em>Avg Step Duration (sec)</em></li>
    <li><em>Avg. Correct Step Duration (sec)</em> changed to <em>Avg Correct Step Duration (sec)</em></li>
    <li><em>Avg. Error Step Duration (sec)</em> changed to <em>Avg Error Step Duration (sec)</em></li>
</ul>

<h4 class="date">August 2009</h4>
<ul>
    <li>Renamed <em>Avg Assistance Time</em> to <em>Avg Step Duration</em>.</li>
    <li>Renamed <em>Avg Correct Step Time</em> to <em>Avg Correct Step Duration</em>.</li>
    <li>Added <em>Avg Error Step Duration</em>.</li>
</ul>

</div>