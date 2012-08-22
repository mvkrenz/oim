package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.servlet.CertificateHostServlet;

public class CertificateMenuView implements IView {
    static Logger log = Logger.getLogger(CertificateMenuView.class);  

	private String current;
	private UserContext context;
	//private CertificateRequestUserRecord userrec;
	
	public CertificateMenuView(UserContext context, String current) {
		this.current = current;
		this.context = context;
		/*
		//find if user has "current" user certificate
		CertificateRequestHostRecord hostrec;
		try {
			UserCertificateRequestModel umodel = new UserCertificateRequestModel(context);
			this.userrec = umodel.getCurrent();
		} catch(SQLException e) {
			log.error("Failed to find out if user has current user certificate");
		}
		*/
	}
	public void render(PrintWriter out) {		
		Authorization auth = context.getAuthorization();
		
		out.write("<div class=\"well\" style=\"padding: 8px 0;\">");
		out.write("<ul class=\"nav nav-list\">");
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
		
		if(current.equals("certificatesearchhost")) {
			out.write("<li class=\"active\">");
			out.write("<a href=\"certificatesearchhost\"><i class=\"icon-white icon-search\"></i> Search</a></li>");	
		} else {
			out.write("<li>");
			out.write("<a href=\"certificatesearchhost\"><i class=\"icon-search\"></i> Search</a></li>");	
		}		
		
		if(auth.allows("admin_gridadmin") || auth.allows("admin_pki_quota ")) {
			out.write("<li class=\"nav-header\">Administration</li>");
			if(current.equals("gridadmin")) {
				out.write("<li class=\"active\">");
				out.write("<a href=\"gridadmin\"><i class=\"icon-white icon-flag\"></i> GridAdmin</a></li>");	
			} else {
				out.write("<li>");
				out.write("<a href=\"gridadmin\"><i class=\"icon-flag\"></i> GridAdmin</a></li>");	
			}		
			
			if(current.equals("quotaadmin")) {
				out.write("<li class=\"active\">");
				out.write("<a href=\"quotaadmin\"><i class=\"icon-white icon-lock\"></i> Quota</a></li>");	
			} else {
				out.write("<li>");
				out.write("<a href=\"quotaadmin\"><i class=\"icon-lock\"></i> Quota</a></li>");	
			}		
			
		}
		
		out.write("</ul>");
		out.write("</div>");
	}
}
