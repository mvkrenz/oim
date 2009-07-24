package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.common.DivRepForm;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.form.ResourceFormDE;

public class ResourceEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceEditServlet.class);  
	private String parent_page = "resource";	

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
				throw new ServletException("You can't edit that");
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
		bread_crumb.addCrumb("Resource", parent_page);
		bread_crumb.addCrumb(rec.name,  null);
		contentview.setBreadCrumb(bread_crumb);

		Page page = new Page(new MenuView(context, parent_page), contentview, createSideView());
		page.render(response.getWriter());
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("About", new HtmlView("This form allows you to edit this resource's registration information.</p>"));		
		view.addContactNote();		
		// view.addContactLegent();		
		return view;
	}
}