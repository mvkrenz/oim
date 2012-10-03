package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRepRoot;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CpuInfoModel;
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.model.db.record.CpuInfoRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.view.divrep.form.CpuInfoFormDE;
import edu.iu.grid.oim.view.divrep.form.GridAdminFormDE;
import edu.iu.grid.oim.view.divrep.form.QuotaConfigFormDE;

import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

public class GridAdminEditServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(GridAdminEditServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin_gridadmin"); 
		
		ArrayList<GridAdminRecord> recs;
		String title;
		String domain = null;
		
		String dirty_domain = request.getParameter("domain");
		if(dirty_domain != null) {
			domain = dirty_domain.trim(); //TODO - validate dirty_domain
			title = "Update Domain/GridAdmins";
		} else {
			recs = new ArrayList<GridAdminRecord>();//empty list
			title = "New Dmoain/GridAdmins";	
		}
		ContentView contentview = new ContentView(context);
		
		BootPage page = new BootPage(context, new BootMenuView(context, "certificate"), new Content(context, domain, title), null);	
		page.render(response.getWriter());	
	}
	
	class Content implements IView {
		UserContext context;
		ArrayList<GridAdminRecord> recs;
		String domain;
		String page_title;
		Content(UserContext context, String domain, String page_title) {
			this.context = context;
			this.recs = recs;
			this.page_title = page_title;
			this.domain = domain;
		}
		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\"content\">");
		
			out.write("<div class=\"row-fluid\">");
			
			out.write("<div class=\"span3\">");
			CertificateMenuView menu = new CertificateMenuView(context, "gridadmin");
			menu.render(out);
			out.write("</div>"); //span3
			
			out.write("<div class=\"span9\">");
			out.write("<h2>"+page_title+"</h2>");	
			try {
				GridAdminFormDE form = new GridAdminFormDE(context, domain, "gridadmin");
				form.render(out);
			} catch (SQLException e) {
				log.error("SQLError file rendering quota config form", e);
			}
			out.write("</div>"); //span9
			out.write("</div>"); //row-fluid
			out.write("</div>"); //content
		}
	}
	
	/*
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		return view;
	}
	*/
}