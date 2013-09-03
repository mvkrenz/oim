
function setAutocomplete(node)
{
	$( node ).autocomplete({
		source: function( request, response ) {
			parent_id = node[0].parentNode.id;
			//$(node[0]).parents(".contact_editor").addClass("autocomplete_loading");
			$.ajax({
				url: "divrep?action=request&nodeid=" + parent_id,
				dataType: "json",
				data: {
					limit: 12,
					q: request.term
				},
				success: function( data ) {
					response( $.map( data, function( item ) {
						return {
							label: item.name + " <" + item.email + ">",
							value: item.name,
							id: item.id
						}
					}));
				}
			});
		},
		minLength: 2,
		delay: 300,
		select: function( event, ui ) {
			parent_id = node[0].parentNode.id;
			divrep(parent_id, "change", ui.item.id);
			/*
			log( ui.item ?
				"Selected: " + ui.item.label :
				"Nothing selected, input was " + this.value);
			*/
		},
		open: function() {
			$( this ).removeClass( "ui-corner-all" ).addClass( "ui-corner-top" );
		},
		close: function() {
			$( this ).removeClass( "ui-corner-top" ).addClass( "ui-corner-all" );
		}
	}).blur(function() {$(this).val("");});
}
