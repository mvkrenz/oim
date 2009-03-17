package edu.iu.grid.oim.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

/**
 * Servlet implementation class ErrorServlet
 */
public class ErrorServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
    
	protected static String[] vars = {
        "javax.servlet.error.status_code",
        "javax.servlet.error.exception_type",
        "javax.servlet.error.message",
        "javax.servlet.error.exception",
        "javax.servlet.error.request_uri"
    };

    public ErrorServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		MenuView menuview = createMenuView(baseURL(), null);
		ContentView contentview = createContentView(request);		
		Page page = new Page(menuview, contentview, new SideContentView());
		
		response.getWriter().print(page.toHTML());
	}
	
	protected ContentView createContentView(HttpServletRequest request)
	{
		ContentView contentview = new ContentView();
        
		contentview.add("<h2>Oops!</h2>");
		contentview.add("<p>Sorry, we have encountered a problem.</p>");
		
		if(debug()) {
			contentview.add("<table>");
	        for (int i = 0; i < vars.length; i++) {
	    		contentview.add("<TR><TD>" + vars[i] + "</TD><TD>" +
	                request.getAttribute(vars[i]) + 
	                "</TD></TR>");
	        }
	        contentview.add("</table>");
		}
		
		return contentview;
	}
}
