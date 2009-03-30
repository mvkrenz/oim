package com.webif.divex;

public class DialogDE extends DivEx {

	String message;
	String title;
	/*
	static public enum Style { BUTTON, ALINK };
	Style style = Style.BUTTON;
	public void setStyle(Style _style) { style = _style; }
	*/
	Boolean show = false;
	
	public DialogDE(DivEx parent, String _title, String _message) {
		super(parent);
		message = _message;
		title = _title;
	}
	
	public void open()
	{
		if(!show) {
			show = true;
			redraw();
		} else {
			//already shown - I just have to re-open it
			js("$('#"+getNodeID()+"').dialog('open');");
		}
	}

	public String render() {
		String html = "";
		html += "<div title=\""+title+"\" class=\"hidden\" id=\""+getNodeID()+"\">";
		if(show) {
			html += message;
	
			String js = "$('#"+getNodeID()+"').dialog({"+
				//"autoOpen: false,"+
				"buttons: {" +
					"Cancel: function() { "+ 
						"$(this).dialog(\"close\");"+
					"}, "+
					"Ok: function() { "+ 
						"divex('"+getNodeID()+"', 'click', 'ok');"+
						"$(this).dialog(\"close\");"+
					"}"+
				"}"+
			"});";
			
			html += "<script type='text/javascript'>";
			html += js;
			html += "</script>";
		}
		html += "</div>";
		
		return html;
	}

	protected void onEvent(Event e) {
		//dialog divex doesn't process any event - letting jquery handle this for now
	}
}
