package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepStaticContent;

import edu.iu.grid.oim.lib.StaticConfig;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.DowntimeClassModel;
import edu.iu.grid.oim.model.db.DowntimeSeverityModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeServiceModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
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
import edu.iu.grid.oim.view.divrep.RemoveDowntimeDialog;

public class ResourceDowntimeServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceDowntimeServlet.class);  
	
	RemoveDowntimeDialog remove_downtime_dialog;
	
    public ResourceDowntimeServlet() {
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		auth.check("edit_my_resource");
		
		remove_downtime_dialog = new RemoveDowntimeDialog(context.getPageRoot(), context);
		
		try {			
			//construct view
			MenuView menuview =new MenuView(context, "resourcedowntime");
			ContentView contentview = createContentView();
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
		ResourceModel model = new ResourceModel(context);
		ArrayList<ResourceRecord> resources = model.getAllActiveNotDisabedEditable();
		Collections.sort(resources, new Comparator<ResourceRecord> () {
			public int compare(ResourceRecord a, ResourceRecord b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});

		ContentView contentview = new ContentView();	
		//contentview.add(new HtmlView("<h1>Resource Downtime</h1>"));
	
		if(resources.size() == 0) {
			contentview.add(new HtmlView("<p>You currently don't have any resources that list your contact in any of the contact types.</p>"));
		}
		
		for(ResourceRecord rec : resources) {

			String name = rec.name;

			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
	
			//downtime
			GenericView downtime_view = new GenericView();
			ResourceDowntimeModel dmodel = new ResourceDowntimeModel(context);
			Collection <ResourceDowntimeRecord> dt_records = dmodel.getRecentDowntimesByResourceID(rec.id, StaticConfig.getDowntimeEditableEndDays());
			for(ResourceDowntimeRecord drec : dt_records) {
				downtime_view.add(createDowntimeView(drec));
			}
			if (dt_records.isEmpty()) {
				downtime_view.add(new HtmlView("No scheduled downtime"));
			}
			contentview.add(downtime_view);
		
			class NewButtonDE extends DivRepButton
			{
				String url;
				public NewButtonDE(DivRep parent, String _url)
				{
					super(parent, "Add New Downtime");
					url = _url;
				}
				protected void onEvent(DivRepEvent e) {
					redirect(url);
				}
			};
			contentview.add(new DivRepWrapper(new NewButtonDE(context.getPageRoot(), StaticConfig.getApplicationBase()+"/resourcedowntimeedit?rid=" + rec.id)));
		}
		
		contentview.add(new DivRepWrapper(remove_downtime_dialog));
		
		return contentview;
	}
	
	private IView createDowntimeView(final ResourceDowntimeRecord rec) throws SQLException
	{
		GenericView view = new GenericView();
		
		view.add(new HtmlView("<div class=\"downtime\">"));
		
		class RemoveButtonDE extends DivRepButton
		{
			public RemoveButtonDE(DivRep parent)
			{
				super(parent, "images/delete.png");
				setStyle(DivRepButton.Style.IMAGE);
				addClass("right");
			}
			protected void onEvent(DivRepEvent e) {
				//remove_downtime_dialog.summary.setHtml("<br/><b>Summary</b><br/><i>"+rec.downtime_summary + "</i>");
				//remove_downtime_dialog.summary.redraw();
				remove_downtime_dialog.setRecord(rec);
				remove_downtime_dialog.open();	
			}
		};
		view.add(new DivRepWrapper(new RemoveButtonDE(context.getPageRoot())));
		
		RecordTableView table = new RecordTableView();
		table.addRow("Summary", rec.downtime_summary);

		DateFormat dformat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
		String start = "";
		dformat.setTimeZone(getTimeZone());
		start += dformat.format(rec.start_time) + " (" + getTimeZone().getID() + ")";
		table.addRow("Start Time", new HtmlView(start));
		
		table.addRow("End Time", dformat.format(rec.end_time) + " (" + getTimeZone().getID() + ")");
		
		DowntimeClassModel dtcmodel = new DowntimeClassModel(context);
		table.addRow("Downtime Class", dtcmodel.get(rec.downtime_class_id).name);
		
		DowntimeSeverityModel dtsmodel = new DowntimeSeverityModel(context);
		table.addRow("Downtime Severity", dtsmodel.get(rec.downtime_severity_id).name);
		
		table.addRow("Affected Services", createAffectedServices(rec.id));
		
		DNModel dnmodel = new DNModel(context);
		table.addRow("DN", dnmodel.get(rec.dn_id).dn_string);
		
		view.add(table);
		
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
		table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), StaticConfig.getApplicationBase()+"/resourcedowntimeedit?rid=" + rec.resource_id + "&did=" + rec.id)));

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
		
		view.add("About", new HtmlView("<p>This page allows you to schedule maintenance (downtime) for resources you are listed as a contact for, and therefore are authorized to edit.</p><p>Non-Active or Disabled Resources are filtered out.</p><p>This page only shows downtimes that end within the previous "+StaticConfig.getDowntimeEditableEndDays()+" days.</p>"));		
		return view;
	}
}
