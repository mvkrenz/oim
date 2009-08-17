package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.Utils;

public class OsgGridTypeServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(OsgGridTypeServlet.class);  
	
    public OsgGridTypeServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		//setContext(request);
		auth.check("admin");

		try {
			//construct view
			MenuView menuview = new MenuView(context, "admin");
			ContentView contentview = createContentView();
			
			//setup crumbs
			BreadCrumbView bread_crumb = new BreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("OSG Grid Types",  null);
			contentview.setBreadCrumb(bread_crumb);
			
			Page page = new Page(context, menuview, contentview, createSideView());
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView() 
		throws ServletException, SQLException
	{
		OsgGridTypeModel model = new OsgGridTypeModel(context);
		Collection<OsgGridTypeRecord> ogts = model.getAll();
		
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>OSG Grid Types</h1>"));
		
		for(OsgGridTypeRecord rec : ogts) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
			
			RecordTableView table = new RecordTableView();
			contentview.add(table);
			table.addRow("Description", rec.description);
			
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
			table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), StaticConfig.getApplicationBase()+"/osggridtypeedit?osg_grid_type_id=" + rec.id)));
		}
		return contentview;
	}

	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends DivRepButton
		{
			String url;
			public NewButtonDE(DivRep parent, String _url)
			{
				super(parent, "Add New OSG Grid Type");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getPageRoot(), "osggridtypeedit"));
		
		
		
		return view;
	}
}
