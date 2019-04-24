<%          // Author: Brett Leber
            // Version: $Revision: 5347 $
            // Last modified by: $Author: bleber $
            // Last modified on: $Date: 2009-02-23 15:53:55 -0500 (Mon, 23 Feb 2009) $
            // $KeyWordsOff: $
            //

            %>

	<h2>Sample Selector</h2>
        <p>Sample Selector is a tool for creating and editing
        <strong>samples</strong>, or groups of data you compare across&mdash;they're
        not "samples" in the statistical sense, but more like filters.</p>
        
        <p>By default, a single sample exists: "All Data". With the Sample
        Selector, you can create new samples to organize your data.</p>
        
        <p>You can use samples to:</p>
        <ul class="concise">
            <li>Compare across conditions</li>
            <li>Narrow the scope of data analysis to a specific time range,
            set of students, problem category, or unit of a curriculum (for example)
            </li>
        </ul>
        
        <p>A sample is composed of one or more <strong>filters</strong>, specific
        conditions that narrow down your sample.</p>
        
        <ul class="concise" style="font-weight:bold">
            <li><a href="#creating-a-sample">Creating a sample</a></li>
            <li><a href="#multiple-filters">The effect of multiple filters</a></li>
            <li><a href="#filter-operators">Filter Operator Reference</a> (eg, =, &lt;, Like, In)</li>
            <li><a href="#procedures">Procedures</a></li>
        </ul>
        
        <h3 id="creating-a-sample">Creating a sample</h3>
        <p>The general process for creating a sample is to:</p>
        <ul class="concise">
            <li>Add a filter from the categories at the left to the composition
            area at the right</li>
            <li>Modify the filter to select the subset of data you're interested 
            in, saving it when done</li>
            <li>View the sample preview table to see the effect of adding your filter,
            making sure you don't have an empty set (ie, a filter or combination
            of filters that exclude all transactions).</li> 
            <li>Name and describe the sample</li>
            <li>Decide whether to share the sample with others who can view the
            dataset</li>
            <li>Save the sample</li>
        </ul>
        
        <h3 id="multiple-filters">The effect of multiple filters</h3>
        <p>DataShop interprets each filter after the first as an additional
        restriction on the data that is included in the sample. This is also known
        as a logical "AND". You can see the results of multiple filters in the
        sample preview as soon as all filters are "saved". </p>
	
        <p>The columns available to filter on are organized into categories.  The 
	categories are:</p>
	<ul class="concise">
            <li>Condition</li>
            <li>Dataset Level</li>		
            <li>Problem</li>
            <li>School</li>
            <li>Student</li>
            <li>Tutor Transaction</li>
	</ul>

    
        <h3 id="filter-operators">Filter Operator Reference</h3>
        <ul class="concise">
            <li><strong>=</strong> Equals; needs to match exactly to include those rows</li>
            <li><strong>&ne;</strong> Not equal; an exact non-match</li>
            <li><strong>&lt;</strong> Less than</li>
            <li><strong>&gt;</strong> Greater than</li>
            <li><strong>&le;</strong> Less than or equal</li>
            <li><strong>&ge;</strong> Greater than or equal</li>
            <li><strong>In</strong> Includes rows where the column value is equal to any of several
            values in a specified list (a logical "OR"). Specify values in the form ('item one', 'item two', 'item three').</li>
            <li><strong>Not In</strong> Like "In", but only includes rows whose column value <i>does
            not</i> match any of the values in the list.
            <li><strong>Like</strong> Includes rows whose column value matches a pattern you define.
            Use the underscore character (_) to match any single character; and the percent sign (%) to
            match any number of characters (including no characters). Unquoted text is considered to be
            '%text%'. Single-quoted text will be matched exactly as it is entered.</li>
            <li><strong>Not Like</strong> Similar to "Like", but <i>excludes</i> rows with values that match
            the pattern.</li>
            <li><strong>Is Null</strong> Is not set; has no value. This is different than a value being
            <i>empty</i> ("").</li>
            <li><strong>Is Not Null</strong> Is set to something; has a value. This could be any value,
            including an <i>empty</i> value ("").</li>
        </ul>
        <p><strong>Note:</strong> Operators <strong>&ne;</strong>, <strong>Not In</strong>, and
        <strong>Not Like</strong> all <em>include</em> rows where the value being compared is
        null.</p>

<a id="procedures"></a>
    
	<h3>To define a new sample:</h3>
	<p>To define a new sample, click the New Sample button (<img 
            src="images/add_file.gif" alt="new sample icon" class="icon" />)to
	the right of the word Samples.</p>
	<p>Enter a name and description for your new sample.</p>
	<p>If you check 'Share this Sample', your sample will be viewable to
	all persons who have access to the current dataset. Only you will be
	able to modify or delete the sample.</p>
	<p><img src="images/alert.gif" alt="alert" class="icon" /> Note: the <strong>All
	Data</strong> sample cannot be deleted.</p>
    
	<h3>To add a column filter:</h3>
	<ol>
		<li>Select a column category using the combobox underneath the label
		Column Categories. Selecting a new column category updates the list of
		available columns directly beneath it.
		</li>
		<li>Select the desired column to use as a filter.</li>
		<li>Press <strong>Add</strong> to add the filter.</li>
	</ol>
    
	<h3>To modify a column filter:</h3>
	<ol>
		<li>Select the column filter from by clicking its row from the list of
		filtered columns.</li>
		<li>Select an <a href="#filter-operators">operator</a> (eg, =, &lt;, or Like).
		</li>
		<li>Press <strong>Set</strong> to update the filter.</li>
		<li>Repeat steps 1-3 to modify existing filters.</li>
		<li>Examine the Data Preview below for an idea of what your sample
		will look like. When you are happy with your sample, press <strong>Save</strong>
		to save the sample.</li>
		<li>Your sample now appears in the list of Samples in the sample list
		to the left of the Sample Selector.</li>
		<li>Close the Sample Selector by pressing the X in the upper-right
		corner.</li>
	</ol>
	<p><img src="images/alert.gif" alt="alert" class="icon" /> Note: More than one Column Filter
	acts as an 'AND' operation.  An example would be adding both a student id filter and a
	problem name filter.  This would return transactions that matched both the student
	<strong>and</strong> problem filters.  To do OR on the same column type 
	(i.e. Student 1 OR Student 2) use the 'In' operator described above.</p>
    
	<h3>To delete a column filter:</h3>
	<ul class="concise">
		<li>Click the delete icon (<img src="images/delete.gif" alt="alert"
			class="icon" />) to the right of the filter in the Filtered Columns
		section to remove just one filter or select the filter and press Remove.</li>
	</ul>
    
	<h3>To modify a sample:</h3>
	<ol>
		<li>Close the sample selector if it is open.</li>
		<li>Click the edit icon (<img src="images/edit.gif" alt="edit"
			class="icon" />) to the right of the name of the sample that you'd like to modify.</li>
	</ol>
    
	<h3>To delete a sample:</h3>
	<ol>
		<li>Close the sample selector if it is open.</li>
		<li>Click the delete icon (<img src="images/delete.gif"
			alt="trash can icon" class="icon" />) to the right of the sample
		name that you'd like to remove.</li>
	</ol>
