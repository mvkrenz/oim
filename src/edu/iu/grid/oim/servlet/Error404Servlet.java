package edu.iu.grid.oim.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

public class Error404Servlet extends HttpServlet {
    public Error404Servlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		MenuView menuview = new MenuView(Context.getGuestContext(), "_error_");
		ContentView contentview = createContentView(request);		
		Page page = new Page(Context.getGuestContext(), menuview, contentview, new SideContentView());
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView(HttpServletRequest request)
	{
		ContentView contentview = new ContentView();
        
		contentview.add(new HtmlView("<h2>Oops!</h2>"));
		contentview.add(new HtmlView("<p>Sorry, we can't find the page you have requested.</p>"));
		contentview.add(new HtmlView("<p>If you think this is a bug, please submit an issue ticket at <a href=\"https://oim.grid.iu.edu/gocticket\">GOC Ticket Submission Form</a>.</p>"));
		
		return contentview;
	}
}
