
function setAutocomplete(node)
{
    node.each(function(i, dom) {

    	$(dom).autocomplete("divrep?action=request&nodeid=" + dom.parentNode.id, {
	        mustMatch: true,
	        matchContains: true,
	        width: 300,
	        delay: 100,
			formatItem: function(data, i, n, value) {
				return data[1] + "<br/>Email: " + data[2];
			},
			formatResult: function(data, value) {
				return " ";
			}
    	}); 
    });
    
    node.result(function(event, data, formatted) {
        divrep(this.parentNode.id, "change", data[0]);
    });
}
