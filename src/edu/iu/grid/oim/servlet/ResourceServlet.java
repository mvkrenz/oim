package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
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

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.TextFormElementDE;

import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.DowntimeClassModel;
import edu.iu.grid.oim.model.db.DowntimeSeverityModel;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.ResourceAliasModel;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeServiceModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ResourceWLCGModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.VOFieldOfScienceModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.VOResourceOwnershipModel;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOResourceOwnershipRecord;
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

public class ResourceServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceServlet.class);  
	
    public ResourceServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		setContext(request);
		auth.check("edit_my_resource");
		
		try {
			//construct view
			MenuView menuview = createMenuView("resource");
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
		contentview.add(new HtmlView("<h1>Resource</h1>"));
	
		for(ResourceRecord rec : resources) {
			
			//TODO - need to make "disabled" more conspicuous
			String name = rec.name;
			/*
			if(rec.disable) {
				name += " (Disabled)";
			}
			*/
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
	
			RecordTableView table = new RecordTableView();
			contentview.add(table);

			table.addRow("Resource FQDN", rec.fqdn);

			//pull resource group
			// Can we show hierarchy here? -agopu
			ResourceGroupModel gmodel = new ResourceGroupModel(context);
			ResourceGroupRecord resource_group_rec = gmodel.get(rec.resource_group_id);
			String resource_group_name = null;
			if(resource_group_rec != null) {
				resource_group_name = resource_group_rec.name;
			}
			table.addRow("Resource Group Name", resource_group_name);

			table.addRow("Resource Description", rec.description);
			table.addRow("Information URL", new HtmlView("<a target=\"_blank\" href=\""+rec.url+"\">"+rec.url+"</a>"));
			table.addRow("Resource FQDN Alias", new HtmlView(getAlias(rec.id)));
			
			//Resource Services
			ResourceServiceModel rsmodel = new ResourceServiceModel(context);
			ArrayList<ResourceServiceRecord> services = rsmodel.getAllByResourceID(rec.id);
			GenericView services_view = new GenericView();
			for(ResourceServiceRecord rsrec : services) {
				services_view.add(createServiceView(rsrec));
			}
			table.addRow("Services", services_view);
			
			// Ownership information
			table.addRow("VO Owners of This Resource", getVOOwners(rec.id));
			
			//contacts (only shows contacts that are filled out)
			ContactTypeModel ctmodel = new ContactTypeModel(context);
			ContactRankModel crmodel = new ContactRankModel(context);
			ContactModel pmodel = new ContactModel(context);
			ResourceContactModel rcmodel = new ResourceContactModel(context);
			ArrayList<ResourceContactRecord> rclist = rcmodel.getByResourceID(rec.id);
			HashMap<Integer, ArrayList<ResourceContactRecord>> voclist_grouped = rcmodel.groupByContactTypeID(rclist);
			for(Integer type_id : voclist_grouped.keySet()) {
				ArrayList<ResourceContactRecord> clist = voclist_grouped.get(type_id);
				ContactTypeRecord ctrec = ctmodel.get(type_id);
				
				String cliststr = "";
				
				for(ResourceContactRecord vcrec : clist) {
					ContactRecord person = pmodel.get(vcrec.contact_id);
					ContactRankRecord rank = crmodel.get(vcrec.contact_rank_id);

					cliststr += "<div class='contact_rank contact_"+rank.name+"'>";
					cliststr += person.name;
					cliststr += "</div>";
				
				}
				
				table.addRow(ctrec.name, new HtmlView(cliststr));
			}		
			
			//WLCG
			ResourceWLCGModel wmodel = new ResourceWLCGModel(context);
			ResourceWLCGRecord wrec = wmodel.get(rec.id);
			table.addRow("WLCG Information", createWLCGView(wrec));
			
			table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);

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
			class EditDowntimeButtonDE extends ButtonDE
			{
				String url;
				public EditDowntimeButtonDE(DivEx parent, String _url)
				{
					super(parent, "Add/Edit Downtime");
					this.setStyle(Style.ALINK);
					url = _url;
				}
				protected void onEvent(Event e) {
					redirect(url);
				}
			};
			table.add(new DivExWrapper(new EditButtonDE(context.getDivExRoot(), 
					Config.getApplicationBase()+"/resourceedit?resource_id=" + rec.id)));
			table.add(new DivExWrapper(new EditDowntimeButtonDE(context.getDivExRoot(), 
					Config.getApplicationBase()+"/resourcedowntimeedit?id=" + rec.id)));
		}
		
		return contentview;
	}
	
	private IView createWLCGView(ResourceWLCGRecord rec)
	{
		GenericView view = new GenericView();
		if(rec == null) {
			view.add(new HtmlView("No WLCG Information"));
			return view;
		}
		RecordTableView table = new RecordTableView("inner_table");
		table.addRow("Part of Interop BDII?", rec.interop_bdii);
		table.addRow("Part of Interop Monitoring?", rec.interop_monitoring);
		table.addRow("Part of Interop Accounting?", rec.interop_accounting);
		if (rec.interop_accounting == true) {
			table.addRow("WLCG Accounting Name", rec.accounting_name);
		}
		table.addRow("KSI2K Minimum", rec.ksi2k_minimum);
		table.addRow("KSI2K Maximum", rec.ksi2k_maximum);
		table.addRow("Storage Capacity Minimum (TB)", rec.storage_capacity_minimum);
		table.addRow("Storage Capacity Maximum(TB)", rec.storage_capacity_maximum);
		
		view.add(table);
		return view;
	}
	
	private IView createServiceView(ResourceServiceRecord rec)
	{
		GenericView view = new GenericView();
		
		try {
			ServiceModel smodel = new ServiceModel(context);
			ServiceRecord srec;
			srec = smodel.get(rec.service_id);

			RecordTableView table = new RecordTableView("inner_table");
			table.addHeaderRow(srec.description);
			table.addRow("Hidden Service?", rec.hidden);
			table.addRow("Central Service?", rec.central);
			table.addRow("Optional ServiceURI Override", rec.endpoint_override);
			view.add(table);

		} catch (SQLException e) {
			log.error(e);
		}
		
		return view;
	}

	private IView createAffectedServices(int downtime_id) throws SQLException
	{
		String html = "";
		ResourceDowntimeServiceModel model = new ResourceDowntimeServiceModel(context);
		Collection<ResourceDowntimeServiceRecord> services;

		services = model.getByDowntimeID(downtime_id);
		for(ResourceDowntimeServiceRecord service : services) {
			html += service.service_id + "<br/>";
		}
		return new HtmlView(html);

	}
	
	private String getAlias(int resource_id) throws SQLException
	{
		String html = "";
		ResourceAliasModel ramodel = new ResourceAliasModel(context);
		ArrayList<ResourceAliasRecord> recs = ramodel.getAllByResourceID(resource_id);
		for(ResourceAliasRecord rec : recs) {
			html += StringEscapeUtils.escapeHtml(rec.resource_alias) + "<br/>";
		}
		if (html.length() == 0) html = "N/A"; // Need to make this be consistent with other NULL/empty objects -agopu
		return html;
	}

	private IView getVOOwners(Integer resource_id) throws SQLException
	{
		VOResourceOwnershipModel model = new VOResourceOwnershipModel(context);
		Collection<VOResourceOwnershipRecord> list = model.getAllByResourceID(resource_id);
		
		if(list == null) {
			return null;
		}
		
		String out = "";
		VOModel vo_model = new VOModel(context);
		for(VOResourceOwnershipRecord rec : list) {
//			VORecord keyrec = new VORecord();
//			keyrec.id = rec.vo_id;
			VORecord vo_rec = vo_model.get(rec.vo_id);
			out += vo_rec.name;
			if (rec.percent != null) {
				out += ": " + rec.percent + "%";
			}
			out += "<br/>";
		}
		return new HtmlView(out);
	}

	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends ButtonDE
		{
			String url;
			public NewButtonDE(DivEx parent, String _url)
			{
				super(parent, "Add New Resource");
				url = _url;
			}
			protected void onEvent(Event e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getDivExRoot(), "resourceedit"));
		view.add("About", new HtmlView("This page shows a list of resources that you have access to edit."));		
		return view;
	}
}
