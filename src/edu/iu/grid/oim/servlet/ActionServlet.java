package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.CpuInfoModel;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.model.db.record.CpuInfoRecord;

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

public class ActionServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ActionServlet.class);  
	
    public ActionServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		auth.check("admin");
		
		try {
			//construct view
			BootMenuView menuview = new BootMenuView(context, "admin");
			ContentView contentview = createContentView();
			
			//setup crumbs
			BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("Action",  null);
			contentview.setBreadCrumb(bread_crumb);
			
			BootPage page = new BootPage(context, menuview, contentview, createSideView());
			page.render(response.getWriter());				
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView() 
		throws ServletException, SQLException
	{
		ActionModel model = new ActionModel(context);
		Collection<ActionRecord> recs = model.getAll();
		
		ContentView contentview = new ContentView();	
		//contentview.add(new HtmlView("<h1>Action</h1>"));
	
		/*
		for(ActionRecord rec : recs) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
		 	table.addRow("Name", rec.name);
			table.addRow("Description", rec.description);
			table.add(new HtmlView("<a class=\"btn\" href=\"actionedit?id=" + rec.id + "\">Edit</a>"));
		}
		*/
		
		contentview.add(new HtmlView("<table class=\"table nohover\">"));
		contentview.add(new HtmlView("<thead><tr><th>Name</th><th>Description</th><th></th></tr></thead>"));	

		contentview.add(new HtmlView("<tbody>"));
		for(ActionRecord rec : recs) {
			contentview.add(new HtmlView("<tr>"));	
			contentview.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.name)+"</td>"));	
			contentview.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.description)+"</td>"));	
			
			contentview.add(new HtmlView("<td>"));
			contentview.add(new HtmlView("<a class=\"btn\" href=\"actionedit?id="+rec.id+"\">Edit</a>"));
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
				super(parent, "Add New Action record");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getPageRoot(), "actionedit"));
		*/
		view.add("<a class=\"btn\" href=\"actionedit\">Add New Action</a>");
		return view;
	}
}
