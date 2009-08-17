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

import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.view.divrep.form.ResourceGroupFormDE;
import edu.iu.grid.oim.view.divrep.form.SiteFormDE;

public class ResourceGroupEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceGroupEditServlet.class);  
	private String parent_page = "resourcegroup";	
	
    public ResourceGroupEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		//setContext(request);
		auth.check("edit_all_resource_group");
		
		ResourceGroupRecord rec;
		String title;

		try {
			//if site_id is provided then we are doing update, otherwise do new.
			String id_str = request.getParameter("id");
			if(id_str != null) {
				//pull record to update
				int id = Integer.parseInt(id_str);
				ResourceGroupModel model = new ResourceGroupModel(context);
				ResourceGroupRecord keyrec = new ResourceGroupRecord();
				keyrec.id = id;
				rec = model.get(keyrec);
				title = "Update Resource Group";
			} else {
				rec = new ResourceGroupRecord();
				title = "New Resource Group";	
			}

			ResourceGroupFormDE form = new ResourceGroupFormDE(context, rec, StaticConfig.getApplicationBase()+"/"+parent_page);
			
			//put the form in a view and display
			ContentView contentview = new ContentView();
			contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
			contentview.add(new DivRepWrapper(form));
			
			//setup crumbs
			BreadCrumbView bread_crumb = new BreadCrumbView();
			bread_crumb.addCrumb("Resource Group",  parent_page);
			bread_crumb.addCrumb(rec.name,  null);
			contentview.setBreadCrumb(bread_crumb);
			
			Page page = new Page(context, new MenuView(context, "resourcegroup"), contentview, createSideView());
			
			page.render(response.getWriter());	
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("About", new HtmlView("This form allows you to edit this resource_group's registration information.</p>"));		
		view.addContactNote();		
		// view.addContactLegent();		
		return view;
	}
}