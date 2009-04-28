package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.servlet.ServletBase;

public class MenuView implements IView {
	private ArrayList<MenuItem> menu = new ArrayList<MenuItem>();
	private String current;
	
	public MenuView(Context context, String _current) {
		Authorization auth = context.getAuthorization();
		
		menu.add(new MenuItem("Home", "home"));
		
		if(auth.allows("edit_my_resource")) {
			menu.add(new MenuItem("Resource", "resource"));
		}
		
		if(auth.allows("edit_my_resource")) {
			menu.add(new MenuItem("Downtime", "resourcedowntime"));
		}
		
		if(auth.allows("edit_my_vo")) {
			menu.add(new MenuItem("Virtual Organization", "vo"));
		}
		
		if(auth.allows("edit_my_sc")) {
			menu.add(new MenuItem("Support Center", "sc"));
		}
		
		if(auth.allows("edit_my_contact")) {
			menu.add(new MenuItem("Contact", "contact"));
		}
		
		if(auth.getDNID() != null) {
			menu.add(new MenuItem("Profile", "profileedit"));
		}
		
		if(auth.getDNID() != null) {
			menu.add(new MenuItem("Log", "log"));
		}
		
		if(auth.allows("admin")) {
			menu.add(new MenuItem("Administration", "admin"));	
		}
		
		current = _current;
	}
	
	public void render(PrintWriter out) {		
		out.println("<div class=\"menu\">");
	    for(MenuItem item : menu) {
	    	String cls = "";
	    	if(item.url.compareTo(current) == 0) {
	    		cls = "selected";
	    	}
	    	out.println("<span class=\""+ cls +"\"><a href=\""+ Config.getApplicationBase() + "/" + item.url+"\">"+StringEscapeUtils.escapeHtml(item.name)+"</a></span>");
	    }
		out.println("</div>");
		
	}

}
