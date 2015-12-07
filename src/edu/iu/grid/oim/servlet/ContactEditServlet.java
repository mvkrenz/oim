package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.divrep.form.ContactFormDE;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.SideContentView;

public class ContactEditServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ContactEditServlet.class);  
	private String parent_page = "contact";	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
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
				throw new AuthorizationException("you can't edit contact ID:" + id_str);
			}
			try {
				ContactRecord keyrec = new ContactRecord();
				keyrec.id = id;
				rec = model.get(keyrec);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = rec.name;
		} else {
			rec = new ContactRecord();
			rec.submitter_dn_id = auth.getDNID();
			title = "New Contact";	
		}

		//String origin_url = StaticConfig.getApplicationBase()+"/"+parent_page;
		ContactFormDE form = new ContactFormDE(context, rec, parent_page);
		
		//put the form in a view and display
		ContentView contentview = new ContentView(context);
		if(rec.id != null) {
			contentview.add(new HtmlView("<p class=\"pull-right\"><a class=\"btn\" href=\"contact?id="+rec.id+"\">Show Readonly View</a></p>"));
		}
		contentview.add(new DivRepWrapper(form));
		
		//setup crumbs
		BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
		bread_crumb.addCrumb("OSG Contact",  parent_page);
		bread_crumb.addCrumb(title,  null);
		contentview.setBreadCrumb(bread_crumb);
		
		BootPage page = new BootPage(context, new BootMenuView(context, parent_page), contentview, null);
		page.render(response.getWriter());	
	}
	
	private SideContentView createSideView(ContactRecord rec)
	{
		SideContentView view = new SideContentView();
		return view;
	}
}