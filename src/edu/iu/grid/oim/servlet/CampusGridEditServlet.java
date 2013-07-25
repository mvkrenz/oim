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
import edu.iu.grid.oim.model.db.CampusGridModel;
import edu.iu.grid.oim.model.db.ProjectModel;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.CampusGridRecord;
import edu.iu.grid.oim.model.db.record.ProjectRecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LogView;
import edu.iu.grid.oim.view.ProjectView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.form.CampusGridFormDE;

public class CampusGridEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(CampusGridEditServlet.class);  
	private String parent_page = "campusgrid";	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("edit_my_campusgrid");		
		
		CampusGridRecord rec;
		ArrayList<ProjectRecord> projects = null;
		ArrayList<LogRecord> logs = null;
		
		//if id is provided then we are doing update, otherwise do new.
		String id_str = request.getParameter("id");
		if(id_str != null) {
			
			//check authorization
			int id = Integer.parseInt(id_str);
			CampusGridModel model = new CampusGridModel(context);
			if(!model.canEdit(id)) {			
				response.sendRedirect("campusgrid?id="+id);
			}
			
			try {
				CampusGridRecord keyrec = new CampusGridRecord();
				keyrec.id = id;
				rec = model.get(keyrec);
				
				//pull projects
				ProjectModel pmodel = new ProjectModel(context);
				projects = pmodel.getByCGID(id);
				
				//pull logs
				LogModel logmodel = new LogModel(context);
				logs = logmodel.search("edu.iu.grid.oim.model.db.CampusGrid%", String.valueOf(id)+"%");
				
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
		} else {
			rec = new CampusGridRecord();
		}
			
		CampusGridFormDE form;
		try {
			form = new CampusGridFormDE(context, rec, parent_page);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		
		//put the form in a view and display
		ContentView contentview = new ContentView(context);
		//contentview.add(new HtmlView("<h1>"+title+"</h1>"));	

		if(rec.disable != null && rec.disable == true) {
			contentview.add(new HtmlView("<div class=\"alert\">This CampusGrid is disabled.</div>"));
		}
		
		contentview.add(new DivRepWrapper(form));
		
		//setup crumbs
		BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
		bread_crumb.addCrumb("Campus Grid",  parent_page);
		bread_crumb.addCrumb(rec.name,  null);
		contentview.setBreadCrumb(bread_crumb);
		
		BootPage page = new BootPage(context, new BootMenuView(context, parent_page), contentview, createSideView(projects, logs, rec));
		page.render(response.getWriter());	
	}
	
	private SideContentView createSideView(ArrayList<ProjectRecord> projects, ArrayList<LogRecord> logs, CampusGridRecord rec)
	{
		SideContentView view = new SideContentView();
		if(rec.id != null) {
			view.add(new HtmlView("<p class=\"\"><a class=\"btn\" href=\"campusgrid?id="+rec.id+"\">Show Readonly View</a></p>"));
		}
		
		if(projects != null) {
			view.add(new ProjectView(projects));	
			view.add(new HtmlView("<a href=\"projectedit?cg_id="+rec.id+"\" class=\"btn pull-right\"><i class=\"icon-plus-sign\"></i> Add New Project</a>"));
			view.add(new HtmlView("<br clear=\"both\">"));
		}
		view.addContactNote();		
		if(logs != null) {
			view.add(new LogView(logs));	
		}
		return view;
	}
}