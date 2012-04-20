package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.TimeZone;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.AuthorizationException;

//provide common functions that all servlet uses
public class ServletBase extends HttpServlet {
    //static Logger log = Logger.getLogger(ServletBase.class);  
    
    
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
		
    	TimeZone.setDefault(TimeZone.getTimeZone("UTC")); //LogServlet won't display correct items if I don't do this
	}
		
	
	/*
	protected TimeZone getTimeZone()
	{
    	return auth.getTimeZone();
	}
	*/
	
	//send session message
	public void message(HttpSession sess, String type, String msg) {
	}
	
	/*
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{	
		//TimeZone timezone = null; //force to reload the timezone in case user updates it
		try {
			String method = req.getMethod();
			if(method.equals("GET")) {
				resp.setCharacterEncoding("UTF-8");
				doGet(req, resp);
			}
		} catch (AuthorizationException e) {
			log.warn("Unauthorized access to " + req.getRequestURI(), e);
			
			//try to redirect
			try {
				String request_uri = URLEncoder.encode(req.getRequestURI(), "UTF-8");
				req.getSession().setAttribute("request_uri", request_uri);
			} catch (UnsupportedEncodingException e1) {
				log.error(e);
			} 
			
			req.getSession().setAttribute("exception", e);
			resp.sendRedirect("error");
		}
	}
	*/
}
