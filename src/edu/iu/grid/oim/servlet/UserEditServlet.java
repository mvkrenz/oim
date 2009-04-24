package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.webif.divex.DivExRoot;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Config;

import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.view.divex.form.SiteFormDE;
import edu.iu.grid.oim.view.divex.form.UserFormDE;

public class UserEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(UserEditServlet.class);  
	private String current_page = "user";	

    public UserEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setContext(request);
		auth.check("admin");
	
		String title;

		DNRecord rec;
		try {
			//if site_id is provided then we are doing update, otherwise do new.
			String dn_id_str = request.getParameter("id");
			if(dn_id_str != null) {
				//pull record to update
				int id = Integer.parseInt(dn_id_str);
				DNModel dnmodel = new DNModel(context);
				rec = dnmodel.get(id);
				title = "Update User";
			} else {
				rec = new DNRecord();
				title = "New User";	
			}
		
			UserFormDE form = new UserFormDE(context, rec, Config.getApplicationBase()+"/"+current_page);
			
			//put the form in a view and display
			ContentView contentview = new ContentView();
			contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
			contentview.add(new DivExWrapper(form));
			
			//setup crumbs
			BreadCrumbView bread_crumb = new BreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("User",  "user");
			bread_crumb.addCrumb(rec.dn_string, null);
			contentview.setBreadCrumb(bread_crumb);
			
			Page page = new Page(createMenuView("admin"), contentview, createSideView());			
			page.render(response.getWriter());	
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("TODO", new HtmlView("Whatever"));
		return view;
	}
}