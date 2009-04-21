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

import edu.iu.grid.oim.model.db.ServiceGroupModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.ServiceGroupRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.view.divex.form.ResourceGroupFormDE;
import edu.iu.grid.oim.view.divex.form.ServiceGroupFormDE;
import edu.iu.grid.oim.view.divex.form.SiteFormDE;

public class ServiceGroupEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ServiceGroupEditServlet.class);  
	
    public ServiceGroupEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		auth.check("admin");
		
		ServiceGroupRecord rec;
		String title;

		try {
			//if site_id is provided then we are doing update, otherwise do new.
			String id_str = request.getParameter("id");
			if(id_str != null) {
				//pull record to update
				int id = Integer.parseInt(id_str);
				ServiceGroupModel model = new ServiceGroupModel(auth);
				ServiceGroupRecord keyrec = new ServiceGroupRecord();
				keyrec.id = id;
				rec = model.get(keyrec);
				title = "Update Service Group";
			} else {
				rec = new ServiceGroupRecord();
				title = "New Service Group";	
			}

			ServiceGroupFormDE form = new ServiceGroupFormDE(DivExRoot.getInstance(request), rec, Config.getApplicationBase()+"/servicegroup", auth);
			
			//put the form in a view and display
			ContentView contentview = new ContentView();
			contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
			contentview.add(new DivExWrapper(form));
			
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