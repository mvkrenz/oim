package com.webif.divex;

import java.io.PrintWriter;

public class DialogDE extends DivEx {

	String message;
	String title;
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
			//already shown - I just have to re-open it (nothing needs to be re-drawn)
			js("$('#"+getNodeID()+"').dialog('open');");
		}
	}

	public void render(PrintWriter out) {
		String js = "";
		out.print("<div title=\""+title+"\" id=\""+getNodeID()+"\">");
		if(show) {

			out.print(message);
	
			js += "$('#"+getNodeID()+"').dialog({"+
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
			
			out.print("<script type='text/javascript'>");
			out.write(js);
			out.print("</script>");			

		}
		out.print("</div>");
	}

	protected void onEvent(Event e) {
		//dialog divex doesn't process any event - letting jquery handle this for now
	}
}
