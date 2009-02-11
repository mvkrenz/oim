<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<script src="jquery/jquery-1.2.6.js" type="text/javascript"></script>
<script src="jquery/ui/ui.core.js" type="text/javascript"></script>

<script type="text/javascript">
function setkoukanevents()
{
	var nodes = $(".koukan[koukan_event!=true]");
	nodes.click(function() {
		$.getScript("koukan?nodeid="+this.id+"&action=click");
	}).attr("koukan_event", "true");
}

$(document).ready(function() {
	//set koukan event
	setkoukanevents();
	//load application
	$("#main").load("koukan?nodeid=main&action=load");
});
</script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

<div id="main" class="koukan">Loading Koukan Framework Test...</div>

</head>
<body>
<h1>Koukan Framework Demo</h1>
</body>
</html>