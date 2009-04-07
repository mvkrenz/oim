package com.webif.divex;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

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
	
	Boolean confirm = false;
	String confirm_message = null;
	public void setConfirm(Boolean b, String message) {
		confirm = b;
		confirm_message = message;
	}
	
	public ButtonDE(DivEx parent, String _title) {
		super(parent);
		title = _title;
	}
	
	public void render(PrintWriter out) {
		//TODO implement click once feature - disabling button just doesn't work well

		String js = "divex(this.id, \"click\");return false;";
		if(confirm) {
			js = "var answer = confirm(\""+confirm_message+"\");if(answer) {"+js+"}";
		}
		
		switch(style) {
		case BUTTON:
			out.print("<input type='button' id='"+getNodeID()+"' onclick='"+js+"' value='"+
				StringEscapeUtils.escapeHtml(title)+"' />");
			break;
		case ALINK:
			//TODO - implement clickonce
			out.print("<a href='#' id='"+getNodeID()+"' onclick='"+js+"'>"+
				StringEscapeUtils.escapeHtml(title)+"</a>");
			break;
		case IMAGE:
			//TODO - implement clickonce
			out.print("<a href='#' id='"+getNodeID()+"' onclick='"+js+"'><img align='top' src='"+image_url+"' alt='"+
				StringEscapeUtils.escapeHtml(title)+"'/></a>");
			break;
		}
	}

	//user should override this to intercept click event.
	//or use event listener
	protected void onEvent(Event e) {}
}
