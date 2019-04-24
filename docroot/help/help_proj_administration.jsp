<%// Author: Alida Skogsholm
            // Version: $Revision: 11092 $
            // Last modified by: $Author: bleber $
            // Last modified on: $Date: 2014-05-27 12:42:21 -0400 (Tue, 27 May 2014) $
            // $KeyWordsOff: $
            //

            %>

	<h2>Project / Dataset Administration</h2>
    <ul class="concise">
        <li><a href="#pa-role">Project Admin role</a>
        <li><a href="#sharing-data">Sharing your data</a>
        <li><a href="#granting-access">Granting project access to users</a></li>
        <li><a href="#pi-v-pa">PI versus project admin</a></li>
        <li><a href="#access-requests">Access Requests</a>
            <ul>
            <li><a href="#my-requests">My Requests for Access</a></li>
            <li><a href="#requests-for-access">Requests for Access to My Projects</a></li>
            <li><a href="#responding">Responding to a Request</a></li>
            </ul>
        </li>
        <li><a href="#making-public">Making a project public</a></li>
    </ul>
    
    <h3 id="pa-role">Project Admin role</h3>
    
	<p>All projects in DataShop have one or more <em>project admins</em>. A <em>project 
    admin</em> has complete control over the project and the datasets in it, as shown below.</p>
	<p>When you create a new project in DataShop, you are automatically granted the the <em>project admin</em> role.</p>
    
    <h3 id="sharing-data">Sharing your data</h3>
    
    <p>As a project admin in DataShop, you have two options with regard to sharing your data:</p>
    
    <ul class="concise">
      <li><strong>keep your project private</strong> (DataShop default) so that no one can access the project unless you specifically grant access. 
         You may proactively <a href="#granting-access">grant access</a> to any individual researchers whom you choose.
         If a researcher <a href="#access-requests">requests access</a> to your project, you may grant or deny the request.</li>
      <li><strong><a href="#making-public">make your project public</a></strong>, which means that all registered DataShop users can access the data at any time, 
      without having to request access from you.</li>
    </ul>
    
    <p>Regardless of whether you want your data to be <strong>private</strong> or <strong>public</strong>, before you can choose 
    to <strong>share</strong> your project with anyone outside your research team, the DataShop Research Manager must first determine 
    that it is <a href="help?page=irb">shareable</a>. <strong>In other words, before a project has been determined to be shareable, you cannot make it public, 
    nor can you share it with anyone outside your research team (even if the project is private).</strong> Once your project has been determined to be shareable,
    you have the sharing options above. See our <a href="help?page=irb">help page on IRB and Data Shareability</a> 
    for more information on what constitutes "shareable".</p>

    <h3 id="granting-access">Granting project access to users</h3>
   <p>You can grant access to users from the <strong>Permissions</strong> page of a project for which you're the admin.
   Enter the DataShop username of the user, select the level of access you would like to grant them, and click <strong>Add</strong>.</p>
   <p>If your project is currently private, other DataShop users can request access to your project. You can respond
   to such requests on the Permissions page of the project or the <a href="AccessRequests">Access Requests</a> page.
   See <a href="#access-requests">Access Requests</a> below for more information.</p>

        <h4 id="auth-level-desc-heading">Access level descriptions</h4>
    <%@ include file="/auth_level_desc.jspf"%>

    <h3 id="pi-v-pa">PI versus project admin</h3>
    <p>Each project in DataShop also has a PI (principal investigator). The PI has the authority to
        review requests for access; however, only a user with the project admin role can add to the list of 
        users who have access. In general, a PI also needs the "admin" role (as defined above)
        to make changes to a project and its datasets.
    </p>

    <h3 id="access-requests">Access Requests</h3>
    <p>On the Access Requests page, you  can view responses to your requests for project access and respond to requests
    for access to your projects.</p>
    
    <h4 id="my-requests">My Requests for Access</h4>
    <p>This table lists the status of recent requests you have made. (You can make new requests
    from the <a href="index.jsp?datasets=other">private datasets</a> page.) If a request has been denied or if more than 24 hours
    have elapsed since the time you requested access and you haven't received a response,
    you can re-request access.</p>
    
    <h4 id="requests-for-access">Requests for Access to My Projects</h4>
    <p>This portion of the page is divided into three subtabs:</p>
    <ul class="concise">
        <li><strong>Not Reviewed</strong>. Requests that you need to act on by reviewing. 
        Click <strong>Respond</strong> to approve or deny the request. You can also modify the
        type of access (view, edit, or project admin) that the requester receives.</li>
        <li><strong>Recent Activity</strong>. Recent requests that you have responded to, or in the case
        of a project where there is a principal investigator or data provider other than you, requests 
        that he or she has responded to. Click an access level to modify your response.</li>
        <li><strong>Access Report</strong>. A report showing all users who have requested access to your
        projects, or who have had access at any point in time. You can sort by table headers or search the
        report. Click <strong>Export</strong> to export a version of the table that you can open in Excel
        or keep for your records. If you are the principal investigator or data provider for a public project,
        you will also see rows corresponding to users who have viewed datasets in your project(s).</li>
    </ul>
    <p>On any subtab, click the expand icon (<img src="images/expand.png" alt="icon" style="border:none;vertical-align:top" />) 
    to show the history for a user's request to a project, including your responses.</p>
    
    <h4 id="responding">Responding to a request</h4>
    <p>In the respond dialog, you can choose the type of access to grant or to deny access. You can also enter a short "reason", which can
    be shared with the requester. (Any text you enter here will be shared with the project's principal investigator or data provider, if one exists
    besides you.)</p>
    <p>To email the requester directly, click the requester's name.</p>

    <h4 id="making-public">Making a project public</h4>
    <p>Making a project public means any registered DataShop user can view it at any time (they do not need to request access from you).
    To make a project public, click the <strong>Make this project public</strong> button from the <strong>Permissions</strong> page of a project 
    for which you're the admin. If you are the PI or data provider for a project that has both a PI and data provider defined, 
    you can "vote" to make the project public, and the corresponding PI or data provider will have to vote similarly for the 
    change to take effect.</p>
    <p><strong>Note:</strong> A project can only be made public if it meets our <a href="help?page=irb">requirements for shareability</a>.</p>
    