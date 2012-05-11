package edu.iu.grid.oim.servlet;

import java.io.IOException;

import java.sql.SQLException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;

import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.divrep.form.QuotaConfigFormDE;


public class QuotaAdminServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(QuotaAdminServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");
		
		try {
			//construct view
			BootMenuView menuview = new BootMenuView(context, "admin");;
			ContentView contentview = createContentView(context);
			
			BootPage page = new BootPage(context, menuview, contentview, null);
			page.render(response.getWriter());				
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(final UserContext context) throws ServletException, SQLException
	{
		ContentView contentview = new ContentView();
		
		//setup crumbs
		BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
		bread_crumb.addCrumb("Administration",  "admin");
		bread_crumb.addCrumb("Quota Administration",  null);
		contentview.setBreadCrumb(bread_crumb);
		
		QuotaConfigFormDE form = new QuotaConfigFormDE(context);
		
		contentview.add(new DivRepWrapper(form));		
		return contentview;
	}
}
