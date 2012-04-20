package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.VOModel;

import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
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
import edu.iu.grid.oim.view.TableView;
import edu.iu.grid.oim.view.Utils;
import edu.iu.grid.oim.view.TableView.Row;

public class SiteServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(SiteServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		//auth.check("edit_all_site");
		
		try {	
			//construct view
			BootMenuView menuview = new BootMenuView(context, "site");
			ContentView contentview = null;
			
			//display either list, or a single resource
			SiteRecord rec = null;
			SideContentView sideview = null;
			String site_id_str = request.getParameter("site_id");
			if(site_id_str != null) {
				Integer site_id = Integer.parseInt(site_id_str);
				SiteModel model = new SiteModel(context);
				rec = model.get(site_id);
				contentview = new ContentView();
				
				// setup crumbs
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				// bread_crumb.addCrumb("Administration", "admin");
				bread_crumb.addCrumb("Topology", "topology");
				bread_crumb.addCrumb("Site " + rec.name, null);
				contentview.setBreadCrumb(bread_crumb);

				contentview.add(createSiteContent(context, rec)); //false = no edit button	
				
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
	
	protected ContentView createSiteContent(UserContext context, final SiteRecord rec) throws ServletException, SQLException {
		/*
		SiteModel model = new SiteModel(context);
		ArrayList<SiteRecord> sites = model.getAll();
		Collections.sort(sites, new Comparator<SiteRecord> () {
			public int compare(SiteRecord a, SiteRecord b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
		*/

		ContentView contentview = new ContentView();	

		contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));

		RecordTableView table = new RecordTableView();
		contentview.add(table);

	 	table.addRow("Long Name", rec.long_name);
		table.addRow("Description", rec.description);
		table.addRow("Street", rec.address_line_1);
		table.addRow("Address line 2", rec.address_line_2);
		table.addRow("City", rec.city);
		table.addRow("State", rec.state);
		table.addRow("Zipcode", rec.zipcode);
		table.addRow("Country", rec.country);
		table.addRow("Longitude", rec.longitude);
		table.addRow("Latitude", rec.latitude);
		table.addRow("Facility", getFacilityName(context, rec.facility_id));
		table.addRow("Support Center", getSCName(context, rec.sc_id));
		table.addRow("Active", rec.active);
		table.addRow("Disable", rec.disable);
		
		/*
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
		table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), StaticConfig.getApplicationBase()+"/siteedit?site_id=" + rec.id)));
		*/
		if(context.getAuthorization().isUser()) {
			table.add(new HtmlView("<a class=\"btn\" href=\"siteedit?id=" + rec.id + "\">Edit</a>"));
		}
		
		return contentview;
	}
		
	
	protected ContentView createListContentView(UserContext context) 
		throws ServletException, SQLException
	{
		SiteModel model = new SiteModel(context);
		ArrayList<SiteRecord> sites = model.getAll();
		Collections.sort(sites, new Comparator<SiteRecord> () {
			public int compare(SiteRecord a, SiteRecord b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});

		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h2>Administrative Sites</h2>"));
		
		//contentview.add(new HtmlView("<p>Following are the currently registered virtual organizations on OIM - you do not have edit access on these records.</p>"));

		ItemTableView table = new ItemTableView(5);
		for(final SiteRecord rec : sites) {
			String name = rec.name;
			GenericView vo = new GenericView();		
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
			vo.add(new HtmlView("<a class=\""+disable_css+"\" title=\""+StringEscapeUtils.escapeHtml(rec.long_name)+"\" href=\"site?site_id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+tag+"</a>"));
			//vo.add(new HtmlView("<p>"+StringEscapeUtils.escapeHtml(rec.long_name)+"</p>"));
			table.add(vo);

		}
		contentview.add(table);
		
		return contentview;
	}
	
	private String getSCName(UserContext context, Integer sc_id) throws SQLException
	{
		if(sc_id == null) return null;
		SCModel model = new SCModel(context);
		SCRecord sc = model.get(sc_id);	
		if(sc == null) {
			return null;
		}
		return sc.name;
	}
	
	private String getFacilityName(UserContext context, Integer facility_id) throws SQLException
	{
		if(facility_id == null) return null;
		FacilityModel model = new FacilityModel(context);
		FacilityRecord facility = model.get(facility_id);	
		if(facility == null) {
			return null;
		}
		return facility.name;
	}

	private SideContentView createSideView(UserContext context)
	{
		SideContentView view = new SideContentView();
		if(context.getAuthorization().isUser()) {
			view.add(new HtmlView("<a class=\"btn\" href=\"siteedit\">Add New Administrative Site</a>"));
		}
		/*
		class NewButtonDE extends DivRepButton
		{
			String url;
			public NewButtonDE(DivRep parent, String _url)
			{
				super(parent, "Add New Administrative Site");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getPageRoot(), "siteedit"));
		view.add("About", new HtmlView("This page shows a list of administratives sites in various facilities that all registered OIM users are able to edit. We ask that you please refrain from editing sites that are not directly related to you unless there is a specific reason to do so! All changes are audited by GOC staff."));		
		*/
		return view;
	}
}
