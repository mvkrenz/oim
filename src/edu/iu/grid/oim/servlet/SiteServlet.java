package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.webif.divex.ButtonDE;
import com.webif.divex.DialogDE;
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;

import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.DNModel;

import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;

import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.View;
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
	
    public SiteServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		setAuth(request);
		
		//pull list of all sites
		Collection<SiteRecord> sites = null;
		SiteModel model = new SiteModel(auth);
		try {
			sites = model.getAllEditable();
		
			//construct view
			MenuView menuview = createMenuView("site");
			DivExRoot root = DivExRoot.getInstance(request);
			ContentView contentview = createContentView(root, sites);
			Page page = new Page(menuview, contentview, createSideView(root));
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(final DivExRoot root, Collection<SiteRecord> sites) 
		throws ServletException, SQLException
	{
		ContentView contentview = new ContentView();	
		contentview.add("<h1>Administrativs Sites</h1>");
	
		for(SiteRecord rec : sites) {
			contentview.add("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>");
			
			log.debug("Rendering Site " + rec.name);
	
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

			table.addRow("Facility", getFacilityName(rec.facility_id));
			table.addRow("Support Center", getSCName(rec.sc_id));
//			table.addRow("Submitter Contact", getSubmitterName(rec.submitter_dn_id));
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
			table.add(new EditButtonDE(root, BaseURL()+"/siteedit?site_id=" + rec.id));
			
			/*
			class DeleteDialogDE extends DialogDE
			{
				SiteRecord rec;
				public DeleteDialogDE(DivEx parent, SiteRecord _rec)
				{
					super(parent, "Delete " + _rec.name, "Are you sure you want to delete this site?");
					rec = _rec;
				}
				protected void onEvent(Event e) {
					if(e.getValue().compareTo("ok") == 0) {
						VOModel model = new SiteModel(con, auth);
						try {
							model.delete(rec.id);
							alert("Record Successfully removed.");
							redirect("site");
						} catch (AuthorizationException e1) {
							log.error(e1);
							alert(e1.getMessage());
						} catch (SQLException e1) {
							log.error(e1);
							alert(e1.getMessage());
						}
					}
				}
			}
		
			if(auth.allows("admin_site")) {
				final DeleteDialogDE delete_dialog = new DeleteDialogDE(root, rec);
				table.add(" or ");
				table.add(delete_dialog);
				
				class DeleteButtonDE extends ButtonDE
				{
					public DeleteButtonDE(DivEx parent, String _name)
					{
						super(parent, "Delete");
						setStyle(ButtonDE.Style.ALINK);
					}
					protected void onEvent(Event e) {
						delete_dialog.open();
					}
				};
				table.add(new DeleteButtonDE(root, rec.name));
			}	
			*/

		}
		
		return contentview;
	}
	
	private String getSCName(Integer sc_id) throws SQLException
	{
		if(sc_id == null) return null;
		SCModel model = new SCModel(auth);
		SCRecord sc = model.get(sc_id);	
		if(sc == null) {
			return null;
		}
		return sc.name;
	}
	
	private String getFacilityName(Integer facility_id) throws SQLException
	{
		if(facility_id == null) return null;
		FacilityModel model = new FacilityModel(auth);
		FacilityRecord facility = model.get(facility_id);	
		if(facility == null) {
			return null;
		}
		return facility.name;
	}
	/*
	private String getSubmitterName(Integer submitter_dn_id) throws SQLException
	{
		if(submitter_dn_id == null) return null;
		DNModel model = new DNModel(auth);
		DNRecord dn = model.get(submitter_dn_id);	
		if(dn == null) {
			return null;
		}
		return dn.name;
	}
	*/

	private SideContentView createSideView(DivExRoot root)
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends ButtonDE
		{
			String url;
			public NewButtonDE(DivEx parent, String _url)
			{
				super(parent, "Add New Virtual Organization");
				url = _url;
			}
			protected void onEvent(Event e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(root, "voedit"));
		view.add("About", new HtmlView("This page shows a list of Virtual Organization that you have access to edit."));		
		return view;
	}
}
