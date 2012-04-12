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

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
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

public class AuthtypeServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(AuthtypeServlet.class);  
	
    public AuthtypeServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		auth.check("admin");
		
		try {

			//construct view
			BootMenuView menuview = new BootMenuView(context, "admin");;
			ContentView contentview = createContentView();
			
			//setup crumbs
			BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("Authorization Type",  null);
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
		AuthorizationTypeModel model = new AuthorizationTypeModel(context);
		Collection<AuthorizationTypeRecord> recs = model.getAll();
		
		ContentView contentview = new ContentView();	
		//contentview.add(new HtmlView("<h1>Authorization Type</h1>"));
	
		for(AuthorizationTypeRecord rec : recs) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
				
			RecordTableView table = new RecordTableView();
			contentview.add(table);

		 	table.addRow("Name", rec.name);
		 	/*
			class EditButtonDE extends DivRepButton
			{
				String url;
				public EditButtonDE(DivRep parent, String _url)
				{
					super(parent, "Edit");
					url = _url;
				}
				protected void onEvent(DivRepEvent e) {
					redirect(url);
				}
			};
			table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), StaticConfig.getApplicationBase()+"/authtypeedit?id=" + rec.id)));
			*/
		 	table.add(new HtmlView("<a class=\"btn\" href=\"authtypeedit?id="+rec.id+"\">Edit</a>"));
		}
		
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
				super(parent, "Add New Authorization Type");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getPageRoot(), "authtypeedit"));
		*/
		view.add(new HtmlView("<a class=\"btn\" href=\"authtypeedit\">Add New AuthorizationType</a>"));
		return view;
	}
}
