package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import edu.iu.grid.oim.lib.Config;

import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.view.divex.form.FieldOfScienceFormDE;

public class FieldOfScienceEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(FieldOfScienceEditServlet.class);  
	private String current_page = "fieldofscience";	

    public FieldOfScienceEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		auth.check("admin");
		
		FieldOfScienceRecord rec;
		String title;

		// TODO: Do we need any request parameter-value checks?
		String id_str = request.getParameter("id");
		if(id_str != null) {
			//pull record to update
			int id = Integer.parseInt(id_str);
			FieldOfScienceModel model = new FieldOfScienceModel(context);
			try {
				rec = model.get(id);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = "Update Field Of Science";
		} else {
			rec = new FieldOfScienceRecord();
			title = "New Field Of Science";	
		}
	
		FieldOfScienceFormDE form;
		String origin_url = Config.getApplicationBase()+"/"+current_page;
		try {
			form = new FieldOfScienceFormDE(context, rec, origin_url);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		
		//put the form in a view and display
		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
		contentview.add(new DivExWrapper(form));
		
		//setup crumbs
		BreadCrumbView bread_crumb = new BreadCrumbView();
		bread_crumb.addCrumb("Administration",  "admin");
		bread_crumb.addCrumb("Field Of Science",  "fieldofscience");
		bread_crumb.addCrumb(rec.name,  null);

		contentview.setBreadCrumb(bread_crumb);
		
		Page page = new Page(new MenuView(context, "admin"), contentview, createSideView());	
		page.render(response.getWriter());	
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("Misc-no-op", new HtmlView("Misc-no-op"));
		return view;
	}
}