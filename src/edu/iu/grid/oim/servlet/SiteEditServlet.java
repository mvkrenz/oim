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

import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.view.divex.form.SiteFormDE;

public class SiteEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(SiteEditServlet.class);  
	private String parent_page = "site";	

    public SiteEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		//setContext(request);
		auth.check("edit_all_site");
		
		SiteRecord rec;
		String title;

		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<script src=\"http://maps.google.com/maps?file=api&v=2&key="+Config.getGMapAPIKey()+"\" type=\"text/javascript\"></script>"));

		try {
			//if site_id is provided then we are doing update, otherwise do new.
			String site_id_str = request.getParameter("site_id");
			if(site_id_str != null) {
				//pull record to update
				int site_id = Integer.parseInt(site_id_str);
				SiteModel model = new SiteModel(context);
				SiteRecord keyrec = new SiteRecord();
				keyrec.id = site_id;
				rec = model.get(keyrec);
				title = "Update Site";
			} else {
				rec = new SiteRecord();
				title = "New Site";	
			}
	
			String origin_url = Config.getApplicationBase()+"/"+parent_page;
			SiteFormDE form = new SiteFormDE(context, rec, origin_url);
			contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
			contentview.add(new DivExWrapper(form));
			
			Page page = new Page(new MenuView(context, parent_page), contentview, createSideView());
			
			page.render(response.getWriter());	
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("About", new HtmlView("This form allows you to edit this administrative site's (a.k.a deparment's) registration information.</p>"));		
		view.addContactNote();		
		// view.addContactLegent();		
		return view;
	}
}