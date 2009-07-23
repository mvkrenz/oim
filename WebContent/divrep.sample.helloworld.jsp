<%@ page import="java.io.PrintWriter, com.webif.divrep.*, com.webif.divrep.common.*" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>DivRep on JSP</title>

<script type="text/javascript" src="divrep.js"></script>
<link href="css/divrep.css" rel="stylesheet" type="text/css"/>
<link href="css/divrep.samples.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="http://jqueryjs.googlecode.com/files/jquery-1.3.2.min.js"></script>

</head>
<body>

<%
DivRepPage pageroot = DivRepRoot.initPageRoot(request);
PrintWriter writer = new PrintWriter(out);

final DivRepButton button = new DivRepButton(pageroot, "Click Me");
button.addEventListener(new DivRepEventListener() {
	public void handleEvent(DivRepEvent e) {
		button.alert("Clicked via Event Listener!");
	}});
button.render(writer);
%>
</body>
</html>