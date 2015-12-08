package edu.iu.grid.oim.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;

public class LogoutServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(LogoutServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		context.getSession().invalidate();
		
		//construct home url
		String homeurl = "http://"+context.getRequestURL().getHost();
		if(StaticConfig.conf.getProperty("application.guestport") != null) {
			homeurl += ":"+StaticConfig.conf.getProperty("application.guestport");
		}
		homeurl += StaticConfig.conf.getProperty("application.base");
		
		response.sendRedirect(homeurl);
	}
}
