package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.LogModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LogView;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.view.divrep.form.SiteFormDE;

public class SiteEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(SiteEditServlet.class);  
	private String parent_page = "topology";	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("edit_all_site");
		
		SiteRecord rec;
		ArrayList<LogRecord> logs = null;

		ContentView contentview = new ContentView(context);
//		contentview.add(new HtmlView("<script src=\"http://maps.google.com/maps?file=api&v=2&key="+StaticConfig.getGMapAPIKey()+"\" type=\"text/javascript\"></script>"));
		try {
			String title;
			
			//if site_id is provided then we are doing update, otherwise do new.
			String site_id_str = request.getParameter("site_id");
			if(site_id_str != null) {
				//pull record to update
				int site_id = Integer.parseInt(site_id_str);
				SiteModel model = new SiteModel(context);
				SiteRecord keyrec = new SiteRecord();
				keyrec.id = site_id;
				rec = model.get(keyrec);
				title = "Edit Site " + rec.name;
				
				//pull logs
				LogModel logmodel = new LogModel(context);
				logs = logmodel.search("edu.iu.grid.oim.model.db.Site%", String.valueOf(site_id));
			} else {
				rec = new SiteRecord();
				title = "New Site";	
				
				String facility_id_str = request.getParameter("facility_id");
				if(facility_id_str != null) {
					rec.facility_id = Integer.parseInt(facility_id_str);
				}
			}
			
			// setup crumbs
			BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
			// bread_crumb.addCrumb("Administration", "admin");
			bread_crumb.addCrumb("Topology", parent_page);
			bread_crumb.addCrumb(title, null);
			contentview.setBreadCrumb(bread_crumb);
			if(rec.disable != null && rec.disable == true) {
				contentview.add(new HtmlView("<div class=\"alert\">This site is currently disabled.</div>"));
			}
			
			//String origin_url = StaticConfig.getApplicationBase()+"/"+parent_page;
			SiteFormDE form = new SiteFormDE(context, rec, parent_page);
			//contentview.add(new HtmlView("<h1>"+title+"</h1>"));	

			contentview.add(new DivRepWrapper(form));
			
			BootPage page = new BootPage(context, new BootMenuView(context, parent_page), contentview, createSideView(logs, rec));
			
			page.render(response.getWriter());	
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private SideContentView createSideView(ArrayList<LogRecord> logs, SiteRecord rec)
	{
		SideContentView view = new SideContentView();
		if(rec.id != null) {
			view.add(new HtmlView("<p><a class=\"btn\" href=\"site?site_id="+rec.id+"\">Show Readonly View</a></p>"));
		}
		if(logs != null) {
			view.add(new LogView(logs));	
		}
		return view;
	}
}