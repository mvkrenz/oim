package com.webif.divex;

import java.io.PrintWriter;

public class StaticDE extends DivEx {
	String html;
	
	public StaticDE(DivEx parent, String _html) {
		super(parent);
		html = _html;
	}
	
	public void render(PrintWriter out) {
		out.print(html);
	}
	public void setHtml(String _html) {
		html = _html;
	}

	protected void onEvent(Event e) {
		//static doesn't handle any event
	}
}
