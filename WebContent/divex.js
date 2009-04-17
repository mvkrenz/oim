

function divex(id, event, value) {
	//stop bubble
	if(!event) var event = window.event;//IE
	if(event) {
		event.cancelBubble = true;//IE
		if (event.stopPropagation) event.stopPropagation();//Standard
	} else {
		event = new Object();
		event.type = "unknown";
	}
	
	jQuery.ajax({
		url: "divex",
		async: false,
		data: { nodeid: id,
			action: event.type,
			value : value },
		type: "POST",
		dataType: "script",//Evaluates the response as JavaScript and returns it as plain text. Disables caching unless option "cache" is used. Note: This will turn POSTs into GETs for remote-domain requests. 
	   success: function(msg){
	   },
	   error: function (XMLHttpRequest, textStatus, errorThrown) {
		   // typically only one of textStatus or errorThrown 
		   // will have info
		   alert(textStatus.errorThrown);
		   this; // the options for this ajax request
	   }
	});
}

//this is basically the same thing as jquery.load, but instead of replace the content 
//of the div, it replace the whole div using replaceWith().
function divex_replace( node, url ) 
{
	var self = node;
	// Request the remote document
	jQuery.ajax({
		url: url,
		type: "GET",
		cache: false,
		dataType: "html", //Returns HTML as plain text; included script tags are evaluated when inserted in the DOM. 
		complete: function(res, status){
			// If successful, inject the HTML into all the matched elements
			if ( status == "success" || status == "notmodified" )
				// See if a selector was specified
				node.replaceWith(res.responseText);
		}
	});
	return this;
}