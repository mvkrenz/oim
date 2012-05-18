package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.servlet.ServletBase;

public class BootMenuView implements IView {
	private String current;
	private UserContext user;
	
	public BootMenuView(UserContext user, String current) {
		this.current = current;
		this.user = user;
	}
	
	public void render(PrintWriter out) {		
		Authorization auth = user.getAuthorization();
		
		out.println("<div class=\"navbar navbar-fixed-top\">");	
		out.println("<div class=\"navbar-inner\">");
		out.println("<div class=\"container-fluid\">");
		
		out.println("<a class=\"btn btn-navbar\" data-toggle=\"collapse\" data-target=\".nav-collapse\">");
		out.println("<span class=\"icon-bar\"></span>");
		out.println("<span class=\"icon-bar\"></span>");
		out.println("<span class=\"icon-bar\"></span>");
		out.println("</a>");
		
		out.println("<a class=\"brand\" href=\"http://opensciencegrid.org\"><img src=\"images/osglogo.40x30.png\"></a>");
		
		out.println("<div class=\"nav-collapse\">");
		
		//application menu
		out.println("<ul class=\"nav\">");
		
		out.println("<li class=\"dropdown\">");
		out.println("<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\">"+StaticConfig.getApplicationName()+" <b class=\"caret\"></b></a>");
		out.println("<ul class=\"dropdown-menu\">");
			out.println("<li><a href=\"https://myosg.grid.iu.edu\">MyOSG</a></li>");
			out.println("<li class=\"active\"><a href=\"#\">OIM</a></li>");
			out.println("<li><a href=\"https://ticket.grid.iu.edu\">Ticket</a></li>");
			out.println("<li><a href=\"https://twiki.grid.iu.edu\">TWiki</a></li>");
			out.println("<li class=\"divider\"></li>");
			out.println("<li><a href=\"http://repo.grid.iu.edu\">Repo</a></li>");
			out.println("<li><a href=\"http://software.grid.iu.edu\">Software Cache</a></li>");
			out.println("<li><a href=\"http://is.grid.iu.edu\">OSG BDII</a></li>");
			out.println("<li class=\"divider\"></li>");
			out.println("<li><a href=\"http://blogs.grid.iu.edu\">Blog</a></li>");
			out.println("<li><a href=\"http://display.grid.iu.edu\">Display</a></li>");
			out.println("<li><a href=\"http://osggoc.blogspot.com/\">News</a></li>");
		out.println("</ul>");
		out.println("</li>");
		
		out.println("<li class=\"divider-vertical\"></li>");
		
		if(current.equals("home")) {
			out.println("<li class=\"active\"><a href=\"home\">Home</a></li>");
		} else {
			out.println("<li><a href=\"home\">Home</a></li>");	
		}
		if(current.equals("certificate")) {
			out.println("<li class=\"active\"><a href=\"certificate\">Certificate</a></li>");
		} else {
			//for now, only show if it's debug
			//if(StaticConfig.isDebug()) {
			//out.println("<li><a href=\"certificate\">Certificate</a></li>");	
			//}
		}
		if(current.equals("topology")) {
			out.println("<li class=\"active\"><a href=\"topology\">Topology</a></li>");
		} else {
			out.println("<li><a href=\"topology\">Topology</a></li>");	
		}
		
		if(auth.isUser()) {
			if(current.equals("resourcedowntime")) {
				out.println("<li class=\"active\"><a href=\"resourcedowntime\">Downtimes</a></li>");
			} else {
				out.println("<li><a href=\"resourcedowntime\">Downtimes</a></li>");	
			}
		}
	
		if(current.equals("vo")) {
			out.println("<li class=\"active\"><a href=\"vo\">Virtual Organizations</a></li>");
		} else {
			out.println("<li><a href=\"vo\">Virtual Organizations</a></li>");	
		}
		
		if(current.equals("sc")) {
			out.println("<li class=\"active\"><a href=\"sc\">Support Centers</a></li>");
		} else {
			out.println("<li><a href=\"sc\">Support Centers</a></li>");	
		}
				
		out.println("</ul>");//end of left items
		
		
		out.println("<ul class=\"nav pull-right\">");
		
		//account menu
		if(auth.isUser()) {
			ContactRecord contact = auth.getContact();
			//user menu
			out.println("<li class=\"dropdown\">");
			out.println("<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\">"+contact.name+" <b class=\"caret\"></b></a>");
			out.println("<ul class=\"dropdown-menu\">");
			
				if(current.equals("profileedit")) {
					out.println("<li class=\"active\">");
				} else {
					out.println("<li>");		
				}
				out.println("<a href=\"profileedit\">My Profile</a></li>");		
				
				if (auth.allows("read_all_logs")) {
					if(current.equals("log")) {
						out.println("<li class=\"active\">");
					} else {
						out.println("<li>");		
					}
					out.println("<a href=\"log?logtype=1&start_type=2&end_type=1&transaction_1=on&transaction_2=on&transaction_3=on&model_0=on&model_1=on&model_2=on&model_3=on&model_4=on&model_5=on&model_6=on?\">OIM Log</a></li>");
				}
				
				if(current.equals("contact")) {
					out.println("<li class=\"active\">");
				} else {
					out.println("<li>");		
				}
				out.println("<a href=\"contact\">OSG Contacts</a></li>");
				
				if (auth.allows("edit_measurement")) {
					if(current.equals("cpuinfo")) {
						out.println("<li class=\"active\">");
					} else {
						out.println("<li>");		
					}
					out.println("<a href=\"cpuinfo\">CPU Info</a></li>");	
				}
				
				if (auth.allows("read_report")) {
					out.println("<li class=\"divider\"></li>");
					
					if(current.equals("reportregistration")) {
						out.println("<li class=\"active\">");
					} else {
						out.println("<li>");		
					}			
					out.println("<a href=\"reportregistration\">Registration Report</a></li>");
					
					if(current.equals("reportconfirmation")) {
						out.println("<li class=\"active\">");
					} else {
						out.println("<li>");		
					}					
					out.println("<a href=\"reportconfirmation\">Confirmation Report</a></li>");
					
					if(auth.allows("read_report.error")) {
						
						if(current.equals("reportdata")) {
							out.println("<li class=\"active\">");
						} else {
							out.println("<li>");		
						}		
						out.println("<a href=\"reportdata\">Data Issues</a></li>");	
					}
				}
				
				if (auth.allows("admin")) {
					out.println("<li class=\"divider\"></li>");
					if(current.equals("admin")) {
						out.println("<li class=\"active\">");
					} else {
						out.println("<li>");		
					}							
					out.println("<a href=\"admin\">Administration</a></li>");
				}
	
				out.println("<li class=\"divider\"></li>");
				String httpurl = StaticConfig.conf.getProperty("application.guestbase");
				out.println("<li><a href=\""+httpurl+"\">Logoff</a></li>");
				
			out.println("</ul>");//dropdown-menu
			out.println("</li>");//dropdown
		} else if(auth.isGuest()){
			out.println("<li><a href=\""+StaticConfig.conf.getProperty("application.base")+"\">Login</a></li>");	
		} else if(auth.isUnregistered()) {
			if(current.equals("register")) {
				out.println("<li class=\"active\">");
			} else {
				out.println("<li>");		
			}	
			out.println("<a href=\"register\">Register</a></li>");
			
			String httpurl = StaticConfig.conf.getProperty("application.guestbase");
			out.println("<li><a href=\""+httpurl+"\">Logoff</a></li>");
		} else if(auth.isDisabled()) {			
			String httpurl = StaticConfig.conf.getProperty("application.guestbase");
			out.println("<li><a href=\""+httpurl+"\">Logoff</a></li>");
		}
		
		out.println("</ul>");//nav (pull-right)
		
		out.println("</div>");//nav-collaps	
		
		out.println("</div>");//container-fluid
		out.println("</div>");//navbar-inner
		out.println("</div>");//navbar-fixed-top		
	}

}
