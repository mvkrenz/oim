package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.UUID;

public class BootTabView implements IView {
	
	LinkedHashMap<String, IView> tabs = new LinkedHashMap<String, IView>();
	UUID uuid = UUID.randomUUID();
	Boolean left = false;
	
	public BootTabView() {
	}
	
	public void setLeftTabl(Boolean left) {
		this.left = left;
	}
	
	public void addtab(String name, IView tab) {
		tabs.put(name,  tab);
	}
	
	public int size() {
		return tabs.size();
	}
	
	public void render(PrintWriter out) {
		//no tab, no show
		if(tabs.size() == 0) return;
		
		if(left) {
			out.write("<div class=\"tabbable tabs-left\">");
		}
		
		String id = uuid.toString();
		out.write("<ul class=\"nav nav-tabs\" id=\""+id.toString()+"\">\n");
		boolean first = true;
		Integer c = 0;
		for(String name : tabs.keySet()) {
			String tabid = id + "_" + c.toString();
			if(first) {
				out.write("<li class=\"active\"><a href=\"#"+tabid+"\" data-toggle=\"tab\">"+name+"</a></li>\n");
				first = false;
			} else {
				out.write("<li><a href=\"#"+tabid+"\" data-toggle=\"tab\">"+name+"</a></li>\n");
			}
			c++;
		}
		out.write("</ul>\n");
 
		out.write("<div class=\"tab-content\">\n");
		first = true;
		c = 0;
		for(IView tab : tabs.values()) {
			String tabid = id + "_" + c.toString();
			if(first) {
				out.write("<div class=\"tab-pane active\" id=\""+tabid+"\">\n");
				first = false;
			} else {
				out.write("<div class=\"tab-pane\" id=\""+tabid+"\">\n");
			}
			c++;
			tab.render(out);
	  		out.write("</div>\n");
		}
  		out.write("</div>\n");
  		
		if(left) {
			out.write("<div class=\"tabbable tabs-left\">");
		}
	}
}
