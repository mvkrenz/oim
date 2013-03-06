package edu.iu.grid.oim.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.divrep.form.ContactFormDE;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;

public class ProfileEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ProfileEditServlet.class);  
	private String parent_page = "home";	
	private ContactFormDE form;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("edit_my_contact");
		
		ContactRecord rec = auth.getContact();

		form = new ContactFormDE(context, rec, parent_page);
		
		//put the form in a view and display
		ContentView contentview = new ContentView(context);
		contentview.add(new HtmlView("<h1>My Profile</h1>"));	
		contentview.add(new DivRepWrapper(form));
		
		BootPage page = new BootPage(context, new BootMenuView(context, "profileedit"), contentview, null);
		page.render(response.getWriter());	
			
	}
}