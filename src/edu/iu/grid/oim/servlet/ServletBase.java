package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.LinkView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;

public class ServletBase extends HttpServlet {
    static Logger log = Logger.getLogger(ServletBase.class);  
	protected Authorization auth;

	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
	}
	
	protected void setAuth(HttpServletRequest request) throws AuthorizationException
	{
		auth = new Authorization(request);
	}
	
	protected MenuView createMenuView(String current)
	{
		ArrayList<MenuItem> menu = new ArrayList<MenuItem>();
		menu.add(new MenuItem("Home", "home"));
		menu.add(new MenuItem("Resource", "resource"));	
		menu.add(new MenuItem("Virtual Organization", "vo"));			
		menu.add(new MenuItem("Contact", "Contact" ));
		menu.add(new MenuItem("Administration", "admin"));	
		MenuView menuview = new MenuView(menu, current);
		return menuview;
	}
	
	static public String BaseURL()
	{
		//TODO - figure this out dynamicly.
		return "/oim";
	}
	
	boolean debug()
	{
		return (getServletContext().getInitParameter("debug").compareTo("true") == 0);	
	}
}
