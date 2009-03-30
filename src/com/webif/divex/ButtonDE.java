package com.webif.divex;

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
	
	public String render() {
		String html = "";
		String clickonce_code = "";
		switch(style) {
		case BUTTON:
			if(clickonce) {
				clickonce_code = "$(this).attr(\"disabled\", \"disabled\").attr(\"value\", \"Loading...\");";
			}
			html += "<input type='button' id='"+getNodeID()+"' onclick='divex(this.id, \"click\");"+
				clickonce_code+"' value='"+Utils.strFilter(title)+"' />";
			break;
		case ALINK:
			//TODO - implement clickonce
			html += "<a href='#' id='"+getNodeID()+"' onclick='divex(this.id, \"click\");return false;'>"+Utils.strFilter(title)+"</a>";
			break;
		case IMAGE:
			//TODO - implement clickonce
			html += "<a href='#' id='"+getNodeID()+"' onclick='divex(this.id, \"click\");return false;'><img align='top' src='"+image_url+"' alt='"+
				Utils.strFilter(title)+"'/></a>";
			break;
		}

		return html;
	}

	//user should override this to intercept click event.
	//or use event listener
	protected void onEvent(Event e) {}
}
