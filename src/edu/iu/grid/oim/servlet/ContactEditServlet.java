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
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.view.divex.form.ContactFormDE;
import edu.iu.grid.oim.view.divex.form.SCFormDE;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

public class ContactEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ContactEditServlet.class);  
	private String parent_page = "contact";	

    public ContactEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setContext(request);
		auth.check("edit_my_contact");
		
		ContactRecord rec;
		String title;

		//if sc_id is provided then we are doing update, otherwise do new.
		String id_str = request.getParameter("id");
		if(id_str != null) {
			//check authorization
			ContactModel model = new ContactModel(context);
			int id = Integer.parseInt(id_str);
			if(!model.canEdit(id)) {
				throw new ServletException("you can't edit that");
			}
			try {
				ContactRecord keyrec = new ContactRecord();
				keyrec.id = id;
				rec = model.get(keyrec);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = "Update Contact";
		} else {
			rec = new ContactRecord();
			rec.submitter_dn_id = auth.getDNID();
			title = "New Contact";	
		}

		String origin_url = Config.getApplicationBase()+"/"+parent_page;
		ContactFormDE form = new ContactFormDE(context, rec, origin_url);
		
		//put the form in a view and display
		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
		contentview.add(new DivExWrapper(form));
		
		//setup crumbs
		BreadCrumbView bread_crumb = new BreadCrumbView();
		bread_crumb.addCrumb("Contact",  parent_page);
		bread_crumb.addCrumb(rec.name,  null);
		contentview.setBreadCrumb(bread_crumb);
		
		Page page = new Page(new MenuView(context, parent_page), contentview, createSideView());
		page.render(response.getWriter());	
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("TODO", new HtmlView("Whatever"));
		return view;
	}
}