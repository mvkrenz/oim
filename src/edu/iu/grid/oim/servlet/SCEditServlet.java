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
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.view.divex.form.SCFormDE;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

public class SCEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(SCEditServlet.class);  
	private String parent_page = "sc";	

    public SCEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setContext(request);
		auth.check("edit_my_sc");
		
		SCRecord rec;
		String title;

		//if sc_id is provided then we are doing update, otherwise do new.
		String sc_id_str = request.getParameter("sc_id");
		if(sc_id_str != null) {
			//check authorization
			int sc_id = Integer.parseInt(sc_id_str);
			SCModel model = new SCModel(context);			
			if(!model.canEdit(sc_id)) {
				throw new ServletException("Sorry, you don't have permission to edit this SC " + sc_id);
			}
			
			try {
				SCRecord keyrec = new SCRecord();
				keyrec.id = sc_id;
				rec = model.get(keyrec);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = "Update Support Center";
		} else {
			rec = new SCRecord();
			title = "New Support Center";	
		}
	
		SCFormDE form;
		String origin_url = Config.getApplicationBase()+"/"+parent_page;
		try {
			form = new SCFormDE(context, rec, origin_url);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		
		//put the form in a view and display
		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
		contentview.add(new DivExWrapper(form));
		
		//setup crumbs
		BreadCrumbView bread_crumb = new BreadCrumbView();
		bread_crumb.addCrumb("Support Center",  parent_page);
		bread_crumb.addCrumb(rec.name,  null);
		contentview.setBreadCrumb(bread_crumb);
		
		Page page = new Page(createMenuView(parent_page), contentview, createSideView());		
		page.render(response.getWriter());	
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("TODO", new HtmlView("Whatever"));
		return view;
	}
}