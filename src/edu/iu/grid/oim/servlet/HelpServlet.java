package edu.iu.grid.oim.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.ExternalLinkView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LinkView;
import edu.iu.grid.oim.view.ListView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

public class HelpServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(HelpServlet.class);  
    
    public HelpServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		//setContext(request);
		
		MenuView menuview = new MenuView(context, "help");
		ContentView contentview;
		contentview = createContentView();
		
		Page page = new Page(context, menuview, contentview, new SideContentView());
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView()
	{
		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<h1>Help</h1>"));

		contentview.add(new HtmlView("<h3>Definitions</h3>"));
		contentview.add(new HtmlView("<p>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMTermDefinition", "Basic OIM Definitions"));
		contentview.add(new HtmlView("</p>"));

		contentview.add(new HtmlView("<h3>New Registration Help</h3>"));
		contentview.add(new HtmlView("<p>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMRegistrationInstructions#Resource_or_Service_Registration", "Resource/Service Registration"));
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMRegistrationInstructions#Support_Center_Registration", "Support Center (SC) Registration"));
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMRegistrationInstructions#VO_Registration", "Virtual Organization (VO) Registration"));
		contentview.add(new HtmlView("</p>"));
			
		contentview.add(new HtmlView("<h3>Resource/Service Maintenance</h3>"));
		contentview.add(new HtmlView("<p>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMMaintTool#Adding_a_New_Maintenance_Window", "Schedule Resource Maintenance"));
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMMaintTool#Modifying_a_Maintenance_Window", "Modify Existing Maintenance"));
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMMaintTool#Deleting_a_Maintenance_Window", "Cancel Existing Maintenance"));
		contentview.add(new HtmlView("</p>"));
		
		
		contentview.add(new HtmlView("<h3>Standard Operating Procedures</h3>"));
		contentview.add(new HtmlView("<p>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMStandardOperatingProcedures#Resource_Registration_in_OIM", "Resources/Services"));
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMStandardOperatingProcedures#Support_Center_Registration_in_O", "Support Centers (SC)"));
			contentview.add(new HtmlView("<br/>"));
			contentview.add(new ExternalLinkView("https://twiki.grid.iu.edu/twiki/bin/view/Operations/OIMStandardOperatingProcedures#Virtual_Organization_Registratio", "Virtual Organizations (VO)"));
		contentview.add(new HtmlView("</p>"));
		
		
		return contentview;
	}
}
