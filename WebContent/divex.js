
var divex_processing_id = null;
function divexClearProcessing() {
	divex_processing_id = null;
	$(".divex_processing").removeClass("divex_processing");
}

function divex(id, event, value) {
	//make sure there is only one request at the same time (prevent double clicking of submit button)
	if(divex_processing_id == id) {
		//console.log('previous request on same target still running - ignoring');
		return;
	}
	divex_processing_id = id;
	
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
		    divexClearProcessing();
		},
	    error: function (XMLHttpRequest, textStatus, errorThrown) {
		   alert(textStatus.errorThrown);
		   divexClearProcessing();
	    }
	});
}

//this is basically the same thing as jquery.load, but instead of replace the content 
//of the div, it replace the whole div using replaceWith().
var divex_replace_counter = 0;
var divex_jscallback = null;
function divex_replace( node, url) 
{
	//count how many requests are there
	divex_replace_counter++;
	
	if(node.length == 0) {
		alert("couldn't find the node to replace with - maybe it's not wrapped with div?");
	}
	var self = node;
	// Request the remote document
	jQuery.ajax({
		url: url,
		type: "GET",
		cache: false,
		dataType: "html", //Returns HTML as plain text; included script tags are evaluated when inserted in the DOM. 
		complete: function(res, status){
			// If successful, inject the HTML into all the matched elements
			if ( status == "success" || status == "notmodified" ) {
				node.replaceWith(res.responseText);
			}
			--divex_replace_counter;
		}
	});
	return this;
}
function divex_runjs()
{
	if(divex_replace_counter == 0) {
		//console.log('counter is 0. running jscallack');
		divex_jscallback();
	} else {
		//console.log('waiting');
		setTimeout(divex_runjs, 50);
	}
}

//Firefox 3.0.10 (and may be others) has a bug where windows.location based redirect directly
//from the returned javascript causes the browser history to incorrectly enter entry and hitting
//back button will make the browser skip previous page and render previous - previous page.
//timeout will prevent this issue from happening.
var divex_redirect_url = null;
function divex_doRedirect()
{
	window.location = divex_redirect_url;
}
function divex_redirect(url)
{
	divex_redirect_url = url;
	setTimeout(divex_doRedirect, 0); //immediately call the timer
}