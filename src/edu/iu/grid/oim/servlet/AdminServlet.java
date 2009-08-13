package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.Footprint;
import edu.iu.grid.oim.lib.AuthorizationException;
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
		if(!auth.allows("admin")) {
			throw new ServletException("You need action authorization for admin");
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
			
			contentview.add(new InternalLinkView("authmatrix", "Authorization Matrix" ));
			contentview.add(new HtmlView("<br/>"));
			
			contentview.add(new InternalLinkView("user", "Users" ));
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
			
			contentview.add(new InternalLinkView("adminannualreview", "Annual Review Controller"));
			contentview.add(new HtmlView("<br/>"));
			
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new HtmlView("<h3>Reports</h3>"));
			contentview.add(new InternalLinkView("reportregistration", "Registration"));
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new InternalLinkView("reportconfirmation", "Confirmation"));
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
		
		final DivRepButton clear_button = new DivRepButton(context.getPageRoot(), "Clear All Cache");
		clear_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				SmallTableModelBase.emptyAllCache();
				clear_button.alert("Done!");
			}
		});
		operations.add(clear_button);
		
		/*
		final DivRepButton fptest_button = new DivRepButton(context.getPageRoot(), "Test FP Ticket");
		fptest_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				Footprint ticket = new Footprint(context);
				ticket.createNewResourceTicket("test_resource");
				
				fptest_button.alert("Done!");
			}
		});
		operations.add(fptest_button);

		final DivRepButton error_button = new DivRepButton(context.getPageRoot(), "Simulate Servlet Error");
		error_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				error_button.redirect(StaticConfig.getApplicationBase() + "/simulateerror");
			}
		});
		operations.add(error_button);
		*/
		return view;
	}

}
