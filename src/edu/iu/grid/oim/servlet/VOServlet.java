package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.webif.divex.ButtonDE;
import com.webif.divex.ClickEvent;
import com.webif.divex.DialogDE;
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;

import edu.iu.grid.oim.lib.Action;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
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

public class VOServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(VOServlet.class);  
	
    public VOServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		setAuth(request);
		
		//pull list of all vos
		ResultSet vos = null;
		VOModel model = new VOModel(con, auth);
		Set<Integer> accessible_ids = null;
		try {
			vos = model.getAll();
			
			//if user doesn't have admin_vo role, then pull vo_ids that user is set up as a contact
			if(!auth.allows(Action.admin_vo)) {
				accessible_ids = model.getAccessibleIDs();
			}
			
			//construct view
			MenuView menuview = createMenuView(baseURL(), "vo");
			DivExRoot root = DivExRoot.getInstance(request);
			ContentView contentview = createContentView(root, vos, accessible_ids);
			Page page = new Page(menuview, contentview, createSideView(root));
			
			response.getWriter().print(page.toHTML());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(final DivExRoot root, ResultSet vos, Set<Integer> accessible_ids) 
		throws ServletException, SQLException
	{
		ContentView contentview = new ContentView();	
		contentview.add("<h1>Virtual Organization</h1>");
		
		while(vos.next()) {
			
			VORecord rec = new VORecord(vos);
			contentview.add("<h2>"+Utils.strFilter(rec.name)+"</h2>");
			
			RecordTableView table = new RecordTableView();
			contentview.add(table);
			table.setClass("record_table");
			table.addRow("Long Name", rec.long_name);
			table.addRow("Description", rec.description);
			table.addRow("Primary URL", rec.primary_url);
			table.addRow("AUP URL", rec.aup_url);
			table.addRow("Membership Services URL", rec.membership_services_url);
			table.addRow("Purpose URL", rec.purpose_url);
			table.addRow("Support URL", rec.support_url);
			table.addRow("App Description", rec.app_description);
			table.addRow("Community", rec.community);
			table.addRow("Footprints ID", rec.footprints_id);
			table.addRow("Support Center", getSCName(rec.sc_id));
			table.addRow("Parent Virtual Organization", getParentVOName(rec.parent_vo_id));
			table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);
			
			//add edit / delete button
			if(accessible_ids == null || accessible_ids.contains(rec.id)) {	
				
				class EditButtonDE extends ButtonDE
				{
					String url;
					public EditButtonDE(DivEx parent, String _url)
					{
						super(parent, "Edit");
						url = _url;
					}
					protected void onClick(ClickEvent e) {
						redirect(url);
					}
				};
				
				table.add(new EditButtonDE(root, baseURL()+"/voedit?vo_id=" + rec.id));
				table.add(" or ");

				class DeleteDialogDE extends DialogDE
				{
					VORecord rec;
					public DeleteDialogDE(DivEx parent, VORecord _rec)
					{
						super(parent, "Delete " + _rec.name, "Are you sure you want to delete this Virtual Organization and associated contacts?");
						rec = _rec;
					}
					protected void onClick(ClickEvent e) {
						if(e.value.compareTo("ok") == 0) {
							VOModel model = new VOModel(con, auth);
							try {
								model.delete(rec.id);
								alert("Record Successfully removed.");
								redirect("vo");
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
			
				final DeleteDialogDE delete_dialog = new DeleteDialogDE(root, rec);
				table.add(delete_dialog);
			
				class DeleteButtonDE extends ButtonDE
				{
					public DeleteButtonDE(DivEx parent, String _name)
					{
						super(parent, "Delete");
						setStyle(ButtonDE.Style.ALINK);
					}
					protected void onClick(ClickEvent e) {
						delete_dialog.open();
					}
				};
				table.add(new DeleteButtonDE(root, rec.name));
			}
		}
		
		return contentview;
	}
	
	private String getParentVOName(Integer parent_vo_id) throws AuthorizationException, SQLException
	{
		if(parent_vo_id == null) return null;
		VOModel model = new VOModel(con, auth);
		VORecord parent = model.get(parent_vo_id);	
		if(parent == null) {
			return null;
		}
		return parent.name;
	}
	
	private String getSCName(Integer sc_id) throws AuthorizationException, SQLException
	{
		if(sc_id == null) return null;
		SCModel model = new SCModel(con, auth);
		SCRecord sc = model.get(sc_id);	
		if(sc == null) {
			return null;
		}
		return sc.name;
	}
	
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
			protected void onClick(ClickEvent e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(root, "voedit"));
		
		return view;
	}
}
