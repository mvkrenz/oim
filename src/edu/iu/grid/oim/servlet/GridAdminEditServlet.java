package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

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
		
		GridAdminRecord rec;
		String title;

		//if cpu_info_id is provided then we are doing update, otherwise do new.
		// AG: Do we need any request parameter-value checks?
		String dirty_id = request.getParameter("id");
		if(dirty_id != null) {
			//pull record to update
			int id = Integer.parseInt(dirty_id);
			GridAdminModel model = new GridAdminModel(context);
			try {
				rec = model.get(id);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = "Update GridAmin";
		} else {
			rec = new GridAdminRecord();
			title = "New GridAdmin";	
		}
		ContentView contentview = new ContentView(context);
		
		/*
		GridAdminFormDE form;
		try {
			form = new GridAdminFormDE(context, rec, "gridadmin");
			contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
			contentview.add(new DivRepWrapper(form));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		*/
		
		/*
		//setup crumbs
		BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
		bread_crumb.addCrumb("Administration",  "admin");
		bread_crumb.addCrumb("GridAdmin",  "gridadmin");
		bread_crumb.addCrumb(title,  null);
		contentview.setBreadCrumb(bread_crumb);
		*/
		
		BootPage page = new BootPage(context, new BootMenuView(context, "certificate"), new Content(context, rec, title), null);	
		page.render(response.getWriter());	
	}
	
	class Content implements IView {
		UserContext context;
		GridAdminRecord rec;
		String page_title;
		Content(UserContext context, GridAdminRecord rec, String page_title) {
			this.context = context;
			this.rec = rec;
			this.page_title = page_title;
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
				GridAdminFormDE form = new GridAdminFormDE(context, rec, "gridadmin");
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