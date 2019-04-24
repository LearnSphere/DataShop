<%@page isErrorPage="true" %>
<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2008
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 12833 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-12-14 12:33:24 -0500 (Mon, 14 Dec 2015) $
// $KeyWordsOff: $
%>
<html>
<head>
<title>DataShop > Page Not Found</title>
<style type="text/css">
body, html{
	font-family:Arial, Verdana, Helvetica, sans-serif;
	color: #333;
	background-color:#37567f;
	margin-top:0;
	margin-right:0;
	height:100%;
}
#errorMsgTable {
	padding:0;
	margin:1em auto;
	width:24em;
	border:1px solid #666;
	background-color:#fff;
	border-collapse:collapse;
	position:absolute;
        top:65px;
}
.logo {
    background: #37567f;
    color: #FFF;
    height: 58px;
    width: 350px;
    display: inline-block;
    position: relative;
    font-size: 14px;
    font-family: 'Open Sans', sans-serif;
}
.logo h1 {
    margin: 0px 0px 0px 2px;
    font-size: 2em;
    font-weight: 400;
    letter-spacing: 0.03em;
}
.logo h1 span {
    font-weight: 300;
    font-style: italic;
    position: relative;
    left: -7px;
    top: 0px;
}
.logo span {
    font-size: 0.9em;
    font-style: italic;
    margin: 0px 0px 0px 2px;
    position: relative;
    top: -2px;
}
</style>
<%@ include file="/google-analytics.jspf" %>
</head>
<body>
<a href="index.jsp" title="DataShop Home">
<div class="logo">
     <h1>DataShop <span>@CMU<span></h1>
     <span>a data analysis service for the learning science community</span>
</div>
</a>
<table>
<tbody>
<tr><td>
    <table id="errorMsgTable">
    <th>Page not found.
    <p>
    If you'd like to report the problem, contact us by 
    <a href="help?page=contact">sending us an email</a>
    </p>
    <p>&mdash; The DataShop Team</p>
    </th>
    </table>
</td></tr>
</tbody>
</table>
</body>
</html>
