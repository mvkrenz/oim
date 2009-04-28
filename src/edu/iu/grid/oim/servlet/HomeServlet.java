package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.webif.divex.DivExRoot;

import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.SideContentView;

public class HomeServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(HomeServlet.class);  
    
    public HomeServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setContext(request);
		
		MenuView menuview = new MenuView(context, "home");
		ContentView contentview;
		if(auth.getDNID() == null) {
			contentview = createSignupContentView();	
		} else {
			contentview = createContentView();
		}
		
		Page page = new Page(menuview, contentview, new SideContentView());
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView()
	{
		ContentView contentview = new ContentView();
		
		contentview.add(new HtmlView("<h1>OIM Home</h1>"));
		
		return contentview;
	}
	
	protected ContentView createSignupContentView()
	{
		ContentView contentview = new ContentView();
		
		contentview.add(new HtmlView("<h1>OIM Registration</h1>"));
		contentview.add(new HtmlView("<p>Your DN ("+auth.getUserDN()+") is not yet registered at OIM.</p>"));
		contentview.add(new HtmlView("<p>Please open a ticket at <a href=\"https://oim.grid.iu.edu/gocticket\">GOC Ticket Form</a> requesting your OIM account.</p>"));
		
		return contentview;
	}

}
