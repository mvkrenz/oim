package com.webif.divex;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.ButtonDE.Style;

public class TogglerDE extends DivEx {
	ButtonDE show_button;
	ButtonDE hide_button;
	DivEx content;
	Boolean show = false;
	TogglerDE me;
	
	public TogglerDE(DivEx parent, DivEx _content) {
		super(parent);
		me = this;
		content = _content;
		show_button = new ButtonDE(this, "Show Detail");
		show_button.setStyle(Style.ALINK);
		show_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				show = true;
				me.redraw();
			}});
		hide_button = new ButtonDE(this, "Hide Detail");
		hide_button.setStyle(Style.ALINK);
		hide_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				show = false;
				me.redraw();
			}});
	}
	
	public void render(PrintWriter out) 
	{
		out.write("<div id=\""+getNodeID()+"\">");
		if(show) {
			hide_button.render(out);
			content.render(out);
		} else {
			show_button.render(out);
		}
		out.write("</div>");
	}

	protected void onEvent(Event e) 
	{
		//no event
	}
}
