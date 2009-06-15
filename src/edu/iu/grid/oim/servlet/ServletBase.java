package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.webif.divrep.DivRepRoot;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.view.MenuView;

public class ServletBase extends HttpServlet {
    static Logger log = Logger.getLogger(ServletBase.class);  
	
    protected Context context;
	protected Authorization auth;
	
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
	}

	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		try {
			context = new Context(req);
			auth = context.getAuthorization();
			log.info(req.getRequestURI() + "?" + req.getQueryString());
			if(auth.getDNID() == null) {
				String path = req.getServletPath();
				if(path.equals("/home") || path.equals("/help")) {
					//certain pages can be displayed without DN (TODO - need a better way to do this)
				} else if(!path.equals("/register")) {
					resp.sendRedirect(Config.getApplicationBase()+"/register");
					return;
				}
			}
			
			String method = req.getMethod();
			if(method.equals("GET")) {
				doGet(req, resp);
			}
		} catch (AuthorizationException e) {
			throw new ServletException(e);
		}	
		context.close();
	}
}
