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

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.LogModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LogView;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.view.divrep.form.ResourceGroupFormDE;

public class ResourceGroupEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceGroupEditServlet.class);  
	private String parent_page = "topology";	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("edit_all_resource_group");
		
		ResourceGroupRecord rec;
		ArrayList<LogRecord> logs = null;
		
		try {
			String title;
			
			//if site_id is provided then we are doing update, otherwise do new.
			String id_str = request.getParameter("id");
			if(id_str != null) {
				//pull record to update
				int id = Integer.parseInt(id_str);
				ResourceGroupModel model = new ResourceGroupModel(context);
				ResourceGroupRecord keyrec = new ResourceGroupRecord();
				keyrec.id = id;
				rec = model.get(keyrec);
				title = "Edit Resource Group " + rec.name;
				
				//pull logs
				LogModel logmodel = new LogModel(context);
				logs = logmodel.search("edu.iu.grid.oim.model.db.ResourceGroup%", String.valueOf(id));
			} else {
				rec = new ResourceGroupRecord();
				title = "New Resource Group";	
				
				String gridtype_id_str = request.getParameter("gridtype_id");
				if(gridtype_id_str != null) {
					rec.osg_grid_type_id = Integer.parseInt(gridtype_id_str);
				}
				String site_id_str = request.getParameter("site_id");
				if(site_id_str != null) {
					rec.site_id = Integer.parseInt(site_id_str);
				}
				
			}

			ResourceGroupFormDE form = new ResourceGroupFormDE(context, rec, parent_page);
			
			//put the form in a view and display
			ContentView contentview = new ContentView(context);
			//contentview.add(new HtmlView("<h1>"+title+"</h1>"));
			if(rec.disable != null && rec.disable == true) {
				contentview.add(new HtmlView("<div class=\"alert\">This Resource Group is currently disabled.</div>"));
			}
			contentview.add(new DivRepWrapper(form));
			
			//setup crumbs
			BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
			bread_crumb.addCrumb("Topology",  parent_page);
			bread_crumb.addCrumb(title,  null);
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