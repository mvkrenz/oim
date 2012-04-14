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
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContactAssociationView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.ExternalLinkView;
import edu.iu.grid.oim.view.GenericView;
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
		BootMenuView menuview = new BootMenuView(context, "home");
		ContentView contentview;
		contentview = createContentView();
		BootPage page = new BootPage(context, menuview, contentview, createSideView());

		GenericView header = new GenericView();
		header.add(new HtmlView("<h1>OSG Information Management System</h1>"));
		header.add(new HtmlView("<p class=\"lead\">Defines the topology used by various OSG services based on the <a target=\"_blank\" href=\"http://osg-docdb.opensciencegrid.org/cgi-bin/ShowDocument?docid=18\">OSG Blueprint Document</a></p>"));

		page.setPageHeader(header);
		
		page.render(response.getWriter());
	}
	
	protected ContentView createContentView() throws ServletException
	{
		ContentView contentview = new ContentView();
		
		//contentview.add(new HtmlView("<h1>OIM</h1>"));

		// TODO agopu: need to clean this up with some divs etc. Nicer font, etc.
		String welcome_string = "";
		/*
		if(auth.isGuest()) {
			welcome_string += auth.getNoDNWarning(); 		
		}
		else if(!auth.isUser()) {
			if(auth.isDisabled()) {
				welcome_string += auth.getDisabledUserWarning();		
			} else {
				welcome_string += auth.getUnregisteredUserWarning();		
			}
		}
		*/
		
		//welcome_string += "<p>Please see Help page for more information.</p>";
		contentview.add(new HtmlView(welcome_string));
	
		//add confirmation button
		if(auth.isUser()) {
			try {
				contentview.add(new DivRepWrapper(new Confirmation(auth.getContact().id, context)));
			} catch (SQLException e) {
				log.error(e);
			}				
		}
		
		//show entities that this user is associated
		if(auth.isUser()) {
			//ToolTip tip = new ToolTip("Your account is associated with the following entities, and therefore allows you to modify their content.");
			//contentview.add(new HtmlView("<h2>Your OSG Entities "+tip.render()+"</h2>"));
			try {
				ContactAssociationView caview = new ContactAssociationView(context, auth.getContact().id);
				caview.showNewButtons(true);
				contentview.add(caview);
			} catch (SQLException e) {
				throw new ServletException(e);
			}
		}
		
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView contentview = new SideContentView();
		contentview.add(new HtmlView("<h3>Documentations</h3>"));
		contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMTermDefinition", "OIM Definitions"));
		contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMRegistrationInstructions", "Registration"));
		contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMMaintTool", "Resource Downtime"));
		contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMStandardOperatingProcedures", "Operating Procedures"));
		/*
		//contentview.add(new HtmlView("<h3>Definitions</h3>"));
		//contentview.add(new HtmlView("<div class=\"indent\">"));
			//contentview.add(new HtmlView("<br/>"));
			//contentview.add(new HtmlView("<a href=\"https://docs.google.com/present/view?id=ddtgc5bt_113fp3fmvgp\">OSG Topology Slideshow</a>"));
			//contentview.add(new HtmlView(""));
		//contentview.add(new HtmlView("</div>"));

		//contentview.add(new HtmlView("<h3>New Registration Help</h3>"));
		//contentview.add(new HtmlView("<div class=\"indent\">"));
			//contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMRegistrationInstructions#Resource_or_Service_Registration", "Resource/Service Registration"));
			//contentview.add(new HtmlView("<br/>"));
			//contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMRegistrationInstructions#Support_Center_Registration", "Support Center (SC) Registration"));
			//contentview.add(new HtmlView("<br/>"));
			//contentview.add(new HtmlView("</div>"));
			
		contentview.add(new HtmlView("<h3>Resource/Service Maintenance</h3>"));
		contentview.add(new HtmlView("<div class=\"indent\">"));
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMMaintTool#Modifying_a_Maintenance_Window", "Modify Existing Maintenance"));
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMMaintTool#Deleting_a_Maintenance_Window", "Cancel Existing Maintenance"));
		contentview.add(new HtmlView("</div>"));
		
		contentview.add(new HtmlView("<h3>Standard Operating Procedures</h3>"));
		contentview.add(new HtmlView("<div class=\"indent\">"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMStandardOperatingProcedures#Resource_Registration_in_OIM", "Resources/Services"));
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMStandardOperatingProcedures#Support_Center_Registration_in_O", "Support Centers (SC)"));
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMStandardOperatingProcedures#Virtual_Organization_Registratio", "Virtual Organizations (VO)"));
		contentview.add(new HtmlView("</div>"));
		*/
		if(auth.isUser()) {
			contentview.addContactLegend();
		}
		
		return contentview;
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
				out.write("<h2>Content Confirmation</h2>");
			
				out.write("<p class=\"divrep_round divrep_elementerror\">You have not recently confirmed that your information in OIM is current</p>");
		
				out.write("<p>The last time you confirmed your profile information was "+crec.confirmed.toString()+"</p>");
				out.write("<p>Please go to the ");
				out.write("<a href=\"profileedit\">My Profile</a>");
				out.write(" page to check your profile information</p>");
				out.write("</div>");
			}
		}	
	}
}
