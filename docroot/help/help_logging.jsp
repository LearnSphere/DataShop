<%// Author: Alida Skogsholm
  // Version: $Revision: 15555 $
  // Last modified by: $Author: ctipper $
  // Last modified on: $Date: 2018-10-09 16:51:29 -0400 (Tue, 09 Oct 2018) $
  // $KeyWordsOff: $
  //
%>

    <h2>Logging New Data</h2>

        <p>There are a number of ways to log data from your course or study to DataShop. Important
        factors for selecting a method are the stage and technology of the tutoring system implementation.
        We'll examine a variety of cases in detail below, including each of their requirements and benefits.</p>

        <p>The definitive reference for the logging format accepted by DataShop is the <a
            href="/dtd/guide/">Guide to the Tutor Message format</a>.
        This guide is intended primarily for software developers
        who to want implement logging in an educational tutoring application, or convert existing logs created
        by a tutoring application.</p>

        <ul class="concise">
            <li><a href="#project-assignment">Getting Started - Project Assignment</a></li>
            <li><a href="#logging-urls">Logging URLs</a></li>
            <li><a href="#ctat-and-oli-based">CTAT- and OLI-based tutors</a></li>
            <li><a href="#java-tutors">Java tutors or educational applications not CTAT-
            or OLI-based</a></li>
            <li><a href="#cl">Tutors deployed via Carnegie Learning</a></li>
            <li><a href="#not-java-or-flash">Tutors not created in Java or Flash</a></li>
        </ul>


    <h3 id="project-assignment">Getting Started - Project Assignment</h3>
    <p>Prior to logging data, you may want to reserve the dataset and assign it to a project
    so that you can view your data without our assistance.
    Otherwise, you will need to <a href="help?page=contact">contact us</a> to obtain access
    to the data after logging begins. You can create a "placeholder" (or files-only) dataset for one of your projects
    by visiting the <a href="UploadDataset">Upload a dataset</a> page. Choose the project or create a new one,
    and elect for "No transaction data now". After the dataset is created, you can access
    the data as it becomes available from the logging servers.</p>

    <p><img src="images/help/create_dataset_in_project.png" /></p>

<h3 id="logging-urls">Logging URLs</h3>
    <p>Tutors or applications logging to DataShop should use one of the following URLs based on the
    status of data collection and the study in general. See the <a
    href="https://pslc-qa.andrew.cmu.edu/">log conversion schedule</a> for exact log conversion
    times. <strong>If you don't see your data, please <a href="help?page=contact">contact us</a>.</strong></p>

    <table id="table-logging-urls">
        <col style="width:20%"/>
        <col style="width:35%"/>
        <col />
        <thead>
            <tr>
                <th>Name</th>
                <th>URL</th>
                <th>Description</th></tr>
        </thead>
        <tbody>
            <tr>
                <td>DataShop <acronym title="Quality Assurance">QA</acronym></td>
                <td><strong>https://pslc-qa.andrew.cmu.edu/log/server</strong></td>
                <td>Log here to test that logging works from your application, course, or study, and
                to verify the content of your log messages. <strong style="color:rgb(207, 0,
                0)">This server is for test data only, not for data from real subjects. No research
                can be done on data collected on this server.</strong> Processed logs are generally
                available in the DataShop QA web application within two hours: <a
                href="https://pslc-qa.andrew.cmu.edu/datashop">https://pslc-qa.andrew.cmu.edu/datashop</a>
                </td>
            </tr>
            <tr>
                <td>DataShop Production</td>
                <td><strong>https://learnlab.web.cmu.edu/log/server</strong></td>
                <td>Log here to record real student data from a study or course. Processed logs are
                generally available in the DataShop Production web application the following day:
                <a href="https://pslcdatashop.web.cmu.edu/">https://pslcdatashop.web.cmu.edu/</a>
                </td>
            </tr>
        </tbody>
    </table>

    <h3 id="ctat-and-oli-based">CTAT- and OLI-Based Tutors</h3>

        <p>Any tutor created with the Cognitive Tutor Authoring Tools (CTAT) or Open Learning
        Initiative's (OLI) tools has logging functionality built-in.</p>
        <p>Information on configuring logging in these tools can be found 
        <a target="_blank" href="https://github.com/CMUCTAT/CTAT/wiki/Logging-Documentation">here</a>.</p>

            <h3 id="java-tutors">Java tutors or educational applications not CTAT- or OLI-based</h3>

            <p>If you are creating a Java tutor or educational application and would like to log to
            DataShop, download and use
            the existing <a href="/about/libraries.html">logging library</a>. The Java
            logging library includes well-documented APIs and sample applications for learning
            to integrate logging functionality into your application. Also see the <a
            href="/dtd/guide/">Guide to the Tutor Message format</a> for more on the
            logging format produced by the logging libraries.</p>

            <h3 id="cl">Tutors deployed via Carnegie Learning</h3>
            <p>Carnegie Learning provides logging of tutors deployed in their curriculum management system.
            To view these logs in DataShop, the logs must first be harvested from the local machines used in the
            study. As honest broker, Carnegie Learning then anonymizes the data and provides DataShop with
            converted logs.</p>
            <p>If you are running a study involving Carnegie Learning tutors and you would like to use
            DataShop, please <a href="help?page=contact">contact us</a> and inform us of
            the location of the study, who's involved, and the timing of the study. Early notice is appreciated.</p>

            <h3 id="not-java-or-flash">Tutors not created in Java or HTML</h3>
            <p>An existing project with sufficient technical resources may be best suited to implementing
            logging in a different language. If so, developers should consult the <a
            href="/dtd/guide/">Guide to the Tutor Message format</a>.
            In this case, the tutor should write log data to files, and the files can be zipped and <a
                href="help?page=contact">sent to us</a>.</p>
            <p><img src="images/alert.gif" alt="alert" class="icon" /> <strong>Note:</strong> DataShop
            currently supports versions 2 and 4 of the <a href="/dtd/">logging
            format</a>.</p>


    <table id="table-logging-urls">
        <col style="width:20%"/>
        <col style="width:35%"/>
        <col />
        <thead>
            <tr>
                <th>Name</th>
                <th>URL</th>
                <th>Description</th></tr>
        </thead>
        <tbody>
            <tr>
                <td>DataShop <acronym title="Quality Assurance">QA</acronym></td>
                <td><strong>https://pslc-qa.andrew.cmu.edu/log/server</strong></td>
                <td>Log here to test that logging works from your application, course, or study, and
                to verify the content of your log messages. <strong style="color:rgb(207, 0,
                0)">This server is for test data only, not for data from real subjects. No research
                can be done on data collected on this server.</strong> Processed logs are generally
                available in the DataShop QA web application within two hours: <a
                href="https://pslc-qa.andrew.cmu.edu/datashop">https://pslc-qa.andrew.cmu.edu/datashop</a>
                </td>
            </tr>
            <tr>
                <td>DataShop Production</td>
                <td><strong>https://learnlab.web.cmu.edu/log/server</strong></td>
                <td>Log here to record real student data from a study or course. Processed logs are
                generally available in the DataShop Production web application the following day:
                <a href="https://pslcdatashop.web.cmu.edu/">https://pslcdatashop.web.cmu.edu/</a>
                </td>
            </tr>
        </tbody>
    </table>
