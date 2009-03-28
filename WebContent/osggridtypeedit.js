//is this really used?
function setAutocomplete(node)
{
    node.each(function(i, dom) {

    	$(dom).autocomplete("divex?action=request&nodeid=" + dom.parentNode.id, {
	        mustMatch: true,
	        matchContains: true,
	        width: 300,
			formatItem: function(data, i, n, value) {
				return data[1] + "<br/>Email: " + data[2];
			},
			formatResult: function(data, value) {
				return " ";
			}
    	}); 
    });
    
    node.result(function(event, data, formatted) {
        divex(this.parentNode.id, "change", data[0]);
    });
}
