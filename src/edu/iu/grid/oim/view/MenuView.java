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
		
		// Always show home page with information about what OIM is... how can we make this better? -agopu
		menu.add(new MenuItem("Home", "home"));

		// TODO Need a clean way to show a nice page when unregistered user shows up with links 
		//   to MYOSG for public OIM information, etc. -agopu
		if(auth.getDNID() == null && auth.getUserCN() != null && auth.getUserCN() != null) {
			menu.add(new MenuItem("Register to OIM", "register"));
		}
		
		if(auth.allows("edit_my_vo")) {
			menu.add(new MenuItem("Virtual Organizations", "vo"));
		}
		
		if(auth.allows("edit_my_sc")) {
			menu.add(new MenuItem("Support Centers", "sc"));
		}

		if(auth.allows("edit_my_resource")) {
			menu.add(new MenuItem("Resources", "resource"));
		}
		
		if(auth.allows("edit_my_resource")) {
			menu.add(new MenuItem("Downtime", "resourcedowntime"));
		}
		
		// Do we need auth checks for these given we will allow anyone to edit? I guess not .. -agopu
		if(auth.allows("edit_all_resource_group")) {
			menu.add(new MenuItem("Resource Groups", "resourcegroup"));	
		}
		if(auth.allows("edit_all_site")) {
			menu.add(new MenuItem("Sites", "site"));	
		}
		if(auth.allows("edit_all_facility")) {
			menu.add(new MenuItem("Facilities", "facility"));	
		}
		if(auth.allows("edit_my_contact")) {
			menu.add(new MenuItem("Contacts", "contact"));
		}
		
		if(auth.getDNID() != null) {
			menu.add(new MenuItem("Profile", "profileedit"));
		}
		if(auth.getDNID() != null) {
			menu.add(new MenuItem("Log", "log"));
		}
		if (auth.allows("edit_measurement")) {
			menu.add(new MenuItem("CPU Info", "cpuinfo"));	
		}
		if (auth.allows("admin")) {
			menu.add(new MenuItem("Admin", "admin"));	
		}
		
		menu.add(new MenuItem("Help", "help"));

		current = _current;
	}
	
	public void render(PrintWriter out) {		
		out.println("<div class=\"menu\">");
	    for(MenuItem item : menu) {
	    	String cls = "";
	    	if(item.url.compareTo(current) == 0) {
	    		cls = "selected";
	    	}
	    	//out.println("<span class=\""+ cls +"\"><a href=\""+ Config.getApplicationBase() + "/" + item.url+"\">"+StringEscapeUtils.escapeHtml(item.name)+"</a></span>");
	    	out.println("<span class=\"link "+ cls +"\" onclick=\"divex_redirect('"+ Config.getApplicationBase() + "/" + item.url+"');\">"+StringEscapeUtils.escapeHtml(item.name)+"</span>");
	    }
		out.println("</div>");
		
	}

}
