
<%          // Author: Brett Leber
            // Version: $Revision: 5230 $
            // Last modified by: $Author: bleber $
            // Last modified on: $Date: 2008-12-02 15:12:48 -0500 (Tue, 02 Dec 2008) $
            // $KeyWordsOff: $
            %>

<h2>Filtering Data</h2>

<p>Filter data in DataShop using the controls running vertically along the left side of the
screen. Each control provides a method for filtering the data that is included in the current
report.</p>

<p>You can filter data by:</p>

<ul class="concise">
    <li><a href="help?page=sampleSelector">Sample (created and edited via the Sample Selector)</a>&mdash;provides the
    ability to filter on various aspects of the data. Some tools in DataShop support comparing
    across samples (such as the Learning Curve and Performance Profiler tools).</li>
    <li>Knowledge Components (KCs)&mdash;include only data that are associated with any of the
    selected knowledge components. <a href="#kcsets">Save the set of selected KCs</a> for sharing with collaborators.</li>
    <li>Students&mdash;include only data that are associated with any of the selected students.</li>
    <li>Problems&mdash;include only data that are associated with any of the selected problems.</li>
</ul>

<p>Each filtering choice you make combines with the other filters to control which data are
included in the current report.</p>

<h3>Filtering by Knowledge Components</h3>

<p>Select knowledge components to include in the current report by selecting them in the
"Knowledge Components" navigation box. If a knowledge component box is checked, all transactions
associated with that knowledge component will be included in the current report.</p>

<h4 id="kcsets">KC Sets</h4>
<p>Save your current selected set of knowledge components by using the <strong>Manage
KC Sets</strong> dialog. Launch the <strong>Manage KC Sets</strong> by clicking the wrench icon (<img
    src="images/wrench.png" alt="Manage KC Sets icon" class="icon" />)at the top of the "Knowledge Components"
navigation box.</p>

<p>From the <span class="screen-item">Manage KC Sets</span> dialog, you can save the set of
selected KCs. (Selected KCs are indicated with checkmarks next to their name in the navigation
panel, and with dark borders around their thumbnails on the Learning Curve report.) By saving a set,
you'll be able to load the set again at anytime, and share it with collaborators.</p>

<p>You might create a set to:</p>
<ul class="concise">
    <li>Examine a subset of KCs across conditions</li>
    <li>Remove KCs that aren't worth investigating or including in reports because they are too
    easy or otherwise irrelevant.</li>
    <li>Quickly toggle between sets of KCs</li>
    <li>Isolate common KCs between study conditions</li>
    <li>Examine only some KCs that appear in a single condition</li>
</ul>

<p>KC Sets are always shared with others who can access the dataset. If a person doesn't own a
set, however, he or she can only load the set, not delete or rename it.</p>

<h3>Filtering by Students and Problems</h3>

<p>Select students to include in the current report by selecting them in the "Students"
navigation box. If a student box is checked, all transactions associated with that student will be
included in the current report.</p>

<p>Select problems to include in the current report by selecting them in the "Problems"
navigation box. If a problem box is checked, all transactions associated with that problem will be
included in the current report.</p>
