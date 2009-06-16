
var divrep_processing_id = null;
function divrepClearProcessing() {
	divrep_processing_id = null;
	$(".divrep_processing").removeClass("divrep_processing");
}

function divrep(id, event, value) {

	//make sure there is only one request at the same time (prevent double clicking of submit button)
	if(divrep_processing_id == id) {
		//previous request on same target still running - ignore;
		return;
	}
	
	//weird thing about the browser's event handling is that, although javascript is single threaded,
	//for some reason browser start running event handler while another handler is still executing.
	//we need to make sure that this doesn't happen.
	if(divrep_processing_id != null) {
		//wait until the previous processing ends
		setTimeout(function() { divrep(id, event, value);}, 100);
		return;
	}
	
	divrep_processing_id = id;
	
	//stop bubble
	if(!event) var event = window.event;//IE
	if(event) {
		event.cancelBubble = true;//IE
		if (event.stopPropagation) event.stopPropagation();//Standard
	} else {
		event = new Object();
		event.type = "unknown";
	}
	
	console.log("calling jQuery.ajax for " + id);
	jQuery.ajax({
		url: "divrep",
		async: false,
		data: { nodeid: id,
			action: event.type,
			value : value },
		type: "POST",
		dataType: "script",//Evaluates the response as JavaScript and returns it as plain text. Disables caching unless option "cache" is used. Note: This will turn POSTs into GETs for remote-domain requests. 
	    success: function(msg){
		    divrepClearProcessing();
		},
	    error: function (XMLHttpRequest, textStatus, errorThrown) {
		   alert(textStatus.errorThrown);
		   divrepClearProcessing();
	    }
	});
	console.log("end jQuery.ajax for " + id);
}

//this is basically the same thing as jquery.load, but instead of replacing the content 
//of the div, it replace the whole div using replaceWith().
var divrep_replace_counter = 0;
function divrep_replace( node, url) 
{
	//count how many requests are there
	divrep_replace_counter++;
	
	if(node.length == 0) {
		alert("couldn't find the divrep node - maybe it's not wrapped with div?");
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
			--divrep_replace_counter;
		}
	});
	return this;
}

var divrep_jscallback = null;
function divrep_runjs()
{
	if(divrep_replace_counter == 0) {
		divrep_jscallback();
	} else {
		//retry later..
		setTimeout(divrep_runjs, 100);
	}
}

//Firefox 3.0.10 (and may be others) has a bug where windows.location based redirect directly
//from the returned javascript causes the browser history to incorrectly enter entry and hitting
//back button will make the browser skip previous page and render previous - previous page.
//timeout will prevent this issue from happening.
var divrep_redirect_url = null;
function divrep_redirect(url)
{
	divrep_redirect_url = url;
	setTimeout(divrep_doRedirect, 0); //immediately call the timer
}
function divrep_modified(mod)
{
	if(mod) {
		window.onbeforeunload = divrep_confirm_close;
	} else {
		window.onbeforeunload = null;
	}
}
function divrep_confirm_close()
{
    return "You have not submitted the changes you made on this form.";
}
function divrep_doRedirect()
{
	try {
		window.location.href = divrep_redirect_url;
	} catch(error) {
		//IE7 blows up if user cancel the onbeforeunload confirmation invoked by window redirect
		//this block silences IE7
	}
}

