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
import edu.iu.grid.oim.model.db.SCContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.ViewWrapper;

public class SCServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(SCServlet.class);  
	
    public SCServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		//setContext(request);
		auth.check("edit_my_sc");
		
		try {		
			//construct view
			MenuView menuview = new MenuView(context, "sc");
			ContentView contentview = createContentView();
			Page page = new Page(context, menuview, contentview, createSideView());
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView() 
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
		contentview.add(new HtmlView("<h1>Support Centers I am authorized to edit</h1>"));
		if(editable_scs.size() == 0) {
			contentview.add(new HtmlView("<p>You currently are not listed as a contact of any contact type on any support center - therefore you are not authorized to edit any SCs.</p>"));
		}
		for(SCRecord rec : editable_scs) {
			String name = rec.name;
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
			contentview.add(showSC(rec, true)); //true = show edit button
		}
		
		if(readonly_scs.size() != 0) {
			contentview.add(new HtmlView("<br/><h1>Read-Only Support Centers</h1>"));
			contentview.add(new HtmlView("<p>Following are the currently registered support centers on OIM - you do not have edit access on these records.</p>"));
	
			for(SCRecord rec : readonly_scs) {
				String name = rec.name;
				contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
				contentview.add(showSC(rec, false)); //false = no edit button
			}
		}
		
		return contentview;
	}
	
	private DivRepToggler showSC(final SCRecord rec, final boolean show_edit_button)
	{
		DivRepToggler toggler = new DivRepToggler(context.getPageRoot()) {
			public DivRep createContent() {
				RecordTableView table = new RecordTableView();
				try {			
				 	table.addRow("Long Name", rec.long_name);
					table.addRow("Description", rec.description);
					table.addRow("Community", rec.community);
		
					ContactTypeModel ctmodel = new ContactTypeModel(context);
					ContactRankModel crmodel = new ContactRankModel(context);
					ContactModel pmodel = new ContactModel(context);
					
					//contacts (only shows contacts that are filled out)
					SCContactModel sccmodel = new SCContactModel(context);
					ArrayList<SCContactRecord> scclist = sccmodel.getBySCID(rec.id);
					HashMap<Integer, ArrayList<SCContactRecord>> scclist_grouped = sccmodel.groupByContactTypeID(scclist);
					for(Integer type_id : scclist_grouped.keySet()) {
						ContactTypeRecord ctrec = ctmodel.get(type_id);
		
						ArrayList<SCContactRecord> clist = scclist_grouped.get(type_id);
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
							cliststr += person.name;
							cliststr += "</div>";
						}
						
						table.addRow(ctrec.name, new HtmlView(cliststr));
					}			
					if(auth.allows("admin")) {
						table.addRow("Footprints ID", rec.footprints_id);
					}
					table.addRow("Active", rec.active);
					table.addRow("Disable", rec.disable);
				
					if(show_edit_button) {
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
					}
				} catch (SQLException e) {
					return new DivRepStaticContent(this, e.toString());
				}
				return new ViewWrapper(context.getPageRoot(), table);
			}
		};
		return toggler;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends DivRepButton
		{
			String url;
			public NewButtonDE(DivRep parent, String _url)
			{
				super(parent, "Add New Support Center");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};

		view.add("Operation", new NewButtonDE(context.getPageRoot(), "scedit"));
		//view.add("About", new HtmlView("This page shows a list of Support Centers that you have access to edit."));		
		view.addContactLegend();
		
		return view;
	}
}
