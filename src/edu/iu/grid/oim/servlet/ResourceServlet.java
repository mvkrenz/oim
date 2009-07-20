package edu.iu.grid.oim.servlet;

import java.io.IOException;
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

import com.webif.divrep.common.DivRepButton;
import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.common.DivRepToggler;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.ResourceAliasModel;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeServiceModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ResourceWLCGModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.VOResourceOwnershipModel;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOResourceOwnershipRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.ViewWrapper;

public class ResourceServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceServlet.class);  
	
    public ResourceServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		auth.check("edit_my_resource");
		
		try {
			//construct view
			MenuView menuview = new MenuView(context, "resource");
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
		contentview.add(new HtmlView("<h1>Resource</h1>"));
		
		if(resources.size() == 0) {
			contentview.add(new HtmlView("<p>You currently don't have any resources that list your contact in any of the contact types.</p>"));
		}
	
		for(ResourceRecord rec : resources) {
			
			//TODO - need to make "disabled" more conspicuous
			String name = rec.name;
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
	
			//Place table in side the ViewWrapper, then wrap that into DivRepToggler
			RecordTableView table = new RecordTableView();
			// TODO agopu: 10 is an arbitrary number -- perhaps we should make this a user preference? show/hide?
			DivRepToggler toggler = new DivRepToggler(context.getPageRoot(), new ViewWrapper(context.getPageRoot(), table));
			if (resources.size() > 10) {
				toggler.setShow(false);
			} else {
				toggler.setShow(true);
			}
			contentview.add(toggler);
			table.addRow("Resource FQDN", rec.fqdn);

			//pull resource group
			ResourceGroupModel gmodel = new ResourceGroupModel(context);
			ResourceGroupRecord resource_group_rec = gmodel.get(rec.resource_group_id);
			String resource_group_name = null;
			if(resource_group_rec != null) {
				resource_group_name = resource_group_rec.name;
			}
			
			//pull site
			SiteModel smodel = new SiteModel(context);
			SiteRecord srec = smodel.get(resource_group_rec.site_id);
			
			//pull facility
			FacilityModel fmodel = new FacilityModel(context);
			FacilityRecord frec = fmodel.get(srec.facility_id);
			
			//pull support center
			SCModel scmodel = new SCModel(context);
			SCRecord screc = scmodel.get(srec.sc_id);
			
			RecordTableView hierarchy_table = new RecordTableView("inner_table");
			hierarchy_table.addHeaderRow("This Resource Group Belongs To");
			hierarchy_table.addRow("Site", srec.name);
			hierarchy_table.addRow("Facility", frec.name);
			hierarchy_table.addRow("Support Center", screc.name);
			GenericView hierarchy = new GenericView();
			hierarchy.add(new HtmlView(StringEscapeUtils.escapeHtml(resource_group_name)));
			hierarchy.add(hierarchy_table);
			
			table.addRow("Resource Group", hierarchy);
			
			table.addRow("Resource Description", rec.description);
			if(rec.url != null && rec.url.length() != 0) {
				table.addRow("Information URL", new HtmlView("<a target=\"_blank\" href=\""+rec.url+"\">"+rec.url+"</a>"));	
			} else {
				table.addRow("Information URL", (String)null);
			}
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
				ContactTypeRecord ctrec = ctmodel.get(type_id);
				
				ArrayList<ResourceContactRecord> clist = voclist_grouped.get(type_id);
				Collections.sort(clist, new Comparator<ResourceContactRecord> (){
					public int compare(ResourceContactRecord a, ResourceContactRecord b) {
						if (a.getRank() > b.getRank()) // We are comparing based on rank id 
							return 1; 
						return 0;
					}
				});
				String cliststr = "";
				
				for(ResourceContactRecord vcrec : clist) {
					ContactRecord person = pmodel.get(vcrec.contact_id);
					ContactRankRecord rank = crmodel.get(vcrec.contact_rank_id);

					cliststr += "<div class='contact_rank contact_"+rank.name+"'>"+person.name+"</div>";
				}
				
				table.addRow(ctrec.name, new HtmlView(cliststr));
			}		
			
			//WLCG
			ResourceWLCGModel wmodel = new ResourceWLCGModel(context);
			ResourceWLCGRecord wrec = wmodel.get(rec.id);
			table.addRow("WLCG Information", createWLCGView(wrec));

			class EditDowntimeButtonDE extends DivRepButton
			{
				String url;
				public EditDowntimeButtonDE(DivRep parent, String _url)
				{
					super(parent, "Add/Edit Downtime");
					this.setStyle(Style.ALINK);
					url = _url;
				}
				protected void onEvent(DivRepEvent e) {
					redirect(url);
				}
			};

			table.addRow("Downtime", new DivRepWrapper(new EditDowntimeButtonDE(context.getPageRoot(), 
					StaticConfig.getApplicationBase()+"/resourcedowntimeedit?id=" + rec.id)));
			
			table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);

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
			table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), 
					StaticConfig.getApplicationBase()+"/resourceedit?id=" + rec.id)));

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
			table.addRow("ServiceURI Override", rec.endpoint_override);
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

		String percentages = ""; //80,20
		String legends = ""; //ATLAS(80%)|Other(20%)
		Double total = 0D;
		VOModel vo_model = new VOModel(context);
		for(VOResourceOwnershipRecord rec : list) {
			VORecord vo_rec = vo_model.get(rec.vo_id);
			
			if(legends.length() != 0) {
				legends += "|";
			}
			legends += vo_rec.name+"("+rec.percent+"%)";
			
			if(percentages.length() != 0) {
				percentages += ",";
			}
			percentages += rec.percent;
			total += rec.percent;
		}
		if(total < 100D) {
			if(legends.length() != 0) {
				legends += "|";
			}
			legends += "Unknown("+(100-total)+"%)";			
			if(percentages.length() != 0) {
				percentages += ",";
			}
			percentages += (100-total);
		}
		
		String url = "http://chart.apis.google.com/chart?chco=00cc00&cht=p3&chd=t:"+percentages+"0&chs=300x65&chl="+legends;
		return new HtmlView("<img src=\""+url+"\"/>");
	}

	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends DivRepButton
		{
			String url;
			public NewButtonDE(DivRep parent, String _url)
			{
				super(parent, "Add New Resource");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getPageRoot(), "resourceedit"));
		view.add("About", new HtmlView("This page shows a list of resources that you have access to edit."));	
		view.addContactLegend();
		return view;
	}
}
