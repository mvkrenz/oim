package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRepRoot;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.MenuView;

public class ServletBase extends HttpServlet {
    static Logger log = Logger.getLogger(ServletBase.class);  
	
    protected Context context;
	protected Authorization auth;
	private TimeZone timezone;
	
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
	}
	
	protected TimeZone getTimeZone()
	{
    	return auth.getTimeZone();
	}

	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{	
		try {
			context = new Context(req);
			auth = context.getAuthorization();
			timezone = null; //force to reload the timezone in case user updates it
			
			/*
			log.info(req.getRequestURI() + "?" + req.getQueryString());
			if(auth.getDNID() == null) {
				String path = req.getServletPath();
				if(path.equals("/home") || path.equals("/help")) {
					//certain pages can be displayed without DN (TODO - need a better way to do this)
				} else if(!path.equals("/register")) {
					resp.sendRedirect(StaticConfig.getApplicationBase()+"/register");
					return;
				}
			}
			*/
			
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
			resp.sendRedirect(StaticConfig.getApplicationBase()+"/error");
		}
		
		context.close();
	}
}
