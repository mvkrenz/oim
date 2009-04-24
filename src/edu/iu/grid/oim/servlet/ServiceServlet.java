package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;

import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.model.db.MetricModel;
import edu.iu.grid.oim.model.db.MetricServiceModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.record.MetricServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;

public class ServiceServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ServiceServlet.class);  
	
    public ServiceServlet() {
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
			bread_crumb.addCrumb("Service",  null);
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
		contentview.add(new HtmlView("<h1>Service</h1>"));

		ServiceModel model = new ServiceModel(context);
		
		for(ServiceRecord rec : model.getAll()) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
	
			RecordTableView table = new RecordTableView();
			contentview.add(table);
			table.addRow("Name", rec.name);
			table.addRow("Description", rec.description);
			table.addRow("Port", rec.port.toString());
			table.addRow("Service Group ID", rec.service_group_id.toString());
			
			//downtime
			GenericView metric_view = new GenericView();
			MetricServiceModel dmodel = new MetricServiceModel(context);
			for(MetricServiceRecord drec : dmodel.getAllByServiceID(rec.id)) {
				metric_view.add(createMetricView(drec));
			}
			table.addRow("Metrics", metric_view);
		
		 	
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
			table.add(new DivExWrapper(new EditButtonDE(context.getDivExRoot(), Config.getApplicationBase()+"/serviceedit?id=" + rec.id)));
		}
		
		return contentview;
	}

	private IView createMetricView(MetricServiceRecord rec) 
	{
		GenericView view = new GenericView();
		RecordTableView table = new RecordTableView("inner_table");
		table.addHeaderRow(rec.metric_id.toString());
		table.addRow("Critical", rec.critical);
	
		view.add(table);
		
		return view;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("About", new HtmlView("Todo.."));		
		return view;
	}
}
