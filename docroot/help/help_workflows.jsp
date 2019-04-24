<h2>Workflows</h2>

<p>A workflow is a component-based process model that can be used to analyze, manipulate, or visualize data. Each component acts as a stand-alone program with its own inputs, options, and outputs. The inputs to each component can be data or files, and the output of each component is made available after the workflow has been run. A generic workflow might consist of the following steps.</p>
<ol class="concise">
    <li><b><i>import</i></b> a tab-delimited file</li>
    <li><b><i>analyze</i></b> the file</li>
    <li><b><i>visualize</i></b> the results of the analysis component</li>
</ol>

<p>
A component is essentially a black box which receives data from other components and carries out an arbitrary task which results in the generation of new data. The newly generated data is then passed to the component's successor. Some common component types could be <i>analyses</i>, <i>imports</i>, <i>transformations</i>, and <i>visualizations</i>.
</p>

<p>Every component has a set of required inputs and user-defined options. The constraints on inputs and options are defined in each component's XML schema definition (XSD). The XSD file defines the structure of a component. Each component is then defined by it's XSD and run-time program. XML is used by the components to communicate with one another.
</p>

<div id="wf-videos">
<p>Checkout the "How-To" videos created by some of the early users of the workflow tool.</p>
<table>
<tr>
<td><iframe src="https://www.youtube.com/embed/6aUET-Qg0PQ" frameborder="0" allowfullscreen></iframe></td>
<td><iframe src="https://www.youtube.com/embed/Jt03TTywT8Y" frameborder="0" allowfullscreen></iframe></td>
<td><iframe src="https://www.youtube.com/embed/KtUljshNZhg" frameborder="0" allowfullscreen></iframe></td>
</tr>
<tr>
<td>LearnSphere Workflow Tools: Overview</td>
<td>BKT Analysis using Tigris Workflows</td>
<td>Creating new workflow components</td>
</tr>
</table>
</div>

<br/>

<p>
    <strong>Note:</strong> At this time, any new workflow components must be approved by a
    DataShop Administrator. Please <a href="help?page=contact">contact us</a> if you have a component you'd
    like to have added to DataShop.
</p>

<p class="clearFloat"></p>
