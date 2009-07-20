package com.webif.divrep.common;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.DivRepEventListener;
import com.webif.divrep.common.DivRepButton.Style;

public class DivRepToggler extends DivRep {
	String show_html;
	String hide_html;
	String loading_html;
	
	DivRep content;
	Boolean show = false;

	//simple togger with content prefetched..
	public DivRepToggler(DivRep parent, DivRep _content) {
		super(parent);
		content = _content;
		
		//set generic show/hide buttons
		show_html = "Show Detail";
		hide_html = "Hide Detail";
		loading_html = "<p class=\"loading\">Loading...</p>";
	}
	
	public void setShow(Boolean _show)
	{
		show = _show;
	}
	public void setShowHtml(String html)
	{
		show_html = html;
	}
	public void setHideHtml(String html)
	{
		hide_html = html;
	}
	public void setLoading(String html)
	{
		loading_html = html;
	}
	
	//show (no content)
	// - clicks
	// - load content
	//hide (content)
	// - clicks (immidiately hide)
	//show (no content)
	// - clicks (immidiately show)
	
	public void render(PrintWriter out) 
	{
		if(show) {
			//initially shown
			out.write("<div id=\""+getNodeID()+"\">");
			
			//show button (hidden)
			out.write("<div id=\""+getNodeID()+"_show\" onclick=\"$('#"+content.getNodeID()+"').show();$('#"+getNodeID()+"_hide').show();$(this).hide();\" class=\"divrep_hidden\">");
			out.write(show_html);
			out.write("</div>");
			
			//hide button
			out.write("<div id=\""+getNodeID()+"_hide\" onclick=\"$('#"+content.getNodeID()+"').hide();$(this).hide();$('#"+getNodeID()+"_show').show();\">");
			out.write(hide_html);
			out.write("</div>");
			
			content.render(out);
		} else {
			//content initially not loaded
			out.write("<div id=\""+getNodeID()+"\" onclick=\"$('#"+getNodeID()+"_loading').show();divrep('"+getNodeID()+"', event)\">");
			out.write(show_html);
			
			//loading
			out.write("<div id=\""+getNodeID()+"_loading\" class=\"divrep_hidden\">");
			out.write(loading_html);
			out.write("</div>");
		}
		out.write("</div>");
	}

	protected void onEvent(DivRepEvent e) 
	{
		show = true;
		redraw();
	}
}
