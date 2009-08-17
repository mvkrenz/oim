package edu.iu.grid.oim.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

public class NeedcertServlet extends HttpServlet {
    public NeedcertServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		MenuView menuview = new MenuView(Context.getGuestContext(), "home");
		ContentView contentview = createContentView(request);		
		Page page = new Page(Context.getGuestContext(), menuview, contentview, new SideContentView());//pass guest authorization
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView(HttpServletRequest request)
	{
		ContentView contentview = new ContentView();

		contentview.add(new HtmlView("<h2>No Certificate</h2>"));
		contentview.add(new HtmlView("<p>Please login with a DOE certificate.</p>"));
		
		return contentview;
	}
}
