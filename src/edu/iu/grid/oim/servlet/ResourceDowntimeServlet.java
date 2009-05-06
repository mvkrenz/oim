package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;

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

import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.DowntimeClassModel;
import edu.iu.grid.oim.model.db.DowntimeSeverityModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ServiceModel;

import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeServiceModel;

import edu.iu.grid.oim.model.db.ResourceModel;

import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

import edu.iu.grid.oim.model.db.record.ResourceRecord;

import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;

public class ResourceDowntimeServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceDowntimeServlet.class);  
	
    public ResourceDowntimeServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		//setContext(request);
		auth.check("edit_my_resource");
		
		//pull list of all vos

		try {		
			//construct view
			MenuView menuview =new MenuView(context, "resourcedowntime");
			ContentView contentview = createContentView();
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
		ResourceModel model = new ResourceModel(context);
		Collection<ResourceRecord> resources = model.getAllEditable();
		
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>Resource Downtime</h1>"));
	
		for(ResourceRecord rec : resources) {
			
			//TODO - need to make "disabled" more conspicuous
			String name = rec.name;

			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
	
			RecordTableView table = new RecordTableView();
			contentview.add(table);
			
			//downtime
			GenericView downtime_view = new GenericView();
			ResourceDowntimeModel dmodel = new ResourceDowntimeModel(context);
			Collection <ResourceDowntimeRecord> dt_records = dmodel.getFutureDowntimesByResourceID(rec.id);
			for(ResourceDowntimeRecord drec : dt_records) {
				downtime_view.add(createDowntimeView(drec));
			}
			if (dt_records.isEmpty()) {
				table.addRow("No Existing Downtime Schedule", downtime_view);
			}
			else {
				table.addRow("Existing Downtime Schedule", downtime_view);
			}
		
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
			table.add(new DivExWrapper(new EditButtonDE(context.getDivExRoot(), Config.getApplicationBase()+"/resourcedowntimeedit?id=" + rec.id)));
		}
		
		return contentview;
	}
	

	private IView createDowntimeView(ResourceDowntimeRecord rec) throws SQLException
	{
		GenericView view = new GenericView();
		RecordTableView table = new RecordTableView("inner_table");
		table.addHeaderRow("Downtime");
		table.addRow("Summary", rec.downtime_summary);
		table.addRow("Start Time", rec.start_time.toString() + " UTC");
		table.addRow("End Time", rec.end_time.toString() + " UTC");
		
		DowntimeClassModel dtcmodel = new DowntimeClassModel(context);
		table.addRow("Downtime Class", dtcmodel.get(rec.downtime_class_id).name);
		
		DowntimeSeverityModel dtsmodel = new DowntimeSeverityModel(context);
		table.addRow("Downtime Severity", dtsmodel.get(rec.downtime_severity_id).name);
		
		table.addRow("Affected Services", createAffectedServices(rec.id));
		
		DNModel dnmodel = new DNModel(context);
		table.addRow("DN", dnmodel.get(rec.dn_id).dn_string);
		
		table.addRow("Disable", rec.disable);		

		view.add(table);
		
		return view;
	}
	
	private IView createAffectedServices(int downtime_id) throws SQLException
	{
		String html = "";
		ResourceDowntimeServiceModel model = new ResourceDowntimeServiceModel(context);
		Collection<ResourceDowntimeServiceRecord> services;

		services = model.getByDowntimeID(downtime_id);
		ServiceModel smodel = new ServiceModel(context);
		for(ResourceDowntimeServiceRecord service : services) {
			ServiceRecord rec = smodel.get(service.service_id);
			html += rec.name + "<br/>";
		}
		return new HtmlView(html);

	}
			
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		view.add("About", new HtmlView("TODO..."));		
		return view;
	}
}
