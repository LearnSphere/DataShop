<h2>R (software)</h2>
<p>R is a language and <a href="http://www.r-project.org/index.html">free software
environment</a> for statistical computing and graphics. If you're familiar with R, you can easily open a
DataShop <a href="help?page=stepRollup">student-step export</a> or 
<a href="help?page=export#table-by-tx">transaction export</a> stored on your computer. 
The command to do so is shown below:</p>

<pre>R&gt; ds = read.delim("export.txt", header = TRUE, quote="\"", dec=".", fill = TRUE, comment.char="")</pre>

<p>The above command reads the tab-delimited file and stores it in a "data frame" object called
<code>ds</code> (you could use any variable name here). The function <code>read.delim()</code> is shorthand for
<code>read.table()</code> with some default values for the various arguments to <code>read.table()</code>.
This command works because the student-step and transaction export files have a form that R expects
for data frames:
<ul class="concise">
    <li>the first line of the file has a name for each variable in the data frame</li>
    <li>each additional line of the file has as its first item a row label and the values for
    each variable.</li>
</ul>
<p>You can graphically view the data frame with the command <code>edit(ds)</code>.</p>

<p><strong>Note:</strong> Each of the above commands loads data into R and implicitly tells R that all columns
that appear as strings should be considered factors (for factor analysis). Although convenient, this can be problematic 
for some types of analysis. To avoid this issue, append the parameter <code>stringsAsFactors="false"</code> to one of the 
above commands. Then identify individual columns as factors using the R syntax <code>dat$col = factor(dat$col)</code>, 
where <code>dat</code> is the data frame and <code>col</code> is the column name.</p>

<p>At this point, you can work with the data in R to perform any analysis you'd like.</p>

<h3>Using R to replicate the AFM model</h3>

    <!-- Redundantly included from page=modelValues -->
   <p>Using R notation, the AFM model (applied to a modified student-step export file called "ds") 
    can be approximately* represented as:</p>
    <pre style="margin-left:2em">R&gt; L = length(ds$Anon.Student.Id)</pre>
    <pre style="margin-left:2em">R&gt; success = vector(mode="numeric", length=L)</pre>
    <pre style="margin-left:2em">R&gt; success[ds$First.Attempt=="correct"]=1</pre>
    <pre style="margin-left:2em">R&gt; model1.lmer &lt;- lmer(success~knowledge_component+
   knowledge_component:opportunity+(1|anon_student_id),data=ds,family=binomial())</pre>
   
    <p><strong>Note:</strong> The <code>success</code> variable must be 0 or 1. The first three R commands simply convert the "First Attempt" values (in the student-step export) of "incorrect" and "hint" to 0, and "correct" to "1".</p>
   
    <p><strong>*</strong> The AFM code is different from the R expression above in two ways:</p>
    <ol><li>To reduce over-fitting the data, AFM assumes learning cannot be negative and thus constraints the 
    "slope" estimates of the <code>knowledge_component:opportunity</code> parameters to be greater or equal to 0.<br />
    </li><li>The optimization applies a penalty to estimates of the student parameters (<code>anon_student_id</code>)
    for deviating from 0&mdash;essentially treating <code>anon_student_id</code> as a random effect.</li></ol>

    <p>The above R code, with additional analyses, is available for download <a href="/downloads/DataShop-AFM.R">here</a>.</p>    
    <p>For more about the AFM model, see the <a href="help?page=modelValues">Model Values help page</a>.</p>

<h3>Using DataShop Web Services to access DataShop data in R</h3>

<p>The following R commands show how you can access DataShop data via web services from R. Windows paths are shown in the example.
A few prerequisites are needed for this script to run successfully:</p>
<ul class="concise">
    <li>Get access to web services, including your access keys, at the 
    <a href="/WebServicesCredentials">Web Services Credentials</a> page.</li>
    <li>Download and extract the <a href="/about/webservices.html#java-client">DataShop sample web services client</a>
    to <strong>C:\ws</strong></li>
    <li>Enter your access key ID in the file <strong>C:\ws\webservices.properties</strong> in the line that starts
    <code>api.token=</code>, and enter your secret access key in the line that starts <code>secret=</code>. Save the file.</li>
    <li>Edit the script below by specifying a <strong>datasetid</strong> for a dataset you have access to. This is the 
    number that appears in the URL of DataShop when browsing a dataset (e.g., 
    https://pslcdatashop.web.cmu.edu/DatasetInfo?<strong>datasetId=123</strong>). Also
    set the <strong>datasetlength</strong> to a number higher than the number of student-step rows.</li>
</ul>

<p>This code will retrieve transaction data for only the columns specified in <strong>colsneeded</strong>.
It retrieves the transaction data in batches of 5000. For a full reference of possible columns and data formats, 
see our <a href="/about/webservices.html#api">Web Services API</a>.

    <pre style="margin-left:3em">setwd('C:/ws/')
df&lt;-data.frame()

colsneeded &lt;- "row,anon_student_id,session_id,time,duration,problem_name,attempt_at_step,outcome,condition"
datasetid &lt;- 123
datasetlength &lt;- 40000
datasetlength &lt;- datasetlength/5000

for (i in 0:(datasetlength-1)) {
  callval &lt;- paste("java -jar C:/ws/dist/datashop-webservices.jar \"https://pslcdatashop.web.cmu.edu/services/datasets/",
    datasetid,"/transactions?offset=",
    i*5000,"&limit=5000&cols=",colsneeded,"\"",sep="")

  tablines &lt;- read.delim(pipe(callval),  header = TRUE, sep = "\t")

  df &lt;- rbind(df,tablines)
  print(paste("Group",i,"completed."))
}</pre>

<h3>Useful Links</h3>
<ul class="concise">
    <li><a href="http://cran.r-project.org/doc/manuals/R-data.html">R Data Import/Export
    guide</a></li>
    <li><a href="http://www.r-project.org/index.html">R project homepage</a></li>
    <li><a href="http://pslcdatashop.org/api/">API for DataShop Web Services</a></li>
    <li><a href="http://pslcdatashop.org/about/webservices.html#javaclient">Sample Java Client</a></li>
</ul>


<p class="clearFloat"></p>