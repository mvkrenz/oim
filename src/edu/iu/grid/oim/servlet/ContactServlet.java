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

import com.webif.divrep.Button;
import com.webif.divrep.DivRep;
import com.webif.divrep.Event;
import com.webif.divrep.Toggler;

import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.ViewWrapper;

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

		if(contacts.size() == 0) {
			contentview.add(new HtmlView("<p>You currently don't have any contacts that you are the submitter of.</p>"));
		}
		
		for(ContactRecord rec : contacts) {
			String image;
			if(rec.person == false) {
				image = "<img align=\"top\" src=\""+Config.getApplicationBase()+"/images/group.png\"/> ";
			} else {
				image = "";//"<img src=\""+Config.getApplicationBase()+"/images/user.png\"/> ";			
			}
			contentview.add(new HtmlView("<h2>"+image+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));

			RecordTableView table = new RecordTableView();
			// TODO agopu: 10 is an arbitrary number -- perhaps we should make this a user preference? show/hide?
			if (contacts.size() > 10) {
				contentview.add(new Toggler(context.getPageRoot(), new ViewWrapper(context.getPageRoot(), table),false));
			} else {
				contentview.add(new Toggler(context.getPageRoot(), new ViewWrapper(context.getPageRoot(), table),true));
			}

			table.addRow("Primary Email", new HtmlView("<a class=\"mailto\" href=\"mailto:"+rec.primary_email+"\">"+StringEscapeUtils.escapeHtml(rec.primary_email)+"</a>"));
			table.addRow("Secondary Email", rec.secondary_email);

			table.addRow("Primary Phone", rec.primary_phone);
			table.addRow("Primary Phone Ext", rec.primary_phone_ext);

			table.addRow("Secondary Phone", rec.secondary_phone);
			table.addRow("Secondary Phone Ext", rec.secondary_phone_ext);
			
			if(rec.person == false) {
				table.addRow("Personal Information", new HtmlView("(Not a personal contact)"));
			} else {
				RecordTableView personal_table = new RecordTableView("inner_table");
				table.addRow("Personal Information", personal_table);
	
				personal_table.addRow("Address Line 1", rec.address_line_1);
				personal_table.addRow("Address Line 2", rec.address_line_2);
				personal_table.addRow("City", rec.city);
				personal_table.addRow("State", rec.state);
				personal_table.addRow("ZIP Code", rec.zipcode);
				personal_table.addRow("Country", rec.country);
				personal_table.addRow("Instant Messaging", rec.im);
	
				String img = rec.photo_url;
				if(rec.photo_url == null || rec.photo_url.length() == 0) {
					img = Config.getApplicationBase() + "/images/noavatar.gif";
				} 
				personal_table.addRow("Photo", new HtmlView("<img class=\"avatar\" src=\""+img+"\"/>"));
				personal_table.addRow("Contact Preference", rec.contact_preference);	
			}

			table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);
			
			if(auth.allows("admin")) {
				String submitter_dn = null;
				if(rec.submitter_dn_id != null) {
					DNRecord dn = dnmodel.get(rec.submitter_dn_id);
					submitter_dn = dn.dn_string;
				}
				table.addRow("Submitter DN", submitter_dn);
			}

			if(auth.allows("admin")) {
				String dn_string = null;
				DNRecord dnrec = dnmodel.getByContactID(rec.id);
				if(dnrec != null) {
					dn_string = dnrec.dn_string;
				}
				table.addRow("Associated DN", dn_string);		
			}

			class EditButtonDE extends Button
			{
				String url;
				public EditButtonDE(DivRep parent, String _url)
				{
					super(parent, "Edit");
					url = _url;
				}
				protected void onEvent(Event e) {
					redirect(url);
				}
			};
			table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), Config.getApplicationBase()+"/contactedit?id=" + rec.id)));
		}
		return contentview;
	}	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends Button
		{
			String url;
			public NewButtonDE(DivRep parent, String _url)
			{
				super(parent, "Add New Contact");
				url = _url;
			}
			protected void onEvent(Event e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getPageRoot(), "contactedit"));		
		return view;
	}
}
