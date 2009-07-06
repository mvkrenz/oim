package com.webif.divrep.common;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.DivRepEventListener;
import com.webif.divrep.common.DivRepButton.Style;

public class DivRepToggler extends DivRep {
	DivRepButton show_button;
	DivRepButton hide_button;
	DivRep content;
	Boolean show = false;
	DivRepToggler me;

	public DivRepToggler(DivRep parent, DivRep _content, Boolean _show) {
		super(parent);
		show = _show;
		me = this;
		content = _content;
		show_button = new DivRepButton(this, "Show Detail");
		show_button.setStyle(Style.ALINK);
		show_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				show = true;
				me.redraw();
			}});
		hide_button = new DivRepButton(this, "Hide Detail");
		hide_button.setStyle(Style.ALINK);
		hide_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				show = false;
				me.redraw();
			}});
	}
	
	//let client customize the button
	public DivRepButton getShowButton()
	{
		return show_button;
	}
	public DivRepButton getHideButton()
	{
		return hide_button;
	}
		
	public DivRepToggler(DivRep parent, DivRep _content) {
		super(parent);
		me = this;
		content = _content;
		show_button = new DivRepButton(this, "Show Detail");
		show_button.setStyle(Style.ALINK);
		show_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				show = true;
				me.redraw();
			}});
		hide_button = new DivRepButton(this, "Hide Detail");
		hide_button.setStyle(Style.ALINK);
		hide_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
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

	protected void onEvent(DivRepEvent e) 
	{
		//no event
	}
}
