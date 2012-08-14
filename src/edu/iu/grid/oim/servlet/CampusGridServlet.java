package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CampusGridContactModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.CampusGridModel;

import edu.iu.grid.oim.model.db.record.CampusGridContactRecord;
import edu.iu.grid.oim.model.db.record.CampusGridRecord;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.ItemTableView;
import edu.iu.grid.oim.view.ToolTip;

import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.form.CampusGridFormDE;

public class CampusGridServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(CampusGridServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		
		try {	
			//construct view
			BootMenuView menuview = new BootMenuView(context, "campusgrid");
			ContentView contentview = null;
			
			//display either list, or a single resource
			CampusGridRecord rec = null;
			SideContentView sideview = null;
			String id_str  = request.getParameter("id");
			if(id_str != null) {
				Integer id = Integer.parseInt(id_str);
				CampusGridModel model = new CampusGridModel(context);
				rec = model.get(id);
				contentview = new ContentView(context);
				
				// setup crumbs
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				bread_crumb.addCrumb("Topology", "topology");
				bread_crumb.addCrumb("Campus Grid " + rec.name, null);
				contentview.setBreadCrumb(bread_crumb);

				contentview.add(createContent(context, rec)); //false = no edit button	
				
				sideview = createSideView(context);

			} else {
				contentview = createListContentView(context);
			}
			
			BootPage page = new BootPage(context, menuview, contentview, createSideView(context));
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContent(UserContext context, final CampusGridRecord rec) throws ServletException, SQLException {
		ContentView contentview = new ContentView(context);	

		CampusGridModel model = new CampusGridModel(context);
		if(model.canEdit(rec.id)) {
			contentview.add(new HtmlView("<a class=\"btn pull-right\" href=\"campusgridedit?id=" + rec.id + "\">Edit</a>"));
		}
		
		contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));

		RecordTableView table = new RecordTableView();
		contentview.add(table);

	 	table.addRow("Name", rec.name);
		table.addRow("Description", rec.description);
		table.addRow("Submit Host FQDN", rec.fqdn);
		table.addRow("Gratia Probe URL", rec.gratia);
		table.addRow("Longitude", rec.longitude);
		table.addRow("Latitude", rec.latitude);
		ContactTypeModel ctmodel = new ContactTypeModel(context);
		ContactRankModel crmodel = new ContactRankModel(context);
		ContactModel pmodel = new ContactModel(context);
		
		//contacts (only shows contacts that are filled out)
		CampusGridContactModel cgcmodel = new CampusGridContactModel(context);
		ArrayList<CampusGridContactRecord> cgclist = cgcmodel.getByCampusGridID(rec.id);
		HashMap<Integer, ArrayList<CampusGridContactRecord>> cgclist_grouped = cgcmodel.groupByContactTypeID(cgclist);

		for(ContactTypeRecord.Info contact_type : CampusGridFormDE.ContactTypes) {
			if(cgclist_grouped.containsKey(contact_type.id)) {
				ContactTypeRecord ctrec = ctmodel.get(contact_type.id);

				ArrayList<CampusGridContactRecord> clist = cgclist_grouped.get(contact_type.id);
				Collections.sort(clist, new Comparator<CampusGridContactRecord> (){
					public int compare(CampusGridContactRecord a, CampusGridContactRecord b) {
						if (a.getRank() > b.getRank()) // We are comparing based on rank id 
							return 1; 
						return 0;
					}
				});
				String cliststr = "";

				for(CampusGridContactRecord vcrec : clist) {
					ContactRecord person = pmodel.get(vcrec.contact_id);
					ContactRankRecord rank = crmodel.get(vcrec.contact_rank_id);

					cliststr += "<div class='contact_rank contact_"+rank.name+"'>";
					cliststr +=  StringEscapeUtils.escapeHtml(person.name.trim());
					cliststr += "</div>";
				}
				ToolTip tip = new ToolTip(contact_type.desc);
				table.addRow(ctrec.name + " " + tip.render(), new HtmlView(cliststr));
			}
		}		
		table.addRow("Active", rec.active);
		table.addRow("Disable", rec.disable);
	
		
		return contentview;
	}
		
	
	protected ContentView createListContentView(UserContext context) 
		throws ServletException, SQLException
	{
		CampusGridModel model = new CampusGridModel(context);
		ArrayList<CampusGridRecord> campusgrids = model.getAll();
		Collections.sort(campusgrids, new Comparator<CampusGridRecord> () {
			public int compare(CampusGridRecord a, CampusGridRecord b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
		
		ArrayList<CampusGridRecord> editable_cgs = new ArrayList<CampusGridRecord>();
		ArrayList<CampusGridRecord> readonly_cgs = new ArrayList<CampusGridRecord>();
		for(CampusGridRecord rec : campusgrids) {
			if(model.canEdit(rec.id)) {
				editable_cgs.add(rec);
			} else {
				readonly_cgs.add(rec);
			}
		}
		
		ContentView contentview = new ContentView(context);	
		
		if(context.getAuthorization().isUser()) {
			contentview.add(new HtmlView("<h2>My Campus Grids</h2>"));
			if(editable_cgs.size() == 0) {
				contentview.add(new HtmlView("<p>There are no campusgrid where you are listed as a contact (except as submitter) - therefore you are not authorized to edit any campus grid.</p>"));
			}
			ItemTableView table = new ItemTableView(5);
			for(final CampusGridRecord rec : editable_cgs) {
				table.add(renderLink(rec, true));
			}	
			contentview.add(table);
		}
		
		if(readonly_cgs.size() != 0) {
			contentview.add(new HtmlView("<h2>Campus Grids</h2>"));
			ItemTableView table = new ItemTableView(5);
			for(final CampusGridRecord rec : readonly_cgs) {
				table.add(renderLink(rec, true));
			}	
			contentview.add(table);
		}
		
		return contentview;
	}
	
	private GenericView renderLink(CampusGridRecord rec, boolean editable) {
		String name = rec.name;
		GenericView cg = new GenericView();		
		String disable_css = "";
		String tag = "";
		if(rec.disable) {
			disable_css += " disabled";
			tag += " (Disabled)";
		}
		if(!rec.active) {
			disable_css += " inactive";
			tag += " (Inactive)";
		}
		if(editable) {
			cg.add(new HtmlView("<a class=\""+disable_css+"\" title=\""+StringEscapeUtils.escapeHtml(rec.name)+"\" href=\"campusgridedit?id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+tag+"</a>"));
			
		} else {
			cg.add(new HtmlView("<a class=\""+disable_css+"\" title=\""+StringEscapeUtils.escapeHtml(rec.name)+"\" href=\"campusgrid?id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+tag+"</a>"));
		}
		return cg;
	}
	
	private SideContentView createSideView(UserContext context)
	{
		SideContentView view = new SideContentView();
		if(context.getAuthorization().isUser()) {
			view.add(new HtmlView("<a class=\"btn\" href=\"campusgridedit\"><i class=\"icon-plus-sign\"></i> Add New Campus Grid</a>"));
		}
		return view;
	}
}
