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
import edu.iu.grid.oim.model.db.ProjectModel;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.ProjectRecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LogView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.form.ProjectFormDE;

public class ProjectEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ProjectEditServlet.class);  
	private String parent_page = "project";	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		//auth.check("edit_my_vo");	//??
		
		ProjectRecord rec = new ProjectRecord();
		ArrayList<LogRecord> logs = null;
		
		//if vo_id is provided then we are doing update, otherwise do new.
		String id_str = request.getParameter("id");
		if(id_str != null) {
			
			//check authorization
			int id = Integer.parseInt(id_str);
			ProjectModel model = new ProjectModel(context);
			if(!model.canEdit(id)) {
				response.sendRedirect("project?id="+id);
			}
			
			try {
				rec = model.get(id);
				LogModel logmodel = new LogModel(context);
				logs = logmodel.search("edu.iu.grid.oim.model.db.Project%", String.valueOf(id)+"%");
				
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			//title = "Virtual Organization Update";
		}
			
		try {
			ProjectFormDE form = new ProjectFormDE(context, rec, parent_page);
			ContentView contentview = new ContentView(context);
			contentview.add(new DivRepWrapper(form));
			
			//setup crumbs
			BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
			bread_crumb.addCrumb("Project",  parent_page);
			bread_crumb.addCrumb(rec.name,  null);
			contentview.setBreadCrumb(bread_crumb);
			
			BootPage page = new BootPage(context, new BootMenuView(context, parent_page), contentview, createSideView(context, logs, rec));
			page.render(response.getWriter());	
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private SideContentView createSideView(UserContext context, ArrayList<LogRecord> logs, ProjectRecord rec)
	{
		SideContentView view = new SideContentView();
		
		if(rec.id != null) {
			view.add(new HtmlView("<p><a class=\"btn\" href=\"project?id="+rec.id+"\">Show Readonly View</a></p>"));
		}
	
		if(logs != null) {
			view.add(new LogView(logs));	
		}
		return view;
	}
}