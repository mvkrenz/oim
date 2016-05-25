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

import com.divrep.DivRep;
import com.divrep.DivRepContainer;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepToggler;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.ResourceAliasModel;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeServiceModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.ResourceServiceDetailModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ResourceWLCGModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.VOResourceOwnershipModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceDetailRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOResourceOwnershipRecord;
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
import edu.iu.grid.oim.view.PersonView;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.ToolTip;
import edu.iu.grid.oim.view.URLView;
import edu.iu.grid.oim.view.TableView.Row;
import edu.iu.grid.oim.view.divrep.ViewWrapper;
import edu.iu.grid.oim.view.divrep.form.ResourceFormDE;
import edu.iu.grid.oim.view.divrep.form.SCFormDE;
import edu.iu.grid.oim.view.divrep.form.SiteFormDE;

public class ResourceServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		boolean show_edit_button = false;
		
		try {
			//construct view
			ContentView contentview = null;
			BootMenuView menuview = new BootMenuView(context, "topology");
			ResourceRecord rec = null;
			
			//display either list, or a single resource
			String resource_id_str = request.getParameter("id");
			if(resource_id_str != null) {
				Integer resource_id = Integer.parseInt(resource_id_str);
				ResourceModel model = new ResourceModel(context);
				rec = model.get(resource_id);
				contentview = new ContentView(context);
				
				// setup crumbs
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				// bread_crumb.addCrumb("Administration", "admin");
				bread_crumb.addCrumb("Topology", "topology");
				bread_crumb.addCrumb("Resource " + rec.name, null);
				contentview.setBreadCrumb(bread_crumb);

				//contentview.add(new HtmlView("<h2>"+rec.name+"</h2>"));	
				if(rec.active == false) {
					contentview.add(new HtmlView("<div class=\"alert\">This resource is currently inactive.</div>"));
				}
				if(rec.disable == true) {
					contentview.add(new HtmlView("<div class=\"alert\">This resource is currently disabled.</div>"));
				}
				show_edit_button = model.canEdit(resource_id);
				contentview.add(createResourceContent(context, rec, model.canEdit(resource_id))); //false = no edit button

			} else {
				contentview = createListContentView(context);
			}
			
			BootPage page = new BootPage(context, menuview, contentview, createSideView(context, rec, show_edit_button));
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		} catch (NumberFormatException e) {
			log.info(e);
			//invalid resource id.. just be quiet
		}
	}
	
	protected ContentView createListContentView(final UserContext context) 
		throws ServletException, SQLException
	{
		ResourceModel model = new ResourceModel(context);
		ArrayList<ResourceRecord> resources = model.getAll();
		Collections.sort(resources, new Comparator<ResourceRecord> () {
			public int compare(ResourceRecord a, ResourceRecord b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
		
		ArrayList<ResourceRecord> editable_resources = new ArrayList<ResourceRecord>();
		ArrayList<ResourceRecord> readonly_resources = new ArrayList<ResourceRecord>();
		for(ResourceRecord rec : resources) {
			if(model.canEdit(rec.id)) {
				editable_resources.add(rec);
			} else {
				readonly_resources.add(rec);
			}
		}
	
		ContentView contentview = new ContentView(context);
		contentview.add(new HtmlView("<h1>Resources I am authorized to edit</h1>"));
		if(editable_resources.size() == 0) {
			contentview.add(new HtmlView("<p>You currently are not listed as a contact of any contact type (except submitter) on any resource or the resources that a VO owns where you are the VO manager - therefore you are not authorized to edit any resources.</p>"));
		}
		for(final ResourceRecord rec : editable_resources) {
			String name = rec.name;
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
			contentview.add(new DivRepToggler(context.getPageRoot()) {
				@Override
				public DivRep createContent() {
					return createResourceContent(context, rec, true);
				}
				//contentview.add(showResource(rec, false)); //false = no edit button
			}); //true = show edit button
		}
		
		if(readonly_resources.size() != 0) {
			contentview.add(new HtmlView("<br/><h1>Read-Only Resources</h1>"));
			contentview.add(new HtmlView("<p>The following are the resources currently registered with OIM for which you do not have edit access</p>"));
	
			for(final ResourceRecord rec : readonly_resources) {
				String name = rec.name;
				contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
				contentview.add(new DivRepToggler(context.getPageRoot()) {
					@Override
					public DivRep createContent() {
						return createResourceContent(context, rec, false);
					}
					//contentview.add(showResource(rec, false)); //false = no edit button
				});
			}
		}
		
		return contentview;
	}
	
	private DivRep createResourceContent(UserContext context, ResourceRecord rec, boolean show_edit_button) {
		RecordTableView table = new RecordTableView();
		try {
			
			table.addRow("Resource FQDN", rec.fqdn);

			//pull resource group
			ResourceGroupModel gmodel = new ResourceGroupModel(context);
			ResourceGroupRecord resource_group_rec;
		
			resource_group_rec = gmodel.get(rec.resource_group_id);

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
			hierarchy_table.addRow("Facility", frec.name);
			hierarchy_table.addRow("Site", srec.name);
			hierarchy_table.addHeaderRow("This Resource Group is Supported By ");
			hierarchy_table.addRow("Support Center", screc.name);
			GenericView hierarchy = new GenericView();
			hierarchy.add(new HtmlView(StringEscapeUtils.escapeHtml(resource_group_name)));
			hierarchy.add(hierarchy_table);
			
			table.addRow("Resource Group", hierarchy);
			
			table.addRow("Resource Description", rec.description);
			table.addRow("Information URL", new URLView(rec.url));	
			table.addRow("Resource FQDN Alias", new HtmlView(getAlias(context, rec.id)));
			
			//Resource Services
			ResourceServiceModel rsmodel = new ResourceServiceModel(context);
			ArrayList<ResourceServiceRecord> services = rsmodel.getByResourceID(rec.id);
			ResourceServiceDetailModel rsdmodel = new ResourceServiceDetailModel(context);
			ArrayList<ResourceServiceDetailRecord> details = rsdmodel.getAllByResourceID(rec.id);
			GenericView services_view = new GenericView();
			for(ResourceServiceRecord rsrec : services) {
				//pull service details for this service
				HashMap<String, String> values = new HashMap<String, String>();
				for(ResourceServiceDetailRecord drec : details) {
					if(drec.service_id.equals(rsrec.service_id)) {
						values.put(drec.key, drec.value);
					}
				}
				services_view.add(createServiceView(context, rsrec, values));
			}
			table.addRow("Services", services_view);
			
			// Ownership information
			table.addRow("VO Owners of This Resource", getVOOwners(context, rec.id));
			
			//contacts (only shows contacts that are filled out)
			ContactTypeModel ctmodel = new ContactTypeModel(context);
			//ContactRankModel crmodel = new ContactRankModel(context);
			ContactModel pmodel = new ContactModel(context);
			ResourceContactModel rcmodel = new ResourceContactModel(context);
			ArrayList<ResourceContactRecord> rclist = rcmodel.getByResourceID(rec.id);
			HashMap<Integer, ArrayList<ResourceContactRecord>> resourceclist_grouped = rcmodel.groupByContactTypeID(rclist);

			for(ContactTypeRecord.Info contact_type : ResourceFormDE.ContactTypes) {
				ContactTypeRecord ctrec = ctmodel.get(contact_type.id);
				
				if(resourceclist_grouped.containsKey(contact_type.id)) {

					ArrayList<ResourceContactRecord> clist = resourceclist_grouped.get(contact_type.id);
					Collections.sort(clist, new Comparator<ResourceContactRecord> (){
						public int compare(ResourceContactRecord a, ResourceContactRecord b) {
							if (a.contact_rank_id > b.contact_rank_id) // We are comparing based on rank id 
								return 1; 
							return 0;
						}
					});
					
					StringBuffer cliststr = new StringBuffer();
					for(ResourceContactRecord vcrec : clist) {
						ContactRecord person = pmodel.get(vcrec.contact_id);
						ContactRank rank = ContactRank.get(vcrec.contact_rank_id);
						PersonView pv = new PersonView(person, rank);
						cliststr.append(pv.render());
					}
					ToolTip tip = new ToolTip(contact_type.desc);
					table.addRow(ctrec.name + " " + tip.render(), new HtmlView(cliststr.toString()));
				}
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
				protected void onClick(DivRepEvent e) {
					redirect(url);
				}
			};
			
			Row r;
			ToolTip tip = new ToolTip("Shows if this resource has been activated by GOC staff. Until it is activated, this resource is not available to OSG in general sense.");
			r = table.addRow(new HtmlView("Active" + tip.render()), rec.active);
			
			tip = new ToolTip("Shows if this resource has been removed from the OIM database.");
			r = table.addRow(new HtmlView("Disable" + tip.render()), rec.disable);

			if(show_edit_button) {
				table.add(new HtmlView("<a class=\"btn\" href=\"resourceedit?id=" + rec.id+"\">Edit</a>"));
			}
		} catch (SQLException e) {
			return new DivRepStaticContent(context.getPageRoot(), e.toString());
		}
		return new ViewWrapper(context.getPageRoot(), table);
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
		table.addRow("HEPSPEC Value", rec.hepspec);
		table.addRow("APEL Normalization Factor", rec.apel_normal_factor);
		table.addRow("Storage Capacity Minimum (TB)", rec.storage_capacity_minimum);
		table.addRow("Storage Capacity Maximum (TB)", rec.storage_capacity_maximum);
		table.addRow("Tape Capacity (TB)", rec.tape_capacity);
		
		view.add(table);
		return view;
	}
	
	private IView createServiceView(UserContext context, ResourceServiceRecord rec, HashMap<String, String> details)
	{
		GenericView view = new GenericView();
		
		try {
			ServiceModel smodel = new ServiceModel(context);
			ServiceRecord srec;
			srec = smodel.get(rec.service_id);

			RecordTableView table = new RecordTableView("inner_table");
			table.addHeaderRow(srec.description);

			//TODO - should I do something more human friendly things?
			for(String key : details.keySet()) {
				String value = details.get(key);
				table.addRow(key, value);
			}
			
			view.add(table);

		} catch (SQLException e) {
			log.error(e);
		}
		
		return view;
	}
	
	private String getAlias(UserContext context, int resource_id) throws SQLException
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

	private IView getVOOwners(UserContext context, Integer resource_id) throws SQLException
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

	private SideContentView createSideView(UserContext context, ResourceRecord rec, boolean show_edit_button)
	{
		SideContentView view = new SideContentView();
		if(show_edit_button) {
			view.add(new HtmlView("<p><a class=\"btn\" href=\"resourceedit?id=" + rec.id+"\">Edit</a></p>"));
		}
		if(context.getAuthorization().isUser()) {
			view.add(new HtmlView("<p><a class=\"btn\" href=\"resourceedit\">Register New Resource</a></p>"));
		}
		view.addContactLegend();
		return view;
	}
}
