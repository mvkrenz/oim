package com.webif.divrep.common;

import java.io.PrintWriter;
import com.webif.divrep.DivRep;
import com.webif.divrep.Event;

public class Static extends DivRep {
	String html;
	
	public Static(DivRep parent, String _html) {
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
