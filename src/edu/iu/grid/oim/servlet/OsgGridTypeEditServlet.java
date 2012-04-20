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
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
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
import edu.iu.grid.oim.view.divrep.form.OsgGridTypeFormDE;

public class OsgGridTypeEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(OsgGridTypeEditServlet.class);  
	private String parent_page = "osggridtype";	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");
		
		OsgGridTypeRecord rec;
		String title;

		//if osg_grid_type_id is provided then we are doing update, otherwise do new.
		// AG: Do we need any request parameter-value checks?
		String osg_grid_type_id_str = request.getParameter("osg_grid_type_id");
		if(osg_grid_type_id_str != null) {
			//pull record to update
			int osg_grid_type_id = Integer.parseInt(osg_grid_type_id_str);
			OsgGridTypeModel model = new OsgGridTypeModel(context);
			try {
				OsgGridTypeRecord keyrec = new OsgGridTypeRecord();
				keyrec.id = osg_grid_type_id;
				rec = model.get(keyrec);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = "Update " + rec.name;
		} else {
			rec = new OsgGridTypeRecord();
			title = "New";	
		}
	
		OsgGridTypeFormDE form;
		//String origin_url = StaticConfig.getApplicationBase()+"/"+parent_page;
		try {
			form = new OsgGridTypeFormDE(context, rec, parent_page);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		
		//put the form in a view and display
		ContentView contentview = new ContentView();
		//contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
		contentview.add(new DivRepWrapper(form));
		
		//setup crumbs
		BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
		bread_crumb.addCrumb("Administration",  "admin");
		bread_crumb.addCrumb("OSG Grid Types",  parent_page);
		bread_crumb.addCrumb(title, null);
		
		contentview.setBreadCrumb(bread_crumb);
		
		BootPage page = new BootPage(context, new BootMenuView(context, "admin"), contentview, createSideView());	
		page.render(response.getWriter());	
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		//view.add("Misc-no-op", new HtmlView("Misc-no-op"));
		return view;
	}
}