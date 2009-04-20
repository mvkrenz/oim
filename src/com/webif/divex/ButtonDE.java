package com.webif.divex;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

public class ButtonDE extends DivEx {
	String title;
	
	static public enum Style { BUTTON, ALINK, IMAGE };
	Style style = Style.BUTTON;
	public void setStyle(Style _style) { style = _style; }
	
	Boolean confirm = false;
	String confirm_message = null;
	public void setConfirm(Boolean b, String message) {
		confirm = b;
		confirm_message = message;
	}
	
	//if the button style is IMAGE, _title will be used as URL
	public ButtonDE(DivEx parent, String _title) {
		super(parent);
		title = _title;
	}
	
	public void render(PrintWriter out) {
		//TODO implement click once feature - disabling button just doesn't work well

		String js = "if(!$(this).hasClass(\"disabled\")) { $(this).addClass(\"disabled\"); $(this).attr(\"value\", \"Processing..\"); divex(this.id, event); } return false;";
		if(confirm) {
			js = "var answer = confirm(\""+confirm_message+"\");if(answer) {"+js+"}";
		}
		
		switch(style) {
		case BUTTON:
			out.print("<input type='button' id='"+getNodeID()+"' onclick='"+js+"' value='&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
				StringEscapeUtils.escapeHtml(title)+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' />");
			break;
		case ALINK:
			out.print("<a href='#' id='"+getNodeID()+"' onclick='"+js+"'>"+
				StringEscapeUtils.escapeHtml(title)+"</a>");
			break;
		case IMAGE:
			out.print("<a href='#' id='"+getNodeID()+"' onclick='"+js+"'><img src='"+title+"' alt='button'/></a>");
			break;
		}
	}

	//user should override this to intercept click event.
	//or use event listener
	protected void onEvent(Event e) {}
}
