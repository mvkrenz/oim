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
import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.model.db.ServiceGroupModel;
import edu.iu.grid.oim.model.db.record.ServiceGroupRecord;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;

public class ServiceGroupServlet extends ServletBase implements Servlet 
{
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ServiceGroupServlet.class);  
	
    public ServiceGroupServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		setContext(request);
		auth.check("admin");
		
		try {
			//construct view
			MenuView menuview = createMenuView("admin");
			ContentView contentview = createContentView();

			//setup crumbs
			BreadCrumbView bread_crumb = new BreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("Service Group",  null);
			contentview.setBreadCrumb(bread_crumb);
			
			Page page = new Page(menuview, contentview, createSideView());
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView() 
		throws ServletException, SQLException
	{
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>Resource Groups</h1>"));
	
		ServiceGroupModel model = new ServiceGroupModel(context);
		Collection<ServiceGroupRecord> rgs = model.getAll();
		for(ServiceGroupRecord rec : rgs) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));

			
			RecordTableView table = new RecordTableView();
			contentview.add(table);

			table.addRow("Name", rec.name);
			table.addRow("Description", rec.description);
			
			class EditButtonDE extends ButtonDE
			{
				String url;
				public EditButtonDE(DivEx parent, String _url)
				{
					super(parent, "Edit");
					url = _url;
				}
				protected void onEvent(Event e) {
					redirect(url);
				}
			};
			table.add(new DivExWrapper(new EditButtonDE(context.getDivExRoot(), Config.getApplicationBase()+"/servicegroupedit?id=" + rec.id)));
		}
		
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends ButtonDE
		{
			String url;
			public NewButtonDE(DivEx parent, String _url)
			{
				super(parent, "Add New Service Group");
				url = _url;
			}
			protected void onEvent(Event e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getDivExRoot(), "servicegroupedit"));
		view.add("About", new HtmlView("This page shows a list of Service Groups that you have access to edit."));		
		return view;
	}
}