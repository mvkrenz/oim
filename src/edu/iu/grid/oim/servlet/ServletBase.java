package edu.iu.grid.oim.servlet;

import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

//provide common functions that all servlet uses
public class ServletBase extends HttpServlet {

	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
		
    	TimeZone.setDefault(TimeZone.getTimeZone("UTC")); //LogServlet won't display correct items if I don't do this
	}
		

	//send session message
	public void message(HttpSession sess, String type, String msg) {
	}
}
