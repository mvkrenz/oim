

/*value is optional for click event*/
function divex(id, action, value) {
	/* .ajax doesn't support unicode
	 *
	 */
	/*
	$.ajax({
		  url: "divex",
		  cache: false,
		  data: { nodeid: id,
				action: action,
				value : value },
		  success: function(js){
					eval(js);
		  },
		  error: function() {
			  alert("Oops! We seems to have lost a connection to the server. Please try again.");
		  }
	});
	*/
		$.post("divex", 
				{ 	nodeid: id,
					action: action,
					value : value },
				function(js) {
						eval(js);
				}
		);

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