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
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divex.form.ResourceDowntimeFormDE;
import edu.iu.grid.oim.view.divex.form.ResourceFormDE;
import edu.iu.grid.oim.view.divex.form.ResourceDowntimeFormDE.DowntimeEditor;

public class ResourceDowntimeEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceDowntimeEditServlet.class);  
	private String current_page = "resourcedowntime";	
	private Integer resource_id;
	private ResourceDowntimeFormDE form;

    public ResourceDowntimeEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		auth.check("edit_my_resource");
		
		ResourceRecord rec;
		String title;

		String id_str = request.getParameter("id");
		if(id_str != null) {
			//pull record to update
			resource_id = Integer.parseInt(id_str);
			ResourceModel model = new ResourceModel(auth);
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
			title = "Update Downtime Schedule";
		} else {
			throw new ServletException("Can't do new resource");
		}
	
		String origin_url = Config.getApplicationBase()+"/"+current_page;
		try {
			DivExRoot root = DivExRoot.getInstance(request);
			form = new ResourceDowntimeFormDE(root, auth, origin_url, resource_id);
			
			//put the form in a view and display
			ContentView contentview = new ContentView();
			contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
			contentview.add(new DivExWrapper(form));
			
			Page page = new Page(createMenuView(current_page), contentview, createSideView(root));
			page.render(response.getWriter());	
			
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private SideContentView createSideView(DivExRoot root)
	{
		SideContentView view = new SideContentView();
		
		ButtonDE add_button = new ButtonDE(root, "Add New Downtime");
		add_button.setStyle(ButtonDE.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				try {
					ResourceDowntimeRecord rec = new ResourceDowntimeRecord();
					rec.resource_id = resource_id;
					DowntimeEditor elem = form.addDowntime(rec);
					elem.scrollToShow(null);
				} catch (SQLException e1) {
					log.error(e1);
				}
			}
		});
		view.add("Operation", add_button);
		view.add("TODO", new HtmlView("Whatever"));
		return view;
	}
}