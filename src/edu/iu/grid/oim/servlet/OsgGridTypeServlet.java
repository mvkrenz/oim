package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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

import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.Utils;

public class OsgGridTypeServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(OsgGridTypeServlet.class);  
	
    public OsgGridTypeServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		setAuth(request);
		auth.check("admin_osg_grid_type");

		try {
			//construct view
			MenuView menuview = createMenuView("admin");
			DivExRoot root = DivExRoot.getInstance(request);
			
			OsgGridTypeModel model = new OsgGridTypeModel(auth);
			Collection<OsgGridTypeRecord> ogts = model.getAll();
			ContentView contentview = createContentView(root, ogts);
			Page page = new Page(menuview, contentview, createSideView(root));
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(final DivExRoot root, Collection<OsgGridTypeRecord> ogts) 
		throws ServletException, SQLException
	{
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>OSG Grid Types</h1>"));
		
		for(OsgGridTypeRecord rec : ogts) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
			
			RecordTableView table = new RecordTableView();
			contentview.add(table);
			table.addRow("Description", rec.description);
			
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
			table.add(new DivExWrapper(new EditButtonDE(root, BaseURL()+"/osg_grid_type_edit?osg_grid_type_id=" + rec.id)));
			/*
			class DeleteDialogDE extends DialogDE
			{
				VORecord rec;
				public DeleteDialogDE(DivEx parent, VORecord _rec)
				{
					super(parent, "Delete " + _rec.name, "Are you sure you want to delete this Virtual Organization and associated contacts?");
					rec = _rec;
				}
				protected void onEvent(Event e) {
					if(e.getValue().compareTo("ok") == 0) {
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
		
			if(auth.allows("admin_vo")) {
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

	
	private SideContentView createSideView(DivExRoot root)
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends ButtonDE
		{
			String url;
			public NewButtonDE(DivEx parent, String _url)
			{
				super(parent, "Add New OSG Grid Type");
				url = _url;
			}
			protected void onEvent(Event e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(root, "osg_grid_type_edit"));
		
		
		
		return view;
	}
}
