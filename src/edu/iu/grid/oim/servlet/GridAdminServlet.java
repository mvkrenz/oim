package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepTextArea;

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
import edu.iu.grid.oim.view.divrep.BootDialogForm;
import edu.iu.grid.oim.view.divrep.form.GridAdminRequestForm;

public class GridAdminServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(GridAdminServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		
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
			Authorization auth = context.getAuthorization();
			ContactModel cmodel = new ContactModel(context);
			GridAdminModel model = new GridAdminModel(context);
			try {
				LinkedHashMap<String, ArrayList<ContactRecord>> recs = model.getAll();
				/*
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
				*/
				
				out.write("<div id=\"content\">");
			
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span3\">");
				CertificateMenuView menu = new CertificateMenuView(context, "gridadmin");
				menu.render(out);
				out.write("</div>"); //span3
				
				out.write("<div class=\"span9\">");
				
				if(auth.allows("admin_gridadmin")) {
					out.write("<a class=\"pull-right btn\" href=\"gridadminedit\"><i class=\"icon-plus\"></i> Add New Domain</a>");
				} else if(auth.isUser()) {
					final GridAdminRequestForm form = new GridAdminRequestForm(context);
					form.render(out);
					
					DivRepButton request = new DivRepButton(context.getPageRoot(), "Request for GridAdmin Enrollment");
					request.addClass("btn");
					request.addClass("pull-right");
					request.render(out);
					request.addEventListener(new DivRepEventListener() {
						@Override
						public void handleEvent(DivRepEvent e) {
							form.show();
						}});
				}
				out.write("<h2>GridAdmins</h2>");
				
				if(auth.allows("admin_gridadmin")) {
					out.write("<table class=\"table\">");
				} else {
					out.write("<table class=\"table nohover\">");	
				}
				out.write("<thead><tr><th>Domain</th><th>GridAdmins</th><th></th></tr></thead>");	
				out.write("<tbody>");
				for(String domain : recs.keySet()) {
					out.write("<tr>");
					out.write("<td>"+StringEscapeUtils.escapeHtml(domain)+"</td>");
					
					out.write("<td><ul>");
					for(ContactRecord ga : recs.get(domain)) {
						out.write("<li>"+StringEscapeUtils.escapeHtml(ga.name)+"</li>");							
					}
					out.write("</ul></td>");
					
					out.write("<td>");
					if(auth.allows("admin_gridadmin")) {
						out.write("<a href=\"gridadminedit?domain="+domain+"\" class=\"btn btn-mini\">Edit</a>");
					}
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
