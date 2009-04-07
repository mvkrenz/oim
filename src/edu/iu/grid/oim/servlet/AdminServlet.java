package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;

import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LinkView;
import edu.iu.grid.oim.view.ListView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.SideContentView;

public class AdminServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(AdminServlet.class);  
    
    public AdminServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		MenuView menuview = createMenuView("admin");
		ContentView contentview = createContentView();
		Page page = new Page(menuview, contentview, createSideView());
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView()
	{
		ContentView contentview = new ContentView();
		
		contentview.add(new HtmlView("<h1>Administration</h1>"));
		ListView list = new ListView();
		if(auth.allows("admin_osg_grid_type")) {
			list.add(new LinkView("osg_grid_type", "OSG Grid Types"));
		}
		if(auth.allows("admin_site")) {
			list.add(new LinkView("site", "Administrative Sites"));
		}
		if(auth.allows("admin_authorization")) {
			list.add(new LinkView("authmatrix", "Authorization" ));
		}
		contentview.add(list);

		// Need to move to separate section accessible to Metrics group (Brian et al.) -agopu
		//hayashis - Do we really need to separate, or simply add to the above list with different auth check?
		ListView metrics_list = new ListView();
		metrics_list.add(new LinkView("cpuinfo", "CPU Information"));
		contentview.add(metrics_list);
		
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		/*
		HtmlView menu = new HtmlView("<a href=\"osg_grid_type\">OSG Grid Types</a><br/>"+
				"<a href=\"authmatrix\">Authorization Matrix</a>");
		view.add("Menu", menu);
		*/	
		return view;
	}

}
