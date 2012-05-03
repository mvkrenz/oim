package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.StringEscapeUtils;

public class CertificateMenuView implements IView {

	private String current;
	
	public CertificateMenuView(String current) {
		this.current = current;
	}
	public void render(PrintWriter out) {
		out.write("<ul class=\"nav nav-pills nav-stacked\">");
		
		Integer current_user_id = 30; //TODO find user certificate that user is using to login (if any)
		if(current.equals("certificateuser_current")) {
			out.write("<li class=\"active\">");
		} else {
			out.write("<li>");
		}
		out.write("<a href=\"certificateuser?id="+current_user_id+"\">Current User Certificate</a></li>");
		
		if(current.equals("certificateuser")) {
			out.write("<li class=\"active\">");
		} else {
			out.write("<li>");
		}
		out.write("<a href=\"certificateuser\">User Certificate Requests</a></li>");
		
		if(current.equals("certificatehost")) {
			out.write("<li class=\"active\">");
		} else {
			out.write("<li>");
		}		
		out.write("<a href=\"certificatehost\">Host Certificate Requests</a></li>");
		
		if(current.equals("certificaterequestuser")) {
			out.write("<li class=\"active\">");
		} else {
			out.write("<li>");
		}				
		out.write("<a href=\"certificaterequestuser\">Request New User Certificate</a></li>");
		
		if(current.equals("certificaterequesthost")) {
			out.write("<li class=\"active\">");
		} else {
			out.write("<li>");
		}			
		out.write("<a href=\"certificaterequesthost\">Request New Host Certificate</a></li>");
		out.write("</ul>");
	}
}
