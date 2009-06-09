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
import com.webif.divex.EventListener;

import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.lib.Footprint;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.model.db.SmallTableModelBase;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.GenericView;
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
		if(!auth.allows("admin") && !auth.allows("edit_measurement")) {
			throw new ServletException("You need action authorization for admin or edit_measurement.");
		}
		
		MenuView menuview = new MenuView(context, "admin");
		ContentView contentview = createContentView();
		Page page = new Page(menuview, contentview, createSideView());
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView()
	{
		ContentView contentview = new ContentView();
		
		contentview.add(new HtmlView("<h1>OIM Administration</h1>"));
		if(auth.allows("admin")) {
			ListView auth_list = new ListView();
			auth_list.add(new LinkView("action", "Actions"));
			auth_list.add(new LinkView("authtype", "Authorization Types"));
			auth_list.add(new LinkView("authmatrix", "Authorization-Action Matrix" ));
			auth_list.add(new LinkView("user", "User-Authorization Level Mapping" ));
			contentview.add(auth_list);
			
			ListView goc_task_list = new ListView();
			goc_task_list.add(new LinkView("osggridtype", "OSG Grid Types"));
			goc_task_list.add(new LinkView("servicegroup", "Service Groups"));
			goc_task_list.add(new LinkView("service", "Services")); //service table, metric_service table
			goc_task_list.add(new LinkView("metric", "RSV Metrics"));
			goc_task_list.add(new LinkView("fieldofscience", "Fields of Science (associated with VOs)"));
			contentview.add(goc_task_list);
		}

		if (auth.allows("admin") || auth.allows("edit_measurement")) {
			ListView metrics_list = new ListView();
			metrics_list.add(new LinkView("cpuinfo", "CPU Information"));
			contentview.add(metrics_list);
		}
		
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		GenericView operations = new GenericView();
		
		view.add("Operation", operations);
		if(!auth.allows("admin")) return view;
		
		final ButtonDE clear_button = new ButtonDE(context.getPageRoot(), "Clear All Cache");
		clear_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				SmallTableModelBase.emptyAllCache();
				clear_button.alert("Done!");
			}
		});
		operations.add(clear_button);
		
		
		final ButtonDE fptest_button = new ButtonDE(context.getPageRoot(), "Test FP Ticket");
		fptest_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				Footprint ticket = new Footprint(context);
				ticket.createNewResourceTicket("test_resource");
				
				fptest_button.alert("Done!");
			}
		});
		operations.add(fptest_button);

		final ButtonDE error_button = new ButtonDE(context.getPageRoot(), "Simulate Servlet Error");
		error_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				error_button.redirect(Config.getApplicationBase() + "/simulateerror");
			}
		});
		operations.add(error_button);
		
		return view;
	}

}
