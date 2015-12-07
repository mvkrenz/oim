package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.UserCertificateTable;
import edu.iu.grid.oim.view.divrep.form.CertificateSearchUserForm;

public class CertificateSearchUserServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(CertificateSearchUserServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
				
		//do search if requested
		CertificateSearchUserForm form = new CertificateSearchUserForm(request, context);
		BootMenuView menuview = new BootMenuView(context, "certificate");
		BootPage page = new BootPage(context, menuview, createContent(request, context, form), null);
		page.render(response.getWriter());
	}
	
	protected IView createContent(
				final HttpServletRequest request, 
				final UserContext context, 
				final CertificateSearchUserForm form) throws ServletException
	{
		return new IView(){
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\"content\">");
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span3\">");
 				CertificateMenuView menu = new CertificateMenuView(context, "certificatesearchuser");
				menu.render(out);
				out.write("</div>"); //span3
				
				out.write("<div class=\"span9\">");
				ArrayList<CertificateRequestUserRecord> results = form.search(); //run search before rendering form so that any messages will be displayed
				form.render(out);
				renderResult(out, context, results);
				out.write("</div>"); //span9
				
				out.write("</div>"); //row-fluid
				out.write("</div>"); //content
			}
		};
	}
	
	protected void renderResult(PrintWriter out, UserContext context, ArrayList<CertificateRequestUserRecord> results) {
		if(!results.isEmpty()) {
			out.write("<h2>Results</h2>");
			UserCertificateTable table = new UserCertificateTable(context, results, true);
			table.render(out);
		}
	}
}
