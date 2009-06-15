package com.webif.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divrep.Button.Style;

public class Toggler extends DivRep {
	Button show_button;
	Button hide_button;
	DivRep content;
	Boolean show = false;
	Toggler me;

	public Toggler(DivRep parent, DivRep _content, Boolean _show) {
		super(parent);
		show = _show;
		me = this;
		content = _content;
		show_button = new Button(this, "Show Detail");
		show_button.setStyle(Style.ALINK);
		show_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				show = true;
				me.redraw();
			}});
		hide_button = new Button(this, "Hide Detail");
		hide_button.setStyle(Style.ALINK);
		hide_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				show = false;
				me.redraw();
			}});
	}
		
	public Toggler(DivRep parent, DivRep _content) {
		super(parent);
		me = this;
		content = _content;
		show_button = new Button(this, "Show Detail");
		show_button.setStyle(Style.ALINK);
		show_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				show = true;
				me.redraw();
			}});
		hide_button = new Button(this, "Hide Detail");
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
