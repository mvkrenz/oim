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
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divex.form.OsgGridTypeFormDE;

public class OsgGridTypeEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(OsgGridTypeEditServlet.class);  
	private String current_page = "osg_grid_type";	

    public OsgGridTypeEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		
		OsgGridTypeRecord rec;
		String title;

		//if osg_grid_type_id is provided then we are doing update, otherwise do new.
		// AG: Do we need any request parameter-value checks?
		String osg_grid_type_id_str = request.getParameter("osg_grid_type_id");
		if(osg_grid_type_id_str != null) {
			//pull record to update
			int osg_grid_type_id = Integer.parseInt(osg_grid_type_id_str);
			OsgGridTypeModel model = new OsgGridTypeModel(auth);
			try {
				OsgGridTypeRecord keyrec = new OsgGridTypeRecord();
				keyrec.id = osg_grid_type_id;
				rec = model.get(keyrec);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = "Update OSG Grid Type";
		} else {
			rec = new OsgGridTypeRecord();
			title = "New OSG Grid Type";	
		}
	
		OsgGridTypeFormDE form;
		String origin_url = BaseURL()+"/"+current_page;
		try {
			form = new OsgGridTypeFormDE(DivExRoot.getInstance(request), rec, origin_url, auth);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		
		//put the form in a view and display
		ContentView contentview = new ContentView();
		contentview.add("<h1>"+title+"</h1>");	
		contentview.add(form);
		
		Page page = new Page(createMenuView("admin"), contentview, createSideView());	
		page.addExternalJS(BaseURL()+"/osggridtypeedit.js");
		page.render(response.getWriter());	
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("Misc-no-op", new HtmlView("Misc-no-op"));
		return view;
	}
}