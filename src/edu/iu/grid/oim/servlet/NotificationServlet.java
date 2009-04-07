package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

import edu.iu.grid.oim.notification.NotificationBase;
import edu.iu.grid.oim.model.db.NotificationModel;
import edu.iu.grid.oim.model.db.record.NotificationRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;

public class NotificationServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(NotificationServlet.class);  
	
    public NotificationServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		setAuth(request);
		
		//pull list of all vos
		NotificationModel model = new NotificationModel(auth);
		Collection<NotificationRecord> notifications = null;
		try {
			
			notifications = model.getAll();
			
			//construct view
			MenuView menuview = createMenuView("notification");
			DivExRoot root = DivExRoot.getInstance(request);
			ContentView contentview = createContentView(root, notifications);
			Page page = new Page(menuview, contentview, createSideView(root));
			page.render(response.getWriter());				
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(final DivExRoot root, Collection<NotificationRecord> notifications) 
		throws ServletException, SQLException
	{
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>Notification</h1>"));
	
		for(RecordBase it : notifications) {
			NotificationRecord rec = (NotificationRecord)it;
			NotificationBase notification = rec.getNotification();
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(notification.getTitle())+"</h2>"));
			RecordTableView table = notification.createReadView(root, auth);
			contentview.add(table);
		
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

			table.add(new DivExWrapper(new EditButtonDE(root, BaseURL()+"/notificationedit?cpu_id=" + rec.id)));
			
			class DeleteButtonDE extends ButtonDE
			{
				private NotificationRecord rec;
				public DeleteButtonDE(DivEx parent, NotificationRecord _rec)
				{
					super(parent, "Delete");
					rec = _rec;
					setStyle(ButtonDE.Style.ALINK);
					setConfirm(true, rec.getTitle());
				}
				protected void onEvent(Event e) {
					NotificationModel model = new NotificationModel(auth);
					try {
						model.remove(rec);
						alert("Record Successfully removed.");
						redirect("notification");
					} catch (SQLException e1) {
						log.error(e1);
						alert(e1.getMessage());
					}
				}
			};
			table.add(new HtmlView(" or "));
			table.add(new DeleteButtonDE(root, rec));	
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
				super(parent, "Add New Notification");
				url = _url;
			}
			protected void onEvent(Event e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(root, "notificationedit"));
		
		return view;
	}
}
