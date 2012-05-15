package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.UserCertificateRequestModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.servlet.CertificateHostServlet;

public class CertificateMenuView implements IView {
    static Logger log = Logger.getLogger(CertificateMenuView.class);  

	private String current;
	private UserContext context;
	private CertificateRequestUserRecord userrec;
	
	public CertificateMenuView(UserContext context, String current) {
		this.current = current;
		this.context = context;
		
		//find if user has "current" user certificate
		CertificateRequestHostRecord hostrec;
		try {
			UserCertificateRequestModel umodel = new UserCertificateRequestModel(context);
			this.userrec = umodel.getCurrent();
		} catch(SQLException e) {
			log.error("Failed to find out if user has current user certificate");
		}
	}
	public void render(PrintWriter out) {		
		out.write("<div class=\"well\" style=\"padding: 8px 0;\">");
		out.write("<ul class=\"nav nav-list\">");
		out.write("<li class=\"nav-header\">User Certificates</li>");
		if(userrec != null) {
			if(current.equals("certificateuser_current")) {
				out.write("<li class=\"active\">");
				out.write("<a href=\"certificateuser?id="+userrec.id+"\"><i class=\"icon-white icon-home\"></i> Current</a></li>");
			} else {
				out.write("<li>");
				out.write("<a href=\"certificateuser?id="+userrec.id+"\"><i class=\"icon-home\"></i> Current</a></li>");
			}
		}
		
		if(current.equals("certificaterequestuser")) {
			out.write("<li class=\"active\">");
			out.write("<a href=\"certificaterequestuser\"><i class=\"icon-white icon-plus\"></i> Request New</a></li>");
		} else {
			out.write("<li>");
			out.write("<a href=\"certificaterequestuser\"><i class=\"icon-plus\"></i> Request New</a></li>");
		}				
		
		if(current.equals("certificateuser")) {
			out.write("<li class=\"active\">");
			out.write("<a href=\"certificateuser\"><i class=\"icon-white icon-list\"></i> Show Requests</a></li>");	
		} else {
			out.write("<li>");
			out.write("<a href=\"certificateuser\"><i class=\"icon-list\"></i> Show Requests</a></li>");	
		}		
		
		
		out.write("<li class=\"nav-header\">Host Certificates</li>");
		
		if(current.equals("certificaterequesthost")) {
			out.write("<li class=\"active\">");
			out.write("<a href=\"certificaterequesthost\"><i class=\"icon-white icon-plus\"></i> Request New</a></li>");
		} else {
			out.write("<li>");
			out.write("<a href=\"certificaterequesthost\"><i class=\"icon-plus\"></i> Request New</a></li>");
		}				
		
		if(current.equals("certificatehost")) {
			out.write("<li class=\"active\">");
			out.write("<a href=\"certificatehost\"><i class=\"icon-white icon-list\"></i> Show Requests</a></li>");	
		} else {
			out.write("<li>");
			out.write("<a href=\"certificatehost\"><i class=\"icon-list\"></i> Show Requests</a></li>");	
		}		
		
		out.write("</ul>");
		out.write("</div>");
	}
}
