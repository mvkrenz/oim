package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.SideContentView;

public class HomeServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(HomeServlet.class);  
    
    public HomeServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		MenuView menuview = new MenuView(context, "home");
		ContentView contentview;
		contentview = createContentView();
		Page page = new Page(context, menuview, contentview, createSideView());
		page.render(response.getWriter());
	}
	
	protected ContentView createContentView()
	{
		ContentView contentview = new ContentView();
		
		contentview.add(new HtmlView("<h1>OIM Home</h1>"));

		// TODO agopu: need to clean this up with some divs etc. Nicer font, etc.
		String welcome_string = "<p>Welcome to the OSG Information Management System.</p>";
		if(auth.isGuest()) {
			welcome_string += "<p>Please provide a DOE certificate via your web browser in order to use or register to OIM.</p>";
		} else {
			welcome_string += "<p>In the menu along the top, you will find options for registering or updating information for various OSG entities.</p>";
		}
		welcome_string += "<p>Please see Help page for more information.</p>";
		contentview.add(new HtmlView(welcome_string));
	
		//add confirmation button
		if(auth.isOIMUser()) {
			try {
				contentview.add(new DivRepWrapper(new Confirmation(auth.getContactID(), context)));
			} catch (SQLException e) {
				log.error(e);
			}				
		}

		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();	
		return view;
	}
	
	class Confirmation extends DivRep
	{
		final ContactRecord crec;
		final ContactModel cmodel;
		final Context context;
		
		public Confirmation(Integer contact_id, Context _context) throws SQLException {
			super(_context.getPageRoot());
			
	    	cmodel = new ContactModel(_context);
	    	crec = (ContactRecord) cmodel.get(contact_id).clone();	    	
	    	context = _context;
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void render(PrintWriter out) {
			if(crec.isConfirmationExpired()) {
				out.write("<div id=\""+getNodeID()+"\">");
				out.write("<h3>Content Confirmation</h3>");
			
				out.write("<p class=\"divrep_round divrep_elementerror\">You have not recently confirmed that your information in OIM is current</p>");
		
				out.write("<p>The last time you confirmed your profile information was "+crec.confirmed.toString()+"</p>");
				out.write("<p>Please go to the ");
				out.write("<a href=\""+StaticConfig.getApplicationBase()+"/profileedit"+"\">profile</a>");
				out.write(" page to check your profile information</p>");
				out.write("</div>");
			}
		}	
	}
}
