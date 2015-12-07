package edu.iu.grid.oim.view;

import java.io.PrintWriter;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;

public class CertificateMenuView implements IView {
    static Logger log = Logger.getLogger(CertificateMenuView.class);  

	private String current;
	private UserContext context;

	public CertificateMenuView(UserContext context, String current) {
		this.current = current;
		this.context = context;
	}
	public void render(PrintWriter out) {		
		Authorization auth = context.getAuthorization();
		
		out.write("<div class=\"well\" style=\"padding: 8px 0;\">");
		out.write("<ul class=\"nav nav-list\">");

		if(current.equals("home")) {
			out.write("<li class=\"active\">");
			out.write("<a href=\"certificate\"><i class=\"icon-white icon-home\"></i> Home</a></li>");
		} else {
			out.write("<li>");
			out.write("<a href=\"certificate\"><i class=\"icon-home\"></i> Home</a></li>");
		}	
		
		out.write("<li class=\"nav-header\">User Certificates</li>");
		
		if(current.equals("certificaterequestuser")) {
			out.write("<li class=\"active\">");
			out.write("<a href=\"certificaterequestuser\"><i class=\"icon-white icon-plus\"></i> Request New</a></li>");
		} else {
			out.write("<li>");
			out.write("<a href=\"certificaterequestuser\"><i class=\"icon-plus\"></i> Request New</a></li>");
		}				
		
		if(auth.isUser()) {
			if(current.equals("certificateuser")) {
				out.write("<li class=\"active\">");
				out.write("<a href=\"certificateuser\"><i class=\"icon-white icon-list\"></i> My Requests</a></li>");	
			} else {
				out.write("<li>");
				out.write("<a href=\"certificateuser\"><i class=\"icon-list\"></i> My Requests</a></li>");	
			}		
		}
		
		if(current.equals("certificatesearchuser")) {
			out.write("<li class=\"active\">");
			out.write("<a href=\"certificatesearchuser\"><i class=\"icon-white icon-search\"></i> Search</a></li>");	
		} else {
			out.write("<li>");
			out.write("<a href=\"certificatesearchuser\"><i class=\"icon-search\"></i> Search</a></li>");	
		}	
		
		
		out.write("<li class=\"nav-header\">Host Certificates</li>");
		
		if(current.equals("certificaterequesthost")) {
			out.write("<li class=\"active\">");
			out.write("<a href=\"certificaterequesthost\"><i class=\"icon-white icon-plus\"></i> Request New</a></li>");
		} else {
			out.write("<li>");
			out.write("<a href=\"certificaterequesthost\"><i class=\"icon-plus\"></i> Request New</a></li>");
		}				
		
		if(auth.isUser()) {
			if(current.equals("certificatehost")) {
				out.write("<li class=\"active\">");
				out.write("<a href=\"certificatehost\"><i class=\"icon-white icon-list\"></i> My Requests</a></li>");	
			} else {
				out.write("<li>");
				out.write("<a href=\"certificatehost\"><i class=\"icon-list\"></i> My Requests</a></li>");	
			}
		}
		
		if(!auth.allows("admin_gridadmin")) {
			//all non-gridadmin_admin oim user can access read-only gridadmin page
			if(current.equals("gridadmin")) {
				out.write("<li class=\"active\">");
				out.write("<a href=\"gridadmin\"><i class=\"icon-white icon-flag\"></i> GridAdmins</a></li>");	
			} else {
				out.write("<li>");
				out.write("<a href=\"gridadmin\"><i class=\"icon-flag\"></i> GridAdmins</a></li>");	
			}	
		}
		
		if(current.equals("certificatesearchhost")) {
			out.write("<li class=\"active\">");
			out.write("<a href=\"certificatesearchhost\"><i class=\"icon-white icon-search\"></i> Search</a></li>");	
		} else {
			out.write("<li>");
			out.write("<a href=\"certificatesearchhost\"><i class=\"icon-search\"></i> Search</a></li>");	
		}		
		
		if(auth.allows("admin_gridadmin") || auth.allows("admin_pki_config")) {
			out.write("<li class=\"nav-header\">Administration</li>");
			if(current.equals("gridadmin")) {
				out.write("<li class=\"active\">");
				out.write("<a href=\"gridadmin\"><i class=\"icon-white icon-flag\"></i> GridAdmins</a></li>");	
			} else {
				out.write("<li>");
				out.write("<a href=\"gridadmin\"><i class=\"icon-flag\"></i> GridAdmin</a></li>");	
			}		
			
			if(current.equals("certificateconfig")) {
				out.write("<li class=\"active\">");
				out.write("<a href=\"certificateconfig\"><i class=\"icon-white icon-lock\"></i> Config</a></li>");	
			} else {
				out.write("<li>");
				out.write("<a href=\"certificateconfig\"><i class=\"icon-lock\"></i> Config</a></li>");	
			}		
			
		}
		
		out.write("</ul>");
		out.write("</div>");
		
		//out.write("<div class=\"alert alert-info alert-block\">Please submit a <a href=\"https://ticket.grid.iu.edu/\" target=\"_blank\">GOC Ticket</a> for assistance.</div>");
		
		//Jira OIM issue collector (this prevents page to load if jira is down, and it's just not really used very often
		//out.write("<script type=\"text/javascript\" src=\"https://jira.opensciencegrid.org/s/d41d8cd98f00b204e9800998ecf8427e/en_US93at5v-1988229788/6252/17/1.4.7/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?collectorId=12fd1a7d\"></script>");
	}
}
