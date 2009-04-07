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

import edu.iu.grid.oim.model.db.NotificationModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.NotificationRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.view.divex.form.NotificationFormDE;
import edu.iu.grid.oim.view.divex.form.SiteFormDE;

public class NotificationEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(NotificationEditServlet.class);  
	private String current_page = "notification";	

    public NotificationEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		
		NotificationRecord rec;
		try {
			//if site_id is provided then we are doing update, otherwise do new.
			String notification_id_str = request.getParameter("id");
			String title;
			if(notification_id_str != null) {
				//pull record to update
				int notification_id = Integer.parseInt(notification_id_str);
				NotificationModel model = new NotificationModel(auth);
				NotificationRecord keyrec = new NotificationRecord();
				keyrec.id = notification_id;
				rec = model.get(keyrec);
				title = "Update Notification";
			} else {
				rec = new NotificationRecord();
				title = "New Notification";	
			}
		
			NotificationFormDE form;
			String origin_url = BaseURL()+"/"+current_page;
				form = new NotificationFormDE(DivExRoot.getInstance(request), rec, origin_url, auth);
			
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