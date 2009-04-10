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
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divex.form.ResourceFormDE;

public class ResourceEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceEditServlet.class);  
	private String current_page = "resource";	

    public ResourceEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		
		ResourceRecord rec;
		String title;

		String resource_id_str = request.getParameter("resource_id");
		if(resource_id_str != null) {
			//pull record to update
			int resource_id = Integer.parseInt(resource_id_str);
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
			title = "Update Resource";
		} else {
			rec = new ResourceRecord();
			title = "New Resource";	
		}
	
		ResourceFormDE form;
		String origin_url = BaseURL()+"/"+current_page;
		try {
			form = new ResourceFormDE(DivExRoot.getInstance(request), rec, origin_url, auth);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		
		//put the form in a view and display
		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
		contentview.add(new DivExWrapper(form));
		
		Page page = new Page(createMenuView(current_page), contentview, createSideView());
		
		//for contact editor
		page.addExternalCSS(BaseURL()+"/jquery/plugin/jquery.autocomplete.css");
		page.addExternalJS(BaseURL()+"/jquery/plugin/jquery.autocomplete.js");
		page.addExternalJS(BaseURL()+"/autocomplete.js");
		
		page.render(response.getWriter());	
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("TODO", new HtmlView("Whatever"));
		return view;
	}
}