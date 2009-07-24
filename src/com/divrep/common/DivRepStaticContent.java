package com.divrep.common;

import java.io.PrintWriter;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;

public class DivRepStaticContent extends DivRep {
	String html;
	
	public DivRepStaticContent(DivRep parent, String _html) {
		super(parent);
		html = _html;
	}
	
	public void render(PrintWriter out) {
		out.print(html);
	}
	public void setHtml(String _html) {
		html = _html;
	}

	protected void onEvent(DivRepEvent e) {
		//static doesn't handle any event
	}
}
