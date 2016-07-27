package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepToggler;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
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
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{			
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("view_topology");
		
		try {
			//construct view
			BootMenuView menuview = new BootMenuView(context, "topology");
			String facility_id_str = request.getParameter("facility_id");
			Integer facility_id = null;
			if(facility_id_str != null) {
				facility_id = Integer.parseInt(facility_id_str);
			}
			ContentView contentview = createContentView(context, facility_id);
			
			BootPage page = new BootPage(context, menuview, contentview, null);
			page.addExCSS("hierarchy.css");
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
		
		//context.storeDivRepSession();
	}
		
	protected ContentView createContentView(UserContext context, Integer facility_id) 
		throws ServletException, SQLException
	{
		Authorization auth = context.getAuthorization();
		ContentView contentview = new ContentView(context);
		//contentview.add(new HtmlView("<h1>OSG Topology</h1>"));
		
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
		
		if(auth.allows("edit_all_facility")) {
			contentview.add(new HtmlView("<p><a class=\"btn pull-right\" href=\"facilityedit\"><i class=\"icon-plus-sign\"></i> Add Facility</a></p>"));
		}
		
		//display facility selector
		LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap<Integer, String>();
		for(FacilityRecord rec : frecs) {
			keyvalues.put(rec.id, rec.name);
		}
		
		final DivRepSelectBox facility_selector = new DivRepSelectBox(context.getPageRoot(), keyvalues);
		facility_selector.setNullLabel("(Show All Facilities)");
		facility_selector.setValue(facility_id);
		facility_selector.addClass("inline-block");
		facility_selector.addEventListener(new DivRepEventListener() {

			@Override
			public void handleEvent(DivRepEvent e) {
				if(e.value.length() != 0) {
					facility_selector.redirect("topology?facility_id="+e.value);
				} else {
					facility_selector.redirect("topology");
				}
			}
		});
		//contentview.add(new HtmlView("Facility  "));
		contentview.add(facility_selector);
		//contentview.add(new HtmlView("</td></tr></table>"));

		final DivRepCheckBox hideDisabled = new DivRepCheckBox(context.getPageRoot());
	
		hideDisabled.addEventListener(new DivRepEventListener() {

			@Override
			public void handleEvent(DivRepEvent e) {

					hideDisabled.redirect("topology");
			}
		});
		
		contentview.add(new HtmlView("<br>Hide Disabled Resources/Groups "));
	contentview.add(hideDisabled);


		
		contentview.add(new HtmlView("<div id=\"topology\">"));
		
		for(FacilityRecord frec : frecs) {
			if((facility_id != null && !frec.id.equals(facility_id)) ) continue;
			
			String disable_css = "";
			String tag = "";
			if(frec.disable) {
				disable_css += " rg_disabled";
				tag += "(Disabled)";
			}

			contentview.add(new HtmlView("<div class=\"hierarchy_facility round4 well "+disable_css+"\">"));
			if(auth.allows("edit_all_site")) {
				//contentview.add(new HtmlView("<div class=\"hierarchy_site round4 new\" style=\"width: 90px;\">"));
				contentview.add(new HtmlView("&nbsp;<a class=\"new pull-right\" href=\"siteedit?facility_id="+frec.id+"\"><i class=\"icon-plus-sign\"></i> Add Site</a>"));
				//contentview.add(new HtmlView("</div>"));	
			}
			if(auth.allows("edit_all_facility")) {
				contentview.add(new HtmlView("<a href=\"facilityedit?facility_id="+frec.id+"\"><b>"+StringEscapeUtils.escapeHtml(frec.name)+"</b> <span>Facility "+tag+"</span></a>"));
					//"<span class=\"edit facility_edit\" onclick=\"document.location='"+StaticConfig.getStaticBase()+"/facilityedit?facility_id="+frec.id+"';\">Edit</span>"));
			} else {
				contentview.add(new HtmlView("<b>"+StringEscapeUtils.escapeHtml(frec.name)+"</b> <span>Facility "+tag+"</span>"));
			}
			
			ArrayList<SiteRecord> srecs = smodel.getByFacilityID(frec.id);
			Collections.sort(srecs, new Comparator<SiteRecord> () {
				public int compare(SiteRecord a, SiteRecord b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			});
			for(SiteRecord srec : srecs) {
				//if( hideDisabled.getValue() == false && srec.disable) continue;

				disable_css = "";
				tag = "";
				if(srec.disable) {
					disable_css += " site_disabled";
					tag += "(Disabled)";
				}
				
				contentview.add(new HtmlView("<div class=\"hierarchy_site round4 "+disable_css+"\">"));
				SCRecord screc = scmodel.get(srec.sc_id);
				contentview.add(new HtmlView("<div class=\"hierarchy_sitesc\">Supported by <a href=\"sc?id="+screc.id+"\"><b>"+StringEscapeUtils.escapeHtml(screc.name)+"</b></a></div>"));
				
				if(auth.allows("edit_all_site")) {
					contentview.add(new HtmlView("<a href=\"siteedit?site_id="+srec.id+"\"><b class=\"site_name\">"+StringEscapeUtils.escapeHtml(srec.name)+"</b> <span>Site "+tag+"</span></a>"));
							//"<span class=\"edit site_edit\" onclick=\"document.location='"+StaticConfig.getStaticBase()+"/siteedit?site_id="+srec.id+"';\">Edit</span>"));
				} else {
					contentview.add(new HtmlView("<a href=\"site?site_id="+srec.id+"\"><b class=\"site_name\">"+StringEscapeUtils.escapeHtml(srec.name)+"</b> <span>Site "+tag+"</span></a>"));					
				}
				//put all production on the right, and itb on the left
				ArrayList<ResourceGroupRecord> rgrecs = rgmodel.getBySiteID(srec.id);
				Collections.sort(rgrecs, new Comparator<ResourceGroupRecord> () {
					public int compare(ResourceGroupRecord a, ResourceGroupRecord b) {
						return a.getName().compareToIgnoreCase(b.getName());
					}
				});
				GenericView prod = new GenericView();
				if(auth.allows("edit_all_resource_group")) {
					prod.add(new HtmlView("<a class=\"new pull-right\" href=\"resourcegroupedit?gridtype_id=1&site_id="+srec.id+"\"><i class=\"icon-plus-sign icon-white\"></i> Add Production Resource Group</a>"));
					//prod.add(new HtmlView("</div>"));
				}
				prod.add(new HtmlView("<h4 class=\"resource_groups_header\">Production Resource Groups</h4>"));
				GenericView itb = new GenericView();
				if(auth.allows("edit_all_resource_group")) {
					//itb.add(new HtmlView("<div class=\"hierarchy_rg round4 new\" style=\"width: 250px;\">"));
					itb.add(new HtmlView("<a class=\"new pull-right\" href=\"resourcegroupedit?gridtype_id=2&site_id="+srec.id+"\"><i class=\"icon-plus-sign icon-white\"></i> Add ITB Resource Group</a>"));
					//itb.add(new HtmlView("</div>"));
				}
				itb.add(new HtmlView("<h4 class=\"resource_groups_header\">ITB Resource Groups</h4>"));
				for(ResourceGroupRecord rgrec : rgrecs) {
					GenericView rg = new GenericView();
					disable_css = "";
					tag = "";
					if(rgrec.disable) {
						disable_css += " rg_disabled";
						tag += "(Disabled)";
					}
					rg.add(new HtmlView("<div class=\"hierarchy_rg round4"+disable_css+"\">"));
					
					if(auth.allows("edit_my_resource")) {
						rg.add(new HtmlView("<a class=\"new pull-right\" href=\"resourceedit?rg_id="+rgrec.id+"\"><i class=\"icon-plus-sign icon-white\"></i> Add Resource</a>"));
						//rg.add(new HtmlView("</div>"));//resource_group
					}
					if(auth.allows("edit_all_resource_group")) {
						rg.add(new HtmlView("<a href=\"resourcegroupedit?id="+rgrec.id+"\"><b>"+StringEscapeUtils.escapeHtml(rgrec.name)+"</b> "+tag+"</a>"));
						//"<span class=\"edit rg_edit\" onclick=\"document.location='"+StaticConfig.getStaticBase()+"/resourcegroupedit?id="+rgrec.id+"';\">Edit</span>"));
					} else {
						rg.add(new HtmlView("<b>"+StringEscapeUtils.escapeHtml(rgrec.name)+"</b> "+tag));						
					}
					
					ArrayList<ResourceRecord> rrecs = rmodel.getByGroupID(rgrec.id);
					Collections.sort(rrecs, new Comparator<ResourceRecord> () {
						public int compare(ResourceRecord a, ResourceRecord b) {
							return a.getName().compareToIgnoreCase(b.getName());
						}
					});
					for(ResourceRecord rrec : rrecs) {
						//if( hideDisabled.getValue() == false && rrec.disable) continue;
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

						ArrayList<ResourceServiceRecord> rsrecs = rsmodel.getByResourceID(rrec.id);
						for(ResourceServiceRecord rsrec : rsrecs) {
							ServiceRecord servicerec = servicemodel.get(rsrec.service_id);
							rg.add(new HtmlView("<span class=\"hierarchy_service\">"+StringEscapeUtils.escapeHtml(servicerec.name)+"</span>"));
						}	
						if(auth.allows("edit_my_resource")) {
							rg.add(new HtmlView("<a href=\"resourceedit?id="+rrec.id+"\"><b>"+StringEscapeUtils.escapeHtml(rrec.name)+"</b> <span class=\"fqdn\">"+StringEscapeUtils.escapeHtml(rrec.fqdn)+"</span> "+tag + "</a>"));
									//"<span class=\"edit r_edit\" onclick=\"document.location='"+StaticConfig.getStaticBase()+"/resourceedit?resource_id="+rrec.id+"';\">Edit</span>"));
						} else {
							rg.add(new HtmlView("<a href=\"resource?id="+rrec.id+"\"><b>"+StringEscapeUtils.escapeHtml(rrec.name)+"</b> <span class=\"fqdn\">"+StringEscapeUtils.escapeHtml(rrec.fqdn)+"</span> "+tag + "</a>"));
						}
						
						rg.add(new HtmlView("</div>"));//resource
					}	
					if(rrecs.size() == 0) {
						rg.add(new HtmlView("<br>"));
					}
					//rg.add(new HtmlView("<div class=\"hierarchy_r round4 new\" style=\"width: 110px;\">"));
					
					rg.add(new HtmlView("</div>"));//resource_group

					if(rgrec.osg_grid_type_id == 1) {
						prod.add(rg);
					} else {
						itb.add(rg);
					}
				}	
				//prod.add(new HtmlView("<div class=\"hierarchy_rg round4 new\" style=\"width: 250px;\">"));

				
				contentview.add(new HtmlView("<div class=\"row-fluid\">"));
				contentview.add(new HtmlView("<div class=\"span6\">"));
				contentview.add(prod);
				contentview.add(new HtmlView("</div>"));
				contentview.add(new HtmlView("<div class=\"span6\">"));
				contentview.add(itb);
				contentview.add(new HtmlView("</div>"));
				contentview.add(new HtmlView("</div>"));
				
				contentview.add(new HtmlView("</div>"));//site
			}	
			
			contentview.add(new HtmlView("</div>"));//facility
		}
		//contentview.add(new HtmlView("<a href=\""+StaticConfig.getApplicationBase()+"/facilityedit\">Add New Facility</a>"));
		contentview.add(new HtmlView("</div>"));//topology
		
		return contentview;
	}
}
