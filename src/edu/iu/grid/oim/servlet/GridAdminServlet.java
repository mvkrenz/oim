package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.Servlet;
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
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.SideContentView;

public class GridAdminServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(GridAdminServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");
		
		try {

			//construct view
			BootMenuView menuview = new BootMenuView(context, "admin");;
			ContentView contentview = createContentView(context);
			
			//setup crumbs
			BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("GridAdmin Administration",  null);
			contentview.setBreadCrumb(bread_crumb);
			
			BootPage page = new BootPage(context, menuview, contentview, createSideView());
			page.render(response.getWriter());				
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(UserContext context) 
		throws ServletException, SQLException
	{
		ContactModel cmodel = new ContactModel(context);
		GridAdminModel model = new GridAdminModel(context);
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
		
		ContentView contentview = new ContentView();			
		contentview.add(new HtmlView("<table class=\"table nohover\">"));
		contentview.add(new HtmlView("<thead><tr><th>Domain</th><th>GridAdmin</th><th></th></tr></thead>"));	

		contentview.add(new HtmlView("<tbody>"));
		for(GridAdminRecord rec : recs) {
			contentview.add(new HtmlView("<tr>"));	
			contentview.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.domain)+"</td>"));		
			ContactRecord crec = cmodel.get(rec.contact_id);
			contentview.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(crec.name)+"</td>"));		
			
			contentview.add(new HtmlView("<td>"));
			contentview.add(new HtmlView("<a class=\"btn\" href=\"gridadminedit?id="+rec.id+"\">Edit</a>"));
			contentview.add(new HtmlView("</td>"));
			
			contentview.add(new HtmlView("</tr>"));	
		}
		contentview.add(new HtmlView("</tbody>"));
		contentview.add(new HtmlView("</table>"));				
		
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add(new HtmlView("<a class=\"btn\" href=\"gridadminedit\">Add New GridAdmin</a>"));
		return view;
	}
}
