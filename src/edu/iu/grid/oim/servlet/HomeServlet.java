package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.View;
import edu.iu.grid.oim.view.divex.TestApplicationDE;

/**
 * Servlet implementation class HomeServlet
 */
public class HomeServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(HomeServlet.class);  
    
    public HomeServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		//init sample divex app (just to see how this could work)
		TestApplicationDE test_deapp = 
			(TestApplicationDE)request.getSession().getAttribute("test_divex");
		if(test_deapp == null) {
			test_deapp = new TestApplicationDE();
			request.getSession().setAttribute("test_divex", test_deapp);
		}
		
		MenuView menuview = createMenuView(baseURL(), "home");
		ContentView contentview = createContentView();
		contentview.add(test_deapp);
		
		Page page = new Page(menuview, contentview);
		response.getWriter().print(page.toHTML());
	}
	
	protected ContentView createContentView()
	{
		ContentView contentview = new ContentView();
		
		contentview.add("<h1>OIM Home</h1>");
		
		return contentview;
	}

}
