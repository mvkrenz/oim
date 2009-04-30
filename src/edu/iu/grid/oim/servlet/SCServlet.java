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
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;
import com.webif.divex.TogglerDE;

import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
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
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.TableView;
import edu.iu.grid.oim.view.Utils;
import edu.iu.grid.oim.view.TableView.Row;
import edu.iu.grid.oim.view.divex.form.ViewWrapperDE;

public class SCServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(SCServlet.class);  
	
    public SCServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		setContext(request);
		auth.check("edit_my_sc");
		
		try {		
			//construct view
			MenuView menuview = new MenuView(context, "sc");
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
		SCModel model = new SCModel(context);
		Collection<SCRecord> scs = model.getAllEditable();
		
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>Support Centers</h1>"));
	
		for(SCRecord rec : scs) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));

			RecordTableView table = new RecordTableView();
			contentview.add(new TogglerDE(context.getDivExRoot(), new ViewWrapperDE(context.getDivExRoot(), table)));
			//contentview.add(table);

		 	table.addRow("Long Name", rec.long_name);
			table.addRow("Description", rec.description);
			table.addRow("Community", rec.community);

			table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);
			if(auth.allows("admin")) {
				table.addRow("Footprints ID", rec.footprints_id);
			}

			ContactTypeModel ctmodel = new ContactTypeModel(context);
			ContactRankModel crmodel = new ContactRankModel(context);
			ContactModel pmodel = new ContactModel(context);
			
			//contacts (only shows contacts that are filled out)
			SCContactModel sccmodel = new SCContactModel(context);
			ArrayList<SCContactRecord> scclist = sccmodel.getBySCID(rec.id);
			HashMap<Integer, ArrayList<SCContactRecord>> scclist_grouped = sccmodel.groupByContactTypeID(scclist);
			for(Integer type_id : scclist_grouped.keySet()) {
				ArrayList<SCContactRecord> clist = scclist_grouped.get(type_id);
				ContactTypeRecord ctrec = ctmodel.get(type_id);
				
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
			table.add(new DivExWrapper(new EditButtonDE(context.getDivExRoot(), Config.getApplicationBase()+"/scedit?sc_id=" + rec.id)));
		}
		
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends ButtonDE
		{
			String url;
			public NewButtonDE(DivEx parent, String _url)
			{
				super(parent, "Add New Support Center");
				url = _url;
			}
			protected void onEvent(Event e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getDivExRoot(), "scedit"));
		view.add("About", new HtmlView("This page shows a list of Support Centers that you have access to edit."));		
		return view;
	}
}
