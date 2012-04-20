package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepStaticContent;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.divrep.form.ContactFormDE;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

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
		
		ContactRecord rec;
		try {
			rec = auth.getContact();
				
			//String origin_url = StaticConfig.getApplicationBase()+"/"+parent_page;
			form = new ContactFormDE(context, rec, parent_page, true);
			
			//put the form in a view and display
			ContentView contentview = new ContentView();
			contentview.add(new HtmlView("<h1>My Profile</h1>"));	
			/*
			if(auth.isDisabled()) {
				contentview.add(new HtmlView(auth.getDisabledUserWarning()));
			} 
			*/
			contentview.add(new DivRepWrapper(form));
			
			BootPage page = new BootPage(context, new BootMenuView(context, "profileedit"), contentview, createSideView(context));
			page.render(response.getWriter());	
			
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private SideContentView createSideView(UserContext context) throws SQLException
	{
		Authorization auth = context.getAuthorization();
		SideContentView view = new SideContentView();
		/*
		view.add("About", new HtmlView("<p>This page lets you edit your OIM profile.</p>"+
				"<p>On your OIM profile, you can set contact information like email, phone number and extension, an email address for SMS text messages, and postal address (only applicable to human contacts).</p>"+
				"<p>You can also set your local timezone so other applications like GOCTicket and MyOSG can display timestamps in your local timezone.</p>"+
				"<p>You can also provide a link to an image that you would like to use as your profile picture!</p>"));
		*/
		HashSet<String> auth_types = auth.getAuthorizationTypesForCurrentDN();
		view.add("<h3>Your Authentication Types</h3>");
		if(auth_types.size() > 0) {
			//compose list of auth types
			StringBuffer auth_type_string = new StringBuffer();
			auth_type_string.append("<ul>");
			for (String auth_type: auth_types) {
				auth_type_string.append("<li>" + auth_type + "</li>");
			}
			auth_type_string.append("</ul>");
			view.add(new HtmlView(auth_type_string.toString()));	
		} else {
			view.add(new HtmlView("N/A (Your account is de-activated)"));		
		}
		
		return view;
	}
}