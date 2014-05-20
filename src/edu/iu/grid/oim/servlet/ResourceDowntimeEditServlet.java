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
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LogView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.form.ResourceDowntimeFormDE;

public class ResourceDowntimeEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceDowntimeEditServlet.class);  
	private String parent_page = "resourcedowntime";	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("edit_my_resource");
		
		Integer resource_id = null;
		Integer downtime_id = null;
		ResourceDowntimeFormDE form;
		
		ResourceRecord rec;
		ArrayList<LogRecord> logs = null;

		String rid_str = request.getParameter("rid");
		if(rid_str != null) {
			//pull resource information
			resource_id = Integer.parseInt(rid_str);
			ResourceModel model = new ResourceModel(context);
			if(!model.canEdit(resource_id)) {
				throw new ServletException("You are not authorized to edit this resource!");
			}
			try {
				ResourceRecord keyrec = new ResourceRecord();
				keyrec.id = resource_id;
				rec = model.get(keyrec);
				
				//pull logs
				LogModel logmodel = new LogModel(context);
				logs = logmodel.search("edu.iu.grid.oim.model.db.ResourceDowntime%", String.valueOf(rec.id)+"%");
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			//title = rec.name + " Downtime";
		} else {
			throw new ServletException("resourc id not set");
		}
		
		String did_str = request.getParameter("did");
		if(did_str != null) {
			//pul downtime information
			downtime_id = Integer.parseInt(did_str);
		}
		
		//String origin_url = StaticConfig.getApplicationBase()+"/"+parent_page;
		try {
			form = new ResourceDowntimeFormDE(context, parent_page, resource_id, downtime_id, auth.getTimeZone());
			
			//put the form in a view and display
			ContentView contentview = new ContentView(context);

			//contentview.add(new HtmlView("<h2>"+title+"</h2>"));	
			contentview.add(new DivRepWrapper(form));
			
			//setup crumbs
			BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
			bread_crumb.addCrumb("Resource Downtime", parent_page);
			bread_crumb.addCrumb(rec.name,  null);
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
		view.add("Downtime Class", new HtmlView(
				"<p><b>SCHEDULED</b></p><p>Downtimes that has been scheduled long before the acutual downtime.</p>" + 
				"<p><b>UNSCHEDULED</b></p><p>Downtimes that has occured due to an event such as a sudden hardware failure, or any unforseen circumstances.</p>"));		
		if(logs != null) {
			view.add(new LogView(logs));	
		}
		return view;
	}
}