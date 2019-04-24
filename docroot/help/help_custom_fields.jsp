<%// Author: Alida Skogsholm
            // Version: $Revision: 12254 $
            // Last modified by: $Author: mkomisin $
            // Last modified on: $Date: 2015-04-23 12:29:39 -0400 (Thu, 23 Apr 2015) $
            // $KeyWordsOff: $
%>

<h2>Custom Fields</h2>

<p>A custom field is a new column you define for annotating transaction data.
DataShop currently supports adding and modifying custom fields at the transaction level.</p>
<p>You can add custom fields when logging or importing data, or after a dataset exists
(using the Custom Fields page and/or web services).</p>
<p>Many tutors have been instrumented to record custom fields while logging.
Some examples include a field that captures the time of each tutor response
to the millisecond; a field noting the agent that took the action in a multi-agent system;
and a field recording a categorization of the problem the student is working on.
Data mining tools can be used to generate new information about an existing dataset, and the
output of those programs can be stored in custom fields without having to create a new
dataset.    </p>

<p>You can add or modify a custom field's metadata from the Custom Fields page (Dataset Info &gt; Custom Fields),
but to set the data in that custom field, you need to use <a href="/about/webservices.html">web
services</a>, which is a way to interact with DataShop through a program you write.</p>

<h3>Permissions</h3>
<p>A custom field has an owner, the user who created it. Users who have edit or
admin permission for a project can create custom fields for a dataset in it.
Only the owner or a DataShop administrator can delete or modify the custom field.
Only DataShop administrators can delete custom fields that were logged with the data.</p>

<h3>Custom Field Metadata</h3>
<p>The following fields describe a custom field:</p>
<ul class="concise">
    <li><strong>name</strong>&mdash;descriptive name for the new custom field. Must be unique across all custom fields
    for the dataset. Must be no more than 255 characters.</li>
    <li><strong>description</strong>&mdash;description for the new custom field. Must be no more than 500 characters.</li>
    <li><strong>level</strong>&mdash;the level of aggregation that the custom field describes. Currently, the only accepted value
    is transaction. Future versions may support other levels such as step or student.
    Cannot be modified later.</li>
</ul>

<h3>Data Types</h3>
<p>A custom field value is classified as one or more of the following data types assigned internally by DataShop:</p>
<ul class="concise">
    <li><strong>number</strong>&mdash;must be no more than 65,000 characters</li>
    <li><strong>string</strong>&mdash;must be no more than 65,000 characters</li>
    <li><strong>date</strong>&mdash;see <a href="/about/importverify.html#note-2">format suggestions</a>;
    must be no more than 255 characters</li>
</ul>
<p>The Custom Fields page indicates the types of custom fields, what percentage of those custom fields
fall into the aforementioned categories, and what percentage of transactions are associated with each
custom field.</p>
<p><strong>Caveat:</strong> Very large custom fields may cause unexpected behavior in some applications.  Excel correctly handles
exports with very large custom field values if you import the text from Excel. Other text editors may incorrectly wrap the
text values if they become too large while programs like vim, jEdit, and Notepad++ correctly handle the maximum lengths. Additionally,
when viewing custom fields in the web interface, the values are truncated to 255 characters to prevent issues with browsers. To
get the full custom field value, use the <strong>transaction export</strong> feature.</p>
<p><a href="help?page=export#excel_warning"> Read
our recommended method for opening exported data in Excel</a></p>