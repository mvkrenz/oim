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

import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
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

import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.RemoveDowntimeDialog;

public class ResourceDowntimeServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceDowntimeServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		//auth.check("edit_my_resource");
		if(!auth.isUser()) {
			//context.message(MessageType.ERROR, "This page is for OIM user only. Please use MyOSG to see current downtimes.");
			throw new ServletException("This page is for OIM user only. Please use MyOSG to see current downtimes.");
		}
		
		try {			
			//construct view
			BootMenuView menuview = new BootMenuView(context, "resourcedowntime");
			ContentView contentview = createContentView(context);
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
		ResourceModel model = new ResourceModel(context);
		ArrayList<ResourceRecord> resources = model.getAllActiveNotDisabedEditable();
		Collections.sort(resources, new Comparator<ResourceRecord> () {
			public int compare(ResourceRecord a, ResourceRecord b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
		
		RemoveDowntimeDialog remove_dialog = new RemoveDowntimeDialog(context.getPageRoot(), context);
		
		ContentView contentview = new ContentView(context);	
		if(resources.size() == 0) {
			context.message(MessageType.ERROR, "You currently don't have any resources that list your contact in any of the contact types.");
		} else {

			contentview.add(new HtmlView("<table class=\"table nohover\">"));
			contentview.add(new HtmlView("<thead><tr><th>Resource Name</th><th>Downtimes</th></tr></thead>"));

			contentview.add(new HtmlView("<tbody>"));
			for(ResourceRecord rec : resources) {
				contentview.add(new HtmlView("<tr>"));
				contentview.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.name)+"</td>"));
		
				//downtimes
				contentview.add(new HtmlView("<td>"));
				contentview.add(new HtmlView("<a class=\"btn pull-right\" href=\"resourcedowntimeedit?rid=" + rec.id + "\"><i class=\"icon-plus-sign\"></i> Add New Downtime</a>"));
				GenericView downtime_view = new GenericView();
				ResourceDowntimeModel dmodel = new ResourceDowntimeModel(context);
				Collection <ResourceDowntimeRecord> dt_records = dmodel.getRecentDowntimesByResourceID(rec.id, StaticConfig.getDowntimeEditableEndDays());
				for(ResourceDowntimeRecord drec : dt_records) {
					downtime_view.add(createDowntimeView(context, remove_dialog, drec));
				}
				if (dt_records.isEmpty()) {
					contentview.add(new HtmlView("No scheduled downtime"));
				} else {
					contentview.add(new HtmlView("<br clear=\"all\">"));
					contentview.add(downtime_view);
				}
				contentview.add(new HtmlView("</td>"));
				
				contentview.add(new HtmlView("</tr>"));
			}
			contentview.add(new HtmlView("</tbody></table>"));	
		}
		

		contentview.add(new DivRepWrapper(remove_dialog));
		
		return contentview;
	}
	
	private IView createDowntimeView(UserContext context, final RemoveDowntimeDialog remove_dialog, final ResourceDowntimeRecord rec) throws SQLException
	{
		Authorization auth = context.getAuthorization();
		GenericView view = new GenericView();
		
		view.add(new HtmlView("<div class=\"well downtime_detail\">"));
		
		class RemoveButtonDE extends DivRepButton
		{
			public RemoveButtonDE(UserContext context)
			{
				super(context.getPageRoot(), "images/delete.png");
				setStyle(DivRepButton.Style.IMAGE);
				addClass("right");
			}
			protected void onEvent(DivRepEvent e) {
				remove_dialog.setRecord(rec);
				remove_dialog.open();	
			}
		};
		view.add(new DivRepWrapper(new RemoveButtonDE(context)));
		
		view.add(new HtmlView("<a class=\"btn\" href=\"resourcedowntimeedit?rid=" + rec.resource_id + "&did=" + rec.id + "\">Edit</a>"));
		
		//RecordTableView table = new RecordTableView();
		view.add(new HtmlView("<table class=\"table\">"));
		view.add(new HtmlView("<thead><tr><th>Summary</th><td>"+StringEscapeUtils.escapeHtml(rec.downtime_summary)+"</td></tr></thead>"));

		DateFormat dformat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
		String start = "";
		dformat.setTimeZone(auth.getTimeZone());
		start += dformat.format(rec.start_time) + " (" + auth.getTimeZone().getID() + ")";
		view.add(new HtmlView("<tr><th>Start Time</th><td>"+start+"</td></tr>"));
		
		String end = dformat.format(rec.end_time) + " (" + auth.getTimeZone().getID() + ")";
		view.add(new HtmlView("<tr><th>End Time</th><td>"+end+"</td></tr>"));
		
		DowntimeClassModel dtcmodel = new DowntimeClassModel(context);
		DowntimeSeverityModel dtsmodel = new DowntimeSeverityModel(context);
		view.add(new HtmlView("<tr><th>Class / Severity</th><td>"+dtcmodel.get(rec.downtime_class_id).name+" / "+dtsmodel.get(rec.downtime_severity_id).name +"</td></tr>"));
		//table.addRow("Downtime Class", dtcmodel.get(rec.downtime_class_id).name);
		
		view.add(new HtmlView("<tr><th>Affected&nbsp;Services</th><td>"+createAffectedServices(context, rec.id)+"</td></tr>"));
		//table.addRow("Affected Services", createAffectedServices(rec.id));
		
		DNModel dnmodel = new DNModel(context);
		view.add(new HtmlView("<tr><th>Entered By</th><td class=\"muted\">"+dnmodel.get(rec.dn_id).dn_string+"</td></tr>"));
		//table.addRow("DN", dnmodel.get(rec.dn_id).dn_string);
		
		view.add(new HtmlView("<tr><th>Updated At</th><td class=\"muted\">"+dformat.format(rec.timestamp)+"</td></tr>"));
		
		view.add(new HtmlView("</table>"));
	
		view.add(new HtmlView("</div>"));
		
		return view;
	}
	
	private String createAffectedServices(UserContext context, int downtime_id) throws SQLException
	{
		String html = "";
		ResourceDowntimeServiceModel model = new ResourceDowntimeServiceModel(context);
		Collection<ResourceDowntimeServiceRecord> services;

		services = model.getByDowntimeID(downtime_id);
		ServiceModel smodel = new ServiceModel(context);
		for(ResourceDowntimeServiceRecord service : services) {
			ServiceRecord rec = smodel.get(service.service_id);
			html += StringEscapeUtils.escapeHtml(rec.name) + "<br/>";
		}
		return html;
	}
			
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		view.add(new HtmlView("<p>This page allows you to schedule maintenance (downtime) for resources you are listed as a contact for, and therefore are authorized to edit.</p><p>Non-Active or Disabled Resources are filtered out.</p><p>This page only shows downtimes that end within the previous "+StaticConfig.getDowntimeEditableEndDays()+" days.</p>"));		
		return view;
	}
}
