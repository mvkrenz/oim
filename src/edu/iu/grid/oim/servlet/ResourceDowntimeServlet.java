package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.webif.divrep.common.Button;
import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepRoot;
import com.webif.divrep.Event;


import edu.iu.grid.oim.lib.StaticConfig;

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
import edu.iu.grid.oim.model.db.record.SiteRecord;

import edu.iu.grid.oim.model.db.record.ResourceRecord;

import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
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
		ArrayList<ResourceRecord> resources = model.getAllEditable();
		Collections.sort(resources, new Comparator<ResourceRecord> () {
			public int compare(ResourceRecord a, ResourceRecord b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});

		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>Resource Downtime</h1>"));
	
		if(resources.size() == 0) {
			contentview.add(new HtmlView("<p>You currently don't have any resources that list your contact in any of the contact types.</p>"));
		}
		
		for(ResourceRecord rec : resources) {
			
			String name = rec.name;

			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
	
			//RecordTableView table = new RecordTableView();
			
			//downtime
			GenericView downtime_view = new GenericView();
			ResourceDowntimeModel dmodel = new ResourceDowntimeModel(context);
			Collection <ResourceDowntimeRecord> dt_records = dmodel.getFutureDowntimesByResourceID(rec.id);
			for(ResourceDowntimeRecord drec : dt_records) {
				downtime_view.add(createDowntimeView(drec));
			}
			if (dt_records.isEmpty()) {
				downtime_view.add(new HtmlView("No scheduled downtime"));
			}
			contentview.add(downtime_view);
		
			class EditButtonDE extends Button
			{
				String url;
				public EditButtonDE(DivRep parent, String _url)
				{
					super(parent, "Add/Edit Downtime");
					url = _url;
				}
				protected void onEvent(Event e) {
					redirect(url);
				}
			};
			contentview.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), StaticConfig.getApplicationBase()+"/resourcedowntimeedit?id=" + rec.id)));
			//contentview.add(table);
		}
		
		return contentview;
	}
	
	private IView createDowntimeView(ResourceDowntimeRecord rec) throws SQLException
	{
		GenericView view = new GenericView();
		
		view.add(new HtmlView("<div class=\"downtime\">"));
		
		RecordTableView table = new RecordTableView();
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
		
		view.add(table);
		
		view.add(new HtmlView("</div>"));
		
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
		
		view.add("About", new HtmlView("This page allows you to schedule maintenance (downtime) for resources you are listed as a contact for, and therefore are authorized to edit."));		
		return view;
	}
}
