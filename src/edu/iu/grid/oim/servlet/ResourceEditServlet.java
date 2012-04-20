package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepForm;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.form.ResourceFormDE;

public class ResourceEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceEditServlet.class);  
	private String parent_page = "topology";	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();		
		auth.check("edit_my_resource");

		ResourceRecord rec;
		String title;
		
		String resource_id_str = request.getParameter("id");
		if(resource_id_str != null) {
			//pull record to update
			int resource_id = Integer.parseInt(resource_id_str);
			ResourceModel model = new ResourceModel(context);
			if(!model.canEdit(resource_id)) {
				//throw new AuthorizationException("You can't edit this resource ID:" + resource_id_str);
				response.sendRedirect("resource?id="+resource_id);
			}
			try {
				ResourceRecord keyrec = new ResourceRecord();
				keyrec.id = resource_id;
				rec = model.get(keyrec);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = "Edit Resource " + rec.name;
		} else {
			rec = new ResourceRecord();
			title = "New Resource";	
			
			String rg_id_str = request.getParameter("rg_id");
			if(rg_id_str != null) {
				rec.resource_group_id = Integer.parseInt(rg_id_str);
			}
		}
		
		
		DivRepForm form;
		//String origin_url = StaticConfig.getApplicationBase()+"/"+parent_page;
		try {
			form = new ResourceFormDE(context, rec, parent_page);
		} catch (SQLException e) {
			throw new ServletException(e);
		}

		//put the form in a view and display
		ContentView contentview = new ContentView();
		if(rec.active != null && rec.active == false) {
			contentview.add(new HtmlView("<div class=\"alert\">This resource is currently inactive.</div>"));
		}
		if(rec.active != null && rec.disable == true) {
			contentview.add(new HtmlView("<div class=\"alert\">This resource is currently disabled.</div>"));
		}
		contentview.add(new DivRepWrapper(form));

		//setup crumbs
		BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
		bread_crumb.addCrumb("Topology", parent_page);
		bread_crumb.addCrumb(title,  null);
		contentview.setBreadCrumb(bread_crumb);

		BootPage page = new BootPage(context, new BootMenuView(context, parent_page), contentview, createSideView(rec));
		page.render(response.getWriter());
	}
	
	private SideContentView createSideView(ResourceRecord rec)
	{
		SideContentView view = new SideContentView();
		
		if(rec.id != null) {
			view.add(new HtmlView("<p><a class=\"btn\" href=\"resource?id="+rec.id+"\">Show Read-only View</a></p>"));
			view.add(new HtmlView("<p><a class=\"btn\" href=\"resourcedowntimeedit?rid="+rec.id+"\">Add New Downtime</a></p>"));
		}
		
		view.addContactNote();		
		return view;
	}
}