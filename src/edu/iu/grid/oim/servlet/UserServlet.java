package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import javax.servlet.Servlet;
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
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.SideContentView;

public class UserServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(UserServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");
		
		//construct view
		BootMenuView menuview = new BootMenuView(context, "admin");
		ContentView contentview = createContentView(context);
		
		//setup crumbs
		BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
		bread_crumb.addCrumb("Administration",  "admin");
		bread_crumb.addCrumb("DN/AuthType Mapping",  null);
		contentview.setBreadCrumb(bread_crumb);
		
		BootPage page = new BootPage(context, menuview, contentview, null);
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView(final UserContext context) 
		throws ServletException
	{
		ContentView content = new ContentView(context);
		IView mainview = new IView() {

			@Override
			public void render(PrintWriter out) {
				out.write("<p><a class=\"btn pull-right\" href=\"useredit\">Add DN/AuthType Mapping</a></p>");	
				
				//pull list of all DNs
				DNModel model = new DNModel(context);
				DNAuthorizationTypeModel dnauthmodel = new DNAuthorizationTypeModel(context);
				AuthorizationTypeModel authmodel = new AuthorizationTypeModel(context);
			
				out.write("<table class=\"table nohover\">");
				out.write("<thead><tr><th>DN</th><th>Contact</th><th>Authorizatoin Types</th><th></th></tr></thead>");	

				out.write("<tbody>");
				try {
					for(final DNRecord rec : model.getAll()) {
						String cls = "";
						if(rec.disable){
							cls += " disabled";
						}
						out.write("<tr class=\""+cls+"\">");	
						out.write("<td>");	
						out.write(StringEscapeUtils.escapeHtml(rec.dn_string));
						if(rec.disable) {
							out.write(" <span class=\"label\">Disabled</span>");
						}
						
						out.write("</td>");	
						
						ContactModel cmodel = new ContactModel(context);
						ContactRecord crec = cmodel.get(rec.contact_id);
						String contact = crec.name + " <" + crec.primary_email + ">";
						out.write("<td>"+StringEscapeUtils.escapeHtml(contact)+"</td>");	
						
						Collection<Integer> types = dnauthmodel.getAuthorizationTypesByDNID(rec.id);
						String auth_html = "<ul>";
						for(Integer auth_type : types) {
							AuthorizationTypeRecord auth_rec = authmodel.get(auth_type);
						 	auth_html += "<li>"+StringEscapeUtils.escapeHtml(auth_rec.name) + "</li>";
						}
						auth_html += "</ul>";
						out.write("<td>"+auth_html+"</td>");	

						out.write("<td>");
						out.write("<a class=\"btn btn-mini\" href=\"useredit?id="+rec.id+"\">Edit</a>&nbsp;");
						out.write("</td>");
						
						out.write("</tr>");	
					}
				} catch (SQLException e) {
					out.write("<p class=\"alert\">"+e.getMessage()+"</p>");
				}
				out.write("</tbody>");
				out.write("</table>");	
			}
		};
		content.add(mainview);
		return content;
	}

	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		//view.add(new HtmlView("This page shows a list of DN entries with associated user and authorization type mappings.<p><br/> You can modify an existing entry to correct wrong mapping of a user or auth-type. <p><br/> You can also add a new DN for an existing contact without a DN, and map that DN to various auth-types."));		
		return view;
	}
}
