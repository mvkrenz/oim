

/*value is optional for click event*/
function divex_click(id, value) {
	$.post("divex", 
			{ 	nodeid: id,
				action: "click",
				value : value },
			function(js) {
					eval(js);
			}
	);
}

function divex_change(id, value)
{
	$.post("divex", 
		{ 	nodeid: id,
			action: "change",
			value: value },
		function(js) {
				eval(js);
		}
	);
}