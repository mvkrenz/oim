package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.TogglerDE;
import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divex.ViewWrapperDE;

public class ContactServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ContactServlet.class);  
	
    public ContactServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		//setContext(request);
		
		//pull list of all SCs
		ContactModel model = new ContactModel(context);
		try {
		
			//construct view
			MenuView menuview = new MenuView(context, "contact");
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
		ContactModel model = new ContactModel(context);
		ArrayList<ContactRecord> contacts = model.getAllEditable();
		Collections.sort(contacts, new Comparator<ContactRecord> (){
			public int compare(ContactRecord a, ContactRecord b) {
				return a.getName().compareToIgnoreCase(b.getName()); // We are comparing based on name
			}
		});

		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>Contacts</h1>"));
		
		return createContentViewHelper (contentview, contacts);
	}

	protected ContentView createContentViewHelper (ContentView contentview, Collection<ContactRecord> contacts) 
		throws ServletException, SQLException
	{  
		DNModel dnmodel = new DNModel(context);

		for(ContactRecord rec : contacts) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));

			RecordTableView table = new RecordTableView();
			// TODO agopu: 10 is an arbitrary number -- perhaps we should make this a user preference? show/hide?
			if (contacts.size() > 10) {
				contentview.add(new TogglerDE(context.getDivExRoot(), new ViewWrapperDE(context.getDivExRoot(), table),false));
			} else {
				contentview.add(new TogglerDE(context.getDivExRoot(), new ViewWrapperDE(context.getDivExRoot(), table),true));
			}

			table.addRow("Primary Email", rec.primary_email);
			table.addRow("Secondary Email", rec.secondary_email);

			table.addRow("Primary Phone", rec.primary_phone);
			table.addRow("Primary Phone Ext", rec.primary_phone_ext);

			table.addRow("Secondary Phone", rec.secondary_phone);
			table.addRow("Secondary Phone Ext", rec.secondary_phone_ext);

			table.addRow("Address Line 1", rec.address_line_1);
			table.addRow("Address Line 2", rec.address_line_2);
			table.addRow("City", rec.city);
			table.addRow("State", rec.state);
			table.addRow("ZIP Code", rec.zipcode);
			table.addRow("Country", rec.country);

			table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);

			table.addRow("Person", rec.person);
			table.addRow("Instant Messaging", rec.im);

			String img = rec.photo_url;
			if(rec.photo_url == null || rec.photo_url.length() == 0) {
				img = Config.getApplicationBase() + "/images/noavatar.gif";
			} 
			table.addRow("Photo", new HtmlView("<img class=\"avatar\" src=\""+img+"\"/>"));
			table.addRow("Contact Preference", rec.contact_preference);	

			if(auth.allows("admin")) {
				String submitter_dn = null;
				if(rec.submitter_dn_id != null) {
					submitter_dn = rec.submitter_dn_id.toString();
				}
				table.addRow("Submitter DN", submitter_dn);
			}

			if(auth.allows("admin")) {
				String dn_string = "";
				DNRecord dnrec = dnmodel.getByContactID(rec.id);
				if(dnrec != null) {
					dn_string = dnrec.dn_string;
				}
				table.addRow("Associated DN", dn_string);		
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
			table.add(new DivExWrapper(new EditButtonDE(context.getDivExRoot(), Config.getApplicationBase()+"/contactedit?id=" + rec.id)));
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
				super(parent, "Add New Contact");
				url = _url;
			}
			protected void onEvent(Event e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getDivExRoot(), "contactedit"));		
		return view;
	}
}
