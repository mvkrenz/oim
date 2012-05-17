package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.view.divrep.form.ActionFormDE;
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

public class ActionEditServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ActionEditServlet.class);  
	private String current_page = "action";	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext user = new UserContext(request);
		user.getAuthorization().check("admin");
		
		ActionRecord rec;
		String title;

		//if cpu_info_id is provided then we are doing update, otherwise do new.
		// AG: Do we need any request parameter-value checks?
		String id_str = request.getParameter("id");
		if(id_str != null) {
			//pull record to update
			int id = Integer.parseInt(id_str);
			ActionModel model = new ActionModel(user);
			try {
				rec = model.get(id);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = rec.name;
		} else {
			rec = new ActionRecord();
			title = "New Action";	
		}
	
		ActionFormDE form;
		//String origin_url = StaticConfig.getApplicationBase()+"/"+current_page;
		try {
			form = new ActionFormDE(user, rec, current_page);
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
		bread_crumb.addCrumb("Action",  "action");
		bread_crumb.addCrumb(title,  null);

		contentview.setBreadCrumb(bread_crumb);
		
		BootPage page = new BootPage(user, new BootMenuView(user, "admin"), contentview, createSideView());	
		page.render(response.getWriter());	
		//user.storeDivRepSession();
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		//view.add("Misc-no-op", new HtmlView("Misc-no-op"));
		return view;
	}
}