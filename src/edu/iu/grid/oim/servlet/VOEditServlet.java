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
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.LogModel;
import edu.iu.grid.oim.model.db.ProjectModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.ProjectRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LogView;
import edu.iu.grid.oim.view.ProjectView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.form.VOFormDE;

public class VOEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(VOEditServlet.class);  
	private String parent_page = "vo";	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("edit_my_vo");		
		
		VORecord rec;
		ArrayList<LogRecord> logs = null;
		ArrayList<ProjectRecord> projects = null;
		
		//if vo_id is provided then we are doing update, otherwise do new.
		String vo_id_str = request.getParameter("id");
		if(vo_id_str != null) {
			
			//check authorization
			int vo_id = Integer.parseInt(vo_id_str);
			VOModel model = new VOModel(context);
			if(!model.canEdit(vo_id)) {
				response.sendRedirect("vo?id="+vo_id);
			}
			
			try {
				VORecord keyrec = new VORecord();
				keyrec.id = vo_id;
				rec = model.get(keyrec);
				
				//pull projects
				ProjectModel pmodel = new ProjectModel(context);
				projects = pmodel.getByVOID(vo_id);
				
				//pull logs
				LogModel logmodel = new LogModel(context);
				logs = logmodel.search("edu.iu.grid.oim.model.db.VO%", String.valueOf(vo_id)+"%");
				
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			//title = "Virtual Organization Update";
		} else {
			rec = new VORecord();
			//title = "New Virtual Organization";	
		}
			
		VOFormDE form;
		try {
			form = new VOFormDE(context, rec, parent_page);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		
		//put the form in a view and display
		ContentView contentview = new ContentView(context);
		//contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
		if(rec.active != null && rec.active == false) {
			contentview.add(new HtmlView("<div class=\"alert\">This Virtual Organization is currently inactive.</div>"));
		}
		if(rec.disable != null && rec.disable == true) {
			contentview.add(new HtmlView("<div class=\"alert\">This Virtual Organization is currently disabled.</div>"));
		}
		
		contentview.add(new DivRepWrapper(form));
		
		//setup crumbs
		BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
		bread_crumb.addCrumb("Virtual Organization",  parent_page);
		bread_crumb.addCrumb(rec.name,  null);
		contentview.setBreadCrumb(bread_crumb);
		
		BootPage page = new BootPage(context, new BootMenuView(context, parent_page), contentview, createSideView(context, projects, logs, rec));
		page.render(response.getWriter());	
	}
	
	private SideContentView createSideView(UserContext context, ArrayList<ProjectRecord> projects, ArrayList<LogRecord> logs, VORecord rec)
	{
		SideContentView view = new SideContentView();
		
		if(rec.id != null) {
			view.add(new HtmlView("<p><a class=\"btn\" href=\"vo?id="+rec.id+"\">Show Readonly View</a></p>"));
		}
		
		view.addRARequest(context, rec);	
		if(projects != null) {
			view.add(new ProjectView(projects, new ContactModel(context)));	
			view.add(new HtmlView("<a href=\"projectedit?vo_id="+rec.id+"\" class=\"btn pull-right\"><i class=\"icon-plus-sign\"></i> Add New Project</a>"));
			view.add(new HtmlView("<br clear=\"both\">"));
		}
		view.addContactNote();	
		if(logs != null) {
			view.add(new LogView(logs));	
		}
		return view;
	}
}