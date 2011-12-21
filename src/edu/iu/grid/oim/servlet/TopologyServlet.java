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
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepToggler;

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
import edu.iu.grid.oim.model.db.ResourceServiceDetailModel;
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
import edu.iu.grid.oim.model.db.record.ResourceServiceDetailRecord;
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
import edu.iu.grid.oim.view.ItemTableView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.ToolTip;
import edu.iu.grid.oim.view.URLView;
import edu.iu.grid.oim.view.TableView.Row;
import edu.iu.grid.oim.view.divrep.ViewWrapper;
import edu.iu.grid.oim.view.divrep.form.ResourceFormDE;
import edu.iu.grid.oim.view.divrep.form.SCFormDE;

public class TopologyServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(TopologyServlet.class);  
	
    public TopologyServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{			
		try {
			//construct view
			MenuView menuview = new MenuView(context, "topology");
			ContentView contentview = createContentView();
			
			Page page = new Page(context, menuview, contentview, createSideView());
			page.addCSS("hierarchy.css");
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
		//contentview.add(new HtmlView("<h1>OSG Topology</h1>"));
		contentview.add(new HtmlView("<p>Click on entity names to view/edit.</p>"));
		
		FacilityModel fmodel = new FacilityModel(context);
		SiteModel smodel = new SiteModel(context);
		SCModel scmodel = new SCModel(context);
		ResourceGroupModel rgmodel = new ResourceGroupModel(context);
		ResourceModel rmodel = new ResourceModel(context);
		ResourceServiceModel rsmodel = new ResourceServiceModel(context);
		ServiceModel servicemodel = new ServiceModel(context);
		ArrayList<FacilityRecord> frecs = fmodel.getAll();
		Collections.sort(frecs, new Comparator<FacilityRecord> () {
			public int compare(FacilityRecord a, FacilityRecord b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
		
		for(FacilityRecord frec : frecs) {
			contentview.add(new HtmlView("<div class=\"hierarchy_facility round4\">"));
			contentview.add(new HtmlView("<a href=\""+StaticConfig.getStaticBase()+"/facilityedit?facility_id="+frec.id+"\"><b>"+StringEscapeUtils.escapeHtml(frec.name)+"</b> <span>Facility</span></a>"));
					//"<span class=\"edit facility_edit\" onclick=\"document.location='"+StaticConfig.getStaticBase()+"/facilityedit?facility_id="+frec.id+"';\">Edit</span>"));
			
			ArrayList<SiteRecord> srecs = smodel.getByFacilityID(frec.id);
			Collections.sort(srecs, new Comparator<SiteRecord> () {
				public int compare(SiteRecord a, SiteRecord b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			});
			for(SiteRecord srec : srecs) {
				contentview.add(new HtmlView("<div class=\"hierarchy_site round4\">"));
				SCRecord screc = scmodel.get(srec.sc_id);
				contentview.add(new HtmlView("<div class=\"hierarchy_sitesc\">Supported by <a href=\""+StaticConfig.getStaticBase()+"/scedit?id="+screc.id+"\"><b>"+StringEscapeUtils.escapeHtml(screc.name)+"</b></a></div>"));
				
				contentview.add(new HtmlView("<a href=\""+StaticConfig.getStaticBase()+"/siteedit?site_id="+srec.id+"\"><b>"+StringEscapeUtils.escapeHtml(srec.name)+"</b> <span>Site</span></a>"));
						//"<span class=\"edit site_edit\" onclick=\"document.location='"+StaticConfig.getStaticBase()+"/siteedit?site_id="+srec.id+"';\">Edit</span>"));
				
				//put all production on the right, and itb on the left
				ArrayList<ResourceGroupRecord> rgrecs = rgmodel.getBySiteID(srec.id);
				Collections.sort(rgrecs, new Comparator<ResourceGroupRecord> () {
					public int compare(ResourceGroupRecord a, ResourceGroupRecord b) {
						return a.getName().compareToIgnoreCase(b.getName());
					}
				});
				GenericView prod = new GenericView();
				prod.add(new HtmlView("<h4 class=\"resource_groups_header\">Production Resource Groups</h4>"));
				GenericView itb = new GenericView();
				itb.add(new HtmlView("<h4 class=\"resource_groups_header\">ITB Resource Groups</h4>"));
				for(ResourceGroupRecord rgrec : rgrecs) {
					GenericView rg = new GenericView();
					String disable_css = "";
					String tag = "";
					if(rgrec.disable) {
						disable_css += " rg_disabled";
						tag += "(Disabled)";
					}
					rg.add(new HtmlView("<div class=\"hierarchy_rg round4"+disable_css+"\">"));
					rg.add(new HtmlView("<a href=\""+StaticConfig.getStaticBase()+"/resourcegroupedit?id="+rgrec.id+"\"><b>"+StringEscapeUtils.escapeHtml(rgrec.name)+"</b> "+tag+"</a>"));
					//"<span class=\"edit rg_edit\" onclick=\"document.location='"+StaticConfig.getStaticBase()+"/resourcegroupedit?id="+rgrec.id+"';\">Edit</span>"));
				
					ArrayList<ResourceRecord> rrecs = rmodel.getByGroupID(rgrec.id);
					Collections.sort(rrecs, new Comparator<ResourceRecord> () {
						public int compare(ResourceRecord a, ResourceRecord b) {
							return a.getName().compareToIgnoreCase(b.getName());
						}
					});
					for(ResourceRecord rrec : rrecs) {
						disable_css = "";
						tag = "";
						if(rrec.disable) {
							disable_css += " r_disabled";
							tag += "(Disabled)";
						}
						if(!rrec.active) {
							disable_css += " r_inactive";
							tag += " (Inactive)";
						}
						rg.add(new HtmlView("<div class=\"hierarchy_r round4"+disable_css+"\">"));

						ArrayList<ResourceServiceRecord> rsrecs = rsmodel.getByResourceID(rgrec.id);
						for(ResourceServiceRecord rsrec : rsrecs) {
							ServiceRecord servicerec = servicemodel.get(rsrec.service_id);
							rg.add(new HtmlView("<span class=\"hierarchy_service\">"+StringEscapeUtils.escapeHtml(servicerec.name)+"</span>"));
						}	
						rg.add(new HtmlView("<a href=\""+StaticConfig.getStaticBase()+"/resourceedit?id="+rrec.id+"\"><b>"+StringEscapeUtils.escapeHtml(rrec.name)+"</b> <span class=\"fqdn\">"+StringEscapeUtils.escapeHtml(rrec.fqdn)+"</span> "+tag + "</a>"));
								//"<span class=\"edit r_edit\" onclick=\"document.location='"+StaticConfig.getStaticBase()+"/resourceedit?resource_id="+rrec.id+"';\">Edit</span>"));

						rg.add(new HtmlView("</div>"));//resource
					}	
					if(rrecs.size() == 0) {
						rg.add(new HtmlView("<br>"));
					}
					rg.add(new HtmlView("<a href=\""+StaticConfig.getStaticBase()+"/resourceedit?rg_id="+rgrec.id+"\">Add New Resource</a>"));

					rg.add(new HtmlView("</div>"));//resource_group

					if(rgrec.osg_grid_type_id == 1) {
						prod.add(rg);
					} else {
						itb.add(rg);
					}
				}	
				prod.add(new HtmlView("<a href=\""+StaticConfig.getStaticBase()+"/resourcegroupedit?gridtype_id=1&site_id="+srec.id+"\">Add New Production Resource Group</a>"));
				itb.add(new HtmlView("<a href=\""+StaticConfig.getStaticBase()+"/resourcegroupedit?gridtype_id=2&site_id="+srec.id+"\">Add New ITB Resource Group</a>"));
				
				ItemTableView table = new ItemTableView(2);
				table.add(prod);
				table.add(itb);
				contentview.add(table);
				
				contentview.add(new HtmlView("</div>"));//site
			}	
			contentview.add(new HtmlView("<a href=\""+StaticConfig.getStaticBase()+"/siteedit?facility_id="+frec.id+"\">Add New Site</a>"));
				
			contentview.add(new HtmlView("</div>"));//facility
		}
		contentview.add(new HtmlView("<a href=\""+StaticConfig.getStaticBase()+"/facilityedit\">Add New Facility</a>"));
		
		
		return contentview;
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
				super(parent, "Add New Resource");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};
		*/
		//view.add("Operation", new NewButtonDE(context.getPageRoot(), "resourceedit"));	
		return view;
	}
}
