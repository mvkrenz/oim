

/*value is optional for click event*/
function divex(id, action, value) {
	jQuery.ajax({
		url: "divex",
		async: false,
		data: { nodeid: id,
			action: action,
			value : value },
		type: "POST",
		dataType: "script",
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
		dataType: "html",
		complete: function(res, status){
			// If successful, inject the HTML into all the matched elements
			if ( status == "success" || status == "notmodified" )
				// See if a selector was specified
				node.replaceWith(res.responseText);
		}
	});
	return this;
}