package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.IView;

public class GridAdminServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(GridAdminServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin_gridadmin");
		
		//construct view
		BootMenuView menuview = new BootMenuView(context, "certificate");;	
		BootPage page = new BootPage(context, menuview, new Content(context), null);
		page.render(response.getWriter());		
	}
	
	class Content implements IView {
		UserContext context;
		public Content(UserContext context) {
			this.context = context;
		}
		
		@Override
		public void render(PrintWriter out) {
			ContactModel cmodel = new ContactModel(context);
			GridAdminModel model = new GridAdminModel(context);
			try {
				ArrayList<GridAdminRecord> recs = model.getAll();
				Collections.sort(recs, new Comparator<GridAdminRecord> (){
					public int compare(GridAdminRecord a, GridAdminRecord b) {
						//TODO - correct algorithm is to split domain by ., then compare each token in reverse order
						String ar = reverse(a.domain);
						String br = reverse(b.domain);
						return (ar.compareTo(br));
					}
					
					public String reverse(String in) {
						int length = in.length();
						StringBuilder reverse = new StringBuilder();
						for(int i = length; i > 0; --i) {
							char result = in.charAt(i-1);
							reverse.append(result);
						}
						return reverse.toString();
					}
				});
				
				out.write("<div id=\"content\">");
				
				/*
				//setup crumbs
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				bread_crumb.addCrumb("Administration",  "admin");
				bread_crumb.addCrumb("GridAdmin Administration",  null);
				bread_crumb.render(out);
				*/
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span3\">");
				CertificateMenuView menu = new CertificateMenuView(context, "gridadmin");
				menu.render(out);
				out.write("</div>"); //span3
				
				out.write("<div class=\"span9\">");
				
				out.write("<a class=\"pull-right btn\" href=\"gridadminedit\"><i class=\"icon-plus\"></i> Add New GridAdmin</a>");
				out.write("<h2>GridAdmin Administration</h2>");
				
				out.write("<table class=\"table nohover\">");
				out.write("<thead><tr><th>Domain</th><th>GridAdmin</th><th></th></tr></thead>");	
				out.write("<tbody>");
				for(GridAdminRecord rec : recs) {
					out.write("<tr>");	
					out.write("<td>"+StringEscapeUtils.escapeHtml(rec.domain)+"</td>");		
					ContactRecord crec = cmodel.get(rec.contact_id);
					out.write("<td>"+StringEscapeUtils.escapeHtml(crec.name)+"</td>");		
					
					out.write("<td>");
					out.write("<a class=\"btn\" href=\"gridadminedit?id="+rec.id+"\">Edit</a>");
					out.write("</td>");
					
					out.write("</tr>");	
				}
				out.write("</tbody>");
				out.write("</table>");		
				
				out.write("</div>"); //span9
				out.write("</div>"); //row-fluid
				out.write("</div>"); //content
				
			} catch (SQLException e) {
				log.error("Failed to construct gridadmin list", e);
			}
		}
	}
	
	/*
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add(new HtmlView("<a class=\"btn\" href=\"gridadminedit\">Add New GridAdmin</a>"));
		return view;
	}
	*/
}
