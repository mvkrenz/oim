package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

public class BootTabView implements IView {
	
	UUID uuid = UUID.randomUUID();
	Boolean left = false;
	
	public class Tab {
		String name;
		IView view;
		boolean pull;
	}
	ArrayList<Tab> tabs = new ArrayList<Tab>();
	Tab active_tab = null;
	public void setActive(Tab tab) {
		active_tab = tab;
	}
	
	public BootTabView() {
	}
	
	public void setLeftTabl(Boolean left) {
		this.left = left;
	}
	
	public Tab addtab(String name, IView view) {
		return addtab(name, view, false);
	}
	public Tab addtab(String name, IView view, boolean pull) {
		Tab tab = new Tab();
		tab.name = name;
		tab.view = view;
		tab.pull = pull;
		tabs.add(tab);
		
		//TODO select first tab by default
		if(active_tab == null) {
			active_tab = tab;
		}
		return tab;
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
		Integer c = 0;
		for(Tab tab : tabs) {
			String tabid = id + "_" + c.toString();
			String cls = "";
			if(tab == active_tab) {
				cls += "active "; 
			}
			if(tab.pull) {
				cls += "pull-right "; 
			}
			out.write("<li class=\""+cls+"\"><a href=\"#"+tabid+"\" data-toggle=\"tab\">"+tab.name+"</a></li>\n");
			
			c++;
		}
		out.write("</ul>\n");
 
		out.write("<div class=\"tab-content\">\n");
		c = 0;
		for(Tab tab : tabs) {
			String tabid = id + "_" + c.toString();
			String cls = "";
			if(tab == active_tab) {
				cls += "active ";
			}
			out.write("<div class=\"tab-pane "+cls+"\" id=\""+tabid+"\">\n");
			tab.view.render(out);
	  		out.write("</div>\n");
	  		
			c++;
		}
  		out.write("</div>\n");
  		
		if(left) {
			out.write("<div class=\"tabbable tabs-left\">");
		}
	}

}
