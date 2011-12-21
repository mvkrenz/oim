package edu.iu.grid.oim.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.ExternalLinkView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LinkView;
import edu.iu.grid.oim.view.ListView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.ToolTip;

public class HelpServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(HelpServlet.class);  
    
    public HelpServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		//setContext(request);
		
		MenuView menuview = new MenuView(context, "help");
		ContentView contentview;
		contentview = createContentView();
		
		Page page = new Page(context, menuview, contentview, new SideContentView());
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView()
	{
		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<h1>Help</h1>"));

		
		
		return contentview;
	}
}
