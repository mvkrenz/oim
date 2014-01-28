package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
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

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.MetricModel;
import edu.iu.grid.oim.model.db.MetricServiceModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.model.db.ServiceGroupModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.record.MetricServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");
		
		try {	
			//construct view
			BootMenuView menuview = new BootMenuView(context, "admin");
			ContentView contentview = createContentView(context);
			
			//setup crumbs
			BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("Service",  null);
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
		ContentView contentview = new ContentView(context);	
		//contentview.add(new HtmlView("<h1>Service</h1>"));

		ServiceModel model = new ServiceModel(context);
		ServiceGroupModel sgmodel = new ServiceGroupModel(context);
		
		for(ServiceRecord rec : model.getAll()) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
	
			RecordTableView table = new RecordTableView();
			contentview.add(table);
			table.addRow("Name", rec.name);
			table.addRow("Description", rec.description);
			//table.addRow("Service Type", rec.type);
			if (rec.port != null) {
				table.addRow("Port", rec.port.toString());	
			}
			table.addRow("Service Group", sgmodel.get(rec.service_group_id).name);
			// table.addRow("Service Group", rec.name);
			
			//Metric stuff
			GenericView metric_view = new GenericView();
			MetricServiceModel dmodel = new MetricServiceModel(context);
			for(MetricServiceRecord drec : dmodel.getAllByServiceID(rec.id)) {
				metric_view.add(createMetricView(context, drec));
			}
			table.addRow("Metrics", metric_view);
		
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
			table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), StaticConfig.getApplicationBase()+"/serviceedit?id=" + rec.id)));
			*/
			table.add(new HtmlView("<a class=\"btn\" href=\"serviceedit?id=" + rec.id +"\">Edit</a>"));
		}
		
		return contentview;
	}

	private IView createMetricView(UserContext context, MetricServiceRecord rec) 
	{
		GenericView view = new GenericView();
		RecordTableView table = new RecordTableView("inner_table");
		MetricModel metric_model = new MetricModel(context);
		// Probably need to be more careful looking for null stuff
		try {
			if (rec.critical) 
				table.addBoldRow(rec.metric_id.toString()+ " " + metric_model.get(rec.metric_id).name +" (Critical)");
			else
				table.addRow(rec.metric_id.toString()+ " " + metric_model.get(rec.metric_id).name);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		view.add(table);
		
		return view;
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
				super(parent, "Add New Service");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getPageRoot(), "serviceedit"));
		*/
		view.add(new HtmlView("<a class=\"btn\" href=\"serviceedit\">Add New Service</a>"));		

		//view.add("About", new HtmlView("Todo.."));		
		return view;
	}
}
