package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
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
		if(auth.isUnregistered()) {
			menu.add(new MenuItem("Register", "register"));
		}
		
		if(auth.allows("view_topology")) {
			menu.add(new MenuItem("Topology", "topology"));
		}
		
		if(auth.allows("edit_my_resource")) {
			menu.add(new MenuItem("Downtimes", "resourcedowntime"));
		}
		
		if(auth.allows("edit_my_vo")) {
			menu.add(new MenuItem("Virtual Organizations", "vo"));
		}
		
		if(auth.allows("edit_my_sc")) {
			menu.add(new MenuItem("Support Centers", "sc"));
		}
		
		if(auth.allows("edit_my_contact")) {
			menu.add(new MenuItem("Contacts", "contact"));
		}
		if (auth.isUser()) {
			menu.add(new MenuItem("My Profile", "profileedit"));
		}
		if (auth.isUser()) {
			menu.add(new MenuItem("Logs", "log", "?type=1&start_type=2&end_type=1&transaction_1=on&transaction_2=on&transaction_3=on&model_1=on&model_2=on&model_3=on&model_4=on&model_5=on&model_6=on&model_7=on&"));
		}
		if (auth.allows("edit_measurement")) {
			menu.add(new MenuItem("CPU Info", "cpuinfo"));	
		}
		if (auth.allows("read_report")) {
			menu.add(new MenuItem("Reports", "report"));	
		}
		if (auth.allows("admin")) {
			menu.add(new MenuItem("Admin", "admin"));	
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
	    	//out.println("<span class=\""+ cls +"\"><a href=\""+ StaticConfig.getApplicationBase() + "/" + item.url+"\">"+StringEscapeUtils.escapeHtml(item.name)+"</a></span>");
	    	String url = StaticConfig.getApplicationBase() + "/" + item.url;
	    	if(item.param != null) {
	    		url += item.param;
	    	}
	    	out.println("<div class=\"link "+ cls +"\" onclick=\"divrep_redirect('"+url+"');\">"+StringEscapeUtils.escapeHtml(item.name)+"</div>");
	    }
		out.println("</div>");
		
	}

}
