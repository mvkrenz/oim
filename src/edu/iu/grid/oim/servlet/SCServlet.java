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

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.ItemTableView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.ToolTip;
import edu.iu.grid.oim.view.divrep.ViewWrapper;
import edu.iu.grid.oim.view.divrep.form.SCFormDE;
import edu.iu.grid.oim.view.divrep.form.VOFormDE;

public class SCServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(SCServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		//auth.check("edit_my_sc");
		
		try {		
			//construct view
			//MenuView menuview = new MenuView(context, "sc");
			//ContentView contentview = createContentView();
			
			//construct view
			BootMenuView menuview = new BootMenuView(context, "sc");
			ContentView contentview = null;
			SideContentView sideview = null;
			//display either list, or a single resource
			SCRecord rec = null;
			String sc_id_str = request.getParameter("id");
			if(sc_id_str != null) {
				Integer sc_id = Integer.parseInt(sc_id_str);
				SCModel model = new SCModel(context);
				rec = model.get(sc_id);
				contentview = new ContentView();
				
				// setup crumbs
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				// bread_crumb.addCrumb("Administration", "admin");
				bread_crumb.addCrumb("Support Center", "sc");
				bread_crumb.addCrumb(rec.name, null);
				contentview.setBreadCrumb(bread_crumb);

				//contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));	
				if(rec.active == false) {
					contentview.add(new HtmlView("<div class=\"alert\">This Support Center is currently inactive.</div>"));
				}
				if(rec.disable == true) {
					contentview.add(new HtmlView("<div class=\"alert\">This Support Center is currently disabled.</div>"));
				}
				contentview.add(createSCContent(context, rec, model.canEdit(sc_id))); //false = no edit button	
				sideview = createSideView(context);
			} else {
				contentview = createListContent(context);
			}
			
			BootPage page = new BootPage(context, menuview, contentview, sideview);
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createListContent(UserContext context) 
		throws ServletException, SQLException
	{
		SCModel model = new SCModel(context);
		ArrayList<SCRecord> scs = model.getAll();
		Collections.sort(scs, new Comparator <SCRecord>() {
			public int compare(SCRecord a, SCRecord b) {
				return a.getName().compareToIgnoreCase(b.getName()); // We are comparing based on name
			}
		});
		ArrayList<SCRecord> editable_scs = new ArrayList<SCRecord>();
		ArrayList<SCRecord> readonly_scs = new ArrayList<SCRecord>();
		for(SCRecord rec : scs) {
			if(model.canEdit(rec.id)) {
				editable_scs.add(rec);
			} else {
				readonly_scs.add(rec);
			}
		}
		
		ContentView contentview = new ContentView();
		if(context.getAuthorization().isUser()) {
			contentview.add(new HtmlView("<a href=\"scedit\" class=\"btn pull-right\"><i class=\"icon-plus-sign\"></i> Add New Support Center</a>"));
			contentview.add(new HtmlView("<h2>My Support Centers</h2>"));
			if(editable_scs.size() == 0) {
				contentview.add(new HtmlView("<p>You currently are not listed as a contact of any contact type (except submitter) on any support center - therefore you are not authorized to edit any SCs.</p>"));
			}
			
			ItemTableView table = new ItemTableView(5);
			for(SCRecord rec : editable_scs) {
				String name = rec.name;
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
				table.add(new HtmlView("<a class=\""+disable_css+"\" title=\""+StringEscapeUtils.escapeHtml(rec.long_name)+"\" href=\"scedit?id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+tag+"</a>"));
	
				//contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
				//contentview.add(showSC(rec, true)); //true = show edit button
			}
			contentview.add(table);
		}
			
		if(readonly_scs.size() != 0) {
			contentview.add(new HtmlView("<h2>Support Centers</h2>"));
			//contentview.add(new HtmlView("<p>Following are the currently registered support centers on OIM - you do not have edit access on these records.</p>"));
			ItemTableView table = new ItemTableView(5);
			for(SCRecord rec : readonly_scs) {
				String name = rec.name;
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
				table.add(new HtmlView("<a class=\""+disable_css+"\" title=\""+StringEscapeUtils.escapeHtml(rec.long_name)+"\" href=\"sc?id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+tag+"</a>"));

				//contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
				//contentview.add(showSC(rec, false)); //false = no edit button
			}
			contentview.add(table);
		}
		
		return contentview;
	}
	
	public DivRep createSCContent(UserContext context, final SCRecord rec, final boolean show_edit_button) {
		RecordTableView table = new RecordTableView();
		try {			
		 	table.addRow("Long Name", rec.long_name);
			table.addRow("Description", rec.description);
			table.addRow("Community", rec.community);
			table.addRow("Ex. Assignment ID", rec.external_assignment_id);

			ContactTypeModel ctmodel = new ContactTypeModel(context);
			ContactRankModel crmodel = new ContactRankModel(context);
			ContactModel pmodel = new ContactModel(context);
			
			//contacts (only shows contacts that are filled out)
			SCContactModel sccmodel = new SCContactModel(context);
			ArrayList<SCContactRecord> scclist = sccmodel.getBySCID(rec.id);
			HashMap<Integer, ArrayList<SCContactRecord>> scclist_grouped = sccmodel.groupByContactTypeID(scclist);
			for(ContactTypeRecord.Info contact_type : SCFormDE.ContactTypes) {
				if(scclist_grouped.containsKey(contact_type.id)) {
					ContactTypeRecord ctrec = ctmodel.get(contact_type.id);

					ArrayList<SCContactRecord> clist = scclist_grouped.get(contact_type.id);
					Collections.sort(clist, new Comparator<SCContactRecord> (){
						public int compare(SCContactRecord a, SCContactRecord b) {
							if (a.getRank() > b.getRank()) // We are comparing based on rank id 
								return 1; 
							return 0;
						}
					});
					String cliststr = "";
					
					for(SCContactRecord sccrec : clist) {
						ContactRecord person = pmodel.get(sccrec.contact_id);
						ContactRankRecord rank = crmodel.get(sccrec.contact_rank_id);
	
						cliststr += "<div class='contact_rank contact_"+rank.name+"'>";
						cliststr += StringEscapeUtils.escapeHtml(person.name);
						cliststr += "</div>";
					}
					ToolTip tip = new ToolTip(contact_type.desc);
					table.addRow(ctrec.name + " " + tip.render(), new HtmlView(cliststr));
				}
			}			
			if(context.getAuthorization().allows("admin")) {
				table.addRow("Footprints ID", rec.footprints_id);
			}
			table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);
		
			if(show_edit_button) {
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
				table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), StaticConfig.getApplicationBase()+"/scedit?id=" + rec.id)));
				*/
				table.add(new HtmlView("<a class=\"btn\" href=\"scedit?id=" + rec.id + "\">Edit</a>"));

			}
		} catch (SQLException e) {
			return new DivRepStaticContent(context.getPageRoot(), e.toString());
		}
		return new ViewWrapper(context.getPageRoot(), table);
	}
	
	private SideContentView createSideView(UserContext context)
	{
		SideContentView view = new SideContentView();
		
		//view.add(new HtmlView("<h3>Other Actions</h3>"));
		//view.add(new HtmlView("<div class=\"indent\">"));
		//view.add(new HtmlView("<div class=\"indent\">"));
		if(context.getAuthorization().isUser()) {
			view.add(new HtmlView("<p><a class=\"btn\" href=\"scedit\">Register New Support Center</a></p>"));
		}
		/*
		 *
		if(rec != null) {
			view.add(new HtmlView("<p><a href=\""+StaticConfig.getApplicationBase()+"/log?type=6&id="+rec.id+"\">View Update History</a></p>"));
		}
		*/
		//view.add(new HtmlView("</div>"));
		view.addContactLegend();
		return view;
	}
}
