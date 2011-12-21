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

import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
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

    public ResourceEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
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
				response.sendRedirect( StaticConfig.getApplicationBase()+"/resource?id="+resource_id);
			}
			try {
				ResourceRecord keyrec = new ResourceRecord();
				keyrec.id = resource_id;
				rec = model.get(keyrec);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = "Resource Update";
		} else {
			rec = new ResourceRecord();
			title = "New Resource";	
			
			String rg_id_str = request.getParameter("rg_id");
			if(rg_id_str != null) {
				rec.resource_group_id = Integer.parseInt(rg_id_str);
			}
		}

		DivRepForm form;
		String origin_url = StaticConfig.getApplicationBase()+"/"+parent_page;
		try {
			form = new ResourceFormDE(context, rec, origin_url);
		} catch (SQLException e) {
			throw new ServletException(e);
		}

		//put the form in a view and display
		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
		contentview.add(new DivRepWrapper(form));

		//setup crumbs
		BreadCrumbView bread_crumb = new BreadCrumbView();
		bread_crumb.addCrumb("Topology", parent_page);
		bread_crumb.addCrumb(rec.name,  null);
		contentview.setBreadCrumb(bread_crumb);

		Page page = new Page(context, new MenuView(context, parent_page), contentview, createSideView(rec));
		page.render(response.getWriter());
	}
	
	private SideContentView createSideView(ResourceRecord rec)
	{
		SideContentView view = new SideContentView();
		
		view.add(new HtmlView("<h3>Other Actions</h3>"));
		view.add(new HtmlView("<div class=\"indent\">"));
		if(rec.id != null) {	
			view.add(new HtmlView("<p><a href=\""+StaticConfig.getApplicationBase()+"/resourcedowntimeedit?rid="+rec.id+"\">Add New Downtime</a></p>"));
		}
		view.add(new HtmlView("<p><a href=\""+StaticConfig.getApplicationBase()+"/resourcegroupedit\">Register New Resource Group</a></p>"));
		view.add(new HtmlView("</div>"));
		
		view.addContactNote();		
		return view;
	}
}