package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRepPage;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;


public class FPTemplatesServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(FPTemplatesServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");
	
		try {			
			//construct view
			BootMenuView menuview = new BootMenuView(context, "admin");
			ContentView contentview = createContentView(context);
		
			PrintWriter out = response.getWriter();
			if(request.getParameter("plain") != null) {
				contentview.render(out);
			} else {
				//set crumbs
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				bread_crumb.addCrumb("Administration",  "admin");
				bread_crumb.addCrumb("Footprints Ticket Templates",  null);
				contentview.setBreadCrumb(bread_crumb);
				
				BootPage page = new BootPage(context, menuview, contentview, null);
				page.render(out);			
			}
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(UserContext context) 
		throws ServletException, SQLException
	{	
		ContentView contentview = new ContentView(context);	
		//contentview.add(new HtmlView("<h1>Footprints Ticket Templates</h1>"));
		contentview.add(new HtmlView("<p>Following templates will be used to generate Footprints tickets after each Resource, VO, and SC registrartion.</p>"));
		
		DivRepPage page = context.getPageRoot();
		FPTemplatesForm form = new FPTemplatesForm(page, context);
		contentview.add(form);
		
		return contentview;
	}
	
	class FPTemplatesForm extends DivRepForm {
	
		private DivRepTextArea resource_template;	
		private DivRepTextArea vo_template;
		private DivRepTextArea sc_template;
		private UserContext context;

		public FPTemplatesForm(DivRepPage page, UserContext context) throws AuthorizationException, SQLException
		{	
			super(page, "admin");
			this.context = context;

			ConfigModel config = new ConfigModel(context);
			//ContactModel contactmodel = new ContactModel(context);
			
			new DivRepStaticContent(this, "<h2>Resource Registration</h2>");
			new DivRepStaticContent(this, "<p><b>##RESOURCE_NAME##</b> will be replaced by the actual resource name registered.</p>");
			resource_template = new DivRepTextArea(this);
			resource_template.setValue(config.ResourceFPTemplate.getString());
			resource_template.addClass("fptemplate");
			resource_template.setHeight(200);
			
			
			new DivRepStaticContent(this, "<h2>VO Registration</h2>");
			new DivRepStaticContent(this, "<p><b>##VO_NAME##</b> will be replaced by the actual VO name registered.</p>");
			vo_template = new DivRepTextArea(this);
			vo_template.setValue(config.VOFPTemplate.getString());
			vo_template.addClass("fptemplate");
			vo_template.setHeight(200);

			
			new DivRepStaticContent(this, "<h2>SC Registration</h2>");
			new DivRepStaticContent(this, "<p><b>##SC_NAME##</b> will be replaced by the actual SC name registered.</p>");

			sc_template = new DivRepTextArea(this);
			sc_template.setValue(config.SCFPTemplate.getString());
			sc_template.addClass("fptemplate");
			sc_template.setHeight(200);
	
		}
		protected Boolean doSubmit() {
			ConfigModel config = new ConfigModel(context);
			try {
				config.ResourceFPTemplate.set(resource_template.getValue());
				config.VOFPTemplate.set(vo_template.getValue());
				config.SCFPTemplate.set(sc_template.getValue());
			} catch (SQLException e) {
				log.error("Failed to update config", e);
				this.alert("Sorry, failed to update config");
				return false;
			}
			context.message(MessageType.SUCCESS, "Successfully updated Footprints template.");
			return true;
		}
		
	}
}
