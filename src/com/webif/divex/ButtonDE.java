package com.webif.divex;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import edu.iu.grid.oim.view.Utils;

public class ButtonDE extends DivEx {
	String title;
	
	static public enum Style { BUTTON, ALINK, IMAGE };
	Style style = Style.BUTTON;
	public void setStyle(Style _style) { style = _style; }

	String image_url = null;
	public void setImageUrl(String _url) { image_url = _url; }
	
	//set this to false if you want user to be able to click multiple times
	Boolean clickonce = true;
	public void setClickOnce(Boolean b) { clickonce = b; }
	
	public ButtonDE(DivEx parent, String _title) {
		super(parent);
		title = _title;
	}
	
	public void render(PrintWriter out) {
		//TODO implement click once feature - disabling button just doesn't work well

		switch(style) {
		case BUTTON:
			out.print("<input type='button' id='"+getNodeID()+"' onclick='divex(this.id, \"click\");' value='"+
				StringEscapeUtils.escapeHtml(title)+"' />");
			break;
		case ALINK:
			//TODO - implement clickonce
			out.print("<a href='#' id='"+getNodeID()+"' onclick='divex(this.id, \"click\");return false;'>"+
				StringEscapeUtils.escapeHtml(title)+"</a>");
			break;
		case IMAGE:
			//TODO - implement clickonce
			out.print("<a href='#' id='"+getNodeID()+"' onclick='divex(this.id, \"click\");return false;'><img align='top' src='"+image_url+"' alt='"+
				StringEscapeUtils.escapeHtml(title)+"'/></a>");
			break;
		}
	}

	//user should override this to intercept click event.
	//or use event listener
	protected void onEvent(Event e) {}
}
