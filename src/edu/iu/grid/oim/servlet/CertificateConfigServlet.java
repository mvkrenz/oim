package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.SQLException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.IView;

import edu.iu.grid.oim.view.divrep.form.CertificateConfigFormDE;

public class CertificateConfigServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(CertificateConfigServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin_pki_config");
		BootMenuView menuview = new BootMenuView(context, "certificate");;
		BootPage page = new BootPage(context, menuview, new Content(context), null);
		page.render(response.getWriter());				
	}
	
	class Content implements IView {
		UserContext context;
		Content(UserContext context) {
			this.context = context;
		}
		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\"content\">");

			out.write("<div class=\"row-fluid\">");
			
			out.write("<div class=\"span3\">");
			CertificateMenuView menu = new CertificateMenuView(context, "certificateconfig");
			menu.render(out);
			out.write("</div>"); //span3
			
			out.write("<div class=\"span9\">");
			try {
				CertificateConfigFormDE form = new CertificateConfigFormDE(context);
				form.render(out);
			} catch (SQLException e) {
				log.error("SQLError file rendering quota config form", e);
			}
			out.write("</div>"); //span9
			out.write("</div>"); //row-fluid
			out.write("</div>"); //content
		}
	}
}
