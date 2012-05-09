package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.SmallTableModelBase;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.CertificateMenuView;
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
import edu.iu.grid.oim.view.divrep.form.CertificateRequestUserForm;

public class AdminServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(AdminServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");

		BootMenuView menuview = new BootMenuView(context, "admin");
		BootPage page = new BootPage(context, menuview, createContentView(context), createSideView(context));
		page.render(response.getWriter());	
	}
	
	protected IView createContentView(final UserContext context)
	{
		return new IView() {
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\"content\">");
				
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span4\">");
				out.write("<h3>Authentication / Authorization</h3>");
				out.write("<p><a href=\"action\">Action</a></p>");
				out.write("<p><a href=\"authtype\">Authorization Types</a></p>");
				out.write("<p><a href=\"authmatrix\">Action/Authorization Matrix</a></p>");
				out.write("<p><a href=\"user\">DN/AuthType Mapping</a></p>");
				out.write("</div>");
				
				out.write("<div class=\"span4\">");
				out.write("<h3>Certificate Management</h3>");
				out.write("<p><a href=\"gridadmin\">GridAdmin</a></p>");
				out.write("<p><a href=\"quotaadmin\">Quota</a></p>");
				out.write("</div>");
				
				out.write("<div class=\"span4\">");
				out.write("<h3>Misc.</h3>");
				out.write("<p><a href=\"osggridtype\">OSG Grid Types</a></p>");
				out.write("<p><a href=\"servicegroup\">Service Groups</a></p>");
				out.write("<p><a href=\"service\">Services</a></p>");
				out.write("<p><a href=\"metric\">RSV Metrics</a></p>");
				out.write("<p><a href=\"fieldofscience\">Fields of Science (associated with VOs)</a></p>");
				out.write("<p><a href=\"fptemplates\">Footprints Ticket Templates</a></p>");
				out.write("</div>");
				
				out.write("</div>");//row-fluid
				
				out.write("</div>"); //content
			}
		
		};
	}
	
	private SideContentView createSideView(UserContext context)
	{
		SideContentView view = new SideContentView();
		GenericView operations = new GenericView();
		
		if(!context.getAuthorization().allows("admin")) return view;
		
		final DivRepButton clear_button = new DivRepButton(context.getPageRoot(), "Clear All Cache");
		clear_button.addClass("btn");
		clear_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				SmallTableModelBase.emptyAllCache();
				clear_button.alert("Done!");
			}
		});
		operations.add(clear_button);
		view.add(operations);

		return view;
	}

}
