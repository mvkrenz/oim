package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.webif.divrep.Button;
import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepRoot;
import com.webif.divrep.Event;
import com.webif.divrep.EventListener;

import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.lib.Footprint;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.model.db.SmallTableModelBase;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.InternalLinkView;
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
			contentview.add(new HtmlView("<h3>Authentication / Authorization</h3>"));
			contentview.add(new InternalLinkView("action", "Actions"));
			contentview.add(new HtmlView("<br/>"));
			
			contentview.add(new InternalLinkView("authtype", "Authorization Types"));
			contentview.add(new HtmlView("<br/>"));
			
			contentview.add(new InternalLinkView("authmatrix", "Authorization-Action Matrix" ));
			contentview.add(new HtmlView("<br/>"));
			
			contentview.add(new InternalLinkView("user", "User-Authorization Level Mapping" ));
			contentview.add(new HtmlView("<br/>"));
			
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new HtmlView("<h3>GOC Administration</h3>"));
			contentview.add(new InternalLinkView("osggridtype", "OSG Grid Types"));
			contentview.add(new HtmlView("<br/>"));
			
			contentview.add(new InternalLinkView("servicegroup", "Service Groups"));
			contentview.add(new HtmlView("<br/>"));
			
			contentview.add(new InternalLinkView("service", "Services")); //service table, metric_service table
			contentview.add(new HtmlView("<br/>"));
			
			contentview.add(new InternalLinkView("metric", "RSV Metrics"));
			contentview.add(new HtmlView("<br/>"));
			
			contentview.add(new InternalLinkView("fieldofscience", "Fields of Science (associated with VOs)"));
			contentview.add(new HtmlView("<br/>"));
		}
		

		if (auth.allows("admin") || auth.allows("edit_measurement")) {
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new HtmlView("<h3>Measurement</h3>"));
			contentview.add(new InternalLinkView("cpuinfo", "CPU Information"));
			contentview.add(new HtmlView("<br/>"));
		}
		
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		GenericView operations = new GenericView();
		
		view.add("Operation", operations);
		if(!auth.allows("admin")) return view;
		
		final Button clear_button = new Button(context.getPageRoot(), "Clear All Cache");
		clear_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				SmallTableModelBase.emptyAllCache();
				clear_button.alert("Done!");
			}
		});
		operations.add(clear_button);
		
		/*
		final Button fptest_button = new Button(context.getPageRoot(), "Test FP Ticket");
		fptest_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				Footprint ticket = new Footprint(context);
				ticket.createNewResourceTicket("test_resource");
				
				fptest_button.alert("Done!");
			}
		});
		operations.add(fptest_button);

		final Button error_button = new Button(context.getPageRoot(), "Simulate Servlet Error");
		error_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				error_button.redirect(Config.getApplicationBase() + "/simulateerror");
			}
		});
		operations.add(error_button);
		*/
		return view;
	}

}
