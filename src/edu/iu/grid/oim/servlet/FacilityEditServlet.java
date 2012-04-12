package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRepRoot;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;

import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.record.FacilityRecord;

import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.view.divrep.form.FacilityFormDE;

public class FacilityEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(FacilityEditServlet.class);  
	private String parent_page = "topology";	

    public FacilityEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// setContext(request);
		auth.check("edit_all_facility");

		FacilityRecord rec;
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
			} else {
				rec = new FacilityRecord();
				title = "New Facility";
			}

			FacilityFormDE form;
			//String origin_url = StaticConfig.getApplicationBase() + "/"+ parent_page;
			form = new FacilityFormDE(context, rec, parent_page);

			// put the form in a view and display
			ContentView contentview = new ContentView();
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

			BootPage page = new BootPage(context, new BootMenuView(context, parent_page),
					contentview, createSideView());

			page.render(response.getWriter());
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		//view.add(new HtmlView("<p>A facility represents an instituition (like BNL, Fermilab, etc.) or a university.</p>"));
//				new HtmlView("This form allows you to edit this support center's registration information.</p>"));		
		//view.addContactNote();		
		// view.addContactLegent();		
		return view;
	}
}