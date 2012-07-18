package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.ServiceGroupModel;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.ServiceGroupRecord;

public class FieldOfScienceServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(FieldOfScienceServlet.class);  

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
			bread_crumb.addCrumb("Field of Science",  null);
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
		FieldOfScienceModel model = new FieldOfScienceModel(context);
		Collection<FieldOfScienceRecord> recs = model.getAll();
		
		ContentView contentview = new ContentView(context);	
		//contentview.add(new HtmlView("<h1>Field Of Science VOs can be associated with</h1>"));
	
		/*
		for(FieldOfScienceRecord rec : recs) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
				
			RecordTableView table = new RecordTableView();
			contentview.add(table);

		 	table.addRow("Name", rec.name);
		 	table.add(new HtmlView("<a class=\"btn\" href=\"fieldofscienceedit?id="+rec.id+"\">Edit</a>"));
		}*/
		
		contentview.add(new HtmlView("<table class=\"table nohover\">"));
		contentview.add(new HtmlView("<thead><tr><th>Name</th><th></th></tr></thead>"));	

		contentview.add(new HtmlView("<tbody>"));
		for(FieldOfScienceRecord rec : recs) {
			contentview.add(new HtmlView("<tr>"));	
			contentview.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.name)+"</td>"));		
			contentview.add(new HtmlView("<td>"));
			contentview.add(new HtmlView("<a class=\"btn\" href=\"fieldofscienceedit?id="+rec.id+"\">Edit</a>"));
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
		/*
		class NewButtonDE extends DivRepButton
		{
			String url;
			public NewButtonDE(DivRep parent, String _url)
			{
				super(parent, "Add New Field Of Science");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getPageRoot(), "fieldofscienceedit"));
		*/
		view.add(new HtmlView("<a class=\"btn\" href=\"fieldofscienceedit\">Add New Field Of Science</a>"));
	 	
		return view;
	}
}
