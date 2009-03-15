

function divex_click(node) {
	//"console.dir(node);
	$.getScript('divex?nodeid='+node.id+'&action=click');
}

function divex_change(node, value) {
	$.getScript('divex?nodeid='+node.id+'&action=change&value='+escape(value));
}