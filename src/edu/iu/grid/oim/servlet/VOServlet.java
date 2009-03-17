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
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

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
			contentview.add("<h2>"+valueFilter(rec.name)+"</h2>");
			contentview.add("<table class='record_table' summary='record detail'>");
			contentview.add("<tr><th>Long Name</th><td>"+valueFilter(rec.long_name)+"</td></tr>");
			contentview.add("<tr><th>Description</th><td>"+valueFilter(rec.description)+"</td></tr>");
			contentview.add("<tr><th>Primary URL</th><td>"+valueFilter(rec.primary_url)+"</td></tr>");
			contentview.add("<tr><th>AUP URL</th><td>"+valueFilter(rec.aup_url)+"</td></tr>");
			contentview.add("<tr><th>Membership Services URL</th><td>"+valueFilter(rec.membership_services_url)+"</td></tr>");
			contentview.add("<tr><th>Purpose URL</th><td>"+valueFilter(rec.purpose_url)+"</td></tr>");
			contentview.add("<tr><th>Support URL</th><td>"+valueFilter(rec.support_url)+"</td></tr>");
			contentview.add("<tr><th>App Description</th><td>"+valueFilter(rec.app_description)+"</td></tr>");
			contentview.add("<tr><th>Community</th><td>"+valueFilter(rec.community)+"</td></tr>");
			contentview.add("<tr><th>Footprints ID</th><td>"+valueFilter(rec.footprints_id)+"</td></tr>");

			contentview.add("<tr><th>Support Center</th><td>"+valueFilter(getSCName(rec.sc_id))+"</td></tr>");
			contentview.add("<tr><th>Parent Virtual Organization</th><td>"+valueFilter(getParentVOName(rec.parent_vo_id))+"</td></tr>");
			
			contentview.add("<tr><th>Active</th><td>"+boolFilter(rec.active)+"</td></tr>");
			contentview.add("<tr><th>Disable</th><td>"+boolFilter(rec.disable)+"</td></tr>");
			
			
			if(accessible_ids == null || accessible_ids.contains(rec.id)) {
				contentview.add("<tr><th></th><td>");	
			
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

				contentview.add(new EditButtonDE(root, baseURL()+"/voedit?vo_id=" + rec.id));
				contentview.add(new HtmlView(" or "));

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
			
				final DialogDE delete_dialog = new DeleteDialogDE(root, rec);
				contentview.add(delete_dialog);
			
				class DeleteButtonDE extends ButtonDE
				{
					int id;
					public DeleteButtonDE(DivEx parent, int _id, String _name)
					{
						super(parent, "Delete");
						id = _id;
						setStyle(ButtonDE.Style.ALINK);
					}
					protected void onClick(ClickEvent e) {
						delete_dialog.open();
					}
				};
				contentview.add(new DeleteButtonDE(root, rec.id, rec.name));
				contentview.add("</td></tr>");
			}
			contentview.add("</table>");
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
