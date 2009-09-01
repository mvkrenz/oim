
function setAutocomplete(node)
{
    node.each(function(i, dom) {

    	$(dom).autocomplete("divrep?action=request&nodeid=" + dom.parentNode.id, {
	        mustMatch: true,
	        matchContains: true,
	        width: 300,
	        delay: 100,
			formatItem: function(item) {
				return item[1] + "<br/>Email: " + item[2];
			},
			formatResult: function(item) {
				$(".ac_results").hide();//I believe thiss fixed the ghost ac_result window issue
				return " ";
			}
    	}); 
    });
    
    node.result(function(event, item) {
    	if(item != null) {
    		divrep(this.parentNode.id, "change", item[0]);
    	}
    });
}
