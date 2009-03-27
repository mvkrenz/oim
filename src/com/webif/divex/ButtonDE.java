package com.webif.divex;

public class ButtonDE extends DivEx {
	String title;
	
	static public enum Style { BUTTON, ALINK, IMAGE };
	Style style = Style.BUTTON;
	public void setStyle(Style _style) { style = _style; }

	String image_url = null;
	public void setImageUrl(String _url) { image_url = _url; }
	
	public ButtonDE(DivEx parent, String _title) {
		super(parent);
		title = _title;
	}
	
	public String render() {
		String html = "";
	
		switch(style) {
		case BUTTON:
			html += "<input type='button' id='"+getNodeID()+"' onclick='divex(this.id, \"click\");' value='"+title+"' />";
			break;
		case ALINK:
			html += "<a href='#' id='"+getNodeID()+"' onclick='divex(this.id, \"click\");return false;'>"+title+"</a>";
			break;
		case IMAGE:
			html += "<a href='#' id='"+getNodeID()+"' onclick='divex(this.id, \"click\");return false;'><img align='top' src='"+image_url+"' alt='"+title+"'/></a>";
			break;
		}

		return html;
	}
}
