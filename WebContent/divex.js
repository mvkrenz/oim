

/*value is optional for click event*/
function divex(id, action, value) {
	$.post("divex", 
			{ 	nodeid: id,
				action: action,
				value : value },
			function(js) {
					eval(js);
			}
	);
}
