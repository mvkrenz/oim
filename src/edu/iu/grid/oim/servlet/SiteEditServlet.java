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

import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.view.divex.form.SiteFormDE;

public class SiteEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(SiteEditServlet.class);  
	private String current_page = "site";	

    public SiteEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		auth.check("admin");
		
		SiteRecord rec;
		String title;

		try {
			//if site_id is provided then we are doing update, otherwise do new.
			String site_id_str = request.getParameter("site_id");
			if(site_id_str != null) {
				//pull record to update
				int site_id = Integer.parseInt(site_id_str);
				SiteModel model = new SiteModel(auth);
				SiteRecord keyrec = new SiteRecord();
				keyrec.id = site_id;
				rec = model.get(keyrec);
			title = "Update Site";
			} else {
				rec = new SiteRecord();
			title = "New Site";	
		}
	
		SiteFormDE form;
		String origin_url = BaseURL()+"/"+current_page;
			form = new SiteFormDE(DivExRoot.getInstance(request), rec, origin_url, auth);
		
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