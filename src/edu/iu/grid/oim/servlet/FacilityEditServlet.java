package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRepRoot;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.LogModel;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;

import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LogView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.view.divrep.form.FacilityFormDE;

public class FacilityEditServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(FacilityEditServlet.class);  
	private String parent_page = "topology";	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("edit_all_facility");

		FacilityRecord rec;
		ArrayList<LogRecord> logs = null;
		
		try {
			String title;

			// if site_id is provided then we are doing update, otherwise do
			// new.
			String facility_id_str = request.getParameter("facility_id");
			if (facility_id_str != null) {
				// pull record to update
				int facility_id = Integer.parseInt(facility_id_str);
				FacilityModel model = new FacilityModel(context);
				FacilityRecord keyrec = new FacilityRecord();
				keyrec.id = facility_id;
				rec = model.get(keyrec);
				title = "Edit Facility " + rec.name;
				
				//pull logs
				LogModel logmodel = new LogModel(context);
				logs = logmodel.search("edu.iu.grid.oim.model.db.Facility%", String.valueOf(facility_id)+"%");
				
			} else {
				rec = new FacilityRecord();
				title = "New Facility";
			}

			FacilityFormDE form;
			//String origin_url = StaticConfig.getApplicationBase() + "/"+ parent_page;
			form = new FacilityFormDE(context, rec, parent_page);

			// put the form in a view and display
			ContentView contentview = new ContentView(context);
			//contentview.add(new HtmlView("<h1>" + title + "</h1>"));
			if(rec.disable != null && rec.disable == true) {
				contentview.add(new HtmlView("<div class=\"alert\">This facility is currently disabled.</div>"));
			}
			contentview.add(new DivRepWrapper(form));

			// setup crumbs
			BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
			// bread_crumb.addCrumb("Administration", "admin");
			bread_crumb.addCrumb("Topology", parent_page);
			bread_crumb.addCrumb(title, null);
			contentview.setBreadCrumb(bread_crumb);

			BootPage page = new BootPage(context, new BootMenuView(context, parent_page), contentview, createSideView(logs));

			page.render(response.getWriter());
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private SideContentView createSideView(ArrayList<LogRecord> logs)
	{
		SideContentView view = new SideContentView();
		if(logs != null) {
			view.add(new LogView(logs));	
		}
		return view;
	}
}