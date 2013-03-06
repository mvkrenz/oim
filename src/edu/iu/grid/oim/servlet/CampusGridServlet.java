package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.db.CampusGridContactModel;
import edu.iu.grid.oim.model.db.CampusGridFieldOfScienceModel;
import edu.iu.grid.oim.model.db.CampusGridSubmitNodeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.CampusGridModel;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;

import edu.iu.grid.oim.model.db.record.CampusGridContactRecord;
import edu.iu.grid.oim.model.db.record.CampusGridFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.CampusGridRecord;
import edu.iu.grid.oim.model.db.record.CampusGridSubmitNodeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;

import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.ItemTableView;
import edu.iu.grid.oim.view.ToolTip;

import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.CampusGridSubmitNodes;
import edu.iu.grid.oim.view.divrep.form.CampusGridFormDE;

public class CampusGridServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(CampusGridServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		//Authorization auth = context.getAuthorization();
		
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
				bread_crumb.addCrumb("Campus Grid", "campusgrid");
				bread_crumb.addCrumb(rec.name, null);
				contentview.setBreadCrumb(bread_crumb);

				contentview.add(createContent(context, rec)); //false = no edit button	
				
				sideview = createSideView(context, rec);

			} else {
				contentview = createListContentView(context);
			}
			
			BootPage page = new BootPage(context, menuview, contentview, sideview);
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContent(UserContext context, final CampusGridRecord rec) throws ServletException, SQLException {
		ContentView contentview = new ContentView(context);	
		
		contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));

		RecordTableView table = new RecordTableView();
		contentview.add(table);

	 	table.addRow("Name", rec.name);
		table.addRow("Description", rec.description);
		table.addRow("Gratia Probe URL", rec.gratia);
		
		HashMap<Integer, String> maturities = CampusGridFormDE.Maturities;
		table.addRow("Maturity Level", maturities.get(rec.maturity));
		
		//load submit node resources
		ResourceServiceModel rsmodel = new ResourceServiceModel(context);
		ResourceModel rmodel = new ResourceModel(context);
		LinkedHashMap<Integer, String> submitnodes = new LinkedHashMap<Integer, String>();
		if(rec.gateway_submitnode_id != null) {
			ResourceRecord r = rmodel.get(rec.gateway_submitnode_id);
			table.addRow("Gateway Submit Node", r.name);
		} else {
			table.addRow("Gateway Submit Node", "");	
		}
		
		table.addRow("Submit Node FQDNS", getSubmitNodeFQDNs(context, rec.id));	
		table.addRow("Field of Science", getFieldOfScience(context, rec.id));
		table.addRow("Longitude", rec.longitude);
		table.addRow("Latitude", rec.latitude);
		
		ContactTypeModel ctmodel = new ContactTypeModel(context);
		//ontactRankModel crmodel = new ContactRankModel(context);
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
					//ContactRankRecord rank = crmodel.get(vcrec.contact_rank_id);

					cliststr += "<div class='contact_rank contact_"+ContactRank.get(vcrec.contact_rank_id)+"'>";
					cliststr +=  StringEscapeUtils.escapeHtml(person.name.trim());
					cliststr += "</div>";
				}
				ToolTip tip = new ToolTip(contact_type.desc);
				table.addRow(ctrec.name + " " + tip.render(), new HtmlView(cliststr));
			}
		}		
		table.addRow("Disable", rec.disable);
	
		
		return contentview;
	}
		
	private IView getSubmitNodeFQDNs(UserContext context, Integer id) throws SQLException {
		StringBuffer out = new StringBuffer();
		out.append("<ul>");
		CampusGridSubmitNodeModel cmodel = new CampusGridSubmitNodeModel(context);
		for(CampusGridSubmitNodeRecord rarec : cmodel.getAllByCampusGridID(id)) {
			out.append("<li>"+rarec.fqdn+"</li>");
		}
		out.append("</ul>");
		return new HtmlView(out.toString());
	}

	private IView getFieldOfScience(UserContext context, Integer cg_id) throws SQLException
	{
		CampusGridFieldOfScienceModel model = new CampusGridFieldOfScienceModel(context);
		ArrayList<CampusGridFieldOfScienceRecord> list = model.getByCampusGridID(cg_id);
		
		if(list == null) {
			return null;
		}
		String out = "";
		FieldOfScienceModel fmodel = new FieldOfScienceModel(context);
		out += "<ul>";
		for(CampusGridFieldOfScienceRecord rec : list) {
			FieldOfScienceRecord keyrec = new FieldOfScienceRecord();
			keyrec.id = rec.field_of_science_id;
			FieldOfScienceRecord frec = fmodel.get(keyrec);
			out += "<li>" + frec.name + "</li>";
		}
		out += "</ul>";
		return new HtmlView(out);
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
			contentview.add(new HtmlView("<a class=\"btn pull-right\" href=\"campusgridedit\"><i class=\"icon-plus-sign\"></i> Add New Campus Grid</a>"));
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
				table.add(renderLink(rec, model.canEdit(rec.id)));
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
		if(editable) {
			cg.add(new HtmlView("<a class=\""+disable_css+"\" title=\""+StringEscapeUtils.escapeHtml(rec.name)+"\" href=\"campusgridedit?id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+tag+"</a>"));
			
		} else {
			cg.add(new HtmlView("<a class=\""+disable_css+"\" title=\""+StringEscapeUtils.escapeHtml(rec.name)+"\" href=\"campusgrid?id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+tag+"</a>"));
		}
		return cg;
	}
	
	private SideContentView createSideView(UserContext context, CampusGridRecord rec)
	{
		SideContentView view = new SideContentView();

		CampusGridModel model = new CampusGridModel(context);
		if(model.canEdit(rec.id)) {
			view.add(new HtmlView("<a class=\"btn\" href=\"campusgridedit?id=" + rec.id + "\">Edit</a>"));
		}
		return view;
	}
}
