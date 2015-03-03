package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.IView;
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
			//VOModel vomodel = new VOModel(context);
			GridAdminModel model = new GridAdminModel(context);
			try {
				LinkedHashMap<String, ArrayList<GridAdminRecord>> recs = model.getAll();
				
				out.write("<div id=\"content\">");
				out.write("<div class=\"row-fluid\">");
				out.write("<div class=\"span3\">");
				CertificateMenuView menu = new CertificateMenuView(context, "gridadmin");
				menu.render(out);
				out.write("</div>"); //span3
				
				out.write("<div class=\"span9\">");
			
				if(auth.isUser()) {
					final GridAdminRequestForm form = new GridAdminRequestForm(context);
					form.render(out);
					
					DivRepButton request = new DivRepButton(context.getPageRoot(), "Request for GridAdmin Enrollment ...");
					request.addClass("btn");
					request.addClass("pull-right");
					if(auth.allows("admin_gridadmin")) {
						request.setDisabled(true);
						request.setToolTip("You have admin_gridadmin privilege. You don't need to request for enrollment.");
					}
					request.render(out);
					request.addEventListener(new DivRepEventListener() {
						@Override
						public void handleEvent(DivRepEvent e) {
							form.show();
						}
					});
				}
				if(auth.allows("admin_gridadmin")) {
					out.write("<a class=\"btn pull-right\" style=\"margin-right: 5px\" href=\"gridadminedit\"><i class=\"icon-plus\"></i> Add New Domain</a>");
				} 
				
				out.write("<h2>GridAdmins</h2>");
				
				if(auth.allows("admin_gridadmin")) {
					out.write("<table class=\"table\">");
				} else {
					out.write("<table class=\"table nohover\">");	
				}
				out.write("<thead><tr><th></th><th>Domain</th><th>VO</th><th>CA</th><th>GridAdmins</th><th></th></tr></thead>");	
				out.write("<tbody>");
				
				for(String domain : recs.keySet()) {
					//for each domain
					boolean first_domain = true;
					//ArrayList<GridAdminRecord> allrecs_in_domain = recs.get(domain);
					HashMap<VORecord, ArrayList<GridAdminRecord>> groups = model.getByDomainGroupedByVO(domain);
					for(VORecord vo : groups.keySet()) {
						ArrayList<GridAdminRecord> gas = groups.get(vo);

						out.write("<tr>");
						
						//domain name
						if(first_domain) {
							first_domain = false;
							out.write("<td rowspan=\""+groups.size()+"\">");
							if(auth.allows("admin_gridadmin")) {
								out.write("<a href=\"gridadminedit?domain="+domain+"\" class=\"pull-left btn btn-mini\">Edit</a>");
							}
							out.write("</td>");
							out.write("<td rowspan=\""+groups.size()+"\">"+StringEscapeUtils.escapeHtml(domain) + "</td>");
						}
						out.write("<td>" + vo.name + "</td>");
						out.write("<td>" + vo.certificate_signer + "</td>");
						//finally, the contact itself
						out.write("<td>");
						out.write("<ul>");
						for(GridAdminRecord ga : gas) {
							ContactRecord gac = cmodel.get(ga.contact_id);
							out.write("<li>");
							out.write(StringEscapeUtils.escapeHtml(gac.name));
							if(auth.isUser()) {
								out.print(" <code>"+StringEscapeUtils.escapeHtml("<"+gac.primary_email+">")+"</code>");
							}
							out.write("</li>");
						}
						out.write("</ul>");
						out.write("</td>");
						
						out.write("</tr>");
					}
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
}
