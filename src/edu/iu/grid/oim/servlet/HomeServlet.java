package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import com.divrep.DivRep;
import com.divrep.DivRepEvent;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.ContactAssociationView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.ToolTip;

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
	
	protected ContentView createContentView() throws ServletException
	{
		ContentView contentview = new ContentView();
		
		contentview.add(new HtmlView("<h1>OIM</h1>"));

		// TODO agopu: need to clean this up with some divs etc. Nicer font, etc.
		String welcome_string = "<p>The topology used by various OSG systems and services based on the <a target=\"_blank\" href=\"http://osg-docdb.opensciencegrid.org/cgi-bin/ShowDocument?docid=18.\">OSG Blueprint Document</a></p>";
		if(auth.isGuest()) {
			welcome_string += auth.getNoDNWarning(); 		
		}
		else if(!auth.isOIMUser()) {
			if(auth.isDisabledOIMUser()) {
				welcome_string += auth.getDisabledUserWarning();		
			} else {
				welcome_string += auth.getUnregisteredUserWarning();		
			}
		}
		
		//welcome_string += "<p>Please see Help page for more information.</p>";
		contentview.add(new HtmlView(welcome_string));
	
		//add confirmation button
		if(auth.isOIMUser()) {
			try {
				contentview.add(new DivRepWrapper(new Confirmation(auth.getContactID(), context)));
			} catch (SQLException e) {
				log.error(e);
			}				
		}
		
		//show entities that this user is associated
		if(auth.isOIMUser()) {
			ToolTip tip = new ToolTip("Your account is associated with the following entities, and therefore allows you to modify their content.");
			contentview.add(new HtmlView("<h2>Your OSG Entities "+tip.render()+"</h2>"));
			try {
				ContactAssociationView caview = new ContactAssociationView(context, auth.getContactID());
				caview.showNewButtons(true);
				contentview.add(caview);
			} catch (SQLException e) {
				throw new ServletException(e);
			}
		}
		
		//show oim hierarchy doc
		ToolTip tip = new ToolTip("The following slideshow presentation walks you through various entities in the OSG topology used by OIM, and describes the relationships between those entities. If you are new to the OSG and/or OIM, we strongly urge you to take a few minutes to go through this slideshow!");
		contentview.add(new HtmlView("<h2>OSG Topology Slideshow "+tip.render()+"</h2>"));
		contentview.add(new HtmlView("<iframe src=\"http://docs.google.com/present/embed?id=ddtgc5bt_113fp3fmvgp&size=l\" frameborder=\"0\" width=\"700\" height=\"559\"></iframe>"));

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
	    	crec = (ContactRecord) cmodel.get(contact_id);//.clone();	    	
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
