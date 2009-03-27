

/*value is optional for click event*/
function divex(id, action, value) {
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

}
