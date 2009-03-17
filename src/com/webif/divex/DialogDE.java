package com.webif.divex;

public class DialogDE extends DivEx {

	String message;
	/*
	static public enum Style { BUTTON, ALINK };
	Style style = Style.BUTTON;
	public void setStyle(Style _style) { style = _style; }
	*/
	Boolean show = false;
	
	public DialogDE(DivEx parent, String _title, String _message) {
		super(parent);
		message = _message;
		setAttr("title", _title);
		setAttr("class", "hidden");
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

	public String toHTML() {
		String html = "";
		if(show) {
			html += message;
	
			String js = "$('#"+getNodeID()+"').dialog({"+
				//"autoOpen: false,"+
				"buttons: {" +
					"Cancel: function() { "+ 
						"$(this).dialog(\"close\");"+
					"}, "+
					"Ok: function() { "+ 
						"divex_click('"+getNodeID()+"', 'ok');"+
						"$(this).dialog(\"close\");"+
					"}"+
				"}"+
			"});";
			
			html += "<script type='text/javascript'>";
			html += js;
			html += "</script>";
		}
		
		return html;
	}
}
