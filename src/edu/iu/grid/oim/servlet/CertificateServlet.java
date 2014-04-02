package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.EditableContent;

public class CertificateServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(CertificateServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		BootMenuView menuview = new BootMenuView(context, "certificate");
		BootPage page = new BootPage(context, menuview, createContent(context), null);
		page.render(response.getWriter());		
	}
	
	protected IView createContent(final UserContext context) throws ServletException {
		
		return new IView(){
			@Override
			public void render(PrintWriter out) {
				
				out.write("<div id=\"content\">");
				
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span3\">");
 				CertificateMenuView menu = new CertificateMenuView(context, "home");
				menu.render(out);
				out.write("</div>"); //span3
				
				//main content
				out.write("<div class=\"span9\">");
				ConfigModel config = new ConfigModel(context);
				Config home_content = config.new Config(config, "certificate_home_content", "Edit me");
				Authorization auth = context.getAuthorization();
				if(auth.allows("admin") || auth.allows("admin_ra")) {
					EditableContent content = new EditableContent(context.getPageRoot(), context, home_content);
					content.render(out);
				} else {
					out.write(home_content.getString());
				}
				out.write("</div>"); //span9
				
				out.write("</div>"); //row-fluid
				
				out.write("</div>"); //content
			}
		};
	}
}
