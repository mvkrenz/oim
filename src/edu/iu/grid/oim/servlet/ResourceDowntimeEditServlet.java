package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;
import com.webif.divex.EventListener;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divex.form.ResourceDowntimeFormDE;
import edu.iu.grid.oim.view.divex.form.ResourceFormDE;

public class ResourceDowntimeEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceDowntimeEditServlet.class);  
	private String parent_page = "resourcedowntime";	
	private Integer resource_id;
	private ResourceDowntimeFormDE form;

    public ResourceDowntimeEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setContext(request);
		auth.check("edit_my_resource");
		
		ResourceRecord rec;
		String title;

		String id_str = request.getParameter("id");
		if(id_str != null) {
			//pull record to update
			resource_id = Integer.parseInt(id_str);
			ResourceModel model = new ResourceModel(context);
			if(!model.canEdit(resource_id)) {
				throw new ServletException("You can't edit that");
			}
			try {
				ResourceRecord keyrec = new ResourceRecord();
				keyrec.id = resource_id;
				rec = model.get(keyrec);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = rec.name + " Downtime";
		} else {
			throw new ServletException("Can't do new resource");
		}
	
		String origin_url = Config.getApplicationBase()+"/"+parent_page;
		try {
			form = new ResourceDowntimeFormDE(context, origin_url, resource_id);
			
			//put the form in a view and display
			ContentView contentview = new ContentView();
			contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
			contentview.add(new DivExWrapper(form));
			
			//setup crumbs
			BreadCrumbView bread_crumb = new BreadCrumbView();
			bread_crumb.addCrumb("Resource Downtime", parent_page);
			bread_crumb.addCrumb(rec.name,  null);
			contentview.setBreadCrumb(bread_crumb);
			
			Page page = new Page(createMenuView(parent_page), contentview, createSideView());
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